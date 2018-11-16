package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import java.math.BigDecimal;

/**
 *
 * @author cailin
 */
@typeof("ms.lang.decimal")
public class CDecimal extends CPrimitive implements Cloneable {

	@SuppressWarnings("FieldNameHidesFieldInSuperclass")
	public static final CClassType TYPE = CClassType.get("ms.lang.decimal");

	private final BigDecimal val;

	public CDecimal(String value, Target t) {
		super(value, ConstructType.INT, t);
		try {
			val = new BigDecimal(value);
		} catch (NumberFormatException e) {
			throw new CREFormatException("Could not create decimal from value \"" + value + "\"", t);
		}
	}

	public CDecimal(double value, Target t) {
		super(Double.toString(value), ConstructType.DOUBLE, t);
		val = new BigDecimal(value);
	}

	@Override
	public boolean isDynamic() {
		return false;
	}

	@Override
	public CDecimal clone() throws CloneNotSupportedException {
		return this;
	}

	@Override
	public String docs() {
		return "A decimal is a arbitrary-precision signed decimal numbers of unlimited size (bounded only by the"
				+ " system's capacity). In general, they are transparently handled by the math system in MethodScript,"
				+ " however they cannot be used in place of doubles in general in functions. To fail fast, and to "
				+ " prevent arbitrary behavior, it has been decided that the decimal value cannot be converted"
				+ " automatically to a double, and instead you must manually cast it via double(@decimal). This will"
				+ " fail in cases where the value cannot be represented in a double, but in places that readily"
				+ " accept a decimal value, they will always work appropriately. A decimal can be created from another"
				+ " value using the decimal() function, or, if directly in code, defined with a trailing m character,"
				+ " such as 123456.1234m. In general, it is not useful to store integral values that would otherwise"
				+ " fit in an int datatype, as operations with them will be less efficient, but having an otherwise"
				+ " double value does make sense for where precision needs to be exact, and java's floating point math"
				+ " and rounding does not suffice.";
	}

	@Override
	public Version since() {
		return MSVersion.V3_3_2;
	}

	@Override
	public CClassType[] getSuperclasses() {
		return new CClassType[]{CPrimitive.TYPE};
	}

	@Override
	public CClassType[] getInterfaces() {
		return new CClassType[]{};
	}

}
