/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.functions;

import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;

/**
 * Only should be used for test functions, or other typically unreleased or
 * temporary functions.
 * @author Layton
 */
public abstract class DummyFunction extends AbstractFunction{

	public ExceptionType[] thrown() {
		return ExceptionType.values();
	}

	public boolean isRestricted() {
		return false;
	}

	public Boolean runAsync() {
		return null;
	}

	public Integer[] numArgs() {
		return new Integer[]{Integer.MAX_VALUE};
	}

	public String docs() {
		return "mixed {...} A dummy function. This should not show up in the documentation.";
	}

	public CHVersion since() {
		return CHVersion.V0_0_0;
	}

	@Override
	public boolean appearInDocumentation() {
		return false;
	}

	public String getName() {
		return this.getClass().getSimpleName();
	}		
	
	
	
}
