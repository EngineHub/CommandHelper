package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Optimizable;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.events.EventList;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Jason Unger <entityreborn@gmail.com>
 */
public class ExtensionMeta {
	public static String docs() {
		return "Provides the ability for finding out information about installed"
				+ " extensions, including events and functions.";
	}
	
	@api
	public static class function_exists extends AbstractFunction implements Optimizable {

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			try {
				FunctionList.getFunction(args[0].val().toLowerCase());
			} catch (ConfigCompileException ex) {
				return new CBoolean(false, t);
			}
			
			return new CBoolean(true, t);
		}

		@Override
		public String getName() {
			return "function_exists";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {name} Returns true if the function is known to "
					+ Implementation.GetServerType().getBranding() + ". This is a special function; it"
					+ " is resolved at compile time, and allows for conditional uses of functions that"
					+ " may or may not exist, such as functions that might or might not be loaded in an extension."
					+ " This is useful for shared code in environments where an extension may or may not"
					+ " be available. if(function_exists('my_extension_function')){ my_extension_function() } can"
					+ " then be used to selectively \"bypass\" the compiler restrictions that would normally cause a fatal"
					+ " compile error, since that function is missing. Therefore, you can wrap extension related code"
					+ " around extension specific blocks, and make that code portable to other installations that"
					+ " may not have the extension installed.";
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
			if (!(children.get(0).getData() instanceof CString)) {
				throw new ConfigCompileException(getName() + " can only accept hardcoded string values", t);
			}
			
			return new ParseTree(this.exec(t, null, children.get(0).getData()), children.get(0).getFileOptions());
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Wrapping a block of code that uses extension functions", 
						"if(function_exists('my_function')){\n"
						+ "\tmy_function()\n"
						+ "}\n", "<No errors will occur if the extension that contains my_function() isn't loaded>")
			};
		}
		
	}
	
	@api
	public static class event_exists extends AbstractFunction implements Optimizable {

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			boolean found = EventList.getEvent(args[0].val().toLowerCase()) != null;
			
			return new CBoolean(found, t);
		}

		@Override
		public String getName() {
			return "event_exists";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {name} Returns true if the event is known to "
					+ Implementation.GetServerType().getBranding() + ".";
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
			if (!(children.get(0).getData() instanceof CString)) {
				throw new ConfigCompileException(getName() + " can only accept hardcoded string values", t);
			}
			
			return new ParseTree(this.exec(t, null, children.get(0).getData()), children.get(0).getFileOptions());
		}
	}
}
