package com.laytonsmith.core.environments;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author lsmith
 */
public class Environment implements Cloneable {
	
	public interface EnvironmentImpl{
		public EnvironmentImpl clone() throws CloneNotSupportedException;
	}

	private Map<Class<? extends EnvironmentImpl>, EnvironmentImpl> environments = new HashMap<Class<? extends EnvironmentImpl>, EnvironmentImpl>();
	
	public static Environment createEnvironment(EnvironmentImpl ... envs){
		Environment e = new Environment();
		for(EnvironmentImpl ee : envs){
			e.addEnv(ee);
		}
		return e;
	}
	
	private Environment(){
		//Private constructor
	}
	
	/**
	 * Gets the requested sub-environment, if available in this environment.
	 * @param <T> The class of the environment that is returned
	 * @param clazz The environment type requested
	 * @throws InvalidEnvironmentException If the environment doesn't exist; this function will either
	 * return the environment, or throw an exception.
	 * @return 
	 */
	public final <T extends EnvironmentImpl> T getEnv(Class<T> clazz) throws InvalidEnvironmentException {
		if(environments.containsKey(clazz)){
			return (T)environments.get(clazz);
		} else {
			throw new InvalidEnvironmentException(clazz.getSimpleName() + " is not included in this environment.");
		}
	}
	
	/**
	 * Returns true if this environment contains the sub environment. If this returns false, a call
	 * to getEnv with this argument will cause an InvalidArgumentException to be thrown
	 * @param clazz
	 * @return 
	 */
	public final boolean hasEnv(Class<? extends EnvironmentImpl> clazz){
		return environments.containsKey(clazz);
	}
	
	private void addEnv(EnvironmentImpl mixin){
		environments.put(mixin.getClass(), mixin);
	}

	@Override
	public Environment clone() throws CloneNotSupportedException {
		Environment clone = (Environment)super.clone();
		clone.environments = new HashMap<Class<? extends EnvironmentImpl>, Environment.EnvironmentImpl>();
		for(Class c : environments.keySet()){
			clone.environments.put(c, environments.get(c).clone());
		}
		return clone;
	}		
}
