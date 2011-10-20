/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.api;
import com.laytonsmith.aliasengine.exceptions.CancelCommandException;
import com.laytonsmith.aliasengine.exceptions.ConfigRuntimeException;
import com.laytonsmith.aliasengine.Constructs.CArray;
import com.laytonsmith.aliasengine.Constructs.CString;
import com.laytonsmith.aliasengine.Constructs.CVoid;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.Static;
import com.laytonsmith.aliasengine.functions.Exceptions.ExceptionType;
import java.io.File;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Layton
 */
public class Environment {
    public static String docs(){
        return "Allows you to manipulate the environment around the player";
    }
    
    @api public static class get_block_at implements Function{

        public String getName() {
            return "get_block_at";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2, 3, 4};
        }

        public String docs() {
            return "string {x, y, z, [world] | xyzArray, [world]} Gets the id of the block at x, y, z. This function expects "
                    + "either 1 or 3 arguments. If 1 argument is passed, it should be an array with the x, y, z"
                    + " coordinates. The format of the return will be x:y where x is the id of the block, and"
                    + " y is the meta data for the block. All blocks will return in this format, but blocks"
                    + " that don't have meta data normally will return 0 in y. If world isn't specified, the current"
                    + " player's world is used.";
        }
        
        public ExceptionType[] thrown(){
            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.LengthException, ExceptionType.InvalidWorldException};
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

        public Construct exec(int line_num, File f, CommandSender p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            double x = 0;
            double y = 0;
            double z = 0;
            World w = null;
            String world = null;
            if(p instanceof Player){
                w = ((Player)p).getWorld();
            }
            if(args.length == 1 || args.length == 2){
                if(args[0] instanceof CArray){
                    CArray ca = (CArray)args[0];
                    if(ca.size() == 3){
                        x = Static.getDouble(ca.get(0, line_num));
                        y = Static.getDouble(ca.get(1, line_num));
                        z = Static.getDouble(ca.get(2, line_num));
                    } else {
                        throw new ConfigRuntimeException("get_block_at expects the array at param 1 to have 3 arguments", ExceptionType.LengthException,
                                line_num, f);
                    }
                } else {
                    throw new ConfigRuntimeException("get_block_at expects param 1 to be an array", ExceptionType.CastException, line_num, f);
                }
                if(args.length == 2){
                    world = args[1].val();
                }
            } else if(args.length == 3 || args.length == 4){
                x = Static.getDouble(args[0]);
                y = Static.getDouble(args[1]);
                z = Static.getDouble(args[2]);
                if(args.length == 4){
                    world = args[3].val();
                }
            }
            if(world != null){
                w = Static.getServer().getWorld(world);
            }
            if(w == null){
                throw new ConfigRuntimeException("The specified world " + world + " doesn't exist", ExceptionType.InvalidWorldException, line_num, f);
            }
            x = java.lang.Math.floor(x);
            y = java.lang.Math.floor(y);
            z = java.lang.Math.floor(z);
            Block b = w.getBlockAt((int)x, (int)y, (int)z);
            return new CString(b.getTypeId() + ":" + b.getData(), line_num, f);
        }
        public Boolean runAsync(){
            return false;
        }
        
    }
    
    @api public static class set_block_at implements Function{

        public String getName() {
            return "set_block_at";
        }

        public Integer[] numArgs() {
            return new Integer[]{2, 3, 4, 5};
        }

        public String docs() {
            return "void {x, y, z, id, [world] | xyzArray, id, [world]} Sets the id of the block at the x y z coordinates specified. If the"
                    + " first argument passed is an array, it should be x y z coordinates. id must"
                    + " be a blocktype identifier similar to the type returned from get_block_at, except if the meta"
                    + " value is not specified, 0 is used. If world isn't specified, the current player's world"
                    + " is used.";
        }
        
        public ExceptionType[] thrown(){
            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.LengthException, ExceptionType.FormatException, ExceptionType.InvalidWorldException};
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

        public Construct exec(int line_num, File f, CommandSender p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            double x = 0;
            double y = 0;
            double z = 0;
            String id = null;
            String world = null;
            World w = null;
            if(p instanceof Player){
                w = ((Player)p).getWorld();
            }
            if((args.length == 2 || args.length == 3) && args[0] instanceof CArray){
                CArray ca = (CArray)args[0];
                if(ca.size() != 3){
                    throw new ConfigRuntimeException("set_block_at expects the parameter 1 to be an array with 3 elements.", ExceptionType.LengthException,
                            line_num, f);
                }
                x = Static.getNumber(ca.get(0, line_num));
                y = Static.getNumber(ca.get(1, line_num));
                z = Static.getNumber(ca.get(2, line_num));
                id = args[1].val();
                if(args.length == 3){
                    world = args[2].val();
                }
                
            } else {
                x = Static.getNumber(args[0]);
                y = Static.getNumber(args[1]);
                z = Static.getNumber(args[2]);
                id = args[3].val();
                if(args.length == 5){
                    world = args[4].val();
                }
            }
            if(world != null){
                w = Static.getServer().getWorld(world);
            }
            if(w == null){
                throw new ConfigRuntimeException("The specified world " + world + " doesn't exist", ExceptionType.InvalidWorldException, line_num, f);
            }
            x = java.lang.Math.floor(x);
            y = java.lang.Math.floor(y);
            z = java.lang.Math.floor(z);
            int ix = (int)x;
            int iy = (int)y;
            int iz = (int)z;
            System.out.println("Setting block at " + ix + "," + iy + "," + iz);
            Block b = w.getBlockAt(ix, iy, iz);
            StringBuilder data = new StringBuilder();
            StringBuilder meta = new StringBuilder();
            boolean inMeta = false;
            for(int i = 0; i < id.length(); i++){
                Character c = id.charAt(i);
                if(!inMeta){
                    if(!Character.isDigit(c) && c != ':'){
                        throw new ConfigRuntimeException("id must be formatted as such: 'x:y' where x and y are integers", ExceptionType.FormatException,
                                line_num, f);
                    }
                    if(c == ':'){
                        inMeta = true;
                        continue;
                    }
                    data.append(c);
                } else {
                    meta.append(c);
                }
            }
            if(meta.length() == 0){
                meta.append("0");
            }
            
            int idata = Integer.parseInt(data.toString());
            byte imeta = Byte.parseByte(meta.toString());
            b.setTypeId(idata);
            b.setData(imeta);
            
            return new CVoid(line_num, f);
        }
        public Boolean runAsync(){
            return false;
        }
        
    }
    
}
