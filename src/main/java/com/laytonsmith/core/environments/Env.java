package com.laytonsmith.core.environments;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author lsmith
 */
public class Env {
	
	public interface EnvImpl{
		
	}

	private Map<Class<? extends EnvImpl>, EnvImpl> environments = new HashMap<Class<? extends EnvImpl>, EnvImpl>();
	
	public static Env createEnvironment(EnvImpl ... envs){
		Env e = new Env();
		for(EnvImpl ee : envs){
			e.addEnv(ee);
		}
		return e;
	}
	
	private Env(){
		//Private constructor
	}
	
	public final <T extends EnvImpl> T getEnv(Class<T> clazz){
		if(environments.containsKey(clazz)){
			return (T)environments.get(clazz);
		} else {
			throw new InvalidEnvironmentException(clazz.getSimpleName() + " is not included in this environment.");
		}
	}
	
	private void addEnv(EnvImpl mixin){
		environments.put(mixin.getClass(), mixin);
	}
}
