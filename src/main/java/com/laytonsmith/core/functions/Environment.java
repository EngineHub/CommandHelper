/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.blocks.MCSign;
import com.laytonsmith.abstraction.bukkit.BukkitMCServer;
import com.laytonsmith.core.api;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.Env;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import java.io.File;
import net.minecraft.server.Packet0KeepAlive;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;

/**
 *
 * @author Layton
 */
public class Environment {

    public static String docs() {
        return "Allows you to manipulate the environment around the player";
    }

    @api
    public static class get_block_at implements Function {

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

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.LengthException, ExceptionType.InvalidWorldException};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.0.2";
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            double x = 0;
            double y = 0;
            double z = 0;
            MCWorld w = null;
            String world = null;
            if (env.GetPlayer() instanceof MCPlayer) {
                w = env.GetPlayer().getWorld();
            }
            if (args.length == 1 || args.length == 2) {
                if (args[0] instanceof CArray) {
                    CArray ca = (CArray) args[0];
                    if (ca.size() == 3) {
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
                if (args.length == 2) {
                    world = args[1].val();
                }
            } else if (args.length == 3 || args.length == 4) {
                x = Static.getDouble(args[0]);
                y = Static.getDouble(args[1]);
                z = Static.getDouble(args[2]);
                if (args.length == 4) {
                    world = args[3].val();
                }
            }
            if (world != null) {
                w = Static.getServer().getWorld(world);
            }
            if (w == null) {
                throw new ConfigRuntimeException("The specified world " + world + " doesn't exist", ExceptionType.InvalidWorldException, line_num, f);
            }
            x = java.lang.Math.floor(x);
            y = java.lang.Math.floor(y);
            z = java.lang.Math.floor(z);
            MCBlock b = w.getBlockAt((int) x, (int) y, (int) z);
            return new CString(b.getTypeId() + ":" + b.getData(), line_num, f);
        }

        public Boolean runAsync() {
            return false;
        }
    }

    @api
    public static class set_block_at implements Function {

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

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.LengthException, ExceptionType.FormatException, ExceptionType.InvalidWorldException};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.0.2";
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            double x = 0;
            double y = 0;
            double z = 0;
            String id = null;
            String world = null;
            MCWorld w = null;
            if (env.GetPlayer() instanceof MCPlayer) {
                w = env.GetPlayer().getWorld();
            }
            if ((args.length == 2 || args.length == 3) && args[0] instanceof CArray) {
                MCLocation l = Static.GetLocation(args[0], env.GetPlayer().getWorld(), line_num, f);
                x = l.getBlockX();
                y = l.getBlockY();
                z = l.getBlockZ();
                world = l.getWorld().getName();
                id = args[1].val();
                if (args.length == 3) {
                    world = args[2].val();
                }

            } else {
                x = Static.getNumber(args[0]);
                y = Static.getNumber(args[1]);
                z = Static.getNumber(args[2]);
                id = args[3].val();
                if (args.length == 5) {
                    world = args[4].val();
                }
            }
            if (world != null) {
                w = Static.getServer().getWorld(world);
            }
            if (w == null) {
                throw new ConfigRuntimeException("The specified world " + world + " doesn't exist", ExceptionType.InvalidWorldException, line_num, f);
            }
            x = java.lang.Math.floor(x);
            y = java.lang.Math.floor(y);
            z = java.lang.Math.floor(z);
            int ix = (int) x;
            int iy = (int) y;
            int iz = (int) z;
            System.out.println("Setting block at " + ix + "," + iy + "," + iz);
            MCBlock b = w.getBlockAt(ix, iy, iz);
            StringBuilder data = new StringBuilder();
            StringBuilder meta = new StringBuilder();
            boolean inMeta = false;
            for (int i = 0; i < id.length(); i++) {
                Character c = id.charAt(i);
                if (!inMeta) {
                    if (!Character.isDigit(c) && c != ':') {
                        throw new ConfigRuntimeException("id must be formatted as such: 'x:y' where x and y are integers", ExceptionType.FormatException,
                                line_num, f);
                    }
                    if (c == ':') {
                        inMeta = true;
                        continue;
                    }
                    data.append(c);
                } else {
                    meta.append(c);
                }
            }
            if (meta.length() == 0) {
                meta.append("0");
            }

            int idata = Integer.parseInt(data.toString());
            byte imeta = Byte.parseByte(meta.toString());
            b.setTypeId(idata);
            b.setData(imeta);

            return new CVoid(line_num, f);
        }

        public Boolean runAsync() {
            return false;
        }
    }

    @api
    public static class set_sign_text implements Function {

        public String getName() {
            return "set_sign_text";
        }

        public Integer[] numArgs() {
            return new Integer[]{2, 3, 4, 5};
        }

        public String docs() {
            return "void {xyzLocation, lineArray | xyzLocation, line1, [line2, [line3, [line4]]]}"
                    + " Sets the text of the sign at the given location. If the block at x,y,z isn't a sign,"
                    + " a RangeException is thrown. If the text on a line overflows 15 characters, it is simply"
                    + " truncated.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.RangeException, ExceptionType.FormatException};
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
            MCLocation l = Static.GetLocation(args[0], environment.GetPlayer() == null ? null : environment.GetPlayer().getWorld(), line_num, f);
            if (l.getBlock().isSign()) {
                String line1 = "";
                String line2 = "";
                String line3 = "";
                String line4 = "";
                if (args.length == 2 && args[1] instanceof CArray) {
                    CArray ca = (CArray) args[1];
                    if (ca.size() >= 1) {
                        line1 = ca.get(0, line_num).val();
                    }
                    if (ca.size() >= 2) {
                        line2 = ca.get(1, line_num).val();
                    }
                    if (ca.size() >= 3) {
                        line3 = ca.get(2, line_num).val();
                    }
                    if (ca.size() >= 4) {
                        line4 = ca.get(3, line_num).val();
                    }

                } else {
                    if (args.length >= 2) {
                        line1 = args[1].val();
                    }
                    if (args.length >= 3) {
                        line2 = args[2].val();
                    }
                    if (args.length >= 4) {
                        line3 = args[3].val();
                    }
                    if (args.length >= 5) {
                        line4 = args[4].val();
                    }
                }
                /**
                 * TODO: Remove this bukkit reference
                 */                
                MCSign s = l.getBlock().getSign();
                s.setLine(0, line1);
                s.setLine(1, line2);
                s.setLine(2, line3);
                s.setLine(3, line4);
                return new CVoid(line_num, f);
            } else {
                throw new ConfigRuntimeException("The block at the specified location is not a sign", ExceptionType.RangeException, line_num, f);
            }
        }
    }

    @api
    public static class get_sign_text implements Function {

        public String getName() {
            return "get_sign_text";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "array {xyzLocation} Given a location array, returns an array of 4 strings of the text in the sign at that"
                    + " location. If the location given isn't a sign, then a RangeException is thrown.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.RangeException, ExceptionType.FormatException};
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
            MCLocation l = Static.GetLocation(args[0], environment.GetPlayer() == null ? null : environment.GetPlayer().getWorld(), line_num, f);
            if (l.getBlock().isSign()) {
                MCSign s = l.getBlock().getSign();
                CString line1 = new CString(s.getLine(0), line_num, f);
                CString line2 = new CString(s.getLine(1), line_num, f);
                CString line3 = new CString(s.getLine(2), line_num, f);
                CString line4 = new CString(s.getLine(3), line_num, f);
                return new CArray(line_num, f, line1, line2, line3, line4);
            } else {
                throw new ConfigRuntimeException("The block at the specified location is not a sign", ExceptionType.RangeException, line_num, f);
            }
        }
    }

    @api
    public static class is_sign_at implements Function {

        public String getName() {
            return "is_sign_at";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "boolean {xyzLocation} Returns true if the block at this location is a sign.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.FormatException};
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
            MCLocation l = Static.GetLocation(args[0], environment.GetPlayer() == null ? null : environment.GetPlayer().getWorld(), line_num, f);
            return new CBoolean(l.getBlock().getType().equals(org.bukkit.Material.SIGN_POST)
                    || l.getBlock().getType().equals(org.bukkit.Material.WALL_SIGN), line_num, f);
        }
    }

    @api
    public static class break_block implements Function {

        public String getName() {
            return "break_block";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "void {locationObject} Mostly simulates a block break at a location. (Does not trigger an event)";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.FormatException};
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
            MCLocation l;
            MCPlayer p;
            p = environment.GetPlayer();
            MCWorld w = (p != null ? p.getWorld() : null);
            l = Static.GetLocation(args[0], w, line_num, f);
            if (l.getWorld() instanceof CraftWorld) {
                CraftWorld cw = (CraftWorld) l.getWorld();
                net.minecraft.server.Block.byId[l.getBlock().getTypeId()].dropNaturally(cw.getHandle(), l.getBlockX(), l.getBlockY(), l.getBlockZ(), l.getBlock().getData(), 1.0f, 0);
            }
            l.getBlock().setTypeId(0);
            CraftServer cs = (CraftServer)((BukkitMCServer)Static.getServer()).__Server();
            cs.getHandle().a(new Packet0KeepAlive(), 0);
            return new CVoid(line_num, f);
            
        }
    }
}
