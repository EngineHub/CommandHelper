package com.laytonsmith.core.environments;

import com.laytonsmith.PureUtilities.DaemonManager;
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
	private final DaemonManager daemonManager = new DaemonManager();

	/**
	 * Creates a new {@link StaticRuntimeEnv}. All fields in the constructor are required, and must not be null.
	 * @param profiler - The Profiler to use.
	 * @param network - The pre-configured PersistenceNetwork object to use.
	 * @param profiles - The Profiles object to use.
	 * @param taskManager - The TaskManager object to use.
	 */
	public StaticRuntimeEnv(Profiler profiler, PersistenceNetwork network, Profiles profiles, TaskManager taskManager) {
		Static.AssertNonNull(profiler, "Profiler must not be null");
		Static.AssertNonNull(network, "PersistenceNetwork must not be null");
		Static.AssertNonNull(profiles, "Profiles must not be null");
		Static.AssertNonNull(taskManager, "TaskManager must not be null");
		this.profiler = profiler;
		this.persistenceNetwork = network;
		this.profiles = profiles;
		this.taskManager = taskManager;
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

	@Override
	public EnvironmentImpl clone() throws CloneNotSupportedException {
		// This is a static final environment, a clone would be identical.
		return this;
	}
}
