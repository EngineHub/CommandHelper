package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.api;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

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
//            if (Thread.currentThread().getName().equals("Server thread")) {
//                throw new ConfigRuntimeException("sleep() cannot be run in the main server thread", 
//                        null, t);
//            }
//            Construct x = args[0];
//            double time = Static.getNumber(x);
//            Integer i = (Integer) (Prefs.);
//            if (i > time || i <= 0) {
//                try {
//                    Thread.sleep((int)(time * 1000));
//                } catch (InterruptedException ex) {
//                }
//            } else {
//                throw new ConfigRuntimeException("The value passed to sleep must be less than the server defined value of " + i + " seconds or less.", 
//                        ExceptionType.RangeException, t);
//            }
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
			long time = Static.getInt(args[0]);
			int offset = 0;
			long delay = time;
			if (args.length == 3) {
				offset = 1;
				delay = Static.getInt(args[1]);
			}
			if (!(args[1 + offset] instanceof CClosure)) {
				throw new ConfigRuntimeException(getName() + " expects a closure to be sent as the second argument", ExceptionType.CastException, t);
			}
			final CClosure c = (CClosure) args[1 + offset];
			final AtomicInteger ret = new AtomicInteger(-1);

			ret.set(StaticLayer.SetFutureRepeater(time, delay, new Runnable() {
				public void run() {
					c.getEnv().getEnv(GlobalEnv.class).SetCustom("timeout-id", ret.get());
					try {
						ProfilePoint p = environment.getEnv(GlobalEnv.class).GetProfiler().start("Executing timeout with id " + ret.get() + " (defined at " + t.toString() + ")", LogLevel.ERROR);
						c.execute(null);
						p.stop();
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
			long time = Static.getInt(args[0]);
			if (!(args[1] instanceof CClosure)) {
				throw new ConfigRuntimeException(getName() + " expects a closure to be sent as the second argument", ExceptionType.CastException, t);
			}
			final CClosure c = (CClosure) args[1];
			final AtomicInteger ret = new AtomicInteger(-1);
			ret.set(StaticLayer.SetFutureRunnable(time, new Runnable() {
				public void run() {
					c.getEnv().getEnv(GlobalEnv.class).SetCustom("timeout-id", ret.get());
					try {
						ProfilePoint p = environment.getEnv(GlobalEnv.class).GetProfiler().start("Executing timeout with id " + ret.get() + " (defined at " + t.toString() + ")", LogLevel.ERROR);
						c.execute(null);
						p.stop();
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
				StaticLayer.ClearFutureRunnable((int) Static.getInt(args[0]));
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
			return new Integer[]{1, 2};
		}

		public String docs() {
			return getBundledDocs();
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
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
			if (args.length == 2) {
				now = new Date(Static.getInt(args[1]));
			}
			SimpleDateFormat dateFormat = new SimpleDateFormat(args[0].toString());
			return new CString(dateFormat.format(now), t);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Basic usage", "simple_date('h:mm a')"),
						new ExampleScript("Usage with quoted letters", "simple_date('yyyy.MM.dd G \\'at\\' HH:mm:ss z')"),
						new ExampleScript("Adding a single quote", "simple_date('EEE, MMM d, \\'\\'yy')"),
						new ExampleScript("Specifying alternate time", "simple_date('EEE, MMM d, \\'\\'yy', 0)"),
						new ExampleScript("With timezone", "simple_date('hh \\'o\\'\\'clock\\' a, zzzz')"),
						new ExampleScript("With simple timezone", "simple_date('hh \\'o\\'\\'clock\\' a, zzzz')"),
						new ExampleScript("With timezone", "simple_date('K:mm a, z')"),
						new ExampleScript("With 5 digit year", "simple_date('yyyyy.MMMMM.dd GGG hh:mm aaa')"),
						new ExampleScript("Long format", "simple_date('EEE, d MMM yyyy HH:mm:ss Z')"),
						new ExampleScript("Computer readable format", "simple_date('yyMMddHHmmssZ')"),
						new ExampleScript("With milliseconds", "simple_date('yyyy-MM-dd\\'T\\'HH:mm:ss.SSSZ')"),};
		}
	}
}
