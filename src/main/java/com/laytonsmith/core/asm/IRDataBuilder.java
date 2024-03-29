package com.laytonsmith.core.asm;

/**
 *
 */
public class IRDataBuilder {

	public static IRData setReturnVariable(int variableId, IRType type) {
		if(type == IRType.OTHER) {
			throw new Error("OTHER cannot be returned outside of a single instruction set.");
		}
		IRData data = new IRData();
		data.resultVariable = variableId;
		data.returnCategory = IRReturnCategory.NORMAL;
		data.resultType = type;
		return data;
	}

	public static IRData asUnreachable() {
		IRData data = new IRData();
		data.returnCategory = IRReturnCategory.UNREACHABLE;
		return data;
	}

	public static IRData asVoid() {
		IRData data = new IRData();
		data.returnCategory = IRReturnCategory.VOID;
		return data;
	}

	public static IRData asOther(IRReturnCategory category) {
		IRData data = new IRData();
		data.returnCategory = category;
		data.resultVariable = -1;
		return data;
	}

	public static IRData asConstant(IRType type, String value) {
		if(type == IRType.OTHER) {
			throw new Error("OTHER cannot be returned outside of a single instruction set.");
		}
		IRData data = new IRData();
		data.resultType = type;
		data.returnCategory = IRReturnCategory.CONSTANT;
		data.constant = value;
		return data;
	}

}
