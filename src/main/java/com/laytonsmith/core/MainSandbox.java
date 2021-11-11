package com.laytonsmith.core;



import java.util.Arrays;

/**
 * This class is for testing concepts
 */
public class MainSandbox {

	static class C<T extends Number> {

	}

    public static void main(String[] args) throws Exception {
		C<? extends Number> c = new C<Integer>();
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
