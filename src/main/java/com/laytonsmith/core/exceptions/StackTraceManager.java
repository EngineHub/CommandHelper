package com.laytonsmith.core.exceptions;

import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CRE.CREStackOverflowError;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

/**
 * This object allows for management of a stack trace chain. Each execution environment should have one.
 */
public class StackTraceManager {

	/**
	 * The runtime setting key for configuring the maximum call depth.
	 */
	public static final String MAX_CALL_DEPTH_SETTING = "system.max_call_depth";

	/**
	 * The default maximum call depth. Can be overridden at runtime via the
	 * {@code system.max_call_depth} runtime setting.
	 */
	public static final int DEFAULT_MAX_CALL_DEPTH = 1024;

	private static final CInt DEFAULT_MAX_DEPTH_MIXED
			= new CInt(DEFAULT_MAX_CALL_DEPTH, Target.UNKNOWN);

	private final Stack<StackTraceFrame> elements = new Stack<>();
	private final GlobalEnv gEnv;

	/**
	 * Creates a new, empty StackTraceManager object.
	 *
	 * @param gEnv The global environment, used to read runtime settings for the call depth limit.
	 */
	public StackTraceManager(GlobalEnv gEnv) {
		this.gEnv = gEnv;
	}

	/**
	 * Adds a new stack trace element and checks the call depth against the configured maximum.
	 * If the depth exceeds the limit, a {@link CREStackOverflowError} is thrown.
	 *
	 * @param element The element to be pushed on
	 */
	public void addStackTraceFrame(StackTraceFrame element) {
		elements.add(element);
		Mixed setting = gEnv.GetRuntimeSetting(MAX_CALL_DEPTH_SETTING, DEFAULT_MAX_DEPTH_MIXED);
		int maxDepth = ArgumentValidation.getInt32(setting, element.getDefinedAt(), null);
		if(elements.size() > maxDepth) {
			throw new CREStackOverflowError("Stack overflow", element.getDefinedAt());
		}
	}

	/**
	 * Pops the top stack trace trail element off.
	 */
	public void popStackTraceFrame() {
		elements.pop();
	}

	/**
	 * Returns a copy of the current element list.
	 *
	 * @return
	 */
	public List<StackTraceFrame> getCurrentStackTrace() {
		List<StackTraceFrame> l = new ArrayList<>(elements);
		Collections.reverse(l);
		return l;
	}

	/**
	 * Returns true if the current stack is empty.
	 *
	 * @return
	 */
	public boolean isStackEmpty() {
		return elements.isEmpty();
	}

	/**
	 * Returns true if the current stack has only one element in it.
	 *
	 * @return
	 */
	public boolean isStackSingle() {
		return elements.size() == 1;
	}

	/**
	 * Returns the current depth of the stack trace (the number of proc/closure frames currently active).
	 *
	 * @return The current stack depth
	 */
	public int getDepth() {
		return elements.size();
	}

	/**
	 * Sets the current element's target. This should be changed at every new element execution.
	 *
	 * @param target
	 */
	public void setCurrentTarget(Target target) {
		if(!isStackEmpty()) {
			elements.peek().setDefinedAt(target);
		}
	}


}
