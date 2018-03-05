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

	public static boolean isKeyword(Construct node, String keyword) {
		return node instanceof CKeyword && keyword.equals(node.val());
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

}
