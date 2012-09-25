package com.laytonsmith.core.environments;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author lsmith
 */
public class Environment {
	
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
	
	public final <T extends EnvironmentImpl> T getEnv(Class<T> clazz){
		if(environments.containsKey(clazz)){
			return (T)environments.get(clazz);
		} else {
			throw new InvalidEnvironmentException(clazz.getSimpleName() + " is not included in this environment.");
		}
	}
	
	private void addEnv(EnvironmentImpl mixin){
		environments.put(mixin.getClass(), mixin);
	}

	@Override
	public Environment clone() throws CloneNotSupportedException {
		Environment clone = new Environment();
		clone.environments = new HashMap<Class<? extends EnvironmentImpl>, Environment.EnvironmentImpl>();
		for(Class c : environments.keySet()){
			clone.environments.put(c, environments.get(c).clone());
		}
		return clone;
	}		
}
