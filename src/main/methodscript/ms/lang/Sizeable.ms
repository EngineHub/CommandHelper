<!
	strict;
	name: Sizeable.ms;
	author: cailin;
	created: 2019-04-13;
	description: The file containing the type ms.lang.Sizeable;
	compilerOptions: UltraStrict;
>

/**
 * A Sizeable object is one that has a length, and can report that length.
 */
public native interface ms.lang.Sizeable {

	/**
	 * Returns the size of this object. Depending on what this object is, that may represent slightly different things.
	 */
	int size();
}
