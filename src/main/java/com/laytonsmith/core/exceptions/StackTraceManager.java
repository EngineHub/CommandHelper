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
	private final Stack<Boolean> markedElements = new Stack<>();

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
		markedElements.add(Boolean.FALSE);
	}

	/**
	 * Pops the top stack trace trail element off.
	 */
	public void popStackTraceElement(){
		elements.pop();
		markedElements.pop();
	}

	/**
	 * Marks the top element as "dirty", meaning that it is no longer in scope, but is being retained for error message
	 * purposes. {@link #popAllMarkedElements() } can be used to remove all such marked elements.
	 */
	public void markElement(){
		for(int i = markedElements.size() -1; i >= 0; i--){
			if(!markedElements.get(i)){
				markedElements.set(i, Boolean.TRUE);
				break;
			}

		}
	}

	/**
	 * Pops all marked elements off that were marked with {@link #markElement() }.
	 */
	public void popAllMarkedElements(){
		while(!markedElements.isEmpty()){
			if(markedElements.peek()){
				markedElements.pop();
				elements.pop();
			} else {
				break;
			}
		}
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
	 * Returns a copy of the current element list, but ignores the marked values. This should be used
	 * for getting a stack trace outside of an exception.
	 * @return
	 */
	public List<ConfigRuntimeException.StackTraceElement> getUnmarkedStackTrace(){
		Stack<ConfigRuntimeException.StackTraceElement> elems = new Stack<>();
		elems.addAll(elements);
		Stack<Boolean> tempMarks = new Stack<>();
		tempMarks.addAll(markedElements);
		while(!tempMarks.isEmpty()){
			if(tempMarks.peek()){
				elems.pop();
				tempMarks.pop();
			} else {
				break;
			}
		}
		Collections.reverse(elems);
		return elems;
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
