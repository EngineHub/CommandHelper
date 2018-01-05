package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.ArgumentParser;
import com.laytonsmith.PureUtilities.ArgumentSuite;
import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscoveryCache;
import com.laytonsmith.PureUtilities.CommandExecutor;
import com.laytonsmith.PureUtilities.Common.ArrayUtils;
import com.laytonsmith.PureUtilities.Common.FileUtil;
import com.laytonsmith.PureUtilities.Common.Misc;
import com.laytonsmith.PureUtilities.Common.OSUtils;
import com.laytonsmith.PureUtilities.Common.RSAEncrypt;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.PureUtilities.TermColors;
import com.laytonsmith.PureUtilities.ZipReader;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.compiler.OptimizationUtilities;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.extensions.ExtensionManager;
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
import com.laytonsmith.tools.docgen.DocGenUI;
import com.laytonsmith.tools.docgen.ExtensionDocGen;
import com.laytonsmith.tools.docgen.sitedeploy.SiteDeploy;
import com.laytonsmith.tools.pnviewer.PNViewer;
import java.awt.HeadlessException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
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
import org.yaml.snakeyaml.Yaml;

/**
 *
 *
 */
public class Main {

    public static final ArgumentSuite ARGUMENT_SUITE;
    private static final ArgumentParser helpMode;
    private static final ArgumentParser managerMode;
    private static final ArgumentParser interpreterMode;
    private static final ArgumentParser mslpMode;
    private static final ArgumentParser versionMode;
    private static final ArgumentParser copyrightMode;
    private static final ArgumentParser printDBMode;
    private static final ArgumentParser docsMode;
    private static final ArgumentParser verifyMode;
    private static final ArgumentParser installCmdlineMode;
    private static final ArgumentParser uninstallCmdlineMode;
    private static final ArgumentParser syntaxMode;
    private static final ArgumentParser docgenMode;
    private static final ArgumentParser apiMode;
    private static final ArgumentParser examplesMode;
    private static final ArgumentParser optimizerTestMode;
    private static final ArgumentParser cmdlineMode;
    private static final ArgumentParser extensionDocsMode;
    private static final ArgumentParser docExportMode;
    private static final ArgumentParser profilerSummaryMode;
    private static final ArgumentParser rsaKeyGenMode;
    private static final ArgumentParser pnViewerMode;
    private static final ArgumentParser coreFunctionsMode;
    private static final ArgumentParser uiMode;
    private static final ArgumentParser newMode;
    private static final ArgumentParser siteDeploy;

    static {
	MethodScriptFileLocations.setDefault(new MethodScriptFileLocations());
	ArgumentSuite suite = new ArgumentSuite()
		.addDescription("These are the command line tools for CommandHelper. For more information about a"
			+ " particular mode, run help <mode name>. To run a command, in general, use the command:\n\n"
			+ "\tjava -jar " + MethodScriptFileLocations.getDefault().getJarFile().getName() + " <mode name> <[mode specific arguments]>\n");
	helpMode = ArgumentParser.GetParser()
		.addDescription("Displays help for all modes, or the given mode if one is provided.")
		.addArgument("Displays help for the given mode.", "mode name", false);
	suite.addMode("help", helpMode).addModeAlias("--help", "help").addModeAlias("-help", "help")
		.addModeAlias("/?", "help");
	managerMode = ArgumentParser.GetParser()
		.addDescription("Launches the built in interactive data manager, which will allow command line access to the full persistence database.");
	suite.addMode("manager", managerMode);
	interpreterMode = ArgumentParser.GetParser()
		.addDescription("Launches the minimal cmdline interpreter.")
		.addArgument("location-----", ArgumentParser.Type.STRING, ".", "Sets the initial working directory of the interpreter. This is optional, but"
			+ " is automatically set by the mscript program. The option name is strange, to avoid any conflicts with"
			+ " script arguments.", "location-----", false);
	suite.addMode("interpreter", interpreterMode);
	mslpMode = ArgumentParser.GetParser()
		.addDescription("Creates an MSLP file based on the directory specified.")
		.addArgument("The path to the folder", "path/to/folder", true);
	suite.addMode("mslp", mslpMode);
	versionMode = ArgumentParser.GetParser()
		.addDescription("Prints the version of CommandHelper, and exits.");
	suite.addMode("version", versionMode).addModeAlias("--version", "version").addModeAlias("-version", "version")
		.addModeAlias("-v", "version");
	copyrightMode = ArgumentParser.GetParser()
		.addDescription("Prints the copyright and exits.");
	suite.addMode("copyright", copyrightMode);
	printDBMode = ArgumentParser.GetParser()
		.addDescription("Prints out the built in database in a human readable form, then exits.");
	suite.addMode("print-db", printDBMode);
	docsMode = ArgumentParser.GetParser()
		.addDescription("Prints documentation for the functions that CommandHelper knows about, then exits.")
		.addArgument("html", "The type of the documentation, defaulting to html. It may be one of the following: " + StringUtils.Join(DocGen.MarkupType.values(), ", ", ", or "), "type", false);
	suite.addMode("docs", docsMode);
	verifyMode = ArgumentParser.GetParser()
		.addDescription("Compiles the given file, returning a json describing the errors in the file, or returning"
			+ " nothing if the file compiles cleanly.")
		.addArgument("The file to check", "<file>", true);
	suite.addMode("verify", verifyMode);
	installCmdlineMode = ArgumentParser.GetParser()
		.addDescription("Installs MethodScript to your system, so that commandline scripts work. (Currently only unix is supported.)");
	suite.addMode("install-cmdline", installCmdlineMode);
	uninstallCmdlineMode = ArgumentParser.GetParser()
		.addDescription("Uninstalls the MethodScript interpreter from your system.");
	suite.addMode("uninstall-cmdline", uninstallCmdlineMode);
	syntaxMode = ArgumentParser.GetParser()
		.addDescription("Generates the syntax highlighter for the specified editor (if available).")
		.addArgument("The type of the syntax file to generate. Don't specify a type to see the available options.", "[type]", false);
	suite.addMode("syntax", syntaxMode);
	docgenMode = ArgumentParser.GetParser()
		.addDescription("Starts the automatic wiki uploader GUI.");
	suite.addMode("docgen", docgenMode);
	apiMode = ArgumentParser.GetParser()
		.addDescription("Prints documentation for the function specified, then exits.")
		.addArgument("The name of the function to print the information for", "function", true);
	suite.addMode("api", apiMode);
	examplesMode = ArgumentParser.GetParser()
		.addDescription("Installs one of the built in LocalPackage examples, which may in and of itself be useful.")
		.addArgument("The name of the package to install. Leave blank to see a list of examples to choose from.", "[packageName]", true);
	suite.addMode("examples", examplesMode);
	optimizerTestMode = ArgumentParser.GetParser()
		.addDescription("Given a source file, reads it in and outputs the \"optimized\" version. This is meant as a debug"
			+ " tool, but could be used as an obfuscation tool as well.")
		.addArgument("File path", "file", true);
	suite.addMode("optimizer-test", optimizerTestMode);
	cmdlineMode = ArgumentParser.GetParser()
		.addDescription("Given a source file, runs it in cmdline mode. This is similar to"
			+ " the interpreter mode, but allows for tty input (which is required for some functions,"
			+ " like the prompt_* functions) and provides better information for errors, as the"
			+ " file is known.")
		.addArgument("File path/arguments", "fileAndArgs", true);
	suite.addMode("cmdline", cmdlineMode);
	extensionDocsMode = ArgumentParser.GetParser()
		.addDescription("Generates markdown documentation for the specified extension utilizing its code, to be used most likely on the extensions github page.")
		.addArgument('i', "input-jar", ArgumentParser.Type.STRING, "The extension jar to generate doucmenation for.", "input-jar", true)
		.addArgument('o', "output-file", ArgumentParser.Type.STRING, "The file to output the generated documentation to.", "output-file", false);
	suite.addMode("extension-docs", extensionDocsMode);
	docExportMode = ArgumentParser.GetParser()
		.addDescription("Outputs all known function documentation as a json. This includes known extensions"
			+ " as well as the built in functions.")
		.addArgument("extension-dir", ArgumentParser.Type.STRING, "./CommandHelper/extensions", "Provides the path to your extension directory, if not the default, \"./CommandHelper/extensions\"", "extension-dir", false)
		.addArgument('o', "output-file", ArgumentParser.Type.STRING, "The file to output the generated json to. If this parameter is missing, it is simply printed to screen.", "output-file", false);
	suite.addMode("doc-export", docExportMode);
	profilerSummaryMode = ArgumentParser.GetParser()
		.addDescription("Analyzes the output file for a profiler session, and generates a summary report of the results.")
		.addArgument('i', "ignore-percentage", ArgumentParser.Type.NUMBER, "0", "This value dictates how much of the lower end data is ignored."
			+ " If the function took less time than this percentage of the total time, it is omitted from the"
			+ " results.", "ignore-percentage", false)
		.addArgument("Path to the profiler file to use", "input-file", true);
	suite.addMode("profiler-summary", profilerSummaryMode);
	rsaKeyGenMode = ArgumentParser.GetParser()
		.addDescription("Creates an ssh compatible rsa key pair. This is used with the Federation system, but is useful with other tools as well.")
		.addArgument('o', "output-file", ArgumentParser.Type.STRING, "Output file for the keys. For instance, \"/home/user/.ssh/id_rsa\"."
			+ " The public key will have the same name, with \".pub\" appended.", "output-file", true)
		.addArgument('l', "label", ArgumentParser.Type.STRING, "Label for the public key. For instance, \"user@localhost\"", "label", true);
	suite.addMode("key-gen", rsaKeyGenMode);
	pnViewerMode = ArgumentParser.GetParser()
		.addDescription("Launches the Persistence Network viewer. This is a GUI tool that can help you visualize your databases.")
		.addFlag("server", "Sets up a server running on this machine, that can be accessed by remote Persistence Network Viewers."
			+ " If this is set, you must also provide the --port and --password options.")
		.addArgument("port", ArgumentParser.Type.NUMBER, "The port for the server to listen on.", "port", false)
		.addArgument("password", ArgumentParser.Type.STRING, "The password that remote clients will need to provide to connect. Leave the field blank to be prompted for a password.", "password", false);
	suite.addMode("pn-viewer", pnViewerMode);
	coreFunctionsMode = ArgumentParser.GetParser()
		.addDescription("Prints a list of functions tagged with the @core annotation, then exits.");
	suite.addMode("core-functions", coreFunctionsMode);
	uiMode = ArgumentParser.GetParser()
		.addDescription("Launches a GUI that provides a list of all the sub GUI tools provided, and allows selection of a module. This"
			+ " command creates a subshell to run the launcher in, so that the original cmdline shell returns.")
		.addFlag("in-shell", "Runs the launcher in the same shell process. By default, it creates a new process and causes the initial shell to return.");
	suite.addMode("ui", uiMode);
	siteDeploy = ArgumentParser.GetParser()
		.addDescription("Deploys the documentation site, using the preferences specified in the configuration file. This mechanism completely re-writes"
			+ " the remote site, so that builds are totally reproduceable.")
		.addArgument('c', "config", ArgumentParser.Type.STRING,
			MethodScriptFileLocations.getDefault().getSiteDeployFile().getAbsolutePath(),
			"The path to the config file for deployment", "configFile", false)
		.addFlag("generate-prefs", "Generates the preferences file initially, which you can then fill in.")
		.addFlag("use-local-cache", "Generally, when the uploader runs, it checks the remote server to see if"
			+ " the file already exists there (and is unchanged compared to the local file). If it is unchanged,"
			+ " the upload is skipped. However, even checking with the remote to see what the status of the"
			+ " remote file is takes time. If you are the only one uploading files, then we can simply use"
			+ " a local cache of what the remote system has, and we can skip the step of checking with the"
			+ " remote server for any given file. The cache is always populated, whether or not this flag"
			+ " is set, so if you aren't sure if you can trust the cache, run once without this flag, then"
			+ " for future runs, you can be sure that the local cache is up to date.")
		.addFlag("clear-local-cache", "Clears the local cache of all entries, then exits.")
		.addFlag('d', "do-validation", "Validates all of the uploaded web pages, and prints out a summary of the results."
			+ " This requires internet connection.")
		.addFlag("install", "When installing a fresh server, it is useful to have the setup completely automated. If this flag"
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
			+ " file available, but no login is necessary.");
	suite.addMode("site-deploy", siteDeploy);
	newMode = ArgumentParser.GetParser()
		.addDescription("Creates a blank script in the specified location with the appropriate permissions, having the correct hashbang, and ready to be executed. If"
			+ " the specified file already exists, it will refuse to create it, unless --force is set.")
		.addArgument("Location and name to create the script as. Multiple arguments can be provided, and they will create multiple files.", "<file>", true)
		.addFlag('f', "force", "Forces the file to be overwritten, even if it already exists");
	suite.addMode("new", newMode);

	ARGUMENT_SUITE = suite;
    }

    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public static void main(String[] args) throws Exception {
	try {
	    Implementation.setServerType(Implementation.Type.SHELL);

	    CHLog.initialize(MethodScriptFileLocations.getDefault().getJarDirectory());
	    Prefs.init(MethodScriptFileLocations.getDefault().getPreferencesFile());

	    Prefs.SetColors();
	    if (Prefs.UseColors()) {
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

	    if (args.length == 0) {
		args = new String[]{"--help"};
	    }

	    // I'm not sure why this is in Main, but if this breaks something, it needs to be put back.
	    // However, if it is put back, then it needs to be figured out why this causes the terminal
	    // to lose focus on mac.
	    //AnnotationChecks.checkForceImplementation();
	    ArgumentParser mode;
	    ArgumentParser.ArgumentParserResults parsedArgs;

	    try {
		ArgumentSuite.ArgumentSuiteResults results = ARGUMENT_SUITE.match(args, "help");
		mode = results.getMode();
		parsedArgs = results.getResults();
	    } catch (ArgumentParser.ResultUseException | ArgumentParser.ValidationException e) {
		StreamUtils.GetSystemOut().println(TermColors.RED + e.getMessage() + TermColors.RESET);
		mode = helpMode;
		parsedArgs = null;
	    }

	    if (mode == helpMode) {
		String modeForHelp = null;
		if (parsedArgs != null) {
		    modeForHelp = parsedArgs.getStringArgument();
		}
		modeForHelp = ARGUMENT_SUITE.getModeFromAlias(modeForHelp);
		if (modeForHelp == null) {
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

	    if (mode == managerMode) {
		Manager.start();
		System.exit(0);
	    } else if (mode == coreFunctionsMode) {
		List<String> core = new ArrayList<>();
		for (api.Platforms platform : api.Platforms.values()) {
		    for (FunctionBase f : FunctionList.getFunctionList(platform)) {
			if (f.isCore()) {
			    core.add(f.getName());
			}
		    }
		}
		Collections.sort(core);
		StreamUtils.GetSystemOut().println(StringUtils.Join(core, ", "));
		System.exit(0);
	    } else if (mode == interpreterMode) {
		new Interpreter(parsedArgs.getStringListArgument(), parsedArgs.getStringArgument("location-----"));
		System.exit(0);
	    } else if (mode == installCmdlineMode) {
		Interpreter.install();
		System.exit(0);
	    } else if (mode == uninstallCmdlineMode) {
		Interpreter.uninstall();
		System.exit(0);
	    } else if (mode == docgenMode) {
		DocGenUI.main(args);
		System.exit(0);
	    } else if (mode == mslpMode) {
		String mslp = parsedArgs.getStringArgument();
		if (mslp.isEmpty()) {
		    StreamUtils.GetSystemOut().println("Usage: --mslp path/to/folder");
		    System.exit(0);
		}
		MSLPMaker.start(mslp);
		System.exit(0);
	    } else if (mode == versionMode) {
		StreamUtils.GetSystemOut().println("You are running " + Implementation.GetServerType().getBranding() + " version " + loadSelfVersion());
		System.exit(0);
	    } else if (mode == copyrightMode) {
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
	    } else if (mode == printDBMode) {
		ConnectionMixinFactory.ConnectionMixinOptions options = new ConnectionMixinFactory.ConnectionMixinOptions();
		options.setWorkingDirectory(MethodScriptFileLocations.getDefault().getConfigDirectory());
		PersistenceNetwork pn = new PersistenceNetwork(MethodScriptFileLocations.getDefault().getPersistenceConfig(),
			new URI("sqlite://" + MethodScriptFileLocations.getDefault().getDefaultPersistenceDBFile().getCanonicalPath()
				//This replace is required on Windows.
				.replace('\\', '/')), options);
		Map<String[], String> values = pn.getNamespace(new String[]{});
		for (String[] s : values.keySet()) {
		    StreamUtils.GetSystemOut().println(StringUtils.Join(s, ".") + "=" + values.get(s));
		}
		System.exit(0);
	    } else if (mode == docsMode) {
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
		DocGen.functions(docs, api.Platforms.INTERPRETER_JAVA, true);
		StreamUtils.GetSystemErr().println("Done.");
		System.exit(0);
	    } else if (mode == examplesMode) {
		ExampleLocalPackageInstaller.run(MethodScriptFileLocations.getDefault().getJarDirectory(),
			parsedArgs.getStringArgument());
	    } else if (mode == verifyMode) {
		String file = parsedArgs.getStringArgument();
		if ("".equals(file)) {
		    StreamUtils.GetSystemErr().println("File parameter is required.");
		    System.exit(1);
		}
		File f = new File(file);
		String script = FileUtil.read(f);
		try {
		    try {
			MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, f, file.endsWith("ms")));
		    } catch (ConfigCompileException ex) {
			Set<ConfigCompileException> s = new HashSet<>(1);
			s.add(ex);
			throw new ConfigCompileGroupException(s);
		    }
		} catch (ConfigCompileGroupException ex) {
		    List<Map<String, Object>> err = new ArrayList<>();
		    for (ConfigCompileException e : ex.getList()) {
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
	    } else if (mode == apiMode) {
		String function = parsedArgs.getStringArgument();
		if ("".equals(function)) {
		    StreamUtils.GetSystemErr().println("Usage: java -jar CommandHelper.jar --api <function name>");
		    System.exit(1);
		}
		FunctionBase f;
		try {
		    f = FunctionList.getFunction(function, Target.UNKNOWN);
		} catch (ConfigCompileException e) {
		    StreamUtils.GetSystemErr().println("The function '" + function + "' was not found.");
		    System.exit(1);
		    throw new Error();
		}
		DocGen.DocInfo di = new DocGen.DocInfo(f.docs());
		String ret = di.ret.replaceAll("</?[a-z].*?>", "");
		String args2 = di.args.replaceAll("</?[a-z].*?>", "");
		String desc = (di.desc + (di.extendedDesc != null ? "\n\n" + di.extendedDesc : "")).replaceAll("</?[a-z].*?>", "");
		StreamUtils.GetSystemOut().println(StringUtils.Join(new String[]{
		    function,
		    "Returns " + ret,
		    "Expects " + args2,
		    desc
		}, " // "));
		System.exit(0);
	    } else if (mode == syntaxMode) {
		// TODO: Maybe load extensions here?
		List<String> syntax = parsedArgs.getStringListArgument();
		String type = (syntax.size() >= 1 ? syntax.get(0) : null);
		String theme = (syntax.size() >= 2 ? syntax.get(1) : null);
		StreamUtils.GetSystemOut().println(SyntaxHighlighters.generate(type, theme));
		System.exit(0);
	    } else if (mode == optimizerTestMode) {
		String path = parsedArgs.getStringArgument();
		File source = new File(path);
		String plain = FileUtil.read(source);
		Security.setSecurityEnabled(false);
		String optimized;
		try {
		    try {
			optimized = OptimizationUtilities.optimize(plain, source);
		    } catch(ConfigCompileException ex) {
			Set<ConfigCompileException> group = new HashSet<>();
			group.add(ex);
			throw new ConfigCompileGroupException(group);
		    }
		} catch(ConfigCompileGroupException ex) {
		    ConfigRuntimeException.HandleUncaughtException(ex, null);
		    System.exit(1);
		    return;
		}
		StreamUtils.GetSystemOut().println(optimized);
		System.exit(0);
	    } else if (mode == cmdlineMode) {
		//We actually can't use the parsedArgs, because there may be cmdline switches in
		//the arguments that we want to ignore here, but otherwise pass through. parsedArgs
		//will prevent us from seeing those, however.
		List<String> allArgs = new ArrayList<>(Arrays.asList(args));
		//The 0th arg is the cmdline verb though, so remove that.
		allArgs.remove(0);
		if (allArgs.isEmpty()) {
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
	    } else if (mode == extensionDocsMode) {
		String inputJarS = parsedArgs.getStringArgument("input-jar");
		String outputFileS = parsedArgs.getStringArgument("output-file");
		if (inputJarS == null) {
		    StreamUtils.GetSystemOut().println("Usage: --input-jar extension-docs path/to/extension.jar [--output-file path/to/output.md]\n\tIf the output is blank, it is printed to stdout.");
		    System.exit(1);
		}
		File inputJar = new File(inputJarS);
		OutputStream outputFile = StreamUtils.GetSystemOut();
		if (outputFileS != null) {
		    outputFile = new FileOutputStream(new File(outputFileS));
		}
		ExtensionDocGen.generate(inputJar, outputFile);
	    } else if (mode == docExportMode) {
		String extensionDirS = parsedArgs.getStringArgument("extension-dir");
		String outputFileS = parsedArgs.getStringArgument("output-file");
		OutputStream outputFile = StreamUtils.GetSystemOut();
		if (outputFileS != null) {
		    outputFile = new FileOutputStream(new File(outputFileS));
		}
		Implementation.forceServerType(Implementation.Type.BUKKIT);
		File extensionDir = new File(extensionDirS);
		if (extensionDir.exists()) {
		    //Might not exist, but that's ok, however we will print a warning
		    //to stderr.
		    for (File f : extensionDir.listFiles()) {
			if (f.getName().endsWith(".jar")) {
			    cd.addDiscoveryLocation(f.toURI().toURL());
			}
		    }
		} else {
		    StreamUtils.GetSystemErr().println("Extension directory specificed doesn't exist: "
			    + extensionDirS + ". Continuing anyways.");
		}
		new DocGenExportTool(cd, outputFile).export();
	    } else if (mode == profilerSummaryMode) {
		String input = parsedArgs.getStringArgument();
		if ("".equals(input)) {
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
	    } else if (mode == rsaKeyGenMode) {
		String outputFileString = parsedArgs.getStringArgument('o');
		File privOutputFile = new File(outputFileString);
		File pubOutputFile = new File(outputFileString + ".pub");
		String label = parsedArgs.getStringArgument('l');
		if (privOutputFile.exists() || pubOutputFile.exists()) {
		    StreamUtils.GetSystemErr().println("Either the public key or private key file already exists. This utility will not overwrite any existing files.");
		    System.exit(1);
		}
		RSAEncrypt enc = RSAEncrypt.generateKey(label);
		FileUtil.write(enc.getPrivateKey(), privOutputFile);
		FileUtil.write(enc.getPublicKey(), pubOutputFile);
		System.exit(0);
	    } else if (mode == pnViewerMode) {
		if (parsedArgs.isFlagSet("server")) {
		    if (parsedArgs.getNumberArgument("port") == null) {
			StreamUtils.GetSystemErr().println("When running as a server, port is required.");
			System.exit(1);
		    }
		    int port = parsedArgs.getNumberArgument("port").intValue();
		    if (port > 65535 || port < 1) {
			StreamUtils.GetSystemErr().println("Port must be between 1 and 65535.");
			System.exit(1);
		    }
		    String password = parsedArgs.getStringArgument("password");
		    if ("".equals(password)) {
			ConsoleReader reader = null;
			try {
			    reader = new ConsoleReader();
			    reader.setExpandEvents(false);
			    Character cha = new Character((char) 0);
			    password = reader.readLine("Enter password: ", cha);
			} finally {
			    if (reader != null) {
				reader.shutdown();
			    }
			}
		    }
		    if (password == null) {
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
	    } else if (mode == uiMode) {
		if (parsedArgs.isFlagSet("in-shell")) {
		    // Actually launch the GUI
		    UILauncher.main(args);
		} else {
		    // Relaunch the jar in a new process with the --run flag set,
		    // so that the process will be in its own subshell
		    CommandExecutor ce = new CommandExecutor("java -jar "
			    + ClassDiscovery.GetClassContainer(Main.class).getPath() + " "
			    + StringUtils.Join(args, " ") + " --in-shell");
		    ce.start();
		    System.exit(0);
		}
	    } else if(mode == siteDeploy){
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
		String configString = parsedArgs.getStringArgument("config");
		if("".equals(configString)) {
		    System.err.println("Config file missing, check command and try again");
		    System.exit(1);
		}
		File config = new File(configString);
		SiteDeploy.run(generatePrefs, useLocalCache, config, "", doValidation);
	    } else if (mode == newMode) {
		String li = OSUtils.GetLineEnding();
		for (String file : parsedArgs.getStringListArgument()) {
		    File f = new File(file);
		    if (f.exists() && !parsedArgs.isFlagSet('f')) {
			System.out.println(file + " already exists, refusing to create");
			continue;
		    }
		    f.createNewFile();
		    f.setExecutable(true);
		    FileUtil.write("#!/usr/bin/env /usr/local/bin/mscript"
			    + li + li
			    + "/**" + li
			    + " * Name: " + f.getName() + li
			    + " * Author: " + StaticLayer.GetConvertor().GetUser(null) + li
			    + " * Creation Date: " + new Scheduling.simple_date().exec(Target.UNKNOWN, null, new CString("yyyy-MM-dd", Target.UNKNOWN)).val() + li
			    + " * Description: " + li
			    + " */" + li + li, f, true);
		}
	    } else {
		throw new Error("Should not have gotten here");
	    }
	} catch (NoClassDefFoundError error) {
	    StreamUtils.GetSystemErr().println(getNoClassDefFoundErrorMessage(error));
	}
    }

    public static String getNoClassDefFoundErrorMessage(NoClassDefFoundError error) {
	String ret = "The main class requires craftbukkit or bukkit to be included in order to run. If you are seeing"
		+ " this message, you have two options. First, it seems you have renamed your craftbukkit jar, or"
		+ " you are altogether not using craftbukkit. If this is the case, you can download craftbukkit and place"
		+ " it in the correct directory (one above this one) or you can download bukkit, rename it to bukkit.jar,"
		+ " and put it in the CommandHelper directory.";
	//if (Prefs.DebugMode()) {
	    ret += " If you're dying for more details, here:\n";
	    ret += Misc.GetStacktrace(error);
	//}
	return ret;
    }

    @SuppressWarnings({"ThrowableInstanceNotThrown", "ThrowableInstanceNeverThrown"})
    public static String loadSelfVersion() throws Exception {
	File file = new File(new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()), "plugin.yml");
	ZipReader reader = new ZipReader(file);
	if (!reader.exists()) {
	    throw new Exception(new FileNotFoundException(String.format("%s does not exist", file.getPath())));
	}
	try {
	    String contents = reader.getFileContents();
	    Yaml yaml = new Yaml();
	    Map<String, Object> map = (Map<String, Object>) yaml.load(contents);
	    return (String) map.get("version");
	} catch (RuntimeException | IOException ex) {
	    throw new Exception(ex);
	}
    }
}
