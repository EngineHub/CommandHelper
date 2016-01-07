package com.laytonsmith.core.exceptions.CRE;

import com.laytonsmith.core.constructs.CArray;

/**
 * This is a wrapper around a CArray object that represents an exception.
 */
public class CRECausedByWrapper extends Throwable {
	private final CArray exception;
	public CRECausedByWrapper(CArray exception){
		this.exception = exception.clone();
	}
	
	public CArray getException(){
		return exception;
	}
}
