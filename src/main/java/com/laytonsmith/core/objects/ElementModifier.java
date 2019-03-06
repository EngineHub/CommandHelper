package com.laytonsmith.core.objects;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

/**
 * These are the list of modifers that are valid on an Element (i.e. property or method).
 */
public enum ElementModifier {
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
	 * level code.
	 */
	ABSTRACT;
}
