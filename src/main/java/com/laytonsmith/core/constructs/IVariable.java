package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Version;
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
	private final Target definedTarget;
	public static final String VARIABLE_NAME_REGEX = "@[\\p{L}0-9_]+";

	public IVariable(String name, Target t) throws ConfigCompileException {
		super(name, ConstructType.IVARIABLE, t);
		if(!name.matches(VARIABLE_NAME_REGEX)) {
			throw new ConfigCompileException("IVariables must match the regex: " + VARIABLE_NAME_REGEX, t);
		}
		this.varValue = new CString("", t);
		this.name = name;
		this.type = Auto.TYPE;
		this.definedTarget = t;
	}

	public IVariable(CClassType type, String name, Mixed value, Target t) {
		super(name, ConstructType.IVARIABLE, t);
		if(!type.equals(Auto.TYPE) && !(value instanceof CNull)) {
			if(!InstanceofUtil.isInstanceof(value, type)) {
				throw new CRECastException(name + " is of type " + type.val() + ", but a value of type "
						+ value.typeof() + " was assigned to it.", t);
			}
		}
		if(type.equals(CVoid.TYPE)) {
			throw new CRECastException("Variables may not be of type void", t);
		}
		this.type = type;
		if(value == null) {
			throw new NullPointerException();
		}
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
		return (IVariable) clone;
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

}
