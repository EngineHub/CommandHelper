/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.Utilities;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.ClassMirror;
import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.constructs.CClassType;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;

/**
 *
 * @author Cailin
 */
public class CheckOverrides extends AbstractProcessor {

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		Set<ClassMirror<?>> classes = ClassDiscovery.getDefaultInstance().getClassesWithAnnotation(typeof.class);
		Set<String> errors = new HashSet<>();
		for(ClassMirror<?> clazz : classes) {
			try {
				// Make sure that TYPE has the same type as the typeof annotation
				CClassType type = (CClassType) ReflectionUtils.get(clazz.loadClass(), "TYPE");
				if(type == null) {
					errors.add("TYPE is null? " + clazz.getClassName());
					continue;
				}
				if(!type.val().equals(clazz.getAnnotation(typeof.class).getValue("value"))) {
					errors.add(clazz.getClassName() + "'s TYPE value is different than the typeof annotation on it");
				}
			} catch (ReflectionUtils.ReflectionException ex) {
				errors.add(clazz.getClassName() + " needs to add the following:\n\t@SuppressWarnings(\"FieldNameHidesFieldInSuperclass\")\n"
						+ "\tpublic static final CClassType TYPE = CClassType.get(\"" + clazz.getAnnotation(typeof.class).getValue("value") + "\");");
			}
		}
		if(!errors.isEmpty()) {
			throw new RuntimeException("\n" + StringUtils.Join(errors, "\n"));
		}
		return false;
	}

}
