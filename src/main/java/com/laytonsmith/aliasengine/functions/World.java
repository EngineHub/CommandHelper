/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.Constructs.CArray;
import com.laytonsmith.aliasengine.Constructs.CVoid;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.Env;
import com.laytonsmith.aliasengine.Static;
import com.laytonsmith.aliasengine.api;
import com.laytonsmith.aliasengine.exceptions.ConfigRuntimeException;
import com.laytonsmith.aliasengine.functions.Exceptions.ExceptionType;
import java.io.File;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 *
 * @author Layton
 */
public class World {
    public static String docs(){
        return "Provides functions for manipulating a world";
    }
    
    @api public static class get_spawn implements Function{

        public String getName() {
            return "array {[world]} Returns a location array for the specified world, or the current player's world, if not specified.";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1};
        }

        public String docs() {
            return "";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.InvalidWorldException};
        }

        public boolean isRestricted() {
            return true;
        }

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.3.0";
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(int line_num, File f, Env environment, Construct... args) throws ConfigRuntimeException {
            String world;
            if(args.length == 1){
                world = environment.GetPlayer().getWorld().getName();
            } else {
                world = args[0].val();
            }
            return Static.GetLocationArray(Static.getServer().getWorld(world).getSpawnLocation());
        }
        
    }
    
    @api public static class refresh_chunk implements Function{

        public String getName() {
            return "refresh_chunk";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2, 3};
        }

        public String docs() {
            return "void {[world], x, z | [world], locationArray} Resends the chunk to all clients, using the specified world, or the current"
                    + " players world if not provided.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{};
        }

        public boolean isRestricted() {
            return true;
        }

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.3.0";
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(int line_num, File f, Env environment, Construct... args) throws ConfigRuntimeException {
            Player m = environment.GetPlayer();
            org.bukkit.World world;
            int x;
            int z;
            if(args.length == 1){
                //Location array provided                
                Location l = Static.GetLocation(args[0], m!=null?m.getWorld():null, line_num, f);
                world = l.getWorld();
                x = l.getBlockX();
                z = l.getBlockZ();
            } else if(args.length == 2) {
                //Either location array and world provided, or x and z. Test for array at pos 2
                if(args[1] instanceof CArray){
                    world = Static.getServer().getWorld(args[0].val());
                    Location l = Static.GetLocation(args[1], null, line_num, f);
                    x = l.getBlockX();
                    z = l.getBlockZ();
                } else {
                    if(m == null){
                        throw new ConfigRuntimeException("No world specified", ExceptionType.InvalidWorldException, line_num, f);
                    }
                    world = m.getWorld();
                    x = (int)Static.getInt(args[0]);
                    z = (int)Static.getInt(args[1]);
                }
            } else {
                //world, x and z provided
                world = Static.getServer().getWorld(args[0].val());
                x = (int)Static.getInt(args[1]);
                z = (int)Static.getInt(args[2]);
            }
            world.refreshChunk(x, z);
            return new CVoid(line_num, f);
        }
        
    }
}
