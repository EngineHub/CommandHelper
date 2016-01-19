package com.laytonsmith.core.exceptions.CRE;

import com.laytonsmith.core.constructs.CArray;

/**
 * Wraps a CArray that represents a causedBy exception.
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
