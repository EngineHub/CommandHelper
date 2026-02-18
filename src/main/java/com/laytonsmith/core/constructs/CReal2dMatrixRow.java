package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.environments.Environment;
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

	/** @deprecated Use {@link #get(String, Target, Environment)} instead. */
	@Deprecated
	@Override
	public Mixed get(String index, Target t) throws ConfigRuntimeException {
		return get(index, t, null);
	}

	@Override
	public Mixed get(String index, Target t, Environment env) throws ConfigRuntimeException {
		throw new CRECastException("Real2dMatrix only supports int keys.", t);
	}

	/** @deprecated Use {@link #get(int, Target, Environment)} instead. */
	@Deprecated
	@Override
	public CDouble get(int index, Target t) throws ConfigRuntimeException {
		return (CDouble) get(index, t, null);
	}

	@Override
	public Mixed get(int index, Target t, Environment env) throws ConfigRuntimeException {
		return new CDouble(getNative(index, t), t);
	}

	/** @deprecated Use {@link #get(Mixed, Target, Environment)} instead. */
	@Deprecated
	@Override
	public Mixed get(Mixed index, Target t) throws ConfigRuntimeException {
		return get(index, t, null);
	}

	@Override
	public Mixed get(Mixed index, Target t, Environment env) throws ConfigRuntimeException {
		return get(ArgumentValidation.getInt32(index, t), t, env);
	}

	/** @deprecated Use {@link #keySet(Environment)} instead. */
	@Deprecated
	@Override
	public Set<Mixed> keySet() {
		return keySet(null);
	}

	@Override
	public Set<Mixed> keySet(Environment env) {
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

	/** @deprecated Use {@link #slice(int, int, Target, Environment)} instead. */
	@Deprecated
	@Override
	public Mixed slice(int begin, int end, Target t) {
		return slice(begin, end, t, null);
	}

	@Override
	public Mixed slice(int begin, int end, Target t, Environment env) {
		CArray ret = new CArray(t);
		int step = (begin <= end) ? 1 : -1;

		// Note: loop includes 'begin', excludes 'end', just like typical slice semantics
		for(int i = begin; i != end; i += step) {
			CDouble d = (CDouble) get(i, t, env);
			ret.push(d, t);
		}

		return ret;
	}

	/** @deprecated Use {@link #getBooleanValue(Environment, Target)} instead. */
	@Deprecated
	@Override
	public boolean getBooleanValue(Target t) {
		return getBooleanValue(null, t);
	}

	@Override
	public boolean getBooleanValue(Environment env, Target t) {
		// 0 dimension matrices are not possible, so this will
		// always have at least one value in it, thus always
		// true.
		return true;
	}

	/** @deprecated Use {@link #size(Environment)} instead. */
	@Deprecated
	@Override
	public long size() {
		return size(null);
	}

	@Override
	public long size(Environment env) {
		return parent.columns;
	}

	/** @deprecated Use {@link #set(Mixed, Mixed, Target, Environment)} instead. */
	@Deprecated
	@Override
	public void set(Mixed index, Mixed value, Target t) {
		set(index, value, t, null);
	}

	@Override
	public void set(Mixed index, Mixed value, Target t, Environment env) {
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
