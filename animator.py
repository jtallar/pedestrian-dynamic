import sys
import json
import utils
import objects as obj

BLACK = ' 0 0 0'
WHITE = ' 255 255 255'
GREEN = ' 0 255 0'
RED = ' 255 0 0'
C = '1e-10 '
LINE_RAD = 2.5e-2

def get_line(x_start, y, length, color):
    step = LINE_RAD / 2
    x_it = x_start + LINE_RAD
    line = ''
    count = 0
    while x_it < x_start + length - LINE_RAD:
        line += str(LINE_RAD) + ' ' + str(x_it) + ' ' + str(y) + ' 0'+color+'\n'
        x_it += step
        count += 1
    return count, line

# Read params from config.json
with open("config.json") as file:
    config = json.load(file)

if len(sys.argv) >= 2:
    config["dynamic_file"] = sys.argv[1]
if len(sys.argv) >= 3:
    config["N"] = sys.argv[2]

dynamic_filename = utils.read_config_param(
    config, "dynamic_file", lambda el : el, lambda el : True)
animation_filename = utils.read_config_param(
    config, "animation_file", lambda el : el, lambda el : True)

N = utils.read_config_param(
    config, "N", lambda el : int(el), lambda el : el > 0)
L = utils.read_config_param(
    config, "L", lambda el : float(el), lambda el : el > 0)
d = utils.read_config_param(
    config, "d", lambda el : float(el), lambda el : el > 0)

rmin = utils.read_config_param(
    config, "rmin", lambda el : float(el), lambda el : el > 0)
rmax = utils.read_config_param(
    config, "rmax", lambda el : float(el), lambda el : el > 0)

delta_t_mult = utils.read_config_param(
    config, "dt_anim_mult", lambda el : int(el), lambda el : el > 0)

bot_left_count, bot_left_line = get_line(0, 0, 0.5 * (L - d), BLACK)
door_count, door_line = get_line(0.5 * (L - d + 0.2), 0, d - 0.2, GREEN)
bot_right_count, bot_right_line = get_line(0.5 * (L + d), 0, 0.5 * (L - d), BLACK)
exit_count, exit_line = get_line(0.5 * (L - 3), -10, 3, GREEN)

def write_corners(ovito_file, N, L, d, bot_margin):
    corners = str(N+4+door_count+exit_count+bot_left_count+bot_right_count)+'\n\n'+C+'0 ' + str(bot_margin) + ' 0'+WHITE+'\n'+C+'0 '+str(L)+' 0'+WHITE+'\n'+C+str(L)+' ' + str(bot_margin) + ' 0'+WHITE+'\n'+C+str(L)+' '+str(L)+' 0'+WHITE+'\n'
    ovito_file.write(corners)
    ovito_file.write(bot_left_line)
    ovito_file.write(door_line)
    ovito_file.write(bot_right_line)
    ovito_file.write(exit_line)

def get_ovito_line(r, x, y, p_id, rmin, rmax):
    return str(r)+' '+str(x)+' '+str(y)+' '+str(p_id)+' ' + str((rmax - r) / (rmax - rmin)) + ' 0 0' +'\n'

ovito_file = open(animation_filename, "w")
dynamic_file = open(dynamic_filename, "r")

restart = True
target_count = delta_t_mult
p_count, p_lines = 0, ''
for linenum, line in enumerate(dynamic_file):
    if restart:
        target_count += 1
        time = float(line.rstrip())
        restart = False
        p_id = 0
        continue
    if "*" == line.rstrip():
        restart = True
        if target_count >= delta_t_mult:
            target_count = 0
            write_corners(ovito_file, p_count, L, d, -10)
            ovito_file.write(p_lines)
            p_count, p_lines = 0, ''
        continue
    
    if target_count >= delta_t_mult:
        line_vec = line.rstrip().split(' ') # id x y vx vy r
        p_id = int(line_vec[0])
        (x,y,r) = (float(line_vec[1]), float(line_vec[2]), float(line_vec[5]))
        (vx,vy) = (float(line_vec[3]), float(line_vec[4]))
        p_lines += get_ovito_line(r, x, y, p_id, rmin, rmax)
        p_count += 1

print(f'Generated {animation_filename}')

dynamic_file.close()
ovito_file.close()