package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.ClassMirror;
import com.laytonsmith.PureUtilities.Common.Annotations.InterfaceRunnerFor;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.core.natives.interfaces.MixedInterfaceRunner;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

/**
 * A utility class for managing the native class lists.
 */
public class NativeTypeList {

	private static Set<String> nativeTypes;

	/**
	 * Returns a list of all the known native classes.
	 *
	 * @return
	 */
	public static Set<String> getNativeTypeList() {
		if(nativeTypes != null) {
			return nativeTypes;
		}
		nativeTypes = new HashSet<>();
		for(ClassMirror<? extends Mixed> c : ClassDiscovery.getDefaultInstance().getClassesWithAnnotationThatExtend(typeof.class, Mixed.class)) {
			nativeTypes.add(c.loadAnnotation(typeof.class).value());
		}
		// Also add this one in
		nativeTypes.add("mixed");
		return nativeTypes;
	}

	/**
	 * Returns the java class for the given MethodScript object name. This cannot return anything of a type more
	 * specific than Mixed.
	 *
	 * @param methodscriptType
	 * @return
	 * @throws ClassNotFoundException If the class can't be found
	 */
	public static Class<? extends Mixed> getNativeClass(String methodscriptType) throws ClassNotFoundException {
		for(ClassMirror<? extends Mixed> c : ClassDiscovery.getDefaultInstance().getClassesWithAnnotationThatExtend(typeof.class, Mixed.class)) {
			if(c.getAnnotation(typeof.class).getProxy(typeof.class).value().equals(methodscriptType)) {
				return c.loadClass();
			}
		}
		throw new ClassNotFoundException("Could not find the class of type " + methodscriptType);
	}

	/**
	 * Like {@link #getNativeClass(java.lang.String)}, except if there is an interface runner for this type, that class
	 * is returned instead. This works, because MixedInterfaceRunner extends Mixed. In general, if you need to construct
	 * an object to call the methods defined in MixedInterfaceRunner, this is the method you should use. Despite being
	 * an instanceof Mixed, you should only call the methods defined in {@link MixedInterfaceRunner}, as all other
	 * methods will throw exceptions.
	 *
	 * @param methodscriptType
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static Class<? extends Mixed> getNativeClassOrInterfaceRunner(String methodscriptType) throws ClassNotFoundException {
		try {
			return getInterfaceRunnerFor(methodscriptType);
		} catch (ClassNotFoundException | IllegalArgumentException ex) {
			return getNativeClass(methodscriptType);
		}
	}

	/**
	 * Returns the interface runner for the specified methodscript type.
	 *
	 * @param methodscriptType
	 * @return
	 * @throws ClassNotFoundException If the methodscript type could not be found
	 * @throws IllegalArgumentException If the underlying type isn't a java interface or abstract class
	 */
	public static Class<? extends MixedInterfaceRunner> getInterfaceRunnerFor(String methodscriptType) throws
			ClassNotFoundException, IllegalArgumentException {
		Class<? extends Mixed> c = getNativeClass(methodscriptType);
		if(!c.isInterface() && (c.getModifiers() & Modifier.ABSTRACT) == 0) {
			throw new IllegalArgumentException(methodscriptType + " does not represent a java interface or abstract class");
		}
		Set<Class<? extends MixedInterfaceRunner>> set = ClassDiscovery.getDefaultInstance()
				.loadClassesWithAnnotationThatExtend(InterfaceRunnerFor.class, MixedInterfaceRunner.class);
		for(Class<? extends MixedInterfaceRunner> cl : set) {
			if(cl == MixedInterfaceRunner.class) {
				continue;
			}
			if(cl.getAnnotation(InterfaceRunnerFor.class).value() == c) {
				return cl;
			}
		}
		throw new ClassNotFoundException("Could not find the runner for interface of type " + methodscriptType);
	}

}
