/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.CancelCommandException;
import com.laytonsmith.aliasengine.ConfigRuntimeException;
import com.laytonsmith.aliasengine.Constructs.CArray;
import com.laytonsmith.aliasengine.Constructs.CVoid;
import com.laytonsmith.aliasengine.Constructs.Construct;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 *
 * @author Layton
 */
public class Weather {
    @api public static class lightning implements Function{

        public String getName() {
            return "lightning";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            int x;
            int y;
            int z;
            if(args[0] instanceof CArray){
                CArray a = (CArray)args[0];
                if(a.size() != 3){
                    throw new ConfigRuntimeException("lightning expects the array to have 3 integers");
                }
                x = Static.getInt(a.get(0));
                y = Static.getInt(a.get(1));
                z = Static.getInt(a.get(2));
            } else {
                throw new ConfigRuntimeException("lightning expects an array as the one argument");
            }
            p.getWorld().strikeLightning(new Location(p.getWorld(), x, y, z)); 
//            World w = ((CraftWorld)p.getWorld()).getHandle();
//            EntityWeatherStorm e = new EntityWeatherStorm(w, x, y, z);
//            w.a(e);
            
            return new CVoid(line_num);
        }

        public String docs() {
            return "void {strikeLocArray} Makes lightning strike at the x y z coordinates specified in the array(x, y, z).";
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }
        
    }
    
    @api public static class storm implements Function{

        public String getName() {
            return "storm";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            boolean b = Static.getBoolean(args[0]);
            p.getWorld().setStorm(b);
            return new CVoid(line_num);
        }

        public String docs() {
            return "void {isStorming} Creates a storm if isStorming is true, stops a storm if isStorming is false";
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
