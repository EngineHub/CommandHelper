/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.Constructs.CArray;
import com.laytonsmith.aliasengine.Constructs.CInt;
import com.laytonsmith.aliasengine.Constructs.CString;
import com.laytonsmith.aliasengine.Constructs.CVoid;
import com.laytonsmith.aliasengine.functions.exceptions.CancelCommandException;
import com.laytonsmith.aliasengine.functions.exceptions.ConfigRuntimeException;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.Static;
import com.laytonsmith.aliasengine.functions.Exceptions.ExceptionType;
import java.io.File;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Layton
 */
public class WorldEdit_ {

    public static String docs() {
        return "Provides various methods for programmatically hooking into WorldEdit";
    }

    @api public static class sk_pos1 extends SKFunction {

        public String getName() {
            return "sk_pos1";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1, 2};
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.CastException};
        }

        public String docs() {
            return "mixed {[player], locationArray | [player]} Sets the player's point 1, or returns it if the array to set isn't specified. If"
                    + " the location is returned, it is returned as a 4 index array:(x, y, z, world)";
        }

        public Construct exec(int line_num, File f, CommandSender p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Player m = null;
            Location l = null;
            boolean setter = false;
            Static.checkPlugin("WorldEdit", line_num, f);
            
            if(p instanceof Player){
                m = (Player)p;
            }
            if(args.length == 2){
                m = Static.GetPlayer(args[0].val(), line_num, f);
                l = Static.GetLocation(args[1], m.getWorld(), line_num, f);
                setter = true;
            } else if(args.length == 1){
                if(args[0] instanceof CArray){
                    l = Static.GetLocation(args[0], (m==null?null:m.getWorld()), line_num, f);
                    setter = true;
                } else {
                    m = Static.GetPlayer(args[0].val(), line_num, f);
                }
            }

            if (m == null) {
                throw new ConfigRuntimeException(this.getName() + " needs a player", ExceptionType.PlayerOfflineException, line_num, f);
            }

            if(setter){
                Region sel = Static.getWorldEditPlugin().getSession(m).getRegionSelector(BukkitUtil.getLocalWorld(l.getWorld())).getIncompleteRegion();
                if (!(sel instanceof CuboidRegion)) {
                    throw new ConfigRuntimeException("Only cuboid regions are supported with " + this.getName(), ExceptionType.PluginInternalException, line_num, f);
                }
                ((CuboidRegion) sel).setPos1(BukkitUtil.toVector(l));
                return new CVoid(line_num, f);
            } else {
                Region sel = Static.getWorldEditPlugin().getSession(m).getRegionSelector(BukkitUtil.getLocalWorld(m.getWorld())).getIncompleteRegion();
                if (!(sel instanceof CuboidRegion)) {
                    throw new ConfigRuntimeException("Only cuboid regions are supported with " + this.getName(), ExceptionType.PluginInternalException, line_num, f);
                }
                Vector pt = ((CuboidRegion) sel).getPos1();
                if (pt == null) throw new ConfigRuntimeException("Point in " + this.getName() +  "undefined", line_num, f);
                return new CArray(line_num, f,
                        new CInt(pt.getBlockX(), line_num, f),
                        new CInt(pt.getBlockY(), line_num, f),
                        new CInt(pt.getBlockZ(), line_num, f),
                        new CString(m.getWorld().getName(), line_num, f));
            }
        }
    }

    @api public static class sk_pos2 extends SKFunction {

        public String getName() {
            return "sk_pos2";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1, 2};
        }

        public String docs() {
            return "mixed {[player], array | [player]} Sets the player's point 2, or returns it if the array to set isn't specified";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.CastException};
        }

        public Construct exec(int line_num, File f, CommandSender p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Player m = null;
            Location l = null;
            boolean setter = false;
            Static.checkPlugin("WorldEdit", line_num, f);

            if (p instanceof Player) {
                m = (Player)p;
            }
            if (args.length == 2){
                m = Static.GetPlayer(args[0].val(), line_num, f);
                l = Static.GetLocation(args[1], m.getWorld(), line_num, f);
                setter = true;
            } else if (args.length == 1){
                if (args[0] instanceof CArray) {
                    l = Static.GetLocation(args[0], (m==null?null:m.getWorld()), line_num, f);
                    setter = true;
                } else {
                    m = Static.GetPlayer(args[0].val(), line_num, f);
                }
            }

            if (m == null) {
                throw new ConfigRuntimeException(this.getName() + " needs a player", ExceptionType.PlayerOfflineException, line_num, f);
            }

            if(setter){
                Region sel = Static.getWorldEditPlugin().getSession(m).getRegionSelector(BukkitUtil.getLocalWorld(l.getWorld())).getIncompleteRegion();
                if (!(sel instanceof CuboidRegion)) {
                    throw new ConfigRuntimeException("Only cuboid regions are supported with " + this.getName(), ExceptionType.PluginInternalException, line_num, f);
                }
                ((CuboidRegion) sel).setPos2(BukkitUtil.toVector(l));
                return new CVoid(line_num, f);
            } else {
                Region sel = Static.getWorldEditPlugin().getSession(m).getRegionSelector(BukkitUtil.getLocalWorld(m.getWorld())).getIncompleteRegion();
                if (!(sel instanceof CuboidSelection)) {
                    throw new ConfigRuntimeException("Only cuboid regions are supported with " + this.getName(), ExceptionType.PluginInternalException, line_num, f);
                }
                Vector pt = ((CuboidRegion)sel).getPos2();
                if (pt == null) throw new ConfigRuntimeException("Point in " + this.getName() +  "undefined", line_num, f);
                return new CArray(line_num, f,
                        new CInt(pt.getBlockX(), line_num, f),
                        new CInt(pt.getBlockY(), line_num, f),
                        new CInt(pt.getBlockZ(), line_num, f),
                        new CString(m.getWorld().getName(), line_num, f));
            }
        }
    }

    public static class sk_points extends SKFunction {

        public String getName() {
            return "sk_points";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1, 2};
        }

        public String docs() {
            return "mixed {[player], arrayOfArrays | [player]} Sets a series of points, or returns the poly selection for this player, if one is specified."
                    + " The array should be an array of arrays, and the arrays should be array(x, y, z)";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.CastException};
        }

        public Construct exec(int line_num, File f, CommandSender p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            try {
                return null;
            } catch (NoClassDefFoundError e) {
                throw new ConfigRuntimeException("It does not appear as though the WorldEdit or WorldGuard plugin is loaded properly. Execution of " + this.getName() + " cannot continue.", ExceptionType.InvalidPluginException, line_num, f);
            }
        }
    }

    public static class sk_region_info extends SKFunction {

        public String getName() {
            return "sk_region_info";
        }

        public Integer[] numArgs() {
            return new Integer[]{};
        }

        public String docs() {
            return "array {region} Given a region name, returns an array of information about that region, as follows:<ul>"
                    + " <li>0 - An array of points that define this region</li>"
                    + " <li>1 - An array of owners of this region</li>"
                    + " <li>2 - An array of members of this region</li>"
                    + " <li>3 - An array of arrays of this region's flags, where each array is: array(flag_name, value)</li>"
                    + " <li>4 - This region's priority</li>"
                    + " <li>5 - The volume of this region (in meters cubed)</li>"
                    + "</ul>"
                    + "If the region cannot be found, a PluginInternalException is thrown.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PluginInternalException};
        }

        public Construct exec(int line_num, File f, CommandSender p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            try {
                return null;
            } catch (NoClassDefFoundError e) {
                throw new ConfigRuntimeException("It does not appear as though the WorldEdit or WorldGuard plugin is loaded properly. Execution of " + this.getName() + " cannot continue.", ExceptionType.InvalidPluginException, line_num, f);
            }
        }
    }

    public static class sk_region_overlaps extends SKFunction {

        public String getName() {
            return "sk_region_overlaps";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public String docs() {
            return "boolean {region1, region2, [regionN...]} Returns true or false whether or not the specified regions overlap.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PluginInternalException};
        }

        public Construct exec(int line_num, File f, CommandSender p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            try {
                return null;
            } catch (NoClassDefFoundError e) {
                throw new ConfigRuntimeException("It does not appear as though the WorldEdit or WorldGuard plugin is loaded properly. Execution of " + this.getName() + " cannot continue.", ExceptionType.InvalidPluginException, line_num, f);
            }
        }
    }

    public static class sk_all_regions extends SKFunction {

        public String getName() {
            return "sk_all_regions";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1};
        }

        public String docs() {
            return "array {[world]} Returns all the regions in all worlds, or just the one world, if specified.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{};
        }

        public Construct exec(int line_num, File f, CommandSender p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            try {
                return null;
            } catch (NoClassDefFoundError e) {
                throw new ConfigRuntimeException("It does not appear as though the WorldEdit or WorldGuard plugin is loaded properly. Execution of " + this.getName() + " cannot continue.", ExceptionType.InvalidPluginException, line_num, f);
            }
        }
    }

    public static abstract class SKFunction implements Function {

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.2.0";
        }

        public Boolean runAsync() {
            return false;
        }
    }
}
