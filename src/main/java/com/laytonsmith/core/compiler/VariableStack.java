package com.laytonsmith.core.compiler;

import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;

/**
 * A VariableStack is the container for variables during runtime. Within a stack, variables may not be redefined, across
 * layers, but lower layers may access/set the value of existing variables defined in higher layers.
 *
 * This class may be used both at runtime or compile time, though at compile time, the value may be set to null.
 */
public final class VariableStack {

	/**
	 * The STACK_LIMIT is an unreasonably high level of 2**14 == 16,384. This is the limit for how many stack
	 * frames can exist. This is put in place to detect bugs with variable stack more easily.
	 */
	public static final int STACK_LIMIT = (int) java.lang.Math.pow(2, 14);
	private final List<Map<String, Mixed>> vars;
	/**
	 * During compilation, if a value is modified in two different branches, it becomes impossible to tell what
	 * the value will be after that set of branches is complete. In that case, during compilation, the variable should
	 * be assigned this value, to assert that the value is no longer effectively final, and that it should no longer
	 * be used for optimization purposes.
	 */
	public static final CNull NONDETERMINISTIC
			// This gets around the instance checking
			= CNull.GenerateCNull(new Target(10, null, 10));

	/**
	 * This exception is thrown if a new stack frame is being pushed on, but it is already at the limit.
	 */
	public static class VariableStackOverflowException extends RuntimeException {
		public VariableStackOverflowException(String msg) {
			super(msg);
		}
	}

	/**
	 * This exception is thrown if the variable was already defined, but is being redefined.
	 */
	public static class VariableAlreadyDefinedException extends RuntimeException {
		public VariableAlreadyDefinedException(String msg) {
			super(msg);
		}
	}

	/**
	 * This exception is thrown if the variable was not defined
	 */
	public static class VariableNotDefinedException extends RuntimeException {
		public VariableNotDefinedException(String msg) {
			super(msg);
		}
	}

	/**
	 * Constructs a new, empty VariableStack, with one level already defined.
	 */
	public VariableStack() {
		vars = new ArrayList<>();
		try {
			pushScope();
		} catch(VariableStackOverflowException ex) {
			// This is virtually guaranteed to work, unless STACK_LIMIT was defined to a bogus value.
			throw new Error(ex);
		}
	}

	/**
	 * Pushes a new scope onto the stack. For each pushScope call, there must be exactly one {@link #popScope} call
	 * associated with this, and it should exist in a finally block, to ensure that it always happens, no matter what.
	 * <p>
	 * There is a soft limit of stack frames that may be defined, which is stored as {@link #STACK_LIMIT}. This limit
	 * is not a hard limit, but instead put in place to ensure there is not a runaway creation of stack frames. If this
	 * limit is reached, a {@link VariableStackOverflowException} is thrown.
	 * @throws VariableStack.VariableStackOverflowException If the size of the stack is at the {@link #STACK_LIMIT}, but
	 * a new scope is being pushed.
	 */
	public void pushScope() throws VariableStackOverflowException {
		if(vars.size() >= STACK_LIMIT) {
			throw new VariableStackOverflowException("Attempting to push a new scope, but that would cause the stack"
					+ " limit to overflow the allowed limit of " + STACK_LIMIT);
		}
		vars.add(new HashMap<>());
	}

	/**
	 * Pops a variable scope.
	 */
	public void popScope() {
		if(vars.size() == 1) {
			throw new Error("Attempting to pop a variable stack, but there are none left to pop! Each pushScope"
					+ " may only have exactly one popScope associated with it, and the popScope should be in"
					+ " a finally clause.");
		}
		vars.remove(vars.size() - 1);
	}

	/**
	 * Gets the value of the variable. The whole stack is searched, starting from the bottom, to the top. If the value
	 * could not be found, then null is returned, which means that the value was not defined yet. Depending on the
	 * circumstances, this may not necessarily be an exceptional case, so no exception is thrown, but the caller should
	 * check for null and throw an appropriate exception if necessary.
	 * @param name The variable name.
	 * @return The assigned value, or null if it was not defined. Note that a java null is returned if it was not
	 * defined. In general, if a value was defined in code with no value, {@link CNull#UNDEFINED} should have
	 * been set, and if the value was defined in code with a MethodScript null, {@link CNull#NULL} should have been
	 * set, and so it is important to distinguish between these three types of null.
	 */
	public Mixed get(String name) {
		return get(name, getStackFrame(name));
	}

	/**
	 * Gets the value of the variable. The whole stack is searched in reverse, starting with {@code stackFrame}. If
	 * you know for sure that the variable is defined at a particular location, then this method should be used instead,
	 * so that the stack search completes in O(1) time, rather than O(n). The location of the stack frame should
	 * generally be discovered at compile time, and stored with the variable access node, then passed in here.
	 * <p>
	 * Despite this fact, when using reflection or other dynamic mechanisms, it may still be necessary, at runtime, to
	 * search the entire stack, so it's not necessarily an error to use {@link #get(String)} at runtime, but generally
	 * should be avoided as much as possible.
	 * <p>
	 * Even if the stackFrame is not correct, so long as the value was defined in a higher stack, it would still be
	 * found. This is not an error per se, and may come in useful to reduce the search space some, but not entirely.
	 * <p>
	 * See {@link #getStackFrame(String)} to find the correct stack frame, for future accesses.
	 * @param name
	 * @param stackFrame
	 * @return
	 */
	public Mixed get(String name, int stackFrame) {
		ListIterator<Map<String, Mixed>> it = vars.listIterator(stackFrame);
		while(it.hasPrevious()) {
			Map<String, Mixed> map = it.previous();
			if(map.containsKey(name)) {
				return map.get(name);
			}
		}
		return null;
	}

	/**
	 * Gets the stack frame where a particular variable was defined. This generally should be stored along with variable
	 * usage, and passed in to {@link #get(java.lang.String, int)}, instead of using {@link #get(java.lang.String)},
	 * as it turns the access time from O(n) to O(1). During compilation time, however, this information must
	 * initially be determined, and this method can be used to determine that.
	 * @param name The name of the variable to find the stack frame for
	 * @return The stack frame location that can be passed in to {@link #get(java.lang.String, int)}
	 * @throws VariableNotDefinedException If the variable has not yet been defined. This generally should be changed
	 * into an error or warning, depending on the mode, but it indicates that a variable is being used before it has
	 * been defined.
	 */
	public int getStackFrame(String name) throws VariableNotDefinedException {
		for(int i = vars.size() - 1; i >= 0; i--) {
			Map<String, Mixed> map = vars.get(i);
			if(map.containsKey(name)) {
				return i;
			}
		}
		throw new VariableNotDefinedException(name + " has not beed defined yet");
	}

	/**
	 * Assigns a new value. The stack is searched for the existence of a value already defined, and if so, a
	 * {@link VariableAlreadyDefinedException} is thrown. Generally, this should only be used at compile time,
	 * since it searches the whole stack for the existence of the value first, which is a O(n) operation. At runtime,
	 *
	 * @param name The name of the variable.
	 * @param value The value of the variable. This should be {@link CNull#UNDEFINED} if it is just a definition, and
	 * no assignment is being made, and {@link CNull#NULL} if it is being defined with a MethodScript null value.
	 * In no case, should a java null be passed in here, and if attempted, a {@link NullPointerException} is thrown.
	 * <p>
	 * In general, the value should be defined at compile time, if it is known, as this allows an actual value to be
	 * returned later during compilation, so effectively final values can be used as such. With branch detection logic,
	 * it is possible to determine for sure in some cases what the value should be.
	 * @throws VariableAlreadyDefinedException If the value is already defined.
	 */
	public void assignNew(String name, Mixed value) throws VariableAlreadyDefinedException {
		Objects.requireNonNull(value, "value passed in to assignNew cannot be null");
		aahhhh
	}

	/**
	 * Assigns a
	 * @param name
	 * @param value
	 */
	public void assign(String name, Mixed value) {
		vars.peek().put(name, value);
	}

	public void assign(String name, Mixed value, int stackFrame) {

	}
}
