package com.laytonsmith.core.constructs;

/**
 *
 */
public class CSemicolon extends Construct {

	public CSemicolon(Target t) {
		super(";", ConstructType.SEMICOLON, t);
	}

	@Override
	public boolean isDynamic() {
		return false;
	}

}
