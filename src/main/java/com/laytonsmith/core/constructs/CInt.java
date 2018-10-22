package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;

/**
 *
 *
 */
@typeof("ms.lang.int")
public class CInt extends CNumber implements Cloneable {

	@SuppressWarnings("FieldNameHidesFieldInSuperclass")
	public static final CClassType TYPE = CClassType.get("ms.lang.int");

	public static final long serialVersionUID = 1L;
	final long val;

	/**
	 * The size of the CINT_POOL. This number MUST be a positive even number. If this value is reduced in the future,
	 * a thorough examination of all usages of {@link #unsafeGetFromPool(int)} must be done, to ensure that
	 * the change will not break any of those usages. There is also a guarantee that this value will never
	 * be reduced below 2, unless the pool system is removed entirely. (Therefore making it always valid to call
	 * {@link #unsafeGetFromPool(int)} with a 0 or 1.)
	 */
	public static final int CINT_POOL_SIZE = 256;
	/**
	 * The upper value of the CINT_POOL size
	 */
	public static final int CINT_POOL_HIGH = (CINT_POOL_SIZE / 2);
	/**
	 * The lower value of the CINT_POOL size
	 */
	public static final int CINT_POOL_LOW = CINT_POOL_HIGH - CINT_POOL_SIZE + 1;
	// Initialized at the bottom, this is used to create an integer pool,
	// in the same way as Java's integer pool. In general, this should be
	// used when possible. There is a method that abstract away the need
	// for knowing the size of the pool, but that value is available here
	// as well.
	private static final CInt[] CINT_POOL = new CInt[CINT_POOL_SIZE];

	/**
	 * Returns a CInt from the pool, if the value is between {@code CINT_POOL_LOW} and {@code CINT_POOL_HIGH}
	 * (inclusive). To save memory, this should be used in all cases. A new instance of the CInt will be
	 * returned if it is not within the bounds, so this method is always safe to call.
	 * @param i The underlying integer value
	 * @param t The code target. This is ignored if the value is returned from the pool, but is used if the
	 * CInt object was newly constructed.
	 * @return Either an existing CInt from the pool, or a new instance of a CInt
	 */
	public static CInt getFromPool(long i, Target t) {
		if(i >= CINT_POOL_LOW && i <= CINT_POOL_HIGH) {
			return unsafeGetFromPool((int)i);
		} else {
			return new CInt(i, t);
		}
	}

	/**
	 * Parses the string as a long, and then calls {@link #getFromPool(long, com.laytonsmith.core.constructs.Target)}.
	 * @param i The underlying integer value
	 * @param t The code target. This is ignored if the value is returned from the pool, but is used if the
	 * CInt object was newly constructed.
	 * @throws CREFormatException If the value is not integral
	 * @return Either an existing CInt from the pool, or a new instance of a CInt
	 */
	public static CInt getFromPool(String i, Target t) {
		try {
			return getFromPool(Long.parseLong(i), t);
		} catch (NumberFormatException e) {
			throw new CREFormatException("Could not parse " + i + " as an integer", t);
		}
	}

	/**
	 * Returns a CInt from the pool, with no bounds checking. This should only be used by code within the core system,
	 * as the underlying method is subject to change without notice, and any breakages that occur will not be fixed.
	 * As such, this method is marked as deprecated, to highlight this fact. It *is* guaranteed that unless this method
	 * is removed entirely, values 0 and 1 will always work.
	 *
	 * For internal use: There is no bounds checking done here, so ensure that the value is definitely within the bounds
	 * beforehand. Particularly, this method is useful if the value is being hardcoded, or there is otherwise some
	 * guarantee that the value will be within the bounds. If you can guarantee that, then this method will save a
	 * few cycles. However, if you have to do bounds checking in the code to find out, then it is better to just use
	 * {@link #getFromPool}, because then the work is centralized at least, and will require less work if the pool
	 * size is changed later, as that will require a manual survey of the usages of this method.
	 * @param i
	 * @return
	 * @deprecated Use {@link #getFromPool(int, com.laytonsmith.core.constructs.Target)} instead of this method,
	 * except for internal code.
	 */
	@Deprecated
	public static CInt unsafeGetFromPool(int i) {
		return CINT_POOL[i + CINT_POOL_HIGH];
	}

	/**
	 * To take advantage of the pool system, this constructor will be removed in future versions.
	 * Please remove references to this, and use
	 * {@link #getFromPool(java.lang.String, com.laytonsmith.core.constructs.Target)} instead.
	 * @param value
	 * @param t
	 * @deprecated
	 */
	@Deprecated
	private CInt(String value, Target t) {
		super(value, Construct.ConstructType.INT, t);
		try {
			val = Long.parseLong(value);
		} catch (NumberFormatException e) {
			throw new CREFormatException("Could not parse " + value + " as an integer", t);
		}
	}

	/**
	 * To take advantage of the pool system, this constructor will be removed in future versions.
	 * Please remove references to this, and use
	 * {@link #getFromPool(long, com.laytonsmith.core.constructs.Target)} instead.
	 * @param value
	 * @param t
	 * @deprecated
	 */
	@Deprecated
	private CInt(long value, Target t) {
		super(Long.toString(value), Construct.ConstructType.INT, t);
		val = value;
	}

	public long getInt() {
		return val;
	}

	@Override
	@SuppressWarnings("CloneDoesntCallSuperClone")
	public CInt clone() throws CloneNotSupportedException {
		return this;
	}

	@Override
	public boolean isDynamic() {
		return false;
	}

	@Override
	public String docs() {
		return "An integer is a discreet numerical value. All positive and negative counting numbers, as well as 0.";
	}

	@Override
	public Version since() {
		return CHVersion.V3_0_1;
	}

	@Override
	public CClassType[] getSuperclasses() {
		return new CClassType[]{CNumber.TYPE};
	}

	@Override
	public CClassType[] getInterfaces() {
		return new CClassType[]{};
	}

	static {
		// Initialize the pool
		for(int i = 0; i < CINT_POOL_SIZE; ++i) {
			CINT_POOL[i] = new CInt(i, Target.UNKNOWN);
		}
	}

}
