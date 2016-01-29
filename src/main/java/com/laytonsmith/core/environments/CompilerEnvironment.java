package com.laytonsmith.core.environments;

import com.laytonsmith.core.constructs.IVariable;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Set;
import java.util.Stack;

/**
 * A CompilerEnvironment is available only at compile time, and contains compilation
 * specific data.
 */
public class CompilerEnvironment implements Environment.EnvironmentImpl, Cloneable {

	Stack<Set<IVariable>> variableStack = new Stack<>();

	/**
	 * Pushes a new variable stack
	 */
	public void pushVariableStack(){
		variableStack.push(new HashSet<IVariable>());
	}

	/**
	 * Pops the lowest variable stack from the list
	 */
	public void popVariableStack(){
		variableStack.pop();
	}

	public void defineIVariable(IVariable ivar){
		variableStack.peek().add(ivar);
	}

	/**
	 * Returns the ivariable from the lowest stack element. If the IVariable is
	 * not defined, null is returned.
	 * @param name
	 * @return
	 */
	public IVariable getIVariableFromStack(String name){
		ListIterator<Set<IVariable>> stackIterator = variableStack.listIterator(variableStack.size());
		while(stackIterator.hasPrevious()){
			Set<IVariable> set = stackIterator.previous();
			for(IVariable v : set){
				if(v.getVariableName().equals(name)){
					return v;
				}
			}
		}
		return null;
	}

	@Override
	public Environment.EnvironmentImpl clone() throws CloneNotSupportedException {
		CompilerEnvironment clone = (CompilerEnvironment) super.clone();

		return clone;
	}

}
