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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class AsmCompiler {

	private final static Set<Class<? extends Environment.EnvironmentImpl>> ENVS = new HashSet<>();
	static {
		ENVS.add(GlobalEnv.class);
		ENVS.add(CompilerEnvironment.class);
		ENVS.add(LLVMEnvironment.class);
	}

	private final File llc;
	private final File lld;

	public AsmCompiler(File llc, File lld) {
		this.llc = llc;
		this.lld = lld;
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
	}

	public void compileEntryPoint(File file, File outputDirectory, String exeName)
			throws IOException, DataSourceException, URISyntaxException, Profiles.InvalidProfileException,
			ConfigCompileException, ConfigCompileGroupException, InterruptedException {
		StringBuilder ir = new StringBuilder();
		StringBuilder strings = new StringBuilder();

		strings.append("\n");
		String irTop = "@exit = global i64 0\n"
				+ "declare i32 @puts(i8* nocapture) nounwind\n"
				+ "define i64* @main() {\n";
		String irBottom = "exit:\n"
				+ "ret i64* @exit\n"
				+ "}\n"
				+ "define i32* @mainCRTStartup() {\n"
				+ "%ret = call i64* @main()\n"
				+ "%ret32 = bitcast i64* @exit to i32*\n"
				+ "ret i32* %ret32\n"
				+ "}";
		ir.append(strings.toString());
		ir.append(irTop);
		compileFile(file, ir);
		ir.append(irBottom);
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
//			StringWrit
//			ex.setSystemOut();
			String error = "Not yet implemented!"; //CommandExecutor.Execute(outputDirectory, );
			if(!"".equals(error)) {
				throw new InternalException("Assembly failed:\n" + error);
			}
		}
		{
			String error = CommandExecutor.Execute(outputDirectory, lld.getAbsolutePath(), obj.getAbsolutePath());
			if(!"".equals(error)) {
				throw new InternalException("Linking failed:\n" + error);
			}
		}
	}

	private void compileFile(File file, StringBuilder ir)
			throws IOException, DataSourceException, Profiles.InvalidProfileException,
				ConfigCompileException, URISyntaxException, ConfigCompileGroupException {
		String script = FileUtil.read(file);
		Environment env = Static.GenerateStandaloneEnvironment(true);
		env = env.cloneAndAdd(new LLVMEnvironment());
		ParseTree tree
				= MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, env, file, true), env, ENVS);
		ir.append(getIR(tree.getChildAt(0), env));
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
