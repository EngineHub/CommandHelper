package com.laytonsmith.core.environments;

import com.laytonsmith.PureUtilities.Common.MutableObject;
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
import com.laytonsmith.core.events.BoundEvent;
import com.laytonsmith.core.natives.interfaces.ArrayAccess;
import com.laytonsmith.core.profiler.Profiler;
import com.laytonsmith.database.Profiles;
import com.laytonsmith.persistence.PersistenceNetwork;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A global environment is always available, and contains the objects that the
 * core functionality uses.
 *
 * @author lsmith
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
	private PermissionsResolver permissionsResolver = null;
	private final Map<String, Boolean> flags = new HashMap<>();
	private final Map<String, Object> custom = new HashMap<>();
	private Script script = null;
	private final MutableObject<File> root;
	private final MutableObject<CClosure> uncaughtExceptionHandler = new MutableObject<>();
	private Map<String, Procedure> procs = null;
	private IVariableList iVariableList = null;
	private String label = null;
	private final DaemonManager daemonManager = new DaemonManager();
	private boolean dynamicScriptingMode = false;
	private final Profiles profiles;
	private BoundEvent.ActiveEvent event = null;
	private boolean interrupt = false;
	private final List<ArrayAccess.ArrayAccessIterator> arrayAccessList = Collections.synchronizedList(new ArrayList<ArrayAccess.ArrayAccessIterator>());

	/**
	 * Creates a new GlobalEnvironment. All fields in the constructor are required, and cannot be null.
	 * @param queue The ExecutionQueue object to use
	 * @param profiler The Profiler to use
	 * @param network The pre-configured PersistenecNetwork object to use
	 * @param resolver The PermissionsResolver to use
	 * @param root The root working directory to use
	 * @param profiles The SQL Profiles object to use
	 */
	public GlobalEnv(ExecutionQueue queue, Profiler profiler, PersistenceNetwork network, PermissionsResolver resolver, 
			File root, Profiles profiles) {
		Static.AssertNonNull(queue, "ExecutionQueue cannot be null");
		Static.AssertNonNull(profiler, "Profiler cannot be null");
		Static.AssertNonNull(network, "PersistenceNetwork cannot be null");
		Static.AssertNonNull(resolver, "PermissionsResolver cannot be null");
		Static.AssertNonNull(root, "Root file cannot be null");
		this.executionQueue = queue;
		this.profiler = profiler;
		this.persistenceNetwork = network;
		this.permissionsResolver = resolver;
		this.root = new MutableObject(root);
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

	public PersistenceNetwork GetPersistenceNetwork() {
		return persistenceNetwork;
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
			clone.procs = new HashMap<>(procs);
		} else {
			clone.procs = new HashMap<>();
		}
		if (iVariableList != null) {
			clone.iVariableList = (IVariableList) iVariableList.clone();
		}
		return clone;
	}

	public File GetRootFolder() {
		return root.getObject();
	}
	
	/**
	 * Sets the root working directory. It cannot be null.
	 * @param file 
	 */
	public void SetRootFolder(File file){
		Static.AssertNonNull(file, "Root file cannot be null");
		this.root.setObject(file);
	}

	public void SetExceptionHandler(CClosure construct) {
		uncaughtExceptionHandler.setObject(construct);
	}

	public CClosure GetExceptionHandler() {
		return uncaughtExceptionHandler.getObject();
	}

	/**
	 * Returns the Map of known procedures in this environment. If the list of
	 * procedures is currently empty, a new one is created and stored in the
	 * environment.
	 *
	 * @return
	 */
	public Map<String, Procedure> GetProcs() {
		if (procs == null) {
			procs = new HashMap<>();
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
	
	public void SetEvent(BoundEvent.ActiveEvent e){
        event = e;
    }
    
    /**
     * Returns the active event, or null if not in scope.
     * @return 
     */
    public BoundEvent.ActiveEvent GetEvent(){
        return event;
    }
	
	/**
	 * Sets the interrupted flag. Interrupted scripts should halt immediately.
	 * @param interrupted 
	 */
	public synchronized void SetInterrupt(boolean interrupted){
		interrupt = interrupted;
	}
	
	/**
	 * Returns true if this script has been interrupted, and should immediately halt execution.
	 * @return 
	 */
	public synchronized boolean IsInterrupted(){
		return interrupt;
	}
	
	/**
	 * Returns the array access list. Note that the returned array is threadsafe,
	 * though iteration over the list requires manual synchronization on the list.
	 * @return 
	 */
	public List<ArrayAccess.ArrayAccessIterator> GetArrayAccessIterators(){
		return arrayAccessList;
	}
	
	/**
	 * Returns a list of all ArrayAccessIterators for the specified array. 
	 * @param array
	 * @return 
	 */
	public List<ArrayAccess.ArrayAccessIterator> GetArrayAccessIteratorsFor(ArrayAccess array){
		List<ArrayAccess.ArrayAccessIterator> list = new ArrayList<>();
		synchronized(arrayAccessList){
			for(ArrayAccess.ArrayAccessIterator value : arrayAccessList){
				if(value.underlyingArray() == array){
					list.add(value);
				}
			}
		}
		return list;
	}
}
