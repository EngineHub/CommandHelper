package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.AbstractMixedClass;
import com.laytonsmith.core.natives.interfaces.ArrayAccessSet;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Cailin
 */
@typeof("ms.lang.Real2dMatrixRow")
public class CReal2dMatrixRow extends AbstractMixedClass implements com.laytonsmith.core.natives.interfaces.Iterable,
		ArrayAccessSet {

	@SuppressWarnings("FieldNameHidesFieldInSuperclass")
	public static final CClassType TYPE = CClassType.get(CReal2dMatrixRow.class);

	CReal2dMatrix parent;
	int rowIndex;

	public CReal2dMatrixRow(CReal2dMatrix parent, int rowIndex) {
		this.parent = parent;
		this.rowIndex = rowIndex;
	}

	@Override
	public String docs() {
		return "This class is a reference to a row in a matrix. The underlying \"array\" is shallow, and changes to"
				+ " this data will be reflected in the parent matrix.";
	}

	@Override
	public Version since() {
		return MSVersion.V3_3_5;
	}

	@Override
	public CClassType[] getSuperclasses() {
		return new CClassType[]{Mixed.TYPE};
	}

	@Override
	public CClassType[] getInterfaces() {
		return new CClassType[]{com.laytonsmith.core.natives.interfaces.Iterable.TYPE, ArrayAccessSet.TYPE};
	}

	@Override
	public Mixed get(String index, Target t) throws ConfigRuntimeException {
		throw new CRECastException("Real2dMatrix only supports int keys.", t);
	}

	@Override
	public CDouble get(int index, Target t) throws ConfigRuntimeException {
		return new CDouble(getNative(index, t), t);
	}

	@Override
	public Mixed get(Mixed index, Target t) throws ConfigRuntimeException {
		return get(ArgumentValidation.getInt32(index, t), t);
	}

	@Override
	public Set<Mixed> keySet() {
		Set<Mixed> set = new HashSet<>();
		for(int i = 0; i < parent.columns; i++) {
			set.add(new CInt(i, Target.UNKNOWN));
		}
		return set;
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
		CArray ret = new CArray(t);
		int step = (begin <= end) ? 1 : -1;

		// Note: loop includes 'begin', excludes 'end', just like typical slice semantics
		for(int i = begin; i != end; i += step) {
			CDouble d = get(i, t);
			ret.push(d, t);
		}

		return ret;
	}

	@Override
	public boolean getBooleanValue(Target t) {
		// 0 dimension matrices are not possible, so this will
		// always have at least one value in it, thus always
		// true.
		return true;
	}

	@Override
	public long size() {
		return parent.columns;
	}

	@Override
	public void set(Mixed index, Mixed value, Target t) {
		int in = ArgumentValidation.getInt32(index, t);
		double d = ArgumentValidation.getDouble(value, t);
		setNative(in, d, t);
	}

	@Override
	public String toString() {
		return "[Real2dMatrixRow]";
	}

	public void setNative(int index, double value, Target t) {
		parent.data[parent.columns * this.rowIndex + index] = value;
	}

	public double getNative(int index, Target t) {
		return parent.data[parent.columns * this.rowIndex + index];
	}

}
