package com.laytonsmith.core.profiler;

import com.laytonsmith.PureUtilities.Common.DateUtils;
import com.laytonsmith.PureUtilities.Common.FileUtil;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.ExecutionQueue;
import com.laytonsmith.PureUtilities.Preferences;
import com.laytonsmith.PureUtilities.Preferences.Preference;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.MethodScriptExecutionQueue;
import com.laytonsmith.core.Static;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * TODO: The following points need profile hooks: 1 - Execution Queue task run times 3 - Procedure execution run times
 * (with parameters)
 */
/**
 *
 *
 */
public final class Profiler {

	/**
	 * Useful for test cases and other places where a profiler is needed, but not desired, this can be called to
	 * generate a fake Profiler.
	 *
	 * @return
	 */
	public static Profiler FakeProfiler() {
		Profiler p = new Profiler();
		p.profilerOn = false;
		return p;
	}

	public static void Install(File initFile) throws IOException {
		//We just want to create the config file initially
		GetPrefs(initFile);
	}

	private static Preferences GetPrefs(File initFile) throws IOException {
		List<Preference> defaults = new ArrayList<>(Arrays.asList(new Preference[]{
			new Preference("profiler-on", "false", Preferences.Type.BOOLEAN, "Turns the profiler on or off. The profiler can cause a slight amount of lag, so generally speaking"
			+ " you don't leave it on during normal operation."),
			new Preference("profiler-granularity", "1", Preferences.Type.INT, "Sets the granularity of the profiler. 1 logs some things, while 5 logs everything possible."),
			new Preference("profiler-log", "logs/profiling/internal/%Y-%M-%D-profiler.log", Preferences.Type.STRING, "The location of the profiler output log. The following macros are supported"
			+ " and will expand to the specified values: %Y - Year, %M - Month, %D - Day, %h - Hour, %m - Minute, %s - Second"),
			new Preference("write-to-file", "true", Preferences.Type.BOOLEAN, "If true, will write results out to file."),
			new Preference("write-to-screen", "false", Preferences.Type.BOOLEAN, "If true, will write results out to screen."),
			new Preference("profile-log-threshold", "0.005", Preferences.Type.DOUBLE, "If a profile point took less than this amount of time (in ms) to run, it won't be logged. This is good for reducing data blindness"
			+ " caused by too much data being displayed. Normally you only care about things that took longer than a certain amount, not things that took less than a certain amount. Setting this to 0"
			+ " will trigger everything.")}));
		Preferences prefs = new Preferences("CommandHelper", Static.getLogger(), defaults, "These settings control the integrated profiler");
		prefs.init(initFile);
		return prefs;
	}
	//Needs to be package protected
	Map<ProfilePoint, Long> operations;
	long queuedProfilePoints = 0;

	private LogLevel configGranularity;
	private boolean profilerOn;
	private String logFile;
	private boolean writeToFile;
	private boolean writeToScreen;
	private Preferences prefs;
	private File initFile;
	private double logThreshold;
	//To prevent file fights across threads, we only want one outputQueue.
	private static ExecutionQueue outputQueue;
	@SuppressWarnings("checkstyle:membername") // Allow custom name since it more clearly describes the object.
	private final ProfilePoint NULL_OP = new ProfilePoint("NULL_OP", this);

	private Profiler() {
		//Private constructor for FakeProfiler
		if(outputQueue == null) {
			outputQueue = new MethodScriptExecutionQueue("CommandHelper-Profiler", "default");
		}
	}

	@SuppressWarnings("ResultOfObjectAllocationIgnored")
	public Profiler(File initFile) throws IOException {
		this();
		prefs = GetPrefs(initFile);
		//We want speed here, not memory usage, so lets put an excessively large capacity, and excessively low load factor
		operations = new HashMap<>(1024, 0.25f);
		this.initFile = initFile;

		configGranularity = LogLevel.getEnum(prefs.getIntegerPreference("profiler-granularity"));
		if(configGranularity == null) {
			configGranularity = LogLevel.ERROR;
		}
		profilerOn = prefs.getBooleanPreference("profiler-on");
		logFile = prefs.getStringPreference("profiler-log");
		writeToFile = prefs.getBooleanPreference("write-to-file");
		writeToScreen = prefs.getBooleanPreference("write-to-screen");
		logThreshold = prefs.getDoublePreference("profile-log-threshold");
		new GarbageCollectionDetector(this);
		//As a form of calibration, we want to "warm up" a point.
		//For whatever reason, this levels out the profile points pretty well.
		ProfilePoint warmupPoint = this.start("Warming up the profiler", LogLevel.VERBOSE);
		this.stop(warmupPoint);
	}

	/**
	 * Starts a timer, and returns a profile point object, which should be used to stop this timer later. A special
	 * ProfilePoint is returned if this profile point shouldn't be logged based on the granularity settings, which short
	 * circuits the entire profiling process, for non-trigger points, which should speed operation considerably.
	 *
	 * @param name The name to be used during logging
	 * @param granularity
	 * @return
	 */
	public ProfilePoint start(String name, LogLevel granularity) {
		if(!isLoggable(granularity)) {
			return NULL_OP;
		}
		ProfilePoint p = new ProfilePoint(name, this);
		start0(p, granularity);
		return p;
	}

	/**
	 * "Starts" an operation. Note that for each start, you must use EXACTLY one stop, with exactly the same object for
	 * operationName. Multiple profile points can share the same name, and they will be stacked and lined up
	 * accordingly.
	 *
	 * @param operationName The name of the operation. A corresponding call to DoStop must be called with this exact
	 * same object.
	 * @param granularity The granularity at which to log.
	 */
	private void start0(ProfilePoint operationName, LogLevel granularity) {
		if(operations.containsKey(operationName)) {
			//Nope. Can't queue up multiple versions of the same
			//id
			throw new RuntimeException("Cannot queue the same profile point multiple times!");
		}
		queuedProfilePoints++;
		operationName.setGranularity(granularity);

		//This line should ALWAYS be last in the function
		operations.put(operationName, System.nanoTime());
	}

	private static final Map<Long, String> INDENTS = new TreeMap<Long, String>();

	static {
		//Let's just warm it up some
		for(int i = 0; i < 10; i++) {
			getIndent(i);
		}
	}

	private static String getIndent(long count) {
		if(!INDENTS.containsKey(count)) {
			StringBuilder b = new StringBuilder();
			for(int i = 0; i < count; i++) {
				b.append(" ");
			}
			INDENTS.put(count, b.toString());
		}
		return INDENTS.get(count);
	}
	private static final String GC_STRING = " (however, the garbage collector was run during this profile point)";

	public void stop(ProfilePoint operationName) {
		//This line should ALWAYS be first in the function
		long stop = System.nanoTime();
		if(operationName == NULL_OP) {
			return;
		}
		if(!operations.containsKey(operationName)) {
			return;
		}
		long total = stop - operations.get(operationName);
		//1 million nano seconds in 1 ms. We want x.xxx ms shown, so divide by 1000, round (well, integer truncate, since it's faster), then divide by 1000 again.
		//voila, significant figure to the 3rd degree.
		double time = (total / 1000) / 1000.0;
		if(time >= logThreshold) {
			String stringTime = Double.toString(time);
			if(stringTime.length() < 6 && stringTime.contains(".")) {
				while(stringTime.length() < 6) {
					stringTime += "0";
				}
			}
			stringTime += "ms";
			if(time > 1000) {
				//Let's change this to seconds, actually.
				stringTime = Double.toString(((long) time) / 1000.0) + "sec";
			}
			String operationMessage = operationName.getMessage() != null ? " Message: " + operationName.getMessage() : "";
			doLog("[" + stringTime + "][Lvl:" + (operationName.getGranularity().getLevel()) + "]:" + getIndent(queuedProfilePoints)
					+ operationName.toString() + operationMessage + (operationName.wasGCd() ? GC_STRING : ""));
		}
		queuedProfilePoints--;
	}

	public boolean isLoggable(LogLevel granularity) {
		if(!profilerOn || granularity == null) {
			return false;
		}
		return granularity.getLevel() <= configGranularity.getLevel();
	}

	/**
	 * Pushes a log to either the screen or the log file, depending on config settings. Arbitrary messages can be logged
	 * using this method.
	 *
	 * @param message
	 */
	public void doLog(final String message) {
		outputQueue.push(null, null, () -> {
			if(writeToScreen) {
				StreamUtils.GetSystemOut().println(message);
			}
			if(writeToFile) {
				File file = new File(initFile.getParentFile(), DateUtils.ParseCalendarNotation(logFile));
				try {
					FileUtil.write(DateUtils.ParseCalendarNotation("%Y-%M-%D %h:%m.%s") + ": " + message + Static.LF(), //Message to log
							file, //File to output to
							FileUtil.APPEND, //We want to append
							true); //Create it for us if it doesn't exist
				} catch (IOException ex) {
					StreamUtils.GetSystemErr().println("While trying to write to the profiler log file (" + file.getAbsolutePath() + "), received an IOException: " + ex.getMessage());
				}
			}
		});
	}
}
