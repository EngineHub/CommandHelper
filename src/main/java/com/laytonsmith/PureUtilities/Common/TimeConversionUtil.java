package com.laytonsmith.PureUtilities.Common;

import java.math.BigDecimal;

/**
 * This class provides various methods for working with time units, but ultimately converting human readable times
 * to seconds or milliseconds.
 * @author cailin
 */
public class TimeConversionUtil {

	public enum TimeUnit {
		/**
		 * 1/1,000 of a second
		 */
		MILLISECOND(1),
		/**
		 * 1 second is 1000 milliseconds
		 */
		SECOND(1000),
		/**
		 * 1 minute is 60 seconds
		 */
		MINUTE(60 * 1000),
		/**
		 * 1 hour is 60 minutes
		 */
		HOUR(60 * 60 * 1000),
		/**
		 * 1 day is 24 hours
		 */
		DAY(24 * 60 * 60 * 1000),
		/**
		 * 1 week is 7 days
		 */
		WEEK(7 * 24 * 60 * 60 * 1000),
		/**
		 * While the definition of a month may vary depending on the particular month, this is a synonym for
		 * {@link #MONTH31}
		 */
		MONTH(31 * 24 * 60 * 60 * 1000),
		/**
		 * This is a month with 28 days (typically only February in a non-leap year has this many days)
		 */
		MONTH28(28 * 24 * 60 * 60 * 1000),
		/**
		 * This is a month with 29 days (typically only February in a leap year has this many days)
		 */
		MONTH29(29 * 24 * 60 * 60 * 1000),
		/**
		 * This is a month with 30 days. (Typically April, June, September, and
		 * November have 30 days).
		 */
		MONTH30(30 * 24 * 60 * 60 * 1000),
		/**
		 * This is a month with 31 days. This is a synonym for {@link #MONTH}. (Typically, January, March, May, July,
		 * August, October, and December have 31 days).
		 */
		MONTH31(31 * 24 * 60 * 60 * 1000),
		/**
		 * While a year has a different number of days depending on whether or not this is a leap year, this is
		 * a synonym for {@link #YEAR365}.
		 */
		YEAR(365 * 24 * 60 * 60 * 1000),
		/**
		 * This is a year with 365 days, the typical year length on non-leap years
		 */
		YEAR365(365 * 24 * 60 * 60 * 1000),
		/**
		 * This is a year with 366 days, the typical year length on leap years
		 */
		YEAR366(366 * 24 * 60 * 60 * 1000);

		private final long factor;
		private TimeUnit(long factor) {
			this.factor = factor;
		}

		protected long factor() {
			return this.factor;
		}
	}

	/**
	 * This wraps the {@link #inMilliseconds(int, com.laytonsmith.PureUtilities.Common.TimeUtils.TimeUnit)} method,
	 * but rounds the result to the nearest second.
	 * @param number The number of time units you wish to convert
	 * @param unit The time unit you wish to use
	 * @return The number of seconds in the specified time unit
	 */
	public static long inSeconds(int number, TimeUnit unit) {
		long ms = inMilliseconds(number, unit);
		// Use precise division via Big Decimal
		return Math.round(BigDecimal.valueOf(ms).divide(BigDecimal.valueOf(1000)).doubleValue());
	}

	/**
	 * Returns the number of milliseconds in the given time unit. For instance,
	 * {@code inMilliseconds(1, TimeUnit.MINUTE)} would return 60000 and
	 * {@code inMilliseconds(3, TimeUnit.MINUTE)} would return 180000.
	 * @param number The number of time units you wish to convert
	 * @param unit The time unit you wish to use
	 * @return The number of milliseconds in the specified time unit
	 */
	public static long inMilliseconds(int number, TimeUnit unit) {
		return number * unit.factor();
	}

}
