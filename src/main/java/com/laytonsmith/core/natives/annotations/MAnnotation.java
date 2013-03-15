package com.laytonsmith.core.natives.annotations;

import com.laytonsmith.annotations.typename;
import com.laytonsmith.core.Documentation;

/**
 * The superclass for all annotations.
 *
 * @author lsmith
 */
public abstract class MAnnotation implements Documentation {

	/**
	 * Returns the name of this annotation, which should be the typename
	 * that this class is tagged with, hence this method is final.
	 * @return 
	 */
	public final String getName() {
		return this.getClass().getAnnotation(typename.class).value();
	}

	/**
	 * Returns a list of meta annotations. By default, this returns an empty
	 * array, but it can be overridden.
	 * @return 
	 */
	public MAnnotation[] getMetaAnnotations(){
		return new MAnnotation[]{};
	}
	
}
