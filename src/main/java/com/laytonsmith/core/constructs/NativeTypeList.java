package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.ClassMirror;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public class NativeTypeList {

	public static Set<String> getNativeTypeList(){
		Set<String> ret = new HashSet<>();
		for(ClassMirror<Mixed> c : ClassDiscovery.getDefaultInstance().getClassesWithAnnotationThatExtend(typeof.class, Mixed.class)){
			ret.add(c.loadAnnotation(typeof.class).value());
		}
		// Also add this one in
		ret.add("mixed");
		return ret;
	}
}
