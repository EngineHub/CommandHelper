package com.laytonsmith.PureUtilities.ClassLoading.ClassMirror;

import java.io.Serializable;

/**
 * A package mirror provides information about the package a class is in.
 */
public class PackageMirror implements Serializable {
	private static final long serialVersionUID = 1L;
	private String name;
	
	public PackageMirror(String name){
		this.name = name;
	}
	
	public String getName(){
		return name;
	}
}
