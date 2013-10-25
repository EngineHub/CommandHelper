package com.laytonsmith.tools;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscoveryCache;
import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.PureUtilities.Common.FileUtil;
import com.laytonsmith.PureUtilities.RunnableQueue;
import com.laytonsmith.PureUtilities.TermColors;
import static com.laytonsmith.PureUtilities.TermColors.*;
import com.laytonsmith.abstraction.AbstractConvertor;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.MCColor;
import com.laytonsmith.abstraction.MCEnchantment;
import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCFireworkBuilder;
import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.MCItemMeta;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCNote;
import com.laytonsmith.abstraction.MCPluginMeta;
import com.laytonsmith.abstraction.MCRecipe;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.enums.MCRecipeType;
import com.laytonsmith.abstraction.enums.MCTone;
import com.laytonsmith.annotations.convert;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.CHLog;
import com.laytonsmith.core.Installer;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.Main;
import com.laytonsmith.core.MethodScriptCompiler;
import com.laytonsmith.core.MethodScriptComplete;
import com.laytonsmith.core.MethodScriptFileLocations;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Prefs;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.IVariable;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.Token;
import com.laytonsmith.core.constructs.Variable;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.profiler.ProfilePoint;
import com.laytonsmith.database.Profiles;
import com.laytonsmith.persistance.DataSourceException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is a command line implementation of the in game interpreter mode. This
 * should only be run while the server is stopped, as it has full access to
 * filesystem resources. Many things won't work as intended, but pure abstract
 * functions should still work fine.
 */
public class Interpreter {

	/**
	 * THIS MUST NEVER EVER EVER EVER EVER EVER EVER CHANGE. EVER.
	 *
	 * BAD THINGS WILL HAPPEN TO EVERYBODY YOU LOVE IF THIS IS CHANGED!
	 */
	private static final String INTERPRETER_INSTALLATION_LOCATION = "/usr/local/bin/mscript";
	static boolean multilineMode = false;
	static String script;
	private static Environment env;
	
	public static void startWithTTY(String file, List<String> args) throws IOException, DataSourceException, URISyntaxException, Profiles.InvalidProfileException{
		doStartup();
		try{
			File fromFile = new File(file).getCanonicalFile();
			execute(FileUtil.read(fromFile), args, fromFile);
		} catch(ConfigCompileException ex){
			ConfigRuntimeException.React(ex, null, null);
			System.out.println(TermColors.reset());
			System.exit(1);
		}
	}

	/**
	 * Starts the interpreter.
	 * @param args
	 * @throws IOException
	 * @throws DataSourceException
	 * @throws URISyntaxException 
	 */
	public static void start(List<String> args) throws IOException, DataSourceException, URISyntaxException, Profiles.InvalidProfileException {
		doStartup();
		Scanner scanner = new Scanner(System.in);
		if (System.console() != null) {
			pl(YELLOW + "You are now in cmdline interpreter mode. Type a dash (-) on a line by itself to exit, and >>> to enter"
				+ " multiline mode.\nMost Minecraft features will not work, and your working directory is the"
				+ " CommandHelper.jar directory, not the Server directory. Have fun!");
			p(BLUE + ":" + WHITE);
		}
		if (System.console() == null) {
			//We need to read in everything, it's basically in multiline mode
			StringBuilder script = new StringBuilder();
			String line = null;
			try {
				while ((line = scanner.nextLine()) != null) {
					script.append(line).append("\n");
				}
			} catch (NoSuchElementException e) {
				//Done
			}
			try {
				execute(script.toString(), args);
				System.out.print(TermColors.reset());
			} catch (ConfigCompileException ex) {
				ConfigRuntimeException.React(ex, null, null);
				System.out.print(TermColors.reset());
				System.exit(1);
			}

		}
		try {
			while (textLine(scanner.nextLine())) {
				if (System.console() != null) {
					p(BLUE + ":" + WHITE);
				}
			}
		} catch (NoSuchElementException e) {
			//End of file
		}
	}
	
	private static void doStartup() throws IOException, DataSourceException, URISyntaxException, Profiles.InvalidProfileException{
		MethodScriptFileLocations.getDefault().getCacheDirectory().mkdirs();
		ClassDiscoveryCache cdc = new ClassDiscoveryCache(MethodScriptFileLocations.getDefault().getCacheDirectory());
		cdc.setLogger(Logger.getLogger(Interpreter.class.getName()));
		ClassDiscovery.getDefaultInstance().setClassDiscoveryCache(cdc);
		ClassDiscovery.getDefaultInstance().addDiscoveryLocation(ClassDiscovery.GetClassContainer(Interpreter.class));
		//First, we need to initialize the convertor
		Implementation.setServerType(Implementation.Type.SHELL);
		Installer.Install(MethodScriptFileLocations.getDefault().getConfigDirectory());
		CHLog.initialize(MethodScriptFileLocations.getDefault().getConfigDirectory());
		//Next, we need to get the "installation location", so we won't spew config files everywhere
		env = Static.GenerateStandaloneEnvironment();
		if(Prefs.UseColors()){
			TermColors.EnableColors();
		} else {
			TermColors.DisableColors();
		}
	}

	public static boolean textLine(String line) throws IOException {
		if (line.equals("-")) {
			//Exit interpreter mode
			pl(YELLOW + "Now exiting interpreter mode" + reset());
			return false;
		} else if (line.equals(">>>")) {
			//Start multiline mode
			if (multilineMode) {
				pl(RED + "You are already in multiline mode!");
			} else {
				multilineMode = true;
				pl(YELLOW + "You are now in multiline mode. Type <<< on a line by itself to execute.");
				pl(":" + WHITE + ">>>");
			}
		} else if (line.equals("<<<")) {
			//Execute multiline
			pl(":" + WHITE + "<<<");
			multilineMode = false;
			try {
				execute(script, null);
				script = "";
			} catch (ConfigCompileException e) {
				ConfigRuntimeException.React(e, null, null);
			}
		} else {
			if (multilineMode) {
				//Queue multiline
				script = script + line + "\n";
			} else {
				try {
					//Execute single line
					execute(line, null);
				} catch (ConfigCompileException ex) {
					ConfigRuntimeException.React(ex, null, null);
				}
			}
		}
		return true;
	}

	public static void execute(String script, List<String> args) throws ConfigCompileException, IOException {
		execute(script, args, null);
	}
	public static void execute(String script, List<String> args, File fromFile) throws ConfigCompileException, IOException {
		if(fromFile == null){
			fromFile = new File("Interpreter");
		}
		ProfilePoint compile = env.getEnv(GlobalEnv.class).GetProfiler().start("Compilation", LogLevel.VERBOSE);
		ParseTree tree;
		try {
			List<Token> stream = MethodScriptCompiler.lex(script, fromFile, true);
			tree = MethodScriptCompiler.compile(stream);
		} finally {
			compile.stop();
		}
		Environment env = Environment.createEnvironment(Interpreter.env.getEnv(GlobalEnv.class));
		env.getEnv(GlobalEnv.class).SetCustom("cmdline", true);
		List<Variable> vars = null;
		if (args != null) {
			vars = new ArrayList<Variable>();
			//Build the @arguments variable, the $ vars, and $ itself. Note that
			//we have special handling for $0, that is the script name, like bash.
			//However, it doesn't get added to either $ or @arguments, due to the
			//uncommon use of it.
			StringBuilder finalArgument = new StringBuilder();
			CArray arguments = new CArray(Target.UNKNOWN);
			{
				//Set the $0 argument
				Variable v = new Variable("$0", "", Target.UNKNOWN);
				v.setVal(fromFile.toString());
				v.setDefault(fromFile.toString());
				vars.add(v);
			}
			for (int i = 0; i < args.size(); i++) {
				String arg = args.get(i);
				if (i > 1) {
					finalArgument.append(" ");
				}
				Variable v = new Variable("$" + Integer.toString(i + 1), "", Target.UNKNOWN);
				v.setVal(new CString(arg, Target.UNKNOWN));
				v.setDefault(arg);
				vars.add(v);
				finalArgument.append(arg);
				arguments.push(new CString(arg, Target.UNKNOWN));
			}
			Variable v = new Variable("$", "", false, true, Target.UNKNOWN);
			v.setVal(new CString(finalArgument.toString(), Target.UNKNOWN));
			v.setDefault(finalArgument.toString());
			vars.add(v);
			env.getEnv(GlobalEnv.class).GetVarList().set(new IVariable("@arguments", arguments, Target.UNKNOWN));
		}
		try {
			ProfilePoint p = Interpreter.env.getEnv(GlobalEnv.class).GetProfiler().start("Interpreter Script", LogLevel.ERROR);
			try {
				MethodScriptCompiler.execute(tree, env, new MethodScriptComplete() {
					public void done(String output) {
						//Do nothing
					}
				}, null, vars);
				env.getEnv(GlobalEnv.class).GetDaemonManager().waitForThreads();
			} finally {
				p.stop();
			}
		} catch (CancelCommandException e) {
			if (System.console() != null) {
				pl(":");
			}
		} catch (ConfigRuntimeException e) {
			ConfigRuntimeException.React(e, env);
			//No need for the full stack trace
			if(System.console() == null){
				System.exit(1);
			}
		} catch (NoClassDefFoundError e) {
			System.err.println(RED + Main.getNoClassDefFoundErrorMessage(e) + reset());
			System.err.println("Since you're running from standalone interpreter mode, this is not a fatal error, but one of the functions you just used required"
				+ " an actual backing engine that isn't currently loaded. (It still might fail even if you load the engine though.) You simply won't be"
				+ " able to use that function here.");
			if(System.console() == null){
				System.exit(1);
			}
		} catch (Exception e) {
			pl(RED + e.toString());
			e.printStackTrace();
			if(System.console() == null){
				System.exit(1);
			}
		}
	}

	public static void install() {
		if (TermColors.SYSTEM == TermColors.SYS.UNIX) {
			try {
				URL jar = Interpreter.class.getProtectionDomain().getCodeSource().getLocation();
				File exe = new File(INTERPRETER_INSTALLATION_LOCATION);
				String bashScript = Static.GetStringResource("/interpreter-helpers/bash.sh");
				try {
					bashScript = bashScript.replaceAll("%%LOCATION%%", jar.toURI().getPath());
				} catch (URISyntaxException ex) {
					ex.printStackTrace();
				}
				exe.createNewFile();
				if (!exe.canWrite()) {
					throw new IOException();
				}
				FileUtil.write(bashScript, exe);
				exe.setExecutable(true, false);
			} catch (IOException e) {
				System.err.println("Cannot install. You must run the command with sudo for it to succeed, however, did you do that?");
				return;
			}
		} else {
			System.err.println("Sorry, cmdline functionality is currently only supported on unix systems! Check back soon though!");
			return;
		}
		System.out.println("MethodScript has successfully been installed on your system. Note that you may need to rerun the install command"
			+ " if you change locations of the jar, or rename it. Be sure to put \"#!" + INTERPRETER_INSTALLATION_LOCATION + "\" at the top of all your scripts,"
			+ " if you wish them to be executable on unix systems, and set the execution bit with chmod +x <script name> on unix systems.");
		System.out.println("Try this script to test out the basic features of the scripting system:\n");
		System.out.println(Static.GetStringResource("/interpreter-helpers/sample.ms"));
	}

	public static void uninstall() {
		if (TermColors.SYSTEM == TermColors.SYS.UNIX) {
			try {
				File exe = new File(INTERPRETER_INSTALLATION_LOCATION);
				if (!exe.delete()) {
					throw new IOException();
				}
			} catch (IOException e) {
				System.err.println("Cannot uninstall. You must run the command with sudo for it to succeed, however, did you do that?");
				return;
			}
		} else {
			System.err.println("Sorry, cmdline functionality is currently only supported on unix systems! Check back soon though!");
			return;
		}
		System.out.println("MethodScript has been uninstalled from this system.");
	}
	
	@convert(type=Implementation.Type.SHELL)
	public static class ShellConvertor extends AbstractConvertor{
		
		RunnableQueue queue = new RunnableQueue("ShellInterpreter-userland");

		public MCLocation GetLocation(MCWorld w, double x, double y, double z, float yaw, float pitch) {
			throw new UnsupportedOperationException("This method is not supported from a shell.");
		}

		public Class GetServerEventMixin() {
			throw new UnsupportedOperationException("This method is not supported from a shell.");
		}

		public MCEnchantment[] GetEnchantmentValues() {
			throw new UnsupportedOperationException("This method is not supported from a shell.");
		}

		public MCEnchantment GetEnchantmentByName(String name) {
			throw new UnsupportedOperationException("This method is not supported from a shell.");
		}

		public MCServer GetServer() {
			throw new UnsupportedOperationException("This method is not supported from a shell.");
		}

		public MCItemStack GetItemStack(int type, int qty) {
			throw new UnsupportedOperationException("This method is not supported from a shell.");
		}

		public void Startup(CommandHelperPlugin chp) {
			
		}

		public int LookupItemId(String materialName) {
			throw new UnsupportedOperationException("This method is not supported from a shell.");
		}

		public String LookupMaterialName(int id) {
			throw new UnsupportedOperationException("This method is not supported from a shell.");
		}

		public MCItemStack GetItemStack(int type, int data, int qty) {
			throw new UnsupportedOperationException("This method is not supported from a shell.");
		}
		
		private static int runnableID = 0;
		private static List<Integer> runnableList = new ArrayList<Integer>();

		public int SetFutureRunnable(DaemonManager dm, final long ms, final Runnable r) {
			final int id = ++runnableID;
			Runnable m = new Runnable() {

				public void run() {
					try {
						Thread.sleep(ms);
					} catch (InterruptedException ex) {
						Logger.getLogger(Interpreter.class.getName()).log(Level.SEVERE, null, ex);
					}
					if(runnableList.contains(id)){
						r.run();
					}
				}
			};
			runnableList.add(id);
			queue.invokeLater(dm, m);
			return id;
		}

		public void ClearAllRunnables() {
			runnableList.clear();
		}

		public void ClearFutureRunnable(int id) {
			runnableList.remove(id);
		}

		public int SetFutureRepeater(DaemonManager dm, final long ms, final long initialDelay, final Runnable r) {
			final int id = runnableID++;
			Runnable m = new Runnable() {

				public void run() {
					try {
						Thread.sleep(initialDelay);
					} catch (InterruptedException ex) {
						Logger.getLogger(Interpreter.class.getName()).log(Level.SEVERE, null, ex);
					}
					while(runnableList.contains(id)){
						r.run();
						try {
							Thread.sleep(ms);
						} catch (InterruptedException ex) {
							Logger.getLogger(Interpreter.class.getName()).log(Level.SEVERE, null, ex);
						}
					}
				}
			};
			
			runnableList.add(id);
			queue.invokeLater(dm, m);
			return id;
		}

		public MCEntity GetCorrectEntity(MCEntity e) {
			throw new UnsupportedOperationException("This method is not supported from a shell.");
		}

		public MCInventory GetEntityInventory(int entityID) {
			throw new UnsupportedOperationException("This method is not supported from a shell.");
		}

		public MCInventory GetLocationInventory(MCLocation location) {
			throw new UnsupportedOperationException("This method is not supported from a shell.");
		}

		public MCNote GetNote(int octave, MCTone tone, boolean sharp) {
			throw new UnsupportedOperationException("This method is not supported from a shell.");
		}

		public synchronized int getMaxBlockID() {
			throw new UnsupportedOperationException("This method is not supported from a shell.");
		}

		public synchronized int getMaxItemID() {
			throw new UnsupportedOperationException("This method is not supported from a shell.");
		}

		public synchronized int getMaxRecordID() {
			throw new UnsupportedOperationException("This method is not supported from a shell.");
		}

		public MCColor GetColor(int red, int green, int blue) {
			throw new UnsupportedOperationException("This method is not supported from a shell.");
		}

		public MCFireworkBuilder GetFireworkBuilder() {
			throw new UnsupportedOperationException("This method is not supported from a shell.");
		}

		public MCPluginMeta GetPluginMeta() {
			throw new UnsupportedOperationException("This method is not supported from a shell.");
		}

		public MCMaterial getMaterial(int id) {
			throw new UnsupportedOperationException("This method is not supported from a shell.");
		}

		public MCItemMeta GetCorrectMeta(MCItemMeta im) {
			throw new UnsupportedOperationException("This method is not supported from a shell.");
		}

		public List<MCEntity> GetEntitiesAt(MCLocation loc, double radius) {
			throw new UnsupportedOperationException("This method is not supported from a shell.");
		}

		@Override
		public MCRecipe GetNewRecipe(MCRecipeType type, MCItemStack result) {
			throw new UnsupportedOperationException("This method is not supported from a shell.");
		}

		@Override
		public MCRecipe GetRecipe(MCRecipe unspecific) {
			throw new UnsupportedOperationException("This method is not supported from a shell.");
		}
	}
}
