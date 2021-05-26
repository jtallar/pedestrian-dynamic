import numpy as np
import random
import math
import objects as obj

MAX_ITERATIONS = 100000

def check_superposition(part, row, col, matrix):
    for r in range(row - 1, row + 2):
        for c in range(col - 1, col + 2):
            cell_cur = matrix[r][c]
            while cell_cur:
                if part.border_distance(cell_cur.particle) <= 0:
                    return True
                cell_cur = cell_cur.next
    
    return False

def get_row_col(x, y, cell_width, M):
    cell_index = int(x / cell_width) + int(y / cell_width) * M
    return (int(cell_index / M + 1), int(cell_index % M + 1))

# Generate random particles
def particles(n, side, r):
    part_list = []
    
    # Matrix with list of particles for each cell to check collision
    M = int(side / (r * 2.1))
    cell_width = side / M
    head_matrix = np.full((M + 2, M + 2), None)

    # Create n particles
    total_k = 0
    (count, iterations) = (0, 0)
    while count < n and iterations < MAX_ITERATIONS:
        iterations += 1
        # Generate random x, y
        (x, y) = (random.uniform(r, side - r), random.uniform(r, side - r))
        # Row, Col go from 1 to M
        (row, col) = get_row_col(x, y, cell_width, M)
        part = obj.Particle(count + 1, x, y, 0.0, 0.0, r)

        # If superposition exists, skip and regenerate new particle
        if check_superposition(part, row, col, head_matrix):
            continue

        head_matrix[row][col] = obj.ParticleNode(part, head_matrix[row][col])
        part_list.append(part)
        
        count += 1

    print(f'Generated {count} particles.')

    return part_list

#Generate dynamic and static files, reading N and L from arg
def data_files(side, particles, dynamic_filename):
    dynamic_file = open(dynamic_filename, "w")
    dynamic_file.write('0')       # dynamic time

    for p in particles:
        # Write dynamic file
        # TODO: Check that 7E precision is OK for this system
        dynamic_file.write('\n%.7E %.7E %.7E %.7E' % (p.x, p.y, p.vx, p.vy))

    dynamic_file.write('\n*')       # dynamic separator

    dynamic_file.close()
