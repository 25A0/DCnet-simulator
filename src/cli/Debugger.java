package cli;

import java.util.Set;
import java.util.HashMap;

/**
 * Static class to control debug output
 *
 */
public class Debugger {
	/**
	 * The debugging level. Can be changed by calling setlevel
	 */
	private static int level = 1;
	public static final int DEBUG_OFF = 0, DEBUG_SOME = 1, DEBUG_FULL = 2;

	/**
	 * A map containing tags and whether they're currently tracked.
	 * Output for tracked tags will be printed to stdout.
	 */
	private static final HashMap<String, Boolean> trackedTags = new HashMap<String, Boolean>();
	
	public static void print(int level, String s) {
		if(level <= 0 || level > Debugger.level) return;
		else {
			System.out.print(s);
		}
	}
	
	public static void println(int level, String s) {
		print(level, s+"\n");
	}

	public static void print(String tag, String s) {
		if(!trackedTags.containsKey(tag)) {
			trackedTags.put(tag, false);
		} else if(trackedTags.get(tag)) {
			System.out.print("#" + tag + '\t' + s);
		}
	}

	public static void println(String tag, String s) {
		print(tag, s + '\n');
	}
	
	/**
	 * Change the current debug level
	 * @param level The new level. Values outside the bounds will be set to the closest bound.
	 */
	public static void setLevel(int level) {
		if(level <= 0) {
			Debugger.level = 0;
		} else if(level >= DEBUG_FULL) {
			Debugger.level = DEBUG_FULL;
		} else {
			Debugger.level = level;
		}
		System.out.println("[Debugger] Debugging level has been set to " + Debugger.level);
	}

	public static void trackAdd(String tag) {
		if(trackedTags.containsKey(tag) && trackedTags.get(tag)) {
			System.out.println("[Debugger] Tag " + tag + " is being tracked already.");
		} else {
			trackedTags.put(tag, true);
			System.out.println("[Debugger] Now tracking " + tag);
		}
	}

	public static void trackRemove(String tag) {
		if(!trackedTags.containsKey(tag) && !trackedTags.remove(tag)) {
			System.out.println("[Debugger] Tag " + tag + " was not being tracked.");	
		} else {
			trackedTags.remove(tag);
			System.out.println("[Debugger] Tag " + tag + " is no longer tracked.");
		}	
	}

	public static Set<String> seenTags() {
		return trackedTags.keySet();
	}


	
}
