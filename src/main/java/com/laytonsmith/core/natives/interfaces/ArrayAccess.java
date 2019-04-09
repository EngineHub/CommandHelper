package com.laytonsmith.core.natives.interfaces;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import java.util.Set;

/**
 * Things that implement this can be accessed like an array, with array_get, or [].
 */
@typeof("ms.lang.ArrayAccess")
public interface ArrayAccess extends Mixed {

	@SuppressWarnings("FieldNameHidesFieldInSuperclass")
	public static final CClassType TYPE = CClassType.get(ArrayAccess.class);

	/**
	 * Return the mixed at this location. This should throw an exception if the index does not exist. This method will
	 * not be called if {@link #isAssociative()} returns false.
	 *
	 * @param index
	 * @param t
	 * @return
	 */
	public Mixed get(String index, Target t) throws ConfigRuntimeException;

	/**
	 * Returns the mixed at this location. This should throw an exception if the index does not exist. This method will
	 * not be called if {@link #isAssociative()} returns true.
	 *
	 * @param index
	 * @param t
	 * @return
	 * @throws ConfigRuntimeException
	 */
	public Mixed get(int index, Target t) throws ConfigRuntimeException;

	/**
	 * Returns the mixed at this location. This should throw an exception if the index does not exist. This method may
	 * be called whether or not it isAssociative returns true.
	 *
	 * @param index
	 * @param t
	 * @return
	 * @throws ConfigRuntimeException
	 */
	public Mixed get(Mixed index, Target t) throws ConfigRuntimeException;

	/**
	 * If {@link #isAssociative()} returns true, this should return a set of all keys. If {@link #isAssociative()}
	 * returns false, this method will not be called.
	 *
	 * @return
	 */
	public Set<Mixed> keySet();

	/**
	 * Unlike {@link #canBeAssociative()}, this is a runtime flag. If the underlying object is associative (that is, it
	 * is an unordered, non numeric key set), this should return true. If this is true, then
	 * {@link #get(java.lang.String, com.laytonsmith.core.constructs.Target)} will not be called, and
	 * {@link #get(int, com.laytonsmith.core.constructs.Target)} will be called instead. If this is false, the opposite
	 * will occur.
	 *
	 * @return
	 */
	public boolean isAssociative();

	/**
	 * Just because it is accessible as an array doesn't mean it will be associative. For optimiziation purposes, it may
	 * be possible to check at compile time if the code is attempting to send a non-integral index, in which case we can
	 * throw a compile error. This is a compile time flag, and is not used during the runtime, just during compilation.
	 *
	 * @return
	 */
	public boolean canBeAssociative();

	/**
	 * Returns a slice at the specified location. Should throw an exception if an element in the range doesn't exist.
	 *
	 * @param begin
	 * @param end
	 * @param t
	 * @return
	 */
	public Mixed slice(int begin, int end, Target t);


	@Override
	public String docs();

	@Override
	public Version since();

}
