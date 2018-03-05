package com.laytonsmith.PureUtilities;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Provides a Queue with a limited size. As more elements are added, if they exceed the limit set initially, elements
 * are removed from the head of the queue.
 *
 * @param <E> The type of elements held in this collection.
 */
public class LimitedQueue<E> extends LinkedList<E> {

	private int limit;

	/**
	 * Creates a new LimitedQueue.
	 *
	 * @param limit The limit to set. If an element is added to this queue, and the size of the queue exceeds this, the
	 * head element is removed.
	 */
	public LimitedQueue(int limit) {
		this.limit = limit;
	}

	/**
	 * Changes the limit after construction. If the limit decreases, and the size of the elements is greater, the excess
	 * elements are discarded at that time.
	 *
	 * @param limit The new limit
	 */
	public void setLimit(int limit) {
		this.limit = limit;
		checkSize();
	}

	@Override
	public boolean add(E o) {
		super.add(o);
		checkSize();
		return true;
	}

	//Override addAll to be more efficient. Only need to do the check at the end.
	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		int i = 0;
		for(E e : c) {
			super.add(index + i, e);
			i++;
		}
		checkSize();
		return true;
	}

	private void checkSize() {
		while(size() > limit) {
			super.remove();
		}
	}
}
