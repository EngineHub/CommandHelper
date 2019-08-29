<!
	strict;
	name: Sliceable.ms;
	author: cailin;
	created: 2019-04-14;
	description: The file containing the type ms.lang.Sliceable;
	compilerOptions: UltraStrict;
>

/**
 * A Sliceable object is one that implements ArrayAccess<int, V>, and can accept slice notation in addition to normal
 * get operations.
 */
public /* should probably be native */ interface ms.lang.Sliceable<V> implements ArrayAccess<int, V> {

	/**
	 * Returns an array of values between the start and finish values, inclusive. That is, if you had
	 * array(0, 1, 2, 3, 4, 5), calling slice(1, 3) would return array(1, 2, 3).
	 *
	 * You can also provide the values in reverse, and the values will be returned reversed, for instance,
	 * slice(3, 1) would return array(3, 2, 1). Negative values can also be provided, and those will be considered
	 * as starting from the end of the array, with -1 indicating the very last value, for instance, slice(-1, -3)
	 * would return array(5, 4, 3).
	 *
	 * By default, we simply loop through the start and finish values, (in the correct order), and return the values,
	 * but subclasses could perhaps provide a more efficient method.
	 */
	array<V> slice(int @start, int @finish) {
		TODO: Need to support reverse as well
		array<V> @ret = array<V>();
		for(int @i = @start, @i <= @finish, ++@i) {
			@ret[] = @this[@i];
		}
		return(@ret);
	}
}
