package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.CHVersion;

/**
 * Represents a MethodScript boolean.
 */
@typeof("boolean")
public final class CBoolean extends CPrimitive implements Cloneable{

	public static final long serialVersionUID = 1L;

	/**
	 * True values do not normally need to be duplicated, since they are
	 * immutable, and for values that have an unknown code target, are
	 * always equal. In cases where a true is generated from inside Java,
	 * this value should be returned, instead of generating a new one.
	 */
	public static final CBoolean TRUE = new CBoolean(true, Target.UNKNOWN);

	/**
	 * False values do not normally need to be duplicated, since they are
	 * immutable, and for values that have an unknown code target, are
	 * always equal. In cases where a false is generated from inside Java,
	 * this value should be returned, instead of generating a new one.
	 */
	public static final CBoolean FALSE = new CBoolean(false, Target.UNKNOWN);

	private final boolean val;

	/**
	 * Private constructor to force usage of {@link #GenerateCBoolean(boolean, com.laytonsmith.core.constructs.Target)}, which can
	 * return existing objects.
	 * @param t
	 */
	private CBoolean(boolean value, Target t) {
		super(Boolean.toString(value), ConstructType.BOOLEAN, t);
		val = value;
    }

	/**
	 * return b ? CBoolean.TRUE : CBoolean.FALSE;
	 * @param b The boolean value
	 * @return
	 */
	public static CBoolean get(boolean b) {
		return b ? CBoolean.TRUE : CBoolean.FALSE;
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
		return (t == Target.UNKNOWN) ? get(b) : new CBoolean(b, t);
	}

	/**
	 * Returns the primitive boolean value of this CBoolean.
	 * @return
	 */
	public boolean getBoolean() {
		return val;
	}

	/**
	 * Negates this CBoolean.
	 * @return
	 */
	public CBoolean not() {
		return GenerateCBoolean(!val, getTarget());
	}

	@Override
	@SuppressWarnings("CloneDoesntCallSuperClone")
	public CBoolean clone() throws CloneNotSupportedException {
		return this;
	}

	@Override
	public boolean isDynamic() {
		return false;
	}

	@Override
	public String docs() {
		return "A boolean represents a true or false value.";
	}

	@Override
	public Version since() {
		return CHVersion.V3_0_1;
	}
}
