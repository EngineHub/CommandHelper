/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.objects;

import com.laytonsmith.annotations.MEnum;

/**
 * An AccessModifier describes the state of visibility of an item.
 */
@MEnum("ms.lang.AccessModifier")
public enum AccessModifier {
	/**
	 * A public class is one that can be accessed from any other class.
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
	PRIVATE;
}
