package com.laytonsmith.core.environments;

import com.laytonsmith.core.constructs.IVariable;
import com.laytonsmith.core.constructs.IVariableList;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.StackTraceFrame;
import com.laytonsmith.core.exceptions.StackTraceManager;
import com.laytonsmith.core.functions.Exceptions;
import com.laytonsmith.core.StepAction;
import com.laytonsmith.core.natives.interfaces.Mixed;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@link PausedState} backed by the live interpreter state. Used in synchronous
 * debugging mode where the interpreter thread is blocked and its state is read
 * directly rather than from a snapshot copy.
 *
 * <p>This class does NOT own or copy any of the state it references. The interpreter
 * thread must remain blocked while this object is being read.</p>
 */
public class LivePausedState implements PausedState {

	private final Environment env;
	private final Target pauseTarget;
	private final StepAction.FlowControl pendingFlowControl;

	public LivePausedState(Environment env, Target pauseTarget,
			StepAction.FlowControl pendingFlowControl) {
		this.env = env;
		this.pauseTarget = pauseTarget;
		this.pendingFlowControl = pendingFlowControl;
	}

	@Override
	public Target getPauseTarget() {
		return pauseTarget;
	}

	@Override
	public Environment getEnvironment() {
		return env;
	}

	@Override
	public List<StackTraceFrame> getCallStack() {
		StackTraceManager stm = env.getEnv(GlobalEnv.class).peekStackTraceManager();
		if(stm == null) {
			return Collections.emptyList();
		}
		return stm.getCurrentStackTrace();
	}

	@Override
	public int getUserCallDepth() {
		StackTraceManager stm = env.getEnv(GlobalEnv.class).peekStackTraceManager();
		if(stm == null) {
			return 0;
		}
		return stm.getDepth();
	}

	@Override
	public Map<String, Mixed> getVariables() {
		IVariableList varList = env.getEnv(GlobalEnv.class).GetVarList();
		Map<String, Mixed> result = new LinkedHashMap<>();
		for(String name : varList.keySet()) {
			IVariable iv = varList.get(name);
			if(iv != null) {
				result.put(name, iv.ival());
			}
		}
		return result;
	}

	@Override
	public boolean isExceptionPause() {
		return pendingFlowControl != null
				&& pendingFlowControl.getAction() instanceof Exceptions.ThrowAction;
	}

	@Override
	public ConfigRuntimeException getPauseException() {
		if(pendingFlowControl != null
				&& pendingFlowControl.getAction() instanceof Exceptions.ThrowAction ta) {
			return ta.getException();
		}
		return null;
	}

	@Override
	public String toString() {
		return "LivePausedState{target=" + pauseTarget + ", depth=" + getUserCallDepth()
				+ ", vars=" + getVariables().size() + "}";
	}
}
