# Damped Oscillator + Radiation Interaction

## What to Install
- `python3 -m pip install numpy`
- `python3 -m pip install matplotlib`
- Download and install OVITO from: https://www.ovito.org/
### Versions
`python 3.8`

## Configuration
Everything is configured by modifying `config.json`. Available configuration keys are:
   - `dynamic_file`: dynamic file filepath
   - `simulation_file`: animation file filepath
   - `osc`: configurations for damped oscillator
      - `algo`: name of the algorithm to use
      - `mass`: particle mass
      - `k`: coefficient of the restoring force
      - `gamma`: characteristic gamma quantity
      - `tf`: end time of simulation
      - `r0`: initial position
      - `A`: oscillator amplitude
   - `rad`: configurations for radiation interaction system
      - `algo`: name of the algorithm to use
      - `mass`: particle mass
      - `k`: Coulomb's constant
      - `N`: number of static particles per dimension (NxN matrix)
      - `D`: distance between static particles
      - `Q`: particle charge
      - `v0`: particle initial horizontal velocity
      - `use_seed`: if true use fixed seed, if false use nanoseconds
      - `seed`: fixed seed value
   - `delta_t_sim`: timestep between simulation measurements
   - `delta_t_print`: timestep between simulation prints to file
   - `delta_t_anim`: timestep between animation prints to file
   - `plot`: determines whether to plot or not single analysis, must be true or false

# Damped Oscillator

## Simulation
To generate executable and run damped oscillator simulation
1. Run `./prepare.sh` in root to generate executables (only required once).
2. Run `./target/tp4-simu-1.0/damped-osc.sh -Dalgo=algo -Ddt=dt`. Parameters from `config.json` can be overwritten by using `algo` and `dt` properties.

Output will be printed to `dynamic_file`, showing time and particle position and velocity for each timestep.

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

# Radiation Interaction

## Simulation
To generate executable and run damped oscillator simulation
1. Run `./prepare.sh` in root to generate executables (only required once).
2. Run `./target/tp4-simu-1.0/radiation-interaction.sh -Ddt=dt -Dv0=v0`. Parameters from `config.json` can be overwritten by using `dt` and `v0` properties.

Output will be printed to `dynamic_file`, showing time and particle position and velocity for each timestep.

## Animation Tool
Generates `simulation_file` using information from `dynamic_file`.
Run `python3 animator.py [dynamic_file]`, using the parameters from `config.json`. If provided, param overwrites `dynamic_file` from config.

To view the animation, you must open `simulation_file` with Ovito:
`./bin/ovito simulation_file`. 

Particle color shows whether charge is positive (red) or negative (black).

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