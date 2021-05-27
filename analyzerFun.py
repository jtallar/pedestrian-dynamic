# import sys
# import json
import utils
# import math
import objects as obj


def analyze(static_filename, dynamic_filename, delta_t, delta_t_intercol, delta_v_mod, small_dcm_count, plot_boolean):
    dynamic_file = open(dynamic_filename, "r")

    static_file = open(static_filename, "r")
    N = int(static_file.readline())
    L = float(static_file.readline())

    restart = True
    (time, prev_time, p_id, in_box) = (0, 0, 0, 0)
    download = []

    for linenum, line in enumerate(dynamic_file):
        if restart:
            # Download curve - Sequentially adding N° of particles out of box
            download.append(N - in_box)
            prev_time = time
            time = float(line.rstrip())
            # Reset variables
            restart = False
            in_box = 0
            p_id = 0
            continue
        if "*" == line.rstrip():
            restart = True
            continue

        line_vec = line.rstrip().split(' ')
        # (id, x=0, y=0, vx=0, vy=0, r=0, m=0):
        part = obj.Particle(p_id, float(line_vec[0]), float(line_vec[1]), float(line_vec[2]), float(line_vec[3]), particle_radius[p_id], particle_mass[p_id])
        v_mod = part.get_v_mod()
        if part.y < 20:
            in_box += 1
        p_id += 1

    # Close files
    dynamic_file.close()
    static_file.close()

    # Initialize plotting
    utils.init_plotter()


#    a) Para vdmax = 2 m/s, d =1.2 m y N = 200, simular varios egresos. En cada caso graficar la curva de
#       descarga, es decir, el número de partículas que salieron en función del tiempo. Para ello se deberá
#       registrar los tiempos de salida de cada peatón con la mayor precisión disponible (dt, no dt2).

#    b) Promediar las distintas curvas del punto (a) para obtener una sola curva que indique el
#       comportamiento promedio del sistema para esos parámetros. Para ello tomar el número de
#       partículas salientes (n(t)) como variable independiente (eje horizontal) y promediar los tiempos (eje
#       vertical). Luego invertir los ejes para tener la curva de descarga n(t). Graficar las barras de error
#       horizontales. Analizar en qué rango de n el caudal (Q) es constante (Q = dn / dt).

#    c) Tomando vdmax = 2 m/s, realizar al menos 3 repeticiones para cada una de las simulaciones
#       variando d ={1.2, 1.8, 2.4, 3.0}m y N = {200, 260, 320, 380} partículas respectivamente (para cada
#       valor de d, solo un valor de N según le corresponda ordenadamente, por ej. a d =1.8 m le corresponde N =260).
#           Calcular el caudal en el intervalo donde el mismo es estacionario durante la
#       descarga, mostrando algunos ejemplos de estas evoluciones temporales.
#           Graficar el caudal medio en función del ancho de la salida d con barras de error.

#     d) Ajustar los caudales obtenidos para distintos anchos de salida con la Ley de Beverloo de medios
#       granulares, usando los "Conceptos de Regresiones" dados en la Teórica 0.