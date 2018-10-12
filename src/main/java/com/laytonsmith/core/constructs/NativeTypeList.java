package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.ClassMirror;
import com.laytonsmith.PureUtilities.Common.Annotations.InterfaceRunnerFor;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.core.natives.interfaces.MixedInterfaceRunner;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * A utility class for managing the native class lists.
 */
public class NativeTypeList {

	private static Set<String> nativeTypes;

	/**
	 * Given a simple name of a class, attempts to resolve
	 * within the native types (not user defined types). If the class can't be found, null is returned,
	 * but that just means that it's not defined in the native types, not that it doesn't exist at all.
	 * @param simpleName
	 * @return
	 */
	public static String resolveType(String simpleName) {
		// Optimization, using internal members
		if(nativeTypes == null) {
			getNativeTypeList();
		}
		// This list should only extremely rarely change
		// This mechanism won't work long term. It works for now, because it just so happens that no
		// simple class names are repeated across the code. But if there were two classes A in different
		// namespaces, then it would only find the first one, rather than causing an error, which is
		// the correct behavior when it's ambiguous. This is the same thing for user classes as well,
		// once those are added.
		Set<String> defaultPackages = new HashSet<>(Arrays.asList("", "ms.lang.", "com.commandhelper."));
		for(String pack : defaultPackages) {
			for(String type : nativeTypes) {
				if((pack + simpleName).equals(type)) {
					return type;
				}
			}
		}
		return null;
	}

	/**
	 * Returns a list of all the known native classes.
	 *
	 * @return
	 */
	public static Set<String> getNativeTypeList() {
		if(nativeTypes != null) {
			return new HashSet<>(nativeTypes);
		}
		nativeTypes = new HashSet<>();
		// Ensure that the jar is loaded. This is mostly useful to not have to worry about unit tests, but in production,
		// this should actually be redundant.
		ClassDiscovery.getDefaultInstance().addDiscoveryLocation(ClassDiscovery.GetClassContainer(Mixed.class));
		for(ClassMirror<? extends Mixed> c : ClassDiscovery.getDefaultInstance().getClassesWithAnnotationThatExtend(typeof.class, Mixed.class)) {
			nativeTypes.add(c.loadAnnotation(typeof.class).value());
		}

		return new HashSet<>(nativeTypes);
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
		String fqcn = resolveType(methodscriptType);
		// Don't use nativeTypes, because we need the class, not the string name.
		for(ClassMirror<? extends Mixed> c : ClassDiscovery.getDefaultInstance().getClassesWithAnnotationThatExtend(typeof.class, Mixed.class)) {
			if(c.getAnnotation(typeof.class).getProxy(typeof.class).value().equals(fqcn)) {
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
