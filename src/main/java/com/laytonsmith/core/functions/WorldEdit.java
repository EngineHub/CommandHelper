/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.bukkit.BukkitMCLocation;
import com.laytonsmith.abstraction.bukkit.BukkitMCPlayer;
import com.laytonsmith.abstraction.bukkit.BukkitMCWorld;
import com.laytonsmith.core.*;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.CuboidRegionSelector;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.GlobalRegionManager;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

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

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            MCPlayer m = null;
            MCLocation l = null;
            boolean setter = false;
            Static.checkPlugin("WorldEdit", t);
            
            if(env.GetCommandSender() instanceof MCPlayer){
                m = env.GetPlayer();
            }
            if(args.length == 2){
                m = Static.GetPlayer(args[0].val(), t);
                l = ObjectGenerator.GetGenerator().location(args[1], m.getWorld(), t);
                setter = true;
            } else if(args.length == 1){
                if(args[0] instanceof CArray){
                    l = ObjectGenerator.GetGenerator().location(args[0], (m==null?null:m.getWorld()), t);
                    setter = true;
                } else {
                    m = Static.GetPlayer(args[0].val(), t);
                }
            }

            if (m == null) {
                throw new ConfigRuntimeException(this.getName() + " needs a player", ExceptionType.PlayerOfflineException, t);
            }

            RegionSelector sel = Static.getWorldEditPlugin(t).getSession(((BukkitMCPlayer)m)._Player()).getRegionSelector(BukkitUtil.getLocalWorld(((BukkitMCWorld)m.getWorld()).__World()));
            if (!(sel instanceof CuboidRegionSelector)) {
                throw new ConfigRuntimeException("Only cuboid regions are supported with " + this.getName(), ExceptionType.PluginInternalException, t);
            }
            if(setter){
                sel.selectPrimary(BukkitUtil.toVector(((BukkitMCLocation)l)._Location()));
                return new CVoid(t);
            } else {
                Vector pt = ((CuboidRegion) sel.getIncompleteRegion()).getPos1();
                if (pt == null) throw new ConfigRuntimeException("Point in " + this.getName() +  "undefined", t);
                return new CArray(t,
                        new CInt(pt.getBlockX(), t),
                        new CInt(pt.getBlockY(), t),
                        new CInt(pt.getBlockZ(), t),
                        new CString(m.getWorld().getName(), t));
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

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            MCPlayer m = null;
            MCLocation l = null;
            boolean setter = false;
            Static.checkPlugin("WorldEdit", t);

            if (env.GetCommandSender() instanceof MCPlayer) {
                m = env.GetPlayer();
            }
            if (args.length == 2){
                m = Static.GetPlayer(args[0].val(), t);
                l = ObjectGenerator.GetGenerator().location(args[1], m.getWorld(), t);
                setter = true;
            } else if (args.length == 1){
                if (args[0] instanceof CArray) {
                    l = ObjectGenerator.GetGenerator().location(args[0], (m==null?null:m.getWorld()), t);
                    setter = true;
                } else {
                    m = Static.GetPlayer(args[0].val(), t);
                }
            }

            if (m == null) {
                throw new ConfigRuntimeException(this.getName() + " needs a player", ExceptionType.PlayerOfflineException, t);
            }

            RegionSelector sel = Static.getWorldEditPlugin(t).getSession(((BukkitMCPlayer)m)._Player()).getRegionSelector(BukkitUtil.getLocalWorld(((BukkitMCWorld)m.getWorld()).__World()));
            if (!(sel instanceof CuboidRegionSelector)) {
                throw new ConfigRuntimeException("Only cuboid regions are supported with " + this.getName(), ExceptionType.PluginInternalException, t);
            }

            if(setter){
                sel.selectSecondary(BukkitUtil.toVector(((BukkitMCLocation)l)._Location()));
                return new CVoid(t);
            } else {
                Vector pt = ((CuboidRegion)sel.getIncompleteRegion()).getPos2();
                if (pt == null) throw new ConfigRuntimeException("Point in " + this.getName() +  "undefined", t);
                return new CArray(t,
                        new CInt(pt.getBlockX(), t),
                        new CInt(pt.getBlockY(), t),
                        new CInt(pt.getBlockZ(), t),
                        new CString(m.getWorld().getName(), t));
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
//        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
//            Static.checkPlugin("WorldEdit", t);
//            return new CVoid(t);
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

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
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
                if (world == null) throw new ConfigRuntimeException("Unknown world specified", ExceptionType.PluginInternalException, t);
                RegionManager mgr = Static.getWorldGuardPlugin(t).getGlobalRegionManager().get(world);
                ProtectedRegion region = mgr.getRegion(regionName);
                if (region == null) throw new ConfigRuntimeException("Region could not be found!", ExceptionType.PluginInternalException, t);
                
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
                
                
                CArray ret = new CArray(t);
                
                CArray pointSet = new CArray(t);
                for(Location l : points){
                    CArray point = new CArray(t);
                    point.push(new CInt(l.getBlockX(), t));
                    point.push(new CInt(l.getBlockY(), t));
                    point.push(new CInt(l.getBlockZ(), t));
                    point.push(new CString(l.getWorld().getName(), t));
                    pointSet.push(point);
                }
                CArray ownerSet = new CArray(t);
                for(String owner : owners){
                    ownerSet.push(new CString(owner, t));
                }
                CArray memberSet = new CArray(t);
                for(String member : members){
                    memberSet.push(new CString(member, t));
                }
                CArray flagSet = new CArray(t);
                for(Map.Entry<String, String> flag : flags.entrySet()){
                    CArray fl = new CArray(t, 
                            new CString(flag.getKey(), t), 
                            new CString(flag.getValue(), t)
                    );
                    flagSet.push(fl);
                }
                ret.push(pointSet);
                ret.push(ownerSet);
                ret.push(memberSet);
                ret.push(flagSet);
                ret.push(new CInt(priority, t));
                ret.push(new CDouble(volume, t));
                return ret;
                
            } catch (NoClassDefFoundError e) {
                throw new ConfigRuntimeException("It does not appear as though the WorldEdit or WorldGuard plugin is loaded properly. Execution of " + this.getName() + " cannot continue.", ExceptionType.InvalidPluginException, t, e);
            }
        }
    }

    @api public static class sk_region_overlaps extends SKFunction {

        public String getName() {
            return "sk_region_overlaps";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public String docs() {
            return "boolean {world, region1, array(region2, [regionN...])} Returns true or false whether or not the specified regions overlap.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PluginInternalException};
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            String region1 = args[1].val();
            List<ProtectedRegion> checkRegions = new ArrayList<ProtectedRegion>();
            Static.checkPlugin("WorldGuard", t);
            World world = Bukkit.getServer().getWorld(args[0].val());
            if (world == null) throw new ConfigRuntimeException("Unknown world specified", ExceptionType.PluginInternalException, t);
            RegionManager mgr = Static.getWorldGuardPlugin(t).getGlobalRegionManager().get(world);
            if (args[2] instanceof CArray) {
                CArray arg = (CArray)args[2];
                for (int i = 0; i < arg.size(); i++) {
                    ProtectedRegion region = mgr.getRegion(arg.get(i, t).val());
                    if (region == null) throw new ConfigRuntimeException("Region " + arg.get(i, t).val() + " could not be found!", ExceptionType.PluginInternalException, t);
                    checkRegions.add(region);
                }
            } else {
                ProtectedRegion region = mgr.getRegion(args[2].val());
                    if (region == null) throw new ConfigRuntimeException("Region " + args[2] + " could not be found!", ExceptionType.PluginInternalException, t);
                    checkRegions.add(region);
            }
            
            ProtectedRegion region = mgr.getRegion(region1);
            if (region == null) throw new ConfigRuntimeException("Region could not be found!", ExceptionType.PluginInternalException, t);
            
            try {
                if (!region.getIntersectingRegions(checkRegions).isEmpty()) return new CBoolean(true, t);
            } catch (Exception e) {}
            return new CBoolean(false, t);
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

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Static.checkPlugin("WorldGuard", t);
            List<World> checkWorlds = null;
            CArray arr = new CArray(t);
            if (args.length == 1) {
                World world = Bukkit.getServer().getWorld(args[0].val());
                if (world != null) checkWorlds = Arrays.asList(world);
            }
            if (checkWorlds == null) {
                checkWorlds = Bukkit.getServer().getWorlds();
            }
            GlobalRegionManager mgr = Static.getWorldGuardPlugin(t).getGlobalRegionManager();
            for (World world : checkWorlds) {
                for (String region : mgr.get(world).getRegions().keySet()) arr.push(new CString(region, t));
            }
            return arr;
            
        }
    }
    
        
    @api public static class sk_current_regions extends SKFunction {
        public String getName() {
            return "sk_current_regions";
        }
        
        public Integer[] numArgs() {
            return new Integer[]{0, 1};
        }
        
        public String docs() {
            return "mixed {[player]} Returns the list regions that player is in. If no player specified, then the current player is used."
                    + " If region is found, an array of region names are returned, else an empty is returned";
        }
        
        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.PluginInternalException};
        }
        
        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Static.checkPlugin("WorldGuard", t);
            World world;
            
            MCPlayer m = null;
            
            if(env.GetCommandSender() instanceof MCPlayer){
                m = env.GetPlayer();
            }
            if (args.length == 1) {
                m = Static.GetPlayer(args[0].val(), t);
            }
            
            if (m == null) {
                throw new ConfigRuntimeException(this.getName() + " needs a player", ExceptionType.PlayerOfflineException, t);
            }
            
            world = Bukkit.getServer().getWorld(m.getWorld().getName());
            
            RegionManager mgr = Static.getWorldGuardPlugin(t).getGlobalRegionManager().get(world);
            Vector pt = new Vector(m.getLocation().getBlockX(), m.getLocation().getBlockY(), m.getLocation().getBlockZ());
            ApplicableRegionSet set = mgr.getApplicableRegions(pt);
            
            CArray regions = new CArray(t);
            
            List<ProtectedRegion> sortedRegions = new ArrayList<ProtectedRegion>();
            
            for (ProtectedRegion r : set) {
                boolean placed = false;
                for (int i = 0; i < sortedRegions.size(); i++) {
                    if (sortedRegions.get(i).volume() < r.volume()) {
                        sortedRegions.add(i, r);
                        placed = true;
                        break;
                    }
                }
                if (!placed) {
                    sortedRegions.add(r);
                }
            }
            
            for (ProtectedRegion region : sortedRegions) {
                regions.push(new CString(region.getId(), t));
            }
            
            if (regions.size() > 0) {
                return regions;
            }
            
            return new CArray(t);
        }
    }

    @api public static class sk_regions_at extends SKFunction {
        public String getName() {
            return "sk_regions_at";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "mixed {Locationarray} Returns a list of regions at the specified location. "
                    + "If regions are found, an array of region names are returned, otherwise, an empty array is returned.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.PluginInternalException};
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Static.checkPlugin("WorldGuard", t);
            World world;
            MCPlayer m = null;

            if (!(args[0] instanceof CArray)) {
                throw new ConfigRuntimeException(this.getName() + " needs a locationarray", ExceptionType.CastException, t);
            }

            if (!(env.GetCommandSender() instanceof MCPlayer)) {
                throw new ConfigRuntimeException(this.getName() + " needs a player", ExceptionType.PlayerOfflineException, t);
            }

            m = env.GetPlayer();
            MCWorld w = m.getWorld();
            MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], w, t);

            world = Bukkit.getServer().getWorld(w.getName());

            RegionManager mgr = Static.getWorldGuardPlugin(t).getGlobalRegionManager().get(world);
            Vector pt = new Vector(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            ApplicableRegionSet set = mgr.getApplicableRegions(pt);

            CArray regions = new CArray(t);

            List<ProtectedRegion> sortedRegions = new ArrayList<ProtectedRegion>();

            for (ProtectedRegion r : set) {
                boolean placed = false;
                for (int i = 0; i < sortedRegions.size(); i++) {
                    if (sortedRegions.get(i).volume() < r.volume()) {
                        sortedRegions.add(i, r);
                        placed = true;
                        break;
                    }
                }
                if (!placed) {
                    sortedRegions.add(r);
                }
            }

            for (ProtectedRegion region : sortedRegions) {
                regions.push(new CString(region.getId(), t));
            }

            if (regions.size() > 0) {
                return regions;
            }

            return new CArray(t);
        }
    }
    
    @api public static class sk_region_volume extends SKFunction {
        public String getName() {
            return "sk_region_volume";
        }
        
        public Integer[] numArgs() {
            return new Integer[]{2};
        }
        
        public String docs() {
            return "int {region, world} Returns the volume of the given region in the given world.";
        }
        
        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PluginInternalException};
        }
        
        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Static.checkPlugin("WorldGuard", t);
            World world;
            
            world = Bukkit.getServer().getWorld(args[1].val());
            
            RegionManager mgr = Static.getWorldGuardPlugin(t).getGlobalRegionManager().get(world);
            
            ProtectedRegion region = mgr.getRegion(args[0].val());
            
            if (region == null) {
                throw new ConfigRuntimeException(String.format("The region (%s) does not exist in world (%s).", args[0].val(), args[1].val()), ExceptionType.PluginInternalException, t);
            }
            
            return new CInt(region.volume(), t);
        }
    }

    public static abstract class SKFunction extends AbstractFunction {

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_2_0;
        }

        public Boolean runAsync() {
            return false;
        }
    }
}
