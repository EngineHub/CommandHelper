package com.laytonsmith.PureUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author lsmith
 */
public final class StringUtils {

	private StringUtils() {
	}

	public static String Join(Set set, String glue) {
		StringBuilder b = new StringBuilder();
		boolean first = true;
		for (Object o : set) {
			if (!first) {
				b.append(glue);
			}
			first = false;
			b.append(o);
		}
		return b.toString();
	}

	public static String Join(Object[] list, String glue) {
		StringBuilder b = new StringBuilder();
		boolean first = true;
		for (Object o : list) {
			if (!first) {
				b.append(glue);
			}
			first = false;
			b.append(o);
		}
		return b.toString();
	}

	public static String Join(List list, String glue) {
		StringBuilder b = new StringBuilder();
		boolean first = true;
		for (Object o : list) {
			if (!first) {
				b.append(glue);
			}
			first = false;
			b.append(o);
		}
		return b.toString();
	}

	private static int minimum(int a, int b, int c) {
		return Math.min(Math.min(a, b), c);
	}

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
	 * @param string
	 * @param regex
	 * @return 
	 */
	public static String [] trimSplit(String string, String regex){
		String [] split = string.split(regex);
		for(int i = 0; i < split.length; i++){
			split[i] = split[i].trim();
		}
		return split;
	}
}
