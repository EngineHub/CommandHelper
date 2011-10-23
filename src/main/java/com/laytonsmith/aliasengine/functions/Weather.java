/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.api;
import com.laytonsmith.aliasengine.exceptions.CancelCommandException;
import com.laytonsmith.aliasengine.exceptions.ConfigRuntimeException;
import com.laytonsmith.aliasengine.Constructs.CArray;
import com.laytonsmith.aliasengine.Constructs.CVoid;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.Env;
import com.laytonsmith.aliasengine.Static;
import com.laytonsmith.aliasengine.functions.Exceptions.ExceptionType;
import java.io.File;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
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
            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.LengthException, ExceptionType.InvalidWorldException};
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            int x;
            int y;
            int z;
            World w = null;
            if(args.length == 1){
                if(args[0] instanceof CArray){
                    CArray a = (CArray)args[0];
                    if(a.size() != 3){
                        throw new ConfigRuntimeException("lightning expects the array to be a location array", 
                                ExceptionType.LengthException, line_num, f);
                    }
                    Location l = Static.GetLocation(a, (env.GetCommandSender() instanceof Player?env.GetPlayer().getWorld():null), line_num, f);
                    x = (int)java.lang.Math.floor(l.getX());
                    y = (int)java.lang.Math.floor(l.getY());
                    z = (int)java.lang.Math.floor(l.getZ());
                    w = l.getWorld();
                } else {
                    throw new ConfigRuntimeException("lightning expects an array as the one argument", 
                            ExceptionType.CastException, line_num, f);
                }
            } else {
                x = (int)java.lang.Math.floor(Static.getNumber(args[0]));
                y = (int)java.lang.Math.floor(Static.getNumber(args[1]));
                z = (int)java.lang.Math.floor(Static.getNumber(args[2]));
            }
            if(w != null){
                w.strikeLightning(new Location(w, x, y + 1, z)); 
            } else {
                throw new ConfigRuntimeException("World was not specified", ExceptionType.InvalidWorldException, line_num, f);
            }
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
            return new Integer[]{1, 2};
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            boolean b = Static.getBoolean(args[0]);
            World w = null;
            if(env.GetCommandSender() instanceof Player){
                w = env.GetPlayer().getWorld();
            }
            if(args.length == 2){
                w = Static.getServer().getWorld(args[1].val());
            }
            if(w != null){
                w.setStorm(b);
            } else {
                throw new ConfigRuntimeException("World was not specified", ExceptionType.InvalidWorldException, line_num, f);
            }
            return new CVoid(line_num, f);
        }

        public String docs() {
            return "void {isStorming, [world]} Creates a storm if isStorming is true, stops a storm if isStorming is false";
        }
        
        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.InvalidWorldException};
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
