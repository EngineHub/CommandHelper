package com.laytonsmith.core;

import com.laytonsmith.core.constructs.CClassType;


/**
 * This class is for testing concepts
 */
public class MainSandbox {


	public static void main(String[] args) throws Exception {
		System.out.println(FullyQualifiedClassName.forFullyQualifiedClass("asdf.asdf.asdf"));
		CClassType.defineClass(FullyQualifiedClassName.forFullyQualifiedClass("asdf.asdf"));
		System.out.println(CClassType.get(FullyQualifiedClassName.forFullyQualifiedClass("asdf.asdf")));
	}

}
