package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.constructs.generics.GenericParameters;
import com.laytonsmith.core.constructs.generics.GenericTypeParameters;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CRERangeException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.ArrayHandling;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 *
 *
 */
@typeof("ms.lang.slice")
public class CSlice extends CArray {

	@SuppressWarnings("FieldNameHidesFieldInSuperclass")
	public static final CClassType TYPE = CClassType.get(CSlice.class)
			.withSuperParameters(GenericTypeParameters.nativeBuilder(CArray.TYPE)
				.addParameter(CInt.TYPE, null))
			.done();

	private long start;
	private long finish;
	private int direction;
	private long max;
	private long size;

	public CSlice(String slice, Target t, Environment env) throws ConfigCompileException {
		super(t, GenericParameters.emptyBuilder(CArray.TYPE)
				.addNativeParameter(CInt.TYPE, null)
				.buildNative(), env);
		String[] split = slice.split("\\.\\.");
		if(split.length > 2) {
			throw new ConfigCompileException("Invalid slice notation! (" + slice + ")", t);
		}

		String sstart;
		String sfinish;
		if(split.length == 1) {
			sstart = slice.trim().substring(0, slice.trim().length() - 2);
			sfinish = "-1";
		} else if(slice.trim().startsWith("..")) {
			sstart = "0";
			sfinish = slice.trim().substring(2);
		} else {
			sstart = split[0];
			sfinish = split[1];
		}
		try {
			start = Long.parseLong(sstart.trim());
			finish = Long.parseLong(sfinish.trim());
		} catch (NumberFormatException e) {
			throw new CRECastException("Expecting integer in a slice, but was given \"" + sstart + "\" and \"" + sfinish + "\"", t);
		}
		calculateCaches();
	}

	public CSlice(long from, long to, Target t, Environment env) {
		super(t, null, env);
		this.start = from;
		this.finish = to;
		calculateCaches();
	}

	@Override
	public List<Mixed> asList(Environment env) {
		CArray ca = new ArrayHandling.range().exec(Target.UNKNOWN, env, null, new CInt(start, Target.UNKNOWN), new CInt(finish, Target.UNKNOWN));
		return ca.asList(env);
	}

	private void calculateCaches() {
		direction = start < finish ? 1 : start == finish ? 0 : -1;
		max = Math.abs(finish - start);
		size = max + 1;
	}

	public long getStart() {
		return start;
	}

	public long getFinish() {
		return finish;
	}

	@Override
	public boolean isDynamic() {
		return false;
	}

	@Override
	public String val() {
		return start + ".." + finish;
	}

	@Override
	public String toString() {
		return val();
	}

	@Override
	protected String getString(Stack<CArray> arrays, Target t, Environment env) {
		//We don't need to consider arrays, because we can't
		//get stuck in an infinite loop.
		return val();
	}

	@Override
	public boolean inAssociativeMode() {
		return false;
	}

	@Override
	public void set(Mixed index, Mixed c, Target t, Environment env) {
		throw new CRECastException("CSlices cannot set values", t);
	}

	@Override
	public Mixed get(String index, Target t, Environment env) {
		return get(new CString(index, t), t, env);
	}

	@Override
	public Mixed get(int index, Target t) {
		long i = index;
		if(i > max) {
			throw new CRERangeException("Index out of bounds. Index: " + i + " Size: " + max, t);
		}
		return new CInt(start + (direction * i), t);
	}


	@Override
	public Mixed get(Mixed index, Target t, Environment env) {
		long i = ArgumentValidation.getInt(index, t, env);
		if(i > max) {
			throw new CRERangeException("Index out of bounds. Index: " + i + " Size: " + max, t);
		}
		return new CInt(start + (direction * i), t);
	}

	@Override
	public Set<Mixed> keySet(Environment env) {
		// To keep our memory footprint down, we create a "fake" keyset here, which doesn't
		// require actually creating an entire Set. Removing items from the set isn't supported,
		// but all iteration options are.
		return new AbstractSet<Mixed>() {

			@Override
			public Iterator<Mixed> iterator() {
				return new Iterator<Mixed>() {

					int index = 0;

					@Override
					public boolean hasNext() {
						return index < size;
					}

					@Override
					public Mixed next() {
						return new CInt(index++, Target.UNKNOWN);
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException("Not supported yet.");
					}
				};
			}

			@Override
			public int size() {
				return (int) CSlice.this.size(env);
			}
		};
	}

	@Override
	public long size(Environment env) {
		return size;
	}

	@Override
	public boolean contains(Mixed c) {
		try {
			long i = ArgumentValidation.getInt(c, Target.UNKNOWN, fallbackEnv);
			if(start < finish) {
				return start <= i && i <= finish;
			} else {
				return start >= i && i <= finish;
			}
		} catch (ConfigRuntimeException e) {
			return false;
		}
	}

	@Override
	public boolean containsKey(String c) {
		try {
			long i = Long.parseLong(c);
			return i >= 0 && i < size;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	@Override
	public String docs() {
		return "A slice is a value that represents a numeric range, either in the positive direction, or negative.";
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
		return CClassType.EMPTY_CLASS_ARRAY;
	}

	@Override
	public Mixed slice(int begin, int end, Target t, Environment env) {
		if(begin < end) {
			return new CSlice(start - begin, finish - end, t, env);
		} else {
			return new CSlice(finish - begin, start - end, t, env);
		}
	}

	@Override
	public GenericParameters getGenericParameters() {
		return null;
	}
}
