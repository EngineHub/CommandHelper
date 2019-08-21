package com.laytonsmith.PureUtilities;

import com.laytonsmith.PureUtilities.ArgumentParser.ArgumentParserResults;
import com.laytonsmith.PureUtilities.ArgumentParser.ResultUseException;
import com.laytonsmith.PureUtilities.ArgumentParser.ValidationException;
import com.laytonsmith.PureUtilities.Common.ArrayUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * An ArgumentSuite is an ArgumentParser that supports "modes".
 *
 * A mode is a required parameter that causes a fully separate argument parser to be used to parse the remaining
 * arguments. This allows for finer control over mutually exclusive parameters in both the documentation and the
 * validation, as well as wider support for traditional use cases.
 *
 */
public class ArgumentSuite {

	private final Map<String, ArgumentParser> suite;
	private final Map<String, String> aliases;
	private String description;

	public ArgumentSuite() {
		suite = new TreeMap<>((o1, o2) -> {
			return o1.compareTo(o2);
		});
		aliases = new LinkedHashMap<>();
	}

	/**
	 * Adds a new mode. A mode name may contain dashes, which would look like normal argument flags, but would actually
	 * be a mode. This is useful especially for a --help command, which shows the ArgumentSuite's help.
	 *
	 * @param modeName The name of this mode. This may not contain spaces.
	 * @param mode The sub-ArgumentParser that will be used when in this mode.
	 * @return
	 * @throws IllegalArgumentException if the name of the mode contains spaces
	 */
	public ArgumentSuite addMode(String modeName, ArgumentParser mode) {
		validateModeName(modeName);
		suite.put(modeName, mode);
		return this;
	}

	/**
	 * Adds a mode alias. This is the recommended behavior instead of adding the same mode with a different name,
	 * because the built description is aware of the difference between an alias and the real mode. All the same rules
	 * apply to aliases that apply to mode names. The realModeName doesn't strictly need to exist yet.
	 *
	 * @param alias
	 * @param realModeName
	 * @return
	 */
	public ArgumentSuite addModeAlias(String alias, String realModeName) {
		validateModeName(alias);
		aliases.put(alias, realModeName);
		return this;
	}

	private void validateModeName(String modeName) {
		if(modeName.contains(" ")) {
			throw new IllegalArgumentException("The mode name may not contain a space.");
		}
	}

	/**
	 * Adds a description, which is used in {
	 *
	 * @see #getBuiltDescription}
	 * @param description
	 * @return
	 */
	public ArgumentSuite addDescription(String description) {
		this.description = description;
		return this;
	}

	/**
	 * Returns a mode that was previously registered.
	 *
	 * @param name The name of the mode to get
	 * @return The mode registered under the provided name
	 * @throws IllegalArgumentException if the mode is not registered
	 */
	public ArgumentParser getMode(String name) {
		if(suite.containsKey(name)) {
			return suite.get(name);
		} else {
			throw new IllegalArgumentException("No mode by the name \"" + name + "\" has been registered.");
		}
	}

	/**
	 * Selects the appropriate mode, and calls match on that ArgumentParser.
	 *
	 * @param args The pre-parsed arguments
	 * @param defaultMode The default mode, which will be used only if no arguments were passed in.
	 * @return
	 * @throws ResultUseException if the mode cannot be found, or if the sub-ArgumentParser throws an exception.
	 * @throws com.laytonsmith.PureUtilities.ArgumentParser.ValidationException
	 * @throws com.laytonsmith.PureUtilities.ArgumentSuite.ModeNotFoundException If the mode itself was not found.
	 */
	public ArgumentSuiteResults match(String[] args, String defaultMode) throws ResultUseException,
			ValidationException, ModeNotFoundException {
		String[] nonModeArgs = ArrayUtils.EMPTY_STRING_ARRAY;
		String mode;
		if(args.length > 1) {
			mode = args[0];
			nonModeArgs = ArrayUtils.cast(ArrayUtils.slice(args, 1, args.length - 1), String[].class);
		} else if(args.length == 1) {
			mode = args[0];
		} else {
			//0 argsm, use the defaultMode.
			mode = defaultMode;
		}
		if(aliases.containsKey(mode)) {
			mode = aliases.get(mode);
		}
		if(suite.containsKey(mode)) {
			return new ArgumentSuiteResults(mode, suite.get(mode), suite.get(mode).match(nonModeArgs));
		} else {
			throw new ModeNotFoundException("Mode " + mode + " was not found.");
		}
	}

	/**
	 * Selects the appropriate mode, and calls match on that ArgumentParser.
	 *
	 * @param args The unparsed arguments
	 * @param defaultMode The default mode, which will be used only if no arguments were passed in.
	 * @return
	 * @throws ResultUseException if the mode cannot be found, or if the sub-ArgumentParser throws an exception.
	 * @throws com.laytonsmith.PureUtilities.ArgumentParser.ValidationException
	 * @throws com.laytonsmith.PureUtilities.ArgumentSuite.ModeNotFoundException If the mode could not be found
	 */
	public ArgumentSuiteResults match(String args, String defaultMode) throws ResultUseException, ValidationException,
			ModeNotFoundException {
		//We're going to use ArgumentParser's parse method to get a string list, then
		//pass that to the other match
		return match(ArgumentParser.lex(args).toArray(new String[]{}), defaultMode);
	}

	/**
	 * Returns a built description of this ArgumentSuite, which would be appropriate to display if no arguments are
	 * passed in (or the mode name is help, -help, --help, etc)
	 *
	 * @return
	 */
	public String getBuiltDescription() {
		StringBuilder b = new StringBuilder();
		if(description != null) {
			b.append(description).append("\n\n");
		}
		b.append("Modes: (a mode must be the first argument) \n");
		for(String mode : suite.keySet()) {
			b.append("\t").append(TermColors.BOLD).append(TermColors.BRIGHT_GREEN).append(mode);
			if(aliases.containsValue(mode)) {
				List<String> keys = new ArrayList<>();
				for(String alias : aliases.keySet()) {
					if(aliases.get(alias).equals(mode)) {
						keys.add(alias);
					}
				}
				b.append(TermColors.RESET).append(" (Alias");
				if(keys.size() != 1) {
					b.append("es");
				}
				b.append(": ").append(StringUtils.Join(keys, ", ")).append(")");
			}
			b.append(TermColors.RESET).append(": ").append(suite.get(mode).getDescription()).append("\n");
		}
		return b.toString();
	}

	/**
	 * A convenience method to get the real mode name registered for this alias, or null if no such alias exists. If the
	 * alias is actually a mode, it is simply returned. Useful for perhaps a help mode, to resolve the actual mode
	 * named. If {@code alias} is null, null is returned.
	 *
	 * @param alias
	 * @return
	 */
	public String getModeFromAlias(String alias) {
		if(alias == null) {
			return null;
		}
		if(suite.containsKey(alias)) {
			return alias;
		} else if(aliases.containsKey(alias)) {
			return aliases.get(alias);
		} else {
			return null;
		}
	}

	/**
	 * A convenience method to get the underlying ArgumentParser based on the mode name given. Aliases will not suffice,
	 * but you may call getModeFromAlias to resolve the mode name first. Null is returned if no mode exists with that
	 * name. Useful for perhaps a help mode, to generically display a mode's help. If {@code mode} is null, null is returned.
	 *
	 * @param mode
	 * @return
	 */
	public ArgumentParser getModeFromNameOrNull(String mode) {
		if(mode == null) {
			return null;
		}
		if(suite.containsKey(mode)) {
			return suite.get(mode);
		} else {
			return null;
		}
	}

	/**
	 * This exception is only thrown if the mode could not be found. If that is the case, the arguments were not
	 * attempted to be parsed. If the exception is not thrown, then that means that the mode WAS found, but the
	 * arguments are wrong. It can be appropriate to then manually look up the mode, and display the help just
	 * for that mode, instead of all modes.
	 */
	public static final class ModeNotFoundException extends Exception {

		public ModeNotFoundException(String message) {
			super(message);
		}

	}

	public static final class ArgumentSuiteResults {

		private final ArgumentParser mode;
		private final ArgumentParserResults results;
		private final String modeName;

		private ArgumentSuiteResults(String modeName, ArgumentParser mode, ArgumentParserResults results) {
			this.modeName = modeName;
			this.mode = mode;
			this.results = results;
		}

		/**
		 * Returns the name of the mode that was selected. (Not the alias)
		 *
		 * @return
		 */
		public String getModeName() {
			return modeName;
		}

		/**
		 * The ArgumentParser for the given mode. This will be a reference to the mode passed in, so you can do == on
		 * it.
		 *
		 * @return
		 */
		public ArgumentParser getMode() {
			return mode;
		}

		/**
		 * The ArgumentParserResults for the ArgumentParser mode
		 *
		 * @return
		 */
		public ArgumentParserResults getResults() {
			return results;
		}
	}

}
