package com.laytonsmith.PureUtilities.ClassMirror;

/**
 * A package mirror provides information about the package a class is in.
 */
public class PackageMirror {
	private String name;
	
	public PackageMirror(String name){
		this.name = name;
	}
	
	public String getName(){
		return name;
	}
}
