/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.api;
import com.laytonsmith.aliasengine.GenericTreeNode;
import com.laytonsmith.aliasengine.exceptions.CancelCommandException;
import com.laytonsmith.aliasengine.exceptions.ConfigRuntimeException;
import com.laytonsmith.aliasengine.Constructs.*;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.Env;
import com.laytonsmith.aliasengine.Script;
import com.laytonsmith.aliasengine.Static;
import com.laytonsmith.aliasengine.functions.Exceptions.ExceptionType;
import java.io.File;
import java.util.Map;
import org.bukkit.command.CommandSender;

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

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            for(Construct c : args){
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

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            for(Construct c : args){
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
    
    
}
