# Pedestrian Dynamic - Contractile Particle Method

## What to Install
- `python3 -m pip install numpy`
- `python3 -m pip install matplotlib`
- Download and install OVITO from: https://www.ovito.org/
### Versions
`python 3.8`

## Configuration
Everything is configured by modifying `config.json`. Available configuration keys are:
   - `dynamic_file`: dynamic file filepath
   - `exit_file`: exit file filepath por all exit times
   - `animation_file`: animation file filepath
   - `N`: number of pedestrians in the room
   - `L`: room side, room will be of L x L
   - `d`: exit width
   - `rmin`: minimum pedestrian radius
   - `rmax`: maximum pedestrian radius
   - `vdmax`: maximum desired velocity
   - `tau`: time taken for a particle to reach its maximum radius
   - `beta`: velocity formula exponent
   - `use_seed`: if true use fixed seed, if false use nanoseconds
   - `seed`: fixed seed value
   - `dt_print_mult`: integer timestep multiplier for simulation prints to file. dt_print = dt * dt_print_mult
   - `dt_anim_mult`: integer timestep multiplier for animation prints to file. dt_anim = dt * dt_print_mult * dt_anim_mult
   - `plot`: determines whether to plot or not single analysis, must be true or false

## Particle generator
To generate initial particle positions by creating `dynamic_file`. 
Generates N particles with random positions, rmin and 0 velocity. If N cannot be reached in a number of iterations, resets and tries again.
Run `python3 generator.py [dynamic_filename] [N]`.
If provided, `dynamic_filename` and `N` parameters overwrite config params

## Simulation
To generate executable and run pedestrian dynamic simulation
1. Run `./prepare.sh` in root to generate executables (only required once).
2. Run `./target/tp5-simu-1.0/pedestrian-dynamic.sh -Dn=N -Dd=d -Ddynamic=dynamic.txt -Dexit=exit.txt`. Parameters from `config.json` can be overwritten by using `n`, `d`, `dynamic` and `exit` properties.

Output will be appended to `dynamic_file`, showing time and particle position and velocity for each timestep dt_print. Particle exit times will be printed to `exit_file` with a precision of dt.

## Animation Tool
Generates `simulation_file` using information from `dynamic_file`.
Run `python3 animator.py [dynamic_file] [N] [d]`, using the parameters from `config.json`. If provided, params overwrite `dynamic_file` and `d` from config.

To view the animation, you must open `simulation_file` with Ovito:
`./bin/ovito simulation_file`. 

Particle color shows particle radius, where particles with `rmin` are red (many contacts) and particles with `rmax` are black (fewer contacts).

### Column Mapping 
Configure the file column mapping as follows:
   - Column 1 - Radius
   - Column 2 - Position - X
   - Column 3 - Position - Y
   - Column 4 - Particle Identifier
   - Column 5 - Color - R
   - Column 6 - Color - G
   - Column 7 - Color - B

## Analysis Tools

### analyzerSame.py
Generate plots and metrics given simulation files as input.
Run `python3 analyzerSame.py [file_1 file_2 ...]`, using parameters from `config.json`.

If one or more filenames are provided, analysis will be performed individually and then condensed for multiple simulations. This can be used to provide multiple repetitions for a given set of parameters. If plot is false, then no graphs are plotted.

Plots shown are:
- n = f(t), n: number of particles that left

If multiple files are provided, some more plots are shown:
- Discharge = f(n), taking dn=10
- n = f(t) for each simulation file
- t = f(n) for each simulation file
- n = f(t avg) with horizontal error bars for each value
- t avg = f(n) with vertical error bars for each value

### same.sh
This script can be used to run `pedestrian-dynamic` simulation multiple times with the same params, given N, d and a number of repetitions.
`./same.sh N d rep`

The script runs `rep` simulations with the parameters `N` and `d`. Then, it runs `analyzerSame.py` with the output datafiles as parameters.

### analyzerSame.py
Generate plots and metrics given multiple simulation files as input.
Run `python3 analyzerMult.py [file_1 file_2 ...]`. Filenames must be in the format `name-N-...`.

Aanalysis will be performed individually and then condensed for multiple simulations. This can be used to provide multiple repetitions for a multiple sets of parameters. If plot is false, then no graphs are plotted.

Plots shown for each (N,d) pair are:
- Discharge = f(n), taking dn=10
- n = f(t avg) with horizontal error bars for each value
- t avg = f(n) with vertical error bars for each value

Plots shown as a comparison between sets of parameters are
- Discharge = f(n) for each (N,d) pair
- Discharge = f(t) for each (N,d) pair
- Average discharge in stationary sector for each d value with vertical error bars
- Average discharge in stationary sector adjusted via Beverloo's Law (with corresponding error plot)
- Average discharge in stationary sector adjusted via Beverloo's Law with the vertical error bars and a continous adjustment (with corresponding error plot)


### multiple.sh
This script can be used to run `pedestrian-dynamic` simulation multiple times with different params, given a number of repetitions.
`./multiple.sh rep`

The script runs `rep` simulations for each of the following (N;d) params: (200;1.2), (260;1.8), (320;2.4), (380;3.0). Then, it runs `analyzerMult.py` with the output datafiles as parameters.