package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CREIndexOverflowException;
import com.laytonsmith.core.exceptions.CRE.CRERangeException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.ArrayAccess;
import com.laytonsmith.core.natives.interfaces.ObjectType;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;

/**
 *
 *
 */
@typeof("ms.lang.string")
public class CString extends CPrimitive implements Cloneable, ArrayAccess {

	@SuppressWarnings("FieldNameHidesFieldInSuperclass")
	public static final CClassType TYPE = CClassType.get("ms.lang.string");

	public CString(String value, Target t) {
		super(value == null ? "" : value, ConstructType.STRING, t);
	}

	public CString(char value, Target t) {
		this(Character.toString(value), t);
	}

	public CString(CharSequence value, Target t) {
		this(value.toString(), t);
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
	public long size() {
		return val().length();
	}

	@Override
	public boolean canBeAssociative() {
		return false;
	}

	@Override
	public Construct slice(int begin, int end, Target t) {
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
	public Construct get(int index, Target t) throws ConfigRuntimeException {
		try {
			return new CString(this.val().charAt(index), t);
		} catch (StringIndexOutOfBoundsException e) {
			throw new CRERangeException("No character at index " + index + ". Indices only go up to " + (this.val().length() - 1), t);
		}
	}

	@Override
	public final Construct get(Construct index, Target t) throws ConfigRuntimeException {
		int i = Static.getInt32(index, t);
		return get(i, t);
	}

	@Override
	public final Construct get(String index, Target t) {
		try {
			int i = Integer.parseInt(index);
			return get(i, t);
		} catch (NumberFormatException e) {
			throw new CREFormatException("Expecting numerical index, but recieved " + index, t);
		}
	}

	@Override
	public boolean isAssociative() {
		return false;
	}

	@Override
	public Set<Construct> keySet() {
		return new AbstractSet<Construct>(){
			@Override
			public int size() {
				return CString.this.val().length();
			}

			@Override
			public Iterator<Construct> iterator() {
				return new Iterator<Construct>() {
					int i = 0;
					@Override
					public boolean hasNext() {
						return i < CString.this.val().length();
					}

					@Override
					public Construct next() {
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
		return CHVersion.V3_0_1;
	}

	@Override
	public CClassType[] getSuperclasses() {
		return new CClassType[]{CPrimitive.TYPE};
	}

	@Override
	public CClassType[] getInterfaces() {
		return new CClassType[]{ArrayAccess.TYPE};
	}

	@Override
	public ObjectType getObjectType() {
		return ObjectType.CLASS;
	}

}
