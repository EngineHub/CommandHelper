package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Sizable;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 *
 */
@typeof("mutable_primitive")
public class CMutablePrimitive extends CArray implements Sizable {

	private Construct value = CNull.NULL;

	public CMutablePrimitive(Target t){
		this(null, t);
	}

	public CMutablePrimitive(Construct value, Target t) {
		super(t, 0);
		set(value, t);
	}

	@Override
	public boolean isDynamic() {
		return true;
	}

	/**
	 * Sets the value of the underlying primitive. Only primitives (and null) may
	 * be stored.
	 * @param value
	 * @param t
	 */
	public void set(Construct value, Target t){
		if(value instanceof CArray){
			throw ConfigRuntimeException.BuildException("mutable_primitives can only store primitive values.", CREFormatException.class, t);
		}
		this.value = value;
	}

	/**
	 * Sets the value as if {@link #set(com.laytonsmith.core.constructs.Construct, com.laytonsmith.core.constructs.Target)} were called,
	 * then returns a reference to this object.
	 * @param value
	 * @param t
	 * @return
	 */
	public CMutablePrimitive setAndReturn(Construct value, Target t){
		set(value, t);
		return this;
	}

	public Construct get(){
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
		return value.getQuote();
	}

	@Override
	public long size() {
		if(value instanceof CString){
			return ((CString)value).size();
		} else {
			return 0;
		}
	}

	@Override
	public Construct get(Construct index, Target t) {
		return value;
	}

	@Override
	public boolean canBeAssociative() {
		return false;
	}

	@Override
	public List<Construct> asList() {
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
	protected List<Construct> getArray() {
		List<Construct> array = new ArrayList<>();
		array.add(value);
		return array;
	}

	@Override
	protected String getString(Stack<CArray> arrays, Target t) {
		return value.val();
	}

	@Override
	public void push(Construct c, Integer i, Target t) {
		set(c, t);
	}

	@Override
	public void set(Construct index, Construct c, Target t) {
		throw ConfigRuntimeException.BuildException("mutable_primitives cannot have values set in them", CRECastException.class, t);
	}

	@Override
	public String docs() {
		return "A mutible primitive is a special data type that allows you to store primitives by reference, instead of value.";
	}

	@Override
	public Version since() {
		return CHVersion.V3_3_1;
	}

	

}
