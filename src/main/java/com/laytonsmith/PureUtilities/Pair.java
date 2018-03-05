package com.laytonsmith.PureUtilities;

import java.util.Map;
import java.util.Objects;

/**
 * Creates an object pair. The hashcode and equals functions have been overridden to use the underlying object's hash
 * code and equals combined. The underlying objects may be null.
 *
 * @param <A> The first object's type
 * @param <B> The second object's type
 */
public class Pair<A, B> implements Map.Entry<A, B> {

	private final A fst;
	private B snd;

	/**
	 * Creates a new Pair with the specified values.
	 *
	 * @param a
	 * @param b
	 */
	public Pair(A a, B b) {
		fst = a;
		snd = b;
	}

	@Override
	public String toString() {
		return "<" + Objects.toString(fst) + ", " + Objects.toString(snd) + ">";
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 47 * hash + Objects.hashCode(this.fst);
		hash = 47 * hash + Objects.hashCode(this.snd);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null) {
			return false;
		}
		if(getClass() != obj.getClass()) {
			return false;
		}
		final Pair<?, ?> other = (Pair<?, ?>) obj;
		if(!Objects.equals(this.fst, other.fst)) {
			return false;
		}
		if(!Objects.equals(this.snd, other.snd)) {
			return false;
		}
		return true;
	}

	@Override
	public A getKey() {
		return fst;
	}

	@Override
	public B getValue() {
		return snd;
	}

	@Override
	public B setValue(B value) {
		B old = snd;
		snd = (B) value;
		return old;
	}
}
