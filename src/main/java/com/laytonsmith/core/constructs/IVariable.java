package com.laytonsmith.core.constructs;

import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;

/**
 *
 *
 */
public class IVariable extends Construct implements Cloneable {

    public static final long serialVersionUID = 1L;
    private Construct var_value;
    final private String name;
	final private CClassType type;
	final private Target definedTarget;
	final public static String VARIABLE_NAME_REGEX = "@[\\p{L}0-9_]+";

    public IVariable(String name, Target t) throws ConfigCompileException {
        super(name, ConstructType.IVARIABLE, t);
		if(!name.matches(VARIABLE_NAME_REGEX)){
			throw new ConfigCompileException("IVariables must match the regex: " + VARIABLE_NAME_REGEX, t);
		}
        this.var_value = new CString("", t);
        this.name = name;
		this.type = CClassType.AUTO;
		this.definedTarget = t;
    }

    public IVariable(CClassType type, String name, Construct value, Target t) {
        super(name, ConstructType.IVARIABLE, t);
		if(!type.equals(CClassType.AUTO) && !(value instanceof CNull)){
			if(!InstanceofUtil.isInstanceof(value, type.val())){
				throw new CRECastException(name + " is of type " + type.val() + ", but a value of type "
						+ value.typeof() + " was assigned to it.", t);
			}
		}
		if(type.equals(CClassType.VOID)){
			throw new CRECastException("Variables may not be of type void", t);
		}
		this.type = type;
		if(value == null){
			throw new NullPointerException();
		}
        this.var_value = value;
        this.name = name;
		this.definedTarget = t;
    }

    @Override
    public String val() {
        return var_value.val();
    }

    public Construct ival() {
        var_value.setTarget(getTarget());
        return var_value;
    }

    /**
     * Returns the name of the variable, including the @ sign
     * @return
     */
    public String getVariableName() {
        return name;
    }

    public void setIval(Construct c) {
        var_value = c;
    }

    @Override
    public String toString() {
        return this.name + ":(" + this.ival().getClass().getSimpleName() + ") '" + this.ival().val() + "'";
    }

    @Override
    public IVariable clone() throws CloneNotSupportedException {
        IVariable clone = (IVariable) super.clone();
        if (this.var_value != null) {
            clone.var_value = this.var_value.clone();
        }
        return (IVariable) clone;
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

	/**
	 * Returns the type the variable was defined with, not the type of the current value.
	 * @return
	 */
	public CClassType getDefinedType(){
		return type;
	}

	/**
	 * Returns the target where the variable was initially defined at, not where the enclosed value
	 * was defined.
	 * @return
	 */
	public Target getDefinedTarget(){
		return definedTarget;
	}
}
