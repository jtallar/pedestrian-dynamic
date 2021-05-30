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
To generate executable and run damped oscillator simulation
1. Run `./prepare.sh` in root to generate executables (only required once).
2. Run `./target/tp5-simu-1.0/damped-osc.sh -Dn=N -Dd=d -Ddynamic=dynamic.txt -Dexit=exit.txt`. Parameters from `config.json` can be overwritten by using `n`, `d`, `dynamic` and `exit` properties.

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

### analysisOsc.py
Generate plots and metrics given a single simulation file as input.
Run `python3 analysisOsc.py [file_1 file_2 ...]`, using parameters from `config.json`.

If one or more filenames are provided, analysis will be performed individually and then condensed for multiple simulations. This can be used to provide one simulation file for each available algorithm. If plot is false, then no graphs are plotted.

Metrics calculated for each simulation are:
- ECM

Plots shown are:
- Analytic trajectory + estimated trajectory for each simulation file.

### multipleDtOsc.sh
This script can be used to run `damped-osc` simulation multiple times, given a starting timestep value, a step to increase dt each iteration and a maximum dt.
`./multipleDtOsc.sh dt_start dt_step dt_end`

The script runs three simulations for each available dt from `dt_start` to the highest `dt_start + K * dt_step` that is lower or equal than `dt_end` using Verlet, Beeman and Gear Predictor-Corrector 5 respectively. Then, it runs `analysisOsc.py` with the three output datafiles as parameters.

### aux_analysisOscDelta.py
Contains obtained values using the previously mentioned script. It is used to plot ECM = f(dt) for the three algorithms at once. Values should be copied manually to the corresponding lists.

## Analysis Tools

### analysisRad.py
Generate plots and metrics given a single simulation file as input.
Run `python3 analysisRad.py [file_1 file_2 ...]`, using parameters from `config.json`.

If one or more filenames are provided, analysis will be performed individually and then condensed for multiple simulations. This can be used to provide one simulation file for each timestep or v0, or to use multiple repetitions for each value. If plot is false, then no graphs are plotted.

Metrics calculated for each simulation are:
- Total trajectory length (L)
- Sum of total energy difference (sum of |ET(0)-ET(t)|)
- Average of total energy difference
- Ending motive

Plots shown are:
- |ET(t=0) - ET(t>0)| = f(t) with log scale
- L = f(t)
- Particle trajectory

If multiple files are provided, some more metrics are calculated are:
- Average total energy difference for all files (mean±stdev)
- Average total trajectory length for all files (mean±stdev)
- Average number of steps for all files (mean±stdev)
- Ending count for each possible ending motive

And some more plots are shown:
- |ET(t=0) - ET(t>0)| = f(t) with log scale for each dt
- Average |ET(t=0) - ET(t>0)| = f(t) with error bars for each dt
- Multiple particle trajectories (both with dt and v0 in legend)

### multipleDtRad.sh
This script can be used to run simulation multiple times, given a starting timestep value, a step to increase dt each iteration, a maximum dt and a number of repetitions.
`./multipleDtRad.sh dt_start dt_step dt_end rep`

The script runs `rep` simulations for each available dt from `dt_start` to the highest `dt_start + K * dt_step` that is lower or equal than `dt_end`. Initial velocity is set to `10e3`. Then, it runs `analysisRad.py` with the `rep` output datafiles as parameters for each dt.

### multipleV0.sh
This script can be used to run `radiation-interaction` simulation multiple times, given a step to increase v0 each iteration and a number of repetitions.
`./multipleV0.sh v0_step rep`

The script runs `rep` simulations for each available v0 from `10e3` to the highest `10e3 + K * v0_step` that is lower or equal than `100e3`. Timestep is set to `1e-16`. Then, it runs `analysisRad.py` with the `rep` output datafiles as parameters for each v0.

### aux_analysisRadVel.py
Contains obtained values using the previously mentioned script. It is used to plot L = f(V0) for different initial velocities at once and a probability distribution for each ending motive. Values should be copied manually to the corresponding lists.