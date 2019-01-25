/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.natives.interfaces;

import java.util.Arrays;

/**
 * This class contains iteration information for the ArrayAccess object as it is being iterated. This assumes that
 * the object being iterated is not associative. Associative arrays have far simpler handling, and can therefore
 * skip the handling needed for non-associative arrays.
 */
public class Iterator {

	private final Iterable array;
	private int current = 0;
	private int[] blacklist = new int[]{-1};
	private int blacklistSize = 0;

	/**
	 * Creates a new ArrayAccessIterator. If the array is associative, a RuntimeException is thrown, since
	 * associative arrays have far simpler handling, and should not use this mechanism.
	 *
	 * @param array
	 */
	public Iterator(Iterable array) {
		if(array.isAssociative()) {
			throw new RuntimeException();
		}
		this.array = array;
	}

	/**
	 * Returns the index of the currently iterated object.
	 *
	 * @return
	 */
	public int getCurrent() {
		return current;
	}

	/**
	 * Decrements the current counter. This operation is used when the current item, or an item before the current
	 * item is removed.
	 */
	public void decrementCurrent() {
		--current;
	}

	/**
	 * Increments the current counter. This operation is used when a new item is inserted before or at the current
	 * item. It is also used at the end of the loop.
	 */
	public void incrementCurrent() {
		++current;
	}

	/**
	 * Increments all the values in the blacklist. This is used when a new item is inserted into the array, after
	 * the current index.
	 *
	 * @param from The value to search from. Any values after this are incremented, and values before it are not.
	 */
	public void incrementBlacklistAfter(int from) {
		for(int i = 0; i < blacklist.length; i++) {
			if(blacklist[i] > from) {
				blacklist[i]++;
			}
		}
	}

	/**
	 * Adds a value to the blacklist. This is used when a value is added after the current index. This value will
	 * not be iterated in the future.
	 *
	 * @param index The index to add to the blacklist.
	 */
	public void addToBlacklist(int index) {
		if(blacklistSize == blacklist.length) {
			int[] bl = new int[blacklist.length * 2];
			Arrays.fill(bl, -1);
			System.arraycopy(blacklist, 0, bl, 0, blacklist.length);
			blacklist = bl;
		}
		blacklist[blacklistSize] = index;
		blacklistSize++;
	}

	/**
	 * Checks through the blacklist, and returns true if this index is blacklisted. If so, this value should be
	 * skipped in the iteration.
	 *
	 * @param index The index to check.
	 * @return True if this value should be skipped.
	 */
	public boolean isBlacklisted(int index) {
		for(int v : blacklist) {
			if(v == index) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets the underlying ArrayAccess object.
	 *
	 * @return
	 */
	public Iterable underlyingArray() {
		return array;
	}

}
