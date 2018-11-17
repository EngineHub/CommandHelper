package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Common.ArrayUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.FullyQualifiedClassName;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.exceptions.CRE.CREUnsupportedOperationException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.ArrayAccess;
import com.laytonsmith.core.natives.interfaces.MEnumType;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A CClassType represent
 */
@typeof("ms.lang.ClassType")
public final class CClassType extends Construct implements ArrayAccess {

	public static final String PATH_SEPARATOR = FullyQualifiedClassName.PATH_SEPARATOR;

	private static final Map<FullyQualifiedClassName, CClassType> CACHE = new HashMap<>();

	// The only types that can be created here are the ones that don't have a real class associated with them, or the
	// TYPE value itself
	@SuppressWarnings("FieldNameHidesFieldInSuperclass")
	public static final CClassType TYPE;
	public static final CClassType AUTO;

	static {
		try {
			TYPE = new CClassType("ms.lang.ClassType", Target.UNKNOWN);
			AUTO = new CClassType("auto", Target.UNKNOWN);
		} catch (ClassNotFoundException e) {
			throw new Error(e);
		}
	}

	/**
	 * This should generally be used instead of creating a new empty array in getInterfaces, if no interfaces are
	 * implemented by this class. This saves memory.
	 */
	public static final CClassType[] EMPTY_CLASS_ARRAY = new CClassType[0];

	static {
		CACHE.put(FullyQualifiedClassName.forFullyQualifiedClass("ms.lang.ClassType"), TYPE);
	}

	private final boolean isTypeUnion;
	private final FullyQualifiedClassName fqcn;

	/**
	 * This is an invalid instance of the underlying type that can only be used for Documentation purposes or finding
	 * out meta information about the class. Because these can be a type union, this is an array.
	 */
	private final Mixed[] invalidType;

	/**
	 * This *MUST* contain a list of non type union types.
	 */
	private final SortedSet<FullyQualifiedClassName> types = new TreeSet<>();

	/**
	 * Returns the singular instance of CClassType that represents this type.
	 *
	 * <p>IMPORTANT: The type MUST be fully qualified AND exist as a real, instantiable class, or this will cause
	 * errors. The only time this method is preferred vs {@link #get(com.laytonsmith.core.FullyQualifiedClassName)} is
	 * when used to define the TYPE value.
	 *
	 * Unlike the other getters, this will not throw a ClassNotFoundException, it will instead throw an Error.
	 * @param type
	 * @return
	 */
	public static CClassType get(String type) {
		try {
			return get(FullyQualifiedClassName.forFullyQualifiedClass(type));
		} catch(ClassNotFoundException ex) {
			throw new Error(ex);
		}
	}

	/**
	 * Returns the singular instance of CClassType that represents this type.
	 *
	 * @param type
	 * @return
	 */
	public static CClassType get(FullyQualifiedClassName type) throws ClassNotFoundException {
		assert type != null;
		if(!CACHE.containsKey(type)) {
			CACHE.put(type, new CClassType(type, Target.UNKNOWN));
		}
		return CACHE.get(type);
	}

	/**
	 * Returns the singular instance of CClassType that represents this type union. string|int and int|string are both
	 * considered the same type union, as they are first normalized into a canonical form.
	 *
	 * Use {@link #get(com.laytonsmith.core.constructs.CClassType...)} instead, to ensure type safety, unless absolutely
	 * impossible (comes from user input, for instance).
	 *
	 * @param types
	 * @return
	 */
	public static CClassType get(FullyQualifiedClassName... types) throws ClassNotFoundException {

		SortedSet<FullyQualifiedClassName> t = new TreeSet<>(Arrays.asList(types));
		FullyQualifiedClassName type
				= FullyQualifiedClassName.forFullyQualifiedClass(StringUtils.Join(t, "|", e -> e.getFQCN()));
		if(!CACHE.containsKey(type)) {
			CACHE.put(type, new CClassType(type, Target.UNKNOWN));
		}
		return CACHE.get(type);
	}

	/**
	 * Returns the singular instance of CClassType that represents this type union. string|int and int|string are both
	 * considered the same type union, as they are first normalized into a canonical form.
	 *
	 * @param types
	 * @return
	 */
	public static CClassType get(CClassType... types) throws ClassNotFoundException {
		return get(Stream.of(types)
				.map(e -> e.getFQCN())
				.sorted()
				.collect(Collectors.toSet())
				.toArray(new FullyQualifiedClassName[types.length]));
	}

	/**
	 *
	 * @param type This must be the fully qualified string name.
	 * @param t
	 */
	private CClassType(String type, Target t) throws ClassNotFoundException {
		this(FullyQualifiedClassName.forFullyQualifiedClass(type), t);
	}

	/**
	 * Creates a new CClassType
	 *
	 * @param type
	 * @param t
	 */
	private CClassType(FullyQualifiedClassName type, Target t) throws ClassNotFoundException {
		super(type.getFQCN(), ConstructType.CLASS_TYPE, t);
		isTypeUnion = type.isTypeUnion();
		fqcn = type;
		if(isTypeUnion) {
			// Split them out
			types.addAll(Stream.of(type.getFQCN().split("|"))
					.map(e -> FullyQualifiedClassName.forFullyQualifiedClass(e)).collect(Collectors.toList()));
		} else {
			types.add(type);
		}
		if("auto".equals(type.getFQCN())) {
			invalidType = null;
		} else {
			// TODO: This must change once user types are introduced
			invalidType = new Mixed[types.size()];
			for(int i = 0; i < invalidType.length; i++) {
				invalidType[i] = NativeTypeList.getInvalidInstanceForUse(fqcn);
			}
		}
	}

	@Override
	public boolean isDynamic() {
		return false;
	}

	@Override
	@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
	public boolean equals(Object obj) {
		// Because we maintain a static list of singletons, we can short circuit this check. If obj is not == to
		// us, we are different objects. If this is ever not correct, we have a serious problem elsewhere, as this
		// assumption is held elsewhere in code.
		return this == obj;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	/**
	 * Returns true if there is more than one type in this type
	 *
	 * @return
	 */
	public boolean isTypeUnion() {
		return this.isTypeUnion;
	}

	/**
	 * Returns true if checkClass extends, implements, or otherwise derives from superClass
	 *
	 * @param checkClass
	 * @param superClass
	 * @return
	 */
	public static boolean doesExtend(CClassType checkClass, CClassType superClass) {
		if(checkClass.equals(superClass)) {
			// more efficient check
			return true;
		}
		for(CClassType tCheck : checkClass.getTypes()) {
			for(CClassType tSuper : superClass.getTypes()) {
				try {
					// TODO: This is currently being done in a very lazy way. It needs to be reworked.
					// For now, this is ok, but will not work once user types are added.
					Class cSuper = NativeTypeList.getNativeClass(tSuper.getFQCN());
					Class cCheck = NativeTypeList.getNativeClass(tCheck.getFQCN());
					if(!cSuper.isAssignableFrom(cCheck)) {
						return false;
					}
				} catch (ClassNotFoundException ex) {
					throw new RuntimeException(ex);
				}
			}
		}
		return true;
	}

	/**
	 * Returns true if this class extends the specified one
	 *
	 * @param superClass
	 * @return
	 */
	public boolean doesExtend(CClassType superClass) {
		return doesExtend(this, superClass);
	}

	/**
	 * Works like {@link #doesExtend(com.laytonsmith.core.constructs.CClassType, com.laytonsmith.core.constructs.CClassType)
	 * }, however rethrows the {@link ClassNotFoundException} that doesExtend throws as an {@link Error}. This should
	 * not be used unless the class names come from hardcoded values.
	 *
	 * @param checkClass
	 * @param superClass
	 * @return
	 */
	public static boolean unsafeDoesExtend(CClassType checkClass, CClassType superClass) {
		return doesExtend(checkClass, superClass);
	}

	/**
	 * Performs an unsafe check to see if this class extends the specified one
	 *
	 * @param superClass
	 * @return
	 */
	public boolean unsafeDoesExtend(CClassType superClass) {
		return unsafeDoesExtend(this, superClass);
	}

	/**
	 * Returns true if the specified class extends this one
	 *
	 * @param checkClass
	 * @return
	 * @throws ClassNotFoundException
	 */
	public boolean isExtendedBy(CClassType checkClass) throws ClassNotFoundException {
		return doesExtend(checkClass, this);
	}

	/**
	 * Performs an unsafe check to see if the specified class extends this one
	 *
	 * @param checkClass
	 * @return
	 */
	public boolean unsafeIsExtendedBy(CClassType checkClass) {
		return unsafeDoesExtend(checkClass, this);
	}

	@Override
	public CClassType[] getSuperclasses() {
		return new CClassType[]{Mixed.TYPE};
	}

	@Override
	public CClassType[] getInterfaces() {
		return CClassType.EMPTY_CLASS_ARRAY;
	}

	/**
	 * Returns the superclasses for the underlying type, not the superclasses for ClassType itself.
	 * @return
	 */
	public CClassType[] getSuperclassesForType() {
		return Stream.of(invalidType).flatMap(e -> Stream.of(e.getSuperclasses()))
				.collect(Collectors.toSet()).toArray(CClassType.EMPTY_CLASS_ARRAY);
	}

	/**
	 *  Returns the interfaces for the underlying type, not the interfaces for ClassType itself.
	 * @return
	 */
	public CClassType[] getInterfacesForType() {
		return Stream.of(invalidType).flatMap(e -> Stream.of(e.getInterfaces()))
				.collect(Collectors.toSet()).toArray(CClassType.EMPTY_CLASS_ARRAY);
	}

	/**
	 * Returns a set of individual types for this type. If it is a class union, multiple types will be returned in the
	 * set. Each of the CClassTypes within this set are guaranteed to not be a type union.
	 *
	 * This might be ok to make public if necessary in the future.
	 *
	 * @return
	 */
	protected Set<CClassType> getTypes() {
		Set<CClassType> t = new HashSet<>();
		for(FullyQualifiedClassName type : types) {
			try {
				t.add(CClassType.get(type));
			} catch(ClassNotFoundException ex) {
				// This can't happen, because
				throw new Error(ex);
			}
		}
		return t;
	}

	/**
	 * Returns the package that this class is in. If the class is not in a package, or if this is a class union, null
	 * is returned.
	 * @return
	 */
	public CPackage getPackage() {
		if(isTypeUnion) {
			return null;
		}
		if(!val().contains(PATH_SEPARATOR)) {
			return null;
		}
		String[] parts = val().split(Pattern.quote(PATH_SEPARATOR));
		return new CPackage(Target.UNKNOWN, ArrayUtils.slice(parts, 0, parts.length - 2));
	}

	/**
	 * Returns the name of the class type without the package. If this is a type union, then each type is simplified,
	 * and returned as a string such as "int|string".
	 * @return
	 */
	public String getSimpleName() {
		return fqcn.getSimpleName();
	}

	@Override
	public String docs() {
		return "A ClassType is a value that represents an object type. This includes primitives or other value types.";
	}

	@Override
	public Version since() {
		return MSVersion.V3_3_1;
	}

	public FullyQualifiedClassName getFQCN() {
		return fqcn;
	}

	public boolean isEnum() {
		if("ms.lang.enum".equals(fqcn.getFQCN())) {
			// By default, this returns true when something is instanceof a thing, but in this case, we don't want
			// that, because ironically, ms.lang.enum is itself not an enum.
			return false;
		}
		return doesExtend(MEnumType.TYPE);
	}

	@Override
	public CClassType typeof() {
		return CClassType.TYPE;
	}

	// TODO: These getters will eventually be re-done to support static methods, but for now that is out of scope,
	// so we just specifically support enums for now.
	@Override
	public Mixed get(String index, Target t) throws ConfigRuntimeException {
		if(isEnum()) {
			try {
				return NativeTypeList.getNativeEnumType(fqcn).get(index, t);
			} catch(ClassNotFoundException ex) {
				throw new RuntimeException(ex);
			}
		}
		throw new CREUnsupportedOperationException("Unsupported operation", t);
	}

	@Override
	public Mixed get(int index, Target t) throws ConfigRuntimeException {
		if(isEnum()) {
			try {
				return NativeTypeList.getNativeEnumType(fqcn).get(index, t);
			} catch(ClassNotFoundException ex) {
				throw new RuntimeException(ex);
			}
		}
		throw new CREUnsupportedOperationException("Unsupported operation", t);
	}

	@Override
	public Mixed get(Mixed index, Target t) throws ConfigRuntimeException {
		if(isEnum()) {
			try {
				return NativeTypeList.getNativeEnumType(fqcn).get(index, t);
			} catch(ClassNotFoundException ex) {
				throw new RuntimeException(ex);
			}
		}
		throw new CREUnsupportedOperationException("Unsupported operation", t);
	}

	@Override
	public Set<Mixed> keySet() {
		if(isEnum()) {
			try {
				return NativeTypeList.getNativeEnumType(fqcn).keySet();
			} catch(ClassNotFoundException ex) {
				throw new RuntimeException(ex);
			}
		}
		return new HashSet<>();
	}

	@Override
	public long size() {
		if(isEnum()) {
			try {
				return NativeTypeList.getNativeEnumType(fqcn).size();
			} catch(ClassNotFoundException ex) {
				throw new RuntimeException(ex);
			}
		}
		return 0;
	}

	@Override
	public boolean isAssociative() {
		return true;
	}

	@Override
	public boolean canBeAssociative() {
		return true;
	}

	@Override
	public Mixed slice(int begin, int end, Target t) {
		throw new CREUnsupportedOperationException("Unsupported operation", t);
	}



}
