package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CClosure;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.ProgramFlowManipulationException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class ExecutionQueue {

	public static String docs() {
		return "An execution queue is a queue of closures, which are"
				+ " queued up to be run in sequence by the engine."
				+ " Unlike set_timeout and set_interval, there is no"
				+ " time component, it's simply a queue of operations"
				+ " to execute sequentially. See the"
				+ " [[Execution_Queue|article on the learning trail]]"
				+ " for more information.";
	}

	@api(environments = {GlobalEnv.class})
	public static class queue_push extends AbstractFunction {

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
			return null;
		}

		@Override
		public Mixed exec(Target t, final Environment environment, Mixed... args) throws ConfigRuntimeException {
			final CClosure c;
			String queue = null;
			if(!(args[0].isInstanceOf(CClosure.TYPE))) {
				throw new CRECastException("Parameter 1 to " + getName() + " must be a closure.", t);
			}
			c = ((CClosure) args[0]);
			if(args.length == 2) {
				queue = Construct.nval(args[1]);
			}

			environment.getEnv(GlobalEnv.class).GetExecutionQueue().push(environment.getEnv(GlobalEnv.class).GetDaemonManager(), queue, new Runnable() {

				@Override
				public void run() {
					try {
						StaticLayer.GetConvertor().runOnMainThreadAndWait(new Callable<Object>() {

							@Override
							public Object call() throws Exception {
								try {
									c.executeCallable();
								} catch (ConfigRuntimeException ex) {
									ConfigRuntimeException.HandleUncaughtException(ex, environment);
								} catch (ProgramFlowManipulationException ex) {
									// Ignored
								}
								return null;
							}
						});
					} catch (InterruptedException ex) {
						// ignore
					} catch (Exception ex) {
						Logger.getLogger(ExecutionQueue.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
			});

			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "queue_push";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "void {closure, [queue]} Queues a task up at the end of the specified queue.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

	}

	@api(environments = {GlobalEnv.class})
	public static class queue_push_front extends AbstractFunction {

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
			return null;
		}

		@Override
		public Mixed exec(Target t, final Environment environment, Mixed... args) throws ConfigRuntimeException {
			final CClosure c;
			String queue = null;
			if(!(args[0].isInstanceOf(CClosure.TYPE))) {
				throw new CRECastException("Parameter 1 to " + getName() + " must be a closure.", t);
			}
			c = ((CClosure) args[0]);
			if(args.length == 2) {
				queue = Construct.nval(args[1]);
			}

			environment.getEnv(GlobalEnv.class).GetExecutionQueue().pushFront(environment.getEnv(GlobalEnv.class).GetDaemonManager(), queue, new Runnable() {

				@Override
				public void run() {
					try {
						StaticLayer.GetConvertor().runOnMainThreadAndWait(new Callable<Object>() {

							@Override
							public Object call() throws Exception {
								try {
									c.executeCallable();
								} catch (ConfigRuntimeException ex) {
									ConfigRuntimeException.HandleUncaughtException(ex, environment);
								} catch (ProgramFlowManipulationException ex) {
									// Ignored
								}
								return null;
							}
						});
					} catch (InterruptedException ex) {
						// ignore
					} catch (Exception ex) {
						Logger.getLogger(ExecutionQueue.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
			});

			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "queue_push_front";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "void {closure, [queue]} Queues a task at the front of the queue.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

	}

	@api(environments = {GlobalEnv.class})
	public static class queue_remove extends AbstractFunction {

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
			String queue = null;
			if(args.length == 1) {
				queue = Construct.nval(args[0]);
			}
			environment.getEnv(GlobalEnv.class).GetExecutionQueue().remove(queue);
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "queue_remove";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "void {[queue]} Removes the last task at the end of the queue from the queue.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

	}

	@api(environments = {GlobalEnv.class})
	public static class queue_remove_front extends AbstractFunction {

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
			String queue = null;
			if(args.length == 1) {
				queue = Construct.nval(args[0]);
			}
			environment.getEnv(GlobalEnv.class).GetExecutionQueue().removeFront(queue);
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "queue_remove_front";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "void {[queue]} Removes a task from the front of the queue. That is, the next task"
					+ " that would have been run is removed.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

	}

	@api(environments = {GlobalEnv.class})
	public static class queue_clear extends AbstractFunction {

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
			String queue = null;
			if(args.length == 1) {
				queue = Construct.nval(args[0]);
			}
			environment.getEnv(GlobalEnv.class).GetExecutionQueue().clear(queue);
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "queue_clear";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "void {[queue]} Clears out all tasks that are on the queue. If no tasks were"
					+ " on the queue, nothing happens.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

	}

	@api(environments = {GlobalEnv.class})
	public static class queue_running extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return null;
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
			String queue = null;
			if(args.length == 1) {
				queue = Construct.nval(args[0]);
			}
			return CBoolean.get(environment.getEnv(GlobalEnv.class).GetExecutionQueue().isRunning(queue));
		}

		@Override
		public String getName() {
			return "queue_running";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "boolean {[queue]} Returns true if the specified queue still has tasks running on it.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

	}

	@api(environments = {GlobalEnv.class})
	public static class queue_delay extends AbstractFunction {

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
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			String queue = null;
			if(args.length == 2) {
				queue = Construct.nval(args[1]);
			}
			final long delay = Static.getInt(args[0], t);
			environment.getEnv(GlobalEnv.class).GetExecutionQueue().push(environment.getEnv(GlobalEnv.class).GetDaemonManager(), queue, new Runnable() {

				@Override
				public void run() {
					try {
						Thread.sleep(delay);
					} catch (InterruptedException ex) {
						//
					}
				}
			});
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "queue_delay";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "void {x, [queue]} Queues up a non-disruptive sleep at the end of the queue. This task"
					+ " will stall the execution thread for x milliseconds.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

	}

	@api(environments = {GlobalEnv.class})
	public static class queue_delay_front extends AbstractFunction {

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
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			String queue = null;
			if(args.length == 2) {
				queue = Construct.nval(args[1]);
			}
			final long delay = Static.getInt(args[0], t);
			environment.getEnv(GlobalEnv.class).GetExecutionQueue().pushFront(environment.getEnv(GlobalEnv.class).GetDaemonManager(), queue, new Runnable() {

				@Override
				public void run() {
					try {
						Thread.sleep(delay);
					} catch (InterruptedException ex) {
						//
					}
				}
			});
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "queue_delay_front";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "void {x, [queue]} Works like queue_delay, but puts the delay at the front of the queue.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

	}
}
