package com.laytonsmith.tools.docgen.localization;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Provides a generic interface for machine translating text.
 * @author Cailin
 */
public interface MachineTranslation {

	/**
	 * Returns a translation for the single given English string to the given locale. This call is blocking.
	 * @param locale The locale to translate to. All implementations MUST support the art locale, and provide the
	 * implementation provided in {@link ArtMachineTranslation#doArtTranslation(java.lang.String)}.
	 * @param english The original English text.
	 * @throws TranslationException If an error occurs during the translation.
	 * @return The translated text.
	 */
	String translate(String locale, String english) throws TranslationException;

	/**
	 * Returns a translation for the list of given English strings to the given locale. This call is blocking.
	 * @param locale The locale to translate to. All implementations MUST support the art locale, and provide the
	 * implementation provided in {@link ArtMachineTranslation#doArtTranslation(java.lang.String)}. By default,
	 * this method just loops through all the strings given, and does individual calls to {@link #translate}, but
	 * subclasses may choose to provide a more performant method.
	 * @param englishStrings The original English texts.
	 * @param callback If an error occurs during the translation, what would normally be an exception thrown from the
	 * method is simply delivered to the ErrorCallback. Any results that were collected will still be returned, however
	 * this being called may indicate that the returned results may be empty, and the results are partial. However,
	 * since translations are not free, any successful translations from the batch should be returned, even if the
	 * method cannot continue due to a fatal error. The error message should most certainly be displayed to the user,
	 * so they can potentially respond to the conditions in the error message. Generally speaking, if this is called,
	 * the requests will not continue to be made, but this is up to the implementation to decide, since some error
	 * messages may not indicate an issue with the requests themselves. Regardless, it always holds true that this being
	 * called indicates partial results.
	 * @return The translated text. The map maps from English to the required locale, so the mapping
	 * can be reversed.
	 */
	default Map<String, String> bulkTranslate(String locale, Set<String> englishStrings, ErrorCallback callback) {
		Map<String, String> ret = new HashMap<>();
		for(String english : englishStrings) {
			try {
				ret.put(english, translate(locale, english));
			} catch (TranslationException ex) {
				callback.error(ex);
			}
		}
		return ret;
	}

	/**
	 * Thrown indicating there was an exception while trying to do a translation.
	 */
	public static class TranslationException extends Exception {

		private final boolean wasFatal;

		public TranslationException(boolean wasFatal) {
			this.wasFatal = wasFatal;
		}

		public TranslationException(Exception cause, boolean wasFatal) {
			super(cause);
			this.wasFatal = wasFatal;
		}

		public TranslationException(String message, Exception cause, boolean wasFatal) {
			super(message, cause);
			this.wasFatal = wasFatal;
		}

		public TranslationException(String message, boolean wasFatal) {
			super(message);
			this.wasFatal = wasFatal;
		}

		/**
		 * Returns true if the error was fatal. Regardless of this value, it means that the results are partial,
		 * but if it's true, it gave up early because the error was considered "fatal" meaning that repeated requests
		 * would inevitably fail.
		 * @return
		 */
		public boolean wasFatal() {
			return this.wasFatal;
		}
	}

	public static interface ErrorCallback {
		/**
		 * Called iff an exception was thrown during processing.
		 * @param ex
		 */
		void error(TranslationException ex);
	}

	/**
	 * Provides a helper method for correctly implementing the specified ART locale machine translation.
	 */
	public static class ArtMachineTranslation {

		/**
		 * Provides the correct implementation for the ART locale machine translation.
		 * @param english
		 * @return
		 */
		public static String doArtTranslation(String english) {
			return english
					.replace("a", "å")
					.replace("e", "ə")
					.replace("i", "î")
					.replace("o", "ø")
					.replace("u", "ü")
					.replace("y", "ʎ");
		}

		public static Map<String, String> doArtTranslation(Set<String> english) {
			Map<String, String> ret = new HashMap<>();
			for(String s : english) {
				ret.put(s, doArtTranslation(s));
			}
			return ret;
		}
	}
}
