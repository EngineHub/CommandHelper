package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.enums.MCTagType;

import java.util.Set;

/**
 * Minecraft NBT containers that can be used to read and modify tags in supported game objects.
 * This includes item meta, entities, block entities, chunks, worlds, etc.
 */
public interface MCTagContainer extends AbstractionObject {

	/**
	 * Returns whether the tag container does not contain any tags.
	 * @return whether container is empty
	 */
	boolean isEmpty();

	/**
	 * Gets a set of namespaced keys for each tag that exists in this container. (e.g. "namespace:key")
	 * @return a set of keys
	 */
	Set<MCNamespacedKey> getKeys();

	/**
	 * Returns the tag type with the given key.
	 * MCTagType can be used to convert tags to and from MethodScript constructs.
	 * Returns null if a tag with that key does not exist.
	 * @param key the tag key
	 * @return the type for the tag
	 */
	MCTagType getType(MCNamespacedKey key);

	/**
	 * Returns the tag value with the given key and tag type.
	 * Returns null if a tag with that key and type does not exist.
	 * @param key the tag key
	 * @param type the tag type
	 * @return the value for the tag
	 */
	Object get(MCNamespacedKey key, MCTagType type);

	/**
	 * Sets the tag value with the given key and tag type.
	 * Throws an IllegalArgumentException if the type and value do not match.
	 * @param key the tag key
	 * @param type the tag type
	 * @param value the tag value
	 */
	void set(MCNamespacedKey key, MCTagType type, Object value);

	/**
	 * Deletes the tag with the given key from this container.
	 * @param key the tag key
	 */
	void remove(MCNamespacedKey key);

	/**
	 * Creates a new tag container from this container context.
	 * This can then be used to nest a tag container with the {@link #set} method.
	 * @return a new tag container
	 */
	MCTagContainer newContainer();

}
