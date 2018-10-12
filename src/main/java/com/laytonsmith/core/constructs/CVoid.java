package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.CHVersion;

/**
 *
 */
@typeof("void")
public final class CVoid extends Construct implements Cloneable {

	@SuppressWarnings("FieldNameHidesFieldInSuperclass")
	public static final CClassType TYPE = CClassType.VOID;

	/**
	 * Void values do not normally need to be duplicated, since they are immutable, and for values that have an unknown
	 * code target, are always equal. In cases where a void is generated from inside Java, this value should be
	 * returned, instead of generating a new one.
	 */
	public static final CVoid VOID = new CVoid(Target.UNKNOWN);

	/**
	 * Private constructor, because there can only be one
	 *
	 * @param t
	 */
	private CVoid(Target t) {
		super("", ConstructType.VOID, t);
	}

	@Override
	@SuppressWarnings("CloneDoesntCallSuperClone")
	public CVoid clone() throws CloneNotSupportedException {
		return this;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof CVoid;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		return hash;
	}

	@Override
	public boolean isDynamic() {
		return false;
	}

	@Override
	public String docs() {
		return "void isn't a datatype per se, but represents a lack of a datatype. Void values can't be assigned to variables,"
				+ " or otherwise used.";
	}

	@Override
	public Version since() {
		return CHVersion.V3_0_1;
	}

	@Override
	public CClassType[] getSuperclasses() {
		throw new RuntimeException("Cannot call getSuperclasses on void");
	}

	@Override
	public CClassType[] getInterfaces() {
		throw new RuntimeException("Cannot call getInterfaces on void");
	}

}
