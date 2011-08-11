/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.functions.exceptions.CancelCommandException;
import com.laytonsmith.aliasengine.functions.exceptions.ConfigRuntimeException;
import com.laytonsmith.aliasengine.Constructs.CArray;
import com.laytonsmith.aliasengine.Constructs.CVoid;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.Static;
import com.laytonsmith.aliasengine.functions.Exceptions.ExceptionType;
import java.io.File;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 *
 * @author Layton
 */
public class Weather {
    public static String docs(){
        return "Provides functions to control the weather";
    }
    
    @api public static class lightning implements Function{

        public String getName() {
            return "lightning";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 3};
        }
        
        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.LengthException};
        }

        public Construct exec(int line_num, File f, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            int x;
            int y;
            int z;
            if(args.length == 1){
                if(args[0] instanceof CArray){
                    CArray a = (CArray)args[0];
                    if(a.size() != 3){
                        throw new ConfigRuntimeException("lightning expects the array to have 3 integers", ExceptionType.LengthException, line_num);
                    }
                    x = (int)java.lang.Math.floor(Static.getNumber(a.get(0, line_num)));
                    y = (int)java.lang.Math.floor(Static.getNumber(a.get(1, line_num)));
                    z = (int)java.lang.Math.floor(Static.getNumber(a.get(2, line_num)));
                } else {
                    throw new ConfigRuntimeException("lightning expects an array as the one argument", ExceptionType.CastException, line_num);
                }
            } else {
                x = (int)java.lang.Math.floor(Static.getNumber(args[0]));
                y = (int)java.lang.Math.floor(Static.getNumber(args[1]));
                z = (int)java.lang.Math.floor(Static.getNumber(args[2]));
            }
            p.getWorld().strikeLightning(new Location(p.getWorld(), x, y + 1, z)); 
//            World w = ((CraftWorld)p.getWorld()).getHandle();
//            EntityWeatherStorm e = new EntityWeatherStorm(w, x, y, z);
//            w.a(e);
            
            return new CVoid(line_num, f);
        }

        public String docs() {
            return "void {strikeLocArray | x, y, z} Makes lightning strike at the x y z coordinates specified in the array(x, y, z).";
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }
        public String since() {
            return "3.0.1";
        }
        public Boolean runAsync(){
            return false;
        }
    }
    
    @api public static class storm implements Function{

        public String getName() {
            return "storm";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public Construct exec(int line_num, File f, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            boolean b = Static.getBoolean(args[0]);
            p.getWorld().setStorm(b);
            return new CVoid(line_num, f);
        }

        public String docs() {
            return "void {isStorming} Creates a storm if isStorming is true, stops a storm if isStorming is false";
        }
        
        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }
        public String since() {
            return "3.0.1";
        }
        public Boolean runAsync(){
            return false;
        }
        
    }
    
}
