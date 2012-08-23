package com.laytonsmith.tools;

import com.laytonsmith.PureUtilities.FileUtility;
import com.laytonsmith.PureUtilities.TermColors;
import static com.laytonsmith.PureUtilities.TermColors.*;
import com.laytonsmith.abstraction.Convertor;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.MCEnchantment;
import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.annotations.convert;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.Env;
import com.laytonsmith.core.GenericTreeNode;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.Main;
import com.laytonsmith.core.MethodScriptCompiler;
import com.laytonsmith.core.MethodScriptComplete;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Prefs;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.Threader;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.IVariable;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.Token;
import com.laytonsmith.core.constructs.Variable;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.profiler.ProfilePoint;
import com.laytonsmith.core.profiler.Profiler;
import com.laytonsmith.persistance.DataSourceException;
import com.laytonsmith.persistance.SerializedPersistance;
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
 *
 * @author layton
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
	private static File jarLocation;
	private static Profiler profiler;

	public static void start(List<String> args) throws IOException {
		//First, we need to initialize the convertor
		Implementation.setServerType(Implementation.Type.SHELL);
		//Next, we need to get the "installation location", so we won't spew config files everywhere
		jarLocation = new File(Interpreter.class.getProtectionDomain().getCodeSource().getLocation().getFile()).getParentFile();
		Prefs.init(new File(jarLocation, "CommandHelper/preferences.txt"));
		profiler = new Profiler(new File(jarLocation, "CommandHelper/profiler.config"));
//		try {
//			Env env = new Env();
//			env.SetProfiler(profiler);
//			MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex("player()", null)), env, null, null);
//		} catch (ConfigCompileException ex) {
//		}
		try {
			Static.persist = new SerializedPersistance(new File(jarLocation, "CommandHelper/persistance.ser"));
		} catch (DataSourceException ex) {
			Logger.getLogger(Interpreter.class.getName()).log(Level.SEVERE, null, ex);
		}
		if (TermColors.SYSTEM == TermColors.SYS.WINDOWS) {
			TermColors.DisableColors();
		}
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
				System.exit(0);
			} catch (ConfigCompileException ex) {
				ConfigRuntimeException.DoReport(ex, null, null);
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
				ConfigRuntimeException.DoReport(e, null, null);
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
					ConfigRuntimeException.DoReport(ex, null, null);
				}
			}
		}
		return true;
	}

	public static void execute(String script, List<String> args) throws ConfigCompileException, IOException {
		ProfilePoint compile = profiler.start("Compilation", LogLevel.VERBOSE);
		List<Token> stream = MethodScriptCompiler.lex(script, new File("Interpreter"));
		ParseTree tree = MethodScriptCompiler.compile(stream);
		compile.stop();
		Env env = new Env();
		env.SetPlayer(null);
		env.SetLabel("*");
		env.SetProfiler(profiler);
		env.SetCustom("cmdline", true);
		List<Variable> vars = null;
		if (args != null) {
			vars = new ArrayList<Variable>();
			//Build the @arguments variable, the $ vars, and $ itself. Note that
			//we have special handling for $0, that is the script name, like bash.
			//However, it doesn't get added to either $ or @arguments, due to the
			//uncommon use of it.
			StringBuilder finalArgument = new StringBuilder();
			CArray arguments = new CArray(Target.UNKNOWN);
			for (int i = 0; i < args.size(); i++) {
				String arg = args.get(i);
				if (i > 1) {
					finalArgument.append(" ");
				}
				Variable v = new Variable("$" + Integer.toString(i), "", Target.UNKNOWN);
				v.setVal(Static.resolveConstruct(arg, Target.UNKNOWN));
				vars.add(v);
				if (i != 0) {
					finalArgument.append(arg);
					arguments.push(Static.resolveConstruct(arg, Target.UNKNOWN));
				}
			}
			Variable v = new Variable("$", "", false, true, Target.UNKNOWN);
			v.setVal(new CString(finalArgument.toString(), Target.UNKNOWN));
			vars.add(v);
			env.GetVarList().set(new IVariable("@arguments", arguments, Target.UNKNOWN));
		}
		try {
			ProfilePoint p = profiler.start("Interpreter Script", LogLevel.ERROR);
			MethodScriptCompiler.execute(tree, env, new MethodScriptComplete() {
				public void done(String output) {
					output = output.trim();
					if (output.isEmpty()) {
						if (System.console() != null) {
							pl(":");
						}
					} else {
						if (output.startsWith("/")) {
							//Run the command
							pl((System.console() != null ? ":" + YELLOW : "") + output);
						} else {
							//output the results
							pl((System.console() != null ? ":" + GREEN : "") + output);
						}
					}
				}
			}, null, vars);
			p.stop();
		} catch (CancelCommandException e) {
			if (System.console() != null) {
				pl(":");
			}
		} catch (ConfigRuntimeException e) {
			ConfigRuntimeException.DoReport(e);
			//No need for the full stack trace        
		} catch (Exception e) {
			pl(RED + e.toString());
			e.printStackTrace();
		} catch (NoClassDefFoundError e) {
			System.err.println(RED + Main.getNoClassDefFoundErrorMessage(e) + reset());
			System.err.println("Since you're running from standalone interpreter mode, this is not a fatal error, but one of the functions you just used required"
				+ " an actual backing engine that isn't currently loaded. (It still might fail even if you load the engine though.) You simply won't be"
				+ " able to use that function here.");
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
				FileUtility.write(bashScript, exe);
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
	public static class ShellConvertor implements Convertor{

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

		public int SetFutureRunnable(final long ms, final Runnable r) {
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
			Threader.GetThreader().submit(m);
			return id;
		}

		public void ClearAllRunnables() {
			runnableList.clear();
		}

		public void ClearFutureRunnable(int id) {
			runnableList.remove(id);
		}

		public int SetFutureRepeater(final long ms, final long initialDelay, final Runnable r) {
			final int id = ++runnableID;
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
			Threader.GetThreader().submit(m);
			return id;
		}

		public MCEntity GetCorrectEntity(MCEntity e) {
			throw new UnsupportedOperationException("This method is not supported from a shell.");
		}
		
	}
}
