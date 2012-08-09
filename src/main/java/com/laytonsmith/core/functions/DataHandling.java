

package com.laytonsmith.core.functions;

import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.noboilerplate;
import com.laytonsmith.core.*;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.exceptions.*;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Layton
 */
public class DataHandling {

    public static String docs() {
        return "This class provides various methods to control script data and program flow.";
    }

    @api
    public static class array extends AbstractFunction {

        public String getName() {
            return "array";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            return new CArray(t, args);
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{};
        }

        public String docs() {
            return "array {[var1, [var2...]]} Creates an array of values.";
        }

        public boolean isRestricted() {
            return false;
        }
        public CHVersion since() {
            return CHVersion.V3_0_1;
        }

        public Boolean runAsync() {
            return null;
        }

//        @Override
//        public boolean canOptimize() {
//            //FALSE. Can't optimize, because this returns a reference. This is
//            //a much more complicated issue. TODO
//            return false;
//        }
//
//        @Override
//        public Construct optimize(Target t, Construct... args) {
//            return exec(t, null, args);
//        }
    }

    @api
    public static class assign extends AbstractFunction {

        public String getName() {
            return "assign";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Construct c = args[1];
            while (c instanceof IVariable) {
                IVariable cur = (IVariable) c;
                c = env.GetVarList().get(cur.getName(), cur.getTarget()).ival();
            }
            if (args[0] instanceof IVariable) {
                IVariable v = new IVariable(((IVariable) args[0]).getName(), c, t);
                env.GetVarList().set(v);
                return v;
            }
            throw new ConfigRuntimeException("assign only accepts an ivariable or array reference as the first argument", ExceptionType.CastException, t);
        }

        private static class Chain {

            ArrayList<Construct> indexChain = new ArrayList<Construct>();
        }

        private void prepare(CArrayReference container, Chain c) {
            if (container.array instanceof CArrayReference) {
                prepare((CArrayReference) container.array, c);
                c.indexChain.add(container.index);
            } else {
                c.indexChain.add(container.index);
            }
        }

        public Construct array_assign(Target t, Env env, Construct arrayAndIndex, Construct toSet) {
            Construct ival = toSet;
            while (ival instanceof IVariable) {
                IVariable cur = (IVariable) ival;
                ival = env.GetVarList().get(cur.getName(), cur.getTarget()).ival();
            }
            Chain c = new Chain();
            prepare((CArrayReference) arrayAndIndex, c);
            CArray inner = (CArray) ((CArrayReference) arrayAndIndex).getInternalArray();
            for (int i = 0; i < c.indexChain.size(); i++) {
                if (i == c.indexChain.size() - 1) {
                    //Last one, set it
                    inner.set(c.indexChain.get(i), ival);
                } else {
                    boolean makeIt = false;
                    Construct ct = null;
                    if (!inner.containsKey(c.indexChain.get(i).val())) {
                        makeIt = true;
                    } else {
                        ct = inner.get(c.indexChain.get(i), t);
                        if (!(ct instanceof CArray)) {
                            makeIt = true;
                        }
                    }
                    if (makeIt) {
                        Construct newArray = new CArray(t);
                        inner.set(c.indexChain.get(i), newArray);
                        ct = newArray;
                    }
                    inner = (CArray) ct;
                }
            }
            String name = ((CArrayReference) arrayAndIndex).name.getName();
            env.GetVarList().set(new IVariable(name, (CArray) ((CArrayReference) arrayAndIndex).getInternalArray(), t));
            return new IVariable("=anon", ival, t);
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public String docs() {
            return "ivariable {ivar, mixed} Accepts an ivariable ivar as a parameter, and puts the specified value mixed in it. Returns the variable that was assigned.";
        }

        public boolean isRestricted() {
            return false;
        }

        @Override
        public boolean preResolveVariables() {
            return false;
        }

        public CHVersion since() {
            return CHVersion.V3_0_1;
        }

        public Boolean runAsync() {
            return null;
        }

        @Override
        public boolean canOptimize() {
            return true;
        }

        @Override
        public Construct optimize(Target t, Construct... args) throws ConfigCompileException {
            //We can't really optimize, but we can check that we are
            //getting an ivariable.
            if(!(args[0] instanceof IVariable)){
                throw new ConfigCompileException("Expecting an ivar for argument 1 to assign", t);
            }
            return null;
        }

    }

    @api
    @noboilerplate
    public static class _for extends AbstractFunction {

        public String getName() {
            return "for";
        }

        public Integer[] numArgs() {
            return new Integer[]{4};
        }

        public Construct exec(Target t, Env env, Construct... args) {
            return new CVoid(t);
        }

        @Override
        public boolean useSpecialExec() {
            return true;
        }

        @Override
        public Construct execs(Target t, Env env, Script parent, GenericTreeNode<Construct>... nodes) {
            GenericTreeNode<Construct> assign = nodes[0];
            GenericTreeNode<Construct> condition = nodes[1];
            GenericTreeNode<Construct> expression = nodes[2];
            GenericTreeNode<Construct> runnable = nodes[3];

            Construct counter = parent.eval(assign, env);
            if (!(counter instanceof IVariable)) {
                throw new ConfigRuntimeException("First parameter of for must be an ivariable", ExceptionType.CastException, t);
            }
            int _continue = 0;
            while (true) {
                boolean cond = Static.getBoolean(parent.seval(condition, env));
                if (cond == false) {
                    break;
                }
                if (_continue >= 1) {
                    --_continue;
                    parent.eval(expression, env);
                    continue;
                }
                try {
                    parent.eval(runnable, env);
                } catch (LoopBreakException e) {
                    int num = e.getTimes();
                    if (num > 1) {
                        e.setTimes(--num);
                        throw e;
                    }
                    return new CVoid(t);
                } catch (LoopContinueException e) {
                    _continue = e.getTimes() - 1;
                    parent.eval(expression, env);
                    continue;
                }
                parent.eval(expression, env);
            }
            return new CVoid(t);
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public String docs() {
            return "void {assign, condition, expression1, expression2} Acts as a typical for loop. The assignment is first run. Then, a"
                    + " condition is checked. If that condition is checked and returns true, expression2 is run. After that, expression1 is run. In java"
                    + " syntax, this would be: for(assign; condition; expression1){expression2}. assign must be an ivariable, either a "
                    + "pre defined one, or the results of the assign() function. condition must be a boolean.";
        }

        public boolean isRestricted() {
            return false;
        }
        public CHVersion since() {
            return CHVersion.V3_0_1;
        }
        //Doesn't matter, run out of state

        public Boolean runAsync() {
            return null;
        }

        @Override
        public boolean allowBraces() {
            return true;
        }

    }

    @api
    public static class foreach extends AbstractFunction {

        public String getName() {
            return "foreach";
        }

        public Integer[] numArgs() {
            return new Integer[]{3};
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            return new CVoid(t);
        }

        @Override
        public Construct execs(Target t, Env env, Script that, GenericTreeNode<Construct>... nodes) {
            GenericTreeNode<Construct> array = nodes[0];
            GenericTreeNode<Construct> ivar = nodes[1];
            GenericTreeNode<Construct> code = nodes[2];

            Construct arr = that.seval(array, env);
            Construct iv = that.eval(ivar, env);
            if(arr instanceof CSlice){
                long start = ((CSlice)arr).getStart();
                long finish = ((CSlice)arr).getFinish();
                if(finish < start){
                    throw new ConfigRuntimeException("When using the .. notation, the left number may not be greater than the right number. Recieved " + start + " and " + finish, ExceptionType.RangeException, t);
                }
                arr = new ArrayHandling.range().exec(t, env, new CInt(start, t), new CInt(finish + 1, t));
            }
            if (arr instanceof CArray) {
                if (iv instanceof IVariable) {
                    CArray one = (CArray) arr;
                    IVariable two = (IVariable) iv;
                    if (!one.inAssociativeMode()) {
                        for (int i = 0; i < one.size(); i++) {
                            env.GetVarList().set(new IVariable(two.getName(), one.get(i, t), t));
                            try {
                                that.eval(code, env);
                            } catch (LoopBreakException e) {
                                int num = e.getTimes();
                                if (num > 1) {
                                    e.setTimes(--num);
                                    throw e;
                                }
                                return new CVoid(t);
                            } catch (LoopContinueException e) {
                                i += e.getTimes() - 1;
                                continue;
                            }
                        }
                    } else {
                        for (int i = 0; i < one.size(); i++) {
                            String index = one.keySet().toArray(new String[]{})[i];
                            env.GetVarList().set(new IVariable(two.getName(), one.get(index, t), t));
                            try {
                                that.eval(code, env);
                            } catch (LoopBreakException e) {
                                int num = e.getTimes();
                                if (num > 1) {
                                    e.setTimes(--num);
                                    throw e;
                                }
                                return new CVoid(t);
                            } catch (LoopContinueException e) {
                                i += e.getTimes() - 1;
                                continue;
                            }
                        }
                    }
                } else {
                    throw new ConfigRuntimeException("Parameter 2 of foreach must be an ivariable", ExceptionType.CastException, t);
                }
            } else {
                throw new ConfigRuntimeException("Parameter 1 of foreach must be an array", ExceptionType.CastException, t);
            }

            return new CVoid(t);
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.RangeException};
        }

        public String docs() {
            return "void {array, ivar, code} Walks through array, setting ivar equal to each element in the array, then running code."
                    + " In addition, foreach(1..4, @i, code()) is also valid, setting @i to 1, 2, 3, 4 each time. The same syntax is valid as"
                    + " in an array slice, except negative indexes cannot be tolerated.";
        }

        public boolean isRestricted() {
            return false;
        }
        public CHVersion since() {
            return CHVersion.V3_0_1;
        }
        //Doesn't matter, runs out of state anyways

        public Boolean runAsync() {
            return null;
        }

        @Override
        public boolean useSpecialExec() {
            return true;
        }

        @Override
        public boolean allowBraces() {
            return true;
        }

    }

    @api
    @noboilerplate
    public static class _while extends AbstractFunction{

        public String getName() {
            return "while";
        }

        public String docs() {
            return "void {condition, code} While the condition is true, the code is executed. break and continue work"
                    + " inside a dowhile, but continuing more than once is pointless, since the loop isn't inherently"
                    + " keeping track of any counters anyways. Breaking multiple times still works however.";
        }

        public CHVersion since() {
            return CHVersion.V3_3_1;
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public ExceptionType[] thrown() {
            return null;
        }

        public boolean isRestricted() {
            return false;
        }

        public Boolean runAsync() {
            return null;
        }

        @Override
        public Construct execs(Target t, Env env, Script parent, GenericTreeNode<Construct>... nodes) {
            try{
                while(Static.getBoolean(parent.seval(nodes[0], env))){
                    try{
                        parent.seval(nodes[1], env);
                    } catch(LoopContinueException e){
                        //ok.
                    }
                }
            } catch(LoopBreakException e){
                if(e.getTimes() > 1){
                    throw new LoopBreakException(e.getTimes() - 1);
                }
            }
            return new CVoid(t);
        }

        @Override
        public boolean useSpecialExec() {
            return true;
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            return new CNull();
        }


    }

    @api
    @noboilerplate
    public static class _dowhile extends AbstractFunction{

        public ExceptionType[] thrown() {
            return null;
        }

        public boolean isRestricted() {
            return false;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            return new CNull();
        }

        public String getName() {
            return "dowhile";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            return "void {code, condition} Like while, but always runs the code at least once. The condition is checked"
                    + " after each run of the code, and if it is true, the code is run again. break and continue work"
                    + " inside a dowhile, but continuing more than once is pointless, since the loop isn't inherently"
                    + " keeping track of any counters anyways. Breaking multiple times still works however.";
        }

        public CHVersion since() {
            return CHVersion.V3_3_1;
        }

        @Override
        public boolean useSpecialExec() {
            return true;
        }

        @Override
        public Construct execs(Target t, Env env, Script parent, GenericTreeNode<Construct>... nodes) {
            try{
                do{
                    try{
                        parent.seval(nodes[0], env);
                    } catch(LoopContinueException e){
                        //ok. No matter how many times it tells us to continue, we're only going to continue once.
                    }
                } while(Static.getBoolean(parent.seval(nodes[1], env)));
            } catch(LoopBreakException e){
                if(e.getTimes() > 1){
                    throw new LoopBreakException(e.getTimes() - 1);
                }
            }
            return new CVoid(t);
        }

    }

    @api
    public static class _break extends AbstractFunction {

        public String getName() {
            return "break";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1};
        }

        public String docs() {
            return "nothing {[int]} Stops the current loop. If int is specified, and is greater than 1, the break travels that many loops up. So, if you had"
                    + " a loop embedded in a loop, and you wanted to break in both loops, you would call break(2). If this function is called outside a loop"
                    + " (or the number specified would cause the break to travel up further than any loops are defined), the function will fail. If no"
                    + " argument is specified, it is the same as calling break(1).";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return false;
        }
        public CHVersion since() {
            return CHVersion.V3_1_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            int num = 1;
            if (args.length == 1) {
                num = (int) Static.getInt(args[0]);
            }
            throw new LoopBreakException(num);
        }
    }

    @api
    public static class _continue extends AbstractFunction {

        public String getName() {
            return "continue";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1};
        }

        public String docs() {
            return "void {[int]} Skips the rest of the code in this loop, and starts the loop over, with it continuing at the next index. If this function"
                    + " is called outside of a loop, the command will fail. If int is set, it will skip 'int' repetitions. If no argument is specified,"
                    + " 1 is used.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return false;
        }
        public CHVersion since() {
            return CHVersion.V3_1_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            int num = 1;
            if (args.length == 1) {
                num = (int) Static.getInt(args[0]);
            }
            throw new LoopContinueException(num);
        }
    }

    @api
    public static class is_string extends AbstractFunction {

        public String getName() {
            return "is_string";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "boolean {item} Returns whether or not the item is a string. Everything but arrays can be used as strings.";
        }

        public ExceptionType[] thrown() {
            return null;
        }

        public boolean isRestricted() {
            return false;
        }
        public CHVersion since() {
            return CHVersion.V3_1_2;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            return new CBoolean(!(args[0] instanceof CArray), t);
        }

        @Override
        public boolean canOptimize() {
            return true;
        }

        @Override
        public Construct optimize(Target t, Construct... args) throws ConfigCompileException {
            return exec(t, null, args);
        }
    }

    @api
    public static class is_array extends AbstractFunction {

        public String getName() {
            return "is_array";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "boolean {item} Returns whether or not the item is an array";
        }

        public ExceptionType[] thrown() {
            return null;
        }

        public boolean isRestricted() {
            return false;
        }
        public CHVersion since() {
            return CHVersion.V3_1_2;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            return new CBoolean(args[0] instanceof CArray, t);
        }

        @Override
        public boolean canOptimize() {
            return true;
        }

        @Override
        public Construct optimize(Target t, Construct... args) throws ConfigCompileException {
            return exec(t, null, args);
        }
    }

    @api
    public static class is_double extends AbstractFunction {

        public String getName() {
            return "is_double";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "boolean {item} Returns whether or not the given item is a double. Note that numeric strings and integers"
                    + " can usually be used as a double, however this function checks the actual datatype of the item. If"
                    + " you just want to see if an item can be used as a number, use is_numeric() instead.";
        }

        public ExceptionType[] thrown() {
            return null;
        }

        public boolean isRestricted() {
            return false;
        }
        public CHVersion since() {
            return CHVersion.V3_1_2;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            return new CBoolean(args[0] instanceof CDouble, t);
        }

        @Override
        public boolean canOptimize() {
            return true;
        }

        @Override
        public Construct optimize(Target t, Construct... args) throws ConfigCompileException {
            return exec(t, null, args);
        }
    }

    @api
    public static class is_integer extends AbstractFunction {

        public String getName() {
            return "is_integer";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "boolean {item} Returns whether or not the given item is an integer. Note that numeric strings can usually be used as integers,"
                    + " however this function checks the actual datatype of the item. If you just want to see if an item can be used as a number,"
                    + " use is_integral() or is_numeric() instead.";
        }

        public ExceptionType[] thrown() {
            return null;
        }

        public boolean isRestricted() {
            return false;
        }
        public CHVersion since() {
            return CHVersion.V3_1_2;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            return new CBoolean(args[0] instanceof CInt, t);
        }

        @Override
        public boolean canOptimize() {
            return true;
        }

        @Override
        public Construct optimize(Target t, Construct... args) throws ConfigCompileException {
            return exec(t, null, args);
        }
    }

    @api
    public static class is_boolean extends AbstractFunction {

        public String getName() {
            return "is_boolean";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "boolean {item} Returns whether the given item is of the boolean datatype. Note that all datatypes can be used as booleans, however"
                    + " this function checks the specific datatype of the given item.";
        }

        public ExceptionType[] thrown() {
            return null;
        }

        public boolean isRestricted() {
            return false;
        }
        public CHVersion since() {
            return CHVersion.V3_1_2;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            return new CBoolean(args[0] instanceof CBoolean, t);
        }

        @Override
        public boolean canOptimize() {
            return true;
        }

        @Override
        public Construct optimize(Target t, Construct... args) throws ConfigCompileException {
            return exec(t, null, args);
        }
    }

    @api
    public static class is_null extends AbstractFunction {

        public String getName() {
            return "is_null";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "boolean {item} Returns whether or not the given item is null.";
        }

        public ExceptionType[] thrown() {
            return null;
        }

        public boolean isRestricted() {
            return false;
        }
        public CHVersion since() {
            return CHVersion.V3_1_2;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            return new CBoolean(args[0] instanceof CNull, t);
        }

        @Override
        public boolean canOptimize() {
            return true;
        }

        @Override
        public Construct optimize(Target t, Construct... args) throws ConfigCompileException {
            return exec(t, null, args);
        }
    }

    @api
    public static class is_numeric extends AbstractFunction {

        public String getName() {
            return "is_numeric";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "boolean {item} Returns false if the item would fail if it were used as a numeric value."
                    + " If it can be parsed or otherwise converted into a numeric value, true is returned.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{};
        }

        public boolean isRestricted() {
            return false;
        }
        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            boolean b = true;
            try {
                Static.getNumber(args[0]);
            } catch (ConfigRuntimeException e) {
                b = false;
            }
            return new CBoolean(b, t);
        }

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        @Override
        public boolean canOptimize() {
            return true;
        }

        @Override
        public Construct optimize(Target t, Construct... args) throws ConfigCompileException {
            return exec(t, null, args);
        }
    }

    @api
    public static class is_integral extends AbstractFunction {

        public String getName() {
            return "is_integral";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "boolean {item} Returns true if the numeric value represented by "
                    + " a given double or numeric string could be cast to an integer"
                    + " without losing data (or if it's an integer). For instance,"
                    + " is_numeric(4.5) would return true, and integer(4.5) would work,"
                    + " however, equals(4.5, integer(4.5)) returns false, because the"
                    + " value was narrowed to 4.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return false;
        }
        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            double d;
            try {
                d = Static.getDouble(args[0]);
            } catch (ConfigRuntimeException e) {
                return new CBoolean(false, t);
            }
            return new CBoolean((long) d == d, t);
        }

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        @Override
        public boolean canOptimize() {
            return true;
        }

        @Override
        public Construct optimize(Target t, Construct... args) throws ConfigCompileException {
            return exec(t, null, args);
        }
    }

    @api
    public static class proc extends AbstractFunction {

        public String getName() {
            return "proc";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public String docs() {
            return "void {[name], [ivar...], procCode} Creates a new user defined procedure (also known as \"function\") that can be called later in code. Please see the more detailed"
                    + " documentation on procedures for more information.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.FormatException};
        }

        public boolean isRestricted() {
            return true;
        }

        @Override
        public boolean preResolveVariables() {
            return false;
        }

        public CHVersion since() {
            return CHVersion.V3_1_3;
        }

        public Boolean runAsync() {
            return null;
        }

        @Override
        public Construct execs(Target t, Env env, Script parent, GenericTreeNode<Construct>... nodes) {
            Procedure myProc = getProcedure(t, env, parent, nodes);
            env.GetProcs().put(myProc.getName(), myProc);
            return new CVoid(t);
        }
        
        public static Procedure getProcedure(Target t, Env env, Script parent, GenericTreeNode<Construct> ... nodes){
            String name = "";
            List<IVariable> vars = new ArrayList<IVariable>();
            GenericTreeNode<Construct> tree = null;
	    boolean usesAssign = false;
            for (int i = 0; i < nodes.length; i++) {
                if (i == nodes.length - 1) {
                    tree = nodes[i];
                } else {
			if(nodes[i].getData() instanceof CFunction){
				if(((CFunction)nodes[i].getData()).getValue().equals("assign")){
					if(nodes[i].getChildAt(1).getData().isDynamic()){
						usesAssign = true;						
					}
				}
			}
                    Construct cons = parent.eval(nodes[i], env);
                    if (i == 0 && cons instanceof IVariable) {
                        throw new ConfigRuntimeException("Anonymous Procedures are not allowed", ExceptionType.InvalidProcedureException, t);
                    } else {
                        if (i == 0 && !(cons instanceof IVariable)) {
                            name = cons.val();
                        } else {
                            if (!(cons instanceof IVariable)) {
                                throw new ConfigRuntimeException("You must use IVariables as the arguments", ExceptionType.InvalidProcedureException, t);
                            } else {
				IVariable ivar = null;
				try {
					Construct c = cons;
					while(c instanceof IVariable){
						c = env.GetVarList().get(((IVariable)c).getName(), t).ival();
					}
					ivar = new IVariable(((IVariable)cons).getName(), c.clone(), t);
				} catch (CloneNotSupportedException ex) {
					//
				}
                                vars.add(ivar);
                            }
                        }
                    }
                }
            }
            Procedure myProc = new Procedure(name, vars, tree, t);
	    if(usesAssign){
		    myProc.definitelyNotConstant();
	    }
	    return myProc;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            return new CVoid(t);
        }

        @Override
        public boolean useSpecialExec() {
            return true;
        }

        /**
         * Returns either null to indicate that the procedure is not const, or
         * returns a single Construct, which should replace the call to the
         * procedure.
         * @param t
         * @param myProc
         * @param children
         * @return
         * @throws ConfigCompileException
         * @throws ConfigRuntimeException 
         */
        public static Construct optimizeProcedure(Target t, Procedure myProc, List<GenericTreeNode<Construct>> children) throws ConfigCompileException, ConfigRuntimeException {            
            if(myProc.isPossiblyConstant()){
                //Oooh, it's possibly constant. So, let's run it with our children.
                try{
                    GenericTreeNode<Construct> root = new GenericTreeNode<Construct>(new CFunction("__autoconcat__", Target.UNKNOWN));
                    Script fakeScript = Script.GenerateScript(root, "*");
                    Env env = new Env();
                    env.SetScript(fakeScript);
                    Construct c = myProc.cexecute(children, env);
                    //Yup! It worked. It's a const proc.
                    return c;
                } catch(ConfigRuntimeException e){
                    if(e.getExceptionType() == ExceptionType.InvalidProcedureException){
                        //This is the only valid exception that doesn't strictly mean it's a bad
                        //call.
                        return null;
                    }
                    throw e; //Rethrow it. Since the functions are all static, and we actually are
                    //running it with a mostly legit environment, this is a real runtime error,
                    //and we can safely convert it to a compile error upstream
                } catch(Exception e){
                    //Nope. Something is preventing us from running it statically.
                    //We don't really care. We just know it can't be optimized.
                    return null;
                }
            } else {
                //Oh. Well, we tried.
                return null;
            }
        }

//        @Override
//        public boolean canOptimizeDynamic() {
//            return true;
//        }
//
//        @Override
//        public GenericTreeNode<Construct> optimizeDynamic(Target t, List<GenericTreeNode<Construct>> children) throws ConfigCompileException, ConfigRuntimeException {
//            //We seriously lose out on the ability to optimize this procedure
//            //if we are assigning a dynamic value as a default, but we have to check
//            //that here. If we don't, we lose the information
//            return ;
//        }                
                
    }

    @api
    public static class _return extends AbstractFunction {

        public String getName() {
            return "return";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1};
        }

        public String docs() {
            return "nothing {mixed} Returns the specified value from this procedure. It cannot be called outside a procedure.";
        }

        public ExceptionType[] thrown() {
            return null;
        }

        public boolean isRestricted() {
            return false;
        }
        public CHVersion since() {
            return CHVersion.V3_2_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            Construct ret = (args.length == 1 ? args[0] : new CVoid(t));
            throw new FunctionReturnException(ret);
        }
    }

    @api
    public static class include extends AbstractFunction {

        public String getName() {
            return "include";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "void {path} Includes external code at the specified path.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.IncludeException};
        }

        public boolean isRestricted() {
            return true;
        }
        public CHVersion since() {
            return CHVersion.V3_2_0;
        }

        public Boolean runAsync() {
            return true;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            return new CVoid(t);
        }

        @Override
        public Construct execs(Target t, Env env, Script parent, GenericTreeNode<Construct>... nodes) {
            GenericTreeNode<Construct> tree = nodes[0];
            Construct arg = parent.seval(tree, env);
            String location = arg.val();
            GenericTreeNode<Construct> include = IncludeCache.get(new File(t.file().getParent(), location), t);
            parent.eval(include.getChildAt(0), env);
            return new CVoid(t);
        }
        @Override
        public boolean useSpecialExec() {
            return true;
        }

        @Override
        public boolean canOptimize() {
            return true;
        }

        @Override
        public Construct optimize(Target t, Construct... args) throws ConfigCompileException {
            //We can't optimize per se, but if the path is constant, and the code is uncompilable, we
            //can give a warning, and go ahead and cache the tree.
            String path = args[0].val();
            IncludeCache.get(new File(t.file().getParent(), path), t);
            return null;
        }
    }

    @api
    public static class call_proc extends AbstractFunction {

        public String getName() {
            return "call_proc";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public String docs() {
            return "mixed {proc_name, [var1...]} Dynamically calls a user defined procedure. call_proc(_myProc, 'var1') is the equivalent of"
                    + " _myProc('var1'), except you could dynamically build the procedure name if need be. This is useful for having callbacks"
                    + " in procedures. Throws an InvalidProcedureException if the procedure isn't defined.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.InvalidProcedureException};
        }

        public boolean isRestricted() {
            return true;
        }
        public CHVersion since() {
            return CHVersion.V3_2_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            return new CVoid(t);
        }

        @Override
        public Construct execs(Target t, Env env, Script parent, GenericTreeNode<Construct>... nodes) {
            if(nodes.length < 1){
                throw new ConfigRuntimeException("Expecting at least one argument to call_proc", ExceptionType.InsufficientArgumentsException, t);
            }
            Construct[] args = new Construct[nodes.length];
            for (int i = 0; i < nodes.length; i++) {
                args[i] = parent.seval(nodes[i], env);
            }

            Procedure proc = env.GetProcs().get(args[0].val());
            if (proc != null) {
                List<Construct> vars = new ArrayList<Construct>(Arrays.asList(args));
                vars.remove(0);
                Env newEnv = null;
                try {
                    newEnv = env.clone();
                } catch (CloneNotSupportedException ex) {}
                return proc.execute(vars, newEnv);
            }
            throw new ConfigRuntimeException("Unknown procedure \"" + args[0].val() + "\"",
                    ExceptionType.InvalidProcedureException, t);
        }
        @Override
        public boolean useSpecialExec() {
            return true;
        }
    }

    @api
    public static class is_proc extends AbstractFunction {

        public String getName() {
            return "is_proc";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "boolean {procName} Returns whether or not the given procName is currently defined, i.e. if calling this proc wouldn't"
                    + " throw an exception.";
        }

        public ExceptionType[] thrown() {
            return null;
        }

        public boolean isRestricted() {
            return true;
        }
        public CHVersion since() {
            return CHVersion.V3_2_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) {
            return new CBoolean(env.GetProcs().get(args[0].val()) == null ? false : true, t);
        }
    }

    @api
    public static class is_associative extends AbstractFunction {

        public String getName() {
            return "is_associative";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "boolean {array} Returns whether or not the array is associative. If the parameter is not an array, throws a CastException.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return false;
        }
        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            if (args[0] instanceof CArray) {
                return new CBoolean(((CArray) args[0]).inAssociativeMode(), t);
            } else {
                throw new ConfigRuntimeException(this.getName() + " expects argument 1 to be an array", ExceptionType.CastException, t);
            }
        }

        @Override
        public boolean canOptimize() {
            return true;
        }

        @Override
        public Construct optimize(Target t, Construct... args) throws ConfigCompileException {
            return exec(t, null, args);
        }
    }

    @api
    public static class is_closure extends AbstractFunction {

        public String getName() {
            return "is_closure";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "boolean {arg} Returns true if the argument is a closure (could be executed)"
                    + " or false otherwise";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{};
        }

        public boolean isRestricted() {
            return false;
        }
        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            return new CBoolean(args[0] instanceof CClosure, t);
        }

        public CHVersion since() {
            return CHVersion.V3_3_1;
        }
    }

    @api
    public static class _import extends AbstractFunction {

        public String getName() {
            return "import";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public String docs() {
            return "mixed {ivar | key[, namespace, ...,]} This function imports a value from the global value"
                    + " register. In the first mode, it looks for an ivariable with the specified"
                    + " name, and stores the value in the variable, and returns void. In the"
                    + " second mode, it looks for a value stored with the specified key, and"
                    + " returns that value. Items can be stored with the export function. If"
                    + " the specified ivar doesn't exist, the ivar will be assigned an empty"
                    + " string, and if the specified string key doesn't exist, null is returned."
                    + " See the documentation on [[CommandHelper/import-export|imports/exports]]"
                    + " for more information.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{};
        }

        public boolean isRestricted() {
            return true;
        }

        @Override
        public boolean preResolveVariables() {
            return false;
        }

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            if (args[0] instanceof IVariable) {
                //Mode 1     
                IVariable var = (IVariable) args[0];
                environment.GetVarList().set(Globals.GetGlobalIVar(var));
                return new CVoid(t);
            } else {
                //Mode 2
                String key = GetNamespace(args, null, getName(), t);
                return Globals.GetGlobalConstruct(key);
            }
        }
    }

    @api
    public static class _export extends AbstractFunction {

        public String getName() {
            return "export";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public String docs() {
            return "void {ivar | key[, namespace, ...,], value} Stores a value in the global storage register."
                    + " When using the first mode, the ivariable is stored so it can be imported"
                    + " later, and when using the second mode, an arbitrary value is stored with"
                    + " the give key, and can be retreived using the secode mode of import. If"
                    + " the value is already stored, it is overwritten. See import() and"
                    + " [[CommandHelper/import-export|importing/exporting]]";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.InsufficientArgumentsException};
        }

        public boolean isRestricted() {
            return true;
        }

        @Override
        public boolean preResolveVariables() {
            return false;
        }

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            if (args.length == 1) {
                if (args[0] instanceof IVariable) {
                    IVariable cur = (IVariable) args[0];
                    Globals.SetGlobal(environment.GetVarList().get(cur.getName(), cur.getTarget()));
                } else {
                    throw new ConfigRuntimeException("Expecting a IVariable when only one parameter is specified", ExceptionType.InsufficientArgumentsException, t);
                }
            } else {
                String key = GetNamespace(args, args.length - 1, getName(), t);
                Construct c = args[args.length - 1];
                //We want to store the value contained, not the ivar itself
                while(c instanceof IVariable){
                    c = environment.GetVarList().get(((IVariable)c).getName(), t).ival();
                }
                Globals.SetGlobal(key, c);
            }
            return new CVoid(t);
        }
    }

    @api
    public static class closure extends AbstractFunction {

        public String getName() {
            return "closure";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public String docs() {
            return "closure {[varNames...,] code} Returns a closure on the provided code. A closure is"
                    + " a datatype that represents some code as code, not the results of some"
                    + " code after it is run. Code placed in a closure can be used as"
                    + " a string, or executed by other functions using the eval() function."
                    + " If a closure is \"to string'd\" it will not necessarily look like"
                    + " the original code, but will be functionally equivalent. The current environment"
                    + " is \"snapshotted\" and stored with the closure, however, this information is"
                    + " only stored in memory, it isn't retained during a serialization operation."
                    + " Also, the special variable @arguments is automatically created for you, and contains"
                    + " an array of all the arguments passed to the closure, much like procedures."
                    + " See the wiki article on [[CommandHelper/Closures|closures]] for more details"
                    + " and examples.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{};
        }

        public boolean isRestricted() {
            return true;
        }

        @Override
        public boolean preResolveVariables() {
            return false;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            return new CVoid(t);
        }

        @Override
        public Construct execs(Target t, Env env, Script parent, GenericTreeNode<Construct>... nodes) {
            String[] names = new String[nodes.length - 1];
            Construct[] defaults = new Construct[nodes.length - 1];
            for (int i = 0; i < nodes.length - 1; i++) {
                GenericTreeNode<Construct> node = nodes[i];
                GenericTreeNode<Construct> newNode = new GenericTreeNode<Construct>(new CFunction("g", t));
                List<GenericTreeNode<Construct>> children = new ArrayList<GenericTreeNode<Construct>>();
                children.add(node);
                newNode.setChildren(children);
                Script fakeScript = Script.GenerateScript(newNode, env.GetLabel());
                Construct ret = MethodScriptCompiler.execute(newNode, env, null, fakeScript);
                if (!(ret instanceof IVariable)) {
                    throw new ConfigRuntimeException("Arguments sent to closure (barring the last) must be ivariables", ExceptionType.CastException, t);
                }
                names[i] = ((IVariable) ret).getName();
                try {
                    defaults[i] = ((IVariable) ret).ival().clone();
                } catch (CloneNotSupportedException ex) {
                    Logger.getLogger(DataHandling.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            CClosure closure = new CClosure(nodes[nodes.length - 1], env, names, defaults, t);
            return closure;
        }

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }
        @Override
        public boolean useSpecialExec() {
            return true;
        }
    }

    @api
    public static class execute extends AbstractFunction {

        public String getName() {
            return "execute";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public String docs() {
            return "void {[values...,] closure} Executes the given closure. You can also send arguments"
                    + " to the closure, which it may or may not use, depending on the particular closure's"
                    + " definition.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return true;
        }
        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            if (args[args.length - 1] instanceof CClosure) {
                Construct[] vals = new Construct[args.length - 1];
                System.arraycopy(args, 0, vals, 0, args.length - 1);
                CClosure closure = (CClosure) args[args.length - 1];
                closure.execute(vals);
            } else {
                throw new ConfigRuntimeException("Only a closure (created from the closure function) can be sent to execute()", ExceptionType.CastException, t);
            }
            return new CVoid(t);
        }

        public CHVersion since() {
            return CHVersion.V3_3_1;
        }
    }

    @api
    public static class _boolean extends AbstractFunction {

        public String getName() {
            return "boolean";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "boolean {item} Returns a new construct that has been cast to a boolean. The item is cast according to"
                    + " the boolean conversion rules. Since all data types can be cast to a"
                    + " a boolean, this function will never throw an exception.";
        }

        public ExceptionType[] thrown() {
            return null;
        }

        public boolean isRestricted() {
            return false;
        }
        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            return new CBoolean(Static.getBoolean(args[0]), t);
        }

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        @Override
        public boolean canOptimize() {
            return true;
        }

        @Override
        public Construct optimize(Target t, Construct... args) throws ConfigCompileException {
            return exec(t, null, args);
        }
    }

    @api
    public static class _integer extends AbstractFunction {

        public String getName() {
            return "integer";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "integer {item} Returns a new construct that has been cast to an integer."
                    + " This function will throw a CastException if is_numeric would return"
                    + " false for this item, but otherwise, it will be cast properly. Data"
                    + " may be lost in this conversion. For instance, 4.5 will be converted"
                    + " to 4, by using integer truncation. You can use is_integral to see"
                    + " if this data loss would occur.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return false;
        }
        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            return new CInt((long) Static.getDouble(args[0]), t);
        }

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        @Override
        public boolean canOptimize() {
            return true;
        }

        @Override
        public Construct optimize(Target t, Construct... args) throws ConfigCompileException {
            return exec(t, null, args);
        }
    }

    @api
    public static class _double extends AbstractFunction {

        public String getName() {
            return "double";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "double {item} Returns a new construct that has been cast to an double."
                    + " This function will throw a CastException if is_numeric would return"
                    + " false for this item, but otherwise, it will be cast properly.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return false;
        }
        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            return new CDouble(Static.getDouble(args[0]), t);
        }

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        @Override
        public boolean canOptimize() {
            return true;
        }

        @Override
        public Construct optimize(Target t, Construct... args) throws ConfigCompileException {
            return exec(t, null, args);
        }
    }

    @api
    public static class _string extends AbstractFunction {

        public String getName() {
            return "string";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "string {item} Creates a new construct that is the \"toString\" of an item."
                    + " For arrays, an human readable version is returned; this should not be"
                    + " used directly, as the format is not guaranteed. Booleans return \"true\""
                    + " or \"false\" and null returns \"null\".";
        }

        public ExceptionType[] thrown() {
            return null;
        }

        public boolean isRestricted() {
            return false;
        }
        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            return new CString(args[0].val(), t);
        }

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        @Override
        public boolean canOptimize() {
            return true;
        }

        @Override
        public Construct optimize(Target t, Construct... args) throws ConfigCompileException {
            return exec(t, null, args);
        }
    }

    /**
     * Generates the namespace for this value, given an array of constructs.
     * If the entire list of arguments isn't supposed to be part of the namespace,
     * the value to be excluded may be specified.
     * @param args
     * @param exclude
     * @return
     */
    private static String GetNamespace(Construct [] args, Integer exclude, String name, Target t){
        if(exclude != null && args.length < 2 || exclude == null && args.length < 1){
            throw new ConfigRuntimeException(name + " was not provided with enough arguments. Check the documentation, and try again.", ExceptionType.InsufficientArgumentsException, t);
        }
        boolean first = true;
        StringBuilder b = new StringBuilder();
        for(int i = 0; i < args.length; i++){
            if(exclude != null && exclude == i){
                continue;
            }
            if(!first){
                b.append(".");
            }
            first = false;
            b.append(args[i].val());
        }
        return b.toString();
    }
}
