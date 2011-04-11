/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.CancelCommandException;
import com.laytonsmith.aliasengine.ConfigRuntimeException;
import com.laytonsmith.aliasengine.Constructs.*;
import com.laytonsmith.aliasengine.Constructs.Construct;
import java.util.ArrayList;
import org.bukkit.entity.Player;

/**
 *
 * @author Layton
 */
public class BasicLogic {
    public static class _if implements Function{

        public String getName() {
            return "if";
        }

        public Integer[] numArgs() {
            return new Integer[]{2, 3};
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            //We need to test differently for each type of data
            boolean val = false;
            Construct test = args[0];
            if(test instanceof CBoolean){
                val = ((CBoolean)test).getBoolean();
            } else if(test instanceof CDouble){
                CDouble ctest = ((CDouble)test);
                if(ctest.getDouble() > 0 || ctest.getDouble() < 0){
                    val = true;
                } else{
                    val = false;
                }
            } else if(test instanceof CInt){
                CInt ctest = (CInt) test;
                if(ctest.getInt() > 0 || ctest.getInt() < 0){
                    val = true;
                } else {
                    val = false;
                }
            } else if(test instanceof CNull){
                val = false;
            } else if(test instanceof CString){
                if(test.val().equals("")){
                    val = false;
                } else {
                    val = true;
                }
            } else if(test instanceof CVoid){
                val = false;
            }
            
            if(val){
                return args[1];
            } else{
                if(args.length == 3){
                    return args[2];
                } else{
                    return new CVoid(line_num);
                }
            }
        }

        public String docs() {
            return "If the first argument evaluates to a true value, the second argument is returned, otherwise the third argument is returned."
                    + " If there is no third argument, it returns void.";
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }
        
        
    }
    
    public static class _equals implements Function{

        public String getName() {
            return "equals";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            return new CBoolean(args[0].val().equals(args[1].val()), line_num);
        }

        public String docs() {
            return "Returns true or false if the two arguments are equal";
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }
        
    }
    
    public static class lt implements Function{

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

        public String docs() {
            return "Returns the results of a less than operation";
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }
        
    }
    
    public static class gt implements Function{

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

        public String docs() {
            return "Returns the result of a greater than operation";
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }
        
    }
    
    public static class lte implements Function{

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

        public String docs() {
            return "Returns the result of a less than or equal to operation";
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }
        
    }
    
    public static class gte implements Function{

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

        public String docs() {
            return "Returns the result of a greater than or equal to operation";
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }
        
    }
    
    public static class add implements Function{

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
            return "Adds all the arguments together, and returns either a double or an integer";
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }
        
    }
    
    public static class subtract implements Function{

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
            return "Subtracts the arguments from left to right, and returns either a double or an integer";
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }
        
    }
    
    public static class multiply implements Function{

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
            return "Multiplies the arguments together, and returns either a double or an integer";
        }

        public boolean isRestricted() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }
        
    }
    
    public static class divide implements Function{

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
            return "Divides the arguments from left to right, and returns either a double or an integer";
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }
        
    }
    
    public static class mod implements Function{

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
            return "Returns the modulo";
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }
        
    }
    
    public static class pow implements Function{

        public String getName() {
            return "pow";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            double arg1 = Static.getNumber(args[0]);
            double arg2 = Static.getNumber(args[1]);
            return new CDouble(Math.pow(arg1, arg2), line_num);
        }

        public String docs() {
            return "Returns the mathematical power function";
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }
        
    }
    
    public static class Static{
        public static double getNumber(Construct c){
            double d;
            if(c instanceof CInt){
                d = ((CInt)c).getInt();
            } else if(c instanceof CDouble){
                d = ((CDouble)c).getDouble();
            } else{
                throw new ConfigRuntimeException("Expecting a number, but recieved " + c.val() + " instead");
            }
            return d;
        }
        
        public static double getDouble(Construct c){
            try{
                return getNumber(c);
            } catch(ConfigRuntimeException e){
                throw new ConfigRuntimeException("Expecting a double, but recieved " + c.val() + " instead");
            }
        }
        
        public static int getInt(Construct c){
            int i;
            if(c instanceof CInt){
                i = ((CInt)c).getInt();
            } else{
                throw new ConfigRuntimeException("Expecting an integer, but recieved " + c.val() + " instead");
            }
            return i;
        }
        
        public static boolean anyDoubles(Construct ... c){
            for(int i = 0; i < c.length; i++){
                if(c[i] instanceof CDouble){
                    return true;
                }
            }
            return false;
        }
    }
}
