package com.laytonsmith.PureUtilities.Common;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides wrapper methods around some common methods that Class and some of the java.reflect package class left out.
 */
public class ClassUtils {

	private static final Pattern ARRAY_COUNT_PATTERN = Pattern.compile("\\[\\]");

	/**
	 * Returns the Class object, given the in-code class name. This takes into account inner classes not being handled
	 * the same normally, as well as ... for varargs, and [] for arrays. For instance, java.lang.String[] would return
	 * the class object for String[].class. Primitives are handled correctly as well. This works like Class.forName in
	 * all other regards, however.
	 *
	 * @param className The canonical class name.
	 * @return The class object.
	 * @throws ClassNotFoundException If the class can't be found.
	 */
	public static Class forCanonicalName(String className) throws ClassNotFoundException {
		return forCanonicalName(className, false, false, null);
	}

	/**
	 * Returns the Class object, given the in-code class name. This takes into account inner classes not being handled
	 * the same normally, as well as ... for varargs, and [] for arrays. For instance, java.lang.String[] would return
	 * the class object for String[].class. Primitives are handled correctly as well. This works like Class.forName in
	 * all other regards, however.
	 *
	 * @param className The canonical class name
	 * @param initialize whether the class must be initialized
	 * @param classLoader classLoader from which the class must be loaded
	 * @return The class object.
	 * @throws ClassNotFoundException If the class can't be found.
	 */
	public static Class forCanonicalName(String className, boolean initialize, ClassLoader classLoader) throws ClassNotFoundException {
		return forCanonicalName(className, true, initialize, classLoader);
	}

	/**
	 * Private version, which accepts the useInitializer parameter.
	 *
	 * @param className
	 * @param useInitializer
	 * @param initialize
	 * @param classLoader
	 * @return
	 * @throws ClassNotFoundException
	 */
	private static Class forCanonicalName(String className, boolean useInitializer, boolean initialize, ClassLoader classLoader) throws ClassNotFoundException {
		className = StringUtils.replaceLast(className, "\\.\\.\\.", "[]");
		//Of course primitives all need to be dealt with specially.
		int arrays = 0;
		Matcher m = ARRAY_COUNT_PATTERN.matcher(className);
		while(m.find()) {
			arrays++;
		}
		String simpleName = className.replaceAll("\\[\\]", "");
		String primitiveID = null;
		Class primitiveClass = null;
		if(null != simpleName) {
			switch(simpleName) {
				case "boolean":
					primitiveID = "Z";
					primitiveClass = boolean.class;
					break;
				case "byte":
					primitiveID = "B";
					primitiveClass = byte.class;
					break;
				case "short":
					primitiveID = "S";
					primitiveClass = short.class;
					break;
				case "int":
					primitiveID = "I";
					primitiveClass = int.class;
					break;
				case "long":
					primitiveID = "J";
					primitiveClass = long.class;
					break;
				case "float":
					primitiveID = "F";
					primitiveClass = float.class;
					break;
				case "double":
					primitiveID = "D";
					primitiveClass = double.class;
					break;
				case "char":
					primitiveID = "C";
					primitiveClass = char.class;
					break;
			}
		}
		if(primitiveClass != null) {
			if(arrays > 0) {
				//This will be dealt with below
				className = StringUtils.stringMultiply(arrays, "[") + primitiveID;
			} else {
				//Class.forName doesn't know how to deal with this, so short circuit.
				return primitiveClass;
			}
		} else if(arrays > 0) {
			//Ok, we need to get it from the canonical name
			className = StringUtils.stringMultiply(arrays, "[") + "L" + simpleName + ";";
		}
		Class c = null;
		try {
			if(useInitializer) {
				c = Class.forName(className, initialize, classLoader);
			} else {
				c = Class.forName(className);
			}
		} catch (ClassNotFoundException ex) {
			//Ok, try replacing the last . with $ as this may be an inner class
			String name = className;
			while(name.contains(".")) {
				name = StringUtils.replaceLast(name, "\\.", "$");
				try {
					if(useInitializer) {
						c = Class.forName(name, initialize, classLoader);
					} else {
						c = Class.forName(name);
					}
					//Awesome, found it.
					break;
				} catch (ClassNotFoundException e) {
					//No? Try again then.
				}
			}
			if(c == null) {
				//We really couldn't find it.
				throw ex;
			}
		}
		return c;
	}

	/**
	 * Returns the name of the class, as the JVM would output it. For instance, for an int, "I" is returned, for an
	 * array of Objects, "[Ljava/lang/Object;" is returned. If the input is null, null is returned.
	 *
	 * @param clazz
	 * @return
	 */
	public static String getJVMName(Class clazz) {
		if(clazz == null) {
			return null;
		}
		//For arrays, .getName() is fine.
		if(clazz.isArray()) {
			return clazz.getName().replace('.', '/');
		}
		if(clazz == boolean.class) {
			return "Z";
		} else if(clazz == byte.class) {
			return "B";
		} else if(clazz == short.class) {
			return "S";
		} else if(clazz == int.class) {
			return "I";
		} else if(clazz == long.class) {
			return "J";
		} else if(clazz == float.class) {
			return "F";
		} else if(clazz == double.class) {
			return "D";
		} else if(clazz == char.class) {
			return "C";
		} else {
			return "L" + clazz.getName().replace('.', '/') + ";";
		}
	}

	/**
	 * Returns the common name of a class, as it would be typed out in source code. In general, this returns the same as
	 * Class.getName, but for arrays, it outputs <code>[[Ljava.lang.Object;</code> which would be better written as
	 * <code>java.lang.Object[][]</code>.
	 *
	 * @param c
	 * @return
	 */
	public static String getCommonName(Class c) {
		if(!c.isArray()) {
			//This is fine for non arrays.
			return c.getName();
		}
		int arrayCount = c.getName().lastIndexOf("[") + 1;
		Class cc = c.getComponentType();
		while(cc.isArray()) {
			cc = cc.getComponentType();
		}
		return cc.getName() + StringUtils.stringMultiply(arrayCount, "[]");
	}

	/**
	 * Converts the binary name to the common name. For instance, for [Ljava/lang/Object;, java.lang.Object[] would be
	 * returned. The classes don't necessarily need to exist for this method to work.
	 *
	 * @param classname
	 * @return
	 */
	public static String getCommonNameFromJVMName(String classname) {
		int arrayCount = classname.lastIndexOf("[") + 1;
		classname = classname.substring(arrayCount);
		//ZBSIJDFC
		if("Z".equals(classname)) {
			classname = "boolean";
		} else if("B".equals(classname)) {
			classname = "byte";
		} else if("S".equals(classname)) {
			classname = "short";
		} else if("I".equals(classname)) {
			classname = "int";
		} else if("J".equals(classname)) {
			classname = "long";
		} else if("D".equals(classname)) {
			classname = "double";
		} else if("F".equals(classname)) {
			classname = "float";
		} else if("C".equals(classname)) {
			classname = "char";
		} else if("V".equals(classname)) {
			return "void"; //special case
		} else {
			classname = classname.substring(1, classname.length() - 1).replace('/', '.').replace('$', '.');
		}
		return classname + StringUtils.stringMultiply(arrayCount, "[]");
	}

	/**
	 * Returns a list of all classes that the specified class can be validly cast to. This includes all super classes,
	 * as well as all interfaces (and superclasses of those interfaces, etc) and java.lang.Object, as well as the class
	 * itself.
	 *
	 * @param c The class to search for.
	 * @return
	 */
	public static Set<Class<?>> getAllCastableClasses(Class<?> c) {
		Set<Class<?>> ret = new HashSet<>();
		getAllCastableClassesWithBlacklist(c, ret);
		return ret;
	}

	/**
	 * Private version of {@link #getAllCastableClasses(java.lang.Class)}
	 *
	 * @param c
	 * @param blacklist
	 * @return
	 */
	private static Set<Class<?>> getAllCastableClassesWithBlacklist(Class<?> c, Set<Class<?>> blacklist) {
		if(blacklist.contains(c)) {
			return blacklist;
		}
		while(true) {
			blacklist.add(c);
			Class<?> su = c.getSuperclass();
			if(su == null) {
				return blacklist;
			}
			blacklist.add(su);
			blacklist.addAll(getAllCastableClassesWithBlacklist(su, blacklist));
			for(Class<?> iface : c.getInterfaces()) {
				blacklist.add(iface);
				blacklist.addAll(getAllCastableClassesWithBlacklist(iface, blacklist));
			}
			c = su;
		}
	}

}
