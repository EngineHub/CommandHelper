package com.laytonsmith.core.natives.annotations;

/**
 * The types of elements that an annotation can be tagged to.
 */
public enum ElementType {

	/**
	 * Class, interface (including annotation type), or enum declaration
	 */
	TYPE,
	/**
	 * Field declaration (includes enum constants)
	 */
	FIELD,
	/**
	 * Method declaration
	 */
	METHOD,
	/**
	 * Parameter declaration
	 */
	PARAMETER,
	/**
	 * Constructor declaration
	 */
	CONSTRUCTOR,
	/**
	 * Local variable declaration
	 */
	LOCAL_VARIABLE,
	/**
	 * Annotation type declaration
	 */
	ANNOTATION_TYPE,
	/**
	 * Package declaration
	 */
	PACKAGE,
	/**
	 * Any assignable parameter. This is essentially a shortcut for FIELD, PARAMETER, and LOCAL_VARIABLE.
	 */
	ASSIGNABLE,
}
