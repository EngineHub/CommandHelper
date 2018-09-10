package com.laytonsmith.core.constructs;

import com.methodscript.PureUtilities.Version;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 *
 *
 */
public class CSymbol extends Construct {

	String conversion = "";
	Token.TType symbolType;

	public CSymbol(String symbol, Token.TType type, Target target) {
		super(symbol, ConstructType.SYMBOL, target);
		symbolType = type;
		switch(symbolType) {
			case CONCAT:
				conversion = "concat";
				break;
			case PLUS:
				conversion = "add";
				break;
			case MINUS:
				conversion = "subtract";
				break;
			case MULTIPLICATION:
				conversion = "multiply";
				break;
			case DIVISION:
				conversion = "divide";
				break;
			case EQUALS:
				conversion = "equals";
				break;
			case NOT_EQUALS:
				conversion = "nequals";
				break;
			case STRICT_EQUALS:
				conversion = "sequals";
				break;
			case STRICT_NOT_EQUALS:
				conversion = "snequals";
				break;
			case LT:
				conversion = "lt";
				break;
			case GT:
				conversion = "gt";
				break;
			case LTE:
				conversion = "lte";
				break;
			case GTE:
				conversion = "gte";
				break;
			case LOGICAL_AND:
				conversion = "and";
				break;
			case LOGICAL_OR:
				conversion = "or";
				break;
			case DEFAULT_AND:
				conversion = "dand";
				break;
			case DEFAULT_OR:
				conversion = "dor";
				break;
			case LOGICAL_NOT:
				conversion = "not";
				break;
//			case BIT_AND:
//				conversion = "bit_and";
//				break;
//			case BIT_OR:
//				conversion = "bit_or";
//				break;
//			case BIT_XOR:
//				conversion = "bit_xor";
//				break;
			case MODULO:
				conversion = "mod";
				break;
			case INCREMENT:
				conversion = "inc";
				break;
			case DECREMENT:
				conversion = "dec";
				break;
			case EXPONENTIAL:
				conversion = "pow";
				break;
			default:
				conversion = "";
				break;
		}
	}

	public String convert() {
		return conversion;
	}

	public boolean isPostfix() {
		return symbolType.isPostfix();
	}

	public boolean isUnary() {
		return symbolType.isUnary();
	}

	public boolean isMultaplicative() {
		return symbolType.isMultaplicative();
	}

	public boolean isAdditive() {
		return symbolType.isAdditive();
	}

	public boolean isRelational() {
		return symbolType.isRelational();
	}

	public boolean isEquality() {
		return symbolType.isEquality();
	}

//	public boolean isBitwiseAnd() {
//		return symbolType.isBitwiseAnd();
//	}
//
//	public boolean isBitwiseXor() {
//		return symbolType.isBitwiseXor();
//	}
//
//	public boolean isBitwiseOr() {
//		return symbolType.isBitwiseOr();
//	}
	public boolean isLogicalAnd() {
		return symbolType.isLogicalAnd();
	}

	public boolean isLogicalOr() {
		return symbolType.isLogicalOr();
	}

	public boolean isDefaultAnd() {
		return symbolType.isDefaultAnd();
	}

	public boolean isDefaultOr() {
		return symbolType.isDefaultOr();
	}

	@Override
	public boolean isDynamic() {
		//It gets turned into a function, but only after the __autoconcat__ features take over,
		//which essentially removes all symbols.
		return false;
	}

	public boolean isExponential() {
		return symbolType.isExponential();
	}

	public boolean isAssignment() {
		return symbolType.isAssignment();
	}

	public String convertAssignment() {
		switch(symbolType) {
			case PLUS_ASSIGNMENT:
				return "add";
			case MINUS_ASSIGNMENT:
				return "subtract";
			case MULTIPLICATION_ASSIGNMENT:
				return "multiply";
			case DIVISION_ASSIGNMENT:
				return "divide";
			case CONCAT_ASSIGNMENT:
				return "concat";
			default:
				return null;
		}
	}

	@Override
	public Version since() {
		return super.since();
	}

	@Override
	public String docs() {
		return super.docs();
	}

	@Override
	public CClassType[] getSuperclasses() {
		return new CClassType[]{Mixed.TYPE};
	}

	@Override
	public CClassType[] getInterfaces() {
		return new CClassType[]{};
	}

}
