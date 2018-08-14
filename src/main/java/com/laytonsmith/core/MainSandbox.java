package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.Common.Annotations.NonInheritImplements;


/**
 * This class is for testing concepts
 */
public class MainSandbox {

	public static interface Interface {
		void method(int i, String b, int j);
	}
	@NonInheritImplements(Interface.class)
	public static class A1 {
		public void method(int i, String b, int j) {
			System.out.println("i: " + i + "; b: " + b);
		}
	}

	public static class A2 extends A1 {
		void moreMethods() {

		}
	}

	public static void main(String[] argv) throws Exception {
		Interface interf = NonInheritImplements.Helper.Cast(Interface.class, new A1());
		System.out.println(NonInheritImplements.Helper.Instanceof(new A2(), Interface.class));
		myMethod(interf);
	}

	public static void myMethod(Interface iface) {
		iface.method(12, "string", 123);
	}

}
