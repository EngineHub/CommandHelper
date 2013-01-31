package com.laytonsmith.core.environments;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author lsmith
 */
public final class Environment implements Cloneable {
	
	public interface EnvironmentImpl{
		public EnvironmentImpl clone() throws CloneNotSupportedException;
	}

	//Implements LinkedList, so we have a consistent search order
	private List<EnvironmentImpl> environments = new LinkedList<EnvironmentImpl>();
	
	public static Environment createEnvironment(EnvironmentImpl ... envs){
		Environment e = new Environment();
		for(EnvironmentImpl ee : envs){
			if(e == null){
				//Don't allow null environments
				throw new NullPointerException("Environments cannot be null");
			}
			e.addEnv(ee);
		}
		return e;
	}
	
	private Environment(){
		//Private constructor
	}
	
	/**
	 * Gets the requested sub-environment. If multiple environments implement the given
	 * class type, it will only return one, but it will be consistently returned for this
	 * instance.
	 * @param <T> The class of the environment that is returned
	 * @param clazz The environment type requested
	 * @throws InvalidEnvironmentException If the environment doesn't exist; this function will either
	 * return the environment, or throw an exception.
	 * @return 
	 */
	public final <T extends EnvironmentImpl> T getEnv(Class<T> clazz) throws InvalidEnvironmentException {
		for(EnvironmentImpl e : environments){
			if(clazz.isAssignableFrom(e.getClass())){
				return (T)e;
			}
		}
		throw new InvalidEnvironmentException(clazz.getSimpleName() + " is not included in this environment.");
	}
	
	/**
	 * Returns true if this environment contains the sub environment. If this returns false, a call
	 * to getEnv with this argument will cause an InvalidArgumentException to be thrown
	 * @param clazz
	 * @return 
	 */
	public final boolean hasEnv(Class<? extends EnvironmentImpl> clazz){
		for(EnvironmentImpl e : environments){
			if(clazz.isAssignableFrom(e.getClass())){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns the first CustomEnvironment set. Though there may be multiple
	 * environments set that implement CustomEnvironment, the same one will be
	 * consistently returned from this instance of Environment. Since an
	 * Environment is immutable, this will hold true during the entire lifetime
	 * of the environment.
	 * @return A CustomEnvironment from this Environment, or null if none were
	 * set.
	 */
	public CustomEnvironment getCustomEnvironment(){
		for(EnvironmentImpl ei : environments){
			if(ei instanceof CustomEnvironment){
				return (CustomEnvironment)ei;
			}
		}
		return null;
	}
	
	private void addEnv(EnvironmentImpl mixin){
		environments.add(mixin);
	}
	
	public boolean hasEnv(Class<? extends EnvironmentImpl> clazz) {
		return environments.containsKey(clazz);
	}

	@Override
	public Environment clone() throws CloneNotSupportedException {
		Environment clone = (Environment)super.clone();
		clone.environments = new LinkedList<EnvironmentImpl>();
		for(EnvironmentImpl e : environments){
			clone.environments.add(e.clone());
		}
		return clone;
	}		
}
