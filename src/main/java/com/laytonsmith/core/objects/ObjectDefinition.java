package com.laytonsmith.core.objects;

import com.laytonsmith.PureUtilities.ObjectHelpers;
import com.laytonsmith.PureUtilities.ObjectHelpers.Equals;
import com.laytonsmith.PureUtilities.ObjectHelpers.HashCode;
import com.laytonsmith.PureUtilities.ObjectHelpers.ToString;
import com.laytonsmith.PureUtilities.SmartComment;
import com.laytonsmith.core.FullyQualifiedClassName;
import com.laytonsmith.core.UnqualifiedClassName;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.NativeTypeList;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.natives.interfaces.Commentable;
import com.laytonsmith.core.natives.interfaces.MAnnotation;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
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
 * annotations, but those are none-the-less represented in this class.
 */
@HashCode
@Equals
public class ObjectDefinition implements Commentable {
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
	private final Set<UnqualifiedClassName> superclasses;
	@ToString
	private final Set<UnqualifiedClassName> interfaces;
	private final CClassType containingClass;
	private final Target definitionTarget;
	private final List<Element> properties;
	private final SmartComment classComment;
	private final List<Object> genericParameters;
	private final Class<? extends Mixed> nativeClass;

	private Set<CClassType> qualifiedSuperclasses;
	private Set<CClassType> qualifiedInterfaces;

	public ObjectDefinition(AccessModifier accessModifier, Set<ObjectModifier> objectModifiers, ObjectType objectType,
			CClassType type,
			Set<UnqualifiedClassName> superclasses, Set<UnqualifiedClassName> interfaces,
			CClassType containingClass, Target t,
			List<ElementDefinition> properties, List<MAnnotation> annotations,
			SmartComment classComment, List<Object> genericParameters, Class<? extends Mixed> nativeClass) {
		this.accessModifier = accessModifier;
		this.objectModifiers = objectModifiers;
		this.objectType = objectType;
		this.type = type;
		this.superclasses = superclasses;
		this.interfaces = interfaces;
		this.containingClass = containingClass;
		this.definitionTarget = t;
		this.properties = new ArrayList<>();
		for(ElementDefinition d : properties) {
			this.properties.add(d.createConcreteType(type));
		}
		this.annotations = annotations;
		this.classComment = classComment;
		this.genericParameters = genericParameters;
		this.nativeClass = nativeClass;
	}

	/**
	 * Returns the class name
	 * @return
	 */
	public String getClassName() {
		return getName();
	}

	/**
	 * Returns the FQCN of this object.
	 * @return
	 */
	public FullyQualifiedClassName getFQCN() {
		return this.type.getFQCN();
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

	private volatile boolean classesQualified = false;
	/**
	 * Qualifies the unqualified class names used internally, and allows {@link #getSuperclasses()} and
	 * {@link #getInterfaces()} to be used. If this method is not called first, those methods will throw an Error.
	 * Calling this method more than once does nothing, but is not an error. This normally should be done as part of
	 * the compilation process.
	 * @param env
	 * @throws ConfigCompileGroupException If one or more of the classes couldn't be found.
	 */
	public void qualifyClasses(Environment env) throws ConfigCompileGroupException {
		if(classesQualified) {
			return;
		}
		synchronized(this) {
			if(classesQualified) {
				return;
			}
			Set<ConfigCompileException> uhohs = new HashSet<>();
			@SuppressWarnings("LocalVariableHidesMemberVariable")
			Set<CClassType> superclasses = new HashSet<>();
			@SuppressWarnings("LocalVariableHidesMemberVariable")
			Set<CClassType> interfaces = new HashSet<>();
			for(UnqualifiedClassName ucn : this.superclasses) {
				try {
					superclasses.add(CClassType.get(ucn.getFQCN(env)));
				} catch (ClassNotFoundException ex) {
					uhohs.add(new ConfigCompileException("Could not find " + ucn.getUnqualifiedClassName(),
							ucn.getTarget(), ex));
				}
			}
			for(UnqualifiedClassName ucn : this.interfaces) {
				try {
					interfaces.add(CClassType.get(ucn.getFQCN(env)));
				} catch (ClassNotFoundException ex) {
					uhohs.add(new ConfigCompileException("Could not find " + ucn.getUnqualifiedClassName(),
							ucn.getTarget(), ex));
				}
			}
			for(Element element : properties) {
				try {
					element.qualifyType(env);
				} catch (ClassNotFoundException ex) {
					uhohs.add(new ConfigCompileException("Could not find " + element.getUCN(),
							element.getTarget(), ex));
				}
			}
			if(!uhohs.isEmpty()) {
				throw new ConfigCompileGroupException(uhohs);
			}
			this.qualifiedSuperclasses = superclasses;
			this.qualifiedInterfaces = interfaces;
			classesQualified = true;
		}
	}

	/**
	 * Returns a List of superclasses.
	 * @return
	 */
	public Set<CClassType> getSuperclasses() {
		if(qualifiedSuperclasses == null) {
			throw new Error("qualifyClasses() must be called before getSuperclasses can be used (" + getType() + ")");
		}
		return new HashSet<>(qualifiedSuperclasses);
	}

	/**
	 * Returns a list of implementing interfaces.
	 * @return
	 */
	public Set<CClassType> getInterfaces() {
		if(qualifiedSuperclasses == null) {
			throw new Error("qualifyClasses() must be called before getInterfaces can be used (" + getType() + ")");
		}
		return new HashSet<>(qualifiedInterfaces);
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

	public List<Element> getElements() {
		return properties;
	}

	public List<MAnnotation> getAnnotations() {
		return annotations;
	}

	@Override
	public SmartComment getElementComment() {
		return classComment;
	}

	public List<Object> getGenericParameters() {
		return genericParameters;
	}

	/**
	 * Checks if this is a native class, and can be properly cast to an actual native java class. If this
	 * returns true, then calling one of the methods in {@link NativeTypeList} will most certainly succeed.
	 * @return True if this is a native class, false otherwise.
	 */
	public boolean isNative() {
		if(!getObjectModifiers().contains(ObjectModifier.NATIVE)) {
			// If it doesn't claim to be native, it certainly isn't.
			return false;
		}
		try {
			// We want to ensure that if we attempt to instantiate this
			// through the native type list, it will succeed, so we actually
			// do that test here.
			NativeTypeList.getNativeClassOrInterfaceRunner(type.getFQCN());
			return true;
		} catch (ClassNotFoundException ex) {
			return false;
		}
	}

}
