package com.laytonsmith.core.compiler;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.Implementation.Type;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.api.Platforms;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.RuntimeEnvironment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;

/**
 * The compiler environment provides compilation settings, or other controller
 * specific values. This allows for separation of the compiler from the general layout
 * of configuration and other files. The compiler environment is available to the runtime
 * environment as well, but contains values that the compiler (or function optimizations)
 * might need, and are usually considered "static". The settings are all passed in to the constructor,
 * but you can use the various factory methods to create an environment from other sources.
 * @author lsmith
 */
public class CompilerEnvironment implements Environment.EnvironmentImpl, RuntimeEnvironment {	
	/**
	 * A constant is a construct that is defined in source like ${this}. The value
	 * must be passed in at compile time.
	 */
	private Map<String, Construct> constants = new HashMap<String, Construct>();
	
	/**
	 * A list of included parse trees. These likely will have come from other files, but
	 * the compilation result should have been cached.
	 */
	private List<ParseTree> includes = new ArrayList<ParseTree>();

	/**
	 * A list of assigned vars are kept here, so when in strict mode, if a
	 * variable hasn't been declared yet, it will be a compiler error.
	 */
	private Stack<Set<String>> knownVars = new Stack<Set<String>>();
	private final Implementation.Type implementation;
	private final api.Platforms platform;
	private Map<String, Object> custom = new HashMap<String, Object>();
	private SortedSet<String> flags = new TreeSet<String>();
	
	public CompilerEnvironment(Implementation.Type implementation, api.Platforms platform){
		this.implementation = implementation;
		this.platform = platform;
	}
	
	//TODO: Need to figure out how to do known procs.
	public void setConstant(String name, Construct value){
		constants.put(name, value);
	}
	public void setConstant(String name, String value){
		setConstant(name, new CString(value, Target.UNKNOWN));
	}
	public Construct getConstant(String name){		
		return constants.get(name);
	}
	public void pushVariableStack(){
		knownVars.push(new HashSet<String>());
	}
	public void popVariableStack(){
		knownVars.pop();
	}
	public void addKnownVar(String name){
		knownVars.peek().add(name);
	}
	public boolean isVarKnown(String name){
		for(Set<String> scope : knownVars){
			if(scope.contains(name)){
				return true;
			}
		}
		return false;
	}
	
	public void addInclude(ParseTree tree){
		includes.add(tree);
	}
	
	public List<ParseTree> getIncludes(){
		return new ArrayList<ParseTree>(includes);
	}
	
	
	@Override
	public CompilerEnvironment clone() throws CloneNotSupportedException {
		CompilerEnvironment clone = (CompilerEnvironment)super.clone();
		
		return clone;
	}

	public void SetCustom(String name, Object var) {
		custom.put(name, var);
	}

	public Object GetCustom(String name) {
		return custom.get(name);
	}

	public boolean HasFlag(String name) {
		return flags.contains(name);
	}

	public void SetFlag(String name) {
		flags.add(name);
	}

	public void ClearFlag(String name) {
		flags.remove(name);
	}
	
	public Implementation.Type GetImplementation(){
		return implementation;
	}

	public Platforms GetPlatform() {
		return platform;
	}
	
}
