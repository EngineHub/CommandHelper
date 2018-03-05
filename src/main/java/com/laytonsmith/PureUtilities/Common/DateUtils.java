package com.laytonsmith.PureUtilities.Common;

import java.util.Calendar;

/**
 *
 *
 */
public final class DateUtils {

	private DateUtils() {
	}

	/**
	 * Convenience notation for ParseCalendarNotation(name, null)
	 */
	public static String ParseCalendarNotation(String name) {
		return ParseCalendarNotation(name, null);
	}

	/**
	 * Parses a calendar notation. The following patterns are replaced with the following:
	 * <table>
	 * <tr><td>%Y</td><td>Year</td></tr>
	 * <tr><td>%M</td><td>Month</td></tr>
	 * <tr><td>%D</td><td>Day</td></tr>
	 * <tr><td>%h</td><td>Hour</td></tr>
	 * <tr><td>%m</td><td>Minute</td></tr>
	 * <tr><td>%s</td><td>Second</td></tr>
	 * </table>
	 *
	 * A generally standard format for human readable logs is: %Y-%M-%D %h:%m.%s
	 *
	 * @param name
	 * @param c
	 * @return
	 */
	public static String ParseCalendarNotation(String name, Calendar c) {
		if(c == null) {
			c = Calendar.getInstance();
		}
		String year = String.format("%04d", c.get(Calendar.YEAR));
		String month = String.format("%02d", 1 + c.get(Calendar.MONTH)); //January is 0
		String day = String.format("%02d", c.get(Calendar.DAY_OF_MONTH));
		String hour = String.format("%02d", c.get(Calendar.HOUR));
		String minute = String.format("%02d", c.get(Calendar.MINUTE));
		String second = String.format("%02d", c.get(Calendar.SECOND));
		return name.replaceAll("%Y", year).replaceAll("%M", month)
				.replaceAll("%D", day).replaceAll("%h", hour)
				.replaceAll("%m", minute).replaceAll("%s", second);
	}
}
