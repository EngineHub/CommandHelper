/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.CancelCommandException;
import com.laytonsmith.aliasengine.ConfigRuntimeException;
import com.laytonsmith.aliasengine.Constructs.CArray;
import com.laytonsmith.aliasengine.Constructs.CInt;
import com.laytonsmith.aliasengine.Constructs.CVoid;
import com.laytonsmith.aliasengine.Constructs.Construct;
import org.bukkit.entity.Player;

/**
 *
 * @author Layton
 */
public class ArrayHandling {
    @api public static class array_size implements Function{

        public String getName() {
            return "array_size";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            return new CInt(((CArray)args[0]).size(), line_num);
        }

        public String docs() {
            return "Returns the size of this array as an integer";
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }
        
    }
    
    @api public static class array_get implements Function{

        public String getName() {
            return "array_get";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            return ((CArray)args[0]).get(((CInt)args[1]).getInt());
        }

        public String docs() {
            return "Returns the element specified at the index of the array. If the element doesn't exist, an exception is thrown. "
                    + "array_get(array, index)";
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }
        
    }
    
    @api public static class array_set implements Function{

        public String getName() {
            return "array_set";
        }

        public Integer[] numArgs() {
            return new Integer[]{3};
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            ((CArray)args[0]).set(((CInt)args[1]).getInt(), args[2]);
            return new CVoid(line_num);
        }

        public String docs() {
            return "Sets the value of the array at the specified index. array_set(array, index, value). Returns void.";
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return false;
        }
        
    }
    
    @api public static class array_push implements Function{

        public String getName() {
            return "array_push";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String docs() {
            return "Pushes the specified value onto the end of the array";
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return false;
        }
        
    }
}
