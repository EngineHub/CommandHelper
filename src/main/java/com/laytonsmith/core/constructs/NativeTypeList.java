package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.ClassMirror;
import com.laytonsmith.PureUtilities.ClassLoading.DynamicEnum;
import com.laytonsmith.PureUtilities.Common.Annotations.InterfaceRunnerFor;
import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.annotations.MDynamicEnum;
import com.laytonsmith.annotations.MEnum;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.FullyQualifiedClassName;
import com.laytonsmith.core.natives.interfaces.MEnumType;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.core.natives.interfaces.MixedInterfaceRunner;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * A utility class for managing the native class lists.
 */
public class NativeTypeList {

	// TODO: These methods should add caching, they are called too freqently, and have such a high complexity,
	// that this needs to be done.

	private static final Object NATIVE_TYPE_LOCK = new Object();
	private static volatile Set<String> nativeTypes;
	private static Set<FullyQualifiedClassName> fqNativeTypes;

	/**
	 * The name of the static method in all Mixed classes, which if present, is called when
	 * getInvalidInstanceForUse is called on that class. The method should return a value of
	 * that type, and should accept no parameters. The method name is "ConstructInvalidInstance".
	 */
	public static final String INVALID_INSTANCE_METHOD_NAME = "ConstructInvalidInstance";

	private static final Map<FullyQualifiedClassName, Mixed> INVALID_INSTANCE_CACHE = new ConcurrentHashMap<>();

	/**
	 * Given a simple name of a class, attempts to resolve
	 * within the native types (not user defined types). If the class can't be found, null is returned,
	 * but that just means that it's not defined in the native types, not that it doesn't exist at all.
	 *
	 * Use {@link FullyQualifiedClassName#forDefaultClasses(java.lang.String)} instead.
	 * @param simpleName
	 * @return
	 */
	public static String resolveNativeType(String simpleName) {
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
	 * Returns a list of all the known native classes. This method is threadsafe.
	 *
	 * @return
	 */
	public static Set<FullyQualifiedClassName> getNativeTypeList() {
		@SuppressWarnings("LocalVariableHidesMemberVariable")
		Set<String> nativeTypes = NativeTypeList.nativeTypes;
		if(nativeTypes == null) {
			synchronized(NATIVE_TYPE_LOCK) {
				nativeTypes = NativeTypeList.nativeTypes;
				if(nativeTypes == null) {
					Set<FullyQualifiedClassName> fqNativeTypesPrivate = new HashSet<>();
					nativeTypes = new HashSet<>();

					// Ensure that the jar is loaded. This is mostly useful to not have to worry about unit tests, but
					// in production, this should actually be redundant.
					ClassDiscovery.getDefaultInstance()
							.addDiscoveryLocation(ClassDiscovery.GetClassContainer(Mixed.class));

					for(ClassMirror<? extends Mixed> c : ClassDiscovery.getDefaultInstance()
							.getClassesWithAnnotationThatExtend(typeof.class, Mixed.class)) {
						nativeTypes.add((String) c.getAnnotation(typeof.class).getValue("value"));
						fqNativeTypesPrivate.add(FullyQualifiedClassName.forNativeClass(c.loadClass()));
					}

					for(ClassMirror<? extends Enum> c : ClassDiscovery.getDefaultInstance()
							.getClassesWithAnnotationThatExtend(MEnum.class, Enum.class)) {
						String name = (String) c.getAnnotation(MEnum.class).getValue("value");
						nativeTypes.add(name);
						fqNativeTypesPrivate.add(FullyQualifiedClassName.forFullyQualifiedClass(name));
					}

					for(ClassMirror<? extends DynamicEnum> c : ClassDiscovery.getDefaultInstance()
							.getClassesWithAnnotationThatExtend(MDynamicEnum.class, DynamicEnum.class)) {
						String name = (String) c.getAnnotation(MDynamicEnum.class).getValue("value");
						nativeTypes.add(name);
						fqNativeTypesPrivate.add(FullyQualifiedClassName.forFullyQualifiedClass(name));
					}
					fqNativeTypes = fqNativeTypesPrivate;
					NativeTypeList.nativeTypes = nativeTypes;
				}
			}
		}
		return new HashSet<>(fqNativeTypes);
	}


	private static final Map<FullyQualifiedClassName, Class<? extends Mixed>> NATIVE_CLASS_CACHE
			= new ConcurrentHashMap<>();

	private static interface NullClass extends Mixed {}

	/**
	 * ConcurrentHashMap cannot accept null values (or keys) so we have to use this to put into the
	 * map as our "null" value.
	 */
	private static final Class<? extends Mixed> NULL_CLASS_OBJECT = NullClass.class;

	/**
	 * Returns the java class for the given MethodScript object name. This cannot return anything of a type more
	 * specific than Mixed. For classes that represent enums, an anonymous subclass of {@link MEnumType} will be
	 * returned, which cannot be instantiated directly. In case you need to actually instantiate an instance, you
	 * should use {@link #getInvalidInstanceForUse(com.laytonsmith.core.FullyQualifiedClassName)} instead, which
	 * makes this distinction for you.
	 *
	 * @param fqcn The fully qualified class reference.
	 * @return The java Class object for the class.
	 * @throws ClassNotFoundException If the class can't be found. For user defined classes, this definitely will
	 * be thrown.
	 */
	public static Class<? extends Mixed> getNativeClass(FullyQualifiedClassName fqcn) throws ClassNotFoundException {
		if(fqcn.getNativeClass() != null) {
			return fqcn.getNativeClass();
		}
		// This is super super expensive, so we cannot afford to run this more than once per class. Even more
		// ideally, this information should be stored WITH the class, so there's no runtime penalty whatsoever,
		// but having a local cache is at least an improvement.
		if(NATIVE_CLASS_CACHE.containsKey(fqcn)) {
			Class<? extends Mixed> c = NATIVE_CLASS_CACHE.get(fqcn);
			if(c == NULL_CLASS_OBJECT) {
				// Storing null means it can't be found
				throw new ClassNotFoundException("Could not find the class of type " + fqcn);
			}
		}
		if("auto".equals(fqcn.getFQCN())) {
			// This is an error, as auto is not a real type, but a meta type. Thus this method should never be called
			// with this input, and we can give a more specific error message.
			throw new ClassNotFoundException("auto is not a real type, and cannot be retrieved");
		}
		// Don't use nativeTypes, because we need the class, not the string name.
		for(ClassMirror<? extends Mixed> c : ClassDiscovery.getDefaultInstance()
				.getClassesWithAnnotationThatExtend(typeof.class, Mixed.class)) {
			if(c.getAnnotation(typeof.class).getProxy(typeof.class).value().equals(fqcn.getFQCN())) {
				NATIVE_CLASS_CACHE.put(fqcn, c.loadClass());
				return c.loadClass();
			}
		}
		try {
			Class<? extends Mixed> c = getNativeEnumType(fqcn).getClass();
			NATIVE_CLASS_CACHE.put(fqcn, c);
			return c;
		} catch (ClassNotFoundException e) {
			//
		}
		NATIVE_CLASS_CACHE.put(fqcn, NULL_CLASS_OBJECT);
		throw new ClassNotFoundException("Could not find the class of type " + fqcn);
	}

	/**
	 * Returns the java enum type for the given MethodScript enum name. This does not work with DynamicEnums.
	 * @param fqcn
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static Class<? extends Enum<?>> getNativeEnum(FullyQualifiedClassName fqcn) throws ClassNotFoundException {
		if("ms.lang.enum".equals(fqcn.getFQCN())) {
			throw new ClassNotFoundException("ms.lang.enum is not an actual enum, and cannot be converted to one");
		}
		for(ClassMirror<? extends Enum> c : ClassDiscovery.getDefaultInstance()
				.getClassesWithAnnotationThatExtend(MEnum.class, Enum.class)) {
			if(c.getAnnotation(MEnum.class).getProxy(MEnum.class).value().equals(fqcn.getFQCN())) {
				return (Class<Enum<?>>) c.loadClass();
			}
		}
		throw new ClassNotFoundException("Could not find the class of type " + fqcn);
	}

	/**
	 * Returns the MEnumType for the given methodscriptType.
	 * @param fqcn
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static MEnumType getNativeEnumType(FullyQualifiedClassName fqcn) throws ClassNotFoundException {
		if("ms.lang.enum".equals(fqcn.getFQCN())) {
			return MEnumType.getRootEnumType();
		}
		try {
			Class<? extends Enum<?>> e = getNativeEnum(fqcn);
			return MEnumType.FromEnum(fqcn, (Class<Enum<?>>) e, null, null);
		} catch (ClassNotFoundException ex) {
			// Try DynamicEnums
			for(ClassMirror<? extends DynamicEnum> c : ClassDiscovery.getDefaultInstance()
					.getClassesWithAnnotationThatExtend(MDynamicEnum.class, DynamicEnum.class)) {
				if(c.getAnnotation(MDynamicEnum.class).getProxy(MDynamicEnum.class).value().equals(fqcn.getFQCN())) {
					List<?> values;
					try {
						// This cast currently holds true, which is what we need to access the raw enum value anyways,
						// so if this becomes untrue, the change here should be done carefully.
						values = (List<?>) c.getMethod("values", new Class[0])
								.loadMethod().invoke(null);
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
							| NoSuchMethodException ex1) {
						throw new RuntimeException(ex1);
					}
					Enum[] constants = values.stream()
							.map((Object e) -> (Enum<?>) ReflectionUtils.get(DynamicEnum.class, e, "abstracted"))
							.collect(Collectors.toList()).toArray(new Enum[values.size()]);
					return MEnumType.FromPartialEnum(fqcn, c.loadClass(), constants, null, null);
				}
			}
		}
		throw new ClassNotFoundException("Could not find the class of type " + fqcn);
	}

	/**
	 * Like {@link #getNativeClass(FullyQualifiedClassName)}, except if there is an interface runner for this type, that class
	 * is returned instead. This works, because MixedInterfaceRunner extends Mixed. In general, if you need to construct
	 * an object to call the methods defined in MixedInterfaceRunner, this is the method you should use. Despite being
	 * an instanceof Mixed, you should only call the methods defined in {@link MixedInterfaceRunner}, as all other
	 * methods will throw exceptions.
	 *
	 * @param fqcn
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static Class<? extends Mixed> getNativeClassOrInterfaceRunner(FullyQualifiedClassName fqcn)
			throws ClassNotFoundException {
		try {
			return getInterfaceRunnerFor(fqcn);
		} catch (ClassNotFoundException | IllegalArgumentException ex) {
			return getNativeClass(fqcn);
		}
	}

	/**
	 * Returns the interface runner for the specified methodscript type.
	 *
	 * @param fqcn
	 * @return
	 * @throws ClassNotFoundException If the methodscript type could not be found
	 * @throws IllegalArgumentException If the underlying type isn't a java interface or abstract class
	 */
	public static Class<? extends MixedInterfaceRunner> getInterfaceRunnerFor(FullyQualifiedClassName fqcn) throws
			ClassNotFoundException, IllegalArgumentException {
		Class<? extends Mixed> c = getNativeClass(fqcn);
		if(!c.isInterface() && (c.getModifiers() & Modifier.ABSTRACT) == 0) {
			throw new IllegalArgumentException(fqcn + " does not represent a java interface or abstract class");
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
		throw new ClassNotFoundException("Could not find the runner for interface of type " + fqcn);
	}

	/**
	 * For documentation and other purposes, it is useful to have a default, invalid instance of the underlying type.
	 * For enums and non-enums, the process of instantiating the value is different (MEnums are actually valid as is).
	 * <p>
	 * This method abstracts away the difference, and you can call this instead. This should never be exposed to user
	 * code, to actually construct an instance of an object, but should only be used internally.
	 * <p>
	 * If you need a reference to a native class, it may be easier to use {@link #getNativeInvalidInstanceForUse}.
	 * @param fqcn
	 * @return
	 * @throws java.lang.ClassNotFoundException
	 */
	public static Mixed getInvalidInstanceForUse(FullyQualifiedClassName fqcn) throws ClassNotFoundException {
		if(INVALID_INSTANCE_CACHE.containsKey(fqcn)) {
			return INVALID_INSTANCE_CACHE.get(fqcn);
		}
		Class<? extends Mixed> c = getNativeClassOrInterfaceRunner(fqcn);
		if(ReflectionUtils.hasMethod(c, INVALID_INSTANCE_METHOD_NAME, Mixed.class)) {
			Mixed m = (Mixed) ReflectionUtils.invokeMethod(c, null, INVALID_INSTANCE_METHOD_NAME);
			INVALID_INSTANCE_CACHE.put(fqcn, m);
			return m;
		}
		Mixed m;
		if(MEnumType.class.isAssignableFrom(c)) {
			m = getNativeEnumType(fqcn);
		} else { // Not abstract
			m =  ReflectionUtils.instantiateUnsafe(c);
		}
		INVALID_INSTANCE_CACHE.put(fqcn, m);
		return m;
	}

	/**
	 * For documentation and other purposes, it is useful to have a default, invalid instance of the underlying type.
	 * For enums and non-enums, the process of instantiating the value is different (MEnums are actually valid as is).
	 * <p>
	 * This method abstracts away the difference, and you can call this instead. This should never be exposed to user
	 * code, to actually construct an instance of an object, but should only be used internally.
	 * <p>
	 * Unlike {@link #getInvalidInstanceForUse(com.laytonsmith.core.FullyQualifiedClassName)} this rewraps the
	 * underlying exception as an Error, because native classes should always be found.
	 * @param clazz
	 * @return
	 */
	public static Mixed getNativeInvalidInstanceForUse(Class<? extends Mixed> clazz) {
		if(ClassDiscovery.GetClassAnnotation(clazz, typeof.class) == null) {
			throw new RuntimeException(clazz + " is missing typeof annotation!");
		}
		try {
			return getInvalidInstanceForUse(FullyQualifiedClassName.forNativeClass(clazz));
		} catch (ClassNotFoundException e) {
			throw new Error(e);
		}
	}

}
