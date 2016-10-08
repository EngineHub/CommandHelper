package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 *
 */
@typeof("ClassType")
public class CClassType extends Construct {

    public static final CClassType MIXED = new CClassType("mixed", Target.UNKNOWN);
    public static final CClassType AUTO = new CClassType("auto", Target.UNKNOWN);
    public static final CClassType VOID = new CClassType("void", Target.UNKNOWN);
    public static final CClassType NULL = new CClassType("null", Target.UNKNOWN);

    private final SortedSet<String> types = new TreeSet<>(new Comparator<String>() {

	@Override
	public int compare(String o1, String o2) {
	    return o1.compareTo(o2);
	}
    });

    /**
     * Convenience method to generate a new CClassType object. The target is Target.UNKNOWN. This should
     * generally only be used by getSuperclasses and getInterfaces.
     *
     * @param types
     * @return
     */
    public static CClassType build(String ... types) {
	return new CClassType(Target.UNKNOWN, types);
    }

    /**
     * Creates a new CClassType
     *
     * @param type
     * @param t
     */
    public CClassType(String type, Target t) {
	super(type, ConstructType.CLASS_TYPE, t);
	types.add(type);
    }

    /**
     * Creates a type union type.
     *
     * @param t
     * @param types
     */
    public CClassType(Target t, String... types) {
	super(StringUtils.Join(types, "|"), ConstructType.CLASS_TYPE, t);
	this.types.addAll(Arrays.asList(types));
    }

    @Override
    public boolean isDynamic() {
	return false;
    }

    @Override
    public boolean equals(Object obj) {
	if (obj instanceof CClassType) {
	    return this.types.equals(((CClassType) obj).types);
	} else {
	    return false;
	}
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
	return this.types.size() > 1;
    }

    /**
     * Returns true if this class extends the specified one
     *
     * @param superClass
     * @return
     * @throws ClassNotFoundException
     */
    public boolean doesExtend(CClassType superClass) throws ClassNotFoundException {
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
	    t.add(new CClassType(type, getTarget()));
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
    public static boolean doesExtend(CClassType checkClass, CClassType superClass) throws ClassNotFoundException {
	if (checkClass.equals(superClass)) {
	    // more efficient check
	    return true;
	}
	for (CClassType tCheck : checkClass.getTypes()) {
	    for (CClassType tSuper : superClass.getTypes()) {
		Class cSuper = NativeTypeList.getNativeClass(tSuper.val());
		Class cCheck = NativeTypeList.getNativeClass(tCheck.val());
		if (!cSuper.isAssignableFrom(cCheck)) {
		    return false;
		}
	    }
	}
	return true;
    }

    /**
     * Works like {@link #doesExtend(com.laytonsmith.core.constructs.CClassType, com.laytonsmith.core.constructs.CClassType)
     * }, however rethrows the {@link ClassNotFoundException} that doesExtend throws as an {@link Error}. This should
     * not be used unless the class names come from hardcoded values, and is known for sure to exist.
     *
     * @param checkClass
     * @param superClass
     * @throws Error If the specified class type cannot be found
     * @return
     */
    public static boolean unsafeDoesExtend(CClassType checkClass, CClassType superClass) throws Error {
	try {
	    return doesExtend(checkClass, superClass);
	} catch (ClassNotFoundException ex) {
	    throw new Error(ex);
	}
    }

    /**
     * Returns the superclass for this object, or null if it is mixed, void, or null. If it is a type union,
     * the most common superclass is returned.
     *
     * @return
     */
    public CClassType[] getSuperClasses() {
	if (isTypeUnion()) {
	    return new CClassType[]{getMostCommonSuperClass()};
	} else {
	    String type = types.first();
	    if("void".equals(type) || "null".equals(type) || "mixed".equals(type)) {
		return null;
	    }
	    CClassType[] c = ReflectionUtils.instantiateUnsafe(getUnderlyingClass()).getSuperclasses();
	    return c;
	}
    }

    /**
     * Returns the underlying object type, or null if this represents a user defined class. If this is a type union, the
     * most common super class is returned.
     *
     * @return
     */
    public Class<? extends Mixed> getUnderlyingClass() {
	if (isTypeUnion()) {
	    return getMostCommonSuperClass().getUnderlyingClass();
	} else {
	    try {
		return NativeTypeList.getNativeClass(types.first());
	    } catch (ClassNotFoundException ex) {
		// This will happen for user defined types
		return null;
	    }
	}
    }

    /**
     * Returns a CClassType that represents the most common super class. If the class isn't a type union, it simply
     * returns a copy of this class.
     *
     * @return
     */
    public CClassType getMostCommonSuperClass() {
	if (!isTypeUnion()) {
	    return new CClassType(types.first(), getTarget());
	} else {
	    // I don't have time to finish this right now, so for now, just return mixed.
	    return CClassType.MIXED;
//	    // We must only return one object here. Consider the following tree:
//	    // A extends B, C; B extends M; C extends D, E; F extends M; E extends M;
//	    // And also:
//	    // G extends D, E; D extends F; E extends M; F extends M;
//	    // A few things to note: Everything extends M by proxy. Assume the type union
//	    // is for A | G. G is basically in the same place as C (they both extend D, E,
//	    // which of course means the rest of their tree is the exact same. One might
//	    // be tempted to simply say that the most common super type is D | E. While it
//	    // is valid to say that A | G := D | E (that is, A and G can both correctly be
//	    // cast to D or E) that doesn't help us as far as the rest of compiler is concerned.
//	    // We must always return precisely one type for this, because consider the case where
//	    // A | G = new B(); In this case, it would be incorrect to cast it to D or E, because B
//	    // only extends M, not D or E. So, yes, A | G has the most common super types of D and E, but
//	    // we must continue further, and find the most common super type of D | E. In this case, we find
//	    // that it is M. We are essentially saying, since A | G can contain any of the following
//	    // types: A, G, B, C, D, E, F, M, then we have to find the most common super type of all of
//	    // these. In this case, the only one that fits the bill is M. Thus, the most common
//	    // super type for A | G is M. A | G := M
//	    List<String> l = new ArrayList<>(types);
//	    // We know that there are at least 2 in this list, (otherwise it wouldn't be a type union in the
//	    // first place. So, the general algorithm is two parts. If there are more than 2 types in the union,
//	    // we find the most common superclass of the first two, then compare that to the third, then compare
//	    // that to the fourth, etc (returning mixed as soon as we find it, if we do find it). Within each
//	    // comparison, we do a n**2 comparison of the whole chain of the first type, comparing to the
//	    // full chain of the second type. Once we find a match, we set the "initial" value to that, then
//	    // move on to the next type, returning the "initial" value once we find it.
//	    CClassType initial = new CClassType(l.get(0), getTarget());
//	    l.remove(0);
//	    outer:
//	    while (true) {
//		if (l.isEmpty()) {
//		    return initial;
//		}
//		CClassType second = new CClassType(l.get(0), getTarget());
//		l.remove(0);
//		do {
//		    do {
//			if (initial.equals(second)) {
//			    continue outer;
//			}
//			second = second.getSuperClass();
//		    } while (!second.equals(MIXED));
//		    if (second.equals(MIXED)) {
//			return MIXED;
//		    }
//		    initial = initial.getSuperClass();
//		} while (!initial.equals(MIXED));
//		if (initial.equals(MIXED)) {
//		    return MIXED;
//		}
//		break;
//	    }
//	    return initial;
	}
    }

    @Override
    public String docs() {
	return "A ClassType is a value that represents an object type. This includes primitives or other value types.";
    }

    @Override
    public Version since() {
	return CHVersion.V3_3_1;
    }

    @Override
    public CClassType[] getSuperclasses() {
	return new CClassType[]{CClassType.build("mixed")};
    }

    @Override
    public CClassType[] getInterfaces() {
	return new CClassType[]{};
    }

}
