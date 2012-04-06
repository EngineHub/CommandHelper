package com.laytonsmith.core.functions;

import com.laytonsmith.core.Env;
import com.laytonsmith.core.GenericTreeNode;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
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
    public Construct execs(Target t, Env env, Script parent, GenericTreeNode<Construct>... nodes) {
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
    public GenericTreeNode<Construct> optimizeDynamic(Target t, List<GenericTreeNode<Construct>> children) throws ConfigCompileException, ConfigRuntimeException{
        GenericTreeNode<Construct> node = new GenericTreeNode<Construct>();
        node.data = new CFunction(this.getName(), t);
        node.children = children;
        node.optimized = true;
        return node;
    }

    public boolean allowBraces() {
        return false;
    }        

}
