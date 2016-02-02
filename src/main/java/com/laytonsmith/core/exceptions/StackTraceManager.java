package com.laytonsmith.core.exceptions;

import com.laytonsmith.core.constructs.Target;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

/**
 * This object allows for management of a stack trace chain. Each execution environment should have one. 
 */
public class StackTraceManager {

	private final Stack<ConfigRuntimeException.StackTraceElement> elements = new Stack<>();

	/**
	 * Creates a new, empty StackTraceManager object.
	 */
	public StackTraceManager(){
		//
	}

	/**
	 * Adds a new stack trace trail
	 * @param element The element to be pushed on
	 */
	public void addStackTraceElement(ConfigRuntimeException.StackTraceElement  element){
		elements.add(element);
	}

	/**
	 * Pops the top stack trace trail element off.
	 */
	public void popStackTraceElement(){
		elements.pop();
	}

	/**
	 * Returns a copy of the current element list.
	 * @return
	 */
	public List<ConfigRuntimeException.StackTraceElement> getCurrentStackTrace(){
		List<ConfigRuntimeException.StackTraceElement> l = new ArrayList<>(elements);
		Collections.reverse(l);
		return l;
	}

	/**
	 * Returns true if the current stack is empty.
	 * @return
	 */
	public boolean isStackEmpty(){
		return elements.isEmpty();
	}

	/**
	 * Returns true if the current stack has only one element in it.
	 * @return
	 */
	public boolean isStackSingle(){
		return elements.size() == 1;
	}

	/**
	 * Sets the current element's target. This should be changed at every new element execution.
	 * @param target 
	 */
	public void setCurrentTarget(Target target) {
		if(!isStackEmpty()){
			elements.peek().setDefinedAt(target);
		}
	}

}
