<!
    strict;
    name: Hashable;
    author: cailin;
    created: 2019-04-08;
    description: The file containing the type ms.lang.Hashable;
    compilerOptions: UltraStrict;
>

/**
 * A Hashable class is one that knows how to provide a hash code for the object. In general, this is
 * mostly useful when dealing with collections, to implement a 
 */
public interface ms.lang.Hashable {
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
     * (even if it is running the same version of the program).
     *
     * When using the EqualsGenerator mechanism, hashCode is also provided, and one must only implement
     * this interface, but not implement the method, so long as the class is tagged with the
     * @{EqualsGenerator} annotation.
     */
    int hashCode();
}
