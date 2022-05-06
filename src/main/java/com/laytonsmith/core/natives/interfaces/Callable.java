package com.laytonsmith.core.natives.interfaces;

import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.generics.ConstraintLocation;
import com.laytonsmith.core.constructs.generics.Constraints;
import com.laytonsmith.core.constructs.generics.GenericDeclaration;
import com.laytonsmith.core.constructs.generics.constraints.UnboundedConstraint;
import com.laytonsmith.core.constructs.generics.constraints.VariadicTypeConstraint;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.ProgramFlowManipulationException;

/**
 * A Callable represents something that is executable.
 */
@typeof("ms.lang.Callable")
public interface Callable extends Mixed {

	@SuppressWarnings("FieldNameHidesFieldInSuperclass")
	public static final CClassType TYPE = CClassType.getWithGenericDeclaration(Callable.class,
			new GenericDeclaration(Target.UNKNOWN,
				new Constraints(Target.UNKNOWN, ConstraintLocation.DEFINITION, new UnboundedConstraint(Target.UNKNOWN, "ReturnType")),
				new Constraints(Target.UNKNOWN, ConstraintLocation.DEFINITION, new VariadicTypeConstraint(Target.UNKNOWN, "Parameters"))));

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

	/**
	 * Returns the environment associated with this callable.
	 * @return
	 */
	Environment getEnv();
}
