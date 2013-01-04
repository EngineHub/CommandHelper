

package com.laytonsmith.core.constructs;

import java.util.Collections;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * 
 * @author Layton
 */
public class IVariableList implements Cloneable {
	Stack<Map<IVariable, Construct>> varStack = new Stack<Map<IVariable, Construct>>();
    
	/**
	 * Pushes a new scope onto the stack. Variables that are defined in here
	 * are only valid during this scope, and when the scope is popped, and variables
	 * that were defined in this scope are desctructed as well.
	 */
	public void pushScope(){
		varStack.push(new HashMap<IVariable, Construct>());
	}
	
	public void popScope(){
		try{
			//Call the destructors on all Constructs
			Map<IVariable, Construct> scope = varStack.pop();
			for(Construct c : scope.values()){
				c.destructor();
			}
		} catch(EmptyStackException e){
			//This is actually a programming error.
			throw new Error("varStack was empty, but popScope was called on it anyways");
		}
	}
	
	/**
	 * Sets a variable in the current scope, unless it exists already in
	 * a higher scope.
	 * @param v
	 * @param value 
	 * @return true is returned if the construct already existed, and it is simply
	 * being re-set, and false is returned if it is being set for the first time.
	 */
    public boolean set(IVariable v, Construct value){
		//Quick error check; if no scopes exist, this is not set up correctly
		if(varStack.empty()){
			throw new Error("varStack is empty, but set was called. Did you forget to call pushScope()?");
		}
		//If value is an IVariable itself, we're doing @a = @b, and we actually need to get the value of @b.
		//This should only go 1 deep, because an IVariable should never actually get stored due to this check.
		if(value instanceof IVariable){
			value = get(((IVariable)value), value.getTarget());
		}
		Stack<Map<IVariable, Construct>> scopes = new Stack<Map<IVariable, Construct>>();
		//Manually clone the stack
		for(Map<IVariable, Construct> m : varStack){
			scopes.push(m);
		}
		//Now reverse it, because we want to start from the top scope
		Collections.reverse(scopes);
		while(!scopes.empty()){
			Map<IVariable, Construct> scope = scopes.pop();
			if(scope.containsKey(v)){
				scope.put(v, value);
				//Found and put in
				return true;
			}
		}
		//First time.
		varStack.peek().put(v, value);
		return false;
    }
    
	/**
	 * 
	 * @param name
	 * @param t
	 * @param inStrictMode
	 * @return 
	 */
    public Construct get(IVariable name, Target t){
		Stack<Map<IVariable, Construct>> scopes = new Stack<Map<IVariable, Construct>>();
		//Manually clone the stack
		for(Map<IVariable, Construct> m : varStack){
			scopes.push(m);
		}
        while(!scopes.empty()){
			Map<IVariable, Construct> scope = scopes.pop();
			if(scope.containsKey(name)){
				return scope.get(name);
			}
		}
		//It wasn't found, so that means it wasn't initialized. If we're in
		//strict mode, this is an error, but the compiler would have already
		//caught this, so instead, it has to mean that we are in non-strict mode,
		//so we can safely return the default.
		return new CString("", t);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(varStack.size()).append(" scope(s) defined");
        return b.toString();
    }
    
    @Override
    public IVariableList clone(){
		try{
			IVariableList clone = (IVariableList)super.clone();
			//We actually want to do a deep clone here, so we're gonna manually clone the stack.
			clone.varStack = new Stack<Map<IVariable, Construct>>();
			for(Map<IVariable, Construct> scope : varStack){
				//Deep clone on these guys too. The IVariables are immutable, so no need to
				//clone them, but the rest of the constructs need cloning.
				Map<IVariable, Construct> cloneScope = new HashMap<IVariable, Construct>();
				for(IVariable key : scope.keySet()){
					cloneScope.put(key, scope.get(key).clone());
				}
				clone.varStack.push(cloneScope);
			}
			return clone;
		} catch(CloneNotSupportedException e){
			//Shouldn't happen, but if it does, it's a programming error.
			throw new Error(e);
		}
    }

    //only the reflection package should be accessing this
    public Set<String> keySet() {
        Set<String> names = new HashSet<String>();
		for(Map<IVariable, Construct> scope : varStack){
			for(IVariable key : scope.keySet()){
				names.add(key.getName());
			}
		}
		return names;
    }
    
    
}
