package com.laytonsmith.core.objects;

import com.laytonsmith.annotations.MEnum;

/**
 *
 * @author cailin
 */
@MEnum("ms.lang.ObjectType")
public enum ObjectType {
	// Maybe consider making this an extendable object type so extensions can add more types of objects?
	// Probably not, that's fairly fundamental, but it may be worth thinking about.
	/**
	 * A class is defined with the {@code class} keyword and represents a class that is instantiatable (in general) and
	 * can (in general) be extended, with the subclass inheriting this class's methods.
	 */
	CLASS(true, true, true, true),
	/**
	 * An abstract class is defined with the {@code abstract class} keywords, and represents a class that can be
	 * extended, with the subclass inheriting this class's methods. It cannot be directly instantiated, however
	 */
	ABSTRACT(false, true, true, true),
	/**
	 * An interface is defined with the {@code interface} keyword, and represents an interface. Subclasses can implement
	 * this interface, which makes them able to be grouped together with other interfaces of the same type. No methods
	 * may be implemented in the class, it merely defines the methods that other classes should implement.
	 */
	INTERFACE(false, false, false, true),
	/**
	 * An annotation is a supplementary tag that can be tagged onto various other elements, to provide meta information
	 * about that class.
	 */
	ANNOTATION(false, false, false, true),
	/**
	 * An enum class is one that is a list of values. The values themselves are instances of this enum, which can
	 * otherwise function like a final class, and provide custom methods within the class.
	 */
	ENUM(false, true, true, true),
	/**
	 * A mask is a class that is similar to an enum, but is required to have an ordinal attached to it. It is limited to
	 * 64 elements in the mask, and each element is required to return a value that is 2*n where n is the ordinal place.
	 * This way, a single mask value can contain multiple enum values within.
	 */
	MASK(false, true, true, true);

	private final boolean isInstantiatable;
	private final boolean extendsMixed;
	private final boolean canUseExtends;
	private final boolean canUseImplements;

	private ObjectType(boolean isInstantiatable, boolean extendsMixed, boolean canUseExtends,
			boolean canUseImplements) {
		this.isInstantiatable = isInstantiatable;
		this.extendsMixed = extendsMixed;
		this.canUseExtends = canUseExtends;
		this.canUseImplements = canUseImplements;
	}


	/**
	 * Returns true if this type can be instantiated. That is, if it is compatible with the {@code new} keyword.
	 * @return
	 */
	public boolean isInstantiatable() {
		return this.isInstantiatable;
	}

	/**
	 * Returns true if this object type inherently extends mixed. For instance, interfaces do not, but classes do.
	 * @return
	 */
	public boolean extendsMixed() {
		return this.extendsMixed;
	}

	/**
	 * Returns true if this object type can use the extends keyword.
	 * @return
	 */
	public boolean canUseExtends() {
		return this.canUseExtends;
	}

	/**
	 * Returns true if this object type can use the implements keyword.
	 * @return
	 */
	public boolean canUseImplements() {
		return this.canUseImplements;
	}

}
