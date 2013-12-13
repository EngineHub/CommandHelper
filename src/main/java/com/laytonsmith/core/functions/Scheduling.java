package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.Range;
import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.hide;
import com.laytonsmith.core.*;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.ProgramFlowManipulationException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.laytonsmith.core.profiler.ProfilePoint;
import com.laytonsmith.tools.docgen.DocGenTemplates;
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
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Layton
 */
public class Scheduling {

	public static void ClearScheduledRunners() {
		StaticLayer.ClearAllRunnables();
	}

	public static String docs() {
		return "This class contains methods for dealing with time and server scheduling.";
	}

	@api
	public static class time extends AbstractFunction {

		public String getName() {
			return "time";
		}

		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		public String docs() {
			return "int {} Returns the current unix time stamp, in milliseconds. The resolution of this is not guaranteed to be extremely accurate. If "
					+ "you need extreme accuracy, use nano_time()";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_1_0;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			return new CInt(System.currentTimeMillis(), t);
		}
	}

	@api
	public static class nano_time extends AbstractFunction {

		public String getName() {
			return "nano_time";
		}

		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		public String docs() {
			return "int {} Returns an arbitrary number based on the most accurate clock available on this system. Only useful when compared to other calls"
					+ " to nano_time(). The return is in nano seconds. See the Java API on System.nanoTime() for more information on the usage of this function.";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_1_0;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			return new CInt(System.nanoTime(), t);
		}
	}

	@api
	@hide("Only meant for cmdline/testing")
	public static class sleep extends AbstractFunction {

		public String getName() {
			return "sleep";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "void {seconds} Sleeps the script for the specified number of seconds, up to the maximum time limit defined in the preferences file."
					+ " Seconds may be a double value, so 0.5 would be half a second."
					+ " PLEASE NOTE: Sleep times are NOT very accurate, and should not be relied on for preciseness.";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_1_0;
		}

		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Construct x = args[0];
            double time = Static.getNumber(x, t);
			try {
				Thread.sleep((int)(time * 1000));
			} catch (InterruptedException ex) {
			}
			return new CVoid(t);
		}

		public Boolean runAsync() {
			//Because we stop the thread
			return true;
		}
	}

	@api(environments={GlobalEnv.class})
	public static class set_interval extends AbstractFunction {

		public String getName() {
			return "set_interval";
		}

		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		public String docs() {
			return "int {timeInMS, [initialDelayInMS,] closure} Sets a task to run every so often. This works similarly to set_timeout,"
					+ " except the task will automatically re-register itself to run again. Note that the resolution"
					+ " of the time is in ms, however, the server will only have a resolution of up to 50 ms, meaning"
					+ " that a time of 1-50ms is essentially the same as 50ms. The inital delay defaults to the same"
					+ " thing as timeInMS, that is, there will be a pause between registration and initial firing. However,"
					+ " this can be set to 0 (or some other number) to adjust how long of a delay there is before it begins.";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(final Target t, final Environment environment, Construct... args) throws ConfigRuntimeException {
			long time = Static.getInt(args[0], t);
			int offset = 0;
			long delay = time;
			if (args.length == 3) {
				offset = 1;
				delay = Static.getInt(args[1], t);
			}
			if (!(args[1 + offset] instanceof CClosure)) {
				throw new ConfigRuntimeException(getName() + " expects a closure to be sent as the second argument", ExceptionType.CastException, t);
			}
			final CClosure c = (CClosure) args[1 + offset];
			final AtomicInteger ret = new AtomicInteger(-1);

			ret.set(StaticLayer.SetFutureRepeater(environment.getEnv(GlobalEnv.class).GetDaemonManager(), time, delay, new Runnable() {
				public void run() {
					c.getEnv().getEnv(GlobalEnv.class).SetCustom("timeout-id", ret.get());
					try {
						ProfilePoint p = environment.getEnv(GlobalEnv.class).GetProfiler().start("Executing timeout with id " + ret.get() + " (defined at " + t.toString() + ")", LogLevel.ERROR);
						try {
							c.execute(null);
						} finally {
							p.stop();
						}
					} catch (ConfigRuntimeException e) {
						ConfigRuntimeException.React(e, environment);
					} catch (CancelCommandException e) {
						//Ok
					} catch (ProgramFlowManipulationException e) {
						ConfigRuntimeException.DoWarning("Using a program flow manipulation construct improperly! " + e.getClass().getSimpleName());
					}
				}
			}));
			return new CInt(ret.get(), t);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api(environments={GlobalEnv.class})
	public static class set_timeout extends AbstractFunction {

		public String getName() {
			return "set_timeout";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public String docs() {
			return "int {timeInMS, closure} Sets a task to run in the specified number of ms in the future."
					+ " The task will only run once. Note that the resolution"
					+ " of the time is in ms, however, the server will only have a resolution of up to 50 ms, meaning"
					+ " that a time of 1-50ms is essentially the same as 50ms.";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(final Target t, final Environment environment, Construct... args) throws ConfigRuntimeException {
			long time = Static.getInt(args[0], t);
			if (!(args[1] instanceof CClosure)) {
				throw new ConfigRuntimeException(getName() + " expects a closure to be sent as the second argument", ExceptionType.CastException, t);
			}
			final CClosure c = (CClosure) args[1];
			final AtomicInteger ret = new AtomicInteger(-1);
			ret.set(StaticLayer.SetFutureRunnable(environment.getEnv(GlobalEnv.class).GetDaemonManager(), time, new Runnable() {
				public void run() {
					c.getEnv().getEnv(GlobalEnv.class).SetCustom("timeout-id", ret.get());
					try {
						ProfilePoint p = environment.getEnv(GlobalEnv.class).GetProfiler().start("Executing timeout with id " + ret.get() + " (defined at " + t.toString() + ")", LogLevel.ERROR);
						try {
							c.execute(null);
						} finally {
							p.stop();
						}
					} catch (ConfigRuntimeException e) {
						ConfigRuntimeException.React(e, environment);
					} catch (CancelCommandException e) {
						//Ok
					} catch (ProgramFlowManipulationException e) {
						ConfigRuntimeException.DoWarning("Using a program flow manipulation construct improperly! " + e.getClass().getSimpleName());
					}
				}
			}));
			return new CInt(ret.get(), t);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api(environments={GlobalEnv.class})
	public static class clear_task extends AbstractFunction {

		public String getName() {
			return "clear_task";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		public String docs() {
			return "void {[id]} Stops the interval or timeout that is specified. The id can be gotten by"
					+ " storing the integer returned from either set_timeout or set_interval."
					+ " An invalid id is simply ignored. The clear_task function is more useful for set_timeout, where"
					+ " you may queue up some task to happen in the far future, yet have some trigger to"
					+ " prevent it from happening. ID is optional, but only if called from within a set_interval or set_timeout"
					+ " closure, in which case it defaults to the id of that particular task.";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.InsufficientArgumentsException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			if (args.length == 0 && environment.getEnv(GlobalEnv.class).GetCustom("timeout-id") != null) {
				StaticLayer.ClearFutureRunnable((Integer) environment.getEnv(GlobalEnv.class).GetCustom("timeout-id"));
			} else if (args.length == 1) {
				StaticLayer.ClearFutureRunnable(Static.getInt32(args[0], t));
			} else {
				throw new ConfigRuntimeException("No id was passed to clear_task, and it's not running inside a task either.", ExceptionType.InsufficientArgumentsException, t);
			}
			return new CVoid(t);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class simple_date extends AbstractFunction {

		public String getName() {
			return "simple_date";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3};
		}

		public String docs() {
			Map<String, DocGenTemplates.Generator> map = new HashMap<String, DocGenTemplates.Generator>();
			map.put("timezoneValues", new DocGenTemplates.Generator() {

				public String generate(String... args) {
					String [] timezones = new String[0];
					try{
						timezones = TimeZone.getAvailableIDs();
					} catch(NullPointerException e){
						//This is due to a JDK bug. As you can see, the code above
						//should never NPE due to our mistake, so it would only occur
						//during an internal error. The solution that worked for me is here: 
						//https://bugs.launchpad.net/ubuntu/+source/tzdata/+bug/1053160
						//however, this appears to be an issue in Open JDK, so performance on
						//other systems may vary. We will handle this error by reporting that
						//list could not be retrieved, using the Join method's empty parameter.
					}
					//Let's sort the timezones
					List<String> tz = new ArrayList<String>(Arrays.asList(timezones));
					Collections.sort(tz);
					return StringUtils.Join(tz, ", ", " or ", " or ", "Couldn't retrieve the list of timezones!");
				}
			});
			return getBundledDocs(map);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.FormatException};
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment env, Construct... args) {
			Date now = new Date();
			if (args.length >= 2 && !(args[1] instanceof CNull)) {
				now = new Date(Static.getInt(args[1], t));
			}
			TimeZone timezone = TimeZone.getDefault();
			if(args.length >= 3){
				timezone = TimeZone.getTimeZone(args[2].val());
			}
			SimpleDateFormat dateFormat;
			try{
				dateFormat = new SimpleDateFormat(args[0].toString());
			} catch(IllegalArgumentException ex){
				throw new Exceptions.FormatException(ex.getMessage(), t);
			}
			dateFormat.setTimeZone(timezone);
			return new CString(dateFormat.format(now), t);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						/* 1 */ new ExampleScript("Basic usage", "simple_date('h:mm a')", ":11:36 AM"),
						/* 2 */ new ExampleScript("Usage with quoted letters", "simple_date('yyyy.MM.dd G \\'at\\' HH:mm:ss z')", ":2013.06.13 AD at 11:36:46 CDT"),
						/* 3 */ new ExampleScript("Adding a single quote", "simple_date('EEE, MMM d, \\'\\'yy')", ":Wed, Jun 5, '13"),
						/* 4 */ new ExampleScript("Specifying alternate time", "simple_date('EEE, MMM d, \\'\\'yy', 0)"),
						/* 5 */ new ExampleScript("With timezone", "simple_date('hh \\'o\\'\\'clock\\' a, zzzz')", ":11 o'clock AM, Central Daylight Time"),
						/* 6 */ new ExampleScript("With timezone", "simple_date('hh \\'o\\'\\'clock\\' a, zzzz')", ":11 o'clock AM, Central Daylight Time"),
						/* 7 */ new ExampleScript("With simple timezone", "simple_date('K:mm a, z')", ":11:42 AM, CDT"),
						/* 8 */ new ExampleScript("With alternate timezone", "simple_date('K:mm a, z', time(), 'GMT')", ":4:42 PM, GMT"),
						/* 9 */ new ExampleScript("With 5 digit year", "simple_date('yyyyy.MMMMM.dd GGG hh:mm aaa')", ":02013.June.05 AD 11:42 AM"),
						/* 10 */ new ExampleScript("Long format", "simple_date('EEE, d MMM yyyy HH:mm:ss Z')", ":Wed, 5 Jun 2013 11:42:56 -0500"),
						/* 11 */ new ExampleScript("Computer readable format", "simple_date('yyMMddHHmmssZ')", ":130605114256-0500"),
						/* 12 */ new ExampleScript("With milliseconds", "simple_date('yyyy-MM-dd\\'T\\'HH:mm:ss.SSSZ')", ":2013-06-05T11:42:56.799-0500"),
			};
		}
	}
	
	@api
	public static class set_cron extends AbstractFunction implements Optimizable {
		
		private static Thread cronThread = null;
		private static final Object cronThreadLock = new Object();
		private static final Map<Integer, CronFormat> cronJobs = new HashMap<Integer, CronFormat>();
		private static boolean stopCron = false;
		private static final AtomicInteger jobIDs = new AtomicInteger(1);
		
		/**
		 * Stops a job from running again, and returns true if the value was
		 * actually removed. False is returned otherwise.
		 * @return
		 * @param jobID The job ID
		 */
		public static boolean stopJob(int jobID){
			synchronized(cronJobs){
				if(cronJobs.containsKey(jobID)){
					cronJobs.remove(jobID);
					return true;
				} else {
					return false;
				}
			}
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.FormatException};
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
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			//First things first, check the format of the arguments.
			if(!(args[0] instanceof CString)){
				throw new Exceptions.CastException("Expected string for argument 1 in " + getName(), t);
			}
			if(!(args[1] instanceof CClosure)){
				throw new Exceptions.CastException("Expected closure for argument 2 in " + getName(), t);
			}
			CronFormat format = validateFormat(args[0].val(), t);
			format.job = ((CClosure)args[1]);
			//At this point, the format is complete. We need to start up the cron thread if it's not running, and
			//then register this job, as well as inform clear_task of this id.
			synchronized(cronThreadLock){
				if(cronThread == null){
					final DaemonManager dm = environment.getEnv(GlobalEnv.class).GetDaemonManager();
					stopCron = false;
					StaticLayer.GetConvertor().addShutdownHook(new Runnable() {

						@Override
						public void run() {
							cronThread = null;
							stopCron = true;
							synchronized(cronThreadLock){
								cronThreadLock.notifyAll();
							}
						}
					});
					cronThread = new Thread(new Runnable() {

						@Override
						public void run() {
							long lastMinute = 0;
							while(!stopCron){
								//We want to check to make sure that we only run once per minute, even though the
								//checks happen every second. This ensures that we have a fast enough sampling
								//period, while ensuring that we don't repeat tasks within the same minute.
								if((System.currentTimeMillis() / 1000 / 60) > lastMinute){
									//Set the lastMinute value to now
									lastMinute = System.currentTimeMillis() / 1000 / 60;
									//Activate
									synchronized(cronJobs){
										Calendar c = Calendar.getInstance();
										for(final CronFormat f : cronJobs.values()){
											//Check to see if it is currently time to run each job
											if(f.min.contains(c.get(Calendar.MINUTE))
													&& f.hour.contains(c.get(Calendar.HOUR_OF_DAY))
													&& f.day.contains(c.get(Calendar.DAY_OF_MONTH))
													&& f.month.contains(c.get(Calendar.MONTH) + 1)
													&& f.dayOfWeek.contains(c.get(Calendar.DAY_OF_WEEK) - 1)
													){
												//All the fields match, so let's trigger this job
												StaticLayer.GetConvertor().runOnMainThreadLater(dm, new Runnable() {

													@Override
													public void run() {
														try {
															f.job.execute();
														} catch(ConfigRuntimeException ex){
															ConfigRuntimeException.React(ex, f.job.getEnv());
														}
													}
												});
											}
										}
									}
								} //else continue, we'll wait another second.
								synchronized(cronThreadLock){
									try {
										cronThreadLock.wait(1000);
									} catch (InterruptedException ex) {
										//Continue
									}
								}
							}
							dm.deactivateThread(cronThread);
						}
					}, Implementation.GetServerType().getBranding() + "-CronDaemon");
					dm.activateThread(cronThread);
					cronThread.start();
				}
			}
			int jobID = jobIDs.getAndIncrement();
			synchronized(cronJobs){
				cronJobs.put(jobID, format);
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
		
		private CronFormat validateFormat(String format, Target t){
			//Now we need to look at the format of the cron task, and convert it to a standardized format.
			//Our goal here is to remove all ranges, predefined names, including @hourly and January.
			format = format.trim();
			//Changes tabs to spaces
			format = format.replace("\t", " ");
			//Change multiple spaces to one space
			format = format.replaceAll("( )+", " ");
			//Lowercase everything
			format = format.toLowerCase();
			if("@yearly".equals(format) || "@annually".equals(format)){
				format = "0 0 1 1 *";
			}
			if("@monthly".equals(format)){
				format = "0 0 1 * *";
			}
			if("@weekly".equals(format)){
				format = "0 0 * * 0";
			}
			if("@daily".equals(format)){
				format = "0 0 * * *";
			}
			if("@hourly".equals(format)){
				format = "0 * * * *";
			}
			//Check for invalid characters
			if(format.matches("[^a-z0-9\\*\\-,@/]")){
				throw new Exceptions.FormatException("Invalid characters found in format for " + getName() + ": \"" + format + "\". Check your format and try again.", t);
			}
			//Now split into the segments.
			String[] sformat = format.split(" ");
			if(sformat.length != 5){
				throw new Exceptions.FormatException("Expected 5 segments in " + getName() + ", but " + StringUtils.PluralTemplateHelper(sformat.length, "%d was", "%d were") + " found.", t);
			}
			String min = sformat[0];
			String hour = sformat[1];
			String day = sformat[2];
			String month = sformat[3];
			String dayOfWeek = sformat[4];
			
			//Now replace the special shortcut names
			for(String key : MONTHS.keySet()){
				month = month.replace(key, Integer.toString(MONTHS.get(key)));
			}
			for(String key : DAYS.keySet()){
				dayOfWeek = dayOfWeek.replace(key, Integer.toString(DAYS.get(key)));
			}
			for(String key : HOURS.keySet()){
				hour = hour.replace(key, Integer.toString(HOURS.get(key)));
			}
			//Split on commas
			List<String> minList = new ArrayList<String>(Arrays.asList(min.split(",")));
			List<String> hourList = new ArrayList<String>(Arrays.asList(hour.split(",")));
			List<String> dayList = new ArrayList<String>(Arrays.asList(day.split(",")));
			List<String> monthList = new ArrayList<String>(Arrays.asList(month.split(",")));
			List<String> dayOfWeekList = new ArrayList<String>(Arrays.asList(dayOfWeek.split(",")));
			
			List<List<String>> segments = Arrays.asList(minList, hourList, dayList, monthList, dayOfWeekList);
			//Now go through each and pull out any ranges. At this point, everything
			//is numbers or *
			for(int i = 0; i < segments.size(); i++){
				List<String> segment = segments.get(i);
				Iterator<String> it = segment.iterator();
				List<String> addAll = new ArrayList<String>();
				Range range = RANGES.get(i);
				while(it.hasNext()){
					String part = it.next();
					Matcher rangeMatcher = RANGE.matcher(part);
					if(rangeMatcher.find()){
						it.remove();
						Integer minRange = Integer.parseInt(rangeMatcher.group(1));
						Integer maxRange = Integer.parseInt(rangeMatcher.group(2));
						Range r = new Range(minRange, maxRange);
						if(!r.isAscending()){
							throw new Exceptions.FormatException("Ranges must be min to max, and not the same value in format for " + getName(), t);
						}
						List<Integer> rr = r.getRange();
						for(int j = 0; j < rr.size(); j++){
							addAll.add(Integer.toString(rr.get(j)));
						}
						continue;
					}
					Matcher everyMatcher = EVERY.matcher(part);
					if(everyMatcher.find()){
						it.remove();
						Integer every = Integer.parseInt(everyMatcher.group(1));
						for(int j = range.getMin(); j <= range.getMax(); j+=every){
							addAll.add(Integer.toString(j));
						}
					}
					if("*".equals(part)){
						it.remove();
						for(int j = range.getMin(); j <= range.getMax(); j++){
							addAll.add(Integer.toString(j));
						}
					}
				}
				segment.addAll(addAll);
				Collections.sort(segment);
			}
			//Everything is ints now, so parse it into a CronFormat object.
			CronFormat f = new CronFormat();
			for(int i = 0; i < 5; i++){
				List<Integer> list = new ArrayList<Integer>();
				List<String> segment = segments.get(i);
				Range range = RANGES.get(i);
				for(String s : segment){
					try{
						list.add(Integer.parseInt(s));
					} catch (NumberFormatException ex){
						//Any unexpected strings would show up here. The expected string values would have already
						//been replaced with a number, so this should work if there are no errors.
						throw new Exceptions.FormatException("Unknown string passed in format for " + getName() + " \"" + s + "\"", t);
					}
				}
				Collections.sort(list);
				if(!range.contains(list.get(0))){
					throw new Exceptions.FormatException("Expecting value to be within the range " + range + " in format for " + getName() + ", but the value was " + list.get(0), t);
				}
				if(!range.contains(list.get(list.size() - 1))){
					throw new Exceptions.FormatException("Expecting value to be within the range " + range + " in format for " + getName() + ", but the value was " + list.get(list.size() - 1), t);
				}
				Set<Integer> set = new TreeSet<Integer>(list);
				switch(i){
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
			public Set<Integer> min = new HashSet<Integer>();
			public Set<Integer> hour = new HashSet<Integer>();
			public Set<Integer> day = new HashSet<Integer>();
			public Set<Integer> month = new HashSet<Integer>();
			public Set<Integer> dayOfWeek = new HashSet<Integer>();
			
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
			return CHVersion.V3_3_1;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC);
		}

		@Override
		public ParseTree optimizeDynamic(Target t, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException {
			if(children.get(0).isConst()){
				if(children.get(0).getData() instanceof CString){
					validateFormat(children.get(0).getData().val(), t);
				}
			}
			return null;
		}
		
	}
	
	@api
	public static class clear_cron extends AbstractFunction {

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.RangeException};
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
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			Integer id = (Integer)environment.getEnv(GlobalEnv.class).GetCustom("cron-task-id");
			if(args.length == 1){
				id = (int)Static.getInt(args[0], t);
			}
			if(id == null){
				throw new Exceptions.RangeException("No task ID provided, and not running from within a cron task.", t);
			}
			if(!set_cron.stopJob(id)){
				throw new Exceptions.RangeException("Task ID invalid", t);
			}
			return new CVoid(t);
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
			return CHVersion.V3_3_1;
		}
		
	}
	
}
