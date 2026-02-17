package com.laytonsmith.PureUtilities;

import java.util.Objects;
import java.util.Optional;

/**
 * The {@code Either} class can contain neither of, or one of either the specified types of values.
 * @param <L> The first possible type.
 * @param <R> The second possible type.
 */
public final class Either<L, R> {

	/**
	 * Creates a new Either object with the first possible type.
	 * @param <L> The first possible type, which is the type of the input parameter.
	 * @param <R> The other possible type, which this value will not contain.
	 * @param value The value to store.
	 * @return A new Either object.
	 */
	public static <L, R> Either<L, R> left(L value) {
		return new Either<>(Optional.of(value), Optional.empty());
	}

	/**
	 * Creates a new Either object with the second possible type.
	 * @param <L> The other possible type, which this value will not contain.
	 * @param <R> The second possible type, which is the type of the input parameter.
	 * @param value The value to store.
	 * @return A new Either object.
	 */
	public static <L, R> Either<L, R> right(R value) {
		return new Either<>(Optional.empty(), Optional.of(value));
	}

	/**
	 * Creates a new Either object, which does not contain any value.
	 * @param <L> The first possible type.
	 * @param <R> The second possible type.
	 * @return A new, empty, Either object.
	 */
	public static <L, R> Either<L, R> neither() {
		return new Either<>(Optional.empty(), Optional.empty());
	}

	private final Optional<L> left;
	private final Optional<R> right;

	private Either(Optional<L> left, Optional<R> right) {
		this.left = left;
		this.right = right;
	}

	/**
	 * Returns the left value. May be empty, even if the other value is also empty.
	 * @return
	 */
	public Optional<L> getLeft() {
		return this.left;
	}

	/**
	 * Returns the left value. May be empty, even if the other value is also empty.
	 * @return
	 */
	public Optional<R> getRight() {
		return this.right;
	}

	/**
	 * Returns if there was a left side.
	 * @return
	 */
	public boolean hasLeft() {
		return this.left.isPresent();
	}

	/**
	 * Returns if there was a right side.
	 * @return
	 */
	public boolean hasRight() {
		return this.right.isPresent();
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 19 * hash + Objects.hashCode(this.left);
		hash = 19 * hash + Objects.hashCode(this.right);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj == null) {
			return false;
		}
		if(getClass() != obj.getClass()) {
			return false;
		}
		final Either<?, ?> other = (Either<?, ?>) obj;
		if(!Objects.equals(this.left, other.left)) {
			return false;
		}
		return Objects.equals(this.right, other.right);
	}

	@Override
	public String toString() {
		return "Either{" + "left=" + left + ", right=" + right + '}';
	}

}
