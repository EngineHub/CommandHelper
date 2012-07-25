package com.laytonsmith.core.functions;

import com.laytonsmith.core.constructs.Target;

/**
 * This interface should be extended to let the static compiler know how to "execute"
 * a function.
 * @author layton
 */
public interface CompiledFunction {
    public String compile(Target t, String ... args);
}
