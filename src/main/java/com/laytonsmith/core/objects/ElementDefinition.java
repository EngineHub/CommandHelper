package com.laytonsmith.core.objects;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.UnqualifiedClassName;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Set;

/**
 * An ElementDefinition is the definition of elements within an object. These can be either properties or methods, as
 * they are handled the same in most ways, this superclass can safely represent both. For methods, the defaultValue
 * should always be provided, a Callable. There is special support for native types, in which a Method/Field is
 * provided.
 *
 * In general, an ElementDefinition is not useable, until it is converted into an Element, which is the same as the
 * definition, but includes a {@code definedIn} property. This is set and created when the field is actually associated
 * with a class definition, which happens later. A free floating element definition is otherwise useless, however.
 */
@typeof("ms.lang.ElementDefinition")
public abstract class ElementDefinition extends Construct {

	@SuppressWarnings("FieldNameHidesFieldInSuperclass")
	public static final CClassType TYPE = CClassType.get(ElementDefinition.class);

	private final AccessModifier accessModifier;
	private final Set<ElementModifier> elementModifiers;
	private CClassType type;
	private final UnqualifiedClassName unqualifiedType;
	private final String name;
	private final Target t;

	private final String signature;

	private final ParseTree tree;
	private java.lang.reflect.Method nativeMethod = null;
	private Field nativeField = null;

	/**
	 * Constructs a new element definition. If this is a native method or field,
	 * you must also call {@link #setNativeField(java.lang.reflect.Field)} or
	 * {@link #setNativeMethod(java.lang.reflect.Method)} immediately after construction.
	 * @param accessModifier The access modifier of the element
	 * @param elementModifiers The modifiers of the element
	 * @param type The type of the element (variable type for fields, return type
	 * for methods, java null for constructors)
	 * @param name The name of the element (should start with @ if this is a
	 * variable declaration).
	 * @param tree The default value, if this is a field, and null if this
	 * is a method. If the default value is MethodScript null, or is not set, you MUST
	 * send either {@link CNull#NULL} or {@link CNull#UNDEFINED}, rather than java
	 * null.
	 * @param signature The signature, which serves as the "toString" of this element.
	 * @param constructType The ConstructType
	 * @param t The code target where this element is defined in.
	 * @throws NullPointerException If one of the required fields is null
	 * @throws IllegalArgumentException If both defaultValue and method are non-null.
	 */
	public ElementDefinition(
			AccessModifier accessModifier,
			Set<ElementModifier> elementModifiers,
			UnqualifiedClassName type,
			String name,
			ParseTree tree,
			String signature,
			ConstructType constructType,
			Target t
	) {
		super(signature, constructType, t);
		Objects.requireNonNull(accessModifier);
		Objects.requireNonNull(elementModifiers);
		Objects.requireNonNull(name);

		this.signature = signature;
		this.accessModifier = accessModifier;
		this.elementModifiers = elementModifiers;
		this.unqualifiedType = type;
		this.name = name;
		this.tree = tree;
		this.t = t;
	}

	/**
	 * If the underlying class is a native class, the actual Method can be provided here. No
	 * checks are done at this point, but it MUST be true that the method return type is castable to {@link Mixed}, as
	 * well as all arguments.
	 * @param m
	 */
	public void setNativeMethod(java.lang.reflect.Method m) {
		this.nativeMethod = m;
	}

	/**
	 * If the underlying class is a native class, the actual Field can be provided here. No
	 * checks are done at this point, but it MUST be true that the field type is castable to {@link Mixed}.
	 * @param f
	 */
	public void setNativeField(Field f) {
		this.nativeField = f;
	}

	/**
	 * Returns true if the underlying item is a reference to a native class.
	 * @return
	 */
	public boolean isNative() {
		return this.isNativeMethod() || this.isNativeField();
	}

	/**
	 * Returns true if the underlying item is a reference to a native class method.
	 * @return
	 */
	public boolean isNativeMethod() {
		return this.nativeMethod != null;
	}

	/**
	 * Returns true if the underlying item is a reference to a native class field.
	 * @return
	 */
	public boolean isNativeField() {
		return this.nativeField != null;
	}

	/**
	 * The access modifier of the element.
	 * @return
	 */
	public AccessModifier getAccessModifier() {
		return accessModifier;
	}

	/**
	 * The element modifiers.
	 * @return
	 */
	public Set<ElementModifier> getElementModifiers() {
		return elementModifiers;
	}

	/**
	 * The type of the element. For methods, this is the return type.
	 * @return
	 */
	public CClassType getType() {
		if(type == null) {
			throw new Error("qualifyType must be called before getType can be used");
		}
		return type;
	}

	/**
	 * Returns the unqualified type of this object. This is never an error to call, unlike {@link #getType()}.
	 * @return
	 */
	public UnqualifiedClassName getUCN() {
		return unqualifiedType;
	}

	/**
	 * The name of the element.
	 * @return
	 */
	public String getElementName() {
		return name;
	}

	public Target getTarget() {
		return t;
	}

	/**
	 * The default value of the element. This will be {@link CNull#UNDEFINED} if this was a property of the class with
	 * no assignment at all, and {@link CNull#NULL} if it was defined as null. For methods, this will be the method
	 * code itself.
	 * <p>
	 * Because this is the prototype of the element, we can't simply define this as a Mixed, we need
	 * to evaluate the prototypical value when we instantiate the object. Therefore, the ParseTree is stored here. For
	 * atomic values, it's ok to just pull them out and use them, but for others, you must invoke the ParseTree to get
	 * the value.
	 * <p>
	 * Iff this is a native type, this will this return (java) null, though that fact should not be relied on,
	 * use {@link #isNative()} to determine that for sure.
	 * @return
	 */
	public ParseTree getTree() {
		return tree;
	}

	/**
	 * If this is a native class, and it represents a method, this should return a reference to the Java Method.
	 * It will return null if this is not a native class, but that
	 * fact should not be relied on, use {@link #isNative()} to determine that for sure.
	 * @return
	 */
	public java.lang.reflect.Method getNativeMethod() {
		return nativeMethod;
	}

	/**
	 * If this is a native class, and it represents a field, this should return a reference to the Java Field.
	 * It will return null if this is not a native class, but
	 * that fact should not be relied on, use {@link #isNative()} to determine that for sure.
	 * @return
	 */
	public Field getNativeField() {
		return nativeField;
	}

	@Override
	public boolean isDynamic() {
		return false;
	}

	@Override
	public CClassType[] getSuperclasses() {
		return new CClassType[]{Mixed.TYPE};
	}

	@Override
	public CClassType[] getInterfaces() {
		return CClassType.EMPTY_CLASS_ARRAY;
	}

	@Override
	public String docs() {
		return "An ElementDefinition is an intermediate step before creating an Element. This should not be used.";
	}

	@Override
	public Version since() {
		return MSVersion.V3_3_4;
	}

	public String getSignature() {
		return signature;
	}

	/**
	 * Creates a concrete type, based on this defintion, which requires the CClassType.
	 * @param definedIn
	 * @return
	 */
	public abstract Element createConcreteType(CClassType definedIn);

}
