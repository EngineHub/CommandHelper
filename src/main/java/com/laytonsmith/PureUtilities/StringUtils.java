package com.laytonsmith.PureUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author lsmith
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
	 * @param entryGlue The glue to use between the key and value of each pair
	 * in the map
	 * @param elementGlue The glue to use between each key-value element pairs
	 * in the map
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
	 * @param entryGlue The glue to use between the key and value of each pair
	 * in the map
	 * @param elementGlue The glue to use between each key-value element pairs
	 * in the map
	 * @param lastElementGlue The glue for the last two elements
	 * @param glueForTwoItems If only two items are in the map, then this glue
	 * is used instead. If it is null, then lastElementGlue is used instead.
	 * @param empty If the map is completely empty, this string is simply
	 * returned. If null, an empty string is used.
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
	 * @param entryGlue The glue to use between the key and value of each pair
	 * in the map
	 * @param elementGlue The glue to use between each key-value element pairs
	 * in the map
	 * @param lastElementGlue The glue for the last two elements
	 * @param glueForTwoItems If only two items are in the map, then this glue
	 * is used instead. If it is null, then lastElementGlue is used instead.
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
	 * @param entryGlue The glue to use between the key and value of each pair
	 * in the map
	 * @param elementGlue The glue to use between each key-value element pairs
	 * in the map
	 * @param lastElementGlue The glue for the last two elements
	 * @param glueForTwoItems If only two items are in the map, then this glue
	 * is used instead. If it is null, then lastElementGlue is used instead.
	 * @param empty If the map is completely empty, this string is simply
	 * returned. If null, an empty string is used.
	 * @return The concatenated string
	 */
	public static String Join(Map map, String entryGlue, String elementGlue, String lastElementGlue, String elementGlueForTwoItems, String empty) {
		//Just create a list of glued together entries, then send it to the other Join method
		List<String> list = new ArrayList<String>();
		for (Object key : map.keySet()) {
			StringBuilder b = new StringBuilder();
			b.append(key).append(entryGlue).append(map.get(key));
			list.add(b.toString());
		}
		return Join(list, elementGlue, lastElementGlue, elementGlueForTwoItems, empty);
	}

	/**
	 * Joins a set together (using StringBuilder's {
	 *
	 * @see StringBuilder#append(Object)} method to "toString" the Object) using
	 * the specified string for glue.
	 * @param list The set to concatenate
	 * @param glue The glue to use
	 * @return The concatenated string
	 */
	public static String Join(Set set, String glue) {
		return Join(set, glue, null, null, null);
	}

	/**
	 * Joins a set together (using StringBuilder's {
	 *
	 * @see StringBuilder#append(Object)} method to "toString" the Object) using
	 * the specified string for glue. If lastGlue is null, it is the same as
	 * glue, but otherwise it is used to glue just the last two items together,
	 * which is useful for sets that are being read by a human, to have a proper
	 * conjunction at the end.
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
	 * @see StringBuilder#append(Object)} method to "toString" the Object) using
	 * the specified string for glue. If lastGlue is null, it is the same as
	 * glue, but otherwise it is used to glue just the last two items together,
	 * which is useful for sets that are being read by a human, to have a proper
	 * conjunction at the end.
	 * @param list The set to concatenate
	 * @param glue The glue to use
	 * @param lastGlue The glue for the last two elements
	 * @param glueForTwoItems If only two items are in the set, then this glue
	 * is used instead. If it is null, then lastGlue is used instead.
	 * @return The concatenated string
	 */
	public static String Join(Set set, String glue, String lastGlue, String glueForTwoItems) {
		return Join(set, glue, lastGlue, glueForTwoItems, null);
	}

	/**
	 * Joins a set together (using StringBuilder's {
	 *
	 * @see StringBuilder#append(Object)} method to "toString" the Object) using
	 * the specified string for glue. If lastGlue is null, it is the same as
	 * glue, but otherwise it is used to glue just the last two items together,
	 * which is useful for sets that are being read by a human, to have a proper
	 * conjunction at the end.
	 * @param list The set to concatenate
	 * @param glue The glue to use
	 * @param lastGlue The glue for the last two elements
	 * @param glueForTwoItems If only two items are in the set, then this glue
	 * is used instead. If it is null, then lastGlue is used instead.
	 * @param empty If the set is completely empty, this string is simply
	 * returned. If null, an empty string is used.
	 * @return The concatenated string
	 */
	public static String Join(Set set, String glue, String lastGlue, String glueForTwoItems, String empty) {
		List list = new ArrayList(set);
		if (lastGlue == null) {
			lastGlue = glue;
		}
		if (glueForTwoItems == null) {
			glueForTwoItems = lastGlue;
		}
		if (list.isEmpty()) {
			return empty == null ? "" : empty;
		} else if (list.size() == 2) {
			StringBuilder b = new StringBuilder();
			return b.append(list.get(0)).append(glueForTwoItems).append(list.get(1)).toString();
		} else {
			StringBuilder b = new StringBuilder();
			for (int i = 0; i < list.size(); i++) {
				Object o = list.get(i);
				if (i != 0) {
					if (i == list.size() - 1) {
						b.append(lastGlue);
					} else {
						b.append(glue);
					}
				}
				b.append(o);
			}
			return b.toString();
		}
	}

	/**
	 * Joins an array together (using StringBuilder's {
	 *
	 * @see StringBuilder#append(Object)} method to "toString" the Object) using
	 * the specified string for glue.
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
	 * @see StringBuilder#append(Object)} method to "toString" the Object) using
	 * the specified string for glue. If lastGlue is null, it is the same as
	 * glue, but otherwise it is used to glue just the last two items together,
	 * which is useful for lists that are being read by a human, to have a
	 * proper conjunction at the end.
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
	 * @see StringBuilder#append(Object)} method to "toString" the Object) using
	 * the specified string for glue. If lastGlue is null, it is the same as
	 * glue, but otherwise it is used to glue just the last two items together,
	 * which is useful for lists that are being read by a human, to have a
	 * proper conjunction at the end.
	 * @param list The array to concatenate
	 * @param glue The glue to use
	 * @param lastGlue The glue for the last two elements
	 * @param glueForTwoItems If only two items are in the array, then this glue
	 * is used instead. If it is null, then lastGlue is used instead.
	 * @return The concatenated string
	 */
	public static String Join(Object[] list, String glue, String lastGlue, String glueForTwoItems) {
		return Join(list, glue, lastGlue, glueForTwoItems, null);
	}

	/**
	 * Joins an array together (using StringBuilder's {
	 *
	 * @see StringBuilder#append(Object)} method to "toString" the Object) using
	 * the specified string for glue. If lastGlue is null, it is the same as
	 * glue, but otherwise it is used to glue just the last two items together,
	 * which is useful for lists that are being read by a human, to have a
	 * proper conjunction at the end.
	 * @param list The array to concatenate
	 * @param glue The glue to use
	 * @param lastGlue The glue for the last two elements
	 * @param glueForTwoItems If only two items are in the array, then this glue
	 * is used instead. If it is null, then lastGlue is used instead.
	 * @param empty If the array is completely empty, this string is simply
	 * returned. If null, an empty string is used.
	 * @return The concatenated string
	 */
	public static String Join(Object[] list, String glue, String lastGlue, String glueForTwoItems, String empty) {
		if (lastGlue == null) {
			lastGlue = glue;
		}
		if (glueForTwoItems == null) {
			glueForTwoItems = lastGlue;
		}
		if (list.length == 0) {
			return empty == null ? "" : empty;
		} else if (list.length == 2) {
			StringBuilder b = new StringBuilder();
			return b.append(list[0]).append(glueForTwoItems).append(list[1]).toString();
		} else {
			StringBuilder b = new StringBuilder();
			for (int i = 0; i < list.length; i++) {
				Object o = list[i];
				if (i != 0) {
					if (i == list.length - 1) {
						b.append(lastGlue);
					} else {
						b.append(glue);
					}
				}
				b.append(o);
			}
			return b.toString();
		}
	}

	/**
	 * Joins a list together (using StringBuilder's {
	 *
	 * @see StringBuilder#append(Object)} method to "toString" the Object) using
	 * the specified string for glue.
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
	 * @see StringBuilder#append(Object)} method to "toString" the Object) using
	 * the specified string for glue. If lastGlue is null, it is the same as
	 * glue, but otherwise it is used to glue just the last two items together,
	 * which is useful for lists that are being read by a human, to have a
	 * proper conjunction at the end.
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
	 * @see StringBuilder#append(Object)} method to "toString" the Object) using
	 * the specified string for glue. If lastGlue is null, it is the same as
	 * glue, but otherwise it is used to glue just the last two items together,
	 * which is useful for lists that are being read by a human, to have a
	 * proper conjunction at the end.
	 * @param list The list to concatenate
	 * @param glue The glue to use
	 * @param lastGlue The glue for the last two elements
	 * @param glueForTwoItems If only two items are in the list, then this glue
	 * is used instead. If it is null, then lastGlue is used instead.
	 * @return The concatenated string
	 */
	public static String Join(List list, String glue, String lastGlue, String glueForTwoItems) {
		return Join(list, glue, lastGlue, glueForTwoItems, null);
	}

	/**
	 * Joins a list together (using StringBuilder's {
	 *
	 * @see StringBuilder#append(Object)} method to "toString" the Object) using
	 * the specified string for glue. If lastGlue is null, it is the same as
	 * glue, but otherwise it is used to glue just the last two items together,
	 * which is useful for lists that are being read by a human, to have a
	 * proper conjunction at the end.
	 * @param list The list to concatenate
	 * @param glue The glue to use
	 * @param lastGlue The glue for the last two elements
	 * @param glueForTwoItems If only two items are in the list, then this glue
	 * is used instead. If it is null, then lastGlue is used instead.
	 * @param empty If the list is completely empty, this string is simply
	 * returned. If null, an empty string is used.
	 * @return The concatenated string
	 */
	public static String Join(List list, String glue, String lastGlue, String glueForTwoItems, String empty) {
		if (lastGlue == null) {
			lastGlue = glue;
		}
		if (glueForTwoItems == null) {
			glueForTwoItems = lastGlue;
		}
		if (list.isEmpty()) {
			return empty == null ? "" : empty;
		} else if (list.size() == 2) {
			StringBuilder b = new StringBuilder();
			return b.append(list.get(0)).append(glueForTwoItems).append(list.get(1)).toString();
		} else {
			StringBuilder b = new StringBuilder();
			for (int i = 0; i < list.size(); i++) {
				Object o = list.get(i);
				if (i != 0) {
					if (i == list.size() - 1) {
						b.append(lastGlue);
					} else {
						b.append(glue);
					}
				}
				b.append(o);
			}
			return b.toString();
		}
	}

	private static int minimum(int a, int b, int c) {
		return Math.min(Math.min(a, b), c);
	}

	/**
	 * Returns the levenshtein distance of two character sequences. For
	 * instance, "123" and "133" would have a string distance of 1, while "123"
	 * and "123" would be 0, since they are the same string.
	 *
	 * @param str1
	 * @param str2
	 * @return
	 */
	public static int LevenshteinDistance(CharSequence str1,
			CharSequence str2) {
		int[][] distance = new int[str1.length() + 1][str2.length() + 1];

		for (int i = 0; i <= str1.length(); i++) {
			distance[i][0] = i;
		}
		for (int j = 0; j <= str2.length(); j++) {
			distance[0][j] = j;
		}

		for (int i = 1; i <= str1.length(); i++) {
			for (int j = 1; j <= str2.length(); j++) {
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
	 * would parse into 4 arguments, individually, "this", "is", "a 'quoted'",
	 * "'string'". It essentially handles the very basic case of command line
	 * argument parsing.
	 *
	 * @param args
	 * @return
	 */
	public static List<String> ArgParser(String args) {
		//First, we have to tokenize the strings. Since we can have quoted arguments, we can't simply split on spaces.
		List<String> arguments = new ArrayList<String>();
		StringBuilder buf = new StringBuilder();
		boolean state_in_single_quote = false;
		boolean state_in_double_quote = false;
		for (int i = 0; i < args.length(); i++) {
			Character c0 = args.charAt(i);
			Character c1 = i + 1 < args.length() ? args.charAt(i + 1) : null;

			if (c0 == '\\') {
				if (c1 == '\'' && state_in_single_quote
						|| c1 == '"' && state_in_double_quote
						|| c1 == ' ' && !state_in_double_quote && !state_in_single_quote
						|| c1 == '\\' && (state_in_double_quote || state_in_single_quote)) {
					//We are escaping the next character. Add it to the buffer instead, and
					//skip ahead two
					buf.append(c1);
					i++;
					continue;
				}

			}

			if (c0 == ' ') {
				if (!state_in_double_quote && !state_in_single_quote) {
					//argument split
					if (buf.length() != 0) {
						arguments.add(buf.toString());
						buf = new StringBuilder();
					}
					continue;
				}
			}
			if (c0 == '\'' && !state_in_double_quote) {
				if (state_in_single_quote) {
					state_in_single_quote = false;
					arguments.add(buf.toString());
					buf = new StringBuilder();
				} else {
					if (buf.length() != 0) {
						arguments.add(buf.toString());
						buf = new StringBuilder();
					}
					state_in_single_quote = true;
				}
				continue;
			}
			if (c0 == '"' && !state_in_single_quote) {
				if (state_in_double_quote) {
					state_in_double_quote = false;
					arguments.add(buf.toString());
					buf = new StringBuilder();
				} else {
					if (buf.length() != 0) {
						arguments.add(buf.toString());
						buf = new StringBuilder();
					}
					state_in_double_quote = true;
				}
				continue;
			}
			buf.append(c0);
		}
		if (buf.length() != 0) {
			arguments.add(buf.toString());
		}
		return arguments;
	}

	public static String trimLeft(String str) {
		//If the argument is null then return empty string
		if (str == null) {
			return "";
		}

		/* The charAt method returns the character at a particular position in a String.
		 * We check to see if the character at position 0 (the leading character) is a space.
		 * If it is, use substring to make a new String that starts after the space.
		 */
		int len = 0;
		while (str.charAt(len) == ' ') {
			len++;
		}
		return str.substring(len);
	}

	public static String trimRight(String str) {
		//If the argument is null then return empty string
		if (str == null) {
			return "";
		}

		/* The logic for Rtrim is, While the last character in the String is a space, remove it.
		 * In the code, take the length of the string and use it to determine if the last character is a space.
		 */
		int len = str.length();
		while (len > 0 && str.charAt(len - 1) == ' ') {
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
		for (int i = 0; i < split.length; i++) {
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
		if (regex == null) {
			return string;
		}
		if (string == null) {
			return null;
		}
		if (regex.length() > string.length()) {
			//It can't be contained in here
			return string;
		}
		Matcher m = Pattern.compile(regex).matcher(string);
		int start = -1;
		int end = -1;
		while (m.find()) {
			start = m.start();
			end = m.end();
		}
		if (start == -1 || end == -1) {
			//Didn't find it, return the whole string
			return string;
		} else {
			return string.substring(0, start) + replacement + string.substring(end, string.length());
		}
	}

	/**
	 * Convenience method for HumanReadableByteCount(bytes, true).
	 * @param bytes
	 * @return 
	 */
	public static String HumanReadableByteCount(long bytes){
		return HumanReadableByteCount(bytes, true);
	}
	
	/**
	 * Returns a human readable byte count, given a byte count.
	 * @param bytes
	 * @param si
	 * @return 
	 */
	public static String HumanReadableByteCount(long bytes, boolean si) {
		int unit = si ? 1000 : 1024;
		if (bytes < unit) {
			return bytes + " B";
		}
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
}
