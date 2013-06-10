
package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.arguments.Argument;
import com.laytonsmith.core.arguments.ArgumentBuilder;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CClosure;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 *
 * @author Layton
 */
public class ExecutionQueue {
	public static String docs(){
		return "An execution queue is a queue of closures, which are"
			+ " queued up to be run in sequence by the engine."
			+ " Unlike set_timeout and set_interval, there is no"
			+ " time component, it's simply a queue of operations"
			+ " to execute sequentially. See the"
			+ " [[CommandHelper/Execution_Queue|article on the learning trail]]"
			+ " for more information.";
	}
	
	@api(environments={GlobalEnv.class})
	public static class queue_push extends AbstractFunction{

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			final CClosure c;
			String queue = null;
			if(!(args[0] instanceof CClosure)){
				throw new ConfigRuntimeException("Parameter 1 to " + getName() + " must be a closure.", ExceptionType.CastException, t);
			}
			c = ((CClosure)args[0]);
			if(args.length == 2){
				queue = args[1].val();
			}
			
			environment.getEnv(GlobalEnv.class).GetExecutionQueue().push(environment.getEnv(GlobalEnv.class).GetDaemonManager(), queue, new Runnable() {

				public void run() {
					StaticLayer.SetFutureRunnable(0, new Runnable() {

						public void run() {							
							c.execute(null);
						}
					});
				}
			});
			
			return new CVoid(t);
		}

		public String getName() {
			return "queue_push";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "Queues a task up at the end of the specified queue.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("The task to push on to the queue", CClosure.class, "closure"),
						new Argument("The queue name", CString.class, "queue").setOptionalDefaultNull()
					);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}
	
	@api(environments={GlobalEnv.class})
	public static class queue_push_front extends AbstractFunction{

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			final CClosure c;
			String queue = null;
			if(!(args[0] instanceof CClosure)){
				throw new ConfigRuntimeException("Parameter 1 to " + getName() + " must be a closure.", ExceptionType.CastException, t);
			}
			c = ((CClosure)args[0]);
			if(args.length == 2){
				queue = args[1].val();
			}
			
			environment.getEnv(GlobalEnv.class).GetExecutionQueue().pushFront(environment.getEnv(GlobalEnv.class).GetDaemonManager(), queue, new Runnable() {

				public void run() {
					StaticLayer.SetFutureRunnable(0, new Runnable() {

						public void run() {							
							c.execute(null);
						}
					});
				}
			});
			
			return new CVoid(t);
		}

		public String getName() {
			return "queue_push_front";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "Queues a task at the front of the queue.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("The task to push on to the queue", CClosure.class, "closure"),
						new Argument("The queue name", CString.class, "queue").setOptionalDefaultNull()
					);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}
	
	@api(environments={GlobalEnv.class})
	public static class queue_remove extends AbstractFunction{

		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			String queue = null;
			if(args.length == 1){
				queue = args[0].val();
			}
			environment.getEnv(GlobalEnv.class).GetExecutionQueue().remove(queue);
			return new CVoid(t);
		}

		public String getName() {
			return "queue_remove";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		public String docs() {
			return "Removes the last task at the end of the queue from the queue.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("The queue name", CString.class, "queue").setOptionalDefaultNull()
					);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}
	
	@api(environments={GlobalEnv.class})
	public static class queue_remove_front extends AbstractFunction{

		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			String queue = null;
			if(args.length == 1){
				queue = args[0].val();
			}
			environment.getEnv(GlobalEnv.class).GetExecutionQueue().removeFront(queue);
			return new CVoid(t);
		}

		public String getName() {
			return "queue_remove_front";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		public String docs() {
			return "Removes a task from the front of the queue. That is, the next task"
				+ " that would have been run is removed.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("The queue name", CString.class, "queue").setOptionalDefaultNull()
					);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}
	
	@api(environments={GlobalEnv.class})
	public static class queue_clear extends AbstractFunction{

		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			String queue = null;
			if(args.length == 1){
				queue = args[0].val();
			}
			environment.getEnv(GlobalEnv.class).GetExecutionQueue().clear(queue);
			return new CVoid(t);
		}

		public String getName() {
			return "queue_clear";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		public String docs() {
			return "Clears out all tasks that are on the queue. If no tasks were"
				+ " on the queue, nothing happens.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("The queue name", CString.class, "queue").setOptionalDefaultNull()
					);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}
	
	@api(environments={GlobalEnv.class})
	public static class queue_running extends AbstractFunction{

		public ExceptionType[] thrown() {
			return null;
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			String queue = null;
			if(args.length == 1){
				queue = args[0].val();
			}
			return new CBoolean(environment.getEnv(GlobalEnv.class).GetExecutionQueue().isRunning(queue), t);
		}

		public String getName() {
			return "queue_running";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		public String docs() {
			return "Returns true if the specified queue still has tasks running on it.";
		}
		
		public Argument returnType() {
			return new Argument("True, if the given queue is running", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("The queue name", CString.class, "queue").setOptionalDefaultNull()
					);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}
	
	@api(environments={GlobalEnv.class})
	public static class queue_delay extends AbstractFunction{

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			String queue = null;
			if(args.length == 2){
				queue = args[1].val();
			}
			final long delay = Static.getInt(args[0], t);
			environment.getEnv(GlobalEnv.class).GetExecutionQueue().push(environment.getEnv(GlobalEnv.class).GetDaemonManager(), queue, new Runnable() {

				public void run() {
					try {
						Thread.sleep(delay);
					} catch (InterruptedException ex) {
						//
					}
				}
			});
			return new CVoid(t);
		}

		public String getName() {
			return "queue_delay";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "Queues up a non-disruptive sleep at the end of the queue. This task"
				+ " will stall the execution thread for time milliseconds.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("The delay, in ms, to queue up", CInt.class, "time"),
						new Argument("The queue name", CString.class, "queue").setOptionalDefaultNull()
					);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}
	
	@api(environments={GlobalEnv.class})
	public static class queue_delay_front extends AbstractFunction{

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			String queue = null;
			if(args.length == 2){
				queue = args[1].val();
			}
			final long delay = Static.getInt(args[0], t);
			environment.getEnv(GlobalEnv.class).GetExecutionQueue().pushFront(environment.getEnv(GlobalEnv.class).GetDaemonManager(), queue, new Runnable() {

				public void run() {
					try {
						Thread.sleep(delay);
					} catch (InterruptedException ex) {
						//
					}
				}
			});
			return new CVoid(t);
		}

		public String getName() {
			return "queue_delay_front";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "Works like queue_delay, but puts the delay at the front of the queue.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("The delay, in ms, to queue up at the front of the queue", CInt.class, "time"),
						new Argument("The queue name", CString.class, "queue").setOptionalDefaultNull()
					);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}
}
