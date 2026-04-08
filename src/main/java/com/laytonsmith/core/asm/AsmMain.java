package com.laytonsmith.core.asm;

import com.laytonsmith.PureUtilities.ArgumentParser;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.TermColors;
import com.laytonsmith.core.AbstractCommandLineTool;
import com.laytonsmith.core.InternalException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.tool;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public class AsmMain {

	@tool(value = "asm", undocumented = true)
	public static class AsmMainCmdlineTool extends AbstractCommandLineTool {

		private boolean throwOnError = false;

		/**
		 * When set to true, errors that would normally call System.exit will instead throw a
		 * RuntimeException. This is intended for use in test environments where System.exit
		 * would kill the test runner.
		 */
		public void setThrowOnError(boolean value) {
			this.throwOnError = value;
		}

		private void logErrorAndQuit(String error, int code) {
			StreamUtils.GetSystemErr().println(TermColors.RED + error + TermColors.RESET);
			if(throwOnError) {
				throw new RuntimeException("ASM compilation failed (exit code " + code + "): " + error);
			}
			System.exit(code);
		}

		@Override
		public ArgumentParser getArgumentParser() {
			// Normally we would keep this builder in here, but there are so many options that it makes
			// more sense to just keep it all in the AsmCompiler class.
			return AsmCompiler.getArgs();
		}

		@Override
		public void execute(ArgumentParser.ArgumentParserResults parsedArgs) throws Exception {
			if(parsedArgs.isFlagSet("install-toolchain") || parsedArgs.isFlagSet("install-toolchain-non-interactive")) {
				try {
					new AsmInstaller().install(parsedArgs.isFlagSet("install-toolchain-non-interactive"));
				} catch (IOException | InterruptedException e) {
					StreamUtils.GetSystemErr().println(e.getMessage());
					if(throwOnError) {
						throw new RuntimeException("Toolchain installation failed", e);
					}
					System.exit(1);
				}
				return;
			}

			File input = new File(".");
			File output = new File("./target");
			if(!"".equals(parsedArgs.getStringArgument())) {
				input = new File(parsedArgs.getStringArgument());
			}
			if(parsedArgs.getStringArgument('o') != null) {
				output = new File(parsedArgs.getStringArgument('o'));
			}
			String exeName = input.isDirectory() ? input.getName() : input.getName().replaceAll("\\..*?$", "");

			try {
				try {
					if(input.isDirectory()) {
						logErrorAndQuit("Currently only single files are supported.", 1);
					} else {
						AsmCompiler compiler = new AsmCompiler(parsedArgs);
						try {
							compiler.compileEntryPoint(input, output, exeName);
						} catch (IOException ex) {
							logErrorAndQuit(ex.getMessage(), 1);
						} catch (InternalException ex) {
							logErrorAndQuit(ex.getMessage() + "\nAn internal exception occurred, which is not caused by your code. "
									+ (parsedArgs.isFlagSet("verbose")
									? "Please report this with all the above information."
									: "Please re-run with the --verbose switch."), 2);
						}
					}
				} catch (ConfigCompileException ex) {
					Set<ConfigCompileException> exs = new HashSet<>();
					exs.add(ex);
					throw new ConfigCompileGroupException(exs);
				}
			} catch (ConfigCompileGroupException ex) {
				ConfigRuntimeException.HandleUncaughtException(ex, "One or more compile errors occurred during compilation.", null);
			}
		}
	}
}
