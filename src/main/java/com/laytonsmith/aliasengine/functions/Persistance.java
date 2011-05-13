/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.CancelCommandException;
import com.laytonsmith.aliasengine.ConfigRuntimeException;
import com.laytonsmith.aliasengine.Constructs.CNull;
import com.laytonsmith.aliasengine.Constructs.CVoid;
import com.laytonsmith.aliasengine.Constructs.Construct;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.entity.Player;

/**
 *
 * @author Layton
 */
public class Persistance {
    public static String docs(){
        return "Allows scripts to store data from execution to execution.";
    }
    
    @api public static class store_value implements Function{

        public String getName() {
            return "store_value";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            return "void {key, value} Allows you to store a value, which can then be retrieved later. key must be a string containing"
                    + " only letters, numbers, underscores.";
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.0.2";
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            String key = args[0].val();
            Construct value = args[1];
            for(int i = 0; i < key.length(); i++){
                Character c = key.charAt(i);
                if(c != '_' && !Character.isLetterOrDigit(c)){
                    throw new CancelCommandException("Param 1 in store_value must only contain letters, digits, or underscores.");
                }
            }
            com.laytonsmith.aliasengine.Static.getPersistance().setValue(new String[]{"commandhelper", "function", "storage", key}, value);
            try {
                com.laytonsmith.aliasengine.Static.getPersistance().save();
            } catch (Exception ex) {
                Logger.getLogger(Persistance.class.getName()).log(Level.SEVERE, null, ex);
            }
            return new CVoid(line_num);
        }
        
    }
    
    @api public static class get_value implements Function{

        public String getName() {
            return "get_value";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "Mixed {key} Returns a stored value stored with store_value. If the key doesn't exist in storage, null"
                    + " is returned. On a more detailed note: If the value stored in the persistance database is not actually a construct,"
                    + " then null is also returned.";
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.0.2";
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {            
            Object o = com.laytonsmith.aliasengine.Static.getPersistance().getValue(new String[]{"commandhelper", "function", "storage", args[0].val()});
            if(o == null){
                return new CNull(line_num);
            }
            try{
                return (Construct)o;
            } catch(ClassCastException e){
                return new CNull(line_num);
            }
        }
        
    }
}
