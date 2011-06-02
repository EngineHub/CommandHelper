/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine;

import com.laytonsmith.aliasengine.Constructs.*;
import com.laytonsmith.aliasengine.Constructs.Construct.ConstructType;
import com.laytonsmith.aliasengine.Constructs.Token.TType;
import com.laytonsmith.aliasengine.Constructs.Variable;
import com.laytonsmith.aliasengine.functions.BasicLogic._if;
import com.laytonsmith.aliasengine.functions.DataHandling._for;
import com.laytonsmith.aliasengine.functions.DataHandling.foreach;
import com.laytonsmith.aliasengine.functions.Function;
import com.laytonsmith.aliasengine.functions.FunctionList;
import com.laytonsmith.aliasengine.functions.IVariableList;
import com.laytonsmith.aliasengine.functions.Meta.eval;
import com.sk89q.bukkit.migration.PermissionsResolverManager;
import com.sk89q.commandhelper.CommandHelperPlugin;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

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
    private String label;
    private List<Variable> left_vars;
    FunctionList func_list;
    private IVariableList varList = new IVariableList();
    boolean hasBeenCompiled = false;
    boolean compilerError = false;

    public String toString() {
        StringBuilder b = new StringBuilder();
        for (Token t : left) {
            b.append(t.val()).append(" ");
        }
        b.append("compiled: ").append(hasBeenCompiled).append("; errors? ").append(compilerError);
        return b.toString();
    }

    public Script(List<Token> left, List<Token> right) throws ConfigCompileException {
        this.left = left;
        this.fullRight = right;
        this.left_vars = new ArrayList<Variable>();
    }

    public boolean uncompilable() {
        return compilerError;
    }

    public void run(final List<Variable> vars, final Player p, final MScriptComplete done) {
        if (!hasBeenCompiled || compilerError) {
            throw new ConfigRuntimeException("Unable to run command, script not yet compiled, or a compiler error occured for that command.");
//            System.err.println("Unable to run command, script not yet compiled, or a compiler error occured for that command.");
//            p.sendMessage(ChatColor.RED + "Unable to run command, script not yet compiled, or a compiler error occured for that command.");
//            return;
        }
        
        final Plugin self = CommandHelperPlugin.self;
        Static.getServer().getScheduler().scheduleAsyncDelayedTask(self, new Runnable() {

            public void run() {
                try {
                    for (GenericTreeNode<Construct> rootNode : cright) {
                        StringBuilder b = new StringBuilder();
                        for (GenericTreeNode<Construct> gg : rootNode.getChildren()) {
                            String ret = eval(gg, p, vars).val();
                            if (ret != null && !ret.trim().equals("")) {
                                b.append(ret).append(" ");
                            }
                        }
                        done.done(b.toString());
                    }
                } catch (ConfigRuntimeException e) {
                    p.sendMessage(e.getMessage());
                    System.out.println(e.getMessage());
                } catch (CancelCommandException e) {
                    p.sendMessage(e.getMessage());
                } catch(LoopBreakException e){
                    p.sendMessage("The break() function must be used inside a for() or foreach() loop");
                } catch(LoopContinueException e){
                    p.sendMessage("The continue() function must be used inside a for() or foreach() loop");
                } catch (Throwable t) {
                    System.out.println("An unexpected exception occured during the execution of a script.");
                    t.printStackTrace();
                    p.sendMessage("An unexpected exception occured during the execution of your script. Please check the console for more information.");
                }
                done.done(null);
            }
        });
    }

    public Construct eval(GenericTreeNode<Construct> c, final Player player, final List<Variable> vars) throws CancelCommandException {
        final Construct m = c.getData();
        if (m.ctype == ConstructType.FUNCTION) {
            try {
                final Function f;
                f = func_list.getFunction(m);
                f.varList(varList);
                //We have special handling for loop and other control flow functions
                if (f instanceof _for) {
                    _for fr = (_for) f;
                    List<GenericTreeNode<Construct>> ch = c.getChildren();
                    try {
                        return fr.execs(m.line_num, player, this, ch.get(0), ch.get(1), ch.get(2), ch.get(3), vars);
                    } catch (IndexOutOfBoundsException e) {
                        throw new ConfigRuntimeException("Invalid number of parameters passed to for");
                    }
                } else if (f instanceof _if) {
                    _if fr = (_if) f;
                    List<GenericTreeNode<Construct>> ch = c.getChildren();
                    try {
                        return fr.execs(m.line_num, player, this, ch.get(0), ch.get(1), ch.size() > 2 ? ch.get(2) : null, vars);
                    } catch (IndexOutOfBoundsException e) {
                        throw new ConfigRuntimeException("Invalid number of parameters passed to if");
                    }
                } else if (f instanceof foreach) {
                    foreach fe = (foreach) f;
                    List<GenericTreeNode<Construct>> ch = c.getChildren();
                    try {
                        return fe.execs(m.line_num, player, this, ch.get(0), ch.get(1), ch.get(2), vars);
                    } catch (IndexOutOfBoundsException e) {
                        throw new ConfigRuntimeException("Invalid number of parameters passed to foreach");
                    }
                } else if (f instanceof eval) {
                    List<GenericTreeNode<Construct>> ch = c.getChildren();
                    if (ch.size() > 1) {
                        throw new ConfigRuntimeException("Invalid number of parameters passed to eval");
                    }
                    GenericTreeNode<Construct> root = MScriptCompiler.compile(MScriptCompiler.lex(ch.get(0).getData().val()));
                    StringBuilder b = new StringBuilder();
                    for (GenericTreeNode<Construct> child : root.getChildren()) {
                        CString cs = new CString(eval(child, player, vars).val(), 0);
                        if (!cs.val().trim().equals("")) {
                            b.append(cs.val()).append(" ");
                        }
                    }
                    return new CString(b.toString(), 0);
                }
                ArrayList<Construct> args = new ArrayList<Construct>();
                for (GenericTreeNode<Construct> c2 : c.getChildren()) {
                    args.add(eval(c2, player, vars));
                }
                if (f.isRestricted()) {
                    boolean perm;
                    PermissionsResolverManager perms = Static.getPermissionsResolverManager();
                    if (perms != null) {
                        perm = perms.hasPermission(player.getName(), "ch.func.use." + f.getName())
                                || perms.hasPermission(player.getName(), "commandhelper.func.use." + f.getName());
                        if (label != null && (perms.hasPermission(player.getName(), "ch.alias." + label))
                                || perms.hasPermission(player.getName(), "commandhelper.alias." + label)) {
                            perm = true;
                        }
                    } else {
                        perm = true;
                    }
                    if (player.isOp()) {
                        perm = true;
                    }
                    if (!perm) {
                        throw new CancelCommandException("You do not have permission to use the " + f.getName() + " function.");
                    }
                }
                Object[] a = args.toArray();
                final Construct[] ca = new Construct[a.length];
                for (int i = 0; i < a.length; i++) {
                    ca[i] = (Construct) a[i];
                    //if it's a variable, go ahead and cast it to the correct data type
                    if (ca[i].ctype == ConstructType.VARIABLE) {
                        Variable v = (Variable) ca[i];
                        for (Variable var : vars) {
                            if (var.getName().equals(v.getName())) {
                                ca[i] = Static.resolveConstruct(var.val(), var.line_num);
                                break;
                            }
                        }
                    }
                    //CArray, CBoolean, CDouble, CInt, CMap, CNull, CString, CVoid.
                    if (!(ca[i] instanceof CArray || ca[i] instanceof CBoolean || ca[i] instanceof CDouble
                            || ca[i] instanceof CInt || ca[i] instanceof CMap || ca[i] instanceof CNull
                            || ca[i] instanceof CString || ca[i] instanceof CVoid || ca[i] instanceof IVariable)) {
                        throw new ConfigRuntimeException("Invalid Construct being passed as an argument to a function");
                    }
                }
                if (f.preResolveVariables()) {
                    for (int i = 0; i < ca.length; i++) {
                        if (ca[i] instanceof IVariable) {
                            IVariable v = (IVariable) ca[i];
                            ca[i] = varList.get(v.getName()).ival();
                        }
                    }
                }
                //TODO: Layton Hmm....
                if(f.runAsync() == true || f.runAsync() == null){
                    return f.exec(m.line_num, player, ca);
                } else {
                    return blockingNonThreadSafe(player, new Callable<Construct>() {

                        public Construct call() throws Exception {
                            return f.exec(m.line_num, player, ca);
                        }
                    });
                }

            } catch (ConfigCompileException ex) {
                Logger.getLogger(Script.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else if (m.ctype == ConstructType.VARIABLE) {
            Variable v = (Variable)m;
            for(Variable vx : vars){
                if(v.getName().equals(vx.getName())){
                    v = vx;
                    break;
                }
            }            
            return v;
        } else {
            return m;
        }
        return null;
    }

    private Construct blockingNonThreadSafe(final Player p, Callable task) throws CancelCommandException {
        Plugin self = CommandHelperPlugin.self;
        try {
            Future<Construct> f = Static.getServer().getScheduler().callSyncMethod(self, task);
            while (!f.isDone()) {
                Thread.sleep(10);
            }
            return f.get();
        } catch (InterruptedException ex) {
            Logger.getLogger(Script.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            if (ex.getCause() instanceof CancelCommandException) {
                CancelCommandException e = (CancelCommandException) ex.getCause();
                throw e;
            } else {
                Logger.getLogger(Script.class.getName()).log(Level.SEVERE, null, ex.getCause());
            }
        }
        return null;
    }

    public boolean match(String command) {
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
                if (c.ctype != ConstructType.VARIABLE){
//                        || c.ctype == ConstructType.TOKEN
//                        || c.ctype == ConstructType.LITERAL
//                        || c.ctype == ConstructType.STRING || ConstructType.) {
                    if (!c.val().equals(arg)) {
                        isAMatch = false;
                        continue;
                    }
                }
                if (j == cleft.size() - 1) {
                    if (cleft.get(j).ctype == ConstructType.VARIABLE) {
                        Variable lv = (Variable) cleft.get(j);
                        if (lv.final_var) {
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
            if (cleft.get(lastJ).ctype != ConstructType.VARIABLE || 
                    cleft.get(lastJ).ctype == ConstructType.VARIABLE
                    && !((Variable) cleft.get(lastJ)).optional) {
                isAMatch = false;
            }
        }
        boolean lastIsFinal = false;
        if(cleft.get(cleft.size() - 1) instanceof Variable){
            Variable v = (Variable) cleft.get(cleft.size() - 1);
            if(v.final_var){
                lastIsFinal = true;
            }
        }
        if(cleft.size() != cmds.length && !lastIsFinal){
            isAMatch = false;
        }
        ArrayList<Variable> vars = new ArrayList<Variable>();
        Variable v = null;
        for (int j = 0; j < cleft.size(); j++) {
            try {
                if (cleft.get(j).ctype == ConstructType.VARIABLE) {
                    if (((Variable) cleft.get(j)).name.equals("$")) {
                        v = new Variable(((Variable) cleft.get(j)).name,
                                lastVar.toString(), 0);
                    } else {
                        v = new Variable(((Variable) cleft.get(j)).name,
                                args.get(j), 0);
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                v = new Variable(((Variable) cleft.get(j)).name,
                        ((Variable) cleft.get(j)).def, 0);
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
                if (c.ctype == ConstructType.COMMAND
                        || c.ctype == ConstructType.TOKEN
                        || c.ctype == ConstructType.LITERAL) {
                    if (!c.val().equals(arg)) {
                        isAMatch = false;
                    }
                }
                if (j == cleft.size() - 1) {
                    if (cleft.get(j).ctype == ConstructType.VARIABLE) {
                        Variable lv = (Variable) cleft.get(j);
                        if (lv.final_var) {
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
            if (cleft.get(lastJ).ctype == ConstructType.VARIABLE
                    && !((Variable) cleft.get(lastJ)).optional) {
                isAMatch = false;
            }
        }
        ArrayList<Variable> vars = new ArrayList<Variable>();
        Variable v = null;
        for (int j = 0; j < cleft.size(); j++) {
            try {
                if (cleft.get(j).ctype == ConstructType.VARIABLE) {
                    if (((Variable) cleft.get(j)).name.equals("$")) {
                        v = new Variable(((Variable) cleft.get(j)).name,
                                lastVar.toString(), 0);
                    } else {
                        v = new Variable(((Variable) cleft.get(j)).name,
                                args.get(j), 0);
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                v = new Variable(((Variable) cleft.get(j)).name,
                        ((Variable) cleft.get(j)).def, 0);
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
        for (int j = 0; j < left.size(); j++) {
            Token t = left.get(j);
            //Token prev_token = j - 2 >= 0?c.tokens.get(j - 2):new Token(TType.UNKNOWN, "", t.line_num);
            Token last_token = j - 1 >= 0 ? left.get(j - 1) : new Token(TType.UNKNOWN, "", t.line_num);
            Token next_token = j + 1 < left.size() ? left.get(j + 1) : new Token(TType.UNKNOWN, "", t.line_num);
            Token after_token = j + 2 < left.size() ? left.get(j + 2) : new Token(TType.UNKNOWN, "", t.line_num);

            if (j == 0) {
                if (next_token.type == TType.IDENT) {
                    label = t.val();
                    j += 1;
                    left.remove(0);
                    left.remove(0);
                    continue;
                } else {
                    label = null;
                }
            }

            if (t.type == TType.IDENT) {
                continue;
            }

            if (t.type.equals(TType.FINAL_VAR) && left.size() - j >= 5) {
                throw new ConfigCompileException("FINAL_VAR must be the last argument in the alias", t.line_num);
            }
            if (t.type.equals(TType.VARIABLE) || t.type.equals(TType.FINAL_VAR)) {
                left_vars.add(new Variable(t.val(), null, t.line_num));
            }
            if (j == 0 && !t.type.equals(TType.COMMAND)) {
                if (!(next_token.type == TType.IDENT && after_token.type == TType.COMMAND)) {
                    throw new ConfigCompileException("Expected command (/command) at start of alias."
                            + " Instead, found " + t.type + " (" + t.val() + ")", t.line_num);
                }
            }
            if (after_no_def_opt_var && !inside_opt_var) {
                if (t.type.equals(TType.LIT) || t.type.equals(TType.STRING) || t.type.equals(TType.OPT_VAR_ASSIGN)) {
                    throw new ConfigCompileException("You cannot have anything other than optional arguments after your"
                            + " first optional argument, other that other optional arguments with no default", t.line_num);
                }
            }
            if (!t.type.equals(TType.OPT_VAR_START)
                    && !t.type.equals(TType.OPT_VAR_ASSIGN)
                    && !t.type.equals(TType.OPT_VAR_END)
                    && !t.type.equals(TType.VARIABLE)
                    && !t.type.equals(TType.LIT)
                    && !t.type.equals(TType.COMMAND)
                    && !t.type.equals(TType.FINAL_VAR)) {
                if (!(t.type.equals(TType.STRING) && j - 1 > 0 && left.get(j - 1).type.equals(TType.OPT_VAR_ASSIGN))) {
                    throw new ConfigCompileException("Unexpected " + t.type + " (" + t.val() + ")", t.line_num);
                }
            }
            if (last_token.type.equals(TType.COMMAND)) {
                if (!(t.type.equals(TType.VARIABLE) || t.type.equals(TType.OPT_VAR_START) || t.type.equals(TType.FINAL_VAR)
                        || t.type.equals(TType.LIT))) {
                    throw new ConfigCompileException("Unexpected " + t.type + " (" + t.val() + ") after command", t.line_num);
                }
            }
            if (last_token.type.equals(TType.OPT_VAR_START)) {
                inside_opt_var = true;
                if (!(t.type.equals(TType.FINAL_VAR) || t.type.equals(TType.VARIABLE))) {
                    throw new ConfigCompileException("Unexpected " + t.type.toString() + " (" + t.val() + ")", t.line_num);
                }
            }
            if (inside_opt_var && t.type.equals(TType.OPT_VAR_ASSIGN)) {
                if (!((next_token.type.equals(TType.STRING) || next_token.type.equals(TType.LIT)) && after_token.type.equals(TType.OPT_VAR_END)
                        || (next_token.type.equals(TType.OPT_VAR_END)))) {
                    throw new ConfigCompileException("Unexpected token in optional variable", t.line_num);
                } else if (next_token.type.equals(TType.STRING) || next_token.type.equals(TType.LIT)) {
                    left_vars.get(left_vars.size() - 1).def = next_token.val();
                } else {
                    left_vars.get(left_vars.size() - 1).def = "";
                }
            }
            if (t.type.equals(TType.OPT_VAR_END)) {
                if (!inside_opt_var) {
                    throw new ConfigCompileException("Unexpected " + t.type.toString(), t.line_num);
                }
                inside_opt_var = false;
                if (last_token.type.equals(TType.VARIABLE)
                        || last_token.type.equals(TType.FINAL_VAR)) {
                    after_no_def_opt_var = true;
                }
            }
        }

        return true;
    }

    private boolean compileLeft() {
        cleft = new ArrayList<Construct>();
        for (int i = 0; i < left.size(); i++) {
            Token t = left.get(i);
            if (t.type == Token.TType.COMMAND) {
                cleft.add(new Command(t.val(), t.line_num));
            } else if (t.type == Token.TType.VARIABLE) {
                cleft.add(new Variable(t.val(), null, t.line_num));
            } else if (t.type.equals(TType.FINAL_VAR)) {
                Variable v = new Variable(t.val(), null, t.line_num);
                v.final_var = true;
                cleft.add(v);
            } else if (t.type.equals(TType.OPT_VAR_START)) {
                if (i + 2 < left.size() && left.get(i + 2).type.equals(TType.OPT_VAR_ASSIGN)) {
                    Variable v = new Variable(left.get(i + 1).val(),
                            left.get(i + 3).val(), t.line_num);
                    v.optional = true;
                    if (left.get(i + 1).type.equals(TType.FINAL_VAR)) {
                        v.final_var = true;
                    }
                    cleft.add(v);
                    i += 4;
                } else {
                    t = left.get(i + 1);
                    Variable v = new Variable(t.val(), null, t.line_num);
                    v.optional = true;
                    if (t.val().equals("$")) {
                        v.final_var = true;
                    }
                    cleft.add(v);
                    i += 2;
                }
            } else {
                cleft.add(new CString(t.val(), t.line_num));
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
                if(thatCommand == null){
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
                        if (c1.ctype != c2.ctype || ((c1 instanceof Variable) && !((Variable) c1).optional)) {
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
                                && !((Variable) thisCommand.get(k)).optional)) {
                            soFarAMatch = false;
                        }
                    }
                }
                if (thatCommand.size() > thisCommand.size()) {
                    int k = thisCommand.size();
                    //thisCommand is the short one
                    if (!(thatCommand.get(k) instanceof Variable)
                            || (thatCommand.get(k) instanceof Variable
                            && !((Variable) thatCommand.get(k)).optional)) {
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
                            + "matches the signature of " + commandThat.trim(), thisCommand.get(0).line_num);
                }
            }
            
            //Also, check for undefined variables on the right, and unused variables on the left
            ArrayList<String> left_copy = new ArrayList<String>();
            for(Variable v : left_vars){
                left_copy.add(v.getName());
            }
            for(GenericTreeNode<Construct> gtn : cright){
                GenericTree<Construct> tree = new GenericTree<Construct>();
                tree.setRoot(gtn);
                for(GenericTreeNode<Construct> c : tree.build(GenericTreeTraversalOrderEnum.PRE_ORDER)){
                    if(c.getData() instanceof Variable){
                        for(Variable v : left_vars){
                            if(v.getName().equals(((Variable)c.getData()).getName())){
                                //Found it, remove this from the left_copy, and break
                                left_copy.remove(v.getName());
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
