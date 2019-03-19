<!
    strict;
    name: Locale;
    author: cailin;
    created: 2019-03-15;
    description: The file containing the type ms.lang.util.Locale;
>

/**
 * Represents a Locale, per the IETF BCP 47 standard.
 */
public native class ms.lang.util.Locale {
	
    /**
     * Returns the system default locale.
     */
    public static native Locale getDefault();

    /**
     * Given a locale string, returns the Locale 
     */
    public static native Locale fromLocaleString(string @locale);
}
