package com.laytonsmith.core.constructs;

/**
 * Represents a MethodScript boolean.
 */
public abstract class CBoolean extends Construct implements Cloneable {

	protected CBoolean(String value, Target t) {
		super(value, ConstructType.BOOLEAN, t);
	}

	/**
	 * return b ? CTrue.TRUE : CFalse.FALSE;
	 * @param b The boolean value
	 * @return 
	 */
	public static CBoolean get(boolean b) {
		return b ? CTrue.TRUE : CFalse.FALSE;
	}

	public static CBoolean get(String value) {
		try {
			return get(Long.parseLong(value) != 0);
		} catch (NumberFormatException e) {
			try {
				return get(Double.parseDouble(value) != 0);
			} catch (NumberFormatException f) {
				return get(Boolean.parseBoolean(value));
			}
		}
	}

	/**
	 * Constructs a new CBoolean object. Generally speaking, this should
	 * only be used when creating booleans that are literally created
	 * by user code, all internal code should simply use {@link #get(boolean)}.
	 * This method DOES check the target however, and if the target is
	 * {@link Target#UNKNOWN}, {@link #get(boolean)} is returned anyways.
	 * @param b
	 * @param t
	 * @return 
	 */
	public static CBoolean GenerateCBoolean(boolean b, Target t) {
		if (t == Target.UNKNOWN) {
			return get(b);
		} else {
			return b ? CTrue.GenerateCTrue(t) : CFalse.GenerateCFalse(t);
		}
	}

	/**
	 * Returns the primitive boolean value of this CBoolean.
	 * @return 
	 */
	public abstract boolean getBoolean();

	/**
	 * Negates this CBoolean.
	 * @return 
	 */
	public abstract CBoolean not();

	@Override
	@SuppressWarnings("CloneDoesntCallSuperClone")
	public CBoolean clone() throws CloneNotSupportedException {
		return this;
	}

	@Override
	public boolean isDynamic() {
		return false;
	}
}