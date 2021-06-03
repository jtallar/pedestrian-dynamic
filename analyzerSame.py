from os import name
import sys
import json
import utils
import analyzerFun as anl

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

w = 10

if exit_files is None:
    # Perform one analysis only once
    anl.analyze_dload(exit_filename, N, d, plot_boolean)
else:
    # Perform multiple plotting and analysis
    x_superlist = []
    y_superlist = []
    legend_list = []
    for filename in exit_files:
        # Expected filename format: ALGO-dt.txt
        name_data = filename[:-4] # Take filename without .txt extension
        metric = anl.analyze_dload(filename, N, L, False)
        x_superlist.append(metric.time_list)
        y_superlist.append(metric.n_list)
        legend_list.append(name_data)
    # x_superlist.append(metric.time_vec)
    # y_superlist.append(metric.exact_sol)
    # legend_list.append("analítica")

    avg_x, avg_y, err_x, q_list = anl.analyze_avg(x_superlist, y_superlist, d, w, True)

    x_superlist.append(avg_x)
    y_superlist.append(avg_y)
    legend_list.append("promedio")
    if plot_boolean:
        # Initialize plotting
        utils.init_plotter()

        # Plot outgoing particles f(t) + avg
        utils.plot_multiple_values(
            x_superlist,
            'tiempo (s)',
            y_superlist,
            'particulas salientes',
            legend_list,
            sci_y=False
        )

        utils.plot_multiple_values(
            y_superlist,
            'particulas salientes',
            x_superlist,
            'tiempo (s)',
            legend_list,
            sci_y=False
        )

        # Hold execution
        utils.hold_execution()    
