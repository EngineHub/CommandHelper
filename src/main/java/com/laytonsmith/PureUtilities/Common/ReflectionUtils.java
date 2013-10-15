package com.laytonsmith.PureUtilities.Common;

import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Layton
 */
public class ReflectionUtils {

	private ReflectionUtils() {
	}

	public static class ReflectionException extends RuntimeException {

		public ReflectionException(Throwable cause) {
			super(cause);
		}

		/**
		 * Returns the underlying checked exception that was thrown by the
		 * reflective operation.
		 *
		 * @return
		 */
		@Override
		public Throwable getCause() {
			return super.getCause();
		}
	}

	/**
	 * Constructs a new instance of the specified object, assuming it has a no
	 * arg constructor. It will bypass access restrictions if possible.
	 *
	 * @param <T> The class type returned, specified by the class type requested
	 * @param clazz
	 * @return
	 * @throws com.laytonsmith.PureUtilities.ReflectionUtils.ReflectionException
	 */
	public static <T> T newInstance(Class<T> clazz) throws ReflectionException {
		return newInstance(clazz, new Class[]{}, new Object[]{});
	}

	/**
	 * Constructs a new instance of the specified object, using the constructor
	 * that matches the argument types, and passes in the arguments specified.
	 * It will bypass access restrictions if possible.
	 *
	 * @param <T> The class type returned, specified by the class type requested
	 * @param clazz
	 * @param argTypes
	 * @param args
	 * @return
	 * @throws com.laytonsmith.PureUtilities.ReflectionUtils.ReflectionException
	 */
	public static <T> T newInstance(Class<T> clazz, Class[] argTypes, Object[] args) throws ReflectionException {
		try {
			Constructor<T> c = clazz.getDeclaredConstructor(argTypes);
			c.setAccessible(true);
			return c.newInstance(args);
		} catch (InstantiationException ex) {
			throw new ReflectionException(ex);
		} catch (IllegalAccessException ex) {
			throw new ReflectionException(ex);
		} catch (IllegalArgumentException ex) {
			throw new ReflectionException(ex);
		} catch (InvocationTargetException ex) {
			throw new ReflectionException(ex);
		} catch (NoSuchMethodException ex) {
			throw new ReflectionException(ex);
		} catch (SecurityException ex) {
			throw new ReflectionException(ex);
		}
	}

	/**
	 * Gets the value from a static class member, disregarding the access
	 * restrictions.
	 *
	 * @param clazz
	 * @param variableName
	 * @return
	 */
	public static Object get(Class clazz, String variableName) throws ReflectionException {
		return get(clazz, null, variableName);
	}

	/**
	 * Gets a member from a class, disregarding the access restrictions. If
	 * accessing a static variable, the instance may be null. If variableName
	 * contains a dot, then it recursively digs down and grabs that value. So,
	 * given the following class definitions:
	 * <pre>
	 * class A { B bObj; }
	 * class B { C cObj; }
	 * class C { String obj; }
	 * </pre> Then if clazz were A.class, and variableName were "bObj.cObj.obj",
	 * then C's String obj would be returned.
	 *
	 * @param clazz
	 * @param instance
	 * @param variableName
	 * @return
	 */
	public static Object get(Class clazz, Object instance, String variableName) throws ReflectionException {
		try {
			if (variableName.contains(".")) {
				String split[] = variableName.split("\\.");
				Object myInstance = instance;
				Class myClazz = clazz;
				for (String var : split) {
					myInstance = get(myClazz, myInstance, var);
					myClazz = myInstance.getClass();
				}
				return myInstance;
			} else {
				Field f = clazz.getDeclaredField(variableName);
				f.setAccessible(true);
				return f.get(instance);
			}
		} catch (IllegalArgumentException ex) {
			throw new ReflectionException(ex);
		} catch (IllegalAccessException ex) {
			throw new ReflectionException(ex);
		} catch (NoSuchFieldException ex) {
			throw new ReflectionException(ex);
		} catch (SecurityException ex) {
			throw new ReflectionException(ex);
		}
	}

	/**
	 * Sets the value of a member in a static class, disregarding access
	 * restrictions and the final modifier.
	 *
	 * @param clazz
	 * @param variableName
	 * @param value
	 */
	public static void set(Class clazz, String variableName, Object value) throws ReflectionException {
		set(clazz, null, variableName, value);
	}

	/**
	 * Sets the value of a member in a specific instance of an object,
	 * disregarding access restrictions and the final modifier.
	 *
	 * @param clazz
	 * @param instance
	 * @param variableName
	 * @param value
	 */
	public static void set(Class clazz, Object instance, String variableName, Object value) throws ReflectionException {
		try {

			if (variableName.contains(".")) {
				String split[] = variableName.split("\\.");
				Object myInstance = instance;
				Class myClazz = clazz;
				int count = 0;
				for (String var : split) {
					if (count == split.length - 1) {
						//Only the last one needs to be set
						break;
					}
					myInstance = get(myClazz, myInstance, var);
					myClazz = myInstance.getClass();
					count++;
				}
				set(myClazz, myInstance, split[split.length - 1], value);
			} else {

				Field f = clazz.getDeclaredField(variableName);
				f.setAccessible(true);

				//This is the really evil stuff here, this is what removes the final modifier.
				Field modifiersField = Field.class.getDeclaredField("modifiers");
				modifiersField.setAccessible(true);
				modifiersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);

				f.set(instance, value);

			}
		} catch (IllegalArgumentException ex) {
			throw new ReflectionException(ex);
		} catch (IllegalAccessException ex) {
			throw new ReflectionException(ex);
		} catch (NoSuchFieldException ex) {
			throw new ReflectionException(ex);
		} catch (SecurityException ex) {
			throw new ReflectionException(ex);
		}
	}

	/**
	 * Invokes a no argument method, disregarding access restrictions, and
	 * returns the result.
	 *
	 * @param clazz
	 * @param instance
	 * @param methodName
	 * @return
	 */
	public static Object invokeMethod(Class clazz, Object instance, String methodName) throws ReflectionException {
		return invokeMethod(clazz, instance, methodName, new Class[]{}, new Object[]{});
	}

	/**
	 * Grabs the method from the instance object automatically. If multiple
	 * methods match the given name, the most appropriate one is selected based
	 * on the argument types. {@code instance} may not be null.
	 *
	 * @param instance
	 * @param methodName
	 * @throws com.laytonsmith.PureUtilities.ReflectionUtils.ReflectionException
	 */
	public static Object invokeMethod(Object instance, String methodName, Object... params) throws ReflectionException {
		Class c = instance.getClass();
		Class[] argTypes;
		{
			List<Class> cl = new ArrayList<Class>();
			for (Object o : params) {
				if (o != null) {
					cl.add(o.getClass());
				} else {
					//If it's null, we'll just have to assume Object
					cl.add(Object.class);
				}
			}
			argTypes = cl.toArray(new Class[cl.size()]);
		}
		while (c != null) {
			method:
			for (Method m : c.getDeclaredMethods()) {
				if (methodName.equals(m.getName())) {
					try {
						if (m.getParameterTypes().length == argTypes.length) {
							Class[] args = m.getParameterTypes();
							//Check to see that these arguments are subclasses
							//of the method's parameters. If so, this is our method,
							//otherwise, not.
							for (int i = 0; i < argTypes.length; i++) {
								if (!args[i].isAssignableFrom(argTypes[i])) {
									continue method;
								}
							}
							return m.invoke(instance, params);
						}
					} catch (IllegalAccessException ex) {
						throw new ReflectionException(ex);
					} catch (IllegalArgumentException ex) {
						throw new ReflectionException(ex);
					} catch (InvocationTargetException ex) {
						throw new ReflectionException(ex);
					}
				}
			}
			c = c.getSuperclass();
		}
		throw new ReflectionException(new NoSuchMethodException(methodName + " was not found in any of the searched classes."));
	}

	/**
	 * Grabs the method from the instance object automatically. {@code instance}
	 * may not be null.
	 *
	 * @param instance
	 * @param methodName
	 * @throws com.laytonsmith.PureUtilities.ReflectionUtils.ReflectionException
	 */
	public static Object invokeMethod(Object instance, String methodName) throws ReflectionException {
		Class c = instance.getClass();
		while (c != null) {
			for (Method m : c.getDeclaredMethods()) {
				if (methodName.equals(m.getName())) {
					try {
						return m.invoke(instance);
					} catch (IllegalAccessException ex) {
						throw new ReflectionException(ex);
					} catch (IllegalArgumentException ex) {
						throw new ReflectionException(ex);
					} catch (InvocationTargetException ex) {
						throw new ReflectionException(ex);
					}
				}
			}
			c = c.getSuperclass();
		}
		throw new ReflectionException(new NoSuchMethodException(methodName + " was not found in any of the searched classes."));
	}

	/**
	 * Invokes a method with the parameters specified, disregarding access
	 * restrictions, and returns the result.
	 *
	 * @param clazz
	 * @param instance
	 * @param methodName
	 * @param argTypes
	 * @param args
	 * @return
	 */
	public static Object invokeMethod(Class clazz, Object instance, String methodName, Class[] argTypes, Object[] args) throws ReflectionException {
		try {
			Method m = clazz.getDeclaredMethod(methodName, argTypes);
			m.setAccessible(true);
			return m.invoke(instance, args);
		} catch (InvocationTargetException ex) {
			throw new ReflectionException(ex);
		} catch (NoSuchMethodException ex) {
			throw new ReflectionException(ex);
		} catch (IllegalArgumentException ex) {
			throw new ReflectionException(ex);
		} catch (IllegalAccessException ex) {
			throw new ReflectionException(ex);
		} catch (SecurityException ex) {
			throw new ReflectionException(ex);
		}
	}

	/**
	 * Shorthand for {@link #PrintObjectTrace(instance, instanceOnly, null)}
	 */
	public static void PrintObjectTrace(Object instance, boolean instanceOnly) {
		PrintObjectTrace(instance, instanceOnly, null);
	}

	/**
	 * Meant mostly as a debug tool, takes an object and prints out the object's
	 * non-static field information at this current point in time, to the
	 * specified PrintStream. This method will not throw any SecurityExceptions
	 * if a value cannot be reflectively accessed, but instead will print an
	 * error message for that single value.
	 *
	 * @param instance The object to explore. If this is null, "The object is
	 * null" is printed, and the method exits.
	 * @param instanceOnly If true, only the object's class members will be
	 * printed, otherwise, the method will recurse up the object's inheritance
	 * hierarchy, and prints everything.
	 * @param output The print stream to output to, or System.out if null.
	 */
	public static void PrintObjectTrace(Object instance, boolean instanceOnly, PrintStream output) {
		if (output == null) {
			output = System.out;
		}
		if (instance == null) {
			output.println("The object is null");
			return;
		}

		Class iClass = instance.getClass();
		do {
			for (Field f : iClass.getDeclaredFields()) {
				if ((f.getModifiers() & Modifier.STATIC) > 0) {
					continue;
				}
				String value = "null";
				try {
					f.setAccessible(true);
					Object o = ReflectionUtils.get(iClass, instance, f.getName());
					if (o != null) {
						value = o.toString();
					}
				} catch (SecurityException e) {
					value = "Could not access value due to a SecurityException";
				}
				output.println("(" + f.getType() + ") " + f.getName() + ": " + value);
			}
		} while (!instanceOnly && (iClass = iClass.getSuperclass()) != null);
	}
}
