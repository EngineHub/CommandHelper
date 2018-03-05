package com.laytonsmith.PureUtilities.Common;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 *
 */
public final class LogUtils {

	private LogUtils() {
	}

	/**
	 * Get the system's line endings
	 *
	 * @return
	 */
	public static String LF() {
		return System.getProperty("line.separator");
	}

	/**
	 * Logs a message to the given file, appending the current timestamp.
	 *
	 * @param filename
	 * @param message
	 * @throws IOException
	 */
	public static void LoggerMessage(String filename, String message) throws IOException {
		String timestamp = DateUtils.ParseCalendarNotation("%Y-%M-%D %h:%m.%s - ");
		QuickAppend(GetLog(filename), timestamp + message + LF());
	}

	/**
	 * Logs a message to the given file, and prints out to the given logger as well.
	 *
	 * @param filename
	 * @param message
	 * @param logger
	 * @throws IOException
	 */
	public static synchronized void LoggerMessage(String filename, String message, Logger logger) throws IOException {
		if(logger != null) {
			logger.log(Level.INFO, message);
		}
		LoggerMessage(filename, message);
	}

	/**
	 * Logs a message to the given file, and prints the message to the given PrintStream, for instance, System.out.
	 *
	 * @param filename
	 * @param message
	 * @param out
	 * @throws IOException
	 */
	public static synchronized void LoggerMessage(String filename, String message, PrintStream out) throws IOException {
		if(out != null) {
			out.println(message);
		}
		LoggerMessage(filename, message);
	}

	public static void QuickAppend(FileWriter f, String message) throws IOException {
		f.append(message);
		f.flush();
	}

	private static FileWriter GetLog(String filename) throws IOException {
		return new FileWriter(filename, true);
	}

	public static void Log(String filename, String message) throws IOException {
		QuickAppend(GetLog(filename), message + LF());
	}
}
