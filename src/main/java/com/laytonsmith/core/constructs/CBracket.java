package com.laytonsmith.core.constructs;

import com.laytonsmith.core.ParseTree;

/**
 *
 * 
 */
public class CBracket extends Construct {
	ParseTree code;
	
	public CBracket(ParseTree code){
		super(code.toString(), ConstructType.BRACKET, code.getTarget());
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
