package com.laytonsmith.core.natives.interfaces;

import com.laytonsmith.annotations.unsupported;

/**
 * Constructs wishing to overload various operators must implement one or more
 * of these sub interfaces.
 * In general, a construct should support all or none of the methods in each subclass, 
 * but each method may throw a ConfigRuntimeException (or better yet tag itself
 * with @{@link unsupported} and throw the exception) to indicate that just that
 * one particular operator is unsupported. 
 * 
 * <p>
 * Some operators are missing. The operators that have exactly defined behavior that
 * is a function of the other operators are not provided, both to keep implementation
 * complexity and design complexity down. Of note are the following:
 * </p>
 * 
 * <ul>
 *	<li>Pre/postfix increment/decrement - These have very precise usage with variables and memory
 *  usage, so they are unavailable to be altered.</li>
 *	<li>Boolean Logic - If the object can be cross cast to boolean, the logic is dealt with
 *  by doing the cross cast, getting the boolean value, and doing the comparison,
 *  but no method may alter the overall behavior of these operators.</li>
 * </ul>
 * 
 * All methods in the interface also have a "test" version, which is used when the value is unknown,
 * but the type is. This can be used to determine, at compile time, whether or not a value can support
 * that type, assuming it is not auto. All operators use the left hand value to determine the class
 * to use.
 */
public interface Operators {
	
	/**
	 * &gt;= and &lt;= are not provided in this class directly, but instead
	 * are implemented as a two step operation, check for equals, if true, then
	 * return true, otherwise check for greater than or less than, and return
	 * accordingly.
	 */
	public static interface Relational extends Equality {
		//TODO
		/**
		 * Returns true if this object is less than the right hand side (m).
		 * @param m
		 * @return 
		 */
		boolean operatorLessThan(Mixed m);
		
		/**
		 * Should return true if operatorLessThan will work with this class type.
		 * @param clazz
		 * @return 
		 */
		boolean operatorTestLessThan(Class<? extends Mixed> clazz);
		
		//TODO
		/**
		 * Returns true if this object is greater than the right hand side (m).
		 * @param m
		 * @return 
		 */
		boolean operatorGreaterThan(Mixed m);
		
		/**
		 * Should return true if operatorGreaterThan will work with this class type.
		 * @param clazz
		 * @return 
		 */
		boolean operatorTestGreaterThan(Class<? extends Mixed> clazz);
		
	}
	
	/**
	 * This only provides the == sign, as != will just be the opposite of ==, and
	 * === and !== are provided as wrappers around == (and therefore !=) with the
	 * additional type checking. In the case of === and !==, the type checking will
	 * be done first, and if they are not the same type, the function will immediately return
	 * false, and not call the == operator.
	 */
	public static interface Equality {
		/**
		 * Returns true if this object is logically equal to the right hand side (m).
		 * @param m
		 * @return 
		 */
		boolean operatorEquals(Mixed m);
		
		/**
		 * Should return true if operatorEquals will work with this class type.
		 * @param clazz
		 * @return 
		 */
		boolean operatorTestEquals(Class<? extends Mixed> clazz);
	}
	
	public static interface Concatenation {
		//TODO
		/**
		 * Returns the new, concatenated object.
		 * @param m
		 * @return 
		 */
		Mixed operatorConcatenation(Mixed m);
		
		/**
		 * Should return true if operatorConcatenation will work with this class type.
		 * @param clazz
		 * @return 
		 */
		boolean operatorTestConcatenation(Class<? extends Mixed> clazz);
	}
	
	/**
	 * Provides +, -, *, and /. There is a distinct lack of %, because
	 * that only makes sense for integral data types, and therefore the mod function
	 * does not provide generic support for it.
	 * 
	 * <p>Of interest however:</p>
	 * <pre>@a % @n == @a - (@n * floor(@a / @n))</pre>
	 * so conceivably if a "floor" overload were added, this would
	 * simply be a function of that and the Mathematical interface.
	 * </p>
	 */
	public static interface Mathematical {
		//TODO
		/**
		 * Returns the new left hand plus right hand value.
		 * @param m
		 * @return 
		 */
		Mixed operatorAddition(Mixed m);
		
		/**
		 * Should return true if operatorAddition will work with this class type.
		 * @param clazz
		 * @return 
		 */
		boolean operatorTestAddition(Class<? extends Mixed> clazz);
		
		//TODO
		/**
		 * Returns the new left hand minus right hand value.
		 * @param m
		 * @return 
		 */
		Mixed operatorSubtraction(Mixed m);
		
		/**
		 * Should return true if operatorSubtraction will work with this class type.
		 * @param clazz
		 * @return 
		 */
		boolean operatorTestSubtraction(Class<? extends Mixed> clazz);
		
		//TODO
		/**
		 * Returns the new left hand times right hand value.
		 * @param m
		 * @return 
		 */
		Mixed operatorMultiplication(Mixed m);
		
		/**
		 * Should return true if operatorMultiplication will work with this class type.
		 * @param clazz
		 * @return 
		 */
		boolean operatorTestMultiplication(Class<? extends Mixed> clazz);
		
		//TODO
		/**
		 * Returns the new left hand divided by right hand value.
		 * @param m
		 * @return 
		 */
		Mixed operatorDivision(Mixed m);
		
		/**
		 * Should return true if operatorTestDivision will work with this class type.
		 * @param clazz
		 * @return 
		 */
		boolean operatorTestDivision(Class<? extends Mixed> clazz);
	}
	
}
