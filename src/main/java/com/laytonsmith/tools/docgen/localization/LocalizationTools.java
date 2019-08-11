package com.laytonsmith.tools.docgen.localization;

import com.laytonsmith.PureUtilities.ArgumentParser;
import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.CommandExecutor;
import com.laytonsmith.PureUtilities.Common.OSUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.core.AbstractCommandLineTool;
import com.laytonsmith.core.tool;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This file contains cmdline tools for validating translation memories.
 */
public class LocalizationTools {


	@tool(value = "validate-l10n")
	public static class ValidateL10nTool extends AbstractCommandLineTool {

		private static final String DATABASE = "database";

		@Override
		public ArgumentParser getArgumentParser() {
			return ArgumentParser.GetParser()
					.addDescription("(Validate 'L ten N'.) Validates the given localization database."
							+ " This is important to do before"
							+ " submitting a PR to the localization database, otherwise the PR will be rejected."
							+ " The command will print a list"
							+ " of errors and exit with code 1 if there are validation errors, otherwise the command"
							+ " will exit with exit code 0 and print nothing.")
					.addArgument(new ArgumentParser.ArgumentBuilder()
						.setDescription("The location of the database to validate.")
						.setUsageName("path to folder")
						.setRequired()
						.setName(DATABASE)
						.setArgType(ArgumentParser.ArgumentBuilder.BuilderTypeNonFlag.STRING));
		}

		@Override
		public void execute(ArgumentParser.ArgumentParserResults parsedArgs) throws Exception {
			String d = parsedArgs.getStringArgument(DATABASE);
			File database = new File(d);
			if(!database.exists()) {
				System.err.println("Could not find the specified database \"" + d + "\"");
				System.exit(1);
			}

			try {
				// this throws an IOException if the xml itself is invalid
				Set<String> errors = new TranslationMaster(database).validate();
				if(!errors.isEmpty()) {
					System.err.println("Validation failed with one or more errors:\n");
					System.err.println(StringUtils.Join(errors, "\n"));
					System.exit(1);
				}
			} catch (IOException ex) {
				System.err.println("Validation failed with the following exception:\n");
				System.err.println(ex.getMessage());
				System.exit(1);
			}
		}

	}

	@tool("l10n-ui")
	public static class LocalizationUITool extends AbstractCommandLineTool {

		private static final String DATABASE = "database";

		@Override
		public ArgumentParser getArgumentParser() {
			return ArgumentParser.GetParser()
					.addDescription("Launches the Localization (L10n) UI, which simplifies translations.")
					.addArgument(new ArgumentParser.ArgumentBuilder()
						.setDescription("Sets the database from the command line. If this is absent, the UI will"
								+ " prompt you for the location.")
						.setUsageName("path to database")
						.setOptional()
						.setName(DATABASE)
						.setArgType(ArgumentParser.ArgumentBuilder.BuilderTypeNonFlag.STRING))
					.addArgument(new ArgumentParser.ArgumentBuilder()
						.setDescription("Runs the UI in the same shell process. By default, it creates a new"
								+ " process and causes the initial shell to return.")
						.asFlag()
						.setName("in-shell"));
		}

		@Override
		public void execute(ArgumentParser.ArgumentParserResults parsedArgs) throws Exception {
			String database = parsedArgs.getStringArgument(DATABASE);
			if(parsedArgs.isFlagSet("in-shell")) {
				// Actually launch the GUI
				LocalizationUI.launch(database);
			} else {
				// Relaunch the jar in a new process with the --run flag set,
				// so that the process will be in its own subshell
				List<String> largs = new ArrayList<>();
				largs.add("java");
				largs.add("-jar");
				String jarPath = ClassDiscovery.GetClassContainer(LocalizationTools.class).getPath();
				if(OSUtils.GetOS().isWindows() && jarPath.startsWith("/")) {
					jarPath = jarPath.substring(1);
				}
				largs.add(jarPath);
				largs.add("l10n-ui");
				largs.addAll(parsedArgs.getRawArguments());
				largs.add("--in-shell");
				CommandExecutor ce = new CommandExecutor(largs.toArray(new String[largs.size()]));
				ce.start();
				System.exit(0);
			}
		}

		@Override
		public boolean noExitOnReturn() {
			return true;
		}
	}
}
