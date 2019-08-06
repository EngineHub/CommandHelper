/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.tools.docgen.localization;

import com.laytonsmith.PureUtilities.ArgumentParser;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.core.AbstractCommandLineTool;
import com.laytonsmith.core.tool;
import java.io.File;
import java.io.IOException;
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
						.setArgType(ArgumentParser.ArgumentBuilder.BuilderTypeNonFlag.STRING));
		}

		@Override
		public void execute(ArgumentParser.ArgumentParserResults parsedArgs) throws Exception {
			String database = parsedArgs.getStringArgument(DATABASE);
			LocalizationUI.launch(database);
		}

		@Override
		public boolean noExitOnReturn() {
			return true;
		}
	}
}
