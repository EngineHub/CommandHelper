package com.laytonsmith.core.constructs;

/**
 *
 * 
 */
public class NewIVariable extends Construct{
	private String name;
	public NewIVariable(String name, Target t){
		super("", ConstructType.IVARIABLE, t);
		this.name = name;
	}

	@Override
	public boolean isDynamic() {
		return true;
	}
	
	public String getVariableName(){
		return name;
	}
}
