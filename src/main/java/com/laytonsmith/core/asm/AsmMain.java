package com.laytonsmith.core.asm;

import com.laytonsmith.PureUtilities.ArgumentParser;
import com.laytonsmith.PureUtilities.ArgumentParser.ArgumentBuilder;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.core.AbstractCommandLineTool;
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

	private static void LogErrorAndQuit(String error, int code) {
		StreamUtils.GetSystemErr().println(error);
		System.exit(code);
	}

	@tool(value = "asm", undocumented = true)
	public static class AsmMainCmdlineTool extends AbstractCommandLineTool {

		@Override
		public ArgumentParser getArgumentParser() {
			return ArgumentParser.GetParser()
					.addDescription("Provides the interface for compiling MethodScript to native executables. The"
							+ " system compiles to LLVM, and so many of the options here are just wrappers around"
							+ " various LLVM tools.")
					.addArgument(new ArgumentBuilder()
						.setDescription("Installs the LLVM compiler toolchain. This is not necessary if your"
								+ " system is already set up with the toolchain, but this will automatically install"
								+ " the proper toolchain for you. Run as root/Administrator. Ignores other options,"
								+ " and exits once installation is complete. Installation is indempotent, you may"
								+ " run this unconditionally, and if everything is installed correctly, nothing will"
								+ " happen. New updates to MethodScript may require reinstallation of the toolchain.")
						.asFlag()
						.setName("install-toolchain"))
					.addArgument(new ArgumentBuilder()
						.setDescription("Provides the input file/directory. If given a folder, the directory is"
								+ " scanned recursively to find all the ms files, with a file at the root"
								+ " named \"main.ms\" taken to be the entry point. If given a single file, it"
								+ " is compiled individually, and regardless of the name, is considered to"
								+ " be the entry point. By default, the current directory is used.")
						.setUsageName("input")
						.setOptionalAndDefault()
						.setArgType(ArgumentBuilder.BuilderTypeNonFlag.STRING))
					.addArgument(new ArgumentBuilder()
						.setDescription("Provides the output directory where the outputs should be placed."
								+ " By default, this is considered to be the directory ./target.")
						.setUsageName("output file")
						.setOptional()
						.setName('o', "output")
						.setArgType(ArgumentBuilder.BuilderTypeNonFlag.STRING))
					.addArgument(new ArgumentBuilder()
						.setDescription("Sets the output name of the executable."
								+ " The extension is added automatically. If a single file is provided as the"
								+ " input, the name is inherited from that file. Otherwise, the name is inherited"
								+ " by the containing folder.")
						.setUsageName("executable name")
						.setOptional()
						.setName("executable-name")
						.setArgType(ArgumentBuilder.BuilderTypeNonFlag.STRING))
					.addArgument(new ArgumentBuilder()
						.setDescription("Disables outputting of code target information in the LLVM IR file.")
						.asFlag()
						.setName("no-target-logging"));
		}

		@Override
		public void execute(ArgumentParser.ArgumentParserResults parsedArgs) throws Exception {
			if(parsedArgs.isFlagSet("install-toolchain")) {
				try {
					new AsmInstaller().install();
				} catch (IOException | InterruptedException e) {
					StreamUtils.GetSystemErr().println(e.getMessage());
					System.exit(1);
				}
				System.exit(0);
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
						LogErrorAndQuit("Currently only single files are supported.", 1);
					} else {
						AsmCompiler compiler = new AsmCompiler();
						if(parsedArgs.isFlagSet("no-target-logging")) {
							compiler.getEnvironment().setOutputIRCodeTargetLogging(false);
						}
						compiler.compileEntryPoint(input, output, exeName);
					}
				} catch (ConfigCompileException ex) {
					Set<ConfigCompileException> exs = new HashSet<>();
					exs.add(ex);
					throw new ConfigCompileGroupException(exs);
				}
			} catch (ConfigCompileGroupException ex) {
				ConfigRuntimeException.HandleUncaughtException(ex, "One or more compile errors occured during compilation.", null);
			}
		}

	}
}
