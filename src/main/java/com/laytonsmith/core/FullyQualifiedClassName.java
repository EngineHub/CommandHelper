/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.core.constructs.NativeTypeList;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * This class represents a fully qualified class name. It is better to fully qualify the type once, and then pass that
 * around, rather than passing around a string.
 */
public final class FullyQualifiedClassName implements Comparable<FullyQualifiedClassName> {
	public static final String PATH_SEPARATOR = ".";

	private final String fullyQualifiedName;

	private FullyQualifiedClassName(String name) {
		Objects.requireNonNull(name, "The name passed in may not be null");
		this.fullyQualifiedName = name;
	}

	/**
	 * Returns the fully qualified class name for the given reference. This is resolved vs the using statements in the
	 * file.
	 *
	 * <p>NOTE: This function currently doesn't request the using statement list, and until that mechanism is designed,
	 * this method works exactly the same as forDefaultClass. When accepting non-user input, it is currently and will
	 * always be ok to use forDefaultClasses. Currently, this also holds true for user input as well, because only
	 * system classes are defined, but once this feature is implemented, this will have to change, and so this method
	 * will change, unlike forDefaultClass. So, when given user input, this method should always be used, and eventually
	 * when this method is changed, it will be a compile error, but if you know for sure it's a system class, you can
	 * use forDefaultClass instead, and there will be no code changes required in the future.
	 * @param unqualified The (potentially) unqualified type.
	 * @param t The code target.
	 * @return The fully qualified class name.
	 * @throws CRECastException If the class type can't be found
	 */
	public static FullyQualifiedClassName forName(String unqualified, Target t) throws CRECastException {
		return forDefaultClasses(unqualified, t);
	}

	/**
	 * If the class is known for sure to be within the default import list, this method can be used.
	 * @param unqualified The (potentially) unqualified type.
	 * @param t The code target.
	 * @return The FullyQualifiedClassName.
	 * @throws CRECastException If the class type can't be found
	 */
	public static FullyQualifiedClassName forDefaultClasses(String unqualified, Target t) throws CRECastException {
		String fqcn = NativeTypeList.resolveNativeType(unqualified);
		if(fqcn == null) {
			throw new CRECastException("Cannot find \"" + unqualified + "\" type", t);
		}
		return new FullyQualifiedClassName(fqcn);
	}

	/**
	 * If you know for a fact that the name is already fully qualified, this step skips qualification. If you aren't
	 * sure whether or not the name is fully qualified, don't use the method, the other methods will accept a fully
	 * qualified class name, but not change it, but if it isn't fully qualified, then it will do so.
	 * @param qualified
	 * @return
	 */
	public static FullyQualifiedClassName forFullyQualifiedClass(String qualified) {
		return new FullyQualifiedClassName(qualified);
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof FullyQualifiedClassName)) {
			return false;
		}
		return fullyQualifiedName.equals(((FullyQualifiedClassName) obj).fullyQualifiedName);
	}

	@Override
	public int hashCode() {
		return fullyQualifiedName.hashCode();
	}

	/**
	 * Returns the string representation of the fully qualified class name.
	 * @return
	 */
	public String getFQCN() {
		return fullyQualifiedName;
	}

	@Override
	public String toString() {
		return fullyQualifiedName;
	}

	@Override
	public int compareTo(FullyQualifiedClassName o) {
		return this.fullyQualifiedName.compareTo(o.fullyQualifiedName);
	}

	public boolean isTypeUnion() {
		return this.fullyQualifiedName.contains("|");
	}

	public String getSimpleName() {
		List<String> parts = new ArrayList<>();
		for(String t : fullyQualifiedName.split("\\|")) {
			String[] sparts = t.split(Pattern.quote(PATH_SEPARATOR));
			try {
				parts.add(sparts[sparts.length - 1]);
			} catch (ArrayIndexOutOfBoundsException e) {
				throw new ArrayIndexOutOfBoundsException("Could not properly get simple name for " + fullyQualifiedName);
			}
		}
		return StringUtils.Join(parts, "|");
	}


}
