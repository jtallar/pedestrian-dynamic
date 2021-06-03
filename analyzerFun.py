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

def analyze_avg(x_superlist, y_superlist, d, w, plot_boolean):
    x_avg_list = [sts.mean(k) for k in zip(*x_superlist)]
    x_err_list = [sts.stdev(k) for k in zip(*x_superlist)]

    # de window a size 
    # if time_list[-1]-time_list[-2] != 0:      # TODO: QUE PASA SI LA RESTA DA 0 
    # w = 10
    q_list = []
    for i in range(w,len(x_avg_list)):
        q_list.append(w/(x_avg_list[i]-x_avg_list[i-w]))         # Caudal

    # else: q_list.append(1)              
    # x_avg_list = sts.mean(x_superlist)
    if plot_boolean:
        # Initialize plotting
        utils.init_plotter()
        # Plot Salientes = f(t)
        utils.plot_values(
             y_superlist[0][w:], 'particulas salientes', 
             q_list, 'caudal',
             sci_y=False, precision=0
         )
        utils.plot_error_bars_x(x_avg_list,"tiempo", y_superlist[0],"particulas salientes", x_err_list)
        # Hold execution
        # utils.hold_execution()
    return x_avg_list, y_superlist[0], x_err_list, q_list

def get_radius_array(dynamic_file, min_n, max_n):
    dynamic_file = open(dynamic_file, "r")

    it_sum_rad, tot_sum_rad = 0, 0
    it_len, tot_len = 0, 0
    restart = True
    for linenum, line in enumerate(dynamic_file):
        if restart:
            restart = False
            continue
        if "*" == line.rstrip():
            restart = True
            if it_len >= min_n and it_len <= max_n:
                tot_sum_rad += it_sum_rad / it_len
                tot_len += 1
            it_sum_rad, it_len = 0, 0
            continue
        
        it_sum_rad += float(line.rstrip().split(' ')[5])
        it_len += 1

    return tot_sum_rad / tot_len