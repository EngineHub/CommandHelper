<!
	strict;
	name: string.ms;
	author: cailin;
	created: 2019-03-15;
	description: The file containing the type ms.lang.string;
	compilerOptions: UltraStrict;
>

use ms.lang.util;

/**
 * A string is a value that contains character data. The character encoding is stored with the string as well.
 * @since 3.0.1
 */
public native class ms.lang.string implements Sliceable, Iterable {

	/**
	 * Creates a new, empty string.
	 */
	public native string();

	/**
	 * Creates a new string, equal to the provided string. The two instances will be equal, but not ref_equal.
	 */
	public native string(string @copy);

	/**
	 * Returns a new string that has been lowercased, per the current locale definition of "lowercase".
	 * @returns A lowercased string.
	 * @since 3.3.4
	 */
	public string toLowerCase() {
		return(@this->toLowerCase(Locale::getDefault()));
	}

	/**
	 * Returns a new string that has been lowercased, per the provided locale.
	 * @returns A lowercased string.
	 * @since 3.3.4
	 */
	public native string toLowerCase(Locale @locale);

	/**
	 * Returns a new string that has been uppercased, per the current locale definition of "uppercase".
	 * @returns An uppercased string.
	 * @since 3.3.4
	 */
	public string toUpperCase() {
		return(@this->toUpperCase(Locale::getDefault()));
	}

	/**
	 * Returns a new string that has been uppercased, per the provided locale.
	 * @returns An uppercased string.
	 * @since 3.3.4
	 */
	public native string toUpperCase(Locale @locale);

	/**
	 * Returns true if the string matches the given regex, false otherwise.
	 * @param regex The regex to test against.
	 */
	public native boolean matches(@{ValidRegex} string @regex);

	@{Override}
	public native int size();

}
