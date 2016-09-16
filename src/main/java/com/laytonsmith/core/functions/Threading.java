
package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.core;
import com.laytonsmith.annotations.noboilerplate;
import com.laytonsmith.annotations.seealso;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CClosure;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREInsufficientArgumentsException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.FunctionReturnException;
import com.laytonsmith.core.exceptions.LoopManipulationException;
import com.laytonsmith.core.exceptions.ProgramFlowManipulationException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

/**
 *
 */
@core
public class Threading {
	public static String docs(){
		return "This experimental and private API is subject to removal, or incompatible changes, and should not"
				+ " be yet heavily relied on in normal development.";
	}

	@api
	@noboilerplate
	@seealso({x_run_on_main_thread_later.class, x_run_on_main_thread_now.class})
	public static class x_new_thread extends AbstractFunction {

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
		public Construct exec(final Target t, final Environment environment, Construct... args) throws ConfigRuntimeException {
			String id = args[0].val();
			if(!(args[1] instanceof CClosure)){
				throw new CRECastException("Expected closure for arg 2", t);
			}
			final CClosure closure = (CClosure) args[1];
			new Thread(new Runnable() {

				@Override
				public void run() {
					DaemonManager dm = environment.getEnv(GlobalEnv.class).GetDaemonManager();
					dm.activateThread(Thread.currentThread());
					try {
						closure.execute();
					} catch(FunctionReturnException ex){
						// Do nothing
					} catch(LoopManipulationException ex){
						ConfigRuntimeException.HandleUncaughtException(ConfigRuntimeException.CreateUncatchableException("Unexpected loop manipulation"
								+ " operation was triggered inside the closure.", t), environment);
					} catch(ConfigRuntimeException ex){
						ConfigRuntimeException.HandleUncaughtException(ex, environment);
					} catch(CancelCommandException ex){
						if(ex.getMessage() != null){
							new Echoes.console().exec(t, environment, new CString(ex.getMessage(), t), CBoolean.FALSE);
						}
					} finally {
						dm.deactivateThread(Thread.currentThread());
					}
				}
			}, "(" + Implementation.GetServerType().getBranding() + ") " + id).start();
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "x_new_thread";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "void {id, closure} Creates a new thread, named id, and runs the closure on that thread."
					+ " Note that many operations are not advisable to be run on other threads, and unless otherwise"
					+ " stated, functions are generally not thread safe. You can use " + new x_run_on_main_thread_later().getName()
					+ "() and " + new x_run_on_main_thread_now().getName() + "() to ensure operations will be run"
					+ " correctly, however.";
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			String get_current_thread = new x_get_current_thread().getName();
			String new_thread = this.getName();
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "msg(" + get_current_thread + "());\n"
						+ new_thread + "('myThread', closure(){\n"
						+ "\tsleep(5); // Sleep here, to allow the main thread to get well past us, for demonstration purposes\n"
						+ "\tmsg(" + get_current_thread + "());\n"
						+ "});\n"
						+ "msg('End of main thread');",
						"MainThread\n"
						+ "End of main thread\n"
						+ "myThread")
			};
		}

	}

	@api
	public static class x_get_current_thread extends AbstractFunction {

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
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			return new CString(Thread.currentThread().getName(), t);
		}

		@Override
		public String getName() {
			return "x_get_current_thread";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "string {} Returns the thread id (thread name) of the currently running thread.";
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", this.getName() + "()", "MainThread")
			};
		}

	}

	@api
	@noboilerplate
	@seealso({x_run_on_main_thread_now.class})
	public static class x_run_on_main_thread_later extends AbstractFunction {

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
		public Construct exec(final Target t, final Environment environment, Construct... args) throws ConfigRuntimeException {
			final CClosure closure = Static.getObject(args[0], t, CClosure.class);
			StaticLayer.GetConvertor().runOnMainThreadLater(environment.getEnv(GlobalEnv.class).GetDaemonManager(), new Runnable() {

				@Override
				public void run() {
					try {
						closure.execute();
					} catch(ConfigRuntimeException e){
						ConfigRuntimeException.HandleUncaughtException(e, environment);
					} catch(ProgramFlowManipulationException e){
						// Ignored
					}
				}
			});
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "x_run_on_main_thread_later";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "void {closure} Runs the closure on the main thread later. If the function call is itself being run from the main thread, then"
					+ " the function still will not block, but it is not an error to call this from the main thread. If an exception is thrown"
					+ " from the closure, it is handled using the uncaught exception handler.";
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_1;
		}

	}

	@api
	@noboilerplate
	@seealso({x_run_on_main_thread_later.class})
	public static class x_run_on_main_thread_now extends AbstractFunction {

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
		public Construct exec(final Target t, final Environment environment, Construct... args) throws ConfigRuntimeException {
			final CClosure closure = Static.getObject(args[0], t, CClosure.class);
			Object ret;
			try {
				ret = StaticLayer.GetConvertor().runOnMainThreadAndWait(new Callable<Object>() {

					@Override
					public Object call() throws Exception {
						try {
							closure.execute();
						} catch(FunctionReturnException e){
							return e.getReturn();
						} catch(ConfigRuntimeException | ProgramFlowManipulationException e){
							return e;
						}
						return CNull.NULL;
					}
				});


			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
			if(ret instanceof RuntimeException){
				throw (RuntimeException)ret;
			} else {
				return (Construct) ret;
			}
		}

		@Override
		public String getName() {
			return "x_run_on_main_thread_now";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "mixed {closure} Runs the closure on the main thread now, blocking the current thread until it is finished."
					+ " If the function call is itself being run from the main thread, then"
					+ " the function still will block as expected; it is not an error to call this from the main thread. Unlike"
					+ " running on the main thread later, if the underlying code throws an exception, it is thrown as a normal part of"
					+ " the execution. If the closure returns a value, it is returned by " + getName() + ".";
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_1;
		}

	}

	@api
	@noboilerplate
	@seealso({x_new_thread.class})
	public static class _synchronized extends AbstractFunction {
		private static final HashMap<String, Integer> syncObjectMap = new HashMap<String, Integer>();
		
		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInsufficientArgumentsException.class};
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
		public Construct exec(final Target t, final Environment environment, Construct... args) throws ConfigRuntimeException {
			
			// Check argument size and class.
			if(args.length < 2) {
				throw new CREInsufficientArgumentsException(getName() + " requires at least 2 arguments.", t);
			}
			final CString cSyncName = Static.getObject(args[0], t, CString.class);
			final CClosure closure = Static.getObject(args[1], t, CClosure.class);
			
			// Get the String reference to synchronize or use the passed argument.
			String syncName = cSyncName.val();
			String syncStr;
			synchronized(syncObjectMap) {
				searchLabel: {
					for(Entry<String, Integer> entry : syncObjectMap.entrySet()) {
						if(entry.getKey().equals(syncName)) {
							syncStr = entry.getKey();
							entry.setValue(entry.getValue() + 1);
							break searchLabel;
						}
					}
					syncObjectMap.put(syncName, 1);
					syncStr = syncName;
				}
			}
			
			// Get the closure arguments.
			Construct[] closureArgs;
			if(args.length <= 2) {
				closureArgs = null;
			} else {
				closureArgs = new Construct[args.length - 2];
				System.arraycopy(args, 2, closureArgs, 0, closureArgs.length);
			}
			
			// Execute the closure, synchronized using the given or already existing object reference.
			Construct ret = null;
			RuntimeException ex = null;
			synchronized(syncStr) {
				try {
					closure.execute(closureArgs);
					ret = CVoid.VOID;
				} catch(FunctionReturnException e) {
					ret = e.getReturn();
				} catch(ConfigRuntimeException | ProgramFlowManipulationException e) {
					ex = e;
				}
			}
			
			// Remove 1 from the call count or remove the synchronize object if there's no thread waiting for it.
			synchronized(syncObjectMap) {
				int count = syncObjectMap.get(syncStr); // This should never return null.
				if(count <= 1) {
					syncObjectMap.remove(syncStr);
				} else {
					for(Entry<String, Integer> entry : syncObjectMap.entrySet()) {
						if(entry.getKey() == syncName) { // Equals by reference.
							entry.setValue(count - 1);
							break;
						}
					}
				}
			}
			
			// Throw the RuntimeException or return the return value.
			if(ex != null) {
				throw ex;
			}
			return ret;
		}
		
		@Override
		public String getName() {
			return "synchronized";
		}
		
		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}
		
		@Override
		public String docs() {
			return "mixed {string, closure} Synchronizes the code in the closure for all calls with the same string argument."
					+ " This means that if two threads will call " + getName() + "('example', @someClosure), the second call"
					+ " will hang the thread until the code in the closure of the first call has finished executing."
					+ " If you call this function from within a closure in this function, the closure will simply be executed."
					+ " Returns the return value of the closure.";
		}
		
		@Override
		public Version since() {
			return CHVersion.V3_3_2;
		}
		
	}
	
}
