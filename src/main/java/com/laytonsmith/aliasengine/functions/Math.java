/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.CancelCommandException;
import com.laytonsmith.aliasengine.ConfigRuntimeException;
import com.laytonsmith.aliasengine.Constructs.*;
import com.laytonsmith.aliasengine.Static;
import java.util.Random;
import org.bukkit.entity.Player;

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

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            double tally = Static.getNumber(args[0]);
            for(int i = 1; i < args.length; i++){
                tally += Static.getNumber(args[i]);
            }
            if(Static.anyDoubles(args)){
                return new CDouble(tally, line_num);
            } else {
                return new CInt((int)tally, line_num);
            }
        }

        public String docs() {
            return "mixed {var1, [var2...]} Adds all the arguments together, and returns either a double or an integer";
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
        public boolean runAsync(){
            return true;
        }
    }
    
    @api public static class subtract implements Function{

        public String getName() {
            return "subtract";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            double tally = Static.getNumber(args[0]);
            for(int i = 1; i < args.length; i++){
                tally -= Static.getNumber(args[i]);
            }
            if(Static.anyDoubles(args)){
                return new CDouble(tally, line_num);
            } else {
                return new CInt((int)tally, line_num);
            }
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
        public boolean runAsync(){
            return true;
        }
    }
    
    @api public static class multiply implements Function{

        public String getName() {
            return "multiply";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            double tally = Static.getNumber(args[0]);
            for(int i = 1; i < args.length; i++){
                tally *= Static.getNumber(args[i]);
            }
            if(Static.anyDoubles(args)){
                return new CDouble(tally, line_num);
            } else {
                return new CInt((int)tally, line_num);
            }
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
        public boolean runAsync(){
            return true;
        }
    }
    
    @api public static class divide implements Function{

        public String getName() {
            return "divide";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            double tally = Static.getNumber(args[0]);
            for(int i = 1; i < args.length; i++){
                tally /= Static.getNumber(args[i]);
            }
            if(tally == (int)tally){
                return new CInt((int)tally, line_num);
            } else {
                return new CDouble(tally, line_num);
            }
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
        public boolean runAsync(){
            return true;
        }
    }
    
    @api public static class mod implements Function{

        public String getName() {
            return "mod";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            long arg1 = Static.getInt(args[0]);
            long arg2 = Static.getInt(args[1]);
            return new CInt(arg1 % arg2, line_num);
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
        public boolean runAsync(){
            return true;
        }
    }
    
    @api public static class pow implements Function{

        public String getName() {
            return "pow";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            double arg1 = Static.getNumber(args[0]);
            double arg2 = Static.getNumber(args[1]);
            return new CDouble(java.lang.Math.pow(arg1, arg2), line_num);
        }

        public String docs() {
            return "double {x, n} Returns x to the power of n";
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
        public boolean runAsync(){
            return true;
        }
    }
    
    @api public static class inc implements Function{

        IVariableList varList;
        
        public String getName() {
            return "inc";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            if(args[0] instanceof IVariable){
                IVariable v = varList.get(((IVariable)args[0]).getName());
                Construct newVal;
                if(Static.anyDoubles(v.ival())){
                    newVal = new CDouble(Static.getDouble(v.ival()) + 1, line_num);
                } else {
                    newVal = new CInt(Static.getInt(v.ival()) + 1, line_num);
                }
                v = new IVariable(v.getName(), newVal, line_num);
                varList.set(v);
                return v;
            }
            throw new ConfigRuntimeException("inc expects argument 1 to be an ivar");
        }

        public String docs() {
            return "ivar {var} Adds 1 to var, and stores the new value. Equivalent to ++var in other languages. Expects ivar to be a variable, then"
                    + " returns the ivar.";
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {
            this.varList = varList;
        }

        public boolean preResolveVariables() {
            return false;
        }
        public String since() {
            return "3.0.1";
        }
        public boolean runAsync(){
            return true;
        }
    }
    
    @api public static class dec implements Function{

        IVariableList varList;
        
        public String getName() {
            return "dec";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            if(args[0] instanceof IVariable){
                IVariable v = varList.get(((IVariable)args[0]).getName());
                Construct newVal;
                if(Static.anyDoubles(v.ival())){
                    newVal = new CDouble(Static.getDouble(v.ival()) - 1, line_num);
                } else {
                    newVal = new CInt(Static.getInt(v.ival()) - 1, line_num);
                }
                v = new IVariable(v.getName(), newVal, line_num);
                varList.set(v);
                return v;
            }
            throw new ConfigRuntimeException("dec expects argument 1 to be an ivar");
        }

        public String docs() {
            return "ivar {var} Subtracts 1 to var, and stores the new value. Equivalent to --var in other languages. Expects ivar to be a variable, then"
                    + " returns the ivar.";
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {
            this.varList = varList;
        }

        public boolean preResolveVariables() {
            return false;
        }
        public String since() {
            return "3.0.1";
        }
        public boolean runAsync(){
            return true;
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

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            long min = 0;
            long max = 0;
            if(args.length == 1){
                max = Static.getInt(args[0]);
            } else {
                min = Static.getInt(args[0]);
                max = Static.getInt(args[1]);
            }
            if(max > Integer.MAX_VALUE || min > Integer.MAX_VALUE){
                throw new ConfigRuntimeException("max and min must be below int max, defined as " + Integer.MAX_VALUE);
            }
            int i = r.nextInt((int)(max - min)) + ((int)min);
            
            return new CInt(i, line_num);
            
        }
        public boolean runAsync(){
            return true;
        }
    }
    
    
}
