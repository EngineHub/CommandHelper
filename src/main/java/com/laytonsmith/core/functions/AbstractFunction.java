package com.laytonsmith.core.functions;

import com.laytonsmith.core.Env;
import com.laytonsmith.core.GenericTreeNode;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;

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

}
