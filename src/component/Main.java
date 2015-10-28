package component;

import cli.CLI;
import cli.SchedulingController;

public class Main {

	public static void main(String... args) {
		SchedulingController sc = new SchedulingController();
		CLI cli = new CLI(sc, args);
	}
}