package com.laytonsmith.core.natives.interfaces;

import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;

/**
 * A Callable represents something that is executable.
 */
@typeof("ms.lang.Callable")
public interface Callable extends Mixed {

	@SuppressWarnings("FieldNameHidesFieldInSuperclass")
	public static final CClassType TYPE = CClassType.get(Callable.class);

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
	 */
	Mixed executeCallable(Environment env, Target t, Mixed... values)
			throws ConfigRuntimeException, CancelCommandException;

	/**
	 * Returns the environment associated with this callable.
	 * @return
	 */
	Environment getEnv();
}
