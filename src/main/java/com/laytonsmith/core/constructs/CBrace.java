package com.laytonsmith.core.constructs;

import com.laytonsmith.core.ParseTree;

/**
 *
 * @author lsmith
 */
public class CBrace extends CFunction{
	
	ParseTree code;
	public CBrace(ParseTree code){
		super(code.toString(), code.getTarget());
		this.code = code;
	}

	@Override
	public boolean isDynamic() {
		return true;
	}
	
	public ParseTree getNode(){
		return code;
	}

	@Override
	public String toString() {
		return "{" + super.toString() + "}";
	}
	
}
