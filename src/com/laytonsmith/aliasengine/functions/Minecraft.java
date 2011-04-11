/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.CancelCommandException;
import com.laytonsmith.aliasengine.ConfigRuntimeException;
import com.laytonsmith.aliasengine.Constructs.CInt;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.Data_Values;
import org.bukkit.entity.Player;

/**
 *
 * @author Layton
 */
public class Minecraft {
    public static class data_values implements Function{

        public String getName() {
            return "data_values";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Construct c = args[0];
            return new CInt(Data_Values.val(c.val()), line_num);
        }

        public String docs() {
            return "Does a lookup to return the data value of a name. For instance, returns 1 for 'stone'. If an integer is given,"
                    + " simply returns that number";
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
