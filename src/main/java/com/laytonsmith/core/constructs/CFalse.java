package com.laytonsmith.core.constructs;

import com.laytonsmith.annotations.typeof;

/**
 * Represents a MethodScript false value.
 */
@typeof("boolean")
public final class CFalse extends CBoolean {

	public static final long serialVersionUID = 1L;

	/**
	 * False values do not normally need to be duplicated, since they are
	 * immutable, and for values that have an unknown code target, are
	 * always equal. In cases where a false is generated from inside Java,
	 * this value should be returned, instead of generating a new one.
	 */
	public static final CFalse FALSE = new CFalse(Target.UNKNOWN);

	/**
	 * Private constructor to force usage of {@link #GenerateCFalse(com.laytonsmith.core.constructs.Target)}, which can
	 * return existing objects.
	 * @param t 
	 */
	private CFalse(Target t) {
		super("false", t);
	}

	/**
	 * Constructs a new CFalse object. Generally speaking, this should
	 * only be used when creating false values that are literally created
	 * by user code, all internal code should simply use {@link #FALSE}.
	 * This method DOES check the target however, and if the target is
	 * {@link Target#UNKNOWN}, {@link #FALSE} is returned anyways.
	 * @param t
	 * @return 
	 */
	public static CFalse GenerateCFalse(Target t) {
		return (t == Target.UNKNOWN) ? FALSE : new CFalse(t);
	}

	@Override
	public boolean getBoolean() {
		return false;
	}

	@Override
	public CTrue not() {
		return CTrue.GenerateCTrue(getTarget());
	}
}