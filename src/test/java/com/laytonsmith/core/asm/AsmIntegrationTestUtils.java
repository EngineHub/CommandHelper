package com.laytonsmith.core.asm;

import com.laytonsmith.PureUtilities.CommandExecutor;
import com.laytonsmith.PureUtilities.Common.FileUtil;
import com.laytonsmith.PureUtilities.Common.OSUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.Implementation.Type;
import com.laytonsmith.core.asm.AsmMain.AsmMainCmdlineTool;
import com.laytonsmith.testing.StaticTest;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class AsmIntegrationTestUtils {

	private static boolean installed = false;
	private static boolean validated = false;
	private static boolean validationResult = false;

	/**
	 * Installs the toolchain. This is only run once per program, but unconditionally installs it.
	 *
	 * @throws Exception
	 */
	public static void installToolchain() throws Exception {
		if(System.getProperty("methodscript_run_llvm_integration_tests") == null
				&& System.getenv("methodscript_run_llvm_integration_tests") == null) {
			throw new UnsupportedOperationException();
		}
		if(!installed) {
			Implementation.setServerType(Type.TEST);
			StaticTest.InstallFakeServerFrontend();
			AsmMainCmdlineTool tool = new AsmMain.AsmMainCmdlineTool();
			tool.execute(tool.getArgumentParser().match("--install-toolchain"));
			installed = true;
		}
	}

	/**
	 * Returns true if the test should be skipped, because LLVM is not installed on this platform, and it couldn't
	 * automatically be installed, or if the methodscript_run_llvm_integration_tests system property or environment
	 * variable is not set.
	 *
	 * @return
	 */
	public static boolean skipTest() throws Exception {
		if(System.getProperty("methodscript_run_llvm_integration_tests") == null
				&& System.getenv("methodscript_run_llvm_integration_tests") == null) {
			return true;
		}
		installToolchain();
		if(validated) {
			return !validationResult;
		}
		validationResult = AsmInstaller.validateToolchain();
		validated = true;
		return !validationResult;
	}

	/**
	 * Writes the program out to a temporary file, and returns a reference to the file. The file is marked for
	 * deletion on exit.
	 * <p>
	 * If skipTest returns true and this method is called, an exception is thrown.
	 *
	 * @param program
	 * @return
	 * @throws Exception
	 */
	public static File installProgram(String program) throws Exception {
		if(skipTest()) {
			throw new UnsupportedOperationException();
		}
		File f = File.createTempFile("methodScriptAsmIntegrationTest", ".ms");
		FileUtil.write(program, f);
		f.deleteOnExit();
		File target = new File(f.getParentFile(), "target");
		target.mkdir();
		target.deleteOnExit();
		return f;
	}

	/**
	 * Compiles the file.
	 * <p>
	 * If skipTest returns true and this method is called, an exception is thrown.
	 *
	 * @param file
	 * @throws Exception
	 */
	public static void compileFile(File file) throws Exception {
		if(skipTest()) {
			throw new UnsupportedOperationException();
		}
		AsmMainCmdlineTool tool = new AsmMain.AsmMainCmdlineTool();
		tool.execute(tool.getArgumentParser().match("\"" + file.getAbsolutePath() + "\" -o \""
				+ new File(file.getParentFile(), "target").getCanonicalPath() + "\" --verbose"));
	}

	/**
	 * Given a methodscript file, returns a reference to the executable.
	 *
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static File getExecutableFromMSFile(File file) throws Exception {
		File target = new File(file.getParentFile(), "target");
		String suffix = "";
		if(OSUtils.GetOS().isWindows()) {
			suffix = ".exe";
		}
		return new File(target, file.getName().substring(0, file.getName().indexOf(".")) + suffix);
	}

	/**
	 * Executes a program, and returns the output to system.out.
	 * <p>
	 * If skipTest returns true and this method is called, an exception is thrown.
	 *
	 * @param file
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public static String executeProgram(File file, String... args) throws Exception {
		if(skipTest()) {
			throw new UnsupportedOperationException();
		}
		String[] a = new String[args.length + 1];
		a[0] = file.getAbsolutePath();
		System.arraycopy(args, 0, a, 1, args.length);
		String ret = CommandExecutor.Execute(a);
		return ret;
	}

	/**
	 * Given a full program, installs the toolchain, writes the program to a temporary file, executes it, collects
	 * the output, and checks to make sure it matches the expected output. This is an all in one function, you may
	 * need to do some of the parts yourself, which are broken into different methods for code re-use where possible.
	 * <p>
	 * Note that if the output ends in a system newline, that is stripped.
	 * <p>
	 * Unlike the other piecemeal methods in this class, this one does not require the caller to try skipTest first. It
	 * will silently succeed without testing if the integration tests are disabled.
	 *
	 * @param expected The expected output. If the output doesn't match this, it fails the test.
	 * @param program  The program to run.
	 * @param args     Any arguments to pass to the program on the command line.
	 * @return
	 */
	public static void integrationTest(String expected, String program, String... args) throws Exception {
		if(skipTest()) {
			return;
		}
		String output = integrationTestAndReturn(program, args);
		assertEquals(expected, output);
	}

	/**
	 * Given a full program, installs the toolchain, writes the program to a temporary file, executes it, collects
	 * the output, and returns the output. This is an all in one function, you may
	 * need to do some of the parts yourself, which are broken into different methods for code re-use where possible.
	 * <p>
	 * Note that if the output ends with a system newline, that is stripped.
	 * <p>
	 * If skipTest returns true and this method is called, an exception is thrown.
	 *
	 * @param program
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public static String integrationTestAndReturn(String program, String... args) throws Exception {
		if(skipTest()) {
			throw new UnsupportedOperationException();
		}
		installToolchain();
		File msFile = installProgram(program);
		compileFile(msFile);
		File exe = getExecutableFromMSFile(msFile);
		String output = executeProgram(exe, args);
		if(output.endsWith(OSUtils.GetLineEnding())) {
			output = StringUtils.replaceLast(output, OSUtils.GetLineEnding(), "");
		}
		return output;
	}
}
