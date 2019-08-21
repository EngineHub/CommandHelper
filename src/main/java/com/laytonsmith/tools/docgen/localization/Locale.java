package com.laytonsmith.tools.docgen.localization;

import java.util.Arrays;
import java.util.List;

/**
 * Supported values are the language codes found under the translation section of this page:
 * https://api.cognitive.microsofttranslator.com/languages?api-version=3.0 along with the special code "art" which is a
 * programmatically generated test language, that is always available.
 */
@SuppressWarnings({"checkstyle:nowhitespacebefore"})
public enum Locale {

	/**
	 * As in artificial, this is the official language code for made up languages, and is always assumed to be present,
	 * so cannot be removed.
	 */
	ART("art", "Artificial", "Årtïfïcïål", TextDirection.LTR, java.util.Locale.forLanguageTag("art-x-dummy"),
			Arrays.asList()),
	KO("ko", "Korean", "한국어", TextDirection.LTR, java.util.Locale.KOREAN, Arrays.asList("Malgun Gothic")),
	NB("nb", "Norwegian", "Norsk", TextDirection.LTR, java.util.Locale.forLanguageTag("nb"), Arrays.asList()),
//	PT("pt", "Portuguese", "Português", TextDirection.LTR, java.util.Locale.forLanguageTag("pt"), Arrays.asList()),
//	HE("he", "Hebrew", "עברית", TextDirection.RTL, java.util.Locale.forLanguageTag("he"), Arrays.asList()),
//	NL("nl", "Dutch", "Nederlands", TextDirection.LTR, java.util.Locale.forLanguageTag("nl"), Arrays.asList()),
	;
	private final String locale;
	private final String englishName;
	private final String localName;
	private final TextDirection textDirection;
	private final List<String> useFonts;
	private final java.util.Locale javaUtilLocale;

	private Locale(String locale, String englishName, String localName, TextDirection textDirection,
			java.util.Locale javaUtilLocale, List<String> useFonts) {
		this.locale = locale;
		this.englishName = englishName;
		this.localName = localName;
		this.textDirection = textDirection;
		this.javaUtilLocale = javaUtilLocale;
		this.useFonts = useFonts;
	}

	/**
	 * Locale name
	 *
	 * @return
	 */
	public String getLocale() {
		return locale;
	}

	/**
	 * The name of the language in English
	 * @return
	 */
	public String getEnglishName() {
		return englishName;
	}

	/**
	 * The name of this language in that language
	 *
	 * @return
	 */
	public String getLocalName() {
		return localName;
	}

	/**
	 * Is this locale ltr or rtl
	 *
	 * @return
	 */
	public TextDirection getTextDirection() {
		return textDirection;
	}

	/**
	 * Some locales require other fonts to display correctly. In that case, the named fonts will be used. If more than
	 * one is provided, the first available font is used.
	 *
	 * @return
	 */
	public List<String> getUseFonts() {
		return useFonts;
	}

	/**
	 * Returns the <code>java.util.{@link java.util.Locale}</code> object for this locale.
	 * @return
	 */
	public java.util.Locale getJavaUtilLocale() {
		return javaUtilLocale;
	}

	/**
	 * Returns the {@link Locale} object for the given locale string.
	 * @param locale
	 * @return
	 */
	public static Locale fromLocale(String locale) {
		for(Locale d : values()) {
			if(d.getLocale().equals(locale)) {
				return d;
			}
		}
		throw new IllegalArgumentException("Locale \"" + locale + "\" not supported.");
	}

	/**
	 * Returns the default dummy locale, {@link #ART}.
	 * @return
	 */
	public static Locale getDummyLocale() {
		return Locale.ART;
	}

}
