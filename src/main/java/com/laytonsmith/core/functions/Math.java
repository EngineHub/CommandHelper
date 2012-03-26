/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.functions;

import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Env;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.api;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.sk89q.worldedit.expression.Expression;
import com.sk89q.worldedit.expression.ExpressionException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Layton
 */
public class Math {
    public static String docs(){
        return "Provides mathematical functions to scripts";
    }
    @api public static class add extends AbstractFunction{

        public String getName() {
            return "add";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            double tally = Static.getNumber(args[0]);
            for(int i = 1; i < args.length; i++){
                tally += Static.getNumber(args[i]);
            }
            if(Static.anyDoubles(args)){
                return new CDouble(tally, t);
            } else {
                return new CInt((long)tally, t);
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
        public CHVersion since() {
            return CHVersion.V3_0_1;
        }
        public Boolean runAsync(){
            return null;
        }

        @Override
        public boolean canOptimize() {
            return true;
        }

        @Override
        public Construct optimize(Target t, Construct... args) {
            return exec(t, null, args);
        }                
    }
    
    @api public static class subtract extends AbstractFunction{

        public String getName() {
            return "subtract";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            double tally = Static.getNumber(args[0]);
            for(int i = 1; i < args.length; i++){
                tally -= Static.getNumber(args[i]);
            }
            if(Static.anyDoubles(args)){
                return new CDouble(tally, t);
            } else {
                return new CInt((long)tally, t);
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
        public CHVersion since() {
            return CHVersion.V3_0_1;
        }
        public Boolean runAsync(){
            return null;
        }
        
        @Override
        public boolean canOptimize() {
            return true;
        }

        @Override
        public Construct optimize(Target t, Construct... args) {
            return exec(t, null, args);
        }
    }
    
    @api public static class multiply extends AbstractFunction{

        public String getName() {
            return "multiply";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            double tally = Static.getNumber(args[0]);
            for(int i = 1; i < args.length; i++){
                tally *= Static.getNumber(args[i]);
            }
            if(Static.anyDoubles(args)){
                return new CDouble(tally, t);
            } else {
                return new CInt((long)tally, t);
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
        public CHVersion since() {
            return CHVersion.V3_0_1;
        }
        public Boolean runAsync(){
            return null;
        }
        
        @Override
        public boolean canOptimize() {
            return true;
        }

        @Override
        public Construct optimize(Target t, Construct... args) {
            return exec(t, null, args);
        }
    }
    
    @api public static class divide extends AbstractFunction{

        public String getName() {
            return "divide";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            double tally = Static.getNumber(args[0]);
            for(int i = 1; i < args.length; i++){
                double next = Static.getNumber(args[i]);
                if(next == 0){
                    throw new ConfigRuntimeException("Division by 0!", ExceptionType.RangeException, t);
                }
                tally /= next;
            }
            if(tally == (int)tally){
                return new CInt((long)tally, t);
            } else {
                return new CDouble(tally, t);
            }
        }
        
        public ExceptionType[] thrown(){
            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.RangeException};
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
        public CHVersion since() {
            return CHVersion.V3_0_1;
        }
        public Boolean runAsync(){
            return null;
        }
        
        public boolean canOptimize(){
            return true;
        }
        
        public Construct optimize(Target t, Construct ... args){
            return exec(t, null, args);
        }
    }
    
    @api public static class mod extends AbstractFunction{

        public String getName() {
            return "mod";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            long arg1 = Static.getInt(args[0]);
            long arg2 = Static.getInt(args[1]);
            return new CInt(arg1 % arg2, t);
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
        public CHVersion since() {
            return CHVersion.V3_0_1;
        }
        public Boolean runAsync(){
            return null;
        }
        
        @Override
        public boolean canOptimize() {
            return true;
        }

        @Override
        public Construct optimize(Target t, Construct... args) {
            return exec(t, null, args);
        }
    }
    
    @api public static class pow extends AbstractFunction{

        public String getName() {
            return "pow";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            double arg1 = Static.getNumber(args[0]);
            double arg2 = Static.getNumber(args[1]);
            return new CDouble(java.lang.Math.pow(arg1, arg2), t);
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
        public CHVersion since() {
            return CHVersion.V3_0_1;
        }
        public Boolean runAsync(){
            return null;
        }
        
        @Override
        public boolean canOptimize() {
            return true;
        }

        @Override
        public Construct optimize(Target t, Construct... args) {
            return exec(t, null, args);
        }
    }
    
    @api public static class inc extends AbstractFunction{
        
        public String getName() {
            return "inc";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            long value = 1;
            if(args.length == 2){
                if(args[1] instanceof IVariable){
                    IVariable cur2 = (IVariable)args[1];
                    args[1] = env.GetVarList().get(cur2.getName(), cur2.getTarget());
                }
                value = Static.getInt(args[1]);
            }
            if(args[0] instanceof IVariable){
                IVariable cur = (IVariable)args[0];
                IVariable v = env.GetVarList().get(cur.getName(), cur.getTarget());
                Construct newVal;
                if(Static.anyDoubles(v.ival())){
                    newVal = new CDouble(Static.getDouble(v.ival()) + value, t);
                } else {
                    newVal = new CInt(Static.getInt(v.ival()) + value, t);
                }
                v = new IVariable(v.getName(), newVal, t);
                env.GetVarList().set(v);
                return v;
            } else {
                if(Static.anyDoubles(args[0])){
                    return new CDouble(Static.getNumber(args[0]) + value, t);
                } else {
                    return new CInt(Static.getInt(args[0]) + value, t);
                }
            }
            
        }

        public String docs() {
            return "ivar {var, [x]} Adds x to var, and stores the new value. Equivalent to ++var in other languages. Expects ivar to be a variable, then"
                    + " returns the ivar, or, if var is a constant number, simply adds x to it, and returns the new number.";
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
        public CHVersion since() {
            return CHVersion.V3_0_1;
        }
        public Boolean runAsync(){
            return null;
        }
        
        @Override
        public boolean canOptimize() {
            return true;
        }

        @Override
        public Construct optimize(Target t, Construct... args) {
            if(args[0] instanceof IVariable){
                return null; //Can't optimize this
            }
            return exec(t, null, args);
        }
    }
    
    @api public static class postinc extends AbstractFunction{

        public String getName() {
            return "postinc";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            long value = 1;
            if(args.length == 2){
                if(args[1] instanceof IVariable){
                    IVariable cur2 = (IVariable)args[1];
                    args[1] = env.GetVarList().get(cur2.getName(), cur2.getTarget());
                }
                value = Static.getInt(args[1]);
            }
            if(args[0] instanceof IVariable){
                IVariable cur = (IVariable)args[0];
                IVariable v = env.GetVarList().get(cur.getName(), cur.getTarget());
                Construct newVal;
                if(Static.anyDoubles(v.ival())){
                    newVal = new CDouble(Static.getDouble(v.ival()) + value, t);
                } else {
                    newVal = new CInt(Static.getInt(v.ival()) + value, t);
                }
                Construct oldVal = null;
                try {
                    oldVal = v.ival().clone();
                } catch (CloneNotSupportedException ex) {
                    Logger.getLogger(Math.class.getName()).log(Level.SEVERE, null, ex);
                }
                v = new IVariable(v.getName(), newVal, t);
                env.GetVarList().set(v);
                return oldVal;
            } else {
                if(Static.anyDoubles(args[0])){
                    return new CDouble(Static.getNumber(args[0]) + value, t);
                } else {
                    return new CInt(Static.getInt(args[0]) + value, t);
                }
            }
        }

        public String docs() {
            return "ivar {var, [x]} Adds x to var, and stores the new value. Equivalent to var++ in other languages. Expects ivar to be a variable, then"
                    + " returns a copy of the old ivar, or, if var is a constant number, simply adds x to it, and returns the new number.";
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
        public CHVersion since() {
            return CHVersion.V3_3_1;
        }
        public Boolean runAsync(){
            return null;
        }
        
        @Override
        public boolean canOptimize() {
            return true;
        }

        @Override
        public Construct optimize(Target t, Construct... args) {
            if(args[0] instanceof IVariable){
                return null; //Can't optimize this
            }
            return exec(t, null, args);
        }
        
    }
    
    @api public static class dec extends AbstractFunction{
        
        public String getName() {
            return "dec";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            long value = 1;
            if(args.length == 2){
                if(args[1] instanceof IVariable){
                    IVariable cur2 = (IVariable)args[1];
                    args[1] = env.GetVarList().get(cur2.getName(), cur2.getTarget());
                }
                value = Static.getInt(args[1]);
            }
            if(args[0] instanceof IVariable){
                IVariable cur = (IVariable)args[0];
                IVariable v = env.GetVarList().get(cur.getName(), cur.getTarget());
                Construct newVal;
                if(Static.anyDoubles(v.ival())){
                    newVal = new CDouble(Static.getDouble(v.ival()) - value, t);
                } else {
                    newVal = new CInt(Static.getInt(v.ival()) - value, t);
                }
                v = new IVariable(v.getName(), newVal, t);
                env.GetVarList().set(v);
                return v;
            } else {
                if(Static.anyDoubles(args[0])){
                    return new CDouble(Static.getNumber(args[0]) + value, t);
                } else {
                    return new CInt(Static.getInt(args[0]) + value, t);
                }
            }
        }

        public String docs() {
            return "ivar {var, [value]} Subtracts value from var, and stores the new value. Value defaults to 1. Equivalent to --var (or var -= value) in other languages. Expects ivar to be a variable, then"
                    + " returns the ivar, , or, if var is a constant number, simply adds x to it, and returns the new number.";
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
        public CHVersion since() {
            return CHVersion.V3_0_1;
        }
        public Boolean runAsync(){
            return null;
        }
        
        @Override
        public boolean canOptimize() {
            return true;
        }

        @Override
        public Construct optimize(Target t, Construct... args) {
            if(args[0] instanceof IVariable){
                return null; //Can't optimize this
            }
            return exec(t, null, args);
        }
    }
    
    @api public static class postdec extends AbstractFunction{

        public String getName() {
            return "postdec";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            long value = 1;
            if(args.length == 2){
                if(args[1] instanceof IVariable){
                    IVariable cur2 = (IVariable)args[1];
                    args[1] = env.GetVarList().get(cur2.getName(), cur2.getTarget());
                }
                value = Static.getInt(args[1]);
            }
            if(args[0] instanceof IVariable){
                IVariable cur = (IVariable)args[0];
                IVariable v = env.GetVarList().get(cur.getName(), cur.getTarget());
                Construct newVal;
                if(Static.anyDoubles(v.ival())){
                    newVal = new CDouble(Static.getDouble(v.ival()) - value, t);
                } else {
                    newVal = new CInt(Static.getInt(v.ival()) - value, t);
                }
                Construct oldVal = null;
                try {
                    oldVal = v.ival().clone();
                } catch (CloneNotSupportedException ex) {
                    Logger.getLogger(Math.class.getName()).log(Level.SEVERE, null, ex);
                }
                v = new IVariable(v.getName(), newVal, t);
                env.GetVarList().set(v);
                return oldVal;
            } else {
                if(Static.anyDoubles(args[0])){
                    return new CDouble(Static.getNumber(args[0]) + value, t);
                } else {
                    return new CInt(Static.getInt(args[0]) + value, t);
                }
            }
        }

        public String docs() {
            return "ivar {var, [x]} Subtracts x from var, and stores the new value. Equivalent to var-- in other languages. Expects ivar to be a variable, then"
                    + " returns a copy of the old ivar, , or, if var is a constant number, simply adds x to it, and returns the new number.";
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
        public CHVersion since() {
            return CHVersion.V3_3_1;
        }
        public Boolean runAsync(){
            return null;
        }
        
        @Override
        public boolean canOptimize() {
            return true;
        }

        @Override
        public Construct optimize(Target t, Construct... args) {
            if(args[0] instanceof IVariable){
                return null; //Can't optimize this
            }
            return exec(t, null, args);
        }
        
    }
    
    @api public static class rand extends AbstractFunction{
        
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

        public CHVersion since() {
            return CHVersion.V3_0_1;
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
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
                        t);
            }
           
            long range = max - min;
            if(range <= 0){
                throw new ConfigRuntimeException("max - min must be greater than 0", 
                        ExceptionType.RangeException, t);
            }
            long rand = java.lang.Math.abs(r.nextLong());
            long i = (rand % (range)) + min;

            return new CInt(i, t);
        }
        public Boolean runAsync(){
            return null;
        }
    }
    
    @api
    public static class abs extends AbstractFunction{

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

        public CHVersion since() {
            return CHVersion.V3_1_2;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            double d = Static.getDouble(args[0]);
            return new CDouble(java.lang.Math.abs(d), t);
        }
        
        @Override
        public boolean canOptimize() {
            return true;
        }

        @Override
        public Construct optimize(Target t, Construct... args) {
            return exec(t, null, args);
        }
        
    }
    
    @api public static class floor extends AbstractFunction{

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

        public CHVersion since() {
            return CHVersion.V3_1_3;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            return new CInt((long)java.lang.Math.floor(Static.getNumber(args[0])), t);
        }
        
        @Override
        public boolean canOptimize() {
            return true;
        }

        @Override
        public Construct optimize(Target t, Construct... args) {
            return exec(t, null, args);
        }
        
    }
    
    @api public static class ceil extends AbstractFunction{

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

        public CHVersion since() {
            return CHVersion.V3_1_3;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            return new CInt((long)java.lang.Math.ceil(Static.getNumber(args[0])), t);
        }
        
        @Override
        public boolean canOptimize() {
            return true;
        }

        @Override
        public Construct optimize(Target t, Construct... args) {
            return exec(t, null, args);
        }
        
    }
    
    @api public static class sqrt extends AbstractFunction{

        public String getName() {
            return "sqrt";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "number {number} Returns the square root of a number. Note that this is mathematically equivalent to pow(number, .5)."
                    + " Imaginary numbers are not supported, so number must be positive.";
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

        public CHVersion since() {
            return CHVersion.V3_2_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            double d = Static.getNumber(args[0]);
            if(d < 0){
               throw new ConfigRuntimeException("sqrt expects a number >= 0", ExceptionType.RangeException, t); 
            }
            double m = java.lang.Math.sqrt(d);
            if(m == (int)m){
                return new CInt((long) m, t);
            } else {
                return new CDouble(m, t);
            }
        }
        
        @Override
        public boolean canOptimize() {
            return true;
        }

        @Override
        public Construct optimize(Target t, Construct... args) {
            return exec(t, null, args);
        }
        
    }
    
    @api public static class min extends AbstractFunction{

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

        public CHVersion since() {
            return CHVersion.V3_2_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            if(args.length == 0){
                throw new ConfigRuntimeException("You must send at least one parameter to min", 
                        ExceptionType.InsufficientArgumentsException, t);
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
                return new CInt((long)lowest, t);
            } else {
                return new CDouble(lowest, t);
            }
        }
        
        public List<Construct> recList(List<Construct> list, Construct ... args){
            for(Construct c : args){
                if(c instanceof CArray){
                    for(int i = 0; i < ((CArray)c).size(); i++){
                        recList(list, ((CArray)c).get(i, Target.UNKNOWN));
                    }
                } else {
                    list.add(c);
                }
            }
            return list;
        }
        
        @Override
        public boolean canOptimize() {
            return true;
        }

        @Override
        public Construct optimize(Target t, Construct... args) {
            return exec(t, null, args);
        }
        
    }
    
    @api public static class max extends AbstractFunction{

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

        public CHVersion since() {
            return CHVersion.V3_2_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            if(args.length == 0){
                throw new ConfigRuntimeException("You must send at least one parameter to max", 
                        ExceptionType.InsufficientArgumentsException, t);
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
                return new CInt((long)highest, t);
            } else {
                return new CDouble(highest, t);
            }
        }
        
        public List<Construct> recList(List<Construct> list, Construct ... args){
            for(Construct c : args){
                if(c instanceof CArray){
                    for(int i = 0; i < ((CArray)c).size(); i++){
                        recList(list, ((CArray)c).get(i, Target.UNKNOWN));
                    }
                } else {
                    list.add(c);
                }
            }
            return list;
        }
        
        @Override
        public boolean canOptimize() {
            return true;
        }

        @Override
        public Construct optimize(Target t, Construct... args) {
            return exec(t, null, args);
        }
        
    }
    
    @api public static class sin extends AbstractFunction{

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

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            return new CDouble(java.lang.Math.sin(Static.getNumber(args[0])), t);
        }
        
        @Override
        public boolean canOptimize() {
            return true;
        }

        @Override
        public Construct optimize(Target t, Construct... args) {
            return exec(t, null, args);
        }
        
    }
    
    @api public static class cos extends AbstractFunction{

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

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            return new CDouble(java.lang.Math.cos(Static.getNumber(args[0])), t);
        }
        
        @Override
        public boolean canOptimize() {
            return true;
        }

        @Override
        public Construct optimize(Target t, Construct... args) {
            return exec(t, null, args);
        }
        
    }
    
    @api public static class tan extends AbstractFunction{

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

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            return new CDouble(java.lang.Math.tan(Static.getNumber(args[0])), t);
        }
        
        @Override
        public boolean canOptimize() {
            return true;
        }

        @Override
        public Construct optimize(Target t, Construct... args) {
            return exec(t, null, args);
        }
        
    }
    
    @api public static class asin extends AbstractFunction{

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

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            return new CDouble(java.lang.Math.asin(Static.getNumber(args[0])), t);
        }
        
        @Override
        public boolean canOptimize() {
            return true;
        }

        @Override
        public Construct optimize(Target t, Construct... args) {
            return exec(t, null, args);
        }
        
    }
    
    @api public static class acos extends AbstractFunction{

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

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            return new CDouble(java.lang.Math.acos(Static.getNumber(args[0])), t);
        }
        
        @Override
        public boolean canOptimize() {
            return true;
        }

        @Override
        public Construct optimize(Target t, Construct... args) {
            return exec(t, null, args);
        }
        
    }
    
    @api public static class atan extends AbstractFunction{

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

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            return new CDouble(java.lang.Math.atan(Static.getNumber(args[0])), t);
        }
        
        @Override
        public boolean canOptimize() {
            return true;
        }

        @Override
        public Construct optimize(Target t, Construct... args) {
            return exec(t, null, args);
        }
        
    }
    
    @api public static class to_radians extends AbstractFunction{

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

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            return new CDouble(java.lang.Math.toRadians(Static.getNumber(args[0])), t);
        }
        
        @Override
        public boolean canOptimize() {
            return true;
        }

        @Override
        public Construct optimize(Target t, Construct... args) {
            return exec(t, null, args);
        }
        
    }
    
    @api public static class to_degrees extends AbstractFunction{

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

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            return new CDouble(java.lang.Math.toDegrees(Static.getNumber(args[0])), t);
        }
        
        @Override
        public boolean canOptimize() {
            return true;
        }

        @Override
        public Construct optimize(Target t, Construct... args) {
            return exec(t, null, args);
        }
        
    }
    
    @api public static class atan2 extends AbstractFunction{

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

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            return new CDouble(java.lang.Math.atan2(Static.getNumber(args[0]), Static.getNumber(args[1])), t);
        }
        
        @Override
        public boolean canOptimize() {
            return true;
        }

        @Override
        public Construct optimize(Target t, Construct... args) {
            return exec(t, null, args);
        }
        
    }
    
    @api public static class round extends AbstractFunction{

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

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            return new CInt(java.lang.Math.round(Static.getNumber(args[0])), t);
        }
        
        @Override
        public boolean canOptimize() {
            return true;
        }

        @Override
        public Construct optimize(Target t, Construct... args) {
            return exec(t, null, args);
        }
        
    }
    
    @api public static class expr extends AbstractFunction{

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

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {            
            String expr = args[0].val();
            CArray vars = null;
            if(args.length == 2 && args[1] instanceof CArray){
                vars = (CArray)args[1];
            } else if(args.length == 2 && !(args[1] instanceof CArray)){
                throw new ConfigRuntimeException("The second argument of expr() should be an array", ExceptionType.CastException, t);
            }
            if(vars != null && !vars.inAssociativeMode()){
                throw new ConfigRuntimeException("The array provided to expr() must be an associative array", ExceptionType.CastException, t);
            }
            double[] da;
            String[] varNames;
            if(vars != null){
                int i = 0;
                da = new double[vars.size()];
                varNames = new String[vars.size()];
                for(String key : vars.keySet()){
                    varNames[i] = key;
                    da[i] = Static.getDouble(vars.get(key, t));
                    i++;
                }
            } else {
                da = new double[0];
                varNames = new String[0];
            }
            try {
                Expression e = Expression.compile(expr, varNames);
                return new CDouble(e.evaluate(da), t);
            } catch (ExpressionException ex) {
                throw new ConfigRuntimeException("Your expression was invalidly formatted", ExceptionType.PluginInternalException, t, ex);
            }
        }
        
        @Override
        public boolean canOptimize() {
            return true;
        }

        @Override
        public Construct optimize(Target t, Construct... args) {
            return exec(t, null, args);
        }
        
    }
    
    @api public static class neg extends AbstractFunction{

        public String getName() {
            return "neg";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "number {number} Negates a number, essentially multiplying the number by -1";
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

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            if(args[0] instanceof CInt){
                return new CInt(-(Static.getInt(args[0])), t);
            } else {
                return new CDouble(-(Static.getDouble(args[0])), t);
            }
        }

        public CHVersion since() {
            return CHVersion.V3_3_1;
        }
        
        @Override
        public boolean canOptimize() {
            return true;
        }

        @Override
        public Construct optimize(Target t, Construct... args) {
            return exec(t, null, args);
        }
        
    }
    
}
