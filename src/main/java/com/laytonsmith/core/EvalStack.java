package com.laytonsmith.core;

import java.util.ArrayDeque;
import java.util.Iterator;

/**
 * A wrapper around an {@link ArrayDeque} of {@link StackFrame}s that provides a debugger-friendly
 * {@link #toString()} showing the current execution stack in the style of MethodScript stack traces.
 */
public class EvalStack implements Iterable<StackFrame> {

	private final ArrayDeque<StackFrame> stack;

	public EvalStack() {
		this.stack = new ArrayDeque<>();
	}

	public void push(StackFrame frame) {
		stack.push(frame);
	}

	public StackFrame pop() {
		return stack.pop();
	}

	public StackFrame peek() {
		return stack.peek();
	}

	public boolean isEmpty() {
		return stack.isEmpty();
	}

	public int size() {
		return stack.size();
	}

	@Override
	public Iterator<StackFrame> iterator() {
		return stack.iterator();
	}

	@Override
	public String toString() {
		if(stack.isEmpty()) {
			return "<empty stack>";
		}
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		Iterator<StackFrame> it = stack.descendingIterator();
		while(it.hasNext()) {
			if(!first) {
				sb.append("\n");
			}
			sb.append("at ").append(it.next().toString());
			first = false;
		}
		return sb.toString();
	}
}
