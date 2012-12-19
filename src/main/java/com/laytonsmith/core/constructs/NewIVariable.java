package com.laytonsmith.core.constructs;

/**
 *
 * @author lsmith
 */
public class NewIVariable extends IVariable{
	private String name;
	public NewIVariable(String name, Target t){
		super("", null, t);
		this.name = name;
	}

	@Override
	public boolean isDynamic() {
		return true;
	}
	
	public String getName(){
		return name;
	}
}
