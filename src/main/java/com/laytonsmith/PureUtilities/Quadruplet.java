package com.laytonsmith.PureUtilities;

import java.util.Objects;

/**
 * Creates an object quadruplet. The hashcode and equals functions have been overridden to use the underlying object's hash
 * code and equals combined. The underlying objects may be null.
 *
 * @param <A> The first object's type.
 * @param <B> The second object's type.
 * @param <C> The third object's type.
 * @param <D> The fourth object's type.
 */
public class Quadruplet<A, B, C, D> {

	private final A fst;
	private final B snd;
	private final C trd;
	private final D frth;

	/**
	 * Creates a new Quadruplet with the specified values.
	 *
	 * @param a
	 * @param b
	 * @param c
	 * @param d
	 */
	public Quadruplet(A a, B b, C c, D d) {
		fst = a;
		snd = b;
		trd = c;
		frth = d;
	}

	@Override
	public String toString() {
		return "<"
				+ Objects.toString(fst) + ", "
				+ Objects.toString(snd) + ", "
				+ Objects.toString(trd) + ", "
				+ Objects.toString(frth) + ">";
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 47 * hash + Objects.hashCode(this.fst);
		hash = 47 * hash + Objects.hashCode(this.snd);
		hash = 47 * hash + Objects.hashCode(this.trd);
		hash = 47 * hash + Objects.hashCode(this.frth);
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
		final Quadruplet<?, ?, ?, ?> other = (Quadruplet<?, ?, ?, ?>) obj;
		if(!Objects.equals(this.fst, other.fst)) {
			return false;
		}
		if(!Objects.equals(this.snd, other.snd)) {
			return false;
		}
		if(!Objects.equals(this.trd, other.trd)) {
			return false;
		}
		if(!Objects.equals(this.frth, other.frth)) {
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

	public D getFourth() {
		return frth;
	}
}
