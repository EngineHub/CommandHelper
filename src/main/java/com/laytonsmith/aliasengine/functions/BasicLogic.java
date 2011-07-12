/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.GenericTreeNode;
import com.laytonsmith.aliasengine.CancelCommandException;
import com.laytonsmith.aliasengine.ConfigRuntimeException;
import com.laytonsmith.aliasengine.Constructs.*;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.Script;
import com.laytonsmith.aliasengine.Static;
import com.laytonsmith.aliasengine.functions.Exceptions.ExceptionType;
import java.util.List;
import org.bukkit.entity.Player;

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
        
        public Construct execs(int line_num, Player p, Script parent, 
                GenericTreeNode<Construct> condition, GenericTreeNode<Construct> __if, 
                GenericTreeNode<Construct> __else, List<Variable> vars) throws CancelCommandException{
            if(Static.getBoolean(Static.resolveDollarVar(parent.eval(condition, p, vars), vars))){
                return parent.eval(__if, p, vars);
            } else {
                if(__else == null){
                    return new CVoid(line_num);
                }
                return parent.eval(__else, p, vars);
            }
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            throw new UnsupportedOperationException("That operation is not yet supported");
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
    
    @api public static class _equals implements Function{

        public String getName() {
            return "equals";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            if(Static.anyBooleans(args)){
                boolean arg1 = Static.getBoolean(args[0]);
                boolean arg2 = Static.getBoolean(args[1]);
                return new CBoolean(arg1 == arg2, line_num);
            }
            if(args[0].val().equals(args[1].val())){
                return new CBoolean(true, line_num);
            }
            try{
                double arg1 = Static.getNumber(args[0]);
                double arg2 = Static.getNumber(args[1]);
                return new CBoolean(arg1 == arg2, line_num);
            } catch (ConfigRuntimeException e){
                return new CBoolean(false, line_num);
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
    
    @api public static class lt implements Function{

        public String getName() {
            return "lt";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            double arg1 = Static.getNumber(args[0]);
            double arg2 = Static.getNumber(args[1]);
            return new CBoolean(arg1 < arg2, line_num);
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

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            double arg1 = Static.getNumber(args[0]);
            double arg2 = Static.getNumber(args[1]);
            return new CBoolean(arg1 > arg2, line_num);
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

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            double arg1 = Static.getNumber(args[0]);
            double arg2 = Static.getNumber(args[1]);
            return new CBoolean(arg1 <= arg2, line_num);
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

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            double arg1 = Static.getNumber(args[0]);
            double arg2 = Static.getNumber(args[1]);
            return new CBoolean(arg1 >= arg2, line_num);
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

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            for(Construct c : args){
                boolean b = Static.getBoolean(c);
                if(b == false){
                    return new CBoolean(false, line_num);
                }
            }
            return new CBoolean(true, line_num);
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

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            for(Construct c : args){
                if(Static.getBoolean(c)){
                    return new CBoolean(true, line_num);
                }
            }
            return new CBoolean(false, line_num);
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

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            return new CBoolean(!Static.getBoolean(args[0]), line_num);
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
