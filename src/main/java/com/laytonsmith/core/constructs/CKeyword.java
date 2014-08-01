package com.laytonsmith.core.constructs;

import com.laytonsmith.core.compiler.Keyword;
import com.laytonsmith.core.compiler.KeywordList;

/**
 *
 *
 */
public class CKeyword extends CBareString {

	public CKeyword(String name, Target t){
		super(name, t);
	}

	public Keyword getKeyword(){
		return KeywordList.getKeywordByName(this.val());
	}
}
