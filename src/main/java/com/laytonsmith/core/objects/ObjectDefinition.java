package com.laytonsmith.core.objects;

import com.laytonsmith.PureUtilities.ObjectHelpers;
import com.laytonsmith.PureUtilities.ObjectHelpers.Equals;
import com.laytonsmith.PureUtilities.ObjectHelpers.HashCode;
import com.laytonsmith.PureUtilities.ObjectHelpers.ToString;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.natives.interfaces.MAnnotation;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Contains the definition of an object. Within certain limits, this information is available to the runtime, and in
 * limited cases, can even be modified at runtime. For the large part, however, the data in an ObjectDefinition is
 * read only.
 *
 * Everything is an object at the core, but there are subtypes of Object that have special handling, such as enums or
 * annotations, but those are non-the-less represented in this class.
 */
@HashCode
@Equals
public class ObjectDefinition {
	@ToString
	private final List<MAnnotation> annotations;
	@ToString
	private final AccessModifier accessModifier;
	@ToString
	private final Set<ObjectModifier> objectModifiers;
	@ToString
	private final ObjectType objectType;
	@ToString
	private final CClassType type;
	@ToString
	private final CClassType[] superclasses;
	@ToString
	private final CClassType[] interfaces;
	private final CClassType containingClass;
	private final Target definitionTarget;
	private final Map<String, List<ElementDefinition>> properties;

	public ObjectDefinition(AccessModifier accessModifier, Set<ObjectModifier> objectModifiers, ObjectType objectType,
			CClassType type,
			CClassType[] superclasses, CClassType[] interfaces, CClassType containingClass, Target t,
			Map<String, List<ElementDefinition>> properties, List<MAnnotation> annotations) {
		this.accessModifier = accessModifier;
		this.objectModifiers = objectModifiers;
		this.objectType = objectType;
		this.type = type;
		this.superclasses = superclasses;
		this.interfaces = interfaces;
		this.containingClass = containingClass;
		this.definitionTarget = t;
		this.properties = properties;
		this.annotations = annotations;
	}

//	@SuppressWarnings("LocalVariableHidesMemberVariable")
//	public ObjectDefinition(FullyQualifiedClassName fqcn) throws ClassNotFoundException {
//		// TODO: This is bad, because the object definition should be based on the
//		// code in the methodscript folder, not based on the classes defined in java.
//		// The classes definitions should be loaded based on those, and for native classes,
//		// the appropriate Java class should be loaded, not the other way around.
//		Mixed m = NativeTypeList.getInvalidInstanceForUse(fqcn);
//		AccessModifier accessModifier = m.getAccessModifier();
//		Set<ObjectModifier> objectModifiers = m.getObjectModifiers();
//		ObjectType objectType = m.getObjectType();
//		CClassType type = m.typeof();
//		CClassType[] superclasses = m.getSuperclasses();
//		CClassType[] interfaces = m.getInterfaces();
//		CClassType containingClass = m.getContainingClass();
//		Target t = new Target(0, new File("/Natives:/" + type.getFQCN().getFQCN().replace(".", "/") + ".ms"), 0);
//		List<MAnnotation> annotations = null; // m.getAnnotations();
//		this.accessModifier = accessModifier;
//		this.objectModifiers = objectModifiers;
//		this.objectType = objectType;
//		this.type = type;
//		this.superclasses = superclasses;
//		this.interfaces = interfaces;
//		this.containingClass = containingClass;
//		this.definitionTarget = t;
//		// For now, we just load the methods based on the @ExposedProperty annotation. But in general, in the
//		// future, the native methods will be compiled from actual MethodScript code, and only the methods that
//		// are defined in the code as native will be loaded from the actual Java code. However, getting to that
//		// step requires implementing the compiler mechanisms for reading in class definitions, which will come
//		// later. This code will be mostly re-useable anyhow, because native methods will still need to be defined
//		// and loaded in the same way, it's just that they might be accompanied by code that was defined purely
//		// in MethodScript.
//		// TODO: define this
//		this.properties = new HashMap<>();
//		this.annotations = annotations;
//	}

	public String getClassName() {
		return this.type.getFQCN().getFQCN();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj == null) {
			return false;
		}
		if(getClass() != obj.getClass()) {
			return false;
		}
		final ObjectDefinition other = (ObjectDefinition) obj;
		if(!Objects.equals(this.type, other.type)) {
			return false;
		}
		return true;
	}

	public boolean exactlyEquals(Object obj) {
		return ObjectHelpers.DoEquals(this, obj);
	}

	@Override
	public int hashCode() {
		return ObjectHelpers.DoHashCode(this);
	}

	@Override
	public String toString() {
		return ObjectHelpers.DoToString(this);
	}

	/**
	 * Gets the access modifier associated with this class
	 * @return
	 */
	public AccessModifier getAccessModifier() {
		return accessModifier;
	}

	/**
	 * Gets the object modifiers associated with this class
	 * @return
	 */
	public Set<ObjectModifier> getObjectModifiers() {
		return EnumSet.copyOf(objectModifiers);
	}

	/**
	 * Gets the type of this object, i.e. if it is a class or an enum, etc.
	 * @return
	 */
	public ObjectType getObjectType() {
		return objectType;
	}

	/**
	 * Returns the type of the class
	 * @return
	 */
	public CClassType getType() {
		return type;
	}

	/**
	 * Returns the name of the class. This is exactly equivalent to {@code getType().getFQCN().getFQCN()}.
	 * @return
	 */
	public String getName() {
		return type.getFQCN().getFQCN();
	}

	/**
	 * Returns a List of superclasses.
	 * @return
	 */
	public List<CClassType> getSuperclasses() {
		return Arrays.asList(superclasses);
	}

	/**
	 * Returns a list of implementing interfaces.
	 * @return
	 */
	public List<CClassType> getInterfaces() {
		return Arrays.asList(interfaces);
	}

	/**
	 * Returns the ClassType that contains this class. If this is not an inner class, this value will be null.
	 * @return
	 */
	public CClassType getContainingClass() {
		return containingClass;
	}

	/**
	 * Returns the code target where the class was originally defined. Native classes have this value set, but it is a
	 * synthesized value, with line/column set to 0, and the file set to a fake file name. For instance
	 * "/Natives:/ms/lang/string.ms"
	 * @return
	 */
	public Target getDefinitionTarget() {
		return definitionTarget;
	}

	public Map<String, List<ElementDefinition>> getElements() {
		return properties;
	}

	public List<MAnnotation> getAnnotations() {
		return annotations;
	}

}
