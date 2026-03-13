package com.laytonsmith.core.exceptions;

import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.Target;

import java.io.IOException;

/**
 * A single frame in a MethodScript stack trace. Each frame corresponds to a user-visible
 * call boundary, such as a procedure call, closure call, or include. Internal function calls
 * (if, for, array_push, etc.) do not produce stack trace frames.
 */
public class StackTraceFrame {

	private final String procedureName;
	private Target definedAt;
	private final Target callSite;

	/**
	 * Creates a new StackTraceFrame.
	 *
	 * @param procedureName The name of the procedure
	 * @param definedAt The code target where the procedure is defined at.
	 */
	public StackTraceFrame(String procedureName, Target definedAt) {
		this(procedureName, definedAt, Target.UNKNOWN);
	}

	/**
	 * Creates a new StackTraceFrame with a call site.
	 *
	 * @param procedureName The name of the procedure
	 * @param definedAt The code target where the procedure is defined at.
	 * @param callSite The code target of the call expression in the caller's code.
	 */
	public StackTraceFrame(String procedureName, Target definedAt, Target callSite) {
		this.procedureName = procedureName;
		this.definedAt = definedAt;
		this.callSite = callSite;
	}

	/**
	 * Gets the name of the procedure.
	 *
	 * @return
	 */
	public String getProcedureName() {
		return procedureName;
	}

	/**
	 * Gets the code target where the procedure is defined at.
	 *
	 * @return
	 */
	public Target getDefinedAt() {
		return definedAt;
	}

	/**
	 * Gets the code target of the call expression in the caller's code. For example,
	 * if {@code <main>} calls {@code _foo()}, the call site is where {@code _foo()}
	 * appears in {@code <main>}. Returns {@link Target#UNKNOWN} if not available.
	 *
	 * @return The call site target
	 */
	public Target getCallSite() {
		return callSite;
	}

	@Override
	public String toString() {
		return procedureName + " (Defined at " + definedAt + ")";
	}

	/**
	 * Returns a CArray representation of this stack trace frame,
	 * suitable for use in MethodScript code.
	 *
	 * @return
	 */
	public CArray getObjectFor() {
		CArray element = CArray.GetAssociativeArray(Target.UNKNOWN);
		element.set("id", getProcedureName());
		try {
			String name = "Unknown file";
			if(getDefinedAt().file() != null) {
				name = getDefinedAt().file().getCanonicalPath();
			}
			element.set("file", name);
		} catch(IOException ex) {
			// This shouldn't happen, but if it does, we want to fall back to something marginally useful
			String name = "Unknown file";
			if(getDefinedAt().file() != null) {
				name = getDefinedAt().file().getAbsolutePath();
			}
			element.set("file", name);
		}
		element.set("line", new CInt(getDefinedAt().line(), Target.UNKNOWN), Target.UNKNOWN);
		element.set("col", new CInt(getDefinedAt().col(), Target.UNKNOWN), Target.UNKNOWN);
		return element;
	}

	/**
	 * In general, only the core elements should change this.
	 *
	 * @param target
	 */
	void setDefinedAt(Target target) {
		definedAt = target;
	}
}
