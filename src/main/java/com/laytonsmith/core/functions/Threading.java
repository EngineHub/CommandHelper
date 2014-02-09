
package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.hide;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.constructs.CClosure;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;

/**
 *
 */
public class Threading {
	public String docs(){
		return "This experimental and private API is subject to removal, or incompatible changes, and should not"
				+ " be used in normal development.";
	}
	
	@api
	@hide("experimental")
	public static class x_new_thread extends AbstractFunction {

		@Override
		public Exceptions.ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
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
		public Construct exec(Target t, final Environment environment, Construct... args) throws ConfigRuntimeException {
			String id = args[0].val();
			if(!(args[1] instanceof CClosure)){
				throw new Exceptions.CastException("Expected closure for arg 1", t);
			}
			final CClosure closure = (CClosure) args[1];
			new Thread(new Runnable() {

				@Override
				public void run() {
					DaemonManager dm = environment.getEnv(GlobalEnv.class).GetDaemonManager();
					dm.activateThread(Thread.currentThread());
					closure.execute();
					dm.deactivateThread(Thread.currentThread());
				}
			}, id).start();
			return new CVoid(t);
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
			return "void {id, closure} Creates a new thread, named id, and runs the closure.";
		}

		@Override
		public Version since() {
			return CHVersion.V0_0_0;
		}
		
	}

}
