package com.laytonsmith.PureUtilities;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class provides a generic way to have a threadsafe map of singleton values, where a key maps to a single value,
 * and where the value is only created if it doesn't exist and only once.
 *
 * There is a decent amount of complexity involved in this task, and so this class wraps the functionality. The class
 * extends Map, so it can generally be used in place of other Map objects.
 *
 * Insertions will trigger synchronization, but given it is a singleton pool, this is assumed to not happen frequently.
 * The put and remove methods will trigger an exception if they are called. Only the internal generator is allowed to
 * insert values into the internal map, and values are not allowed to be removed.
 *
 * @author cailin
 */
public class ConcurrentSingletonHashMap<T, V> implements Map<T, V> {

	/*
	 * You might notice that no fields in this class are volatile. Normally, when you double lock, you must do
	 * something like this to be totally correct:
	 *
	 * <pre>
	 * volatile Object value = null; // Note the volatility
	 * construct() {
	 *	Object result = value;
	 *	if(result == null) {
	 *		synchronized(result) {
	 *		if(result == null) {
	 *			result = new Object();
	 *			value = result;
	 *		}
	 *		}
	 *	}
	 *	return result;
	 * }
	 * </pre>
	 *
	 * Note that we are doing the double locking per usual, but the value is volatile. The local result value seems
	 * unnecessary at first, but the effect of this is that in cases where value is already initialized
	 * (i.e., most of the time), the volatile field is only accessed once (due to "return result;" instead of
	 * "return value;"), which can improve the method's overall performance by as much as 25 percent.
	 *
	 * However, in the case that we have before us, the ConcurrentHashMap handles this for us, by guaranteeing that
	 * we never get a value that is partially constructed in the get() method.
	 *
	 *
	 */
	private final Map<T, V> map = new ConcurrentHashMap<>();
	private final ValueGenerator<T, V> generator;

	public interface ValueGenerator<T, V> {

		V generate(T key);
	}

	public ConcurrentSingletonHashMap(ValueGenerator<T, V> generator) {
		this.generator = generator;
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}

	@Override
	public V get(Object key) {
		@SuppressWarnings("unchecked")
		T k = (T) key;
		// Usual case, it already exists. No synchronization.
		if(map.containsKey(k)) {
			return map.get(k);
		}
		// It does not exist. We must now synchronize.
		synchronized(map) {
			// It may have since been created since we got the lock
			if(map.containsKey(k)) {
				return map.get(k);
			}
			// It truly does not exist, so now we must create it, put it in the map, then return it.
			V value = generator.generate(k);
			map.put(k, value);
			return value;
		}
	}

	/**
	 * This method unconditionally throws an exception.
	 *
	 * @param key
	 * @param value
	 * @return
	 * @throws UnsupportedOperationException Put operations are not allowed, and so this exception is always thrown.
	 */
	@Override
	public V put(T key, V value) {
		throw new UnsupportedOperationException("Put operations are not allowed in " + this.getClass().getSimpleName());
	}

	/**
	 * This method unconditionally throws an exception.
	 *
	 * @param key
	 * @return
	 * @throws UnsupportedOperationException Remove operations are not allowed, and so this exception is always thrown.
	 */
	@Override
	public V remove(Object key) {
		throw new UnsupportedOperationException("Remove operations are not allowed in " + this.getClass().getSimpleName());
	}

	/**
	 * This method unconditionally throws an exception.
	 *
	 * @param m
	 * @return
	 * @throws UnsupportedOperationException Put operations are not allowed, and so this exception is always thrown.
	 */
	@Override
	public void putAll(Map<? extends T, ? extends V> m) {
		throw new UnsupportedOperationException("Put operations are not allowed in " + this.getClass().getSimpleName());
	}

	/**
	 * This method unconditionally throws an exception.
	 *
	 * @param key
	 * @return
	 * @throws UnsupportedOperationException Remove operations are not allowed, and so this exception is always thrown.
	 */
	@Override
	public void clear() {
		throw new UnsupportedOperationException("Remove operations are not allowed in " + this.getClass().getSimpleName());
	}

	@Override
	public Set<T> keySet() {
		return map.keySet();
	}

	@Override
	public Collection<V> values() {
		return map.values();
	}

	@Override
	public Set<Entry<T, V>> entrySet() {
		return map.entrySet();
	}

}
