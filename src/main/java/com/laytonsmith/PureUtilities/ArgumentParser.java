package com.laytonsmith.PureUtilities;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

// TODO: Add enum type to arg types, as well as providing hooks for autocomplete and custom argument validators

/**
 * An ArgumentParser allows for programmatic registration of arguments, which
 * will be automatically parsed and validated. Additionally, automatically
 * generated help text can be retrieved and displayed, perhaps if a --help
 * argument is present.
 *
 */
public final class ArgumentParser {

	/**
	 * Eases the operation of building a single argument. A new instance of this should be constructed, and the IDE
	 * will guide you through selecting the rest of the elements. Using this builder, it is not possible to create
	 * an inconsistent Argument.
	 */
	public static class ArgumentBuilder {

		enum Mode { SHORT, LONG, BOTH, DEFAULT }
		Mode mode;

		char shortArg;
		String longArg;

		String description;
		String usageName;
		boolean required;

		Type argType = Type.ARRAY_OF_STRINGS;
		String defaultValue;

		/**
		 * Sets the description for this argument. For consistency, it is arbitrarily decided that these descriptions
		 * should end in a period.
		 * @param description The description
		 * @return The next step in the build process
		 */
		public ArgumentBuilderRequired2 setDescription(String description) {
			Objects.requireNonNull(description, "description may not be null");
			ArgumentBuilder.this.description = description;
			return new ArgumentBuilderRequired2();
		}

		public class ArgumentBuilderRequired2 {
			ArgumentBuilderRequired2(){}

			/**
			 * Sets the usage name for this argument. Typically, this is just the name
			 * of the long argument, or a very short phrase describing the value,
			 * but is only used for informational purposes, so
			 * is ok to be repeated across arguments.
			 * <p>
			 * During help generation, this argument is surrounded by &lt;&gt; to indicate that it
			 * is an argument, so there is no need to wrap the value in symbols.
			 * <p>
			 * The documentation will look something like this, assuming the argument is named "path"
			 * and the usageName is "path to file":
			 * <pre>
			 *	--path &lt;path to file&gt;
			 * </pre>
			 *
			 * If the type of the argument is an array, {@code , ...} is appended, and if it is numeric, {@code #}
			 * is prepended.
			 * <p>
			 * Flags do not have a parameter, so no usage name is needed.
			 *
			 * @param usageName The display name in the help docs.
			 * @return The next step in the build process.
			 */
			public ArgumentBuilderRequired3 setUsageName(String usageName) {
				Objects.requireNonNull(usageName, "usageName may not be null");
				ArgumentBuilder.this.usageName = usageName;
				return new ArgumentBuilderRequired3();
			}

			/**
			 * Treat this argument as a flag. A flag is a value, which is true if present, and false if not present.
			 * It takes no arguments, and cannot be the default argument.
			 * @return The next step in the build process.
			 */
			public FlagBuilderMode asFlag() {
				return new FlagBuilderMode();
			}
		}

		public class ArgumentBuilderRequired3 {
			ArgumentBuilderRequired3(){}

			/**
			 * Configures this as a required argument. If the argument is missing, then a {@link ValidationException}
			 * will be thrown during parsing.
			 * @return The next step in the build process.
			 */
			public RequiredArgumentBuilderMode setRequired() {
				ArgumentBuilder.this.required = true;
				return new RequiredArgumentBuilderMode();
			}

			/**
			 * Shortcut to setting this argument as required and as the default argument.
			 * @return The final step in the build process.
			 */
			public RequiredArgumentBuilderOptional setRequiredAndDefault() {
				return setRequired().asDefault();
			}

			/**
			 * Configures this as an optional argument. The build
			 * process will allow you to set a default value if
			 * desired.
			 * @return The next step in the build process.
			 */
			public OptionalArgumentBuilderMode setOptional() {
				ArgumentBuilder.this.required = false;
				return new OptionalArgumentBuilderMode();
			}

			/**
			 * Shortcut to setting this argument as optional and as the default argument.
			 * @return The final step in the build process.
			 */
			public OptionalArgumentBuilderOptional setOptionalAndDefault() {
				return setOptional().asDefault();
			}

		}

		public class FlagBuilderMode {
			/**
			 * Creates an argument that is a flag. That is, this is a boolean true if present,
			 * boolean false if not. There are no arguments attached to this value.
			 * @param shortArg The short code for the argument
			 * @return The next step in the build process.
			 */
			public ArgumentBuilderFlag setName(char shortArg) {
				new OptionalArgumentBuilderMode().setName(shortArg);
				argType = Type.BOOLEAN;
				return new ArgumentBuilderFlag();
			}

			/**
			 * Creates an argument that is a flag. That is, this is a boolean true if present,
			 * boolean false if not. There are no arguments attached to this value.
			 * @param longArg The long code for the argument
			 * @return The next step in the build process.
			 */
			public ArgumentBuilderFlag setName(String longArg) {
				new OptionalArgumentBuilderMode().setName(longArg);
				argType = Type.BOOLEAN;
				return new ArgumentBuilderFlag();
			}

			/**
			 * Creates an argument that is a flag. That is, this is a boolean true if present,
			 * boolean false if not. There are no arguments attached to this value.
			 * @param shortArg The short code for the argument
			 * @param longArg The long code for the argument
			 * @return The next step in the build process.
			 */
			public ArgumentBuilderFlag setName(char shortArg, String longArg) {
				new OptionalArgumentBuilderMode().setName(shortArg, longArg);
				argType = Type.BOOLEAN;
				return new ArgumentBuilderFlag();
			}
		}

		public class OptionalArgumentBuilderMode {
			/**
			 * Sets the name of this argument, using a short code. The value passed in must
			 * be a printable character (as defined by {@link Character#isAlphabetic(int)})
			 * and cannot be \0.
			 *
			 * In this mode, the argument may only be addressed via the short arg.
			 *
			 * This does not set a flag, this sets an argument with input.
			 * @param shortArg The short arg
			 * @return The next step in the build process
			 */
			public OptionalArgumentBuilderOptional setName(char shortArg) {
				if(shortArg == '\0') {
					throw new NullPointerException("shortArg may not be the null character");
				}

				if(!Character.isAlphabetic(shortArg)) {
					throw new IllegalArgumentException("shortArg must be a alphabetical character");
				}

				mode = Mode.SHORT;
				ArgumentBuilder.this.shortArg = shortArg;
				return new OptionalArgumentBuilderOptional();
			}

			/**
			 * Sets the name of this argument, using a long code. The value passed in may not
			 * be null.
			 *
			 * In this mode, the argument may only be addressed via the long arg.
			 *
			 * This does not set a flag, this sets an argument with input.
			 *
			 * @param longArg The long arg
			 * @return The next step in the build process
			 */
			public OptionalArgumentBuilderOptional setName(String longArg) {
				Objects.requireNonNull(longArg, "longArg must not be null");

				mode = Mode.LONG;
				ArgumentBuilder.this.longArg = longArg;
				return new OptionalArgumentBuilderOptional();
			}

			/**
			 * Sets the name of this argument, using a short code and a long code.
			 * The short arg passed in must
			 * be a printable character (as defined by {@link Character#isAlphabetic(int)})
			 * and cannot be \0.
			 *
			 * The long arg passed in may not be null.
			 *
			 * @param shortArg The single character address for this argument
			 * @param longArg The long name of this argument
			 * @return The next step in the build process.
			 */
			public OptionalArgumentBuilderOptional setName(char shortArg, String longArg) {
				setName(shortArg);
				setName(longArg);

				// reset mode
				mode = Mode.BOTH;
				return new OptionalArgumentBuilderOptional();
			}

			/**
			 * Considers this argument to be a default argument, that is, this contains
			 * the parameters passed in without parameter names. There may only be one of
			 * these arguments in the list. If a second one is attempted to be added, an
			 * exception is thrown.
			 * @return The next step in the build process.
			 */
			public OptionalArgumentBuilderOptional asDefault() {
				mode = Mode.DEFAULT;
				return new OptionalArgumentBuilderOptional();
			}
		}

		public class RequiredArgumentBuilderMode {
			/**
			 * Sets the name of this argument, using a short code. The value passed in must
			 * be a printable character (as defined by {@link Character#isAlphabetic(int)})
			 * and cannot be \0.
			 *
			 * In this mode, the argument may only be addressed via the short arg.
			 *
			 * This does not set a flag, this sets an argument with input.
			 * @param shortArg The short arg
			 * @return The next step in the build process
			 */
			public RequiredArgumentBuilderOptional setName(char shortArg) {
				if(shortArg == '\0') {
					throw new NullPointerException("shortArg may not be the null character");
				}

				if(!Character.isAlphabetic(shortArg)) {
					throw new IllegalArgumentException("shortArg must be a alphabetical character");
				}

				mode = Mode.SHORT;
				ArgumentBuilder.this.shortArg = shortArg;
				return new RequiredArgumentBuilderOptional();
			}

			/**
			 * Sets the name of this argument, using a long code. The value passed in may not
			 * be null.
			 *
			 * In this mode, the argument may only be addressed via the long arg.
			 *
			 * This does not set a flag, this sets an argument with input.
			 *
			 * @param longArg The long arg
			 * @return The next step in the build process
			 */
			public RequiredArgumentBuilderOptional setName(String longArg) {
				Objects.requireNonNull(longArg, "longArg must not be null");

				mode = Mode.LONG;
				ArgumentBuilder.this.longArg = longArg;
				return new RequiredArgumentBuilderOptional();
			}

			/**
			 * Sets the name of this argument, using a short code and a long code.
			 * The short arg passed in must
			 * be a printable character (as defined by {@link Character#isAlphabetic(int)})
			 * and cannot be \0.
			 *
			 * The long arg passed in may not be null.
			 *
			 * @param shortArg The single character address for this argument
			 * @param longArg The long name of this argument
			 * @return The next step in the build process.
			 */
			public RequiredArgumentBuilderOptional setName(char shortArg, String longArg) {
				setName(shortArg);
				setName(longArg);

				// reset mode
				mode = Mode.BOTH;
				return new RequiredArgumentBuilderOptional();
			}

			/**
			 * Considers this argument to be a default argument, that is, this contains
			 * the parameters passed in without parameter names. There may only be one of
			 * these arguments in the list. If a second one is attempted to be added, an
			 * exception is thrown.
			 * @return The next step in the build process.
			 */
			public RequiredArgumentBuilderOptional asDefault() {
				mode = Mode.DEFAULT;
				return new RequiredArgumentBuilderOptional();
			}
		}

		/**
		 * A subset of types that are only valid for non-flag types.
		 */
		public static enum BuilderTypeNonFlag {
			/**
			 * A single string value
			 */
			STRING(Type.STRING),
			/**
			 * An array of strings
			 */
			ARRAY_OF_STRINGS(Type.ARRAY_OF_STRINGS),
			/**
			 * A numeric value
			 */
			NUMBER(Type.NUMBER),
			/**
			 * An array of numeric values
			 */
			ARRAY_OF_NUMBERS(Type.ARRAY_OF_NUMBERS);

			Type type;
			private BuilderTypeNonFlag(Type type) {
				this.type = type;
			}

			Type getType() {
				return this.type;
			}
		}

		/**
		 * Represents an object that is ready to be built. While user code cannot
		 * build the Argument directly, this is used internally.
		 */
		public abstract class ArgumentBuilderFinal {
			private ArgumentBuilderFinal(){}

			/**
			 * After building, this Argument should be in a consistent state.
			 * @return
			 */
			abstract Argument build();
		}

		public final class ArgumentBuilderFlag extends ArgumentBuilderFinal {
			private ArgumentBuilderFlag(){}

			@Override
			Argument build() {
				return new OptionalArgumentBuilderOptional().build();
			}
		}

		public final class OptionalArgumentBuilderOptional extends ArgumentBuilderFinal {
			OptionalArgumentBuilderOptional(){}

			/**
			 * Sets the argument type. By default, ARRAY_OF_STRINGS is assumed.
			 * @param argType The type of this argument
			 * @return {@code this}, which can be called repeatedly to set the
			 * optional arguments, or used as is.
			 */
			public OptionalArgumentBuilderOptional setArgType(BuilderTypeNonFlag argType) {
				Objects.requireNonNull(argType, "argType cannot be null");
				ArgumentBuilder.this.argType = argType.getType();
				return this;
			}

			/**
			 * For arguments that are not required, the default value may be set.
			 * If this argument is required, calling this method is an error.
			 * @param defaultVal
			 * @return {@code this}, which can be called repeatedly to set the
			 * optional arguments, or used as is.
			 */
			public OptionalArgumentBuilderOptional setDefaultVal(String defaultVal) {
				if(required) {
					throw new IllegalArgumentException("Required arguments cannot have a default value provided");
				}
				ArgumentBuilder.this.defaultValue = defaultVal;
				return this;
			}

			@Override
			Argument build() {
				return new Argument(shortArg == '\0' ? null : shortArg, longArg, argType, defaultValue, description,
						usageName, required);
			}

		}

		public final class RequiredArgumentBuilderOptional extends ArgumentBuilderFinal {
			RequiredArgumentBuilderOptional(){}

			/**
			 * Sets the argument type. By default, ARRAY_OF_STRINGS is assumed.
			 * @param argType The type of this argument
			 * @return {@code this}, which can be called repeatedly to set the
			 * optional arguments, or used as is.
			 */
			public RequiredArgumentBuilderOptional setArgType(BuilderTypeNonFlag argType) {
				Objects.requireNonNull(argType, "argType cannot be null");
				ArgumentBuilder.this.argType = argType.getType();
				return this;
			}

			@Override
			Argument build() {
				return new OptionalArgumentBuilderOptional().build();
			}

		}

	}

	/**
	 * A description of the command itself.
	 */
	String description = "";
	String extendedDescription = "";
	/**
	 * The model for the arguments
	 */
	List<Argument> argumentModel = new ArrayList<>();
	/**
	 * Whether to throw an error if unrecognized arguments were provided
	 */
	boolean errorOnUnknown = true;

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

	private static final class Argument {

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
				defaultList = ArgumentParser.lex(defaultVal);
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
				arrayVal = ArgumentParser.lex(val);
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
					b.append(TermColors.GREEN).append("-").append(shortArg).append(TermColors.RESET);
				} else {
					b.append(TermColors.GREEN).append("--").append(longArg).append(TermColors.RESET);
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

	public final class ArgumentParserResults {

		List<Argument> arguments = new ArrayList<>();
		List<String> unclassified = new ArrayList<>();
		List<String> rawArgs;

		private ArgumentParserResults(List<String> rawArgs) {
			this.rawArgs = new ArrayList<>(rawArgs);
		}

		private void updateArgument(Argument a) {
			if(a == null) {
				return;
			}
			List<Argument> toRemove = new ArrayList<>();
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

		private void updateUnclassifiedArgument(String a) {
			unclassified.add(a);
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
					return new ArrayList<>();
				}
				return new ArrayList<>(a.arrayVal);
			} catch (ResultUseException e) {
				return new ArrayList<>();
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
		 * Returns the list of values associated with the switch represented by this short code. If the switch
		 * wasn't set, the default return value is returned instead.
		 * @param flag
		 * @param defaultReturn
		 * @return
		 * @throws com.laytonsmith.PureUtilities.ArgumentParser.ResultUseException
		 */
		public List<String> getStringListArgument(Character flag, List<String> defaultReturn)
				throws ResultUseException {
			List<String> d = getStringListArgument(flag);
			if(d == null) {
				return defaultReturn;
			}
			return d;
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

		/**
		 * Returns the list of values associated with the switch represented by this long code. If the switch
		 * wasn't set, the default return value is returned instead.
		 * @param flag
		 * @param defaultReturn
		 * @return
		 * @throws com.laytonsmith.PureUtilities.ArgumentParser.ResultUseException
		 */
		public List<String> getStringListArgument(String flag, List<String> defaultReturn) throws ResultUseException {
			List<String> d = getStringListArgument(flag);
			if(d == null) {
				return defaultReturn;
			}
			return d;
		}

		private List<String> getStringListArgument(Argument arg) {
			if(arg == null) {
				return null;
			}
			if(arg.argType != Type.ARRAY_OF_STRINGS) {
				throw new ClassCastException("Argument type not set to " + Type.ARRAY_OF_STRINGS.name() + ". Cannot return a " + "string list" + ".");
			}
			return new ArrayList<>(arg.arrayVal);
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

			List<Double> list = new ArrayList<>();
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

		/**
		 * Returns a list of the raw, unprocessed arguments. This includes all arguments as is, with no processing.
		 * @return
		 */
		public List<String> getRawArguments() {
			return new ArrayList<>(rawArgs);
		}
	}

	public static ArgumentParser GetParser() {
		return new ArgumentParser();
	}

	static enum Type {
		STRING,
		NUMBER,
		ARRAY_OF_STRINGS,
		ARRAY_OF_NUMBERS,
		BOOLEAN
	}

	/**
	 * Adds the configured argument to the ArgumentParser.
	 * <p>
	 * To build an Argument, create a new instance of {@link ArgumentBuilder}.
	 * @param arg The argument to add to the list
	 * @return {@code this}, for continued chaining
	 */
	public ArgumentParser addArgument(ArgumentBuilder.ArgumentBuilderFinal arg) {
		Argument arg0 = arg.build();
		// Check to make sure this isn't a duplicate value
		for(Argument a : argumentModel) {
			// default arg
			if(a.shortArg == null && a.longArg == null) {
				// This is the default arg, ensure that arg is not the default
				if(arg0.shortArg == null && arg0.longArg == null) {
					throw new IllegalArgumentException("Only 1 default argument may be provided.");
				}
			}
			if(a.shortArg != null && arg0.shortArg != null && a.shortArg.equals(arg0.shortArg)) {
				throw new IllegalArgumentException("A new argument with short arg '" + arg0.shortArg + "' was provided,"
						+ " but a previous argument with the same short arg was already provided.");
			}
			if(a.longArg != null && arg0.longArg != null && a.longArg.equals(arg0.longArg)) {
				throw new IllegalArgumentException("A new argument with long arg \"" + arg0.longArg + "\" was provided,"
						+ " but a previous argument with the same long arg was already provided.");
			}
		}
		argumentModel.add(arg0);
		return this;
	}

	/**
	 * Adds a description to the ArgumentParser object, which is used when
	 * building the description returned by getBuiltDescription. For consistency,
	 * it is arbitrarily decided that these descriptions should end with a period.
	 *
	 * @param description
	 * @return
	 */
	public ArgumentParser addDescription(String description) {
		this.description = description;
		return this;
	}

	/**
	 * Provides an extended description. This is not returned with {@link #getDescription()}, but is included
	 * as part of the built description.
	 * @param extendedDescription
	 * @return
	 */
	public ArgumentParser addExtendedDescription(String extendedDescription) {
		this.extendedDescription = extendedDescription;
		return this;
	}

	/**
	 * If set to true (which is the default), then unknown options will cause an error. If false,
	 * they will not cause an error, and can even still be accessed in the results. However, this is
	 * only recommended for situations where arguments are processed in a later step as well, if all
	 * the possibly valid arguments are known up front, then this should generally remain true, so
	 * as to fail faster and give the user a better overall experience.
	 *
	 * Also note that it is possible to escape dashes, so the argument parser doesn't accept the argument
	 * as a known flag, but instead a literal, which can then be further processed later by another
	 * argument parser, for instance. It may be better to instead instruct users to pass the arguments
	 * through escaped, so that arguments can still be validated. (This works for both short and
	 * long arguments, i.e. {@code \-s} or {@code \--long}.)
	 *
	 * @param errorOnUnknown
	 * @return {@code this} for easier chaining.
	 */
	public ArgumentParser setErrorOnUnknownArgs(boolean errorOnUnknown) {
		this.errorOnUnknown = errorOnUnknown;
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
		if(!this.extendedDescription.equals("")) {
			b.append("\t").append(this.extendedDescription).append("\n\n");
		}
		List<Character> shortCodes = new ArrayList<>();
		List<String> longCodes = new ArrayList<>();
		List<Character> shortCodesDone = new ArrayList<>();
		List<String> longCodesDone = new ArrayList<>();
		List<String> aliases = new ArrayList<>();
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
		List<Character> shortFlags = new ArrayList<>();
		List<String> longFlags = new ArrayList<>();
		List<Character> shortArguments = new ArrayList<>();
		List<String> longArguments = new ArrayList<>();
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

		b.append(TermColors.BOLD).append("Usage:\n\t").append(TermColors.RESET);
		//Get the short flags first, then the long flags, then the short arguments, then the long arguments
		List<String> parts = new ArrayList<>();
		if(!shortFlags.isEmpty()) {
			StringBuilder usage = new StringBuilder();
			usage.append("[").append("-");
			for(Character c : shortFlags) {
				usage.append(c);
			}
			usage.append("]");
			parts.add(usage.toString());
		}

		for(String s : longFlags) {
			parts.add("[--" + s + "]");
		}

		List<Argument> usageList = new ArrayList<>();
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
			String s = "<";
			if(getArgument().isNumeric()) {
				s += "#";
			}
			s += getArgument().usageName;
			if(getArgument().isArray()) {
				s += ", ...";
			}
			s += ">";
			parts.add(s);
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

		b.append("\n\n");
		Argument def = getArgument();
		if(def != null && def.description != null) {
			b.append(def.generateDescription(false));
		}

		if(flags.length() != 0) {
			b.append(TermColors.BOLD).append("Flags").append(TermColors.RESET);
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
			} else {
				b.append(TermColors.BOLD).append("Options:\n").append(TermColors.RESET);
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
	 * Returns just the description that was registered with {@link #addDescription(java.lang.String)}
	 *
	 * @return The description, or empty string, if one has not been set yet.
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
	 * @throws com.laytonsmith.PureUtilities.ArgumentParser.ValidationException
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
	 * @throws com.laytonsmith.PureUtilities.ArgumentParser.ValidationException
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
		List<String> arguments = new ArrayList<>();
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
		ArgumentParserResults results = new ArgumentParserResults(args);
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
				if(errorOnUnknown && getArgument(arg) == null) {
					throw new ValidationException("Unrecognized argument: " + arg);
				}
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
						if(errorOnUnknown) {
							throw new ValidationException("Unrecognized flag: " + c);
						} else {
							results.updateUnclassifiedArgument("-" + c.toString());
							continue;
						}
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
		//Check to see if all the required values are here
		List<String> missing = new ArrayList<>();
		model: for(Argument model : argumentModel) {
			if(model.required) {
				for(Argument r : results.arguments) {
					if(r.modelEquals(model)) {
						continue model;
					}
				}
				if(model.shortArg != null && model.longArg == null) {
					missing.add(model.shortArg.toString());
				} else if(model.shortArg == null && model.longArg != null) {
					missing.add(model.longArg);
				} else if(model.shortArg != null && model.longArg != null) {
					missing.add(model.shortArg + "/" + model.longArg);
				} else {
					missing.add("<default argument>");
				}
			}
		}
		if(!missing.isEmpty()) {
			throw new ValidationException("Missing required argument(s): " + StringUtils.Join(missing, ", "));
		}

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
