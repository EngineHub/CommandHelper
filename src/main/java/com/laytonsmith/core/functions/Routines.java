
package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CLock;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Construct.ConstructType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;

/**
 *
 */
public class Routines {
//	public static String docs(){
//		return "This class of functions provides Routines capabilities. Currently all these functions"
//				+ " are only available via cmdline.";
//	}
//	
//	@api public static class get_lock extends AbstractFunction {
//
//		@Override
//		public Exceptions.ExceptionType[] thrown() {
//			return new Exceptions.ExceptionType[]{};
//		}
//
//		@Override
//		public boolean isRestricted() {
//			return true;
//		}
//
//		@Override
//		public Boolean runAsync() {
//			return null;
//		}
//
//		@Override
//		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
//			return new CLock(t);
//		}
//
//		@Override
//		public String getName() {
//			return "get_lock";
//		}
//
//		@Override
//		public Integer[] numArgs() {
//			return new Integer[]{0};
//		}
//
//		@Override
//		public String docs() {
//			return "lock {} Returns a lock object, which can be used to provide a reference counting"
//					+ " mutex amongst threads.";
//		}
//
//		@Override
//		public Version since() {
//			return CHVersion.V3_3_1;
//		}
//		
//	}
//	
//	@api public static class is_lock extends AbstractFunction {
//
//		@Override
//		public Exceptions.ExceptionType[] thrown() {
//			return null;
//		}
//
//		@Override
//		public boolean isRestricted() {
//			return false;
//		}
//
//		@Override
//		public Boolean runAsync() {
//			return null;
//		}
//
//		@Override
//		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
//			return new CBoolean(args[0] instanceof CLock, t);
//		}
//
//		@Override
//		public String getName() {
//			return "is_lock";
//		}
//
//		@Override
//		public Integer[] numArgs() {
//			return new Integer[]{1};
//		}
//
//		@Override
//		public String docs() {
//			return "boolean {object} Returns true iff the object is a lock object.";
//		}
//
//		@Override
//		public Version since() {
//			return CHVersion.V3_3_1;
//		}
//		
//	}
//	
//	@api public static class routine extends AbstractFunction {
//
//		@Override
//		public Exceptions.ExceptionType[] thrown() {
//			return new Exceptions.ExceptionType[]{};
//		}
//
//		@Override
//		public boolean isRestricted() {
//			return true;
//		}
//
//		@Override
//		public Boolean runAsync() {
//			return null;
//		}
//
//		@Override
//		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
//			throw new UnsupportedOperationException("TODO: Not supported yet.");
//		}
//
//		@Override
//		public String getName() {
//			return "routine";
//		}
//
//		@Override
//		public Integer[] numArgs() {
//			return new Integer[]{Integer.MAX_VALUE};
//		}
//
//		@Override
//		public String docs() {
//			return "";
//		}
//
//		@Override
//		public Version since() {
//			return CHVersion.V3_3_1;
//		}
//		
//	}
}
