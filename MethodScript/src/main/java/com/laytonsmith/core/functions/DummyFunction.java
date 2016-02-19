
package com.laytonsmith.core.functions;

import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;

/**
 * Only should be used for test functions, or other typically unreleased or
 * temporary functions.
 */
public abstract class DummyFunction extends AbstractFunction{

	@Override
	public Class<? extends CREThrowable>[] thrown() {
		return new Class[]{CREThrowable.class};
	}

	@Override
	public boolean isRestricted() {
		return false;
	}

	@Override
	public Boolean runAsync() {
		return null;
	}

	@Override
	public Integer[] numArgs() {
		return new Integer[]{Integer.MAX_VALUE};
	}

	@Override
	public String docs() {
		return "mixed {...} A dummy function. This should not show up in the documentation.";
	}

	@Override
	public CHVersion since() {
		return CHVersion.V0_0_0;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}		
	
}
