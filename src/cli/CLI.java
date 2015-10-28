package cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileInputStream;

import java.util.Set;

/**
 * <h2>Command line interface</h2>
 *
 */
public class CLI {
	/**
	 * A reader that helps reading input from System.in
	 */
	private BufferedReader br;
	/**
	 * A local controller that handles some special commands like "exit"
	 */
	private CLC controller;
	/**
	 * The inner controller that handles all other commands.
	 */
	private CLC innerController;
	/**
	 * A boolean that determines whether the CLI should stop waiting for user input
	 */
	private boolean stopped = false;
	
	/**
	 * Initializes a new Command Line Interface. This interface holds a controller
	 * to handle commands.
	 * @param controller The controller that handles user commands
	 * @param args The list of arguments which are still to be evaluated
	 */
	public CLI(CLC controller, String... args) {
		br = new BufferedReader(new InputStreamReader(System.in));
		this.innerController = controller;
		this.controller = new CLIController();
		try {
			if(args.length > 0) {
				String[] scriptArgs = new String[args.length + 1];
				scriptArgs[0] = "run";
				for(int i = 0; i < args.length; i++) {
					scriptArgs[i+1] = args[i];
				}
				ArgSet scripts = new ArgSet(scriptArgs);
				this.controller.handle(scripts);
			}
			if(!stopped) {
				readLoop();
			}
			System.exit(0);
		} catch (IOException e) {
			System.err.println("[CLI] An error occurred while reading input from the command line.");
			e.printStackTrace();
		}
	}
	
	/**
	 * Read a line of input from the console and process it.
	 * Checks if stopped is true. In this case the loop stops.
	 * @throws IOException
	 */
	private void readLoop() throws IOException {
		String s;
		do {
			System.out.print("[Scheduling sim] $ ");
			s = br.readLine();
			if(s == null) return;
			controller.handle(new ArgSet(s));
		} while(!stopped);
	}
	
	/**
	 * This controller catches all commands that are meant to interact with this wrapper 
	 * rather than with the contained controller. 
	 *
	 */
	private class CLIController extends CLC {
		public CLIController() {
			Action exitAction = new Action() {
				@Override
				public void execute(ArgSet args) {
					stopped = true;
				}
			};
			
			Action debugAction = new Action() {
				@Override
				public void execute(ArgSet args) {
					int i = args.fetchInteger();
					Debugger.setLevel(i);
				}
			};

			Action debugTrackAddAction = new Action() {
				@Override
				public void execute(ArgSet args) {
					String tag = args.pop();
					Debugger.trackAdd(tag);
				}
			};

			Action debugTrackRemoveAction = new Action() {
				@Override
				public void execute(ArgSet args) {
					String tag = args.pop();
					Debugger.trackRemove(tag);
				}
			};

			Action debugTrackListAction = new Action() {
				@Override
				public void execute(ArgSet args) {
					Set<String> tags = Debugger.seenTags();
					System.out.println("Seen tags:");
					for(String s: tags) {
						System.out.println("\t " + s);
					}
				}
			};

			Action scriptAction = new Action() {
				@Override
				public void execute(ArgSet args) {
					while(args.hasArg()) {
						String path = args.pop();
						try {
							File f = new File(path);
							BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
							String s;
							do {
								s = br.readLine();
								if(s != null) {
									controller.handle(new ArgSet(s));
								}
							} while(s != null);
							br.close();
						} catch(FileNotFoundException e) {
							System.out.println("[CommandLineInterface] The file " + path + " does not exist.");
						} catch(IOException e) {
							System.out.println("[CommandLineInterface] An error occurred while reading from file " + path);
							e.printStackTrace();
							return;
						}
					}

				}
			};

			Action echoAction = new Action() {
				@Override
				public void execute(ArgSet args) {
					if(args.hasStringArg()) {
						System.out.println(args.fetchString());
					} else if(args.hasArg()) {
						System.out.println(args.pop());
					} else {
						System.out.println("[CommandLineInterface] the command \"echo\" requires an argument enclosed by quotation marks");
					}
				}
			};
			
			Action innerAction = new CommandAction(innerController);
			
			mapCommand("exit", exitAction);
			mapCommand("quit", exitAction);
			getContext("debug").mapCommand("level", debugAction);
			getContext("debug").getContext("track").mapCommand("add", debugTrackAddAction);
			getContext("debug").getContext("track").mapCommand("remove", debugTrackRemoveAction);
			getContext("debug").getContext("track").mapCommand("list", debugTrackListAction);
			mapAbbreviation('r', scriptAction);
			mapCommand("run", scriptAction);
			mapCommand("echo", echoAction);
			setDefaultAction(innerAction);
		}
	}
}
