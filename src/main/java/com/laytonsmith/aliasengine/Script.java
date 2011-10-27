/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine;

import com.laytonsmith.aliasengine.exceptions.CancelCommandException;
import com.laytonsmith.aliasengine.exceptions.ConfigCompileException;
import com.laytonsmith.aliasengine.exceptions.ConfigRuntimeException;
import com.laytonsmith.aliasengine.exceptions.LoopBreakException;
import com.laytonsmith.aliasengine.exceptions.LoopContinueException;
import com.laytonsmith.aliasengine.Constructs.*;
import com.laytonsmith.aliasengine.Constructs.Construct.ConstructType;
import com.laytonsmith.aliasengine.Constructs.Token.TType;
import com.laytonsmith.aliasengine.Constructs.Variable;
import com.laytonsmith.aliasengine.functions.BasicLogic._if;
import com.laytonsmith.aliasengine.functions.DataHandling._for;
import com.laytonsmith.aliasengine.functions.DataHandling.call_proc;
import com.laytonsmith.aliasengine.functions.DataHandling.foreach;
import com.laytonsmith.aliasengine.functions.DataHandling.include;
import com.laytonsmith.aliasengine.functions.DataHandling.is_proc;
import com.laytonsmith.aliasengine.functions.DataHandling.proc;
import com.laytonsmith.aliasengine.functions.EventBinding.bind;
import com.laytonsmith.aliasengine.functions.Exceptions.ExceptionType;
import com.laytonsmith.aliasengine.functions.Exceptions._try;
import com.laytonsmith.aliasengine.functions.Function;
import com.laytonsmith.aliasengine.functions.FunctionList;
import com.laytonsmith.aliasengine.functions.IVariableList;
import com.laytonsmith.aliasengine.functions.IncludeCache;
import com.laytonsmith.aliasengine.functions.Meta.eval;
import com.laytonsmith.aliasengine.exceptions.FunctionReturnException;
import com.laytonsmith.aliasengine.functions.Meta.scriptas;
import com.sk89q.bukkit.migration.PermissionsResolverManager;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

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
 * @author Layton
 */
public class Script {

    private List<Token> left;
    private List<List<Token>> right;
    private List<Token> fullRight;
    private List<Construct> cleft;
    private List<GenericTreeNode<Construct>> cright;
    private Map<String, Variable> left_vars;
    boolean hasBeenCompiled = false;
    boolean compilerError = false;
    private Env OriginalEnv;
    private Env CurrentEnv;

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        for (Token t : left) {
            b.append(t.val()).append(" ");
        }
        b.append("compiled: ").append(hasBeenCompiled).append("; errors? ").append(compilerError);
        return b.toString();
    }

    private Procedure getProc(String name) {
        return CurrentEnv.GetProcs().get(name);
    }
    
    public Env getCurrentEnv(){
        return CurrentEnv;
    }
    
    public Env getOriginalEnv(){
        return OriginalEnv;
    }

    public Script(List<Token> left, List<Token> right, Env env) {
        this.left = left;
        this.fullRight = right;
        this.left_vars = new HashMap<String, Variable>();
        this.OriginalEnv = env;
    }
    
    private Script(){}
    
    public static Script GenerateScript(GenericTreeNode<Construct> tree, Env env){
        Script s = new Script();
        
        s.hasBeenCompiled = true;
        s.compilerError = false;
        s.cright = new ArrayList<GenericTreeNode<Construct>>();
        s.cright.add(tree);
        s.OriginalEnv = env;
        GenericTree<Construct> root = new GenericTree<Construct>();
        root.setRoot(tree);
        for(GenericTreeNode<Construct> node : root.build(GenericTreeTraversalOrderEnum.PRE_ORDER)){
            if(node.getData() instanceof IVariable){
                s.OriginalEnv.GetVarList().set((IVariable)node.getData());
            }
        }
        
        return s;
    }

    public boolean uncompilable() {
        return compilerError;
    }

    public void run(final List<Variable> vars, Env myEnv, final MScriptComplete done) {
        //Some things, such as the label are determined at compile time
        this.CurrentEnv = myEnv;
        this.CurrentEnv.SetLabel(this.OriginalEnv.GetLabel());
        CommandSender p = myEnv.GetCommandSender();
        if (!hasBeenCompiled || compilerError) {
            int line_num = 0;
            if (left.size() >= 1) {
                line_num = left.get(0).line_num;
            }
            throw new ConfigRuntimeException("Unable to run command, script not yet compiled, or a compiler error occured for that command.",
                    null, line_num, null);
        }
        if (p instanceof Player) {
            if (CurrentEnv.GetLabel() != null) {
                PermissionsResolverManager perms = Static.getPermissionsResolverManager();
                String[] groups = CurrentEnv.GetLabel().substring(1).split("/");
                for (String group : groups) {
                    if (group.startsWith("-") && perms.inGroup(((Player)p).getName(), group.substring(1))) {
                        //negative permission
                        throw new ConfigRuntimeException("You do not have permission to use that command", ExceptionType.InsufficientPermissionException,
                                0, null);
                    } else if (perms.inGroup(((Player)p).getName(), group)) {
                        //They do have permission.
                        break;
                    }
                }
            }
        }

//        final Plugin self = CommandHelperPlugin.self;
//        Static.getServer().getScheduler().scheduleAsyncDelayedTask(self, new Runnable() {

//            public void run() {
        try {
            for (GenericTreeNode<Construct> rootNode : cright) {
                GenericTree<Construct> tree = new GenericTree<Construct>();
                tree.setRoot(rootNode);
                for (GenericTreeNode<Construct> tempNode : tree.build(GenericTreeTraversalOrderEnum.PRE_ORDER)) {
                    if (tempNode.data instanceof Variable) {
                        ((Variable) tempNode.data).setVal(
                                Static.resolveConstruct(
                                Static.resolveDollarVar(left_vars.get(((Variable) tempNode.data).getName()), vars).toString(), tempNode.data.getLineNum(), tempNode.data.getFile()));
                    }
                }
                File auto_include = new File("plugins/CommandHelper/auto_include.ms");
                if (auto_include.exists()) {
                    MScriptCompiler.execute(IncludeCache.get(auto_include, 0, auto_include), CurrentEnv, null, this);
                }
                MScriptCompiler.execute(tree.getRoot(), CurrentEnv, done, this);
            }
        } catch (ConfigRuntimeException e) {
            System.out.println(e.getMessage() + " :: " + e.getExceptionType() + ":" + e.getFile() + ":" + e.getLineNum());
            p.sendMessage(e.getMessage() + " :: " + e.getExceptionType() + ":" + e.getSimpleFile() + ":" + e.getLineNum());
        } catch (CancelCommandException e) {
            //p.sendMessage(e.getMessage());
            //The message in the exception is actually empty
        } catch (LoopBreakException e) {
            p.sendMessage("The break() function must be used inside a for() or foreach() loop");
        } catch (LoopContinueException e) {
            p.sendMessage("The continue() function must be used inside a for() or foreach() loop");
        } catch (FunctionReturnException e) {
            p.sendMessage("The return() function must be used inside a procedure.");
        } catch (Throwable t) {
            System.out.println("An unexpected exception occured during the execution of a script.");
            t.printStackTrace();
            p.sendMessage("An unexpected exception occured during the execution of your script. Please check the console for more information.");
        }
        if (done != null) {
            done.done(null);
        }
//            }
//        });
    }

    public Construct eval(GenericTreeNode<Construct> c, final Env env) throws CancelCommandException {
        final Construct m = c.getData();
        CurrentEnv = env;
        CurrentEnv.SetLabel(OriginalEnv.GetLabel());
        if (m.getCType() == ConstructType.FUNCTION) {
                env.SetScript(this);
                if (m.val().matches("^_[^_].*")) {
                    //Not really a function, so we can't put it in Function.
                    Procedure p = getProc(m.val());
                    if (p == null) {
                        throw new ConfigRuntimeException("Unknown procedure \"" + m.val() + "\"", ExceptionType.InvalidProcedureException, m.getLineNum(), m.getFile());
                    }
                    List<Construct> variables = new ArrayList<Construct>();
                    for (GenericTreeNode<Construct> child : c.getChildren()) {
                        variables.add(eval(child, env));
                    }
                    variables = Arrays.asList(preResolveVariables(variables.toArray(new Construct[]{})));
                    return p.execute(variables, env.clone());
                }
                final Function f;
                try{
                    f = FunctionList.getFunction(m);
                } catch(ConfigCompileException e){
                    //Turn it into a config runtime exception. This shouldn't ever happen though.
                    throw new ConfigRuntimeException("Unable to find function " + m.val(), m.getLineNum(), m.getFile());
                }
                //We have special handling for loop and other control flow functions
                if (f instanceof _for) {
                    _for fr = (_for) f;
                    List<GenericTreeNode<Construct>> ch = c.getChildren();
                    try {
                        return fr.execs(m.getLineNum(), m.getFile(), env, this, ch.get(0), ch.get(1), ch.get(2), ch.get(3));
                    } catch (IndexOutOfBoundsException e) {
                        throw new ConfigRuntimeException("Invalid number of parameters passed to for", ExceptionType.InsufficientArgumentsException, m.getLineNum(), m.getFile());
                    }
                } else if (f instanceof _if) {
                    _if fr = (_if) f;
                    List<GenericTreeNode<Construct>> ch = c.getChildren();
                    try {
                        return fr.execs(m.getLineNum(), m.getFile(), env, this, ch.get(0), ch.get(1), ch.size() > 2 ? ch.get(2) : null);
                    } catch (IndexOutOfBoundsException e) {
                        throw new ConfigRuntimeException("Invalid number of parameters passed to if", ExceptionType.InsufficientArgumentsException, m.getLineNum(), m.getFile());
                    }
                } else if (f instanceof foreach) {
                    foreach fe = (foreach) f;
                    List<GenericTreeNode<Construct>> ch = c.getChildren();
                    try {
                        return fe.execs(m.getLineNum(), m.getFile(), env, this, ch.get(0), ch.get(1), ch.get(2));
                    } catch (IndexOutOfBoundsException e) {
                        throw new ConfigRuntimeException("Invalid number of parameters passed to foreach", ExceptionType.InsufficientArgumentsException, m.getLineNum(), m.getFile());
                    }
                } else if (f instanceof eval) {
                    List<GenericTreeNode<Construct>> ch = c.getChildren();
                    if (ch.size() > 1) {
                        throw new ConfigRuntimeException("Invalid number of parameters passed to eval", ExceptionType.InsufficientArgumentsException, m.getLineNum(), m.getFile());
                    }
                    try{
                        GenericTreeNode<Construct> root = MScriptCompiler.compile(MScriptCompiler.lex(ch.get(0).getData().val(), null));
                        StringBuilder b = new StringBuilder();
                        for (GenericTreeNode<Construct> child : root.getChildren()) {
                            CString cs = new CString(eval(child, env).val(), 0, null);
                            if (!cs.val().trim().equals("")) {
                                b.append(cs.val()).append(" ");
                            }
                        }
                        return new CString(b.toString(), 0, null);
                    } catch(ConfigCompileException e){
                        
                    }
                } else if (f instanceof _try) {
                    List<GenericTreeNode<Construct>> ch = c.getChildren();
                    if (ch.size() != 4 && ch.size() != 3) {
                        throw new ConfigRuntimeException("Invalid number of parameters passed to try", ExceptionType.InsufficientArgumentsException, m.getLineNum(), m.getFile());
                    }
                    GenericTreeNode<Construct> fourth = null;
                    if (ch.size() == 4) {
                        fourth = ch.get(3);
                    }
                    return ((_try) f).execs(m.getLineNum(), m.getFile(), env, this, ch.get(0), ch.get(1), ch.get(2), fourth);
                } else if (f instanceof proc) {
                    List<GenericTreeNode<Construct>> ch = c.getChildren();
                    if (ch.size() <= 1) {
                        throw new ConfigRuntimeException("Invalid number of parameters sent to proc()", ExceptionType.InvalidProcedureException, m.getLineNum(), m.getFile());
                    }
                    String name = "";
                    List<IVariable> vars = new ArrayList<IVariable>();
                    GenericTreeNode<Construct> tree = null;
                    for (int i = 0; i < ch.size(); i++) {
                        if (i == ch.size() - 1) {
                            tree = ch.get(i);
                        } else {
                            Construct cons = eval(ch.get(i), env);
                            if (i == 0 && cons instanceof IVariable) {
                                //Soon, this will be allowed, so anonymous procedures can be created, but for now
                                //it's not allowed
                                throw new ConfigRuntimeException("Anonymous Procedures are not allowed", ExceptionType.InvalidProcedureException, m.getLineNum(), m.getFile());
                            } else {
                                if (i == 0 && !(cons instanceof IVariable)) {
                                    name = cons.val();
                                } else {
                                    if (!(cons instanceof IVariable)) {
                                        throw new ConfigRuntimeException("You must use IVariables as the arguments", ExceptionType.InvalidProcedureException, m.getLineNum(), m.getFile());
                                    } else {
                                        vars.add((IVariable) cons);
                                    }
                                }
                            }
                        }
                    }
                    Procedure myProc = new Procedure(name, vars, tree, (CFunction) c.getData());
                    env.GetProcs().put(name, myProc);
                    return new CVoid(m.getLineNum(), m.getFile());                
                } else if (f instanceof call_proc) {
                    Construct[] ar = new Construct[c.getChildren().size()];
                    for (int i = 0; i < c.getChildren().size(); i++) {
                        ar[i] = eval(c.getChildAt(i), env);
                    }
                    ar = preResolveVariables(ar);
                    return ((call_proc) f).execs(m.getLineNum(), m.getFile(), env, ar);
                } else if (f instanceof include) {
                    return ((include) f).execs(m.getLineNum(), m.getFile(), env, c.getChildren(), this);
                } else if(f instanceof bind){
                    if(c.getChildren().size() < 5){
                        throw new ConfigRuntimeException("bind accepts 5 or more parameters", ExceptionType.InsufficientArgumentsException, m.getLineNum(), m.getFile());
                    }
                    Construct name = preResolveVariable(eval(c.getChildAt(0), env));
                    Construct options = preResolveVariable(eval(c.getChildAt(1), env));
                    Construct prefilter = preResolveVariable(eval(c.getChildAt(2), env));
                    Construct event_object = eval(c.getChildAt(3), env);
                    IVariableList custom_params = new IVariableList();
                    for(int a = 0; a < c.getChildren().size() - 5; a++){
                        Construct var = eval(c.getChildAt(4 + a), env);
                        if(!(var instanceof IVariable)){
                            throw new ConfigRuntimeException("The custom parameters must be ivariables", ExceptionType.CastException, m.getLineNum(), m.getFile());
                        }
                        custom_params.set((IVariable)var);
                    }
                    Env newEnv = env.clone();
                    newEnv.SetVarList(custom_params);
                    GenericTreeNode<Construct> tree = c.getChildAt(c.getChildren().size() - 1);
                    return ((bind)f).execs(name, options, prefilter, event_object, tree, newEnv, m.getLineNum(), m.getFile());
                } else if(f instanceof scriptas){
                    Construct user = eval(c.getChildAt(0), env);
                    GenericTreeNode<Construct> script = c.getChildAt(1);
                    env.SetCustom("script_node", script);
                    return ((scriptas)f).exec(m.getLineNum(), m.getFile(), env, user);
                }


                ArrayList<Construct> args = new ArrayList<Construct>();
                for (GenericTreeNode<Construct> c2 : c.getChildren()) {
                    args.add(eval(c2, env));
                }
                if (f.isRestricted()) {
                    boolean perm = false;
                    PermissionsResolverManager perms = Static.getPermissionsResolverManager();
                    if (perms != null) {
                        if(env.GetCommandSender() instanceof Player){
                            perm = perms.hasPermission(env.GetPlayer().getName(), "ch.func.use." + f.getName())
                                    || perms.hasPermission(env.GetPlayer().getName(), "commandhelper.func.use." + f.getName());
                            if (env.GetLabel() != null && env.GetLabel().startsWith("~")) {
                                String[] groups = env.GetLabel().substring(1).split("/");
                                for (String group : groups) {
                                    if (perms.inGroup(env.GetPlayer().getName(), group)) {
                                        perm = true;
                                        break;
                                    }
                                }
                            } else {
                                if (env.GetLabel() != null && (perms.hasPermission(env.GetPlayer().getName(), "ch.alias." + env.GetLabel()))
                                        || perms.hasPermission(env.GetPlayer().getName(), "commandhelper.alias." + env.GetLabel())) {
                                    perm = true;
                                }
                            }
                        } else if(env.GetCommandSender() instanceof ConsoleCommandSender){
                            perm = true;
                        }
                    } else {
                        perm = true;
                    }
                    if (env.GetCommandSender() == null || 
                            env.GetCommandSender().isOp()) {
                        perm = true;
                    }
                    if (!perm) {
                        throw new ConfigRuntimeException("You do not have permission to use the " + f.getName() + " function.",
                                ExceptionType.InsufficientPermissionException, m.getLineNum(), m.getFile());
                    }
                }
                Object[] a = args.toArray();
                Construct[] ca = new Construct[a.length];
                for (int i = 0; i < a.length; i++) {
                    ca[i] = (Construct) a[i];
                    //CArray, CBoolean, CDouble, CInt, CNull, CString, CVoid, CEntry, CLabel (only to sconcat).
                    if (!(ca[i] instanceof CArray || ca[i] instanceof CBoolean || ca[i] instanceof CDouble
                            || ca[i] instanceof CInt || ca[i] instanceof CNull
                            || ca[i] instanceof CString || ca[i] instanceof CVoid 
                            || ca[i] instanceof IVariable || ca[i] instanceof CEntry)
                            && (!f.getName().equals("sconcat") && (ca[i] instanceof CLabel))) {
                        throw new ConfigRuntimeException("Invalid Construct (" 
                                + ca[i].getClass() + ") being passed as an argument to a function (" 
                                + f.getName() + ")", null, m.getLineNum(), m.getFile());
                    }
                    if(f.preResolveVariables() && ca[i] instanceof IVariable){
                        ca[i] = env.GetVarList().get(((IVariable)ca[i]).getName()).ival();
                    }
                }

                //TODO: Will revisit this in the future. For now, remove the ability for
                //functions to run asyncronously.
                //if(f.runAsync() == true || f.runAsync() == null){
                Construct ret = f.exec(m.getLineNum(), m.getFile(), env, ca);
                return ret;
                /*} else {
                return blockingNonThreadSafe(player, new Callable<Construct>() {
                
                public Construct call() throws Exception {
                return f.exec(m.getLineNum(), player, ca);
                }
                });
                }*/

        } else if (m.getCType() == ConstructType.VARIABLE) {
            return Static.resolveConstruct(m.val(), m.getLineNum(), m.getFile());
        } else {
            return m;
        }
    }
    public Construct preResolveVariable(Construct ca){
        Construct[] c = new Construct[1];
        c[0] = ca;
        return preResolveVariables(c)[0];
    }
    public Construct[] preResolveVariables(Construct[] ca) {
        for (int i = 0; i < ca.length; i++) {
            if (ca[i] instanceof IVariable) {
                IVariable v = (IVariable) ca[i];
                ca[i] = CurrentEnv.GetVarList().get(v.getName()).ival();
            } else if (ca[i] instanceof CArray) {
//                CArray ca2 = (CArray) ca[i];
//                Construct [] ca_raw = new Construct[ca2.size()];
//                for(int j = 0; j < ca_raw.length; j++){
//                    ca_raw[j] = ca2.get(j, 0);
//                }
//                List<Construct> resolved = Arrays.asList(preResolveVariables(ca_raw));
//                for(int j = 0; j < resolved.size(); j++){
//                    
//                }
            }
        }
        return ca;
    }

//    private Construct blockingNonThreadSafe(final Player p, Callable task) throws CancelCommandException {
//        Plugin self = CommandHelperPlugin.self;
//        try {
//            Future<Construct> f = Static.getServer().getScheduler().callSyncMethod(self, task);
//            while (!f.isDone()) {
//                Thread.sleep(10);
//            }
//            return f.get();
//        } catch (InterruptedException ex) {
//            Logger.getLogger(Script.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (ExecutionException ex) {
//            if (ex.getCause() instanceof CancelCommandException) {
//                CancelCommandException e = (CancelCommandException) ex.getCause();
//                throw e;
//            } else {
//                Logger.getLogger(Script.class.getName()).log(Level.SEVERE, null, ex.getCause());
//            }
//        }
//        return null;
//    }
    public boolean match(String command) {
        boolean case_sensitive = (Boolean)Static.getPreferences().getPreference("case-sensitive");
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
//                        || c.getCType() == ConstructType.TOKEN
//                        || c.getCType() == ConstructType.LITERAL
//                        || c.getCType() == ConstructType.STRING || ConstructType.) {
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
        ArrayList<Variable> vars = new ArrayList<Variable>();
        Variable v = null;
        for (int j = 0; j < cleft.size(); j++) {
            try {
                if (cleft.get(j).getCType() == ConstructType.VARIABLE) {
                    if (((Variable) cleft.get(j)).getName().equals("$")) {
                        v = new Variable(((Variable) cleft.get(j)).getName(),
                                lastVar.toString(), 0, null);
                    } else {
                        v = new Variable(((Variable) cleft.get(j)).getName(),
                                args.get(j), 0, null);
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                v = new Variable(((Variable) cleft.get(j)).getName(),
                        ((Variable) cleft.get(j)).getDefault(), 0, null);
            }
            if (v != null) {
                vars.add(v);
            }
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
                                lastVar.toString().trim(), 0, null);
                    } else {
                        v = new Variable(((Variable) cleft.get(j)).getName(),
                                args.get(j), 0, null);
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                v = new Variable(((Variable) cleft.get(j)).getName(),
                        ((Variable) cleft.get(j)).getDefault(), 0, null);
            }
            if (v != null) {
                vars.add(v);
            }
        }
        return vars;
    }

    public void compile() throws ConfigCompileException {
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
    }

    private boolean verifyLeft() throws ConfigCompileException {
        boolean inside_opt_var = false;
        boolean after_no_def_opt_var = false;
        String lastVar = null;
        for (int j = 0; j < left.size(); j++) {
            Token t = left.get(j);
            //Token prev_token = j - 2 >= 0?c.tokens.get(j - 2):new Token(TType.UNKNOWN, "", t.line_num);
            Token last_token = j - 1 >= 0 ? left.get(j - 1) : new Token(TType.UNKNOWN, "", t.line_num, t.file);
            Token next_token = j + 1 < left.size() ? left.get(j + 1) : new Token(TType.UNKNOWN, "", t.line_num, t.file);
            Token after_token = j + 2 < left.size() ? left.get(j + 2) : new Token(TType.UNKNOWN, "", t.line_num, t.file);

            if (j == 0) {
                if (next_token.type == TType.IDENT) {
                    OriginalEnv.SetLabel(t.val());
                    j--;
                    left.remove(0);
                    left.remove(0);
                    continue;
                }
            }

            if (t.type == TType.IDENT) {
                continue;
            }

            if (t.type.equals(TType.FINAL_VAR) && left.size() - j >= 5) {
                throw new ConfigCompileException("FINAL_VAR must be the last argument in the alias", t.line_num);
            }
            if (t.type.equals(TType.VARIABLE) || t.type.equals(TType.FINAL_VAR)) {
                Variable v = new Variable(t.val(), null, t.line_num, t.file);
                lastVar = t.val();
                v.setOptional(last_token.type.equals(TType.LSQUARE_BRACKET));
                left_vars.put(t.val(), v);
                if (v.isOptional()) {
                    after_no_def_opt_var = true;
                } else {
                    v.setDefault("");
                }
            }
            if (j == 0 && !t.type.equals(TType.COMMAND)) {
                if (!(next_token.type == TType.IDENT && after_token.type == TType.COMMAND)) {
                    throw new ConfigCompileException("Expected command (/command) at start of alias."
                            + " Instead, found " + t.type + " (" + t.val() + ")", t.line_num);
                }
            }
            if (last_token.type.equals(TType.LSQUARE_BRACKET)) {
                inside_opt_var = true;
                if (!(t.type.equals(TType.FINAL_VAR) || t.type.equals(TType.VARIABLE))) {
                    throw new ConfigCompileException("Unexpected " + t.type.toString() + " (" + t.val() + ")", t.line_num);
                }
            }
            if (after_no_def_opt_var && !inside_opt_var) {
                if (t.type.equals(TType.VARIABLE) || t.type.equals(TType.FINAL_VAR)) {
                    throw new ConfigCompileException("You cannot have anything other than optional arguments after your"
                            + " first optional argument, other that other optional arguments with no default", t.line_num);
                }
            }
            if (!t.type.equals(TType.LSQUARE_BRACKET)
                    && !t.type.equals(TType.OPT_VAR_ASSIGN)
                    && !t.type.equals(TType.RSQUARE_BRACKET)
                    && !t.type.equals(TType.VARIABLE)
                    && !t.type.equals(TType.LIT)
                    && !t.type.equals(TType.COMMAND)
                    && !t.type.equals(TType.FINAL_VAR)) {
                if (!(t.type.equals(TType.STRING) && j - 1 > 0 && left.get(j - 1).type.equals(TType.OPT_VAR_ASSIGN))) {
                    throw new ConfigCompileException("Unexpected " + t.type + " (" + t.val() + ")", t.line_num);
                }
            }
            if (last_token.type.equals(TType.COMMAND)) {
                if (!(t.type.equals(TType.VARIABLE) || t.type.equals(TType.LSQUARE_BRACKET) || t.type.equals(TType.FINAL_VAR)
                        || t.type.equals(TType.LIT))) {
                    throw new ConfigCompileException("Unexpected " + t.type + " (" + t.val() + ") after command", t.line_num);
                }
            }
            if (inside_opt_var && t.type.equals(TType.OPT_VAR_ASSIGN)) {
                if (!((next_token.type.equals(TType.STRING) || next_token.type.equals(TType.LIT)) && after_token.type.equals(TType.RSQUARE_BRACKET)
                        || (next_token.type.equals(TType.RSQUARE_BRACKET)))) {
                    throw new ConfigCompileException("Unexpected token in optional variable", t.line_num);
                } else if (next_token.type.equals(TType.STRING) || next_token.type.equals(TType.LIT)) {
                    left_vars.get(lastVar).setDefault(next_token.val());
                }
            }
            if (t.type.equals(TType.RSQUARE_BRACKET)) {
                if (!inside_opt_var) {
                    throw new ConfigCompileException("Unexpected " + t.type.toString(), t.line_num);
                }
                inside_opt_var = false;
//                if (last_token.type.equals(TType.VARIABLE)
//                        || last_token.type.equals(TType.FINAL_VAR)) {
//                    after_no_def_opt_var = true;
//                }
            }
        }

        return true;
    }

    private boolean compileLeft() {
        cleft = new ArrayList<Construct>();
        for (int i = 0; i < left.size(); i++) {
            Token t = left.get(i);
            if (t.type == Token.TType.COMMAND) {
                cleft.add(new Command(t.val(), t.line_num, t.file));
            } else if (t.type == Token.TType.VARIABLE) {
                cleft.add(new Variable(t.val(), null, t.line_num, t.file));
            } else if (t.type.equals(TType.FINAL_VAR)) {
                Variable v = new Variable(t.val(), null, t.line_num, t.file);
                v.setFinal(true);
                cleft.add(v);
            } else if (t.type.equals(TType.LSQUARE_BRACKET)) {
                if (i + 2 < left.size() && left.get(i + 2).type.equals(TType.OPT_VAR_ASSIGN)) {
                    Variable v = new Variable(left.get(i + 1).val(),
                            left.get(i + 3).val(), t.line_num, t.file);
                    v.setOptional(true);
                    if (left.get(i + 1).type.equals(TType.FINAL_VAR)) {
                        v.setFinal(true);
                    }
                    cleft.add(v);
                    i += 4;
                } else {
                    t = left.get(i + 1);
                    Variable v = new Variable(t.val(), null, t.line_num, t.file);
                    v.setOptional(true);
                    if (t.val().equals("$")) {
                        v.setFinal(true);
                    }
                    cleft.add(v);
                    i += 2;
                }
            } else {
                cleft.add(new CString(t.val(), t.line_num, t.file));
            }
        }
        return true;
    }

    public void compileRight() throws ConfigCompileException {
        List<Token> temp = new ArrayList<Token>();
        right = new ArrayList<List<Token>>();
        for (Token t : fullRight) {
            if (t.type == TType.SEPERATOR) {
                right.add(temp);
                temp = new ArrayList<Token>();
            } else {
                temp.add(t);
            }
        }
        right.add(temp);
        cright = new ArrayList<GenericTreeNode<Construct>>();
        for (List<Token> l : right) {
            cright.add(MScriptCompiler.compile(l));
        }
    }

    public void checkAmbiguous(ArrayList<Script> scripts) throws ConfigCompileException {
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
                        if (c1.val() == null || !c1.val().equals(c2.val())) {
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
                        + "matches the signature of " + commandThat.trim(), thisCommand.get(0).getLineNum());
            }
        }

        //Also, check for undefined variables on the right, and unused variables on the left
        ArrayList<String> left_copy = new ArrayList<String>();
        for (Map.Entry<String, Variable> v : left_vars.entrySet()) {
            left_copy.add(v.getValue().getName());
        }
        Arrays.asList(new String[]{}).toArray(new String[]{});
        for (GenericTreeNode<Construct> gtn : cright) {
            GenericTree<Construct> tree = new GenericTree<Construct>();
            tree.setRoot(gtn);
            List<GenericTreeNode<Construct>> builtTree = tree.build(GenericTreeTraversalOrderEnum.PRE_ORDER);
            for (GenericTreeNode<Construct> c : builtTree) {
                if (c.getData() instanceof Variable) {
                    for (Map.Entry<String, Variable> v : left_vars.entrySet()) {
                        if (v.getValue().getName().equals(((Variable) c.getData()).getName())) {
                            //Found it, remove this from the left_copy, and break
                            left_copy.remove(v.getValue().getName());
                            break;
                            //TODO: Layton!
                        }
                    }
                }
            }
        }
        //}
    }
}
