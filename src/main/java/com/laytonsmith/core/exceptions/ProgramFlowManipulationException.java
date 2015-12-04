package com.laytonsmith.core.exceptions;

import com.laytonsmith.core.constructs.Target;

/**
 * If an exception is meant to break the program flow in the script itself, it should
 * extend this, so if an exception passes all the way up to a top level handler, it
 * can address it in a standard way if it doesn't know what to do with these types
 * of exceptions. Things like break, continue, etc are considered Program Flow Manipulations.
 * 
 */
public abstract class ProgramFlowManipulationException extends RuntimeException {

	private final Target t;
	/**
	 * 
	 * @param t The target at which this program flow manipulation construct
	 * was defined.
	 */
	protected ProgramFlowManipulationException(Target t){
		this.t = t;
	}
	
	/**
	 * Returns the code target at which this program flow manipulation construct
	 * was defined, so that if it was used improperly, a full stacktrace can
	 * be shown.
	 * @return 
	 */
	public Target getTarget() {
		return t;
	}

	@Override
	public Throwable fillInStackTrace(){
		return this;
	}
}
