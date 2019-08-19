package com.laytonsmith.core.environments;

import com.laytonsmith.core.compiler.CompilerEnvironment;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This is a generic environment storage class. Environments cannot be removed from the environment, but the environment
 * can be cloned, and added to at that time. Classes don't need to strictly know what environments are contained within
 * the class, but if they are expecting a specific environment type, they can pull it out, and gain the advantage of a
 * specific type of exception being thrown if it is not, and if it is, getting type safety on the environment itself.
 * <p>
 * "Sub environments" must implement {@link EnvironmentImpl}, which ensures that they are cloneable.
 */
public final class Environment implements Cloneable {

	/**
	 * "Sub environments" must implement this. This ensures that they are cloneable.
	 */
	public interface EnvironmentImpl extends Cloneable {

		public EnvironmentImpl clone() throws CloneNotSupportedException;
	}

	private Map<Class<? extends EnvironmentImpl>, EnvironmentImpl> environments = new HashMap<>();

	/**
	 * Creates a new Environment, with the specified sub environments. Note that the Environment created is immutable,
	 * but cloneable, and modifyable at clone time.
	 *
	 * @param envs
	 * @return
	 */
	public static Environment createEnvironment(EnvironmentImpl... envs) {
		Environment e = new Environment();
		for(EnvironmentImpl ee : envs) {
			e.addEnv(ee);
		}
		return e;
	}

	private Environment() {
		//Private constructor
	}

	/**
	 * Returns the environment specified.
	 *
	 * @param <T> The type of the environment, specified by the clazz parameter.
	 * @param clazz The class of the environment to return
	 * @return The environment requested
	 * @throws InvalidEnvironmentException If the environment doesn't exist
	 */
	public final <T extends EnvironmentImpl> T getEnv(Class<T> clazz) throws InvalidEnvironmentException {
		if(environments.containsKey(clazz)) {
			return (T) environments.get(clazz);
		} else {
			throw new InvalidEnvironmentException(clazz.getSimpleName() + " is not included in this environment.");
		}
	}

	private void addEnv(EnvironmentImpl mixin) {
		environments.put(mixin.getClass(), mixin);
	}

	/**
	 * Returns true if the specified environment exists.
	 *
	 * @param clazz
	 * @return
	 */
	public boolean hasEnv(Class<? extends EnvironmentImpl> clazz) {
		return environments.containsKey(clazz);
	}

	/**
	 * Provides a way to clone an environment and add new {@link EnvironmentImpl}'s to it. This allow Environment to
	 * remain immutable, but still allows for adding new ones.
	 *
	 * @param envs
	 * @return
	 */
	public Environment cloneAndAdd(EnvironmentImpl... envs) {
		try {
			Environment clone = clone();
			for(EnvironmentImpl ee : envs) {
				clone.addEnv(ee);
			}
			return clone;
		} catch (CloneNotSupportedException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Clones this environment. Sub environments are cloned as well.
	 *
	 * @return
	 * @throws CloneNotSupportedException
	 */
	@Override
	public Environment clone() throws CloneNotSupportedException {
		Environment clone = (Environment) super.clone();
		clone.environments = new HashMap<>();
		for(Map.Entry<Class<? extends EnvironmentImpl>, EnvironmentImpl> entry : environments.entrySet()) {
			clone.environments.put(entry.getKey(), entry.getValue().clone());
		}
		return clone;
	}

	/**
	 * Returns the set of Class objects for the contained classes.
	 * @return
	 */
	public Set<Class<? extends EnvironmentImpl>> getEnvClasses() {
		return environments.keySet();
	}

	/**
	 * Environments that are used by the core compiler/runtime will always be present. This function returns a
	 * set of those classes.
	 * @return
	 */
	public static Set<Class<? extends EnvironmentImpl>> getDefaultEnvClasses() {
		return new HashSet<>(Arrays.asList(GlobalEnv.class, CompilerEnvironment.class));
	}
}
