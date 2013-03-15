package com.laytonsmith.core.compiler;

import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 * If an MAnnotation is a parameter annotation, and can hook into the compile time or runtime
 * checks, then it should implement this interface, which allows for the compiler to send it
 * information about the parameter it is annotating (either at compile or runtime, depending
 * on the options set, and the nature of the code).
 */
public interface CompilerAwareAnnotation {
	
	/**
	 * This function validates a parameter that is passed in. The validation
	 * must either simply return (indicating that the parameter is within the limits)
	 * or throw a ConfigRuntimeException (of any type) if the validation is unsuccessful.
	 * This function purposefully is not able to throw a {@link ConfigCompileException}, because
	 * it shouldn't be aware of whether or not this is being validated at runtime or compile time.
	 * The framework will convert this to a compile error if it is being run at compile time.
	 * <p>
	 * By the time this method is called, the type checking has already been done for the parameter.
	 * @param parameter The parameter to validate
	 * @param t The target to use in the exception
	 * @throws ConfigRuntimeException 
	 */
	void validateParameter(Mixed parameter, Target t) throws ConfigRuntimeException;
	
}
