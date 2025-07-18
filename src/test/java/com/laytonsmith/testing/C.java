package com.laytonsmith.testing;

import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.Auto;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.IVariable;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.Variable;
import com.laytonsmith.core.environments.Environment;

/**
 * This class provides methods for more easily creating different Constructs for testing purposes.
 *
 */
public class C {

	//Shortcut to Construct.class
	public static final Class<CArray> ARRAY = CArray.class;
	public static final Class<CBoolean> BOOLEAN = CBoolean.class;
	public static final Class<CDouble> DOUBLE = CDouble.class;
	public static final Class<CInt> INT = CInt.class;
	public static final Class<CNull> NULL = CNull.class;
	public static final Class<CString> STRING = CString.class;
	public static final Class<CVoid> VOID = CVoid.class;
	public static final Class<IVariable> IVARIABLE = IVariable.class;
	public static final Class<Variable> VARIABLE = Variable.class;

	public static CArray Array(Construct... elems) {
		return new CArray(Target.UNKNOWN, elems);
	}

	public static CBoolean Boolean(boolean b) {
		return CBoolean.get(b);
	}

	public static CDouble Double(double d) {
		return new CDouble(d, Target.UNKNOWN);
	}

	public static CInt Int(long val) {
		return new CInt(val, Target.UNKNOWN);
	}

	public static CNull Null() {
		return CNull.NULL;
	}

	public static CString String(String s) {
		return new CString(s, Target.UNKNOWN);
	}

	public static Construct Void() {
		return CVoid.VOID;
	}

	public static IVariable IVariable(String name, Construct val, Environment env) {
		return new IVariable(Auto.TYPE, name, val, Target.UNKNOWN, env);
	}

	public static Variable Variable(String name, String val) {
		return new Variable(name, val, false, false, Target.UNKNOWN);
	}

	/**
	 * Returns a construct in the same way that constructs are resolved in scripts.
	 *
	 * @param val
	 * @return
	 */
	public static Construct onstruct(String val) {
		return Static.resolveConstruct(val, Target.UNKNOWN);
	}

	public static Construct onstruct(long val) {
		return Static.resolveConstruct(Long.toString(val), Target.UNKNOWN);
	}

	public static Construct onstruct(boolean val) {
		return Static.resolveConstruct((val ? "true" : "false"), Target.UNKNOWN);
	}

	public static Construct onstruct(double val) {
		return Static.resolveConstruct(java.lang.Double.toString(val), Target.UNKNOWN);
	}
}
