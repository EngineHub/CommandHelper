package com.laytonsmith.core.constructs;

/**
 *
 * @author lsmith
 */
public class IVariable extends Construct implements Cloneable {
	private final String name;
	public IVariable(String name, Target t){
		super("", t);
		this.name = name;
	}

	@Override
	public boolean isDynamic() {
		return true;
	}
	
	public String getName(){
		return name;
	}

	@Override
	/**
	 * This overrides Construct to make two variables equal if they
	 * have the same name, not the same value.
	 */
	public boolean equals(Object obj) {
		return obj instanceof IVariable && ((IVariable)obj).name.equals(name);
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 29 * hash + (this.name != null ? this.name.hashCode() : 0);
		return hash;
	}

	/**
	 * IVariables are immutable, so this instance is returned.
	 * @return
	 * @throws CloneNotSupportedException 
	 */
	@Override
	public IVariable clone() {
		return this;
	}

	@Override
	public String toString() {
		return name;
	}

	public String typeName() {
		return "$ival";
	}

	
}
