<!
    strict;
    name: Comparable;
    author: cailin;
    created: 2019-04-08;
    description: The file containing the type ms.lang.Comparable;
    compilerOptions: UltraStrict;
>

/**
 * A class that is Comparable can be compared to another class using the standard method access, or it can be compared
 * using the traditional operators, < and >. One may note that only lessThan and greaterThan are defined here, 
 * and ==, <= and >=
 * are missing. Those three operations are a function of equals and < and >, and are therefore not provided here. In
 * order to support the full range of comparison operators, <, >, <=, >=, and ==, you must implement this interface
 * and the equals method defined in mixed (which all classes already implement).
 *
 * @param T Generally speaking, this should be {@code self}, however, this is not strictly required. In case it is
 * not, however, then the equals method should generally support whatever this type is as well.
 */
public interface ms.lang.Comparable<T> {

    /**
     * If this returns true, that indicates that this object is less than (and not equal to) the passed in object.
     * If these were numbers, and @this were 1, and @obj were 2, this should return true. If @obj were 1, this should
     * return false. If @obj were 0, this should return false.
     * 
     * @param obj The parameter to compare against
     */
    boolean lessThan(T @obj);

    /**
     * If this returns true, that indicates that this object is greater than (and not equal to) the passed in object.
     * If these were numbers, and @this were 1, and @obj were 2, this should return false. If @obj were 1, this should
     * return false. If @obj were 0, this should return true.
     */
    boolean greaterThan(T @obj);
}
