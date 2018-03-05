package com.laytonsmith.PureUtilities.Common;

/**
 * This class wraps an object, which is mutable. Useful for places where you must be able to manipulate a final
 * variable, for instance, in anonymous classes. The common Object methods are forwarded to the underlying object,
 * unless it is null, in which case various defaults are returned.
 */
public final class MutableObject<T> {

	private T obj = null;

	/**
	 * Constructs a new MutableObject, which is null.
	 */
	public MutableObject() {

	}

	/**
	 * Constructs a new MutableObject, wrapping the specified object.
	 *
	 * @param obj
	 */
	public MutableObject(T obj) {
		setObject(obj);
	}

	/**
	 * Sets the underlying object.
	 *
	 * @param obj
	 */
	public void setObject(T obj) {
		this.obj = obj;
	}

	/**
	 * Gets the underlying object.
	 *
	 * @return
	 */
	public T getObject() {
		return obj;
	}

	@Override
	@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
	public boolean equals(Object obj) {
		if(this.obj == null) {
			return obj == null;
		} else {
			return this.obj.equals(obj);
		}
	}

	@Override
	public int hashCode() {
		if(this.obj == null) {
			return 0;
		} else {
			return this.obj.hashCode();
		}
	}

	@Override
	public String toString() {
		if(this.obj == null) {
			return "null";
		} else {
			return this.obj.toString();
		}
	}
}
