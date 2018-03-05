package com.laytonsmith.core.exceptions;

import com.laytonsmith.core.constructs.Target;

/**
 * This is thrown by constructs like break and continue to indicate that a loop specific
 * ProgramFlowManipulationException is being thrown.
 */
public abstract class LoopManipulationException extends ProgramFlowManipulationException {

	private int times;
	private final String name;

	protected LoopManipulationException(int times, String name, Target t) {
		super(t);
		this.times = times;
		this.name = name;
	}

	/**
	 * Returns the number of times specified in the loop manipulation.
	 *
	 * @return
	 */
	public int getTimes() {
		return times;
	}

	/**
	 * Sets the number of times remaining in the loop manipulation. After handling an interation, you should decrement
	 * the number and set it here.
	 *
	 * @param number
	 */
	public void setTimes(int number) {
		this.times = number;
	}

	/**
	 * Returns the construct name that triggers this loop manipulation, i.e: break or continue.
	 *
	 * @return
	 */
	public String getName() {
		return name;
	}
}
