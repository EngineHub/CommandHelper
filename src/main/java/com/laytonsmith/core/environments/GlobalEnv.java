package com.laytonsmith.core.environments;

import com.laytonsmith.PureUtilities.Common.MutableObject;
import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.PureUtilities.ExecutionQueue;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.MethodScriptExecutionQueue;
import com.laytonsmith.core.Procedure;
import com.laytonsmith.core.Profiles;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CClosure;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.IVariableList;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment.EnvironmentImpl;
import com.laytonsmith.core.events.BoundEvent;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.StackTraceManager;
import com.laytonsmith.core.natives.interfaces.ArrayAccess;
import com.laytonsmith.core.natives.interfaces.Iterator;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.core.profiler.Profiler;
import com.laytonsmith.core.taskmanager.TaskManager;
import com.laytonsmith.persistence.PersistenceNetwork;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A global environment is always available at runtime, and contains the objects that the core functionality uses.
 */
public class GlobalEnv implements Environment.EnvironmentImpl, Cloneable {

	//Fields that are MutableObjects are shared among all environments.
	//This makes some things "system wide", for instance, the uncaught
	//exception handler should not be different for closures vs outside
	//of closures. This only applies to things that can change during runtime
	//via a script, and should be totally global.
	//Anything else varies based on the particular needs of
	//that field. Note that lists, maps, and other reference based objects don't
	//need to use MutableObjects, as they are inherently Mutable themselves.
	private ExecutionQueue executionQueue = null;
	private Profiler profiler = null;
	//This is changed reflectively in a test, please don't rename.
	private PersistenceNetwork persistenceNetwork = null;
	private final Map<String, Boolean> flags = new HashMap<>();
	private final Map<String, Object> custom = new HashMap<>();
	private Script script = null;
	private final MutableObject<File> root;
	private final MutableObject<CClosure> uncaughtExceptionHandler = new MutableObject<>();
	private Map<String, Procedure> procs = null;
	private IVariableList iVariableList = null;
	private String label = null;
	private boolean inCmdlineMode = false;
	private boolean inInterpreterMode = false;
	private final DaemonManager daemonManager = new DaemonManager();
	private boolean dynamicScriptingMode = false;
	private final Profiles profiles;
	private BoundEvent.ActiveEvent event = null;
	private boolean interrupt = false;
	private final List<Iterator> arrayAccessList = Collections.synchronizedList(new ArrayList<>());
	private final MutableObject<TaskManager> taskManager = new MutableObject<>();
	private final WeakHashMap<Thread, StackTraceManager> stackTraceManagers = new WeakHashMap<>();
	private final MutableObject<Map<String, Mixed>> runtimeSettings
			= new MutableObject<>(new ConcurrentHashMap<>());
	private FileOptions fileOptions;

	/**
	 * Creates a new GlobalEnvironment. All fields in the constructor are required, and cannot be null.
	 *
	 * @param queue The ExecutionQueue object to use
	 * @param profiler The Profiler to use
	 * @param network The pre-configured PersistenceNetwork object to use
	 * @param root The root working directory to use
	 * @param profiles The Profiles object to use
	 * @param taskManager The TaskManager object to use
	 * @param inCmdlineMode {@code true} if running in cmdline mode, {@code false} otherwise.
	 * @param inInterpreterMode {@code true} if running in interpreter mode, {@code false} otherwise.
	 */
	public GlobalEnv(ExecutionQueue queue, Profiler profiler, PersistenceNetwork network,
			File root, Profiles profiles, TaskManager taskManager, boolean inCmdlineMode, boolean inInterpreterMode) {
		Static.AssertNonNull(queue, "ExecutionQueue cannot be null");
		Static.AssertNonNull(profiler, "Profiler cannot be null");
		Static.AssertNonNull(network, "PersistenceNetwork cannot be null");
		Static.AssertNonNull(root, "Root file cannot be null");
		Static.AssertNonNull(taskManager, "TaskManager cannot be null");
		this.executionQueue = queue;
		this.profiler = profiler;
		this.persistenceNetwork = network;
		this.root = new MutableObject(root);
		if(this.executionQueue instanceof MethodScriptExecutionQueue) {
			((MethodScriptExecutionQueue) executionQueue).setEnvironment(this);
		}
		this.profiles = profiles;
		this.taskManager.setObject(taskManager);
		this.inCmdlineMode = inCmdlineMode;
		this.inInterpreterMode = inInterpreterMode;
	}

	/**
	 * Thrown if one of the no-op classes is used.
	 */
	public static class GlobalEnvNoOpException extends RuntimeException {
		public GlobalEnvNoOpException(String message) {
			super(message);
		}
	}

	/**
	 * When constructing a GlobalEnv in meta circumstances, it may be helpful to provide
	 * a no-op execution queue. This value is set up for that purpose. However, it's
	 * not truly no-op, as an exception is thrown if any method in the interface are
	 * used, as this points to a situation where something is being called that isn't
	 * compatible with a no op execution.
	 */
	public static final ExecutionQueue NO_OP_EXECUTION_QUEUE
			= GetErrorNoOp(ExecutionQueue.class, "NO_OP_EXECUTION_QUEUE");

	/**
	 * When constructing a GlobalEnv in meta circumstances, it may be helpful to provide
	 * a no-op profiler. This value is set up for that purpose. It is
	 * truly no-op, and no exception is thrown if a method in the interface is
	 * used.
	 */
	public static final Profiler NO_OP_PROFILER = Profiler.FakeProfiler();

	/**
	 * When constructing a GlobalEnv in meta circumstances, it may be helpful to provide
	 * a no-op persistence network. This value is set up for that purpose. However, it's
	 * not truly no-op, as an exception is thrown if any method in the interface are
	 * used, as this points to a situation where something is being called that isn't
	 * compatible with a no op execution.
	 */
	public static final PersistenceNetwork NO_OP_PN = GetErrorNoOp(PersistenceNetwork.class, "NO_OP_PN");

	/**
	 * When constructing a GlobalEnv in meta circumstances, it may be helpful to provide
	 * a no-op Profiles object. This value is set up for that purpose. However, it's
	 * not truly no-op, as an exception is thrown if any method in the interface are
	 * used, as this points to a situation where something is being called that isn't
	 * compatible with a no op execution.
	 */
	public static final Profiles NO_OP_PROFILES = GetErrorNoOp(Profiles.class, "NO_OP_PROFILES");

	/**
	 * When constructing a GlobalEnv in meta circumstances, it may be helpful to provide
	 * a no-op profiler. This value is set up for that purpose. It is
	 * truly no-op, and no exception is thrown if a method in the interface is
	 * used.
	 */
	public static final TaskManager NO_OP_TASK_MANAGER = GetNoOp(TaskManager.class);

	private static <T> T GetErrorNoOp(Class<T> iface, String identifier) {
		return (T) Proxy.newProxyInstance(GlobalEnv.class.getClassLoader(),
				new Class[]{iface}, (Object proxy, Method method, Object[] args) -> {
					throw new GlobalEnvNoOpException(identifier);
				});
	}

	private static <T> T GetNoOp(Class<T> iface) {
		return (T) Proxy.newProxyInstance(GlobalEnv.class.getClassLoader(),
				new Class[]{iface}, (Object proxy, Method method, Object[] args) -> null);
	}

	public ExecutionQueue GetExecutionQueue() {
		return executionQueue;
	}

	public Profiler GetProfiler() {
		return profiler;
	}

	public PersistenceNetwork GetPersistenceNetwork() {
		return persistenceNetwork;
	}

	public TaskManager GetTaskManager() {
		return taskManager.getObject();
	}

	/**
	 * Sets the value of a flag
	 *
	 * @param name
	 * @param value
	 */
	public void SetFlag(String name, boolean value) {
		flags.put(name, value);
	}

	/**
	 * Returns the value of a flag. Null if unset.
	 *
	 * @param name
	 * @return
	 */
	public Boolean GetFlag(String name) {
		if(!flags.containsKey(name)) {
			return null;
		} else {
			return flags.get(name);
		}
	}

	/**
	 * Clears the value of a flag from the flag list, causing further calls to GetFlag(name) to return null.
	 *
	 * @param name
	 */
	public void ClearFlag(String name) {
		flags.remove(name);
	}

	public void SetScript(Script s) {
		this.script = s;
	}

	public Script GetScript() {
		return script;
	}

	/**
	 * Use this if you would like to stick a custom variable in the environment. It should be discouraged to use this
	 * for more than one shot transfers. Typically, an setter and getter should be made to wrap the element.
	 *
	 * @param name
	 * @param var
	 */
	public void SetCustom(String name, Object var) {
		if(!custom.containsKey("custom")) {
			custom.put("custom", new HashMap<String, Object>());
		}
		((Map<String, Object>) custom.get("custom")).put(name, var);
	}

	/**
	 * Returns the custom value to which the specified key is mapped, or null if this map contains no mapping for the
	 * key.
	 *
	 * @param name
	 * @return
	 */
	public Object GetCustom(String name) {
		if(!custom.containsKey("custom")) {
			custom.put("custom", new HashMap<String, Object>());
		}
		return ((Map<String, Object>) custom.get("custom")).get(name);
	}

	@Override
	public EnvironmentImpl clone() throws CloneNotSupportedException {
		GlobalEnv clone = (GlobalEnv) super.clone();
		if(procs != null) {
			clone.procs = new HashMap<>(procs);
		} else {
			clone.procs = new HashMap<>();
		}
		if(cloneVars && iVariableList != null) {
			clone.iVariableList = iVariableList.clone();
		} else if(!cloneVars) {
			clone.iVariableList = new IVariableList();
		}
		return clone;
	}

	private boolean cloneVars = true;

	/**
	 * Determines whether or not, when cloning this environment, the variable list should be cloned as well.
	 *
	 * @param set
	 */
	public void setCloneVars(boolean set) {
		this.cloneVars = set;
	}

	public boolean getCloneVars() {
		return this.cloneVars;
	}

	/**
	 * Gets the current working directory. It is guaranteed that this will be a folder, not a file, and that it will not
	 * be null.
	 *
	 * @return
	 */
	public File GetRootFolder() {
		return root.getObject();
	}

	/**
	 * Sets the root working directory. It cannot be null, or a file, it must be a directory.
	 *
	 * @param file
	 * @throws NullPointerException If file is null
	 * @throws IllegalArgumentException If the file specified is not a directory.
	 */
	public void SetRootFolder(File file) {
		Static.AssertNonNull(file, "Root file cannot be null");
		if(file.isFile()) {
			throw new IllegalArgumentException("File provided to SetRootFolder must be a folder, not a file. (" + file.toString() + " was found.)");
		}
		this.root.setObject(file);
	}

	public void SetExceptionHandler(CClosure construct) {
		uncaughtExceptionHandler.setObject(construct);
	}

	public CClosure GetExceptionHandler() {
		return uncaughtExceptionHandler.getObject();
	}

	/**
	 * Returns the Map of known procedures in this environment. If the list of procedures is currently empty, a new one
	 * is created and stored in the environment.
	 *
	 * @return
	 */
	public Map<String, Procedure> GetProcs() {
		if(procs == null) {
			procs = new HashMap<>();
		}
		return procs;
	}

	public void SetProcs(Map<String, Procedure> procs) {
		this.procs = procs;
	}

	/**
	 * This function will return the variable list in this environment. If the environment doesn't contain a variable
	 * list, an empty one is created, and stored in the environment.
	 *
	 * @return
	 */
	public IVariableList GetVarList() {
		if(iVariableList == null) {
			iVariableList = new IVariableList();
		}
		return iVariableList;
	}

	public void SetVarList(IVariableList varList) {
		iVariableList = varList;
	}

	public String GetLabel() {
		return label;
	}

	public void SetLabel(String label) {
		this.label = label;
	}

	public boolean inCmdlineMode() {
		return this.inCmdlineMode;
	}

	public boolean inInterpreterMode() {
		return this.inInterpreterMode;
	}

	public DaemonManager GetDaemonManager() {
		return daemonManager;
	}

	/**
	 * Turns dynamic scripting mode on or off. If this is true, that means that the script came from a dynamic source,
	 * such as eval, or other sources other than the file system.
	 *
	 * @param dynamicScriptingMode
	 */
	public void SetDynamicScriptingMode(boolean dynamicScriptingMode) {
		this.dynamicScriptingMode = dynamicScriptingMode;
	}

	/**
	 * Returns whether or not dynamic script mode is on or off. If this is true, that means that the script came from a
	 * dynamic source, such as eval, or other sources other than the file system.
	 *
	 * @return
	 */
	public boolean GetDynamicScriptingMode() {
		return this.dynamicScriptingMode;
	}

	public Profiles getProfiles() {
		return this.profiles;
	}

	public void SetEvent(BoundEvent.ActiveEvent e) {
		event = e;
	}

	/**
	 * Returns the active event, or null if not in scope.
	 *
	 * @return
	 */
	public BoundEvent.ActiveEvent GetEvent() {
		return event;
	}

	/**
	 * Sets the interrupted flag. Interrupted scripts should halt immediately.
	 *
	 * @param interrupted
	 */
	public synchronized void SetInterrupt(boolean interrupted) {
		interrupt = interrupted;
	}

	/**
	 * Returns true if this script has been interrupted, and should immediately halt execution. This is monitored
	 * in the core execution engine, however, if a function could be particularly long running, this value should
	 * be manually checked, since the engine is not pre-emptive.
	 *
	 * @return
	 */
	public synchronized boolean IsInterrupted() {
		return interrupt;
	}

	/**
	 * Returns the array access list. Note that the returned array is threadsafe, though iteration over the list
	 * requires manual synchronization on the list.
	 *
	 * @return
	 */
	public List<Iterator> GetArrayAccessIterators() {
		return arrayAccessList;
	}

	/**
	 * Returns a list of all ArrayAccessIterators for the specified array.
	 *
	 * @param array
	 * @return
	 */
	public List<Iterator> GetArrayAccessIteratorsFor(ArrayAccess array) {
		List<Iterator> list = new ArrayList<>();
		synchronized(arrayAccessList) {
			for(Iterator value : arrayAccessList) {
				if(value.underlyingArray() == array) {
					list.add(value);
				}
			}
		}
		return list;
	}

	/**
	 * Returns the StacKTraceManager for the currently running thread.
	 *
	 * @return
	 */
	public StackTraceManager GetStackTraceManager() {
		Thread currentThread = Thread.currentThread();
		synchronized(stackTraceManagers) {
			StackTraceManager manager = stackTraceManagers.get(currentThread);
			if(manager == null) {
				manager = new StackTraceManager();
				stackTraceManagers.put(currentThread, manager);
			}
			return manager;
		}
	}

	/**
	 * Returns the runtime setting for a particular value. Runtime settings are a collection of free settings, which
	 * individual functions can define setting
	 * names and values, though in general, to prevent stepping over each other, the following guidelines should be
	 * used for the names:
	 * <p>
	 * The general format of the setting should be hierarchical, with dots separating the setting name categories, i.e.
	 * {@code function.function_name.setting_name} where the following top level hierarchies are defined:
	 * <ul>
	 *	<li>function - settings relating to functions. The second category should be the function name.</li>
	 *  <li>event - settings relating to events. The second category should be the event name.</li>
	 *  <li>extension - settings relating to extensions, that don't fit in the previous two categories.</li>
	 *  <li>system - settings related to the system that aren't based on functions or events</li>
	 * </ul>
	 * <p>
	 * Given that these settings can change at any time, it is important that these values be re-read each time. Due to
	 * this, the underlying Map is threadsafe. If a setting is missing, then code must define what should happen, as
	 * that may be different behavior. The documentation defining the behavior of the component in question must specify
	 * the behavior, there is no generic mechanism defined for documenting these.
	 * <p>
	 * The map maps CString values to Mixed values, so the value may be anything, and as such, much be typechecked by
	 * code first.
	 * <p>
	 * The values are defined globally, and cannot be scoped down to other scopes, so these should be used only in cases
	 * where such global settings make sense, usually in regards to setting the default value for a particular parameter
	 * or option.
	 * @param name The setting name
	 * @return The Mixed value, or null, if it is not contained in the set.
	 */
	public Mixed GetRuntimeSetting(String name) {
		return runtimeSettings.getObject().get(name);
	}

	/**
	 * Works like {@link #GetRuntimeSetting(java.lang.String)} but if the value is not set, or is set to CNull, the
	 * default value is returned. If a totally missing value has a different meaning than a CNull value, you should use
	 * {@link #GetRuntimeSettingOrCNull(java.lang.String, com.laytonsmith.core.natives.interfaces.Mixed)}.
	 * @param name The setting name. See the stipulations for naming conventions in
	 * {@link #GetRuntimeSetting(java.lang.String)}
	 * @param defaultValue The value to return if the value was totally missing from the map or was set to CNull.
	 * @return Either the user specified value, if present, or the defaultValue.
	 */
	public Mixed GetRuntimeSetting(String name, Mixed defaultValue) {
		Mixed value = GetRuntimeSettingOrCNull(name, defaultValue);
		if(CNull.NULL.equals(value)) {
			return defaultValue;
		} else {
			return value;
		}
	}

	/**
	 * As many runtime settings are just booleans, this convenience method can be used to deal directly with java
	 * booleans. If the value supplied is not a Booleanish though, the default value is returned, with a warning
	 * issued. See {@link #GetRuntimeSetting(java.lang.String)} for details on the parameters.
	 * @param name
	 * @param defaultValue
	 * @param t
	 * @return
	 */
	public boolean GetRuntimeSetting(String name, boolean defaultValue, Target t) {
		Mixed b = GetRuntimeSetting(name, CBoolean.get(defaultValue));
		try {
			return ArgumentValidation.getBooleanish(b, t);
		} catch (CRECastException ex) {
			MSLog.GetLogger().w(MSLog.Tags.RUNTIME, "Runtime setting \"" + name + "\" is not a boolean value, but was"
					+ " expected to be. The default value is being used instead.", t);
			return defaultValue;
		}
	}

	/**
	 * Works like {@link #GetRuntimeSetting(java.lang.String)} but if the value is not set in the map, the defaultValue
	 * is returned. Note that if the value is set to CNull by the user, this will return CNull, not your default value.
	 * If you want CNull to be considered the same as totally missing, use
	 * {@link #GetRuntimeSetting(java.lang.String, com.laytonsmith.core.natives.interfaces.Mixed)}.
	 * @param name The setting name. See the stipulations for naming conventions in
	 * {@link #GetRuntimeSetting(java.lang.String)}
	 * @param defaultValue The value to return if the value was totally missing from the map.
	 * @return Either the user specified value, if present, or the defaultValue.
	 */
	public Mixed GetRuntimeSettingOrCNull(String name, Mixed defaultValue) {
		if(runtimeSettings.getObject().containsKey(name)) {
			return runtimeSettings.getObject().get(name);
		} else {
			return defaultValue;
		}
	}

	/**
	 * Sets the value of a runtime setting. If value is java null (CNull.NULL is different), then the value is simply
	 * removed from the settings list. In general, this method should only be called by set/remove_runtime_setting, and
	 * should never be modified by java code otherwise.
	 * @param name The setting name.
	 * @param value The value to set in the map
	 */
	public void SetRuntimeSetting(String name, Mixed value) {
		if(value == null) {
			runtimeSettings.getObject().remove(name);
		} else {
			runtimeSettings.getObject().put(name, value);
		}
	}

	/**
	 * The file options should be set before execution of each function, so the function can have
	 * access to the current parse tree's file options.
	 * @param options
	 */
	public void SetFileOptions(FileOptions options) {
		this.fileOptions = options;
	}

	/**
	 * The FileOptions are set for each function, so calling this returns the file options for the current
	 * function execution, which will vary from place to place.
	 * @return
	 */
	public FileOptions GetFileOptions() {
		return this.fileOptions;
	}
}
