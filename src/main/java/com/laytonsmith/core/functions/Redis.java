package com.laytonsmith.core.functions;

//import com.laytonsmith.PureUtilities.Version;
//import com.laytonsmith.annotations.api;
//import com.laytonsmith.core.CHVersion;
//import com.laytonsmith.core.constructs.Construct;
//import com.laytonsmith.core.constructs.Target;
//import com.laytonsmith.core.environments.Environment;
//import com.laytonsmith.core.exceptions.ConfigRuntimeException;
//import com.laytonsmith.core.functions.Exceptions.ExceptionType;
//import java.lang.reflect.Method;
//import java.util.Arrays;
//import java.util.List;
//import redis.clients.jedis.Jedis;
/**
 *
 */
public class Redis {

	public static String docs() {
		return "This class of functions provides hooks into a redis system.";
	}

//	@api
//	public static class redis extends AbstractFunction {
//
//		private static List<Method> functionList = null;
//		/**
//		 * The list of valid commands is built dynamically based on the functions listed in
//		 * the Jedis class. Only some functions are supported, but all of them are dynamically
//		 * generated. This function builds the list of supported methods, which can then be built
//		 * from. The general contract of the redis mscript function is that the command name
//		 * should be the function name, with the corresponding arguments following. The return
//		 * type of the function will vary, but will be based on the function's return type.
//		 * 
//		 * Only the public methods are supported.
//		 */
//		private static void BuildFunctionList(){
//			if(functionList != null){
//				return;
//			}
//			for(Method m : Jedis.class.getMethods()){
//				if(m.getReturnType() == Object.class){
//					//Generic returns aren't supported
//					continue;
//				}
//
//				for(Class c : m.getParameterTypes()){
//					c
//				}
//				functionList.add(m);
//			}
//		}
//
//		@Override
//		public ExceptionType[] thrown() {
//			return new ExceptionType[]{ExceptionType.CastException};
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
//			BuildFunctionList();
//			throw new UnsupportedOperationException("TODO: Not supported yet.");
//		}
//
//		@Override
//		public String getName() {
//			return "redis";
//		}
//
//		@Override
//		public Integer[] numArgs() {
//			return new Integer[]{Integer.MAX_VALUE};
//		}
//
//		@Override
//		public String docs() {
//			StringBuilder docs = new StringBuilder();
//
//			return docs.toString();
//		}
//
//		@Override
//		public Version since() {
//			return CHVersion.V3_3_1;
//		}
//
//	}
}
