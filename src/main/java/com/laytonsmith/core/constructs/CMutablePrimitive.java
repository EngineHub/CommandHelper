package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.constructs.generics.GenericParameters;
import com.laytonsmith.core.constructs.generics.GenericTypeParameters;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.core.natives.interfaces.Sizeable;
import com.laytonsmith.core.natives.interfaces.ValueType;
import com.laytonsmith.core.objects.ObjectModifier;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 *
 */
@typeof("ms.lang.mutable_primitive")
public final class CMutablePrimitive extends CArray implements Sizeable {

	@SuppressWarnings("FieldNameHidesFieldInSuperclass")
	public static final CClassType TYPE = CClassType.get(CMutablePrimitive.class)
			.withSuperParameters(GenericTypeParameters.nativeBuilder(CArray.TYPE).addParameter(Auto.LHSTYPE))
			.done();

	private Mixed value = CNull.NULL;

	public CMutablePrimitive(Target t, Environment env) {
		this(null, t, env);
	}

	public CMutablePrimitive(Mixed value, Target t, Environment env) {
		super(t, 0, GenericParameters.emptyBuilder(CArray.TYPE)
				.addNativeParameter(Auto.TYPE, null)
				.buildNative(), env);
		set(value, t, env);
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
	public void set(Mixed value, Target t, Environment env) {
		if(!value.isInstanceOf(ValueType.TYPE, null, env)) {
			throw new CREFormatException("mutable_primitives can only store primitive values.", t);
		}
		this.value = value;
	}

	@Override
	public void set(Mixed index, Mixed c, Target t, Environment env) {
		throw new CRECastException("mutable_primitives cannot have values set in them", t);
	}

	/**
	 * Sets the value as if
	 * {@link #set(Mixed, com.laytonsmith.core.constructs.Target, Environment)} were called, then
	 * returns a reference to this object.
	 *
	 * @param value
	 * @param t
	 * @return
	 */
	public CMutablePrimitive setAndReturn(Mixed value, Target t, Environment env) {
		set(value, t, env);
		return this;
	}

	public Mixed get() {
		return value;
	}

	@Override
	public Mixed get(Mixed index, Target t, Environment env) {
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
	public long size(Environment env) {
		if(value.isInstanceOf(Sizeable.TYPE, null, env)) {
			return ArgumentValidation.getObject(value, Target.UNKNOWN, Sizeable.class).size(env);
		} else {
			return 0;
		}
	}

	@Override
	public boolean canBeAssociative() {
		return false;
	}

	@Override
	public List<Mixed> asList(Environment env) {
		return getArray();
	}

	@Override
	public void clear() {
		value = CNull.NULL;
	}

	@Override
	public CArray createNew(Target t, Environment env) {
		return new CMutablePrimitive(value, t, fallbackEnv);
	}

	@Override
	protected List<Mixed> getArray() {
		List<Mixed> array = new ArrayList<>();
		array.add(value);
		return array;
	}

	@Override
	protected String getString(Stack<CArray> arrays, Target t, Environment env) {
		return value.val();
	}

	@Override
	public void push(Mixed c, Integer i, Target t, Environment env) {
		set(c, t, env);
	}

	@Override
	public String docs() {
		return "A mutable primitive is a special data type that allows you to store primitives by reference, instead of value.";
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

	@Override
	public Set<ObjectModifier> getObjectModifiers() {
		return EnumSet.of(ObjectModifier.FINAL);
	}

	@Override
	public GenericParameters getGenericParameters() {
		return null;
	}
}
