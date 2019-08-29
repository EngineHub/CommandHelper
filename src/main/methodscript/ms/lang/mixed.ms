<!
	strict;
	name: mixed.ms;
	author: cailin;
	created: 2019-04-08;
	description: The file containing the type ms.lang.mixed;
	compilerOptions: UltraStrict;
>

/**
 * mixed is the base class for all subclasses. Whether or not a defined class expressely extends
 * this class, it is none-the-less an extension of this class. Due to that, all methods in this
 * class are available to all objects.
 *
 * Despite being the base class, mixed is abstract; it cannot be instantiated directly.
 */
public native abstract class ms.lang.mixed {

	// TODO: Consider if this should just do a shallow field comparison, instead of doing reference checks?
	// This is probably the default use case anyways, and use of ref_equals is should probably be fairly rare.
	// On the other hand, this is so ubiquitous, maybe it would be confusing to do something else? Additionally,
	// doing that would be much more expensive than reference checking, so the default would be not very

	/**
	 * Returns true if this object is equal to the object being passed in. This method is what is called
	 * when the == operator is used (as well as ===, after the type checking is done as well, which is done
	 * automatically by the system), and can
	 * be overridden by subclasses to change the default behavior. For two objects, it should always be
	 * true that two objects that are equal also have the same hashCode, but the inverse is not
	 * necessarily true.
	 *
	 * There are a few rules that implementations of this method must follow, to provide a correct
	 * implementation:
	 *
	 * - The operation must be reflexive, i.e. x.equals(x) should always be true.
	 * - The operation should be symmetrical, if x.equals(y), then x.equals(y) should also return true.
	 * - The operation should be transitive, if x.equals(y), and y.equals(z), then z.equals(x) should return true.
	 * - The operation should be consistent, so long as the underlying data does not change, that is, multiple
	 * invocations should return the same value. For immutable classes, that means it should always return the same
	 * value.
	 * - The operation should always return false if the input value is null.
	 * - If the input type is not of the same class type (or a subclass), it should always return false,
	 * except in the case where the input value is cross-castable to this class. In that case, the class
	 * should be cross cast, then equality should be considered.
	 *
	 * If the method is not overridden by the subclass, then the default method is inherited, which just
	 * calls {@function ref_equals} on @this and the input object. For value types, this will return true
	 * if the values are the same.
	 *
	 * It is important to note the contract for hashCode states that if two objects are equal,
	 * then they should return the same hash code. Therefore, it is important that if this method is changed,
	 * so too should the hashCode method.
	 *
	 * Since implementation of this method is often the same for all classes, and is relatively difficult
	 * to implement properly, a shortcut is available
	 * to ease the implementation, and remain within these contracts.
	 * Simply tag the class with the @{EqualsGenerator} annotation, and then
	 * tag any fields within the class that should be considered when calculating equality with the @{Equals}
	 * annotation. The method is then generated for you, (along with the hashCode method) with all the complexities
	 * considered. There is no runtime penalty for this, the method is generated at compile time.
	 */
	@{BinaryOperatorOverload("==")}
	public boolean equals(mixed @obj) {
		if(@this instanceof ValueType) {
			return(equals(@this, @obj));
		} else {
			return(ref_equals(@this, @obj));
		}
	}

	/**
	 * Returns a hash code value for this object. The hash code value is simply an integer that
	 * represents this object distinctly. This method is used often in various ways, including to
	 * speed up performance of various utility classes such as a HashMap. In general, this method
	 * should return the same value for objects that are equal, and different values for objects that
	 * are false, however, this is not part of the contract of hashCode. The only contract is that
	 * for a given object, if equals returns true, then hashCode MUST return the same value for those
	 * two objects. For unequal objects, however, it is recommended, but not required that the method
	 * return different values for each object. When invoked on the same object across time, however,
	 * it should return the same value, assuming the underlying fields that are part of the equals
	 * calculation have not changed. It is not necessary or expected that in different runs of the
	 * program, the same hash code is returned for values that would otherwise be equal. Therefore,
	 * the hash code should not be stored with the object in anything other than memory, nor can
	 * the hash code be compared against another hash code of the same object from another process
	 * (even if it is running the same version of the program). Subclasses that override this should
	 * never advertise that a particular hash code will be returned for a particular value, as that
	 * is not part of the contract of hash code, and should not be implied to be part of it.
	 *
	 * When using the EqualsGenerator mechanism, hashCode is also provided, and one must only implement
	 * this interface, but not implement the method, so long as the class is tagged with the
	 * @{EqualsGenerator} annotation.
	 *
	 * The default implementation of this simply returns 0, which is extremely inefficient, but
	 * meets the base requirements of a hashCode implementation, therefore preventing hard-to-detect bugs.
	 * You will instead find easy-to-spot performance degredation.
	 * Given how inefficient it is, if it is possible that your object is
	 * used as part of an object that needs to be added to, for instance, a HashMap, it is highly
	 * recommended to provide a better implementation. The method generated from EqualsGenerator is
	 * a much better implementation, but requires a bit of instrumentation in your code, so it can't
	 * be done fully automatically.
	 *
	 * If implementing this without the EqualsGenerator, the generally accepted best approach to
	 * implementing a good hashCode method is taken from Josh Bloch's *Effective Java*.
	 * Two random, large, prime numbers are picked, a, and b. The hash is set to
	 * a, then, for each field that is of signifigance (that is, it is used in the calculation of equals),
	 * the hash is multiplied by b, and then the hash code
	 * of the field is added to it (0 is used if the field is null).
	 * This process is repeated, until all fields are considered. If the class is immutable (either in fact
	 * because of the immutable keyword, or in essense) it may be worth caching the return value,
	 * so it does not have to be recalculated each time.
	 */
	public int hashCode() {
		return(0);
	}

	/**
	 * Returns a human readable version of the object. By default, this is fairly useless, as it is just
	 * the class name and hash code (which, if not overloaded, is just 0).
	 * It is strongly encouraged for subclasses to override this method
	 * and provide a more friendly output. Calling toString on an object is different than cross casting
	 * to a string, though the results for a particular object may be the same. In general, there is no
	 * contract that an object be able to be reproduced from its toString output, but when cross casting
	 * to a string, that is the assumption.
	 *
	 * As a general rule, the toString method should return all interesting information about an object,
	 * though this can be undesirable for objects that have a large number of fields.
	 */
	public string toString() {
		return(getClass() . "@" . hashCode()->toHexString());
	}

	/**
	 * Returns the runtime type of the current object. This will always be the class of this object,
	 * or some subclass. The generic type of the return value is defined as ?, but the actual return
	 * type will be of type ? extends self. This is equivalent to calling typeof on the value.
	 */
	public final ClassType<?> getClassType() {
		return(typeof(@this));
	}

	// One might notice the distinct lack of some methods that are typically present in other Java inspired languages,
	// particularly notify/wait. notify/wait is moved to its own
	// Lock object, as it seems silly to make every object a monitor, when such functionality is only convenient
	// to certain subclasses. However,
	// there are other ways around that, and I am intentionally choosing to provide the absolute bare minimum
	// default methods, since literally every single class would otherwise inherit them. The finalize method is
	// also missing. This will be implemented in the same way as
	// https://docs.oracle.com/javase/9/docs/api/java/lang/ref/Cleaner.html and
	// https://docs.oracle.com/javase/9/docs/api/java/lang/ref/PhantomReference.html.
	// hashCode *is* defined here. At first glance, one might think it's easy enough to move it to
	// a Hashable interface or something, but then you have to duplicate all container type objects.
	// For instance, assume you have class Pair<T, V>. This cannot implement Hashable, because it would not be
	// possible to provide a good implementation if T or V doesn't implement Hashable. In that case, you could
	// create class HashablePair<T extends Hashable, V extends Hashable> implements Hashable, but then you have
	// to maintain two of all classes that are are container type classes. Further, all members of a class
	// would also have to be hashable for that class to be able to implement it as well. Implementing this method
	// correctly is difficult, however, which is why, out of the box, the EqualsGenerator will also generate that
	// method as well.
}
