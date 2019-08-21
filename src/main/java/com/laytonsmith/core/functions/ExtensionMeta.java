package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.seealso;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.Optimizable;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.events.Event;
import com.laytonsmith.core.events.EventList;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.extensions.ExtensionManager;
import com.laytonsmith.core.extensions.ExtensionTracker;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.net.URL;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
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
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
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
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			try {
				FunctionList.getFunction(args[0].val().toLowerCase(), environment.getEnvClasses(), t);
			} catch (ConfigCompileException ex) {
				return CBoolean.FALSE;
			}

			return CBoolean.TRUE;
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
					+ " may or may not exist, such as functions that might or might not be loaded in an extension,"
					+ " or from different versions."
					+ " This is useful for shared code in environments where an extension may or may not"
					+ " be available, or an older version of " + Implementation.GetServerType().getBranding()
					+ ". if(function_exists('my_extension_function')){ my_extension_function() } can"
					+ " then be used to selectively \"bypass\" the compiler restrictions that would normally cause a fatal"
					+ " compile error, since that function is missing. Therefore, you can wrap extension related code"
					+ " around extension specific blocks, and make that code portable to other installations that"
					+ " may not have the extension installed.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC);
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {
			if(children.size() != 1) {
				throw new ConfigCompileException(getName() + " can only accept one argument", t);
			}

			if(!(children.get(0).getData().isInstanceOf(CString.TYPE))) {
				throw new ConfigCompileException(getName() + " can only accept hardcoded string values", t);
			}

			CBoolean val;
			try {
				FunctionList.getFunction(children.get(0).getData().val(), envs, t);
				val = CBoolean.TRUE;
			} catch (ConfigCompileException ex) {
				val = CBoolean.FALSE;
			}

			return new ParseTree(val, children.get(0).getFileOptions());
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
	@seealso(function_exists.class)
	public static class event_exists extends AbstractFunction implements Optimizable {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
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
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			return CBoolean.get(EventList.getEvent(args[0].val().toLowerCase()) != null);
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
					+ Implementation.GetServerType().getBranding() + "."
					+ " Like function_exists(), this function is resolved at compile time,"
					+ " and allows for conditional uses of events that may or may not exist.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC);
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {
			if(children.size() != 1) {
				throw new ConfigCompileException(getName() + " can only accept one argument", t);
			}

			if(!(children.get(0).getData().isInstanceOf(CString.TYPE))) {
				throw new ConfigCompileException(getName() + " can only accept hardcoded string values", t);
			}

			return new ParseTree(this.exec(t, null, children.get(0).getData()), children.get(0).getFileOptions());
		}
	}

	@api
	@seealso(function_exists.class)
	public static class extension_exists extends AbstractFunction implements Optimizable {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
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
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Map<URL, ExtensionTracker> trackers = ExtensionManager.getTrackers();
			for(ExtensionTracker tracker : trackers.values()) {
				String identifier = tracker.getIdentifier();
				if((identifier != null) && identifier.equalsIgnoreCase(args[0].val())) {
					return CBoolean.TRUE;
				}
			}
			return CBoolean.FALSE;
		}

		@Override
		public String getName() {
			return "extension_exists";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {name} Returns true if the extention is known to "
					+ Implementation.GetServerType().getBranding() + " and loaded."
					+ " Like function_exists(), this function is resolved at compile time.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC);
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {
			if(children.size() != 1) {
				throw new ConfigCompileException(getName() + " can only accept one argument", t);
			} else if(!(children.get(0).getData().isInstanceOf(CString.TYPE))) {
				throw new ConfigCompileException(getName() + " can only accept hardcoded string values", t);
			} else {
				return new ParseTree(this.exec(t, null, children.get(0).getData()), children.get(0).getFileOptions());
			}
		}
	}

	@api
	public static class extension_info extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
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
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Map<URL, ExtensionTracker> trackers = ExtensionManager.getTrackers();

			CArray retn = CArray.GetAssociativeArray(t);

			if(args.length == 0) {
				for(URL url : trackers.keySet()) {
					ExtensionTracker trk = trackers.get(url);
					CArray trkdata;

					if(!retn.containsKey(trk.getIdentifier())) {
						trkdata = CArray.GetAssociativeArray(t);
					} else {
						trkdata = (CArray) retn.get(trk.getIdentifier(), t);
					}

					// For both events and functions, make sure we don't overwrite
					// in cases of extensions with the same name. Shouldn't happen,
					// but lets handle it nicely. This will ALWAYS happen for old
					// style extensions, as they don't have an identifier.
					CArray funcs;
					if(!trkdata.containsKey("functions")) {
						funcs = new CArray(t);
					} else {
						funcs = (CArray) trkdata.get("functions", t);
					}
					for(FunctionBase func : trk.getFunctions()) {
						if(!funcs.contains(func.getName())) {
							funcs.push(new CString(func.getName(), t), t);
						}
					}
					funcs.sort(CArray.ArraySortType.STRING_IC);
					trkdata.set("functions", funcs, t);

					CArray events;
					if(!trkdata.containsKey("events")) {
						events = new CArray(t);
					} else {
						events = (CArray) trkdata.get("events", t);
					}
					for(Event event : trk.getEvents()) {
						events.push(new CString(event.getName(), t), t);
					}
					events.sort(CArray.ArraySortType.STRING_IC);
					trkdata.set("events", events, t);

					trkdata.set("version", trk.getVersion().toString());

					if(trk.getIdentifier() != null) {
						retn.set(trk.getIdentifier(), trkdata, t);
					} else {
						retn.set("__unidentified__", trkdata, t);
					}
				}
			} else {
				for(ExtensionTracker tracker : trackers.values()) {
					String identifier = tracker.getIdentifier();
					if(identifier == null) {
						identifier = "__unidentified__";
					}
					if(identifier.equals(args[0].val())) {
						CArray functions = (retn.containsKey("functions")) ? (CArray) retn.get("functions", t) : new CArray(t);
						for(FunctionBase function : tracker.getFunctions()) {
							if(!functions.contains(function.getName())) {
								functions.push(new CString(function.getName(), t), t);
							}
						}
						functions.sort(CArray.ArraySortType.STRING_IC);
						retn.set("functions", functions, t);
						CArray events = (retn.containsKey("events")) ? (CArray) retn.get("events", t) : new CArray(t);
						for(Event event : tracker.getEvents()) {
							if(!events.contains(event.getName())) {
								events.push(new CString(event.getName(), t), t);
							}
						}
						events.sort(CArray.ArraySortType.STRING_IC);
						retn.set("events", events, t);
						retn.set("version", tracker.getVersion().toString(), t);
					}
				}
			}

			return retn;
		}

		@Override
		public String getName() {
			return "extension_info";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "array {[extensionName]} Returns extension info for the extensions"
					+ " the system has loaded, or the given extension if extensionName is specified. Included data will be events,"
					+ " functions and version, keyed by the name of the extension"
					+ " (or __unidentified__ if it's an old-style extension).";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}
	}
}
