package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.compiler.Keyword;
import com.laytonsmith.core.compiler.KeywordList;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 *
 *
 */
public class CKeyword extends CBareString {

	public CKeyword(String name, Target t) {
		super(name, t);
	}

	public Keyword getKeyword() {
		return KeywordList.getKeywordByName(this.val());
	}

	public static boolean isKeyword(ParseTree node, String keyword) {
		return node.getData() instanceof CKeyword && keyword.equals(node.getData().val());
	}

	public static boolean isKeyword(Mixed node, String keyword) {
		return node instanceof CKeyword && keyword.equals(node.val());
	}

	public static boolean isKeyword(Token t, String keyword) {
		return t != null && t.val().equals(keyword)
				&& (t.type == Token.TType.KEYWORD || t.type == Token.TType.FUNC_NAME)
				&& KeywordList.getKeywordNames().contains(keyword);
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

	@Override
	public CKeyword duplicate() {
		throw new UnsupportedOperationException("Should have been removed at compile time");
	}
}
