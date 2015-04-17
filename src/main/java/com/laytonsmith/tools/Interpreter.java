package com.laytonsmith.tools;

import com.laytonsmith.PureUtilities.Common.FileUtil;
import com.laytonsmith.PureUtilities.Common.MutableObject;
import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.PureUtilities.LimitedQueue;
import com.laytonsmith.PureUtilities.RunnableQueue;
import com.laytonsmith.PureUtilities.SignalHandler;
import com.laytonsmith.PureUtilities.SignalType;
import com.laytonsmith.PureUtilities.Signals;
import com.laytonsmith.PureUtilities.TermColors;
import static com.laytonsmith.PureUtilities.TermColors.BLUE;
import static com.laytonsmith.PureUtilities.TermColors.RED;
import static com.laytonsmith.PureUtilities.TermColors.YELLOW;
import static com.laytonsmith.PureUtilities.TermColors.p;
import static com.laytonsmith.PureUtilities.TermColors.pl;
import static com.laytonsmith.PureUtilities.TermColors.reset;
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
import com.laytonsmith.abstraction.MCMetadataValue;
import com.laytonsmith.abstraction.MCNote;
import com.laytonsmith.abstraction.MCPlugin;
import com.laytonsmith.abstraction.MCPluginMeta;
import com.laytonsmith.abstraction.MCRecipe;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.enums.MCRecipeType;
import com.laytonsmith.abstraction.enums.MCTone;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.convert;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
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
import com.laytonsmith.core.constructs.CClosure;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.IVariable;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.Token;
import com.laytonsmith.core.constructs.Variable;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.environments.InvalidEnvironmentException;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventUtils;
import com.laytonsmith.core.events.drivers.CmdlineEvents;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.FunctionReturnException;
import com.laytonsmith.core.functions.FunctionBase;
import com.laytonsmith.core.functions.FunctionList;
import com.laytonsmith.core.profiler.ProfilePoint;
import com.laytonsmith.core.Profiles;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.functions.Exceptions;
import com.laytonsmith.persistence.DataSourceException;
import com.laytonsmith.tools.docgen.DocGenTemplates;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import jline.console.ConsoleReader;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.StringsCompleter;

/**
 * This is a command line implementation of the in game interpreter mode. This
 * should only be run while the server is stopped, as it has full access to
 * filesystem resources. Many things won't work as intended, but pure abstract
 * functions should still work fine.
 */
public final class Interpreter {

	/**
	 * THIS MUST NEVER EVER EVER EVER EVER EVER EVER CHANGE. EVER.
	 *
	 * BAD THINGS WILL HAPPEN TO EVERYBODY YOU LOVE IF THIS IS CHANGED!
	 */
	private static final String INTERPRETER_INSTALLATION_LOCATION = "/usr/local/bin/mscript";

	private boolean inTTYMode = false;
	private boolean multilineMode = false;
	private String script = "";
	private Environment env;
	private Thread scriptThread = null;

	private volatile boolean isExecuting = false;

	private final Queue<String> commandHistory = new LimitedQueue<>(MAX_COMMAND_HISTORY);

	/**
	 * If they mash ctrlC a bunch, they probably really want to quit, so we'll
	 * keep track of this, and reset it only if they then run an actual command.
	 */
	private volatile int ctrlCcount = 0;

	/**
	 * After this many mashes of Ctrl+C, clearly they want to exit, so we'll
	 * exit the shell.
	 */
	private static final int MAX_CTRL_C_MASHES = 5;

	/**
	 * Max commands that are tracked.
	 */
	private static final int MAX_COMMAND_HISTORY = 100;

	public static void startWithTTY(String file, List<String> args) throws IOException, DataSourceException, URISyntaxException, Profiles.InvalidProfileException {
		File fromFile = new File(file).getCanonicalFile();
		Interpreter interpreter = new Interpreter(args, fromFile.getParentFile().getPath(), true);
		try {
			interpreter.execute(FileUtil.read(fromFile), args, fromFile);
		} catch (ConfigCompileException ex) {
			ConfigRuntimeException.HandleUncaughtException(ex, null, null);
			System.out.println(TermColors.reset());
			System.exit(1);
		} catch(ConfigCompileGroupException ex){
			ConfigRuntimeException.HandleUncaughtException(ex, null);
			System.out.println(TermColors.reset());
			System.exit(1);
		}
	}

	private String getHelpMsg(){
		String msg = YELLOW + "You are now in cmdline interpreter mode. Use exit() to exit, and >>> to enter"
					+ " multiline mode.";
		try {
			msg += "\nYour current working directory is: " + env.getEnv(GlobalEnv.class).GetRootFolder().getCanonicalPath();
		} catch (IOException ex) {
			//
		}
		return msg;
	}

	/**
	 * Creates a new Interpreter object. This object can then be manipulated via
	 * the cmdline interactively, or standalone, via the execute method.
	 *
	 * @param args Any arguments passed in to the script. They are set as $vars
	 * @param cwd The initial working directory.
	 * @throws IOException
	 * @throws DataSourceException
	 * @throws URISyntaxException
	 */
	public Interpreter(List<String> args, String cwd) throws IOException, DataSourceException, URISyntaxException, Profiles.InvalidProfileException {
		this(args, cwd, false);
	}

	private Interpreter(List<String> args, String cwd, boolean inTTYMode) throws IOException, DataSourceException, URISyntaxException, Profiles.InvalidProfileException {
		doStartup();
		env.getEnv(GlobalEnv.class).SetRootFolder(new File(cwd));
		if(inTTYMode){
			//Ok, done. They'll have to execute from here.
			return;
		}
		//We have two modes here, piped input, or interactive console.
		if (System.console() == null) {
			Scanner scanner = new Scanner(System.in);
			//We need to read in everything, it's basically in multiline mode
			StringBuilder script = new StringBuilder();
			String line;
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
				ConfigRuntimeException.HandleUncaughtException(ex, null, null);
				System.out.print(TermColors.reset());
				System.exit(1);
			} catch(ConfigCompileGroupException ex){
				ConfigRuntimeException.HandleUncaughtException(ex, null);
				System.out.println(TermColors.reset());
				System.exit(1);
			}

		} else {
			final ConsoleReader reader = new ConsoleReader();
			reader.setExpandEvents(false);
			//Get a list of all the function names. This will be provided to the auto completer.
			Set<FunctionBase> functions = FunctionList.getFunctionList(api.Platforms.INTERPRETER_JAVA);
			List<String> names = new ArrayList<>();
			for(FunctionBase f : functions){
				if(f.appearInDocumentation()){
					names.add(f.getName());
				}
			}
			reader.addCompleter(new ArgumentCompleter(new ArgumentCompleter.AbstractArgumentDelimiter() {

				@Override
				public boolean isDelimiterChar(CharSequence buffer, int pos) {
					char c = buffer.charAt(pos);
					return !Character.isLetter(c) && c != '_';
				}
			}, new StringsCompleter(names){

				@Override
				public int complete(String buffer, int cursor, List<CharSequence> candidates) {
					//The autocomplete can be improved a bit, instead of putting a space after it,
					//let's put a parenthesis.
					int ret = super.complete(buffer, cursor, candidates);
					if(candidates.size() == 1){
						String functionName = candidates.get(0).toString().trim();
						candidates.set(0,  functionName + "()");
					}
					return ret;
				}

			}));
			while(true){
				String prompt;
				if(multilineMode){
					prompt = TermColors.WHITE + ">" + reset();
				} else {
					prompt = getPrompt();
				}
				String line = reader.readLine(prompt);
				if(!textLine(line)){
					break;
				}
			}

			//Perhaps this code will be revisited in the future, so that more things
			//can be done, like syntax highlighting, function keys, etc, but in order
			//to do that, history, command completion, etc, will all have to be re-implemented,
			//and implemented around readCharacter, which is a lot of work.
//			p(getPrompt());
//			boolean exit = false;
//			while(true){
//				jline.console.ConsoleReader reader = new jline.console.ConsoleReader();
//				StringBuilder line = new StringBuilder();
//				while(true){
//					int c = reader.readCharacter();
//					if(c == 27){
//						//Escape sequence
//						int c2 = reader.readCharacter();
//						if(c2 == 79){
//							//F1-F4
//							int c3 = reader.readCharacter();
//							if(c3 == 80){
//								//F1
//								System.out.println("F1");
//								continue;
//							} else if(c3 == 81){
//								//F2
//								System.out.println("F2");
//								continue;
//							} else if(c3 == 82){
//								//F3
//								System.out.println("F3");
//								continue;
//							} else if(c3 == 83){
//								//F4
//								System.out.println("F4");
//								continue;
//							}
//						} else if(c2 == 91){
//							//At least 3 characters
//							int c3 = reader.readCharacter();
//							if(c3 == 68){
//								//Left arrow
//								System.out.println("Left Arrow");
//								continue;
//							} else if(c3 == 65){
//								//Up Arrow
//								System.out.println("Up Arrow");
//								continue;
//							} else if(c3 == 66){
//								//Down Arrow
//								System.out.println("Down Arrow");
//								continue;
//							} else if(c3 == 67){
//								//Right Arrow
//								System.out.println("Right Arrow");
//								continue;
//							} else if(c3 == 72){
//								//Home
//								System.out.println("Home");
//								continue;
//							} else if(c3 == 70){
//								//End
//								System.out.println("End");
//								continue;
//							} else {
//								//At least 4 characters
//								int c4 = reader.readCharacter();
//								if(c4 == 126){
//									if(c3 == 50){
//										//Insert
//										System.out.println("Insert");
//										continue;
//									} else if(c3 == 51){
//										//Delete
//										System.out.println("Delete");
//										continue;
//									} else if(c3 == 53){
//										//Page Up
//										System.out.println("Page Up");
//										continue;
//									} else if(c3 == 54){
//										//Page Down
//										System.out.println("Page Down");
//										continue;
//									}
//								} else {
//									//At least 5 characters
//									int c5 = reader.readCharacter();
//									if(c5 == 126){
//										if(c3 == 49){
//											if(c4 == 53){
//												//F5
//												System.out.println("F5");
//												continue;
//											} else if(c4 == 55){
//												//F6
//												System.out.println("F6");
//												continue;
//											} else if(c4 == 56){
//												//F7
//												System.out.println("F7");
//												continue;
//											} else if(c4 == 57){
//												//F8
//												System.out.println("F8");
//												continue;
//											}
//										} else if(c3 == 50){
//											if(c4 == 48){
//												//F9
//												System.out.println("F9");
//												continue;
//											} else if(c4 == 49){
//												//F10
//												System.out.println("F10");
//												continue;
//											} else if(c4 == 51){
//												//F11
//												System.out.println("F11");
//												continue;
//											} else if(c4 == 52){
//												//F12
//												System.out.println("F12");
//												continue;
//											}
//										} else {
//											//Unknown
//											continue;
//										}
//									} else {
//										//Unknown. This hopefully won't ever happen.
//										continue;
//									}
//								}
//							}
//						} else {
//							continue; //Unrecognized. Hopefully this will be fine?
//						}
//					}
//					if(c == 13){ //"Enter" character
//						//done, send the line in for processing
//						System.out.println();
//						break;
//					}
//					if(c == 127){
//						reader.moveCursor(-1);
//					}
//					line.append((char)c);
//					reader.putString(Character.toString((char)c));
//				}
//				if(!textLine(line.toString())){
//					exit = true;
//				}
//				if(multilineMode){
//					p(">");
//				} else {
//					p(getPrompt());
//				}
//			}
		}
	}

	private String getPrompt(){
		CClosure c = (CClosure) env.getEnv(GlobalEnv.class).GetCustom("cmdline_prompt");
		if(c != null){
			try {
				c.execute();
			} catch(FunctionReturnException ex){
				String val = ex.getReturn().val();
				return Static.MCToANSIColors(val) + TermColors.RESET;
			} catch(ConfigRuntimeException ex){
				ConfigRuntimeException.HandleUncaughtException(ex, env);
			}
		}
		return BLUE + ":" + TermColors.RESET;
	}

	private void doStartup() throws IOException, DataSourceException, URISyntaxException, Profiles.InvalidProfileException {

		Installer.Install(MethodScriptFileLocations.getDefault().getConfigDirectory());
		Installer.InstallCmdlineInterpreter();

		env = Static.GenerateStandaloneEnvironment();
		env.getEnv(GlobalEnv.class).SetCustom("cmdline", true);
		if (Prefs.UseColors()) {
			TermColors.EnableColors();
		} else {
			TermColors.DisableColors();
		}

		String auto_include = FileUtil.read(MethodScriptFileLocations.getDefault().getCmdlineInterpreterAutoIncludeFile());
		try {
			MethodScriptCompiler.execute(auto_include, MethodScriptFileLocations.getDefault().getCmdlineInterpreterAutoIncludeFile(), true, env, null, null, null);
		} catch (ConfigCompileException ex) {
			ConfigRuntimeException.HandleUncaughtException(ex, "Interpreter will continue to run, however.", null);
		} catch (ConfigCompileGroupException ex){
			ConfigRuntimeException.HandleUncaughtException(ex, null);
		}
		//Install our signal handlers.
		SignalHandler.SignalCallback signalHandler = new SignalHandler.SignalCallback() {

			@Override
			public boolean handle(SignalType type) {
				if(isExecuting){
					env.getEnv(GlobalEnv.class).SetInterrupt(true);
					if(scriptThread != null){
						scriptThread.interrupt();
					}
					for(Thread t : env.getEnv(GlobalEnv.class).GetDaemonManager().getActiveThreads()){
						t.interrupt();
					}
				} else {
					ctrlCcount++;
					if(ctrlCcount > MAX_CTRL_C_MASHES){
						//Ok, ok, we get the hint.
						System.out.println();
						System.out.flush();
						System.exit(130); //Standard Ctrl+C exit code
					}
					pl(YELLOW + "\nUse exit() to exit the shell." + reset());
					p(getPrompt());
				}
				return true;
			}
		};
		try {
			SignalHandler.addHandler(Signals.SIGTERM, signalHandler);
		} catch(IllegalArgumentException ex){
			// Oh well.
		}
		try {
			SignalHandler.addHandler(Signals.SIGINT, signalHandler);
		} catch (IllegalArgumentException ex){
			// Oh well again.
		}
	}

	/**
	 * This evaluates each line of text
	 * @param line
	 * @return
	 * @throws IOException
	 */
	private boolean textLine(String line) throws IOException {
		switch (line) {
			case "-":
				//Exit interpreter mode
				return false;
			case ">>>":
				//Start multiline mode
				if (multilineMode) {
					pl(RED + "You are already in multiline mode!");
				} else {
					multilineMode = true;
					pl(YELLOW + "You are now in multiline mode. Type <<< on a line by itself to execute.");
				}	break;
			case "<<<":
				//Execute multiline
				multilineMode = false;
				try {
					execute(script, null);
					script = "";
				} catch (ConfigCompileException e) {
					ConfigRuntimeException.HandleUncaughtException(e, null, null);
				} catch(ConfigCompileGroupException e){
					ConfigRuntimeException.HandleUncaughtException(e, null);
				}
				break;
			default:
				if (multilineMode) {
					//Queue multiline
					script = script + line + "\n";
				} else {
					try {
						//Execute single line
						execute(line, null);
					} catch (ConfigCompileException ex) {
						ConfigRuntimeException.HandleUncaughtException(ex, null, null);
					} catch(ConfigCompileGroupException ex){
						ConfigRuntimeException.HandleUncaughtException(ex, null);
					}
				}	break;
		}
		return true;
	}

	/**
	 * This executes a script
	 * @param script
	 * @param args
	 * @throws ConfigCompileException
	 * @throws IOException
	 */
	public void execute(String script, List<String> args) throws ConfigCompileException, IOException, ConfigCompileGroupException {
		execute(script, args, null);
	}

	/**
	 * This executes an entire script. The cmdline_prompt_event is first triggered (if used) and
	 * if the event is cancelled, nothing happens.
	 * @param script
	 * @param args
	 * @param fromFile
	 * @throws ConfigCompileException
	 * @throws IOException
	 */
	public void execute(String script, List<String> args, File fromFile) throws ConfigCompileException, IOException, ConfigCompileGroupException {
		CmdlineEvents.cmdline_prompt_input.CmdlinePromptInput input = new CmdlineEvents.cmdline_prompt_input.CmdlinePromptInput(script);
		EventUtils.TriggerListener(Driver.CMDLINE_PROMPT_INPUT, "cmdline_prompt_input", input);
		if(input.isCancelled()){
			return;
		}
		ctrlCcount = 0;
		if("exit".equals(script)){
			pl(YELLOW + "Use exit() if you wish to exit.");
			return;
		}
		if("help".equals(script)){
			pl(getHelpMsg());
			return;
		}
		if (fromFile == null) {
			fromFile = new File("Interpreter");
		}
		isExecuting = true;
		ProfilePoint compile = env.getEnv(GlobalEnv.class).GetProfiler().start("Compilation", LogLevel.VERBOSE);
		final ParseTree tree;
		try {
			List<Token> stream = MethodScriptCompiler.lex(script, fromFile, true);
			tree = MethodScriptCompiler.compile(stream);
		} finally {
			compile.stop();
		}
		//Environment env = Environment.createEnvironment(this.env.getEnv(GlobalEnv.class));
		final List<Variable> vars = new ArrayList<>();
		if (args != null) {
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
			env.getEnv(GlobalEnv.class).GetVarList().set(new IVariable(new CClassType("array", Target.UNKNOWN), "@arguments", arguments, Target.UNKNOWN));
		}
		try {
			ProfilePoint p = this.env.getEnv(GlobalEnv.class).GetProfiler().start("Interpreter Script", LogLevel.ERROR);
			try {
				final MutableObject<Throwable> wasThrown = new MutableObject<>();
				scriptThread = new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							MethodScriptCompiler.execute(tree, env, new MethodScriptComplete() {
								@Override
								public void done(String output) {
									if(System.console() != null && !"".equals(output.trim())){
										System.out.println(output);
									}
								}
							}, null, vars);
						} catch (CancelCommandException e) {
							//Nothing, though we could have been Ctrl+C cancelled, so we need to reset
							//the interrupt flag. But we do that unconditionally below, in the finally,
							//in the other thread.
						} catch (ConfigRuntimeException e) {
							ConfigRuntimeException.HandleUncaughtException(e, env);
							//No need for the full stack trace
							if (System.console() == null) {
								System.exit(1);
							}
						} catch (NoClassDefFoundError e) {
							System.err.println(RED + Main.getNoClassDefFoundErrorMessage(e) + reset());
							System.err.println("Since you're running from standalone interpreter mode, this is not a fatal error, but one of the functions you just used required"
									+ " an actual backing engine that isn't currently loaded. (It still might fail even if you load the engine though.) You simply won't be"
									+ " able to use that function here.");
							if (System.console() == null) {
								System.exit(1);
							}
						} catch (InvalidEnvironmentException ex) {
							System.err.println(RED + ex.getMessage() + " " + ex.getData() + "() cannot be used in this context.");
							if (System.console() == null) {
								System.exit(1);
							}
						} catch (RuntimeException e) {
							pl(RED + e.toString());
							e.printStackTrace(System.err);
							if (System.console() == null) {
								System.exit(1);
							}
						}
					}
				}, "MethodScript-Main");
				scriptThread.start();
				try {
					scriptThread.join();
				} catch (InterruptedException ex) {
					//
				}
				try {
					env.getEnv(GlobalEnv.class).GetDaemonManager().waitForThreads();
				} catch (InterruptedException ex) {
					//
				}
			} finally {
				p.stop();
			}
		} finally {
			env.getEnv(GlobalEnv.class).SetInterrupt(false);
			isExecuting = false;
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
				File manDir = new File("/usr/local/man/man1");
				if (manDir.exists()) {
					//Don't do this installation if the man pages aren't already there.
					String manPage = Static.GetStringResource("/interpreter-helpers/manpage");
					manPage = DocGenTemplates.DoTemplateReplacement(manPage, DocGenTemplates.GetGenerators());
					File manPageFile = new File(manDir, "mscript.1");
					FileUtil.write(manPage, manPageFile);
				}
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

	@convert(type = Implementation.Type.SHELL)
	public static class ShellConvertor extends AbstractConvertor {

		RunnableQueue queue = new RunnableQueue("ShellInterpreter-userland");

		@Override
		public MCLocation GetLocation(MCWorld w, double x, double y, double z, float yaw, float pitch) {
			throw new UnsupportedOperationException("This method is not supported from a shell.");
		}

		@Override
		public Class GetServerEventMixin() {
			return ShellEventMixin.class;
		}

		@Override
		public MCEnchantment[] GetEnchantmentValues() {
			throw new UnsupportedOperationException("This method is not supported from a shell.");
		}

		@Override
		public MCEnchantment GetEnchantmentByName(String name) {
			throw new UnsupportedOperationException("This method is not supported from a shell.");
		}

		@Override
		public MCServer GetServer() {
			throw new UnsupportedOperationException("This method is not supported from a shell.");
		}

		@Override
		public MCItemStack GetItemStack(int type, int qty) {
			throw new UnsupportedOperationException("This method is not supported from a shell.");
		}

		@Override
		public MCItemStack GetItemStack(int type, int data, int qty) {
			throw new UnsupportedOperationException("This method is not supported from a shell.");
		}

		@Override
		public MCItemStack GetItemStack(MCMaterial type, int qty) {
			throw new UnsupportedOperationException("This method is not supported from a shell.");
		}

		@Override
		public MCItemStack GetItemStack(MCMaterial type, int data, int qty) {
			throw new UnsupportedOperationException("This method is not supported from a shell.");
		}

		@Override
		public MCItemStack GetItemStack(String type, int qty) {
			throw new UnsupportedOperationException("This method is not supported from a shell.");
		}

		@Override
		public MCItemStack GetItemStack(String type, int data, int qty) {
			throw new UnsupportedOperationException("This method is not supported from a shell.");
		}

		@Override
		public void Startup(CommandHelperPlugin chp) {

		}

		@Override
		public int LookupItemId(String materialName) {
			throw new UnsupportedOperationException("This method is not supported from a shell.");
		}

		@Override
		public String LookupMaterialName(int id) {
			throw new UnsupportedOperationException("This method is not supported from a shell.");
		}

		@Override
		public MCEntity GetCorrectEntity(MCEntity e) {
			throw new UnsupportedOperationException("This method is not supported from a shell.");
		}

		@Override
		public MCInventory GetEntityInventory(int entityID) {
			throw new UnsupportedOperationException("This method is not supported from a shell.");
		}

		@Override
		public MCInventory GetLocationInventory(MCLocation location) {
			throw new UnsupportedOperationException("This method is not supported from a shell.");
		}

		@Override
		public MCNote GetNote(int octave, MCTone tone, boolean sharp) {
			throw new UnsupportedOperationException("This method is not supported from a shell.");
		}

		@Override
		public synchronized int getMaxBlockID() {
			throw new UnsupportedOperationException("This method is not supported from a shell.");
		}

		@Override
		public synchronized int getMaxItemID() {
			throw new UnsupportedOperationException("This method is not supported from a shell.");
		}

		@Override
		public synchronized int getMaxRecordID() {
			throw new UnsupportedOperationException("This method is not supported from a shell.");
		}

		@Override
		public MCColor GetColor(int red, int green, int blue) {
			throw new UnsupportedOperationException("This method is not supported from a shell.");
		}

		@Override
		public MCFireworkBuilder GetFireworkBuilder() {
			throw new UnsupportedOperationException("This method is not supported from a shell.");
		}

		@Override
		public MCPluginMeta GetPluginMeta() {
			throw new UnsupportedOperationException("This method is not supported from a shell.");
		}

		@Override
		public MCMaterial getMaterial(int id) {
			throw new UnsupportedOperationException("This method is not supported from a shell.");
		}

		@Override
		public MCItemMeta GetCorrectMeta(MCItemMeta im) {
			throw new UnsupportedOperationException("This method is not supported from a shell.");
		}

		@Override
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

		@Override
		public MCMaterial GetMaterial(String name) {
			throw new UnsupportedOperationException("This method is not supported from a shell.");
		}

		@Override
		public MCMetadataValue GetMetadataValue(Object value, MCPlugin plugin) {
			throw new UnsupportedOperationException("This method is not supported from a shell.");
		}

		@Override
		public String GetPluginName() {
			return "MethodScript";
		}

		@Override
		public MCPlugin GetPlugin() {
			throw new UnsupportedOperationException("This method is not supported from a shell.");
		}

		@Override
		public MCColor GetColor(String colorName, Target t) throws Exceptions.FormatException {
			throw new UnsupportedOperationException("Not supported yet.");
		}
	}

}
