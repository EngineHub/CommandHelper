package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.StringUtils;
import com.laytonsmith.abstraction.*;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.enums.MCGameMode;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.documentation;
import com.laytonsmith.annotations.typename;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.*;
import com.laytonsmith.core.arguments.ArgList;
import com.laytonsmith.core.arguments.Argument;
import com.laytonsmith.core.arguments.ArgumentBuilder;
import com.laytonsmith.core.arguments.Generic;
import com.laytonsmith.core.arguments.Signature;
import com.laytonsmith.core.compiler.Optimizable;
import com.laytonsmith.core.compiler.Optimizable.OptimizationOption;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.laytonsmith.core.natives.MItemStack;
import com.laytonsmith.core.natives.MLocation;
import com.laytonsmith.core.natives.MPotion;
import com.laytonsmith.core.natives.MVector3D;
import com.laytonsmith.core.natives.annotations.NonNull;
import com.laytonsmith.core.natives.annotations.Ranged;
import com.laytonsmith.core.natives.interfaces.MObject;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Layton
 */
public class PlayerManagement {
	
	/**
	 * A pre-built argument that represents the player. The name of the variable is "player", and defaults to null.
	 */
	public static final Argument PLAYER_ARG = new Argument("The name of the player to run the function with. If not provided,"
			+ " the player running the current execution context is used.", CString.class, "player").setOptionalDefaultNull();

	public static String docs() {
		return "This class of functions allow players to be managed";
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class player extends AbstractFunction {

		public String getName() {
			return "player";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();

			if (args.length == 1) {
				p = Static.GetPlayer(args[0], t);
			}

			// assuming p is not null, just return the name set by the CommandSender
			// for player entities in CraftBukkit, this is the player's name, and
			// for the console it's "CONSOLE". For CommandBlocks it's "@" unless it has been renamed.
			if (p == null) {
				return Construct.GetNullConstruct(CString.class, t);
			} else {
				String name = p.getName();
				if (p instanceof MCConsoleCommandSender || "CONSOLE".equals(name)) {
					name = Static.getConsoleName();
				}
				if (p instanceof MCBlockCommandSender) {
					name = Static.getBlockPrefix() + name;
				}
				return new CString(name, t);
			}
		}

		public String docs() {
			return "Returns the full name of the partial Player name specified or the Player running the command otherwise. If the command is being run from"
					+ " the console, then the string '~console' is returned. If the command is coming from elsewhere, returns a string chosen by the sender of this command (or null)."
					+ " Note that most functions won't support the user '~console' (they'll throw a PlayerOfflineException), but you can use this to determine"
					+ " where a command is being run from.";
		}
		
		public Argument returnType() {
			return new Argument("", CString.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CString.class, "partialPlayer").setOptionalDefaultNull()
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PlayerOfflineException};
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_0_1;
		}

		public Boolean runAsync() {
			return false;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class all_players extends AbstractFunction {

		public String getName() {
			return "all_players";
		}

		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			MCPlayer[] pa = Static.getServer().getOnlinePlayers();
			CString[] sa = new CString[pa.length];
			for (int i = 0; i < pa.length; i++) {
				sa[i] = new CString(pa[i].getName(), t);
			}
			return new CArray(t, sa);
		}

		public String docs() {
			return "Returns an array of all the player names of all the online players on the server";
		}
		
		public Argument returnType() {
			return new Argument("", CArray.class).setGenerics(new Generic(CString.class));
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.NONE;
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_0_1;
		}

		public Boolean runAsync() {
			return false;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class players_in_radius extends AbstractFunction {

		public String getName() {
			return "players_in_radius";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		boolean inRadius(MCPlayer player, double dist, MCLocation loc) {
			if (!(player.getWorld().equals(loc.getWorld()))) {
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

			if (distance <= dist) {
				return true;
			}

			return false;
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			MCPlayer[] pa = Static.getServer().getOnlinePlayers();
			MCPlayer p = env.getEnv(CommandHelperEnvironment.class).GetPlayer();

			MCLocation loc;
			double dist;

			if (args.length == 1) {
				dist = args[0].primitive(t).castToDouble(t);
				Static.AssertPlayerNonNull(p, t);
				loc = p.getLocation();
			} else {
				if (!(args[0] instanceof CArray)) {
					throw new ConfigRuntimeException("Expecting an array at parameter 1 of players_in_radius",
							ExceptionType.CastException, t);
				}

				loc = ObjectGenerator.GetGenerator().location(args[0], p != null ? p.getWorld() : null, t);
				dist = args[1].primitive(t).castToDouble(t);
			}

			CArray sa = new CArray(t);

			for (int i = 0; i < pa.length; i++) {
				if (inRadius(pa[i], dist, loc)) {
					sa.push(new CString(pa[i].getName(), t));
				}
			}

			return sa;
		}

		public String docs() {
			return "Returns an array of all the player names of all the online players within the given radius";
		}
		
		public Argument returnType() {
			return new Argument("", CArray.class).setGenerics(new Generic(CString.class));
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", MLocation.class, "location").setOptionalDefaultNull(),
						new Argument("", CInt.class, "distance")
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.FormatException, ExceptionType.PlayerOfflineException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		public Boolean runAsync() {
			return false;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class ploc extends AbstractFunction {

		public String getName() {
			return "ploc";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCPlayer m = null;
			if (p instanceof MCPlayer) {
				m = (MCPlayer) p;
			}
			if (args.length == 1) {
				m = Static.GetPlayer(args[0], t);
			}
			Static.AssertPlayerNonNull(m, t);
			MLocation loc = new MLocation(m.getLocation());
			return loc.deconstruct(t);
		}

		public String docs() {
			return "Returns the location of the player specified, or the player running the command otherwise. Note that the y coordinate is"
					+ " in relation to the block the player is standing on. Note that the location also includes the yaw and pitch, which"
					+ " is used to determine the direction the player is facing.";
		}
		
		public Argument returnType() {
			return new Argument("", MLocation.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						PLAYER_ARG
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PlayerOfflineException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_0_1;
		}

		public Boolean runAsync() {
			return false;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_ploc extends AbstractFunction {

		public String getName() {
			return "set_ploc";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3, 4};
		}

		public String docs() {
			return "Sets the location of the player to the specified coordinates. If the coordinates"
					+ " are not valid, or the player was otherwise prevented from moving, false is returned, otherwise true. If player is omitted, "
					+ " the current player is used. Note that 1 is automatically added to the y component, which means that sending a player to"
					+ " x, y, z coordinates shown with F3 will work as expected, instead of getting them stuck inside the floor. ";
		}
		
		public Argument returnType() {
			return new Argument("", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Signature(1,
							PLAYER_ARG,
							new Argument("", MLocation.class, "location")
						), new Signature(2,
							PLAYER_ARG,
							new Argument("", CNumber.class, "x"),
							new Argument("", CNumber.class, "y"),
							new Argument("", CNumber.class, "z")
						)
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.LengthException, ExceptionType.PlayerOfflineException, ExceptionType.FormatException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_1_0;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			String MCPlayer = null;
			double x;
			double y;
			double z;
			MCPlayer m = null;
			MCLocation l = null;
			if (args.length == 1) {
				if (args[0] instanceof CArray) {
					CArray ca = (CArray) args[0];
					l = ObjectGenerator.GetGenerator().location(ca, (p instanceof MCPlayer ? ((MCPlayer) p).getWorld() : null), t);
					x = ca.get(0, t).primitive(t).castToDouble(t);
					y = ca.get(1, t).primitive(t).castToDouble(t);
					z = ca.get(2, t).primitive(t).castToDouble(t);
					if (p instanceof MCPlayer) {
						m = ((MCPlayer) p);
					}

				} else {
					throw new ConfigRuntimeException("Expecting an array at parameter 1 of set_ploc",
							ExceptionType.CastException, t);
				}
			} else if (args.length == 2) {
				if (args[1] instanceof CArray) {
					CArray ca = (CArray) args[1];
					MCPlayer = args[0].val();
					l = ObjectGenerator.GetGenerator().location(ca, Static.GetPlayer(MCPlayer, t).getWorld(), t);
					x = l.getX();
					y = l.getY();
					z = l.getZ();
				} else {
					throw new ConfigRuntimeException("Expecting parameter 2 to be an array in set_ploc",
							ExceptionType.CastException, t);
				}
			} else if (args.length == 3) {
				if (p instanceof MCPlayer) {
					m = (MCPlayer) p;
				}
				x = args[0].primitive(t).castToDouble(t);
				y = args[1].primitive(t).castToDouble(t);
				z = args[2].primitive(t).castToDouble(t);
				l = m.getLocation();
			} else {
				MCPlayer = args[0].val();
				x = args[1].primitive(t).castToDouble(t);
				y = args[2].primitive(t).castToDouble(t);
				z = args[3].primitive(t).castToDouble(t);
				l = StaticLayer.GetLocation(Static.GetPlayer(MCPlayer, t).getWorld(), x, y, z, 0, 0);
			}
			if (m == null && MCPlayer != null) {
				m = Static.GetPlayer(MCPlayer, t);
			}
			Static.AssertPlayerNonNull(m, t);
			if (!l.getWorld().exists()) {
				throw new ConfigRuntimeException("The world specified does not exist.", ExceptionType.InvalidWorldException, t);
			}
			return new CBoolean(m.teleport(StaticLayer.GetLocation(l.getWorld(), x, y + 1, z, m.getLocation().getYaw(), m.getLocation().getPitch())), t);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class pcursor extends AbstractFunction {

		public String getName() {
			return "pcursor";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1, 2};
		}

		public String docs() {
			return "Returns the location of the block the player has highlighted"
					+ " in their crosshairs. If player is omitted, the current player is used. If the block is too far, a"
					+ " RangeException is thrown. An array of ids to be considered transparent can be supplied, otherwise"
					+ " only air will be considered transparent. Providing an empty array will cause air to be considered"
					+ " a potential target, allowing a way to get the block containing the player's head.";
		}
		
		public Argument returnType() {
			return new Argument("", MLocation.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						PLAYER_ARG,
						new Argument("", CArray.class, "transparent").setGenerics(new Generic(new Class[]{MItemStack.class, CInt.class, CString.class}))
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.RangeException,
					ExceptionType.FormatException, ExceptionType.CastException, ExceptionType.PluginInternalException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_0_2;
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			MCPlayer p = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
			HashSet<Short> trans = null;
			if (args.length == 1) {
				if(args[0] instanceof CArray) {
					CArray ta = (CArray) args[0];
					trans = new HashSet<Short>();
					for (int i=0; i < ta.size(); i++) {
						trans.add(ta.get(i).primitive(t).castToInt16(t));
					}
				} else {
					p = Static.GetPlayer(args[0], t);
				}
			} else if (args.length == 2) {
				p = Static.GetPlayer(args[0], t);
				if(args[1] instanceof CArray && !args[1].isNull()) {
					CArray ta = (CArray) args[1];
					trans = new HashSet<Short>();
					for (int i=0; i < ta.size(); i++) {
						trans.add(ta.get(i).primitive(t).castToInt16(t));
					}
				} else {
					throw new Exceptions.FormatException("An array was expected for argument 2 but received " + args[1], t);
				}
			}
			Static.AssertPlayerNonNull(p, t);
			MCBlock b;
			try {
				b = p.getTargetBlock(trans, 10000, false);
			} catch (IllegalStateException ise) {
				throw new ConfigRuntimeException("The server's method of finding the target block has failed."
						+ " There is nothing that can be done about this except standing somewhere else.",
						ExceptionType.PluginInternalException, t);
			}
			if (b == null) {
				throw new ConfigRuntimeException("No block in sight, or block too far",
						ExceptionType.RangeException, t);
			}
			return new CArray(t, new CInt(b.getX(), t),
					new CInt(b.getY(), t),
					new CInt(b.getZ(), t),
					new CString(b.getWorld().getName(), t));
		}

		public Boolean runAsync() {
			return false;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
					new ExampleScript("Demonstrates finding a non-air block", "msg(pcursor())", "{-127, 75, 798, world}"),
					new ExampleScript("Demonstrates looking above the skyline", "msg(pcursor())",
							"(Throws RangeException: No block in sight, or block too far)"),
					new ExampleScript("Demonstrates getting your target while ignoring torches and bedrock",
							"msg(pcursor(array(50, 7)))", "{-127, 75, 798, world}"),
					new ExampleScript("Demonstrates getting Notch's target while ignoring air, water, and lava",
							"msg(pcursor('Notch', array(0, 8, 9, 10, 11)))", "{-127, 75, 798, world}")
			};
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class kill extends AbstractFunction {

		public String getName() {
			return "kill";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCPlayer m = null;
			if (args.length == 1) {
				m = Static.GetPlayer(args[0], t);
			} else {
				if (p instanceof MCPlayer) {
					m = (MCPlayer) p;
				}
			}
			Static.AssertPlayerNonNull(m, t);
			m.setHealth(0);
			return new CVoid(t);
		}

		public String docs() {
			return "void {[playerName]} Kills the specified player, or the current player if it is omitted";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						PLAYER_ARG
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PlayerOfflineException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_0_1;
		}

		public Boolean runAsync() {
			return false;
		}
	}

	@api(environments = {CommandHelperEnvironment.class, GlobalEnv.class})
	public static class pgroup extends AbstractFunction {

		public String getName() {
			return "pgroup";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCPlayer m = null;
			if (args.length == 0) {
				if (p instanceof MCPlayer) {
					m = (MCPlayer) p;
				}
			} else {
				m = Static.GetPlayer(args[0], t);
			}

			Static.AssertPlayerNonNull(m, t);

			String[] sa = env.getEnv(GlobalEnv.class).GetPermissionsResolver().getGroups(m.getName());
			Construct[] ca = new Construct[sa.length];
			for (int i = 0; i < sa.length; i++) {
				ca[i] = new CString(sa[i], t);
			}
			CArray a = new CArray(t, ca);
			return a;
		}

		public String docs() {
			return "array {[playerName]} Returns an array of the groups a player is in. If playerName is omitted, the current player is used.";
		}
		
		public Argument returnType() {
			return new Argument("", CArray.class).setGenerics(new Generic(CString.class));
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						PLAYER_ARG
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PlayerOfflineException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_0_1;
		}

		public Boolean runAsync() {
			return false;
		}
	}

	@typename("PlayerInfo")
	public static class MPlayerInfo extends MObject{
		@documentation(docs="This is the player's exact name")
		public String name;
		@documentation(docs="The current location of the player")
		public MLocation location;
		@documentation(docs="The location of the player's cursor, or null if it is out of sight")
		public MLocation cursor;
		@documentation(docs="The IP address of the player")
		public String ip;
		@documentation(docs="The display name of the player")
		public String displayName;
		@documentation(docs="The player's current health")
		public int health;
		@documentation(docs="The item that the player is currently holding")
		public MItemStack itemInHand;
		@documentation(docs="The world that the player is in")
		public String world;
		@documentation(docs="True if the player is op")
		public boolean op;
		@documentation(docs="A list of the groups the player is in")
		public List<String> groups;
		@documentation(docs="The hostname of the player, or the ip if the reverse lookup didn't return anything, or hasn't yet finished")
		public String hostname;
		@documentation(docs="True if the player is sneaking")
		public boolean sneaking;
		@documentation(docs="The host the player connected to to join this server")
		public String host;
		@documentation(docs="The entity ID of this player")
		public int entityID;
		@documentation(docs="True if this player is currently in a vehicle")
		public boolean riding;
		@documentation(docs="The slot number that the player currently has highlighted in their hotbar")
		public int currentSlot;
		@documentation(docs="True if the player is sleeping")
		public boolean sleeping;
		@documentation(docs="True if the player is blocking")
		public boolean blocking;
		@documentation(docs="True if the player is flying")
		public boolean flying;

		@Override
		protected String alias(String field) {
			if("0".equals(field)){
				return "name";
			} else if("1".equals(field)){
				return "location";
			} else if("2".equals(field)){
				return "cursor";
			} else if("3".equals(field)){
				return "ip";
			} else if("4".equals(field)){
				return "displayName";
			} else if("5".equals(field)){
				return "health";
			} else if("6".equals(field)){
				return "itemInHand";
			} else if("7".equals(field)){
				return "world";
			} else if("8".equals(field)){
				return "op";
			} else if("9".equals(field)){
				return "groups";
			} else if("10".equals(field)){
				return "hostname";
			} else if("11".equals(field)){
				return "sneaking";
			} else if("12".equals(field)){
				return "host";
			} else if("13".equals(field)){
				return "entityID";
			} else if("14".equals(field)){
				return "riding";
			} else if("15".equals(field)){
				return "currentSlot";
			} else if("16".equals(field)){
				return "sleeping";
			} else if("17".equals(field)){
				return "blocking";
			} else if("18".equals(field)){
				return "flying";
			} else {
				return null;
			}
		}		
	}
	@api(environments = {CommandHelperEnvironment.class, GlobalEnv.class})
	public static class pinfo extends AbstractFunction {

		public String getName() {
			return "pinfo";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1, 2};
		}

		public String docs() {
			return "Returns various information about the player specified, or the current player if no argument was given."
					+ " If value is set, it should be an integer of one of the following indexes, and only that information for that index"
					+ " will be returned. Otherwise if value is not specified (or is -1), it returns a PlayerInfo object.";
		}
		
		public Argument returnType() {
			return new Argument("", Mixed.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						PLAYER_ARG,
						new Argument("", CInt.class, "index").setOptionalDefault(-1).addAnnotation(new Ranged(-1, 19))
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.RangeException, ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_1_0;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			MCCommandSender m = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			String player = "";
			int index = -1;
			if (args.length == 0) {
				player = (m instanceof MCPlayer ? ((MCPlayer) m).getName() : null);
				index = -1;
			} else if (args.length == 1) {
				player = args[0].val();
				index = -1;
			} else {
				player = args[0].val();
				index = args[1].primitive(t).castToInt32(t);
			}

			MCPlayer p = Static.GetPlayer(player, t);

			Static.AssertPlayerNonNull(p, t);
			int maxIndex = 18;
			//TODO: This needs converting to the PlayerInfo object
			if (index < -1 || index > maxIndex) {
				throw new ConfigRuntimeException("pinfo expects the index to be between -1 and " + maxIndex,
						ExceptionType.RangeException, t);
			}
			ArrayList<Mixed> retVals = new ArrayList<Mixed>();
			if (index == 0 || index == -1) {
				//MCPlayer name
				retVals.add(new CString(p.getName(), t));
			}
			if (index == 1 || index == -1) {
				//MCPlayer location
				retVals.add(new CArray(t, new CDouble(p.getLocation().getX(), t),
						new CDouble(p.getLocation().getY() - 1, t), new CDouble(p.getLocation().getZ(), t)));
			}
			if (index == 2 || index == -1) {
				//MCPlayer cursor
				MCBlock b;
				try {
					b = p.getTargetBlock(null, 200);
				} catch (IllegalStateException ise) {
					b = null;
				}
				if (b == null) {
					retVals.add(Construct.GetNullConstruct(Mixed.class, t));
				} else {
					retVals.add(new CArray(t, new CInt(b.getX(), t), new CInt(b.getY(), t), new CInt(b.getZ(), t)));
				}
			}
			if (index == 3 || index == -1) {
				//MCPlayer IP
				String add;
				try {
					add = p.getAddress().getAddress().getHostAddress();
				} catch (NullPointerException npe) {
					add = "";
				}

				retVals.add(new CString(add, t));
			}
			if (index == 4 || index == -1) {
				//Display name
				retVals.add(new CString(p.getDisplayName(), t));
			}
			if (index == 5 || index == -1) {
				//MCPlayer health
				retVals.add(new CInt((long) p.getHealth(), t));
			}
			if (index == 6 || index == -1) {
				//Item in hand
				MCItemStack is = p.getItemInHand();
				int data;
				if (is.getTypeId() < 256) {
					if (is.getData() != null) {
						data = is.getData().getData();
					} else {
						data = 0;
					}
				} else {
					data = is.getDurability();
				}
				retVals.add(new CString(is.getTypeId() + ":" + data, t));
			}
			if (index == 7 || index == -1) {
				//World name
				retVals.add(new CString(p.getWorld().getName(), t));
			}
			if (index == 8 || index == -1) {
				//Is op
				retVals.add(new CBoolean(p.isOp(), t));
			}
			if (index == 9 || index == -1) {
				//MCPlayer groups
				String[] sa = env.getEnv(GlobalEnv.class).GetPermissionsResolver().getGroups(p.getName());
				Construct[] ca = new Construct[sa.length];
				for (int i = 0; i < sa.length; i++) {
					ca[i] = new CString(sa[i], t);
				}
				CArray a = new CArray(t, ca);
				retVals.add(a);
			}
			if (index == 10 || index == -1) {
				String hostname;
				try {
					hostname = p.getAddress().getAddress().getHostAddress();
				} catch (NullPointerException npe) {
					hostname = "";
				}

				if (CommandHelperPlugin.hostnameLookupCache.containsKey(p.getName())) {
					hostname = CommandHelperPlugin.hostnameLookupCache.get(p.getName());
				}

				retVals.add(new CString(hostname, t));
			}
			if (index == 11 || index == -1) {
				retVals.add(new CBoolean(p.isSneaking(), t));
			}
			if (index == 12 || index == -1) {
				retVals.add(new CString(p.getHost(), t));
			}
			if (index == 13 || index == -1) {
				retVals.add(new CInt(p.getEntityId(), t));
			}
			if (index == 14 || index == -1) {
				retVals.add(new CBoolean(p.isInsideVehicle(), t));
			}
			if (index == 15 || index == -1) {
				retVals.add(new CInt(p.getInventory().getHeldItemSlot(), t));
			}
			if (index == 16 || index == -1) {
				retVals.add(new CBoolean(p.isSleeping(), t));
			}
			if (index == 17 || index == -1) {
				retVals.add(new CBoolean(p.isBlocking(), t));
			}
			if (index == 18 || index == -1) {
				retVals.add(new CBoolean(p.isFlying(), t));
			}
			if (retVals.size() == 1) {
				return retVals.get(0);
			} else {
				CArray ca = new CArray(t);
				for (Mixed c : retVals) {
					ca.push(c);
				}
				return ca;
			}
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class pworld extends AbstractFunction {

		public String getName() {
			return "pworld";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		public String docs() {
			return "Gets the world of the player specified, or the current player, if playerName isn't specified.";
		}
		
		public Argument returnType() {
			return new Argument("", CString.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						PLAYER_ARG
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PlayerOfflineException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_1_0;
		}

		public Boolean runAsync() {
			return true;
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCPlayer m = null;
			if (args.length == 0) {
				if (p instanceof MCPlayer) {
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
	public static class kick extends AbstractFunction {

		public String getName() {
			return "kick";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1, 2};
		}

		public String docs() {
			return "Kicks the specified player, with an optional message. If no message is specified, "
					+ "\"You have been kicked\" is used. If no player is specified, the current player is used, with the default message.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						PLAYER_ARG,
						new Argument("", CString.class, "message").setOptionalDefault("You have been kicked")
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PlayerOfflineException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_1_0;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			String message = "You have been kicked";
			MCPlayer m = null;
			if (args.length == 0) {
				if (p instanceof MCPlayer) {
					m = (MCPlayer) p;
				}
			}
			if (args.length >= 1) {
				m = Static.GetPlayer(args[0], t);
			}
			if (args.length >= 2) {
				message = args[1].val();
			}
			MCPlayer ptok = m;
			Static.AssertPlayerNonNull(ptok, t);
			ptok.kickPlayer(message);
			return new CVoid(t);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_display_name extends AbstractFunction {

		public String getName() {
			return "set_display_name";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "Sets a player's display name."
					+ " See reset_display_name also. playerName, as well."
					+ " It is important to note that all functions expect the"
					+ " player's real name, not their display name.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						PLAYER_ARG,
						new Argument("", CString.class, "displayName")
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PlayerOfflineException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_1_2;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCPlayer MCPlayer = null;
			String name;
			if (args.length == 1) {
				if (p instanceof MCPlayer) {
					MCPlayer = (MCPlayer) p;
				}
				name = args[0].val();
			} else {
				MCPlayer = Static.GetPlayer(args[0], t);
				name = args[1].val();
			}
			Static.AssertPlayerNonNull(MCPlayer, t);
			MCPlayer.setDisplayName(name);
			return new CVoid(t);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class reset_display_name extends AbstractFunction {

		public String getName() {
			return "reset_display_name";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		public String docs() {
			return "Resets a player's display name to their real name. If playerName isn't specified, defaults to the"
					+ " player running the command.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						PLAYER_ARG
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PlayerOfflineException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_1_2;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCPlayer MCPlayer = null;
			if (args.length == 0) {
				if (p instanceof MCPlayer) {
					MCPlayer = (MCPlayer) p;
				}
			} else {
				MCPlayer = Static.GetPlayer(args[0], t);
			}
			Static.AssertPlayerNonNull(MCPlayer, t);
			MCPlayer.setDisplayName(MCPlayer.getName());
			return new CVoid(t);
		}
	}
	
	@api(environments = {CommandHelperEnvironment.class})
	public static class set_pfacing extends AbstractFunction{

		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						PLAYER_ARG,
						new Argument("", CNumber.class, "yaw").addAnnotation(new Ranged(-90, 90)),
						new Argument("", CNumber.class, "pitch").addAnnotation(new Ranged(0, 360))
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.RangeException, ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			ArgList list = getBuilder().parse(args, this, t);
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if(list.getStringWithNull("player", t) != null){
				p = Static.GetPlayer(list.getString("player", t), t);
			}
			Static.AssertPlayerNonNull(p, t);
			float yaw = list.getFloat("yaw", t);
			float pitch = list.getFloat("pitch", t);
			MCLocation l = p.getLocation();
			l.setYaw(yaw);
			l.setPitch(pitch);
			p.teleport(l);
			return new CVoid(t);
		}

		public String getName() {
			return "set_pfacing";
		}

		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		public String docs() {
			return "Sets the direction the player is facing. See pfacing for what the range of the numbers means.";
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}

//	@api(environments = {CommandHelperEnvironment.class})
//	public static class pfacing extends AbstractFunction {
//
//		public String getName() {
//			return "pfacing";
//		}
//
//		public Integer[] numArgs() {
//			return new Integer[]{0, 1, 2, 3};
//		}
//
//		public String docs() {
//			return "Gets the direction the player is facing. ---- A note on the meaning of the values: The values returned will always be"
//					+ " as such: pitch will always be a number between 90 and -90, with -90 being the player looking up, and 90 being the player looking down. Yaw will"
//					+ " always be a number between 0 and 359.9~. When using it as a setter, pitch must be a number between -90 and 90, and yaw may be any number."
//					+ " If the number given is not between 0 and 359.9~, it will be normalized first. 0 is dead west, 90 is north, etc.";
//		}
//		
//		public Argument returnType() {
//			return new Argument("", C.class);
//		}
//
//		public ArgumentBuilder arguments() {
//			return ArgumentBuilder.Build(
//						new Argument("", C.class, ""),
//						new Argument("", C.class, "")
//					);
//		}
//
//		public ExceptionType[] thrown() {
//			return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.RangeException, ExceptionType.CastException};
//		}
//
//		public boolean isRestricted() {
//			return true;
//		}
//
//		public CHVersion since() {
//			return CHVersion.V3_1_3;
//		}
//
//		public Boolean runAsync() {
//			return false;
//		}
//
//		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
//			MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
//			//Getter
//			if (args.length == 0 || args.length == 1) {
//				MCLocation l = null;
//				if (args.length == 0) {
//					if (p instanceof MCPlayer) {
//						l = ((MCPlayer) p).getLocation();
//					}
//				} else if (args.length == 1) {
//					//if it's a number, we are setting F. Otherwise, it's a getter for the MCPlayer specified.
//					try {
//						Integer.parseInt(args[0].val());
//					} catch (NumberFormatException e) {
//						MCPlayer p2 = Static.GetPlayer(args[0], t);
//						l = p2.getLocation();
//					}
//				}
//				if (l != null) {
//					float yaw = l.getYaw();
//					float pitch = l.getPitch();
//					//normalize yaw
//					if (yaw < 0) {
//						yaw = (((yaw) % 360) + 360);
//					}
//					return new CArray(t, new CDouble(yaw, t), new CDouble(pitch, t));
//				}
//			}
//			//Setter
//			MCPlayer toSet = null;
//			float yaw = 0;
//			float pitch = 0;
//			if (args.length == 1) {
//				//We are setting F for this MCPlayer
//				if (p instanceof MCPlayer) {
//					toSet = (MCPlayer) p;
//					pitch = toSet.getLocation().getPitch();
//				}
//				int g = args[0].primitive(t).castToInt32(t);
//				if (g < 0 || g > 3) {
//					throw new ConfigRuntimeException("The F specifed must be from 0 to 3",
//							ExceptionType.RangeException, t);
//				}
//				yaw = g * 90;
//			} else if (args.length == 2) {
//				//Either we are setting this MCPlayer's pitch and yaw, or we are setting the specified MCPlayer's F.
//				//Check to see if args[0] is a number
//				try {
//					Float.parseFloat(args[0].val());
//					//It's the yaw, pitch variation
//					if (p instanceof MCPlayer) {
//						toSet = (MCPlayer) p;
//					}
//					yaw = (float) args[0].primitive(t).castToDouble(t);
//					pitch = (float) args[1].primitive(t).castToDouble(t);
//				} catch (NumberFormatException e) {
//					//It's the MCPlayer, F variation
//					toSet = Static.GetPlayer(args[0], t);
//					pitch = toSet.getLocation().getPitch();
//					int g = args[1].primitive(t).castToInt32(t);
//					if (g < 0 || g > 3) {
//						throw new ConfigRuntimeException("The F specifed must be from 0 to 3",
//								ExceptionType.RangeException, t);
//					}
//					yaw = g * 90;
//				}
//			} else if (args.length == 3) {
//				//It's the MCPlayer, yaw, pitch variation
//				toSet = Static.GetPlayer(args[0], t);
//				yaw = (float) args[1].primitive(t).castToDouble(t);
//				pitch = (float) args[2].primitive(t).castToDouble(t);
//			}
//
//			//Error check our data
//			if (pitch > 90 || pitch < -90) {
//				throw new ConfigRuntimeException("pitch must be between -90 and 90",
//						ExceptionType.RangeException, t);
//			}
//			Static.AssertPlayerNonNull(toSet, t);
//			MCLocation l = toSet.getLocation().clone();
//			l.setPitch(pitch);
//			l.setYaw(yaw);
//			toSet.teleport(l);
//			return new CVoid(t);
//		}
//	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class pmode extends AbstractFunction {

		public String getName() {
			return "pmode";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		public String docs() {
			return "Returns the player's game mode.";
		}
		
		public Argument returnType() {
			return new Argument("", MCGameMode.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						PLAYER_ARG
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PlayerOfflineException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_1_3;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCPlayer m = null;
			if (p instanceof MCPlayer) {
				m = (MCPlayer) p;
			}
			if (args.length == 1) {
				m = Static.GetPlayer(args[0], t);
			}

			Static.AssertPlayerNonNull(m, t);
			String mode = m.getGameMode().name();
			return new CString(mode, t);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_pmode extends AbstractFunction {

		public String getName() {
			return "set_pmode";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "Sets the player's game mode";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						PLAYER_ARG,
						new Argument("", MCGameMode.class, "mode").addAnnotation(new NonNull())
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.FormatException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_1_3;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			ArgList list = getBuilder().parse(args, this, t);
			MCPlayer p = Static.GetPlayer(list.getStringWithNull("player", t), env, t);
			MCGameMode mode = list.getEnum("mode", MCGameMode.class);
			p.setGameMode(mode);
			return new CVoid(t);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class pexp extends AbstractFunction {

		public String getName() {
			return "pexp";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		public String docs() {
			return "Gets the experience of a player within this level, as a percentage, from 0 to 99. (100 would be next level,"
					+ " therefore, 0.)";
		}
		
		public Argument returnType() {
			return new Argument("", CInt.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						PLAYER_ARG
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.PlayerOfflineException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_1_3;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCPlayer m = null;
			if (p instanceof MCPlayer) {
				m = (MCPlayer) p;
			}
			if (args.length == 1) {
				m = Static.GetPlayer(args[0].val(), t);
			}
			Static.AssertPlayerNonNull(m, t);
			return new CInt((int) (m.getExp() * 100), t);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_pexp extends AbstractFunction {

		public String getName() {
			return "set_pexp";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "Sets the experience of a player within the current level, as a percentage, from 0 to 99.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						PLAYER_ARG,
						new Argument("", CInt.class, "xp").addAnnotation(new Ranged(0, 100))
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.PlayerOfflineException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_1_3;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCPlayer m = null;
			int xp = 0;
			if (p instanceof MCPlayer) {
				m = (MCPlayer) p;
			}
			if (args.length == 2) {
				m = Static.GetPlayer(args[0].val(), t);
				xp = args[1].primitive(t).castToInt32(t);
			} else {
				xp = args[0].primitive(t).castToInt32(t);
			}
			Static.AssertPlayerNonNull(m, t);
			m.setExp(((float) xp) / 100.0F);
			return new CVoid(t);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class give_pexp extends AbstractFunction {

		public String getName() {
			return "give_pexp";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "Gives the player the specified amount of xp.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						PLAYER_ARG,
						new Argument("", CInt.class, "xp")
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		public Boolean runAsync() {
			return true;
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCCommandSender p = environment.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCPlayer m = null;
			int xp = 0;
			if (p instanceof MCPlayer) {
				m = (MCPlayer) p;
			}
			if (args.length == 2) {
				m = Static.GetPlayer(args[0].val(), t);
				xp = args[1].primitive(t).castToInt32(t);
			} else {
				xp = args[0].primitive(t).castToInt32(t);
			}
			Static.AssertPlayerNonNull(m, t);
			m.giveExp(xp);

			return new CVoid(t);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class plevel extends AbstractFunction {

		public String getName() {
			return "plevel";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		public String docs() {
			return "Gets the player's level.";
		}
		
		public Argument returnType() {
			return new Argument("", CInt.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						PLAYER_ARG
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.PlayerOfflineException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_1_3;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCPlayer m = null;
			if (p instanceof MCPlayer) {
				m = (MCPlayer) p;
			}
			if (args.length == 1) {
				m = Static.GetPlayer(args[0].val(), t);
			}
			Static.AssertPlayerNonNull(m, t);
			return new CInt(m.getLevel(), t);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_plevel extends AbstractFunction {

		public String getName() {
			return "set_plevel";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "Sets the level of a player.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						PLAYER_ARG,
						new Argument("", CInt.class, "level").addAnnotation(Ranged.POSITIVE)
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.PlayerOfflineException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_1_3;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCPlayer m = null;
			int level = 0;
			if (p instanceof MCPlayer) {
				m = (MCPlayer) p;
			}
			if (args.length == 2) {
				m = Static.GetPlayer(args[0].val(), t);
				level = args[1].primitive(t).castToInt32(t);
			} else {
				level = args[0].primitive(t).castToInt32(t);
			}
			Static.AssertPlayerNonNull(m, t);
			m.setLevel(level);
			return new CVoid(t);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class ptexp extends AbstractFunction {

		public String getName() {
			return "ptexp";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		public String docs() {
			return "Gets the total experience of a player.";
		}
		
		public Argument returnType() {
			return new Argument("", CInt.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						PLAYER_ARG
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.PlayerOfflineException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_1_3;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCPlayer m = null;
			if (p instanceof MCPlayer) {
				m = (MCPlayer) p;
			}
			if (args.length == 1) {
				m = Static.GetPlayer(args[0].val(), t);
			}
			Static.AssertPlayerNonNull(m, t);
			return new CInt(m.getTotalExperience(), t);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_ptexp extends AbstractFunction {

		public String getName() {
			return "set_ptexp";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "Sets the total experience of a player.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						PLAYER_ARG,
						new Argument("", CInt.class, "xp").addAnnotation(Ranged.POSITIVE)
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.PlayerOfflineException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_1_3;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCPlayer m = null;
			int xp = 0;
			if (p instanceof MCPlayer) {
				m = (MCPlayer) p;
			}
			if (args.length == 2) {
				m = Static.GetPlayer(args[0].val(), t);
				xp = args[1].primitive(t).castToInt32(t);
			} else {
				xp = args[0].primitive(t).castToInt32(t);
			}
			Static.AssertPlayerNonNull(m, t);
			m.setTotalExperience(xp);
//            m.setLevel(0);
//            m.setExp(0);
//            m.setTotalExperience(0);
//            m.giveExp(xp);
			return new CVoid(t);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class pfood extends AbstractFunction {

		public String getName() {
			return "pfood";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		public String docs() {
			return "Returns the player's current food level.";
		}
		
		public Argument returnType() {
			return new Argument("", CInt.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						PLAYER_ARG
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PlayerOfflineException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_1_3;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCPlayer m = null;
			if (p instanceof MCPlayer) {
				m = (MCPlayer) p;
			}
			if (args.length == 1) {
				m = Static.GetPlayer(args[0].val(), t);
			}
			Static.AssertPlayerNonNull(m, t);
			return new CInt(m.getFoodLevel(), t);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_pfood extends AbstractFunction {

		public String getName() {
			return "set_pfood";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "Sets the player's food level.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						PLAYER_ARG,
						new Argument("", CInt.class, "level").addAnnotation(Ranged.POSITIVE)
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_1_3;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCPlayer m = null;
			int level = 0;
			if (p instanceof MCPlayer) {
				m = (MCPlayer) p;
			}
			if (args.length == 2) {
				m = Static.GetPlayer(args[0].val(), t);
				level = args[1].primitive(t).castToInt32(t);
			} else {
				level = args[0].primitive(t).castToInt32(t);
			}
			Static.AssertPlayerNonNull(m, t);
			m.setFoodLevel(level);
			return new CVoid(t);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_peffect extends AbstractFunction {

		public String getName() {
			return "set_peffect";
		}

		public Integer[] numArgs() {
			return new Integer[]{3, 4, 5};
		}

		public String docs() {
			return "Adds (or removes) a potion effect from a player. See http://www.minecraftwiki.net/wiki/Potion_effects for a"
					+ " complete list of potions that can be added, and their effects."
					+ " To remove an effect, set the duration or strength to 0."
					+ " Strength is the number of levels to add to the base power (effect level 1)."
					+ " It returns true if the effect was added or removed as desired. It returns false if the effect was"
					+ " not added or removed as desired (however, this currently only will happen if an effect is attempted"
					+ " to be removed, yet isn't already on the player).";
		}
		
		public Argument returnType() {
			return new Argument("", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						PLAYER_ARG,
						new Argument("The effect ID to be added", CInt.class, "potionID").addAnnotation(new Ranged(1, true, 19, true)),
						new Argument("The strength of the potion", CInt.class, "strength").addAnnotation(Ranged.POSITIVE),
						new Argument("The duration of the effect", CInt.class, "seconds").setOptionalDefault(30).addAnnotation(Ranged.POSITIVE),
						new Argument("If true, the effect is more noticable", CBoolean.class, "ambient").setOptionalDefault(false)
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.CastException,
						ExceptionType.RangeException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			ArgList list = getBuilder().parse(args, this, t);
			MCPlayer player = Static.GetPlayer(list.getStringWithNull("player", t), env, t);
			int potionID = list.getInt("potionID", t);
			int strength = list.getInt("strength", t);
			int seconds = list.getInt("seconds", t);
			boolean ambient = list.getBoolean("ambient", t);
			
			if (seconds == 0) {
				return new CBoolean(player.removeEffect(potionID), t);
			} else {
				player.addEffect(potionID, strength, seconds, ambient, t);
				return new CBoolean(true, t);
			}
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Give player Notch nausea for 30 seconds", "set_peffect('Notch', 9, 0)",
						"The player will experience a wobbly screen."),
				new ExampleScript("Make player ArenaPlayer unable to jump for 10 minutes", "set_peffect('ArenaPlayer', 8, -16, 600)",
						"From the player's perspective, they will not even leave the ground."),
				new ExampleScript("Remove poison from yourself", "set_peffect(player(), 19, 1, 0)",
						"You are now unpoisoned. Note, it does not matter what you set strength to here.")
			};
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_peffect extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PlayerOfflineException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if (args.length > 0) {
				p = Static.GetPlayer(args[0], t);
			}
			Static.AssertPlayerNonNull(p, t);
			return ObjectGenerator.GetGenerator().potions(p.getEffects(), t);
		}

		public String getName() {
			return "get_peffect";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		public String docs() {
			return "Returns an array of effects that are currently active on a given player.";
		}
		
		public Argument returnType() {
			return new Argument("", CArray.class).setGenerics(new Generic(MPotion.class));
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						PLAYER_ARG
					);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_phealth extends AbstractFunction {

		public String getName() {
			return "set_phealth";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "Sets the player's health.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						PLAYER_ARG,
						new Argument("", CInt.class, "health").addAnnotation(new Ranged(0, true, 20, true))
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.RangeException, ExceptionType.PlayerOfflineException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_2_0;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			ArgList list = getBuilder().parse(args, this, t);
			MCPlayer player = Static.GetPlayer(list.getStringWithNull("player", t), env, t);
			int health = list.getInt("health", t);
			player.setHealth(health);
			return new CVoid(t);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class ponline extends AbstractFunction {

		public String getName() {
			return "ponline";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "Returns whether or not the specified player is online. Note"
					+ " that the name must match exactly, but it will not throw a PlayerOfflineException"
					+ " if the player is not online, or if the player doesn't even exist.";
		}
		
		public Argument returnType() {
			return new Argument("", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("The player to check. The name must be an exact match", CString.class, "player")
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			//We have to use this method here, because we might be in the midst
			//of an event, in which the player is offline, but not really. It will
			//throw an exception if the player doesn't exist
			MCPlayer p = null;
			try {
				p = Static.GetPlayer(args[0], t);
			} catch (ConfigRuntimeException e) {
				//They aren't in the player list
			}
			//If the player we grabbed doesn't match exactly, we're referring to another player
			//However, we had to check with Static.GetPlayer first, in case this is an injected player.
			//Otherwise, we need to use the player returned from Static.GetPlayer, not the one returned
			//from the server directly
			if (p != null && !p.getName().equals(args[0].val())) {
				MCOfflinePlayer player = Static.getServer().getOfflinePlayer(args[0].val());
				return new CBoolean(player.isOnline(), t);
			} else if (p != null) {
				return new CBoolean(p.isOnline(), t);
			} else {
				return new CBoolean(false, t);
			}
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class pwhitelisted extends AbstractFunction {

		public String getName() {
			return "pwhitelisted";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "Returns whether or not this player is whitelisted. Note that"
					+ " this will work with offline players, but the name must be exact.";
		}
		
		public Argument returnType() {
			return new Argument("", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("The name of the player. This must be an exact match", CString.class, "player")
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCOfflinePlayer pl = Static.getServer().getOfflinePlayer(args[0].val());
			boolean ret;
			if (pl == null) {
				ret = false;
			} else {
				ret = pl.isWhitelisted();
			}
			return new CBoolean(ret, t);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_pwhitelisted extends AbstractFunction {

		public String getName() {
			return "set_pwhitelisted";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public String docs() {
			return "Sets the whitelist flag of the specified player. Note that"
					+ " this will work with offline players, but the name must be exact.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("The name of the player. This must be an exact match.", CString.class, "player"),
						new Argument("", CBoolean.class, "isWhitelisted")
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCOfflinePlayer pl = Static.getServer().getOfflinePlayer(args[0].val());
			boolean whitelist = args[1].primitive(t).castToBoolean();
			pl.setWhitelisted(whitelist);
			return new CVoid(t);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class pbanned extends AbstractFunction {

		public String getName() {
			return "pbanned";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "Returns whether or not this player is banned. Note that"
					+ " this will work with offline players, but the name must be exact. At this"
					+ " time, this function only works with the vanilla ban system. If you use"
					+ " a third party ban system, you should instead run the command for that"
					+ " plugin instead.";
		}
		
		public Argument returnType() {
			return new Argument("", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("The name of the player. This must be an exact match.", CString.class, "player")
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCOfflinePlayer pl = Static.getServer().getOfflinePlayer(args[0].val());
			return new CBoolean(pl.isBanned(), t);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_pbanned extends AbstractFunction {

		public String getName() {
			return "set_pbanned";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public String docs() {
			return "Sets the ban flag of the specified player. Note that"
					+ " this will work with offline players, but the name must be exact. At this"
					+ " time, this function only works with the vanilla ban system. If you use"
					+ " a third party ban system, you should instead run the command for that"
					+ " plugin instead.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("The name of the player. This must be an exact match.", CString.class, "player"),
						new Argument("", CBoolean.class, "isBanned")
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCOfflinePlayer pl = Static.getServer().getOfflinePlayer(args[0].val());
			boolean ban = args[1].primitive(t).castToBoolean();
			pl.setBanned(ban);
			return new CVoid(t);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_pwalkspeed extends AbstractFunction {

		public String getName() {
			return "set_pwalkspeed";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "Sets players speed. The speed must be between -1 or 1";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						PLAYER_ARG,
						new Argument("", CDouble.class, "speed").addAnnotation(new Ranged(-1, true, 1, true))
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.RangeException, ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCPlayer m = null;
			double speed = 0;
			
			if (p instanceof MCPlayer) {
				m = (MCPlayer) p;
			}
			if (args.length == 2) {
				m = Static.GetPlayer(args[0].val(), env, t);
				speed = args[1].primitive(t).castToDouble(t);
			} else {
				speed = args[0].primitive(t).castToDouble(t);
			}

			if(speed < -1 || speed > 1) {
				throw new ConfigRuntimeException("Speed must be between -1 and 1", ExceptionType.RangeException, t);
			}
			Static.AssertPlayerNonNull(m, t);
			
			m.setWalkSpeed((float) speed);
			return new CVoid(t);
		}
	}
	
	@api(environments = {CommandHelperEnvironment.class})
	public static class get_pwalkspeed extends AbstractFunction {

		public String getName() {
			return "get_pwalkspeed";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		public String docs() {
			return "Gets the players speed. The speed must be between -1 or 1";
		}

		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						PLAYER_ARG
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.FormatException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCPlayer m = null;
			
			if (p instanceof MCPlayer) {
				m = (MCPlayer) p;
			}
			
			if (args.length == 1) {
				m = Static.GetPlayer(args[0].val(), env, t);
			}
			
			Static.AssertPlayerNonNull(m, t);

			return new CDouble(m.getWalkSpeed(), t);
		}
	}
	
	@api(environments = {CommandHelperEnvironment.class})
	public static class set_pflyspeed extends AbstractFunction {

		public String getName() {
			return "set_pflyspeed";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "Sets players fly speed. The speed must be between -1 or 1";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						PLAYER_ARG,
						new Argument("", CDouble.class, "speed").addAnnotation(new Ranged(-1, true, 1, true))
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.RangeException, ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCPlayer m = null;
			float speed = 0;
			
			if (p instanceof MCPlayer) {
				m = (MCPlayer) p;
			}
			if (args.length == 2) {
				m = Static.GetPlayer(args[0].val(), env, t);
				speed = args[1].primitive(t).castToDouble32(t);
			} else {
				speed = args[0].primitive(t).castToDouble32(t);
			}

			if(speed < -1 || speed > 1) {
				throw new ConfigRuntimeException("Speed must be between -1 and 1", ExceptionType.RangeException, t);
			}
			Static.AssertPlayerNonNull(m, t);
			
			m.setFlySpeed(speed);
			return new CVoid(t);
		}
	}
	
	@api(environments = {CommandHelperEnvironment.class})
	public static class get_pflyspeed extends AbstractFunction {

		public String getName() {
			return "get_pflyspeed";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		public String docs() {
			return "Gets the players speed. The speed must be between -1 or 1";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						PLAYER_ARG
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.FormatException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCPlayer m = null;
			
			if (p instanceof MCPlayer) {
				m = (MCPlayer) p;
			}
			
			if (args.length == 1) {
				m = Static.GetPlayer(args[0].val(), env, t);
			}
			
			Static.AssertPlayerNonNull(m, t);

			return new CDouble(m.getFlySpeed(), t);
		}
	}
	
	@api(environments = {CommandHelperEnvironment.class})
	public static class set_pwalkspeed extends AbstractFunction {

		public String getName() {
			return "set_pwalkspeed";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "void {[player], speed} Sets players speed. The speed must be between -1 or 1";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.RangeException, ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCPlayer m = null;
			double speed = 0;
			
			if (p instanceof MCPlayer) {
				m = (MCPlayer) p;
			}
			if (args.length == 2) {
				m = Static.GetPlayer(args[0], t);
				speed = Static.getDouble(args[1], t);
			} else {
				speed = Static.getDouble(args[0], t);
			}

			if(speed < -1 || speed > 1) {
				throw new ConfigRuntimeException("Speed must be between -1 and 1", ExceptionType.RangeException, t);
			}
			Static.AssertPlayerNonNull(m, t);
			
			m.setWalkSpeed((float) speed);
			return new CVoid(t);
		}
	}
	
	@api(environments = {CommandHelperEnvironment.class})
	public static class get_pwalkspeed extends AbstractFunction {

		public String getName() {
			return "get_pwalkspeed";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		public String docs() {
			return "void {[player]} Gets the players speed. The speed must be between -1 or 1";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.FormatException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCPlayer m = null;
			
			if (p instanceof MCPlayer) {
				m = (MCPlayer) p;
			}
			
			if (args.length == 1) {
				m = Static.GetPlayer(args[0], t);
			}
			
			Static.AssertPlayerNonNull(m, t);

			return new CDouble(((double) m.getWalkSpeed()), t);
		}
	}
	
	@api(environments = {CommandHelperEnvironment.class})
	public static class set_pflyspeed extends AbstractFunction {

		public String getName() {
			return "set_pflyspeed";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "void {[player], speed} Sets players fly speed. The speed must be between -1 or 1";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.RangeException, ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCPlayer m = null;
			double speed = 0;
			
			if (p instanceof MCPlayer) {
				m = (MCPlayer) p;
			}
			if (args.length == 2) {
				m = Static.GetPlayer(args[0], t);
				speed = Static.getDouble(args[1], t);
			} else {
				speed = Static.getDouble(args[0], t);
			}

			if(speed < -1 || speed > 1) {
				throw new ConfigRuntimeException("Speed must be between -1 and 1", ExceptionType.RangeException, t);
			}
			Static.AssertPlayerNonNull(m, t);
			
			m.setFlySpeed((float) speed);
			return new CVoid(t);
		}
	}
	
	@api(environments = {CommandHelperEnvironment.class})
	public static class get_pflyspeed extends AbstractFunction {

		public String getName() {
			return "get_pflyspeed";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		public String docs() {
			return "void {[player]} Gets the players speed. The speed must be between -1 or 1";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.FormatException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCPlayer m = null;
			
			if (p instanceof MCPlayer) {
				m = (MCPlayer) p;
			}
			
			if (args.length == 1) {
				m = Static.GetPlayer(args[0], t);
			}
			
			Static.AssertPlayerNonNull(m, t);

			return new CDouble(((double) m.getFlySpeed()), t);
		}
	}
	
	@api(environments = {CommandHelperEnvironment.class})
	public static class pisop extends AbstractFunction {

		public String getName() {
			return "pisop";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		public String docs() {
			return "Returns whether or not the specified player (or the current"
					+ " player if not specified) is op";
		}
		
		public Argument returnType() {
			return new Argument("", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						PLAYER_ARG
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PlayerOfflineException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer m = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if (args.length == 1) {
				m = Static.GetPlayer(args[0].val(), t);
			}
			Static.AssertPlayerNonNull(m, t);
			return new CBoolean(m.isOp(), t);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_compass_target extends AbstractFunction {

		public String getName() {
			return "set_compass_target";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "Sets the player's compass target, and returns the old location.";
		}
		
		public Argument returnType() {
			return new Argument("", MLocation.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						PLAYER_ARG,
						new Argument("", MLocation.class, "location")
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.FormatException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer m = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			MCLocation l;
			if (args.length == 1) {
				l = ObjectGenerator.GetGenerator().location(args[0], null, t);
			} else {
				m = Static.GetPlayer(args[0].val(), t);
				l = ObjectGenerator.GetGenerator().location(args[1], null, t);
			}
			if (m == null) {
				throw new ConfigRuntimeException("That player is not online", ExceptionType.PlayerOfflineException, t);
			}
			Static.AssertPlayerNonNull(m, t);
			MCLocation old = m.getCompassTarget();
			m.setCompassTarget(l);
			return ObjectGenerator.GetGenerator().location(old);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_compass_target extends AbstractFunction {

		public String getName() {
			return "get_compass_target";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		public String docs() {
			return "Gets the compass target of the specified player";
		}
		
		public Argument returnType() {
			return new Argument("", MLocation.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						PLAYER_ARG
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PlayerOfflineException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer m = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if (args.length == 1) {
				m = Static.GetPlayer(args[0].val(), t);
			}
			Static.AssertPlayerNonNull(m, t);
			return ObjectGenerator.GetGenerator().location(m.getCompassTarget());
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class ponfire extends AbstractFunction {

		public String getName() {
			return "ponfire";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		public String docs() {
			return "Returns the number of ticks remaining that this player will"
					+ " be on fire for. If the player is not on fire, 0 is returned, which incidentally"
					+ " is false.";
		}
		
		public Argument returnType() {
			return new Argument("", CInt.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						PLAYER_ARG
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PlayerOfflineException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if (args.length == 1) {
				p = Static.GetPlayer(args[0], t);
			}
			Static.AssertPlayerNonNull(p, t);
			int left = p.getRemainingFireTicks();
			return new CInt(left, t);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_ponfire extends AbstractFunction {

		public String getName() {
			return "set_ponfire";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "Sets the player on fire for the specified number of"
					+ " ticks. If a boolean is given for ticks, false is 0, and true is 20.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						PLAYER_ARG,
						new Argument("", CInt.class, "ticks")
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			Mixed ticks;
			if (args.length == 2) {
				p = Static.GetPlayer(args[0], t);
				ticks = args[1];
			} else {
				ticks = args[0];
			}
			int tick = 0;
			if (ticks instanceof CBoolean) {
				boolean value = ((CBoolean) ticks).primitive(t).castToBoolean();
				if (value) {
					tick = 20;
				}
			} else {
				tick = ticks.primitive(t).castToInt32(t);
			}
			Static.AssertPlayerNonNull(p, t);
			p.setRemainingFireTicks(tick);
			return new CVoid(t);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class phas_flight extends AbstractFunction {

		public String getName() {
			return "phas_flight";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		public String docs() {
			return "Returns whether or not the player has the ability to fly";
		}
		
		public Argument returnType() {
			return new Argument("", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						PLAYER_ARG
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PlayerOfflineException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if (args.length == 1) {
				p = Static.GetPlayer(args[0], t);
			}
			Static.AssertPlayerNonNull(p, t);
			return new CBoolean(p.getAllowFlight(), t);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	@Deprecated
	public static class pset_flight extends set_pflight implements Optimizable {

		@Override
		public String getName() {
			return "pset_flight";
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			return super.exec(t, environment, args);
		}

		@Override
		public String docs() {
			return super.docs() + " DEPRECATED(use set_pflight instead)";
		}
		
		@Override
		public Argument returnType() {
			return super.returnType();
		}

		@Override
		public ArgumentBuilder arguments() {
			return super.arguments();
		}

		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC);
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment e, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException {
			CHLog.GetLogger().Log(CHLog.Tags.COMPILER, LogLevel.WARNING, "Use of pset_flight is deprecated, change it to set_pflight before the next release", t);
			return null;
		}

	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_pflight extends AbstractFunction {

		public String getName() {
			return "set_pflight";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "Sets whether or not this player is allowed to fly";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						PLAYER_ARG,
						new Argument("", CBoolean.class, "flight")
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PlayerOfflineException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			boolean flight;
			if (args.length == 1) {
				flight = args[0].primitive(t).castToBoolean();
			} else {
				p = Static.GetPlayer(args[0], t);
				flight = args[1].primitive(t).castToBoolean();
			}
			Static.AssertPlayerNonNull(p, t);
			p.setAllowFlight(flight);
			return new CVoid(t);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}
	private static final SortedMap<String, Construct> TimeLookup = new TreeMap<String, Construct>();

	static {
		Properties p = new Properties();
		try {
			p.load(Minecraft.class.getResourceAsStream("/time_names.txt"));
			Enumeration e = p.propertyNames();
			while (e.hasMoreElements()) {
				String name = e.nextElement().toString();
				TimeLookup.put(name, new CString(p.getProperty(name).toString(), Target.UNKNOWN));
			}
		} catch (IOException ex) {
			Logger.getLogger(World.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	@Deprecated
	public static class pset_time extends set_ptime implements Optimizable {

		@Override
		public String getName() {
			return "pset_time";
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			return super.exec(t, environment, args);
		}

		@Override
		public String docs() {
			return super.docs() + " DEPRECATED(use set_ptime instead)";
		}
		
		@Override
		public Argument returnType() {
			return super.returnType();
		}

		@Override
		public ArgumentBuilder arguments() {
			return super.arguments();
		}

		public Set<Optimizable.OptimizationOption> optimizationOptions() {
			return EnumSet.of(Optimizable.OptimizationOption.OPTIMIZE_DYNAMIC);
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment e, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException {
			CHLog.GetLogger().Log(CHLog.Tags.COMPILER, LogLevel.WARNING, "Use of pset_time is deprecated, change it to set_ptime before the next release", t);
			return null;
		}

	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_ptime extends AbstractFunction {

		public String getName() {
			return "set_ptime";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			StringBuilder doc = new StringBuilder();
			doc.append("Sets the time of a given player. Should be a number from 0 to"
					+ " 24000, if not, it is modulo scaled. Alternatively, common time notation (9:30pm, 4:00 am)"
					+ " is acceptable, and convenient english mappings also exist:");
			doc.append("<ul>");
			for (String key : TimeLookup.keySet()) {
				doc.append("<li>").append(key).append(" = ").append(TimeLookup.get(key)).append("</li>");
			}
			doc.append("</ul>");
			return doc.toString();
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						PLAYER_ARG,
						new Argument("", CInt.class, CString.class, "time")
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.FormatException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = null;
			if (environment.getEnv(CommandHelperEnvironment.class).GetPlayer() != null) {
				p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			}
			if (args.length == 2) {
				p = Static.GetPlayer(args[0], t);
			}
			Static.AssertPlayerNonNull(p, t);
			long time = 0;
			String stime = (args.length == 1 ? args[0] : args[1]).val().toLowerCase();
			if (TimeLookup.containsKey(stime.replaceAll("[^a-z]", ""))) {
				stime = TimeLookup.get(stime.replaceAll("[^a-z]", "")).val();
			}
			if (stime.matches("^([\\d]+)[:.]([\\d]+)[ ]*?(?:([pa])\\.*m\\.*){0,1}$")) {
				Pattern pa = Pattern.compile("^([\\d]+)[:.]([\\d]+)[ ]*?(?:([pa])\\.*m\\.*){0,1}$");
				Matcher m = pa.matcher(stime);
				m.find();
				int hour = Integer.parseInt(m.group(1));
				int minute = Integer.parseInt(m.group(2));
				String offset = "a";
				if (m.group(3) != null) {
					offset = m.group(3);
				}
				if (offset.equals("p")) {
					hour += 12;
				}
				if (hour == 24) {
					hour = 0;
				}
				if (hour > 24) {
					throw new ConfigRuntimeException("Invalid time provided", ExceptionType.FormatException, t);
				}
				if (minute > 59) {
					throw new ConfigRuntimeException("Invalid time provided", ExceptionType.FormatException, t);
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
				throw new ConfigRuntimeException("Invalid time provided", ExceptionType.FormatException, t);
			}
			time = Long.parseLong(stime);
			p.setPlayerTime(time);
			return new CVoid(t);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class pget_time extends AbstractFunction {

		public String getName() {
			return "pget_time";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		public String docs() {
			return "Returns the time of the specified player, as an integer from"
					+ " 0 to 24000-1";
		}
		
		public Argument returnType() {
			return new Argument("", CInt.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						PLAYER_ARG
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PlayerOfflineException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = null;
			if (environment.getEnv(CommandHelperEnvironment.class).GetPlayer() != null) {
				p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			}
			if (args.length == 1) {
				p = Static.GetPlayer(args[0], t);
			}
			Static.AssertPlayerNonNull(p, t);
			return new CInt(p.getPlayerTime(), t);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class preset_time extends AbstractFunction {

		public String getName() {
			return "preset_time";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		public String docs() {
			return "Resets the time of the player to the time of the world.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						PLAYER_ARG
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.InvalidWorldException, ExceptionType.PlayerOfflineException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = null;
			if (environment.getEnv(CommandHelperEnvironment.class).GetPlayer() != null) {
				p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			}
			if (args.length == 1) {
				p = Static.GetPlayer(args[0], t);
			}
			Static.AssertPlayerNonNull(p, t);
			p.resetPlayerTime();
			return new CVoid(t);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_list_name extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.LengthException, ExceptionType.FormatException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer m = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			String listName;
			if (args.length == 2) {
				m = Static.GetPlayer(args[0], t);
				listName = args[1].val();
			} else {
				listName = args[0].val();
			}

			if (listName.length() > 16) {
				throw new ConfigRuntimeException("set_list_name([player,] name) expects name to be 16 characters or less", Exceptions.ExceptionType.LengthException, t);
			}

			Static.AssertPlayerNonNull(m, t);
			m.setPlayerListName(listName);
			return new CVoid(t);
		}

		public String getName() {
			return "set_list_name";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "Sets the player's list name."
					+ " The name cannot be longer than 16 characters, but colors are supported."
					+ " Setting the name to null resets it. If the name specified is already taken,"
					+ " a FormatException is thrown, and if the length of the name is greater than 16"
					+ " characters, a LengthException is thrown.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						PLAYER_ARG,
						new Argument("", CString.class, "listName").setOptionalDefaultNull()
					);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_list_name extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PlayerOfflineException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer m = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if (args.length == 1) {
				m = Static.GetPlayer(args[0], t);
			}
			return new CString(m.getPlayerListName(), t);
		}

		public String getName() {
			return "get_list_name";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		public String docs() {
			return "Returns the list name of the specified player, or the current player if none specified.";
		}
		
		public Argument returnType() {
			return new Argument("", CString.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						PLAYER_ARG
					);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class pvelocity extends AbstractFunction {

		public String getName() {
			return "pvelocity";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		public String docs() {
			return "Returns an associative array that represents the player's velocity."
					+ " The array contains the following items: magnitude, x, y, z. These represent a"
					+ " 3 dimensional Vector. The important part is x, y, z, however, the magnitude is provided"
					+ " for you as a convenience. (It should equal sqrt(x ** 2 + y ** 2 + z ** 2))";
		}
		
		public Argument returnType() {
			return new Argument("", MVector3D.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						PLAYER_ARG
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PlayerOfflineException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if (args.length == 1) {
				p = Static.GetPlayer(args[0], t);
			}
			MCPlayer.Velocity velocity = p.getVelocity();
			MVector3D vector = new MVector3D();
			vector.magnitude = velocity.magnitude;
			vector.x = velocity.x;
			vector.y = velocity.y;
			vector.z = velocity.z;
			return vector.deconstruct(t);
		}

		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		@Override
		public boolean appearInDocumentation() {
			return false;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_pvelocity extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.PlayerOfflineException, ExceptionType.FormatException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			double x;
			double y;
			double z;
			switch (args.length) {
				case 1:
				case 2: {
					int offset = 0;
					if (args.length == 2) {
						offset = 1;
						p = Static.GetPlayer(args[0], t);
					}
					if (args[offset] instanceof CArray) {
						MCLocation l = ObjectGenerator.GetGenerator().location(args[offset], p.getWorld(), t);
						x = l.getX();
						y = l.getY();
						z = l.getZ();
					} else {
						throw new ConfigRuntimeException("Expecting an array, but \"" + args[offset].val() + "\" was given.", ExceptionType.CastException, t);
					}
					break;
				}
				case 3:
				case 4: {
					int offset = 0;
					if (args.length == 4) {
						offset = 1;
						p = Static.GetPlayer(args[0], t);
					}
					x = args[offset].primitive(t).castToDouble(t);
					y = args[offset + 1].primitive(t).castToDouble(t);
					z = args[offset + 2].primitive(t).castToDouble(t);
					break;
				}
				default:
					throw new RuntimeException();
			}
			MCEntity.Velocity v = new MCEntity.Velocity(x, y, z);
			if (v.magnitude > 10) {
				CHLog.GetLogger().Log(CHLog.Tags.GENERAL, LogLevel.WARNING,
						"The call to " + getName() + " has been cancelled, because the magnitude was greater than 10."
						+ " (It was " + v.magnitude + ")", t);
				return new CBoolean(false, t);
			}
			p.setVelocity(v);
			return new CBoolean(true, t);
		}

		public String getName() {
			return "set_pvelocity";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3, 4};
		}

		public String docs() {
			return "Sets a player's velocity. vector must be an"
					+ " associative array with x, y, and z keys defined (if magnitude is set, it is ignored)."
					+ " If the vector's magnitude is greater than 10, the command is cancelled, because the"
					+ " server won't allow the player to move faster than that. A warning is issued, and false"
					+ " is returned if this"
					+ " happens, otherwise, true is returned.";
		}
		
		public Argument returnType() {
			return new Argument("", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Signature(1,
							PLAYER_ARG,
							new Argument("", MVector3D.class, "velocityVector")
						),
						new Signature(2, 
							PLAYER_ARG,
							new Argument("", CDouble.class, "x"),
							new Argument("", CDouble.class, "y"),
							new Argument("", CDouble.class, "z")
						)
					);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class psend_block_change extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.FormatException, ExceptionType.PlayerOfflineException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			int offset = 0;
			if (args.length == 3) {
				p = Static.GetPlayer(args[0], t);
				offset = 1;
			}
			MCLocation loc = ObjectGenerator.GetGenerator().location(args[0 + offset], p.getWorld(), t);
			MCItemStack item = Static.ParseItemNotation(getName(), args[1 + offset].val(), 1, t);
			p.sendBlockChange(loc, item.getType().getType(), (byte) item.getData().getData());
			return new CVoid(t);
		}

		public String getName() {
			return "psend_block_change";
		}

		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		public String docs() {
			return "Changes a block, but only temporarily, and only for the specified player."
					+ " This can be used to \"fake\" blocks for a player. ItemID is in the 1[:1] data format.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						PLAYER_ARG,
						new Argument("", MLocation.class, "location"),
						new Argument("", CInt.class, CString.class, "itemID")
					);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class phunger extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PlayerOfflineException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if (args.length == 1) {
				p = Static.GetPlayer(args[0], t);
			}
			return new CInt(p.getHunger(), t);
		}

		public String getName() {
			return "phunger";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		public String docs() {
			return "Returns the player's hunger level";
		}
		
		public Argument returnType() {
			return new Argument("", CInt.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						PLAYER_ARG
					);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_phunger extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.RangeException, ExceptionType.PlayerOfflineException, ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			int hunger, hungerIndex = 0;
			if (args.length == 2) {
				p = Static.GetPlayer(args[0], t);
				hungerIndex = 1;
			}
			hunger = args[hungerIndex].primitive(t).castToInt32(t);
			p.setHunger(hunger);
			return new CVoid(t);
		}

		public String getName() {
			return "set_phunger";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "Sets a player's hunger level";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						PLAYER_ARG,
						new Argument("", CInt.class, "hunger").addAnnotation(Ranged.POSITIVE)
					);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class psaturation extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.RangeException, ExceptionType.PlayerOfflineException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if (args.length == 1) {
				p = Static.GetPlayer(args[0], t);
			}
			Static.AssertPlayerNonNull(p, t);
			return new CDouble(p.getSaturation(), t);
		}

		public String getName() {
			return "psaturation";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		public String docs() {
			return "Returns the player's saturation level";
		}
		
		public Argument returnType() {
			return new Argument("", CDouble.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						PLAYER_ARG
					);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_psaturation extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.RangeException, ExceptionType.PlayerOfflineException, ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			float saturation;
			int saturationIndex = 0;
			if (args.length == 2) {
				p = Static.GetPlayer(args[0], t);
				saturationIndex = 1;
			}
			saturation = (float) args[saturationIndex].primitive(t).castToDouble(t);
			p.setSaturation(saturation);
			return new CVoid(t);
		}

		public String getName() {
			return "set_psaturation";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "Sets the player's saturation. While the value can be higher, normally their saturation will only get to 20.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						PLAYER_ARG,
						new Argument("", CInt.class, "saturation").addAnnotation(Ranged.POSITIVE)
					);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class pbed_location extends AbstractFunction {

		public String getName() {
			return "pbed_location";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			MCOfflinePlayer player = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if (args.length == 1) {
				player = Static.getServer().getOfflinePlayer(args[0].val());
			}
			MCLocation loc = player.getBedSpawnLocation();
			MCWorld w;
			try {
				w = loc.getWorld();
			} catch (Exception e) {
				return Construct.GetNullConstruct(MLocation.class, t);
			}
			MLocation l = new MLocation(loc);
			return l.deconstruct(t);
		}

		public String docs() {
			return "Returns the location of the bed of the player. This is set when a player sleeps or by set_pbed_location.";
		}
		
		public Argument returnType() {
			return new Argument("", MLocation.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						PLAYER_ARG
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		public Boolean runAsync() {
			return false;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_pbed_location extends AbstractFunction {

		public String getName() {
			return "set_pbed_location";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3, 4};
		}

		public String docs() {
			return "Sets the location of the bed of the player to the specified coordinates."
					+ " If player is omitted, the current player is used.";
		}
		
		public Argument returnType() {
			return new Argument("", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Signature(1, 
							PLAYER_ARG,
							new Argument("", MLocation.class, "location")
						), new Signature(2,
							PLAYER_ARG,
							new Argument("", CDouble.class, "x"),
							new Argument("", CDouble.class, "y"),
							new Argument("", CDouble.class, "z")
						)
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.LengthException, ExceptionType.PlayerOfflineException, ExceptionType.FormatException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			String pname = null;
			double x;
			double y;
			double z;
			MCPlayer m = null;
			MCLocation l = null;
			if (args.length == 1) {
				if (args[0] instanceof CArray) {
					CArray ca = (CArray) args[0];
					l = ObjectGenerator.GetGenerator().location(ca, (p instanceof MCPlayer ? ((MCPlayer) p).getWorld() : null), t);
					x = ca.get(0, t).primitive(t).castToDouble(t);
					y = ca.get(1, t).primitive(t).castToDouble(t);
					z = ca.get(2, t).primitive(t).castToDouble(t);
					if (p instanceof MCPlayer) {
						m = ((MCPlayer) p);
					}

				} else {
					throw new ConfigRuntimeException("Expecting an array at parameter 1 of set_pbed_location",
							ExceptionType.CastException, t);
				}
			} else if (args.length == 2) {
				if (args[1] instanceof CArray) {
					CArray ca = (CArray) args[1];
					pname = args[0].val();
					l = ObjectGenerator.GetGenerator().location(ca, Static.GetPlayer(pname, t).getWorld(), t);
					x = l.getX();
					y = l.getY();
					z = l.getZ();
				} else {
					throw new ConfigRuntimeException("Expecting parameter 2 to be an array in set_pbed_location",
							ExceptionType.CastException, t);
				}
			} else if (args.length == 3) {
				if (p instanceof MCPlayer) {
					m = (MCPlayer) p;
				}
				x = args[0].primitive(t).castToDouble(t);
				y = args[1].primitive(t).castToDouble(t);
				z = args[2].primitive(t).castToDouble(t);
				l = m.getLocation();
			} else {
				m = Static.GetPlayer(args[0], t);
				x = args[1].primitive(t).castToDouble(t);
				y = args[2].primitive(t).castToDouble(t);
				z = args[3].primitive(t).castToDouble(t);
				l = m.getLocation();
			}
			if (m == null && pname != null) {
				m = Static.GetPlayer(pname, t);
			}
			Static.AssertPlayerNonNull(m, t);
			if (!l.getWorld().exists()) {
				throw new ConfigRuntimeException("The world specified does not exist.", ExceptionType.InvalidWorldException, t);
			};
			m.setBedSpawnLocation(StaticLayer.GetLocation(l.getWorld(), x, y + 1, z, m.getLocation().getYaw(), m.getLocation().getPitch()));
			return new CVoid(t);
		}
	}

	@api(environments={CommandHelperEnvironment.class})
	public static class pvehicle extends AbstractFunction {

		public String getName() {
			return "pvehicle";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		public String docs() {
			return "Returns the id of the vehicle the player is in or null if the player is not in a vehicle.";
		}
		
		public Argument returnType() {
			return new Argument("", CInt.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						PLAYER_ARG
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PlayerOfflineException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if (args.length == 1) {
				p = Static.GetPlayer(args[0].val(), t);
			}

			Static.AssertPlayerNonNull(p, t);
			if (p.isInsideVehicle() == false) {
				return Construct.GetNullConstruct(CInt.class, t);
			}

			return new CInt(p.getVehicle().getEntityId(), t);
		}
	}

	@api(environments={CommandHelperEnvironment.class})
	public static class pvehicle_leave extends AbstractFunction {

		public String getName() {
			return "pvehicle_leave";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		public String docs() {
			return "Causes the player to leave the vehicle they are riding and returns true, or does nothing and returns"
					+ " false if the player is not in a vehicle";
		}
		
		public Argument returnType() {
			return new Argument("", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						PLAYER_ARG
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PlayerOfflineException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if (args.length == 1) {;
				p = Static.GetPlayer(args[0].val(), t);
			}
			Static.AssertPlayerNonNull(p, t);

			return new CBoolean(p.leaveVehicle(), t);
		}
	}

	@api(environments={CommandHelperEnvironment.class})
	public static class get_offline_players extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, Environment environment,
				Mixed... args) throws ConfigRuntimeException {
			MCServer s = environment.getEnv(CommandHelperEnvironment.class).GetCommandSender().getServer();
			CArray ret = new CArray(t);
			for (MCOfflinePlayer offp : s.getOfflinePlayers()) {
				ret.push(new CString(offp.getName(), t));
			}
			return ret;
		}

		public String getName() {
			return "get_offline_players";
		}

		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		public String docs() {
			return "Returns an array of every player who has played on this server.";
		}
		
		public Argument returnType() {
			return new Argument("", CArray.class).setGenerics(new Generic(CString.class));
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.NONE;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
					new ExampleScript("Simple usage", "get_offline_players()", "{Bill, Bob, Joe, Fred}")
			};
		}

	}

	@api(environments={CommandHelperEnvironment.class})
	public static class phas_played extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, Environment environment,
				Mixed... args) throws ConfigRuntimeException {
			MCServer s = environment.getEnv(CommandHelperEnvironment.class).GetCommandSender().getServer();
			MCOfflinePlayer offp = s.getOfflinePlayer(args[0].val());
			return new CBoolean(offp.hasPlayedBefore(), t);
		}

		public String getName() {
			return "phas_played";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "Returns whether the given player has ever been on this server."
					+ " This will not throw a PlayerOfflineException, so the name must be exact.";
		}
		
		public Argument returnType() {
			return new Argument("", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CString.class, "player")
					);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
					new ExampleScript("Demonstrates a player that has played", "phas_played('Notch')", ":true"),
					new ExampleScript("Demonstrates a player that has not played", "phas_played('Herobrine')", ":false")
			};
		}

	}

	@api(environments={CommandHelperEnvironment.class})
	public static class pfirst_played extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, Environment environment,
				Mixed... args) throws ConfigRuntimeException {
			MCCommandSender cs = environment.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCOfflinePlayer op = null;
			if (args.length == 1) {
				op = cs.getServer().getOfflinePlayer(args[0].val());
			} else {
				op = cs.getServer().getOfflinePlayer(cs.getName());
			}
			return new CInt(op.getFirstPlayed(), t);
		}

		public String getName() {
			return "pfirst_played";
		}

		public Integer[] numArgs() {
			return new Integer[]{0,1};
		}

		public String docs() {
			return "Returns the time this player first logged onto this server (as a unix timestamp), or 0 if they never have."
					+ " This will not throw a PlayerOfflineException, so the name must be exact.";
		}
		
		public Argument returnType() {
			return new Argument("", CInt.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CString.class, "player").setOptionalDefaultNull()
					);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
					new ExampleScript("Demonstrates a player that has played", "pfirst_played('Notch')", ":13558362167593"),
					new ExampleScript("Demonstrates a player that has not played", "pfirst_played('Herobrine')", ":0")
			};
		}

	}

	@api(environments={CommandHelperEnvironment.class})
	public static class plast_played extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, Environment environment,
				Mixed... args) throws ConfigRuntimeException {
			MCCommandSender cs = environment.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCOfflinePlayer op = null;
			if (args.length == 1) {
				op = cs.getServer().getOfflinePlayer(args[0].val());
			} else {
				op = cs.getServer().getOfflinePlayer(cs.getName());
			}
			return new CInt(op.getLastPlayed(), t);
		}

		public String getName() {
			return "plast_played";
		}

		public Integer[] numArgs() {
			return new Integer[]{0,1};
		}

		public String docs() {
			return "Returns the time this player was last seen on this server (as a unix time stamp), or 0 if they never were."
					+ " This will not throw a PlayerOfflineException, so the name must be exact.";
		}
		
		public Argument returnType() {
			return new Argument("", CInt.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CString.class, "player").setOptionalDefaultNull()
					);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
					new ExampleScript("Demonstrates a player that has played", "plast_played('Notch')", ":13558362167593"),
					new ExampleScript("Demonstrates a player that has not played", "plast_played('Herobrine')", ":0")
			};
		}

	}
}
