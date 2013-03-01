package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.StringUtils;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCEntity.Velocity;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.MCWorldCreator;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.abstraction.blocks.MCFallingBlock;
import com.laytonsmith.abstraction.enums.MCWorldEnvironment;
import com.laytonsmith.abstraction.enums.MCWorldType;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.*;
import com.laytonsmith.core.arguments.Argument;
import com.laytonsmith.core.arguments.ArgumentBuilder;
import com.laytonsmith.core.arguments.Generic;
import com.laytonsmith.core.arguments.Signature;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.laytonsmith.core.natives.MItemStack;
import com.laytonsmith.core.natives.MLocation;
import com.laytonsmith.core.natives.MVector3D;
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
			return "Returns a location array for the specified world, or the current player's world, if not specified.";
		}
		
		public Argument returnType() {
			return new Argument("", MLocation.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CString.class, "world").setOptionalDefaultNull()
					);
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
			return ObjectGenerator.GetGenerator().location(w.getSpawnLocation());
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
				x = args[0].primitive(t).castToInt32(t);
				y = args[1].primitive(t).castToInt32(t);
				z = args[2].primitive(t).castToInt32(t);
			} else if (args.length == 4) {
				w = Static.getServer().getWorld(args[0].val());
				x = args[1].primitive(t).castToInt32(t);
				y = args[2].primitive(t).castToInt32(t);
				z = args[3].primitive(t).castToInt32(t);
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
			return "Sets the spawn of the world. Note that in some cases, a plugin"
					+ " may set the spawn differently, and this method will do nothing. In that case, you should use"
					+ " the plugin's commands to set the spawn.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Signature(1, 
							new Argument("", MLocation.class, "location")
						), new Signature(2, 
							new Argument("", CString.class, "world").setOptionalDefaultNull(),
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

	@api(environments=CommandHelperEnvironment.class)
	public static class refresh_chunk extends AbstractFunction {

		public String getName() {
			return "refresh_chunk";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3};
		}

		public String docs() {
			return "Resends the chunk data to all clients, using the specified world, or the current"
					+ " players world if not provided.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Signature(1,
							new Argument("", CString.class, "world").setOptionalDefaultNull(),
							new Argument("", CInt.class, "x"),
							new Argument("", CInt.class, "z")
						), new Signature(2, 
							new Argument("", MLocation.class, "location")
						)
					);
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
					x = args[0].primitive(t).castToInt32(t);
					z = args[1].primitive(t).castToInt32(t);
				}
			} else {
				//world, x and z provided
				world = Static.getServer().getWorld(args[0].val());
				x = args[1].primitive(t).castToInt32(t);
				z = args[2].primitive(t).castToInt32(t);
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
			return "void {x, z, [world]| locationArray} Regenerate the chunk, using the specified world, or the current"
					+ " players world if not provided. Beware that this is destructive! Any data in this chunk will be lost!";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Signature(1, 
							new Argument("", CInt.class, "x"),
							new Argument("", CInt.class, "z"),
							new Argument("", CString.class, "world").setOptionalDefaultNull()
						), new Signature(2,
							new Argument("", MLocation.class, "location")
						)
					);
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
					x = args[0].primitive(t).castToInt32(t);
					z = args[1].primitive(t).castToInt32(t);
				}
			} else {
				//world, x and z provided
				world = Static.getServer().getWorld(args[0].val());
				x = args[1].primitive(t).castToInt32(t);
				z = args[2].primitive(t).castToInt32(t);
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
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CString.class, "world").setOptionalDefaultNull(),
						new Argument("", CInt.class, CString.class, "time")
					);
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
			return "Returns the time of the specified world, as an integer from"
					+ " 0 to 24000-1";
		}
		
		public Argument returnType() {
			return new Argument("", CInt.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CString.class, "world").setOptionalDefaultNull()
					);
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
			String name = args[0].val();
			if(args.length == 4){
				MCWorldType type;
				MCWorldEnvironment environment;
				long seed = args[3].primitive(t).castToInt32(t);
				try{
					type = MCWorldType.valueOf(args[1].val().toUpperCase());
				} catch(IllegalArgumentException e){
					throw new ConfigRuntimeException(args[1].val() + " is not a valid world type. Must be one of: "
							+ StringUtils.Join(MCWorldType.values(), ", "), ExceptionType.FormatException, t);
				}
				try{
					environment = MCWorldEnvironment.valueOf(args[2].val().toUpperCase());
				} catch(IllegalArgumentException e){
					throw new ConfigRuntimeException(args[2].val() + " is not a valid environment type. Must be one of: "
							+ StringUtils.Join(MCWorldEnvironment.values(), ", "), ExceptionType.FormatException, t);
				}
				MCWorldCreator creator = StaticLayer.GetConvertor().getWorldCreator(name);
				creator.type(type).environment(environment).seed(seed);
				creator.createWorld();
			} else {
				StaticLayer.GetConvertor().getWorldCreator(name).createWorld();
			}
			return new CVoid(t);
		}

		public String getName() {
			return "create_world";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 4};
		}

		public String docs() {
			return "void {name, [type, environment, seed]} Creates a new world with the specified options."
					+ " If the world already exists, it will be loaded from disk, and the last 3 arguments may be"
					+ " ignored. name is the name of the world, type is one of " 
					+ StringUtils.Join(MCWorldType.values(), ", ") + ", environment is one of "
					+ StringUtils.Join(MCWorldEnvironment.values(), ", ") + ", and seed is an integer.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CString.class, "name"),
						new Argument("", MCWorldType.class, "type").setOptional(),
						new Argument("", MCWorldEnvironment.class, "environment").setOptional(),
						new Argument("", CInt.class, "").setOptional()
					);
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
			return "Returns a list of all currently loaded worlds.";
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
			
			if (args.length == 1 && !args[0].isNull()) {
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
			
			MLocation loc = new MLocation();
			loc.world = l.getWorld().getName();
			loc.x = (double)l.getChunk().getX();
			loc.y = 0d;
			loc.z = (double)l.getChunk().getZ();
			return loc.deconstruct(t);
		}

		public String docs() {
			return "Returns an array of x, z, world "
					+ "coords of the chunk of either the location specified or the location of "
					+ "the player running the command.";
		}
		
		public Argument returnType() {
			return new Argument("", MLocation.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", MLocation.class, "location").setOptionalDefaultNull()
					);
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
			return "Spawns a falling block of the specified id and type at the specified location, applying"
				+ " vector array as a velocity if given. Values for the vector array are doubles, and 1.0"
				+ " is approximately 3 times walking speed. Gravity applies for y.";
		}
		
		public Argument returnType() {
			return new Argument("", CInt.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", MLocation.class, "location"),
						new Argument("", MItemStack.class, CInt.class, CString.class, "item"),
						new Argument("", MVector3D.class, "vector")
					);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}
}
