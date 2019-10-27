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
import com.laytonsmith.PureUtilities.JavaVersion;
import com.laytonsmith.PureUtilities.TermColors;
import com.laytonsmith.PureUtilities.XMLDocument;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.compiler.OptimizationUtilities;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.extensions.Extension;
import com.laytonsmith.core.extensions.ExtensionManager;
import com.laytonsmith.core.extensions.ExtensionTracker;
import com.laytonsmith.core.functions.FunctionBase;
import com.laytonsmith.core.functions.FunctionList;
import com.laytonsmith.core.functions.Meta;
import com.laytonsmith.core.functions.Scheduling;
import com.laytonsmith.persistence.PersistenceNetwork;
import com.laytonsmith.persistence.PersistenceNetworkImpl;
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
import com.laytonsmith.tools.docgen.DocGenTemplates;
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
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import jline.console.ConsoleReader;
import org.json.simple.JSONValue;

/**
 *
 *
 */
public class Main {

	public static class CmdlineToolCollection {
		private final ArgumentSuite suite;
		private final Map<ArgumentParser, CommandLineTool> dynamicTools;

		public CmdlineToolCollection(ArgumentSuite suite, Map<ArgumentParser, CommandLineTool> dynamicTools) {
			this.suite = suite;
			this.dynamicTools = dynamicTools;
		}

		/**
		 * Gets the argument suite for the command line tools
		 * @return
		 */
		public ArgumentSuite getSuite() {
			return suite;
		}

		/**
		 * Gets the tools themselves, keyed on the ArgumentParser object associated with this tool
		 * @return
		 */
		public Map<ArgumentParser, CommandLineTool> getDynamicTools() {
			return dynamicTools;
		}
	}

	public static CmdlineToolCollection GetCommandLineTools() {
		ArgumentSuite suite = new ArgumentSuite()
			.addDescription("These are the command line tools for MethodScript. For more information about a"
					+ " particular mode, run help <mode name>. To run a command, in general, use the command:\n\n"
					+ "\tjava -jar " + MethodScriptFileLocations.getDefault().getJarFile().getName()
					+ " <mode name> <[mode specific arguments]>\n");

		Map<ArgumentParser, CommandLineTool> dynamicTools = new HashMap<>();
		for(Class<? extends CommandLineTool> ctool : ClassDiscovery.getDefaultInstance()
				.loadClassesWithAnnotationThatExtend(tool.class, CommandLineTool.class)) {
			try {
				CommandLineTool tool = ctool.newInstance();
				ArgumentParser ap = tool.getArgumentParser();
				String toolName = ctool.getAnnotation(tool.class).value();
				suite.addMode(toolName, ap);
				String[] aliases = ctool.getAnnotation(tool.class).aliases();
				if(aliases != null) {
					for(String alias : aliases) {
						suite.addModeAlias(alias, toolName);
					}
				}
				dynamicTools.put(ap, tool);
			} catch (InstantiationException | IllegalAccessException ex) {
				Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Could not load " + ctool.getName(), ex);
			}
		}
		return new CmdlineToolCollection(suite, dynamicTools);
	}

	/**
	 * For some commands, where we don't need to initialize the discovery engine (which is relatively expensive),
	 * we can pre-load into this fast startup list. This facility is unfortunately not available to extensions, due
	 * to the nature of how extensions are discovered, but for built in facilities where it makes sense, there's no
	 * need to discover them the slow way. However, if we forget to update this class, we will still fallback anyways,
	 * and always find the classes. Classes that are added to this list must be able to function without extensions,
	 * and without engine startup, as those will not be loaded when the command is executed.
	 */
	private static final Class[] FAST_STARTUP = new Class[]{
		CopyrightMode.class,
		InstallCmdlineMode.class,
		NewMode.class,
		NewTypeMode.class,
		JavaVersionMode.class,
		EditPrefsMode.class
	};

	@SuppressWarnings("ResultOfObjectAllocationIgnored")
	public static void main(String[] args) throws Exception {
		if(args.length > 0) {
			for(Class c : FAST_STARTUP) {
				String tool = ((tool) c.getAnnotation(tool.class)).value();
				if(args[0].equals(tool)) {
					String[] a;
					if(args.length > 1) {
						a = ArrayUtils.slice(args, 1, args.length - 1);
					} else {
						a = new String[0];
					}
					CommandLineTool t = (CommandLineTool) c.newInstance();
					ArgumentParser.ArgumentParserResults res;
					try {
						res	= t.getArgumentParser().match(a);
					} catch (ArgumentParser.ResultUseException | ArgumentParser.ValidationException e) {
						// They screwed up the args. Rather than just throwing the exception, or duplicating
						// code below, let's just stop trying to do the fast startup mode and skip down to
						// normal mode, which will still fail, but will prevent us from having to duplicate
						// that code up here.
						break;
					}
					t.execute(res);
					return;
				}
			}
		}
		ClassDiscovery cd = ClassDiscovery.getDefaultInstance();
		cd.addThisJar();
		Implementation.setServerType(Implementation.Type.SHELL);
		MethodScriptFileLocations.setDefault(new MethodScriptFileLocations());
		ClassDiscoveryCache cdcCache
				= new ClassDiscoveryCache(MethodScriptFileLocations.getDefault().getCacheDirectory());
		cd.setClassDiscoveryCache(cdcCache);

		MSLog.initialize(MethodScriptFileLocations.getDefault().getJarDirectory());
		Prefs.init(MethodScriptFileLocations.getDefault().getPreferencesFile());

		Prefs.SetColors();
		if(Prefs.UseColors()) {
			//Use jansi to enable output to color properly, even on windows.
			org.fusesource.jansi.AnsiConsole.systemInstall();
		}

		cd.addAllJarsInFolder(MethodScriptFileLocations.getDefault().getExtensionsDirectory());
		ExtensionManager.AddDiscoveryLocation(MethodScriptFileLocations.getDefault().getExtensionsDirectory());
		ExtensionManager.Cache(MethodScriptFileLocations.getDefault().getExtensionCacheDirectory());
		ExtensionManager.Initialize(ClassDiscovery.getDefaultInstance());

		if(args.length == 0) {
			args = new String[]{"help"};
		}

		ArgumentParser mode;
		ArgumentParser.ArgumentParserResults parsedArgs;

		CmdlineToolCollection collection = GetCommandLineTools();
		ArgumentSuite suite = collection.getSuite();

		String helpModeName = HelpMode.class.getAnnotation(tool.class).value();

		boolean wasError = false;

		try {
			ArgumentSuite.ArgumentSuiteResults results = suite.match(args, helpModeName);
			mode = results.getMode();
			parsedArgs = results.getResults();
		} catch (ArgumentParser.ResultUseException | ArgumentParser.ValidationException e) {
			// The mode was found, but the arguments are wrong. Unlike the below catch,
			// we want to print the help just for this mode.
			System.out.println(TermColors.RED + e.getMessage() + "\nSee usage.\n" + TermColors.RESET);
			String[] newArgs = new String[]{"help", args[0]};
			ArgumentSuite.ArgumentSuiteResults results = suite.match(newArgs, helpModeName);
			mode = results.getMode();
			parsedArgs = results.getResults();
			wasError = true;
		} catch (ArgumentSuite.ModeNotFoundException e) {
			StreamUtils.GetSystemOut().println(TermColors.RED + e.getMessage() + TermColors.RESET);
			mode = suite.getMode(helpModeName);
			parsedArgs = null;
			wasError = true;
		}

		if(collection.getDynamicTools().containsKey(mode)) {
			CommandLineTool tool = collection.getDynamicTools().get(mode);
			tool.setSuite(suite);
			if(tool.startupExtensionManager()) {
				ExtensionManager.Startup();
			}
			tool.execute(parsedArgs);
			if(wasError) {
				System.exit(1);
			}
			if(!tool.noExitOnReturn()) {
				System.exit(0);
			}
		} else {
			// This means the requested module could not be found, but our lookup for the help module also failed.
			throw new Error("Should not have gotten here");
		}
	}

	@tool("copyright")
	public static class CopyrightMode extends AbstractCommandLineTool {

		@Override
		public ArgumentParser getArgumentParser() {
			return ArgumentParser.GetParser()
				.addDescription("Prints the copyright and exits.");
		}

		@Override
		public void execute(ArgumentParser.ArgumentParserResults parsedArgs) throws Exception {
			String buildYear = new Scheduling.simple_date().exec(Target.UNKNOWN, null,
					new CString("yyyy", Target.UNKNOWN),
					new Meta.engine_build_date().exec(Target.UNKNOWN, null)).val();
			StreamUtils.GetSystemOut().println("The MIT License (MIT)\n"
					+ "\n"
					+ "Copyright (c) 2012-" + buildYear + " Methodscript Contributors\n"
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
		}

	}

	public static ArgumentBuilder.ArgumentBuilderFinal GetEnvironmentParameter() {
		// This needs to be changed once CH code is moved out, as extensions will be able to add new environment
		// types. Or perhaps not, if the solution is an embedded approach.
		ClassDiscovery cd = ClassDiscovery.getDefaultInstance();
		cd.addDiscoveryLocation(ClassDiscovery.GetClassContainer(Main.class));
		String envs = StringUtils.Join(cd.getClassesThatExtend(Environment.EnvironmentImpl.class)
				.stream()
				.map((c) -> c.getClassName())
				.collect(Collectors.toSet()), ", ", ", or ");
		return new ArgumentBuilder()
						.setDescription("The environments to target during compilation. May be one or more of "
							+ envs + ", but note that " + GlobalEnv.class.getName() + " and "
							+ CompilerEnvironment.class.getName()
							+ " are provided for you.")
						.setUsageName("environments")
						.setOptional()
						.setName("environments")
						.setArgType(ArgumentBuilder.BuilderTypeNonFlag.ARRAY_OF_STRINGS);
	}

	public static Set<Class<? extends Environment.EnvironmentImpl>> GetEnvironmentValue(
			ArgumentParser.ArgumentParserResults parsedArgs) {
		List<String> environments = parsedArgs.getStringListArgument("environments", new ArrayList<>());
		Set<Class<? extends Environment.EnvironmentImpl>> envs = new HashSet<>();
		envs.add(GlobalEnv.class);
		envs.add(CompilerEnvironment.class);
		for(String e : environments) {
			try {
				Class c = ClassDiscovery.getDefaultInstance().forName(e).loadClass();
				if(!Environment.EnvironmentImpl.class.isAssignableFrom(c)) {
					System.out.println("The class " + e + " is not a valid option!");
					System.exit(1);
				}
				envs.add((Class<? extends Environment.EnvironmentImpl>) c);
			} catch (ClassNotFoundException ex) {
				System.out.println("The class " + e + " could not be found!");
				System.exit(1);
			}
		}
		return envs;
	}

	@tool("install-cmdline")
	public static class InstallCmdlineMode extends AbstractCommandLineTool {

		@Override
		public ArgumentParser getArgumentParser() {
			return ArgumentParser.GetParser()
					.addDescription("Installs MethodScript to your system, so that commandline scripts work. (Currently only unix is supported.)")
					.addArgument(new ArgumentBuilder()
						.setDescription("Sets the name of the command. This allows support for multiple installations per system.")
						.setUsageName("command name")
						.setOptional()
						.setName("command")
						.setArgType(ArgumentBuilder.BuilderTypeNonFlag.STRING)
						.setDefaultVal("mscript"));
		}

		@Override
		public void execute(ArgumentParser.ArgumentParserResults parsedArgs) throws Exception {
			String commandName = parsedArgs.getStringArgument("command");
			Interpreter.install(commandName);
			System.exit(0);
		}

	}

	@tool("uninstall-cmdline")
	public static class UninstallCmdlineMode extends AbstractCommandLineTool {

		@Override
		public ArgumentParser getArgumentParser() {
			return ArgumentParser.GetParser()
				.addDescription("Uninstalls the MethodScript interpreter from your system.");
		}

		@Override
		public void execute(ArgumentParser.ArgumentParserResults parsedArgs) throws Exception {
			Interpreter.uninstall();
			System.exit(0);
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
			Implementation.forceServerType(Implementation.Type.SHELL);
			ClassDiscovery.getDefaultInstance()
					.addDiscoveryLocation(ClassDiscovery.GetClassContainer(Main.class));
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
						.setRequiredAndDefault())
				.addArgument(GetEnvironmentParameter());
		}

		@Override
		public void execute(ArgumentParser.ArgumentParserResults parsedArgs) throws Exception {
			String mslp = parsedArgs.getStringArgument();
			Set<Class<? extends Environment.EnvironmentImpl>> envs = GetEnvironmentValue(parsedArgs);
			if(mslp.isEmpty()) {
				StreamUtils.GetSystemOut().println("Usage: --mslp path/to/folder");
				System.exit(1);
			}
			MSLPMaker.start(mslp, envs);
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
				.addDescription("Creates a blank, executable script in the specified location with the appropriate permissions,"
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
			CreateNewFiles(parsedArgs.getStringListArgument(), parsedArgs.isFlagSet('f'));
		}

		public static void CreateNewFiles(List<String> files, boolean force) throws IOException {
			String li = OSUtils.GetLineEnding();
			for(String file : files) {
				File f = new File(file);
				if(f.exists() && !force) {
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
						+ "\tauthor: " + System.getProperty("user.name") + ";" + li
						+ "\tcreated: " + new Scheduling.simple_date().exec(Target.UNKNOWN, null, new CString("yyyy-MM-dd", Target.UNKNOWN)).val() + ";" + li
						+ "\tdescription: " + ";" + li
						+ ">" + li + li, f, true);
			}
		}

	}

	@tool("new-type")
	public static class NewTypeMode extends AbstractCommandLineTool {

		@Override
		public ArgumentParser getArgumentParser() {
			return ArgumentParser.GetParser()
					.addDescription("Creates a new type. This command should only be run at the root of a classLibrary,"
							+ " and given the class name, will create the appropriate folder structure (as necessary)"
							+ " as well as providing a default file prepopulated with a reasonable template. If the"
							+ " file already exists, will refuse to continue.")
					.addArgument(new ArgumentBuilder()
						.setDescription("The template type to use")
						.setUsageName("template")
						.setOptional()
						.setName('t', "type")
						.setArgType(ArgumentBuilder.BuilderTypeNonFlag.STRING)
						.setDefaultVal("class"))
					.addArgument(new ArgumentBuilder()
						.setDescription("The class name to create. This should be the fully qualified class"
								+ " name.")
						.setUsageName("fully qualified class name")
						.setRequiredAndDefault()
						.setArgType(ArgumentBuilder.BuilderTypeNonFlag.STRING));
		}

		@Override
		public void execute(ArgumentParser.ArgumentParserResults parsedArgs) throws Exception {
			String clazz = parsedArgs.getStringArgument();
			String template = parsedArgs.getStringArgument('t').toLowerCase();
			List<String> validTemplates = Arrays.asList("annotation", "class", "enum", "interface");
			if(!validTemplates.contains(template)) {
				System.err.println("Invalid template type specified. Valid template types are: "
						+ validTemplates.toString());
			}
			String classSimpleName;
			String[] split = clazz.split("\\.", -1);
			classSimpleName = split[split.length - 1];

			String author = System.getProperty("user.name");
			String created = new Scheduling.simple_date().exec(Target.UNKNOWN, null,
					new CString("yyyy-MM-dd", Target.UNKNOWN)).val();
			File file = new File(clazz.replace(".", "/") + ".ms");
			if(file.exists()) {
				System.err.println("File " + file + " already exists. Refusing to continue.");
				System.exit(1);
			}
			if(file.getParentFile() != null) {
				file.getParentFile().mkdirs();
			}
			String allTemplate;
			if(split[0].equals("ms")) {
				allTemplate = StreamUtils.GetResource("/templates/new-type-templates/native-all.ms");
			} else {
				allTemplate = StreamUtils.GetResource("/templates/new-type-templates/all.ms");
			}
			String typeTemplate = StreamUtils.GetResource("/templates/new-type-templates/" + template + ".ms");
			allTemplate = String.format(allTemplate, classSimpleName, author, created, clazz);
			typeTemplate = String.format(typeTemplate, clazz);
			FileUtil.write(allTemplate + typeTemplate, file);
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

		@Override
		public boolean startupExtensionManager() {
			return false;
		}

	}

	@tool("doc-export")
	public static class DocExportMode extends AbstractCommandLineTool {

		@Override
		public ArgumentParser getArgumentParser() {
			return ArgumentParser.GetParser()
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
		}

		@Override
		public void execute(ArgumentParser.ArgumentParserResults parsedArgs) throws Exception {
			ClassDiscovery cd = ClassDiscovery.getDefaultInstance();
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
		}
	}

	@tool("cmdline-args")
	public static class CmdlineArgsTool extends AbstractCommandLineTool {

		@Override
		public ArgumentParser getArgumentParser() {
			return ArgumentParser.GetParser()
					.addDescription(Implementation.GetServerType().getBranding() + " requires certain arguments"
							+ " to be passed to the java program to properly start up."
							+ " This tool prints out the arguments that it needs,"
							+ " in a version specific manner. Depending on your system, and the version of"
							+ " the program, you may get different arguments, but these will always be up"
							+ " to date. You can either integrate them into your startup flow manually,"
							+ " or dynamically call this command to automatically update it. The command"
							+ " may return an empty string. If so, this means that no commandline flags"
							+ " are needed.");
		}

		@Override
		public void execute(ArgumentParser.ArgumentParserResults parsedArgs) throws Exception {
			String args = "";
			if(JavaVersion.getMajorVersion() > 8) {
				// Need to add the --add-opens values. The values live in interpreter-helper/modules
				String modules = Static.GetStringResource("/interpreter-helpers/modules");
				modules = modules.replaceAll("(.*)\n", "--add-opens $1=ALL-UNNAMED ");
				args += " " + modules;
			}
			args += "-Xrs ";
			StreamUtils.GetSystemOut().println(args.trim());
		}

	}

	@tool("site-deploy")
	public static class SiteDeployTool extends AbstractCommandLineTool {

		@Override
		public ArgumentParser getArgumentParser() {
			return ArgumentParser.GetParser()
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
						.asFlag().setName("no-progress-clear"))
				.addArgument(new ArgumentBuilder()
						.setDescription("If set, overrides the post-script value in the config.")
						.setUsageName("script-location")
						.setOptional()
						.setName("override-post-script")
						.setArgType(ArgumentBuilder.BuilderTypeNonFlag.STRING)
						.setDefaultVal(""))
				.addArgument(new ArgumentBuilder()
						.setDescription("If the rsa key is in the non-default location, that location be specified"
								+ " here")
						.setUsageName("id-rsa-path")
						.setOptional()
						.setName("override-id-rsa")
						.setArgType(ArgumentBuilder.BuilderTypeNonFlag.STRING)
						.setDefaultVal(""));
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
		}

		@Override
		public void execute(ArgumentParser.ArgumentParserResults parsedArgs) throws Exception {
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
			String overridePostScript = parsedArgs.getStringArgument("override-post-script");
			String overrideIdRsa = parsedArgs.getStringArgument("override-id-rsa");
			if(overrideIdRsa.equals("")) {
				overrideIdRsa = null;
			}
			if("".equals(configString)) {
				System.err.println("Config file missing, check command and try again");
				System.exit(1);
			}
			File config = new File(configString);
			SiteDeploy.run(generatePrefs, useLocalCache, config, "", doValidation, !noProgressClear,
					overridePostScript, overrideIdRsa);
		}

	}

	@tool("api")
	public static class APIMode extends AbstractCommandLineTool {

		@Override
		public ArgumentParser getArgumentParser() {
			return ArgumentParser.GetParser()
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
						.asFlag().setName('e', "examples"))
				.addArgument(new ArgumentBuilder()
						.setDescription("The API platform to use. By default, INTERPRETER_JAVA, but may be one of "
							+ StringUtils.Join(api.Platforms.values(), ", ", ", or "))
						.setUsageName("platform")
						.setOptional()
						.setName("platform")
						.setArgType(ArgumentBuilder.BuilderTypeNonFlag.STRING)
						.setDefaultVal("INTERPRETER_JAVA"));
		}

		@Override
		public void execute(ArgumentParser.ArgumentParserResults parsedArgs) throws Exception {
			String function = parsedArgs.getStringArgument();
			boolean examples = parsedArgs.isFlagSet('e');
			api.Platforms platform = api.Platforms.valueOf(parsedArgs.getStringArgument("platform"));
			if("".equals(function)) {
				StreamUtils.GetSystemErr().println("Usage: java -jar CommandHelper.jar api <function name>");
				System.exit(1);
			}
			List<FunctionBase> fl = new ArrayList<>();
			for(FunctionBase fb : FunctionList.getFunctionList(platform, null)) {
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
		}

	}

	@tool("verify")
	public static class VerifyMode extends AbstractCommandLineTool {

		@Override
		public ArgumentParser getArgumentParser() {
			return ArgumentParser.GetParser()
				.addDescription("Compiles the given file, returning a json describing the errors in the file, or"
						+ " returning nothing if the file compiles cleanly. The target environment(s)"
						+ " must be specified if not targetting command line.")
				.addArgument(new ArgumentBuilder()
						.setDescription("The file to check")
						.setUsageName("file")
						.setRequiredAndDefault())
				.addArgument(GetEnvironmentParameter());
		}

		@Override
		public boolean startupExtensionManager() {
			return false;
		}

		@Override
		public void execute(ArgumentParser.ArgumentParserResults parsedArgs) throws Exception {
			String file = parsedArgs.getStringArgument();
			if("".equals(file)) {
				StreamUtils.GetSystemErr().println("File parameter is required.");
				System.exit(1);
			}

			Environment env = Environment.createEnvironment(new CompilerEnvironment());
			env.getEnv(CompilerEnvironment.class).setLogCompilerWarnings(false);
			Set<Class<? extends Environment.EnvironmentImpl>> envs = GetEnvironmentValue(parsedArgs);
			File f = new File(file);
			String script = FileUtil.read(f);
			try {
				try {
					MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, env, f, file.endsWith("ms")), null,
							envs);
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
		}

	}

	@tool("optimizer-test")
	public static class OptimizerTestMode extends AbstractCommandLineTool {

		@Override
		public ArgumentParser getArgumentParser() {
			return ArgumentParser.GetParser()
				.addDescription("Given a source file, reads it in and outputs the \"optimized\" version."
						+ " This is meant as a debug"
						+ " tool, but could be used as an obfuscation tool as well. The target environment(s)"
						+ " must be specified if not targetting command line.")
				.addArgument(new ArgumentBuilder()
						.setDescription("File path")
						.setUsageName("file")
						.setRequiredAndDefault())
				.addArgument(GetEnvironmentParameter());
		}

		@Override
		public void execute(ArgumentParser.ArgumentParserResults parsedArgs) throws Exception {
			String path = parsedArgs.getStringArgument();
			Set<Class<? extends Environment.EnvironmentImpl>> envs = GetEnvironmentValue(parsedArgs);
			File source = new File(path);
			String plain = FileUtil.read(source);
			Security.setSecurityEnabled(false);
			String optimized;
			Environment env = Environment.createEnvironment(new CompilerEnvironment());
			env.getEnv(CompilerEnvironment.class).setLogCompilerWarnings(false);
			try {
				try {
					optimized = OptimizationUtilities.optimize(plain, null, envs, source);
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
		}

	}


	@tool("eval")
	public static class EvalMode extends AbstractCommandLineTool {

		@Override
		public ArgumentParser getArgumentParser() {
			return ArgumentParser.GetParser()
					.addDescription("Runs the given MethodScript code, then exits.")
					.addArgument(new ArgumentBuilder()
						.setDescription("The code to run")
						.setUsageName("methodscript code")
						.setRequiredAndDefault());
		}

		@Override
		public void execute(ArgumentParser.ArgumentParserResults parsedArgs) throws Exception {
			ClassDiscovery.getDefaultInstance().addThisJar();

			String script = parsedArgs.getStringArgument();
			File file = new File("Interpreter");
			Environment env = Static.GenerateStandaloneEnvironment(true);
			Set<Class<? extends Environment.EnvironmentImpl>> envs = Environment.getDefaultEnvClasses();
			MethodScriptCompiler.execute(script, file, true, env, envs, (s) -> {
				System.out.println(s);
			}, null, null);
		}
	}

	@tool("print-db")
	public static class PrintDBMode extends AbstractCommandLineTool {

		@Override
		public ArgumentParser getArgumentParser() {
			return ArgumentParser.GetParser()
				.addDescription("Prints out the built in database in a human readable form, then exits.");
		}

		@Override
		public void execute(ArgumentParser.ArgumentParserResults parsedArgs) throws Exception {
			ConnectionMixinFactory.ConnectionMixinOptions options = new ConnectionMixinFactory.ConnectionMixinOptions();
			options.setWorkingDirectory(MethodScriptFileLocations.getDefault().getConfigDirectory());
			PersistenceNetwork pn = new PersistenceNetworkImpl(MethodScriptFileLocations.getDefault().getPersistenceConfig(),
					new URI("sqlite://" + MethodScriptFileLocations.getDefault().getDefaultPersistenceDBFile().getCanonicalPath()
							//This replace is required on Windows.
							.replace('\\', '/')), options);
			Map<String[], String> values = pn.getNamespace(new String[]{});
			for(String[] s : values.keySet()) {
				StreamUtils.GetSystemOut().println(StringUtils.Join(s, ".") + "=" + values.get(s));
			}
			System.exit(0);
		}
	}

	@tool("docs")
	public static class DocsMode extends AbstractCommandLineTool {

		@Override
		public ArgumentParser getArgumentParser() {
			return ArgumentParser.GetParser()
				.addDescription("Prints documentation for the functions that CommandHelper knows about, then exits.")
				.addArgument(new ArgumentBuilder()
						.setDescription("The type of the documentation, defaulting to html."
							+ " It may be one of the following: "
							+ StringUtils.Join(DocGen.MarkupType.values(), ", ", ", or "))
						.setUsageName("type")
						.setOptionalAndDefault()
						.setDefaultVal("html"));
		}

		@Override
		public void execute(ArgumentParser.ArgumentParserResults parsedArgs) throws Exception {
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
		}
	}

	@tool("syntax")
	public static class SyntaxMode extends AbstractCommandLineTool {

		@Override
		public ArgumentParser getArgumentParser() {
			return ArgumentParser.GetParser()
				.addDescription("Generates the syntax highlighter for the specified editor (if available).")
				.addArgument(new ArgumentBuilder()
						.setDescription("The type of the syntax file to generate. Don't specify a type to see the"
								+ " available options.")
						.setUsageName("type")
						.setOptionalAndDefault()
						.setArgType(ArgumentBuilder.BuilderTypeNonFlag.STRING));
		}

		@Override
		public void execute(ArgumentParser.ArgumentParserResults parsedArgs) throws Exception {
			// TODO: Maybe load extensions here?
			List<String> syntax = parsedArgs.getStringListArgument();
			String type = (syntax.size() >= 1 ? syntax.get(0) : null);
			String theme = (syntax.size() >= 2 ? syntax.get(1) : null);
			StreamUtils.GetSystemOut().println(SyntaxHighlighters.generate(type, theme));
			System.exit(0);
		}
	}

	@tool("examples")
	public static class ExamplesMode extends AbstractCommandLineTool {

		@Override
		public ArgumentParser getArgumentParser() {
			return ArgumentParser.GetParser()
				.addDescription("Installs one of the built in LocalPackage examples, which may in and of itself be"
						+ " useful, but is primarily meant to showcase various features.")
				.addArgument(new ArgumentBuilder()
						.setDescription("The name of the package to install. Leave blank to see a list of examples to"
								+ " choose from.")
						.setUsageName("packageName")
						.setOptionalAndDefault());
		}

		@Override
		public void execute(ArgumentParser.ArgumentParserResults parsedArgs) throws Exception {
			ExampleLocalPackageInstaller.run(MethodScriptFileLocations.getDefault().getJarDirectory(),
						parsedArgs.getStringArgument());
		}
	}

	@tool("cmdline")
	public static class CmdlineMode extends AbstractCommandLineTool {

		@Override
		public ArgumentParser getArgumentParser() {
			return ArgumentParser.GetParser()
				.addDescription("Given a source file, runs it in cmdline mode. This is similar to"
						+ " the interpreter mode, but allows for tty input (which is required for some functions,"
						+ " like the prompt_* functions) and provides better information for errors, as the"
						+ " file is known.")
				.addArgument(new ArgumentBuilder()
						.setDescription("File path/arguments")
						.setUsageName("file and args")
						.setRequiredAndDefault());
		}

		@Override
		public void execute(ArgumentParser.ArgumentParserResults parsedArgs) throws Exception {
			//We actually can't use the parsedArgs, because there may be cmdline switches in
			//the arguments that we want to ignore here, but otherwise pass through. parsedArgs
			//will prevent us from seeing those, however.
			List<String> allArgs = parsedArgs.getRawArguments();
			if(allArgs.isEmpty()) {
				StreamUtils.GetSystemErr().println("Usage: path/to/file.ms [arg1 arg2]");
				System.exit(1);
			}
			String fileName = allArgs.get(0);
			allArgs.remove(0);
			try {
				Interpreter.startWithTTY(fileName, allArgs);
			} catch (Profiles.InvalidProfileException ex) {
				StreamUtils.GetSystemErr().println("Invalid profile file at " + MethodScriptFileLocations.getDefault()
						.getProfilesFile()
						+ ": " + ex.getMessage());
				System.exit(1);
			}
			StaticLayer.GetConvertor().runShutdownHooks();
			System.exit(0);
		}
	}

	@tool("extension-docs")
	public static class ExtensionDocsMode extends AbstractCommandLineTool {

		@Override
		public ArgumentParser getArgumentParser() {
			return ArgumentParser.GetParser()
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
						.setName('o', "output-file")
						.setArgType(ArgumentBuilder.BuilderTypeNonFlag.STRING));
		}

		@Override
		public void execute(ArgumentParser.ArgumentParserResults parsedArgs) throws Exception {
			String inputJarS = parsedArgs.getStringArgument("input-jar");
			String outputFileS = parsedArgs.getStringArgument("output-file");

			File inputJar = new File(inputJarS);
			OutputStream outputFile = StreamUtils.GetSystemOut();
			if(outputFileS != null) {
				outputFile = new FileOutputStream(new File(outputFileS));
			}
			ExtensionDocGen.generate(inputJar, outputFile);
		}
	}

	@tool("profiler-summary")
	public static class ProfilerSummaryMode extends AbstractCommandLineTool {

		@Override
		public ArgumentParser getArgumentParser() {
			return ArgumentParser.GetParser()
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
		}

		@Override
		public void execute(ArgumentParser.ArgumentParserResults parsedArgs) throws Exception {
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
		}
	}

	@tool("key-gen")
	public static class RSAKeyGenMode extends AbstractCommandLineTool {

		@Override
		public ArgumentParser getArgumentParser() {
			return ArgumentParser.GetParser()
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
		}

		@Override
		public void execute(ArgumentParser.ArgumentParserResults parsedArgs) throws Exception {
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
		}
	}

	@tool("pn-viewer")
	public static class PNViewerMode extends AbstractCommandLineTool {

		@Override
		public ArgumentParser getArgumentParser() {
			return ArgumentParser.GetParser()
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
		}

		@Override
		public void execute(ArgumentParser.ArgumentParserResults parsedArgs) throws Exception {
			Implementation.forceServerType(Implementation.Type.SHELL);
			ClassDiscovery.getDefaultInstance()
				.addDiscoveryLocation(ClassDiscovery.GetClassContainer(Main.class));
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
					try(ConsoleReader reader = new ConsoleReader()) {
						reader.setExpandEvents(false);
						Character cha = (char) 0;
						password = reader.readLine("Enter password: ", cha);
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
		}
	}

	@tool("core-functions")
	public static class CoreFunctionsMode extends AbstractCommandLineTool {

		@Override
		public ArgumentParser getArgumentParser() {
			return ArgumentParser.GetParser()
				.addDescription("Prints a list of functions tagged with the @core annotation, then exits.");
		}

		@Override
		public void execute(ArgumentParser.ArgumentParserResults parsedArgs) throws Exception {
			List<String> core = new ArrayList<>();
			for(api.Platforms platform : api.Platforms.values()) {
				for(FunctionBase f : FunctionList.getFunctionList(platform, null)) {
					if(f.isCore()) {
						core.add(f.getName());
					}
				}
			}
			Collections.sort(core);
			StreamUtils.GetSystemOut().println(StringUtils.Join(core, ", "));
			System.exit(0);
		}
	}

	@tool("ui")
	public static class UIMode extends AbstractCommandLineTool {

		@Override
		public ArgumentParser getArgumentParser() {
			return ArgumentParser.GetParser()
				.addDescription("Launches a GUI that provides a list of all the sub GUI tools provided, and allows"
						+ " selection of a module. This command creates a subshell to run the launcher in, so that the"
						+ " original cmdline shell returns.")
				.addArgument(new ArgumentBuilder()
						.setDescription("Runs the launcher in the same shell process. By default, it creates a new"
								+ " process and causes the initial shell to return.")
						.asFlag().setName("in-shell"));
		}

		@Override
		public void execute(ArgumentParser.ArgumentParserResults parsedArgs) throws Exception {
			if(parsedArgs.isFlagSet("in-shell")) {
				// Actually launch the GUI
				UILauncher.main(parsedArgs.getRawArguments().toArray(new String[0]));
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
				largs.addAll(parsedArgs.getRawArguments());
				largs.add("--in-shell");
				CommandExecutor ce = new CommandExecutor(largs.toArray(new String[largs.size()]));
				ce.start();
				System.exit(0);
			}
		}
	}

	@tool("build-extension")
	public static class ExtensionBuilderMode extends AbstractCommandLineTool {

		@Override
		public ArgumentParser getArgumentParser() {
			return ArgumentParser.GetParser()
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
		}

		@Override
		@SuppressWarnings("UseSpecificCatch")
		public void execute(ArgumentParser.ArgumentParserResults parsedArgs) throws Exception {
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
		}
	}

	@tool(value = "help", aliases = {"/?", "--help", "-help", "-h"})
	public static class HelpMode extends AbstractCommandLineTool {

		@Override
		public ArgumentParser getArgumentParser() {
			return ArgumentParser.GetParser()
				.addDescription("Displays help for all modes, or the given mode if one is provided.")
				.addArgument(new ArgumentBuilder().setDescription("Displays help for the given mode.")
						.setUsageName("mode name")
						.setOptionalAndDefault()
						.setArgType(ArgumentBuilder.BuilderTypeNonFlag.STRING))
				.setErrorOnUnknownArgs(false);
		}

		@Override
		public void execute(ArgumentParser.ArgumentParserResults parsedArgs) throws Exception {
			String modeForHelp = null;
			if(parsedArgs != null) {
				modeForHelp = parsedArgs.getStringArgument();
			}
			modeForHelp = getSuite().getModeFromAlias(modeForHelp);
			if(modeForHelp == null) {
				//Display the general help
				StreamUtils.GetSystemOut().println(getSuite().getBuiltDescription());
				System.exit(0);
			} else {
				//Display the help for this mode
				StreamUtils.GetSystemOut().println(getSuite().getMode(modeForHelp).getBuiltDescription());
			}
		}

		@Override
		public boolean startupExtensionManager() {
			return false;
		}

	}

	@tool("help-topic")
	public static class HelpTopicMode extends AbstractCommandLineTool {

		@Override
		public ArgumentParser getArgumentParser() {
			return ArgumentParser.GetParser()
					.addDescription("Provides information on a general topic. To see the list of topics, run with"
							+ " no arguments.")
					.addArgument(new ArgumentBuilder()
						.setDescription("The topic to read more about.")
						.setUsageName("topic name")
						.setOptionalAndDefault()
						.setArgType(ArgumentBuilder.BuilderTypeNonFlag.STRING));
		}

		@Override
		public void execute(ArgumentParser.ArgumentParserResults parsedArgs) throws Exception {
			Map<String, String> topics = new HashMap<>();
			for(ExtensionTracker t : ExtensionManager.getTrackers().values()) {
				for(Extension e : t.getExtensions()) {
					Map<String, String> extTopics = e.getHelpTopics();
					if(extTopics != null) {
						topics.putAll(extTopics);
					}
				}
			}
			String arg = parsedArgs.getStringArgument();
			if("".equals(arg)) {
				SortedSet<String> st = new TreeSet<>(topics.keySet());
				System.out.println(StringUtils.Join(st, ", "));
			} else {
				if(topics.containsKey(arg)) {
					String output = topics.get(arg);
					output = DocGenTemplates.DoTemplateReplacement(output, DocGenTemplates.GetGenerators());
					output = Interpreter.reverseHTML(output);
					System.out.println(output);
				} else {
					System.out.println(TermColors.RED + "Could not find that help topic." + TermColors.RESET);
				}
			}
		}
	}

	@tool("java-version")
	public static class JavaVersionMode extends AbstractCommandLineTool {

		@Override
		public ArgumentParser getArgumentParser() {
			return ArgumentParser.GetParser()
					.addDescription("Prints the current major java version then exits.");
		}

		@Override
		public void execute(ArgumentParser.ArgumentParserResults parsedArgs) throws Exception {
			System.out.println(JavaVersion.getMajorVersion());
			System.exit(0);
		}

	}

	public static String GetDirectoryTextEditor() {
		String cmd;
		if(OSUtils.GetOS().isWindows()) {
			cmd = "code.cmd";
		} else {
			cmd = "vim";
		}

		if(OSUtils.GetOS().isUnixLike()) {
			if(System.getenv("EDITOR") != null) {
				cmd = System.getenv("EDITOR");
			}
			if(System.getenv("VISUAL") != null) {
				cmd = System.getenv("VISUAL");
			}
		}


		if(System.getenv("MS_EDITOR") != null && !System.getenv("MS_EDITOR").equals("")) {
			cmd = System.getenv("MS_EDITOR");
		}

		return cmd;
	}

	@tool("edit-prefs")
	public static class EditPrefsMode extends AbstractCommandLineTool {

		@Override
		public ArgumentParser getArgumentParser() {
			return ArgumentParser.GetParser()
					.addDescription("Launches the prefs directory in a default text editor, or your defined editor.")
					.addExtendedDescription("By default, on Windows, \"code.cmd\" (Visual Studio Code)"
							+ " is the default editor. On"
							+ " Linux systems, vim is default, though if $EDITOR is set, that is used, or if"
							+ " $VISUAL is also set, that is used instead. In all OSes, you can override the"
							+ " default editor with the MS_EDITOR environment variable. The prefs folder is passed"
							+ " as the last argument to the command. Passing in the editor to the command will"
							+ " bypass all these mechanisms, and use the specified editor in that one launch.")
					.addArgument(new ArgumentBuilder()
						.setDescription("Waits for the editor to finish. This is implied for some known programs,"
								+ " where that is necessary (" + StringUtils.Join(NEEDS_WAIT, ", ", ", and ", " and ")
								+ ") but may be specified manually. This is generally not necessary for GUI editors that"
								+ " open in a new window.")
						.asFlag()
						.setName("wait"))
					.addArgument(new ArgumentBuilder()
						.setDescription("Uses a different command to open the editor. This overrides the environment"
								+ " value (if set).")
						.setUsageName("command")
						.setOptionalAndDefault()
						.setArgType(ArgumentBuilder.BuilderTypeNonFlag.STRING)
						.setDefaultVal(""));
		}
		private static final String[] NEEDS_WAIT = new String[] {"vim", "nano", "emacs"};

		@Override
		public void execute(ArgumentParser.ArgumentParserResults parsedArgs) throws Exception {
			Implementation.forceServerType(Implementation.Type.SHELL);
			if(!MethodScriptFileLocations.getDefault()
					.getPreferencesDirectory().exists()) {
				System.err.println("Prefs directory does not exist!");
				System.exit(1);
			}
			String cmd;
			boolean wait = false;

			cmd = GetDirectoryTextEditor() + " %s";
			if(!parsedArgs.getStringArgument().equals("")) {
				cmd = parsedArgs.getStringArgument();
			}

			for(String nw : NEEDS_WAIT) {
				if(cmd.startsWith(nw + " ")) {
					wait = true;
					break;
				}
			}

			CommandExecutor c = new CommandExecutor(String.format(cmd, "\"" + MethodScriptFileLocations.getDefault()
					.getPreferencesDirectory()) + "\"");
			c.setSystemInputsAndOutputs();
			try {
				c.start();
			} catch (IOException ex) {
				System.err.println("Could not launch editor: " + ex.getMessage());
			}
			if(parsedArgs.isFlagSet("wait") || wait) {
				c.waitFor();
			}

			System.exit(0);
		}

	}
}
