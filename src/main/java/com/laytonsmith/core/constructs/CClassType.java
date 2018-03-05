package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 *
 */
@typeof("ClassType")
public class CClassType extends Construct {

	private static final Map<String, CClassType> cache = new HashMap<>();
	@SuppressWarnings("FieldNameHidesFieldInSuperclass")
	public static final CClassType TYPE = new CClassType("ClassType", Target.UNKNOWN);

	/**
	 * This should generally be used instead of creating a new empty array in getInterfaces, if no interfaces are
	 * implemented by this class. This saves memory.
	 */
	public static final CClassType[] EMPTY_CLASS_ARRAY = new CClassType[0];

	static {
		cache.put("ClassType", TYPE);
	}

	private final boolean isTypeUnion;

	private final SortedSet<String> types = new TreeSet<>(new Comparator<String>() {

		@Override
		public int compare(String o1, String o2) {
			return o1.compareTo(o2);
		}
	});

	/**
	 * Returns the singular instance of CClassType that represents this type.
	 *
	 * @param type
	 * @return
	 */
	public static CClassType get(String type) {
		if (!cache.containsKey(type)) {
			cache.put(type, new CClassType(type, Target.UNKNOWN));
		}
		return cache.get(type);
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
	public static CClassType get(String... types) {
		// First, we have to canonicalize this type union
		SortedSet<String> t = new TreeSet<>(Arrays.asList(types));
		String type = StringUtils.Join(t, "|");
		if (!cache.containsKey(type)) {
			cache.put(type, new CClassType(Target.UNKNOWN, t.toArray(new String[t.size()])));
		}
		return cache.get(type);
	}

	/**
	 * Returns the singular instance of CClassType that represents this type union. string|int and int|string are both
	 * considered the same type union, as they are first normalized into a canonical form.
	 *
	 * @param types
	 * @return
	 */
	public static CClassType get(CClassType... types) {
		List<String> stringTypes = new ArrayList<>();
		for (CClassType t : types) {
			// Could be a type union, so we need to break that out
			stringTypes.addAll(t.types);
		}

		return get(stringTypes.toArray(new String[stringTypes.size()]));
	}

	/**
	 * Creates a new CClassType
	 *
	 * @param type
	 * @param t
	 */
	private CClassType(String type, Target t) {
		super(type, ConstructType.CLASS_TYPE, t);
		isTypeUnion = false;
		types.add(type);
	}

	/**
	 * Creates a type union type.
	 *
	 * @param t
	 * @param types
	 */
	private CClassType(Target t, String... types) {
		super(StringUtils.Join(types, "|"), ConstructType.CLASS_TYPE, t);
		isTypeUnion = true;
		this.types.addAll(Arrays.asList(types));
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
	 * Returns true if this class extends the specified one
	 *
	 * @param superClass
	 * @return
	 */
	public boolean doesExtend(CClassType superClass) {
		return doesExtend(this, superClass);
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
	 * Returns a set of individual types for this type. If it is a class union, multiple types will be returned in the
	 * set. Each of the CClassTypes within this set are guaranteed to not be a type union.
	 *
	 * This might be ok to make public if necessary in the future.
	 *
	 * @return
	 */
	protected Set<CClassType> getTypes() {
		Set<CClassType> t = new HashSet<>();
		for (String type : types) {
			t.add(CClassType.get(type));
		}
		return t;
	}

	/**
	 * Returns true if checkClass extends, implements, or otherwise derives from superClass
	 *
	 * @param checkClass
	 * @param superClass
	 * @throws ClassNotFoundException If the specified class type cannot be found
	 * @return
	 */
	public static boolean doesExtend(CClassType checkClass, CClassType superClass) {
		if (checkClass.equals(superClass)) {
			// more efficient check
			return true;
		}
		for (CClassType tCheck : checkClass.getTypes()) {
			for (CClassType tSuper : superClass.getTypes()) {
				try {
					// TODO: This is currently being done in a very lazy way. It needs to be reworked.
					// For now, this is ok, but will not work once user types are added.
					Class cSuper = NativeTypeList.getNativeClass(tSuper.val());
					Class cCheck = NativeTypeList.getNativeClass(tCheck.val());
					if (!cSuper.isAssignableFrom(cCheck)) {
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

	@Override
	public String docs() {
		return "A ClassType is a value that represents an object type. This includes primitives or other value types.";
	}

	@Override
	public Version since() {
		return CHVersion.V3_3_1;
	}

}
