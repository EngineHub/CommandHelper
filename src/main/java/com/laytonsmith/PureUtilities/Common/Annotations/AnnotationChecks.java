package com.laytonsmith.PureUtilities.Common.Annotations;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.ClassMirror;
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
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
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
						+ "\tpublic static final CClassType TYPE = CClassType.get(" + clazz.getSimpleName() + ".class);");
			}
		}
		if(!errors.isEmpty()) {
			throw new Exception("\n" + StringUtils.Join(errors, "\n"));
		}
	}

	@SuppressWarnings("UnnecessaryLabelOnBreakStatement")
	public static void checkForceImplementation() throws Exception {
		Set<String> uhohs = new HashSet<>();
		Set<Constructor<?>> set = ClassDiscovery.getDefaultInstance().loadConstructorsWithAnnotation(ForceImplementation.class);
		for(Constructor<?> cons : set) {
			Class superClass = cons.getDeclaringClass();
			Set<Class> s = ClassDiscovery.getDefaultInstance().loadClassesThatExtend(superClass);
			checkImplements:
			for(Class c : s) {
				// c is the class we want to check to make sure it implements cons
				for(Constructor cCons : c.getDeclaredConstructors()) {
					if(Arrays.equals(cons.getParameterTypes(), cCons.getParameterTypes())) {
						continue checkImplements;
					}
				}
				if(c.isMemberClass() && (c.getModifiers() & Modifier.STATIC) == 0) {
					// Ok, so, an inner, non static class actually passes the super class's reference to the constructor as
					// the first parameter, at a byte code level. So this is a different type of error, or at least, a different
					// error message will be helpful.
					uhohs.add(c.getName() + " must be static.");
				} else {
					uhohs.add(c.getName() + " must implement the constructor with signature (" + getSignature(cons) + "), but doesn't.");
				}
			}
		}

		Set<Method> set2 = ClassDiscovery.getDefaultInstance().loadMethodsWithAnnotation(ForceImplementation.class);
		for(Method cons : set2) {
			Class superClass = cons.getDeclaringClass();
			@SuppressWarnings("unchecked")
			Set<Class<?>> s = ClassDiscovery.getDefaultInstance().loadClassesThatExtend(superClass);
			checkImplements:
			for(Class<?> c : s) {
				if((c.getModifiers() & Modifier.ABSTRACT) != 0) {
					// Abstract classes are not required to implement any ForceImplementation methods
					continue;
				}
				if(c.isInterface()) {
					// Interfaces are exempt from the requirement
					continue;
				}
				// First, check if maybe it has a InterfaceRunner for it
				findRunner:
				for(Class<?> ir : ClassDiscovery.getDefaultInstance().loadClassesWithAnnotation(InterfaceRunnerFor.class)) {
					InterfaceRunnerFor ira = ir.getAnnotation(InterfaceRunnerFor.class);
					if(ira.value() == c) {
						// Aha! It does. Set c to ir, then break this for loop.
						// The runner for this class will act in the stead of this
						// class.
						c = ir;
						break findRunner;
					}
				}
				// c is the class we want to check to make sure it implements cons
				for(Method cCons : c.getDeclaredMethods()) {
					if(cCons.getName().equals(cons.getName()) && Arrays.equals(cons.getParameterTypes(), cCons.getParameterTypes())) {
						continue checkImplements;
					}
				}
				uhohs.add(c.getName() + " must implement the method with signature " + cons.getName() + "(" + getSignature(cons) + "), but doesn't.");
			}
		}

		if(!uhohs.isEmpty()) {
			List<String> uhohsList = new ArrayList<>(uhohs);
			Collections.sort(uhohsList);
			throw new Exception("There " + StringUtils.PluralHelper(uhohs.size(), "error") + ". The following classes need to implement various methods:\n" + StringUtils.Join(uhohs, "\n"));
		}
	}

	private static String getSignature(Member executable) {
		List<String> l = new ArrayList<>();
//		for(Class cc : executable.getParameterTypes()){
//			l.add(cc.getName());
//		}
		if(executable instanceof Method) {
			for(Class cc : ((Method) executable).getParameterTypes()) {
				l.add(cc.getName());
			}
		} else if(executable instanceof Constructor) {
			for(Class cc : ((Constructor) executable).getParameterTypes()) {
				l.add(cc.getName());
			}
		} else {
			throw new Error("Unexpected executable type");
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
			String[] sTypes = (String[]) c.getAnnotation(NonInheritImplements.class).getValue("parameterTypes");
			Class[] typeParameters = new Class[(sTypes == null ? 0 : sTypes.length)];
			for(int i = 0; i < typeParameters.length; i++) {
				typeParameters[i] = Class.forName(sTypes[i]);
			}
			if(typeParameters.length != iface.getTypeParameters().length) {
				uhohs.add(iface + " declares " + iface.getTypeParameters().length + " generic parameter(s), but "
					+ c.getClassName() + " does not provide enough parameters.");
			}
			for(int i = 0; i < typeParameters.length; i++) {
				Class actualType = typeParameters[i];
				TypeVariable<?> definedType = iface.getTypeParameters()[i];
				// TODO: Understand why this is an array. What causes this to happen? Using the <? extends A | B>
				// operator? That's exceedingly rare, so this will probably not break for quite a while, but if I'm
				// correct, then  I really need a real use case to figure this out. For now, grabbing the first value
				// is probably good enough.
				Class t;
				try {
					t = Class.forName(definedType.getBounds()[0].getTypeName());
				} catch (ClassNotFoundException e) {
					throw new Error(e);
				}
				// actualType needs to extend t
				if(!t.isAssignableFrom(actualType)) {
					uhohs.add("The type definition for the [" + i + "] index parameter for @NonInheritImplements"
							+ " parameterTypes value defined in " + c.getClassName() + " does not match the upper"
							+ " boundary, which is " + t + ". It must be a subclass of that.");
				}
			}
			if(!iface.isInterface()) {
				uhohs.add("The class given to @NonInheritImplements, tagged on " + c.getClassName() + " is not an"
						+ " interface, and must be.");
				continue;
			}
			// It's an interface, so go through all the methods it has, and make sure that the class c contains all the
			// methods.
			for(Method im : iface.getDeclaredMethods()) {
				// We have to go through the generic types, and replace them with the specified type in the annotation.
				// Look through all the parameters in im.getGenericParameterTypes(), and if any of them are defined in
				// iface.getTypeParameters(), use that instead of the value in im.getParameterTypes().
				Class[] parameters = new Class[im.getParameterTypes().length];
				outer: for(int i = 0; i < parameters.length; i++) {
					Type ic = im.getGenericParameterTypes()[i];
					for(int j = 0; j < iface.getTypeParameters().length; j++) {
						TypeVariable<?> jt = iface.getTypeParameters()[j];
						if(jt.getName().equals(ic.getTypeName())) {
							parameters[i] = typeParameters[j];
							continue outer;
						}
					}
					parameters[i] = im.getParameterTypes()[i];
				}
//				System.out.println("im: " + im + "; parameters: " + Arrays.toString(parameters)
//				 + " im.getGenericParameterTypes: " + Arrays.toString(im.getGenericParameterTypes()));
				Class expectedReturnType = im.getReturnType();
				for(int j = 0; j < iface.getTypeParameters().length; j++) {
					TypeVariable<?> jt = iface.getTypeParameters()[j];
					if(jt.getName().equals(im.getGenericReturnType().getTypeName())) {
						// Same thing as above, if this is a generic type, we need to replace the
						// expected return type with the defined generic type
						expectedReturnType = typeParameters[j];
						break;
					}
				}
//				System.out.println("expectedReturnType: " + expectedReturnType);
				try {
					MethodMirror m = c.getMethod(im.getName(), parameters);
					Class returnType = m.getType().loadClass();
//					System.out.println("returnType: " + returnType);
					if(returnType != expectedReturnType) {
						uhohs.add("Expected return type for " + m.loadMethod() + " is " + expectedReturnType
							+ " but in reality is " + returnType);
					}
				} catch (NoSuchMethodException ex) {
					String msg = "The class " + c.getClassName() + " implements " + iface.getSimpleName() + " but does not"
							+ " implement the method public " + expectedReturnType.getSimpleName() + " " + im.getName() + "(";
					List<String> params = new ArrayList<>();
					for(int i = 0; i < im.getParameterCount(); i++) {
						params.add(parameters[i].getSimpleName() + " " + im.getParameters()[i].getName());
					}
					msg += StringUtils.Join(params, ", ", ", ", ", ", "");
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
