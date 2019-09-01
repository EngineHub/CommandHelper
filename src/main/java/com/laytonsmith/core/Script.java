package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.SimpleVersion;
import com.laytonsmith.PureUtilities.TermColors;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.compiler.TokenStream;
import com.laytonsmith.core.constructs.CClosure;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Command;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Construct.ConstructType;
import com.laytonsmith.core.constructs.IVariable;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.Token;
import com.laytonsmith.core.constructs.Token.TType;
import com.laytonsmith.core.constructs.Variable;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.environments.InvalidEnvironmentException;
import com.laytonsmith.core.exceptions.CRE.AbstractCREException;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREInsufficientPermissionException;
import com.laytonsmith.core.exceptions.CRE.CREInvalidProcedureException;
import com.laytonsmith.core.exceptions.CRE.CREStackOverflowError;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.FunctionReturnException;
import com.laytonsmith.core.exceptions.LoopBreakException;
import com.laytonsmith.core.exceptions.LoopContinueException;
import com.laytonsmith.core.exceptions.ProgramFlowManipulationException;
import com.laytonsmith.core.exceptions.StackTraceManager;
import com.laytonsmith.core.extensions.Extension;
import com.laytonsmith.core.extensions.ExtensionManager;
import com.laytonsmith.core.extensions.ExtensionTracker;
import com.laytonsmith.core.functions.Function;
import com.laytonsmith.core.functions.FunctionBase;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.core.profiler.ProfilePoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A script is a section of code that has been preprocessed and split into separate commands/actions. For instance, the
 * config script:
 *
 * /command = /cmd
 *
 * /test = /test
 *
 * would be two separate scripts, the first being the /command, and the second being /test. Certain key information is
 * stored in the Script class. First, the information needed to see if a target string should trigger this script.
 * Secondly, the default values of any variables, and thirdly, the unparsed tree for the right side of the script.
 */
public class Script {

	// See set_debug_output()
	public static boolean debugOutput = false;

	private List<Token> left;
	private List<List<Token>> right;
	private List<Token> fullRight;
	private List<Construct> cleft;
	private List<ParseTree> cright;
	private boolean nolog = false;
	//This should be null if we are running in non-alias mode
	private Map<String, Variable> leftVars;
	boolean hasBeenCompiled = false;
	boolean compilerError = false;
	private final long compileTime;
	private String label;
	private Environment currentEnv;
	private Set<Class<? extends Environment.EnvironmentImpl>> envs;
	private FileOptions fileOptions;

	private static final SimpleVersion GARBAGE_VERSION = new SimpleVersion(0, 0, 0, "version-error");

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		for(Token t : left) {
			b.append(t.val()).append(" ");
		}
		b.append("; compiled: ").append(hasBeenCompiled).append("; errors? ").append(compilerError);
		return b.toString();
	}

	private Procedure getProc(String name) {
		return currentEnv.getEnv(GlobalEnv.class).GetProcs().get(name);
	}

	public Environment getCurrentEnv() {
		return currentEnv;
	}

	public String getLabel() {
		return label;
	}

	/**
	 * Returns what would normally be on the left side on an alias ie. in config.msa
	 *
	 * @return label:/alias arg [ optionalArg ]
	 */
	public String getSignature() {
		StringBuilder b = new StringBuilder();
		b.append(getLabel()).append(":");
		for(Token t : left) {
			b.append(t.val()).append(" ");
		}
		return b.toString();
	}

	public Script(List<Token> left, List<Token> right, String label,
			Set<Class<? extends Environment.EnvironmentImpl>> envs, FileOptions fileOptions) {
		this.left = left;
		this.fullRight = right;
		this.leftVars = new HashMap<>();
		this.label = label;
		this.envs = envs;
		compileTime = System.currentTimeMillis();
		this.fileOptions = fileOptions;
	}

	private Script() {
		compileTime = System.currentTimeMillis();
	}

	public long getCompileTime() {
		return compileTime;
	}

	public static Script GenerateScript(ParseTree tree, String label) {
		Script s = new Script();

		s.hasBeenCompiled = true;
		s.compilerError = false;
		s.cright = new ArrayList<>();
		s.cright.add(tree);
		s.label = label;

		return s;
	}

	public boolean uncompilable() {
		return compilerError;
	}

	public void run(final List<Variable> vars, Environment myEnv, final MethodScriptComplete done) {
		//Some things, such as the label are determined at compile time
		this.currentEnv = myEnv;
		this.currentEnv.getEnv(GlobalEnv.class).SetLabel(this.label);
		MCCommandSender p = myEnv.getEnv(CommandHelperEnvironment.class).GetCommandSender();
		if(!hasBeenCompiled || compilerError) {
			Target target = Target.UNKNOWN;
			if(left.size() >= 1) {
				try {
					target = new Target(left.get(0).lineNum, left.get(0).file, left.get(0).column);
				} catch (NullPointerException e) {
					//Oh well, we tried to get more information
				}
			}
			throw ConfigRuntimeException.CreateUncatchableException("Unable to run command, script not yet compiled,"
					+ " or a compiler error occurred for that command. To see the compile error, run /reloadaliases", target);
		}
		enforceLabelPermissions();

		try {
			for(ParseTree rootNode : cright) {
				if(rootNode == null) {
					continue;
				}
				for(Mixed tempNode : rootNode.getAllData()) {
					if(tempNode instanceof Variable) {
						if(leftVars == null) {
							throw ConfigRuntimeException.CreateUncatchableException("$variables may not be used in this context."
									+ " Only @variables may be.", tempNode.getTarget());
						}
						Construct c = Static.resolveDollarVar(leftVars.get(((Variable) tempNode).getVariableName()), vars);
						((Variable) tempNode).setVal(new CString(c.toString(), tempNode.getTarget()));
					}
				}

				MethodScriptCompiler.registerAutoIncludes(currentEnv, this);
				MethodScriptCompiler.execute(rootNode, currentEnv, done, this);
			}
		} catch (ConfigRuntimeException ex) {
			//We don't know how to handle this really, so let's pass it up the chain.
			throw ex;
		} catch (CancelCommandException e) {
			//p.sendMessage(e.getMessage());
			//The message in the exception is actually empty
		} catch (LoopBreakException e) {
			if(p != null) {
				p.sendMessage("The break() function must be used inside a for() or foreach() loop");
			}
			StreamUtils.GetSystemOut().println("The break() function must be used inside a for() or foreach() loop");
		} catch (LoopContinueException e) {
			if(p != null) {
				p.sendMessage("The continue() function must be used inside a for() or foreach() loop");
			}
			StreamUtils.GetSystemOut().println("The continue() function must be used inside a for() or foreach() loop");
		} catch (FunctionReturnException e) {
			if(myEnv.getEnv(GlobalEnv.class).GetEvent() != null) {
				//Oh, we're running in an event handler. Those know how to catch it too.
				throw e;
			}
			if(p != null) {
				p.sendMessage("The return() function must be used inside a procedure.");
			}
			StreamUtils.GetSystemOut().println("The return() function must be used inside a procedure.");
		} catch (Throwable t) {
			StreamUtils.GetSystemOut().println("An unexpected exception occurred during the execution of a script.");
			t.printStackTrace();
			if(p != null) {
				p.sendMessage("An unexpected exception occurred during the execution of your script."
						+ " Please check the console for more information.");
			}
		}
		if(done != null) {
			done.done(null);
		}
	}

	/**
	 * Runs eval on the code tree, and if it returns an ival, resolves it.
	 *
	 * @param c
	 * @param env
	 * @return
	 */
	public Mixed seval(ParseTree c, final Environment env) {
		Mixed ret = eval(c, env);
		while(ret instanceof IVariable) {
			IVariable cur = (IVariable) ret;
			ret = env.getEnv(GlobalEnv.class).GetVarList().get(cur.getVariableName(), cur.getTarget(), env).ival();
		}
		return ret;
	}

	/**
	 * Given the parse tree and environment, executes the tree.
	 *
	 * @param c
	 * @param env
	 * @return
	 * @throws CancelCommandException
	 */
	@SuppressWarnings("UseSpecificCatch")
	public Mixed eval(ParseTree c, final Environment env) throws CancelCommandException {
		if(env.getEnv(GlobalEnv.class).IsInterrupted()) {
			//First things first, if we're interrupted, kill the script
			//unconditionally.
			throw new CancelCommandException("", Target.UNKNOWN);
		}

		final Mixed m = c.getData();
		currentEnv = env;
		if(m instanceof Construct) {
			Construct co = (Construct) m;
			if(co.getCType() != ConstructType.FUNCTION) {
				if(co.getCType() == ConstructType.VARIABLE) {
					return new CString(m.val(), m.getTarget());
				} else {
					return m;
				}
			}
		}

		final CFunction possibleFunction;
		try {
			possibleFunction = (CFunction) m;
		} catch (ClassCastException e) {
			throw ConfigRuntimeException.CreateUncatchableException("Expected to find CFunction at runtime but found: "
					+ m.val(), m.getTarget());
		}

		StackTraceManager stManager = env.getEnv(GlobalEnv.class).GetStackTraceManager();
		boolean addedRootStackElement = false;
		try {
			// If it's an unknown target, this is not user generated code, and we want to skip adding the element here.
			if(stManager.isStackEmpty() && !m.getTarget().equals(Target.UNKNOWN)) {
				stManager.addStackTraceElement(new ConfigRuntimeException.StackTraceElement("<<main code>>", m.getTarget()));
				addedRootStackElement = true;
			}
			stManager.setCurrentTarget(c.getTarget());
			env.getEnv(GlobalEnv.class).SetScript(this);

			if(possibleFunction.hasProcedure()) {
				//Not really a function, so we can't put it in Function.
				Procedure p = getProc(m.val());
				if(p == null) {
					throw new CREInvalidProcedureException("Unknown procedure \"" + m.val() + "\"", m.getTarget());
				}
				ProfilePoint pp = env.getEnv(GlobalEnv.class).GetProfiler().start(m.val() + " execution", LogLevel.INFO);
				Mixed ret;
				try {
					if(debugOutput) {
						doDebugOutput(p.getName(), c.getChildren());
					}
					ret = p.cexecute(c.getChildren(), env, m.getTarget());
				} finally {
					pp.stop();
				}
				return ret;
			} else if(possibleFunction.hasIVariable()) {
				//Check if this ivar is a closure and execute it
				Mixed closure = env.getEnv(GlobalEnv.class).GetVarList().get(m.val(), m.getTarget(), env).ival();
				if(!closure.isInstanceOf(CClosure.TYPE)) {
					throw new CRECastException("Expecting variable to contain a closure to execute, but found type: "
							+ closure.typeof().getSimpleName(), m.getTarget());
				}
				Mixed[] list = new Mixed[c.numberOfChildren()];
				for(int i = 0; i < c.numberOfChildren(); i++) {
					list[i] = env.getEnv(GlobalEnv.class).GetScript().seval(c.getChildAt(i), env);
				}
				return ((CClosure) closure).executeCallable(list);
			}

			final Function f;
			try {
				f = possibleFunction.getFunction();
			} catch (ConfigCompileException e) {
				//Turn it into a config runtime exception. This shouldn't ever happen though.
				throw ConfigRuntimeException.CreateUncatchableException("Unable to find function at runtime: "
						+ m.val(), m.getTarget());
			}

			ArrayList<Mixed> args = new ArrayList<>();
			try {
				if(f.isRestricted() && !Static.hasCHPermission(f.getName(), env)) {
					throw new CREInsufficientPermissionException("You do not have permission to use the "
							+ f.getName() + " function.", m.getTarget());
				}

				if(debugOutput) {
					doDebugOutput(f.getName(), c.getChildren());
				}
				if(f.useSpecialExec()) {
					ProfilePoint p = null;
					if(f.shouldProfile() && env.getEnv(GlobalEnv.class).GetProfiler() != null
							&& env.getEnv(GlobalEnv.class).GetProfiler().isLoggable(f.profileAt())) {
						p = env.getEnv(GlobalEnv.class).GetProfiler().start(f.profileMessageS(c.getChildren()), f.profileAt());
					}
					Mixed ret;
					try {
						ret = f.execs(m.getTarget(), env, this, c.getChildren().toArray(new ParseTree[]{}));
					} finally {
						if(p != null) {
							p.stop();
						}
					}
					return ret;
				}

				for(ParseTree c2 : c.getChildren()) {
					args.add(eval(c2, env));
				}
				Object[] a = args.toArray();
				Mixed[] ca = new Mixed[a.length];
				for(int i = 0; i < a.length; i++) {
					ca[i] = (Mixed) a[i];
					// TODO: This code is outdated and needs to be rethought.
					//CArray, CBoolean, CDouble, CInt, CNull, CString, CVoid, CEntry, CLabel (only to sconcat).
//					if(!(ca[i].isInstanceOf(CArray.class) || ca[i] instanceof CBoolean || ca[i] instanceof CDouble
//							|| ca[i] instanceof CInt || ca[i] instanceof CNull
//							|| ca[i] instanceof CString || ca[i] instanceof CVoid
//							|| ca[i] instanceof IVariable || ca[i] instanceof CEntry || ca[i] instanceof CLabel)
//							&& (!f.getName().equals("__autoconcat__") && (ca[i] instanceof CLabel))) {
//						throw new CRECastException("Invalid Mixed ("
//								+ ca[i].getClass() + ") being passed as an argument to a function ("
//								+ f.getName() + ")", m.getTarget());
//					}
					while(f.preResolveVariables() && ca[i] instanceof IVariable) {
						IVariable cur = (IVariable) ca[i];
						ca[i] = env.getEnv(GlobalEnv.class).GetVarList().get(cur.getVariableName(), cur.getTarget(),
								env).ival();
					}
				}

				{
					//It takes a moment to generate the toString of some things, so lets not do it
					//if we actually aren't going to profile
					ProfilePoint p = null;
					if(f.shouldProfile() && env.getEnv(GlobalEnv.class).GetProfiler() != null
							&& env.getEnv(GlobalEnv.class).GetProfiler().isLoggable(f.profileAt())) {
						p = env.getEnv(GlobalEnv.class).GetProfiler().start(f.profileMessage(ca), f.profileAt());
					}
					Mixed ret;
					try {
						ret = f.exec(m.getTarget(), env, ca);
					} finally {
						if(p != null) {
							p.stop();
						}
					}
					return ret;
				}
				//We want to catch and rethrow the ones we know how to catch, and then
				//catch and report anything else.
			} catch (ConfigRuntimeException | ProgramFlowManipulationException e) {
				if(e instanceof AbstractCREException) {
					((AbstractCREException) e).freezeStackTraceElements(stManager);
				}
				throw e;
			} catch (InvalidEnvironmentException e) {
				if(!e.isDataSet()) {
					e.setData(f.getName());
				}
				throw e;
			} catch (StackOverflowError e) {
				// This handles this in all cases that weren't previously considered. But it still should
				// be individually handled by other cases to ensure that the stack trace is more correct
				throw new CREStackOverflowError(null, c.getTarget(), e);
			} catch (Throwable e) {
				if(e instanceof ThreadDeath) {
					// Bail quickly in this case
					throw e;
				}
				String brand = Implementation.GetServerType().getBranding();
				SimpleVersion version;

				try {
					version = Static.getVersion();
				} catch (Throwable ex) {
					// This failing should not be a dealbreaker, so fill it with default data
					version = GARBAGE_VERSION;
				}

				String culprit = brand;
				outer:
				for(ExtensionTracker tracker : ExtensionManager.getTrackers().values()) {
					for(FunctionBase b : tracker.getFunctions()) {
						if(b.getName().equals(f.getName())) {
							//This extension provided the function, so its the culprit. Report this
							//name instead of the core plugin's name.
							for(Extension extension : tracker.getExtensions()) {
								culprit = extension.getName();
								break outer;
							}
						}
					}
				}

				String emsg = TermColors.RED + "Uh oh! You've found an error in " + TermColors.CYAN + culprit + TermColors.RED + ".\n"
						+ "This happened while running your code, so you may be able to find a workaround,"
						+ (!(e instanceof Exception) ? " (though since this is an Error, maybe not)" : "")
						+ " but is ultimately an issue in " + culprit + ".\n"
						+ "The following code caused the error:\n" + TermColors.WHITE;

				List<String> args2 = new ArrayList<>();
				Map<String, String> vars = new HashMap<>();
				for(Mixed cc : args) {
					if(cc instanceof IVariable) {
						Mixed ccc = env.getEnv(GlobalEnv.class).GetVarList().get(((IVariable) cc).getVariableName(),
								cc.getTarget(), env).ival();
						String vval = ccc.val();
						if(ccc instanceof CString) {
							vval = new CString(ccc.val(), Target.UNKNOWN).getQuote();
						}
						vars.put(((IVariable) cc).getVariableName(), vval);
					}
					if(cc == null) {
						args2.add("java-null");
					} else if(cc instanceof CString) {
						args2.add(new CString(cc.val(), Target.UNKNOWN).getQuote());
					} else if(cc instanceof IVariable) {
						args2.add(((IVariable) cc).getVariableName());
					} else if(cc instanceof CClosure) {
						args2.add("<closure>");
					} else {
						args2.add(cc.val());
					}
				}
				if(!vars.isEmpty()) {
					emsg += StringUtils.Join(vars, " = ", "\n") + "\n";
				}
				emsg += f.getName() + "(";
				emsg += StringUtils.Join(args2, ", ");
				emsg += ")\n";

				emsg += TermColors.RED + "on or around "
						+ TermColors.YELLOW + m.getTarget().file() + TermColors.WHITE + ":" + TermColors.CYAN
						+ m.getTarget().line() + TermColors.RED + ".\n";

				//Server might not be available in this platform, so let's be sure to ignore those exceptions
				String modVersion;
				try {
					modVersion = StaticLayer.GetConvertor().GetServer().getAPIVersion();
				} catch (Exception ex) {
					modVersion = Implementation.GetServerType().name();
				}

				String extensionData = "";
				for(ExtensionTracker tracker : ExtensionManager.getTrackers().values()) {
					for(Extension extension : tracker.getExtensions()) {
						try {
							extensionData += TermColors.CYAN + extension.getName() + TermColors.RED
									+ " (" + TermColors.RESET + extension.getVersion() + TermColors.RED + ")\n";
						} catch (AbstractMethodError ex) {
							// This happens with an old style extensions. Just skip it.
							extensionData += TermColors.CYAN + "Unknown Extension" + TermColors.RED + "\n";
						}
					}
				}
				if(extensionData.isEmpty()) {
					extensionData = "NONE\n";
				}

				emsg += "Please report this to the developers, and be sure to include the version numbers:\n"
						+ TermColors.CYAN + "Server" + TermColors.RED + " version: " + TermColors.RESET + modVersion + TermColors.RED + ";\n"
						+ TermColors.CYAN + brand + TermColors.RED + " version: " + TermColors.RESET + version + TermColors.RED + ";\n"
						+ "Loaded extensions and versions:\n" + extensionData
						+ "Here's the stacktrace:\n" + TermColors.RESET + Static.GetStacktraceString(e);
				System.err.println(emsg);
				throw new CancelCommandException(null, Target.UNKNOWN);
			}
		} finally {
			if(addedRootStackElement && stManager.isStackSingle()) {
				stManager.popStackTraceElement();
			}
		}
	}

	private void doDebugOutput(String nodeName, List<ParseTree> children) {
		List<String> args = new ArrayList<>();
		for(ParseTree t : children) {
			if(t.isConst()) {
				if(t.getData() instanceof CString) {
					args.add(new CString(t.getData().val(), Target.UNKNOWN).getQuote());
				} else {
					args.add(t.getData().val());
				}
			} else if(t.getData() instanceof IVariable) {
				args.add(((IVariable) t.getData()).getVariableName());
			} else if(t.getData() instanceof Variable) {
				args.add(((Variable) t.getData()).getVariableName());
			} else {
				args.add("[[Dynamic Element]]");
			}
		}
		StreamUtils.GetSystemOut().println(TermColors.BG_RED + "[[DEBUG]] " + nodeName + "("
				+ StringUtils.Join(args, ", ") + ")" + TermColors.RESET);
	}

	public boolean match(String command) {
		if(cleft == null) {
			//The compilation error happened during the signature declaration, so
			//we can't match it, nor can we even tell if it's what they intended for us to run.
			return false;
		}
		boolean caseSensitive = Prefs.CaseSensitive();
		String[] cmds = command.split(" ");
		List<String> args = new ArrayList<>(Arrays.asList(cmds));
		boolean isAMatch = true;
		StringBuilder lastVar = new StringBuilder();
		int lastJ = 0;
		for(int j = 0; j < cleft.size(); j++) {
			if(!isAMatch) {
				break;
			}
			lastJ = j;
			Mixed c = cleft.get(j);
			if(args.size() <= j) {
				if(!Construct.IsCType(c, ConstructType.VARIABLE) || !((Variable) c).isOptional()) {
					isAMatch = false;
				}
				break;
			}
			String arg = args.get(j);
			if(!Construct.IsCType(c, ConstructType.VARIABLE)) {
				if(caseSensitive && !c.val().equals(arg) || !caseSensitive && !c.val().equalsIgnoreCase(arg)) {
					isAMatch = false;
					continue;
				}
			} else {
				//It's a variable. If it's optional, the rest of them are optional too, so as long as the size of
				//args isn't greater than the size of cleft, it's a match
				if(((Variable) c).isOptional()) {
					if(args.size() <= cleft.size()) {
						return true;
					} else {
						Mixed fin = cleft.get(cleft.size() - 1);
						if(fin instanceof Variable) {
							if(((Variable) fin).isFinal()) {
								return true;
							}
						}
						return false;
					}
				}
			}
			if(j == cleft.size() - 1) {
				if(Construct.IsCType(cleft.get(j), ConstructType.VARIABLE)) {
					Variable lv = (Variable) cleft.get(j);
					if(lv.isFinal()) {
						for(int a = j; a < args.size(); a++) {
							if(lastVar.length() == 0) {
								lastVar.append(args.get(a));
							} else {
								lastVar.append(" ").append(args.get(a));
							}
						}
					}
				}
			}
		}
		boolean lastIsFinal = false;
		if(cleft.get(cleft.size() - 1) instanceof Variable) {
			Variable v = (Variable) cleft.get(cleft.size() - 1);
			if(v.isFinal()) {
				lastIsFinal = true;
			}
		}
		if((cleft.get(lastJ) instanceof Variable && ((Variable) cleft.get(lastJ)).isOptional())) {
			return true;
		}

		if(cleft.size() != cmds.length && !lastIsFinal) {
			isAMatch = false;
		}
		return isAMatch;
	}

	public List<Variable> getVariables(String command) {
		String[] cmds = command.split(" ");
		List<String> args = new ArrayList<>(Arrays.asList(cmds));

		StringBuilder lastVar = new StringBuilder();

		ArrayList<Variable> vars = new ArrayList<>();
		Variable v = null;
		for(int j = 0; j < cleft.size(); j++) {
			try {
				if(Construct.IsCType(cleft.get(j), ConstructType.VARIABLE)) {
					if(((Variable) cleft.get(j)).getVariableName().equals("$")) {
						for(int k = j; k < args.size(); k++) {
							lastVar.append(args.get(k).trim()).append(" ");
						}
						v = new Variable(((Variable) cleft.get(j)).getVariableName(),
								lastVar.toString().trim(), Target.UNKNOWN);
					} else {
						v = new Variable(((Variable) cleft.get(j)).getVariableName(),
								args.get(j), Target.UNKNOWN);
					}
				}
			} catch (IndexOutOfBoundsException e) {
				v = new Variable(((Variable) cleft.get(j)).getVariableName(),
						((Variable) cleft.get(j)).getDefault(), Target.UNKNOWN);
			}
			if(v != null) {
				vars.add(v);
			}
		}
		return vars;
	}

	public Script compile() throws ConfigCompileException, ConfigCompileGroupException {
		try {
			verifyLeft();
			compileLeft();
			compileRight();
		} catch (ConfigCompileException e) {
			compilerError = true;
			throw e;
		}
		compilerError = false;
		hasBeenCompiled = true;
		return this;
	}

	private boolean verifyLeft() throws ConfigCompileException {
		boolean insideOptVar = false;
		boolean afterNoDefOptVar = false;
		String lastVar = null;
		//Go through our token list and readjust non-spaced symbols. Any time we combine a symbol,
		//the token becomes a string
		List<Token> tempLeft = new ArrayList<>();
		for(int i = 0; i < left.size(); i++) {
			Token t = left.get(i);
			if(i == 0 && t.type == TType.NEWLINE) {
				continue;
			}
			if(t.type.isSymbol() && left.size() - 1 > i && left.get(i + 1).type != TType.WHITESPACE) {
				StringBuilder b = new StringBuilder();
				b.append(t.value);
				i++;
				Token m = left.get(i);
				while(m.type.isSymbol() && m.type != TType.WHITESPACE) {
					b.append(m.value);
					i++;
					m = left.get(i);
				}

				if(m.type != TType.WHITESPACE && m.type != TType.LABEL) {
					b.append(m.value);
				}
				t = new Token(TType.STRING, b.toString(), t.target);
				if(m.type == TType.LABEL) {
					tempLeft.add(t);
					tempLeft.add(m);
					continue;
				}
			}
			//Go ahead and toString the other symbols too
			if(t.type.isSymbol()) {
				t = new Token(TType.STRING, t.value, t.target);
			}
			if(t.type != TType.WHITESPACE) {
				tempLeft.add(t);
			}

		}
		//Look through and concatenate all tokens before the label, if such exists.
		boolean hasLabel = false;
		for(Token aTempLeft : tempLeft) {
			if(aTempLeft.type == TType.LABEL) {
				hasLabel = true;
				break;
			}
		}
		if(hasLabel) {
			StringBuilder b = new StringBuilder();
			int count = 0;
			while(tempLeft.get(count).type != TType.LABEL) {
				b.append(tempLeft.get(count).val());
				count++;
			}
			tempLeft.set(0, new Token(TType.STRING, b.toString(), Target.UNKNOWN));
			for(int i = 0; i < count - 1; i++) {
				tempLeft.remove(1);
			}
		}
		left = tempLeft;
		for(int j = 0; j < left.size(); j++) {
			Token t = left.get(j);
			//Token prev_token = j - 2 >= 0?c.tokens.get(j - 2):new Token(TType.UNKNOWN, "", t.line_num);
			Token lastToken = j - 1 >= 0 ? left.get(j - 1) : new Token(TType.UNKNOWN, "", t.getTarget());
			Token nextToken = j + 1 < left.size() ? left.get(j + 1) : new Token(TType.UNKNOWN, "", t.getTarget());
			Token afterToken = j + 2 < left.size() ? left.get(j + 2) : new Token(TType.UNKNOWN, "", t.getTarget());

			if(j == 0) {
				if(nextToken.type == TType.LABEL) {
					this.label = t.val();
					j--;
					left.remove(0);
					left.remove(0);
					continue;
				}
			}

			if(t.type == TType.LABEL) {
				continue;
			}

			if(t.type.equals(TType.FINAL_VAR) && left.size() - j >= 5) {
				throw new ConfigCompileException("FINAL_VAR must be the last argument in the alias", t.target);
			}
			if(t.type.equals(TType.VARIABLE) || t.type.equals(TType.FINAL_VAR)) {
				Variable v = new Variable(t.val(), null, t.target);
				lastVar = t.val();
				v.setOptional(lastToken.type.equals(TType.LSQUARE_BRACKET));
				leftVars.put(t.val(), v);
				if(v.isOptional()) {
					afterNoDefOptVar = true;
				} else {
					v.setDefault("");
				}
			}
			//We're looking for a command up front
			if(j == 0 && !t.value.startsWith("/")) {
				if(!(nextToken.type == TType.LABEL && afterToken.type == TType.COMMAND)) {
					throw new ConfigCompileException("Expected command (/command) at start of alias."
							+ " Instead, found " + t.type + " (" + t.val() + ")", t.target);
				}
			}
			if(lastToken.type.equals(TType.LSQUARE_BRACKET)) {
				insideOptVar = true;
				if(!(t.type.equals(TType.FINAL_VAR) || t.type.equals(TType.VARIABLE))) {
					throw new ConfigCompileException("Unexpected " + t.type.toString() + " (" + t.val() + "), was expecting"
							+ " a $variable", t.target);
				}
			}
			if(afterNoDefOptVar && !insideOptVar) {
				if(t.type.equals(TType.VARIABLE) || t.type.equals(TType.FINAL_VAR)) {
					throw new ConfigCompileException("You cannot have anything other than optional arguments after your"
							+ " first optional argument.", t.target);
				}
			}
			if(!t.type.equals(TType.LSQUARE_BRACKET)
					&& !t.type.equals(TType.OPT_VAR_ASSIGN)
					&& !t.type.equals(TType.RSQUARE_BRACKET)
					&& !t.type.equals(TType.VARIABLE)
					&& !t.type.equals(TType.LIT)
					&& !t.type.equals(TType.COMMAND)
					&& !t.type.equals(TType.FINAL_VAR)) {
				if(j - 1 > 0 && !(/*t.type.equals(TType.STRING) &&*/left.get(j - 1).type.equals(TType.OPT_VAR_ASSIGN))) {
					throw new ConfigCompileException("Unexpected " + t.type + " (" + t.val() + ")", t.target);
				}
			}
			if(lastToken.type.equals(TType.COMMAND)) {
				if(!(t.type.equals(TType.VARIABLE) || t.type.equals(TType.LSQUARE_BRACKET) || t.type.equals(TType.FINAL_VAR)
						|| t.type.equals(TType.LIT) || t.type.equals(TType.STRING))) {
					throw new ConfigCompileException("Unexpected " + t.type + " (" + t.val() + ") after command", t.target);
				}
			}
			if(insideOptVar && t.type.equals(TType.OPT_VAR_ASSIGN)) {
				if(!(nextToken.type.isAtomicLit() && afterToken.type.equals(TType.RSQUARE_BRACKET)
						|| (nextToken.type.equals(TType.RSQUARE_BRACKET)))) {
					throw new ConfigCompileException("Unexpected token in optional variable", t.target);
				} else if(nextToken.type.isAtomicLit()) {
					leftVars.get(lastVar).setDefault(nextToken.val());
				}
			}
			if(t.type.equals(TType.RSQUARE_BRACKET)) {
				if(!insideOptVar) {
					throw new ConfigCompileException("Unexpected " + t.type.toString(), t.target);
				}
				insideOptVar = false;
			}
		}

		return true;
	}

	private boolean compileLeft() {
		cleft = new ArrayList<>();
		if(label != null && label.startsWith("!")) {
			if(label.length() > 1) {
				label = label.substring(1);
			}
			nolog = true;
		}
		for(int i = 0; i < left.size(); i++) {
			Token t = left.get(i);
			if(t.value.startsWith("/")) {
				cleft.add(new Command(t.val(), t.target));
			} else if(t.type == Token.TType.VARIABLE) {
				cleft.add(new Variable(t.val(), null, t.target));
			} else if(t.type.equals(TType.FINAL_VAR)) {
				Variable v = new Variable(t.val(), null, t.target);
				v.setFinal(true);
				cleft.add(v);
			} else if(t.type.equals(TType.LSQUARE_BRACKET)) {
				if(i + 2 < left.size() && left.get(i + 2).type.equals(TType.OPT_VAR_ASSIGN)) {
					Variable v = new Variable(left.get(i + 1).val(),
							left.get(i + 3).val(), t.target);
					v.setOptional(true);
					if(left.get(i + 1).type.equals(TType.FINAL_VAR)) {
						v.setFinal(true);
					}
					cleft.add(v);
					i += 4;
				} else {
					t = left.get(i + 1);
					Variable v = new Variable(t.val(), null, t.target);
					v.setOptional(true);
					if(t.val().equals("$")) {
						v.setFinal(true);
					}
					cleft.add(v);
					i += 2;
				}
			} else {
				cleft.add(new CString(t.val(), t.getTarget()));
			}
		}
		return true;
	}

	public void compileRight() throws ConfigCompileException, ConfigCompileGroupException {
		List<Token> temp = new ArrayList<>();
		right = new ArrayList<>();
		for(Token t : fullRight) {
			if(t.type == TType.SEPERATOR) {
				right.add(temp);
				temp = new ArrayList<>();
			} else {
				if(t.type == TType.WHITESPACE) {
					continue; //Whitespace is ignored on the right side
				}
				temp.add(t);
			}
		}
		right.add(temp);
		cright = new ArrayList<>();
		for(List<Token> l : right) {
			cright.add(MethodScriptCompiler.compile(new TokenStream(l, fileOptions), null, envs));
		}
	}

	/**
	 * Returns the parse trees representing the right side of all scripts.
	 * @return
	 */
	public List<ParseTree> getTrees() {
		return new ArrayList<>(cright);
	}

	public void checkAmbiguous(List<Script> scripts) throws ConfigCompileException {
		List<Construct> thisCommand = this.cleft;
		for(Script script : scripts) {
			if(script.fileOptions.hasCompilerOption(FileOptions.CompilerOption.AllowAmbiguousCommands)) {
				continue;
			}
			List<Construct> thatCommand = script.cleft;
			if(thatCommand == null) {
				// It hasn't been compiled yet.
				return;
			}
			if(this.cleft == script.cleft) {
				// Of course this command is going to match its own signature.
				continue;
			}

			matchScope:
			{
				for(int k = 0; k < thisCommand.size(); k++) {
					Construct c1 = thisCommand.get(k);
					if(k < thatCommand.size()) {
						Construct c2 = thatCommand.get(k);

						// Commands are not ambigous if they have unequal commands or strings at
						// the same argument position.
						if(c1.getCType() == c2.getCType()
								&& (c1.getCType() == ConstructType.STRING || c1.getCType() == ConstructType.COMMAND)) {
							if(Construct.nval(c1) != Construct.nval(c2) && (Construct.nval(c1) == null
									|| !Construct.nval(c1).equals(Construct.nval(c2)))) {
								break matchScope;
							}
						}

					} else {

						// thatCommand is shorter than thisCommand.
						// Commands are not ambigous if thisCommand contains a non-variable or a non-optional variable
						// after the last Construct in thatCommand.
						if(!(c1 instanceof Variable) || (c1 instanceof Variable && !((Variable) c1).isOptional())) {
							break matchScope;
						} else {
							break; // There is no need to loop over later Constructs, the commands are ambigous.
						}

					}
				}
				if(thatCommand.size() > thisCommand.size()) {

					// thisCommand is shorter than thatCommand.
					// Commands are not ambigous if thatCommand contains a non-variable or a non-optional variable
					// after the last Construct in thisCommand.
					Construct c2 = thatCommand.get(thisCommand.size());
					if(!(c2 instanceof Variable) || (c2 instanceof Variable && !((Variable) c2).isOptional())) {
						break matchScope;
					}

				}

				// The signature of thisCommand and thatCommand are ambigous. Throw a compile exception.
				String commandThis = "";
				for(Construct c : thisCommand) {
					commandThis += c.val() + " ";
				}
				String commandThat = "";
				for(Construct c : thatCommand) {
					commandThat += c.val() + " ";
				}
				script.compilerError = true;
				this.compilerError = true;
				throw new ConfigCompileException("The command " + commandThis.trim() + " is ambiguous because it "
						+ "matches the signature of " + commandThat.trim() + " defined at "
						+ thatCommand.get(0).getTarget(), thisCommand.get(0).getTarget());
			}
		}
	}

	public void enforceLabelPermissions() {
		String label = currentEnv.getEnv(GlobalEnv.class).GetLabel();
		if(label == null || label.equals(Static.GLOBAL_PERMISSION)) {
			return;
		}
		MCPlayer p = currentEnv.getEnv(CommandHelperEnvironment.class).GetPlayer();
		if(p == null) {
			// labels only apply to players
			currentEnv.getEnv(GlobalEnv.class).SetLabel(Static.GLOBAL_PERMISSION);
		} else if(label.startsWith("~")) {
			// group labels
			String[] groups = label.substring(1).split("/");
			for(String group : groups) {
				if(group.startsWith("-") && p.inGroup(group.substring(1))) {
					// negative permission
					throw new CREInsufficientPermissionException("You do not have permission to use that script.",
							Target.UNKNOWN);
				} else if(p.inGroup(group)) {
					// they have explicit permission.
					currentEnv.getEnv(GlobalEnv.class).SetLabel(Static.GLOBAL_PERMISSION);
					return;
				}
			}
		} else if(label.indexOf('.') != -1) {
			// custom permission label
			if(p.hasPermission(label)) {
				currentEnv.getEnv(GlobalEnv.class).SetLabel(Static.GLOBAL_PERMISSION);
			}
		} else if(p.hasPermission("ch.alias." + label) || p.hasPermission("commandhelper.alias." + label)) {
			currentEnv.getEnv(GlobalEnv.class).SetLabel(Static.GLOBAL_PERMISSION);
		}
	}

	public boolean doLog() {
		return !nolog;
	}

}
