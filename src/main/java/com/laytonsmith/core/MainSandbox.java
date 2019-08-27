package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.ClassMirror;
import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.FieldMirror;
import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.MethodMirror;
import com.laytonsmith.PureUtilities.Common.StackTraceUtils;
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
		System.out.println(StackTraceUtils.currentMethod());
		next();
	}

	public static void next() {
		MSLog.StringProvider p = () -> "Method: " + StackTraceUtils.currentMethod(true);
		System.out.println(p.getString());
		System.out.println(lambda().getString());
	}

	public static MSLog.StringProvider lambda() {
		return () -> "Method: " + StackTraceUtils.currentMethod();
	}

}
