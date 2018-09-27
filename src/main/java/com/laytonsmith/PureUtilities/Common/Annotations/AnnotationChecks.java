package com.laytonsmith.PureUtilities.Common.Annotations;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.AbstractMethodMirror;
import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.ClassMirror;
import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.ClassReferenceMirror;
import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.ConstructorMirror;
import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.MethodMirror;
import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.ExhaustiveVisitor;
import com.laytonsmith.annotations.NonInheritImplements;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.constructs.CClassType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class is run by maven at compile time, and checks to ensure that the various annotations referenced here are
 * checked, and fail if any of the parameters are missing.
 */
public class AnnotationChecks {

	public static void checkForTypeInTypeofClasses() throws Exception {
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
			throw new Exception("\n" + StringUtils.Join(errors, "\n"));
		}
	}

	@SuppressWarnings("UnnecessaryLabelOnBreakStatement")
	public static void checkForceImplementation() throws Exception {
		Set<String> uhohs = new HashSet<>();
		Set<ConstructorMirror<?>> set = ClassDiscovery.getDefaultInstance().getConstructorsWithAnnotation(ForceImplementation.class);
		for(ConstructorMirror<?> cons : set) {
			ClassReferenceMirror superClass = cons.getDeclaringClass();
			Set<ClassMirror<?>> s = ClassDiscovery.getDefaultInstance().getClassesThatExtend(ClassDiscovery.getDefaultInstance().getClassMirror(superClass));
			checkImplements:
			for(ClassMirror c : s) {
				if(c.getModifiers().isAbstract()) {
					// Abstract classes are not required to implement this
					continue;
				}
				// c is the class we want to check to make sure it implements cons
				for(ConstructorMirror cCons : c.getConstructors()) {
					if(Arrays.asList(cons.getParams()).equals(Arrays.asList(cCons.getParams()))) {
						continue checkImplements;
					}
				}
				if(c.getJVMClassName().contains("$") && c.getModifiers().isStatic()) {
					// Ok, so, an inner, non static class actually passes the super class's reference to the constructor as
					// the first parameter, at a byte code level. So this is a different type of error, or at least, a different
					// error message will be helpful.
					uhohs.add(c.getClassName() + " must be static.");
				} else {
					uhohs.add(c.getClassName() + " must implement the constructor with signature (" + getSignature(cons) + "), but doesn't.");
				}
			}
		}

		Set<MethodMirror> set2 = ClassDiscovery.getDefaultInstance().getMethodsWithAnnotation(ForceImplementation.class);
		for(MethodMirror cons : set2) {
			ClassReferenceMirror superClass = cons.getDeclaringClass();
			@SuppressWarnings("unchecked")
			Set<ClassMirror<?>> s = ClassDiscovery.getDefaultInstance().getClassesThatExtend(ClassDiscovery.getDefaultInstance().getClassMirror(superClass));
			String S = StringUtils.Join(s, "; ");
			checkImplements:
			for(ClassMirror<?> c : s) {
				if(c.getModifiers().isAbstract() ) {
					// Abstract classes are not required to implement this
					continue;
				}
				// First, check if maybe it has a InterfaceRunner for it
				findRunner:
				for(ClassMirror<?> ir : ClassDiscovery.getDefaultInstance().getClassesWithAnnotation(InterfaceRunnerFor.class)) {
					String ira = (String) ir.getAnnotation(InterfaceRunnerFor.class).getValue("value");
					if(ira.equals(c.getClassName())) {
						// Aha! It does. Set c to ir, then break this for loop.
						// The runner for this class will act in the stead of this
						// class.
						c = ir;
						break findRunner;
					}
				}
				// c is the class we want to check to make sure it implements cons
				for(MethodMirror cCons : c.getMethods()) {
					if(cCons.getName().equals(cons.getName()) && Arrays.asList(cons.getParams()).equals(Arrays.asList(cCons.getParams()))) {
						continue checkImplements;
					}
				}
				System.err.println("Adding uh oh");
				uhohs.add(c.getClassName() + " must implement the method with signature " + cons.getName() + "(" + getSignature(cons) + "), but doesn't.");
			}
		}

		if(!uhohs.isEmpty()) {
			List<String> uhohsList = new ArrayList<>(uhohs);
			Collections.sort(uhohsList);
			throw new Exception("There " + StringUtils.PluralHelper(uhohs.size(), "error") + ". The following classes need to implement various methods:\n" + StringUtils.Join(uhohs, "\n"));
		}
	}

	private static String getSignature(AbstractMethodMirror executable) {
		List<String> l = new ArrayList<>();
		for(ClassReferenceMirror crm : executable.getParams()) {
			l.add(crm.toString());
		}
		return StringUtils.Join(l, ", ");
	}

	public static void verifyExhaustiveVisitors() throws ClassNotFoundException {
		Set<ClassMirror<ExhaustiveVisitor>> toVerify;
		toVerify = ClassDiscovery.getDefaultInstance()
				.getClassesThatExtend(ExhaustiveVisitor.class);
		for(ClassMirror<ExhaustiveVisitor> c : toVerify) {
			ExhaustiveVisitor.verify(c);
		}
	}

	public static void verifyNonInheritImplements() throws ClassNotFoundException {
		Set<ClassMirror<?>> toVerify;
		toVerify = ClassDiscovery.getDefaultInstance()
				.getClassesWithAnnotation(NonInheritImplements.class);
		Set<String> uhohs = new HashSet<>();
		for(ClassMirror<?> c : toVerify) {
			Class<?> iface = Class.forName(c.getAnnotation(NonInheritImplements.class).getValue("value").toString());
			if(!iface.isInterface()) {
				uhohs.add("The class given to @NonInheritImplements, tagged on " + c.getClassName() + " is not an interface, and must be.");
				continue;
			}
			// It's an interface, so go through all the methods it has, and make sure that the class c contains all the
			// methods.
			for(Method im : iface.getDeclaredMethods()) {
				try {
					c.getMethod(im.getName(), im.getParameterTypes());
				} catch (NoSuchMethodException ex) {
					String msg = "The class " + c.getClassName() + " implements " + iface.getSimpleName() + " but does not"
							+ " implement the method public " + im.getReturnType().getSimpleName() + " " + im.getName() + "(";
					List<String> params = new ArrayList<>();
					msg += StringUtils.Join(im.getParameters(), ", ", ", ", ", ", "", (Object item) -> {
						Parameter ci = (Parameter) item;
						return ci.getType().getSimpleName() + " " + ci.getName();
					});
					msg += ") {}";
					uhohs.add(msg);
				}
			}
		}
		if(!uhohs.isEmpty()) {
			String error = StringUtils.Join(uhohs, "\n");
			throw new Error(error);
		}
	}

}
