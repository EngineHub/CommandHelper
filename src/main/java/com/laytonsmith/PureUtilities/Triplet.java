package com.laytonsmith.PureUtilities;

import java.util.Objects;

/**
 * Creates an object triplet. The hashcode and equals functions have been overridden to use the underlying object's hash
 * code and equals combined. The underlying objects may be null.
 *
 * @param <A> The first object's type
 * @param <B> The second object's type
 * @param <C> The third object's type
 */
public class Triplet<A, B, C> {

	private final A fst;
	private final B snd;
	private final C trd;

	/**
	 * Creates a new Triplet with the specified values.
	 *
	 * @param a
	 * @param b
	 * @param c
	 */
	public Triplet(A a, B b, C c) {
		fst = a;
		snd = b;
		trd = c;
	}

	@Override
	public String toString() {
		return "<" + Objects.toString(fst) + ", " + Objects.toString(snd) + ", " + Objects.toString(trd) + ">";
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 47 * hash + Objects.hashCode(this.fst);
		hash = 47 * hash + Objects.hashCode(this.snd);
		hash = 47 * hash + Objects.hashCode(this.trd);
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
		final Triplet<?, ?, ?> other = (Triplet<?, ?, ?>) obj;
		if(!Objects.equals(this.fst, other.fst)) {
			return false;
		}
		if(!Objects.equals(this.snd, other.snd)) {
			return false;
		}
		if(!Objects.equals(this.trd, other.trd)) {
			return false;
		}
		return true;
	}

	public A getFirst() {
		return fst;
	}

	public B getSecond() {
		return snd;
	}

	public C getThird() {
		return trd;
	}
}
