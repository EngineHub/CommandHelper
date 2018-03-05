package com.laytonsmith.core.natives.interfaces;

/**
 *
 * @author cailin
 */
public enum ObjectType {
	/**
	 * A class is defined with the {@code class} keyword and represents a class that is instantiatable (in general) and
	 * can (in general) be extended, with the subclass inheriting this class's methods.
	 */
	CLASS,
	/**
	 * An abstract class is defined with the {@code abstract class} keywords, and represents a class that can be
	 * extended, with the subclass inheriting this class's methods. It cannot be directly instantiated, however
	 */
	ABSTRACT,
	/**
	 * An interface is defined with the {@code interface} keyword, and represents an interface. Subclasses can implement
	 * this interface, which makes them able to be grouped together with other interfaces of the same type. No methods
	 * may be implemented in the class, it merely defines the methods that other classes should implement.
	 */
	INTERFACE,
	/**
	 * An annotation is a supplementary tag that can be tagged onto various other elements, to provide meta information
	 * about that class.
	 */
	ANNOTATION,
	/**
	 * An enum class is one that is a list of values. The values themselves are instances of this enum, which can
	 * otherwise function like a final class, and provide custom methods within the class.
	 */
	ENUM,
	/**
	 * A mask is a class that is similar to an enum, but is required to have an ordinal attached to it. It is limited to
	 * 64 elements in the mask, and each element is required to return a value that is 2*n where n is the ordinal place.
	 * This way, a single mask value can contain multiple enum values within.
	 */
	MASK,

}
