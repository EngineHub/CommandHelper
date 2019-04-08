<!
    strict;
    name: mixed;
    author: cailin;
    created: 2019-04-08;
    description: The file containing the type ms.lang.mixed;
    compilerOptions: UltraStrict;
>

/**
 * mixed is the base class for all subclasses. Whether or not a defined class expressely extends
 * this class, it is none-the-less an extension of this class. Due to that, all methods in this
 * class are available to all objects.
 */
public native class ms.lang.mixed {
	
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
     * calls {@function ref_equals} on @this and the input object.
     *
     * It is important to note the contract for hashCode, if a subclass implements {@link Hashable},
     * which states that if two objects are equal,
     * then they should return the same hash code. Therefore, it is important that if this method is overridden,
     * so too should the hashCode method.
     *
     * Since implementation of this method is often the same for all classes, and is relatively difficult
     * to implement properly, a shortcut is available
     * to ease the implementation, and remain within these contracts. 
     * Simply tag the class with the @{EqualsGenerator} annotation, and then
     * tag any fields within the class that should be considered when calculating equality with the @{Equals}
     * annotation. The method is then generated for you, (along with the hashCode method, if the class implements
     * Hashable) with all the complexities
     * considered. There is no runtime penalty for this, the method is generated at compile time.
     */
    public boolean equals(mixed @obj) {
        return(ref_equals(@this, @obj));
    }

    /**
     * Returns a human readable version of the object. By default, this is fairly useless, as it is just
     * the class name and hash code. It is strongly encouraged for subclasses to override this method
     * and provide a more friendly output. Calling toString on an object is different than cross casting
     * to a string, though the results for a particular object may be the same. In general, there is no
     * contract that an object be able to be reproduced from its toString output, but when cross casting
     * to a string, that is the assumption.
     */
    public string toString() {
        return(getClass() . "@" . hashCode()->toHex());
    }

    /**
     * Returns the runtime type of the current object. This will always be the class of this object,
     * or some subclass. The generic type of the return value is defined as ?, but the actual return
     * type will be of type ? extends self.
     */
    public final native ClassType<?> getClass();

    // One might notice the distinct lack of some methods that are typically present in other Java inspired languages,
    // particularly hashCode and notify/wait. hashCode is defined via Hashable, and notify/wait is moved to its own
    // Lock object, as it seems silly to make every object a monitor, when such functionality is only convenient
    // in an extremely few set of circumstances (I can only think of one offhand, iterating a collection). However,
    // there are other ways around that, and I am intentionally choosing to provide the absolute bare minimum
    // default methods, since literally every single class would otherwise inherit them. The finalize method is
    // also missing. This will be implemented in the same way as 
    // https://docs.oracle.com/javase/9/docs/api/java/lang/ref/Cleaner.html and
    // https://docs.oracle.com/javase/9/docs/api/java/lang/ref/PhantomReference.html.
}
