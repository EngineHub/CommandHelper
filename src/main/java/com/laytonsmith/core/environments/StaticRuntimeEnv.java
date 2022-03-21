package com.laytonsmith.core.environments;

import com.laytonsmith.PureUtilities.Common.MutableObject;
import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.PureUtilities.ExecutionQueue;
import com.laytonsmith.core.compiler.analysis.StaticAnalysis;
import com.laytonsmith.core.constructs.CClosure;
import com.laytonsmith.core.functions.IncludeCache;
import com.laytonsmith.core.Profiles;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.environments.Environment.EnvironmentImpl;
import com.laytonsmith.core.profiler.Profiler;
import com.laytonsmith.core.taskmanager.TaskManager;
import com.laytonsmith.persistence.PersistenceNetwork;

/**
 * This environment contains 'static final' functionality and data that is available at runtime,
 * and that does not differ between compile units. This is the perfect environment to store configuration files
 * or truely global managers in.
 */
public class StaticRuntimeEnv implements Environment.EnvironmentImpl, Cloneable {

	private final Profiler profiler;
	private final PersistenceNetwork persistenceNetwork;
	private final Profiles profiles;
	private final TaskManager taskManager;
	private final IncludeCache includeCache;
	private final StaticAnalysis autoIncludesAnalysis;
	private final ExecutionQueue executionQueue;
	private final DaemonManager daemonManager = new DaemonManager();
	private final MutableObject<CClosure> uncaughtExceptionHandler = new MutableObject<>();

	/**
	 * Creates a new {@link StaticRuntimeEnv}. All fields in the constructor are required, and must not be null.
	 * @param profiler - The Profiler to use.
	 * @param network - The pre-configured PersistenceNetwork object to use.
	 * @param profiles - The Profiles object to use.
	 * @param taskManager - The TaskManager object to use.
	 * @param executionQueue The ExecutionQueue object to use.
	 * @param includeCache - The IncludeCache object to use.
	 * @param autoIncludesAnalysis - The StaticAnalysis of autoIncludes. Can be null when there are no autoIncludes.
	 */
	public StaticRuntimeEnv(Profiler profiler, PersistenceNetwork network, Profiles profiles, TaskManager taskManager,
			ExecutionQueue executionQueue, IncludeCache includeCache, StaticAnalysis autoIncludesAnalysis) {
		Static.AssertNonNull(profiler, "Profiler must not be null");
		Static.AssertNonNull(network, "PersistenceNetwork must not be null");
		Static.AssertNonNull(profiles, "Profiles must not be null");
		Static.AssertNonNull(taskManager, "TaskManager must not be null");
		Static.AssertNonNull(includeCache, "IncludeCache must not be null");
		Static.AssertNonNull(executionQueue, "MethodScriptExecutionQueue must not be null");
		this.profiler = profiler;
		this.persistenceNetwork = network;
		this.profiles = profiles;
		this.taskManager = taskManager;
		this.includeCache = includeCache;
		this.autoIncludesAnalysis = autoIncludesAnalysis;
		this.executionQueue = executionQueue;
	}

	public Profiler GetProfiler() {
		return this.profiler;
	}

	public PersistenceNetwork GetPersistenceNetwork() {
		return this.persistenceNetwork;
	}

	public Profiles getProfiles() {
		return this.profiles;
	}

	public TaskManager GetTaskManager() {
		return this.taskManager;
	}

	public DaemonManager GetDaemonManager() {
		return this.daemonManager;
	}

	public IncludeCache getIncludeCache() {
		return this.includeCache;
	}

	public StaticAnalysis getAutoIncludeAnalysis() {
		return this.autoIncludesAnalysis;
	}

	public ExecutionQueue getExecutionQueue() {
		return this.executionQueue;
	}

	public CClosure getExceptionHandler() {
		return uncaughtExceptionHandler.getObject();
	}

	public void setExceptionHandler(CClosure closure) {
		uncaughtExceptionHandler.setObject(closure);
	}

	@Override
	public EnvironmentImpl clone() throws CloneNotSupportedException {
		// This is a static final environment, a clone would be identical.
		return this;
	}
}
