package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.HeapDumper;
import com.laytonsmith.PureUtilities.TermColors;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.noboilerplate;
import com.laytonsmith.annotations.seealso;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.MethodScriptFileLocations;
import com.laytonsmith.core.Optimizable;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Prefs;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.IVariable;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREIOException;
import com.laytonsmith.core.exceptions.CRE.CREPluginInternalException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.core.natives.interfaces.Sizeable;
import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class Debug {

//	public static boolean EVENT_LOGGING = false;
//	public static int EVENT_LOGGING_LEVEL = 1;
//	public static final Set<Event.Type> EVENT_LOGGING_FILTER = new HashSet<Event.Type>();
//	public static final Set<String> EVENT_PLUGIN_FILTER = new HashSet<String>();
	//public static boolean LOG_TO_SCREEN = false;
//	public static void DoLog(Event.Type filter, int verbosity, String message) {
//		synchronized (EVENT_LOGGING_FILTER) {
//			if(EVENT_LOGGING && EVENT_LOGGING_FILTER.contains(filter) && EVENT_LOGGING_LEVEL >= verbosity) {
//				try {
//					Static.LogDebug(message);
//				} catch (IOException ex) {
//					Logger.getLogger(Debug.class.getVariableName()).log(Level.SEVERE, null, ex);
//				}
//			}
//		}
//	}
//
//	public static boolean IsFiltered(Plugin plugin) {
//		if(EVENT_PLUGIN_FILTER.isEmpty()) {
//			return true;
//		} else {
//			return EVENT_PLUGIN_FILTER.contains(plugin.getClass().getSimpleName().toUpperCase());
//		}
//	}
	public static String docs() {
		return "Provides methods for viewing data about both CommandHelper and the other plugins in your server. Though not meant to"
				+ " be called by normal scripts, these methods are available everywhere other methods are available. Note that for"
				+ " some of these functions to even work, play-dirty mode must set to on. These are most useful in conjuction with"
				+ " interpreter mode.";
	}

//	@api
//	public static class dump_listeners extends AbstractFunction {
//
//		public String getVariableName() {
//			return "dump_listeners";
//		}
//
//		public Integer[] numArgs() {
//			return new Integer[]{0, 1, 2};
//		}
//
//		public String docs() {
//			return " {[typeFilter], [verboseLevel]} Send null as the typeFilter to see possibilities. VerboseLevel can be 1-4";
//		}
//
//		public Class<? extends CREThrowable>[] thrown() {
//			return new Class[]{CRECastException.class, CRESecurityException.class};
//		}
//
//		public boolean isRestricted() {
//			return true;
//		}
//
//		public boolean preResolveVariables() {
//			return true;
//		}
//
//		public MSVersion since() {
//			return "0.0.0";
//		}
//
//		public Boolean runAsync() {
//			return false;
//		}
//
//		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
//			if(!(Boolean) Static.getPreferences().getPreference("allow-debug-logging")) {
//				throw new ConfigRuntimeException("allow-debug-logging is currently set to false. To use " + this.getVariableName() + ", enable it in your preferences.", CRESecurityException.class, t);
//			}
//			StringBuilder b = new StringBuilder("\n");
//			if(args.length >= 1 && args[0] instanceof CNull) {
//				b.append("You can sort the listeners further by specifying one of the options:\n");
//				for(Event.Type t : Event.Type.values()) {
//					b.append(t.name()).append("\n");
//				}
//				return new CString(b.toString(), 0, null);
//			}
//			int verbosity = 1;
//			if(args.length == 2) {
//				verbosity = Static.getInt32(args[1]);
//			}
//			try {
//				SimplePluginManager pm = (SimplePluginManager) AliasCore.parent.getServer().getPluginManager();
//				Field fListener = SimplePluginManager.class.getDeclaredField("listeners");
//				//set it to public
//				fListener.setAccessible(true);
//				EnumMap<Event.Type, SortedSet<RegisteredListener>> listeners =
//						(EnumMap<Event.Type, SortedSet<RegisteredListener>>) fListener.get(pm);
//
//				if(args.length >= 1) {
//					for(RegisteredListener l : listeners.get(Event.Type.valueOf(args[0].val().toUpperCase()))) {
//						b.append(Build(l, verbosity));
//					}
//				} else {
//					for(Event.Type type : listeners.keySet()) {
//						b.append("Type: ").append(type.name()).append("\n");
//						for(RegisteredListener l : listeners.get(type)) {
//							b.append(Build(l, verbosity));
//						}
//					}
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			return new CString(b.toString(), 0, null);
//		}
//
//		public String Build(RegisteredListener l, int verbosity) {
//			StringBuilder b = new StringBuilder();
//			switch (Static.Normalize(verbosity, 1, 5)) {
//				case 1:
//					b.append("Plugin: ").append(l.getPlugin().getClass().getSimpleName()).append("; Priority: ").append(l.getPriority().toString()).append("\n");
//					break;
//				case 2:
//					b.append("Plugin: ").append(l.getPlugin().getClass().getSimpleName()).append(":").append(l.getListener().getClass().getSimpleName()).append("; Priority: ").append(l.getPriority().toString()).append("\n");
//					break;
//				case 3:
//					b.append("Plugin: ").append(l.getPlugin().getClass().getSimpleName()).append(":").append(l.getListener().getClass().getCanonicalName()).append("; Priority: ").append(l.getPriority().toString()).append("\n");
//					break;
//				case 4:
//					b.append("Plugin: ").append(l.getPlugin().getClass().getSimpleName()).append(":").append(l.getListener().getClass().getCanonicalName()).append("\n\t").append("; Priority: ").append(l.getPriority().toString()).append("\n");
//					break;
//				case 5:
//					b.append("Plugin: ").append(l.getPlugin().getClass().getSimpleName()).append(":").append(l.getListener().getClass().getCanonicalName()).append("; Priority: ").append(l.getPriority().toString()).append("\n");
//					b.append("\tMethods defined in listener that override ");
//					try {
//						Class<? extends Listener> parent = (Class<? extends Listener>) l.getListener().getClass().getSuperclass();
//						while(parent.getSuperclass() != null && parent.getSuperclass().equals(Listener.class)) {
//							parent = (Class<? extends Listener>) parent.getSuperclass();
//						}
//						b.append(parent.getSimpleName()).append(":\n");
//						Set<Method> parentSet = new HashSet(Arrays.asList(parent.getDeclaredMethods()));
//						for(Method m : l.getListener().getClass().getDeclaredMethods()) {
//							for(Method pm : parentSet) {
//								if(pm.getVariableName().equals(m.getVariableName()) && Arrays.equals(pm.getParameterTypes(), m.getParameterTypes())) {
//									b.append("\t\t").append(m.getReturnType().getSimpleName()).append(" ").append(m.getVariableName()).append("(").append(Static.strJoin(m.getParameterTypes(), ", ")).append(");\n");
//								}
//							}
//						}
//					} catch (NoClassDefFoundError e) {
//						b.append("Could not get methods for ").append(l.getListener().getClass());
//					}
//					break;
//			}
//			return b.toString();
//		}
//
//		public String BuildClassList(Class[] list) {
//			StringBuilder b = new StringBuilder();
//			ArrayList<String> l = new ArrayList<String>();
//			for(Class c : list) {
//				try {
//					l.add(c.getSimpleName());
//				} catch (NoClassDefFoundError e) {
//				}
//			}
//			return Static.strJoin(list, ", ");
//		}
//	}
	@api(environments = {GlobalEnv.class})
	public static class debug extends AbstractFunction {

		@Override
		public String getName() {
			return "debug";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "void {message} Manually logs a timestamped message to the debug log and the console, if debug-mode is set to true in the preferences";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREIOException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return true;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if(Prefs.DebugMode()) {
				try {
					Static.LogDebug(MethodScriptFileLocations.getDefault().getConfigDirectory(), args[0].val(), LogLevel.DEBUG);
				} catch (IOException ex) {
					throw new CREIOException(ex.getMessage(), t, ex);
				}
			}
			return CVoid.VOID;
		}
	}

	@api
	@seealso(always_trace.class)
	public static class trace extends always_trace {

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			//TODO: Once Prefs are no longer static, check to see if debug mode is on during compilation, and
			//if so, remove this function entirely
			if(Prefs.DebugMode()) {
				return always_trace.doTrace(t, environment, args);
			}
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "trace";
		}

		@Override
		public String docs() {
			return "void {ivar} Works like {{function|always_trace}}, but only if debug-mode is enabled in the"
					+ " preferences. See {{function|always_trace}} for details of the output.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

	}

	@api
	@seealso(trace.class)
	public static class always_trace extends AbstractFunction implements Optimizable {

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
			return doTrace(t, environment, args);
		}

		@Override
		public boolean preResolveVariables() {
			return false;
		}

		@Override
		public String getName() {
			return "always_trace";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "void {ivar} Outputs debug information about a variable to standard out. Unlike {{function|debug}},"
					+ " this only accepts an ivar; it is a meta function. The runtime will then take the variable,"
					+ " and output information about it, in a human readable format, including the variable's"
					+ " defined type, actual type, name and value.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_4;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			// We have to hardcode the output here, because otherwise it just prints to stdout, and we don't curently
			// capture that.
			return new ExampleScript[]{
				new ExampleScript("Basic usage with auto type", "@m = 2;\n"
						+ "always_trace(@m);", "auto (actual type ms.lang.int) @m: 2"),
				new ExampleScript("With defined type", "int @i = 1;\n"
						+ "always_trace(@i);", "ms.lang.int (actual type ms.lang.int) @i: 1"),
				new ExampleScript("With subtype", "number @n = 2.0;\n"
						+ "always_trace(@n);", "ms.lang.number (actual type ms.lang.double) @n: 2.0")
			};
		}

		public static CVoid doTrace(Target t, Environment environment, Mixed... args) {
			if(args[0] instanceof IVariable) {
				IVariable ivar = environment.getEnv(GlobalEnv.class).GetVarList()
						.get(((IVariable) args[0]).getVariableName(), t, environment);
				Mixed val = ivar.ival();
				StreamUtils.GetSystemOut().println(
						TermColors.GREEN + environment.getEnv(GlobalEnv.class).GetStackTraceManager()
								.getCurrentStackTrace().get(0).getProcedureName()
						+ TermColors.RESET + ":"
						+ TermColors.YELLOW + t.file().getName()
						+ TermColors.RESET + ":"
						+ TermColors.CYAN + t.line() + "." + t.col()
						+ TermColors.RESET + ": "
						+ TermColors.BRIGHT_WHITE + ivar.getDefinedType()
						+ TermColors.RESET + " (actual type "
						+ TermColors.BRIGHT_WHITE + val.typeof()
						+ (val.isInstanceOf(Sizeable.TYPE) ? ", length: " + ((Sizeable) val).size() : "")
						+ TermColors.RESET + ") "
						+ TermColors.CYAN + ivar.getVariableName()
						+ TermColors.RESET + ": " + val.val());
				return CVoid.VOID;
			} else {
				throw new CRECastException("Expecting an ivar, but received " + args[0].typeof().getSimpleName()
						+ " instead", t);
			}
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC);
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs, List<ParseTree> children,
				FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {
			if(children.size() != 1) {
				throw new ConfigCompileException(getName() + " expects 1 parameter, but " + children.size()
						+ " were provided.", t);
			}
			ParseTree child = children.get(0);
			if(!(child.getData() instanceof IVariable)) {
				throw new ConfigCompileException(getName() + " can only accept an ivar as the argument.", t);
			}
			return null;
		}

	}

//	@api
//	public static class debug_log_events extends AbstractFunction {
//
//		public String getVariableName() {
//			return "debug_log_events";
//		}
//
//		public Integer[] numArgs() {
//			return new Integer[]{1, 2, 3};
//		}
//
//		public String docs() {
//			return "void {boolean, [level, [logToScreen]]} Turns the event logging on or off. Event logging may be useful in determining the problem if CommandHelper isn't"
//					+ " able to receive events, you can track what's actually happening. play-dirty mode must be enabled for this to work properly however."
//					+ " This feature may also be useful in diagnosing other problems with other plugins as well. Level varies from 1-5, and shows more"
//					+ " information as it increases. You must also set at least one filter with the set_debug_event_filter function before anything"
//					+ " will happen. logToScreen defaults to false. This should only be turned on when you are testing, or have very strict filters set.";
//		}
//
//		public Class<? extends CREThrowable>[] thrown() {
//			return new Class[]{CRECastException.class, CRESecurityException.class};
//		}
//
//		public boolean isRestricted() {
//			return true;
//		}
//
//		public boolean preResolveVariables() {
//			return true;
//		}
//
//		public MSVersion since() {
//			return MSVersion.V3_3_0;
//		}
//
//		public Boolean runAsync() {
//			return false;
//		}
//
//		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
//			if(!(Boolean) Static.getPreferences().getPreference("allow-debug-logging")) {
//				throw new ConfigRuntimeException("allow-debug-logging is currently set to false. To use " + this.getVariableName() + ", enable it in your preferences.", CRESecurityException.class, t);
//			}
//			boolean on = ArgumentValidation.getBoolean(args[0]);
//			int level = 1;
//			if(args.length >= 2){
//				level = Static.Normalize(Static.getInt32(args[1]), 1, 5);
//			}
//			Debug.EVENT_LOGGING = on;
//			Debug.EVENT_LOGGING_LEVEL = level;
//			if(args.length >= 3){
//				Debug.LOG_TO_SCREEN = ArgumentValidation.getBoolean(args[2]);
//			}
//			return CVoid.VOID;
//		}
//	}
//	@api
//	public static class set_debug_event_filter extends AbstractFunction {
//
//		public String getVariableName() {
//			return "set_debug_event_filter";
//		}
//
//		public Integer[] numArgs() {
//			return new Integer[]{1};
//		}
//
//		public String docs() {
//			return "void {array} Logs the specified event types as they occur, assuming that logging is currently enabled. For a list of"
//					+ " available filters, you can run dump_listeners(null). As these events occur, they will be logged according to the logging level.";
//		}
//
//		public Class<? extends CREThrowable>[] thrown() {
//			return new Class[]{CRECastException.class, CREFormatException.class, CRESecurityException.class};
//		}
//
//		public boolean isRestricted() {
//			return true;
//		}
//
//		public boolean preResolveVariables() {
//			return true;
//		}
//
//		public MSVersion since() {
//			return MSVersion.V3_3_0;
//		}
//
//		public Boolean runAsync() {
//			return true;
//		}
//
//		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
//			if(!(Boolean) Static.getPreferences().getPreference("allow-debug-logging")) {
//				throw new ConfigRuntimeException("allow-debug-logging is currently set to false. To use " + this.getVariableName() + ", enable it in your preferences.", CRESecurityException.class, t);
//			}
//			Set<Event.Type> set = new HashSet<Event.Type>();
//			if(args[0].isInstanceOf(CString.TYPE)) {
//				if(args[0].val().equals("*")) {
//					for(Event.Type t : Event.Type.values()) {
//						set.add(t);
//					}
//				} else {
//					try {
//						Event.Type t = Event.Type.valueOf(args[0].val().toUpperCase());
//						set.add(t);
//					} catch (IllegalArgumentException e) {
//						throw new ConfigRuntimeException(args[0].val() + " is not a valid filter type. The filter log has not been changed.", CREFormatException.class, t);
//					}
//				}
//			} else if(args[0].isInstanceOf(CArray.TYPE)) {
//				for(String c : ((CArray) args[0]).keySet()) {
//					try {
//						set.add(Event.Type.valueOf(((CArray) args[0]).get(c, t).val().toUpperCase()));
//					} catch (IllegalArgumentException e) {
//						throw new ConfigRuntimeException(c + " is not a valid filter type. The filter log has not been changed.", CREFormatException.class, t);
//					}
//				}
//			} else {
//				throw new ConfigRuntimeException("The parameter specified to " + this.getVariableName() + " must be an array (or a single string). The filter array has not been changed.", CRECastException.class, t);
//			}
//			synchronized (EVENT_LOGGING_FILTER) {
//				EVENT_LOGGING_FILTER.clear();
//				for(Event.Type t : set) {
//					EVENT_LOGGING_FILTER.add(t);
//				}
//			}
//			return CVoid.VOID;
//		}
//	}
//	@api
//	public static class set_debug_plugin_filter extends AbstractFunction {
//
//		public String getVariableName() {
//			return "set_debug_plugin_filter";
//		}
//
//		public Integer[] numArgs() {
//			return new Integer[]{1};
//		}
//
//		public String docs() {
//			return "void {array} Often times you just are interested in the events a particular plugin is outputting. If the plugin filter"
//					+ " is empty, all plugins are reported (assuming their event types are not filtered out) otherwise, only the ones in"
//					+ " the list are logged. The name of the plugin is the field \"Called from Plugin: \" in the output, not the name"
//					+ " it may be commonly referred to as.";
//		}
//
//		public Class<? extends CREThrowable>[] thrown() {
//			return new Class[]{CRECastException.class, CRESecurityException.class};
//		}
//
//		public boolean isRestricted() {
//			return true;
//		}
//
//		public boolean preResolveVariables() {
//			return true;
//		}
//
//		public MSVersion since() {
//			return MSVersion.V3_3_0;
//		}
//
//		public Boolean runAsync() {
//			return false;
//		}
//
//		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
//			if(!(Boolean) Static.getPreferences().getPreference("allow-debug-logging")) {
//				throw new ConfigRuntimeException("allow-debug-logging is currently set to false. To use " + this.getVariableName() + ", enable it in your preferences.", CRESecurityException.class, t);
//			}
//			if(args[0].isInstanceOf(CString.TYPE)) {
//				EVENT_PLUGIN_FILTER.clear();
//				EVENT_PLUGIN_FILTER.add(args[0].val().toUpperCase());
//			} else if(args[0].isInstanceOf(CArray.TYPE)) {
//				for(String c : ((CArray) args[0]).keySet()) {
//					EVENT_PLUGIN_FILTER.add(((CArray) args[0]).get(c, t).val().toUpperCase());
//				}
//			} else {
//				throw new ConfigRuntimeException(this.getVariableName() + " expects the argument to be a single string, or an array of strings.", CRECastException.class, t);
//			}
//			return CVoid.VOID;
//		}
//	}
	@api
	public static class dump_threads extends AbstractFunction {

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
			Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
			CArray carray = new CArray(t);
			for(Thread thread : threadSet) {
				carray.push(new CString(thread.getName(), t), t);
			}
			return carray;
		}

		@Override
		public String getName() {
			return "dump_threads";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "array {} Returns an array of all thread names that are currently running in the JVM."
					+ " This is a debugging tool for your server, and less of a CommandHelper specific thing.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

	}

	@api
	@noboilerplate
	public static class heap_dump extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPluginInternalException.class};
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
			File file = new File("dump.hprof");
			try {
				HeapDumper.dumpHeap(file.getAbsolutePath(), true);
			} catch (Throwable tt) {
				throw new CREPluginInternalException("Could not create a heap dump: " + tt.getMessage(), t, tt);
			}
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "heap_dump";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "void {} Creates a heap dump file, and places it in the working directory, as \"dump.hprof\". This might"
					+ " throw a PluginInternalException if the heap dump tools aren't available in your JVM. Once dumped,"
					+ " the heap dump can be analyzed using tools such as jhat. More information about jhat can be found"
					+ " [http://docs.oracle.com/javase/6/docs/technotes/tools/share/jhat.html here].";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

	}

	@api
	@noboilerplate
	public static class set_debug_output extends AbstractFunction {

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
			Script.debugOutput = ArgumentValidation.getBoolean(args[0], t);
			if(Script.debugOutput) {
				StreamUtils.GetSystemOut().println(TermColors.BG_RED + "[[DEBUG]] set_debug_output(true)"
						+ TermColors.RESET);
			}
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "set_debug_output";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "void {booleanValue} Turns verbose debug output on or off. This should generally never be on in"
					+ " a production server, but can be useful to quickly trace what a script is doing when it runs in a"
					+ " test environment. When on, every single function call will be printed out, along with the"
					+ " parameters passed in to it. To reduce impact on scripts when this is disabled, this has been"
					+ " implemented as a system wide setting, and applies to all scripts running in the same system.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_3;
		}

	}
}
