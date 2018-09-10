package com.laytonsmith.core.constructs;

import com.methodscript.PureUtilities.Version;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 *
 *
 */
public class Variable extends Construct {

	public static final long serialVersionUID = 1L;
	private final String name;
	private String def;
	private boolean optional;
	private boolean finalVar;
	private CString varValue;

	public Variable(String name, String def, boolean optional, boolean finalVar, Target t) {
		super(name, ConstructType.VARIABLE, t);
		this.name = name;
		setDefault(def);
		this.finalVar = finalVar;
		this.optional = optional;
		this.varValue = new CString(def, t);
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

	public void setFinal(boolean finalVar) {
		this.finalVar = finalVar;
	}

	public boolean isFinal() {
		return finalVar;
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
		if(def == null) {
			def = "";
		}
		this.def = def;
	}

	@Override
	public String val() {
		return varValue.toString();
	}

	public void setVal(CString val) {
		this.varValue = val;
	}

	public void setVal(String val) {
		this.varValue = new CString(val, this.getTarget());
	}

	@Override
	public Variable clone() throws CloneNotSupportedException {
		Variable clone = (Variable) super.clone();
		if(this.varValue != null) {
			clone.varValue = varValue;
		}
		return clone;
	}

	@Override
	public boolean isDynamic() {
		return true;
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
