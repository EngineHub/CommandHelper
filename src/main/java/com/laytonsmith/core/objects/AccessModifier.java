/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.objects;

import com.laytonsmith.annotations.MEnum;

/**
 * An AccessModifier describes the state of visibility of an element or class.
 */
@MEnum("ms.lang.AccessModifier")
public enum AccessModifier {
	/**
	 * A public class is one that can be accessed from any other class. Public methods, fields, and classes are used
	 * to determine the project's version number, and are considered to be the public API of a project, and should be
	 * treated with care, as well as properly documented.
	 */
	PUBLIC,
	/**
	 * An internal class is one that can be accessed from within just this project.
	 */
	INTERNAL,
	/**
	 * A package level class is one that can be accessed from other classes that are within the same package
	 */
	PACKAGE,
	/**
	 * Protected level item is one that can be accessed only from subclasses.
	 */
	PROTECTED,
	/**
	 * A private class is one that can only be accessed from other classes within the same containing class. This is not
	 * useable in top level classes, as it wouldn't make sense otherwise.
	 */
	PRIVATE,
	/**
	 * A default modifier implies that the modifier was left off. This causes the behavior
	 * of one of the other modifiers, but the behavior depends on the context in which
	 * this is used.
	 *
	 * If the method
	 * is defined in a parent class or interface, then the method will adopt the overridden
	 * method's access modifier. For classes, it will be whatever the parent specifically set
	 * it as, and for methods defined in an interface, they will be public. (They are necessarily
	 * public anyways, because all methods in an interface are public.) If the defined method
	 * is not overriding a method in a parent class, then the method is considered internal.
	 * Fields that have the default visibility default to internal.
	 */
	DEFAULT;
}
