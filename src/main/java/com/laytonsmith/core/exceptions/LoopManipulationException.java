
package com.laytonsmith.core.exceptions;

import com.laytonsmith.core.constructs.Target;

/**
 * This is thrown by constructs like break and continue to indicate that
 * a loop specific ProgramFlowManipulationException is being thrown.
 */
public abstract class LoopManipulationException extends ProgramFlowManipulationException {
	private int times;
	private String name;
	protected LoopManipulationException(int times, String name, Target t){
		super(t);
		this.times = times;
		this.name = name;
	}
	public int getTimes(){
        return times;
    }    
    public void setTimes(int number){
        this.times = number;
    }
	/**
	 * Returns the construct name that triggers this loop manipulation, i.e: break or continue.
	 * @return 
	 */
	public String getName(){
		return name;
	}
}
