package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCEntity.Velocity;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.MCWorldCreator;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.abstraction.blocks.MCFallingBlock;
import com.laytonsmith.abstraction.enums.MCDifficulty;
import com.laytonsmith.abstraction.enums.MCGameRule;
import com.laytonsmith.abstraction.enums.MCWorldEnvironment;
import com.laytonsmith.abstraction.enums.MCWorldType;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.*;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Layton
 */
public class World {

	public static String docs() {
		return "Provides functions for manipulating a world";
	}

	@api(environments=CommandHelperEnvironment.class)
	public static class get_spawn extends AbstractFunction {

		public String getName() {
			return "get_spawn";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		public String docs() {
			return "array {[world]} Returns a location array for the specified world, or the current player's world, if not specified.";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.InvalidWorldException, ExceptionType.FormatException};
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

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			String world;
			if (args.length == 1) {
				world = args[0].val();
			} else {
				world = environment.getEnv(CommandHelperEnvironment.class).GetPlayer().getWorld().getName();
			}
			MCWorld w = Static.getServer().getWorld(world);
			if (w == null) {
				throw new ConfigRuntimeException("The specified world \"" + world + "\" is not a valid world.", ExceptionType.InvalidWorldException, t);
			}
			return ObjectGenerator.GetGenerator().location(w.getSpawnLocation(), false);
		}
	}

	@api(environments=CommandHelperEnvironment.class)
	public static class set_spawn extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.InvalidWorldException,
						ExceptionType.CastException, ExceptionType.FormatException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCWorld w = (environment.getEnv(CommandHelperEnvironment.class).GetPlayer() != null ? environment.getEnv(CommandHelperEnvironment.class).GetPlayer().getWorld() : null);
			int x = 0;
			int y = 0;
			int z = 0;
			if (args.length == 1) {
				MCLocation l = ObjectGenerator.GetGenerator().location(args[0], w, t);
				w = l.getWorld();
				x = l.getBlockX();
				y = l.getBlockY();
				z = l.getBlockZ();
			} else if (args.length == 3) {
				x = Static.getInt32(args[0], t);
				y = Static.getInt32(args[1], t);
				z = Static.getInt32(args[2], t);
			} else if (args.length == 4) {
				w = Static.getServer().getWorld(args[0].val());
				x = Static.getInt32(args[1], t);
				y = Static.getInt32(args[2], t);
				z = Static.getInt32(args[3], t);
			}
			if (w == null) {
				throw new ConfigRuntimeException("Invalid world given.", ExceptionType.InvalidWorldException, t);
			}
			w.setSpawnLocation(x, y, z);
			return new CVoid(t);
		}

		public String getName() {
			return "set_spawn";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 3, 4};
		}

		public String docs() {
			return "void {locationArray | [world], x, y, z} Sets the spawn of the world. Note that in some cases, a plugin"
					+ " may set the spawn differently, and this method will do nothing. In that case, you should use"
					+ " the plugin's commands to set the spawn.";
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api(environments=CommandHelperEnvironment.class)
	public static class refresh_chunk extends AbstractFunction {

		public String getName() {
			return "refresh_chunk";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3};
		}

		public String docs() {
			return "void {[world], x, z | [world], locationArray} Resends the chunk data to all clients, using the specified world, or the current"
					+ " players world if not provided.";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.FormatException, ExceptionType.InvalidWorldException};
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

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCPlayer m = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			MCWorld world;
			int x;
			int z;
			if (args.length == 1) {
				//Location array provided                
				MCLocation l = ObjectGenerator.GetGenerator().location(args[0], m != null ? m.getWorld() : null, t);
				world = l.getWorld();
				x = l.getBlockX();
				z = l.getBlockZ();
			} else if (args.length == 2) {
				//Either location array and world provided, or x and z. Test for array at pos 2
				if (args[1] instanceof CArray) {
					world = Static.getServer().getWorld(args[0].val());
					MCLocation l = ObjectGenerator.GetGenerator().location(args[1], null, t);
					x = l.getBlockX();
					z = l.getBlockZ();
				} else {
					if (m == null) {
						throw new ConfigRuntimeException("No world specified", ExceptionType.InvalidWorldException, t);
					}
					world = m.getWorld();
					x = Static.getInt32(args[0], t);
					z = Static.getInt32(args[1], t);
				}
			} else {
				//world, x and z provided
				world = Static.getServer().getWorld(args[0].val());
				x = Static.getInt32(args[1], t);
				z = Static.getInt32(args[2], t);
			}
			world.refreshChunk(x, z);
			return new CVoid(t);
		}
	}

	@api(environments=CommandHelperEnvironment.class)
	public static class regen_chunk extends AbstractFunction {

		public String getName() {
			return "regen_chunk";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3};
		}

		public String docs() {
			return "void {x, z, [world]| locationArray, [world]} Regenerate the chunk, using the specified world, or the current"
					+ " players world if not provided. Beware that this is destructive! Any data in this chunk will be lost!";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.FormatException, ExceptionType.InvalidWorldException};
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

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCPlayer m = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			MCWorld world;
			int x;
			int z;

			if (args.length == 1) {
				//Location array provided                
				MCLocation l = ObjectGenerator.GetGenerator().location(args[0], m != null ? m.getWorld() : null, t);

				world = l.getWorld();
				x = l.getChunk().getX();
				z = l.getChunk().getZ();
			} else if (args.length == 2) {
				//Either location array and world provided, or x and z. Test for array at pos 1
				if (args[0] instanceof CArray) {
					world = Static.getServer().getWorld(args[1].val());
					MCLocation l = ObjectGenerator.GetGenerator().location(args[0], null, t);

					x = l.getChunk().getX();
					z = l.getChunk().getZ();
				} else {
					if (m == null) {
						throw new ConfigRuntimeException("No world specified", ExceptionType.InvalidWorldException, t);
					}

					world = m.getWorld();
					x = Static.getInt32(args[0], t);
					z = Static.getInt32(args[1], t);
				}
			} else {
				//world, x and z provided
				x = Static.getInt32(args[0], t);
				z = Static.getInt32(args[1], t);
				world = Static.getServer().getWorld(args[2].val());
			}

			return new CBoolean(world.regenerateChunk(x, z), t);
		}
	}

	private static final SortedMap<String, Construct> TimeLookup = new TreeMap<String, Construct>();

	static {
		synchronized (World.class) {
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
	}

	@api(environments=CommandHelperEnvironment.class)
	public static class set_world_time extends AbstractFunction {

		public String getName() {
			return "set_world_time";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			StringBuilder doc = new StringBuilder();
			synchronized (World.class) {
				doc.append("void {[world], time} Sets the time of a given world. Should be a number from 0 to"
						+ " 24000, if not, it is modulo scaled. ---- Alternatively, common time notation (9:30pm, 4:00 am)"
						+ " is acceptable, and convenient english mappings also exist:");
				doc.append("<ul>");
				for (String key : TimeLookup.keySet()) {
					doc.append("<li>").append(key).append(" = ").append(TimeLookup.get(key)).append("</li>");
				}
				doc.append("</ul>");
			}
			return doc.toString();
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.InvalidWorldException, ExceptionType.FormatException};
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

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCWorld w = null;
			if (environment.getEnv(CommandHelperEnvironment.class).GetPlayer() != null) {
				w = environment.getEnv(CommandHelperEnvironment.class).GetPlayer().getWorld();
			}
			if (args.length == 2) {
				w = Static.getServer().getWorld(args[0].val());
			}
			if (w == null) {
				throw new ConfigRuntimeException("No world specified", ExceptionType.InvalidWorldException, t);
			}
			long time = 0;
			String stime = (args.length == 1 ? args[0] : args[1]).val().toLowerCase();
			if (TimeLookup.containsKey(stime.replaceAll("[^a-z]", ""))) {
				stime = TimeLookup.get(stime.replaceAll("[^a-z]", "")).val();
			}
			if (stime.matches("^([\\d]+)[:.]([\\d]+)[ ]*?(?:([pa])\\.*m\\.*){0,1}$")) {
				Pattern p = Pattern.compile("^([\\d]+)[:.]([\\d]+)[ ]*?(?:([pa])\\.*m\\.*){0,1}$");
				Matcher m = p.matcher(stime);
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
			w.setTime(time);
			return new CVoid(t);
		}
	}

	@api(environments=CommandHelperEnvironment.class)
	public static class get_world_time extends AbstractFunction {

		public String getName() {
			return "get_world_time";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		public String docs() {
			return "int {[world]} Returns the time of the specified world, as an integer from"
					+ " 0 to 24000-1";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.InvalidWorldException};
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

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCWorld w = null;
			if (environment.getEnv(CommandHelperEnvironment.class).GetPlayer() != null) {
				w = environment.getEnv(CommandHelperEnvironment.class).GetPlayer().getWorld();
			}
			if (args.length == 1) {
				w = Static.getServer().getWorld(args[0].val());
			}
			if (w == null) {
				throw new ConfigRuntimeException("No world specified", ExceptionType.InvalidWorldException, t);
			}
			return new CInt(w.getTime(), t);
		}
	}

	@api(environments={CommandHelperEnvironment.class})
	public static class create_world extends AbstractFunction{

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.FormatException, ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			MCWorldCreator creator = StaticLayer.GetConvertor().getWorldCreator(args[0].val());
			if (args.length >= 3) {
				MCWorldType type;
				try {
					type = MCWorldType.valueOf(args[1].val().toUpperCase());
				} catch (IllegalArgumentException e) {
					throw new ConfigRuntimeException(args[1].val() + " is not a valid world type. Must be one of: " + StringUtils.Join(MCWorldType.values(), ", "), ExceptionType.FormatException, t);
				}
				MCWorldEnvironment environment;
				try {
					environment = MCWorldEnvironment.valueOf(args[2].val().toUpperCase());
				} catch (IllegalArgumentException e) {
					throw new ConfigRuntimeException(args[2].val() + " is not a valid environment type. Must be one of: " + StringUtils.Join(MCWorldEnvironment.values(), ", "), ExceptionType.FormatException, t);
				}
				creator.type(type).environment(environment);
			}
			if ((args.length >= 4) && !(args[3] instanceof CNull)) {
				if (args[3] instanceof CInt) {
					creator.seed(Static.getInt(args[3], t));
				} else {
					creator.seed(args[3].val().hashCode());
				}
			}
			if (args.length == 5) {
				creator.generator(args[4].val());
			}
			creator.createWorld();
			return new CVoid(t);
		}

		public String getName() {
			return "create_world";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 3, 4, 5};
		}

		public String docs() {
			return "void {name, [type, environment, [seed, [generator]]]} Creates a new world with the specified options."
					+ " If the world already exists, it will be loaded from disk, and the last 3 arguments may be"
					+ " ignored. name is the name of the world, type is one of " 
					+ StringUtils.Join(MCWorldType.values(), ", ") + " and environment is one of "
					+ StringUtils.Join(MCWorldEnvironment.values(), ", ") + ". The seed can be an integer, a string (will be the hashcode), or null (will be random int)."
					+ " Generator is the name of a world generator loaded on the server.";
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api(environments={CommandHelperEnvironment.class})
	public static class get_worlds extends AbstractFunction{

		public ExceptionType[] thrown() {
			return null;
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			CArray worlds = new CArray(t);
			for(MCWorld w : Static.getServer().getWorlds()){
				worlds.push(new CString(w.getName(), t));
			}
			return worlds;
		}

		public String getName() {
			return "get_worlds";
		}

		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		public String docs() {
			return "array {} Returns a list of all currently loaded worlds.";
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api(environments={CommandHelperEnvironment.class})
	public static class get_chunk_loc extends AbstractFunction {

		public String getName() {
			return "get_chunk_loc";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			MCCommandSender cs = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCPlayer p = null;
			MCWorld w = null;
			MCLocation l = null;

			if (cs instanceof MCPlayer) {
				p = (MCPlayer)cs;
				Static.AssertPlayerNonNull(p, t);
				l = p.getLocation();
				w = l.getWorld();
			}

			if (args.length == 1) {
				if (args[0] instanceof CArray) {
					l = ObjectGenerator.GetGenerator().location(args[0], w, t);
				} else {
					throw new ConfigRuntimeException("expecting argument 1 of get_chunk_loc to be a location array"
							, ExceptionType.FormatException, t);
				}
			} else {
				if (p == null) {
					throw new ConfigRuntimeException("expecting a player context for get_chunk_loc when used without arguments"
							, ExceptionType.InsufficientArgumentsException, t);
				}
			}

			return new CArray(t,
				new CInt(l.getChunk().getX(), t),
				new CInt(l.getChunk().getZ(), t),
				new CString(l.getChunk().getWorld().getName(), t));
		}

		public String docs() {
			return "array {[location array]} Returns an array of x, z, world "
					+ "coords of the chunk of either the location specified or the location of "
					+ "the player running the command.";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.FormatException, ExceptionType.InsufficientArgumentsException};
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		public Boolean runAsync() {
			return false;
		}
	}

	@api(environments={CommandHelperEnvironment.class})
	public static class spawn_falling_block extends AbstractFunction{

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.FormatException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			MCPlayer p = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
			MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], p != null ? p.getWorld() : null , t);
			MCItemStack item = Static.ParseItemNotation(this.getName(), args[1].val(), 1, t);

			CArray vect = null;

			if (args.length == 3) {
				if (args[2] instanceof CArray) {
					vect = (CArray)args[2];
					
					if (vect.size() < 3) {
						throw new ConfigRuntimeException("Argument 3 of spawn_falling_block must have 3 items", ExceptionType.FormatException, t);
					}
				} else {
					throw new ConfigRuntimeException("Expected array for argument 3 of spawn_falling_block", ExceptionType.FormatException, t);
				}
			}

			MCFallingBlock block = loc.getWorld().spawnFallingBlock(loc, item.getType().getType(), (byte)item.getData().getData());

			if (args.length == 3 && vect != null) {
				double x = Double.valueOf(vect.get(0).val());
				double y = Double.valueOf(vect.get(1).val());
				double z = Double.valueOf(vect.get(2).val());

				Velocity v = new Velocity(x, y, z);

				block.setVelocity(v);
			}

			return new CInt(block.getEntityId(), t);
		}

		public String getName() {
			return "spawn_falling_block";
		}

		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		public String docs() {
			return "integer {location array, id[:type], [vector array ie. array(x, y, z)]} Spawns a"
				+ " falling block of the specified id and type at the specified location, applying"
				+ " vector array as a velocity if given. Values for the vector array are doubles, and 1.0"
				+ " seems to imply about 3 times walking speed. Gravity applies for y.";
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class world_info extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.InvalidWorldException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCWorld w = Static.getServer().getWorld(args[0].val());
			if (w == null) {
				throw new ConfigRuntimeException("Unknown world: " + args[0],
						ExceptionType.InvalidWorldException, t);
			}
			CArray ret = new CArray(t);
			ret.set("name", new CString(w.getName(), t), t);
			ret.set("seed", new CInt(w.getSeed(), t), t);
			ret.set("environment", new CString(w.getEnvironment().name(), t), t);
			ret.set("generator", new CString(w.getGenerator(), t), t);
			ret.set("worldtype", new CString(w.getWorldType().name(), t), t);
			return ret;
		}

		public String getName() {
			return "world_info";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "array {world} Returns an associative array of all the info needed to duplicate the world."
					+ " The keys are name, seed, environment, generator, and worldtype.";
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class unload_world extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.InvalidWorldException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			boolean save = true;
			if (args.length == 2) {
				save = Static.getBoolean(args[1]);
			}
			MCWorld world = Static.getServer().getWorld(args[0].val());
			if (world == null) {
				throw new ConfigRuntimeException("Unknown world: " + args[0].val(), ExceptionType.InvalidWorldException, t);
			}
			return new CBoolean(Static.getServer().unloadWorld(world, save), t);
		}

		public String getName() {
			return "unload_world";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "boolean {world, [save]} Unloads a world, and saves it if save is true (defaults true),"
					+ " and returns whether or not the operation was successful.";
		}

		public Version since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class get_difficulty extends AbstractFunction {

		public String getName() {
			return "get_difficulty";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.InvalidWorldException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public String docs() {
			return "string {world} Returns the difficulty of the world, It will be one of " + StringUtils.Join(MCDifficulty.values(), ", ", ", or ", " or ") + ".";
		}

		public Version since() {
			return CHVersion.V3_3_1;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCWorld world = Static.getServer().getWorld(args[0].val());
			if (world == null) {
				throw new ConfigRuntimeException("Unknown world: " + args[0].val(), ExceptionType.InvalidWorldException, t);
			}
			return new CString(world.getDifficulty().toString(), t);
		}
	}

	@api
	public static class set_difficulty extends AbstractFunction {

		public String getName() {
			return "set_difficulty";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.InvalidWorldException, ExceptionType.FormatException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public String docs() {
			return "void {[world], difficulty} Sets the difficulty of the world with the given name, or all worlds if the name is not given."
					+ " difficulty can be " + StringUtils.Join(MCDifficulty.values(), ", ", ", or ", " or ") + ".";
		}

		public Version since() {
			return CHVersion.V3_3_1;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCDifficulty difficulty;
			if (args.length == 1) {
				try {
					difficulty = MCDifficulty.valueOf(args[0].val().toUpperCase());
				} catch (IllegalArgumentException exception) {
					throw new ConfigRuntimeException("The difficulty \"" + args[0].val() + "\" does not exist.", ExceptionType.FormatException, t);
				}
				for (MCWorld world : Static.getServer().getWorlds()) {
					world.setDifficulty(difficulty);
				}
			} else {
				MCWorld world = Static.getServer().getWorld(args[0].val());
				if (world == null) {
					throw new ConfigRuntimeException("Unknown world: " + args[0].val(), ExceptionType.InvalidWorldException, t);
				}
				try {
					difficulty = MCDifficulty.valueOf(args[1].val().toUpperCase());
				} catch (IllegalArgumentException exception) {
					throw new ConfigRuntimeException("The difficulty \"" + args[1].val() + "\" does not exist.", ExceptionType.FormatException, t);
				}
				world.setDifficulty(difficulty);
			}
			return new CVoid(t);
		}
	}

	@api
	public static class get_pvp extends AbstractFunction {

		public String getName() {
			return "get_pvp";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.InvalidWorldException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public String docs() {
			return "boolean {world} Returns if PVP is allowed in the world.";
		}

		public Version since() {
			return CHVersion.V3_3_1;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCWorld world = Static.getServer().getWorld(args[0].val());
			if (world == null) {
				throw new ConfigRuntimeException("Unknown world: " + args[0].val(), ExceptionType.InvalidWorldException, t);
			}
			return new CBoolean(world.getPVP(), t);
		}
	}

	@api
	public static class set_pvp extends AbstractFunction {

		public String getName() {
			return "set_pvp";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.InvalidWorldException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public String docs() {
			return "void {[world], boolean} Sets if PVP is allowed in the world with the given name, or all worlds if the name is not given.";
		}

		public Version since() {
			return CHVersion.V3_3_1;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			if (args.length == 1) {
				boolean pvp = Static.getBoolean(args[0]);
				for (MCWorld world : Static.getServer().getWorlds()) {
					world.setPVP(pvp);
				}
			} else {
				MCWorld world = Static.getServer().getWorld(args[0].val());
				if (world == null) {
					throw new ConfigRuntimeException("Unknown world: " + args[0].val(), ExceptionType.InvalidWorldException, t);
				}
				world.setPVP(Static.getBoolean(args[1]));
			}
			return new CVoid(t);
		}
	}

	@api
	public static class get_gamerule extends AbstractFunction {

		public String getName() {
			return "get_gamerule";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.InvalidWorldException, ExceptionType.FormatException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public String docs() {
			return "mixed {world, [gameRule]} Returns an associative array containing the values of all existing gamerules for the given world."
					+ " If gameRule is set, the function only returns the value of the specified gamerule, a boolean."
					+ "gameRule can be " + StringUtils.Join(MCGameRule.values(), ", ", ", or ", " or ") + ".";
		}

		public Version since() {
			return CHVersion.V3_3_1;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCWorld world = Static.getServer().getWorld(args[0].val());
			if (world == null) {
				throw new ConfigRuntimeException("Unknown world: " + args[0].val(), ExceptionType.InvalidWorldException, t);
			}
			if (args.length == 1) {
				CArray gameRules = new CArray(t);
				for (MCGameRule gameRule : MCGameRule.values()) {
					gameRules.set(new CString(gameRule.getGameRule(), t), new CBoolean(world.getGameRuleValue(gameRule), t), t);
				}
				return gameRules;
			} else {
				MCGameRule gameRule;
				try {
					gameRule = MCGameRule.valueOf(args[1].val().toUpperCase());
				} catch (IllegalArgumentException exception) {
					throw new ConfigRuntimeException("The gamerule \"" + args[1].val() + "\" does not exist.", ExceptionType.FormatException, t);
				}
				return new CBoolean(world.getGameRuleValue(gameRule), t);
			}
		}
	}

	@api
	public static class set_gamerule extends AbstractFunction {

		public String getName() {
			return "set_gamerule";
		}

		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.InvalidWorldException, ExceptionType.FormatException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public String docs() {
			return "void {[world], gameRule, value} Sets the value of the gamerule for the specified world, value is a boolean. If world is not given the value is set for all worlds."
					+ " gameRule can be " + StringUtils.Join(MCGameRule.values(), ", ", ", or ", " or ") + ".";
		}

		public Version since() {
			return CHVersion.V3_3_1;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCGameRule gameRule;
			if (args.length == 2) {
				try {
					gameRule = MCGameRule.valueOf(args[0].val().toUpperCase());
				} catch (IllegalArgumentException exception) {
					throw new ConfigRuntimeException("The gamerule \"" + args[0].val() + "\" does not exist.", ExceptionType.FormatException, t);
				}
				boolean value = Static.getBoolean(args[1]);
				for (MCWorld world : Static.getServer().getWorlds()) {
					world.setGameRuleValue(gameRule, value);
				}
			} else {
				try {
					gameRule = MCGameRule.valueOf(args[1].val().toUpperCase());
				} catch (IllegalArgumentException exception) {
					throw new ConfigRuntimeException("The gamerule \"" + args[1].val() + "\" does not exist.", ExceptionType.FormatException, t);
				}
				MCWorld world = Static.getServer().getWorld(args[0].val());
				if (world == null) {
					throw new ConfigRuntimeException("Unknown world: " + args[0].val(), ExceptionType.InvalidWorldException, t);
				}
				world.setGameRuleValue(gameRule, Static.getBoolean(args[2]));
			}
			return new CVoid(t);
		}
	}
}
