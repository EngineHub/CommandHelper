package com.laytonsmith.core.objects;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.UnqualifiedClassName;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.Objects;
import java.util.Set;

/**
 *
 */
@typeof("ms.lang.Element")
public abstract class Element extends Construct {

	@SuppressWarnings("FieldNameHidesFieldInSuperclass")
	public static final CClassType TYPE = CClassType.get(Element.class);

	private final AccessModifier accessModifier;
	private final Set<ElementModifier> elementModifiers;
	private CClassType definedIn;
	private CClassType type;
	private final UnqualifiedClassName unqualifiedType;
	private final String name;
	private final Target t;

	private final ParseTree tree;
	private java.lang.reflect.Method nativeMethod = null;
	private java.lang.reflect.Field nativeField = null;

	/**
	 * Constructs a new element definition. If this is a native method or field,
	 * you must also call {@link #setNativeField(java.lang.reflect.Field)} or
	 * {@link #setNativeMethod(java.lang.reflect.Method)} immediately after construction.
	 * @param definition The element definition this instance is based on.
	 * @param definedIn The class that this element is defined in.
	 * @throws NullPointerException If one of the required fields is null.
	 * @throws IllegalArgumentException If both defaultValue and method are non-null.
	 */
	public Element(ElementDefinition definition, CClassType definedIn) {
		super(definition.getSignature(), definition.getCType(), definition.getTarget());
		Objects.requireNonNull(definition);
		Objects.requireNonNull(definedIn);

		this.accessModifier = definition.getAccessModifier();
		this.elementModifiers = definition.getElementModifiers();
		this.unqualifiedType = definition.getUCN();
		this.name = definition.getElementName();
		this.tree = definition.getTree();
		this.t = definition.getTarget();
		this.definedIn = definedIn;
	}

	/**
	 * Qualifies the type. Must be called before {@link #getType()} can be used.
	 * @param env
	 * @throws java.lang.ClassNotFoundException
	 */
	public void qualifyType(Environment env) throws ClassNotFoundException {
		this.type = CClassType.get(unqualifiedType.getFQCN(env));
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
	public void setNativeField(java.lang.reflect.Field f) {
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
	@Override
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

	public CClassType getDefinedIn() {
		if(definedIn == null) {
			throw new Error("qualifyType must be called before getDefinedIn can be used");
		}
		return definedIn;
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

	@Override
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
	public java.lang.reflect.Field getNativeField() {
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
		return "An Element is a value that is at the top level of a class, for instance, a field, or a method.";
	}

	@Override
	public Version since() {
		return MSVersion.V3_3_4;
	}


}
