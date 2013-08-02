package com.laytonsmith.PureUtilities.ClassMirror;

import java.io.Serializable;

/**
 * A package mirror provides information about the package a class is in.
 */
public class PackageMirror implements Serializable {
	private String name;
	
	public PackageMirror(String name){
		this.name = name;
	}
	
	public String getName(){
		return name;
	}
}
