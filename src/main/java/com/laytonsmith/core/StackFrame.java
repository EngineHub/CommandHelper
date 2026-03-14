package com.laytonsmith.core;

import com.laytonsmith.core.constructs.IVariable;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.functions.Function;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents one frame on the interpreter's explicit evaluation stack. Each frame corresponds
 * to a function call or node being evaluated.
 *
 * <p>There are two modes:</p>
 * <ul>
 *   <li><b>Simple mode</b> ({@code flowFunction == null}): The interpreter evaluates children
 *       left-to-right, accumulates results in {@code args}, then calls {@code exec()}.
 *       This is used for normal (non-special-exec) functions.</li>
 *   <li><b>Flow function mode</b> ({@code flowFunction != null}): The interpreter delegates to the
 *       {@link FlowFunction} to decide which children to evaluate and when.
 *       This is used for control flow functions (if, for, and, try, etc.).</li>
 * </ul>
 */
public class StackFrame {

	private final ParseTree node;
	private final Environment env;
	private final Function function;
	private final FlowFunction<?> flowFunction;
	private List<Mixed> args;
	private int childIndex;
	private boolean begun;
	private Object functionState;
	private boolean keepIVariable;

	/**
	 * Creates a stack frame for evaluating the given node.
	 *
	 * @param node The parse tree node being evaluated
	 * @param env The environment at this frame's scope
	 * @param function The function being called (may be null for literal nodes or procedure calls)
	 * @param flowFunction The flow function for special-exec functions (null for simple exec)
	 */
	public StackFrame(ParseTree node, Environment env, Function function, FlowFunction<?> flowFunction) {
		this.node = node;
		this.env = env;
		this.function = function;
		this.flowFunction = flowFunction;
		this.childIndex = 0;
		this.begun = false;
		this.functionState = null;
	}

	/**
	 * Returns the parse tree node this frame is evaluating.
	 */
	public ParseTree getNode() {
		return node;
	}

	/**
	 * Returns the environment at this frame's scope.
	 */
	public Environment getEnv() {
		return env;
	}

	/**
	 * Returns the function being called, or null for literal nodes or procedure calls.
	 */
	public Function getFunction() {
		return function;
	}

	/**
	 * Returns the flow functionr for special-exec functions, or null for simple exec.
	 */
	public FlowFunction<?> getFlowFunction() {
		return flowFunction;
	}

	/**
	 * Returns whether this frame uses a flow function (special-exec) or simple child evaluation.
	 */
	public boolean hasFlowFunction() {
		return flowFunction != null;
	}

	/**
	 * Returns the per-call flow function state. The interpreter stores this opaquely and
	 * passes it back to flow function methods via unchecked cast to the flow function's type parameter.
	 */
	public Object getFunctionState() {
		return functionState;
	}

	/**
	 * Sets the per-call flow function state.
	 */
	public void setFunctionState(Object state) {
		this.functionState = state;
	}

	/**
	 * Sets whether the next child result should keep IVariable as-is (not resolve to value).
	 */
	public void setKeepIVariable(boolean keepIVariable) {
		this.keepIVariable = keepIVariable;
	}

	/**
	 * Returns true if the next child result should keep IVariable as-is.
	 */
	public boolean keepIVariable() {
		return keepIVariable;
	}

	/**
	 * Returns the children of the parse tree node as an array.
	 */
	public ParseTree[] getChildren() {
		List<ParseTree> children = node.getChildren();
		return children.toArray(new ParseTree[0]);
	}

	/**
	 * Returns the number of children of this node.
	 */
	public int getChildCount() {
		return node.numberOfChildren();
	}

	/**
	 * Returns the next child index to evaluate (for simple mode).
	 */
	public int getChildIndex() {
		return childIndex;
	}

	/**
	 * Returns true if there are more children to evaluate (for simple mode).
	 */
	public boolean hasMoreChildren() {
		return childIndex < getChildCount();
	}

	/**
	 * Returns the next child to evaluate and advances the index (for simple mode).
	 */
	public ParseTree nextChild() {
		return node.getChildAt(childIndex++);
	}

	/**
	 * Adds an evaluated child result to the args list (for simple mode).
	 */
	public void addArg(Mixed result) {
		if(args == null) {
			args = new ArrayList<>();
		}
		args.add(result);
	}

	/**
	 * Returns the accumulated evaluated arguments (for simple mode).
	 */
	public Mixed[] getArgs() {
		if(args == null) {
			return new Mixed[0];
		}
		return args.toArray(new Mixed[0]);
	}

	/**
	 * Returns whether begin() has been called on the flow function yet.
	 */
	public boolean hasBegun() {
		return begun;
	}

	/**
	 * Marks this frame's flow function as having been started.
	 */
	public void markBegun() {
		this.begun = true;
	}

	@Override
	public String toString() {
		String name;
		if(function != null) {
			name = function.getName();
		} else if(node.getData() instanceof IVariable iv) {
			name = iv.getVariableName();
		} else {
			name = node.getData().val();
		}
		Target t = node.getTarget();
		String location = t.file() + ":" + t.line() + "." + t.col();
		String mode = flowFunction != null ? "flow" : "simple";
		String state = begun ? "begun" : "pending";
		String detail;
		if(flowFunction != null) {
			String stateStr = functionState != null ? functionState.toString() : "null";
			detail = ", state=" + stateStr;
		} else {
			detail = ", child " + childIndex + "/" + getChildCount();
		}
		String inner = name + ":" + location + " (" + mode + ", " + state + detail + ")";
		// Proc calls are user-visible stack frames
		if(function == null && flowFunction != null) {
			return "[" + inner + "]";
		}
		return inner;
	}
}
