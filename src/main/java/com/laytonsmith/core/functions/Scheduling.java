package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.ArrayUtils;
import com.laytonsmith.PureUtilities.Common.MutableObject;
import com.laytonsmith.PureUtilities.Common.Range;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.core;
import com.laytonsmith.annotations.hide;
import com.laytonsmith.annotations.noboilerplate;
import com.laytonsmith.annotations.seealso;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.Optimizable;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CClosure;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CREInsufficientArgumentsException;
import com.laytonsmith.core.exceptions.CRE.CRERangeException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.ProgramFlowManipulationException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.core.profiler.ProfilePoint;
import com.laytonsmith.core.taskmanager.CoreTaskType;
import com.laytonsmith.core.taskmanager.TaskManager;
import com.laytonsmith.core.taskmanager.TaskState;
import com.laytonsmith.core.taskmanager.TimeoutTaskHandler;
import com.laytonsmith.tools.docgen.DocGenTemplates;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
@core
public class Scheduling {

	public static void ClearScheduledRunners() {
		StaticLayer.ClearAllRunnables();
	}

	public static String docs() {
		return "This class contains methods for dealing with time and server scheduling.";
	}

	@api
	public static class time extends AbstractFunction {

		@Override
		public String getName() {
			return "time";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "int {} Returns the current unix time stamp, in milliseconds. The resolution of this is not guaranteed to be extremely accurate. If "
					+ "you need extreme accuracy, use nano_time()";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_1_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			return new CInt(System.currentTimeMillis(), t);
		}
	}

	@api
	public static class nano_time extends AbstractFunction {

		@Override
		public String getName() {
			return "nano_time";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "int {} Returns an arbitrary number based on the most accurate clock available on this system. Only useful when compared to other calls"
					+ " to nano_time(). The return is in nano seconds. See the Java API on System.nanoTime() for more information on the usage of this function.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_1_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			return new CInt(System.nanoTime(), t);
		}
	}

	@api
	@hide("Only meant for cmdline/testing")
	@noboilerplate
	public static class sleep extends AbstractFunction {

		@Override
		public String getName() {
			return "sleep";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "void {seconds} Sleeps the script for the specified number of seconds, up to the maximum time limit defined in the preferences file."
					+ " Seconds may be a double value, so 0.5 would be half a second."
					+ " PLEASE NOTE: Sleep times are NOT very accurate, and should not be relied on for preciseness.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_1_0;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			Mixed x = args[0];
			double time = Static.getNumber(x, t);
			try {
				Thread.sleep((int) (time * 1000));
			} catch (InterruptedException ex) {
			}
			return CVoid.VOID;
		}

		@Override
		public Boolean runAsync() {
			//Because we stop the thread
			return true;
		}
	}

	@api(environments = {GlobalEnv.class})
	public static class set_interval extends AbstractFunction {

		@Override
		public String getName() {
			return "set_interval";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public String docs() {
			return "int {timeInMS, [initialDelayInMS,] closure} Sets a task to run every so often. This works similarly to set_timeout,"
					+ " except the task will automatically re-register itself to run again. Note that the resolution"
					+ " of the time is in ms, however, the server will only have a resolution of up to 50 ms, meaning"
					+ " that a time of 1-50ms is essentially the same as 50ms. The inital delay defaults to the same"
					+ " thing as timeInMS, that is, there will be a pause between registration and initial firing. However,"
					+ " this can be set to 0 (or some other number) to adjust how long of a delay there is before it begins.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(final Target t, final Environment environment, Mixed... args) throws ConfigRuntimeException {
			long time = Static.getInt(args[0], t);
			int offset = 0;
			long delay = time;
			if(args.length == 3) {
				offset = 1;
				delay = Static.getInt(args[1], t);
			}
			if(!(args[1 + offset].isInstanceOf(CClosure.TYPE))) {
				throw new CRECastException(getName() + " expects a closure to be sent as the second argument", t);
			}
			final CClosure c = (CClosure) args[1 + offset];
			final AtomicInteger ret = new AtomicInteger(-1);

			ret.set(StaticLayer.SetFutureRepeater(environment.getEnv(GlobalEnv.class).GetDaemonManager(), time, delay, () -> {
				c.getEnv().getEnv(GlobalEnv.class).SetCustom("timeout-id", ret.get());
				try {
					ProfilePoint p = environment.getEnv(GlobalEnv.class).GetProfiler().start("Executing timeout"
							+ " with id " + ret.get() + " (defined at " + t.toString() + ")", LogLevel.ERROR);
					try {
						c.executeCallable();
					} finally {
						p.stop();
					}
				} catch (ConfigRuntimeException e) {
					ConfigRuntimeException.HandleUncaughtException(e, environment);
				} catch (CancelCommandException e) {
					//Ok
				} catch (ProgramFlowManipulationException e) {
					ConfigRuntimeException.DoWarning("Using a program flow manipulation construct improperly! " + e.getClass().getSimpleName());
				}
			}));
			return new CInt(ret.get(), t);
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "set_interval(1000, closure(){\n"
				+ "\tmsg('Hello World!');\n"
				+ "});", "<Would message the user \"Hello World!\" every second>"),
				new ExampleScript("Usage with initial delay", "set_interval(1000, 5000, closure(){\n"
				+ "\tmsg('Hello World!');\n"
				+ "});", "<Would message the user \"Hello World!\" every second, however there would be an initial delay of 5 seconds>")
			};
		}

	}

	@api(environments = {GlobalEnv.class})
	public static class set_timeout extends AbstractFunction {

		@Override
		public String getName() {
			return "set_timeout";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "int {timeInMS, closure} Sets a task to run in the specified number of ms in the future."
					+ " The task will only run once. Note that the resolution"
					+ " of the time is in ms, however, the server will only have a resolution of up to 50 ms, meaning"
					+ " that a time of 1-50ms is essentially the same as 50ms.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(final Target t, final Environment environment, Mixed... args) throws ConfigRuntimeException {
			final TaskManager taskManager = environment.getEnv(GlobalEnv.class).GetTaskManager();
			long time = Static.getInt(args[0], t);
			if(!(args[1].isInstanceOf(CClosure.TYPE))) {
				throw new CRECastException(getName() + " expects a closure to be sent as the second argument", t);
			}
			final CClosure c = (CClosure) args[1];
			final AtomicInteger ret = new AtomicInteger(-1);
			final AtomicBoolean isRunning = new AtomicBoolean(false);
			ret.set(StaticLayer.SetFutureRunnable(environment.getEnv(GlobalEnv.class).GetDaemonManager(), time, () -> {
				isRunning.set(true);
				c.getEnv().getEnv(GlobalEnv.class).SetCustom("timeout-id", ret.get());
				taskManager.getTask(CoreTaskType.TIMEOUT, ret.get()).changeState(TaskState.RUNNING);
				try {
					ProfilePoint p = environment.getEnv(GlobalEnv.class).GetProfiler().start("Executing timeout"
							+ " with id " + ret.get() + " (defined at " + t.toString() + ")", LogLevel.ERROR);
					try {
						c.executeCallable();
					} finally {
						p.stop();
					}
				} catch (ConfigRuntimeException e) {
					ConfigRuntimeException.HandleUncaughtException(e, environment);
				} catch (CancelCommandException e) {
					//Ok
				} catch (ProgramFlowManipulationException e) {
					ConfigRuntimeException.DoWarning("Using a program flow manipulation construct improperly! " + e.getClass().getSimpleName());
				} finally {
					taskManager.getTask(CoreTaskType.TIMEOUT, ret.get()).changeState(TaskState.FINISHED);
					environment.getEnv(GlobalEnv.class).SetInterrupt(false);
				}
			}));
			taskManager.addTask(new TimeoutTaskHandler(ret.get(), t, () -> {
				if(isRunning.get()) {
					new clear_task().exec(t, environment, new CInt(ret.get(), t));
					environment.getEnv(GlobalEnv.class).SetInterrupt(true);
					taskManager.getTask(CoreTaskType.TIMEOUT, ret.get()).changeState(TaskState.KILLED);
				}
			}));
			taskManager.getTask(CoreTaskType.TIMEOUT, ret.get()).changeState(TaskState.IDLE);
			return new CInt(ret.get(), t);
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "set_timeout(10000, closure(){\n"
				+ "\tmsg('Hello World!');\n"
				+ "});", "<Would wait 5 seconds, then message the user \"Hello World!\">")
			};
		}

	}

	@api(environments = {GlobalEnv.class})
	public static class clear_task extends AbstractFunction {

		@Override
		public String getName() {
			return "clear_task";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "void {[id]} Stops the interval or timeout that is specified. The id can be gotten by"
					+ " storing the integer returned from either set_timeout or set_interval."
					+ " An invalid id is simply ignored. The clear_task function is more useful for set_timeout, where"
					+ " you may queue up some task to happen in the far future, yet have some trigger to"
					+ " prevent it from happening. ID is optional, but only if called from within a set_interval or set_timeout"
					+ " closure, in which case it defaults to the id of that particular task.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREInsufficientArgumentsException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if(args.length == 0 && environment.getEnv(GlobalEnv.class).GetCustom("timeout-id") != null) {
				StaticLayer.ClearFutureRunnable((Integer) environment.getEnv(GlobalEnv.class).GetCustom("timeout-id"));
			} else if(args.length == 1) {
				StaticLayer.ClearFutureRunnable(Static.getInt32(args[0], t));
			} else {
				throw new CREInsufficientArgumentsException("No id was passed to clear_task, and it's not running inside a task either.", t);
			}
			return CVoid.VOID;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Use from within an interval", "set_interval(1000, closure(){\n"
				+ "\tif(rand(0, 10) == 9){\n"
				+ "\t\tclear_task();\n"
				+ "\t}\n"
				+ "\tmsg('Hello World!');\n"
				+ "});", "<Messages the user until the random number generator produces a 9, at which point the interval is stopped>"),
				new ExampleScript("Using the id returned from set_timeout", "@id = set_timeout(5000, closure(){\n"
				+ "\tmsg('Hello World!');\n"
				+ "});\n"
				+ "clear_task(@id);", "<Nothing happens, as the timeout is cancelled before it runs>")
			};
		}

	}

	@api
	@seealso(Meta.get_locales.class)
	public static class simple_date extends AbstractFunction {

		@Override
		public String getName() {
			return "simple_date";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3, 4};
		}

		@Override
		public String docs() {
			Map<String, DocGenTemplates.Generator> map = new HashMap<>();
			map.put("timezoneValues", (DocGenTemplates.Generator) (String... args) -> {
				String[] timezones = ArrayUtils.EMPTY_STRING_ARRAY;
				try {
					timezones = TimeZone.getAvailableIDs();
				} catch (NullPointerException e) {
					//This is due to a JDK bug. As you can see, the code above
					//should never NPE due to our mistake, so it would only occur
					//during an internal error. The solution that worked for me is here:
					//https://bugs.launchpad.net/ubuntu/+source/tzdata/+bug/1053160
					//however, this appears to be an issue in Open JDK, so performance on
					//other systems may vary. We will handle this error by reporting that
					//list could not be retrieved, using the Join method's empty parameter.
				}
				//Let's sort the timezones
				List<String> tz = new ArrayList<>(Arrays.asList(timezones));
				Collections.sort(tz);
				return StringUtils.Join(tz, ", ", " or ", " or ", "Couldn't retrieve the list of timezones!");
			});
			try {
				return getBundledDocs(map);
			} catch (DocGenTemplates.Generator.GenerateException ex) {
				Logger.getLogger(Scheduling.class.getName()).log(Level.SEVERE, null, ex);
				return getBundledDocs();
			}
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREFormatException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public CString exec(Target t, Environment env, Mixed... args) {
			Date now = new Date();
			if(args.length >= 2 && !(args[1] instanceof CNull)) {
				now = new Date(Static.getInt(args[1], t));
			}
			TimeZone timezone = TimeZone.getDefault();
			if(args.length >= 3 && Construct.nval(args[2]) != null) {
				timezone = TimeZone.getTimeZone(args[2].val());
			}
			Locale locale = Locale.getDefault();
			if(args.length >= 4) {
				String countryCode = Construct.nval(args[3]);
				if(countryCode == null) {
					locale = Locale.getDefault();
				} else {
					locale = Static.GetLocale(countryCode);
				}
				if(locale == null) {
					throw new CREFormatException("The given locale was not found on your system: "
							+ countryCode, t);
				}
			}
			SimpleDateFormat dateFormat;
			try {
				dateFormat = new SimpleDateFormat(args[0].toString(), locale);
			} catch (IllegalArgumentException ex) {
				throw new CREFormatException(ex.getMessage(), t);
			}
			dateFormat.setTimeZone(timezone);
			return new CString(dateFormat.format(now), t);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				/* 1 */new ExampleScript("Basic usage", "simple_date('h:mm a')", ":11:36 AM"),
				/* 2 */ new ExampleScript("Usage with quoted letters", "simple_date('yyyy.MM.dd G \\'at\\' HH:mm:ss z')", ":2013.06.13 AD at 11:36:46 CDT"),
				/* 3 */ new ExampleScript("Adding a single quote", "simple_date('EEE, MMM d, \\'\\'yy')", ":Wed, Jun 5, '13"),
				/* 4 */ new ExampleScript("Specifying alternate time", "simple_date('EEE, MMM d, \\'\\'yy', 0)"),
				/* 5 */ new ExampleScript("With timezone", "simple_date('hh \\'o\\'\\'clock\\' a, zzzz')", ":11 o'clock AM, Central Daylight Time"),
				/* 6 */ new ExampleScript("With timezone", "simple_date('hh \\'o\\'\\'clock\\' a, zzzz')", ":11 o'clock AM, Central Daylight Time"),
				/* 7 */ new ExampleScript("With simple timezone", "simple_date('K:mm a, z')", ":11:42 AM, CDT"),
				/* 8 */ new ExampleScript("With alternate timezone", "simple_date('K:mm a, z', time(), 'GMT')", ":4:42 PM, GMT"),
				/* 9 */ new ExampleScript("With 5 digit year", "simple_date('yyyyy.MMMMM.dd GGG hh:mm aaa')", ":02013.June.05 AD 11:42 AM"),
				/* 10 */ new ExampleScript("Long format", "simple_date('EEE, d MMM yyyy HH:mm:ss Z')", ":Wed, 5 Jun 2013 11:42:56 -0500"),
				/* 10 */ new ExampleScript("Long format with alternate locale", "simple_date('EEE, d MMM yyyy HH:mm:ss Z', 1444418254496, 'CET', 'no_NO')"),
				/* 11 */ new ExampleScript("Computer readable format", "simple_date('yyMMddHHmmssZ')", ":130605114256-0500"),
				/* 12 */ new ExampleScript("With milliseconds", "simple_date('yyyy-MM-dd\\'T\\'HH:mm:ss.SSSZ')", ":2013-06-05T11:42:56.799-0500")};
		}
	}

	@api
	@seealso(simple_date.class)
	public static class parse_date extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			SimpleDateFormat dateFormat;
			Locale locale = Locale.getDefault();
			if(args.length >= 3) {
				String countryCode = Construct.nval(args[2]);
				if(countryCode == null) {
					locale = Locale.getDefault();
				} else {
					locale = Static.GetLocale(countryCode);
				}
				if(locale == null) {
					throw new CREFormatException("The given locale was not found on your system: "
							+ countryCode, t);
				}
			}
			try {
				dateFormat = new SimpleDateFormat(args[0].toString(), locale);
				Date d = dateFormat.parse(args[1].val());
				return new CInt(d.getTime(), t);
			} catch (IllegalArgumentException | ParseException ex) {
				throw new CREFormatException(ex.getMessage(), t);
			}
		}

		@Override
		public String getName() {
			return "parse_date";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public String docs() {
			return "int {dateFormat, dateString, [locale]} Parses a date string, and returns an integer timestamp representing that time. This essentially"
					+ " works in reverse of {{function|simple_date}}. The dateFormat string is the same as simple_date, see the documentation for"
					+ " that function to see full details on that. The dateString is the actual date to be parsed. The dateFormat should be the"
					+ " equivalent format that was used to generate the dateString. In general, this function is fairly lenient, and will still"
					+ " try to parse a dateString that doesn't necessarily conform to the given format, but it shouldn't be relied on to work"
					+ " with malformed data. Various portions of the date may be left off, in which case the missing portions will be assumed,"
					+ " for instance, if the time is left off completely, it is assumed to be midnight, and if the minutes are left off, "
					+ " it is assumed to be on the hour, if the date is left off, it is assumed to be today, etc.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Simple example", "parse_date('yyMMddHHmmssZ', '130605114256-0500')"),
				new ExampleScript("Using the results of simple_date", "@format = 'EEE, d MMM yyyy HH:mm:ss Z';\n"
				+ "msg(parse_date(@format, simple_date(@format, 1)));")
			};
		}

	}

	@api
	public static class get_system_timezones extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			String[] timezones = ArrayUtils.EMPTY_STRING_ARRAY;
			try {
				timezones = TimeZone.getAvailableIDs();
			} catch (NullPointerException e) {
				//This is due to a JDK bug. As you can see, the code above
				//should never NPE due to our mistake, so it would only occur
				//during an internal error. The solution that worked for me is here:
				//https://bugs.launchpad.net/ubuntu/+source/tzdata/+bug/1053160
				//however, this appears to be an issue in Open JDK, so performance on
				//other systems may vary. We will handle this error by reporting that
				//list could not be retrieved, using the Join method's empty parameter.
			}
			//Let's sort the timezones
			List<String> tz = new ArrayList<>(Arrays.asList(timezones));
			Collections.sort(tz);
			CArray ret = new CArray(t);
			for(String s : tz) {
				ret.push(new CString(s, t), t);
			}
			return ret;
		}

		@Override
		public String getName() {
			return "get_system_timezones";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "array&lt;string&gt; {} Returns a list of time zones registered on this system.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_2;
		}

	}

	@api
	public static class set_cron extends AbstractFunction implements Optimizable {

		private static Thread cronThread = null;
		private static final Object CRON_THREAD_LOCK = new Object();
		private static final Map<Integer, CronFormat> CRON_JOBS = new HashMap<Integer, CronFormat>();
		private static final AtomicInteger JOB_IDS = new AtomicInteger(1);

		/**
		 * Stops a job from running again, and returns true if the value was actually removed. False is returned
		 * otherwise.
		 *
		 * @return
		 * @param jobID The job ID
		 */
		public static boolean stopJob(int jobID) {
			synchronized(CRON_JOBS) {
				if(CRON_JOBS.containsKey(jobID)) {
					CRON_JOBS.remove(jobID);
					return true;
				} else {
					return false;
				}
			}
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREFormatException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			//First things first, check the format of the arguments.
			if(!(args[0].isInstanceOf(CString.TYPE))) {
				throw new CRECastException("Expected string for argument 1 in " + getName(), t);
			}
			if(!(args[1].isInstanceOf(CClosure.TYPE))) {
				throw new CRECastException("Expected closure for argument 2 in " + getName(), t);
			}
			CronFormat format = validateFormat(args[0].val(), t);
			format.job = ((CClosure) args[1]);
			//At this point, the format is complete. We need to start up the cron thread if it's not running, and
			//then register this job, as well as inform clear_task of this id.
			synchronized(CRON_THREAD_LOCK) {
				if(cronThread == null) {
					final DaemonManager dm = environment.getEnv(GlobalEnv.class).GetDaemonManager();
					final MutableObject<Boolean> stopCron = new MutableObject<>(false);
					StaticLayer.GetConvertor().addShutdownHook(() -> {
						cronThread = null;
						stopCron.setObject(true);
						synchronized(CRON_JOBS) {
							CRON_JOBS.clear();
						}
						synchronized(CRON_THREAD_LOCK) {
							CRON_THREAD_LOCK.notifyAll();
						}
					});
					cronThread = new Thread(() -> {
						long lastMinute = 0;
						while(!stopCron.getObject()) {
							//We want to check to make sure that we only run once per minute, even though the
							//checks happen every second. This ensures that we have a fast enough sampling
							//period, while ensuring that we don't repeat tasks within the same minute.
							if((System.currentTimeMillis() / 1000 / 60) > lastMinute) {
								//Set the lastMinute value to now
								lastMinute = System.currentTimeMillis() / 1000 / 60;
								//Activate
								synchronized(CRON_JOBS) {
									Calendar c = Calendar.getInstance();
									for(final CronFormat f : CRON_JOBS.values()) {
										//Check to see if it is currently time to run each job
										if(f.min.contains(c.get(Calendar.MINUTE))
												&& f.hour.contains(c.get(Calendar.HOUR_OF_DAY))
												&& f.day.contains(c.get(Calendar.DAY_OF_MONTH))
												&& f.month.contains(c.get(Calendar.MONTH) + 1)
												&& f.dayOfWeek.contains(c.get(Calendar.DAY_OF_WEEK) - 1)) {
											//All the fields match, so let's trigger this job
											StaticLayer.GetConvertor().runOnMainThreadLater(dm, () -> {
												try {
													f.job.executeCallable();
												} catch (ConfigRuntimeException ex) {
													ConfigRuntimeException.HandleUncaughtException(ex, f.job.getEnv());
												}
											});
										}
									}
								}
							} //else continue, we'll wait another second.
							synchronized(CRON_THREAD_LOCK) {
								try {
									CRON_THREAD_LOCK.wait(1000);
								} catch (InterruptedException ex) {
									//Continue
								}
							}
						}
						dm.deactivateThread(cronThread);
					}, Implementation.GetServerType().getBranding() + "-CronDaemon");
					dm.activateThread(cronThread);
					cronThread.start();
				}
			}
			int jobID = JOB_IDS.getAndIncrement();
			synchronized(CRON_JOBS) {
				CRON_JOBS.put(jobID, format);
				format.job.getEnv().getEnv(GlobalEnv.class).SetCustom("cron-task-id", jobID);
			}
			return new CInt(jobID, t);
		}

		private static final Map<String, Integer> MONTHS = new HashMap<String, Integer>();
		private static final Map<String, Integer> DAYS = new HashMap<String, Integer>();
		private static final Map<String, Integer> HOURS = new HashMap<String, Integer>();
		private static final Pattern RANGE = Pattern.compile("(\\d+)-(\\d+)");
		private static final Pattern EVERY = Pattern.compile("\\*/(\\d+)");
		private static final List<Range> RANGES = Arrays.asList(new Range(0, 59), new Range(0, 23), new Range(1, 31), new Range(1, 12), new Range(0, 6));

		static {
			MONTHS.put("jan", 1);
			MONTHS.put("feb", 2);
			MONTHS.put("mar", 3);
			MONTHS.put("apr", 4);
			MONTHS.put("may", 5);
			MONTHS.put("jun", 6);
			MONTHS.put("jul", 7);
			MONTHS.put("aug", 8);
			MONTHS.put("sep", 9);
			MONTHS.put("oct", 10);
			MONTHS.put("nov", 11);
			MONTHS.put("dec", 12);
			MONTHS.put("january", 1);
			MONTHS.put("february", 2);
			MONTHS.put("febuary", 2); //common misspelling
			MONTHS.put("march", 3);
			MONTHS.put("april", 4);
			MONTHS.put("may", 5);
			MONTHS.put("june", 6);
			MONTHS.put("july", 7);
			MONTHS.put("august", 8);
			MONTHS.put("september", 9);
			MONTHS.put("october", 10);
			MONTHS.put("november", 11);
			MONTHS.put("december", 12);

			DAYS.put("sun", 0);
			DAYS.put("mon", 1);
			DAYS.put("tue", 2);
			DAYS.put("wed", 3);
			DAYS.put("thu", 4);
			DAYS.put("fri", 5);
			DAYS.put("sat", 6);
			DAYS.put("sunday", 0);
			DAYS.put("monday", 1);
			DAYS.put("tuesday", 2);
			DAYS.put("wednesday", 3);
			DAYS.put("thursday", 4);
			DAYS.put("friday", 5);
			DAYS.put("saturday", 6);

			HOURS.put("midnight", 0);
			HOURS.put("noon", 12);
		}

		private CronFormat validateFormat(String format, Target t) {
			//Now we need to look at the format of the cron task, and convert it to a standardized format.
			//Our goal here is to remove all ranges, predefined names, including @hourly and January.
			format = format.trim();
			//Changes tabs to spaces
			format = format.replace("\t", " ");
			//Change multiple spaces to one space
			format = format.replaceAll("( )+", " ");
			//Lowercase everything
			format = format.toLowerCase();
			if("@yearly".equals(format) || "@annually".equals(format)) {
				format = "0 0 1 1 *";
			}
			if("@monthly".equals(format)) {
				format = "0 0 1 * *";
			}
			if("@weekly".equals(format)) {
				format = "0 0 * * 0";
			}
			if("@daily".equals(format)) {
				format = "0 0 * * *";
			}
			if("@hourly".equals(format)) {
				format = "0 * * * *";
			}
			//Check for invalid characters
			if(format.matches("[^a-z0-9\\*\\-,@/]")) {
				throw new CREFormatException("Invalid characters found in format for " + getName() + ": \"" + format + "\". Check your format and try again.", t);
			}
			//Now split into the segments.
			String[] sformat = format.split(" ");
			if(sformat.length != 5) {
				throw new CREFormatException("Expected 5 segments in " + getName() + ", but " + StringUtils.PluralTemplateHelper(sformat.length, "%d was", "%d were") + " found.", t);
			}
			String min = sformat[0];
			String hour = sformat[1];
			String day = sformat[2];
			String month = sformat[3];
			String dayOfWeek = sformat[4];

			//Now replace the special shortcut names
			for(String key : MONTHS.keySet()) {
				month = month.replace(key, Integer.toString(MONTHS.get(key)));
			}
			for(String key : DAYS.keySet()) {
				dayOfWeek = dayOfWeek.replace(key, Integer.toString(DAYS.get(key)));
			}
			for(String key : HOURS.keySet()) {
				hour = hour.replace(key, Integer.toString(HOURS.get(key)));
			}
			//Split on commas
			List<String> minList = new ArrayList<>(Arrays.asList(min.split(",")));
			List<String> hourList = new ArrayList<>(Arrays.asList(hour.split(",")));
			List<String> dayList = new ArrayList<>(Arrays.asList(day.split(",")));
			List<String> monthList = new ArrayList<>(Arrays.asList(month.split(",")));
			List<String> dayOfWeekList = new ArrayList<>(Arrays.asList(dayOfWeek.split(",")));

			List<List<String>> segments = Arrays.asList(minList, hourList, dayList, monthList, dayOfWeekList);
			//Now go through each and pull out any ranges. At this point, everything
			//is numbers or *
			for(int i = 0; i < segments.size(); i++) {
				List<String> segment = segments.get(i);
				Iterator<String> it = segment.iterator();
				List<String> addAll = new ArrayList<>();
				Range range = RANGES.get(i);
				while(it.hasNext()) {
					String part = it.next();
					Matcher rangeMatcher = RANGE.matcher(part);
					if(rangeMatcher.find()) {
						it.remove();
						Integer minRange = Integer.parseInt(rangeMatcher.group(1));
						Integer maxRange = Integer.parseInt(rangeMatcher.group(2));
						Range r = new Range(minRange, maxRange);
						if(!r.isAscending()) {
							throw new CREFormatException("Ranges must be min to max, and not the same value in format for " + getName(), t);
						}
						List<Integer> rr = r.getRange();
						for(int j = 0; j < rr.size(); j++) {
							addAll.add(Integer.toString(rr.get(j)));
						}
						continue;
					}
					Matcher everyMatcher = EVERY.matcher(part);
					if(everyMatcher.find()) {
						it.remove();
						Integer every = Integer.parseInt(everyMatcher.group(1));
						for(int j = range.getMin(); j <= range.getMax(); j += every) {
							addAll.add(Integer.toString(j));
						}
					}
					if("*".equals(part)) {
						it.remove();
						for(int j = range.getMin(); j <= range.getMax(); j++) {
							addAll.add(Integer.toString(j));
						}
					}
				}
				segment.addAll(addAll);
				Collections.sort(segment);
			}
			//Everything is ints now, so parse it into a CronFormat object.
			CronFormat f = new CronFormat();
			for(int i = 0; i < 5; i++) {
				List<Integer> list = new ArrayList<>();
				List<String> segment = segments.get(i);
				Range range = RANGES.get(i);
				for(String s : segment) {
					try {
						list.add(Integer.parseInt(s));
					} catch (NumberFormatException ex) {
						//Any unexpected strings would show up here. The expected string values would have already
						//been replaced with a number, so this should work if there are no errors.
						throw new CREFormatException("Unknown string passed in format for " + getName() + " \"" + s + "\"", t);
					}
				}
				Collections.sort(list);
				if(!range.contains(list.get(0))) {
					throw new CREFormatException("Expecting value to be within the range " + range + " in format for " + getName() + ", but the value was " + list.get(0), t);
				}
				if(!range.contains(list.get(list.size() - 1))) {
					throw new CREFormatException("Expecting value to be within the range " + range + " in format for " + getName() + ", but the value was " + list.get(list.size() - 1), t);
				}
				Set<Integer> set = new TreeSet<>(list);
				switch(i) {
					case 0:
						f.min = set;
						break;
					case 1:
						f.hour = set;
						break;
					case 2:
						f.day = set;
						break;
					case 3:
						f.month = set;
						break;
					case 4:
						f.dayOfWeek = set;
						break;
				}
			}
			return f;
		}

		private static class CronFormat {

			public Set<Integer> min = new HashSet<>();
			public Set<Integer> hour = new HashSet<>();
			public Set<Integer> day = new HashSet<>();
			public Set<Integer> month = new HashSet<>();
			public Set<Integer> dayOfWeek = new HashSet<>();

			public CClosure job;

			@Override
			public String toString() {
				return min + "\n" + hour + "\n" + day + "\n" + month + "\n" + dayOfWeek;
			}

		}

		@Override
		public String getName() {
			return "set_cron";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return getBundledDocs();
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC);
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {
			if(children.get(0).isConst()) {
				if(children.get(0).getData().isInstanceOf(CString.TYPE)) {
					validateFormat(children.get(0).getData().val(), t);
				}
			}
			return null;
		}

	}

	@api
	public static class clear_cron extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRERangeException.class, CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Integer id = (Integer) environment.getEnv(GlobalEnv.class).GetCustom("cron-task-id");
			if(args.length == 1) {
				id = (int) Static.getInt(args[0], t);
			}
			if(id == null) {
				throw new CRERangeException("No task ID provided, and not running from within a cron task.", t);
			}
			if(!set_cron.stopJob(id)) {
				throw new CRERangeException("Task ID invalid", t);
			}
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "clear_cron";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "void {[cronID]} Clears the previously registered cron job from the registered list."
					+ " This will prevent the task from running again in the future. If run from within"
					+ " a cron task, the id is optional, and the current task will be prevented from running"
					+ " again in the future. If the ID provided is invalid, a RangeException is thrown.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

	}

}
