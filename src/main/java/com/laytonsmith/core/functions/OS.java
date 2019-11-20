package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.OSUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.CRE.CREUnsupportedOperationException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 *
 */
public class OS {
	public static String docs() {
		return "Contains various methods for interacting with the Operating System. Some of the functions deal with"
				+ " OS specific mechanisms.";
	}


	@api
	public static class get_pid extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREUnsupportedOperationException.class};
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
			try {
				return new CInt(OSUtils.GetMyPid(), t);
			} catch (UnsupportedOperationException ex) {
				throw new CREUnsupportedOperationException(ex, t);
			}
		}

		@Override
		public String getName() {
			return "get_pid";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "int {} Returns the process id (pid) of the current process. In some implementations of Java, this"
					+ " cannot be relied on, and in those cases, an UnsupportedOperationException is thrown. In Java 9"
					+ " and above, this can generally be relied upon to work correctly, however.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_4;
		}

	}
}
