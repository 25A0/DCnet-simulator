package cli;

import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Map;

/**
 * <h2>Command line controller</h2>
 *
 */
public class CLC {
	private Map<String, Action> commandMap;
	private Map<Character, Action> abbrMap;
	private Map<String, Action> optionMap;
	private Map<String, CLC> contextMap;
	private Action defaultAction, rootAction;
	
	/**
	 * The action that will be executed in case that no
	 * specific action has been registered for a given command
	 */
	protected final Action unknownCommandAction = new Action() {
		@Override
		public void execute(ArgSet args) {
			System.out.println("The command " + args.peek() + " is unknown.");
		}
	};

	/**
	 * The action that will be executed by default when calling the
	 * root of this controller
	 */
	protected final Action defaultRootAction = new Action() {
		@Override
		public void execute(ArgSet args) {
			//
		}
	};
	
	/**
	 * Initializes local fields
	 */
	public CLC() {
		commandMap = new HashMap<String, Action>();
		abbrMap = new HashMap<Character, Action>();
		optionMap = new HashMap<String, Action>();
		contextMap = new HashMap<String, CLC>();
		defaultAction = unknownCommandAction;
		rootAction = defaultRootAction;
	}
	
	/**
	 * This function is used to define the behaviour of the command line control.
	 * 
	 * @param command The command that triggers the action
	 * @param a The action that is executed when the given command occurs 
	 */
	public final void mapCommand(String command, Action a) {
		if(commandMap.containsKey(command)) 
			System.err.println("[CLC] Command " + command + " has already been registered.");
		else if(a == null) {
			System.err.println("[CLC] Command " + command + " can not be linked to a null Action.");
		} else if(contextMap.containsKey(command)) {
			contextMap.get(command).setRootAction(a);
		} else {	
			commandMap.put(command, a);
		}
	}

	public final void mapAbbreviation(Character abbr, Action a) {
		if(abbrMap.containsKey(abbr)) {
			System.err.println("[CLC] The abbreviation -" + abbr + " has already been registered.");
		} else if(!Character.isLetter(abbr)) {
			System.err.println("[CLC] The abbreviation -" + abbr + " is not alphabetic.");
		} else if(a == null) {
			System.err.println("[CLC] The abbreviation -" + abbr + " can not be linked to a null Action.");
		} else {
			abbrMap.put(abbr, a);
		}
	}

	public final void mapOption(String option, Action a) {
		if(optionMap.containsKey(option)) {
			System.err.println("[CLC] The option --" + option + " has already been registered.");
		} else if(a == null) {
			System.err.println("[CLC] The option --" + option + " can not be linked to a null Action.");
		} else {
			optionMap.put(option, a);
		}
	}
	
	public final void setContext(String context, CLC contextController) {
		if(contextMap.containsKey(context)) {
			System.out.println("[CLC] Warning: There is already a context controller associated with the context "+  context);
		}
		contextMap.put(context, contextController);
	}

	public final CLC getContext(String context) {
		if(!contextMap.containsKey(context)) {
			contextMap.put(context, new CLC());
			if(commandMap.containsKey(context)) {
				contextMap.get(context).setRootAction(commandMap.get(context));
			}
		}
		return contextMap.get(context);
	}
	
	/**
	 * Changes the action that is executed when no command was triggered.
	 * This can be used to wrap an interface with additional commands without 
	 * disturbing the functionality of the enclosed interface.
	 * 
	 * @param a The command that is meant to be executed. 
	 */
	public final void setDefaultAction(Action a) {
		if(a == null) 
			defaultAction = unknownCommandAction;
		else
			defaultAction = a;
			
	}

	/**r
	 * Changes the action that is executed when this controller is called without
	 * any additional arguments.
	 * @param a The action to be executed
	 */
	public final void setRootAction(Action a) {
		if(a == null) 
			rootAction = defaultRootAction;
		else 
			rootAction = a;
	}
	
	/**
	 * Evaluates an ArgSet. Calls the rootAction if no arguments are passed, 
	 * and calls the defaultAction if no command was triggered by the first 
	 * argument.
	 * 
	 * @param args The set of arguments.
	 */
	public final void handle(ArgSet args) {
		if(args.isComment()) {
			return;
		} else if(args.peek().isEmpty()) {
			// In this case no other arguments are given, so execute the root action
			// of this comtroller
			rootAction.execute(args);
		} else if(args.hasAbbArg()) {
			Character c = args.peek().charAt(1);
			if(abbrMap.containsKey(c)) {
				abbrMap.get(args.fetchAbbr()).execute(args);
			} else {
				defaultAction.execute(args);
			}
		} else if(args.hasOptionArg()) {
			String s = args.peek().substring(2);
			if(optionMap.containsKey(s)) {
				optionMap.get(args.fetchOption()).execute(args);
			} else {
				defaultAction.execute(args);
			}
		} else if(args.hasArg()) {
			String s = args.peek();
			if(contextMap.containsKey(s)) {
				contextMap.get(args.pop()).handle(args);
			} else if(commandMap.containsKey(s)) {
				commandMap.get(args.pop()).execute(args);
			} else {
				defaultAction.execute(args);
			}
		}
	}
	
	/**
	 * This function is called when the focus of the command line
	 * control is forwarded to this controller.
	 *
	 * It can be used to check conditions or initialize variables.
	 */
	protected void onEntering() {

	}

	protected abstract class Action {
		public abstract void execute(ArgSet args);
	}
	
	protected final class CommandAction extends Action {
		private CLC clc;
		public CommandAction(CLC clc) {
			this.clc = clc;
		}
		public final void execute(ArgSet args) {
			clc.onEntering();
			clc.handle(args);
		}
	}
}
