package com.laytonsmith.core;

import com.laytonsmith.annotations.NonInheritImplements;

/**
 * This class is for testing concepts
 */
public class MainSandbox {

	public static interface B1 {
		String method(int i, String b);
	}
	@NonInheritImplements(B1.class)
	public static class A1 {
		public String method(int i, String b) {
			return "";
		}
	}
	public static void main(String[] argv) throws Exception {

	}

}
