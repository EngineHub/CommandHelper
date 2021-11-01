package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Common.ArrayUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.constructs.generics.ConstraintLocation;
import com.laytonsmith.core.constructs.generics.Constraints;
import com.laytonsmith.core.constructs.generics.UnboundedConstraint;
import com.laytonsmith.core.constructs.generics.GenericDeclaration;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREIndexOverflowException;
import com.laytonsmith.core.exceptions.CRE.CREUnsupportedOperationException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Booleanish;
import com.laytonsmith.core.natives.interfaces.Mixed;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This data type is meant to be used for low level systems programming. The API doesn't use it directly, though in
 * the interpreter implementation, it does use an actual fixed size array. For native code, however, this uses native
 * fixed size arrays, which assists with code mapping and is a required primitive for bootstrapping purposes.
 */
@typeof("ms.lang.fixed_array")
public class CFixedArray extends Construct implements
		java.lang.Iterable<Mixed>, Booleanish, com.laytonsmith.core.natives.interfaces.Iterable {

	public static final CClassType TYPE = CClassType.getWithGenericDefinition(CFixedArray.class,
			new GenericDeclaration(Target.UNKNOWN, new Constraints(ConstraintLocation.DEFINITION,
					new UnboundedConstraint(Target.UNKNOWN, "T"))));
	private Mixed[] data;
	private CClassType allowedType;

	public CFixedArray(Target t, CClassType type, int size) {
		super(type.getSimpleName() + "[" + size + "]", ConstructType.ARRAY, t);
		data = new Mixed[size];
		allowedType = type;
	}

	@Override
	public boolean isDynamic() {
		return true;
	}

	@Override
	public Mixed get(String index, Target t) throws ConfigRuntimeException {
		return null;
	}

	@Override
	public Mixed get(int index, Target t) throws ConfigRuntimeException {
		if(index < 0 || index >= data.length) {
			throw new CREIndexOverflowException("Index overflows array size", t);
		}
		Mixed d = data[index];
		if(d == null) {
			return CNull.NULL;
		}
		return d;
	}

	@Override
	public Mixed get(Mixed index, Target t) throws ConfigRuntimeException {
		return get(ArgumentValidation.getInt32(index, t), t);
	}

	@Override
	public Set<Mixed> keySet() {
		Set<Mixed> set = new LinkedHashSet<>(data.length);
		for(int i = 0; i < data.length; i++) {
			set.add(new CInt(i, Target.UNKNOWN));
		}
		return set;
	}

	private void validateSet(Mixed value, Target t) {
		if(!value.typeof().doesExtend(allowedType)) {
			throw new CRECastException("Cannot set value of type " + value.typeof().toString() + " into fixed_array of type " + allowedType.toString(), t);
		}
	}

	public void set(int index, Mixed value, Target t) {
		validateSet(value, t);
		if(index >= data.length || index < 0) {
			throw new CREIndexOverflowException("Index under/overflow in fixed_array", t);
		}
		data[index] = value;
	}

	@Override
	public boolean isAssociative() {
		return false;
	}

	@Override
	public boolean canBeAssociative() {
		return false;
	}

	@Override
	public Mixed slice(int begin, int end, Target t) {
		throw new CREUnsupportedOperationException("slices are not yet implemented on fixed_array", t);
	}

	@Override
	public boolean getBooleanValue(Target t) {
		return size() > 0;
	}

	@Override
	public long size() {
		return data.length;
	}

	public void fill(Mixed value, Target t) {
		validateSet(value, t);
		ArrayUtils.fill(data, value);
	}

	@Override
	public Iterator<Mixed> iterator() {
		return Arrays.stream(data).iterator();
	}

	@Override
	public CClassType[] getInterfaces() {
		return new CClassType[]{Booleanish.TYPE, com.laytonsmith.core.natives.interfaces.Iterable.TYPE};
	}

	@Override
	public CClassType[] getSuperclasses() {
		return new CClassType[]{Mixed.TYPE};
	}

	@Override
	public String docs() {
		return "An fixed_array is a data type, which contains any number of other values. Unlike a normal array however,"
				+ " it cannot be associative, nor can the size be changed later. Additionally, values must be of the"
				+ " initially given type. In general normal arrays should be used instead of this, but fixed_array"
				+ " maps more precisely onto the underlying system array type. All values in the array are initialized"
				+ " initially to null.";
	}

	@Override
	public Version since() {
		return MSVersion.V3_3_5;
	}
}
