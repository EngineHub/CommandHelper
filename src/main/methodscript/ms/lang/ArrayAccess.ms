<!
	strict;
	name: ArrayAccess.ms;
	author: cailin;
	created: 2019-04-14;
	description: The file containing the type ms.lang.ArrayAccess;
	compilerOptions: UltraStrict;
>

/**
 * An object that implements ArrayAccess can have elements be accessed via the [] operator. This has
 * special support in the language, and uses the same mechanism that arrays use. Values can only be returned,
 * however, this does not provide support for setting a value.
 * @param K The type of value that can be passed in as the key type
 * @param V The type of value that will be returned from the get operation.
 */
public native interface ms.lang.ArrayAccess<K, V> {

	/**
	 * Returns the value that corresponds with the given key.
	 * @throws RangeException If the value is out of range, that is, the key does not exist.
	 */
	V get(K @key) throws RangeException;

	/**
	 * Returns a set of all possible keys in this object.
	 */
	Set<K> keySet();

}
