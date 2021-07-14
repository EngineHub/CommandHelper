package com.laytonsmith.core.asm;

import com.laytonsmith.PureUtilities.CommandExecutor;
import com.laytonsmith.PureUtilities.Common.FileUtil;
import com.laytonsmith.PureUtilities.Common.OSUtils;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.InternalException;
import com.laytonsmith.core.MethodScriptCompiler;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Profiles;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.functions.FunctionBase;
import com.laytonsmith.core.functions.FunctionList;
import com.laytonsmith.persistence.DataSourceException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class AsmCompiler {

	private static final Set<Class<? extends Environment.EnvironmentImpl>> ENVS = new HashSet<>();
	static {
		ENVS.add(GlobalEnv.class);
		ENVS.add(CompilerEnvironment.class);
		ENVS.add(LLVMEnvironment.class);
	}

	private final File llc;
	private final File lld;
	private final LLVMEnvironment llvmenv;

	public AsmCompiler(File llc, File lld) {
		this.llc = llc;
		this.lld = lld;
		llvmenv = new LLVMEnvironment();
	}

	/**
	 * Looks in platform specific default locations for the assembler and linker.
	 */
	public AsmCompiler() {
		File llc;
		File lld;
		if(OSUtils.GetOS().isWindows()) {
			llc = new File("C:\\Program Files\\LLVM\\bin\\llc.exe");
			lld = new File("C:\\Program Files\\LLVM\\bin\\lld-link.exe");
		} else {
			throw new UnsupportedOperationException("OS not yet supported");
		}
		this.llc = llc;
		this.lld = lld;
		llvmenv = new LLVMEnvironment();
		// TODO remove this later
		llvmenv.setOutputIRCodeTargetLogging(true);
	}

	/**
	 * Returns the current environment, which can be modified with various compiler options that have i.e. been passed
	 * in from cmdline.
	 * @return
	 */
	public LLVMEnvironment getEnvironment() {
		return llvmenv;
	}

	public void compileEntryPoint(File file, File outputDirectory, String exeName)
			throws IOException, DataSourceException, URISyntaxException, Profiles.InvalidProfileException,
			ConfigCompileException, ConfigCompileGroupException, InterruptedException {
		if(!AsmInstaller.validateToolchain()) {
			return;
		}
		StringBuilder program = new StringBuilder();
		compileFile(file, program);
		StringBuilder ir = new StringBuilder();
		StringBuilder strings = new StringBuilder();
		String nl = OSUtils.GetLineEnding();
		strings.append(nl);
		for(Map.Entry<String, String> entry : llvmenv.getStrings().entrySet()) {
			String string = entry.getKey();
			String id = entry.getValue();
			strings.append("$").append(id).append(" = comdat any").append(nl);
			strings.append("@").append(id).append(" = linkonce_odr dso_local unnamed_addr constant [")
					.append(string.length() + 1).append(" x i8] c\"").append(string)
					.append("\\00\", comdat, align 1").append(nl);
		}
		String irTop = /*"source_filename = \"" + file.getName() + "\"" + nl
				+*/ "define dso_local i32 @main() {" + nl;
		String irBottom = "\tret i32 0" + nl + "}" + nl;
		ir.append(strings.toString());
		ir.append(irTop);
		ir.append(program.toString());
		ir.append(irBottom);
		ir.append(llvmenv.getGlobalDeclarations());
		File ll = new File(outputDirectory, exeName + ".ll");
		File obj = new File(outputDirectory, exeName + ".obj");
		FileUtil.write(ir.toString(), ll);
		{
			String[] args = new String[]{
				llc.getAbsolutePath(),
				"--filetype=obj",
				"-o=\"" + obj.getAbsolutePath() + "\"",
				ll.getAbsolutePath()
			};
			CommandExecutor ex = new CommandExecutor(args);
			ex.setWorkingDir(outputDirectory);
			ex.setSystemInputsAndOutputs();
			ex.start();
			int exitCode = ex.waitFor();
			if(exitCode != 0) {
				throw new InternalException("Assembly failed.");
			}
		}
		{
			List<String> args = new ArrayList<>();
			args.add(lld.getAbsolutePath());
			if(OSUtils.GetOS().isWindows()) {
				String sdkBase = "C:\\Program Files (x86)\\Windows Kits\\10\\Lib\\10.0.19041.0\\";
				String sdkDepth = (OSUtils.GetOSBitDepth() == OSUtils.BitDepth.B64 ? "x64" : "x86");
				args.add("/out:\"" + exeName + ".exe\"");
				args.add("/entry:main");
				args.add("/defaultlib:" + sdkBase + "ucrt\\" + sdkDepth + "\\ucrt.lib");
			} else {
				throw new UnsupportedOperationException("OS not yet supported");
			}
			args.add(obj.getAbsolutePath());
			CommandExecutor ex = new CommandExecutor(args.toArray(new String[args.size()]));
			ex.setWorkingDir(outputDirectory);
			ex.setSystemInputsAndOutputs();
			ex.start();
			int exitCode = ex.waitFor();
			if(exitCode != 0) {
				throw new InternalException("Linking failed.");
			}
		}
	}

	private void compileFile(File file, StringBuilder ir)
			throws IOException, DataSourceException, Profiles.InvalidProfileException,
				ConfigCompileException, URISyntaxException, ConfigCompileGroupException {
		String script = FileUtil.read(file);
		Environment env = Static.GenerateStandaloneEnvironment(true);
		env = env.cloneAndAdd(llvmenv);
		ParseTree tree
				= MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, env, file, true), env, ENVS);
		if(tree != null) {
			ir.append(getIR(tree.getChildAt(0), env));
		} else {
			ir.append("\tunreachable").append(OSUtils.GetLineEnding());
		}
	}

	private String getIR(ParseTree node, Environment env) throws ConfigCompileException {
		if(!(node.getData() instanceof CFunction)) {
			return api.Platforms.COMPILER_LLVM.getResolver().outputConstant(node.getData(), env);
		}
		CFunction cf = ((CFunction) node.getData());
		FunctionBase fb = FunctionList.getFunction(cf, api.Platforms.COMPILER_LLVM, ENVS);
		if(fb instanceof LLVMFunction) {
			return ((LLVMFunction) fb).getIR(node.getTarget(), env, null, node.getChildren().toArray(new ParseTree[0]));
		} else {
			throw new ConfigCompileException("Unsupported function " + cf.getName(), node.getTarget());
		}
	}
}
