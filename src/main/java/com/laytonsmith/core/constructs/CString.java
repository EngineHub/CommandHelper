package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.ExposedElement;
import com.laytonsmith.annotations.NonInheritImplements;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.constructs.generics.GenericParameters;
import com.laytonsmith.core.constructs.generics.GenericTypeParameters;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CRERangeException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.core.objects.ObjectType;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

/**
 *
 *
 */
@typeof("ms.lang.string")
@NonInheritImplements(value = POJOConversion.class, parameterTypes = {CString.class, String.class})
public class CString extends CPrimitive implements Cloneable,
		com.laytonsmith.core.natives.interfaces.Iterable {

	@SuppressWarnings("FieldNameHidesFieldInSuperclass")
	public static final CClassType TYPE = CClassType.get(CString.class)
			.withSuperParameters(GenericTypeParameters
					.nativeBuilder(com.laytonsmith.core.natives.interfaces.Iterable.TYPE)
					.addParameter(CClassType.RECURSIVE_DEFINITION, null))
			.done();

	public CString(String value, Target t) {
		super(value == null ? "" : value, ConstructType.STRING, t);
	}

	public CString(char value, Target t) {
		this(Character.toString(value), t);
	}

	public CString(CharSequence value, Target t) {
		this(value.toString(), t);
	}

	/**
	 * Given the input construct, uses the val() method of it, and constructs a new string based on that.
	 * @param val
	 */
	public CString(Mixed val) {
		this(val.val(), val.getTarget());
	}

	@Override
	public CString clone() throws CloneNotSupportedException {
		return this;
	}

	@Override
	public boolean isDynamic() {
		return false;
	}

	@Override
	public long size(Environment env) {
		return val().length();
	}

	@Override
	public boolean canBeAssociative() {
		return false;
	}

	@Override
	public Mixed slice(int begin, int end, Target t, Environment env) {
		if(begin > end) {
			return new CString("", t);
		}
		try {
			return new CString(this.val().substring(begin, end), t);
		} catch (StringIndexOutOfBoundsException e) {
			throw new CRERangeException("String bounds out of range. Indices only go up to " + (this.val().length() - 1), t);
		}
	}

	@Override
	public String getQuote() {
		return super.getQuote();
	}

	@Override
	public Mixed get(int index, Target t, Environment env) throws ConfigRuntimeException {
		try {
			return new CString(this.val().charAt(index), t);
		} catch (StringIndexOutOfBoundsException e) {
			throw new CRERangeException("No character at index " + index + ". Indices only go up to " + (this.val().length() - 1), t);
		}
	}

	@Override
	public final Mixed get(Mixed index, Target t, Environment env) throws ConfigRuntimeException {
		int i = ArgumentValidation.getInt32(index, t, env);
		return get(i, t, env);
	}

	@Override
	public final Mixed get(String index, Target t, Environment env) {
		try {
			int i = Integer.parseInt(index);
			return get(i, t, env);
		} catch (NumberFormatException e) {
			throw new CREFormatException("Expecting numerical index, but received " + index, t);
		}
	}

	@Override
	public boolean isAssociative() {
		return false;
	}

	@Override
	public Set<Mixed> keySet(Environment env) {
		return new AbstractSet<Mixed>() {
			@Override
			public int size() {
				return CString.this.val().length();
			}

			@Override
			public Iterator<Mixed> iterator() {
				return new Iterator<Mixed>() {
					int i = 0;
					@Override
					public boolean hasNext() {
						return i < CString.this.val().length();
					}

					@Override
					public Mixed next() {
						return new CInt(i++, Target.UNKNOWN);
					}
				};
			}


		};
	}

	@Override
	public String docs() {
		return "A string is a value that contains character data. The character encoding is stored with the string as well.";
	}

	@Override
	public Version since() {
		return MSVersion.V3_0_1;
	}

	@Override
	public CClassType[] getSuperclasses() {
		return new CClassType[]{CPrimitive.TYPE};
	}

	@Override
	public CClassType[] getInterfaces() {
		return new CClassType[]{com.laytonsmith.core.natives.interfaces.Iterable.TYPE};
	}

	@Override
	public ObjectType getObjectType() {
		return ObjectType.CLASS;
	}

	@Override
	public CString duplicate() {
		return new CString(val(), getTarget());
	}

	@Override
	public boolean getBooleanValue(Environment env, Target t) {
		if(val().equals("false")) {
			MSLog.GetLogger().e(MSLog.Tags.FALSESTRING, "String \"false\" evaluates as true (non-empty strings are"
					+ " true). This is most likely not what you meant to do. This warning can globally be disabled"
					+ " with the logger-preferences.ini file.", t);
		}
		return val().length() > 0;
	}

	@ExposedElement
	public String toLowerCase(Environment env, Target t, Locale locale) {
		return val().toLowerCase(locale);
	}

	@ExposedElement
	public String toUpperCase(Environment env, Target t, Locale locale) {
		return val().toUpperCase(locale);
	}

	@ExposedElement
	public boolean matches(
			Environment env, Target t,
			String regex) {
		return val().matches(regex);
	}

	public CString construct(String s, Target t) {
		return new CString(s, t);
	}

	public String convert() {
		return val();
	}

	@Override
	public GenericParameters getGenericParameters() {
		return null;
	}
}
