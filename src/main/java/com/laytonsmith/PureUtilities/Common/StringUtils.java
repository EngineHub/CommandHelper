package com.laytonsmith.PureUtilities.Common;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public final class StringUtils {

	private StringUtils() {
		//
	}

	/**
	 * Joins a map together (using StringBuilder's {
	 *
	 * @see StringBuilder#append(Object)} method to "toString" the Object)
	 *
	 * @param map The map to concatenate
	 * @param entryGlue The glue to use between the key and value of each pair in the map
	 * @param elementGlue The glue to use between each key-value element pairs in the map
	 * @return The concatenated string
	 */
	public static String Join(Map map, String entryGlue, String elementGlue) {
		return Join(map, entryGlue, elementGlue, null, null, null);
	}

	/**
	 * Joins a map together (using StringBuilder's {
	 *
	 * @see StringBuilder#append(Object)} method to "toString" the Object)
	 *
	 * @param map The map to concatenate
	 * @param entryGlue The glue to use between the key and value of each pair in the map
	 * @param elementGlue The glue to use between each key-value element pairs in the map
	 * @param lastElementGlue The glue for the last two elements
	 * @param glueForTwoItems If only two items are in the map, then this glue is used instead. If it is null, then
	 * lastElementGlue is used instead.
	 * @param empty If the map is completely empty, this string is simply returned. If null, an empty string is used.
	 * @return The concatenated string
	 */
	public static String Join(Map map, String entryGlue, String elementGlue, String lastElementGlue) {
		return Join(map, entryGlue, elementGlue, lastElementGlue, null, null);
	}

	/**
	 * Joins a map together (using StringBuilder's {
	 *
	 * @see StringBuilder#append(Object)} method to "toString" the Object)
	 *
	 * @param map The map to concatenate
	 * @param entryGlue The glue to use between the key and value of each pair in the map
	 * @param elementGlue The glue to use between each key-value element pairs in the map
	 * @param lastElementGlue The glue for the last two elements
	 * @param glueForTwoItems If only two items are in the map, then this glue is used instead. If it is null, then
	 * lastElementGlue is used instead.
	 * @return The concatenated string
	 */
	public static String Join(Map map, String entryGlue, String elementGlue, String lastElementGlue, String elementGlueForTwoItems) {
		return Join(map, entryGlue, elementGlue, lastElementGlue, elementGlueForTwoItems, null);
	}

	/**
	 * Joins a map together (using StringBuilder's {
	 *
	 * @see StringBuilder#append(Object)} method to "toString" the Object)
	 *
	 * @param map The map to concatenate
	 * @param entryGlue The glue to use between the key and value of each pair in the map
	 * @param elementGlue The glue to use between each key-value element pairs in the map
	 * @param lastElementGlue The glue for the last two elements
	 * @param glueForTwoItems If only two items are in the map, then this glue is used instead. If it is null, then
	 * lastElementGlue is used instead.
	 * @param empty If the map is completely empty, this string is simply returned. If null, an empty string is used.
	 * @return The concatenated string
	 */
	public static String Join(Map map, String entryGlue, String elementGlue, String lastElementGlue, String elementGlueForTwoItems, String empty) {
		//Just create a list of glued together entries, then send it to the other Join method
		List<String> list = new ArrayList<String>();
		for(Object key : map.keySet()) {
			StringBuilder b = new StringBuilder();
			b.append(key).append(entryGlue).append(map.get(key));
			list.add(b.toString());
		}
		return Join(list, elementGlue, lastElementGlue, elementGlueForTwoItems, empty);
	}

	/**
	 * Joins a set together (using StringBuilder's {
	 *
	 * @see StringBuilder#append(Object)} method to "toString" the Object) using the specified string for glue.
	 * @param list The set to concatenate
	 * @param glue The glue to use
	 * @return The concatenated string
	 */
	public static <T> String Join(Set<T> set, String glue) {
		return Join(set, glue, null, null, null);
	}

	/**
	 * Joins a set together, rendering each item with the custom renderer.
	 * @param set
	 * @param glue
	 * @param r
	 * @return
	 */
	public static <T> String Join(Set<T> set, String glue, Renderer<T> r) {
		return Join(set, glue, null, null, null, r);
	}

	/**
	 * Joins a set together (using StringBuilder's {
	 *
	 * @see StringBuilder#append(Object)} method to "toString" the Object) using the specified string for glue. If
	 * lastGlue is null, it is the same as glue, but otherwise it is used to glue just the last two items together,
	 * which is useful for sets that are being read by a human, to have a proper conjunction at the end.
	 * @param list The set to concatenate
	 * @param glue The glue to use
	 * @param lastGlue The glue for the last two elements
	 * @return The concatenated string
	 */
	public static String Join(Set set, String glue, String lastGlue) {
		return Join(set, glue, lastGlue, null, null);
	}

	/**
	 * Joins a set together (using StringBuilder's {
	 *
	 * @see StringBuilder#append(Object)} method to "toString" the Object) using the specified string for glue. If
	 * lastGlue is null, it is the same as glue, but otherwise it is used to glue just the last two items together,
	 * which is useful for sets that are being read by a human, to have a proper conjunction at the end.
	 * @param list The set to concatenate
	 * @param glue The glue to use
	 * @param lastGlue The glue for the last two elements
	 * @param glueForTwoItems If only two items are in the set, then this glue is used instead. If it is null, then
	 * lastGlue is used instead.
	 * @return The concatenated string
	 */
	public static String Join(Set set, String glue, String lastGlue, String glueForTwoItems) {
		return Join(set, glue, lastGlue, glueForTwoItems, null);
	}

	/**
	 * Joins a set together (using StringBuilder's {
	 *
	 * @see StringBuilder#append(Object)} method to "toString" the Object) using the specified string for glue. If
	 * lastGlue is null, it is the same as glue, but otherwise it is used to glue just the last two items together,
	 * which is useful for sets that are being read by a human, to have a proper conjunction at the end.
	 * @param list The set to concatenate
	 * @param glue The glue to use
	 * @param lastGlue The glue for the last two elements
	 * @param glueForTwoItems If only two items are in the set, then this glue is used instead. If it is null, then
	 * lastGlue is used instead.
	 * @param empty If the set is completely empty, this string is simply returned. If null, an empty string is used.
	 * @return The concatenated string
	 */
	public static <T> String Join(Set<T> set, String glue, String lastGlue, String glueForTwoItems, String empty) {
		return Join(set, glue, lastGlue, glueForTwoItems, empty, null);
	}

	/**
	 * Joins a set together (using StringBuilder's {
	 *
	 * @see StringBuilder#append(Object)} method to "toString" the Object) using the specified string for glue. If
	 * lastGlue is null, it is the same as glue, but otherwise it is used to glue just the last two items together,
	 * which is useful for sets that are being read by a human, to have a proper conjunction at the end.
	 * @param list The set to concatenate
	 * @param glue The glue to use
	 * @param lastGlue The glue for the last two elements
	 * @param glueForTwoItems If only two items are in the set, then this glue is used instead. If it is null, then
	 * lastGlue is used instead.
	 * @param empty If the set is completely empty, this string is simply returned. If null, an empty string is used.
	 * @return The concatenated string
	 */
	public static <T> String Join(Set<T> set, String glue, String lastGlue, String glueForTwoItems, String empty, Renderer<T> renderer) {
		final List<T> list = new ArrayList<T>(set);
		return doJoin(new ItemGetter<T>() {

			@Override
			public T get(int index) {
				return list.get(index);
			}

			@Override
			public int size() {
				return list.size();
			}

			@Override
			public boolean isEmpty() {
				return list.isEmpty();
			}
		}, glue, lastGlue, glueForTwoItems, empty, renderer);
	}

	/**
	 * Joins an array together (using StringBuilder's {
	 *
	 * @see StringBuilder#append(Object)} method to "toString" the Object) using the specified string for glue.
	 * @param list The array to concatenate
	 * @param glue The glue to use
	 * @return The concatenated string
	 */
	public static String Join(Object[] list, String glue) {
		return Join(list, glue, null, null, null);
	}

	/**
	 * Joins an array together (using StringBuilder's {
	 *
	 * @see StringBuilder#append(Object)} method to "toString" the Object) using the specified string for glue. If
	 * lastGlue is null, it is the same as glue, but otherwise it is used to glue just the last two items together,
	 * which is useful for lists that are being read by a human, to have a proper conjunction at the end.
	 * @param list The array to concatenate
	 * @param glue The glue to use
	 * @param lastGlue The glue for the last two elements
	 * @return The concatenated string
	 */
	public static String Join(Object[] list, String glue, String lastGlue) {
		return Join(list, glue, lastGlue, null, null);
	}

	/**
	 * Joins an array together (using StringBuilder's {
	 *
	 * @see StringBuilder#append(Object)} method to "toString" the Object) using the specified string for glue. If
	 * lastGlue is null, it is the same as glue, but otherwise it is used to glue just the last two items together,
	 * which is useful for lists that are being read by a human, to have a proper conjunction at the end.
	 * @param list The array to concatenate
	 * @param glue The glue to use
	 * @param lastGlue The glue for the last two elements
	 * @param glueForTwoItems If only two items are in the array, then this glue is used instead. If it is null, then
	 * lastGlue is used instead.
	 * @return The concatenated string
	 */
	public static String Join(Object[] list, String glue, String lastGlue, String glueForTwoItems) {
		return Join(list, glue, lastGlue, glueForTwoItems, null);
	}

	/**
	 * Joins an array together (using StringBuilder's {
	 *
	 * @see StringBuilder#append(Object)} method to "toString" the Object) using the specified string for glue. If
	 * lastGlue is null, it is the same as glue, but otherwise it is used to glue just the last two items together,
	 * which is useful for lists that are being read by a human, to have a proper conjunction at the end.
	 * @param list The array to concatenate
	 * @param glue The glue to use
	 * @param lastGlue The glue for the last two elements
	 * @param glueForTwoItems If only two items are in the array, then this glue is used instead. If it is null, then
	 * lastGlue is used instead.
	 * @param empty If the array is completely empty, this string is simply returned. If null, an empty string is used.
	 * @return The concatenated string
	 */
	public static String Join(Object[] list, String glue, String lastGlue, String glueForTwoItems, String empty) {
		return Join(list, glue, lastGlue, glueForTwoItems, empty, null);
	}

	/**
	 * Joins an array together (using StringBuilder's {
	 *
	 * @param <T> The array type
	 * @see StringBuilder#append(Object)} method to "toString" the Object) using the specified string for glue. If
	 * lastGlue is null, it is the same as glue, but otherwise it is used to glue just the last two items together,
	 * which is useful for lists that are being read by a human, to have a proper conjunction at the end.
	 * @param list The array to concatenate
	 * @param glue The glue to use
	 * @param lastGlue The glue for the last two elements
	 * @param glueForTwoItems If only two items are in the array, then this glue is used instead. If it is null, then
	 * lastGlue is used instead.
	 * @param empty If the array is completely empty, this string is simply returned. If null, an empty string is used.
	 * @param renderer The item renderer. This renders each item in the list, one at a time. If null, toString will be
	 * used by default on each item.
	 * @return The concatenated string
	 */
	public static <T> String Join(final T[] list, String glue, String lastGlue, String glueForTwoItems, String empty, Renderer<T> renderer) {
		return doJoin(new ItemGetter<T>() {

			@Override
			public T get(int index) {
				return list[index];
			}

			@Override
			public int size() {
				return list.length;
			}

			@Override
			public boolean isEmpty() {
				return list.length == 0;
			}
		}, glue, lastGlue, glueForTwoItems, empty, renderer);
	}

	/**
	 * Joins a list together (using StringBuilder's {
	 *
	 * @see StringBuilder#append(Object)} method to "toString" the Object) using the specified string for glue.
	 * @param list The list to concatenate
	 * @param glue The glue to use
	 * @return The concatenated string
	 */
	public static String Join(List list, String glue) {
		return Join(list, glue, null, null, null);
	}

	/**
	 * Joins a list together (using StringBuilder's {
	 *
	 * @see StringBuilder#append(Object)} method to "toString" the Object) using the specified string for glue. If
	 * lastGlue is null, it is the same as glue, but otherwise it is used to glue just the last two items together,
	 * which is useful for lists that are being read by a human, to have a proper conjunction at the end.
	 * @param list The list to concatenate
	 * @param glue The glue to use
	 * @param lastGlue The glue for the last two elements
	 * @return The concatenated string
	 */
	public static String Join(List list, String glue, String lastGlue) {
		return Join(list, glue, lastGlue, null, null);
	}

	/**
	 * Joins a list together (using StringBuilder's {
	 *
	 * @see StringBuilder#append(Object)} method to "toString" the Object) using the specified string for glue. If
	 * lastGlue is null, it is the same as glue, but otherwise it is used to glue just the last two items together,
	 * which is useful for lists that are being read by a human, to have a proper conjunction at the end.
	 * @param list The list to concatenate
	 * @param glue The glue to use
	 * @param lastGlue The glue for the last two elements
	 * @param glueForTwoItems If only two items are in the list, then this glue is used instead. If it is null, then
	 * lastGlue is used instead.
	 * @return The concatenated string
	 */
	public static String Join(List list, String glue, String lastGlue, String glueForTwoItems) {
		return Join(list, glue, lastGlue, glueForTwoItems, null);
	}

	/**
	 * Joins a list together (using StringBuilder's {
	 *
	 * @see StringBuilder#append(Object)} method to "toString" the Object) using the specified string for glue. If
	 * lastGlue is null, it is the same as glue, but otherwise it is used to glue just the last two items together,
	 * which is useful for lists that are being read by a human, to have a proper conjunction at the end.
	 * @param list The list to concatenate
	 * @param glue The glue to use
	 * @param lastGlue The glue for the last two elements
	 * @param glueForTwoItems If only two items are in the list, then this glue is used instead. If it is null, then
	 * lastGlue is used instead.
	 * @param empty If the list is completely empty, this string is simply returned. If null, an empty string is used.
	 * @return The concatenated string
	 */
	public static String Join(final List list, String glue, String lastGlue, String glueForTwoItems, String empty) {
		return Join(list, glue, lastGlue, glueForTwoItems, empty, null);
	}

	/**
	 * Joins a list together (using StringBuilder's {
	 *
	 * @see StringBuilder#append(Object)} method to "toString" the Object) using the specified string for glue. If
	 * lastGlue is null, it is the same as glue, but otherwise it is used to glue just the last two items together,
	 * which is useful for lists that are being read by a human, to have a proper conjunction at the end.
	 * @param list The list to concatenate
	 * @param glue The glue to use
	 * @param lastGlue The glue for the last two elements
	 * @param glueForTwoItems If only two items are in the list, then this glue is used instead. If it is null, then
	 * lastGlue is used instead.
	 * @param empty If the list is completely empty, this string is simply returned. If null, an empty string is used.
	 * @param renderer The item renderer. This renders each item in the list, one at a time. If null, toString will be
	 * used by default on each item.
	 * @return The concatenated string
	 */
	public static <T> String Join(final List<T> list, String glue, String lastGlue, String glueForTwoItems, String empty, Renderer<T> renderer) {
		return doJoin(new ItemGetter<T>() {

			@Override
			public T get(int index) {
				return list.get(index);
			}

			@Override
			public int size() {
				return list.size();
			}

			@Override
			public boolean isEmpty() {
				return list.isEmpty();
			}
		}, glue, lastGlue, glueForTwoItems, empty, renderer);
	}

	/**
	 * Abstracted version of the join algorithm.
	 *
	 * @param <T>
	 * @param items
	 * @param glue
	 * @param lastGlue
	 * @param glueForTwoItems
	 * @param empty
	 * @param renderer
	 * @return
	 */
	private static <T> String doJoin(ItemGetter<T> items, String glue, String lastGlue, String glueForTwoItems, String empty, Renderer<T> renderer) {
		if(renderer == null) {
			renderer = new Renderer<T>() {

				@Override
				public String toString(T item) {
					if(item == null) {
						return "null";
					} else {
						return item.toString();
					}
				}
			};
		}
		if(lastGlue == null) {
			lastGlue = glue;
		}
		if(glueForTwoItems == null) {
			glueForTwoItems = lastGlue;
		}
		if(items.isEmpty()) {
			return empty == null ? "" : empty;
		} else if(items.size() == 2) {
			StringBuilder b = new StringBuilder();
			return b.append(renderer.toString(items.get(0)))
					.append(glueForTwoItems)
					.append(renderer.toString(items.get(1))).toString();
		} else {
			StringBuilder b = new StringBuilder();
			for(int i = 0; i < items.size(); i++) {
				T o = items.get(i);
				if(i != 0) {
					if(i == items.size() - 1) {
						b.append(lastGlue);
					} else {
						b.append(glue);
					}
				}
				b.append(renderer.toString(o));
			}
			return b.toString();
		}
	}

	private static interface ItemGetter<T> {

		T get(int index);

		int size();

		boolean isEmpty();
	}

	/**
	 * Used to provide a renderer for each item when glueing the items together.
	 *
	 * @param <T> The type of each item
	 */
	public static interface Renderer<T> {

		/**
		 *
		 * @param item
		 * @return
		 */
		String toString(T item);
	}

	private static int minimum(int a, int b, int c) {
		return Math.min(Math.min(a, b), c);
	}

	/**
	 * Returns the levenshtein distance of two character sequences. For instance, "123" and "133" would have a string
	 * distance of 1, while "123" and "123" would be 0, since they are the same string.
	 *
	 * @param str1
	 * @param str2
	 * @return
	 */
	public static int LevenshteinDistance(CharSequence str1,
			CharSequence str2) {
		int[][] distance = new int[str1.length() + 1][str2.length() + 1];

		for(int i = 0; i <= str1.length(); i++) {
			distance[i][0] = i;
		}
		for(int j = 0; j <= str2.length(); j++) {
			distance[0][j] = j;
		}

		for(int i = 1; i <= str1.length(); i++) {
			for(int j = 1; j <= str2.length(); j++) {
				distance[i][j] = minimum(
						distance[i - 1][j] + 1,
						distance[i][j - 1] + 1,
						distance[i - 1][j - 1]
						+ ((str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0
						: 1));
			}
		}

		return distance[str1.length()][str2.length()];
	}

	/**
	 * Splits an argument string into arguments. It is expected that the string:
	 *
	 * <code>this is "a 'quoted'" '\'string\''</code>
	 *
	 * would parse into 4 arguments, individually, "this", "is", "a 'quoted'", "'string'". It essentially handles the
	 * very basic case of command line argument parsing.
	 *
	 * @param args
	 * @return
	 */
	public static List<String> ArgParser(String args) {
		List<String> arguments = new ArrayList<>();
		StringBuilder buf = new StringBuilder();
		char escape = 0;
		char quote = 0;
		boolean wasQuote = false;
		for(int i = 0; i < args.length(); i++) {
			char ch = args.charAt(i);
			char ch2 = 0;
			if(args.length() > i + 1) {
				ch2 = args.charAt(i + 1);
			}
			if(quote != 0) {  // we're in a quote
				if(escape != 0) {  // we're in an escape too
					if(ch == quote) {  // escaping the same quote gives just that quote
						buf.append(ch);
					} else {  // escaping anything else gives the escape and char as written
						buf.append(escape);
						buf.append(quote);
					}
					// in either case, this terminates the escape.
					escape = 0;
					continue;
				} else if(ch == quote) {  // Specifying the same quote again terminates the quote.
					quote = 0;
					wasQuote = true;
					continue;
				}
			} else if(escape != 0) {
				// all escapes outside quotes which are supported simply output the
				// second character, as we aren't handling special ones like \t or \n
				buf.append(ch);
				escape = 0;
				continue;
			} else { // outside of quotes and escapes
				switch(ch) {
					case ' ':  // we can tokenize
						if(wasQuote || buf.length() != 0) {
							arguments.add(buf.toString());
							buf = new StringBuilder();
							wasQuote = false;
						}
						continue;
					case '"':  // we can start quotes
					case '\'':
						quote = ch;
						continue;
				}
			}
			// escape handling and default handling can fall through from either branch to here
			if(ch == '\\' && ch2 == quote) {
				buf.append(ch2);
				i++;
			} else {
				buf.append(ch);
			}
//			switch(ch) {
//				case '\\':
//					escape = ch;
//					break;
//				default:
//					buf.append(ch);
//			}
		}
		if(escape != 0) {  // makes trailing escapes be appended (erroneous string, though, IMO)
			buf.append(escape);
		}
		if(wasQuote || buf.length() != 0) {  // add the final string
			arguments.add(buf.toString());
		}
		return arguments;
	}

	public static String trimLeft(String str) {
		//If the argument is null then return empty string
		if(str == null) {
			return "";
		}

		/* The charAt method returns the character at a particular position in a String.
		 * We check to see if the character at position 0 (the leading character) is a space.
		 * If it is, use substring to make a new String that starts after the space.
		 */
		int len = 0;
		while(str.charAt(len) == ' ') {
			len++;
		}
		return str.substring(len);
	}

	public static String trimRight(String str) {
		//If the argument is null then return empty string
		if(str == null) {
			return "";
		}

		/* The logic for Rtrim is, While the last character in the String is a space, remove it.
		 * In the code, take the length of the string and use it to determine if the last character is a space.
		 */
		int len = str.length();
		while(len > 0 && str.charAt(len - 1) == ' ') {
			len--;
		}
		str = str.substring(0, len);
		return str;
	}

	/**
	 * Works like String.split(), but trims each of the entries also.
	 *
	 * @param string
	 * @param regex
	 * @return
	 */
	public static String[] trimSplit(String string, String regex) {
		String[] split = string.split(regex);
		for(int i = 0; i < split.length; i++) {
			split[i] = split[i].trim();
		}
		return split;
	}

	/**
	 * Works like String.replaceFirst, but replaces the last instance instead.
	 *
	 * @param string
	 * @param regex
	 * @param replacement
	 * @return
	 */
	public static String replaceLast(String string, String regex, String replacement) {
		if(regex == null) {
			return string;
		}
		if(string == null) {
			return null;
		}
		if(regex.length() > string.length()) {
			//It can't be contained in here
			return string;
		}
		Matcher m = Pattern.compile(regex).matcher(string);
		int start = -1;
		int end = -1;
		while(m.find()) {
			start = m.start();
			end = m.end();
		}
		if(start == -1 || end == -1) {
			//Didn't find it, return the whole string
			return string;
		} else {
			return string.substring(0, start) + replacement + string.substring(end, string.length());
		}
	}

	/**
	 * Convenience method for HumanReadableByteCount(bytes, true).
	 *
	 * @param bytes The total number of bytes.
	 * @return The number of bytes, rounded to the nearest uppermost unit. For instance, 1024 will return "1.0 kB"
	 */
	public static String HumanReadableByteCount(long bytes) {
		return HumanReadableByteCount(bytes, true);
	}

	/**
	 * Returns a human readable byte count, given a byte count.
	 *
	 * @param bytes The total number of bytes.
	 * @param si If true, the unit division is 1000, if false, it's 1024.
	 * @return The number of bytes, rounded to the nearest uppermost unit. For instance, 1024 will return "1.0 kiB" or
	 * "1.0 kB" if si is true.
	 */
	public static String HumanReadableByteCount(long bytes, boolean si) {
		int unit = si ? 1000 : 1024;
		if(bytes < unit) {
			return bytes + " B";
		}
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

	/**
	 * Returns a properly agreeing subject verb clause given a count, and singular subject. This version assumes that
	 * the plural subject can be made simply by appending <code>s</code> to the singular subject, which is not always
	 * true. This is useful in cases where forming a sentence requires different wording depending on the count.
	 * Usually, you might use a fairly complex tertiary statement, for instance: <code>String message = "There " + (count==1?"is":"are") +
	 * " " + count + " test failure" + (count==1?"":"s");</code> This is time consuming, and easy to mess up or
	 * accidentally reverse. Instead, you can use this function. Note that this will add <code>is</code> or
	 * </code>are</code> for you. You need only to provide the count, singular subject, and plural subject. If the
	 * subject cannot be made plural with just an <code>s</code>, use
	 * {@link #PluralHelper(int, java.lang.String, java.lang.String)} instead. Usage example:
	 *
	 * <pre>
	 * String message = "There " + PluralHelper(count, "test failure");
	 * //If count is 1: There is 1 test failure
	 * //If count is not 1: There are 2 test failures
	 * </pre>
	 *
	 * @param count The count of items
	 * @param singular The subject of the sentence, as a singular
	 * @return The properly formatted clause.
	 */
	public static String PluralHelper(int count, String singular) {
		return PluralHelper(count, singular, singular + "s");
	}

	/**
	 * Returns a properly agreeing subject verb clause given a count, singular subject, and plural subject. This is
	 * useful in cases where forming a sentence requires different wording depending on the count. Usually, you might
	 * use a fairly complex tertiary statement, for instance: <code>String message = "There " + (count==1?"is":"are") +
	 * " " + count + " test failure" + (count==1?"":"s");</code> This is time consuming, and easy to mess up or
	 * accidentally reverse. Instead, you can use this function. Note that this will add <code>is</code> or
	 * </code>are</code> for you. You need only to provide the count, singular subject, and plural subject. If the
	 * subject can be made plural with just an <code>s</code>, use {@link #PluralHelper(int, java.lang.String)} instead.
	 * Usage example:
	 *
	 * <pre>
	 * String message = "There " + PluralHelper(count, "fish", "fish");
	 * //If count is 1: There is 1 fish
	 * //If count is not 1: There are 2 fish
	 * </pre>
	 *
	 * @param count The count of items
	 * @param singular The subject of the sentence, as a singular
	 * @param plural The subject of the sentence, as a plural
	 * @return The properly formatted clause.
	 */
	public static String PluralHelper(int count, String singular, String plural) {
		return (count == 1 ? "is" : "are") + " " + count + " " + (count == 1 ? singular : plural);
	}

	/**
	 * For even more complex sentences, it may just be easiest to provide a template, which will be replaced, if the
	 * count is singular or plural. Both singularTemplate and pluralTemplate are expected to be String.format templates
	 * with a %d in them, which will be replaced with the actual count number. If the count == 1, then the
	 * singularTemplate will be used, else the pluralTemplate will be used. Usage example:
	 *
	 * <pre>
	 * String message = PluralTemplateHelper(count, "I will buy %d car if it has a good price",
	 * "I will buy %d cars if they have a good price");
	 * </pre>
	 *
	 * @param count The count of items
	 * @param singularTemplate The singular template
	 * @param pluralTemplate The plural template
	 * @return
	 */
	public static String PluralTemplateHelper(int count, String singularTemplate, String pluralTemplate) {
		if(count == 1) {
			return String.format(singularTemplate, count);
		} else {
			return String.format(pluralTemplate, count);
		}
	}

	/**
	 * This is the system newline string. For instance, on windows, this would likely be \r\n, and unix systems would
	 * likely be \n.
	 */
	public static final String NL = System.getProperty("line.separator");

	/**
	 * @deprecated Use {@link #NL} instead.
	 */
	@SuppressWarnings("checkstyle:constantname") // Fixing this violation might break dependents.
	@Deprecated // Deprecated on 14-06-2018 dd-mm-yyyy.
	public static final String nl = NL;

	/**
	 * This returns the system newline string. For instance, on windows, this would likely return \r\n, and unix systems
	 * would likely return \n.
	 *
	 * @return The system newline string.
	 */
	public static String nl() {
		return NL;
	}

	/**
	 * Multiplies a string. For instance, stringMultiply(3, "abc") would return "abcabcabc". If count is 0, an empty
	 * string is returned, and if count is 1, the character sequence itself is returned.
	 *
	 * @param count The repeat count
	 * @param s The sequence to repeat
	 * @return The multiplied string
	 * @throws IllegalArgumentException If count is less than 0.
	 */
	public static String stringMultiply(int count, CharSequence s) {
		if(count < 0) {
			throw new IllegalArgumentException("Count must be greater than or equal to 0");
		}
		if(count == 0) {
			return "";
		}
		if(count == 1) {
			return s.toString();
		}
		//Ok, actually have to do the multiply now.
		StringBuilder b = new StringBuilder(s.length() * count);
		for(int i = 0; i < count; i++) {
			b.append(s);
		}
		return b.toString();
	}

	/**
	 * Given a string, returns a string that could be printed out in Java source code. That is, all escapable characters
	 * are reversed. The returned string will already be surrounded by quotes.
	 *
	 * @param s
	 * @return
	 */
	public static String toCodeString(String s) {
		return "\"" + s.replace("\\", "\\\\")
				.replace("\"", "\\\"")
				.replace("\n", "\\n")
				.replace("\t", "\\t") + "\"";
	}

	/**
	 * Takes a byte array, and returns a string hex representation.
	 *
	 * @param bytes
	 * @return
	 */
	public static String toHex(byte[] bytes) {
		BigInteger bi = new BigInteger(1, bytes);
		return String.format("%0" + (bytes.length << 1) + "X", bi);
	}

	/**
	 * Splits a string on word boundries.
	 *
	 * @param text
	 * @param len
	 * @return
	 */
	public static List<String> lineSplit(String text, int len) {
		// return empty array for null text
		if(text == null) {
			return new ArrayList<>();
		}

		// return text if len is zero or less
		// or text is less than length
		if(len <= 0 || text.length() <= len) {
			return new ArrayList<>(Arrays.asList(new String[]{text}));
		}

		char[] chars = text.toCharArray();
		List<String> lines = new ArrayList<>();
		StringBuilder line = new StringBuilder();
		StringBuilder word = new StringBuilder();

		for(int i = 0; i < chars.length; i++) {
			word.append(chars[i]);

			if(chars[i] == ' ') {
				if((line.length() + word.length()) > len) {
					lines.add(line.toString());
					line.delete(0, line.length());
				}

				line.append(word);
				word.delete(0, word.length());
			}
		}

		// handle any extra chars in current word
		if(word.length() > 0) {
			if((line.length() + word.length()) > len) {
				lines.add(line.toString());
				line.delete(0, line.length());
			}
			line.append(word);
		}

		// handle extra line
		if(line.length() > 0) {
			lines.add(line.toString());
		}

		return lines;
	}

	/**
	 * Calls {@link #lineWrap(java.lang.String, int, java.lang.String, boolean)} with newline string as \n, and
	 * wrapLongWords true.
	 *
	 * @param str The string to word wrap
	 * @param wrapLength The max length of the line
	 */
	public static String lineWrap(String str, int wrapLength) {
		return lineWrap(str, wrapLength, "\n", true);
	}

	/**
	 * <p>
	 * Wraps a single line of text, identifying words by <code>' '</code>.</p>
	 *
	 * <p>
	 * Leading spaces on a new line are stripped. Trailing spaces are not stripped.</p>
	 *
	 * <pre>
	 * WordUtils.wrap(null, *, *, *) = null
	 * WordUtils.wrap("", *, *, *) = ""
	 * </pre>
	 *
	 * (Code from org.apache.commons.lang.WordUtils and slightly modified)
	 *
	 * @param str the String to be word wrapped, may be null
	 * @param wrapLength the column to wrap the words at, less than 1 is treated as 1
	 * @param newLineStr the string to insert for a new line, <code>null</code> uses the system property line separator
	 * @param wrapLongWords true if long words (such as URLs) should be wrapped
	 * @return a line with newlines inserted, <code>null</code> if null input
	 */
	public static String lineWrap(String str, int wrapLength, String newLineStr, boolean wrapLongWords) {
		if(str == null) {
			return null;
		}
		if(newLineStr == null) {
			newLineStr = OSUtils.GetLineEnding();
		}
		if(wrapLength < 1) {
			wrapLength = 1;
		}
		int inputLineLength = str.length();
		int offset = 0;
		StringBuilder wrappedLine = new StringBuilder(inputLineLength + 32);

		while((inputLineLength - offset) > wrapLength) {
			if(str.charAt(offset) == ' ') {
				offset++;
				continue;
			}
			int spaceToWrapAt = str.lastIndexOf(' ', wrapLength + offset);

			if(spaceToWrapAt >= offset) {
				// normal case
				wrappedLine.append(str.substring(offset, spaceToWrapAt));
				wrappedLine.append(newLineStr);
				offset = spaceToWrapAt + 1;

			} else {
				// really long word or URL
				if(wrapLongWords) {
					// wrap really long word one line at a time
					wrappedLine.append(str.substring(offset, wrapLength + offset));
					wrappedLine.append(newLineStr);
					offset += wrapLength;
				} else {
					// do not wrap really long word, just extend beyond limit
					spaceToWrapAt = str.indexOf(' ', wrapLength + offset);
					if(spaceToWrapAt >= 0) {
						wrappedLine.append(str.substring(offset, spaceToWrapAt));
						wrappedLine.append(newLineStr);
						offset = spaceToWrapAt + 1;
					} else {
						wrappedLine.append(str.substring(offset));
						offset = inputLineLength;
					}
				}
			}
		}

		// Whatever is left in line is short enough to just pass through
		wrappedLine.append(str.substring(offset));

		return wrappedLine.toString();
	}

	/**
	 * Works like {@link String#contains(java.lang.CharSequence)}, except case is ignored.
	 *
	 * @param container
	 * @param contains
	 * @return
	 */
	public static boolean containsIgnoreCase(String container, String contains) {
		return container.toLowerCase().contains(contains.toLowerCase());
	}
}
