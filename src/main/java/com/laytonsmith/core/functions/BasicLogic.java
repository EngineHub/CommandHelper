package com.laytonsmith.core.functions;

import com.laytonsmith.annotations.api;
import com.laytonsmith.core.*;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Layton
 */
public class BasicLogic {

    public static String docs() {
        return "These functions provide basic logical operations.";
    }

    @api
    public static class _if extends AbstractFunction {

        public String getName() {
            return "if";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        @Override
        public Construct execs(Target t, Env env, Script parent, ParseTree... nodes) {
            for (ParseTree node : nodes) {
                if (node.getData() instanceof CIdentifier) {
                    return new ifelse().execs(t, env, parent, nodes);
                }
            }
            ParseTree condition = nodes[0];
            ParseTree __if = nodes[1];
            ParseTree __else = null;
            if (nodes.length == 3) {
                __else = nodes[2];
            }

            if (Static.getBoolean(parent.seval(condition, env))) {
                return parent.seval(__if, env);
            } else {
                if (__else == null) {
                    return new CVoid(t);
                }
                return parent.seval(__else, env);
            }
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            return new CVoid(t);
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public String docs() {
            return "mixed {cond, trueRet, [falseRet]} If the first argument evaluates to a true value, the second argument is returned, otherwise the third argument is returned."
                    + " If there is no third argument, it returns void.";
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
        //Doesn't matter, this function is run out of state

        public Boolean runAsync() {
            return false;
        }

        @Override
        public boolean useSpecialExec() {
            return true;
        }

        @Override
        public boolean canOptimizeDynamic() {
            return true;
        }

        @Override
        public ParseTree optimizeDynamic(Target t, List<ParseTree> args) throws ConfigCompileException {
            for (ParseTree arg : args) {
                //If any are CIdentifiers, forward this to ifelse
                if (arg.getData() instanceof CIdentifier) {
                    return new ifelse().optimizeDynamic(t, args);
                }
            }
            //Now check for too many/too few arguments
            if (args.size() == 1 || args.size() > 3) {
                throw new ConfigCompileException("Incorrect number of arguments passed to if()", t);
            }
            if (args.get(0).getData().isDynamic()) {
                return super.optimizeDynamic(t, args); //Can't optimize
            } else {
                if (Static.getBoolean(args.get(0).getData())) {
                    return args.get(1);
                } else {
                    if (args.size() == 3) {
                        return args.get(2);
                    } else {
                        ParseTree node = new ParseTree(new CVoid(t));
                        node.setOptimized(true);
                        return node;
                    }
                }
            }
        }

        @Override
        public boolean allowBraces() {
            return true;
        }                
    }

    @api
    public static class _switch extends AbstractFunction {

        public String getName() {
            return "switch";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public String docs() {
            return "mixed {value, [equals, code]..., [defaultCode]} Provides a switch statement. If none of the conditions"
                    + " match, and no default is provided, void is returned."
                    + " See the documentation on [[CommandHelper/Logic|Logic]] for more information.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.InsufficientArgumentsException};
        }

        public boolean isRestricted() {
            return false;
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
            return new CNull(t);
        }

        @Override
        public Construct execs(Target t, Env env, Script parent, ParseTree... nodes) {
            Construct value = parent.seval(nodes[0], env);
            equals equals = new equals();
            for (int i = 1; i <= nodes.length - 2; i += 2) {
                ParseTree statement = nodes[i];
                ParseTree code = nodes[i + 1];
                Construct evalStatement = parent.seval(statement, env);
                if (evalStatement instanceof CArray) {
                    for (String index : ( (CArray) evalStatement ).keySet()) {
                        Construct inner = ( (CArray) evalStatement ).get(index);
                        if (( (CBoolean) equals.exec(t, env, value, inner) ).getBoolean()) {
                            return parent.seval(code, env);
                        }
                    }
                } else {
                    if (( (CBoolean) equals.exec(t, env, value, evalStatement) ).getBoolean()) {
                        return parent.seval(code, env);
                    }
                }
            }
            if (nodes.length % 2 == 0) {
                return parent.seval(nodes[nodes.length - 1], env);
            }
            return new CVoid(t);
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
    public static class ifelse extends AbstractFunction {

        public String getName() {
            return "ifelse";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public String docs() {
            return "mixed {[boolean1, code]..., [elseCode]} Provides a more convenient method"
                    + " for running if/else chains. If none of the conditions are true, and"
                    + " there is no 'else' condition, void is returned.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.InsufficientArgumentsException};
        }

        public boolean isRestricted() {
            return false;
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
            return new CNull(t);
        }

        @Override
        public Construct execs(Target t, Env env, Script parent, ParseTree... nodes) {
            if (nodes.length < 2) {
                throw new ConfigRuntimeException("ifelse expects at least 2 arguments", ExceptionType.InsufficientArgumentsException, t);
            }
            for (int i = 0; i <= nodes.length - 2; i += 2) {
                ParseTree statement = nodes[i];
                ParseTree code = nodes[i + 1];
                Construct evalStatement = parent.seval(statement, env);
                if (evalStatement instanceof CIdentifier) {
                    evalStatement = parent.seval(( (CIdentifier) evalStatement ).contained(), env);
                }
                if (Static.getBoolean(evalStatement)) {
                    Construct ret = env.GetScript().eval(code, env);
                    return ret;
                }
            }
            if (nodes.length % 2 == 1) {
                Construct ret = env.GetScript().seval(nodes[nodes.length - 1], env);
                if (ret instanceof CIdentifier) {
                    return parent.seval(( (CIdentifier) ret ).contained(), env);
                } else {
                    return ret;
                }
            }
            return new CVoid(t);
        }

        @Override
        public boolean useSpecialExec() {
            return true;
        }

        @Override
        public boolean canOptimizeDynamic() {
            return true;
        }

        @Override
        public ParseTree optimizeDynamic(Target t, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException {

            List<ParseTree> optimizedTree = new ArrayList<ParseTree>();
            //We have to cache the return value if even if we find it, so we can check for syntax errors
            //in all the branches, not just the ones before the first hardcoded true
            ParseTree toReturn = null;
            for (int i = 0; i <= children.size() - 2; i += 2) {
                ParseTree statement = children.get(i);
                ParseTree code = children.get(i + 1);
                Construct evalStatement = statement.getData();
                if (evalStatement instanceof CIdentifier) {
                    //check for an else here, if so, it's a compile error
                    if(evalStatement.val().equals("else")){
                        throw new ConfigCompileException("Unexpected else", t);
                    }
                }
                if(!statement.getData().isDynamic()){
                    if (evalStatement instanceof CIdentifier) {
                        evalStatement = ( (CIdentifier) evalStatement ).contained().getData();                       
                    }
                    //If it's hardcoded true, we found it.
                    if (Static.getBoolean(evalStatement)) {
                        if(toReturn == null){
                            toReturn = code;
                        }
                    } //else it's hard coded false, and we can ignore it.
                } else {
                    optimizedTree.add(statement);
                    optimizedTree.add(code);
                }
            }
            if(toReturn != null){
                return toReturn;
            }
            if (children.size() % 2 == 1) {
                ParseTree ret = children.get(children.size() - 1);
                if (ret.getData() instanceof CIdentifier) {
                    optimizedTree.add(( (CIdentifier) ret.getData() ).contained());
                } else {
                    optimizedTree.add(ret);
                }
                if(optimizedTree.size() == 1){
                    //Oh. Well, we can just return this node then.
                    return optimizedTree.get(0);
                }
            }
            if(optimizedTree.size() == 1){
                //The whole tree has been optimized out. Return void
                return new ParseTree(new CVoid(t));
            }
            ParseTree node = new ParseTree(new CFunction(this.getName(), t));
            node.setChildren(optimizedTree);
            node.setOptimized(true);
            return node;
            
        }
//        @Override
//        public Construct optimize(Target t, Construct... args) throws ConfigCompileException {
//            boolean inNewMode = false;
//            for(int i = 0; i < args.length; i++){
//                if(args[0] instanceof CIdentifier){
//                    inNewMode = true;
//                    break;
//                }
//            }
//            
//            if(!inNewMode){
//                return null;//TODO: We can optimize this, even with some parameters dynamic, but
//                we need the tree.
//            } else {
//                Only an else shoved in the middle is disallowed
//                if(!(args[args.length - 1] instanceof CIdentifier)){
//                    throw new ConfigCompileException("Syntax error", t);
//                }
//                for(int i = 1; i <= args.length; i++){
//                    if(!(args[i] instanceof CIdentifier)){
//                        throw new ConfigCompileException("Syntax error", t);
//                    } else {
//                        CIdentifier ci = (CIdentifier)args[i];
//                        if(ci.val().equals("else")){
//                            throw new ConfigCompileException("Unexpected else, was expecting else if", t);
//                        }
//                    }
//                }
//                return null;
//            }
//        }
//
//        public ParseTree optimizeSpecial(Target target, List<ParseTree> children) {
//            throw new UnsupportedOperationException("Not yet implemented");
//        }
    }

    @api
    public static class equals extends AbstractFunction {
		
		private static equals self = new equals();
		/**
		 * Returns the results that this function would provide, but
		 * in a java specific manner, so other code may easily determine
		 * how this method would respond.
		 * @param one
		 * @param two
		 * @return 
		 */
		public static boolean doEquals(Construct one, Construct two){
			CBoolean ret = (CBoolean)self.exec(Target.UNKNOWN, null, one, two);
			return ret.getBoolean();
		}

        public String getName() {
            return "equals";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            if (args.length <= 1) {
                throw new ConfigRuntimeException("At least two arguments must be passed to equals", ExceptionType.InsufficientArgumentsException, t);
            }
			boolean referenceMatch = true;
			for(int i = 0; i < args.length - 1; i++){
				if(args[i] != args[i + 1]){
					referenceMatch = false;
					break;
				}
			}
			if(referenceMatch){
				return new CBoolean(true, t);
			}
            if (Static.anyBooleans(args)) {
                boolean equals = true;
                for (int i = 1; i < args.length; i++) {
                    boolean arg1 = Static.getBoolean(args[i - 1]);
                    boolean arg2 = Static.getBoolean(args[i]);
                    if (arg1 != arg2) {
                        equals = false;
                        break;
                    }
                }
                return new CBoolean(equals, t);
            }

            {
                boolean equals = true;
                for (int i = 1; i < args.length; i++) {
                    if (!args[i - 1].val().equals(args[i].val())) {
                        equals = false;
                        break;
                    }
                }
                if (equals) {
                    return new CBoolean(true, t);
                }
            }
            try {
                boolean equals = true;
                for (int i = 1; i < args.length; i++) {
                    double arg1 = Static.getNumber(args[i - 1]);
                    double arg2 = Static.getNumber(args[i]);
                    if (arg1 != arg2) {
                        equals = false;
                        break;
                    }
                }
                return new CBoolean(equals, t);
            }
            catch (ConfigRuntimeException e) {
                return new CBoolean(false, t);
            }
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.InsufficientArgumentsException};
        }

        public String docs() {
            return "boolean {var1, var2[, varX...]} Returns true or false if all the arguments are equal";
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
    public static class sequals extends AbstractFunction {

        public String getName() {
            return "sequals";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            return "boolean {val1, val2} Uses a strict equals check, which determines if"
                    + " two values are not only equal, but also the same type. So, while"
                    + " equals('1', 1) returns true, sequals('1', 1) returns false, because"
                    + " the first one is a string, and the second one is an int. More often"
                    + " than not, you want to use plain equals().";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{};
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

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            equals equals = new equals();
            if (args[1].getClass().equals(args[0].getClass())
                    && ( (CBoolean) equals.exec(t, environment, args) ).getBoolean()) {
                return new CBoolean(true, t);
            } else {
                return new CBoolean(false, t);
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
    public static class snequals extends AbstractFunction {

        public String getName() {
            return "snequals";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            return "boolean {val1, val2} Equivalent to not(sequals(val1, val2))";
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
            return new CBoolean(!( (CBoolean) new sequals().exec(t, environment, args) ).getBoolean(), t);
        }

        public CHVersion since() {
            return CHVersion.V3_3_1;
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
    public static class nequals extends AbstractFunction {

        public String getName() {
            return "nequals";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            return "boolean {val1, val2} Returns true if the two values are NOT equal, or false"
                    + " otherwise. Equivalent to not(equals(val1, val2))";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{};
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
            equals e = new equals();
            CBoolean b = (CBoolean) e.exec(t, env, args);
            return new CBoolean(!b.getBoolean(), t);
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
    public static class equals_ic extends AbstractFunction {

        public String getName() {
            return "equals_ic";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public String docs() {
            return "boolean {val1, val2[, valX...]} Returns true if all the values are equal to each other, while"
                    + " ignoring case.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.InsufficientArgumentsException};
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
            if (args.length <= 1) {
                throw new ConfigRuntimeException("At least two arguments must be passed to equals_ic", ExceptionType.InsufficientArgumentsException, t);
            }
            if (Static.anyBooleans(args)) {
                boolean equals = true;
                for (int i = 1; i < args.length; i++) {
                    boolean arg1 = Static.getBoolean(args[i - 1]);
                    boolean arg2 = Static.getBoolean(args[i]);
                    if (arg1 != arg2) {
                        equals = false;
                        break;
                    }
                }
                return new CBoolean(equals, t);
            }

            {
                boolean equals = true;
                for (int i = 1; i < args.length; i++) {
                    if (!args[i - 1].val().equalsIgnoreCase(args[i].val())) {
                        equals = false;
                        break;
                    }
                }
                if (equals) {
                    return new CBoolean(true, t);
                }
            }
            try {
                boolean equals = true;
                for (int i = 1; i < args.length; i++) {
                    double arg1 = Static.getNumber(args[i - 1]);
                    double arg2 = Static.getNumber(args[i]);
                    if (arg1 != arg2) {
                        equals = false;
                        break;
                    }
                }
                return new CBoolean(equals, t);
            }
            catch (ConfigRuntimeException e) {
                return new CBoolean(false, t);
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
    public static class nequals_ic extends AbstractFunction {

        public String getName() {
            return "nequals_ic";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            return "boolean {val1, val2} Returns true if the two values are NOT equal to each other, while"
                    + " ignoring case.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{};
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

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            equals_ic e = new equals_ic();
            return new CBoolean(!( (CBoolean) e.exec(t, environment, args) ).getBoolean(), t);
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
    public static class lt extends AbstractFunction {

        public String getName() {
            return "lt";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            double arg1 = Static.getNumber(args[0]);
            double arg2 = Static.getNumber(args[1]);
            return new CBoolean(arg1 < arg2, t);
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public String docs() {
            return "boolean {var1, var2} Returns the results of a less than operation";
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
    public static class gt extends AbstractFunction {

        public String getName() {
            return "gt";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            double arg1 = Static.getNumber(args[0]);
            double arg2 = Static.getNumber(args[1]);
            return new CBoolean(arg1 > arg2, t);
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public String docs() {
            return "boolean {var1, var2} Returns the result of a greater than operation";
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
    public static class lte extends AbstractFunction {

        public String getName() {
            return "lte";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            double arg1 = Static.getNumber(args[0]);
            double arg2 = Static.getNumber(args[1]);
            return new CBoolean(arg1 <= arg2, t);
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public String docs() {
            return "boolean {var1, var2} Returns the result of a less than or equal to operation";
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
    public static class gte extends AbstractFunction {

        public String getName() {
            return "gte";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            double arg1 = Static.getNumber(args[0]);
            double arg2 = Static.getNumber(args[1]);
            return new CBoolean(arg1 >= arg2, t);
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public String docs() {
            return "boolean {var1, var2} Returns the result of a greater than or equal to operation";
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
    public static class and extends AbstractFunction {

        public String getName() {
            return "and";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public Construct exec(Target t, Env env, Construct... args) {
            return new CNull(t);
        }

        @Override
        public Construct execs(Target t, Env env, Script parent, ParseTree... nodes) {
            for (ParseTree tree : nodes) {
                Construct c = env.GetScript().seval(tree, env);
                boolean b = Static.getBoolean(c);
                if (b == false) {
                    return new CBoolean(false, t);
                }
            }
            return new CBoolean(true, t);
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public String docs() {
            return "boolean {var1, [var2...]} Returns the boolean value of a logical AND across all arguments. Uses lazy determination, so once "
                    + "an argument returns false, the function returns.";
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

        @Override
        public boolean useSpecialExec() {
            return true;
        }
    }

    @api
    public static class or extends AbstractFunction {

        public String getName() {
            return "or";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public Construct exec(Target t, Env env, Construct... args) {
            return new CNull(t);
        }

        @Override
        public Construct execs(Target t, Env env, Script parent, ParseTree... nodes) {
            for (ParseTree tree : nodes) {
                Construct c = env.GetScript().eval(tree, env);
                if (Static.getBoolean(c)) {
                    return new CBoolean(true, t);
                }
            }
            return new CBoolean(false, t);
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public String docs() {
            return "boolean {var1, [var2...]} Returns the boolean value of a logical OR across all arguments. Uses lazy determination, so once an "
                    + "argument resolves to true, the function returns.";
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

        @Override
        public boolean useSpecialExec() {
            return true;
        }
    }

    @api
    public static class not extends AbstractFunction {

        public String getName() {
            return "not";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            return new CBoolean(!Static.getBoolean(args[0]), t);
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public String docs() {
            return "boolean {var1} Returns the boolean value of a logical NOT for this argument";
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
    public static class xor extends AbstractFunction {

        public String getName() {
            return "xor";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            return "boolean {val1, val2} Returns the xor of the two values.";
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

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            boolean val1 = Static.getBoolean(args[0]);
            boolean val2 = Static.getBoolean(args[1]);
            return new CBoolean(val1 ^ val2, t);
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
    public static class nand extends AbstractFunction {

        public String getName() {
            return "nand";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public String docs() {
            return "boolean {val1, [val2...]} Return the equivalent of not(and())";
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

        public Construct exec(Target t, Env environment, Construct... args) {
            return new CNull(t);
        }

        @Override
        public Construct execs(Target t, Env env, Script parent, ParseTree... nodes) {
            and and = new and();
            boolean val = ( (CBoolean) and.execs(t, env, parent, nodes) ).getBoolean();
            return new CBoolean(!val, t);
        }

        @Override
        public boolean useSpecialExec() {
            return true;
        }
    }

    @api
    public static class nor extends AbstractFunction {

        public String getName() {
            return "nor";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public String docs() {
            return "boolean {val1, [val2...]} Returns the equivalent of not(or())";
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

        public Construct exec(Target t, Env environment, Construct... args) {
            return new CNull(t);
        }

        @Override
        public Construct execs(Target t, Env environment, Script parent, ParseTree... args) throws ConfigRuntimeException {
            or or = new or();
            boolean val = ( (CBoolean) or.execs(t, environment, parent, args) ).getBoolean();
            return new CBoolean(!val, t);
        }

        @Override
        public boolean useSpecialExec() {
            return true;
        }
    }

    @api
    public static class xnor extends AbstractFunction {

        public String getName() {
            return "xnor";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            return "boolean {val1, val2} Returns the xnor of the two values";
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

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            xor xor = new xor();
            boolean val = ( (CBoolean) xor.exec(t, environment, args) ).getBoolean();
            return new CBoolean(!val, t);
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
    public static class bit_and extends AbstractFunction {

        public String getName() {
            return "bit_and";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public String docs() {
            return "int {int1, [int2...]} Returns the bitwise AND of the values";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.InsufficientArgumentsException};
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

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            if (args.length < 1) {
                throw new ConfigRuntimeException("bit_and requires at least one argument", ExceptionType.InsufficientArgumentsException, t);
            }
            long val = Static.getInt(args[0]);
            for (int i = 1; i < args.length; i++) {
                val = val & Static.getInt(args[i]);
            }
            return new CInt(val, t);
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
    public static class bit_or extends AbstractFunction {

        public String getName() {
            return "bit_or";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public String docs() {
            return "int {int1, [int2...]} Returns the bitwise OR of the specified values";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.InsufficientArgumentsException};
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

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            if (args.length < 1) {
                throw new ConfigRuntimeException("bit_or requires at least one argument", ExceptionType.InsufficientArgumentsException, t);
            }
            long val = Static.getInt(args[0]);
            for (int i = 1; i < args.length; i++) {
                val = val | Static.getInt(args[i]);
            }
            return new CInt(val, t);
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
    public static class bit_not extends AbstractFunction {

        public String getName() {
            return "bit_not";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "int {int1} Returns the bitwise NOT of the given value";
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

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            return new CInt(~Static.getInt(args[0]), t);
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
    public static class lshift extends AbstractFunction {

        public String getName() {
            return "lshift";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            return "int {value, bitsToShift} Left shifts the value bitsToShift times";
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

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            long value = Static.getInt(args[0]);
            long toShift = Static.getInt(args[1]);
            return new CInt(value << toShift, t);
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
    public static class rshift extends AbstractFunction {

        public String getName() {
            return "rshift";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            return "int {value, bitsToShift} Right shifts the value bitsToShift times";
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

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            long value = Static.getInt(args[0]);
            long toShift = Static.getInt(args[1]);
            return new CInt(value >> toShift, t);
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
    public static class urshift extends AbstractFunction {

        public String getName() {
            return "urshift";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            return "int {value, bitsToShift} Right shifts value bitsToShift times, pushing a 0, making"
                    + " this an unsigned right shift.";
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

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            long value = Static.getInt(args[0]);
            long toShift = Static.getInt(args[1]);
            return new CInt(value >>> toShift, t);
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
    public static class _elseif extends AbstractFunction {

        public String getName() {
            return "elseif";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "elseif {param} Returns an elseif construct. Used internally by the compiler, use"
                    + " in actual code will have undefined behavior.";
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
        public Construct execs(Target t, Env env, Script parent, ParseTree... nodes) {
            return new CIdentifier("elseif", nodes[0], t);
        }

        @Override
        public boolean useSpecialExec() {
            return true;
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            return new CNull(t);
        }

        public CHVersion since() {
            return CHVersion.V3_3_1;
        }

        @Override
        public boolean canOptimize() {
            return true;
        }

        @Override
        public Construct optimize(Target t, Construct... args) throws ConfigCompileException {
            return args[0];
        }

        @Override
        public boolean appearInDocumentation() {
            return false;
        }
    }

    @api
    public static class _else extends AbstractFunction {

        public String getName() {
            return "else";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "else {param} Returns an else construct. Used internally by the compiler, use in"
                    + " code will result in undefined behavior.";
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
        public boolean useSpecialExec() {
            return true;
        }

        @Override
        public Construct execs(Target t, Env env, Script parent, ParseTree... nodes) {
            return new CIdentifier("else", nodes[0], t);
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            return new CNull(t);
        }

        public CHVersion since() {
            return CHVersion.V3_3_1;
        }          

        @Override
        public ParseTree optimizeDynamic(Target t, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException {            
            return super.optimizeDynamic(t, children);
        }

        @Override
        public boolean canOptimizeDynamic() {
            return true;
        }

        @Override
        public boolean appearInDocumentation() {
            return false;
        }
    }
}
