/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.PureUtilities;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

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
	 * accessing a static variable, the instance may be null.
	 *
	 * @param clazz
	 * @param instance
	 * @param variableName
	 * @return
	 */
	public static Object get(Class clazz, Object instance, String variableName) throws ReflectionException {
		try {
			Field f = clazz.getDeclaredField(variableName);
			f.setAccessible(true);
			return f.get(instance);
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
	 * Sets the value of a member in a static class, disregarding access restrictions
	 * and the final modifier.
	 * @param clazz
	 * @param variableName
	 * @param value 
	 */
	public static void set(Class clazz, String variableName, Object value) throws ReflectionException{
		set(clazz, null, variableName, value);
	}
	
	/**
	 * Sets the value of a member in a specific instance of an object, disregarding access
	 * restrictions and the final modifier.
	 * @param clazz
	 * @param instance
	 * @param variableName
	 * @param value 
	 */
	public static void set(Class clazz, Object instance, String variableName, Object value) throws ReflectionException {
		try {
			Field f = clazz.getDeclaredField(variableName);
			f.setAccessible(true);
			
			//This is the really evil stuff here, this is what removes the final modifier.
			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);
			
			f.set(instance, value);
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
	 * Invokes a no argument method, disregarding access restrictions, and returns the
	 * result.
	 * @param clazz
	 * @param instance
	 * @param methodName
	 * @return 
	 */
	public static Object invokeMethod(Class clazz, Object instance, String methodName) throws ReflectionException {
		return invokeMethod(clazz, instance, methodName, new Class[]{}, new Object[]{});
	}

	/**
	 * Invokes a method with the parameters specified, disregarding access restrictions,
	 * and returns the result.
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
		} catch(InvocationTargetException ex){
			throw new ReflectionException(ex);
		} catch(NoSuchMethodException ex){
			throw new ReflectionException(ex);
		} catch (IllegalArgumentException ex) {
			throw new ReflectionException(ex);
		} catch (IllegalAccessException ex) {
			throw new ReflectionException(ex);
		} catch (SecurityException ex) {
			throw new ReflectionException(ex);
		}
	}
}
