
package com.laytonsmith.PureUtilities.ClassMirror;

/**
 * A class reference mirror is a wrapper around a simple class name reference.
 * It cannot directly get more information about a class without actually loading it,
 * so minimal information is available directly, though there is a method for
 * loading the actual class referenced, at which point more information could be
 * retrieved.
 */
public class ClassReferenceMirror {
	private String name;
	public ClassReferenceMirror(String name){
		this.name = name;
	}
	
	
}
