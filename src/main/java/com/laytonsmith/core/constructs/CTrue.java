package com.laytonsmith.core.constructs;

import com.laytonsmith.annotations.typeof;

/**
 * Represents a MethodScript true value.
 */
@typeof("boolean")
public final class CTrue extends CBoolean {

	public static final long serialVersionUID = 1L;

	/**
	 * True values do not normally need to be duplicated, since they are
	 * immutable, and for values that have an unknown code target, are
	 * always equal. In cases where a true is generated from inside Java,
	 * this value should be returned, instead of generating a new one.
	 */
	public static final CTrue TRUE = new CTrue(Target.UNKNOWN);

	/**
	 * Private constructor to force usage of {@link #GenerateCTrue(com.laytonsmith.core.constructs.Target)}, which can
	 * return existing objects.
	 * @param t 
	 */
	private CTrue(Target t) {
		super("true", t);
	}

	/**
	 * Constructs a new CTrue object. Generally speaking, this should
	 * only be used when creating true values that are literally created
	 * by user code, all internal code should simply use {@link #TRUE}.
	 * This method DOES check the target however, and if the target is
	 * {@link Target#UNKNOWN}, {@link #TRUE} is returned anyways.
	 * @param t
	 * @return 
	 */
	public static CTrue GenerateCTrue(Target t) {
		return (t == Target.UNKNOWN) ? TRUE : new CTrue(t);
	}

	@Override
	public boolean getBoolean() {
		return true;
	}

	@Override
	public CFalse not() {
		return CFalse.GenerateCFalse(getTarget());
	}
}