package com.laytonsmith.core.asm;

import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.constructs.Target;
import java.util.List;

/**
 *
 */
public class IRData {
	/*package*/ IRReturnCategory returnCategory;
	/*package*/ int resultVariable = -1;
	/*package*/ IRType resultType;
	/*package*/ ParseTree node;
	/*package*/ List<Target> targets;
	/*package*/ String constant;

	public int getResultVariable() {
		return resultVariable;
	}

	public IRReturnCategory getReturnCategory() {
		return returnCategory;
	}

	public IRType getResultType() {
		return resultType;
	}

	public ParseTree getNode() {
		return node;
	}

	public String getConstant() {
		return constant;
	}

	/**
	 * This returns the reference value, which can *normally* but not always be used directly. It may be necessary
	 * to individually construct the reference using getResultType and getResultVariable (in the case of variables).
	 * @return
	 */
	public String getReference() {
		if(returnCategory == IRReturnCategory.CONSTANT) {
			return resultType.getIRType() + " " + constant;
		} else if(returnCategory == IRReturnCategory.VOID) {
			return "i1 0";
		} else {
			return resultType.getIRType() + " %" + resultVariable;
		}
	}
}
