<!
	strict;
	name: Comparable.ms;
	author: cailin;
	created: 2019-04-08;
	description: The file containing the type ms.lang.Comparable;
	compilerOptions: UltraStrict;
>

/**
 * A class that is Comparable can be compared to another class using the standard method access, or it can be compared
 * using the traditional operators, < and >. One may note that only lessThan and greaterThan are required
 * implementations here, <= and >= are implemented in terms of equals and lessThan/greaterThan, and equals is defined
 * by mixed, and so is missing. It is possible, however, to overload <= or >= if a more efficient approach is
 * possible.
 *
 * @param T Generally speaking, this should be {@code self}, however, this is not strictly required. In case it is
 * not, however, then the equals method must support whatever this type is as well.
 */
public interface ms.lang.Comparable<T> {

	/**
	 * If this returns true, that indicates that this object is less than (and not equal to) the passed in object.
	 * If these were numbers, and @this were 1, and @obj were 2, this should return true. If @obj were 1, this should
	 * return false. If @obj were 0, this should return false.
	 *
	 * @param obj The parameter to compare against
	 */
	@{BinaryOperatorOverload("<")}
	boolean lessThan(T @obj);

	/**
	 * If this returns true, that indicates that this object is greater than (and not equal to) the passed in object.
	 * If these were numbers, and @this were 1, and @obj were 2, this should return false. If @obj were 1, this should
	 * return false. If @obj were 0, this should return true.
	 *
	 * @param obj The parameter to compare against
	 */
	@{BinaryOperatorOverload(">")}
	boolean greaterThan(T @obj);

	/**
	 * Equality is checked on the value, and if it isn't equal, {@link lessThan} is called, and the results of that
	 * are returned.
	 *
	 * @param obj The parameter to compare against
	 */
	@{BinaryOperatorOverload("<=")}
	boolean lessThanOrEqualTo(T @obj) {
		if(@this->equals(@obj)) {
			return(true);
		}
		return(@this->lessThan(@obj));
	}

	/**
	 * Equality is checked on the value, and if it isn't equal, {@link greaterThan} is called, and the results of that
	 * are returned.
	 *
	 * @param obj The parameter to compare against
	 */
	@{BinaryOperatorOverload(">=")}
	boolean greaterThanOrEqualTo(T @obj) {
		if(@this->equals(@obj)) {
			return(true);
		}
		return(@this->greaterThan(@obj));
	}


}
