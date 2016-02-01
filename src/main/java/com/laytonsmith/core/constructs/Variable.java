package com.laytonsmith.core.constructs;

/**
 *
 * 
 */
public class Variable extends Construct {

    public static final long serialVersionUID = 1L;
    final private String name;
    private String def;
    private boolean optional;
    private boolean final_var;
    private CString var_value;

    public Variable(String name, String def, boolean optional, boolean final_var, Target t) {
        super(name, ConstructType.VARIABLE, t);
        this.name = name;
        setDefault(def);
        this.final_var = final_var;
        this.optional = optional;
        this.var_value = new CString(def, t);
    }

    public Variable(String name, String def, Target t) {
        this(name, def, false, false, t);
    }

    @Override
    public String toString() {
        return "var:" + name;
    }

    public String getVariableName() {
        return name;
    }

    public void setFinal(boolean final_var) {
        this.final_var = final_var;
    }

    public boolean isFinal() {
        return final_var;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public boolean isOptional() {
        return optional;
    }

    public String getDefault() {
        return def;
    }

    public void setDefault(String def) {
		if(def == null){
			def = "";
		}
        this.def = def;
    }

    @Override
    public String val() {
        return var_value.toString();
    }

    public void setVal(CString val) {
        this.var_value = val;
    }
	
	public void setVal(String val){
		this.var_value = new CString(val, this.getTarget());
	}

    @Override
    public Variable clone() throws CloneNotSupportedException {
        Variable clone = (Variable) super.clone();
        if (this.var_value != null) {
            clone.var_value = var_value;
        }
        return clone;
    }

    @Override
    public boolean isDynamic() {
        return true;
    }
}
