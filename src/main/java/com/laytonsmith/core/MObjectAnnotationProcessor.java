package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.ClassMirror;
import com.laytonsmith.PureUtilities.Common.Annotations.CheckOverrides;
import com.laytonsmith.PureUtilities.Common.ClassUtils;
import com.laytonsmith.annotations.mobject;
import com.laytonsmith.core.mobjects.MObject;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

/**
 *
 */
@SupportedAnnotationTypes({"com.laytonsmith.annotations.mobject"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class MObjectAnnotationProcessor extends AbstractProcessor {

	@Override
	public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnv) {
		if(!roundEnv.processingOver()) {
			List<Class> classesWithMObjectAnnotation = new ArrayList<>();
			for(Element element : roundEnv.getElementsAnnotatedWith(mobject.class)) {
				String className = element.toString();
				Class c = null;
				try {
					c = getClassFromName(className);
				} catch (ClassNotFoundException ex) {
					Logger.getLogger(CheckOverrides.class.getName()).log(Level.SEVERE, null, ex);
				}
				if(c != null) {
					if(c.isInterface() || (c.getModifiers() & Modifier.ABSTRACT) > 0) {
						processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
								"Only concrete classes may be annotated with " + mobject.class.getName()
								+ " but found " + c.getName() + " to have been annotated with it.");
					}
					classesWithMObjectAnnotation.add(c);
				}
			}
			for(ClassMirror<MObject> c : ClassDiscovery.getDefaultInstance().getClassesThatExtend(MObject.class)) {
				if(c.isInterface() || c.isAbstract()) {
					continue;
				}
				if(!classesWithMObjectAnnotation.contains(c.loadClass())) {
					processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Concrete objects that extend MObject must"
							+ " have the @mobject annotation, but found " + c.toString() + " without it.");
				}
			}
		}
		return false;
	}

	private static Class getClassFromName(String className) throws ClassNotFoundException {
		return ClassUtils.forCanonicalName(className, false, CheckOverrides.class.getClassLoader());
	}

}
