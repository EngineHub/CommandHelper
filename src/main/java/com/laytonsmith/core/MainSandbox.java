package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.ClassMirror;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.ArrayList;
import java.util.List;


/**
 * This class is for testing concepts
 */
public class MainSandbox {


	public static void main(String[] args) throws Exception {
		ClassDiscovery.getDefaultInstance().addDiscoveryLocation(ClassDiscovery.GetClassContainer(MainSandbox.class));
		List<String> l = new ArrayList<>();
		for(ClassMirror<? extends Mixed> m : ClassDiscovery.getDefaultInstance().getClassesWithAnnotationThatExtend(typeof.class, Mixed.class)) {
			if(m.getSimpleName().equals("CNull")
					|| m.getSimpleName().equals("CMutablePrimitive")
					|| m.getSimpleName().equals("UserObject")
					|| m.getSimpleName().equals("CVoid")
					|| m.getSimpleName().equals("Construct")
					|| m.getSimpleName().equals("IVariable")
					|| m.getSimpleName().equals("Variable")
					|| m.getSimpleName().equals("CFunction")
					|| m.getSimpleName().equals("CEntry")
					|| m.getSimpleName().equals("CLabel")
					|| m.getSimpleName().equals("CLabel")
					|| m.getSimpleName().equals("AbstractCREException")
					) {
				continue;
			}
			l.add(m.getSimpleName());
		}
		System.out.println(" instanceof (" + StringUtils.Join(l, "|") + ")([^a-zA-Z0-9])");
		System.out.println(".isInstanceOf($1.class)$2");
	}

}
