/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.functions;

import com.laytonsmith.core.api;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.Env;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.sk89q.worldedit.expression.Expression;
import com.sk89q.worldedit.expression.ExpressionException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Layton
 */
public class Math {
    public static String docs(){
        return "Provides mathematical functions to scripts";
    }
    @api public static class add implements Function{

        public String getName() {
            return "add";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            double tally = Static.getNumber(args[0]);
            for(int i = 1; i < args.length; i++){
                tally += Static.getNumber(args[i]);
            }
            if(Static.anyDoubles(args)){
                return new CDouble(tally, line_num, f);
            } else {
                return new CInt((long)tally, line_num, f);
            }
        }
        
        public ExceptionType[] thrown(){
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public String docs() {
            return "mixed {var1, [var2...]} Adds all the arguments together, and returns either a double or an integer";
        }

        public boolean isRestricted() {
            return false;
        }

        public boolean preResolveVariables() {
            return true;
        }
        public String since() {
            return "3.0.1";
        }
        public Boolean runAsync(){
            return null;
        }
    }
    
    @api public static class subtract implements Function{

        public String getName() {
            return "subtract";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            double tally = Static.getNumber(args[0]);
            for(int i = 1; i < args.length; i++){
                tally -= Static.getNumber(args[i]);
            }
            if(Static.anyDoubles(args)){
                return new CDouble(tally, line_num, f);
            } else {
                return new CInt((long)tally, line_num, f);
            }
        }
        
        public ExceptionType[] thrown(){
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public String docs() {
            return "mixed {var1, [var2...]} Subtracts the arguments from left to right, and returns either a double or an integer";
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
        public Boolean runAsync(){
            return null;
        }
    }
    
    @api public static class multiply implements Function{

        public String getName() {
            return "multiply";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            double tally = Static.getNumber(args[0]);
            for(int i = 1; i < args.length; i++){
                tally *= Static.getNumber(args[i]);
            }
            if(Static.anyDoubles(args)){
                return new CDouble(tally, line_num, f);
            } else {
                return new CInt((long)tally, line_num, f);
            }
        }
        
        public ExceptionType[] thrown(){
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public String docs() {
            return "mixed {var1, [var2...]} Multiplies the arguments together, and returns either a double or an integer";
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
        public Boolean runAsync(){
            return null;
        }
    }
    
    @api public static class divide implements Function{

        public String getName() {
            return "divide";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            double tally = Static.getNumber(args[0]);
            for(int i = 1; i < args.length; i++){
                tally /= Static.getNumber(args[i]);
            }
            if(tally == (int)tally){
                return new CInt((long)tally, line_num, f);
            } else {
                return new CDouble(tally, line_num, f);
            }
        }
        
        public ExceptionType[] thrown(){
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public String docs() {
            return "mixed {var1, [var2...]} Divides the arguments from left to right, and returns either a double or an integer";
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
        public Boolean runAsync(){
            return null;
        }
    }
    
    @api public static class mod implements Function{

        public String getName() {
            return "mod";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            long arg1 = Static.getInt(args[0]);
            long arg2 = Static.getInt(args[1]);
            return new CInt(arg1 % arg2, line_num, f);
        }
        
        public ExceptionType[] thrown(){
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public String docs() {
            return "int {x, n} Returns x modulo n";
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
        public Boolean runAsync(){
            return null;
        }
    }
    
    @api public static class pow implements Function{

        public String getName() {
            return "pow";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            double arg1 = Static.getNumber(args[0]);
            double arg2 = Static.getNumber(args[1]);
            return new CDouble(java.lang.Math.pow(arg1, arg2), line_num, f);
        }

        public String docs() {
            return "double {x, n} Returns x to the power of n";
        }
        
        public ExceptionType[] thrown(){
            return new ExceptionType[]{ExceptionType.CastException};
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
        public Boolean runAsync(){
            return null;
        }
    }
    
    @api public static class inc implements Function{
        
        public String getName() {
            return "inc";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            if(args[0] instanceof IVariable){
                IVariable v = env.GetVarList().get(((IVariable)args[0]).getName());
                Construct newVal;
                long value = 1;
                if(args.length == 2){
                    if(args[1] instanceof IVariable){
                        args[1] = env.GetVarList().get(((IVariable)args[1]).getName());
                    }
                    value = Static.getInt(args[1]);
                }
                if(Static.anyDoubles(v.ival())){
                    newVal = new CDouble(Static.getDouble(v.ival()) + value, line_num, f);
                } else {
                    newVal = new CInt(Static.getInt(v.ival()) + value, line_num, f);
                }
                v = new IVariable(v.getName(), newVal, line_num, f);
                env.GetVarList().set(v);
                return v;
            }
            throw new ConfigRuntimeException("inc expects argument 1 to be an ivar", 
                    ExceptionType.CastException, line_num, f);
        }

        public String docs() {
            return "ivar {var} Adds 1 to var, and stores the new value. Equivalent to ++var in other languages. Expects ivar to be a variable, then"
                    + " returns the ivar.";
        }
        
        public ExceptionType[] thrown(){
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return false;
        }

        public boolean preResolveVariables() {
            return false;
        }
        public String since() {
            return "3.0.1";
        }
        public Boolean runAsync(){
            return null;
        }
    }
    
    @api public static class dec implements Function{
        
        public String getName() {
            return "dec";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            if(args[0] instanceof IVariable){
                IVariable v = env.GetVarList().get(((IVariable)args[0]).getName());
                long value = 1;
                if(args.length == 2){
                    if(args[1] instanceof IVariable){
                        args[1] = env.GetVarList().get(((IVariable)args[1]).getName());
                    }
                    value = Static.getInt(args[1]);
                }
                Construct newVal;
                if(Static.anyDoubles(v.ival())){
                    newVal = new CDouble(Static.getDouble(v.ival()) - value, line_num, f);
                } else {
                    newVal = new CInt(Static.getInt(v.ival()) - value, line_num, f);
                }
                v = new IVariable(v.getName(), newVal, line_num, f);
                env.GetVarList().set(v);
                return v;
            }
            throw new ConfigRuntimeException("dec expects argument 1 to be an ivar", 
                    ExceptionType.CastException, line_num, f);
        }

        public String docs() {
            return "ivar {var, [value]} Subtracts value from var, and stores the new value. Value defaults to 1. Equivalent to --var (or var -= value) in other languages. Expects ivar to be a variable, then"
                    + " returns the ivar.";
        }
        
        public ExceptionType[] thrown(){
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return false;
        }

        public boolean preResolveVariables() {
            return false;
        }
        public String since() {
            return "3.0.1";
        }
        public Boolean runAsync(){
            return null;
        }
    }
    
    @api public static class rand implements Function{
        
        Random r = new Random();

        public String getName() {
            return "rand";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public String docs() {
            return "int {min/max, [max]} Returns a random number from 0 to max, or min to max, depending on usage. Max is exclusive. Min must"
                    + " be less than max, and both numbers must be >= 0";
        }
        
        public ExceptionType[] thrown(){
            return new ExceptionType[]{ExceptionType.RangeException, ExceptionType.CastException};
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

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            long min = 0;
            long max = 0;
            if(args.length == 1){
                max = Static.getInt(args[0]);
            } else {
                min = Static.getInt(args[0]);
                max = Static.getInt(args[1]);
            }
            if(max > Integer.MAX_VALUE || min > Integer.MAX_VALUE){
                throw new ConfigRuntimeException("max and min must be below int max, defined as " + Integer.MAX_VALUE, 
                        ExceptionType.RangeException,
                        line_num, f);
            }
           
            long range = max - min;
            if(range <= 0){
                throw new ConfigRuntimeException("max - min must be greater than 0", 
                        ExceptionType.RangeException, line_num, f);
            }
            long rand = java.lang.Math.abs(r.nextLong());
            long i = (rand % (range)) + min;

            return new CInt(i, line_num, f);
        }
        public Boolean runAsync(){
            return null;
        }
    }
    
    @api
    public static class abs implements Function{

        public String getName() {
            return "abs";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "double {arg} Returns the absolute value of the argument.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.1.2";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            double d = Static.getDouble(args[0]);
            return new CDouble(java.lang.Math.abs(d), line_num, f);
        }
        
    }
    
    @api public static class floor implements Function{

        public String getName() {
            return "floor";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "int {number} Returns the floor of any given number. For example, floor(3.8) returns 3, and floor(-1.1) returns 2";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.1.3";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            return new CInt((long)java.lang.Math.floor(Static.getNumber(args[0])), line_num, f);
        }
        
    }
    
    @api public static class ceil implements Function{

        public String getName() {
            return "ceil";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "int {number} Returns the ceiling of any given number. For example, ceil(3.2) returns 4, and ceil(-1.1) returns -1";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.1.3";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            return new CInt((long)java.lang.Math.ceil(Static.getNumber(args[0])), line_num, f);
        }
        
    }
    
    @api public static class sqrt implements Function{

        public String getName() {
            return "sqrt";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "number {number} Returns the square root of a number. Note that this is mathematically equivalent to pow(number, .5)."
                    + " Imaginary numbers are not supported at this time, so number must be positive.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.RangeException, ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {
            
        }

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
            double d = Static.getNumber(args[0]);
            if(d < 0){
               throw new ConfigRuntimeException("sqrt expects a number >= 0", ExceptionType.RangeException, line_num, f); 
            }
            double m = java.lang.Math.sqrt(d);
            if(m == (int)m){
                return new CInt((long) m, line_num, f);
            } else {
                return new CDouble(m, line_num, f);
            }
        }
        
    }
    
    @api public static class min implements Function{

        public String getName() {
            return "min";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public String docs() {
            return "number {num1, [num2...]} Returns the lowest number in a given list of numbers. If any of the arguments"
                    + " are arrays, they are expanded into individual numbers, and also compared.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.InsufficientArgumentsException};
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
            if(args.length == 0){
                throw new ConfigRuntimeException("You must send at least one parameter to min", 
                        ExceptionType.InsufficientArgumentsException, line_num, f);
            }
            double lowest = Double.POSITIVE_INFINITY;
            List<Construct> list = new ArrayList<Construct>();
            recList(list, args);
            for(Construct c : list){
                double d = Static.getNumber(c);
                if(d < lowest){
                    lowest = d;
                }
            }
            if(lowest == (long)lowest){
                return new CInt((long)lowest, line_num, f);
            } else {
                return new CDouble(lowest, line_num, f);
            }
        }
        
        public List<Construct> recList(List<Construct> list, Construct ... args){
            for(Construct c : args){
                if(c instanceof CArray){
                    for(int i = 0; i < ((CArray)c).size(); i++){
                        recList(list, ((CArray)c).get(i, 0));
                    }
                } else {
                    list.add(c);
                }
            }
            return list;
        }
        
    }
    
    @api public static class max implements Function{

        public String getName() {
            return "max";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public String docs() {
            return "number {num1, [num2...]} Returns the highest number in a given list of numbers. If any of the arguments"
                    + " are arrays, they are expanded into individual numbers, and also compared.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.InsufficientArgumentsException};
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
            if(args.length == 0){
                throw new ConfigRuntimeException("You must send at least one parameter to max", 
                        ExceptionType.InsufficientArgumentsException, line_num, f);
            }
            double highest = Double.NEGATIVE_INFINITY;
            List<Construct> list = new ArrayList<Construct>();
            recList(list, args);
            for(Construct c : list){
                double d = Static.getNumber(c);
                if(d > highest){
                    highest = d;
                }
            }
            if(highest == (long)highest){
                return new CInt((long)highest, line_num, f);
            } else {
                return new CDouble(highest, line_num, f);
            }
        }
        
        public List<Construct> recList(List<Construct> list, Construct ... args){
            for(Construct c : args){
                if(c instanceof CArray){
                    for(int i = 0; i < ((CArray)c).size(); i++){
                        recList(list, ((CArray)c).get(i, 0));
                    }
                } else {
                    list.add(c);
                }
            }
            return list;
        }
        
    }
    
    @api public static class sin implements Function{

        public String getName() {
            return "sin";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "double {number} Returns the sin of the number";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

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
            return new CDouble(java.lang.Math.sin(Static.getNumber(args[0])), line_num, f);
        }
        
    }
    
    @api public static class cos implements Function{

        public String getName() {
            return "cos";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "double {number} Returns the cos of the number";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

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
            return new CDouble(java.lang.Math.cos(Static.getNumber(args[0])), line_num, f);
        }
        
    }
    
    @api public static class tan implements Function{

        public String getName() {
            return "tan";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "double {number} Returns the tan of the number";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

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
            return new CDouble(java.lang.Math.tan(Static.getNumber(args[0])), line_num, f);
        }
        
    }
    
    @api public static class asin implements Function{

        public String getName() {
            return "asin";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "double {number} Returns the arc sin of the number";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

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
            return new CDouble(java.lang.Math.asin(Static.getNumber(args[0])), line_num, f);
        }
        
    }
    
    @api public static class acos implements Function{

        public String getName() {
            return "acos";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "double {number} Returns the arc cos of the number";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

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
            return new CDouble(java.lang.Math.acos(Static.getNumber(args[0])), line_num, f);
        }
        
    }
    
    @api public static class atan implements Function{

        public String getName() {
            return "atan";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "double {number} Returns the arc tan of the number";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

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
            return new CDouble(java.lang.Math.atan(Static.getNumber(args[0])), line_num, f);
        }
        
    }
    
    @api public static class to_radians implements Function{

        public String getName() {
            return "to_radians";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "double {number} Converts the number to radians (which is assumed to have been in degrees)";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

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
            return new CDouble(java.lang.Math.toRadians(Static.getNumber(args[0])), line_num, f);
        }
        
    }
    
    @api public static class to_degrees implements Function{

        public String getName() {
            return "to_degrees";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "double {number} Converts the number to degrees (which is assumed to have been in radians)";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

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
            return new CDouble(java.lang.Math.toDegrees(Static.getNumber(args[0])), line_num, f);
        }
        
    }
    
    @api public static class atan2 implements Function{

        public String getName() {
            return "atan2";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            //lolcopypaste
            return "double {number} Returns the angle theta from the conversion"
                    + " of rectangular coordinates (x, y) to polar coordinates"
                    + " (r, theta). This method computes the phase theta by"
                    + " computing an arc tangent of y/x in the range of -pi to pi.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

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
            return new CDouble(java.lang.Math.atan2(Static.getNumber(args[0]), Static.getNumber(args[1])), line_num, f);
        }
        
    }
    
    @api public static class round implements Function{

        public String getName() {
            return "round";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "int {number} Unlike floor and ceil, rounds the number to the nearest integer.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

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
            return new CDouble(java.lang.Math.round(Static.getNumber(args[0])), line_num, f);
        }
        
    }
    
    @api public static class expr implements Function{

        public String getName() {
            return "expr";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public String docs() {
            return "double {expression, [valueArray]} Sometimes, when you need to calculate an advanced"
                    + " mathematical expression, it is messy to write out everything in terms of functions."
                    + " This function will allow you to evaluate a mathematical expression as a string, using"
                    + " common mathematical notation. For example, (2 + 3) * 4 would return 20. Variables can"
                    + " also be included, and their values given as an associative array. expr('(x + y) * z',"
                    + " array(x: 2, y: 3, z: 4)) would be the same thing as the above example.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.PluginInternalException};
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
            String expr = args[0].val();
            CArray vars = null;
            if(args.length == 2 && args[1] instanceof CArray){
                vars = (CArray)args[1];
            } else if(args.length == 2 && !(args[1] instanceof CArray)){
                throw new ConfigRuntimeException("The second argument of expr() should be an array", ExceptionType.CastException, line_num, f);
            }
            if(vars != null && !vars.inAssociativeMode()){
                throw new ConfigRuntimeException("The array provided to expr() must be an associative array", ExceptionType.CastException, line_num, f);
            }
            double[] da;
            String[] varNames;
            if(vars != null){
                int i = 0;
                da = new double[vars.size()];
                varNames = new String[vars.size()];
                for(Construct key : vars.keySet()){
                    varNames[i] = key.val();
                    da[i] = Static.getDouble(vars.get(key, line_num));
                    i++;
                }
            } else {
                da = new double[0];
                varNames = new String[0];
            }
            try {
                Expression e = Expression.compile(expr, varNames);
                return new CDouble(e.evaluate(da), line_num, f);
            } catch (ExpressionException ex) {
                throw new ConfigRuntimeException("Your expression was invalidly formatted", ExceptionType.PluginInternalException, line_num, f, ex);
            }
        }
        
    }
    
}
