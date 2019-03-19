<!
    strict;
    name: string;
    author: cailin;
    created: 2019-03-15;
    description: The file containing the type ms.lang.string;
>

use ms.lang.util;

/**
 * A string is a value that contains character data. The character encoding is stored with the string as well.
 * @since 3.0.1
 */
public native class ms.lang.string {
	
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
}
