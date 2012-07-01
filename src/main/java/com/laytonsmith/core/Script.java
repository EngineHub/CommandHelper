/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core;

import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.constructs.Construct.ConstructType;
import com.laytonsmith.core.constructs.Token.TType;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.exceptions.*;
import com.laytonsmith.core.functions.DataHandling.assign;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.laytonsmith.core.functions.Function;
import com.laytonsmith.core.functions.FunctionList;
import com.sk89q.wepif.PermissionsResolverManager;
import java.util.*;

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

    public static Script GenerateScript(GenericTreeNode<Construct> tree, String label){
        Script s = new Script();
        
        s.hasBeenCompiled = true;
        s.compilerError = false;
        s.cright = new ArrayList<GenericTreeNode<Construct>>();
        s.cright.add(tree);
        s.label = label;
        //s.OriginalEnv = env;
        GenericTree<Construct> root = new GenericTree<Construct>();
        root.setRoot(tree);
//        for(GenericTreeNode<Construct> node : root.build(GenericTreeTraversalOrderEnum.PRE_ORDER)){
//            if(node.getData() instanceof IVariable){
//                s.OriginalEnv.GetVarList().set((IVariable)node.getData());
//            }
//        }
        
        return s;
    }
    private List<Construct> cleft;
    boolean compilerError = false;
    private List<GenericTreeNode<Construct>> cright;
    private Env CurrentEnv;
    private List<Token> fullRight;
    boolean hasBeenCompiled = false;
    private String label;
    private List<Token> left;
    //This should be null if we are running in non-alias mode
    private Map<String, Variable> left_vars;

    private List<List<Token>> right;

    private Script(){}
    
    public Script(List<Token> left, List<Token> right) {
        this.left = left;
        this.fullRight = right;
        this.left_vars = new HashMap<String, Variable>();        
        //this.OriginalEnv = env;
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
                        + "matches the signature of " + commandThat.trim(), thisCommand.get(0).getTarget());
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
    
    public Script compile() throws ConfigCompileException {
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
    
    private boolean compileLeft() {
        cleft = new ArrayList<Construct>();
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
    
    public void compileRight() throws ConfigCompileException {
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
        cright = new ArrayList<GenericTreeNode<Construct>>();
        for (List<Token> l : right) {
            cright.add(MethodScriptCompiler.compile(l));
        }
    }

    public Construct eval(GenericTreeNode<Construct> c, final Env env) throws CancelCommandException {
        final Construct m = c.getData();
        CurrentEnv = env;
        CurrentEnv.SetLabel(this.label);
        if (m.getCType() == ConstructType.FUNCTION) {
                env.SetScript(this);
                if (m.val().matches("^_[^_].*")) {
                    //Not really a function, so we can't put it in Function.
                    Procedure p = getProc(m.val());
                    if (p == null) {
                        throw new ConfigRuntimeException("Unknown procedure \"" + m.val() + "\"", ExceptionType.InvalidProcedureException, m.getTarget());
                    }
                    Env newEnv = env;
                    try{
                        newEnv = env.clone();
                    } catch(Exception e){}
                    return p.cexecute(c.getChildren(), newEnv);
                }
                final Function f;
                try{
                    f = (Function)FunctionList.getFunction(m);
                } catch(ConfigCompileException e){
                    //Turn it into a config runtime exception. This shouldn't ever happen though.
                    throw new ConfigRuntimeException("Unable to find function " + m.val(), m.getTarget());
                }
                //We have special handling for loop and other control flow functions
                if(f instanceof assign){
                    if(c.getChildAt(0).getData() instanceof CFunction){
                        CFunction test = (CFunction)c.getChildAt(0).getData();
                        if(test.val().equals("array_get")){
                            env.SetFlag("array_get_alt_mode", true);
                            Construct arrayAndIndex = eval(c.getChildAt(0), env);
                            env.ClearFlag("array_get_alt_mode");
                            return ((assign)f).array_assign(m.getTarget(), env, arrayAndIndex, eval(c.getChildAt(1), env));
                        }
                    }
                }
                
                if(f.useSpecialExec()){
                    return f.execs(m.getTarget(), env, this, c.getChildren().toArray(new GenericTreeNode[]{}));
                }

                ArrayList<Construct> args = new ArrayList<Construct>();
                for (GenericTreeNode<Construct> c2 : c.getChildren()) {
                    args.add(eval(c2, env));
                }
                if (f.isRestricted()) {
                    boolean perm = Static.hasCHPermission(f.getName(), env);
                    if (!perm) {
                        throw new ConfigRuntimeException("You do not have permission to use the " + f.getName() + " function.",
                                ExceptionType.InsufficientPermissionException, m.getTarget());
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
                            || ca[i] instanceof IVariable || ca[i] instanceof CEntry || ca[i] instanceof CLabel)
                            && (!f.getName().equals("__autoconcat__") && (ca[i] instanceof CLabel))) {
                        throw new ConfigRuntimeException("Invalid Construct (" 
                                + ca[i].getClass() + ") being passed as an argument to a function (" 
                                + f.getName() + ")", null, m.getTarget());
                    }
                    if(env.GetFlag("array_get_alt_mode") == Boolean.TRUE && i == 0){
                        continue;
                    }
                    if(f.preResolveVariables() && ca[i] instanceof IVariable){
                        IVariable cur = (IVariable)ca[i];
                        ca[i] = env.GetVarList().get(cur.getName(), cur.getTarget()).ival();
                    }
                }

                Construct ret = f.exec(m.getTarget(), env, ca);
                return ret;

        } else if (m.getCType() == ConstructType.VARIABLE) {
            return Static.resolveConstruct(m.val(), m.getTarget());
        } else {
            return m;
        }
    }

    public Env getCurrentEnv(){
        return CurrentEnv;
    }
    
    public String getLabel(){
        return label;
    }

    private Procedure getProc(String name) {
        return CurrentEnv.GetProcs().get(name);
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
        ArrayList<Variable> vars = new ArrayList<Variable>();
        Variable v = null;
        for (int j = 0; j < cleft.size(); j++) {
            try {
                if (cleft.get(j).getCType() == ConstructType.VARIABLE) {
                    if (((Variable) cleft.get(j)).getName().equals("$")) {
                        v = new Variable(((Variable) cleft.get(j)).getName(),
                                lastVar.toString(), Target.UNKNOWN);
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
        return isAMatch;
    }

    public void run(final List<Variable> vars, Env myEnv, final MethodScriptComplete done) {
        //Some things, such as the label are determined at compile time
        this.CurrentEnv = myEnv;
        this.CurrentEnv.SetLabel(this.label);
        MCCommandSender p = myEnv.GetCommandSender();
        if (!hasBeenCompiled || compilerError) {
            Target target = Target.UNKNOWN;
            if (left.size() >= 1) {
                try{
                    target = new Target(left.get(0).line_num, left.get(0).file, left.get(0).column);
                } catch(NullPointerException e){
                    //Oh well, we tried to get more information
                }
            }
            throw new ConfigRuntimeException("Unable to run command, script not yet compiled, or a compiler error occured for that command."
                    + " To see the compile error, run /reloadaliases",
                    null, target);
        }
        if (p instanceof MCPlayer) {
            if (CurrentEnv.GetLabel() != null) {
                PermissionsResolverManager perms = Static.getPermissionsResolverManager();
                String[] groups = CurrentEnv.GetLabel().substring(1).split("/");
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
            for (GenericTreeNode<Construct> rootNode : cright) {
                GenericTree<Construct> tree = new GenericTree<Construct>();
                tree.setRoot(rootNode);
                for (GenericTreeNode<Construct> tempNode : tree.build(GenericTreeTraversalOrderEnum.PRE_ORDER)) {
                    if (tempNode.data instanceof Variable) {
                        if(left_vars == null){
                            throw new ConfigRuntimeException("$variables may not be used in this context. Only @variables may be.", null, tempNode.data.getTarget());
                        }
                        ((Variable) tempNode.data).setVal(
                                Static.resolveConstruct(
                                Static.resolveDollarVar(left_vars.get(((Variable) tempNode.data).getName()), vars).toString(), tempNode.data.getTarget()));
                    }
                }
                
                MethodScriptCompiler.registerAutoIncludes(CurrentEnv, this);
                MethodScriptCompiler.execute(tree.getRoot(), CurrentEnv, done, this);
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
            if(myEnv.GetEvent() != null){
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
    public Construct seval(GenericTreeNode<Construct> c, final Env env){
        Construct ret = eval(c, env);
        if(ret instanceof IVariable){
            IVariable cur = (IVariable)ret;
            return env.GetVarList().get(cur.getName(), cur.getTarget()).ival();
        }
        return ret;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        for (Token t : left) {
            b.append(t.val()).append(" ");
        }
        b.append("compiled: ").append(hasBeenCompiled).append("; errors? ").append(compilerError);
        return b.toString();
    }

    public boolean uncompilable() {
        return compilerError;
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
            if(t.type.isSymbol() && left.size() - 1 >= i && left.get(i + 1).type != TType.WHITESPACE){
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
                            + " first optional argument, other that other optional arguments with no default", t.target);
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
                        || t.type.equals(TType.LIT))) {
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
//                if (last_token.type.equals(TType.VARIABLE)
//                        || last_token.type.equals(TType.FINAL_VAR)) {
//                    after_no_def_opt_var = true;
//                }
            }
        }

        return true;
    }
}
