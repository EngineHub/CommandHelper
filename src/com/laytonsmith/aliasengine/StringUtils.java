/**
 * LICENSING
 *
 * This software is copyright by sunkid <sunkid.com> and is distributed under a dual license:
 *
 * Non-Commercial Use:
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Commercial Use:
 *    Please contact sunkid.com
 */

package com.laytonsmith.aliasengine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

	public static String repeat(String string, int n) {
		StringBuilder s = new StringBuilder();
		for (int a = 0; a < n; a++)
			s.append(string);
		return s.toString();
	}

	public static String constantCaseToEnglish(String constant) {
		String[] words = constant.split("_");
		StringBuilder english = new StringBuilder();
		for (String word : words) {
			english.append(word.toLowerCase());
			english.append(" ");
		}

		return english.substring(0, english.length() - 1);
	}

	public static String toConstantCase(String string) {
		StringBuilder constant = new StringBuilder();
		for (String word : string.split("\\s+")) {
			constant.append(word.toUpperCase());
			constant.append("_");
		}

		return constant.substring(0, constant.length() - 1);
	}

	public static List<String> closestMatch(String string, List<String> candidates) {
		ArrayList<String> results = new ArrayList<String>();

		String searchString = string.trim();

		if (isEmpty(string))
			return results;

		if (searchString.substring(0, 1).matches("[A-Z]")) {
			// first check for "camel-case fuzzy"
			Pattern p = Pattern.compile("([A-Z]|[a-z]|\\*)(?=[a-z*]*)[a-z*]*");
			Matcher m = p.matcher(searchString);

			if (m.find()) {
				StringBuilder regex = new StringBuilder();
				do {
					// System.err.println(m.group() + " " + m.group().matches(".*\\*.*"));
					if (m.group().matches(".*\\*.*")) {
						regex.append(m.group().toLowerCase().replaceAll("\\*", "[a-z\\\\s]*"));
					} else {
						regex.append(m.group().toLowerCase());
						regex.append("[a-z]*\\s+");
					}
					// System.err.println(regex);
				} while (m.find());

				String regexString = regex.toString();
				if (regexString.endsWith("\\s+"))
					regexString = regex.substring(0, regex.length() - 3);

				// System.err.println(regexString);
				for (String c : candidates) {
					if (c.toLowerCase().matches(regexString)) {
						results.add(c);
					}
				}
				return results;
			}
		}

		searchString = searchString.toLowerCase();
		for (String candidateString : candidates) {
			if (candidateString.toLowerCase().equals(searchString)) {
				results.removeAll(null);
				results.add(candidateString);
				return results;
			}

			if (candidateString.toLowerCase().startsWith(searchString)) {
				results.add(candidateString);
			}
		}
		return results;
	}

	public static boolean isTrue(String value) {
		String word = value.toLowerCase();
		return (word.equals("true") || word.equals("1") || word.equals("on"));
	}

	public static String join(String joiner, String... toJoin) {
		return join(joiner, Arrays.asList(toJoin), 0);
	}

	public static String join(String joiner, List<String> toJoin, int start) {
		if (isEmpty(joiner) || toJoin == null || toJoin.size() == 0 || toJoin.size() < start)
			return "";

		StringBuilder result = new StringBuilder();
		for (int n = start; n < toJoin.size(); n++) {
			result.append(toJoin.get(n));
			result.append(joiner);
		}

		return result.substring(0, result.length() - joiner.length());
	}

	public static boolean isEmpty(String string) {
		return (string == null || string.equals(""));
	}

	public static String join(String joiner, String[] toJoin, int start) {
		return join(joiner, Arrays.asList(toJoin), start);
	}
}
