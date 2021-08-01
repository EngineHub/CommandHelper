package com.laytonsmith.core.asm;

import com.laytonsmith.PureUtilities.ArgumentParser;
import com.laytonsmith.PureUtilities.CommandExecutor;
import com.laytonsmith.PureUtilities.Common.FileUtil;
import com.laytonsmith.PureUtilities.Common.OSUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.InternalException;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.MethodScriptCompiler;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Profiles;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.asm.metadata.IRMetadata;
import com.laytonsmith.core.asm.metadata.IRMetadataDICompileUnit;
import com.laytonsmith.core.asm.metadata.IRMetadataDIFile;
import com.laytonsmith.core.asm.metadata.IRMetadataDISubprogram;
import com.laytonsmith.core.asm.metadata.IRMetadataDISubroutineType;
import com.laytonsmith.core.asm.metadata.LLVMMetadataRegistry;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.Target;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class AsmCompiler {

	public static ArgumentParser getArgs() {
		return ArgumentParser.GetParser()
					.addDescription("Provides the interface for compiling MethodScript to native executables. The"
							+ " system compiles to LLVM, and so many of the options here are just wrappers around"
							+ " various LLVM tools. Make sure you install the toolchain first with"
							+ " --install-toolchain.")
					.addArgument(new ArgumentParser.ArgumentBuilder()
						.setDescription("Installs the LLVM compiler toolchain. This is not necessary if your"
								+ " system is already set up with the toolchain, but this will automatically install"
								+ " the proper toolchain for you. Run as root/Administrator. Ignores other options,"
								+ " and exits once installation is complete. Installation is indempotent, you may"
								+ " run this unconditionally, and if everything is installed correctly, nothing will"
								+ " happen. New updates to MethodScript may require reinstallation of the toolchain.")
						.asFlag()
						.setName("install-toolchain"))
					.addArgument(new ArgumentParser.ArgumentBuilder()
						.setDescription("Provides the input file/directory. If given a folder, the directory is"
								+ " scanned recursively to find all the ms files, with a file at the root"
								+ " named \"main.ms\" taken to be the entry point. If given a single file, it"
								+ " is compiled individually, and regardless of the name, is considered to"
								+ " be the entry point. By default, the current directory is used.")
						.setUsageName("input")
						.setOptionalAndDefault()
						.setArgType(ArgumentParser.ArgumentBuilder.BuilderTypeNonFlag.STRING))
					.addArgument(new ArgumentParser.ArgumentBuilder()
						.setDescription("Provides the output directory where the outputs should be placed."
								+ " By default, this is considered to be the directory ./target.")
						.setUsageName("output file")
						.setOptional()
						.setName('o', "output")
						.setArgType(ArgumentParser.ArgumentBuilder.BuilderTypeNonFlag.STRING))
					.addArgument(new ArgumentParser.ArgumentBuilder()
						.setDescription("Sets the output name of the executable."
								+ " The extension is added automatically. If a single file is provided as the"
								+ " input, the name is inherited from that file. Otherwise, the name is inherited"
								+ " by the containing folder.")
						.setUsageName("executable name")
						.setOptional()
						.setName("executable-name")
						.setArgType(ArgumentParser.ArgumentBuilder.BuilderTypeNonFlag.STRING))
					.addArgument(new ArgumentParser.ArgumentBuilder()
						.setDescription("Disables outputting of code target information in the LLVM IR file.")
						.asFlag()
						.setName("no-target-logging"))
					.addArgument(new ArgumentParser.ArgumentBuilder()
						.setDescription("Disables optimizations. This is only useful for debugging, and should not"
								+ " normally be set. If this flag is set, --extraopt is ignored.")
						.asFlag()
						.setName("noopt"))
					.addArgument(new ArgumentParser.ArgumentBuilder()
						.setDescription("Does more aggressive optimizations. This can be done for release binaries,"
								+ " but increases compile time, but in theory may make programs faster.")
						.asFlag()
						.setName("extraopt"))
					.addArgument(new ArgumentParser.ArgumentBuilder()
						.setDescription("Compiles the files to IR without including the headers or running llvm-link,"
								+ " then quits. Useful for debugging the compiler, but results in potentially"
								+ " incomplete (and thus uncompilable) IR.")
						.asFlag()
						.setName("no-llvm-link"))
					.addArgument(new ArgumentParser.ArgumentBuilder()
						.setDescription("Sets the build version. Can be release or debug. Debug builds tend to contain"
								+ " more detailed information, but note that some debug information is set in all"
								+ " builds. Defaults to \"release\", but may be \"debug\" instead.")
						.setUsageName("<debug/release>")
						.setOptional()
						.setName("build-mode")
						.setDefaultVal("release")
						.setArgType(ArgumentParser.ArgumentBuilder.BuilderTypeNonFlag.STRING));
	}

	private static final Set<Class<? extends Environment.EnvironmentImpl>> ENVS = new HashSet<>();
	static {
		ENVS.add(GlobalEnv.class);
		ENVS.add(CompilerEnvironment.class);
		ENVS.add(LLVMEnvironment.class);
	}

	private final File llc;
	private final File lld;
	private final File clang;
	private final File llvmlink;
	private final LLVMEnvironment llvmenv;
	private final ArgumentParser.ArgumentParserResults asmOptions;

	/**
	 * Looks in platform specific default locations for the assembler and linker.
	 * @param asmOptions
	 */
	public AsmCompiler(ArgumentParser.ArgumentParserResults asmOptions) {
		this.asmOptions = asmOptions;
		File llc;
		File lld;
		File clang;
		File llvmlink;
		if(OSUtils.GetOS().isWindows()) {
			llc = new File("C:\\Program Files\\LLVM\\bin\\llc.exe");
			lld = new File("C:\\Program Files\\LLVM\\bin\\lld-link.exe");
			clang = new File("C:\\Program Files\\LLVM\\bin\\clang.exe");
			llvmlink = new File("C:\\Program Files\\LLVM\\bin\\llvm-link.exe");
		} else {
			throw new UnsupportedOperationException("OS not yet supported");
		}
		this.llc = llc;
		this.lld = lld;
		this.clang = clang;
		this.llvmlink = llvmlink;
		llvmenv = new LLVMEnvironment();
		// TODO remove this later
		llvmenv.setOutputIRCodeTargetLogging(true);
		if(asmOptions.isFlagSet("no-target-logging")) {
			llvmenv.setOutputIRCodeTargetLogging(false);
		}
		if(asmOptions.isFlagSet("extraopt")) {
			llvmenv.setOptimizationLevel(LLVMEnvironment.OptimizationLevel.EXTRA);
		}
		if(asmOptions.isFlagSet("noopt")) {
			llvmenv.setOptimizationLevel(LLVMEnvironment.OptimizationLevel.NONE);
		}
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
		Environment env = Static.GenerateStandaloneEnvironment(true);
		File ll;
		File obj;
		if(!file.getAbsolutePath().endsWith(".ll")) {
			if(!AsmInstaller.validateToolchain()) {
				return;
			}
			StringBuilder program = new StringBuilder();
			llvmenv.newMethodFrame("@main");
			// Pop 0..2 for the main args and entry label
			for(int i = 0; i <= 2; i++) {
				llvmenv.getNewLocalVariableReference();
			}

			env = env.cloneAndAdd(llvmenv);
			Target t = new Target(0, file, 0);
			String versionString = "MethodScript version " + MSVersion.LATEST.toString();
			IRMetadataDIFile diFile = new IRMetadataDIFile(env, file, "release".equals(asmOptions.getStringArgument("build-mode")));
			LLVMMetadataRegistry mdRegistry = llvmenv.getMetadataRegistry();
			IRMetadata enums = mdRegistry.getEmptyTuple(env);
			IRMetadata retainedTypes = mdRegistry.getEmptyTuple(env);
			IRMetadata globals = mdRegistry.getEmptyTuple(env);
			IRMetadata imports = mdRegistry.getEmptyTuple(env);
			IRMetadataDISubroutineType mainType = new IRMetadataDISubroutineType(env, CInt.TYPE, CClassType.EMPTY_CLASS_ARRAY);
			IRMetadataDICompileUnit compileUnit = new IRMetadataDICompileUnit(env, diFile, versionString,
					/* isOptimized */ false, enums, retainedTypes, globals, imports);
			IRMetadataDISubprogram subprogram = new IRMetadataDISubprogram(env, "main", diFile, t, mainType, compileUnit);
			compileUnit.setIsDistinct(true);
			IRMetadata llvmDbgCu = IRMetadata.AsAnonymousTuple(env, compileUnit.getReference());
			IRMetadata versionInfo = IRMetadata.AsTuple(env, "!\"" + versionString + "\"");
			IRMetadata llvmIdent = IRMetadata.AsAnonymousTuple(env, versionInfo.getReference());
			IRMetadata llvmModuleFlags = IRMetadata.AsAnonymousTuple(env,
					newModuleFlagsMetadata(env, ModuleFlagMode.MAX, "Dwarf Version", "i32 2").getReference(),
					newModuleFlagsMetadata(env, ModuleFlagMode.MAX, "Debug Info Version", "i32 3").getReference()
			);
			subprogram.setIsDistinct(true);
			IRBuilder builder = compileFile(file, env);
			// If the last line is a terminal statement, we can simplify
			String lastLine = builder.lines.get(builder.lines.size() - 1);
			if(!"unreachable".equals(lastLine)) {
				builder.appendLine(new Target(0, new File("synth"), 0), "ret i32 0");
			}
			program.append(builder.renderIR(env));
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

			////////////////// DATA LAYOUT
			String nameMangling = "e";
			if(OSUtils.GetOS().isWindows()) {
				if(OSUtils.GetOSBitDepth() == OSUtils.BitDepth.B32) {
					nameMangling = "x";
				} else {
					nameMangling = "w";
				}
			}
			// https://llvm.org/docs/LangRef.html#langref-datalayout
			//"e-m:w-p270:32:32-p271:32:32-p272:64:64-i64:64-f80:128-n8:16:32:64-S128"
			String[] datalayout = new String[] {
				"e", // little endian
				"m:" + nameMangling, // name mangling: e = elf type, x = windows x86, w = windows COFF
				"p270:32:32", // Specifies the address space that corresponds to program memory. Harvard architectures can use this to specify what space LLVM should place things such as functions into.
				"p271:32:32",
				"p272:64:64",
				"i64:64", // integer alignment
				"f80:128", // fp alignment
				"n8:16:32:64", // integer widths
				"S128", // Natural alignment of the stack in bits
			};

			String targetTriple = getClangTriple();

			String irHeader = "source_filename = \"" + file.getName() + "\"" + nl
					+ "target datalayout = \"" + StringUtils.Join(datalayout, "-") + "\"" + nl
					+ "target triple = \"" + targetTriple + "\"" + nl;
			if(env.getEnv(CompilerEnvironment.class).getTargetOS().isWindows()) {
				irHeader += "@_fltused = constant i32 0" + nl;
				irHeader += "@__fltused = constant i32 0" + nl;
			}

			String irTop = "define dso_local i32 @main(i32 %0, i8** %1) !dbg " + subprogram.getReference() + " {" + nl;
			String irBottom = "}" + nl + nl;
			String namedMetadata = "!llvm.dbg.cu = " + llvmDbgCu.getDefinition() + nl
					+ "!llvm.ident = " + llvmIdent.getDefinition() + nl
					+ "!llvm.module.flags = " + llvmModuleFlags.getDefinition() + nl;


			ir.append(irHeader); // always first
			ir.append(strings.toString());
			ir.append(irTop);
			ir.append(program.toString());
			ir.append(irBottom);
			ir.append(llvmenv.getGlobalDeclarations()).append(nl);
			ir.append(namedMetadata);
			ir.append(StringUtils.Join(builder.metadata, nl));
			ll = new File(outputDirectory, exeName + ".ll");
			FileUtil.write(ir.toString(), ll);
			if(env.getEnv(CompilerEnvironment.class).getTargetOS().isWindows()) {
				obj = new File(outputDirectory, exeName + ".obj");
			} else {
				obj = new File(outputDirectory, exeName + ".o");
			}
		} else { // end if !.ll
			ll = file;
			// Put the obj file in the same directly.
			if(env.getEnv(CompilerEnvironment.class).getTargetOS().isWindows()) {
				obj = new File(file.getParentFile(), exeName + ".obj");
			} else {
				obj = new File(file.getParentFile(), exeName + ".o");
			}

		}

		if(asmOptions.isFlagSet("no-llvm-link")) {
			// Done.
			return;
		}

		{
			////////////////// LLVM-LINK
			List<File> headersLL = new ArrayList<>();
			for(LLVMEnvironment.Header header : llvmenv.getAdditionalHeaders()) {
				File headerLL = new AsmHeaderCompiler(clang, header).parse();
				headersLL.add(headerLL);
			}
			if(!headersLL.isEmpty()) {
				List<String> args = new ArrayList<>();
				args.add(llvmlink.getAbsolutePath());
				args.add("--disable-debug-info-type-map");
				args.add("-o");
				args.add(ll.getAbsolutePath());
				args.add("-S"); // Write output in LLVM IR (instead of bitcode).
				args.add(ll.getAbsolutePath());
				for(File h : headersLL) {
					args.add(h.getAbsolutePath());
				}
				CommandExecutor ex = new CommandExecutor(args.toArray(new String[args.size()]));
				ex.setWorkingDir(ll.getParentFile());
				ex.setSystemInputsAndOutputs();
				ex.start();
				int exitCode = ex.waitFor();
				if(exitCode != 0) {
					throw new InternalException("Header linkage failed.");
				}
			}
		}
		{
			////////////////// COMPILE
			String[] args = new String[]{
				llc.getAbsolutePath(),
				"--filetype=obj",
				"-o=\"" + obj.getAbsolutePath() + "\"",
				"--preserve-as-comments",
				llvmenv.getOptimizationLevel().getArg(),
				ll.getAbsolutePath()
			};
			CommandExecutor ex = new CommandExecutor(args);
			ex.setWorkingDir(ll.getParentFile());
			ex.setSystemInputsAndOutputs();
			ex.start();
			int exitCode = ex.waitFor();
			if(exitCode != 0) {
				throw new InternalException("Assembly failed.");
			}
		}
		{
			////////////////// LINK
			List<String> args = new ArrayList<>();
			args.add(lld.getAbsolutePath());
			Map<String, String> subprocessEnv = new HashMap<>();
			if(env.getEnv(CompilerEnvironment.class).getTargetOS().isWindows()) {
				String sdkBase = "C:\\Program Files (x86)\\Windows Kits\\10\\";
				String sdkVersion = "10.0.19041.0\\";
				String libBase = sdkBase + "Lib\\" + sdkVersion + "\\";
				String targetDepth = (OSUtils.GetOSBitDepth() == OSUtils.BitDepth.B64 ? "x64" : "x86"); // TODO make this selectable
				args.add("/out:\"" + exeName + ".exe\"");
				args.add("/entry:main");
				String msvcBase = "C:\\Program Files (x86)\\Microsoft Visual Studio\\2019\\BuildTools\\VC\\Tools\\MSVC\\14.29.30037\\lib\\" + targetDepth + "\\";
				String[] libs = new String[]{
					"msvcrt.lib", "libcmt.lib"
				};
				args.addAll(Arrays.asList(libs));
				subprocessEnv.put("LIB", msvcBase + ";"
						+ libBase + "ucrt\\" + targetDepth + ";"
						+ libBase + "um\\" + targetDepth + ";"
						+ System.getenv("LIB"));
				subprocessEnv.put("LIBPATH", msvcBase + ";"
						+ System.getenv("LIBPATH"));
				args.add("/libpath:" + sdkBase + "um\\" + targetDepth);
//				args.add("/MT");
//				args.add("/MD");
//				args.add("/defaultlib:" + MSVCBase + "libcmt.lib");
				args.add("/defaultlib:" + libBase + "ucrt\\" + targetDepth + "\\ucrt.lib");
//				args.add("/defaultlib:" + libBase + "ucrt\\" + sdkDepth + "\\libucrt.lib");
				args.add("/subsystem:console");
			} else {
				throw new UnsupportedOperationException("OS not yet supported");
			}
			args.add(obj.getAbsolutePath());
			CommandExecutor ex = new CommandExecutor(args.toArray(new String[args.size()]));
			ex.setEnvironmentVariables(subprocessEnv);
			ex.setWorkingDir(obj.getParentFile());
			ex.setSystemInputsAndOutputs();
			ex.start();
			int exitCode = ex.waitFor();
			if(exitCode != 0) {
				throw new InternalException("Linking failed.");
			}
		}
	}

	private IRBuilder compileFile(File file, Environment env)
			throws IOException, DataSourceException, Profiles.InvalidProfileException,
				ConfigCompileException, URISyntaxException, ConfigCompileGroupException {
		String script = FileUtil.read(file);
		env = env.cloneAndAdd(); // TODO: This probably isn't good? Might just remove this. Need to revist once multiple files are compiled.
		ParseTree tree
				= MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, env, file, true), env, ENVS);
		IRBuilder builder = new IRBuilder();
		if(tree != null) {
			getIR(builder, tree.getChildAt(0), env);
		} else {
			builder.appendLine(Target.UNKNOWN, "unreachable");
		}
		builder.setFinalMetadata(env);
		return builder;
	}

	public static IRData getIR(IRBuilder builder, ParseTree node, Environment env) throws ConfigCompileException {
		if(node.getData() instanceof CFunction cf) {
			FunctionBase fb = FunctionList.getFunction(cf, api.Platforms.COMPILER_LLVM, ENVS);
			if(fb instanceof LLVMFunction f) {
				return f.buildIR(builder, node.getTarget(), env,
						node.getChildren().toArray(new ParseTree[node.getChildren().size()]));
			} else {
				throw new Error("Unexpected function type");
			}
		} else {
			return LLVMPlatformResolver.outputConstant(node.getData(), env);
		}
	}

	public String getClangTriple() throws InterruptedException, IOException {
		String args[] = new String[]{
			clang.getAbsolutePath(),
			"--print-effective-triple"
		};
		return CommandExecutor.Execute(args).replace("\n", "").replace("\r", "");
	}

	private static enum ModuleFlagMode {
		/**
		 * Emits an error if two values disagree, otherwise the resulting value is that of the operands.
		 */
		ERROR(1),
		/**
		 * Emits a warning if two values disagree. The result value will be the operand for the flag from the first
		 * module being linked, or the max if the other module uses Max (in which case the resulting flag will be Max).
		 */
		WARNING(2),
		/**
		 * Adds a requirement that another module flag be present and have a specified value after linking is performed.
		 * The value must be a metadata pair, where the first element of the pair is the ID of the module flag to be
		 * restricted, and the second element of the pair is the value the module flag should be restricted to. This
		 * behavior can be used to restrict the allowable results (via triggering of an error) of linking IDs with the
		 * Override behavior.
		 */
		REQUIRE(3),
		/**
		 * Uses the specified value, regardless of the behavior or value of the other module. If both modules specify
		 * Override, but the values differ, an error will be emitted.
		 */
		OVERRIDE(4),
		/**
		 * Appends the two values, which are required to be metadata nodes.
		 */
		APPEND(5),
		/**
		 * Appends the two values, which are required to be metadata nodes. However, duplicate entries in the second
		 * list are dropped during the append operation.
		 */
		APPEND_UNIQUE(6),
		/**
		 * Takes the max of the two values, which are required to be integers.
		 */
		MAX(7);

		private final int value;
		private ModuleFlagMode(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}

	/**
	 * Returns a new module flag tuple. Note that flagValue is not escaped, and used as is, but flagName is quoted
	 * properly, however internal quotes should be escaped first.
	 * @param env
	 * @param mode The merge mode, if two modules define the same flags.
	 * @param flagName The name of the flag.
	 * @param flagValue The value of the flag.
	 * @return
	 */
	private IRMetadata newModuleFlagsMetadata(Environment env, ModuleFlagMode mode, String flagName, String flagValue) {
		// https://llvm.org/docs/LangRef.html#module-flags-metadata
		String[] tuples = new String[]{
			"i32 " + mode.getValue(),
			"!\"" + flagName + "\"",
			flagValue
		};
		return IRMetadata.AsTuple(env, tuples);
	}
}
