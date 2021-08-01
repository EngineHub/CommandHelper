package com.laytonsmith.core.asm;

import com.laytonsmith.PureUtilities.CommandExecutor;
import com.laytonsmith.PureUtilities.Common.FileUtil;
import com.laytonsmith.core.InternalException;
import java.io.File;
import java.io.IOException;

/**
 *
 */
public class AsmHeaderCompiler {
	private final File clang;
	private final LLVMEnvironment.Header header;

	public AsmHeaderCompiler(File clangPath, LLVMEnvironment.Header header) {
		this.clang = clangPath;
		this.header = header;
	}

	/**
	 * Compiles a given header, and returns a link to the ll file containing the IR.This file can then
	 * be used with llvm-link, to merge with other ll files.
	 * @return
	 * @throws java.io.IOException
	 * @throws java.lang.InterruptedException
	 */
	public File parse() throws IOException, InterruptedException {
		File tmp = File.createTempFile("systemGen", ".c");
		File tmpIR = File.createTempFile("systemGen", ".ll");
		String code = "#include ";
		if(header.type == LLVMEnvironment.HeaderType.SYSTEM) {
			code += "<" + header.name + ">";
		} else {
			code += "\"" + header.name + "\"";
		}
		code += "\n";
		FileUtil.write(code, tmp, true);
		String[] args = new String[]{
			clang.getAbsolutePath(),
			"-S",
			"-emit-llvm",
			"-w",
			"-Wfatal-errors",
			"-O0", // Otherwise it removes everything!
			"--output",
			tmpIR.getAbsolutePath(),
			tmp.getAbsolutePath()
		};
		CommandExecutor ex = new CommandExecutor(args);
		ex.setWorkingDir(tmp.getParentFile());
		ex.setSystemInputsAndOutputs();
		ex.start();
		int exitCode = ex.waitFor();
		if(exitCode != 0) {
			throw new InternalException("Header compilation failed.");
		}
		tmp.deleteOnExit();
		tmpIR.deleteOnExit();
		return tmpIR;
	}

}
