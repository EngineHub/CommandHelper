package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.CHVersion;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
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

	private final boolean isTypeUnion;

	private final SortedSet<String> types = new TreeSet<>(new Comparator<String>(){

		@Override
		public int compare(String o1, String o2) {
			return o1.compareTo(o2);
		}
	});

	/**
	 * Creates a new CClassType
	 * @param type
	 * @param t
	 */
	public CClassType(String type, Target t) {
		super(type, ConstructType.CLASS_TYPE, t);
		isTypeUnion = false;
		types.add(type);
	}

	/**
	 * Creates a type union type.
	 * @param t
	 * @param types
	 */
	public CClassType(Target t, String ... types){
		super(StringUtils.Join(types, "|"), ConstructType.CLASS_TYPE, t);
		isTypeUnion = true;
		this.types.addAll(Arrays.asList(types));
	}


	@Override
	public boolean isDynamic() {
		return false;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof CClassType){
			return this.types.equals(((CClassType)obj).types);
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
	 * @return
	 */
	public boolean isTypeUnion(){
		return this.isTypeUnion;
	}

	/**
	 * Returns true if this class extends the specified one
	 * @param superClass
	 * @return
	 * @throws ClassNotFoundException
	 */
	public boolean doesExtend(CClassType superClass) throws ClassNotFoundException{
		return doesExtend(this, superClass);
	}

	/**
	 * Performs an unsafe check to see if this class extends the specified one
	 * @param superClass
	 * @return
	 */
	public boolean unsafeDoesExtend(CClassType superClass) {
		return unsafeDoesExtend(this, superClass);
	}

	/**
	 * Returns true if the specified class extends this one
	 * @param checkClass
	 * @return
	 * @throws ClassNotFoundException
	 */
	public boolean isExtendedBy(CClassType checkClass) throws ClassNotFoundException{
		return doesExtend(checkClass, this);
	}

	/**
	 * Performs an unsafe check to see if the specified class extends this one
	 * @param checkClass
	 * @return
	 */
	public boolean unsafeIsExtendedBy(CClassType checkClass) {
		return unsafeDoesExtend(checkClass, this);
	}

	/**
	 * Returns a set of individual types for this type. If it is a class union, multiple types will be returned in the set.
	 * Each of the CClassTypes within this set are guaranteed to not be a type union.
	 *
	 * This might be ok to make public if necessary in the future.
	 * @return
	 */
	protected Set<CClassType> getTypes(){
		Set<CClassType> t = new HashSet<>();
		for(String type : types){
			t.add(new CClassType(type, getTarget()));
		}
		return t;
	}

	/**
	 * Returns true if checkClass extends, implements, or otherwise derives from superClass
	 * @param checkClass
	 * @param superClass
	 * @throws ClassNotFoundException If the specified class type cannot be found
	 * @return
	 */
	public static boolean doesExtend(CClassType checkClass, CClassType superClass) throws ClassNotFoundException{
		if(checkClass.equals(superClass)){
			// more efficient check
			return true;
		}
		for(CClassType tCheck : checkClass.getTypes()){
			for(CClassType tSuper : superClass.getTypes()){
				Class cSuper = NativeTypeList.getNativeClass(tSuper.val());
				Class cCheck = NativeTypeList.getNativeClass(tCheck.val());
				if(!cSuper.isAssignableFrom(cCheck)){
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Works like {@link #doesExtend(com.laytonsmith.core.constructs.CClassType, com.laytonsmith.core.constructs.CClassType) }, however
	 * rethrows the {@link ClassNotFoundException} that doesExtend throws as an {@link Error}. This should not be used unless the
	 * class names come from hardcoded values.
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

	@Override
	public String docs() {
		return "A ClassType is a value that represents an object type. This includes primitives or other value types.";
	}

	@Override
	public Version since() {
		return CHVersion.V3_3_1;
	}



}
