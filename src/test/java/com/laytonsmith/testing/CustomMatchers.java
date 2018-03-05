package com.laytonsmith.testing;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * This class contains custom hamcrest matchers.
 */
public class CustomMatchers {

	@Factory
	public static <T> Matcher<String> regexMatch(String regex) {
		return new RegexMatch(regex);
	}

	private static class RegexMatch extends TypeSafeMatcher<String> {

		String regex;

		public RegexMatch(String regex) {
			this.regex = regex;
		}

		@Override
		protected boolean matchesSafely(String item) {
			return item.matches(regex);
		}

		@Override
		public void describeTo(Description description) {
			description.appendText("value to match regex " + regex);
		}

	}

}
