package cli;

import java.util.InputMismatchException;

/**
 * This class provides convenient methods to handle a list of arguments
 */
public class ArgSet {
	/**
	 * The string that contains all arguments, 
	 * separated by whitespaces
	 */
	public String arg;

	/**
	 * Initializes the ArgSet with an empty String.
	 */
	public ArgSet() {
		this("");
	}

	/**
	 * Initializes the ArgSet with an array of arguments.
	 * This array is turned into a single String of
	 * arguments, separated by a single space character.
	 * @param  args The array of arguments
	 */
	public ArgSet(String[] args) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < args.length; i++) {
			sb.append(args[i]);
			// Append space to separate commands
			if(i < args.length - 1) {
				sb.append(" ");
			}
		}
		this.arg = sb.toString().trim();
	}

	/**
	 * Initializes the ArgSet with a single String
	 * that possibly contains a list of separated 
	 * arguments. The string can be empty.
	 * @param  arg The String that contains one or more arguments
	 */
	public ArgSet(String arg) {
		this(new String[]{arg});
	}

	/**
	 * Tries to read the first argument of the String.
	 * Returns an empty String if no argument is found.
	 * @return The first argument of this ArgSet or an empty String
	 * if no argument was found.
	 */
	public String peek() {
		int i = getFirstArgEndIndex();
		if(i == -1) return "";
		else return arg.substring(0, i);
	}

	/**
	 * Like peek, but removes the argument that was found.
	 * @return The first argument that was found in this ArgSet.
	 */
	public String pop() {
		int i = getFirstArgEndIndex();
		if(i == -1) return "";
		else {
			String firstArg = arg.substring(0, i);
			this.arg = arg.substring(i).trim();
			return firstArg;
		}
	}
	
	/**
	 * Returns the index of the last character of the first argument. So, e.g.
	 * if {@code arg} is "set path=/bin/", then this function will return 2.
	 * 
	 * In detail, this function interprets a character as a white space if its
	 * value is not greater than 20. This is similar to the way Java Strings are
	 * trimmed.
	 * 
	 * @return The index of the last character of the first argument.
	 */
	private int getFirstArgEndIndex() {
		int l = arg.length();
		// An empty string can't have any arguments
		if(l == 0) {
			return -1;
		}
		int i;
		for(i = 0; i < l; i++) {
			char c = arg.charAt(i);
			if(c == '\"') {
				i = getSectionEndIndex(i, '\"');
			} else if (c == '[') {
				i = getSectionEndIndex(i, ']');
			} else  if(c <= 32) {
				return i;
			}
			if(i == -1) return i;
		}
		// In this case we reached the end of the String without encountering any
		// characters that were no white spaces
		return i;
	}

	private int getSectionEndIndex(int start, char terminator) {
		int i = start;
		do {
			i++;
			if(i == arg.length()) {
				// The string is never terminated
				return -1;
			}
		} while(arg.charAt(i) != terminator);
		return i;
	}

	/**
	 * Try to read the first argument as a String. In this case a string is enclosed by " characters.
	 * @return      A string representation of the first argument
	 * @throws 		InputMismatchException if there is no argument available
	 */
	public String fetchString() throws InputMismatchException {
		if(!hasStringArg()) 
			throw new InputMismatchException("There are no string arguments to fetch from.");
		else {
			String s = pop();
			return s.substring(1, s.length() - 1);
		}
	}

	public ArgSet fetchList() throws InputMismatchException {
		if(!hasListArg()) {
			throw new InputMismatchException("There are no list arguments to fetch from.");
		} else {
			String s = pop();
			return new ArgSet(s.substring(1, s.length() - 1));
		}
	}
	
	/**
	 * Try to read the first argument as an Integer
	 * @return      An Integer representation of the first argument
	 * @throws 		InputMismatchException if there is no argument available
	 * @throws NumberFormatException if the cast to an Integer fails
	 */
	public Integer fetchInteger() throws InputMismatchException, NumberFormatException {
		if(peek().isEmpty()) 
			throw new InputMismatchException("There are no integer arguments to fetch from.");
		else {
			Integer i = Integer.valueOf(pop());
			return i;
		}
	}
	
	public Character fetchAbbr() {
		if(!hasAbbArg()) {
			throw new InputMismatchException("There is no abbreviation at the head of ArgSet.");
		} else {
			String s = pop();
			return s.charAt(1);
		}
	}
	
	public String fetchOption() {
		if(!hasOptionArg()) {
			throw new InputMismatchException("There is no option at the head of ArgSet.");
		} else {
			String s = pop();
			return s.substring(2);
		}
	}

	public boolean hasArg() {
		return !peek().isEmpty();
	}

	/**
	 * Checks whether there is another string argument available.
	 * A string argument is enclosed by " characters.
	 * @return True if another string argument is present, false otherwise.
	 */
	public boolean hasStringArg() {
		String s = peek();
		return (!s.isEmpty() && s.charAt(0) == '\"' && s.charAt(s.length() - 1) == '\"');
	}

	/**
	 * Checks whether there is a list argument available.
	 * A list argument starts with [ and ends with ].
	 * @return True if there's a list argument ahead of the reading head.
	 */
	public boolean hasListArg() {
		String s = peek();
		return (!s.isEmpty() && s.charAt(0) == '[' && s.charAt(s.length() - 1) == ']');
	}

	/**
	 * Checks whether there is a numeric argument at the head of the ArgSet.
	 * It attempts to cast the first argument to an integer. If that fails, then
	 * false is returned.
	 * @return False if the ArgSet is empty or if the first argument can not be casted to an integer. True otherwise.
	 */
	public boolean hasIntArg() {
		try{
			String s = peek();
			if(s.isEmpty()) {
				return false;
			} else {
				Integer.valueOf(s);
			}
		} catch(NumberFormatException e) {
			return false;
		}
		return true;
	}

	public boolean hasOptionArg() {
		String s = peek();
		if(s.isEmpty()) {
			return false;
		} else {
			return s.length() > 3 && s.charAt(0) == '-' && s.charAt(1) == '-' && Character.isLetter(s.charAt(2));
		}
	}

	public boolean hasAbbArg() {
		String s = peek();
		if(s.isEmpty()) {
			return false;
		} else {
			return s.length() == 2 && s.charAt(0) == '-' && Character.isLetter(s.charAt(1));
		} 
	}

	public boolean isComment() {
		return arg.length() > 0 && arg.charAt(0) == '#';
	}

}