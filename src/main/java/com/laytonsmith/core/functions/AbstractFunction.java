package com.laytonsmith.core.functions;

import com.laytonsmith.core.Env;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import java.util.List;

/**
 *
 * @author layton
 */
public abstract class AbstractFunction implements Function{

    /**
     * By default, we return CVoid.
     * @param t
     * @param env
     * @param nodes
     * @return 
     */
    public Construct execs(Target t, Env env, Script parent, ParseTree... nodes) {
        return new CVoid(t);
    }

    /**
     * By default, we return false, because most functions do not need this
     * @return 
     */
    public boolean useSpecialExec() {
        return false;
    }

    /**
     * Most functions should show up in the normal documentation.
     * @return 
     */
    public boolean appearInDocumentation() {
        return true;
    }            
    
    /**
     * Most functions can't optimize.
     * @return 
     */
    public boolean canOptimize(){
        return false;
    }
    
    /**
     * Just return null by default. Most functions won't get to this anyways, since
     * canOptimize is returning false.
     * @param t
     * @param args
     * @return 
     */
    public Construct optimize(Target t, Construct ... args) throws ConfigCompileException{
        return null;
    }

    /**
     * Most functions (even the ones that can optimize) cannot optimize dynamic things.
     * @return 
     */
    public boolean canOptimizeDynamic() {
        return false;
    }

    /**
     * It may be that a function can simply check for compile errors, but not optimize. In this
     * case, it is appropriate to use this definition of optimizeDynamic, to return a value
     * that will essentially make no changes.
     * @param t
     * @param children
     * @return 
     */
    public ParseTree optimizeDynamic(Target t, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException{
        return null;
    }

    /**
     * Only an extreme few functions should allow braces.
     * @return 
     */
    public boolean allowBraces() {
        return false;
    }     
    
    /**
     * Most functions don't need the varlist.
     * @param varList 
     */
    public void varList(IVariableList varList) {
        
    }

    /**
     * Most functions want the atomic values, not the variable itself.
     * @return 
     */
    @Override
    public boolean preResolveVariables() {
        return true;
    }

	public ExampleScript[] examples() throws ConfigCompileException{
		return null;
	}	

}
