package com.laytonsmith.PureUtilities.Common;

import java.util.Objects;

/**
 * This class represents a GNU error format message. It supports all of the following message formats:
 *
 * <ul>
 * <li>sourcefile:lineno:column: message</li>
 * <li>sourcefile:lineno: message</li>
 * <li>sourcefile:lineno.column: message</li>
 * <li>sourcefile:line1.column1-line2.column2: message</li>
 * <li>sourcefile:line1.column1-column2: message</li>
 * <li>sourcefile:line1-line2: message</li>
 * </ul>
 *
 * Currently, explicitely <strong>not</strong> supported are the following otherwise valid GNU error message formats:
 * <ul>
 * <li>file1:line1.column1-file2:line2.column2: message</li>
 * <li>program:sourcefile:lineno: message</li>
 * <li>program: message</li>
 * <li>program:sourcefile:lineno:column: message</li>
 * </ul>
 *
 * Once the object is created, the error message is parsed, and the constituent parts are more easily readable from the
 * object's methods.
 *
 * @author cailin
 */
public class GNUErrorMessageFormat {

	public static enum MessageType {
		ERROR, WARNING, INFO, UNKNOWN
	}

	private final String messageLine;

	private String file;
	private int fromLine = -1;
	private int fromColumn = 0;
	private int toLine = -1;
	private int toColumn = 0;
	private MessageType probableMessageType = MessageType.UNKNOWN;
	private String message;
	private boolean parsed = false;

	/**
	 * Constructs a new GNUErrorMessageFormat object from the given error message line.
	 *
	 * @param messageLine the message to parse
	 */
	public GNUErrorMessageFormat(String messageLine) {
		this.messageLine = messageLine;
	}

	/**
	 * Actually parses the message. This is called automatically by all the other methods in this class, but this allows
	 * you to check immediately to ensure that the error is correctly parseable.
	 *
	 * @throws IllegalArgumentException if the message is not parseable.
	 * @return This object, for easier chaining
	 */
	public GNUErrorMessageFormat parse() throws IllegalArgumentException {
		if(parsed) {
			return this;
		}
		parsed = true;
		String[] errorParts = messageLine.split(" ", 2);
		message = errorParts[1].trim();
		errorParts = errorParts[0].split(":", -1);
		if("".equals(errorParts[errorParts.length - 1])) {
			errorParts = ArrayUtils.slice(errorParts, 0, errorParts.length - 2);
		}
		if(errorParts.length > 3) {
			throw new IllegalArgumentException("Not a supported error message format");
		}
		// These are all the formats we need to support
		// sourcefile:lineno:column: message
		// sourcefile:lineno: message
		// sourcefile:lineno.column: message
		// sourcefile:line1.column1-line2.column2: message
		// sourcefile:line1.column1-column2: message
		// sourcefile:line1-line2: message
		switch(errorParts.length) {
			case 2:
				file = errorParts[0];
				String middle = errorParts[1];
				if(middle.matches("\\d+")) {
					fromLine = Integer.parseInt(middle);
				} else if(middle.matches("\\d+\\.\\d+")) {
					String[] s = middle.split("\\.");
					fromLine = Integer.parseInt(s[0]);
					fromColumn = toColumn = Integer.parseInt(s[1]);
				} else if(middle.matches("\\d+\\.\\d+-\\d+\\.\\d+")) {
					String[] s = middle.split("-");
					String s0[] = s[0].split("\\.");
					String s1[] = s[1].split("\\.");
					fromLine = Integer.parseInt(s0[0]);
					fromColumn = Integer.parseInt(s0[1]);
					toLine = Integer.parseInt(s1[0]);
					toColumn = Integer.parseInt(s1[1]);
				} else if(middle.matches("\\d+\\.\\d+-\\d+")) {
					String[] s = middle.split("\\.");
					String[] c = s[1].split("-");
					fromLine = toLine = Integer.parseInt(s[0]);
					fromColumn = Integer.parseInt(c[0]);
					toColumn = Integer.parseInt(c[1]);
				} else if(middle.matches("\\d+-\\d+")) {
					String[] s = middle.split("-");
					fromLine = Integer.parseInt(s[0]);
					toLine = Integer.parseInt(s[1]);
				} else {
					throw new IllegalArgumentException("Could not parse message");
				}
				break;
			case 3:
				file = errorParts[0];
				fromLine = Integer.parseInt(errorParts[1]);
				fromColumn = toColumn = Integer.parseInt(errorParts[2]);
				break;
		}
		// If it contains more than one, then the most severe takes priority anyways
		if(StringUtils.containsIgnoreCase(message, "error")) {
			probableMessageType = MessageType.ERROR;
		} else if(StringUtils.containsIgnoreCase(message, "warning")) {
			probableMessageType = MessageType.WARNING;
		} else if(StringUtils.containsIgnoreCase(message, "info")) {
			probableMessageType = MessageType.INFO;
		}
		if(toLine == -1) {
			toLine = fromLine;
		}
		if(toColumn == -1) {
			toColumn = fromColumn;
		}
		return this;
	}

	/**
	 * The file identifier.
	 *
	 * @return
	 */
	public String file() {
		parse();
		return file;
	}

	/**
	 * The beginning line number of the error. This value is 1 indexed.
	 *
	 * @return
	 */
	public int fromLine() {
		parse();
		return fromLine;
	}

	/**
	 * The beginning column number of the error
	 *
	 * @return
	 */
	public int fromColumn() {
		parse();
		return fromColumn;
	}

	/**
	 * The ending line of the error. This value is 1 indexed.
	 *
	 * @return
	 */
	public int toLine() {
		parse();
		return toLine;
	}

	/**
	 * The ending column number of the error.
	 *
	 * @return
	 */
	public int toColumn() {
		parse();
		return toColumn;
	}

	/**
	 * Returns the probable message type. This is based on whether or not the message contains certain keywords. It may
	 * guess wrong, however, you can ignore this parameter if you wish to do custom analyzation of the message.
	 *
	 * @return
	 */
	public MessageType messageType() {
		parse();
		return probableMessageType;
	}

	/**
	 * Returns the message itself.
	 *
	 * @return
	 */
	public String message() {
		parse();
		return message;
	}

	/**
	 * Returns the original error message line that was used to construct this object.
	 *
	 * @return
	 */
	public String getOriginalErrorLine() {
		return messageLine;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof GNUErrorMessageFormat) {
			return this.messageLine.equals(((GNUErrorMessageFormat) obj).messageLine);
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 17 * hash + Objects.hashCode(this.messageLine);
		return hash;
	}

	@Override
	public String toString() {
		return file + ":" + fromLine + "." + fromColumn + "-" + toLine + "." + toColumn + ": " + message;
	}

}
