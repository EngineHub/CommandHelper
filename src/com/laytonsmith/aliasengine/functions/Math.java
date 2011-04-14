/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.CancelCommandException;
import com.laytonsmith.aliasengine.ConfigRuntimeException;
import com.laytonsmith.aliasengine.Constructs.*;
import org.bukkit.entity.Player;

/**
 *
 * @author Layton
 */
public class Math {
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
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
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
        
    }
    
    @api public static class mod implements Function{

        public String getName() {
            return "mod";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            int arg1 = Static.getInt(args[0]);
            int arg2 = Static.getInt(args[1]);
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
        
    }
}
