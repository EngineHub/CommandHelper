package com.laytonsmith.core.constructs;

import com.laytonsmith.core.ParseTree;

/**
 *
 * 
 */
public class CBrace extends Construct {
	
	ParseTree code;
	public CBrace(ParseTree code){
		super(code.toString(), ConstructType.BRACE, code.getTarget());
		this.code = code;
	}

	@Override
	public boolean isDynamic() {
		return true;
	}
	
	public ParseTree getNode(){
		return code;
	}
	
}
