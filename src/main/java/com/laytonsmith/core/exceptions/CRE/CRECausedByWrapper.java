package com.laytonsmith.core.exceptions.CRE;

import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.Target;

/**
 * Wraps a CArray that represents a causedBy exception.
 */
public class CRECausedByWrapper extends CREThrowable {
	private final CArray exception;
	
	public CRECausedByWrapper(CArray exception){
		super(null, Target.UNKNOWN);
		this.exception = exception.clone();
	}
	
	public CArray getException(){
		return exception;
	}
}
