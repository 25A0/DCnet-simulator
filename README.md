This is the simulator that was used to benchmark the performance of footprint scheduling, 
Pfitzmann's algorithm, and the Herbivore implementation of Chaum's map-reservation algorithm.

Run `make` to compile the simulator. This requires Java 7 (or newer).

After that, run `./simulation.sh` to start the interactive simulator.
The folder `scripts` contains various benchmark scripts that run simulations automatically.
In order to execute a script (e.g. the script `scripts/benchmarks/activity`), enter
	`run scripts/benchmarks/activity`
in the interactive simulator or execute 
	`./simulation.sh scripts/benchmarks/activity`
from the shell.

In order to save the results of a simulation, enter `write` in the interactive simulator once the 
simulation is finished. 
The results will be written to a .csv file in the folder `benchmark-data`. If this file is
already present, the new results will be appended to the existing data.

The plots in the paper can be reproduced with the provided R script `process.R`. This requires
the ggplot2 library. By default, all plots are commented out.

Browsing the predefined scripts gives a good overview of the available options for each simulation.
Below is a quick guideline for running custom simulations:

For footprint scheduling:
	`footprint S B P A C`
	where
		S is the number of slots that can be reserved
		B is the number of bits per footprint
		P is a list containing the number of participants in the simulated networks
		A is the activity rate of all participants as a floating point value between 0 and 1
		C is a boolean that determines whether the discussion should stop once the schedule converged.
			Note that this is only possible in a simulation, not in a real-life scenario where
			footprints are kept secret in general.
	
	So, a simulation of footprint scheduling could look like this:
	`footprint 16 8 [100 200 500 1000 2000 5000 10000] 0.01 false`

For Pfitzmann's algorithm:
	`pfitzmann i P A`
	where
		i is the number of available slots per participant (as discussed in Appendix A)
		P is a list containing the number of participants in the simulated networks
		A is the activity rate of all participants as a floating point value between 0 and 1

	So, a simulation of Pfitzmann's algorithm could look like this:
	`pfitzmann 32 [100 200 500 1000 2000 5000 10000] 0.01`

For the Chaum's map reservations:
	`chaum P A`
	where
		P is a list containing the number of participants in the simulated networks
		A is the activity rate of all participants as a floating point value between 0 and 1

	So, this is how a simulation of Chaum's reservation map could look like:
	`chaum [100 200 500 1000 2000 5000 10000] 0.01`
