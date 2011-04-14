/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.CancelCommandException;
import com.laytonsmith.aliasengine.ConfigRuntimeException;
import com.laytonsmith.aliasengine.Constructs.CArray;
import com.laytonsmith.aliasengine.Constructs.CString;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.Static;
import org.bukkit.entity.Player;

/**
 *
 * @author Layton
 */
public class PlayerManangment {
    @api public static class player implements Function{

        public String getName() {
            return "player";
        }

        public Integer[] numArgs() {
            return new Integer[]{0};
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            if(p == null){
                return new CString("TestPlayer", line_num);
            } else {
                return new CString(p.getName(), line_num);
            }
        }

        public String docs() {
            return "string {} Returns the name of the player running the command";
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }
        
    }
    
    @api public static class all_players implements Function{

        public String getName() {
            return "all_players";
        }

        public Integer[] numArgs() {
            return new Integer[]{0};
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Player [] pa = Static.getServer().getOnlinePlayers();
            CString [] sa = new CString[pa.length];
            for(int i = 0; i < pa.length; i++){
                sa[i] = new CString(pa[i].getName(), line_num);
            }
            return new CArray(line_num, sa);
        }

        public String docs() {
            return "array {} Returns an array of all the player names of all the online players on the server";
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }
        
    }
}
