package com.laytonsmith.core.natives.interfaces;

import com.laytonsmith.core.objects.ObjectType;
import com.laytonsmith.core.objects.ObjectModifier;
import com.laytonsmith.PureUtilities.Common.Annotations.ForceImplementation;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.Documentation;
import com.laytonsmith.core.SimpleDocumentation;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.objects.AccessModifier;
import java.util.Set;

/**
 * Mixed is the root type of all MethodScript objects and primitives.
 */
@typeof("ms.lang.mixed")
public interface Mixed extends Cloneable, Documentation {

	@SuppressWarnings("FieldNameHidesFieldInSuperclass")
	public static final CClassType TYPE = CClassType.get(Mixed.class);

	public String val();

	public void setTarget(Target target);

	public Target getTarget();

	public Mixed clone() throws CloneNotSupportedException;

	/**
	 * Overridden from {@link SimpleDocumentation}. This should just return the value of the typeof annotation,
	 * unconditionally.
	 *
	 * @return
	 */
	@Override
	public String getName();

	@Override
	@ForceImplementation
	public String docs();

	@Override
	@ForceImplementation
	public Version since();

	/**
	 * Returns a list of the classes that this *directly* extends. This is not always equivalent to the classes that the
	 * underlying java class extends. All classes must override this method, but if the class is a phantom class (that
	 * is, it implements Mixed, but does not have a typeof annotation) then it can simply throw an
	 * UnsupportedOperationException.
	 *
	 * For true interfaces (that is, classes that return {@link ObjectType#INTERFACE}, this means the values that this
	 * interface also extends.
	 *
	 * @return
	 */
	@ForceImplementation
	public CClassType[] getSuperclasses();

	/**
	 * Returns a list of the interfaces that this *directly* implements. This is not always equivalent to the interfaces
	 * that the underlying java class extends. All classes must override this method, but if the class is a phantom
	 * class (that is, it implements Mixed, but does not have a typeof annotation) then it can simply throw an
	 * UnsupportedOperationException.
	 *
	 * If this is an interface, this should return an empty array always.
	 *
	 * It's also important to note that for performance reasons, if an empty array is returned, the code should prefer
	 * to use {@link CClassType#EMPTY_CLASS_ARRAY}, and for core code, this is required by a unit test.
	 *
	 * @return
	 */
	@ForceImplementation
	public CClassType[] getInterfaces();

	/**
	 * Returns information about this class, whether it is a class, whether it is final, etc.
	 */
	public ObjectType getObjectType();

	/**
	 * Returns modification information about this class, i.e. if it is final
	 *
	 * @return
	 */
	public Set<ObjectModifier> getObjectModifiers();

	/**
	 * Gets the access level for this object, i.e. public, private...
	 * @return
	 */
	public AccessModifier getAccessModifier();

	/**
	 * Returns the containing class for this object. If null is returned, that means this is a top level class.
	 *
	 * @return
	 */
	public CClassType getContainingClass();

	/**
	 * Generally speaking, we cannot use Java's instanceof keyword to determine if something is an instanceof, because
	 * user classes do not extend the hierarchy of objects in MethodScript. Essentially, we need to extend Java's
	 * instanceof keyword, so in order to do that, we must compare objects with a custom method, rather than rely on
	 * Java's keyword.
	 *
	 * This method works with CClassTypes.
	 *
	 * Implementation note: The implementation of this should just be
	 * {@link Construct#isInstanceof(com.laytonsmith.core.natives.interfaces.Mixed,
	 * com.laytonsmith.core.constructs.CClassType)} which supports Mixed values.
	 *
	 * @param type
	 * @return
	 */
	public boolean isInstanceOf(CClassType type);

	/**
	 * Generally speaking, we cannot use Java's instanceof keyword to determine if something is an instanceof, because
	 * user classes do not extend the hierarchy of objects in MethodScript. Essentially, we need to extend Java's
	 * instanceof keyword, so in order to do that, we must compare objects with a custom method, rather than rely on
	 * Java's keyword.
	 *
	 * This method works with class type directly.
	 *
	 * Implementation note: The implementation of this should just be
	 * {@link Construct#isInstanceof(com.laytonsmith.core.natives.interfaces.Mixed, java.lang.Class)} which supports
	 * Mixed values.
	 *
	 * @param type
	 * @return
	 */
	public boolean isInstanceOf(Class<? extends Mixed> type);

	/**
	 * Returns the typeof this value, as a CClassType object. Not all constructs are annotated with the @typeof
	 * annotation, in which case this is considered a "private" object, which can't be directly accessed via
	 * MethodScript. In this case, an IllegalArgumentException is thrown.
	 *
	 * @return
	 * @throws IllegalArgumentException If the class isn't public facing.
	 */
	public CClassType typeof();

	/**
	 * Casts the class to the specified type. This only works with Java types, and so for dynamic elements, this
	 * may throw a RuntimeException. For dynamic types, use the other castTo.
	 *
	 * <p>For classes that are instanceof this class, (or vice versa) no logic is done, it is just cast, though if
	 * it can't be cast, it will throw a ClassCastException. For classes that are cross castable, they will be first
	 * cross casted.
	 *
	 * An important note, while it seems like you can just cast from the value using standard java cast notation,
	 * the object model is not 1:1, and so dynamic classes may logically extend the java class, but due to restrictions
	 * in java, that can't actually happen in java's object model, so we cannot rely on java's casting functionality
	 * either. Add to that that user types don't exist in the java anyways.
	 * @param <T>
	 * @param clazz
	 * @return
	 * @throws ClassCastException if the class can't be cast
	 */
	// The whole argument validation/Static.get* methods needs to be moved to this mechanism.
	//public <T extends Mixed> T castTo(Class<T> clazz) throws ClassCastException;

}
