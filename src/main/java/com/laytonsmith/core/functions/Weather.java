

package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.*;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;

/**
 *
 * @author Layton
 */
public class Weather {
    public static String docs(){
        return "Provides functions to control the weather";
    }
    
    @api public static class lightning extends AbstractFunction{

        public String getName() {
            return "lightning";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2, 3, 4};
        }
        
        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.LengthException, ExceptionType.InvalidWorldException, ExceptionType.FormatException};
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            int x;
            int y;
            int z;
            MCWorld w = null;
            boolean safe = false;
            int safeIndex = 1;
            if(args[0] instanceof CArray){
                CArray a = (CArray)args[0];
                MCLocation l = ObjectGenerator.GetGenerator().location(a, (env.GetCommandSender() instanceof MCPlayer?env.GetPlayer().getWorld():null), t);
                x = (int)java.lang.Math.floor(l.getX());
                y = (int)java.lang.Math.floor(l.getY());
                z = (int)java.lang.Math.floor(l.getZ());
                w = l.getWorld();
            } else {
                x = (int)java.lang.Math.floor(Static.getNumber(args[0]));
                y = (int)java.lang.Math.floor(Static.getNumber(args[1]));
                z = (int)java.lang.Math.floor(Static.getNumber(args[2]));
                safeIndex = 3;
            }
            if(args.length >= safeIndex + 1){
                safe = Static.getBoolean(args[safeIndex]);
            }
            if(w != null){
                if(!safe){
                    w.strikeLightning(StaticLayer.GetLocation(w, x, y + 1, z)); 
                } else {
                    w.strikeLightningEffect(StaticLayer.GetLocation(w, x, y + 1, z));
                }
            } else {
                throw new ConfigRuntimeException("World was not specified", ExceptionType.InvalidWorldException, t);
            }
            
            return new CVoid(t);
        }

        public String docs() {
            return "void {strikeLocArray, [safe] | x, y, z, [safe]} Makes lightning strike at the x y z coordinates specified in the array(x, y, z). safe"
                    + " defaults to false, but if true, lightning striking a player will not hurt them.";
        }

        public boolean isRestricted() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_0_1;
        }
        public Boolean runAsync(){
            return false;
        }
    }
    
    @api public static class storm extends AbstractFunction{

        public String getName() {
            return "storm";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            boolean b = Static.getBoolean(args[0]);
            MCWorld w = null;
            if(env.GetCommandSender() instanceof MCPlayer){
                w = env.GetPlayer().getWorld();
            }
            if(args.length == 2){
                w = Static.getServer().getWorld(args[1].val());
            }
            if(w != null){
                w.setStorm(b);
            } else {
                throw new ConfigRuntimeException("World was not specified", ExceptionType.InvalidWorldException, t);
            }
            return new CVoid(t);
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

        public CHVersion since() {
            return CHVersion.V3_0_1;
        }
        public Boolean runAsync(){
            return false;
        }
        
    }
    
}
