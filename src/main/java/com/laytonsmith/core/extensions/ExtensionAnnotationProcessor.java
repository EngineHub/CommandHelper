package com.laytonsmith.core.extensions;

import com.laytonsmith.PureUtilities.Common.ClassUtils;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

/**
 * Extension processor to assert certain properties in a given extension. At runtime, another check will be made, incase
 * the extension wasn't processed at compile-time, and in the case of multiple MSExtension annotations, will default to
 * the first it finds, in terms of identification, yet in case of multiples, a warning will be printed, and the system
 * will call the appropriate methods on all lifecycle classes it finds, so that extensions aren't left hanging.
 *
 * @author Jason Unger <entityreborn@gmail.com>
 */
@SupportedAnnotationTypes({"com.laytonsmith.core.extensions.MSExtension"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ExtensionAnnotationProcessor extends AbstractProcessor {

	int found = 0;

	/**
	 * Shortcut to stop the build process with an error.
	 *
	 * @param message the message to print
	 * @param element the element causing an issue
	 */
	private void error(String message, Element element) {
		processingEnv.getMessager().printMessage(Kind.ERROR,
				message, element);
	}

	/**
	 * Process a given set of annotations.
	 *
	 * @param annotations
	 * @param roundEnv
	 * @return
	 */
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		boolean isExtensionWithLifecycleClass = false;

		// Find all annotations in this environment with the MSExtension annotation.
		for(Element possible : roundEnv.getElementsAnnotatedWith(MSExtension.class)) {
			StreamUtils.GetSystemOut().println("Processing " + possible);

			// Make sure this compile unit exposes only one lifecycle class
			if(found > 0) {
				error("A given compile unit (IE, Jar file) may contain only"
						+ " ONE lifecycle class!", possible);
				continue;
			}

			Class clazz;

			// Manually load the class, as the class provided by the element isn't sufficient.
			try {
				clazz = getClassFromName(possible.toString());
			} catch (ClassNotFoundException ex) {
				Logger.getLogger(ExtensionAnnotationProcessor.class.getName()).log(Level.SEVERE, null, ex);
				continue;
			}

			Set<Modifier> modifiers = possible.getModifiers();

			// The class must not be abstract.
			if(modifiers.contains(Modifier.ABSTRACT)) {
				error("Lifecycle classes must not be declared abstract!", possible);
				continue;
			}

			// The class must be static.
			if(!modifiers.contains(Modifier.PUBLIC)) {
				error("Lifecycle classes must be declared public!", possible);
				continue;
			}

			// If the class is an embedded class, it must be declared static as well as public.
			if(clazz.isMemberClass() && !modifiers.contains(Modifier.STATIC)) {
				error("Lifecycle class must be declared static when wrapped "
						+ "by an outer class!", possible);
				continue;
			}

			// The class must extend Extension.class
			if(!Extension.class.isAssignableFrom(clazz)) {
				error("Lifecycle class must extend AbstractExtension!", possible);
				continue;
			}

			MSExtension annotation = null;

			// Let's get the annotation instance pertaining to this class, so we
			// can get the name.
			for(Annotation a : clazz.getAnnotations()) {
				if(a instanceof MSExtension) {
					annotation = (MSExtension) a;
				}
			}

			// We really shouldn't ever get here, because of the call used in the
			// for loop above, but handle it anyway.
			if(annotation == null) {
				error("Lifecycle class must be annotated with MSExtension!", possible);
				continue;
			}

			found++;

			StreamUtils.GetSystemOut().println("Extension '" + annotation.value() + "' checks out ok!");

			isExtensionWithLifecycleClass = true;
		}

		return isExtensionWithLifecycleClass;
	}

	private static Class getClassFromName(String className) throws ClassNotFoundException {
		return ClassUtils.forCanonicalName(className, false, ExtensionAnnotationProcessor.class.getClassLoader());
	}
}
