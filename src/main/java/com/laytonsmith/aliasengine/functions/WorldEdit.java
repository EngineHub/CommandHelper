/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.api;
import com.laytonsmith.aliasengine.Constructs.*;
import com.laytonsmith.aliasengine.exceptions.CancelCommandException;
import com.laytonsmith.aliasengine.exceptions.ConfigRuntimeException;
import com.laytonsmith.aliasengine.Static;
import com.laytonsmith.aliasengine.functions.Exceptions.ExceptionType;
import java.io.File;

import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.CuboidRegionSelector;
import com.sk89q.worldedit.regions.RegionSelector;

import java.util.*;

import com.sk89q.worldguard.protection.GlobalRegionManager;
import com.sk89q.worldguard.protection.UnsupportedIntersectionException;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Layton
 */
public class WorldEdit {

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

            RegionSelector sel = Static.getWorldEditPlugin().getSession(m).getRegionSelector(BukkitUtil.getLocalWorld(m.getWorld()));
            if (!(sel instanceof CuboidRegionSelector)) {
                throw new ConfigRuntimeException("Only cuboid regions are supported with " + this.getName(), ExceptionType.PluginInternalException, line_num, f);
            }
            if(setter){
                sel.selectPrimary(BukkitUtil.toVector(l));
                return new CVoid(line_num, f);
            } else {
                Vector pt = ((CuboidRegion) sel.getIncompleteRegion()).getPos1();
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

            RegionSelector sel = Static.getWorldEditPlugin().getSession(m).getRegionSelector(BukkitUtil.getLocalWorld(m.getWorld()));
            if (!(sel instanceof CuboidRegionSelector)) {
                throw new ConfigRuntimeException("Only cuboid regions are supported with " + this.getName(), ExceptionType.PluginInternalException, line_num, f);
            }

            if(setter){
                sel.selectSecondary(BukkitUtil.toVector(l));
                return new CVoid(line_num, f);
            } else {
                Vector pt = ((CuboidRegion)sel.getIncompleteRegion()).getPos2();
                if (pt == null) throw new ConfigRuntimeException("Point in " + this.getName() +  "undefined", line_num, f);
                return new CArray(line_num, f,
                        new CInt(pt.getBlockX(), line_num, f),
                        new CInt(pt.getBlockY(), line_num, f),
                        new CInt(pt.getBlockZ(), line_num, f),
                        new CString(m.getWorld().getName(), line_num, f));
            }
        }
    }

//    public static class sk_points extends SKFunction {
//
//        public String getName() {
//            return "sk_points";
//        }
//
//        public Integer[] numArgs() {
//            return new Integer[]{0, 1, 2};
//        }
//
//        public String docs() {
//            return "mixed {[player], arrayOfArrays | [player]} Sets a series of points, or returns the poly selection for this player, if one is specified."
//                    + " The array should be an array of arrays, and the arrays should be array(x, y, z)";
//        }
//
//        public ExceptionType[] thrown() {
//            return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.CastException};
//        }
//
//        public Construct exec(int line_num, File f, CommandSender p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
//            Static.checkPlugin("WorldEdit", line_num, f);
//            return new CVoid(line_num, f);
//        }
//    }

    @api public static class sk_region_info extends SKFunction {

        public String getName() {
            return "sk_region_info";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            return "array {region, world} Given a region name, returns an array of information about that region, as follows:<ul>"
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
                String regionName = args[0].val();
                String worldName = args[1].val();
                //Fill these data structures in with the information we need
                List<Location> points = new ArrayList<Location>();
                List<String> owners = new ArrayList<String>();
                List<String> members = new ArrayList<String>();
                Map<String, String> flags = new HashMap<String, String>();
                int priority = -1;
                float volume = -1;
                World world = Bukkit.getServer().getWorld(worldName);
                if (world == null) throw new ConfigRuntimeException("Unknown world specified", ExceptionType.PluginInternalException, line_num, f);
                RegionManager mgr = Static.getWorldGuardPlugin().getGlobalRegionManager().get(world);
                ProtectedRegion region = mgr.getRegion(regionName);
                if (region == null) throw new ConfigRuntimeException("Region could not be found!", ExceptionType.PluginInternalException, line_num, f);
                
                owners.addAll(region.getOwners().getPlayers());
                members.addAll(region.getMembers().getPlayers());
                for (Map.Entry<Flag<?>, Object> ent: region.getFlags().entrySet()) {
                    flags.put(ent.getKey().getName(), String.valueOf(ent.getValue()));
                }
                priority = region.getPriority();
                volume = region.volume();
                boolean first = true;
                if (region instanceof ProtectedPolygonalRegion) {
                    for (BlockVector2D pt : ((ProtectedPolygonalRegion) region).getPoints()) {
                        points.add(new Location(world, pt.getX(), first ? region.getMaximumPoint().getY() 
                                    : region.getMinimumPoint().getY(), pt.getZ()));
                        first = false;
                    }
                } else {
                    points.add(com.sk89q.worldguard.bukkit.BukkitUtil.toLocation(world, region.getMaximumPoint()));
                    points.add(com.sk89q.worldguard.bukkit.BukkitUtil.toLocation(world, region.getMinimumPoint()));
                }
                
                
                CArray ret = new CArray(line_num, f);
                
                CArray pointSet = new CArray(line_num, f);
                for(Location l : points){
                    CArray point = new CArray(line_num, f);
                    point.push(new CInt(l.getBlockX(), line_num, f));
                    point.push(new CInt(l.getBlockY(), line_num, f));
                    point.push(new CInt(l.getBlockZ(), line_num, f));
                    point.push(new CString(l.getWorld().getName(), line_num, f));
                    pointSet.push(point);
                }
                CArray ownerSet = new CArray(line_num, f);
                for(String owner : owners){
                    ownerSet.push(new CString(owner, line_num, f));
                }
                CArray memberSet = new CArray(line_num, f);
                for(String member : members){
                    memberSet.push(new CString(member, line_num, f));
                }
                CArray flagSet = new CArray(line_num, f);
                for(Map.Entry<String, String> flag : flags.entrySet()){
                    CArray fl = new CArray(line_num, f, 
                            new CString(flag.getKey(), line_num, f), 
                            new CString(flag.getValue(), line_num, f)
                    );
                    flagSet.push(fl);
                }
                ret.push(pointSet);
                ret.push(ownerSet);
                ret.push(memberSet);
                ret.push(flagSet);
                ret.push(new CInt(priority, line_num, f));
                ret.push(new CDouble(volume, line_num, f));
                return ret;
                
            } catch (NoClassDefFoundError e) {
                throw new ConfigRuntimeException("It does not appear as though the WorldEdit or WorldGuard plugin is loaded properly. Execution of " + this.getName() + " cannot continue.", ExceptionType.InvalidPluginException, line_num, f);
            }
        }
    }

    @api public static class sk_region_overlaps extends SKFunction {

        public String getName() {
            return "sk_region_overlaps";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE}; //@wraithguard01
        }

        public String docs() {
            return "boolean {world, region1, array(region2, [regionN...])} Returns true or false whether or not the specified regions overlap.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PluginInternalException};
        }

        public Construct exec(int line_num, File f, CommandSender p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            String region1 = args[1].val();
            List<ProtectedRegion> checkRegions = new ArrayList<ProtectedRegion>();
            Static.checkPlugin("WorldGuard", line_num, f);
            World world = Bukkit.getServer().getWorld(args[0].val());
            if (world == null) throw new ConfigRuntimeException("Unknown world specified", ExceptionType.PluginInternalException, line_num, f);
            RegionManager mgr = Static.getWorldGuardPlugin().getGlobalRegionManager().get(world);
            if (args[2] instanceof CArray) {
                CArray arg = (CArray)args[2];
                for (int i = 0; i < arg.size(); i++) {
                    ProtectedRegion region = mgr.getRegion(arg.get(i, line_num).val());
                    if (region == null) throw new ConfigRuntimeException("Region " + arg.get(i, line_num).val() + " could not be found!", ExceptionType.PluginInternalException, line_num, f);
                    checkRegions.add(region);
                }
            } else {
                ProtectedRegion region = mgr.getRegion(args[2].val());
                    if (region == null) throw new ConfigRuntimeException("Region " + args[2] + " could not be found!", ExceptionType.PluginInternalException, line_num, f);
                    checkRegions.add(region);
            }
            
            ProtectedRegion region = mgr.getRegion(region1);
            if (region == null) throw new ConfigRuntimeException("Region could not be found!", ExceptionType.PluginInternalException, line_num, f);
            
            try {
                if (!region.getIntersectingRegions(checkRegions).isEmpty()) return new CBoolean(true, line_num, f);
            } catch (UnsupportedIntersectionException e) {}
            return new CBoolean(false, line_num, f);
        }
    }

    @api public static class sk_all_regions extends SKFunction {

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
            Static.checkPlugin("WorldGuard", line_num, f);
            List<World> checkWorlds = null;
            CArray arr = new CArray(line_num, f);
            if (args.length == 1) {
                World world = Bukkit.getServer().getWorld(args[0].val());
                if (world != null) checkWorlds = Arrays.asList(world);
            }
            if (checkWorlds == null) {
                checkWorlds = Bukkit.getServer().getWorlds();
            }
            GlobalRegionManager mgr = Static.getWorldGuardPlugin().getGlobalRegionManager();
            for (World world : checkWorlds) {
                for (String region : mgr.get(world).getRegions().keySet()) arr.push(new CString(region, line_num, f));
            }
            return arr;
            
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
