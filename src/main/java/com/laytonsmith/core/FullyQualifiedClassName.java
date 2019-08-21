/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.annotations.MEnum;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.constructs.NativeTypeList;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.core.objects.ObjectDefinitionNotFoundException;
import com.laytonsmith.core.objects.ObjectDefinitionTable;
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
	private Class<? extends Mixed> nativeClass;

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
	 * @param env The environment, from which the ObjectDefinitionTable for this runtime is pulled,
	 * which is where classes are looked up.
	 * @return The fully qualified class name.
	 * @throws CRECastException If the class type can't be found
	 */
	public static FullyQualifiedClassName forName(String unqualified, Target t, Environment env)
			throws CRECastException {
		ObjectDefinitionTable odt = env.getEnv(CompilerEnvironment.class).getObjectDefinitionTable();
		try {
			// Try first, if this is a fully qualified name
			FullyQualifiedClassName fqcn = new FullyQualifiedClassName(unqualified);
			odt.get(fqcn);
			// It is, this would have thrown an exception otherwise
			return fqcn;
		} catch (ObjectDefinitionNotFoundException ex) {
			// nope
			// TODO
		}
		// TODO: This need to be removed eventually, but for now is fine, and is needed to make live code work.
		// Eventually, however, this should just end with throwing a CastException (or something), because the
		// class could not be resolved. Native classes should be loaded through the same mechanism as user classes.
		return forDefaultClasses(unqualified, t);
	}

	/**
	 * If the type represents an enum tagged with {@link MEnum}, then this method should be used, but is otherwise
	 * identical to {@link #forNativeClass(java.lang.Class)}.
	 * @param clazz
	 * @return
	 */
	public static FullyQualifiedClassName forNativeEnum(Class<? extends Enum> clazz) {
		MEnum m = clazz.getAnnotation(MEnum.class);
		if(m == null) {
			throw new Error("Native enum " + clazz + " does not provide an MEnum annotation");
		}
		String fqcn = m.value();
		FullyQualifiedClassName f = new FullyQualifiedClassName(fqcn);
		try {
			f.nativeClass = NativeTypeList.getNativeEnumType(f).typeof().getNativeType();
		} catch (ClassNotFoundException ex) {
			// This can't happen, it would have already been the above error.
			throw new Error(ex);
		}
		return f;
	}

	/**
	 * If the type represents a native class, this method can be used. Not only does it never throw an exception
	 * (except an Error, if the class does not define a typeof annotation), getNativeClass will return a reference
	 * to the Class object, which is useful for shortcutting various operations.
	 * @param clazz
	 * @return
	 */
	public static FullyQualifiedClassName forNativeClass(Class<? extends Mixed> clazz) {
		typeof t = ClassDiscovery.GetClassAnnotation(clazz, typeof.class);
		if(t == null) {
			throw new Error("Native class " + clazz + " does not provide a typeof annotation");
		}
		String fqcn = t.value();
		FullyQualifiedClassName f = new FullyQualifiedClassName(fqcn);
		f.nativeClass = clazz;
		return f;
	}

	/**
	 * If the class is known for sure to be within the default import list, this method can be used. If the native
	 * class is available, this MUST not be used, as it causes too much of a performance hit. Instead, use
	 * {@link #forNativeClass(java.lang.Class)} or {@link #forNativeEnum(java.lang.Class)}. DynamicEnums are not
	 * specially supported, but they can safely use this method, though their use should be very limited.
	 * @param unqualified The (potentially) unqualified type.
	 * @param t The code target.
	 * @return The FullyQualifiedClassName.
	 * @throws CRECastException If the class type can't be found
	 */
	private static FullyQualifiedClassName forDefaultClasses(String unqualified, Target t) throws CRECastException {
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
	 * <p>
	 * The class does not have to (yet) exist, though it should exist before usage anywhere else, such as in CClassType.
	 * However, during compiliation, for instance, when the class is being defined, the class name will not exist
	 * between compilation and object specification encoding, so this can be used in the meantime to represent the
	 * class.
	 * <p>
	 * If this represents a native class, use {@link #forNativeClass(java.lang.Class)} instead.
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

	/**
	 * Returns the {@link UnqualifiedClassName} for this fully qualified class name, using the special constructor.
	 * @return
	 */
	public UnqualifiedClassName asUCN() {
		return new UnqualifiedClassName(this);
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

	/**
	 * Returns the underlying native class, iff this is a native class.
	 * @return
	 */
	public Class<? extends Mixed> getNativeClass() {
		return nativeClass;
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
