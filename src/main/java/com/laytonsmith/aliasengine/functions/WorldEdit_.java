/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.CancelCommandException;
import com.laytonsmith.aliasengine.ConfigRuntimeException;
import com.laytonsmith.aliasengine.Constructs.CArray;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.Static;
import com.laytonsmith.aliasengine.functions.Exceptions.ExceptionType;
import com.sk89q.worldedit.LocalSession;
import org.bukkit.entity.Player;

/**
 *
 * @author Layton
 */
public class WorldEdit_ {
    public static String docs(){
        return "Provides various methods for programmatically hooking into WorldEdit";
    }
    
    public static class hpos1 implements Function{

        public String getName() {
            return "hpos1";
        }

        public Integer[] numArgs() {
            return new Integer[]{1,2,4};
        }
        
        public ExceptionType[] thrown() {
            return new ExceptionType[]{};
        }

        public String docs() {
            return "array {player | player, array | player, x, y, z} In the first usage, returns an array for the player's currently selected primary point,"
                    + " selected through WorldEdit. In the second usage, sets the player's primary point to the specified x,y,z location. In the third usage,"
                    + " it works the same, except you needn't send an array.";
        }

        public boolean isRestricted() {
            return true;
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

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Player player = p.getServer().getPlayer(args[0].val());
            if(args.length == 1){
                //Getter
                LocalSession ls = Static.getWorldEditPlugin().getSession(player);
                
            } else {
                int x;
                int y;
                int z;
                if(args[1] instanceof CArray){
                    CArray ca = (CArray)args[1];
                    x = (int)Static.getInt(ca.get(0, line_num));
                    y = (int)Static.getInt(ca.get(1, line_num));
                    z = (int)Static.getInt(ca.get(2, line_num));
                } else {
                    x = (int)Static.getInt(args[1]);
                    y = (int)Static.getInt(args[2]);
                    z = (int)Static.getInt(args[3]);
                }
                //Setter
            }
            return null;
        }
        
    }
    public static class hpos2 implements Function{

        public String getName() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Integer[] numArgs() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String docs() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public ExceptionType[] thrown() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public boolean isRestricted() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void varList(IVariableList varList) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean preResolveVariables() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String since() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Boolean runAsync() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
}
