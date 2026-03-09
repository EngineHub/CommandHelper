package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.SimpleVersion;
import com.laytonsmith.PureUtilities.SmartComment;
import com.laytonsmith.PureUtilities.TermColors;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.compiler.TokenStream;
import com.laytonsmith.core.compiler.analysis.StaticAnalysis;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Command;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Construct.ConstructType;
import com.laytonsmith.core.constructs.IVariable;
import com.laytonsmith.core.constructs.IVariableList;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.Token;
import com.laytonsmith.core.constructs.Token.TType;
import com.laytonsmith.core.constructs.Variable;
import com.laytonsmith.core.constructs.generics.GenericParameters;
import com.laytonsmith.core.constructs.generics.LeftHandGenericUse;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.DebugContext;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.environments.StaticRuntimeEnv;
import com.laytonsmith.core.exceptions.CRE.CREInsufficientPermissionException;
import com.laytonsmith.core.exceptions.CRE.CREInvalidProcedureException;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.StackTraceFrame;
import com.laytonsmith.core.exceptions.StackTraceManager;
import com.laytonsmith.core.exceptions.UnhandledFlowControlException;
import com.laytonsmith.core.functions.ControlFlow;
import com.laytonsmith.core.functions.Exceptions;
import com.laytonsmith.core.functions.Function;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.core.objects.AccessModifier;
import com.laytonsmith.core.objects.ObjectModifier;
import com.laytonsmith.core.objects.ObjectType;
import java.net.URL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
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

	/**
	 * Sentinel value returned by eval/evalLoop when the debugger pauses execution.
	 * Callers should check with {@link #isDebuggerPaused(Mixed)} before using the result.
	 */
	private static final Mixed DEBUGGER_PAUSED
// <editor-fold defaultstate="collapsed" desc="DEBUGGER_PAUSED implementation">
			= new Mixed() {
		@Override
		public String val() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public void setTarget(Target target) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public Target getTarget() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public Mixed clone() throws CloneNotSupportedException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public String getName() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public String docs() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public Version since() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public CClassType[] getSuperclasses() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public CClassType[] getInterfaces() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public ObjectType getObjectType() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public Set<ObjectModifier> getObjectModifiers() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public AccessModifier getAccessModifier() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public CClassType getContainingClass() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public boolean isInstanceOf(CClassType type) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public boolean isInstanceOf(CClassType type, LeftHandGenericUse lhsGenericParameters, Environment env) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public boolean isInstanceOf(Class<? extends Mixed> type) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public CClassType typeof() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public CClassType typeof(Environment env) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public GenericParameters getGenericParameters() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public URL getSourceJar() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public Class<? extends Documentation>[] seeAlso() {
			throw new UnsupportedOperationException("Not supported yet.");
		}
	};
// </editor-fold>

	/**
	 * Returns true if the given result indicates the debugger paused execution
	 * rather than completing normally.
	 *
	 * @param result The return value from eval or execute
	 * @return true if execution was paused by the debugger
	 */
	public static boolean isDebuggerPaused(Mixed result) {
		return result == DEBUGGER_PAUSED;
	}

	// See set_debug_output()
	public static boolean debugOutput = false;

	private List<Token> left;
	private List<Token> fullRight;
	private List<Construct> cleft;
	private final List<ParseTree> cright = new ArrayList<>();
	private boolean nolog = false;
	//This should be null if we are running in non-alias mode
	private Map<String, Variable> leftVars;
	boolean hasBeenCompiled = false;
	boolean compilerError = false;
	private final long compileTime;
	private String label;
	private Set<Class<? extends Environment.EnvironmentImpl>> envs;
	private FileOptions fileOptions;
	private SmartComment smartComment;

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

	/**
	 * This is a useful overload for showing the usage.
	 * @return
	 */
	public String getSignatureWithoutLabel() {
		StringBuilder b = new StringBuilder();
		for(Token t : left) {
			b.append(t.val()).append(" ");
		}
		return b.toString();
	}

	/**
	 * Returns the name of the command, i.e. in "/run $cmd", "run" is the name of the command.
	 * In general, this is only used for command parsing, when dealing directly with the aliases,
	 * they shouldn't be compared using this, use {@link #getSignature()} instead.
	 * @return
	 */
	public String getCommandName() {
		return left.get(0).val().substring(1);
	}

	/**
	 * Returns the smart comment attached to this alias. To prevent null pointer exceptions, this
	 * will never be null, it will be an empty smart comment.
	 * @return
	 */
	public SmartComment getSmartComment() {
		if(smartComment == null) {
			return new SmartComment("");
		}
		return smartComment;
	}

	/**
	 * Returns the target where this script is defined. Since scripts span multiple lines and columns, this is simply
	 * the code target of the first token in the left side, not including the label.
	 * @return
	 */
	public Target getTarget() {
		return left.get(0).getTarget();
	}

	public Script(List<Token> left, List<Token> right, String label,
			Set<Class<? extends Environment.EnvironmentImpl>> envs, FileOptions fileOptions,
			SmartComment smartComment) {
		this.left = left;
		this.fullRight = right;
		this.leftVars = new HashMap<>();
		this.label = label;
		this.envs = envs;
		compileTime = System.currentTimeMillis();
		this.fileOptions = fileOptions;
		this.smartComment = smartComment;
	}

	private Script() {
		compileTime = System.currentTimeMillis();
	}

	public long getCompileTime() {
		return compileTime;
	}

	public FileOptions getScriptFileOptions() {
		return fileOptions;
	}

	public static Script GenerateScript(ParseTree tree, String label, SmartComment comment) {
		Script s = new Script();

		s.hasBeenCompiled = true;
		s.compilerError = false;
		s.cright.add(tree);
		s.label = label;
		s.smartComment = comment;

		return s;
	}

	public boolean uncompilable() {
		return compilerError;
	}

	public void run(final List<Variable> vars, Environment myEnv, final MethodScriptComplete done) {
		//Some things, such as the label are determined at compile time
		myEnv.getEnv(GlobalEnv.class).SetLabel(this.label);
		myEnv.getEnv(GlobalEnv.class).SetAliasComment(this.smartComment);
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
		enforceLabelPermissions(myEnv);

		try {
			for(ParseTree rootNode : cright) {
				if(rootNode == null) {
					continue;
				}
				if(leftVars != null) {
					IdentityHashMap<Mixed, String> dollarBindings = new IdentityHashMap<>();
					for(Mixed tempNode : rootNode.getAllData()) {
						if(tempNode instanceof Variable variable) {
							Construct leftVar = leftVars.get(variable.getVariableName());
							if(leftVar == null) {
								throw ConfigRuntimeException.CreateUncatchableException("$variables may not be used in this context."
										+ " Only @variables may be.", tempNode.getTarget());
							}
							Construct c = Static.resolveDollarVar(leftVar, vars);
							dollarBindings.put(tempNode, c.toString());
						}
					}
					myEnv.getEnv(GlobalEnv.class).SetDollarVarBindings(dollarBindings);
				}

				myEnv.getEnv(StaticRuntimeEnv.class).getIncludeCache().executeAutoIncludes(myEnv, this);
				MethodScriptCompiler.execute(rootNode, myEnv, done, this);
			}
		} catch (ConfigRuntimeException ex) {
			//We don't know how to handle this really, so let's pass it up the chain.
			throw ex;
		} catch (CancelCommandException e) {
			//p.sendMessage(e.getMessage());
			//The message in the exception is actually empty
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
		while(ret instanceof IVariable cur) {
			ret = env.getEnv(GlobalEnv.class).GetVarList().get(cur.getVariableName(), cur.getTarget(), env).ival();
		}
		return ret;
	}

	/**
	 * Iterative interpreter loop. Evaluates a parse tree using an explicit stack instead of
	 * recursive Java calls. This enables:
	 * <ul>
	 *   <li>Control flow (break/continue/return) as first-class FlowControl actions, not exceptions</li>
	 *   <li>Save/restore of execution state (for debugger, async/await)</li>
	 *   <li>1:1 MethodScript-to-stack-frame mapping</li>
	 * </ul>
	 *
	 * <p>Two execution paths exist for function calls:</p>
	 * <ol>
	 *   <li><b>FlowFunction</b> — Function implements {@link FlowFunction}. The loop drives it
	 *       via begin/childCompleted/childInterrupted. (This replaces the old execs mechanism.)</li>
	 *   <li><b>Simple exec</b> — Normal functions. Children are evaluated left-to-right,
	 *       then {@code exec()} is called with the results.</li>
	 * </ol>
	 *
	 * @param root The parse tree to evaluate
	 * @param env The environment
	 * @return The result of evaluation
	 */
	private Mixed iterativeEval(ParseTree root, Environment env) {
		EvalStack stack = new EvalStack();
		stack.push(new StackFrame(root, env, null, null));
		return evalLoop(stack, null, false, null, env);
	}

	/**
	 * Resumes execution from a previously frozen {@link DebugSnapshot}. The snapshot is
	 * created by the interpreter when a debug pause is triggered, and is passed to the
	 * {@link DebugListener#onPaused} callback. The caller must set the desired
	 * {@link DebugContext.StepMode} on the DebugContext before calling this method.
	 *
	 * @param snapshot The frozen state to resume from
	 * @return The result of evaluation, or null if execution pauses again
	 */
	public static Mixed resumeEval(DebugSnapshot snapshot) {
		DebugContext debugCtx = snapshot.env.hasEnv(DebugContext.class)
				? snapshot.env.getEnv(DebugContext.class) : null;
		if(debugCtx != null) {
			debugCtx.setPaused(false, null);
			debugCtx.getListener().onResumed();
		}
		return evalLoop(snapshot.stack, snapshot.lastResult, snapshot.hasResult,
				snapshot.pendingFlowControl, snapshot.env);
	}

	@SuppressWarnings("unchecked")
	private static Mixed evalLoop(EvalStack stack, Mixed lastResult, boolean hasResult,
			StepAction.FlowControl pendingFlowControl, Environment env) {
		DebugContext debugCtx = env.hasEnv(DebugContext.class)
				? env.getEnv(DebugContext.class) : null;

		while(!stack.isEmpty()) {
			GlobalEnv gEnv = env.getEnv(GlobalEnv.class);

			if(gEnv.IsInterrupted()) {
				throw new CancelCommandException("", Target.UNKNOWN);
			}

			// Propagate pending flow control
			StackFrame frame = stack.peek();
			if(pendingFlowControl != null) {
				if(frame.hasFlowFunction() && frame.hasBegun()) {
					Target t = frame.getNode().getTarget();
					StepAction.StepResult<?> response =
							((FlowFunction<Object>) frame.getFlowFunction()).childInterrupted(
							t, frame.getFunctionState(), pendingFlowControl, frame.getEnv());
					if(response != null) {
						pendingFlowControl = null;
						frame.setFunctionState(response.getState());
						StepAction action = response.getAction();
						if(action instanceof StepAction.Evaluate e) {
							frame.setKeepIVariable(e.keepIVariable());
							Environment evalEnv = e.getEnv() != null ? e.getEnv() : frame.getEnv();
							stack.push(new StackFrame(e.getNode(), evalEnv, null, null));
						} else if(action instanceof StepAction.Complete c) {
							lastResult = c.getResult();
							hasResult = true;
							cleanupAndPop(stack, frame);
						} else if(action instanceof StepAction.FlowControl fc) {
							pendingFlowControl = fc;
							cleanupAndPop(stack, frame);
						}
						continue;
					}
				}
				cleanupAndPop(stack, frame);
				if(stack.isEmpty()) {
					if(pendingFlowControl.getAction()
							instanceof ControlFlow.ReturnAction ret) {
						return ret.getValue();
					}
					if(pendingFlowControl.getAction()
							instanceof Exceptions.ThrowAction ta) {
						throw ta.getException();
					}
					throw new UnhandledFlowControlException(pendingFlowControl.getAction());
				}
				continue;
			}

			ParseTree node = frame.getNode();
			Mixed data = node.getData();

			// Debug pause check (after flow control is resolved, only on first visit to function nodes)
			if(debugCtx != null && !debugCtx.isDisconnected()
					&& data instanceof CFunction
					&& !frame.hasBegun()) {
				Target currentTarget = node.getTarget();
				int userDepth = gEnv.GetStackTraceManager().getDepth();
				if(debugCtx.shouldPause(currentTarget, userDepth)) {
					DebugSnapshot snapshot = new DebugSnapshot(
							stack, lastResult, hasResult, pendingFlowControl, env, currentTarget);
					debugCtx.setPaused(true, currentTarget);
					debugCtx.getListener().onPaused(snapshot);
					return DEBUGGER_PAUSED;
				}
			}

			// Literal / variable nodes (no children)
			if(data instanceof Construct co && co.getCType() != Construct.ConstructType.FUNCTION
					&& node.numberOfChildren() == 0) {
				if(co.getCType() == Construct.ConstructType.VARIABLE) {
					String val = gEnv.GetDollarVarBinding(data);
					if(val == null) {
						val = "";
					}
					lastResult = new CString(val, data.getTarget());
				} else {
					lastResult = data;
				}
				hasResult = true;
				stack.pop();
				continue;
			}

			// Sequence nodes (non-function with children, e.g. root node) skip
			// function resolution and use simple exec with function=null
			if(data instanceof CFunction cfunc) {
				// First visit: resolve function or procedure
				if(frame.getFunction() == null && !frame.hasFlowFunction()) {
					if(cfunc.hasProcedure()) {
						Procedure p = gEnv.GetProcs().get(data.val());
						if(p == null) {
							throw new CREInvalidProcedureException(
									"Unknown procedure \"" + data.val() + "\"", data.getTarget());
						}
						FlowFunction<?> procedureFlow = p.createProcedureFlow(data.getTarget());
						stack.pop();
						stack.push(new StackFrame(node, frame.getEnv(), null, procedureFlow));
						hasResult = false;
						continue;
					}

					Function f = cfunc.getCachedFunction();
					if(f == null) {
						try {
							f = cfunc.getFunction();
						} catch(ConfigCompileException ex) {
							throw ConfigRuntimeException.CreateUncatchableException(
									"Unknown function \"" + cfunc.val() + "\"", cfunc.getTarget());
						}
					}

					FlowFunction<?> flowFunction = (f instanceof FlowFunction<?>) ? (FlowFunction<?>) f : null;

					stack.pop();
					StackFrame newFrame = new StackFrame(node, frame.getEnv(), f, flowFunction);
					stack.push(newFrame);
					frame = newFrame;
					hasResult = false;
				}
			}

			Function f = frame.getFunction();

			// Permission check on first visit
			if(!frame.hasBegun() && f != null && f.isRestricted()
					&& !Static.hasCHPermission(f.getName(), frame.getEnv())) {
				throw new CREInsufficientPermissionException(
						"You do not have permission to use the " + f.getName() + " function.",
						data.getTarget());
			}

			// Flow function mode
			if(frame.hasFlowFunction()) {
				Target t = node.getTarget();
				StepAction.StepResult<?> result;
				if(!frame.hasBegun()) {
					frame.markBegun();
					result = ((FlowFunction<Object>) frame.getFlowFunction()).begin(
							t, frame.getChildren(), frame.getEnv());
				} else if(hasResult) {
					// Resolve IVariables unless the parent explicitly asked to keep them
					if(!frame.keepIVariable()) {
						while(lastResult instanceof IVariable cur) {
							GlobalEnv frameGEnv = frame.getEnv().getEnv(GlobalEnv.class);
							lastResult = frameGEnv.GetVarList()
									.get(cur.getVariableName(), cur.getTarget(),
											frame.getEnv()).ival();
						}
					}
					frame.setKeepIVariable(false);
					result = ((FlowFunction<Object>) frame.getFlowFunction()).childCompleted(
							t, frame.getFunctionState(), lastResult, frame.getEnv());
					hasResult = false;
				} else {
					throw ConfigRuntimeException.CreateUncatchableException(
							"Flow function in invalid state for " + data.val(), data.getTarget());
				}

				frame.setFunctionState(result.getState());
				StepAction action = result.getAction();
				if(action instanceof StepAction.Evaluate e) {
					frame.setKeepIVariable(e.keepIVariable());
					Environment evalEnv = e.getEnv() != null ? e.getEnv() : frame.getEnv();
					stack.push(new StackFrame(e.getNode(), evalEnv, null, null));
				} else if(action instanceof StepAction.Complete c) {
					lastResult = c.getResult();
					hasResult = true;
					cleanupAndPop(stack, frame);
				} else if(action instanceof StepAction.FlowControl fc) {
					pendingFlowControl = fc;
					cleanupAndPop(stack, frame);
				}
				continue;
			}

			// Simple exec mode
			if(hasResult) {
				Mixed arg = lastResult;
				while(f != null && f.preResolveVariables() && arg instanceof IVariable cur) {
					GlobalEnv frameGEnv = frame.getEnv().getEnv(GlobalEnv.class);
					arg = frameGEnv.GetVarList().get(cur.getVariableName(), cur.getTarget(),
							frame.getEnv()).ival();
				}
				frame.addArg(arg);
				hasResult = false;
			}

			if(frame.hasMoreChildren()) {
				if(!frame.hasBegun()) {
					frame.markBegun();
				}
				stack.push(new StackFrame(frame.nextChild(), frame.getEnv(), null, null));
			} else {
				if(!frame.hasBegun()) {
					frame.markBegun();
				}
				if(f == null) {
					// Sequence node — return last child's result
					Mixed[] args = frame.getArgs();
					lastResult = args.length > 0 ? args[args.length - 1] : CVoid.VOID;
					hasResult = true;
					stack.pop();
				} else {
					try {
						lastResult = Function.ExecuteFunction(f, data.getTarget(),
								frame.getEnv(), frame.getArgs());
						hasResult = true;
						stack.pop();
					} catch(ConfigRuntimeException e) {
						// Convert MethodScript exceptions to FlowControl(ThrowAction)
						stack.pop();
						pendingFlowControl = new StepAction.FlowControl(
								new com.laytonsmith.core.functions.Exceptions.ThrowAction(e));
					}
				}
			}
		}

		if(debugCtx != null) {
			debugCtx.getListener().onCompleted();
		}

		return lastResult;
	}

	/**
	 * Calls {@link FlowFunction#cleanup} if the frame has a FlowFunction that has begun,
	 * then pops the frame from the stack.
	 */
	@SuppressWarnings("unchecked")
	private static void cleanupAndPop(EvalStack stack, StackFrame frame) {
		if(frame.hasFlowFunction() && frame.hasBegun()) {
			((FlowFunction<Object>) frame.getFlowFunction()).cleanup(
					frame.getNode().getTarget(), frame.getFunctionState(), frame.getEnv());
		}
		stack.pop();
	}

	/**
	 * Given the parse tree and environment, executes the tree.
	 *
	 * @param c
	 * @param env
	 * @return
	 * @throws CancelCommandException
	 */
	public Mixed eval(ParseTree c, final Environment env) throws CancelCommandException {
		return iterativeEval(c, env);
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
		String[] cmds = command.split(" ");
		return match(cmds);
	}

	public boolean match(String[] args) {
		if(cleft == null) {
			//The compilation error happened during the signature declaration, so
			//we can't match it, nor can we even tell if it's what they intended for us to run.
			return false;
		}
		boolean caseSensitive = Prefs.CaseSensitive();
		for(int j = 0; j < cleft.size(); j++) {
			Mixed c = cleft.get(j);
			if(args.length <= j) {
				// If this optional, the rest are too.
				return c instanceof Variable v && v.isOptional();
			}
			if(!(c instanceof Variable)) {
				String arg = args[j];
				if(caseSensitive && !c.val().equals(arg) || !caseSensitive && !c.val().equalsIgnoreCase(arg)) {
					return false;
				}
			} else if(((Variable) c).isOptional()) {
				//It's a variable. If it's optional, the rest of them are optional too, so as long as the size of
				//args isn't greater than the size of cleft, it's a match
				return args.length <= cleft.size() || cleft.get(cleft.size() - 1) instanceof Variable v && v.isFinal();
			}
		}
		return args.length == cleft.size() || cleft.get(cleft.size() - 1) instanceof Variable v && v.isFinal();
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

	public Script compile(Environment env) throws ConfigCompileException, ConfigCompileGroupException {
		try {
			verifyLeft();
			compileLeft();
			compileRight(env);
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
				t = new Token(TType.STRING, b.toString(), t.target.copy());
				if(m.type == TType.LABEL) {
					tempLeft.add(t);
					tempLeft.add(m);
					continue;
				}
			}
			//Go ahead and toString the other symbols too
			if(t.type.isSymbol()) {
				t = new Token(TType.STRING, t.value, t.target.copy());
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
			Token lastToken = j - 1 >= 0 ? left.get(j - 1) : new Token(TType.UNKNOWN, "", t.getTarget().copy());
			Token nextToken = j + 1 < left.size() ? left.get(j + 1) : new Token(TType.UNKNOWN, "", t.getTarget().copy());
			Token afterToken = j + 2 < left.size() ? left.get(j + 2) : new Token(TType.UNKNOWN, "", t.getTarget().copy());

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

	/**
	 * Returns a list of the left hand tokens. For instance, in `/cmd const $var` this will return 3 Constructs, a
	 * Command, a CString, and a Variable. These are the only types that will be in the list.
	 * @return
	 */
	public List<Construct> getParameters() {
		return new ArrayList<>(cleft);
	}

	public void compileRight(Environment env) throws ConfigCompileException, ConfigCompileGroupException {
		List<Token> temp = new ArrayList<>();
		List<List<Token>> right = new ArrayList<>();
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
		for(List<Token> l : right) {
			StaticAnalysis analysis = new StaticAnalysis(true);
			cright.add(MethodScriptCompiler.compile(new TokenStream(l, fileOptions), env, envs, analysis));
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

						// Commands are not ambiguous if they have unequal commands or strings at
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
						// Commands are not ambiguous if thisCommand contains a non-variable or a non-optional variable
						// after the last Construct in thatCommand.
						if(!(c1 instanceof Variable) || (c1 instanceof Variable && !((Variable) c1).isOptional())) {
							break matchScope;
						} else {
							break; // There is no need to loop over later Constructs, the commands are ambiguous.
						}

					}
				}
				if(thatCommand.size() > thisCommand.size()) {

					// thisCommand is shorter than thatCommand.
					// Commands are not ambiguous if thatCommand contains a non-variable or a non-optional variable
					// after the last Construct in thisCommand.
					Construct c2 = thatCommand.get(thisCommand.size());
					if(!(c2 instanceof Variable) || (c2 instanceof Variable && !((Variable) c2).isOptional())) {
						break matchScope;
					}

				}

				// The signature of thisCommand and thatCommand are ambiguous. Throw a compile exception.
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

	public void enforceLabelPermissions(Environment currentEnv) {
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

	/**
	 * A frozen snapshot of the interpreter's execution state, created when the debugger pauses.
	 * Contains everything needed to inspect the current state (variables, call stack, source
	 * location) and to resume execution later.
	 *
	 * <p>The raw interpreter state (eval stack, pending flow control, etc.) is private and
	 * accessible only to {@link Script}. External callers (e.g. a DAP server) can use the
	 * public inspection methods to read variables, call stack, and source location.</p>
	 */
	public static final class DebugSnapshot {

		private final EvalStack stack;
		private final Mixed lastResult;
		private final boolean hasResult;
		private final StepAction.FlowControl pendingFlowControl;
		private final Environment env;
		private final Target pauseTarget;

		private DebugSnapshot(EvalStack stack, Mixed lastResult, boolean hasResult,
				StepAction.FlowControl pendingFlowControl, Environment env, Target pauseTarget) {
			this.stack = stack;
			this.lastResult = lastResult;
			this.hasResult = hasResult;
			this.pendingFlowControl = pendingFlowControl;
			this.env = env;
			this.pauseTarget = pauseTarget;
		}

		/**
		 * Returns the source location where execution paused.
		 */
		public Target getPauseTarget() {
			return pauseTarget;
		}

		/**
		 * Returns the user-visible call stack (proc/closure/include frames) at the point
		 * of the pause. The list is ordered from innermost (most recent) to outermost.
		 *
		 * @return A list of stack trace frames
		 */
		public List<StackTraceFrame> getCallStack() {
			StackTraceManager stm = env.getEnv(GlobalEnv.class).GetStackTraceManager();
			return stm.getCurrentStackTrace();
		}

		/**
		 * Returns the user-visible call depth at the point of the pause. This is the
		 * count of proc/closure/include frames, not the raw eval stack size.
		 *
		 * @return The user-visible call depth
		 */
		public int getUserCallDepth() {
			StackTraceManager stm = env.getEnv(GlobalEnv.class).GetStackTraceManager();
			return stm.getDepth();
		}

		/**
		 * Returns the variables visible at the point of the pause as a name-to-value map.
		 * The map preserves insertion order.
		 *
		 * @return A map from variable name (including the @ prefix) to its current value
		 */
		public Map<String, Mixed> getVariables() {
			IVariableList varList = env.getEnv(GlobalEnv.class).GetVarList();
			Map<String, Mixed> result = new LinkedHashMap<>();
			for(String name : varList.keySet()) {
				IVariable iv = varList.get(name);
				if(iv != null) {
					result.put(name, iv.ival());
				}
			}
			return result;
		}

		@Override
		public String toString() {
			return "DebugSnapshot{target=" + pauseTarget + ", depth=" + getUserCallDepth()
					+ ", vars=" + getVariables().size() + ", hasResult=" + hasResult + "}";
		}
	}

}
