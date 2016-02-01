

package com.laytonsmith.core.natives.interfaces;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import java.util.Arrays;
import java.util.Set;

/**
 * Things that implement this can be accessed like an array, with array_get, or [].
 */
@typeof("ArrayAccess")
public interface ArrayAccess extends Mixed, Sizable {
    /**
     * Return the mixed at this location. This should throw an exception if
     * the index does not exist. This method will not be called if
	 * {@link #isAssociative()} returns false.
     * @param index
	 * @param t
     * @return
     */
    public Construct get(String index, Target t) throws ConfigRuntimeException;

	/**
	 * Returns the mixed at this location. This should throw an exception if
	 * the index does not exist. This method will not be called if
	 * {@link #isAssociative()} returns true.
	 * @param index
	 * @param t
	 * @return
	 * @throws ConfigRuntimeException
	 */
	public Construct get(int index, Target t) throws ConfigRuntimeException;

	/**
	 * Returns the mixed at this location. This should throw an exception if
	 * the index does not exist. This method may be called whether or not
	 * it isAssociative returns true.
	 * @param index
	 * @param t
	 * @return
	 * @throws ConfigRuntimeException
	 */
	public Construct get(Construct index, Target t) throws ConfigRuntimeException;

	/**
	 * If {@link #isAssociative()} returns true, this should return a set of all
	 * keys. If {@link #isAssociative()} returns false, this method will not be
	 * called.
	 * @return
	 */
	public Set<Construct> keySet();

    /**
     * Return the size of the array
     * @return
     */
	@Override
    public long size();

	/**
	 * Unlike {@link #canBeAssociative()}, this is a runtime flag. If the underlying
	 * object is associative (that is, it is an unordered, non numeric key set), this should
	 * return true. If this is true, then {@link #get(java.lang.String, com.laytonsmith.core.constructs.Target)}
	 * will not be called, and {@link #get(int, com.laytonsmith.core.constructs.Target)} will be called
	 * instead. If this is false, the opposite will occur.
	 * @return
	 */
	public boolean isAssociative();

    /**
     * Just because it is accessible as an array doesn't mean it will be associative. For optimiziation purposes, it
     * may be possible to check at compile time if the code is attempting to send a non-integral index,
     * in which case we can throw a compile error. This is a compile time flag, and is not used during
	 * the runtime, just during compilation.
     * @return
     */
    public boolean canBeAssociative();

    /**
     * Returns a slice at the specified location. Should throw an exception if an element in
     * the range doesn't exist.
     * @param begin
     * @param end
     * @param t
     * @return
     */
    public Construct slice(int begin, int end, Target t);

	/**
	 * This class contains iteration information for the ArrayAccess object
	 * as it is being iterated. This assumes that the object being iterated
	 * is not associative. Associative arrays have far simpler handling,
	 * and can therefore skip the handling needed for non-associative arrays.
	 */
	public static class ArrayAccessIterator {
		private final ArrayAccess array;
		private int current = 0;
		private int[] blacklist = new int[]{-1};
		private int blacklistSize = 0;

		/**
		 * Creates a new ArrayAccessIterator. If the array is associative,
		 * a RuntimeException is thrown, since associative arrays have
		 * far simpler handling, and should not use this mechanism.
		 * @param array
		 */
		public ArrayAccessIterator(ArrayAccess array){
			if(array.isAssociative()){
				throw new RuntimeException();
			}
			this.array = array;
		}

		/**
		 * Returns the index of the currently iterated object.
		 * @return
		 */
		public int getCurrent(){
			return current;
		}

		/**
		 * Decrements the current counter. This operation is used when the current
		 * item, or an item before the current item is removed.
		 */
		public void decrementCurrent(){
			--current;
		}

		/**
		 * Increments the current counter. This operation is used when a new
		 * item is inserted before or at the current item. It is also used at the
		 * end of the loop.
		 */
		public void incrementCurrent(){
			++current;
		}

		/**
		 * Increments all the values in the blacklist. This is used when a new
		 * item is inserted into the array, after the current index.
		 * @param from The value to search from. Any values after this are incremented,
		 * and values before it are not.
		 */
		public void incrementBlacklistAfter(int from){
			for(int i = 0; i < blacklist.length; i++){
				if(blacklist[i] > from){
					blacklist[i]++;
				}
			}
		}

		/**
		 * Adds a value to the blacklist. This is used when a value is added after
		 * the current index. This value will not be iterated in the future.
		 * @param index The index to add to the blacklist.
		 */
		public void addToBlacklist(int index){
			if(blacklistSize == blacklist.length){
				int[] bl = new int[blacklist.length * 2];
				Arrays.fill(bl, -1);
				System.arraycopy(blacklist, 0, bl, 0, blacklist.length);
				blacklist = bl;
			}
			blacklist[blacklistSize] = index;
			blacklistSize++;
		}

		/**
		 * Checks through the blacklist, and returns true if this index
		 * is blacklisted. If so, this value should be skipped in the iteration.
		 * @param index The index to check.
		 * @return True if this value should be skipped.
		 */
		public boolean isBlacklisted(int index){
			for(int v : blacklist){
				if(v == index){
					return true;
				}
			}
			return false;
		}

		/**
		 * Gets the underlying ArrayAccess object.
		 * @return
		 */
		public ArrayAccess underlyingArray(){
			return array;
		}

	}

	@Override
	public String docs();

	@Override
	public Version since();
	
}
