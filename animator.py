import sys
import json
import utils
import objects as obj

BLACK = ' 0 0 0'
WHITE = ' 255 255 255'
GREEN = ' 0 255 0'
RED = ' 255 0 0'
C = '1e-18 '

def write_corners(ovito_file, N, Lx, Ly):
    corners = str(N+4)+'\n\n'+C+'0 0 0'+WHITE+'\n'+C+'0 '+str(Ly)+' 0'+WHITE+'\n'+C+str(Lx)+' 0 0'+WHITE+'\n'+C+str(Lx)+' '+str(Ly)+' 0'+WHITE+'\n'
    ovito_file.write(corners)

def get_ovito_line(r, x, y, p_id, pos):
    if pos:
        return str(r)+' '+str(x)+' '+str(y)+' '+str(p_id)+RED+'\n'
    return str(r)+' '+str(x)+' '+str(y)+' '+str(p_id)+BLACK+'\n'

# Read params from config.json
with open("config.json") as file:
    config = json.load(file)

if "rad" not in config:
    invalid_param("rad")

if len(sys.argv) == 2:
    dynamic_filename = sys.argv[1]
else:
    dynamic_filename = utils.read_config_param(
        config, "dynamic_file", lambda el : el, lambda el : True)
animation_filename = utils.read_config_param(
    config, "animation_file", lambda el : el, lambda el : True)
delta_t = utils.read_config_param(
    config, "delta_t_anim", lambda el : float(el), lambda el : el > 0)
# There are (N x N + 1) particles
N = utils.read_config_param(
    config["rad"], "N", lambda el : int(el), lambda el : el > 0)
part_count = N * N + 1
# Area size is (16 x D; 15 x D)
D = utils.read_config_param(
    config["rad"], "D", lambda el : float(el), lambda el : el > 0)
Lx, Ly = 16 * D, 15 * D
radius = 0.1 * D
# Read particle charge
Q = utils.read_config_param(
    config["rad"], "Q", lambda el : float(el), lambda el : el > 0)

# Build static particle list
static_particles = []
for i in range(N):
    for j in range(N):
        static_particles.append(obj.Particle(
                i * N + j, 
                (i + 1) * D,
                j * D,
                q=Q if (i + j) % 2 == 0 else -Q
            ))

ovito_file = open(animation_filename, "w")
dynamic_file = open(dynamic_filename, "r")

restart = True
target_time = 0
for linenum, line in enumerate(dynamic_file):
    if restart:
        time = float(line.rstrip())
        if time >= target_time:
            write_corners(ovito_file, part_count, Lx, Ly)
        restart = False
        p_id = 0
        continue
    if "*" == line.rstrip():
        restart = True
        if time >= target_time:
            target_time += delta_t
            if time >= target_time:
                print('Delta t is too small, there were no events in a gap! Exiting...')
                sys.exit(1)
            line = ''
            for p in static_particles:
                line += get_ovito_line(radius / 2, p.x, p.y, p.id, p.q > 0)
            ovito_file.write(line)
        continue
    
    if time >= target_time:
        line_vec = line.rstrip().split(' ') # x y vx vy
        (x,y,r) = (line_vec[0], line_vec[1], radius)
        (vx,vy) = (float(line_vec[2]), float(line_vec[3]))
        ovito_file.write(get_ovito_line(r, x, y, 0, True))

print(f'Generated {animation_filename}')

dynamic_file.close()
ovito_file.close()