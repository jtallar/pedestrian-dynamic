from os import name
import sys
import json
import utils
import analyzerFun as anl
import statistics as sts

# Read out exit filename param if provided
exit_files = None
if len(sys.argv) >= 2:
    exit_files = sys.argv[1:]

# Read params from config.json
with open("config.json") as file:
    config = json.load(file)

dynamic_filename = utils.read_config_param(
    config, "dynamic_file", lambda el : el, lambda el : True)
exit_filename = utils.read_config_param(
    config, "exit_file", lambda el : el, lambda el : True)
plot_boolean = utils.read_config_param(
    config, "plot", lambda el : bool(el), lambda el : True)

N = utils.read_config_param(
    config, "N", lambda el : float(el), lambda el : el > 0)
L = utils.read_config_param(
    config, "L", lambda el : float(el), lambda el : el > 0)
d = utils.read_config_param(
    config, "d", lambda el : float(el), lambda el : el > 0)

# Perform multiple plotting and analysis
x_superlist, y_superlist, legend_list = [], [], []
n_dict, t_dict = {}, {}
n_to_d = {200:1.2, 260:1.8, 320:2.4, 380:3.0}
# ={1.2, 1.8, 2.4, 3.0}m y N = {200, 260, 320, 380}
for filename in exit_files:
    # Expected filename format: exit-N-i.txt
    name_data = filename[:-4].split('-') # Take filename without .txt extension
    n = int(name_data[1])
    
    metric = anl.analyze_dload(filename, n, L, False)
    x_superlist.append(metric.time_list)
    y_superlist.append(metric.n_list)
    legend_list.append(name_data)
    
    if n not in n_dict:
        n_dict[n] = []
        t_dict[n] = []
    n_dict[n].append(metric.n_list)
    t_dict[n].append(metric.time_list)

q_mean, q_dev = [], []
q_superlist, time_superlist, d_list = [], [], []
W = 10
LEFT_PERC = 0.15
RIGHT_PERC = 0.85

for key in n_dict.keys():
    avg_x, avg_y, err_x, q_list = anl.analyze_avg(t_dict[key], n_dict[key], n_to_d[key], W, plot_boolean)
    q_mean.append(sts.mean(q_list[int(LEFT_PERC * key):int(RIGHT_PERC * key)]))
    q_dev.append(sts.stdev(q_list[int(LEFT_PERC * key):int(RIGHT_PERC * key)]))
    d_list.append(n_to_d[key])
    q_superlist.append(q_list)
    time_superlist.append(avg_y[W:])

# x_superlist.append(avg_x)
# y_superlist.append(avg_y)
# legend_list.append("promedio")

if plot_boolean:
    # Initialize plotting
    utils.init_plotter()

    # Plot outgoing particles f(t) + avg
    utils.plot_multiple_values(
        time_superlist,
        'particles',
        q_superlist,
        'caudal',
        legend_list,
        sci_y=False
    )

    utils.plot_error_bars(
        d_list,
        'd',
        q_mean, 
        'caudal',
        q_dev, 
        sci_y= False
    )

    # Hold execution
    utils.hold_execution() 


    # parsear el dynamic , tomar los radios y promediarlos entre el primer t y el ultimo