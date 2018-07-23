package com.laytonsmith.PureUtilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * An ArgumentParser allows for programmatic registration of arguments, which
 * will be automatically parsed and validated. Additionally, automatically
 * generated help text can be retrieved and displayed, perhaps if a --help
 * argument is present.
 *
 */
public final class ArgumentParser {

	/**
	 * A description of the command itself.
	 */
	String description = "";
	List<Argument> argumentModel = new ArrayList<Argument>();

	/**
	 * Returns the default argument, if it exists.
	 *
	 * @return
	 */
	private Argument getArgument() {
		for(Argument a : argumentModel) {
			if(a.shortArg == null && a.longArg == null) {
				return a;
			}
		}
		return null;
	}

	private Argument getArgument(Character c) {
		for(Argument a : argumentModel) {
			if(a.shortArg == null) {
				continue;
			}
			if(a.shortArg.equals(c)) {
				return a;
			}
		}
		return null;
	}

	private Argument getArgument(String s) {
		for(Argument a : argumentModel) {
			if(a.longArg == null) {
				continue;
			}
			if(a.longArg.equals(s)) {
				return a;
			}
		}
		return null;
	}

	private final class Argument {

		Character shortArg;
		String longArg;
		Type argType;
		String defaultVal;
		List<String> defaultList;
		String description;
		String usageName;
		boolean required;
		String singleVal;
		List<String> arrayVal;

		private Argument(Argument arg) {
			if(arg == null) {
				return;
			}
			this.shortArg = arg.shortArg;
			this.longArg = arg.longArg;
			this.argType = arg.argType;
			this.defaultVal = arg.defaultVal;
			this.description = arg.description;
			this.usageName = arg.usageName;
			this.required = arg.required;
		}

		private Argument(Character shortArg, String longArg, Type argType, String defaultVal, String description, String usageName, boolean required) {
			this.shortArg = shortArg;
			this.longArg = longArg;
			this.argType = argType;
			this.description = description;

			this.defaultVal = defaultVal;
			if(isArray() && defaultVal != null) {
				defaultList = ArgumentParser.this.lex(defaultVal);
			}
			this.usageName = usageName;
			this.required = required;
		}

		public final boolean isFlag() {
			return argType == Type.BOOLEAN;
		}

		public final boolean isArray() {
			return argType == Type.ARRAY_OF_NUMBERS || argType == Type.ARRAY_OF_STRINGS;
		}

		public final boolean isSingle() {
			return argType == Type.NUMBER || argType == Type.STRING;
		}

		public final boolean isNumeric() {
			return argType == Type.NUMBER || argType == Type.ARRAY_OF_NUMBERS;
		}

		private void setValue(String val) {
			if(isArray()) {
				arrayVal = ArgumentParser.this.lex(val);
			} else {
				singleVal = val;
			}
		}

		private void setValue(List<String> val) {
			arrayVal = new ArrayList<>(val);
		}

		public boolean modelEquals(Argument obj) {
			if(this.shortArg != null) {
				return this.shortArg.equals(obj.shortArg);
			} else if(this.longArg != null) {
				return this.longArg.equals(obj.longArg);
			} else {
				return obj.shortArg == null && obj.longArg == null;
			}
		}

		@Override
		public String toString() {
			StringBuilder b = new StringBuilder();
			if(longArg != null && shortArg != null) {
				b.append("--").append(longArg).append("/").append("-").append(shortArg);
			} else if(longArg != null) {
				b.append("--").append(longArg);
			} else if(shortArg != null) {
				b.append("-").append(shortArg);
			}
			b.append(": ");
			if(isSingle()) {
				b.append(singleVal);
			} else if(isArray()) {
				boolean first = true;
				b.append("[");
				for(String s : arrayVal) {
					if(!first) {
						b.append(", ");
					}
					first = false;
					b.append("\"").append(s.replaceAll("\"", "\\\"")).append("\"");
				}
				b.append("]");
			}
			b.append("\n");
			return b.toString();
		}

		private String generateDescription(boolean shortCode) {
			StringBuilder b = new StringBuilder();
			b.append("\t");
			if(shortArg == null && longArg == null) {
				//Default argument
				b.append("<").append(usageName).append(">: ").append(description).append("\n");
			} else {
				//If short code is false, we need to check to see if there is a short code, if so,
				//this is an alias.
				if(shortCode) {
					b.append("-").append(shortArg);
				} else {
					b.append("--").append(longArg);
				}
				b.append(": ");

				if(!shortCode && shortArg != null) {
					//Alias
					b.append("Alias to -").append(shortArg);
				} else {
					if(argType != Type.BOOLEAN) {
						if(required) {
							b.append("Required. ");
						} else {
							b.append("Optional. ");
						}
					}
					if(argType == Type.NUMBER) {
						b.append("A numeric value. ");
					}
					if(argType == Type.ARRAY_OF_NUMBERS) {
						b.append("A list of numbers. ");
					}
					if(argType == Type.ARRAY_OF_STRINGS) {
						b.append("A list. ");
					}
					b.append(description.replaceAll("\n", "\n\t\t"));
				}
				b.append("\n");
			}
			return b.toString();
		}
	}

	private ArgumentParser() {
	}

	public static final class ValidationException extends Exception {

		private ValidationException(String string) {
			super(string);
		}
	}

	public static class ResultUseException extends RuntimeException {

		ResultUseException(String string) {
			super(string);
		}
	}

	public class ArgumentParserResults {

		List<Argument> arguments = new ArrayList<Argument>();

		private void updateArgument(Argument a) {
			if(a == null) {
				return;
			}
			List<Argument> toRemove = new ArrayList<Argument>();
			for(Argument arg : arguments) {
				if(arg.modelEquals(a)) {
					toRemove.add(arg);
				}
			}
			for(Argument arg : toRemove) {
				arguments.remove(arg);
			}
			arguments.add(a);
		}

		/**
		 * Returns true if the flag represented by this short code is set.
		 *
		 * @param flag
		 * @return
		 */
		public boolean isFlagSet(Character flag) {
			return getArg(flag) != null;
		}

		/**
		 * Returns true is the flag represented by this long code is set.
		 *
		 * @param flag
		 * @return
		 */
		public boolean isFlagSet(String flag) {
			return getArg(flag) != null;
		}

		/**
		 * Gets the unassociated arguments passed in as a String. For instance,
		 * if the arguments were <code>These are arguments</code>, then "These
		 * are arguments" will be returned. However, assuming -c is registered
		 * as a single string type, and the arguments are
		 * <code>-c These are arguments</code>, then only "are arguments" is
		 * returned. This will return an empty string if no arguments were set.
		 *
		 * @return
		 */
		public String getStringArgument() {
			try {
				Argument a = getArg();
				if(a.arrayVal == null) {
					return "";
				}
				StringBuilder b = new StringBuilder();
				boolean first = true;
				for(String val : a.arrayVal) {
					if(!first) {
						b.append(" ");
					}
					first = false;
					b.append(val);
				}
				return b.toString();
			} catch (ResultUseException e) {
				return "";
			}
		}

		/**
		 * Returns the string associated with the switch represented by this
		 * short code. If the switch wasn't set, null is returned.
		 *
		 * @param flag
		 * @return
		 * @throws ArgumentParser.ResultUseException
		 */
		public String getStringArgument(Character flag) throws ResultUseException {
			return getStringArgument(getArg(flag));
		}

		/**
		 * Returns the string associated with the switch represented by this
		 * long code. If the switch wasn't set, null is returned.
		 *
		 * @param flag
		 * @return
		 * @throws ArgumentParser.ResultUseException
		 */
		public String getStringArgument(String flag) throws ResultUseException {
			return getStringArgument(getArg(flag));
		}

		private String getStringArgument(Argument arg) {
			if(arg == null) {
				return null;
			}
			if(arg.argType != Type.STRING) {
				throw new ClassCastException("Argument type not set to " + Type.STRING.name() + ". Cannot return a " + "string" + ".");
			}
			return arg.singleVal;
		}

		/**
		 * Returns the value associated with the switch represented by this
		 * short code, pre-parsed as a double. If the switch wasn't set, null is
		 * returned.
		 *
		 * @param flag
		 * @return
		 * @throws ResultUseException, NumberFormatException
		 */
		public Double getNumberArgument(Character flag) throws ResultUseException {
			return getNumberArgument(getArg(flag));
		}

		/**
		 * Returns the value associated with the switch represented by this long
		 * code, pre-parsed as a double. If the switch wasn't set, null is
		 * returned.
		 *
		 * @param flag
		 * @return
		 * @throws ResultUseException, NumberFormatException
		 */
		public Double getNumberArgument(String flag) throws ResultUseException {
			return getNumberArgument(getArg(flag));
		}

		private Double getNumberArgument(Argument arg) {
			if(arg == null) {
				return null;
			}
			if(arg.argType != Type.NUMBER) {
				throw new ClassCastException("Argument type not set to " + Type.NUMBER.name() + ". Cannot return a " + "number" + ".");
			}
			return Double.parseDouble(arg.singleVal);
		}

		/**
		 * Gets the unassociated arguments passed in as a List of Strings. For
		 * instance, if the arguments were <code>These are arguments</code>,
		 * then ["These", "are", "arguments"] will be returned. However,
		 * assuming -c is registered as a single string type, and the arguments
		 * are <code>-c These are arguments</code>, then only ["are",
		 * "arguments"] is returned. This will return an empty array if no
		 * arguments were set.
		 *
		 * @return
		 */
		public List<String> getStringListArgument() {
			try {
				Argument a = getArg();
				if(a.arrayVal == null) {
					return new ArrayList<String>();
				}
				return new ArrayList<String>(a.arrayVal);
			} catch (ResultUseException e) {
				return new ArrayList<String>();
			}
		}

		/**
		 * Returns the list of values associated with the switch represented by
		 * this short code. If the switch wasn't set, null is returned.
		 *
		 * @param flag
		 * @return
		 * @throws ArgumentParser.ResultUseException
		 */
		public List<String> getStringListArgument(Character flag) throws ResultUseException {
			return getStringListArgument(getArg(flag));
		}

		/**
		 * Returns the list of values associated with the switch represented by
		 * this long code. If the switch wasn't set, null is returned.
		 *
		 * @param flag
		 * @return
		 * @throws ArgumentParser.ResultUseException
		 */
		public List<String> getStringListArgument(String flag) throws ResultUseException {
			return getStringListArgument(getArg(flag));
		}

		private List<String> getStringListArgument(Argument arg) {
			if(arg == null) {
				return null;
			}
			if(arg.argType != Type.ARRAY_OF_STRINGS) {
				throw new ClassCastException("Argument type not set to " + Type.ARRAY_OF_STRINGS.name() + ". Cannot return a " + "string list" + ".");
			}
			return new ArrayList<String>(arg.arrayVal);
		}

		/**
		 * Returns the list of values associated with the switch represented by
		 * this short code, pre-parsed into doubles. If the switch wasn't set,
		 * null is returned.
		 *
		 * @param flag
		 * @return
		 * @throws ArgumentParser.ResultUseException
		 */
		public List<Double> getNumberListArgument(Character flag) throws ResultUseException {
			return getNumberListArgument(getArg(flag));
		}

		/**
		 * Returns the list of values associated with the switch represented by
		 * this long code, pre-parsed into doubles. If the switch wasn't set,
		 * null is returned.
		 *
		 * @param flag
		 * @return
		 * @throws ArgumentParser.ResultUseException
		 */
		public List<Double> getNumberListArgument(String flag) throws ResultUseException {
			return getNumberListArgument(getArg(flag));
		}

		private List<Double> getNumberListArgument(Argument arg) {
			if(arg == null) {
				return null;
			}
			if(arg.argType != Type.ARRAY_OF_NUMBERS) {
				throw new ClassCastException("Argument type not set to " + Type.ARRAY_OF_NUMBERS.name() + ". Cannot return a " + "number list" + ".");
			}

			List<Double> list = new ArrayList<Double>();
			for(String s : arg.arrayVal) {
				list.add(Double.parseDouble(s));
			}
			return list;
		}

		private Argument getArg() {
			for(Argument a : arguments) {
				if(a.shortArg == null && a.longArg == null) {
					return a;
				}
			}
			return new Argument(ArgumentParser.this.getArgument());
		}

		private Argument getArg(Character flag) throws ResultUseException {
			for(Argument a : arguments) {
				if(a.shortArg == null) {
					continue;
				}
				if(a.shortArg.equals(flag)) {
					return a;
				}
			}
			return null;
		}

		private Argument getArg(String flag) throws ResultUseException {
			for(Argument a : arguments) {
				if(a.longArg == null) {
					continue;
				}
				if(a.longArg.equals(flag)) {
					return a;
				}
			}
			return null;
		}

		@Override
		public String toString() {
			StringBuilder b = new StringBuilder();
			for(Argument arg : arguments) {
				if(arg.isFlag()) {
					b.append("Flag ");
					if(arg.longArg != null && arg.shortArg != null) {
						b.append("--").append(arg.longArg).append("/").append("-").append(arg.shortArg);
					} else if(arg.longArg != null) {
						b.append("--").append(arg.longArg);
					} else if(arg.shortArg != null) {
						b.append("-").append(arg.shortArg);
					}
					b.append(" is set.\n");
				} else {
					b.append(arg.toString());
				}
			}
			return b.toString();
		}
	}

	public static ArgumentParser GetParser() {
		return new ArgumentParser();
	}

	public static enum Type {

		STRING,
		NUMBER,
		ARRAY_OF_STRINGS,
		ARRAY_OF_NUMBERS,
		BOOLEAN
	}

	private ArgumentParser addArgument0(Character shortArg, String longArg, Type argType, String defaultVal, String description, String usageName, boolean required) {
		//TODO: Make sure this switch doesn't already exist
		argumentModel.add(new Argument(shortArg, longArg, argType, defaultVal, description, usageName, required));
		return this;
	}

	/**
	 * Adds an argument to this argument parser. This is the most complex method
	 * of adding an argument, all other methods are wrappers around this.
	 *
	 * The short code and long code for a switch both represent the underlying
	 * switch, that is, they both "addresses" of a single underlying switch.
	 * When accessing the argument later, you may use either the short code or
	 * the long code to retrieve the value of the switch, but it is important to
	 * understand that they are both pointing to the same item.
	 *
	 * @param shortArg The short code for this switch.
	 * @param longArg The long code for this switch.
	 * @param argType The expected type of this switch.
	 * @param defaultVal The default value of this switch. If defaultVal is not
	 * null, the switch will always exist when calling get*Argument from the
	 * results. If argType is BOOLEAN, setting this will cause the switch to
	 * @param description The description of this argument, which is used when
	 * building the help text created by getBuiltDescription.
	 * @return
	 */
	public ArgumentParser addArgument(Character shortArg, String longArg, Type argType, String defaultVal, String description, String usageName, boolean required) {
		if(argType == Type.BOOLEAN) {
			throw new IllegalArgumentException("Cannot use addArgument to add a flag. Use addFlag instead.");
		}
		if(shortArg == null && longArg == null) {
			if(argType != Type.STRING && argType != Type.ARRAY_OF_STRINGS) {
				throw new IllegalArgumentException("Cannot set the type of the default switch to anything but " + Type.STRING.name() + " or "
						+ Type.ARRAY_OF_STRINGS.name());
			}
		}
		return addArgument0(shortArg, longArg, argType, defaultVal, description, usageName, required);
	}

	/**
	 * Sets the default switch's arg type, default value, and description. The
	 * default switch is the switch that is associated with "loose" arguments,
	 * for instance, <code>these are args</code> would all be loose arguments,
	 * because they aren't associated with any explicit switches. Note that
	 * there is no Type specified here, that's because the arguments can be
	 * grabbed as either an array of strings or a string.
	 *
	 * @param argType
	 * @param defaultVal
	 * @param description
	 * @return
	 */
	public ArgumentParser addArgument(String defaultVal, String description, String usageName, boolean required) {
		return addArgument(null, null, Type.ARRAY_OF_STRINGS, defaultVal, description, usageName, required);
	}

	/**
	 * Sets the default switch with no default value.
	 *
	 * @param argType
	 * @param description
	 * @return
	 */
	public ArgumentParser addArgument(String description, String usageName, boolean required) {
		return addArgument(null, null, Type.ARRAY_OF_STRINGS, null, description, usageName, required);
	}

	/**
	 * Adds a new argument with no default value.
	 *
	 * @param shortArg
	 * @param longArg
	 * @param argType
	 * @param description
	 * @return
	 */
	public ArgumentParser addArgument(Character shortArg, String longArg, Type argType, String description, String usageName, boolean required) {
		return addArgument(shortArg, longArg, argType, null, description, usageName, required);
	}

	/**
	 * Adds a new argument with no long code.
	 *
	 * @param shortArg
	 * @param argType
	 * @param defaultVal
	 * @param description
	 * @return
	 */
	public ArgumentParser addArgument(Character shortArg, Type argType, String defaultVal, String description, String usageName, boolean required) {
		return addArgument(shortArg, null, argType, defaultVal, description, usageName, required);
	}

	/**
	 * Adds a new argument with no short code.
	 *
	 * @param longArg
	 * @param argType
	 * @param defaultVal
	 * @param description
	 * @return
	 */
	public ArgumentParser addArgument(String longArg, Type argType, String defaultVal, String description, String usageName, boolean required) {
		return addArgument(null, longArg, argType, defaultVal, description, usageName, required);
	}

	/**
	 * Adds a new argument with no long code, and no default value.
	 *
	 * @param shortArg
	 * @param argType
	 * @param description
	 * @return
	 */
	public ArgumentParser addArgument(Character shortArg, Type argType, String description, String usageName, boolean required) {
		return addArgument(shortArg, null, argType, null, description, usageName, required);
	}

	/**
	 * Adds a new argument with no short code, and no default value.
	 *
	 * @param longArg
	 * @param argType
	 * @param description
	 * @return
	 */
	public ArgumentParser addArgument(String longArg, Type argType, String description, String usageName, boolean required) {
		return addArgument(null, longArg, argType, null, description, usageName, required);
	}

	/**
	 * Adds a new flag.
	 *
	 * @param shortArg
	 * @param longArg
	 * @param description
	 * @return
	 */
	public ArgumentParser addFlag(Character shortArg, String longArg, String description) {
		return addArgument0(shortArg, longArg, Type.BOOLEAN, null, description, null, false);
	}

	/**
	 * Adds a new flag with no short code.
	 *
	 * @param longArg
	 * @param description
	 * @return
	 */
	public ArgumentParser addFlag(String longArg, String description) {
		return addArgument0(null, longArg, Type.BOOLEAN, null, description, null, false);
	}

	/**
	 * Adds a new flag with no long code.
	 *
	 * @param shortArg
	 * @param description
	 * @return
	 */
	public ArgumentParser addFlag(Character shortArg, String description) {
		return addArgument0(shortArg, null, Type.BOOLEAN, null, description, null, false);
	}

	/**
	 * Adds a description to the ArgumentParser object, which is used when
	 * building the description returned by getBuiltDescription.
	 *
	 * @param description
	 * @return
	 */
	public ArgumentParser addDescription(String description) {
		this.description = description;
		return this;
	}

	/**
	 * Builds a description of this ArgumentParser, which may be useful as
	 * "Usage" text to provide to a user if a call to match throws a
	 * ValidationException, for instance.
	 *
	 * @return
	 */
	public String getBuiltDescription() {
		StringBuilder b = new StringBuilder();
		//Now, we need to go through and get all the switch names in alphabetical
		//order.
		b.append("\t").append(this.description).append("\n\n");
		List<Character> shortCodes = new ArrayList<Character>();
		List<String> longCodes = new ArrayList<String>();
		List<Character> shortCodesDone = new ArrayList<Character>();
		List<String> longCodesDone = new ArrayList<String>();
		List<String> aliases = new ArrayList<String>();
		for(Argument arg : argumentModel) {
			if(arg.shortArg != null) {
				shortCodes.add(arg.shortArg);
			}
			if(arg.longArg != null) {
				longCodes.add(arg.longArg);
			}
			if(arg.shortArg != null && arg.longArg != null) {
				aliases.add(arg.longArg);
			}
		}
		Collections.sort(shortCodes);
		Collections.sort(longCodes);
		//Go through the flags first
		boolean hasShortCodeFlags = false;
		StringBuilder flags = new StringBuilder();
		List<Character> shortFlags = new ArrayList<Character>();
		List<String> longFlags = new ArrayList<String>();
		List<Character> shortArguments = new ArrayList<Character>();
		List<String> longArguments = new ArrayList<String>();
		for(Character c : shortCodes) {
			Argument a = getArgument(c);
			if(a.isFlag()) {
				shortCodesDone.add(c);
				flags.append(a.generateDescription(true));
				hasShortCodeFlags = true;
				shortFlags.add(c);
			} else {
				shortArguments.add(c);
			}
		}

		for(String s : longCodes) {
			Argument a = getArgument(s);
			if(a.isFlag()) {
				longCodesDone.add(s);
				flags.append(a.generateDescription(false));
				if(!aliases.contains(s)) {
					longFlags.add(s);
				}
			} else if(!aliases.contains(s)) {
				longArguments.add(s);
			}
		}

		b.append("Usage:\n\t");
		//Get the short flags first, then the long flags, then the short arguments, then the long arguments
		List<String> parts = new ArrayList<String>();
		if(!shortFlags.isEmpty()) {
			StringBuilder usage = new StringBuilder();
			usage.append("[-");
			for(Character c : shortFlags) {
				usage.append(c);
			}
			usage.append("]");
			parts.add(usage.toString());
		}

		for(String s : longFlags) {
			parts.add("[--" + s + "]");
		}

		List<Argument> usageList = new ArrayList<Argument>();
		for(Character c : shortArguments) {
			usageList.add(getArgument(c));
		}
		for(String s : longArguments) {
			usageList.add(getArgument(s));
		}
		for(Argument a : usageList) {
			StringBuilder usage = new StringBuilder();
			if(!a.required) {
				usage.append("[");
			}
			if(a.shortArg != null) {
				usage.append("-").append(a.shortArg);
			} else {
				usage.append("--").append(a.longArg);
			}
			usage.append(" <");
			if(a.isNumeric()) {
				usage.append("#");
			}
			usage.append(a.usageName);
			if(a.isArray()) {
				usage.append(", ...");
			}
			usage.append(">");

			if(a.defaultVal != null && !"".equals(a.defaultVal)) {
				usage.append(" (default ");
				if(a.argType == Type.STRING) {
					usage.append("\"");
				}
				usage.append(a.defaultVal);
				if(a.argType == Type.STRING) {
					usage.append("\"");
				}
				usage.append(")");
			}

			if(!a.required) {
				usage.append("]");
			}
			parts.add(usage.toString());
		}

		//Now, if the default switch exists, put it here too
		if(getArgument() != null) {
			parts.add("<" + getArgument().usageName + ", ...>");
		}

		{
			StringBuilder usage = new StringBuilder();
			boolean first = true;
			for(String part : parts) {
				if(!first) {
					b.append(" ");
				}
				first = false;
				b.append(part);
			}
			if(parts.isEmpty()) {
				usage.append("No arguments.");
			}
			b.append(usage.toString());
		}
		b.append("\n\nOptions:\n\n");
		Argument def = getArgument();
		if(def != null && def.description != null) {
			b.append(def.generateDescription(false));
		}

		if(flags.length() != 0) {
			b.append("Flags");
			if(hasShortCodeFlags) {
				b.append(" (Short flags may be combined)");
			}
			b.append(":\n");
			b.append(flags.toString());
			b.append("\n");
		}

		if(shortCodes.isEmpty() && longCodes.isEmpty() && def == null && flags.length() == 0) {
			b.append("\tNo flags or options.\n");
		} else {
			if(shortCodes.isEmpty() && longCodes.isEmpty() && def == null) {
				b.append("\tNo options.\n");
			} else if(flags.length() == 0) {
				b.append("\tNo flags.\n");
			}
		}

		for(Character c : shortCodes) {
			if(!shortCodesDone.contains(c)) {
				b.append(getArgument(c).generateDescription(true));
			}
		}
		for(String s : longCodes) {
			if(!longCodesDone.contains(s)) {
				b.append(getArgument(s).generateDescription(false));
			}
		}
		return b.toString();
	}

	/**
	 * Returns just the description that was registered with {
	 *
	 * @see #addDescription(String)}.
	 * @return The description, or null, if one has not been set yet.
	 * @see #getBuiltDescription()
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * This method takes a raw string, which represents the arguments as a
	 * single string. It supports quoted arguments (both single and double), so
	 * "this example" would make the string "this example" one argument instead
	 * of two. Quotes may be escaped with a backslash, like so: "\"quote\"".
	 * Also, all arguments that start with a dash are considered flags, if you
	 * need a literal dash, escape it with a backslash too, \-. Instead of
	 * quoting arguments, you could also add a \ in front of a space to make it
	 * a literal space, <code>like\ this</code>. Within a string, to add a
	 * literal backslash in front of an otherwise escapable character, use two
	 * backslashes <code>"like this\\"</code>
	 *
	 * @param args
	 * @return
	 */
	public ArgumentParserResults match(String args) throws ValidationException {
		return parse(lex(args));
	}

	/**
	 * This method assumes that the arguments have already been parsed out, so,
	 * for instance, if an argument inside of args[x] contains spaces, it will
	 * still be considered one argument. So, ["args with spaces", "args"] may
	 * have looked like this originally: <code>"args with spaces" args </code>
	 * but through some means or another, you have already parsed the arguments
	 * out.
	 *
	 * @param args
	 * @return
	 */
	public ArgumentParserResults match(String[] args) throws ValidationException {
		return parse(Arrays.asList(args));
	}

	/**
	 * Returns a simple List of the arguments, parsed into a proper argument
	 * list. This will work essentially identically to how general shell
	 * arguments are parsed.
	 *
	 * @param args
	 * @return
	 */
	static List<String> lex(String args) {
		//First, we have to tokenize the strings. Since we can have quoted arguments, we can't simply split on spaces.
		List<String> arguments = new ArrayList<String>();
		StringBuilder buf = new StringBuilder();
		boolean stateInSingleQuote = false;
		boolean stateInDoubleQuote = false;
		for(int i = 0; i < args.length(); i++) {
			Character c0 = args.charAt(i);
			Character c1 = i + 1 < args.length() ? args.charAt(i + 1) : null;

			if(c0 == '\\') {
				if(c1 == '\'' && stateInSingleQuote
						|| c1 == '"' && stateInDoubleQuote
						|| c1 == ' ' && !stateInDoubleQuote && !stateInSingleQuote
						|| c1 == '\\' && (stateInDoubleQuote || stateInSingleQuote)) {
					//We are escaping the next character. Add it to the buffer instead, and
					//skip ahead two
					buf.append(c1);
					i++;
					continue;
				}

			}

			if(c0 == ' ') {
				if(!stateInDoubleQuote && !stateInSingleQuote) {
					//argument split
					if(buf.length() != 0) {
						arguments.add(buf.toString());
						buf = new StringBuilder();
					}
					continue;
				}
			}
			if(c0 == '\'' && !stateInDoubleQuote) {
				if(stateInSingleQuote) {
					stateInSingleQuote = false;
					arguments.add(buf.toString());
					buf = new StringBuilder();
				} else {
					if(buf.length() != 0) {
						arguments.add(buf.toString());
						buf = new StringBuilder();
					}
					stateInSingleQuote = true;
				}
				continue;
			}
			if(c0 == '"' && !stateInSingleQuote) {
				if(stateInDoubleQuote) {
					stateInDoubleQuote = false;
					arguments.add(buf.toString());
					buf = new StringBuilder();
				} else {
					if(buf.length() != 0) {
						arguments.add(buf.toString());
						buf = new StringBuilder();
					}
					stateInDoubleQuote = true;
				}
				continue;
			}
			buf.append(c0);
		}
		if(buf.length() != 0) {
			arguments.add(buf.toString());
		}
		return arguments;
	}

	private ArgumentParserResults parse(List<String> args) throws ValidationException {
		ArgumentParserResults results = new ArgumentParserResults();
		//Fill in results with all the defaults
		for(Argument arg : argumentModel) {
			if(arg.defaultVal != null) {
				//For flags, we simply don't add them if they default to false.
				if(!arg.isFlag() || (arg.isFlag() && arg.defaultVal != null)) {
					Argument newArg = new Argument(arg);
					newArg.setValue(arg.defaultVal);
					results.updateArgument(newArg);
				}
			}
		}
		//These are arguments that are not flags.
		List<String> looseArgs = new ArrayList<>();
		Argument lastArg = null;
		for(String arg : args) {
			if(arg.matches("^[\\\\]+-.*$")) {
				//This is an argument that starts with a literal dash, but we need
				//to pull out this first backslash
				looseArgs.add(arg.substring(1));
				continue;
			}

			//Our regexes have a star, because -(-) is a valid argument that is an empty string.
			//"" != null.
			if(arg.matches("--[a-zA-Z0-9\\-]*")) {
				//Finish up the last argument
				results.updateArgument(validateArgument(lastArg, looseArgs));
				//This is a long arg, and so it is the only one.
				arg = arg.substring(2);
				lastArg = getArgument(arg);
				continue;
			}

			if(arg.matches("-[a-zA-Z0-9]*")) {
				//Finish up the last argument
				results.updateArgument(validateArgument(lastArg, looseArgs));
				//This is a short arg, but it could be multiple letters (therefore multiple flags)
				//At most, one of these can be a non-flag argument.
				boolean hasNonFlagArg = false;
				char lastNonFlag = ' ';
				for(int i = 1; i < arg.length(); i++) {
					Character c = arg.charAt(i);
					Argument vArg = getArgument(c);
					if(vArg == null) {
						throw new ValidationException("Unrecognized flag: " + c);
					}
					if(!vArg.isFlag() && hasNonFlagArg) {
						//We have already come across a non-flag argument, and since this one isn't
						//a flag, we need to throw an exception.
						throw new ValidationException("Cannot combine multiple non-flag arguments using the short form. Found '" + c
								+ "' but had already found '" + lastNonFlag + "'. You must split these into multiple arguments, even if they"
								+ " do not have any parameters, for instance, -" + c + " -" + lastNonFlag);
					}
					//Is this a non flag?
					if(!vArg.isFlag()) {
						hasNonFlagArg = true;
						lastNonFlag = c;
						lastArg = vArg;
						//This is all we need to do, it will be dealt with by the next iteration
					} else {
						//Since it's just a flag, we don't need to worry about it regarding loose arguments, so we
						//are just going to go ahead and add it to the results.
						results.updateArgument(vArg);
					}
				}
				continue;
			}

			//It's just a loose arg, so we'll add it to the list and deal with it at the end
			looseArgs.add(arg);
		}

		//Finish up the last argument
		results.updateArgument(validateArgument(lastArg, looseArgs));
		if(looseArgs.size() > 0) {
			//There are loose arguments left, so add them to the loose argument list.
			results.updateArgument(validateArgument(null, looseArgs));
		}
		//TODO: Check to see if all the required values are here

		return results;
	}

	private Argument validateArgument(Argument arg, List<String> looseArgs) throws ValidationException {
		if(arg == null) {
			if(!looseArgs.isEmpty()) {
				//All the loose arguments are accounted for. Either we're done with the arguments,
				//or we just hit a -(-)specifier, so we can stop parsing these, and go ahead and
				//add them to the results.
				Argument a = new Argument(ArgumentParser.this.getArgument());
				a.setValue(looseArgs);
				looseArgs.clear();
				return a;
			}
			return null;
		}
		Argument finishedArgument = new Argument(arg);
		if(arg.isSingle()) {
			//Just the first loose argument is associated with this argument,
			//the rest (if any) belong to the default loose argument list.
			//Of course, looseArgs could be empty, in which case we won't add anything to the list.
			if(looseArgs.size() > 0) {
				String looseArg = looseArgs.get(0);
				looseArgs.remove(0);
				finishedArgument.setValue(looseArg);
				if(arg.isNumeric()) {
					try {
						Double.parseDouble(looseArg);
					} catch (NumberFormatException e) {
						throw new ValidationException("Expecting a numeric value, but \"" + looseArg + "\" was encountered.");
					}
				}
			} else {
				finishedArgument.setValue("");
			}
		} else if(arg.isArray()) {
			finishedArgument.setValue(looseArgs);
			if(arg.isNumeric()) {
				for(String val : looseArgs) {
					try {
						Double.parseDouble(val);
					} catch (NumberFormatException e) {
						throw new ValidationException("Expecting a numeric value, but \"" + val + "\" was encountered.");
					}
				}
			}
			looseArgs.clear();
		}
		return finishedArgument;
	}
}
