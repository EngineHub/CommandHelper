package com.laytonsmith.core.environments;

import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.core.Profiles;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.environments.Environment.EnvironmentImpl;
import com.laytonsmith.core.environments.GlobalEnv.GlobalEnvNoOpException;
import com.laytonsmith.core.profiler.Profiler;
import com.laytonsmith.core.taskmanager.TaskManager;
import com.laytonsmith.persistence.PersistenceNetwork;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

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
	 * When constructing a {@link StaticRuntimeEnv} in meta circumstances, it may be helpful to provide a no-op
	 * profiler. This value is set up for that purpose.
	 * It is truly no-op, and no exception is thrown if a method in the interface is used.
	 */
	public static final Profiler NO_OP_PROFILER = Profiler.FakeProfiler();

	/**
	 * When constructing a {@link StaticRuntimeEnv} in meta circumstances, it may be helpful to provide a no-op
	 * persistence network. This value is set up for that purpose.
	 * However, it's not truly no-op, as an exception is thrown if any method in the interface are used, as this points
	 * to a situation where something is being called that isn't compatible with a no op execution.
	 */
	public static final PersistenceNetwork NO_OP_PN = GetErrorNoOp(PersistenceNetwork.class, "NO_OP_PN");

	/**
	 * When constructing a {@link StaticRuntimeEnv} in meta circumstances, it may be helpful to provide a no-op
	 * Profiles object. This value is set up for that purpose.
	 * However, it's not truly no-op, as an exception is thrown if any method in the interface are used, as this points
	 * to a situation where something is being called that isn't compatible with a no op execution.
	 */
	public static final Profiles NO_OP_PROFILES = GetErrorNoOp(Profiles.class, "NO_OP_PROFILES");

	/**
	 * When constructing a {@link StaticRuntimeEnv} in meta circumstances, it may be helpful to provide a no-op
	 * profiler. This value is set up for that purpose.
	 * It is truly no-op, and no exception is thrown if a method in the interface is used.
	 */
	public static final TaskManager NO_OP_TASK_MANAGER = GetNoOp(TaskManager.class);

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

	@SuppressWarnings("unchecked")
	private static <T> T GetErrorNoOp(Class<T> iface, String identifier) {
		return (T) Proxy.newProxyInstance(StaticRuntimeEnv.class.getClassLoader(),
				new Class[] {iface}, (Object proxy, Method method, Object[] args) -> {
					throw new GlobalEnvNoOpException(identifier);
				});
	}

	@SuppressWarnings("unchecked")
	private static <T> T GetNoOp(Class<T> iface) {
		return (T) Proxy.newProxyInstance(StaticRuntimeEnv.class.getClassLoader(),
				new Class[] {iface}, (Object proxy, Method method, Object[] args) -> null);
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
