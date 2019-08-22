package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.ClassMirror;
import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.FieldMirror;
import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.MethodMirror;
import java.util.Arrays;
import java.util.List;

/**
 * This class is for testing concepts
 */
public class MainSandbox {


	static class Test<T> {
		<J extends List> J method(Class<J> c) {
			return null;
		}

		T method2(int i, int j, int k) {
			return null;
		}

		T field;
	}

	static class Test2 extends Test<List> {

	}

    public static void main(String[] args) throws Exception {
		ClassDiscovery cd = ClassDiscovery.getDefaultInstance();
		cd.addThisJar();

		ClassMirror<Test2> cm = cd.getMirrorFromClass(Test2.class);
		FieldMirror field = cm.getField("field");
		MethodMirror method = cm.getMethod("method", Class.class);
		MethodMirror method2 = cm.getMethod("method2", new Class[]{int.class, int.class, int.class});
//		System.out.println(method.getElementSignature());
//		System.out.println(method2.getElementSignature());
//		System.out.println(field.getElementSignature());
		System.out.println(method2.loadMethod().getGenericReturnType());
	}

}
