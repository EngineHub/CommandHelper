package com.laytonsmith.core.constructs;

import com.laytonsmith.core.compiler.KeywordHandler;

/**
 *
 * @author lsmith
 */
public class CKeyword extends CBareString{
	private final KeywordHandler handler;
	public CKeyword(String name, Target t, KeywordHandler handler){
		super(name, t);
		this.handler = handler;
	}

	public KeywordHandler getHandler() {
		return handler;
	}

}
