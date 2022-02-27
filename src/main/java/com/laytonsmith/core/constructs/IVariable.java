package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.constructs.generics.ConstraintValidator;
import com.laytonsmith.core.constructs.generics.LeftHandGenericUse;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 *
 *
 */
public class IVariable extends Construct implements Cloneable {

	public static final long serialVersionUID = 1L;
	private Mixed varValue;
	private final String name;
	private final CClassType type;
	private final LeftHandGenericUse genericDefinition;
	private final Target definedTarget;
	public static final String VARIABLE_NAME_REGEX = "@[\\p{L}0-9_]+";

	public IVariable(String name, Target t) throws ConfigCompileException {
		this(Auto.TYPE, name, new CString("", t), t, null, null);
	}

	public IVariable(CClassType checkedType, String name, Mixed checkedValue, Target t) throws ConfigCompileException {
		this(checkedType, name, checkedValue, t, null, null);
	}

	/**
	 * Temporary function that sets generic parameters to null. This will be removed in the future once the compiler
	 * supports generics, at which point you should explicitely pass in null to the other constructor if there were
	 * no generics defined.
	 * @param type
	 * @param name
	 * @param value
	 * @param t
	 * @param env
	 * @throws ConfigCompileException
	 * @deprecated Use {@link #IVariable(CClassType, String, Mixed, Target, LeftHandGenericUse, Environment)}
	 */
	@Deprecated
	public IVariable(CClassType type, String name, Mixed value, Target t, Environment env) throws ConfigCompileException {
		this(type, name, value, t, null, env);
	}

	/**
	 * Constructs a new IVariable instance.
	 * @param type The type of value, may be auto
	 * @param name The name of the variable
	 * @param value The value, if it was provided. May be CNull, but cannot be java null
	 * @param t The code target where this value was defined
	 * @param genericDefinition The LHS generic definition. Note that in general, this information is not
	 *                          kept during runtime for local variable, though it is for function parameter definitions,
	 *                          purely for reflection purposes. This may be null if the value was defined without
	 *                          generic parameters. If provided, however, it is validated against the class type.
	 * @param env The environment object
	 * @throws ConfigCompileException If the name of the variable does not match the required regex, or the generic
	 * parameters do not validate (either because they are wrong, or because they were provided when there isn't a
	 * generic definition on the ClassType object).
	 * @throws NullPointerException If the value was null
	 */
	public IVariable(CClassType type, String name, Mixed value, Target t, LeftHandGenericUse genericDefinition,
					Environment env) throws ConfigCompileException {
		super(name, ConstructType.IVARIABLE, t);
		if(!name.matches(VARIABLE_NAME_REGEX)) {
			throw new ConfigCompileException("IVariables must match the regex: " + VARIABLE_NAME_REGEX, t);
		}
		if(type.equals(CVoid.TYPE)) {
			throw new CRECastException("Variables may not be of type void", t);
		}
		if(value == null) {
			throw new NullPointerException();
		}
		if(value instanceof CVoid) {
			throw new CRECastException("Void may not be assigned to a variable", t);
		}
		ConstraintValidator.ValidateLHS(t, type, genericDefinition, env);
		if(env != null && (!type.equals(Auto.TYPE) && !(value instanceof CNull))) {
			if(!InstanceofUtil.isInstanceof(value, type, env)) {
				throw new CRECastException(name + " is of type " + type.val() + ", but a value of type "
						+ value.typeof() + " was assigned to it.", t);
			}
		}
		this.genericDefinition = genericDefinition;
		this.type = type;
		this.varValue = value;
		this.name = name;
		this.definedTarget = t;
	}

	@Override
	public String val() {
		return varValue.val();
	}

	public Mixed ival() {
		varValue.setTarget(getTarget());
		return varValue;
	}

	/**
	 * Returns the name of the variable, including the @ sign
	 *
	 * @return
	 */
	public String getVariableName() {
		return name;
	}

	public void setIval(Mixed c) {
		varValue = c;
	}

	@Override
	public String toString() {
		return this.name + ":(" + this.ival().getClass().getSimpleName() + ") '" + this.ival().val() + "'";
	}

	@Override
	public IVariable clone() throws CloneNotSupportedException {
		IVariable clone = (IVariable) super.clone();
		if(this.varValue != null) {
			clone.varValue = this.varValue.clone();
		}
		return clone;
	}

	@Override
	public boolean isDynamic() {
		return true;
	}

	/**
	 * Returns the type the variable was defined with, not the type of the current value.
	 *
	 * @return
	 */
	public CClassType getDefinedType() {
		return type;
	}

	/**
	 * Returns the target where the variable was initially defined at, not where the enclosed value was defined.
	 *
	 * @return
	 */
	public Target getDefinedTarget() {
		return definedTarget;
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

	public LeftHandGenericUse getGenericDefinition() {
		return genericDefinition;
	}
}
