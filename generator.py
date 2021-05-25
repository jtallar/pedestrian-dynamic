import sys
import json
import genLib as gen
import utils

# Read params from config.json
with open("config.json") as file:
    config = json.load(file)

# If provided, overwrite config params
if len(sys.argv) >= 2:
    config["dynamic_file"] = sys.argv[1]
if len(sys.argv) >= 3:
    config["N"] = sys.argv[2]

dynamic_filename = utils.read_config_param(
    config, "dynamic_file", lambda el : el, lambda el : True)

N = utils.read_config_param(
    config, "N", lambda el : int(el), lambda el : el > 0)
L = utils.read_config_param(
    config, "L", lambda el : float(el), lambda el : el > 0)
rmin = utils.read_config_param(
    config, "rmin", lambda el : float(el), lambda el : el >= 0)

# Random seed configuration
use_seed = utils.read_config_param(
    config, "use_seed", lambda el : bool(el), lambda el : True)
if use_seed:
    utils.set_random_seed(utils.read_config_param(
        config, "seed", lambda el : el, lambda el : True))

particles = gen.particles(N, L, rmin)
while len(particles) != N:
    print(f'Could only fit {len(particles)} particles, trying again...')
    particles = gen.particles(N, L, rmin)

gen.data_files(L, particles, dynamic_filename)
print(f'Generated file: {dynamic_filename}')