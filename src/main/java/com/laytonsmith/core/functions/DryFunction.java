package com.laytonsmith.core.functions;

import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 * A DryFunction is a function that can be invoked at all times, even during compilation, and cannot rely on any other
 * devices other than other DryFunctions and primitives. These functions are typically compiled out, but create a more
 * complex data type at compile time. Essentially, it provides a method that looks nearly identical to execs, but does
 * not accept a Script parameter.
 *
 * Generally speaking, this is not strictly necessary to implement, because functions that useSpecialExec and then
 * don't use the Script parameter are functionally equivalent. However, implementing this interface, then simply
 * having execs call dryExec is a good indication that this function should not be used in such a way to violate the
 * contract.
 */
public interface DryFunction {
	Mixed dryExec(Target t, com.laytonsmith.core.environments.Environment env, ParseTree... nodes);
}
