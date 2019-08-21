package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.core.natives.interfaces.Sizeable;
import com.laytonsmith.core.natives.interfaces.ValueType;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 *
 */
@typeof("ms.lang.mutable_primitive")
public class CMutablePrimitive extends CArray implements Sizeable {

	@SuppressWarnings("FieldNameHidesFieldInSuperclass")
	public static final CClassType TYPE = CClassType.get(CMutablePrimitive.class);

	private Mixed value = CNull.NULL;

	public CMutablePrimitive(Target t) {
		this(null, t);
	}

	public CMutablePrimitive(Mixed value, Target t) {
		super(t, 0);
		set(value, t);
	}

	@Override
	public boolean isDynamic() {
		return true;
	}

	/**
	 * Sets the value of the underlying primitive. Only primitives (and null) may be stored.
	 *
	 * @param value
	 * @param t
	 */
	public void set(Mixed value, Target t) {
		if(!value.isInstanceOf(ValueType.TYPE)) {
			throw new CREFormatException("mutable_primitives can only store primitive values.", t);
		}
		this.value = value;
	}

	@Override
	public void set(Mixed index, Mixed c, Target t) {
		throw new CRECastException("mutable_primitives cannot have values set in them", t);
	}

	/**
	 * Sets the value as if
	 * {@link #set(Mixed, com.laytonsmith.core.constructs.Target)} were called, then
	 * returns a reference to this object.
	 *
	 * @param value
	 * @param t
	 * @return
	 */
	public CMutablePrimitive setAndReturn(Mixed value, Target t) {
		set(value, t);
		return this;
	}

	public Mixed get() {
		return value;
	}

	@Override
	public Mixed get(Mixed index, Target t) {
		return value;
	}

	@Override
	public String val() {
		return value.val();
	}

	@Override
	public String toString() {
		return value.toString();
	}

	@Override
	public CMutablePrimitive clone() {
		return this;
	}

	@Override
	protected String getQuote() {
		return new CString(value.val(), Target.UNKNOWN).getQuote();
	}

	@Override
	public long size() {
		if(value.isInstanceOf(Sizeable.TYPE)) {
			return ArgumentValidation.getObject(value, Target.UNKNOWN, Sizeable.class).size();
		} else {
			return 0;
		}
	}

	@Override
	public boolean canBeAssociative() {
		return false;
	}

	@Override
	public List<Mixed> asList() {
		return getArray();
	}

	@Override
	public void clear() {
		value = CNull.NULL;
	}

	@Override
	public CArray createNew(Target t) {
		return new CMutablePrimitive(value, t);
	}

	@Override
	protected List<Mixed> getArray() {
		List<Mixed> array = new ArrayList<>();
		array.add(value);
		return array;
	}

	@Override
	protected String getString(Stack<CArray> arrays, Target t) {
		return value.val();
	}

	@Override
	public void push(Mixed c, Integer i, Target t) {
		set(c, t);
	}

	@Override
	public String docs() {
		return "A mutible primitive is a special data type that allows you to store primitives by reference, instead of value.";
	}

	@Override
	public Version since() {
		return MSVersion.V3_3_1;
	}

	@Override
	public CClassType[] getSuperclasses() {
		return new CClassType[]{CArray.TYPE};
	}

	@Override
	public CClassType[] getInterfaces() {
		return new CClassType[]{Sizeable.TYPE};
	}

}
