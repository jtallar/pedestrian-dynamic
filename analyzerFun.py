import utils
import objects as obj
import numpy as np
import statistics as sts

def analyze_dload(exit_file, N, d, plot_boolean):
    exit_file = open(exit_file, "r")
    
    n_vec = [0]
    time_list = [0]
    q_list = [0]
    
    for n, line in enumerate(exit_file):   
        time_list.append(float(line))
        n_vec.append(n)
        if time_list[-1]-time_list[-2] != 0:    
            q_list.append(1/(d*(time_list[-1]-time_list[-2])))         # Caudal
        else: q_list.append(1)              # TODO: ?
    # Close files
    exit_file.close()

    # print(f'times = {time_list}\n'
    #       f'particles = {n_vec}'
    #       f'caudal = {q_list}')
    
    # Plot values
    if plot_boolean:
        # Initialize plotting
        utils.init_plotter()
        
        # Plot Salientes = f(t)
        utils.plot_values(
            time_list, 'tiempo (s)', 
            n_vec, 'particulas que salieron',
            sci_x=True, precision=0
        )

        # utils.plot_values(
        #     time_list, 'tiempo (s)', 
        #     q_list, 'particulas que salieron',
        #     sci_x=True, precision=0
        # )

        # Hold execution
        utils.hold_execution()

    return obj.AnalysisDload(time_list, n_vec)

def analyze_avg(x_superlist, y_superlist, plot_boolean):
    x_avg_list = [sts.mean(k) for k in zip(*x_superlist)]
    x_err_list = [sts.stdev(k) for k in zip(*x_superlist)]

    # x_avg_list = sts.mean(x_superlist)
    if plot_boolean:
        # Initialize plotting
        utils.init_plotter()
        # Plot Salientes = f(t)
        # utils.plot_values(
        #     x_avg_list, 'tiempo promedio (s)', 
        #     y_superlist[0], 'particulas que salieron',
        #     sci_x=True, precision=0
        # )
        utils.plot_error_bars_x(x_avg_list,"time mean", y_superlist[0],"out particles", x_err_list)
        # Hold execution
        utils.hold_execution()
    return x_avg_list, y_superlist[0], x_err_list