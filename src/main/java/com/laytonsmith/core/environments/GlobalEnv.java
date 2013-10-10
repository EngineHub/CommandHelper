package com.laytonsmith.core.environments;

import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.PureUtilities.ExecutionQueue;
import com.laytonsmith.core.MethodScriptExecutionQueue;
import com.laytonsmith.core.PermissionsResolver;
import com.laytonsmith.core.Procedure;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CClosure;
import com.laytonsmith.core.constructs.IVariableList;
import com.laytonsmith.core.environments.Environment.EnvironmentImpl;
import com.laytonsmith.core.profiler.Profiler;
import com.laytonsmith.database.Profiles;
import com.laytonsmith.persistance.PersistanceNetwork;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * A global environment is always available, and contains the objects that the
 * core functionality uses.
 *
 * @author lsmith
 */
public class GlobalEnv implements Environment.EnvironmentImpl, Cloneable {

	private ExecutionQueue executionQueue = null;
	private Profiler profiler = null;
	//This is changed reflectively in a test, please don't rename.
	private PersistanceNetwork persistanceNetwork = null;
	private PermissionsResolver permissionsResolver = null;
	private Map<String, Boolean> flags = new HashMap<String, Boolean>();
	private Map<String, Object> custom = new HashMap<String, Object>();
	private Script script = null;
	private File root;
	private CClosure uncaughtExceptionHandler;
	private Map<String, Procedure> procs = null;
	private IVariableList iVariableList = null;
	private String label = null;
	private DaemonManager daemonManager = new DaemonManager();
	private boolean dynamicScriptingMode = false;
	private Profiles profiles;

	public GlobalEnv(ExecutionQueue queue, Profiler profiler, PersistanceNetwork network, PermissionsResolver resolver, File root, Profiles profiles) {
		Static.AssertNonNull(queue, "ExecutionQueue cannot be null");
		Static.AssertNonNull(profiler, "Profiler cannot be null");
		Static.AssertNonNull(network, "PersistanceNetwork cannot be null");
		Static.AssertNonNull(resolver, "PermissionsResolver cannot be null");
		Static.AssertNonNull(root, "Root file cannot be null");
		this.executionQueue = queue;
		this.profiler = profiler;
		this.persistanceNetwork = network;
		this.permissionsResolver = resolver;
		this.root = root;
		if (this.executionQueue instanceof MethodScriptExecutionQueue) {
			((MethodScriptExecutionQueue) executionQueue).setEnvironment(this);
		}
		this.profiles = profiles;
	}

	public ExecutionQueue GetExecutionQueue() {
		return executionQueue;
	}

	public Profiler GetProfiler() {
		return profiler;
	}

	public PersistanceNetwork GetPersistanceNetwork() {
		return persistanceNetwork;
	}

	public PermissionsResolver GetPermissionsResolver() {
		return permissionsResolver;
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
		if (!flags.containsKey(name)) {
			return null;
		} else {
			return flags.get(name);
		}
	}

	/**
	 * Clears the value of a flag from the flag list, causing further calls to
	 * GetFlag(name) to return null.
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
	 * Use this if you would like to stick a custom variable in the environment.
	 * It should be discouraged to use this for more than one shot transfers.
	 * Typically, an setter and getter should be made to wrap the element.
	 *
	 * @param name
	 * @param var
	 */
	public void SetCustom(String name, Object var) {
		if (!custom.containsKey("custom")) {
			custom.put("custom", new HashMap<String, Object>());
		}
		((Map<String, Object>) custom.get("custom")).put(name, var);
	}

	/**
	 * Returns the custom value to which the specified key is mapped, or null if
	 * this map contains no mapping for the key.
	 *
	 * @param name
	 * @return
	 */
	public Object GetCustom(String name) {
		if (!custom.containsKey("custom")) {
			custom.put("custom", new HashMap<String, Object>());
		}
		return ((Map<String, Object>) custom.get("custom")).get(name);
	}

	@Override
	public EnvironmentImpl clone() throws CloneNotSupportedException {
		GlobalEnv clone = (GlobalEnv) super.clone();
		if (procs != null) {
			clone.procs = new HashMap<String, Procedure>(procs);
		} else {
			clone.procs = new HashMap<String, Procedure>();
		}
		if (iVariableList != null) {
			clone.iVariableList = (IVariableList) iVariableList.clone();
		}
		return clone;
	}

	public File GetRootFolder() {
		return root;
	}

	public void SetExceptionHandler(CClosure construct) {
		uncaughtExceptionHandler = construct;
	}

	public CClosure GetExceptionHandler() {
		return uncaughtExceptionHandler;
	}

	/**
	 * Returns the Map of known procedures in this environment. If the list of
	 * procedures is currently empty, a new one is created and stored in the
	 * environment.
	 *
	 * @param env
	 * @return
	 */
	public Map<String, Procedure> GetProcs() {
		if (procs == null) {
			procs = new HashMap<String, Procedure>();
		}
		return procs;
	}

	public void SetProcs(Map<String, Procedure> procs) {
		this.procs = procs;
	}

	/**
	 * This function will return the variable list in this environment. If the
	 * environment doesn't contain a variable list, an empty one is created, and
	 * stored in the environment.
	 *
	 * @param env
	 * @return
	 */
	public IVariableList GetVarList() {
		if (iVariableList == null) {
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
	
	public DaemonManager GetDaemonManager(){
		return daemonManager;
	}
	
	/**
	 * Turns dynamic scripting mode on or off. If this is true, that means
	 * that the script came from a dynamic source, such as eval, or other sources
	 * other than the file system.
	 * @param dynamicScriptingMode 
	 */
	public void SetDynamicScriptingMode(boolean dynamicScriptingMode){
		this.dynamicScriptingMode = dynamicScriptingMode;
	}
	
	/**
	 * Returns whether or not dynamic script mode is on or off. If this is true, that means
	 * that the script came from a dynamic source, such as eval, or other sources
	 * other than the file system.
	 * @return 
	 */
	public boolean GetDynamicScriptingMode(){
		return this.dynamicScriptingMode;
	}

	public Profiles getSQLProfiles() {
		return this.profiles;
	}
}
