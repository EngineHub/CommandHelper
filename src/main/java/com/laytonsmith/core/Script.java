

package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.TermColors;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CEntry;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CLabel;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
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
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.FunctionReturnException;
import com.laytonsmith.core.exceptions.LoopBreakException;
import com.laytonsmith.core.exceptions.LoopContinueException;
import com.laytonsmith.core.exceptions.ProgramFlowManipulationException;
import com.laytonsmith.core.extensions.Extension;
import com.laytonsmith.core.extensions.ExtensionManager;
import com.laytonsmith.core.extensions.ExtensionTracker;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.laytonsmith.core.functions.Function;
import com.laytonsmith.core.functions.FunctionBase;
import com.laytonsmith.core.functions.FunctionList;
import com.laytonsmith.core.profiler.ProfilePoint;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * A script is a section of code that has been preprocessed and split into separate
 * commands/actions. For instance, the config script:
 *
 * /command = /cmd
 *
 * /test = /test
 *
 * would be two seperate scripts, the first being the /command, and the second being /test.
 * Certain key information is stored in the Script class. First, the information needed
 * to see if a target string should trigger this script. Secondly, the default values
 * of any variables, and thirdly, the unparsed tree for the right side of the script.
 */
public class Script {

    private List<Token> left;
    private List<List<Token>> right;
    private List<Token> fullRight;
    private List<Construct> cleft;
    private List<ParseTree> cright;
	private boolean nolog = false;
    //This should be null if we are running in non-alias mode
    private Map<String, Variable> left_vars;
    boolean hasBeenCompiled = false;
    boolean compilerError = false;
    private String label;
    private Environment CurrentEnv;

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        for (Token t : left) {
            b.append(t.val()).append(" ");
        }
        b.append("; compiled: ").append(hasBeenCompiled).append("; errors? ").append(compilerError);
        return b.toString();
    }

    private Procedure getProc(String name) {
        return CurrentEnv.getEnv(GlobalEnv.class).GetProcs().get(name);
    }

    public Environment getCurrentEnv(){
        return CurrentEnv;
    }

    public String getLabel(){
        return label;
    }

    /**
     * Returns what would normally be on the left side on an alias ie. in config.msa
     * @return label:/alias arg [ optionalArg ]
     */
    public String getSignature() {
    	StringBuilder b = new StringBuilder();
    	b.append(getLabel()).append(":");
        for (Token t : left) {
            b.append(t.val()).append(" ");
        }
        return b.toString();
    }

    public Script(List<Token> left, List<Token> right) {
        this.left = left;
        this.fullRight = right;
        this.left_vars = new HashMap<String, Variable>();
        //this.OriginalEnv = env;
    }

    private Script(){}

    public static Script GenerateScript(ParseTree tree, String label){
        Script s = new Script();

        s.hasBeenCompiled = true;
        s.compilerError = false;
        s.cright = new ArrayList<ParseTree>();
        s.cright.add(tree);
        s.label = label;

        return s;
    }

    public boolean uncompilable() {
        return compilerError;
    }

    public void run(final List<Variable> vars, Environment myEnv, final MethodScriptComplete done) {
        //Some things, such as the label are determined at compile time
        this.CurrentEnv = myEnv;
        this.CurrentEnv.getEnv(GlobalEnv.class).SetLabel(this.label);
        MCCommandSender p = myEnv.getEnv(CommandHelperEnvironment.class).GetCommandSender();
        if (!hasBeenCompiled || compilerError) {
            Target target = Target.UNKNOWN;
            if (left.size() >= 1) {
                try{
                    target = new Target(left.get(0).line_num, left.get(0).file, left.get(0).column);
                } catch(NullPointerException e){
                    //Oh well, we tried to get more information
                }
            }
            throw ConfigRuntimeException.CreateUncatchableException("Unable to run command, script not yet compiled, or a compiler error occured for that command."
                    + " To see the compile error, run /reloadaliases", target);
        }
        if (p instanceof MCPlayer) {
            if (CurrentEnv.getEnv(GlobalEnv.class).GetLabel() != null) {
                PermissionsResolver perms = CurrentEnv.getEnv(GlobalEnv.class).GetPermissionsResolver();
                String[] groups = CurrentEnv.getEnv(GlobalEnv.class).GetLabel().split("/");
                for (String group : groups) {
                    if (group.startsWith("-") && perms.inGroup(((MCPlayer)p).getName(), group.substring(1))) {
                        //negative permission
                        throw new ConfigRuntimeException("You do not have permission to use that command", ExceptionType.InsufficientPermissionException,
                                Target.UNKNOWN);
                    } else if (perms.inGroup(((MCPlayer)p).getName(), group)) {
                        //They do have permission.
                        break;
                    }
                }
            }
        }

        try {
            for (ParseTree rootNode : cright) {
				if(rootNode == null){
					continue;
				}
                for (Construct tempNode : rootNode.getAllData()) {
                    if (tempNode instanceof Variable) {
                        if(left_vars == null){
                            ConfigRuntimeException.CreateUncatchableException("$variables may not be used in this context. Only @variables may be.", tempNode.getTarget());
                        }
                        ((Variable) tempNode).setVal(
                                new CString(
                                Static.resolveDollarVar(left_vars.get(((Variable) tempNode).getName()), vars).toString(), tempNode.getTarget()));
                    }
                }

                MethodScriptCompiler.registerAutoIncludes(CurrentEnv, this);
                MethodScriptCompiler.execute(rootNode, CurrentEnv, done, this);
            }
        } catch (ConfigRuntimeException ex) {
            //We don't know how to handle this really, so let's pass it up the chain.
            throw ex;
        } catch (CancelCommandException e) {
            //p.sendMessage(e.getMessage());
            //The message in the exception is actually empty
        } catch (LoopBreakException e) {
            if(p != null){
                p.sendMessage("The break() function must be used inside a for() or foreach() loop");
            }
            System.out.println("The break() function must be used inside a for() or foreach() loop");
        } catch (LoopContinueException e) {
            if(p != null){
                p.sendMessage("The continue() function must be used inside a for() or foreach() loop");
            }
            System.out.println("The continue() function must be used inside a for() or foreach() loop");
        } catch (FunctionReturnException e) {
            if(myEnv.getEnv(GlobalEnv.class).GetEvent() != null){
                //Oh, we're running in an event handler. Those know how to catch it too.
                throw e;
            }
            if(p != null){
                p.sendMessage("The return() function must be used inside a procedure.");
            }
            System.out.println("The return() function must be used inside a procedure.");
        } catch (Throwable t) {
            System.out.println("An unexpected exception occured during the execution of a script.");
            t.printStackTrace();
            if(p != null){
                p.sendMessage("An unexpected exception occured during the execution of your script. Please check the console for more information.");
            }
        }
        if (done != null) {
            done.done(null);
        }
    }

    /**
     * Runs eval on the code tree, and if it returns an ival, resolves it.
     * @param c
     * @param env
     * @return
     */
    public Construct seval(ParseTree c, final Environment env){
        Construct ret = eval(c, env);
        while(ret instanceof IVariable){
            IVariable cur = (IVariable)ret;
            ret = env.getEnv(GlobalEnv.class).GetVarList().get(cur.getName(), cur.getTarget()).ival();
        }
        return ret;
    }

	/**
	 * Given the parse tree and environment, executes the tree.
	 * @param c
	 * @param env
	 * @return
	 * @throws CancelCommandException
	 */
    public Construct eval(ParseTree c, final Environment env) throws CancelCommandException {
		if(env.getEnv(GlobalEnv.class).IsInterrupted()){
			//First things first, if we're interrupted, kill the script
			//unconditionally.
			throw new CancelCommandException("", Target.UNKNOWN);
		}
        final Construct m = c.getData();
        CurrentEnv = env;
		//TODO: Reevaluate if this line is needed. The script doesn't know the label inherently, the
		//environment does, and setting it this way taints the environment.
        CurrentEnv.getEnv(GlobalEnv.class).SetLabel(this.label);
        if (m.getCType() == ConstructType.FUNCTION) {
                env.getEnv(GlobalEnv.class).SetScript(this);
                if (m.val().charAt(0) == '_' && m.val().charAt(1) != '_') {
                    //Not really a function, so we can't put it in Function.
                    Procedure p = getProc(m.val());
                    if (p == null) {
                        throw new ConfigRuntimeException("Unknown procedure \"" + m.val() + "\"", ExceptionType.InvalidProcedureException, m.getTarget());
                    }
                    Environment newEnv = env;
                    try{
                        newEnv = env.clone();
                    } catch(CloneNotSupportedException e){}
					ProfilePoint pp = env.getEnv(GlobalEnv.class).GetProfiler().start(m.val() + " execution", LogLevel.INFO);
                    Construct ret;
					try {
						ret = p.cexecute(c.getChildren(), newEnv, m.getTarget());
					} finally {
						pp.stop();
					}
					return ret;
                }
                final Function f;
                try{
                    f = (Function)FunctionList.getFunction(m);
                } catch(ConfigCompileException e){
                    //Turn it into a config runtime exception. This shouldn't ever happen though.
                    throw ConfigRuntimeException.CreateUncatchableException("Unable to find function " + m.val(), m.getTarget());
                }

				ArrayList<Construct> args = new ArrayList<Construct>();
                try{
					if (f.isRestricted()) {
						boolean perm = Static.hasCHPermission(f.getName(), env);
						if (!perm) {
							throw new ConfigRuntimeException("You do not have permission to use the " + f.getName() + " function.",
									ExceptionType.InsufficientPermissionException, m.getTarget());
						}
					}

					if(f.useSpecialExec()){
						ProfilePoint p = null;
						if(f.shouldProfile() && env.getEnv(GlobalEnv.class).GetProfiler() != null && env.getEnv(GlobalEnv.class).GetProfiler().isLoggable(f.profileAt())){
							p = env.getEnv(GlobalEnv.class).GetProfiler().start(f.profileMessageS(c.getChildren()), f.profileAt());
						}
						Construct ret;
						try {
							ret = f.execs(m.getTarget(), env, this, c.getChildren().toArray(new ParseTree[]{}));
						} finally {
							if(p != null){
								p.stop();
							}
						}
						return ret;
					}

					for (ParseTree c2 : c.getChildren()) {
						args.add(eval(c2, env));
					}
					Object[] a = args.toArray();
					Construct[] ca = new Construct[a.length];
					for (int i = 0; i < a.length; i++) {
						ca[i] = (Construct) a[i];
						//CArray, CBoolean, CDouble, CInt, CNull, CString, CVoid, CEntry, CLabel (only to sconcat).
						if (!(ca[i] instanceof CArray || ca[i] instanceof CBoolean || ca[i] instanceof CDouble
								|| ca[i] instanceof CInt || ca[i] instanceof CNull
								|| ca[i] instanceof CString || ca[i] instanceof CVoid
								|| ca[i] instanceof IVariable || ca[i] instanceof CEntry || ca[i] instanceof CLabel)
								&& (!f.getName().equals("__autoconcat__") && (ca[i] instanceof CLabel))) {
							throw new ConfigRuntimeException("Invalid Construct ("
									+ ca[i].getClass() + ") being passed as an argument to a function ("
									+ f.getName() + ")", null, m.getTarget());
						}
						while(f.preResolveVariables() && ca[i] instanceof IVariable){
							IVariable cur = (IVariable)ca[i];
							ca[i] = env.getEnv(GlobalEnv.class).GetVarList().get(cur.getName(), cur.getTarget()).ival();
						}
					}

					{
						//It takes a moment to generate the toString of some things, so lets not do it
						//if we actually aren't going to profile
						ProfilePoint p = null;
						if(f.shouldProfile() && env.getEnv(GlobalEnv.class).GetProfiler() != null && env.getEnv(GlobalEnv.class).GetProfiler().isLoggable(f.profileAt())){
							p = env.getEnv(GlobalEnv.class).GetProfiler().start(f.profileMessage(ca), f.profileAt());
						}
						Construct ret;
						try {
							ret = f.exec(m.getTarget(), env, ca);
						} finally {
							if(p != null){
								p.stop();
							}
						}
						return ret;
					}
				//We want to catch and rethrow the ones we know how to catch, and then
				//catch and report anything else.
				} catch(ConfigRuntimeException | ProgramFlowManipulationException e){
					throw e;
				} catch(InvalidEnvironmentException e){
					if(!e.isDataSet()){
						e.setData(f.getName());
					}
					throw e;
				} catch(Exception e){
					String version = "Unknown";
					try{
						version = Main.loadSelfVersion();
					} catch(Exception ex){
						//Ignored
					}
					String brand = Implementation.GetServerType().getBranding();
					outer: for(ExtensionTracker tracker : ExtensionManager.getTrackers().values()){
						for(FunctionBase b : tracker.getFunctions()){
							if(b.getName().equals(f.getName())){
								//This extension provided the function, so its the culprit. Report this
								//name instead of the core plugin's name.
								for(Extension extension : tracker.getExtensions()){
									brand = extension.getName();
									break outer;
								}
							}
						}
					}
					String emsg = TermColors.RED + "Uh oh! You've found an error in " + TermColors.CYAN + brand + TermColors.RED
							 + ".\nThis is an error caused while running your code, so you may be able to find a workaround,"
							+ " but is ultimately an error in " + brand
							+ " itself.\nThe line of code that caused the error was this:\n" + TermColors.WHITE;
					List<String> args2 = new ArrayList<>();
					Map<String, String> vars = new HashMap<>();

					for(Construct cc : args){
						if(cc instanceof IVariable){
							Construct ccc = env.getEnv(GlobalEnv.class).GetVarList().get(((IVariable)cc).getName(), cc.getTarget()).ival();
							String vval = ccc.val();
							if(ccc instanceof CString){
								vval = ccc.asString().getQuote();
							}
							vars.put(((IVariable)cc).getName(), vval);
						}
						if(cc == null){
							args2.add("java-null");
						} else if(cc instanceof CString){
							args2.add(cc.asString().getQuote());
						} else if(cc instanceof IVariable){
							args2.add(((IVariable)cc).getName());
						} else {
							args2.add(cc.val());
						}
					}
					//Server might not be available in this platform, so let's be sure to ignore those exceptions
					String modVersion = "Unsupported platform";
					try{
						modVersion = StaticLayer.GetConvertor().GetServer().getModVersion();
					} catch(Exception ex){
						modVersion = Implementation.GetServerType().name();
					}
					if(!vars.isEmpty()){
						emsg += StringUtils.Join(vars, " = ", "\n") + "\n";
					}
					String extensionData = "";
					for(ExtensionTracker tracker : ExtensionManager.getTrackers().values()){
						for(Extension extension : tracker.getExtensions()){
							try {
								extensionData += TermColors.CYAN + extension.getName() + TermColors.RED
										+ " (version " + TermColors.RESET + extension.getVersion() + TermColors.RED + ");\n";
							} catch(AbstractMethodError ex){
								// This happens with an old style extensions. Just skip it.
								extensionData += TermColors.CYAN + "Unknown Extension" + TermColors.RED
										+ " (unknown version);\n";
							}
						}
					}
					if(extensionData.equals("")){
						extensionData = "No extensions are loaded.\n";
					}
					emsg += f.getName() + "(";
					emsg += StringUtils.Join(args2, ", ");
					emsg += ")\n" + TermColors.RED + "on or around "
							+ TermColors.YELLOW + m.getTarget().file() + TermColors.WHITE + ":" + TermColors.CYAN + m.getTarget().line() + TermColors.RED
							+ ".\nPlease report this error to the developers, and be sure to include the version numbers:\n"
							+ TermColors.CYAN + "Server " + TermColors.RED + "version: " + TermColors.RESET + modVersion + TermColors.RED + ";\n"
							+ TermColors.CYAN + Implementation.GetServerType().getBranding() + TermColors.RED + " version: " + TermColors.RESET
								+ version + TermColors.RED + ";\n"
							+ "Loaded extensions and versions:\n"
							+ extensionData
							+ "Here's the stacktrace:\n" + TermColors.RESET;
					emsg += Static.GetStacktraceString(e);
					Static.getLogger().log(Level.SEVERE, emsg);
					throw new CancelCommandException(null, Target.UNKNOWN);
				}
        } else if (m.getCType() == ConstructType.VARIABLE) {
            return new CString(m.val(), m.getTarget());
        } else {
            return m;
        }
    }

    public boolean match(String command) {
        if(cleft == null){
            //The compilation error happened during the signature declaration, so
            //we can't match it, nor can we even tell if it's what they intended for us to run.
            return false;
        }
        boolean case_sensitive = Prefs.CaseSensitive();
        String[] cmds = command.split(" ");
        List<String> args = new ArrayList(Arrays.asList(cmds));
        boolean isAMatch = true;
        StringBuilder lastVar = new StringBuilder();
        int lastJ = 0;
        try {
            for (int j = 0; j < cleft.size(); j++) {
                if (!isAMatch) {
                    break;
                }
                lastJ = j;
                Construct c = cleft.get(j);
                String arg = args.get(j);
                if (c.getCType() != ConstructType.VARIABLE) {
                    if (case_sensitive && !c.val().equals(arg) || !case_sensitive && !c.val().equalsIgnoreCase(arg)) {
                        isAMatch = false;
                        continue;
                    }
                } else {
                    //It's a variable. If it's optional, the rest of them are optional too, so as long as the size of
                    //args isn't greater than the size of cleft, it's a match
                    if (((Variable) c).isOptional()) {
                        if (args.size() <= cleft.size()) {
                            return true;
                        } else {
                            Construct fin = cleft.get(cleft.size() - 1);
                            if (fin instanceof Variable) {
                                if (((Variable) fin).isFinal()) {
                                    return true;
                                }
                            }
                            return false;
                        }
                    }
                }
                if (j == cleft.size() - 1) {
                    if (cleft.get(j).getCType() == ConstructType.VARIABLE) {
                        Variable lv = (Variable) cleft.get(j);
                        if (lv.isFinal()) {
                            for (int a = j; a < args.size(); a++) {
                                if (lastVar.length() == 0) {
                                    lastVar.append(args.get(a));
                                } else {
                                    lastVar.append(" ").append(args.get(a));
                                }
                            }
                        }
                    }
                }
            }
        } catch (IndexOutOfBoundsException e) {
            if (cleft.get(lastJ).getCType() != ConstructType.VARIABLE
                    || cleft.get(lastJ).getCType() == ConstructType.VARIABLE
                    && !((Variable) cleft.get(lastJ)).isOptional()) {
                isAMatch = false;
            }
        }
        boolean lastIsFinal = false;
        if (cleft.get(cleft.size() - 1) instanceof Variable) {
            Variable v = (Variable) cleft.get(cleft.size() - 1);
            if (v.isFinal()) {
                lastIsFinal = true;
            }
        }
        if ((cleft.get(lastJ) instanceof Variable && ((Variable) cleft.get(lastJ)).isOptional())) {
            return true;
        }

        if (cleft.size() != cmds.length && !lastIsFinal) {
            isAMatch = false;
        }
        return isAMatch;
    }

    public List<Variable> getVariables(String command) {
        String[] cmds = command.split(" ");
        List<String> args = new ArrayList(Arrays.asList(cmds));

        StringBuilder lastVar = new StringBuilder();

        ArrayList<Variable> vars = new ArrayList<Variable>();
        Variable v = null;
        for (int j = 0; j < cleft.size(); j++) {
            try {
                if (cleft.get(j).getCType() == ConstructType.VARIABLE) {
                    if (((Variable) cleft.get(j)).getName().equals("$")) {
                        for (int k = j; k < args.size(); k++) {
                            lastVar.append(args.get(k).trim()).append(" ");
                        }
                        v = new Variable(((Variable) cleft.get(j)).getName(),
                                lastVar.toString().trim(), Target.UNKNOWN);
                    } else {
                        v = new Variable(((Variable) cleft.get(j)).getName(),
                                args.get(j), Target.UNKNOWN);
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                v = new Variable(((Variable) cleft.get(j)).getName(),
                        ((Variable) cleft.get(j)).getDefault(), Target.UNKNOWN);
            }
            if (v != null) {
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
        boolean inside_opt_var = false;
        boolean after_no_def_opt_var = false;
        String lastVar = null;
        //Go through our token list and readjust non-spaced symbols. Any time we combine a symbol,
        //the token becomes a string
        List<Token> tempLeft = new ArrayList<Token>();
        for(int i = 0; i < left.size(); i++){
            Token t = left.get(i);
            if(i == 0 && t.type == TType.NEWLINE){
                continue;
            }
            if(t.type.isSymbol() && left.size() - 1 > i && left.get(i + 1).type != TType.WHITESPACE){
                StringBuilder b = new StringBuilder();
                b.append(t.value);
                i++;
                Token m = left.get(i);
                while(m.type.isSymbol() && m.type != TType.WHITESPACE){
                    b.append(m.value);
                    i++;
                    m = left.get(i);
                }

                if(m.type != TType.WHITESPACE && m.type != TType.LABEL){
                    b.append(m.value);
                }
                t = new Token(TType.STRING, b.toString(), t.target);
                if(m.type == TType.LABEL){
                    tempLeft.add(t);
                    tempLeft.add(m);
                    continue;
                }
            }
            //Go ahead and toString the other symbols too
            if(t.type.isSymbol()){
                t = new Token(TType.STRING, t.value, t.target);
            }
            if(t.type != TType.WHITESPACE){
                tempLeft.add(t);
            }

        }
        //Look through and concatenate all tokens before the label, if such exists.
        boolean hasLabel = false;
        for(int i = 0; i < tempLeft.size(); i++){
            if(tempLeft.get(i).type == TType.LABEL){
                hasLabel = true;
                break;
            }
        }
        if(hasLabel){
            StringBuilder b = new StringBuilder();
            int count = 0;
            while(tempLeft.get(count).type != TType.LABEL){
                b.append(tempLeft.get(count).val());
                count++;
            }
            tempLeft.set(0, new Token(TType.STRING, b.toString(), Target.UNKNOWN));
            for(int i = 0; i < count - 1; i++){
                tempLeft.remove(1);
            }
        }
        left = tempLeft;
        for (int j = 0; j < left.size(); j++) {
            Token t = left.get(j);
            //Token prev_token = j - 2 >= 0?c.tokens.get(j - 2):new Token(TType.UNKNOWN, "", t.line_num);
            Token last_token = j - 1 >= 0 ? left.get(j - 1) : new Token(TType.UNKNOWN, "", t.getTarget());
            Token next_token = j + 1 < left.size() ? left.get(j + 1) : new Token(TType.UNKNOWN, "", t.getTarget());
            Token after_token = j + 2 < left.size() ? left.get(j + 2) : new Token(TType.UNKNOWN, "", t.getTarget());

            if (j == 0) {
                if (next_token.type == TType.LABEL) {
                    this.label = t.val();
                    j--;
                    left.remove(0);
                    left.remove(0);
                    continue;
                }
            }

            if (t.type == TType.LABEL) {
                continue;
            }

            if (t.type.equals(TType.FINAL_VAR) && left.size() - j >= 5) {
                throw new ConfigCompileException("FINAL_VAR must be the last argument in the alias", t.target);
            }
            if (t.type.equals(TType.VARIABLE) || t.type.equals(TType.FINAL_VAR)) {
                Variable v = new Variable(t.val(), null, t.target);
                lastVar = t.val();
                v.setOptional(last_token.type.equals(TType.LSQUARE_BRACKET));
                left_vars.put(t.val(), v);
                if (v.isOptional()) {
                    after_no_def_opt_var = true;
                } else {
                    v.setDefault("");
                }
            }
            //We're looking for a command up front
            if (j == 0 && !t.value.startsWith("/")) {
                if (!(next_token.type == TType.LABEL && after_token.type == TType.COMMAND)) {
                    throw new ConfigCompileException("Expected command (/command) at start of alias."
                            + " Instead, found " + t.type + " (" + t.val() + ")", t.target);
                }
            }
            if (last_token.type.equals(TType.LSQUARE_BRACKET)) {
                inside_opt_var = true;
                if (!(t.type.equals(TType.FINAL_VAR) || t.type.equals(TType.VARIABLE))) {
                    throw new ConfigCompileException("Unexpected " + t.type.toString() + " (" + t.val() + "), was expecting"
                            + " a $variable", t.target);
                }
            }
            if (after_no_def_opt_var && !inside_opt_var) {
                if (t.type.equals(TType.VARIABLE) || t.type.equals(TType.FINAL_VAR)) {
                    throw new ConfigCompileException("You cannot have anything other than optional arguments after your"
                            + " first optional argument.", t.target);
                }
            }
            if (!t.type.equals(TType.LSQUARE_BRACKET)
                    && !t.type.equals(TType.OPT_VAR_ASSIGN)
                    && !t.type.equals(TType.RSQUARE_BRACKET)
                    && !t.type.equals(TType.VARIABLE)
                    && !t.type.equals(TType.LIT)
                    && !t.type.equals(TType.COMMAND)
                    && !t.type.equals(TType.FINAL_VAR)) {
                if (j - 1 > 0 && !(/*t.type.equals(TType.STRING) &&*/ left.get(j - 1).type.equals(TType.OPT_VAR_ASSIGN))) {
                    throw new ConfigCompileException("Unexpected " + t.type + " (" + t.val() + ")", t.target);
                }
            }
            if (last_token.type.equals(TType.COMMAND)) {
                if (!(t.type.equals(TType.VARIABLE) || t.type.equals(TType.LSQUARE_BRACKET) || t.type.equals(TType.FINAL_VAR)
                        || t.type.equals(TType.LIT) || t.type.equals(TType.STRING))) {
                    throw new ConfigCompileException("Unexpected " + t.type + " (" + t.val() + ") after command", t.target);
                }
            }
            if (inside_opt_var && t.type.equals(TType.OPT_VAR_ASSIGN)) {
                if (!((next_token.type.equals(TType.STRING) || next_token.type.equals(TType.LIT)) && after_token.type.equals(TType.RSQUARE_BRACKET)
                        || (next_token.type.equals(TType.RSQUARE_BRACKET)))) {
                    throw new ConfigCompileException("Unexpected token in optional variable", t.target);
                } else if (next_token.type.equals(TType.STRING) || next_token.type.equals(TType.LIT)) {
                    left_vars.get(lastVar).setDefault(next_token.val());
                }
            }
            if (t.type.equals(TType.RSQUARE_BRACKET)) {
                if (!inside_opt_var) {
                    throw new ConfigCompileException("Unexpected " + t.type.toString(), t.target);
                }
                inside_opt_var = false;
            }
        }

        return true;
    }

    private boolean compileLeft() {
        cleft = new ArrayList<Construct>();
		if(label != null && label.startsWith("!")){
			if(label.length() > 1){
				label = label.substring(1);
			}
			nolog = true;
		}
        for (int i = 0; i < left.size(); i++) {
            Token t = left.get(i);
            if (t.value.startsWith("/")) {
                cleft.add(new Command(t.val(), t.target));
            } else if (t.type == Token.TType.VARIABLE) {
                cleft.add(new Variable(t.val(), null, t.target));
            } else if (t.type.equals(TType.FINAL_VAR)) {
                Variable v = new Variable(t.val(), null, t.target);
                v.setFinal(true);
                cleft.add(v);
            } else if (t.type.equals(TType.LSQUARE_BRACKET)) {
                if (i + 2 < left.size() && left.get(i + 2).type.equals(TType.OPT_VAR_ASSIGN)) {
                    Variable v = new Variable(left.get(i + 1).val(),
                            left.get(i + 3).val(), t.target);
                    v.setOptional(true);
                    if (left.get(i + 1).type.equals(TType.FINAL_VAR)) {
                        v.setFinal(true);
                    }
                    cleft.add(v);
                    i += 4;
                } else {
                    t = left.get(i + 1);
                    Variable v = new Variable(t.val(), null, t.target);
                    v.setOptional(true);
                    if (t.val().equals("$")) {
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
        List<Token> temp = new ArrayList<Token>();
        right = new ArrayList<List<Token>>();
        for (Token t : fullRight) {
            if (t.type == TType.SEPERATOR) {
                right.add(temp);
                temp = new ArrayList<Token>();
            } else {
                if(t.type == TType.WHITESPACE){
                    continue; //Whitespace is ignored on the right side
                }
                temp.add(t);
            }
        }
        right.add(temp);
        cright = new ArrayList<ParseTree>();
        for (List<Token> l : right) {
            cright.add(MethodScriptCompiler.compile(l));
        }
    }

    public void checkAmbiguous(List<Script> scripts) throws ConfigCompileException {
        //for (int i = 0; i < scripts.size(); i++) {
        List<Construct> thisCommand = this.cleft;
        for (int j = 0; j < scripts.size(); j++) {
            List<Construct> thatCommand = scripts.get(j).cleft;
            if (thatCommand == null) {
                //it hasn't been compiled yet.
                return;
            }
            if (this.cleft == scripts.get(j).cleft) {
                //Of course this command is going to match it's own signature
                continue;
            }
            boolean soFarAMatch = true;
            for (int k = 0; k < thisCommand.size(); k++) {
                try {
                    Construct c1 = thisCommand.get(k);
                    Construct c2 = thatCommand.get(k);
                    if (c1.getCType() != c2.getCType() || ((c1 instanceof Variable) && !((Variable) c1).isOptional())) {
                        soFarAMatch = false;
                    } else {
                        //It's a literal, check to see if it's the same literal
                        if (c1.nval() == null || !c1.val().equals(c2.val())) {
                            soFarAMatch = false;
                        }
                    }
                } catch (IndexOutOfBoundsException e) {
                    /**
                     * The two commands:
                     * /cmd $var1 [$var2]
                     * /cmd $var1
                     * would cause this exception to be thrown, but the signatures
                     * are the same, so the fact that they've matched this far means
                     * they are ambiguous. However,
                     * /cmd $var1 $var2
                     * /cmd $var1
                     * is not ambiguous
                     */
                    //thatCommand is the short one
                    if (!(thisCommand.get(k) instanceof Variable)
                            || (thisCommand.get(k) instanceof Variable
                            && !((Variable) thisCommand.get(k)).isOptional())) {
                        soFarAMatch = false;
                    }
                }
            }
            if (thatCommand.size() > thisCommand.size()) {
                int k = thisCommand.size();
                //thisCommand is the short one
                if (!(thatCommand.get(k) instanceof Variable)
                        || (thatCommand.get(k) instanceof Variable
                        && !((Variable) thatCommand.get(k)).isOptional())) {
                    soFarAMatch = false;
                }
            }

            if (soFarAMatch) {
                String commandThis = "";
                for (Construct c : thisCommand) {
                    commandThis += c.val() + " ";
                }
                String commandThat = "";
                for (Construct c : thatCommand) {
                    commandThat += c.val() + " ";
                }
                scripts.get(j).compilerError = true;
                this.compilerError = true;
                throw new ConfigCompileException("The command " + commandThis.trim() + " is ambiguous because it "
                        + "matches the signature of " + commandThat.trim() + " defined at " + thatCommand.get(0).getTarget(), thisCommand.get(0).getTarget());
            }
        }
    }

    /**
     * This is only used by scriptas to hack the label in and out.
     * @param label
     */
    public void setLabel(String label) {
        this.label = label;
    }

	public boolean doLog(){
		return !nolog;
	}

}
