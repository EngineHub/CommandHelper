
package com.laytonsmith.core.functions;

import com.laytonsmith.core.*;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import java.io.File;
import java.util.List;

/**
 * 
 * @author Layton
 */
public class BasicLogic {
    public static String docs(){
        return "These functions provide basic logical operations.";
    }
    @api public static class _if implements Function{

        public String getName() {
            return "if";
        }

        public Integer[] numArgs() {
            return new Integer[]{2, 3};
        }
        
        public Construct execs(int line_num, File f, Env env, Script parent, 
                GenericTreeNode<Construct> condition, GenericTreeNode<Construct> __if, 
                GenericTreeNode<Construct> __else) throws CancelCommandException{
            if(Static.getBoolean(parent.eval(condition, env))){
                return parent.eval(__if, env);
            } else {
                if(__else == null){
                    return new CVoid(line_num, f);
                }
                return parent.eval(__else, env);
            }
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            return new CVoid(line_num, f);
        }
        
        public ExceptionType[] thrown(){
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public String docs() {
            return "mixed {cond, trueRet, [falseRet]} If the first argument evaluates to a true value, the second argument is returned, otherwise the third argument is returned."
                    + " If there is no third argument, it returns void.";
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return false;
        }
        public String since() {
            return "3.0.1";
        }
        //Doesn't matter, this function is run out of state
        public Boolean runAsync() {
            return false;
        }
        
    }
    
    @api public static class _switch implements Function{

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

        public boolean preResolveVariables() {
            return false;
        }

        public String since() {
            return "3.3.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env environment, Construct... args) throws ConfigRuntimeException {
            return new CNull(line_num, f);
        }
        
        public Construct execs(int line_num, File f, Env env, List<GenericTreeNode<Construct>> children){
            Construct value = env.GetScript().preResolveVariable(env.GetScript().eval(children.get(0), env));
            equals equals = new equals();
            for(int i = 1; i < children.size() - 2; i+=2){
                GenericTreeNode<Construct> statement = children.get(i);
                GenericTreeNode<Construct> code = children.get(i + 1);
                Construct evalStatement = env.GetScript().eval(statement, env);
                evalStatement = env.GetScript().preResolveVariable(evalStatement);
                if(((CBoolean)equals.exec(line_num, f, env, value, evalStatement)).getBoolean()){
                    return env.GetScript().eval(code, env);
                }
            }
            if(children.size() % 2 == 0){
                return env.GetScript().eval(children.get(children.size() - 1), env);
            }
            return new CVoid(line_num, f);
        }
        
    }
    
    @api public static class ifelse implements Function{

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

        public boolean preResolveVariables() {
            return false;
        }

        public String since() {
            return "3.3.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env environment, Construct... args) throws ConfigRuntimeException {
            return new CNull(line_num, f);
        }
        
        public Construct execs(int line_num, File f, Env env, List<GenericTreeNode<Construct>> children){
            if(children.size() < 2){
                throw new ConfigRuntimeException("ifelse expects at least 2 arguments", ExceptionType.InsufficientArgumentsException, line_num, f);
            }
            for(int i = 0; i <= children.size() - 2; i+=2){
                GenericTreeNode<Construct> statement = children.get(i);
                GenericTreeNode<Construct> code = children.get(i + 1);
                Construct evalStatement = env.GetScript().eval(statement, env);
                evalStatement = env.GetScript().preResolveVariable(evalStatement);
                if(Static.getBoolean(evalStatement)){
                    return env.GetScript().eval(code, env);
                }
            }
            if(children.size() % 2 == 1){
                return env.GetScript().eval(children.get(children.size() - 1), env);
            }
            return new CVoid(line_num, f);
        }
        
    }
    
    @api public static class equals implements Function{

        public String getName() {
            return "equals";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            if(Static.anyBooleans(args)){
                boolean arg1 = Static.getBoolean(args[0]);
                boolean arg2 = Static.getBoolean(args[1]);
                return new CBoolean(arg1 == arg2, line_num, f);
            }
            if(args[0].val().equals(args[1].val())){
                return new CBoolean(true, line_num, f);
            }
            try{
                double arg1 = Static.getNumber(args[0]);
                double arg2 = Static.getNumber(args[1]);
                return new CBoolean(arg1 == arg2, line_num, f);
            } catch (ConfigRuntimeException e){
                return new CBoolean(false, line_num, f);
            }
        }
        
        public ExceptionType[] thrown(){
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public String docs() {
            return "boolean {var1, var2} Returns true or false if the two arguments are equal";
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }
        public String since() {
            return "3.0.1";
        }
        public Boolean runAsync() {
            return null;
        }
    }
    
    @api public static class sequals implements Function{

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

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.3.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env environment, Construct... args) throws ConfigRuntimeException {
            equals equals = new equals();
            if(args[1].getClass().equals(args[0].getClass())
                    && ((CBoolean)equals.exec(line_num, f, environment, args)).getBoolean()){
                return new CBoolean(true, line_num, f);
            } else {
                return new CBoolean(false, line_num, f);
            }
        }
        
    }
    
    @api public static class nequals implements Function{

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

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.3.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            equals e = new equals();
            CBoolean b = (CBoolean) e.exec(line_num, f, env, args);
            return new CBoolean(!b.getBoolean(), line_num, f);
        }
        
    }
    
    @api public static class equals_ic implements Function{

        public String getName() {
            return "equals_ic";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            return "boolean {val1, val2} Returns true if the two values are equal to each other, while"
                    + " ignoring case.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{};
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.2.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            if(Static.anyBooleans(args)){
                boolean arg1 = Static.getBoolean(args[0]);
                boolean arg2 = Static.getBoolean(args[1]);
                return new CBoolean(arg1 == arg2, line_num, f);
            }
            if(args[0].val().equalsIgnoreCase(args[1].val())){
                return new CBoolean(true, line_num, f);
            }
            try{
                double arg1 = Static.getNumber(args[0]);
                double arg2 = Static.getNumber(args[1]);
                return new CBoolean(arg1 == arg2, line_num, f);
            } catch (ConfigRuntimeException e){
                return new CBoolean(false, line_num, f);
            }
        }
        
    }
    
    @api public static class nequals_ic implements Function{

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

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.3.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env environment, Construct... args) throws ConfigRuntimeException {
            equals_ic e = new equals_ic();
            return new CBoolean(!((CBoolean)e.exec(line_num, f, environment, args)).getBoolean(), line_num, f);
        }
        
    }
    
    @api public static class lt implements Function{

        public String getName() {
            return "lt";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            double arg1 = Static.getNumber(args[0]);
            double arg2 = Static.getNumber(args[1]);
            return new CBoolean(arg1 < arg2, line_num, f);
        }
        
        public ExceptionType[] thrown(){
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public String docs() {
            return "boolean {var1, var2} Returns the results of a less than operation";
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }
        public String since() {
            return "3.0.1";
        }
        public Boolean runAsync() {
            return null;
        }
    }
    
    @api public static class gt implements Function{

        public String getName() {
            return "gt";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            double arg1 = Static.getNumber(args[0]);
            double arg2 = Static.getNumber(args[1]);
            return new CBoolean(arg1 > arg2, line_num, f);
        }
        
        public ExceptionType[] thrown(){
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public String docs() {
            return "boolean {var1, var2} Returns the result of a greater than operation";
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }
        public String since() {
            return "3.0.1";
        }
        
        public Boolean runAsync() {
            return null;
        }
    }
    
    @api public static class lte implements Function{

        public String getName() {
            return "lte";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            double arg1 = Static.getNumber(args[0]);
            double arg2 = Static.getNumber(args[1]);
            return new CBoolean(arg1 <= arg2, line_num, f);
        }
        
        public ExceptionType[] thrown(){
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public String docs() {
            return "boolean {var1, var2} Returns the result of a less than or equal to operation";
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }
        public String since() {
            return "3.0.1";
        }
        public Boolean runAsync() {
            return null;
        }
    }
    
    @api public static class gte implements Function{

        public String getName() {
            return "gte";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            double arg1 = Static.getNumber(args[0]);
            double arg2 = Static.getNumber(args[1]);
            return new CBoolean(arg1 >= arg2, line_num, f);
        }
        
        public ExceptionType[] thrown(){
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public String docs() {
            return "boolean {var1, var2} Returns the result of a greater than or equal to operation";
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }
        public String since() {
            return "3.0.1";
        }
        public Boolean runAsync() {
            return null;
        }
    }
    
    @api public static class and implements Function{

        public String getName() {
            return "and";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }
        public Construct exec(int line_num, File f, Env env, Construct ... args){
            return new CNull(line_num, f);
        }
        public Construct execs(int line_num, File f, Env env, List<GenericTreeNode<Construct>> args) throws CancelCommandException, ConfigRuntimeException {
            for(GenericTreeNode<Construct> tree : args){
                Construct c = env.GetScript().eval(tree, env);
                boolean b = Static.getBoolean(c);
                if(b == false){
                    return new CBoolean(false, line_num, f);
                }
            }
            return new CBoolean(true, line_num, f);
        }
        
        public ExceptionType[] thrown(){
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public String docs() {
            return "boolean {var1, [var2...]} Returns the boolean value of a logical AND across all arguments. Uses lazy determination, so once "
                    + "an argument returns false, the function returns.";
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }
        public String since() {
            return "3.0.1";
        }
        public Boolean runAsync() {
            return null;
        }
    }
    
    @api public static class or implements Function{

        public String getName() {
            return "or";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public Construct exec(int line_num, File f, Env env, Construct... args){
            return new CNull(line_num, f);
        }
        
        public Construct execs(int line_num, File f, Env env, List<GenericTreeNode<Construct>> args) throws CancelCommandException, ConfigRuntimeException {            
            for(GenericTreeNode<Construct> tree: args){
                Construct c = env.GetScript().eval(tree, env);
                if(Static.getBoolean(c)){
                    return new CBoolean(true, line_num, f);
                }
            }
            return new CBoolean(false, line_num, f);
        }
        
        public ExceptionType[] thrown(){
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public String docs() {
            return "boolean {var1, [var2...]} Returns the boolean value of a logical OR across all arguments. Uses lazy determination, so once an "
                    + "argument resolves to true, the function returns.";
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }
        public String since() {
            return "3.0.1";
        }
        public Boolean runAsync() {
            return null;
        }
    }
    
    @api public static class not implements Function{

        public String getName() {
            return "not";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            return new CBoolean(!Static.getBoolean(args[0]), line_num, f);
        }
        
        public ExceptionType[] thrown(){
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public String docs() {
            return "boolean {var1} Returns the boolean value of a logical NOT for this argument";
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }
        public String since() {
            return "3.0.1";
        }
        public Boolean runAsync() {
            return null;
        }
    }
    
    @api public static class xor implements Function{

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

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.3.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env environment, Construct... args) throws ConfigRuntimeException {
            boolean val1 = Static.getBoolean(args[0]);
            boolean val2 = Static.getBoolean(args[1]);
            return new CBoolean(val1 ^ val2, line_num, f);
        }
        
    }
    
    @api public static class nand implements Function{

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

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.3.0";
        }

        public Boolean runAsync() {
            return null;
        }
        public Construct exec(int line_num, File f, Env environment, Construct... args){
            return new CNull(line_num, f);
        }
        public Construct execs(int line_num, File f, Env environment, List<GenericTreeNode<Construct>> args) throws ConfigRuntimeException {
            and and = new and();
            boolean val = ((CBoolean)and.execs(line_num, f, environment, args)).getBoolean();
            return new CBoolean(!val, line_num, f);
        }
        
    }
    
    @api public static class nor implements Function{

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

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.3.0";
        }

        public Boolean runAsync() {
            return null;
        }
        public Construct exec(int line_num, File f, Env environment, Construct ... args){
            return new CNull(line_num, f);
        }
        
        public Construct execs(int line_num, File f, Env environment, List<GenericTreeNode<Construct>> args) throws ConfigRuntimeException {
            or or = new or();
            boolean val = ((CBoolean)or.execs(line_num, f, environment, args)).getBoolean();
            return new CBoolean(!val, line_num, f);
        }
        
    }
    
    @api public static class xnor implements Function{

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

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.3.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env environment, Construct... args) throws ConfigRuntimeException {
            xor xor = new xor();
            boolean val = ((CBoolean)xor.exec(line_num, f, environment, args)).getBoolean();
            return new CBoolean(!val, line_num, f);
        }
        
    }
    
    @api public static class bit_and implements Function{

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

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.3.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env environment, Construct... args) throws ConfigRuntimeException {
            if(args.length < 1){
                throw new ConfigRuntimeException("bit_and requires at least one argument", ExceptionType.InsufficientArgumentsException, line_num, f);
            }
            long val = Static.getInt(args[0]);
            for(int i = 1; i < args.length; i++){
                val = val & Static.getInt(args[i]);
            }
            return new CInt(val, line_num, f);
        }
        
    }
    
    @api public static class bit_or implements Function{

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

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.3.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env environment, Construct... args) throws ConfigRuntimeException {
            if(args.length < 1){
                throw new ConfigRuntimeException("bit_or requires at least one argument", ExceptionType.InsufficientArgumentsException, line_num, f);
            }
            long val = Static.getInt(args[0]);
            for(int i = 1; i < args.length; i++){
                val = val | Static.getInt(args[i]);
            }
            return new CInt(val, line_num, f);
        }
        
    }
    
    @api public static class bit_not implements Function{

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

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.3.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env environment, Construct... args) throws ConfigRuntimeException {
            return new CInt(~Static.getInt(args[0]), line_num, f);
        }
        
    }
    
    @api public static class lshift implements Function{

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

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.3.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env environment, Construct... args) throws ConfigRuntimeException {
            long value = Static.getInt(args[0]);
            long toShift = Static.getInt(args[1]);
            return new CInt(value << toShift, line_num, f);            
        }
                
    }
    
    @api public static class rshift implements Function{

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

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.3.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env environment, Construct... args) throws ConfigRuntimeException {
            long value = Static.getInt(args[0]);
            long toShift = Static.getInt(args[1]);
            return new CInt(value >> toShift, line_num, f);
        }
        
    }
    
    @api public static class urshift implements Function{

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

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.3.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env environment, Construct... args) throws ConfigRuntimeException {
            long value = Static.getInt(args[0]);
            long toShift = Static.getInt(args[1]);
            return new CInt(value >>> toShift, line_num, f);
        }
        
    }
    
    
}
