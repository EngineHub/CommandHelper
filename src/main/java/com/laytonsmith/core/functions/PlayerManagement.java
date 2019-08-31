package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Vector3D;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.MCBlockCommandSender;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCConsoleCommandSender;
import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCOfflinePlayer;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCBlockData;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.entities.MCCommandMinecart;
import com.laytonsmith.abstraction.enums.MCEntityType;
import com.laytonsmith.abstraction.enums.MCGameMode;
import com.laytonsmith.abstraction.enums.MCPlayerStatistic;
import com.laytonsmith.abstraction.enums.MCPotionEffectType;
import com.laytonsmith.abstraction.enums.MCSound;
import com.laytonsmith.abstraction.enums.MCSoundCategory;
import com.laytonsmith.abstraction.enums.MCWeather;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.seealso;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Optimizable;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CRE.CREBadEntityException;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CREIllegalArgumentException;
import com.laytonsmith.core.exceptions.CRE.CREInsufficientArgumentsException;
import com.laytonsmith.core.exceptions.CRE.CREInvalidWorldException;
import com.laytonsmith.core.exceptions.CRE.CRELengthException;
import com.laytonsmith.core.exceptions.CRE.CRENotFoundException;
import com.laytonsmith.core.exceptions.CRE.CRENullPointerException;
import com.laytonsmith.core.exceptions.CRE.CREPlayerOfflineException;
import com.laytonsmith.core.exceptions.CRE.CREPluginInternalException;
import com.laytonsmith.core.exceptions.CRE.CRERangeException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayerManagement {

	public static String docs() {
		return "This class of functions allow players to be managed. Functions that accept an online player's name will"
				+ " also accept their UUID.";
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class player extends AbstractFunction {

		@Override
		public String getName() {
			return "player";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();

			if(args.length == 1) {
				p = Static.GetPlayer(args[0], t);
			}

			// assuming p is not null, just return the name set by the CommandSender
			// for player entities in CraftBukkit, this is the player's name, and
			// for the console it's "CONSOLE". For CommandBlocks it's "@" unless it has been renamed.
			if(p == null) {
				return CNull.NULL;
			} else {
				String name = p.getName();
				if(p instanceof MCConsoleCommandSender || "CONSOLE".equals(name)) {
					name = Static.getConsoleName();
				}
				if(p instanceof MCBlockCommandSender || p instanceof MCCommandMinecart) {
					name = Static.getBlockPrefix() + name;
				}
				return new CString(name, t);
			}
		}

		@Override
		public String docs() {
			return "string {[playerName] | [uuid]} Returns a player's name. If a string is specified, it will attempt"
					+ " to find a complete match for a partial name. If no string is specified, the current player is"
					+ " returned. UUIDs are also accepted for this and other functions that apply to online players."
					+ " If the command is being run from the console, then the string '" + Static.getConsoleName()
					+ "' is returned. If the command came from a CommandBlock, the block's name prefixed with "
					+ Static.getBlockPrefix() + " is returned. If the command is coming from elsewhere,"
					+ " returns a string chosen by the sender of this command (or null)."
					+ " Note that most functions won't support console or block names"
					+ " (they'll throw a PlayerOfflineException), but you can use this to determine where a command is"
					+ " being run from.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_0_1;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class puuid extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CRENotFoundException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCOfflinePlayer pl = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			boolean dashless = false;
			if(args.length >= 1) {
				try {
					pl = Static.GetPlayer(args[0], t);
				} catch (ConfigRuntimeException cre) {
					pl = Static.GetUser(args[0], t);
				}
			}
			if(args.length == 2) {
				dashless = ArgumentValidation.getBoolean(args[1], t);
			}
			if(pl == null) {
				throw new CREPlayerOfflineException("No matching player could be found.", t);
			}
			UUID uuid = pl.getUniqueID();
			if(uuid == null) {
				throw new CRENotFoundException(
						"Could not find the UUID of the player (are you running in cmdline mode?)", t);
			}
			String uuidStr = uuid.toString();
			return new CString(dashless ? uuidStr.replace("-", "") : uuidStr, t);
		}

		@Override
		public String getName() {
			return "puuid";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1, 2};
		}

		@Override
		public String docs() {
			return "string {[player], [dashless]} Returns the UUID of the current player or the specified player."
					+ " This will attempt to find an offline player, but if that also fails,"
					+ " a PlayerOfflineException will be thrown.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class all_players extends AbstractFunction {

		@Override
		public String getName() {
			return "all_players";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			CArray players = new CArray(t);
			if(args.length == 0) {
				for(MCPlayer player : Static.getServer().getOnlinePlayers()) {
					players.push(new CString(player.getName(), t), t);
				}
			} else {
				MCWorld world = Static.getServer().getWorld(args[0].val());
				if(world == null) {
					throw new CREInvalidWorldException("Unknown world: " + args[0].val(), t);
				}
				for(MCPlayer player : world.getPlayers()) {
					if(player.isOnline()) {
						players.push(new CString(player.getName(), t), t);
					}
				}
			}
			return players;
		}

		@Override
		public String docs() {
			return "array {[world]} Returns an array of all the player names of all the online players on the server."
					+ " If world is given only the name of the players in this world will be returned.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInvalidWorldException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_0_1;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class players_in_radius extends AbstractFunction {

		@Override
		public String getName() {
			return "players_in_radius";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		boolean inRadius(MCPlayer player, double dist, MCLocation loc) {
			if(!(player.getWorld().equals(loc.getWorld()))) {
				return false;
			}

			double x1 = player.getLocation().getX();
			double y1 = player.getLocation().getY();
			double z1 = player.getLocation().getZ();

			double x2 = loc.getX();
			double y2 = loc.getY();
			double z2 = loc.getZ();

			double distance = java.lang.Math.sqrt(
					(x1 - x2) * (x1 - x2)
					+ (y1 - y2) * (y1 - y2)
					+ (z1 - z2) * (z1 - z2));

			return distance <= dist;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			Collection<MCPlayer> pa = Static.getServer().getOnlinePlayers();
			MCPlayer p = env.getEnv(CommandHelperEnvironment.class).GetPlayer();

			MCLocation loc;
			double dist;

			if(args.length == 1) {
				dist = Static.getDouble(args[0], t);
				Static.AssertPlayerNonNull(p, t);
				loc = p.getLocation();
			} else {
				if(!(args[0].isInstanceOf(CArray.TYPE))) {
					throw new CRECastException("Expecting an array at parameter 1 of players_in_radius", t);
				}

				loc = ObjectGenerator.GetGenerator().location(args[0], p != null ? p.getWorld() : null, t);
				dist = Static.getDouble(args[1], t);
			}

			CArray sa = new CArray(t);

			for(MCPlayer pa1 : pa) {
				if(inRadius(pa1, dist, loc)) {
					sa.push(new CString(pa1.getName(), t), t);
				}
			}

			return sa;
		}

		@Override
		public String docs() {
			return "array {[locationArray], distance} Returns an array of all the players within the given radius.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREFormatException.class,
				CREPlayerOfflineException.class, CRENotFoundException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class ploc extends AbstractFunction {

		@Override
		public String getName() {
			return "ploc";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p;
			if(args.length == 0) {
				p = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
				Static.AssertPlayerNonNull(p, t);
			} else {
				p = Static.GetPlayer(args[0], t);
			}
			MCLocation location = p.getLocation();
			if(location == null) {
				throw new CRENotFoundException(
						"Could not find the location of the player (are you running in cmdline mode?)", t);
			}
			location.setY(location.getY() - 1);
			return ObjectGenerator.GetGenerator().location(location);
		}

		@Override
		public String docs() {
			return "array {[playerName]} Returns a location array of the coordinates of the player specified,"
					+ " or the player running the command if no player is specified."
					+ " Note that unlike entity_loc() the y coordinate will be for the block the player is standing on,"
					+ " which is one meter lower. The array returned also includes the player's world, yaw and pitch.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CRENotFoundException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_0_1;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_ploc extends AbstractFunction {

		@Override
		public String getName() {
			return "set_ploc";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3, 4};
		}

		@Override
		public String docs() {
			return "boolean {[player], locationArray | [player], x, y, z} Sets the location of the player to the"
					+ " specified coordinates. If the coordinates are not valid, or the player was otherwise prevented"
					+ " from teleporting, false is returned, otherwise true. If player is omitted, the current player"
					+ " is used. Note that 1 is automatically added to the y coordinate.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CRELengthException.class, CREPlayerOfflineException.class,
				CREFormatException.class, CREInvalidWorldException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_1_0;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
			MCLocation l;
			if(args.length <= 2) {
				if(!(args[args.length - 1].isInstanceOf(CArray.TYPE))) {
					throw new CRECastException("Expecting an array at parameter " + args.length + " of set_ploc", t);
				}
				CArray ca = (CArray) args[args.length - 1];

				if(args.length == 2) {
					p = Static.GetPlayer(args[0], t);
				} else {
					Static.AssertPlayerNonNull(p, t);
				}

				l = ObjectGenerator.GetGenerator().location(ca, p.getWorld(), t);

				// set yaw/pitch to current player values if not given
				MCLocation ploc = p.getLocation();
				if(ca.isAssociative()) {
					if(!(ca.containsKey("yaw"))) {
						l.setYaw(ploc.getYaw());
					}
					if(!(ca.containsKey("pitch"))) {
						l.setPitch(ploc.getPitch());
					}
				} else if(ca.size() < 5) {
					l.setYaw(ploc.getYaw());
					l.setPitch(ploc.getPitch());
				}
			} else {
				if(args.length == 4) {
					p = Static.GetPlayer(args[0], t);
				} else {
					Static.AssertPlayerNonNull(p, t);
				}

				double x = Static.getNumber(args[args.length - 3], t);
				double y = Static.getNumber(args[args.length - 2], t);
				double z = Static.getNumber(args[args.length - 1], t);
				float yaw = 0;
				float pitch = 0;
				MCLocation ploc = p.getLocation();
				if(ploc != null) {
					yaw = ploc.getYaw();
					pitch = ploc.getPitch();
				}
				l = StaticLayer.GetLocation(p.getWorld(), x, y, z, yaw, pitch);
			}

			l.add(0, 1, 0);
			return CBoolean.get(p.teleport(l));
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class pcursor extends AbstractFunction {

		@Override
		public String getName() {
			return "pcursor";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1, 2};
		}

		@Override
		public String docs() {
			return "array {[player], [array]} Returns a location array with the coordinates of the block the player has"
					+ " highlighted in their crosshairs. If player is omitted, the current player is used."
					+ " If the block is too far, a RangeException is thrown. An array of block types to be considered"
					+ " transparent can be supplied, otherwise only air will be considered transparent."
					+ " Providing an empty array will cause air to be considered a potential target, allowing a way to"
					+ " get the block containing the player's head.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CRERangeException.class,
				CREFormatException.class, CRECastException.class, CREPluginInternalException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_0_2;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
			HashSet<MCMaterial> trans = null;
			int transparentIndex = -1;
			if(args.length == 1) {
				if(args[0].isInstanceOf(CArray.TYPE)) {
					transparentIndex = 0;
				} else {
					p = Static.GetPlayer(args[0], t);
				}
			} else if(args.length == 2) {
				p = Static.GetPlayer(args[0], t);
				if(args[1].isInstanceOf(CArray.TYPE)) {
					transparentIndex = 1;
				} else {
					throw new CREFormatException("An array was expected for argument 2 but received " + args[1], t);
				}
			}
			if(transparentIndex >= 0) {
				CArray ta = (CArray) args[transparentIndex];
				trans = new HashSet<>();
				for(Mixed mat : ta.asList()) {
					MCMaterial material = StaticLayer.GetMaterial(mat.val());
					if(material != null) {
						trans.add(StaticLayer.GetMaterial(mat.val()));
						continue;
					}
					try {
						material = StaticLayer.GetMaterialFromLegacy(Static.getInt16(mat, t), 0);
						if(material != null) {
							MSLog.GetLogger().w(MSLog.Tags.DEPRECATION, "The id \"" + mat.val() + "\" is deprecated."
									+ " Converted to \"" + material.getName() + "\"", t);
							trans.add(material);
							continue;
						}
					} catch (CRECastException ex) {
						// ignore and throw a more specific message
					}
					throw new CREFormatException("Could not find a material by the name \"" + mat.val() + "\"", t);
				}
			}
			Static.AssertPlayerNonNull(p, t);
			MCBlock b;
			try {
				b = p.getTargetBlock(trans, 512);
			} catch (IllegalStateException ise) {
				throw new CREPluginInternalException("The server's method of finding the target block has failed."
						+ " There is nothing that can be done about this except standing somewhere else.", t);
			}
			if(b == null) {
				throw new CRERangeException("No block in sight, or block too far", t);
			} else {
				return ObjectGenerator.GetGenerator().location(b.getLocation(), false);
			}
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Demonstrates finding a non-air block",
				"msg(pcursor())",
				"{0: -127, 1: 75, 2: 798, 3: world, x: -127, y: 75, z: 798, world: world}"),
				new ExampleScript("Demonstrates looking above the skyline",
				"msg(pcursor())",
				"(Throws RangeException: No block in sight, or block too far)"),
				new ExampleScript("Demonstrates getting your target while ignoring torches and bedrock",
				"msg(pcursor(array(50, 7)))",
				"{0: -127, 1: 75, 2: 798, 3: world, x: -127, y: 75, z: 798, world: world}"),
				new ExampleScript("Demonstrates getting Notch's target while ignoring air, water, and lava",
				"msg(pcursor('Notch', array(0, 8, 9, 10, 11)))",
				"{0: -127, 1: 75, 2: 798, 3: world, x: -127, y: 75, z: 798, world: world}")
			};
		}
	}

	@api
	public static class ptarget_space extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CRERangeException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if(args.length > 0) {
				p = Static.GetPlayer(args[0], t);
			}
			Static.AssertPlayerNonNull(p, t);
			MCBlock b = p.getTargetSpace(512);
			if(b == null) {
				throw new CRERangeException("No block in sight, or block too far", t);
			}
			return ObjectGenerator.GetGenerator().location(b.getLocation(), false);
		}

		@Override
		public String getName() {
			return "ptarget_space";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "array {[player]} Returns a location array of the space that the player is currently looking at."
					+ " This is the space where if they placed a block (and were close enough), it would end up going.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class pkill extends AbstractFunction {

		@Override
		public String getName() {
			return "pkill";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCPlayer m = null;
			if(args.length == 1) {
				m = Static.GetPlayer(args[0], t);
			} else {
				if(p instanceof MCPlayer) {
					m = (MCPlayer) p;
				}
			}
			Static.AssertPlayerNonNull(m, t);
			m.kill();
			return CVoid.VOID;
		}

		@Override
		public String docs() {
			return "void {[playerName]} Kills the specified player, or the current player if a name is omitted.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_0_1;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}
	}

	@api(environments = {CommandHelperEnvironment.class, GlobalEnv.class})
	public static class pgroup extends AbstractFunction {

		@Override
		public String getName() {
			return "pgroup";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCCommandSender sender;
			if(args.length == 0) {
				sender = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			} else {
				sender = Static.GetCommandSender(args[0].val(), t);
			}

			CArray ret = new CArray(t);
			if(sender != null) {
				for(String group : sender.getGroups()) {
					ret.push(new CString(group, t), t);
				}
			}
			return ret;
		}

		@Override
		public String docs() {
			return "array {[playerName]} Returns an array of the groups a player is in. If playerName is omitted,"
					+ " the current player is used. This relies on \"group.groupname\" permission nodes in your"
					+ " permissions plugin. Otherwise an extension is required to get the groups from the plugin.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_0_1;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}
	}

	@api(environments = {CommandHelperEnvironment.class, GlobalEnv.class})
	public static class pinfo extends AbstractFunction {

		@Override
		public String getName() {
			return "pinfo";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1, 2};
		}

		@Override
		public String docs() {
			return "mixed {[player], [value]} Returns various information about the player specified, or the current"
					+ " player if no argument was given. ----"
					+ " If value is set, it should be an integer of one of the following indexes, and only that"
					+ " information for that index will be returned. Otherwise if value is not specified (or is -1), it"
					+ " returns an array of values with the following pieces of information in the specified index:"
					+ "<ul>"
					+ "<li>0 - Player's name; This will return the player's exact name,"
					+ " even if called with a partial match.</li>"
					+ "<li>1 - Player's location; a location array of the player's coordinates</li>"
					+ "<li>2 - Player's cursor; a location array of the block the player is looking at,"
					+ " or null if no block is in sight.</li>"
					+ "<li>3 - Player's IP; Returns the IP address of this player.</li>"
					+ "<li>4 - Display name; The name that is typically used when displayed on screen.</li>"
					+ "<li>5 - Player's health; The current health of the player, which will be an int from 0-20.</li>"
					+ "<li>6 - Item in hand; The type of item in their main hand.</li>"
					+ "<li>7 - World name; Gets the name of the world this player is in.</li>"
					+ "<li>8 - Is Op; true or false if this player is an op.</li>"
					+ "<li>9 - Player groups; An array of the groups the player is in, by permission nodes.</li>"
					+ "<li>10 - The player's hostname (or IP if a hostname can't be found)</li>"
					+ "<li>11 - Is sneaking?</li><li>12 - Host; The host the player connected to.</li>"
					+ "<li>13 - Player UUID; (deprecated for index 20, but exists for backwards compatibility.)</li>"
					+ "<li>14 - Is player in a vehicle? Returns true or false.</li>"
					+ "<li>15 - Held Slot; The slot number of the player's current hand.</li>"
					+ "<li>16 - Is sleeping?</li>"
					+ "<li>17 - Is blocking?</li>"
					+ "<li>18 - Is flying?</li>"
					+ "<li>19 - Is sprinting?</li>"
					+ "<li>20 - Player UUID; The unique identifier for this player's account as returned by puuid()."
					+ "</ul>";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CRERangeException.class,
				CRECastException.class, CRENotFoundException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_1_0;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCCommandSender m = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			String player;
			int index;
			if(args.length == 0) {
				player = m instanceof MCPlayer ? m.getName() : null;
				index = -1;
			} else if(args.length == 1) {
				player = args[0].val();
				index = -1;
			} else {
				player = args[0].val();
				index = Static.getInt32(args[1], t);
			}
			MCPlayer p = Static.GetPlayer(player, t);

			Static.AssertPlayerNonNull(p, t);
			int maxIndex = 20;
			if(index < -1 || index > maxIndex) {
				throw new CRERangeException(this.getName() + " expects the index to be between -1 and " + maxIndex, t);
			}
			ArrayList<Mixed> retVals = new ArrayList<>();
			if(index == 0 || index == -1) {
				//MCPlayer name
				retVals.add(new CString(p.getName(), t));
			}
			if(index == 1 || index == -1) {
				//MCPlayer location
				MCLocation loc = p.getLocation();
				if(loc == null) {
					throw new CRENotFoundException(
							"Could not find the location of the player (are you running in cmdline mode?)", t);
				}
				retVals.add(new CArray(t,
						new CDouble(loc.getX(), t),
						new CDouble(loc.getY() - 1, t),
						new CDouble(loc.getZ(), t)));
			}
			if(index == 2 || index == -1) {
				//MCPlayer cursor
				MCBlock b;
				try {
					b = p.getTargetBlock(null, 200);
				} catch (IllegalStateException ise) {
					b = null;
				}
				if(b == null) {
					retVals.add(CNull.NULL);
				} else {
					retVals.add(new CArray(t, new CInt(b.getX(), t), new CInt(b.getY(), t), new CInt(b.getZ(), t)));
				}
			}
			if(index == 3 || index == -1) {
				//MCPlayer IP
				String add;
				try {
					add = p.getAddress().getAddress().getHostAddress();
				} catch (NullPointerException npe) {
					add = "";
				}

				retVals.add(new CString(add, t));
			}
			if(index == 4 || index == -1) {
				//Display name
				retVals.add(new CString(p.getDisplayName(), t));
			}
			if(index == 5 || index == -1) {
				//MCPlayer health
				retVals.add(new CDouble(p.getHealth(), t));
			}
			if(index == 6 || index == -1) {
				//Item in hand
				MCItemStack is = p.getInventory().getItemInMainHand();
				retVals.add(new CString(is.getType().getName(), t));
			}
			if(index == 7 || index == -1) {
				//World name
				retVals.add(new CString(p.getWorld().getName(), t));
			}
			if(index == 8 || index == -1) {
				//Is op
				retVals.add(CBoolean.get(p.isOp()));
			}
			if(index == 9 || index == -1) {
				//MCPlayer groups
				CArray a = new CArray(t);
				for(String group : p.getGroups()) {
					a.push(new CString(group, t), t);
				}
				retVals.add(a);
			}
			if(index == 10 || index == -1) {
				String hostname;
				try {
					hostname = p.getAddress().getAddress().getHostAddress();
				} catch (NullPointerException npe) {
					hostname = "";
				}

				if(CommandHelperPlugin.hostnameLookupCache.containsKey(p.getName())) {
					hostname = CommandHelperPlugin.hostnameLookupCache.get(p.getName());
				}

				retVals.add(new CString(hostname, t));
			}
			if(index == 11 || index == -1) {
				retVals.add(CBoolean.get(p.isSneaking()));
			}
			if(index == 12 || index == -1) {
				retVals.add(new CString(p.getHost(), t));
			}
			if(index == 13 || index == -1) {
				retVals.add(new CString(p.getUniqueId().toString(), t));
			}
			if(index == 14 || index == -1) {
				retVals.add(CBoolean.get(p.isInsideVehicle()));
			}
			if(index == 15 || index == -1) {
				retVals.add(new CInt(p.getInventory().getHeldItemSlot(), t));
			}
			if(index == 16 || index == -1) {
				retVals.add(CBoolean.get(p.isSleeping()));
			}
			if(index == 17 || index == -1) {
				retVals.add(CBoolean.get(p.isBlocking()));
			}
			if(index == 18 || index == -1) {
				retVals.add(CBoolean.get(p.isFlying()));
			}
			if(index == 19 || index == -1) {
				retVals.add(CBoolean.get(p.isSprinting()));
			}
			if(index == 20 || index == -1) {
				retVals.add(new CString(p.getUniqueId().toString(), t));
			}
			if(retVals.size() == 1) {
				return retVals.get(0);
			} else {
				CArray ca = new CArray(t);
				for(Mixed c : retVals) {
					ca.push(c, t);
				}
				return ca;
			}
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class pworld extends AbstractFunction {

		@Override
		public String getName() {
			return "pworld";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "string {[playerName]} Gets the world of the player specified,"
					+ " or the current player if playerName isn't specified.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_1_0;
		}

		@Override
		public Boolean runAsync() {
			return true;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCPlayer m = null;
			if(args.length == 0) {
				if(p instanceof MCPlayer) {
					m = (MCPlayer) p;
				}
			} else {
				m = Static.GetPlayer(args[0], t);
			}
			Static.AssertPlayerNonNull(m, t);
			return new CString(m.getWorld().getName(), t);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class pkick extends AbstractFunction {

		@Override
		public String getName() {
			return "pkick";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1, 2};
		}

		@Override
		public String docs() {
			return "void {[playerName], [message]} Kicks the specified player with an optional message."
					+ " If no message is specified, \"You have been kicked\" is used."
					+ " If no player is specified, the current player is used with the default message.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_1_0;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			String message = "You have been kicked";
			MCPlayer m = null;
			if(args.length == 0) {
				if(p instanceof MCPlayer) {
					m = (MCPlayer) p;
				}
			}
			if(args.length >= 1) {
				m = Static.GetPlayer(args[0], t);
			}
			if(args.length >= 2) {
				message = args[1].val();
			}
			MCPlayer ptok = m;
			Static.AssertPlayerNonNull(ptok, t);
			ptok.kickPlayer(message);
			return CVoid.VOID;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class display_name extends AbstractFunction {

		@Override
		public String getName() {
			return "display_name";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "string {[player]} Returns the display name of the player.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRELengthException.class, CREPlayerOfflineException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_2;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCPlayer m;
			if(args.length == 0) {
				m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
				Static.AssertPlayerNonNull(m, t);
			} else {
				m = Static.GetPlayer(args[0].val(), t);
			}
			return new CString(m.getDisplayName(), t);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_display_name extends AbstractFunction {

		@Override
		public String getName() {
			return "set_display_name";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "void {[playerName], newDisplayName} Sets a player's display name. If the first name isn't provided,"
					+ " it sets the display name of the player running the command. See reset_display_name() also."
					+ " All player functions expect the player's real name, not their display name.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_1_2;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCPlayer player;
			String name;
			if(args.length == 1) {
				player = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
				Static.AssertPlayerNonNull(player, t);
				name = args[0].val();
			} else {
				player = Static.GetPlayer(args[0], t);
				name = args[1].val();
			}
			player.setDisplayName(name);
			return CVoid.VOID;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class reset_display_name extends AbstractFunction {

		@Override
		public String getName() {
			return "reset_display_name";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "void {[playerName]} Resets a player's display name to their real name."
					+ " If playerName isn't specified, defaults to the player running the command.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_1_2;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCPlayer player;
			if(args.length == 0) {
				player = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
				Static.AssertPlayerNonNull(player, t);
			} else {
				player = Static.GetPlayer(args[0], t);
			}
			player.setDisplayName(player.getName());
			return CVoid.VOID;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class pfacing extends AbstractFunction {

		@Override
		public String getName() {
			return "pfacing";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1, 2, 3};
		}

		@Override
		public String docs() {
			return "mixed {F | yaw, pitch | player, F | player, yaw, pitch | player | &lt;none&gt;}"
					+ " Gets or sets the direction the player is facing."
					+ " ---- When using the first variation, expects an integer 0-3, which will set the direction the"
					+ " player faces using their existing pitch (up and down) but sets their yaw (left and right) to"
					+ " one of the cardinal directions, as follows: 0 - West, 1 - South, 2 - East, 3 - North, which"
					+ " corresponds to the directions given by F when viewed with F3."
					+ " In the second variation, specific yaw and pitches can be provided."
					+ " If the player is not specified, the current player is used."
					+ " If just the player is specified, that player's yaw and pitch are returned as an array,"
					+ " or if no arguments are given, the current player's yaw and pitch are returned as an array."
					+ " The function returns void when setting the values. (Note that while this"
					+ " function looks like it has ambiguous arguments, players cannot be named numbers.)"
					+ " A note on numbers: The values returned by the getter will always be as such:"
					+ " pitch will always be a number between 90 and -90, with -90 being the player looking up,"
					+ " and 90 being the player looking down. Yaw will always be a number between 0 and 359.9~."
					+ " When setting the facing, pitch must be a number between -90 and 90, and yaw may be any number."
					+ " If the number given is not between 0 and 359.9~, it will be normalized first."
					+ " 0 is dead west, 90 is north, etc.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CRERangeException.class,
				CRECastException.class, CRENotFoundException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_1_3;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			//Getter
			if(args.length == 0 || args.length == 1) {
				MCLocation l = null;
				if(args.length == 0) {
					if(p instanceof MCPlayer) {
						l = ((MCPlayer) p).getLocation();
					}
				} else {
					//if it's a number, we are setting F. Otherwise, it's a getter for the MCPlayer specified.
					if(!(args[0].isInstanceOf(CInt.TYPE))) {
						MCPlayer p2 = Static.GetPlayer(args[0], t);
						l = p2.getLocation();
					}
				}
				if(l != null) {
					// guarantee yaw in the 0 - 359.9~ range
					float yaw = l.getYaw() % 360.0f;
					if(yaw < 0.0f) {
						yaw += 360.0f;
					}
					float pitch = l.getPitch();
					return new CArray(t, new CDouble(yaw, t), new CDouble(pitch, t));
				}
			}
			//Setter
			MCPlayer toSet = null;
			float yaw = 0;
			float pitch = 0;
			if(args.length == 1) {
				//We are setting F for this MCPlayer
				if(p instanceof MCPlayer) {
					toSet = (MCPlayer) p;
					MCLocation loc = toSet.getLocation();
					if(loc == null) {
						throw new CRENotFoundException(
								"Could not find the location of the given player (are you running in cmdline mode?)", t);
					}
					pitch = loc.getPitch();
				}
				int g = Static.getInt32(args[0], t);
				if(g < 0 || g > 3) {
					throw new CRERangeException("The F specifed must be from 0 to 3", t);
				}
				yaw = g * 90;
			} else if(args.length == 2) {
				//Either we are setting this MCPlayer's pitch and yaw, or we are setting the specified MCPlayer's F.
				//Check to see if args[0] is a number
				try {
					yaw = (float) Static.getNumber(args[0], t);
					pitch = (float) Static.getNumber(args[1], t);
					//It's the yaw, pitch variation
					if(p instanceof MCPlayer) {
						toSet = (MCPlayer) p;
					}
				} catch (CRECastException e) {
					//It's the MCPlayer, F variation
					toSet = Static.GetPlayer(args[0], t);
					pitch = toSet.getLocation().getPitch();
					int g = Static.getInt32(args[1], t);
					if(g < 0 || g > 3) {
						throw new CRERangeException("The F specifed must be from 0 to 3", t);
					}
					yaw = g * 90;
				}
			} else if(args.length == 3) {
				//It's the MCPlayer, yaw, pitch variation
				toSet = Static.GetPlayer(args[0], t);
				yaw = (float) Static.getNumber(args[1], t);
				pitch = (float) Static.getNumber(args[2], t);
			}

			//Error check our data
			if(pitch > 90 || pitch < -90) {
				throw new CRERangeException("pitch must be between -90 and 90", t);
			}
			Static.AssertPlayerNonNull(toSet, t);
			MCLocation l = toSet.getLocation();
			if(l == null) {
				throw new CRENotFoundException(
						"Could not find the location of the player (are you running in cmdline mode?)", t);
			}
			l = l.clone();
			l.setPitch(pitch);
			l.setYaw(yaw);
			MCEntity vehicle = null;
			if(toSet.isInsideVehicle()) {
				vehicle = toSet.getVehicle();
			}
			toSet.teleport(l);
			if(vehicle != null) {
				vehicle.setPassenger(toSet);
			}
			return CVoid.VOID;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class pmode extends AbstractFunction {

		@Override
		public String getName() {
			return "pmode";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "string {[player]} Returns the player's game mode."
					+ " It will be one of " + StringUtils.Join(MCGameMode.values(), ", ", ", or ") + ".";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class,
				CRENotFoundException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_1_3;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCPlayer m = null;
			if(p instanceof MCPlayer) {
				m = (MCPlayer) p;
			}
			if(args.length == 1) {
				m = Static.GetPlayer(args[0], t);
			}

			Static.AssertPlayerNonNull(m, t);
			MCGameMode gm = m.getGameMode();
			if(gm == null) {
				throw new CRENotFoundException(
						"Could not find the gamemode of the given player (are you running in cmdline mode?)", t);
			}
			String mode = gm.name();
			return new CString(mode, t);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_pmode extends AbstractFunction {

		@Override
		public String getName() {
			return "set_pmode";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "void {[player], mode} Sets the player's game mode."
					+ " Mode must be one of: " + StringUtils.Join(MCGameMode.values(), ", ", ", or ");
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CREFormatException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_1_3;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCPlayer m = null;
			String mode;
			MCGameMode gm;
			if(p instanceof MCPlayer) {
				m = (MCPlayer) p;
			}
			if(args.length == 2) {
				m = Static.GetPlayer(args[0], t);
				mode = args[1].val();
			} else {
				mode = args[0].val();
			}

			try {
				gm = MCGameMode.valueOf(mode.toUpperCase());
			} catch (IllegalArgumentException e) {
				throw new CREFormatException("Mode must be either " + StringUtils.Join(MCGameMode.values(), ", ", ", or "), t);
			}
			Static.AssertPlayerNonNull(m, t);
			m.setGameMode(gm);
			return CVoid.VOID;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class pexp extends AbstractFunction {

		@Override
		public String getName() {
			return "pexp";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "int {[player]} Gets the experience of a player within this level, as a percentage, from 0 to 99."
					+ " (100 would be next level, therefore, 0.)";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREPlayerOfflineException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_1_3;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCPlayer m = null;
			if(p instanceof MCPlayer) {
				m = (MCPlayer) p;
			}
			if(args.length == 1) {
				m = Static.GetPlayer(args[0].val(), t);
			}
			Static.AssertPlayerNonNull(m, t);
			return new CInt(java.lang.Math.round(m.getExp() * 100), t);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_pexp extends AbstractFunction {

		@Override
		public String getName() {
			return "set_pexp";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "void {[player], xp} Sets the experience of a player within the current level, as a percentage,"
					+ " from 0 to 99. 100 resets the experience to zero and adds a level to the player.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREPlayerOfflineException.class, CRERangeException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_1_3;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCPlayer m = null;
			int xp;
			if(p instanceof MCPlayer) {
				m = (MCPlayer) p;
			}
			if(args.length == 2) {
				m = Static.GetPlayer(args[0].val(), t);
				xp = Static.getInt32(args[1], t);
			} else {
				xp = Static.getInt32(args[0], t);
			}
			Static.AssertPlayerNonNull(m, t);
			if(xp < 0 || xp > 100) {
				throw new CRERangeException("Experience percentage must be from 0 to 100.", t);
			}
			m.setExp(((float) xp) / 100.0F);
			return CVoid.VOID;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class give_pexp extends AbstractFunction {

		@Override
		public String getName() {
			return "give_pexp";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "void {[player], exp} Gives the player the specified amount of experience.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return true;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCCommandSender p = environment.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCPlayer m = null;
			int xp;
			if(p instanceof MCPlayer) {
				m = (MCPlayer) p;
			}
			if(args.length == 2) {
				m = Static.GetPlayer(args[0].val(), t);
				xp = Static.getInt32(args[1], t);
			} else {
				xp = Static.getInt32(args[0], t);
			}
			Static.AssertPlayerNonNull(m, t);
			m.giveExp(xp);

			return CVoid.VOID;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class plevel extends AbstractFunction {

		@Override
		public String getName() {
			return "plevel";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "int {[player]} Gets the player's level.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREPlayerOfflineException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_1_3;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCPlayer m = null;
			if(p instanceof MCPlayer) {
				m = (MCPlayer) p;
			}
			if(args.length == 1) {
				m = Static.GetPlayer(args[0].val(), t);
			}
			Static.AssertPlayerNonNull(m, t);
			return new CInt(m.getLevel(), t);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_plevel extends AbstractFunction {

		@Override
		public String getName() {
			return "set_plevel";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "void {[player], level} Sets the level of a player.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREPlayerOfflineException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_1_3;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCPlayer m = null;
			int level;
			if(p instanceof MCPlayer) {
				m = (MCPlayer) p;
			}
			if(args.length == 2) {
				m = Static.GetPlayer(args[0].val(), t);
				level = Static.getInt32(args[1], t);
			} else {
				level = Static.getInt32(args[0], t);
			}
			Static.AssertPlayerNonNull(m, t);
			m.setLevel(level);
			return CVoid.VOID;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class ptexp extends AbstractFunction {

		@Override
		public String getName() {
			return "ptexp";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "int {[player]} Gets the total experience of a player.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREPlayerOfflineException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_1_3;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCPlayer m = null;
			if(p instanceof MCPlayer) {
				m = (MCPlayer) p;
			}
			if(args.length == 1) {
				m = Static.GetPlayer(args[0].val(), t);
			}
			Static.AssertPlayerNonNull(m, t);
			int texp = m.getExpAtLevel() + java.lang.Math.round(m.getExpToLevel() * m.getExp());
			return new CInt(texp, t);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_ptexp extends AbstractFunction {

		@Override
		public String getName() {
			return "set_ptexp";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "void {[player], exp} Sets the total experience of a player.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREPlayerOfflineException.class, CRERangeException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_1_3;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCPlayer m = null;
			int xp;
			if(p instanceof MCPlayer) {
				m = (MCPlayer) p;
			}
			if(args.length == 2) {
				m = Static.GetPlayer(args[0].val(), t);
				xp = Static.getInt32(args[1], t);
			} else {
				xp = Static.getInt32(args[0], t);
			}
			if(xp < 0) {
				throw new CRERangeException("Experience can't be negative", t);
			}
			Static.AssertPlayerNonNull(m, t);
			int score = m.getTotalExperience();
			m.setLevel(0);
			m.setExp(0);
			m.giveExp(xp);
			m.setTotalExperience(score); // reset experience score so that this function does not affect it
			return CVoid.VOID;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class pfood extends AbstractFunction {

		@Override
		public String getName() {
			return "pfood";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "int {[player]} Returns the player's current food level.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_1_3;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCPlayer m = null;
			if(p instanceof MCPlayer) {
				m = (MCPlayer) p;
			}
			if(args.length == 1) {
				m = Static.GetPlayer(args[0].val(), t);
			}
			Static.AssertPlayerNonNull(m, t);
			return new CInt(m.getFoodLevel(), t);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_pfood extends AbstractFunction {

		@Override
		public String getName() {
			return "set_pfood";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "void {[player], level} Sets the player's food level. This is an integer from 0-?";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_1_3;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCPlayer m = null;
			int level;
			if(p instanceof MCPlayer) {
				m = (MCPlayer) p;
			}
			if(args.length == 2) {
				m = Static.GetPlayer(args[0].val(), t);
				level = Static.getInt32(args[1], t);
			} else {
				level = Static.getInt32(args[0], t);
			}
			Static.AssertPlayerNonNull(m, t);
			m.setFoodLevel(level);
			return CVoid.VOID;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_peffect extends AbstractFunction {

		@Override
		public String getName() {
			return "set_peffect";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3, 4, 5, 6, 7};
		}

		@Override
		public String docs() {
			return "boolean {player, potionEffect, [strength], [seconds], [ambient], [particles], [icon]}"
					+ " Adds one, or modifies an existing, potion effect on a mob."
					+ " The potionEffect can be " + StringUtils.Join(MCPotionEffectType.types(), ", ", ", or ", " or ")
					+ ". It also accepts an integer corresponding to the effect id listed on the Minecraft wiki."
					+ " Strength is an integer representing the power level of the effect, starting at 0."
					+ " Seconds defaults to 30.0. To remove an effect, set the seconds to 0."
					+ " If seconds is less than 0 or greater than 107374182 a RangeException is thrown."
					+ " Ambient takes a boolean of whether the particles should be more transparent."
					+ " Particles takes a boolean of whether the particles should be visible at all."
					+ " Icon takes a boolean for whether or not to show the icon to the player."
					+ " The function returns whether or not the effect was modified.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CRECastException.class,
				CRERangeException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCPlayer m = Static.GetPlayer(args[0].val(), t);

			MCPotionEffectType type = null;
			if(args[1].isInstanceOf(CString.TYPE)) {
				try {
					type = MCPotionEffectType.valueOf(args[1].val().toUpperCase());
				} catch (IllegalArgumentException ex) {
					// maybe it's a number id
				}
			}
			if(type == null) {
				try {
					type = MCPotionEffectType.getById(Static.getInt32(args[1], t));
				} catch (CRECastException | IllegalArgumentException ex) {
					throw new CREFormatException("Invalid potion effect type: " + args[1].val(), t);
				}
			}

			int strength = 0;
			double seconds = 30.0;
			boolean ambient = false;
			boolean particles = true;
			boolean icon = true;
			if(args.length >= 3) {
				strength = Static.getInt32(args[2], t);

				if(args.length >= 4) {
					seconds = Static.getDouble(args[3], t);
					if(seconds < 0.0) {
						throw new CRERangeException("Seconds cannot be less than 0.0", t);
					} else if(seconds * 20 > Integer.MAX_VALUE) {
						throw new CRERangeException("Seconds cannot be greater than 107374182.0", t);
					}

					if(args.length >= 5) {
						ambient = ArgumentValidation.getBoolean(args[4], t);

						if(args.length >= 6) {
							particles = ArgumentValidation.getBoolean(args[5], t);

							if(args.length == 7) {
								icon = ArgumentValidation.getBoolean(args[6], t);
							}
						}
					}
				}
			}

			if(seconds == 0.0) {
				return CBoolean.get(m.removeEffect(type));
			} else {
				return CBoolean.get(m.addEffect(type, strength, (int) (seconds * 20), ambient, particles, icon));
			}
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Give player Notch nausea for 30 seconds",
				"set_peffect('Notch', 'NAUSEA')",
				"The player will experience a wobbly screen."),
				new ExampleScript("Make player ArenaPlayer unable to jump for 10 minutes",
				"set_peffect('ArenaPlayer', 'JUMP_BOOST', -16, 600)",
				"From the player's perspective, they will not even leave the ground."),
				new ExampleScript("Remove poison from yourself",
				"set_peffect(player(), 'POISON', 1, 0)",
				"You are now unpoisoned. Note, it does not matter what you set strength to here.")
			};
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_peffect extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if(args.length > 0) {
				p = Static.GetPlayer(args[0], t);
			}
			Static.AssertPlayerNonNull(p, t);
			return ObjectGenerator.GetGenerator().potions(p.getEffects(), t);
		}

		@Override
		public String getName() {
			return "get_peffect";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "array {[player]} Returns an array of potion effects that are currently active on a given player."
					+ " The array can contain potion effect objects, with the key defining the type of potion effect."
					+ " The arrays contain the following fields: \"id\","
					+ " \"strength\", \"seconds\" remaining, whether the effect is \"ambient\", whether"
					+ " \"particles\" are enabled, and whether the \"icon\" is shown to the player.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class clear_peffects extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CRELengthException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p;
			if(args.length == 0) {
				p = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
				Static.AssertPlayerNonNull(p, t);
			} else {
				p = Static.GetPlayer(args[0], t);
			}
			p.removeEffects();
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "clear_peffects";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "void {[player]} Removes all potion effects from a player.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_2;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class psneaking extends AbstractFunction {

		@Override
		public String getName() {
			return "psneaking";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "boolean {[player]} Returns whether or not the player is sneaking.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRELengthException.class, CREPlayerOfflineException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_2;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCPlayer m;
			if(args.length == 0) {
				m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
				Static.AssertPlayerNonNull(m, t);
			} else {
				m = Static.GetPlayer(args[0].val(), t);
			}
			return CBoolean.get(m.isSneaking());
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class phealth extends AbstractFunction {

		@Override
		public String getName() {
			return "phealth";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "double {[player]} Gets the player's health.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRELengthException.class, CREPlayerOfflineException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_2;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCPlayer m;
			if(args.length == 0) {
				m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
				Static.AssertPlayerNonNull(m, t);
			} else {
				m = Static.GetPlayer(args[0].val(), t);
			}
			return new CDouble(m.getHealth(), t);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_phealth extends AbstractFunction {

		@Override
		public String getName() {
			return "set_phealth";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "void {[player], health} Sets the player's health."
					+ " Health should be a double between 0 and their max health, which is 20.0 by default.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CRERangeException.class, CREPlayerOfflineException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_2_0;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCPlayer m = null;
			if(p instanceof MCPlayer) {
				m = (MCPlayer) p;
			}
			double health;
			if(args.length == 2) {
				m = Static.GetPlayer(args[0].val(), t);
				health = Static.getDouble(args[1], t);
			} else {
				health = Static.getDouble(args[0], t);
			}
			Static.AssertPlayerNonNull(m, t);
			if(health < 0 || health > m.getMaxHealth()) {
				throw new CRERangeException("Health must be between 0 and the player's max health (currently "
						+ m.getMaxHealth() + " for " + m.getName() + ").", t);
			}
			m.setHealth(health);
			return CVoid.VOID;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class ponline extends AbstractFunction {

		@Override
		public String getName() {
			return "ponline";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {player} Returns whether or not the specified player is online."
					+ " Note that the name must match exactly, but it will not throw a PlayerOfflineException"
					+ " if the player is not online, or if the player doesn't even exist.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			try {
				//Static.GetPlayer() autocompletes names, which we don't want in this function,
				//however we have to check if this is an injected player first.
				MCPlayer p = Static.GetPlayer(args[0], t);
				//Now we must check if the name was exact. Skip this if the argument is a UUID.
				if(args[0].val().length() <= 16 && !p.getName().equalsIgnoreCase(args[0].val())) {
					p = Static.getServer().getPlayerExact(args[0].val());
				}
				return CBoolean.get(p != null);
			} catch (ConfigRuntimeException e) {
				//They aren't in the player list
				return CBoolean.FALSE;
			}
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class pwhitelisted extends AbstractFunction {

		@Override
		public String getName() {
			return "pwhitelisted";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {player} Returns whether or not this player is whitelisted."
					+ " This will work with offline players, but the name must be exact. ---- " + UUID_WARNING;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCOfflinePlayer pl = Static.GetUser(args[0].val(), t);
			return CBoolean.get(pl != null && pl.isWhitelisted());
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_pwhitelisted extends AbstractFunction {

		@Override
		public String getName() {
			return "set_pwhitelisted";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "void {player, isWhitelisted} Sets the whitelist flag of the specified player."
					+ " This will work with offline players, but the name must be exact. ---- " + UUID_WARNING;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRENotFoundException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCOfflinePlayer pl = Static.GetUser(args[0].val(), t);
			boolean whitelist = ArgumentValidation.getBoolean(args[1], t);
			if(pl == null) {
				throw new CRENotFoundException(
						this.getName() + " could not get the offline player (are you running in cmdline mode?)", t);
			}
			pl.setWhitelisted(whitelist);
			return CVoid.VOID;
		}
	}

	private static final String UUID_WARNING = " NOTICE: This function accepts UUIDs in place of player names,"
			+ " however due to lack of API from Mojang, some server software is not able to"
			+ " correctly associate a UUID with a player if the player has not recently been online."
			+ " As such, it may not always be possible to ban or whitelist a player by UUID."
			+ " Servers known to have this problem are Bukkit and Spigot. Furthermore,"
			+ " although this API functions, due to the limitations of the vanilla ban/whitelist"
			+ " system, it is recommended to use a 3rd party system or write your own.";

	@api(environments = {CommandHelperEnvironment.class})
	public static class pbanned extends AbstractFunction {

		@Override
		public String getName() {
			return "pbanned";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {player} Returns whether or not this player is banned."
					+ " This will work with offline players, but the name must be exact."
					+ " At this time, this function only works with the vanilla ban system."
					+ " If you use a third party ban system, you should instead run the command for that"
					+ " plugin instead. ---- " + UUID_WARNING;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRENotFoundException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCOfflinePlayer pl = Static.GetUser(args[0].val(), t);
			if(pl == null) {
				throw new CRENotFoundException(
						this.getName() + " could not get the offline player (are you running in cmdline mode?)", t);
			}
			return CBoolean.get(pl.isBanned());
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_pbanned extends AbstractFunction {

		@Override
		public String getName() {
			return "set_pbanned";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3, 4};
		}

		@Override
		public String docs() {
			return "void {player, isBanned, [reason], [source]} Sets the ban flag for the specified player."
					+ " This will work with offline players, but the name must be exact. When banning,"
					+ " a reason message may be provided that the player will see when attempting to login."
					+ " An optional source may also be provided that indicates who or what banned the player."
					+ " At this time, this function only works with the vanilla ban system."
					+ " If you use a third party ban system, you should instead run the command for that"
					+ " plugin instead. ---- " + UUID_WARNING;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRENotFoundException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			String target = args[0].val();
			boolean ban = ArgumentValidation.getBoolean(args[1], t);
			String reason = "";
			String source = "";

			if(target.length() > 16) {
				MCOfflinePlayer pl = Static.GetUser(target, t);
				if(pl == null) {
					throw new CRENotFoundException(
							this.getName() + " could not get the offline player (are you running in cmdline mode?)", t);
				}
				target = pl.getName();
				if(target == null) {
					throw new CRENotFoundException(this.getName() + " could not get offline player's name", t);
				}
			}

			if(ban) {
				if(args.length > 2) {
					reason = Construct.nval(args[2]);
					if(args.length == 4) {
						source = Construct.nval(args[3]);
					}
				}
				Static.getServer().banName(target, reason, source);
			} else {
				Static.getServer().unbanName(target);
			}
			return CVoid.VOID;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_pwalkspeed extends AbstractFunction {

		@Override
		public String getName() {
			return "set_pwalkspeed";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "void {[player], speed} Sets a player's walk speed. The speed must be between -1.0 and 1.0."
					+ " The default player walk speed is 0.2.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CRERangeException.class, CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCPlayer m = null;
			double speed;

			if(p instanceof MCPlayer) {
				m = (MCPlayer) p;
			}
			if(args.length == 2) {
				m = Static.GetPlayer(args[0], t);
				speed = Static.getDouble(args[1], t);
			} else {
				speed = Static.getDouble(args[0], t);
			}

			if(speed < -1 || speed > 1) {
				throw new CRERangeException("Speed must be between -1 and 1", t);
			}
			Static.AssertPlayerNonNull(m, t);

			m.setWalkSpeed((float) speed);
			return CVoid.VOID;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_pwalkspeed extends AbstractFunction {

		@Override
		public String getName() {
			return "get_pwalkspeed";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "double {[player]} Gets a player's walk speed. The speed will be between -1.0 and 1.0.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CREFormatException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCPlayer m = null;

			if(p instanceof MCPlayer) {
				m = (MCPlayer) p;
			}

			if(args.length == 1) {
				m = Static.GetPlayer(args[0], t);
			}

			Static.AssertPlayerNonNull(m, t);

			return new CDouble(m.getWalkSpeed(), t);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_pflyspeed extends AbstractFunction {

		@Override
		public String getName() {
			return "set_pflyspeed";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "void {[player], speed} Sets a player's fly speed. The speed must be between -1.0 and 1.0."
					+ " The default player fly speed is 0.1.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CRERangeException.class, CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCPlayer m = null;
			double speed;

			if(p instanceof MCPlayer) {
				m = (MCPlayer) p;
			}
			if(args.length == 2) {
				m = Static.GetPlayer(args[0], t);
				speed = Static.getDouble(args[1], t);
			} else {
				speed = Static.getDouble(args[0], t);
			}

			if(speed < -1 || speed > 1) {
				throw new CRERangeException("Speed must be between -1 and 1", t);
			}
			Static.AssertPlayerNonNull(m, t);

			m.setFlySpeed((float) speed);
			return CVoid.VOID;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_pflyspeed extends AbstractFunction {

		@Override
		public String getName() {
			return "get_pflyspeed";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "double {[player]} Gets a player's fly speed. The speed will be between -1.0 and 1.0.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CREFormatException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCPlayer m = null;

			if(p instanceof MCPlayer) {
				m = (MCPlayer) p;
			}

			if(args.length == 1) {
				m = Static.GetPlayer(args[0], t);
			}

			Static.AssertPlayerNonNull(m, t);

			return new CDouble(m.getFlySpeed(), t);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class pisop extends AbstractFunction {

		@Override
		public String getName() {
			return "pisop";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "boolean {[player]} Returns whether or not the player is op.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer m = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if(args.length == 1) {
				m = Static.GetPlayer(args[0].val(), t);
			}
			Static.AssertPlayerNonNull(m, t);
			return CBoolean.get(m.isOp());
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_compass_target extends AbstractFunction {

		@Override
		public String getName() {
			return "set_compass_target";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "array {[player], locationArray} Sets the player's compass target, and returns the old location.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CREFormatException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer m = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			MCLocation l;
			if(args.length == 1) {
				l = ObjectGenerator.GetGenerator().location(args[0], null, t);
			} else {
				m = Static.GetPlayer(args[0].val(), t);
				l = ObjectGenerator.GetGenerator().location(args[1], null, t);
			}
			if(m == null) {
				throw new CREPlayerOfflineException("That player is not online", t);
			}
			Static.AssertPlayerNonNull(m, t);
			MCLocation old = m.getCompassTarget();
			m.setCompassTarget(l);
			return ObjectGenerator.GetGenerator().location(old);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_compass_target extends AbstractFunction {

		@Override
		public String getName() {
			return "get_compass_target";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "array {[player]} Gets the compass target location for the specified player.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer m = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if(args.length == 1) {
				m = Static.GetPlayer(args[0].val(), t);
			}
			Static.AssertPlayerNonNull(m, t);
			return ObjectGenerator.GetGenerator().location(m.getCompassTarget(), false);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class ponfire extends AbstractFunction {

		@Override
		public String getName() {
			return "ponfire";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "int {[player]} Returns the number of ticks remaining that this player will be on fire for."
					+ " If the player is not on fire, 0 is returned, which evaluates as false.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if(args.length == 1) {
				p = Static.GetPlayer(args[0], t);
			}
			Static.AssertPlayerNonNull(p, t);
			int left = p.getRemainingFireTicks();
			return new CInt(left, t);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_ponfire extends AbstractFunction {

		@Override
		public String getName() {
			return "set_ponfire";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "void {[player], ticks} Sets the player on fire for the specified number of ticks."
					+ " If a boolean is given for ticks, false is 0, and true is 20.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			Mixed ticks;
			if(args.length == 2) {
				p = Static.GetPlayer(args[0], t);
				ticks = args[1];
			} else {
				ticks = args[0];
			}
			int tick = 0;
			if(ticks.isInstanceOf(CBoolean.TYPE)) {
				boolean value = ((CBoolean) ticks).getBoolean();
				if(value) {
					tick = 20;
				}
			} else {
				tick = Static.getInt32(ticks, t);
			}
			Static.AssertPlayerNonNull(p, t);
			p.setRemainingFireTicks(tick);
			return CVoid.VOID;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class phas_flight extends AbstractFunction {

		@Override
		public String getName() {
			return "phas_flight";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "boolean {[player]} Returns whether or not the player has the ability to fly.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if(args.length == 1) {
				p = Static.GetPlayer(args[0], t);
			}
			Static.AssertPlayerNonNull(p, t);
			return CBoolean.get(p.getAllowFlight());
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	@seealso({set_pflying.class})
	public static class set_pflight extends AbstractFunction {

		@Override
		public String getName() {
			return "set_pflight";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "void {[player], flight} Sets whether or not this player is allowed to fly.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			boolean flight;
			if(args.length == 1) {
				flight = ArgumentValidation.getBoolean(args[0], t);
			} else {
				p = Static.GetPlayer(args[0], t);
				flight = ArgumentValidation.getBoolean(args[1], t);
			}
			Static.AssertPlayerNonNull(p, t);
			p.setAllowFlight(flight);
			return CVoid.VOID;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}
	private static final SortedMap<String, CString> TIME_LOOKUP = new TreeMap<>();

	static {
		Properties p = new Properties();
		try {
			p.load(Minecraft.class.getResourceAsStream("/time_names.txt"));
			Enumeration e = p.propertyNames();
			while(e.hasMoreElements()) {
				String name = e.nextElement().toString();
				TIME_LOOKUP.put(name, new CString(p.getProperty(name), Target.UNKNOWN));
			}
		} catch (IOException ex) {
			Logger.getLogger(World.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_ptime extends AbstractFunction {

		@Override
		public String getName() {
			return "set_ptime";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3};
		}

		@Override
		public String docs() {
			StringBuilder doc = new StringBuilder();
			doc.append("void {[player], time, [relative]} Sets the time of a given player. Relative defaults to false,"
					+ " but if true, the time will be an offset and the player's time will still progress."
					+ " Otherwise it will be locked and should be a number from 0 to 24000, else it is modulo scaled."
					+ " Alternatively, common time notation (9:30pm, 4:00 am) is acceptable,"
					+ " and convenient english mappings also exist:");
			doc.append("<ul>");
			for(String key : TIME_LOOKUP.keySet()) {
				doc.append("<li>").append(key).append(" = ").append(TIME_LOOKUP.get(key)).append("</li>");
			}
			doc.append("</ul>");
			return doc.toString();
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CREFormatException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = null;
			boolean relative = false;
			if(environment.getEnv(CommandHelperEnvironment.class).GetPlayer() != null) {
				p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			}
			if(args.length >= 2) {
				p = Static.GetPlayer(args[0], t);
			}
			if(args.length == 3) {
				relative = ArgumentValidation.getBoolean(args[2], t);
			}
			Static.AssertPlayerNonNull(p, t);
			long time = 0;
			String stime = (args.length == 1 ? args[0] : args[1]).val().toLowerCase();
			if(TIME_LOOKUP.containsKey(stime.replaceAll("[^a-z]", ""))) {
				stime = TIME_LOOKUP.get(stime.replaceAll("[^a-z]", "")).val();
			}
			if(stime.matches("^([\\d]+)[:.]([\\d]+)[ ]*?(?:([pa])\\.*m\\.*){0,1}$")) {
				Pattern pa = Pattern.compile("^([\\d]+)[:.]([\\d]+)[ ]*?(?:([pa])\\.*m\\.*){0,1}$");
				Matcher m = pa.matcher(stime);
				m.find();
				int hour = Integer.parseInt(m.group(1));
				int minute = Integer.parseInt(m.group(2));
				String offset = "a";
				if(m.group(3) != null) {
					offset = m.group(3);
				}
				if(offset.equals("p")) {
					hour += 12;
				}
				if(hour == 24) {
					hour = 0;
				}
				if(hour > 24) {
					throw new CREFormatException("Invalid time provided", t);
				}
				if(minute > 59) {
					throw new CREFormatException("Invalid time provided", t);
				}
				hour -= 6;
				hour = hour % 24;
				long ttime = hour * 1000;
				ttime += ((minute / 60.0) * 1000);
				stime = Long.toString(ttime);
			}
			try {
				Long.valueOf(stime);
			} catch (NumberFormatException e) {
				throw new CREFormatException("Invalid time provided", t);
			}
			time = Long.parseLong(stime);
			p.setPlayerTime(time, relative);
			return CVoid.VOID;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class pget_time extends AbstractFunction {

		@Override
		public String getName() {
			return "pget_time";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "int {[player]} Returns the time of the specified player, as an integer from 0 to 24000-1";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = null;
			if(environment.getEnv(CommandHelperEnvironment.class).GetPlayer() != null) {
				p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			}
			if(args.length == 1) {
				p = Static.GetPlayer(args[0], t);
			}
			Static.AssertPlayerNonNull(p, t);
			return new CInt(p.getPlayerTime(), t);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class preset_time extends AbstractFunction {

		@Override
		public String getName() {
			return "preset_time";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "void {[player]} Resets the visible time for the player to the time of the world.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInvalidWorldException.class, CREPlayerOfflineException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = null;
			if(environment.getEnv(CommandHelperEnvironment.class).GetPlayer() != null) {
				p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			}
			if(args.length == 1) {
				p = Static.GetPlayer(args[0], t);
			}
			Static.AssertPlayerNonNull(p, t);
			p.resetPlayerTime();
			return CVoid.VOID;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class phas_storm extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer m = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if(args.length == 1) {
				m = Static.GetPlayer(args[0], t);
			}
			Static.AssertPlayerNonNull(m, t);
			return CBoolean.get(m.getPlayerWeather() == MCWeather.DOWNFALL);
		}

		@Override
		public String getName() {
			return "phas_storm";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "boolean {[player]} Returns true if the given player is experiencing a storm, as set by"
					+ " set_pstorm(). (ignores world weather)";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_pstorm extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer m = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			int offset = 0;
			if(args.length == 2) {
				m = Static.GetPlayer(args[0], t);
				offset = 1;
			}
			Static.AssertPlayerNonNull(m, t);
			if(args[offset] instanceof CNull) {
				m.resetPlayerWeather();
			} else if(ArgumentValidation.getBoolean(args[offset], t)) {
				m.setPlayerWeather(MCWeather.DOWNFALL);
			} else {
				m.setPlayerWeather(MCWeather.CLEAR);
			}
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "set_pstorm";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "void {[player], downFall} Sets the weather for the given player only. If downFall is true, the"
					+ " player will experience a storm. If downFall is null, it will reset the player's visible weather"
					+ " to that which the player's world is experiencing.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_list_name extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CRELengthException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer m;
			String listName;
			if(args.length == 2) {
				m = Static.GetPlayer(args[0], t);
				listName = Construct.nval(args[1]);
			} else {
				m = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
				Static.AssertPlayerNonNull(m, t);
				listName = Construct.nval(args[0]);
			}
			m.setPlayerListName(listName);
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "set_list_name";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "void {[player], [listName]} Sets the player's list name."
					+ " Colors are supported and setting the name to null resets it.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_list_name extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if(args.length == 1) {
				p = Static.GetPlayer(args[0], t);
			}
			Static.AssertPlayerNonNull(p, t);
			return new CString(p.getPlayerListName(), t);
		}

		@Override
		public String getName() {
			return "get_list_name";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "string {[player]} Returns the name of the player that's display on the player list.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class pvelocity extends AbstractFunction {

		@Override
		public String getName() {
			return "pvelocity";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "array {[player]} Returns an associative array that represents the player's velocity."
					+ " The array contains the following items: magnitude, x, y, z. These represent a"
					+ " 3 dimensional Vector. The important part is x, y, z, however, the magnitude is provided"
					+ " for you as a convenience. (It should equal sqrt(x ** 2 + y ** 2 + z ** 2))";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CRENotFoundException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p;
			if(args.length == 1) {
				p = Static.GetPlayer(args[0], t);
			} else {
				p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
				Static.AssertPlayerNonNull(p, t);
			}
			Vector3D velocity = p.getVelocity();
			if(velocity == null) {
				throw new CRENotFoundException(
						"The players velocity could not be found (Are you running in cmdline mode?)", t);
			}
			CArray vector = ObjectGenerator.GetGenerator().vector(velocity, t);
			vector.set("magnitude", new CDouble(velocity.length(), t), t);
			return vector;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_pvelocity extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREPlayerOfflineException.class, CREFormatException.class,
					CREIllegalArgumentException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			Vector3D v;
			int offset = 0;
			switch(args.length) {
				case 1:
				case 2: {
					if(args.length == 2) {
						offset = 1;
						p = Static.GetPlayer(args[0], t);
					}
					v = ObjectGenerator.GetGenerator().vector(args[offset], t);
					break;
				}
				case 3:
				case 4: {
					if(args.length == 4) {
						offset = 1;
						p = Static.GetPlayer(args[0], t);
					}
					double x = Static.getDouble(args[offset], t);
					double y = Static.getDouble(args[offset + 1], t);
					double z = Static.getDouble(args[offset + 2], t);
					v = new Vector3D(x, y, z);
					break;
				}
				default:
					throw new RuntimeException();
			}
			if(v.length() > 10) {
				MSLog.GetLogger().Log(MSLog.Tags.GENERAL, LogLevel.WARNING,
						"The call to " + getName() + " has been cancelled, because the magnitude was greater than 10."
						+ " (It was " + v.length() + ")", t);
				return CBoolean.FALSE;
			}
			Static.AssertPlayerNonNull(p, t);
			try {
				p.setVelocity(v);
			} catch (IllegalArgumentException ex) {
				throw new CREIllegalArgumentException(ex.getMessage(), t);
			}
			return CBoolean.TRUE;
		}

		@Override
		public String getName() {
			return "set_pvelocity";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3, 4};
		}

		@Override
		public String docs() {
			return "boolean {[player], vector | [player], x, y, z} Sets a player's velocity. vector must be an"
					+ " associative array with x, y, and z keys defined (if magnitude is set, it is ignored)."
					+ " If the vector's magnitude is greater than 10, the command is cancelled, because the"
					+ " server won't allow the player to move faster than that. A warning is issued, and false"
					+ " is returned if this happens, otherwise, true is returned.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class psend_sign_text extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class, CREPlayerOfflineException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			int offset = 0;
			if(args.length == 3 || args.length == 6) {
				p = Static.GetPlayer(args[0], t);
				offset = 1;
			}
			Static.AssertPlayerNonNull(p, t);
			MCLocation loc = ObjectGenerator.GetGenerator().location(args[offset], p.getWorld(), t);

			String[] lines = new String[4];

			if(args.length == 2 || args.length == 3) {
				//Lines are in an array
				CArray lineArray = Static.getArray(args[1 + offset], t);
				if(lineArray.size() != 4) {
					throw new CRECastException("Line array must have 4 elements.", t);
				}
				lines[0] = lineArray.get(0, t).val();
				lines[1] = lineArray.get(1, t).val();
				lines[2] = lineArray.get(2, t).val();
				lines[3] = lineArray.get(3, t).val();
			} else {
				//Lines are in different arguments
				lines[0] = args[1 + offset].val();
				lines[1] = args[2 + offset].val();
				lines[2] = args[3 + offset].val();
				lines[3] = args[4 + offset].val();
			}

			p.sendSignTextChange(loc, lines);
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "psend_sign_text";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3, 5, 6};
		}

		@Override
		public String docs() {
			return "void {[player], locationArray, 1, 2, 3, 4 | [player], locationArray, lineArray}"
					+ " Changes a sign's text only for the specified player. This change does not persist."
					+ " This can be used to \"fake\" sign text for a player. LineArray, if used, must have 4 elements.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class psend_block_change extends AbstractFunction implements Optimizable {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class, CREPlayerOfflineException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			int offset = 0;
			if(args.length == 3) {
				p = Static.GetPlayer(args[0], t);
				offset = 1;
			}
			Static.AssertPlayerNonNull(p, t);
			MCLocation loc = ObjectGenerator.GetGenerator().location(args[offset], p.getWorld(), t);
			MCBlockData data;
			try {
				data = StaticLayer.GetServer().createBlockData(args[1 + offset].val().toLowerCase());
			} catch (IllegalArgumentException ex) {
				String value = args[1 + offset].val();
				if(value.contains(":") && value.length() <= 6) {
					data = Static.ParseItemNotation(getName(), args[1 + offset].val(), 1, t).getType().createBlockData();
				} else {
					throw new CREFormatException("Invalid block format: " + value, t);
				}
			}
			if(!data.getMaterial().isBlock()) {
				throw new CREIllegalArgumentException("The value \"" + args[1 + offset].val()
						+ "\" is not a valid block material.", t);
			}
			p.sendBlockChange(loc, data);
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "psend_block_change";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public String docs() {
			return "void {[player], locationArray, block} Changes a block temporarily for the specified player."
					+ " This can be used to \"fake\" blocks for a player. These illusory blocks will disappear when"
					+ " the client updates them, most often by clicking on them or reloading the chunks."
					+ " A block type or blockdata format is supported. (see set_blockdata_string())";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {
			if(children.size() < 2) {
				return null;
			}
			String value = children.get(children.size() - 1).getData().val();
			if(value.contains(":") && value.length() <= 6) { // longest valid item format without being blockdata string
				MSLog.GetLogger().w(MSLog.Tags.DEPRECATION, "The 1:1 format is deprecated in psend_block_change()", t);
			}
			return null;
		}

		@Override
		public Set<Optimizable.OptimizationOption> optimizationOptions() {
			return EnumSet.of(Optimizable.OptimizationOption.OPTIMIZE_DYNAMIC);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class phunger extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if(args.length == 1) {
				p = Static.GetPlayer(args[0], t);
			}
			Static.AssertPlayerNonNull(p, t);
			return new CInt(p.getFoodLevel(), t);
		}

		@Override
		public String getName() {
			return "phunger";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "int {[player]} Returns the player's hunger level.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_phunger extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRERangeException.class, CREPlayerOfflineException.class, CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			int hungerIndex = 0;
			if(args.length == 2) {
				p = Static.GetPlayer(args[0], t);
				hungerIndex = 1;
			}
			Static.AssertPlayerNonNull(p, t);
			int hunger = Static.getInt32(args[hungerIndex], t);
			p.setFoodLevel(hunger);
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "set_phunger";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "void {[player], hunger} Sets a player's hunger level, where 0 is empty and 20 is full.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class psaturation extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRERangeException.class, CREPlayerOfflineException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if(args.length == 1) {
				p = Static.GetPlayer(args[0], t);
			}
			Static.AssertPlayerNonNull(p, t);
			return new CDouble(p.getSaturation(), t);
		}

		@Override
		public String getName() {
			return "psaturation";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "double {[player]} Returns the player's food saturation level.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_psaturation extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRERangeException.class, CREPlayerOfflineException.class, CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			float saturation;
			int saturationIndex = 0;
			if(args.length == 2) {
				p = Static.GetPlayer(args[0], t);
				saturationIndex = 1;
			}
			Static.AssertPlayerNonNull(p, t);
			saturation = (float) Static.getDouble(args[saturationIndex], t);
			p.setSaturation(saturation);
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "set_psaturation";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "void {[player], saturation} Set's the player's food saturation level."
					+ " If this is above 0.0 and the player's health is below max, the player will experience fast"
					+ " health regeneration.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class pbed_location extends AbstractFunction {

		@Override
		public String getName() {
			return "pbed_location";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCOfflinePlayer player = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if(args.length == 1) {
				player = Static.GetUser(args[0].val(), t);
			} else if(player == null) {
				throw new CREInsufficientArgumentsException(this.getName() + " requires a player as first argument when ran from console", t);
			}
			if(player == null) {
				throw new CRENotFoundException(
						this.getName() + " failed to get an offline player (are you running in cmdline mode?)", t);
			}
			MCLocation loc = player.getBedSpawnLocation();
			if(loc == null) {
				return CNull.NULL;
			} else {
				return ObjectGenerator.GetGenerator().location(loc, false);
			}
		}

		@Override
		public String docs() {
			return "array {[playerName]} Returns a location array of the bed block the player last slept in."
					+ " The player will normally respawn next to this bed if they die."
					+ " However, this respawn location can be forcibly be set by plugins or commands to any location,"
					+ " like when using set_pbed_location().";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInsufficientArgumentsException.class, CRENotFoundException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_pbed_location extends AbstractFunction {

		@Override
		public String getName() {
			return "set_pbed_location";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3, 4, 5};
		}

		@Override
		public String docs() {
			return "void {[player], locationArray, [forced] | [player], x, y, z, [forced]} Sets the respawn location"
					+ " of a player. If player is omitted, the current player is used. The specified location should be"
					+ " the block below the respawn location. If forced is false, it will respawn the player next to"
					+ " that location only if a bed found is found there. (forced defaults to true)";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CRELengthException.class, CREPlayerOfflineException.class,
				CREFormatException.class, CRENullPointerException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			String pname = null;
			MCPlayer m = null;
			MCLocation l;
			int locationIndex;
			boolean forced = true;

			if(args.length == 1) {
				if(args[0].isInstanceOf(CArray.TYPE)) {
					if(p instanceof MCPlayer) {
						m = ((MCPlayer) p);
					}
					locationIndex = 0;
				} else {
					throw new CRECastException("Expecting an array in set_pbed_location", t);
				}
			} else if(args.length == 2) {
				if(args[1].isInstanceOf(CArray.TYPE)) {
					pname = args[0].val();
					locationIndex = 1;
				} else if(args[0].isInstanceOf(CArray.TYPE)) {
					if(p instanceof MCPlayer) {
						m = ((MCPlayer) p);
					}
					locationIndex = 0;
					forced = ArgumentValidation.getBoolean(args[1], t);
				} else {
					throw new CRECastException("Expecting an array in set_pbed_location", t);
				}
			} else if(args.length == 3) {
				if(args[1].isInstanceOf(CArray.TYPE)) {
					pname = args[0].val();
					locationIndex = 1;
					forced = ArgumentValidation.getBoolean(args[2], t);
				} else {
					if(p instanceof MCPlayer) {
						m = (MCPlayer) p;
					}
					locationIndex = 0;
				}
			} else if(args.length == 4) {
				try {
					m = Static.GetPlayer(args[0], t);
					locationIndex = 1;
				} catch (ConfigRuntimeException e) {
					if(p instanceof MCPlayer) {
						m = (MCPlayer) p;
					}
					locationIndex = 0;
					forced = ArgumentValidation.getBoolean(args[3], t);
				}
			} else {
				m = Static.GetPlayer(args[0], t);
				locationIndex = 1;
				forced = ArgumentValidation.getBoolean(args[4], t);
			}

			if(m == null && pname != null) {
				m = Static.GetPlayer(pname, t);
			}
			Static.AssertPlayerNonNull(m, t);

			if(args[locationIndex].isInstanceOf(CArray.TYPE)) {
				CArray ca = (CArray) args[locationIndex];
				l = ObjectGenerator.GetGenerator().location(ca, m.getWorld(), t);
				l.add(0, 1, 0); // someone decided to match ploc() here
			} else {
				l = m.getLocation();
				if(l == null) {
					throw new CRENullPointerException(
							"The given player has a null location (are you running from cmdline mode?)", t);
				}
				l.setX(Static.getNumber(args[locationIndex], t));
				l.setY(Static.getNumber(args[locationIndex + 1], t) + 1);
				l.setZ(Static.getNumber(args[locationIndex + 2], t));
			}

			m.setBedSpawnLocation(l, forced);
			return CVoid.VOID;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class pvehicle extends AbstractFunction {

		@Override
		public String getName() {
			return "pvehicle";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "string {[player]} Returns the UUID of the vehicle which the player is riding,"
					+ " or null if player is not riding a vehicle.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if(args.length == 1) {
				p = Static.GetPlayer(args[0].val(), t);
			}

			Static.AssertPlayerNonNull(p, t);
			if(!p.isInsideVehicle()) {
				return CNull.NULL;
			}

			return new CString(p.getVehicle().getUniqueId().toString(), t);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class pvehicle_leave extends AbstractFunction {

		@Override
		public String getName() {
			return "pvehicle_leave";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "boolean {[player]} Forces a player to leave their vehicle."
					+ " This returns false if the player is not riding a vehicle.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if(args.length == 1) {
				p = Static.GetPlayer(args[0].val(), t);
			}
			Static.AssertPlayerNonNull(p, t);

			return CBoolean.get(p.leaveVehicle());
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_offline_players extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCServer s = Static.getServer();
			CArray ret = new CArray(t);
			// This causes the function to return an empty array for a fake/null server.
			if(s != null && s.getOfflinePlayers() != null) {
				for(MCOfflinePlayer offp : s.getOfflinePlayers()) {
					ret.push(new CString(offp.getName(), t), t);
				}
			}
			return ret;
		}

		@Override
		public String getName() {
			return "get_offline_players";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "array {} Returns an array of every player who has played on this server.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Simple usage", "get_offline_players()", "{Bill, Bob, Joe, Fred}")
			};
		}

	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class phas_played extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCOfflinePlayer offp = Static.GetUser(args[0].val(), t);
			return CBoolean.get(offp != null && offp.hasPlayedBefore());
		}

		@Override
		public String getName() {
			return "phas_played";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {player} Returns whether the given player has ever been on this server."
					+ " The player argument can be a UUID or a name. But if given a name, it must be exact.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Demonstrates a player that has played",
						"phas_played('Notch')", ":true"),
				new ExampleScript("Demonstrates a player that has not played",
						"phas_played('Herobrine')", ":false")
			};
		}

	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class pfirst_played extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCCommandSender cs = environment.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCOfflinePlayer op = null;
			if(args.length == 1) {
				op = Static.GetUser(args[0].val(), t);
			} else if(cs != null) {
				op = Static.GetUser(cs.getName(), t);
			}
			return new CInt((op == null ? 0 : op.getFirstPlayed()), t); // Return 0 for fake/null command senders.
		}

		@Override
		public String getName() {
			return "pfirst_played";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "int {[player]} Returns the unix time stamp, in milliseconds, that this player first logged onto"
					+ " this server, or 0 if they never have."
					+ " The player argument can be a UUID or a name. But if given a name, it must be exact.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Demonstrates a player that has played",
				"pfirst_played('Notch')", "13558362167593"),
				new ExampleScript("Demonstrates a player that has not played",
				"pfirst_played('Herobrine')", "0")
			};
		}

	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class plast_played extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCCommandSender cs = environment.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCOfflinePlayer op = null;
			if(args.length == 1) {
				op = Static.GetUser(args[0].val(), t);
			} else if(cs != null) {
				op = Static.GetUser(cs.getName(), t);
			}
			return new CInt((op == null ? 0 : op.getLastPlayed()), t); // Return 0 for fake/null command senders.
		}

		@Override
		public String getName() {
			return "plast_played";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "int {[player]} Returns the unix time stamp, in milliseconds, that this player was last seen on this"
					+ " server, or 0 if they never were."
					+ " The player argument can be a UUID or a name. But if given a name, it must be exact.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Demonstrates a player that has played",
				"plast_played('Notch')", "13558362167593"),
				new ExampleScript("Demonstrates a player that has not played",
				"plast_played('Herobrine')", "0")
			};
		}
	}

	@api
	public static class plast_known_name extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRELengthException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCOfflinePlayer p = Static.GetUser(args[0].val(), t);
			if(p == null) {
				return CNull.NULL;
			}
			String name = p.getName();
			if(name == null) {
				return CNull.NULL;
			}
			return new CString(name, t);
		}

		@Override
		public String getName() {
			return "plast_known_name";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "string {uuid} Returns the name for this player the last time they were on the server."
					+ " Return null if the player has never been on the server. This is not guaranteed to be their"
					+ " current player name. This is read directly from the player data file for offline players.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_2;
		}
	}

	@api
	public static class save_players extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Static.getServer().savePlayers();
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "save_players";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "void {} Saves current players to disk.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
	@seealso({set_pflight.class})
	public static class set_pflying extends AbstractFunction {

		@Override
		public String getName() {
			return "set_pflying";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "void {[player], flying} Sets the flying state for the player."
					+ "Requires player to have the ability to fly, which is set with set_pflight().";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CREIllegalArgumentException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			boolean flight;
			if(args.length == 1) {
				flight = ArgumentValidation.getBoolean(args[0], t);
			} else {
				p = Static.GetPlayer(args[0], t);
				flight = ArgumentValidation.getBoolean(args[1], t);
			}
			Static.AssertPlayerNonNull(p, t);
			if(!p.getAllowFlight()) {
				throw new CREIllegalArgumentException("Player must have the ability to fly. Set with set_pflight()", t);
			}
			// This is needed in order for the player to enter flight mode whilst standing on the ground.
			if(flight
					&& p.isOnGround()) {
				Vector3D v = p.getVelocity();
				// 0.08 was chosen as it does not change the player's position, whereas higher values do.
				Vector3D newV = v.add(new Vector3D(v.X(), v.Y() + 0.08, v.Z()));
				p.setVelocity(newV);
			}
			p.setFlying(flight);
			// We only want to set whether the player is flying; not whether the player can fly.
			p.setAllowFlight(true);
			return CVoid.VOID;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
	public static class pspectator_target extends AbstractFunction {

		@Override
		public String getName() {
			return "pspectator_target";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "string {[player]} Gets the entity UUID that a spectator is viewing. If the player isn't spectating"
					+ " from an entity, null is returned. If the player isn't in spectator mode, an"
					+ " IllegalArgumentException is thrown.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CREIllegalArgumentException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p;
			if(args.length == 0) {
				p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			} else {
				p = Static.GetPlayer(args[0], t);
			}
			Static.AssertPlayerNonNull(p, t);
			if(p.getGameMode() != MCGameMode.SPECTATOR) {
				throw new CREIllegalArgumentException("Player must be in spectator mode.", t);
			}
			MCEntity e = p.getSpectatorTarget();
			if(e == null) {
				return CNull.NULL;
			}
			return new CString(e.getUniqueId().toString(), t);
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
	public static class set_pspectator_target extends AbstractFunction {

		@Override
		public String getName() {
			return "set_pspectator_target";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "void {[player], entity} Sets the entity for the player to spectate. If set to null, the"
					+ " spectator will stop following an entity. If the player is not in spectator mode an"
					+ " IllegalArgumentException is thrown.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CREIllegalArgumentException.class,
				CREBadEntityException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p;
			int offset = 0;
			if(args.length == 1) {
				p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			} else {
				p = Static.GetPlayer(args[0], t);
				offset = 1;
			}
			Static.AssertPlayerNonNull(p, t);
			if(p.getGameMode() != MCGameMode.SPECTATOR) {
				throw new CREIllegalArgumentException("Player must be in spectator mode.", t);
			}
			if(args[offset] instanceof CNull) {
				p.setSpectatorTarget(null);
			} else {
				p.setSpectatorTarget(Static.getLivingEntity(args[offset], t));
			}
			return CVoid.VOID;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
	@seealso({com.laytonsmith.core.functions.Environment.play_sound.class})
	public static class stop_sound extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInvalidWorldException.class, CRELengthException.class, CREFormatException.class,
				CREPlayerOfflineException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, com.laytonsmith.core.environments.Environment environment, Mixed... args)
				throws ConfigRuntimeException {

			MCPlayer p = Static.GetPlayer(args[0], t);

			MCSound sound;
			try {
				sound = MCSound.valueOf(args[1].val().toUpperCase());
			} catch (IllegalArgumentException iae) {
				throw new CREFormatException("Sound name '" + args[1].val() + "' is invalid.", t);
			}

			if(args.length == 3) {
				MCSoundCategory category;
				try {
					category = MCSoundCategory.valueOf(args[2].val().toUpperCase());
				} catch (IllegalArgumentException iae) {
					throw new CREFormatException("Sound category '" + args[2].val() + "' is invalid.", t);
				}
				p.stopSound(sound, category);
			} else {
				p.stopSound(sound);
			}

			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "stop_sound";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public String docs() {
			return "void {player, sound, [category]} Stops the specified sound for the given player.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_2;
		}

	}

	@api
	@seealso({com.laytonsmith.core.functions.Environment.play_named_sound.class})
	public static class stop_named_sound extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInvalidWorldException.class, CRELengthException.class, CREFormatException.class,
				CREPlayerOfflineException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, com.laytonsmith.core.environments.Environment environment, Mixed... args)
				throws ConfigRuntimeException {

			MCPlayer p = Static.GetPlayer(args[0], t);
			String sound = args[1].val();
			if(args.length == 3) {
				MCSoundCategory category;
				try {
					category = MCSoundCategory.valueOf(args[2].val().toUpperCase());
				} catch (IllegalArgumentException iae) {
					throw new CREFormatException("Sound category '" + args[2].val() + "' is invalid.", t);
				}
				p.stopSound(sound, category);
			} else {
				p.stopSound(sound);
			}

			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "stop_named_sound";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public String docs() {
			return "void {player, sound, [category]} Stops the specified sound for the given player.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_2;
		}

	}

	@api
	public static class pcooldown extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRELengthException.class, CREFormatException.class, CREPlayerOfflineException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p;
			String materialName;
			if(args.length == 2) {
				p = Static.GetPlayer(args[0], t);
				materialName = args[1].val();
			} else {
				p = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
				Static.AssertPlayerNonNull(p, t);
				materialName = args[0].val();
			}

			MCMaterial mat = StaticLayer.GetMaterial(materialName);
			if(mat == null) {
				throw new CREFormatException("Material name is invalid.", t);
			}

			return new CInt(p.getCooldown(mat), t);
		}

		@Override
		public String getName() {
			return "pcooldown";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "int {[player], material} Gets the time left on the player's cooldown for the specified item type."
					+ " This returns an integer representing the time in server ticks until any items of this material"
					+ " can be used again by this player.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_2;
		}

	}

	@api
	public static class set_pcooldown extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRELengthException.class, CREFormatException.class, CREPlayerOfflineException.class,
				CRERangeException.class, CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p;
			String materialName;
			int cooldown;
			if(args.length == 3) {
				p = Static.GetPlayer(args[0], t);
				materialName = args[1].val();
				cooldown = Static.getInt32(args[2], t);
			} else {
				p = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
				Static.AssertPlayerNonNull(p, t);
				materialName = args[0].val();
				cooldown = Static.getInt32(args[1], t);
			}

			MCMaterial mat = StaticLayer.GetMaterial(materialName);
			if(mat == null) {
				throw new CREFormatException("Material name is invalid.", t);
			}

			if(cooldown < 0) {
				throw new CRERangeException("Cooldowns cannot be negative.", t);
			}

			p.setCooldown(mat, cooldown);
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "set_pcooldown";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public String docs() {
			return "int {[player], material, cooldown} Sets the player's cooldown time for the specified item type."
					+ " The cooldown must be a positive integer representing server ticks.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_2;
		}

	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_plist_header extends AbstractFunction {

		@Override
		public String getName() {
			return "get_plist_header";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "string {[player]} Gets the player list header for a player."
					+ " This is the text that appears above the player list that appears when hitting the tab key.";
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p;
			if(args.length == 2) {
				p = Static.GetPlayer(args[0], t);
			} else {
				p = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
				Static.AssertPlayerNonNull(p, t);
			}
			return new CString(p.getPlayerListHeader(), t);
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRELengthException.class, CREPlayerOfflineException.class};
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_4;
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_plist_header extends AbstractFunction {

		@Override
		public String getName() {
			return "set_plist_header";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "void {[player], header} Sets the player list header for a player."
					+ " This is the text that appears above the player list that appears when hitting the tab key."
					+ " Colors and new lines are accepted. Only the given player (or current player if none is given)"
					+ " will see these changes.";
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p;
			String header;
			if(args.length == 2) {
				p = Static.GetPlayer(args[0], t);
				header = args[1].val();
			} else {
				p = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
				Static.AssertPlayerNonNull(p, t);
				header = args[0].val();
			}
			p.setPlayerListHeader(header);
			return CVoid.VOID;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRELengthException.class, CREPlayerOfflineException.class};
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_4;
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_plist_footer extends AbstractFunction {

		@Override
		public String getName() {
			return "get_plist_footer";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "string {[player]} Gets the player list footer for a player."
					+ " This is the text that appears below the player list that appears when hitting the tab key.";
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p;
			if(args.length == 2) {
				p = Static.GetPlayer(args[0], t);
			} else {
				p = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
				Static.AssertPlayerNonNull(p, t);
			}
			return new CString(p.getPlayerListFooter(), t);
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRELengthException.class, CREPlayerOfflineException.class};
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_4;
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_plist_footer extends AbstractFunction {

		@Override
		public String getName() {
			return "set_plist_footer";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "void {[player], footer} Sets the player list footer for a player."
					+ " This is the text that appears below the player list that appears when hitting the tab key."
					+ " Colors and new lines are accepted. Only the given player (or current player if none is given)"
					+ " will see these changes.";
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p;
			String footer;
			if(args.length == 2) {
				p = Static.GetPlayer(args[0], t);
				footer = args[1].val();
			} else {
				p = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
				Static.AssertPlayerNonNull(p, t);
				footer = args[0].val();
			}
			p.setPlayerListFooter(footer);
			return CVoid.VOID;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRELengthException.class, CREPlayerOfflineException.class};
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_4;
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class ptellraw extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			String selector = "@s";
			String json;
			if(args.length == 1) {
				json = new DataTransformations.json_encode().exec(t, environment, args[0]).val();
			} else {
				selector = ArgumentValidation.getString(args[0], t);
				json = new DataTransformations.json_encode().exec(t, environment, args[1]).val();
			}
			new Meta.sudo().exec(t, environment, new CString("/minecraft:tellraw " + selector + " " + json, t));
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "ptellraw";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "void {[string selector], array raw} A thin wrapper around the tellraw command from player context,"
					+ " this simply passes the input to the command. The raw is passed in as a normal"
					+ " (possibly associative) array, and json encoded. No validation is done on the input,"
					+ " so the command may fail. If not provided, the selector defaults to @s. Do not use double quotes"
					+ " (smart string) when providing the selector. See {{function|tellraw}} if you don't need player"
					+ " context. ---- The specification of the array may change from version to version of Minecraft,"
					+ " but is documented here https://minecraft.gamepedia.com/Commands#Raw_JSON_text."
					+ " This function is simply written in terms of json_encode and sudo, and is otherwise equivalent"
					+ " to sudo('/minecraft:tellraw ' . @selector . ' ' . json_encode(@raw))";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_4;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[] {
				new ExampleScript("Simple usage with a plain message",
						"ptellraw('@a', array('text': 'Hello World!'));",
						"<<Would output the plain message to all player.>>"),
				new ExampleScript("Advanced usage with embedded selectors.",
						"ptellraw('@a', array(\n"
								+ "\tarray('selector': '@s'), // prints current player\n"
								+ "\tarray('text': ': Hello '),\n"
								+ "\tarray('selector': '@p') // prints receiving player\n"
								+ "));",
						"<<Would output a message from the current player to all players.>>"),
				new ExampleScript("Complex object",
						"ptellraw(array(\n"
								+ "\tarray('text': 'Hello '),\n"
								+ "\tarray('text': 'World', 'color': 'light_purple'),\n"
								+ "\tarray('text': '!')\n"
								+ "));",
						"<<Would output the colorful message to the current player>>")
			};
		}

	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class pstatistic extends AbstractFunction {

		@Override
		public String getName() {
			return "pstatistic";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3};
		}

		@Override
		public String docs() {
			return "int {[player], statistic, [type]} Returns the specified statistic for the player."
					+ " Some statistics require a block, item or entity type. An IllegalArgumentException will"
					+ " be thrown if the statistic is invalid, or if the type is invalid for that statistic."
					+ " The player argument is required before the type argument is."
					+ " ---- Valid statistics are: " + StringUtils.Join(MCPlayerStatistic.values(), ", ", ", or ", " or ");
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			String filter = null;
			MCPlayerStatistic stat;
			int offset = 0;
			if(args.length > 1) {
				offset = 1;
				p = Static.GetPlayer(args[0], t);
				if(args.length > 2) {
					filter = args[2].val().toUpperCase();
				}
			}

			try {
				stat = MCPlayerStatistic.valueOf(args[offset].val().toUpperCase());
			} catch (IllegalArgumentException ex) {
				throw new CREIllegalArgumentException("Invalid statistic type: " + args[offset].val(), t);
			}

			MCPlayerStatistic.Type type = stat.getType();
			int ret = 0;
			if(type != MCPlayerStatistic.Type.NONE) {
				if(filter == null) {
					throw new CREInsufficientArgumentsException("Missing " + type.name().toLowerCase()
							+ " type for statistic: " + args[offset].val(), t);
				}
				switch(type) {
					case BLOCK:
						MCMaterial block = StaticLayer.GetMaterial(filter);
						if(block == null || !block.isBlock()) {
							throw new CREIllegalArgumentException("Invalid block type: " + filter, t);
						}
						ret = p.getStatistic(stat, block);
						break;
					case ENTITY:
						try {
							ret = p.getStatistic(stat, MCEntityType.valueOf(filter.toUpperCase()));
						} catch (IllegalArgumentException ex) {
							throw new CREIllegalArgumentException("Invalid entity type: " + filter, t);
						}
						break;
					case ITEM:
						MCMaterial item = StaticLayer.GetMaterial(filter);
						if(item == null) {
							throw new CREIllegalArgumentException("Invalid item type: " + filter, t);
						}
						ret = p.getStatistic(stat, item);
						break;
				}
			} else {
				ret = p.getStatistic(stat);
			}
			return new CInt(ret, t);
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREIllegalArgumentException.class, CREPlayerOfflineException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_4;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_pstatistic extends AbstractFunction {

		@Override
		public String getName() {
			return "set_pstatistic";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3, 4};
		}

		@Override
		public String docs() {
			return "void {[player], statistic, [type], amount} Sets the specified statistic for the player."
					+ " Some statistics require a block, item or entity type. An IllegalArgumentException will"
					+ " be thrown if the statistic is invalid, or if the type is invalid for that statistic."
					+ " The player argument is required before the type argument is."
					+ " ---- Valid statistics are: " + StringUtils.Join(MCPlayerStatistic.values(), ", ", ", or ", " or ");
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			String filter = null;
			MCPlayerStatistic stat;
			int offset = 0;
			int amount = 0;
			if(args.length > 2) {
				offset = 1;
				p = Static.GetPlayer(args[0], t);
				if(args.length == 3) {
					amount = Static.getInt32(args[2], t);
				} else {
					filter = args[2].val().toUpperCase();
					amount = Static.getInt32(args[3], t);
				}
			}

			try {
				stat = MCPlayerStatistic.valueOf(args[offset].val().toUpperCase());
			} catch (IllegalArgumentException ex) {
				throw new CREIllegalArgumentException("Invalid statistic type: " + args[offset].val(), t);
			}

			if(amount < 0) {
				throw new CRERangeException("Amount must not be negative.", t);
			}

			MCPlayerStatistic.Type type = stat.getType();
			if(type != MCPlayerStatistic.Type.NONE) {
				if(filter == null) {
					throw new CREInsufficientArgumentsException("Missing " + type.name().toLowerCase()
							+ " type for statistic: " + args[offset].val(), t);
				}
				switch(type) {
					case BLOCK:
						MCMaterial block = StaticLayer.GetMaterial(filter);
						if(block == null || !block.isBlock()) {
							throw new CREIllegalArgumentException("Invalid block type: " + filter, t);
						}
						p.setStatistic(stat, block, amount);
						break;
					case ENTITY:
						try {
							p.setStatistic(stat, MCEntityType.valueOf(filter.toUpperCase()), amount);
						} catch (IllegalArgumentException ex) {
							throw new CREIllegalArgumentException("Invalid entity type: " + filter, t);
						}
						break;
					case ITEM:
						MCMaterial item = StaticLayer.GetMaterial(filter);
						if(item == null) {
							throw new CREIllegalArgumentException("Invalid item type: " + filter, t);
						}
						p.setStatistic(stat, item, amount);
						break;
				}
			} else {
				p.setStatistic(stat, amount);
			}
			return CVoid.VOID;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRERangeException.class, CREPlayerOfflineException.class, CRECastException.class,
					CREIllegalArgumentException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_4;
		}
	}

}
