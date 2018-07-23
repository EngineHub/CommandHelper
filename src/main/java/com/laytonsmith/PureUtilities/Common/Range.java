package com.laytonsmith.PureUtilities.Common;

import java.util.AbstractList;
import java.util.List;

/**
 * This class provides various methods for comparing numeric ranges.
 */
public class Range {

	private final int leftBound;
	private final int rightBound;
	private final boolean leftInclusive;
	private final boolean rightInclusive;

	/**
	 * Creates a new Range object. The left bound doesn't have to be less than the right bound, but if it is greater,
	 * then the range is considered descending, assuming leftBound is not equal to rightBound. Otherwise, it is
	 * ascending.
	 *
	 * @param leftBound The left boundary number
	 * @param rightBound The right boundary number
	 * @param leftInclusive If the left bound should be inclusive
	 * @param rightInclusive If the right bound should be inclusive
	 */
	public Range(int leftBound, int rightBound, boolean leftInclusive, boolean rightInclusive) {
		this.leftBound = leftBound;
		this.rightBound = rightBound;
		this.leftInclusive = leftInclusive;
		this.rightInclusive = rightInclusive;
	}

	/**
	 * Creates a new Range object with both left and right bounds inclusive.
	 *
	 * @param leftBound
	 * @param rightBound
	 */
	public Range(int leftBound, int rightBound) {
		this(leftBound, rightBound, true, true);
	}

	/**
	 * Returns true if the left bound equals the right bound. If this returns true, both {@link #isAscending()} and
	 * {@link #isDecending()} will return false.
	 *
	 * @return
	 */
	public boolean isEqual() {
		return leftBound == rightBound;
	}

	/**
	 * Returns true if the left bound is less than the right bound. If this returns true, both {@link #isEqual()} and
	 * {@link #isDecending()} will return false.
	 *
	 * @return
	 */
	public boolean isAscending() {
		return leftBound < rightBound;
	}

	/**
	 * Returns true if the right bound is less than the left bound. If this returns true, both {@link #isEqual()} and
	 * {@link #isAscending()} will return false.
	 *
	 * @return
	 */
	public boolean isDecending() {
		return rightBound < leftBound;
	}

	/**
	 * Returns a range of integers, starting with the left bound, counting up (or down if descending) by 1, and
	 * returning a List of values. The returned List is optimized to prevent a large memory footprint by generating the
	 * returned values via algorithm, instead of actually storing each value. However, this means that the List is read
	 * only. You can create a new List with a copy constructor to make a new mutable list.
	 *
	 * @return
	 */
	public List<Integer> getRange() {
		//Calculate the size once
		double size = Math.abs(leftBound - rightBound);
		if(!leftInclusive) {
			size--;
		}
		if(rightInclusive) {
			size++;
		}
		final int finalSize = (int) size;
		return new AbstractList<Integer>() {

			@Override
			public Integer get(int index) {
				if(isAscending()) {
					return leftBound + index + (leftInclusive ? 0 : 1);
				} else {
					return leftBound - index - (leftInclusive ? 0 : 1);
				}
			}

			@Override
			public int size() {
				return finalSize;
			}
		};
	}

	/**
	 * Returns the minimum value in this range, regardless of whether it is descending or ascending.
	 *
	 * @return
	 */
	public int getMin() {
		if(isAscending()) {
			if(leftInclusive) {
				return leftBound;
			} else {
				return leftBound + 1;
			}
		} else if(isDecending()) {
			if(rightInclusive) {
				return rightBound;
			} else {
				return rightBound + 1;
			}
		} else {
			return leftBound;
		}
	}

	/**
	 * Returns the maximum value in this range, regardless of whether it is descending or ascending.
	 *
	 * @return
	 */
	public int getMax() {
		if(isDecending()) {
			if(leftInclusive) {
				return leftBound;
			} else {
				return leftBound - 1;
			}
		} else if(isAscending()) {
			if(rightInclusive) {
				return rightBound;
			} else {
				return rightBound - 1;
			}
		} else {
			return leftBound;
		}
	}

	/**
	 * Returns true if the specified value is included in this range.
	 *
	 * @param value The value to test
	 * @return true, if the value is in this range
	 */
	public boolean contains(int value) {
		return value >= getMin() && value <= getMax();
	}

	@Override
	public String toString() {
		return (leftInclusive ? "[" : "(") + leftBound + ", " + rightBound + (rightInclusive ? "]" : ")");
	}

}
