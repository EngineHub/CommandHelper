package com.laytonsmith.PureUtilities;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A LinkedComparatorSet works like a {@link LinkedHashSet}, but the comparison can be given with a custom comparator,
 * instead of forcing use of the hashCode or equals method. In a normal LinkedHashSet, there is no way to provide a
 * custom comparator, so if you wanted a case insensitive LinkedHashSet, you couldn't do this because there is no way to
 * override the comparison to check if "A" were equal to "a". You could do this with a TreeSet, by providing a custom
 * comparator, but if ordering is also important, then you can't use it either. This provides the best of both worlds by
 * providing insertion order guarantees, while still allowing you to override the comparison mechanism.
 */
public class LinkedComparatorSet<T> extends AbstractSet<T> implements Set<T> {

	final EqualsComparator comparator;
	final List<T> list = new ArrayList<T>();

	/**
	 * Creates an empty {@link LinkedComparatorSet} with the given comparator.
	 *
	 * @param comparator
	 */
	public LinkedComparatorSet(EqualsComparator comparator) {
		this(null, comparator);
	}

	/**
	 * Creates a new LinkedComparatorSet, based on the given collection. The comparator, if not null is used to do the
	 * comparison of equality. This constructor has better runtime performance than doing an
	 * {@link #addAll(java.util.Collection)} operation, <code>n log(n)</code> instead of <code>n<sup>2</sup></code>.
	 *
	 * @param c The collection to start off with
	 * @param comparator The comparator to use in place of the equals method on the underlying objects, or null if a
	 * simple {@link Object#equals(java.lang.Object)} check is sufficient.
	 */
	public LinkedComparatorSet(Collection c, EqualsComparator comparator) {
		this.comparator = comparator;
		if(c != null && comparator != null) {
			Set<Integer> skip = new HashSet<Integer>();
			List<T> array = new ArrayList<T>(c);
			for(int i = 0; i < c.size(); i++) {
				if(skip.contains(i)) {
					continue;
				}
				boolean foundMatch = false;
				T item1 = array.get(i);
				for(int j = i + 1; j < array.size(); j++) {
					if(skip.contains(j)) {
						continue;
					}
					T item2 = array.get(j);
					if(comparator.checkIfEquals(item1, item2)) {
						skip.add(j);
						if(!foundMatch) {
							list.add(item1);
						}
						foundMatch = true;
					}
				}
				if(!foundMatch) {
					list.add(item1);
				}
			}
		} else if(c != null) {
			addAll(c);
		}
	}

	@Override
	public Iterator<T> iterator() {
		return list.iterator();
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public boolean add(T e) {
		if(!contains(e)) {
			list.add(e);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * This implementation uses the custom comparator if provided to check for equality, as opposed to the underlying
	 * object's equals methods.
	 *
	 * @param o
	 * @return
	 */
	@Override
	public boolean contains(Object o) {
		if(comparator == null) {
			return super.contains(o);
		} else {
			Iterator<T> e = iterator();
			if(o == null) {
				while(e.hasNext()) {
					if(e.next() == null) {
						return true;
					}
				}
			} else {
				while(e.hasNext()) {
					if(comparator.checkIfEquals(o, e.next())) {
						return true;
					}
				}
			}
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * This implementation uses the custom comparator if provided to check for equality, as opposed to the underlying
	 * object's equals methods.
	 *
	 * @param o
	 * @return
	 */
	@Override
	public boolean remove(Object o) {
		if(comparator == null) {
			return super.remove(o);
		} else {
			Iterator<T> e = iterator();
			if(o == null) {
				while(e.hasNext()) {
					if(e.next() == null) {
						e.remove();
						return true;
					}
				}
			} else {
				while(e.hasNext()) {
					if(comparator.checkIfEquals(o, e.next())) {
						e.remove();
						return true;
					}
				}
			}
			return false;
		}
	}

	/**
	 * This can be passed in to the constructor of {@link LinkedComparatorSet} to "override" the equals contract of the
	 * sub elements, and that will be used instead.
	 */
	public static interface EqualsComparator<T> {

		/**
		 * Should return true if val1 and val2 are "equals" according to your custom contract.
		 *
		 * @param val1
		 * @param val2
		 * @return
		 */
		boolean checkIfEquals(T val1, T val2);
	}
}
