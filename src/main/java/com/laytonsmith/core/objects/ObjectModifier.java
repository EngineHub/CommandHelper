package com.laytonsmith.core.objects;

import com.laytonsmith.annotations.ExposedElement;
import com.laytonsmith.annotations.MEnum;

/**
 *
 * @author cailin
 */
@MEnum("ms.lang.ObjectModifier")
public enum ObjectModifier {
	/**
	 * A final class is one that cannot be extended by subclasses. Only a non-abstract class can use this keyword, as
	 * interfaces and abstract classes must be extended/implemented for its existence to make sense.
	 */
	FINAL,
	/**
	 * A static class is one that is not tied to the containing class's instance scope, but is tied to the static scope.
	 * This in not useable in top level classes, as it wouldn't make sense otherwise.
	 */
	STATIC,
	/**
	 * An abstract class is one that cannot be instantiated directly, only non-abstract subclasses can be. It is an
	 * error for a class to be both final and abstract in user code, though this restriction is not enforced for system
	 * level code (which is taken advantage of by enums, so that ms.lang.enum cannot be subclassed or instantiated).
	 */
	ABSTRACT,
	/**
	 * A native class maps to a class defined in the java code. This modifier is required on the class if any of the
	 * methods or fields are defined as native. And should generally be used for performance enhancement reasons if
	 * there does exist a native class. Additionally, the signatures of the methods and fields that are defined
	 * in this class must match the elements tagged with the {@link ExposedElement} annotations, or it will be an error.
	 */
	NATIVE,
	/**
	 * An immutable object is one in which none of the fields may be set, other than within the constructor.
	 */
	IMMUTABLE;
}
