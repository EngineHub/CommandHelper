package com.laytonsmith.core;



import java.util.Arrays;

/**
 * This class is for testing concepts. Please zero out this class other than the main and general utility functions
 * before committing.
 */
public class MainSandbox {

	public static class A<T extends Number> {}

    public static void main(String[] args) throws Exception {
//		A<? super Object> a;
//		a = new A<? extends Number>();
	}

	public static void print(Object o) {
		if(o == null) {
			System.out.println("null");
			return;
		}
		if(o.getClass().isArray()) {
			if(!o.getClass().isPrimitive()) {
				System.out.println(Arrays.toString((Object[]) o));
			}
		} else {
			System.out.println(o);
		}
	}

}
