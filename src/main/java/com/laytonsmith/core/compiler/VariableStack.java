package com.laytonsmith.core.compiler;

import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 *
 *
 */
public final class VariableStack {

	private final Stack<Map<String, Construct>> vars;

	public VariableStack() {
		vars = new Stack<>();
		pushScope();
	}

	public void pushScope() {
		vars.push(new HashMap<>());
	}

	public void popScope() {
		vars.pop();
	}

	private Construct get(String name, Target t) {
		List<Map<String, Construct>> varsReversed = new ArrayList<>();
		Collections.reverse(varsReversed);
		for(Map<String, Construct> map : varsReversed) {
			if(map.containsKey(name)) {
				return map.get(name);
			}
		}
		return new CString("", t);
	}

	public void assign(String name, Construct value) {
		vars.peek().put(name, value);
	}
}
