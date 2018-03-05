package com.laytonsmith.core;

/**
 * A log level is a generic leveling system that grades the importance of various log messages. In order of importance,
 * the following levels are defined:
 * <ul>
 * <li>OFF</li>
 * <li>ERROR</li>
 * <li>WARNING</li>
 * <li>INFO</li>
 * <li>DEBUG</li>
 * <li>VERBOSE</li>
 * </ul>
 *
 */
public enum LogLevel {

	/**
	 * No logging will occur. This is typically not used by the code itself, but is set as a configuration by the user.
	 * Corresponds to an integer 0.
	 */
	OFF(0),
	/**
	 * Only the most flagrant messages will be logged at this level. Corresponds to an integer 1.
	 */
	ERROR(1),
	/**
	 * Things that should be noted but aren't vital are logged at this level. Corresponds to an integer 2.
	 */
	WARNING(2),
	/**
	 * Informational messages that are often helpful, but not at all vital are logged at this level. Corresponds to an
	 * integer 3.
	 */
	INFO(3),
	/**
	 * Debug messages are logged at this level. Typical users won't need this information except in the case where they
	 * are actively attempting to troubleshoot an issue. Corresponds to an integer 4.
	 */
	DEBUG(4),
	/**
	 * Information that goes above and beyond is logged at this level. Typically verbose information is used when
	 * tracking down a very obscure bug, or otherwise fine-tuning something. Corresponds to an integer 5.
	 */
	VERBOSE(5);
	int level;

	private LogLevel(int i) {
		level = i;
	}

	/**
	 * Gets the level specified, or null if no such level exists.
	 *
	 * @param level
	 * @return
	 */
	public static LogLevel getEnum(int level) {
		for (LogLevel l : LogLevel.values()) {
			if (level == l.getLevel()) {
				return l;
			}
		}
		return null;
	}

	/**
	 * Returns the integral format of this level.
	 *
	 * @return
	 */
	public int getLevel() {
		return level;
	}
}
