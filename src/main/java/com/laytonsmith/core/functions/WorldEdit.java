package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCOfflinePlayer;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.bukkit.BukkitMCCommandSender;
import com.laytonsmith.abstraction.bukkit.BukkitMCLocation;
import com.laytonsmith.abstraction.bukkit.BukkitMCPlayer;
import com.laytonsmith.abstraction.bukkit.BukkitMCWorld;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.*;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.CuboidRegionSelector;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.GlobalRegionManager;
import com.sk89q.worldguard.protection.databases.RegionDBUtil;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.RegionGroupFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Layton
 */
public class WorldEdit {

    public static String docs() {
        return "Provides various methods for programmatically hooking into WorldEdit and WorldGuard";
    }

    @api(environments=CommandHelperEnvironment.class)
    public static class sk_pos1 extends SKFunction {

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

        public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            MCPlayer m = null;
            MCLocation l = null;
            boolean setter = false;
            Static.checkPlugin("WorldEdit", t);

            if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
                m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
            }
            if (args.length == 2) {
                m = Static.GetPlayer(args[0].val(), t);
                l = ObjectGenerator.GetGenerator().location(args[1], m.getWorld(), t);
                setter = true;
            } else if (args.length == 1) {
                if (args[0] instanceof CArray) {
                    l = ObjectGenerator.GetGenerator().location(args[0], ( m == null ? null : m.getWorld() ), t);
                    setter = true;
                } else {
                    m = Static.GetPlayer(args[0].val(), t);
                }
            }

            if (m == null) {
                throw new ConfigRuntimeException(this.getName() + " needs a player", ExceptionType.PlayerOfflineException, t);
            }

            RegionSelector sel = Static.getWorldEditPlugin(t).getSession(( (BukkitMCPlayer) m )._Player()).getRegionSelector(BukkitUtil.getLocalWorld(( (BukkitMCWorld) m.getWorld() ).__World()));
            if (!( sel instanceof CuboidRegionSelector )) {
                throw new ConfigRuntimeException("Only cuboid regions are supported with " + this.getName(), ExceptionType.PluginInternalException, t);
            }
            if (setter) {
                sel.selectPrimary(BukkitUtil.toVector(( (BukkitMCLocation) l )._Location()));
                return new CVoid(t);
            } else {
                Vector pt = ( (CuboidRegion) sel.getIncompleteRegion() ).getPos1();
                if (pt == null) {
                    throw new ConfigRuntimeException("Point in " + this.getName() + "undefined", t);
                }
                return new CArray(t,
                        new CInt(pt.getBlockX(), t),
                        new CInt(pt.getBlockY(), t),
                        new CInt(pt.getBlockZ(), t),
                        new CString(m.getWorld().getName(), t));
            }
        }
    }

    @api(environments=CommandHelperEnvironment.class)
    public static class sk_pos2 extends SKFunction {

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

        public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            MCPlayer m = null;
            MCLocation l = null;
            boolean setter = false;
            Static.checkPlugin("WorldEdit", t);

            if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
                m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
            }
            if (args.length == 2) {
                m = Static.GetPlayer(args[0].val(), t);
                l = ObjectGenerator.GetGenerator().location(args[1], m.getWorld(), t);
                setter = true;
            } else if (args.length == 1) {
                if (args[0] instanceof CArray) {
                    l = ObjectGenerator.GetGenerator().location(args[0], ( m == null ? null : m.getWorld() ), t);
                    setter = true;
                } else {
                    m = Static.GetPlayer(args[0].val(), t);
                }
            }

            if (m == null) {
                throw new ConfigRuntimeException(this.getName() + " needs a player", ExceptionType.PlayerOfflineException, t);
            }

            RegionSelector sel = Static.getWorldEditPlugin(t).getSession(( (BukkitMCPlayer) m )._Player()).getRegionSelector(BukkitUtil.getLocalWorld(( (BukkitMCWorld) m.getWorld() ).__World()));
            if (!( sel instanceof CuboidRegionSelector )) {
                throw new ConfigRuntimeException("Only cuboid regions are supported with " + this.getName(), ExceptionType.PluginInternalException, t);
            }

            if (setter) {
                sel.selectSecondary(BukkitUtil.toVector(( (BukkitMCLocation) l )._Location()));
                return new CVoid(t);
            } else {
                Vector pt = ( (CuboidRegion) sel.getIncompleteRegion() ).getPos2();
                if (pt == null) {
                    throw new ConfigRuntimeException("Point in " + this.getName() + "undefined", t);
                }
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
//        public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
//            Static.checkPlugin("WorldEdit", t);
//            return new CVoid(t);
//        }
//    }
    @api
    public static class sk_region_info extends SKFunction {

        public String getName() {
            return "sk_region_info";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            return "array {region, world} Given a region name, returns an array of information about that region."
                    + " ---- The following information is returned:<ul>"
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

        public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
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
                if (world == null) {
                    throw new ConfigRuntimeException("Unknown world specified", ExceptionType.PluginInternalException, t);
                }
                RegionManager mgr = Static.getWorldGuardPlugin(t).getGlobalRegionManager().get(world);
                ProtectedRegion region = mgr.getRegion(regionName);
                if (region == null) {
                    throw new ConfigRuntimeException("Region could not be found!", ExceptionType.PluginInternalException, t);
                }

                owners.addAll(region.getOwners().getPlayers());
                members.addAll(region.getMembers().getPlayers());
                for (Map.Entry<Flag<?>, Object> ent : region.getFlags().entrySet()) {
                    flags.put(ent.getKey().getName(), String.valueOf(ent.getValue()));
                }
                priority = region.getPriority();
                volume = region.volume();
                boolean first = true;
                if (region instanceof ProtectedPolygonalRegion) {
                    for (BlockVector2D pt : ( (ProtectedPolygonalRegion) region ).getPoints()) {
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
                for (Location l : points) {
                    CArray point = new CArray(t);
                    point.push(new CInt(l.getBlockX(), t));
                    point.push(new CInt(l.getBlockY(), t));
                    point.push(new CInt(l.getBlockZ(), t));
                    point.push(new CString(l.getWorld().getName(), t));
                    pointSet.push(point);
                }
                CArray ownerSet = new CArray(t);
                for (String owner : owners) {
                    ownerSet.push(new CString(owner, t));
                }
                CArray memberSet = new CArray(t);
                for (String member : members) {
                    memberSet.push(new CString(member, t));
                }
                CArray flagSet = new CArray(t);
                for (Map.Entry<String, String> flag : flags.entrySet()) {
                    CArray fl = new CArray(t,
                            new CString(flag.getKey(), t),
                            new CString(flag.getValue(), t));
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

    @api
    public static class sk_region_overlaps extends SKFunction {

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

        public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            String region1 = args[1].val();
            List<ProtectedRegion> checkRegions = new ArrayList<ProtectedRegion>();
            Static.checkPlugin("WorldGuard", t);
            World world = Bukkit.getServer().getWorld(args[0].val());
            if (world == null) {
                throw new ConfigRuntimeException("Unknown world specified", ExceptionType.PluginInternalException, t);
            }
            RegionManager mgr = Static.getWorldGuardPlugin(t).getGlobalRegionManager().get(world);
            if (args[2] instanceof CArray) {
                CArray arg = (CArray) args[2];
                for (int i = 0; i < arg.size(); i++) {
                    ProtectedRegion region = mgr.getRegion(arg.get(i, t).val());
                    if (region == null) {
                        throw new ConfigRuntimeException("Region " + arg.get(i, t).val() + " could not be found!", ExceptionType.PluginInternalException, t);
                    }
                    checkRegions.add(region);
                }
            } else {
                ProtectedRegion region = mgr.getRegion(args[2].val());
                if (region == null) {
                    throw new ConfigRuntimeException("Region " + args[2] + " could not be found!", ExceptionType.PluginInternalException, t);
                }
                checkRegions.add(region);
            }

            ProtectedRegion region = mgr.getRegion(region1);
            if (region == null) {
                throw new ConfigRuntimeException("Region could not be found!", ExceptionType.PluginInternalException, t);
            }

            try {
                if (!region.getIntersectingRegions(checkRegions).isEmpty()) {
                    return new CBoolean(true, t);
                }
            } catch (Exception e) {
            }
            return new CBoolean(false, t);
        }
    }

    @api
    public static class sk_region_intersect extends SKFunction {

        public String getName() {
            return "sk_region_intersect";
        }

        public Integer[] numArgs() {
            return new Integer[]{2, 3};
        }

        public String docs() {
            return "boolean {world, region1, [array(region2, [regionN...])]} Returns an array of regions names which intersect with defined region."
					+ " You can pass an array of regions to verify or omit this parameter and all regions in selected world will be checked.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PluginInternalException};
        }

        public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            String region1 = args[1].val();
            List<ProtectedRegion> checkRegions = new ArrayList<ProtectedRegion>();
			List<ProtectedRegion> getRegions = new ArrayList<ProtectedRegion>();
			CArray listRegions = new CArray(t);

            Static.checkPlugin("WorldGuard", t);
            World world = Bukkit.getServer().getWorld(args[0].val());
            if (world == null) {
                throw new ConfigRuntimeException("Unknown world specified", ExceptionType.PluginInternalException, t);
            }

            RegionManager mgr = Static.getWorldGuardPlugin(t).getGlobalRegionManager().get(world);

			if (args.length == 2) {
				 checkRegions.addAll(mgr.getRegions().values());
			} else {
				if (args[2] instanceof CArray) {
					CArray arg = (CArray) args[2];
					for (int i = 0; i < arg.size(); i++) {
						ProtectedRegion region = mgr.getRegion(arg.get(i, t).val());
						if (region == null) {
							throw new ConfigRuntimeException(String.format("Region %s could not be found!", arg.get(i, t).val()), ExceptionType.PluginInternalException, t);
						}
						checkRegions.add(region);
					}
				} else {
					ProtectedRegion region = mgr.getRegion(args[2].val());
					if (region == null) {
						throw new ConfigRuntimeException(String.format("Region %s could not be found!", args[2]), ExceptionType.PluginInternalException, t);
					}
					checkRegions.add(region);
				}
			}

            ProtectedRegion region = mgr.getRegion(region1);
            if (region == null) {
                throw new ConfigRuntimeException(String.format("Region %s could not be found!", region1), ExceptionType.PluginInternalException, t);
            }

            try {
				getRegions = region.getIntersectingRegions(checkRegions);

                if (!getRegions.isEmpty()) {
					for (ProtectedRegion r : getRegions) {
						if (args.length != 2 || !r.getId().equals(region.getId())) {
							listRegions.push(new CString(r.getId(), t));
						}
					}

					if (listRegions.isEmpty()) {
						return new CBoolean(false, t);
					}

					return listRegions;
                }
            } catch (Exception e) {
            }
            return new CBoolean(false, t);
        }
    }

    @api
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

        public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Static.checkPlugin("WorldGuard", t);
            List<World> checkWorlds = null;
            CArray arr = new CArray(t);
            if (args.length == 1) {
                World world = Bukkit.getServer().getWorld(args[0].val());
                if (world != null) {
                    checkWorlds = Arrays.asList(world);
                }
            }
            if (checkWorlds == null) {
                checkWorlds = Bukkit.getServer().getWorlds();
            }
            GlobalRegionManager mgr = Static.getWorldGuardPlugin(t).getGlobalRegionManager();
            for (World world : checkWorlds) {
                for (String region : mgr.get(world).getRegions().keySet()) {
                    arr.push(new CString(region, t));
                }
            }
            return arr;

        }
    }

    @api(environments=CommandHelperEnvironment.class)
    public static class sk_current_regions extends SKFunction {

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

        public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Static.checkPlugin("WorldGuard", t);
            World world;

            MCPlayer m = null;

            if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
                m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
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

    @api(environments=CommandHelperEnvironment.class)
    public static class sk_regions_at extends SKFunction {

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
            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.PluginInternalException, ExceptionType.InsufficientArgumentsException};
        }

        public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Static.checkPlugin("WorldGuard", t);
            World world;

            if (!( args[0] instanceof CArray )) {
                throw new ConfigRuntimeException(this.getName() + " needs a locationarray", ExceptionType.CastException, t);
            }

            MCWorld w = null;
            MCCommandSender c = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
            if (c != null && !( c instanceof MCPlayer)) {
                w = ((MCPlayer)c).getWorld();
            }

            MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], w, t);

            if (loc.getWorld() == null) {
                throw new ConfigRuntimeException(this.getName() + " needs a world", ExceptionType.InsufficientArgumentsException, t);
            }

            world = Bukkit.getServer().getWorld(loc.getWorld().getName());

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

    @api
    public static class sk_region_volume extends SKFunction {

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

        public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
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

    @api
    public static class sk_region_create extends SKFunction {

        public String getName() {
            return "sk_region_create";
        }

        public Integer[] numArgs() {
            return new Integer[]{2, 3};
        }

        public String docs() {
            return "void {[world], name, array(locationArrayPos1, locationArrayPos2, [[locationArrayPosN]...])} Create region of the given name in the given world.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.InvalidWorldException, ExceptionType.PluginInternalException};
        }

        public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Static.checkPlugin("WorldGuard", t);

            World world = null;
            String region;

            if (args.length == 2) {

                region = args[0].val();

                MCPlayer m = null;

                if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
                    m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
                }

                if (m != null) {
                    world = Bukkit.getServer().getWorld(m.getWorld().getName());
                }
            } else {
                region = args[1].val();
                world = Bukkit.getServer().getWorld(args[0].val());
            }

            if (world == null) {
                throw new ConfigRuntimeException("Unknown world specified", ExceptionType.InvalidWorldException, t);
            }

            RegionManager mgr = Static.getWorldGuardPlugin(t).getGlobalRegionManager().get(world);

            ProtectedRegion regionExists = mgr.getRegion(region);

            if (regionExists != null) {
                throw new ConfigRuntimeException(String.format("The region (%s) exists in world (%s).", region, world.getName()), ExceptionType.PluginInternalException, t);
            }

            if (!(args[args.length - 1] instanceof CArray)) {
                throw new ConfigRuntimeException("Pass an array of points for a new region", ExceptionType.PluginInternalException, t);
            }

            List<BlockVector> points = new ArrayList<BlockVector>();
            List<BlockVector2D> points2D = new ArrayList<BlockVector2D>();

            int x = 0;
            int y = 0;
            int z = 0;

            int minY = 0;
            int maxY = 0;

            ProtectedRegion newRegion = null;

            CArray arg = (CArray) args[args.length - 1];

            for (int i = 0; i < arg.size(); i++) {
                CArray point = (CArray)arg.get(i, t);

                x = Static.getInt32(point.get(0), t);
                y = Static.getInt32(point.get(1), t);
                z = Static.getInt32(point.get(2), t);

                if (arg.size() == 2) {
                    points.add(new BlockVector(x, y, z));
                } else {
                    points2D.add(new BlockVector2D(x, z));

                    if (i == 0) {
                        minY = maxY = y;
                    } else {
                        if (y < minY) {
                            minY = y;
                        } else if (y > maxY) {
                            maxY = y;
                        }
                    }
                }
            }

            if (arg.size() == 2) {
                newRegion = new ProtectedCuboidRegion(region, points.get(0), points.get(1));
            } else {
                newRegion = new ProtectedPolygonalRegion(region, points2D, minY, maxY);
            }

            if (newRegion == null) {
                throw new ConfigRuntimeException("Error while creating protected region", ExceptionType.PluginInternalException, t);
            }

			mgr.addRegion(newRegion);

			try {
				mgr.save();
			} catch (Exception e) {
				throw new ConfigRuntimeException("Error while creating protected region", ExceptionType.PluginInternalException, t, e);
			}

            return new CVoid(t);
        }

        @Override
        public CHVersion since() {
            return CHVersion.V3_3_1;
        }
    }

    @api
    public static class sk_region_update extends SKFunction {

        public String getName() {
            return "sk_region_update";
        }

        public Integer[] numArgs() {
            return new Integer[]{2, 3};
        }

        public String docs() {
            return "void {[world], name, array(locationArrayPos1, locationArrayPos2, [[locationArrayPosN]...])} Updates the location of a given region to the new location. Other properties of the region, like owners, members, priority, etc are unaffected.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.InvalidWorldException, ExceptionType.PluginInternalException};
        }

        public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Static.checkPlugin("WorldGuard", t);

            World world = null;
            String region;

            if (args.length == 2) {

                region = args[0].val();

                MCPlayer m = null;

                if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
                    m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
                }

                if (m != null) {
                    world = Bukkit.getServer().getWorld(m.getWorld().getName());
                }
            } else {
                region = args[1].val();
                world = Bukkit.getServer().getWorld(args[0].val());
            }

            if (world == null) {
                throw new ConfigRuntimeException("Unknown world specified", ExceptionType.InvalidWorldException, t);
            }

            RegionManager mgr = Static.getWorldGuardPlugin(t).getGlobalRegionManager().get(world);

            ProtectedRegion oldRegion = mgr.getRegion(region);

            if (oldRegion == null) {
                throw new ConfigRuntimeException(String.format("The region (%s) doesn't exists in world (%s).", region, world.getName()), ExceptionType.PluginInternalException, t);
            }

            if (!(args[args.length - 1] instanceof CArray)) {
                throw new ConfigRuntimeException("Pass an array of points to define a new region", ExceptionType.PluginInternalException, t);
            }

            List<BlockVector> points = new ArrayList<BlockVector>();
            List<BlockVector2D> points2D = new ArrayList<BlockVector2D>();

            int x = 0;
            int y = 0;
            int z = 0;

            int minY = 0;
            int maxY = 0;

            ProtectedRegion newRegion = null;

            CArray arg = (CArray) args[args.length - 1];

            for (int i = 0; i < arg.size(); i++) {
                CArray point = (CArray)arg.get(i, t);

                x = Static.getInt32(point.get(0), t);
                y = Static.getInt32(point.get(1), t);
                z = Static.getInt32(point.get(2), t);

                if (arg.size() == 2) {
                    points.add(new BlockVector(x, y, z));
                } else {
                    points2D.add(new BlockVector2D(x, z));

                    if (i == 0) {
                        minY = maxY = y;
                    } else {
                        if (y < minY) {
                            minY = y;
                        } else if (y > maxY) {
                            maxY = y;
                        }
                    }
                }
            }

            if (arg.size() == 2) {
                newRegion = new ProtectedCuboidRegion(region, points.get(0), points.get(1));
            } else {
                newRegion = new ProtectedPolygonalRegion(region, points2D, minY, maxY);
            }

            if (newRegion == null) {
                throw new ConfigRuntimeException("Error while redefining protected region", ExceptionType.PluginInternalException, t);
            }

			mgr.addRegion(newRegion);

			newRegion.setMembers(oldRegion.getMembers());
			newRegion.setOwners(oldRegion.getOwners());
			newRegion.setFlags(oldRegion.getFlags());
			newRegion.setPriority(oldRegion.getPriority());
			try {
				newRegion.setParent(oldRegion.getParent());
			} catch (Exception ignore) {
			}

			try {
				mgr.save();
			} catch (Exception e) {
				throw new ConfigRuntimeException("Error while redefining protected region", ExceptionType.PluginInternalException, t, e);
			}

            return new CVoid(t);
        }

        @Override
        public CHVersion since() {
            return CHVersion.V3_3_1;
        }
    }

    @api
    public static class sk_region_remove extends SKFunction {

        public String getName() {
            return "sk_region_remove";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public String docs() {
            return "void {[world], name} Remove existed region.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.InvalidWorldException, ExceptionType.PluginInternalException};
        }

        public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Static.checkPlugin("WorldGuard", t);

            World world = null;
            String region;

            if (args.length == 1) {

                region = args[0].val();

                MCPlayer m = null;

                if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
                    m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
                }

                if (m != null) {
                    world = Bukkit.getServer().getWorld(m.getWorld().getName());
                }
            } else {
                region = args[1].val();
                world = Bukkit.getServer().getWorld(args[0].val());
            }

            if (world == null) {
                throw new ConfigRuntimeException("Unknown world specified", ExceptionType.InvalidWorldException, t);
            }

            RegionManager mgr = Static.getWorldGuardPlugin(t).getGlobalRegionManager().get(world);

            ProtectedRegion regionExists = mgr.getRegion(region);

            if (regionExists == null) {
                throw new ConfigRuntimeException(String.format("The region (%s) doesn't exists in world (%s).", region, world.getName()), ExceptionType.PluginInternalException, t);
            }

            mgr.removeRegion(region);

			try {
				mgr.save();
			} catch (Exception e) {
				throw new ConfigRuntimeException("Error while removing protected region", ExceptionType.PluginInternalException, t, e);
			}

            return new CVoid(t);
        }

        @Override
        public CHVersion since() {
            return CHVersion.V3_3_1;
        }
    }

    @api
    public static class sk_region_exists extends SKFunction {

        public String getName() {
            return "sk_region_exists";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public String docs() {
            return "void {[world], name} Check if a given region exists.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.InvalidWorldException};
        }

        public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Static.checkPlugin("WorldGuard", t);

            World world = null;
            String region;

            if (args.length == 1) {

                region = args[0].val();

                MCPlayer m = null;

                if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
                    m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
                }

                if (m != null) {
                    world = Bukkit.getServer().getWorld(m.getWorld().getName());
                }
            } else {
                region = args[1].val();
                world = Bukkit.getServer().getWorld(args[0].val());
            }

            if (world == null) {
                throw new ConfigRuntimeException("Unknown world specified", ExceptionType.InvalidWorldException, t);
            }

            RegionManager mgr = Static.getWorldGuardPlugin(t).getGlobalRegionManager().get(world);

            ProtectedRegion regionExists = mgr.getRegion(region);

            if (regionExists != null) {
                return new CBoolean(true, t);
            }

            return new CBoolean(false, t);
        }

        @Override
        public CHVersion since() {
            return CHVersion.V3_3_1;
        }
    }

    @api
    public static class sk_region_addowner extends SKFunction {

        public String getName() {
            return "sk_region_addowner";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2, 3};
        }

        public String docs() {
            return "void {region, [world], [owner1] | region, [world], [array(owner1, ownerN, ...)]} Add owner(s) to given region.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.InvalidWorldException, ExceptionType.PluginInternalException};
        }

        public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Static.checkPlugin("WorldGuard", t);

            World world = null;
            MCPlayer m = null;
            String[] owners = null;
            String region = args[0].val();

            if (args.length == 1) {

                if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
                    m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
                }

                if (m != null) {
                    world = Bukkit.getServer().getWorld(m.getWorld().getName());
					owners = new String[1];
					owners[0] = m.getName();
                }

            } else if (args.length == 2) {

                world = Bukkit.getServer().getWorld(args[0].val());

                if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
                    m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
                }

                if (m != null) {
					owners = new String[1];
					owners[0] = m.getName();
                }

            } else {

                world = Bukkit.getServer().getWorld(args[1].val());

                if (args[2] instanceof CArray) {

                    CArray arg = (CArray) args[2];
					owners = new String[(int)arg.size()];

                    for (int i = 0; i < arg.size(); i++) {
						owners[i] = arg.get(i, t).val();
                    }
                } else {
					owners = new String[1];
					owners[0] = args[2].val();
                }

            }

            if (world == null) {
                throw new ConfigRuntimeException("Unknown world specified", ExceptionType.InvalidWorldException, t);
            }

            RegionManager mgr = Static.getWorldGuardPlugin(t).getGlobalRegionManager().get(world);

            ProtectedRegion regionExists = mgr.getRegion(region);

            if (regionExists == null) {
                throw new ConfigRuntimeException(String.format("The region (%s) doesn't exists in world (%s).", region, world.getName()), ExceptionType.PluginInternalException, t);
            }

			RegionDBUtil.addToDomain(regionExists.getOwners(), owners, 0);

			try {
				mgr.save();
			} catch (Exception e) {
				throw new ConfigRuntimeException("Error while adding owner(s) to protected region", ExceptionType.PluginInternalException, t, e);
			}

            return new CVoid(t);
        }

        @Override
        public CHVersion since() {
            return CHVersion.V3_3_1;
        }
    }

    @api
    public static class sk_region_remowner extends SKFunction {

        public String getName() {
            return "sk_region_remowner";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2, 3};
        }

        public String docs() {
            return "void {region, [world], [owner1] | region, [world], [array(owner1, ownerN, ...)]} Remove owner(s) from given region.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.InvalidWorldException, ExceptionType.PluginInternalException};
        }

        public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Static.checkPlugin("WorldGuard", t);

            World world = null;
            MCPlayer m = null;
			String[] owners = null;
            String region = args[0].val();

            if (args.length == 1) {

                if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
                    m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
                }

                if (m != null) {
                    world = Bukkit.getServer().getWorld(m.getWorld().getName());
					owners = new String[1];
					owners[0] = m.getName();
                }

            } else if (args.length == 2) {

                world = Bukkit.getServer().getWorld(args[0].val());

                if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
                    m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
                }

                if (m != null) {
					owners = new String[1];
					owners[0] = m.getName();
                }

            } else {

                world = Bukkit.getServer().getWorld(args[1].val());

                if (args[2] instanceof CArray) {

                    CArray arg = (CArray) args[2];
					owners = new String[(int)arg.size()];

                    for (int i = 0; i < arg.size(); i++) {
						owners[i] = arg.get(i, t).val();
                    }
                } else {
					owners = new String[1];
					owners[0] = args[2].val();
                }

            }

            if (world == null) {
                throw new ConfigRuntimeException("Unknown world specified", ExceptionType.InvalidWorldException, t);
            }

            RegionManager mgr = Static.getWorldGuardPlugin(t).getGlobalRegionManager().get(world);

            ProtectedRegion regionExists = mgr.getRegion(region);

            if (regionExists == null) {
                throw new ConfigRuntimeException(String.format("The region (%s) doesn't exists in world (%s).", region, world.getName()), ExceptionType.PluginInternalException, t);
            }

			RegionDBUtil.removeFromDomain(regionExists.getOwners(), owners, 0);

			try {
				mgr.save();
			} catch (Exception e) {
				throw new ConfigRuntimeException("Error while deleting owner(s) from protected region", ExceptionType.PluginInternalException, t, e);
			}

            return new CVoid(t);
        }

        @Override
        public CHVersion since() {
            return CHVersion.V3_3_1;
        }
    }

    @api
    public static class sk_region_addmember extends SKFunction {

        public String getName() {
            return "sk_region_addmember";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2, 3};
        }

        public String docs() {
            return "void {region, [world], [member1] | region, [world], [array(member1, memberN, ...)]} Add member(s) to given region.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.InvalidWorldException, ExceptionType.PluginInternalException};
        }

        public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Static.checkPlugin("WorldGuard", t);

            World world = null;
            MCPlayer m = null;
			String[] members = null;
            String region = args[0].val();

            if (args.length == 1) {

                if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
                    m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
                }

                if (m != null) {
                    world = Bukkit.getServer().getWorld(m.getWorld().getName());
					members = new String[1];
					members[0] = m.getName();
                }

            } else if (args.length == 2) {

                world = Bukkit.getServer().getWorld(args[0].val());

                if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
                    m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
                }

                if (m != null) {
					members = new String[1];
					members[0] = m.getName();
                }

            } else {

                world = Bukkit.getServer().getWorld(args[1].val());

                if (args[2] instanceof CArray) {

                    CArray arg = (CArray) args[2];

                    for (int i = 0; i < arg.size(); i++) {
						members = new String[(int)arg.size()];
						members[i] = arg.get(i, t).val();
                    }
                } else {
					members = new String[1];
					members[0] = args[2].val();
                }

            }

            if (world == null) {
                throw new ConfigRuntimeException("Unknown world specified", ExceptionType.InvalidWorldException, t);
            }

            RegionManager mgr = Static.getWorldGuardPlugin(t).getGlobalRegionManager().get(world);

            ProtectedRegion regionExists = mgr.getRegion(region);

            if (regionExists == null) {
                throw new ConfigRuntimeException(String.format("The region (%s) doesn't exists in world (%s).", region, world.getName()), ExceptionType.PluginInternalException, t);
            }

			RegionDBUtil.addToDomain(regionExists.getMembers(), members, 0);

			try {
				mgr.save();
			} catch (Exception e) {
				throw new ConfigRuntimeException("Error while adding member(s) to protected region", ExceptionType.PluginInternalException, t, e);
			}

            return new CVoid(t);
        }

        @Override
        public CHVersion since() {
            return CHVersion.V3_3_1;
        }
    }

    @api
    public static class sk_region_remmember extends SKFunction {

        public String getName() {
            return "sk_region_remmember";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2, 3};
        }

        public String docs() {
            return "void {region, [world], [member1] | region, [world], [array(member1, memberN, ...)]} Remove member(s) from given region.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.InvalidWorldException, ExceptionType.PluginInternalException};
        }

        public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Static.checkPlugin("WorldGuard", t);

            World world = null;
            MCPlayer m = null;
			String[] members = null;
            String region = args[0].val();

            if (args.length == 1) {

                if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
                    m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
                }

                if (m != null) {
                    world = Bukkit.getServer().getWorld(m.getWorld().getName());
					members = new String[1];
					members[0] = m.getName();
                }

            } else if (args.length == 2) {

                world = Bukkit.getServer().getWorld(args[0].val());

                if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
                    m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
                }

                if (m != null) {
					members = new String[1];
					members[0] = m.getName();
                }

            } else {

                world = Bukkit.getServer().getWorld(args[1].val());

                if (args[2] instanceof CArray) {

                    CArray arg = (CArray) args[2];
					members = new String[(int)arg.size()];

                    for (int i = 0; i < arg.size(); i++) {
						members[i] = arg.get(i, t).val();
                    }
                } else {
					members = new String[1];
					members[0] = args[2].val();
                }

            }

            if (world == null) {
                throw new ConfigRuntimeException("Unknown world specified", ExceptionType.InvalidWorldException, t);
            }

            RegionManager mgr = Static.getWorldGuardPlugin(t).getGlobalRegionManager().get(world);

            ProtectedRegion regionExists = mgr.getRegion(region);

            if (regionExists == null) {
                throw new ConfigRuntimeException(String.format("The region (%s) doesn't exists in world (%s).", region, world.getName()), ExceptionType.PluginInternalException, t);
            }

			RegionDBUtil.removeFromDomain(regionExists.getMembers(), members, 0);

			try {
				mgr.save();
			} catch (Exception e) {
				throw new ConfigRuntimeException("Error while deleting members(s) from protected region", ExceptionType.PluginInternalException, t, e);
			}

            return new CVoid(t);
        }

        @Override
        public CHVersion since() {
            return CHVersion.V3_3_1;
        }
    }

    @api
    public static class sk_region_flag extends SKFunction {

        public String getName() {
            return "sk_region_flag";
        }

        public Integer[] numArgs() {
            return new Integer[]{4, 5};
        }

        public String docs() {
            return "void {world, region, flagName, flagValue, [group]} Add/change/remove flag for selected region. FlagName should be any"
					+ " supported flag from [http://wiki.sk89q.com/wiki/WorldGuard/Regions/Flags this list]. For the flagValue, use types which"
					+ " are supported by WorldGuard. Add group argument if you want to add WorldGuard group flag (read more about group"
					+ " flag types [http://wiki.sk89q.com/wiki/WorldGuard/Regions/Flags#Group here]). Set flagValue as null (and don't set"
					+ " group) to delete the flag from the region.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.InvalidWorldException, ExceptionType.PluginInternalException};
        }

        public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Static.checkPlugin("WorldGuard", t);

            World world = Bukkit.getServer().getWorld(args[0].val());

            if (world == null) {
                throw new ConfigRuntimeException("Unknown world specified", ExceptionType.InvalidWorldException, t);
            }

			String regionName = args[1].val();
            String flagName = args[2].val();
			String flagValue = null;
			RegionGroup groupValue = null;

			if (args.length >= 4 && !(args[3] instanceof CNull) && args[3].val() != "") {
				flagValue = args[3].val();
			}

            RegionManager mgr = Static.getWorldGuardPlugin(t).getGlobalRegionManager().get(world);

            ProtectedRegion region = mgr.getRegion(regionName);

			if (region == null) {
				if ("__global__".equalsIgnoreCase(regionName)) {
					region = new GlobalProtectedRegion(regionName);
					mgr.addRegion(region);
				} else {
					throw new ConfigRuntimeException(String.format("The region (%s) doesn't exists in world (%s).", regionName, world.getName()), ExceptionType.PluginInternalException, t);
				}
			}

			Flag<?> foundFlag = null;

			for (Flag<?> flag : DefaultFlag.getFlags()) {
				if (flag.getName().replace("-", "").equalsIgnoreCase(flagName.replace("-", ""))) {
					foundFlag = flag;
					break;
				}
			}

			if (foundFlag == null) {
				throw new ConfigRuntimeException(String.format("Unknown flag specified: (%s).", flagName), ExceptionType.PluginInternalException, t);
			}

			if (args.length == 5) {
				String group = args[4].val();
				RegionGroupFlag groupFlag = foundFlag.getRegionGroupFlag();
				if (groupFlag == null) {
					throw new ConfigRuntimeException(String.format("Region flag (%s) does not have a group flag.", flagName), ExceptionType.PluginInternalException, t);
				}

				try {
					groupValue = groupFlag.parseInput(Static.getWorldGuardPlugin(t), new BukkitMCCommandSender(env.getEnv(CommandHelperEnvironment.class).GetCommandSender())._CommandSender(), group);
				} catch (InvalidFlagFormat e) {
					throw new ConfigRuntimeException(String.format("Unknown group (%s).", group), ExceptionType.PluginInternalException, t);
				}

			}

			if (flagValue != null) {
				try {
					setFlag(t, region, foundFlag, new BukkitMCCommandSender(env.getEnv(CommandHelperEnvironment.class).GetCommandSender())._CommandSender(), flagValue);

				} catch (Exception e) {
					throw new ConfigRuntimeException(String.format("Unknown flag value specified: (%s).", flagValue), ExceptionType.PluginInternalException, t);
				}
			} else if (args.length < 5) {
				region.setFlag(foundFlag, null);

				RegionGroupFlag groupFlag = foundFlag.getRegionGroupFlag();
				if (groupFlag != null) {
					region.setFlag(groupFlag, null);
				}
			}

			if (groupValue != null) {
				RegionGroupFlag groupFlag = foundFlag.getRegionGroupFlag();

				if (groupValue == groupFlag.getDefault()) {
					region.setFlag(groupFlag, null);
				} else {
					region.setFlag(groupFlag, groupValue);
				}
			}

			try {
				mgr.save();
			} catch (Exception e) {
				throw new ConfigRuntimeException("Error while defining flags", ExceptionType.PluginInternalException, t, e);
			}

            return new CVoid(t);
        }

		private <V> void setFlag(Target t, ProtectedRegion region,
				Flag<V> flag, CommandSender sender, String value)
						throws InvalidFlagFormat {
			region.setFlag(flag, flag.parseInput(Static.getWorldGuardPlugin(t), sender, value));
		}

        @Override
        public CHVersion since() {
            return CHVersion.V3_3_1;
        }
    }

    @api
    public static class sk_region_setpriority extends SKFunction {

        public String getName() {
            return "sk_region_setpriority";
        }

        public Integer[] numArgs() {
            return new Integer[]{2, 3};
        }

        public String docs() {
            return "void {[world], region, priority} Sets priority for a given region.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.InvalidWorldException, ExceptionType.PluginInternalException};
        }

        public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Static.checkPlugin("WorldGuard", t);

            World world = null;
			String region;
			int priority = 0;

            if (args.length == 2) {

                region = args[0].val();

                MCPlayer m = null;

                if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
                    m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
                }

                if (m != null) {
                    world = Bukkit.getServer().getWorld(m.getWorld().getName());
                }

				priority = Static.getInt32(args[1], t);

            } else {
                region = args[1].val();
                world = Bukkit.getServer().getWorld(args[0].val());

				priority = Static.getInt32(args[2], t);
            }

            if (world == null) {
                throw new ConfigRuntimeException("Unknown world specified", ExceptionType.InvalidWorldException, t);
            }

			if ("__global__".equalsIgnoreCase(region)) {
				throw new ConfigRuntimeException(String.format("The region cannot be named __global__.", region), ExceptionType.PluginInternalException, t);
			}

            RegionManager mgr = Static.getWorldGuardPlugin(t).getGlobalRegionManager().get(world);

            ProtectedRegion regionExists = mgr.getRegion(region);

            if (regionExists == null) {
				throw new ConfigRuntimeException(String.format("The region (%s) doesn't exists in world (%s).", region, world.getName()), ExceptionType.PluginInternalException, t);
            }

			regionExists.setPriority(priority);

			try {
				mgr.save();
			} catch (Exception e) {
				throw new ConfigRuntimeException("Error while setting priority for protected region", ExceptionType.PluginInternalException, t, e);
			}

            return new CVoid(t);
        }

        @Override
        public CHVersion since() {
            return CHVersion.V3_3_1;
        }
    }

    @api
    public static class sk_region_setparent extends SKFunction {

        public String getName() {
            return "sk_region_setparent";
        }

        public Integer[] numArgs() {
            return new Integer[]{2, 3};
        }

        public String docs() {
            return "void {world, region, [parentRegion]} Sets parent region for a given region.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.InvalidWorldException, ExceptionType.PluginInternalException};
        }

        public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Static.checkPlugin("WorldGuard", t);

			String regionName;
			String parentName;


            World world = Bukkit.getServer().getWorld(args[0].val());

            if (world == null) {
                throw new ConfigRuntimeException("Unknown world specified", ExceptionType.InvalidWorldException, t);
            }

			regionName = args[1].val();

			if ("__global__".equalsIgnoreCase(regionName)) {
				throw new ConfigRuntimeException(String.format("You cannot set parents for a __global__ cuboid.", regionName), ExceptionType.PluginInternalException, t);
			}

            RegionManager mgr = Static.getWorldGuardPlugin(t).getGlobalRegionManager().get(world);

            ProtectedRegion child = mgr.getRegion(regionName);

            if (child == null) {
				throw new ConfigRuntimeException(String.format("The region (%s) doesn't exists in world (%s).", regionName, world.getName()), ExceptionType.PluginInternalException, t);
            }

			if (args.length == 2) {
				try {
					child.setParent(null);
				} catch (ProtectedRegion.CircularInheritanceException ignore) {
				}
			} else {
				parentName = args[2].val();
				ProtectedRegion parent = mgr.getRegion(parentName);

				if (parent == null) {
					throw new ConfigRuntimeException(String.format("The region (%s) doesn't exists in world (%s).", parentName, world.getName()), ExceptionType.PluginInternalException, t);
				}

				try {
					child.setParent(parent);
				} catch (ProtectedRegion.CircularInheritanceException e) {
					throw new ConfigRuntimeException(String.format("Circular inheritance detected."), ExceptionType.PluginInternalException, t);
				}
			}

			try {
				mgr.save();
			} catch (Exception e) {
				throw new ConfigRuntimeException("Error while setting parent for protected region", ExceptionType.PluginInternalException, t, e);
			}

            return new CVoid(t);
        }

        @Override
        public CHVersion since() {
            return CHVersion.V3_3_1;
        }
    }

    public static abstract class SKFunction extends AbstractFunction {

        public boolean isRestricted() {
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
