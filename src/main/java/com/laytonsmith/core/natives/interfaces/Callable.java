package com.laytonsmith.core.natives.interfaces;

import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.CallbackYield;
import com.laytonsmith.core.ParseTree;
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
	 * ConfigRuntimeExceptions will bubble up past this, since an execution mechanism may need to do custom handling.
	 *
	 * @param env
	 * @param values The values to be passed to the callable
	 * @param t
	 * @return The return value of the callable, or VOID if nothing was returned
	 * @throws ConfigRuntimeException If any call inside the callable causes a CRE
	 * @throws CancelCommandException If die() is called within the callable
	 * @deprecated Functions that call closures should extend {@link CallbackYield}
	 * instead of calling this directly, which re-enters eval() and defeats the iterative interpreter.
	 */
	@Deprecated
	Mixed executeCallable(Environment env, Target t, Mixed... values)
			throws ConfigRuntimeException, CancelCommandException;

	/**
	 * Returns the environment associated with this callable.
	 * @return
	 */
	Environment getEnv();

	/**
	 * Prepares this callable for evaluation on the shared EvalStack, without re-entering eval().
	 * Returns a {@link PreparedCallable} containing the AST node and prepared environment,
	 * or {@code null} if this callable cannot be prepared (the caller should fall back to
	 * {@link #executeCallable}).
	 *
	 * <p>The caller is responsible for popping the stack trace element (via
	 * {@link com.laytonsmith.core.exceptions.StackTraceManager#popStackTraceElement()})
	 * from the returned environment when done.</p>
	 *
	 * @param callerEnv The caller's environment
	 * @param t The call site target
	 * @param values The argument values to bind
	 * @return A {@link PreparedCallable}, or null for sync-only callables
	 */
	default PreparedCallable prepareForStack(Environment callerEnv, Target t, Mixed... values) {
		return null;
	}

	/**
	 * The result of {@link #prepareForStack}. Contains the AST node to evaluate
	 * and the prepared environment with arguments bound.
	 */
	record PreparedCallable(ParseTree node, Environment env) {}
}
