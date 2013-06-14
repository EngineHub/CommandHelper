package com.laytonsmith.core.functions;

import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.arguments.Argument;
import com.laytonsmith.core.arguments.ArgumentBuilder;
import com.laytonsmith.core.arguments.Generic;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 * Only should be used for test functions, or other typically unreleased or
 * temporary functions.
 *
 */
public abstract class DummyFunction extends AbstractFunction {

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
		return "A dummy function. This should not show up in the documentation.";
	}

	public Argument returnType() {
		return new Argument("...", Mixed.class);
	}

	public ArgumentBuilder arguments() {
		return ArgumentBuilder.Build(
				new Argument("...", CArray.class, "args").setGenerics(Generic.ANY).setVarargs()
				);
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
