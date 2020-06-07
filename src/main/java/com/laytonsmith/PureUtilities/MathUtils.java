package com.laytonsmith.PureUtilities;

/**
 * This class contains methods that can assist with certain mathematical problems.
 * @author P.J.S. Kools
 */
public abstract class MathUtils {

	/**
	 * Check a number's parity.
	 * @param number
	 * @return {@code true} if the number is even, {@code false} otherwise.
	 */
	public static boolean isEven(int number) {
		return (number & 0x01) == 0x00;
	}

	/**
	 * Check a number's parity.
	 * @param number
	 * @return {@code true} if the number is off, {@code false} otherwise.
	 */
	public static boolean isOdd(int number) {
		return (number & 0x01) == 0x01;
	}
}
