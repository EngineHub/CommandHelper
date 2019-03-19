package com.laytonsmith.core;

import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.ProgramFlowManipulationException;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 * A Callable represents something that is executable.
 */
public interface Callable {

	/**
	 * Executes the callable, giving it the supplied arguments. {@code values} may be null, which means that no
	 * arguments are being sent.
	 *
	 * LoopManipulationExceptions will never bubble up past this point, because they are never allowed, so they are
	 * handled automatically, but other ProgramFlowManipulationExceptions will, . ConfigRuntimeExceptions will also
	 * bubble up past this, since an execution mechanism may need to do custom handling.
	 *
	 * @param environment
	 * @param values The values to be passed to the callable
	 * @param t
	 * @return The return value of the callable, or VOID if nothing was returned
	 * @throws ConfigRuntimeException If any call inside the callable causes a CRE
	 * @throws ProgramFlowManipulationException If any ProgramFlowManipulationException is thrown (other than a
	 * LoopManipulationException) within the callable
	 */
	Mixed executeCallable(Environment environment, Target t, Mixed... values)
			throws ConfigRuntimeException, ProgramFlowManipulationException, CancelCommandException;
}
