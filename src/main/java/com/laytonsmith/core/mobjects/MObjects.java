package com.laytonsmith.core.mobjects;

import com.laytonsmith.annotations.mobject;

/**
 * This is a utility class that contains methods for manipulating MObjects.
 */
public class MObjects {

	private MObjects() {
	}
	
	/**
	 * Returns the name of the object, as provided by the annotation.
	 * @param object
	 * @return 
	 */
	public static String getObjectName(MObject object){
		return object.getClass().getAnnotation(mobject.class).value();
	}
}
