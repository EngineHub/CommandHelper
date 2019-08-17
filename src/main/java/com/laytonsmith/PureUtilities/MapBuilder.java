package com.laytonsmith.PureUtilities;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Eases the act of building a map which has static values.
 * @param <K> The key type
 * @param <V> The value type
 */
public class MapBuilder<K, V> extends AbstractMap<K, V> implements Map<K, V> {

	protected MapBuilder(Map<K, V> map) {
		this.map = map;
	}

	/**
	 * Creates a new MapBuilder and adds the given values to the map.
	 * @param <T> The key type.
	 * @param <U> The value type.
	 * @param key The key
	 * @param value The value
	 * @return A new MapBuilder object.
	 */
	public static <T, U> MapBuilder<T, U> start(T key, U value) {
		return new MapBuilder<T, U>(new HashMap<T, U>()).set(key, value);
	}

	/**
	 * Creates a new, empty map builder, which supports the given key and value types.
	 * @param <T> The key type.
	 * @param <U> The value type.
	 * @param keyType
	 * @param valueType
	 * @return A new MapBuilder object.
	 */
	public static <T, U> MapBuilder<T, U> empty(Class<T> keyType, Class<U> valueType) {
		return new MapBuilder<>(new HashMap<>());
	}

	/**
	 * By default, the other constructors use {@link HashMap}s as the backing Map implementation. This may not be
	 * desirable. In that case, you can provide an existing Map with whatever implementation you see fit. The Map
	 * may also be non-empty if you wish to prepopulate the map through other means.
	 * @param <T> The key type.
	 * @param <U> The value type.
	 * @param existingMap An exisiting Map.
	 * @return A new MapBuilder object wrapping the provided Map instance.
	 */
	public static <T, U> MapBuilder<T, U> empty(Map<T, U> existingMap) {
		return new MapBuilder<>(existingMap);
	}

	private final Map<K, V> map;

	/**
	 * Puts a new entry in the map, and returns this.
	 * @param key They key to add.
	 * @param value The value to add.
	 * @return The MapBuilder object, for easy chaining.
	 */
	public MapBuilder<K, V> set(K key, V value) {
		map.put(key, value);
		return this;
	}

	/**
	 * Puts a new entry in the map only if the value is not null. If it is null, nothing is changed. Either way,
	 * the MapBuilder object is returned.
	 * @param key The key to add.
	 * @param value The value to add, possibly null
	 * @return The MapBuilder object, for easy chaining
	 */
	public MapBuilder<K, V> setIfValueNotNull(K key, V value) {
		if(value != null) {
			put(key, value);
		}
		return this;
	}


	/**
	 * Unlike set, which returns the MapBuilder object, this is the original put from Map.
	 * <p>
	 * {@inheritDoc}
	 *
	 * @param key
	 * @param value
	 * @return
	 */
	@Override
	public V put(K key, V value) {
		return map.put(key, value);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		map.putAll(m);
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
	@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
	public boolean equals(Object o) {
		return map.equals(o);
	}

	@Override
	public V get(Object key) {
		return map.get(key);
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public int hashCode() {
		return map.hashCode();
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public Set<K> keySet() {
		return map.keySet();
	}

	@Override
	public V remove(Object key) {
		return map.remove(key);
	}

	@Override
	public boolean remove(Object key, Object value) {
		return map.remove(key, value);
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public String toString() {
		return map.toString();
	}

	@Override
	public Collection<V> values() {
		return map.values();
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		return map.entrySet();
	}

	@Override
	public V getOrDefault(Object key, V defaultValue) {
		return map.getOrDefault(key, defaultValue);
	}

	@Override
	public void forEach(BiConsumer<? super K, ? super V> action) {
		map.forEach(action);
	}

	@Override
	public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
		map.replaceAll(function);
	}

	@Override
	public V putIfAbsent(K key, V value) {
		return map.putIfAbsent(key, value);
	}

	@Override
	public boolean replace(K key, V oldValue, V newValue) {
		return map.replace(key, oldValue, newValue);
	}

	@Override
	public V replace(K key, V value) {
		return map.replace(key, value);
	}

	@Override
	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
		return map.computeIfAbsent(key, mappingFunction);
	}

	@Override
	public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		return map.computeIfPresent(key, remappingFunction);
	}

	@Override
	public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		return map.compute(key, remappingFunction);
	}

	@Override
	public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
		return map.merge(key, value, remappingFunction);
	}
}
