package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.ArgumentParser;
import com.laytonsmith.PureUtilities.ArgumentParser.ArgumentBuilder;
import com.laytonsmith.PureUtilities.ArgumentSuite;
import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscoveryCache;
import com.laytonsmith.PureUtilities.CommandExecutor;
import com.laytonsmith.PureUtilities.Common.ArrayUtils;
import com.laytonsmith.PureUtilities.Common.FileUtil;
import com.laytonsmith.PureUtilities.Common.OSUtils;
import com.laytonsmith.PureUtilities.Common.RSAEncrypt;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Common.UIUtils;
import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.PureUtilities.TermColors;
import com.laytonsmith.PureUtilities.XMLDocument;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.compiler.OptimizationUtilities;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.extensions.ExtensionManager;
import com.laytonsmith.core.extensions.ExtensionTracker;
import com.laytonsmith.core.functions.FunctionBase;
import com.laytonsmith.core.functions.FunctionList;
import com.laytonsmith.core.functions.Scheduling;
import com.laytonsmith.persistence.PersistenceNetwork;
import com.laytonsmith.persistence.io.ConnectionMixinFactory;
import com.laytonsmith.tools.ExampleLocalPackageInstaller;
import com.laytonsmith.tools.Interpreter;
import com.laytonsmith.tools.MSLPMaker;
import com.laytonsmith.tools.Manager;
import com.laytonsmith.tools.ProfilerSummary;
import com.laytonsmith.tools.SyntaxHighlighters;
import com.laytonsmith.tools.UILauncher;
import com.laytonsmith.tools.docgen.DocGen;
import com.laytonsmith.tools.docgen.DocGenExportTool;
import com.laytonsmith.tools.docgen.ExtensionDocGen;
import com.laytonsmith.tools.docgen.sitedeploy.APIBuilder;
import com.laytonsmith.tools.docgen.sitedeploy.SiteDeploy;
import com.laytonsmith.tools.pnviewer.PNViewer;
import java.awt.HeadlessException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jline.console.ConsoleReader;
import org.json.simple.JSONValue;

/**
 *
 *
 */
public class Main {

	public static final ArgumentSuite ARGUMENT_SUITE;
	private static final ArgumentParser HELP_MODE;

	private static final ArgumentParser COPYRIGHT_MODE;
	private static final ArgumentParser PRINT_DB_MODE;
	private static final ArgumentParser DOCS_MODE;
	private static final ArgumentParser VERIFY_MODE;
	private static final ArgumentParser INSTALL_CMDLINE_MODE;
	private static final ArgumentParser UNINSTALL_CMDLINE_MODE;
	private static final ArgumentParser SYNTAX_MODE;
	private static final ArgumentParser DOCGEN_MODE;
	private static final ArgumentParser API_MODE;
	private static final ArgumentParser EXAMPLES_MODE;
	private static final ArgumentParser OPTIMIZER_TEST_MODE;
	private static final ArgumentParser CMDLINE_MODE;
	private static final ArgumentParser EXTENSION_DOCS_MODE;
	private static final ArgumentParser DOC_EXPORT_MODE;
	private static final ArgumentParser PROFILER_SUMMARY_MODE;
	private static final ArgumentParser RSA_KEY_GEN_MODE;
	private static final ArgumentParser PM_VIEWER_MODE;
	private static final ArgumentParser CORE_FUNCTIONS_MODE;
	private static final ArgumentParser UI_MODE;
	private static final ArgumentParser SITE_DEPLOY;
	private static final ArgumentParser EXTENSION_BUILDER_MODE;
	// DO NOT ADD MORE TO THIS LIST. These will eventually all be ported to the @tool/CommandLineTool mechanism, which
	// allows far more flexibility, and provides better grouping anyways. Plus, it allows Main to be referenced
	// statically, without causing exceptions.

	static {
		// TODO: Remove these two lines, once all these are removed, and uncomment them within main()
		Implementation.setServerType(Implementation.Type.SHELL);
		MethodScriptFileLocations.setDefault(new MethodScriptFileLocations());

		ArgumentSuite suite = new ArgumentSuite()
				.addDescription("These are the command line tools for CommandHelper. For more information about a"
						+ " particular mode, run help <mode name>. To run a command, in general, use the command:\n\n"
						+ "\tjava -jar " + MethodScriptFileLocations.getDefault().getJarFile().getName() + " <mode name> <[mode specific arguments]>\n");
		HELP_MODE = ArgumentParser.GetParser()
				.addDescription("Displays help for all modes, or the given mode if one is provided.")
				.addArgument(new ArgumentBuilder().setDescription("Displays help for the given mode.")
						.setUsageName("mode name")
						.setOptionalAndDefault()
						.setArgType(ArgumentBuilder.BuilderTypeNonFlag.STRING))
				.setErrorOnUnknownArgs(false);
		suite.addMode("help", HELP_MODE).addModeAlias("--help", "help").addModeAlias("-help", "help")
				.addModeAlias("/?", "help");
		COPYRIGHT_MODE = ArgumentParser.GetParser()
				.addDescription("Prints the copyright and exits.");
		suite.addMode("copyright", COPYRIGHT_MODE);
		PRINT_DB_MODE = ArgumentParser.GetParser()
				.addDescription("Prints out the built in database in a human readable form, then exits.");
		suite.addMode("print-db", PRINT_DB_MODE);
		DOCS_MODE = ArgumentParser.GetParser()
				.addDescription("Prints documentation for the functions that CommandHelper knows about, then exits.")
				.addArgument(new ArgumentBuilder()
						.setDescription("The type of the documentation, defaulting to html."
							+ " It may be one of the following: "
							+ StringUtils.Join(DocGen.MarkupType.values(), ", ", ", or "))
						.setUsageName("type")
						.setOptionalAndDefault()
						.setDefaultVal("html"));
		suite.addMode("docs", DOCS_MODE);
		VERIFY_MODE = ArgumentParser.GetParser()
				.addDescription("Compiles the given file, returning a json describing the errors in the file, or returning"
						+ " nothing if the file compiles cleanly.")
				.addArgument(new ArgumentBuilder()
						.setDescription("The file to check")
						.setUsageName("file")
						.setRequiredAndDefault());
		suite.addMode("verify", VERIFY_MODE);
		INSTALL_CMDLINE_MODE = ArgumentParser.GetParser()
				.addDescription("Installs MethodScript to your system, so that commandline scripts work. (Currently only unix is supported.)");
		suite.addMode("install-cmdline", INSTALL_CMDLINE_MODE);
		UNINSTALL_CMDLINE_MODE = ArgumentParser.GetParser()
				.addDescription("Uninstalls the MethodScript interpreter from your system.");
		suite.addMode("uninstall-cmdline", UNINSTALL_CMDLINE_MODE);
		SYNTAX_MODE = ArgumentParser.GetParser()
				.addDescription("Generates the syntax highlighter for the specified editor (if available).")
				.addArgument(new ArgumentBuilder()
						.setDescription("The type of the syntax file to generate. Don't specify a type to see the"
								+ " available options.")
						.setUsageName("type")
						.setOptionalAndDefault()
						.setArgType(ArgumentBuilder.BuilderTypeNonFlag.STRING));
		suite.addMode("syntax", SYNTAX_MODE);
		DOCGEN_MODE = ArgumentParser.GetParser()
				.addDescription("Starts the automatic wiki uploader GUI.");
		suite.addMode("docgen", DOCGEN_MODE);
		API_MODE = ArgumentParser.GetParser()
				.addDescription("Prints documentation for the function specified, then exits. The argument is actually"
						+ " a regex, with ^ and $ added to it, so if you would like to search the function list,"
						+ " you can instead provide the rest of the regex. If multiple matches are found, the full"
						+ " list of matches is printed out. For instance \"array.*\" will return all the functions"
						+ " that start with the word \"array\".")
				.addArgument(new ArgumentBuilder()
						.setDescription("The name of the function to print the information for")
						.setUsageName("functionRegex")
						.setRequiredAndDefault()
						.setArgType(ArgumentBuilder.BuilderTypeNonFlag.STRING))
				.addArgument(new ArgumentBuilder()
						.setDescription("Instead of displaying the results in the console, launches the website with"
								+ " this function highlighted. The local documentation is guaranteed to be consistent"
								+ " with your local version of MethodScript, while the online results may be slightly"
								+ " stale, or may be from a different build, but the results are generally richer.")
						.asFlag().setName('o', "online"))
				.addArgument(new ArgumentBuilder()
						.setDescription("Also prints out the examples for the function (if any).")
						.asFlag().setName('e', "examples"));
		suite.addMode("api", API_MODE);
		EXAMPLES_MODE = ArgumentParser.GetParser()
				.addDescription("Installs one of the built in LocalPackage examples, which may in and of itself be useful.")
				.addArgument(new ArgumentBuilder()
						.setDescription("The name of the package to install. Leave blank to see a list of examples to"
								+ " choose from.")
						.setUsageName("packageName")
						.setOptionalAndDefault());
		suite.addMode("examples", EXAMPLES_MODE);
		OPTIMIZER_TEST_MODE = ArgumentParser.GetParser()
				.addDescription("Given a source file, reads it in and outputs the \"optimized\" version. This is meant as a debug"
						+ " tool, but could be used as an obfuscation tool as well.")
				.addArgument(new ArgumentBuilder()
						.setDescription("File path")
						.setUsageName("file")
						.setRequiredAndDefault());
		suite.addMode("optimizer-test", OPTIMIZER_TEST_MODE);
		CMDLINE_MODE = ArgumentParser.GetParser()
				.addDescription("Given a source file, runs it in cmdline mode. This is similar to"
						+ " the interpreter mode, but allows for tty input (which is required for some functions,"
						+ " like the prompt_* functions) and provides better information for errors, as the"
						+ " file is known.")
				.addArgument(new ArgumentBuilder()
						.setDescription("File path/arguments")
						.setUsageName("file and args")
						.setRequiredAndDefault());
		suite.addMode("cmdline", CMDLINE_MODE);
		EXTENSION_DOCS_MODE = ArgumentParser.GetParser()
				.addDescription("Generates markdown documentation for the specified extension utilizing its code, to be"
						+ " used most likely on the extensions github page.")
				.addArgument(new ArgumentBuilder()
						.setDescription("The extension jar to generate documentation for.")
						.setUsageName("path to jar file")
						.setRequired()
						.setName('i', "input-jar")
						.setArgType(ArgumentBuilder.BuilderTypeNonFlag.STRING))
				.addArgument(new ArgumentBuilder()
						.setDescription("The file to output the generated documentation to. (Should probably end in"
								+ " .md, but is not required to.) This argument is optional, and if left off, the"
								+ " output will instead print to stdout.")
						.setUsageName("output file name")
						.setOptional()
						.setName('o', "output-file"));
		suite.addMode("extension-docs", EXTENSION_DOCS_MODE);
		DOC_EXPORT_MODE = ArgumentParser.GetParser()
				.addDescription("Outputs all known function documentation as a json. This includes known extensions"
						+ " as well as the built in functions.")
				.addArgument(new ArgumentBuilder()
						.setDescription("Provides the path to your extension directory.")
						.setUsageName("extension folder")
						.setOptional()
						.setName("extension-dir")
						.setDefaultVal("./CommandHelper/extensions")
						.setArgType(ArgumentBuilder.BuilderTypeNonFlag.STRING))
				.addArgument(new ArgumentBuilder()
						.setDescription("The file to output the generated json to. If this parameter is missing, it is"
								+ " simply printed to screen.")
						.setUsageName("output file")
						.setOptional()
						.setName('o', "output-file")
						.setArgType(ArgumentBuilder.BuilderTypeNonFlag.STRING));
		suite.addMode("doc-export", DOC_EXPORT_MODE);
		PROFILER_SUMMARY_MODE = ArgumentParser.GetParser()
				.addDescription("Analyzes the output file for a profiler session, and generates a summary report of the results.")
				.addArgument(new ArgumentBuilder()
						.setDescription("This value dictates how much of the lower end data is ignored."
							+ " If the function took less time than this percentage of the total time, it is omitted from the"
							+ " results.")
						.setUsageName("ignore-percentage")
						.setOptional()
						.setName('i', "ignore-percentage")
						.setArgType(ArgumentBuilder.BuilderTypeNonFlag.NUMBER)
						.setDefaultVal("0"))
				.addArgument(new ArgumentBuilder()
						.setDescription("Path to the profiler file to use.")
						.setUsageName("input-file")
						.setRequiredAndDefault());
		suite.addMode("profiler-summary", PROFILER_SUMMARY_MODE);
		RSA_KEY_GEN_MODE = ArgumentParser.GetParser()
				.addDescription("Creates an ssh compatible rsa key pair. This is used with the Federation system, but"
						+ " is useful with other tools as well.")
				.addArgument(new ArgumentBuilder()
						.setDescription("Output file for the keys. For instance, \"/home/user/.ssh/id_rsa\"."
							+ " The public key will have the same name, with \".pub\" appended.")
						.setUsageName("file")
						.setRequired()
						.setName('o', "output-file")
						.setArgType(ArgumentBuilder.BuilderTypeNonFlag.STRING))
				.addArgument(new ArgumentBuilder()
						.setDescription("Label for the public key. For instance, \"user@localhost\" or an email"
								+ " address.")
						.setUsageName("label")
						.setRequired()
						.setName('l', "label")
						.setArgType(ArgumentBuilder.BuilderTypeNonFlag.STRING));
		suite.addMode("key-gen", RSA_KEY_GEN_MODE);
		PM_VIEWER_MODE = ArgumentParser.GetParser()
				.addDescription("Launches the Persistence Network viewer. This is a GUI tool that can help you"
						+ " visualize your databases.")
				.addArgument(new ArgumentBuilder()
						.setDescription("Sets up a server running on this machine, that can be accessed by remote"
								+ " Persistence Network Viewers. If this is set, you must also provide the --port and"
								+ " --password options.")
						.asFlag()
						.setName("server"))
				.addArgument(new ArgumentBuilder()
						.setDescription("The port for the server to listen on.")
						.setUsageName("port")
						.setOptional()
						.setName("port")
						.setArgType(ArgumentBuilder.BuilderTypeNonFlag.NUMBER))
				.addArgument(new ArgumentBuilder()
						.setDescription("The password that remote clients will need to provide to connect. Leave the"
								+ " field blank to be prompted for a password.")
						.setUsageName("password")
						.setOptional()
						.setName("password")
						.setArgType(ArgumentBuilder.BuilderTypeNonFlag.STRING));
		suite.addMode("pn-viewer", PM_VIEWER_MODE);
		CORE_FUNCTIONS_MODE = ArgumentParser.GetParser()
				.addDescription("Prints a list of functions tagged with the @core annotation, then exits.");
		suite.addMode("core-functions", CORE_FUNCTIONS_MODE);
		UI_MODE = ArgumentParser.GetParser()
				.addDescription("Launches a GUI that provides a list of all the sub GUI tools provided, and allows"
						+ " selection of a module. This command creates a subshell to run the launcher in, so that the"
						+ " original cmdline shell returns.")
				.addArgument(new ArgumentBuilder()
						.setDescription("Runs the launcher in the same shell process. By default, it creates a new"
								+ " process and causes the initial shell to return.")
						.asFlag().setName("in-shell"));
		suite.addMode("ui", UI_MODE);
		SITE_DEPLOY = ArgumentParser.GetParser()
				.addDescription("Deploys the documentation site, using the preferences specified in the configuration"
						+ " file. This mechanism completely re-writes"
						+ " the remote site, so that builds are totally reproduceable.")
				.addArgument(new ArgumentBuilder()
						.setDescription("The path to the config file for deployment")
						.setUsageName("config file")
						.setOptional()
						.setName('c', "config")
						.setArgType(ArgumentBuilder.BuilderTypeNonFlag.STRING)
						.setDefaultVal(MethodScriptFileLocations.getDefault().getSiteDeployFile().getAbsolutePath()))
				.addArgument(new ArgumentBuilder()
						.setDescription("Generates the preferences file initially, which you can then fill in.")
						.asFlag().setName("generate-prefs"))
				.addArgument(new ArgumentBuilder()
						.setDescription("Generally, when the uploader runs, it checks the remote server to see if"
							+ " the file already exists there (and is unchanged compared to the local file). If it is"
							+ " unchanged, the upload is skipped. However, even checking with the remote to see"
							+ " what the status of the remote file is takes time. If you are the only one uploading"
							+ " files, then we can simply use a local cache of what the remote system has, and we"
							+ " can skip the step of checking with the remote server for any given file. The cache is"
							+ " always populated, whether or not this flag is set, so if you aren't sure if you can"
							+ " trust the cache, run once without this flag, then for future runs, you can be sure that"
							+ " the local cache is up to date.")
						.asFlag().setName("use-local-cache"))
				.addArgument(new ArgumentBuilder()
						.setDescription("Clears the local cache of all entries, then exits.")
						.asFlag().setName("clear-local-cache"))
				.addArgument(new ArgumentBuilder()
						.setDescription("Validates all of the uploaded web pages, and prints out a summary of the"
								+ " results. This uses the value defined in the config file for validation.")
						.asFlag().setName('d', "do-validation"))
				.addArgument(new ArgumentBuilder()
						.setDescription("When set, does not clear the progress bar line. This is mostly useful"
							+ " when debugging the site-deploy tool itself.")
						.asFlag().setName("no-progress-clear"));
				/*.addFlag("install", "When installing a fresh server, it is useful to have the setup completely automated. If this flag"
						+ " is set, then the server is assumed to be a fresh ubuntu server, with nothing else on it. In that case,"
						+ " the server will be installed from scratch automatically. NOTE: This will not account for the fact that"
						+ " the documentation website is generally configured to allow for multiple versions of documentation. Old"
						+ " versions will not be accounted for or uploaded. This process, if desired, must be done manually. If this"
						+ " option is configured, the installation will occur before the upload or processing of files. During installation,"
						+ " a \"lock\" file will be created, and if that file is present, it is assumed that the installation"
						+ " has already occured on the instance, and will not be repeated. This is a safety measure to ensure"
						+ " that the instance will not attempt to be redeployed, making it safe to always add the install"
						+ " flag. If this flag is set, additional options need to be added to the config file. The remote server"
						+ " is assumed to be an already running AWS ubuntu instance, with security groups configured and a pem"
						+ " file available, but no login is necessary.")*/

		suite.addMode("site-deploy", SITE_DEPLOY);


		EXTENSION_BUILDER_MODE = ArgumentParser.GetParser()
				.addDescription("Given a path to the git source repo, pulls down the code, builds the extension with"
						+ " maven, and places the artifact in the extension folder. Git, Maven, and the JDK must all"
						+ " be pre-installed on your system for this to work, but once those are configued and working"
						+ " so you can run git and mvn from the cmdline, the rest of the build system should work.")
				.addArgument(new ArgumentBuilder()
					.setDescription("The path to the git repo (ending in .git usually)."
						+ " May be either http or ssh, this parameter is just passed through to git.")
					.setUsageName("git repo path")
					.setRequired()
					.setName('s', "source")
					.setArgType(ArgumentBuilder.BuilderTypeNonFlag.STRING))
				.addArgument(new ArgumentBuilder()
					.setDescription("The branch to check out. Defaults to \"master\".")
					.setUsageName("branch")
					.setOptional()
					.setName('b', "branch")
					.setArgType(ArgumentBuilder.BuilderTypeNonFlag.STRING)
					.setDefaultVal("master"))
				.addArgument(new ArgumentBuilder()
					.setDescription("The extension directory you want to install the built artifact to, by default, this"
						+ " installation's extension directory.")
					.setUsageName("dir")
					.setOptional()
					.setName('e', "extension-dir")
					.setArgType(ArgumentBuilder.BuilderTypeNonFlag.STRING)
					.setDefaultVal(MethodScriptFileLocations.getDefault().getExtensionsDirectory().getAbsolutePath()))
				.addArgument(new ArgumentBuilder()
					.setDescription("If the checkout folder already exists, it is first deleted, then cloned again.")
					.asFlag()
					.setName('f', "force"));
		suite.addMode("build-extension", EXTENSION_BUILDER_MODE);


		ARGUMENT_SUITE = suite;
	}

	@SuppressWarnings("ResultOfObjectAllocationIgnored")
	public static void main(String[] args) throws Exception {
		try {

			CHLog.initialize(MethodScriptFileLocations.getDefault().getJarDirectory());
			Prefs.init(MethodScriptFileLocations.getDefault().getPreferencesFile());

			Prefs.SetColors();
			if(Prefs.UseColors()) {
				//Use jansi to enable output to color properly, even on windows.
				org.fusesource.jansi.AnsiConsole.systemInstall();
			}

			ClassDiscovery cd = ClassDiscovery.getDefaultInstance();
			cd.addDiscoveryLocation(ClassDiscovery.GetClassContainer(Main.class));
			ClassDiscoveryCache cdcCache
					= new ClassDiscoveryCache(MethodScriptFileLocations.getDefault().getCacheDirectory());
			cd.setClassDiscoveryCache(cdcCache);
			cd.addAllJarsInFolder(MethodScriptFileLocations.getDefault().getExtensionsDirectory());

			ExtensionManager.AddDiscoveryLocation(MethodScriptFileLocations.getDefault().getExtensionsDirectory());
			ExtensionManager.Cache(MethodScriptFileLocations.getDefault().getExtensionCacheDirectory());
			ExtensionManager.Initialize(cd);
			ExtensionManager.Startup();

//			Implementation.setServerType(Implementation.Type.SHELL);
//			MethodScriptFileLocations.setDefault(new MethodScriptFileLocations());

			if(args.length == 0) {
				args = new String[]{"--help"};
			}

			ArgumentParser mode;
			ArgumentParser.ArgumentParserResults parsedArgs;

			Map<ArgumentParser, CommandLineTool> dynamicTools = new HashMap<>();
			for(Class<? extends CommandLineTool> ctool : ClassDiscovery.getDefaultInstance()
					.loadClassesWithAnnotationThatExtend(tool.class, CommandLineTool.class)) {
				CommandLineTool tool = ctool.newInstance();
				ArgumentParser ap = tool.getArgumentParser();
				String toolName = ctool.getAnnotation(tool.class).value();
				ARGUMENT_SUITE.addMode(toolName, ap);
				String[] aliases = ctool.getAnnotation(tool.class).aliases();
				if(aliases != null) {
					for(String alias : aliases) {
						ARGUMENT_SUITE.addModeAlias(alias, toolName);
					}
				}
				dynamicTools.put(ap, tool);
			}

			try {
				ArgumentSuite.ArgumentSuiteResults results = ARGUMENT_SUITE.match(args, "help");
				mode = results.getMode();
				parsedArgs = results.getResults();
			} catch (ArgumentParser.ResultUseException | ArgumentParser.ValidationException e) {
				StreamUtils.GetSystemOut().println(TermColors.RED + e.getMessage() + TermColors.RESET);
				mode = HELP_MODE;
				parsedArgs = null;
			}

			if(mode == HELP_MODE) {
				String modeForHelp = null;
				if(parsedArgs != null) {
					modeForHelp = parsedArgs.getStringArgument();
				}
				modeForHelp = ARGUMENT_SUITE.getModeFromAlias(modeForHelp);
				if(modeForHelp == null) {
					//Display the general help
					StreamUtils.GetSystemOut().println(ARGUMENT_SUITE.getBuiltDescription());
					System.exit(0);
					return;
				} else {
					//Display the help for this mode
					StreamUtils.GetSystemOut().println(ARGUMENT_SUITE.getModeFromName(modeForHelp).getBuiltDescription());
					return;
				}
			}

			//Gets rid of warnings below. We now know parsedArgs will never be null,
			//if it were, the help command would have run.
			assert parsedArgs != null;

			if(mode == CORE_FUNCTIONS_MODE) {
				List<String> core = new ArrayList<>();
				for(api.Platforms platform : api.Platforms.values()) {
					for(FunctionBase f : FunctionList.getFunctionList(platform)) {
						if(f.isCore()) {
							core.add(f.getName());
						}
					}
				}
				Collections.sort(core);
				StreamUtils.GetSystemOut().println(StringUtils.Join(core, ", "));
				System.exit(0);
			} else if(mode == INSTALL_CMDLINE_MODE) {
				Interpreter.install();
				System.exit(0);
			} else if(mode == UNINSTALL_CMDLINE_MODE) {
				Interpreter.uninstall();
				System.exit(0);
			} else if(mode == COPYRIGHT_MODE) {
				StreamUtils.GetSystemOut().println("The MIT License (MIT)\n"
						+ "\n"
						+ "Copyright (c) 2012-2017 Methodscript Contributors\n"
						+ "\n"
						+ "Permission is hereby granted, free of charge, to any person obtaining a copy of \n"
						+ "this software and associated documentation files (the \"Software\"), to deal in \n"
						+ "the Software without restriction, including without limitation the rights to \n"
						+ "use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of \n"
						+ "the Software, and to permit persons to whom the Software is furnished to do so, \n"
						+ "subject to the following conditions:\n"
						+ "\n"
						+ "The above copyright notice and this permission notice shall be included in all \n"
						+ "copies or substantial portions of the Software.\n"
						+ "\n"
						+ "THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR \n"
						+ "IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS \n"
						+ "FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR \n"
						+ "COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER \n"
						+ "IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN \n"
						+ "CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.");
				System.exit(0);
			} else if(mode == PRINT_DB_MODE) {
				ConnectionMixinFactory.ConnectionMixinOptions options = new ConnectionMixinFactory.ConnectionMixinOptions();
				options.setWorkingDirectory(MethodScriptFileLocations.getDefault().getConfigDirectory());
				PersistenceNetwork pn = new PersistenceNetwork(MethodScriptFileLocations.getDefault().getPersistenceConfig(),
						new URI("sqlite://" + MethodScriptFileLocations.getDefault().getDefaultPersistenceDBFile().getCanonicalPath()
								//This replace is required on Windows.
								.replace('\\', '/')), options);
				Map<String[], String> values = pn.getNamespace(new String[]{});
				for(String[] s : values.keySet()) {
					StreamUtils.GetSystemOut().println(StringUtils.Join(s, ".") + "=" + values.get(s));
				}
				System.exit(0);
			} else if(mode == DOCS_MODE) {
				DocGen.MarkupType docs;
				try {
					docs = DocGen.MarkupType.valueOf(parsedArgs.getStringArgument().toUpperCase());
				} catch (IllegalArgumentException e) {
					StreamUtils.GetSystemOut().println("The type of documentation must be one of the following: " + StringUtils.Join(DocGen.MarkupType.values(), ", ", ", or "));
					System.exit(1);
					return;
				}
				//Documentation generator
				StreamUtils.GetSystemErr().print("Creating " + docs + " documentation...");
				StreamUtils.GetSystemOut().println(DocGen.functions(docs, api.Platforms.INTERPRETER_JAVA, true));
				StreamUtils.GetSystemErr().println("Done.");
				System.exit(0);
			} else if(mode == EXAMPLES_MODE) {
				ExampleLocalPackageInstaller.run(MethodScriptFileLocations.getDefault().getJarDirectory(),
						parsedArgs.getStringArgument());
			} else if(mode == VERIFY_MODE) {
				String file = parsedArgs.getStringArgument();
				if("".equals(file)) {
					StreamUtils.GetSystemErr().println("File parameter is required.");
					System.exit(1);
				}
				File f = new File(file);
				String script = FileUtil.read(f);
				try {
					try {
						MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, f, file.endsWith("ms")), null);
					} catch (ConfigCompileException ex) {
						Set<ConfigCompileException> s = new HashSet<>(1);
						s.add(ex);
						throw new ConfigCompileGroupException(s);
					}
				} catch (ConfigCompileGroupException ex) {
					List<Map<String, Object>> err = new ArrayList<>();
					for(ConfigCompileException e : ex.getList()) {
						Map<String, Object> error = new HashMap<>();
						error.put("msg", e.getMessage());
						error.put("file", e.getFile().getAbsolutePath());
						error.put("line", e.getLineNum());
						error.put("col", e.getColumn());
						// TODO: Need to track target length for this
						error.put("len", 0);
						err.add(error);
					}
					String serr = JSONValue.toJSONString(err);
					StreamUtils.GetSystemOut().println(serr);
				}
			} else if(mode == API_MODE) {
				String function = parsedArgs.getStringArgument();
				boolean examples = parsedArgs.isFlagSet('e');
				if("".equals(function)) {
					StreamUtils.GetSystemErr().println("Usage: java -jar CommandHelper.jar api <function name>");
					System.exit(1);
				}
				List<FunctionBase> fl = new ArrayList<>();
				for(FunctionBase fb : FunctionList.getFunctionList(api.Platforms.INTERPRETER_JAVA)) {
					if(fb.getName().matches("^" + function + "$")) {
						fl.add(fb);
					}
				}
				if(fl.isEmpty()) {
					StreamUtils.GetSystemErr().println("The function '" + function + "' was not found.");
					System.exit(1);
				} else if(fl.size() == 1) {
					FunctionBase f = fl.get(0);
					if(parsedArgs.isFlagSet("online")) {
						String url = String.format("https://methodscript.com/docs/%s/API/functions/%s",
								MSVersion.LATEST.toString(), f.getName());
						System.out.println("Launching browser to " + url);
						if(!UIUtils.openWebpage(new URL(url))) {
							System.err.println("Could not launch browser");
						}
					} else {
						StreamUtils.GetSystemOut().println(Interpreter.formatDocsForCmdline(f.getName(), examples));
					}
				} else {
					StreamUtils.GetSystemOut().println("Multiple function matches found:");
					for(FunctionBase fb : fl) {
						StreamUtils.GetSystemOut().println(fb.getName());
					}
				}
				System.exit(0);
			} else if(mode == SYNTAX_MODE) {
				// TODO: Maybe load extensions here?
				List<String> syntax = parsedArgs.getStringListArgument();
				String type = (syntax.size() >= 1 ? syntax.get(0) : null);
				String theme = (syntax.size() >= 2 ? syntax.get(1) : null);
				StreamUtils.GetSystemOut().println(SyntaxHighlighters.generate(type, theme));
				System.exit(0);
			} else if(mode == OPTIMIZER_TEST_MODE) {
				String path = parsedArgs.getStringArgument();
				File source = new File(path);
				String plain = FileUtil.read(source);
				Security.setSecurityEnabled(false);
				String optimized;
				try {
					try {
						optimized = OptimizationUtilities.optimize(plain, source);
					} catch (ConfigCompileException ex) {
						Set<ConfigCompileException> group = new HashSet<>();
						group.add(ex);
						throw new ConfigCompileGroupException(group);
					}
				} catch (ConfigCompileGroupException ex) {
					ConfigRuntimeException.HandleUncaughtException(ex, null);
					System.exit(1);
					return;
				}
				StreamUtils.GetSystemOut().println(optimized);
				System.exit(0);
			} else if(mode == CMDLINE_MODE) {
				//We actually can't use the parsedArgs, because there may be cmdline switches in
				//the arguments that we want to ignore here, but otherwise pass through. parsedArgs
				//will prevent us from seeing those, however.
				List<String> allArgs = new ArrayList<>(Arrays.asList(args));
				//The 0th arg is the cmdline verb though, so remove that.
				allArgs.remove(0);
				if(allArgs.isEmpty()) {
					StreamUtils.GetSystemErr().println("Usage: path/to/file.ms [arg1 arg2]");
					System.exit(1);
				}
				String fileName = allArgs.get(0);
				allArgs.remove(0);
				try {
					Interpreter.startWithTTY(fileName, allArgs);
				} catch (Profiles.InvalidProfileException ex) {
					StreamUtils.GetSystemErr().println("Invalid profile file at " + MethodScriptFileLocations.getDefault().getProfilesFile()
							+ ": " + ex.getMessage());
					System.exit(1);
				}
				StaticLayer.GetConvertor().runShutdownHooks();
				System.exit(0);
			} else if(mode == EXTENSION_DOCS_MODE) {
				String inputJarS = parsedArgs.getStringArgument("input-jar");
				String outputFileS = parsedArgs.getStringArgument("output-file");
				if(inputJarS == null) {
					StreamUtils.GetSystemOut().println("Usage: --input-jar extension-docs path/to/extension.jar [--output-file path/to/output.md]\n\tIf the output is blank, it is printed to stdout.");
					System.exit(1);
				}
				File inputJar = new File(inputJarS);
				OutputStream outputFile = StreamUtils.GetSystemOut();
				if(outputFileS != null) {
					outputFile = new FileOutputStream(new File(outputFileS));
				}
				ExtensionDocGen.generate(inputJar, outputFile);
			} else if(mode == DOC_EXPORT_MODE) {
				String extensionDirS = parsedArgs.getStringArgument("extension-dir");
				String outputFileS = parsedArgs.getStringArgument("output-file");
				OutputStream outputFile = StreamUtils.GetSystemOut();
				if(outputFileS != null) {
					outputFile = new FileOutputStream(new File(outputFileS));
				}
				Implementation.forceServerType(Implementation.Type.BUKKIT);
				File extensionDir = new File(extensionDirS);
				if(extensionDir.exists()) {
					//Might not exist, but that's ok, however we will print a warning
					//to stderr.
					for(File f : extensionDir.listFiles()) {
						if(f.getName().endsWith(".jar")) {
							cd.addDiscoveryLocation(f.toURI().toURL());
						}
					}
				} else {
					StreamUtils.GetSystemErr().println("Extension directory specificed doesn't exist: "
							+ extensionDirS + ". Continuing anyways.");
				}
				new DocGenExportTool(cd, outputFile).export();
			} else if(mode == PROFILER_SUMMARY_MODE) {
				String input = parsedArgs.getStringArgument();
				if("".equals(input)) {
					StreamUtils.GetSystemErr().println(TermColors.RED + "No input file specified! Run `help profiler-summary' for usage." + TermColors.RESET);
					System.exit(1);
				}
				double ignorePercentage = parsedArgs.getNumberArgument("ignore-percentage");
				ProfilerSummary summary = new ProfilerSummary(new FileInputStream(input));
				try {
					summary.setIgnorePercentage(ignorePercentage);
				} catch (IllegalArgumentException ex) {
					StreamUtils.GetSystemErr().println(TermColors.RED + ex.getMessage() + TermColors.RESET);
					System.exit(1);
				}
				StreamUtils.GetSystemOut().println(summary.getAnalysis());
				System.exit(0);
			} else if(mode == RSA_KEY_GEN_MODE) {
				String outputFileString = parsedArgs.getStringArgument('o');
				File privOutputFile = new File(outputFileString);
				File pubOutputFile = new File(outputFileString + ".pub");
				String label = parsedArgs.getStringArgument('l');
				if(privOutputFile.exists() || pubOutputFile.exists()) {
					StreamUtils.GetSystemErr().println("Either the public key or private key file already exists. This utility will not overwrite any existing files.");
					System.exit(1);
				}
				RSAEncrypt enc = RSAEncrypt.generateKey(label);
				FileUtil.write(enc.getPrivateKey(), privOutputFile);
				FileUtil.write(enc.getPublicKey(), pubOutputFile);
				System.exit(0);
			} else if(mode == PM_VIEWER_MODE) {
				if(parsedArgs.isFlagSet("server")) {
					if(parsedArgs.getNumberArgument("port") == null) {
						StreamUtils.GetSystemErr().println("When running as a server, port is required.");
						System.exit(1);
					}
					int port = parsedArgs.getNumberArgument("port").intValue();
					if(port > 65535 || port < 1) {
						StreamUtils.GetSystemErr().println("Port must be between 1 and 65535.");
						System.exit(1);
					}
					String password = parsedArgs.getStringArgument("password");
					if("".equals(password)) {
						ConsoleReader reader = null;
						try {
							reader = new ConsoleReader();
							reader.setExpandEvents(false);
							Character cha = new Character((char) 0);
							password = reader.readLine("Enter password: ", cha);
						} finally {
							if(reader != null) {
								reader.close();
							}
						}
					}
					if(password == null) {
						StreamUtils.GetSystemErr().println("Warning! Running server with no password, anyone will be able to connect!");
						password = "";
					}
					try {
						PNViewer.startServer(port, password);
					} catch (IOException ex) {
						StreamUtils.GetSystemErr().println(ex.getMessage());
						System.exit(1);
					}
				} else {
					try {
						PNViewer.main(parsedArgs.getStringListArgument().toArray(ArrayUtils.EMPTY_STRING_ARRAY));
					} catch (HeadlessException ex) {
						StreamUtils.GetSystemErr().println("The Persistence Network Viewer may not be run from a headless environment.");
						System.exit(1);
					}
				}
			} else if(mode == UI_MODE) {
				if(parsedArgs.isFlagSet("in-shell")) {
					// Actually launch the GUI
					UILauncher.main(args);
				} else {
					// Relaunch the jar in a new process with the --run flag set,
					// so that the process will be in its own subshell
					List<String> largs = new ArrayList<>();
					largs.add("java");
					largs.add("-jar");
					String jarPath = ClassDiscovery.GetClassContainer(Main.class).getPath();
					if(OSUtils.GetOS().isWindows() && jarPath.startsWith("/")) {
						jarPath = jarPath.substring(1);
					}
					largs.add(jarPath);
					largs.addAll(Arrays.asList(args));
					largs.add("--in-shell");
					CommandExecutor ce = new CommandExecutor(largs.toArray(new String[largs.size()]));
					ce.start();
					System.exit(0);
				}
			} else if(mode == SITE_DEPLOY) {
				boolean clearLocalCache = parsedArgs.isFlagSet("clear-local-cache");
				if(clearLocalCache) {
					PersistenceNetwork p = SiteDeploy.getPersistenceNetwork();
					if(p == null) {
						System.out.println("Cannot get reference to persistence network");
						System.exit(1);
						return;
					}
					DaemonManager dm = new DaemonManager();
					p.clearKey(dm, new String[]{"site_deploy", "local_cache"});
					dm.waitForThreads();
					System.out.println("Local cache cleared");
					System.exit(0);
				}
				boolean generatePrefs = parsedArgs.isFlagSet("generate-prefs");
				boolean useLocalCache = parsedArgs.isFlagSet("use-local-cache");
				boolean doValidation = parsedArgs.isFlagSet("do-validation");
				boolean noProgressClear = parsedArgs.isFlagSet("no-progress-clear");
				String configString = parsedArgs.getStringArgument("config");
				if("".equals(configString)) {
					System.err.println("Config file missing, check command and try again");
					System.exit(1);
				}
				File config = new File(configString);
				SiteDeploy.run(generatePrefs, useLocalCache, config, "", doValidation, !noProgressClear);
			} else if(mode == EXTENSION_BUILDER_MODE) {

				try {
					new CommandExecutor("git --version").start().waitFor();
					new CommandExecutor("mvn --version").start().waitFor();
				} catch (IOException e)  {
					System.err.println("Git and Maven are required (and Maven requires the JDK). These three"
							+ " components must be already installed to use this tool.");
					System.exit(1);
				}

				String branch = parsedArgs.getStringArgument("branch");
				String source = parsedArgs.getStringArgument("source");
				boolean force = parsedArgs.isFlagSet("force");
				File extensionDir = new File(parsedArgs.getStringArgument("extension-dir"));

				File checkoutPath;
				checkoutPath = new File(MethodScriptFileLocations.getDefault().getTempDir(),
						source.replaceAll("^.*/(.*?)(?:.git)*?$", "$1"));

				System.out.println("Cloning " + source);
				System.out.println("Using branch " + branch);
				System.out.println("Checkout path is " + checkoutPath);
				System.out.println("Deploying to " + extensionDir);
				System.out.println("------------------------------------------------");

				if(!extensionDir.exists()) {
					if(force) {
						extensionDir.mkdirs();
					} else {
						System.err.println("Extension directory does not exist, refusing to continue."
								+ " If " + extensionDir.getAbsolutePath() + " is the correct"
								+ " directory, manually create it and try again, or use --force.");
						System.exit(1);
					}
				}
				try {
					if(checkoutPath.exists()) {
						if(!force) {
							System.err.println("Checkout path already exists (" + checkoutPath.getAbsolutePath()
									+ "), refusing to continue.");
							System.exit(1);
						} else {
							System.out.println("Deleting " + checkoutPath + " directory...");
							if(!FileUtil.recursiveDelete(checkoutPath)) {
								System.err.println("Could not fully delete checkout path, refusing to continue. Please"
										+ " manually delete " + checkoutPath + ", and try again.");
								System.exit(1);
							}
						}
					}

					new CommandExecutor(new String[]{"git", "clone",
						"--single-branch", "--branch", branch,
						"--depth=1", source, checkoutPath.getAbsolutePath()})
							.setSystemInputsAndOutputs()
							.start().waitFor();
					System.out.println("Building extension...");
					int mvnBuild = new CommandExecutor(new String[]{"mvn", "package", "-DskipTests"})
							.setSystemInputsAndOutputs()
							.setWorkingDir(checkoutPath)
							.start().waitFor();
					if(mvnBuild != 0) {
						System.err.println("Something went wrong in the maven build, unable to continue. Please correct"
								+ " the listed error, and then try again.");
						System.err.flush();
						System.exit(1);
					}
					System.out.println("Extension built, moving artifact to extension directory...");
					// Read the POM for information about what the jar is named
					XMLDocument pom = new XMLDocument(new FileInputStream(new File(checkoutPath, "pom.xml")));
					String artifactId = pom.getNode("/project/artifactId");
					String version = pom.getNode("/project/version");
					String artifactName = artifactId + "-" + version + ".jar";
					System.out.println("Identified " + artifactName + " as the artifact to use");
					FileUtil.copy(new File(checkoutPath, "target/" + artifactName),
							new File(extensionDir, artifactName), null);
					System.out.println("Build complete, cleaning up...");
					if(!FileUtil.recursiveDelete(checkoutPath)) {
						System.err.println("Could not delete " + checkoutPath + ", but build completed successfully.");
						System.exit(1);
					}
					System.exit(0);
				} catch (Exception e) {
					e.printStackTrace(System.err);
				}
			} else if(dynamicTools.containsKey(mode)) {
				dynamicTools.get(mode).execute(parsedArgs);
				System.exit(0);
			} else {
				throw new Error("Should not have gotten here");
			}
		} catch (NoClassDefFoundError error) {
			StreamUtils.GetSystemErr().println(Static.getNoClassDefFoundErrorMessage(error));
		}
	}

	@tool(value = "version", aliases = {"-v", "--v", "-version", "--version"})
	public static class VersionMode extends AbstractCommandLineTool {

		@Override
		public ArgumentParser getArgumentParser() {
			return ArgumentParser.GetParser()
				.addDescription("Prints the version of CommandHelper, and exits.");
		}

		@Override
		public void execute(ArgumentParser.ArgumentParserResults parsedArgs) throws Exception {
			// TODO: This should eventually be changed to use an independent
			// versioning scheme for CH and MS.
			StreamUtils.GetSystemOut().println("You are running "
					+ Implementation.GetServerType().getBranding() + " version " + Static.loadSelfVersion());
			for(ExtensionTracker e : ExtensionManager.getTrackers().values()) {
				StreamUtils.GetSystemOut().println(e.getIdentifier() + ": " + e.getVersion());
			}
		}

	}

	@tool("manager")
	public static class ManagerMode extends AbstractCommandLineTool {

		@Override
		public ArgumentParser getArgumentParser() {
			return ArgumentParser.GetParser()
				.addDescription("Launches the built in interactive data manager, which will allow command line access"
						+ " to the full persistence database.");
		}

		@Override
		public void execute(ArgumentParser.ArgumentParserResults parsedArgs) throws Exception {
			Manager.start();
		}

	}

	@tool("mslp")
	public static class MSLPMode extends AbstractCommandLineTool {

		@Override
		public ArgumentParser getArgumentParser() {
			return ArgumentParser.GetParser()
				.addDescription("Creates an MSLP file based on the directory specified.")
				.addArgument(new ArgumentBuilder().setDescription("The path to the folder")
						.setUsageName("path/to/folder")
						.setRequiredAndDefault());
		}

		@Override
		public void execute(ArgumentParser.ArgumentParserResults parsedArgs) throws Exception {
			String mslp = parsedArgs.getStringArgument();
			if(mslp.isEmpty()) {
				StreamUtils.GetSystemOut().println("Usage: --mslp path/to/folder");
				System.exit(1);
			}
			MSLPMaker.start(mslp);
		}

	}

	@tool("interpreter")
	public static class InterpreterMode extends AbstractCommandLineTool {

		@Override
		public ArgumentParser getArgumentParser() {
			return ArgumentParser.GetParser()
				.addDescription("Launches the minimal cmdline interpreter.")
				.addArgument(new ArgumentBuilder().setDescription("Sets the initial working directory of the"
						+ " interpreter. This is optional, but"
						+ " is automatically set by the mscript program. The option name is strange, to avoid any"
						+ " conflicts with"
						+ " script arguments.")
						.setUsageName("location")
						.setOptional()
						.setName("location-----")
						.setArgType(ArgumentBuilder.BuilderTypeNonFlag.STRING)
						.setDefaultVal("."));
		}

		@Override
		@SuppressWarnings("ResultOfObjectAllocationIgnored")
		public void execute(ArgumentParser.ArgumentParserResults parsedArgs) throws Exception {
			new Interpreter(parsedArgs.getStringListArgument(), parsedArgs.getStringArgument("location-----"));
		}

	}

	@tool("new")
	public static class NewMode extends AbstractCommandLineTool {

		@Override
		public ArgumentParser getArgumentParser() {
			return ArgumentParser.GetParser()
				.addDescription("Creates a blank script in the specified location with the appropriate permissions,"
						+ " having the correct hashbang, and ready to be executed. If"
						+ " the specified file already exists, it will refuse to create it, unless --force is set.")
				.addArgument(new ArgumentBuilder()
						.setDescription("Location and name to create the script as. Multiple arguments can be provided,"
								+ " and they will create multiple files.")
						.setUsageName("file")
						.setRequiredAndDefault())
				.addArgument(new ArgumentBuilder()
						.setDescription("Forces the file to be overwritten, even if it already exists.")
						.asFlag().setName('f', "force"));
		}

		@Override
		public void execute(ArgumentParser.ArgumentParserResults parsedArgs) throws IOException {
			String li = OSUtils.GetLineEnding();
			for(String file : parsedArgs.getStringListArgument()) {
				File f = new File(file);
				if(f.exists() && !parsedArgs.isFlagSet('f')) {
					System.out.println(file + " already exists, refusing to create");
					continue;
				}
				f.createNewFile();
				f.setExecutable(true);
				FileUtil.write("#!/usr/bin/env /usr/local/bin/mscript"
						+ li
						+ "<!" + li
						+ "\tstrict;" + li
						+ "\tname: " + f.getName() + ";" + li
						+ "\tauthor: " + StaticLayer.GetConvertor().GetUser(null) + ";" + li
						+ "\tcreated: " + new Scheduling.simple_date().exec(Target.UNKNOWN, null, new CString("yyyy-MM-dd", Target.UNKNOWN)).val() + ";" + li
						+ "\tdescription: " + ";" + li
						+ ">" + li + li, f, true);
			}
		}

	}

	@tool("json-api")
	public static class JsonTool extends AbstractCommandLineTool {

		@Override
		public ArgumentParser getArgumentParser() {
			return ArgumentParser.GetParser()
					.addDescription("Prints the api.json file to stdout. This takes no parameters.");
		}

		@Override
		public void execute(ArgumentParser.ArgumentParserResults parsedArgs) throws Exception {
			APIBuilder.main(null);
			System.exit(0);
		}

	}

}
