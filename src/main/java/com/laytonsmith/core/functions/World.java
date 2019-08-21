package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Vector3D;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.MCChunk;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.MCWorldBorder;
import com.laytonsmith.abstraction.MCWorldCreator;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.abstraction.blocks.MCBlockFace;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.entities.MCFallingBlock;
import com.laytonsmith.abstraction.enums.MCDifficulty;
import com.laytonsmith.abstraction.enums.MCGameRule;
import com.laytonsmith.abstraction.enums.MCWorldEnvironment;
import com.laytonsmith.abstraction.enums.MCWorldType;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.hide;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.MSVersion;
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
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CREIllegalArgumentException;
import com.laytonsmith.core.exceptions.CRE.CREInsufficientArgumentsException;
import com.laytonsmith.core.exceptions.CRE.CREInvalidWorldException;
import com.laytonsmith.core.exceptions.CRE.CRENotFoundException;
import com.laytonsmith.core.exceptions.CRE.CRERangeException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class World {

	public static String docs() {
		return "Provides functions for manipulating a world";
	}

	@api(environments = CommandHelperEnvironment.class)
	public static class get_spawn extends AbstractFunction {

		@Override
		public String getName() {
			return "get_spawn";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "array {[world]} Returns a location array for the specified world, or the current player's world"
					+ " if one is not specified.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInvalidWorldException.class, CREFormatException.class};
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
			String world;
			if(args.length == 1) {
				world = args[0].val();
			} else {
				if(environment.getEnv(CommandHelperEnvironment.class).GetPlayer() == null) {
					throw new CREInvalidWorldException("A world must be specified in a context with no player.", t);
				}
				world = environment.getEnv(CommandHelperEnvironment.class).GetPlayer().getWorld().getName();
			}
			MCWorld w = Static.getServer().getWorld(world);
			if(w == null) {
				throw new CREInvalidWorldException("The specified world \"" + world + "\" is not a valid world.", t);
			}
			return ObjectGenerator.GetGenerator().location(w.getSpawnLocation(), false);
		}
	}

	@api(environments = CommandHelperEnvironment.class)
	public static class set_spawn extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInvalidWorldException.class, CRECastException.class, CREFormatException.class};
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
			MCWorld w = null;
			if(p != null) {
				w = p.getWorld();
			}
			int x = 0;
			int y = 0;
			int z = 0;
			if(args.length == 1) {
				MCLocation l = ObjectGenerator.GetGenerator().location(args[0], w, t);
				w = l.getWorld();
				x = l.getBlockX();
				y = l.getBlockY();
				z = l.getBlockZ();
			} else if(args.length == 3) {
				x = Static.getInt32(args[0], t);
				y = Static.getInt32(args[1], t);
				z = Static.getInt32(args[2], t);
			} else if(args.length == 4) {
				w = Static.getServer().getWorld(args[0].val());
				x = Static.getInt32(args[1], t);
				y = Static.getInt32(args[2], t);
				z = Static.getInt32(args[3], t);
			}
			if(w == null) {
				throw new CREInvalidWorldException("Invalid world given.", t);
			}
			w.setSpawnLocation(x, y, z);
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "set_spawn";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 3, 4};
		}

		@Override
		public String docs() {
			return "void {locationArray | [world], x, y, z} Sets the spawn of the world. Note that in some cases"
					+ " a plugin may override the spawn, and this method will do nothing. In that case,"
					+ " you should use the plugin's commands to set the spawn.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = CommandHelperEnvironment.class)
	@hide("Deprecated.")
	public static class refresh_chunk extends AbstractFunction {

		@Override
		public String getName() {
			return "refresh_chunk";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3};
		}

		@Override
		public String docs() {
			return "void {[world], x, z | [world], locationArray} This is not guaranteed to work reliably! Resends the"
					+ " chunk data to all clients, using the specified world or current player's world.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREFormatException.class, CREInvalidWorldException.class};
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
			MCWorld world;
			int x;
			int z;
			if(args.length == 1) {
				//Location array provided
				MCLocation l = ObjectGenerator.GetGenerator().location(args[0], m != null ? m.getWorld() : null, t);
				world = l.getWorld();
				x = l.getBlockX();
				z = l.getBlockZ();
			} else if(args.length == 2) {
				//Either location array and world provided, or x and z. Test for array at pos 2
				if(args[1].isInstanceOf(CArray.TYPE)) {
					world = Static.getServer().getWorld(args[0].val());
					MCLocation l = ObjectGenerator.GetGenerator().location(args[1], null, t);
					x = l.getBlockX();
					z = l.getBlockZ();
				} else {
					if(m == null) {
						throw new CREInvalidWorldException("No world specified", t);
					}
					world = m.getWorld();
					x = Static.getInt32(args[0], t);
					z = Static.getInt32(args[1], t);
				}
			} else {
				//world, x and z provided
				world = Static.getServer().getWorld(args[0].val());
				if(world == null) {
					throw new CREInvalidWorldException("World " + args[0].val() + " does not exist.", t);
				}
				x = Static.getInt32(args[1], t);
				z = Static.getInt32(args[2], t);
			}
			world.refreshChunk(x, z);
			return CVoid.VOID;
		}
	}

	@api
	public static class load_chunk extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREFormatException.class, CREInvalidWorldException.class};
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
			MCWorld world;
			int x;
			int z;
			if(args.length == 1) {
				//Location array provided
				MCLocation l = ObjectGenerator.GetGenerator().location(args[0], m != null ? m.getWorld() : null, t);
				world = l.getWorld();
				x = l.getBlockX();
				z = l.getBlockZ();
			} else if(args.length == 2) {
				//Either location array and world provided, or x and z. Test for array at pos 2
				if(args[1].isInstanceOf(CArray.TYPE)) {
					world = Static.getServer().getWorld(args[0].val());
					if(world == null) {
						throw new CREInvalidWorldException("The given world (" + args[0].val() + ") does not exist.", t);
					}
					MCLocation l = ObjectGenerator.GetGenerator().location(args[1], null, t);
					x = l.getBlockX();
					z = l.getBlockZ();
				} else {
					if(m == null) {
						throw new CREInvalidWorldException("No world specified", t);
					}
					world = m.getWorld();
					x = Static.getInt32(args[0], t);
					z = Static.getInt32(args[1], t);
				}
			} else {
				//world, x and z provided
				world = Static.getServer().getWorld(args[0].val());
				if(world == null) {
					throw new CREInvalidWorldException("The given world (" + args[0].val() + ") does not exist.", t);
				}
				x = Static.getInt32(args[1], t);
				z = Static.getInt32(args[2], t);
			}
			world.loadChunk(x, z);
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "load_chunk";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3};
		}

		@Override
		public String docs() {
			return "void {[world], x, z | [world], locationArray} Loads a chunk for a world using the"
					+ " x and z coordinates. The current player's world is used if one isn't provided.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = CommandHelperEnvironment.class)
	public static class unload_chunk extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREFormatException.class, CREInvalidWorldException.class};
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
			MCWorld world;
			int x;
			int z;
			if(args.length == 1) {
				//Location array provided
				MCLocation l = ObjectGenerator.GetGenerator().location(args[0], m != null ? m.getWorld() : null, t);
				world = l.getWorld();
				x = l.getBlockX();
				z = l.getBlockZ();
			} else if(args.length == 2) {
				//Either location array and world provided, or x and z. Test for array at pos 2
				if(args[1].isInstanceOf(CArray.TYPE)) {
					world = Static.getServer().getWorld(args[0].val());
					MCLocation l = ObjectGenerator.GetGenerator().location(args[1], null, t);
					x = l.getBlockX();
					z = l.getBlockZ();
				} else {
					if(m == null) {
						throw new CREInvalidWorldException("No world specified", t);
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
			if(world == null) { // Happends when m is a fake console or null command sender.
				throw new CREInvalidWorldException("No world specified", t);
			}
			world.unloadChunk(x, z);
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "unload_chunk";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3};
		}

		@Override
		public String docs() {
			return "void {[world], x, z | [world], locationArray} Unloads a chunk for a world using the"
					+ " x and z coordinates. The current player's world is used if one is not provided.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
	public static class get_loaded_chunks extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREFormatException.class,
					CREInvalidWorldException.class, CRENotFoundException.class};
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
			MCWorld world;
			if(args.length == 1) {
				// World Provided
				world = Static.getServer().getWorld(args[0].val());
			} else {
				if(m == null) {
					throw new CREInvalidWorldException("No world specified", t);
				}
				world = m.getWorld();
			}
			if(world == null) { // Happens when m is a fake console or null command sender.
				throw new CREInvalidWorldException("No world specified", t);
			}
			MCChunk[] chunks = world.getLoadedChunks();
			if(chunks == null) { // Happens when m is a fake player.
				throw new CRENotFoundException(
						"Could not find the chunk objects of the world (are you running in cmdline mode?)", t);
			}
			CArray ret = new CArray(t);
			for(MCChunk c : chunks) {
				CArray chunk = CArray.GetAssociativeArray(t);
				chunk.set("x", new CInt(c.getX(), t), t);
				chunk.set("z", new CInt(c.getZ(), t), t);
				chunk.set("world", c.getWorld().getName(), t);
				ret.push(chunk, t);
			}
			return ret;
		}

		@Override
		public String getName() {
			return "get_loaded_chunks";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "array {[world]} Gets an array of all currently loaded chunks for a world."
					+ " The current player's world is used if one is not provided."
					+ " The chunk objects are associative arrays with the keys: x, z, and world.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

	}

	@api(environments = CommandHelperEnvironment.class)
	public static class regen_chunk extends AbstractFunction {

		@Override
		public String getName() {
			return "regen_chunk";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3};
		}

		@Override
		public String docs() {
			return "boolean {x, z, [world]| locationArray, [world]} Regenerate the chunk for a world."
					+ " The current player's world is used if one is not provided. Beware that this is destructive!"
					+ " Any data in this chunk will be lost! Returns true if the operation was successful."
					+ " This function is deprecated. Results will vary per platform and may not work at all.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREFormatException.class, CREInvalidWorldException.class};
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
			MCPlayer m = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			MCWorld world;
			int x;
			int z;

			if(args.length == 1) {
				//Location array provided
				MCLocation l = ObjectGenerator.GetGenerator().location(args[0], m != null ? m.getWorld() : null, t);

				world = l.getWorld();
				x = l.getBlockX() >> 4;
				z = l.getBlockZ() >> 4;
			} else if(args.length == 2) {
				//Either location array and world provided, or x and z. Test for array at pos 1
				if(args[0].isInstanceOf(CArray.TYPE)) {
					world = Static.getServer().getWorld(args[1].val());
					if(world == null) {
						throw new CREInvalidWorldException("World " + args[1].val() + " does not exist.", t);
					}
					MCLocation l = ObjectGenerator.GetGenerator().location(args[0], null, t);

					x = l.getBlockX() >> 4;
					z = l.getBlockZ() >> 4;
				} else {
					if(m == null) {
						throw new CREInvalidWorldException("No world specified", t);
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
				if(world == null) {
					throw new CREInvalidWorldException("World " + args[2].val() + " does not exist.", t);
				}
			}

			try {
				return CBoolean.get(world.regenerateChunk(x, z));
			} catch (UnsupportedOperationException ex) {
				return CBoolean.FALSE;
			}
		}
	}

	@api(environments = CommandHelperEnvironment.class)
	public static class is_slime_chunk extends AbstractFunction {

		@Override
		public String getName() {
			return "is_slime_chunk";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3};
		}

		@Override
		public String docs() {
			return "boolean {x, z, [world]| locationArray, [world]} Returns if the chunk is a slime spawning chunk."
					+ " The current player's world is used if one is not provided.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREFormatException.class, CREInvalidWorldException.class};
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

		Random rnd = new Random();

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer m = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			MCWorld world;
			int x;
			int z;
			if(args.length == 1) {
				//Location array provided
				MCLocation l = ObjectGenerator.GetGenerator().location(args[0], m != null ? m.getWorld() : null, t);
				world = l.getWorld();
				x = l.getBlockX() >> 4;
				z = l.getBlockZ() >> 4;
			} else if(args.length == 2) {
				//Either location array and world provided, or x and z. Test for array at pos 1
				if(args[0].isInstanceOf(CArray.TYPE)) {
					world = Static.getServer().getWorld(args[1].val());
					if(world == null) {
						throw new CREInvalidWorldException("The given world (" + args[1].val() + ") does not exist.", t);
					}
					MCLocation l = ObjectGenerator.GetGenerator().location(args[0], null, t);
					x = l.getBlockX() >> 4;
					z = l.getBlockZ() >> 4;
				} else {
					if(m == null) {
						throw new CREInvalidWorldException("No world specified", t);
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
				if(world == null) {
					throw new CREInvalidWorldException("The given world (" + args[2].val() + ") does not exist.", t);
				}
			}
			rnd.setSeed(world.getSeed()
					+ x * x * 0x4c1906
					+ x * 0x5ac0db
					+ z * z * 0x4307a7L
					+ z * 0x5f24f
					^ 0x3ad8025f);
			return CBoolean.get(rnd.nextInt(10) == 0);
		}
	}

	private static final SortedMap<String, CString> TIME_LOOKUP = new TreeMap<>();

	static {
		synchronized(World.class) {
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
	}

	@api(environments = CommandHelperEnvironment.class)
	public static class set_world_time extends AbstractFunction {

		@Override
		public String getName() {
			return "set_world_time";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			StringBuilder doc = new StringBuilder();
			synchronized(World.class) {
				doc.append("void {[world], time} Sets the time of a given world. Should be a number from 0 to"
						+ " 24000, if not, it is modulo scaled. ---- Alternatively, common time notation"
						+ " (9:30pm, 4:00 am) is acceptable, and convenient english mappings also exist:");
				doc.append("<ul>");
				for(String key : TIME_LOOKUP.keySet()) {
					doc.append("<li>").append(key).append(" = ").append(TIME_LOOKUP.get(key)).append("</li>");
				}
				doc.append("</ul>");
			}
			return doc.toString();
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInvalidWorldException.class, CREFormatException.class};
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
			MCWorld w = null;
			if(environment.getEnv(CommandHelperEnvironment.class).GetPlayer() != null) {
				w = environment.getEnv(CommandHelperEnvironment.class).GetPlayer().getWorld();
			}
			if(args.length == 2) {
				w = Static.getServer().getWorld(args[0].val());
			}
			if(w == null) {
				throw new CREInvalidWorldException("No world specified", t);
			}
			String stime = (args.length == 1 ? args[0] : args[1]).val().toLowerCase();
			if(TIME_LOOKUP.containsKey(stime.replaceAll("[^a-z]", ""))) {
				stime = TIME_LOOKUP.get(stime.replaceAll("[^a-z]", "")).val();
			}
			if(stime.matches("^([\\d]+)[:.]([\\d]+)[ ]*?(?:([pa])\\.*m\\.*){0,1}$")) {
				Pattern p = Pattern.compile("^([\\d]+)[:.]([\\d]+)[ ]*?(?:([pa])\\.*m\\.*){0,1}$");
				Matcher m = p.matcher(stime);
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
				w.setTime(Long.parseLong(stime));
			} catch (NumberFormatException e) {
				throw new CREFormatException("Invalid time provided", t);
			}
			return CVoid.VOID;
		}
	}

	@api(environments = CommandHelperEnvironment.class)
	public static class get_world_time extends AbstractFunction {

		@Override
		public String getName() {
			return "get_world_time";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "int {[world]} Returns the time of the specified world, as an integer from 0 to 24000-1";
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
			return MSVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCWorld w = null;
			if(environment.getEnv(CommandHelperEnvironment.class).GetPlayer() != null) {
				w = environment.getEnv(CommandHelperEnvironment.class).GetPlayer().getWorld();
			}
			if(args.length == 1) {
				w = Static.getServer().getWorld(args[0].val());
			}
			if(w == null) {
				throw new CREInvalidWorldException("No world specified", t);
			}
			return new CInt(w.getTime(), t);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class create_world extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class, CRECastException.class};
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
			MCWorldCreator creator = StaticLayer.GetConvertor().getWorldCreator(args[0].val());
			if(args.length >= 3) {
				MCWorldType type;
				try {
					type = MCWorldType.valueOf(args[1].val().toUpperCase());
				} catch (IllegalArgumentException e) {
					throw new CREFormatException(args[1].val() + " is not a valid world type. Must be one of: "
							+ StringUtils.Join(MCWorldType.values(), ", "), t);
				}
				MCWorldEnvironment environment;
				try {
					environment = MCWorldEnvironment.valueOf(args[2].val().toUpperCase());
				} catch (IllegalArgumentException e) {
					throw new CREFormatException(args[2].val() + " is not a valid environment type. Must be one of: "
							+ StringUtils.Join(MCWorldEnvironment.values(), ", "), t);
				}
				creator.type(type).environment(environment);
			}
			if((args.length >= 4) && !(args[3] instanceof CNull)) {
				if(args[3].isInstanceOf(CInt.TYPE)) {
					creator.seed(Static.getInt(args[3], t));
				} else {
					creator.seed(args[3].val().hashCode());
				}
			}
			if(args.length == 5) {
				creator.generator(args[4].val());
			}
			creator.createWorld();
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "create_world";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 3, 4, 5};
		}

		@Override
		public String docs() {
			return "void {name, [type, environment, [seed, [generator]]]} Creates a world with the specified options."
					+ " If a world by that name already exists, it will instead be loaded from disk, and the last three"
					+ " arguments may be ignored. The name is the name of the world, type is one of "
					+ StringUtils.Join(MCWorldType.values(), ", ") + " and environment is one of "
					+ StringUtils.Join(MCWorldEnvironment.values(), ", ") + ". The seed can be an integer,"
					+ " a string (will be the hashcode), or null (will be random int)."
					+ " Generator is the name of a world generator loaded on the server.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_worlds extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return null;
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
			CArray worlds = new CArray(t);
			for(MCWorld w : Static.getServer().getWorlds()) {
				worlds.push(new CString(w.getName(), t), t);
			}
			return worlds;
		}

		@Override
		public String getName() {
			return "get_worlds";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "array {} Returns a list of all currently loaded worlds.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_chunk_loc extends AbstractFunction {

		@Override
		public String getName() {
			return "get_chunk_loc";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			MCCommandSender cs = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCPlayer p = null;
			MCWorld w = null;
			MCLocation l = null;

			if(cs instanceof MCPlayer) {
				p = (MCPlayer) cs;
				Static.AssertPlayerNonNull(p, t);
				l = p.getLocation();
				if(l == null) {
					throw new CRENotFoundException(
							"Could not find the location of the given player (are you running in cmdline mode?)", t);
				}
				w = l.getWorld();
			}

			if(args.length == 1) {
				if(args[0].isInstanceOf(CArray.TYPE)) {
					l = ObjectGenerator.GetGenerator().location(args[0], w, t);
				} else {
					throw new CREFormatException("Expecting argument 1 of get_chunk_loc to be a location array", t);
				}
			} else {
				if(p == null) {
					throw new CREInsufficientArgumentsException(
							"Expecting a player context for get_chunk_loc when used without arguments", t);
				}
			}

			CArray chunk = CArray.GetAssociativeArray(t);
			chunk.set(0, new CInt(l.getBlockX() >> 4, t), t);
			chunk.set(1, new CInt(l.getBlockZ() >> 4, t), t);
			chunk.set(2, new CString(l.getWorld().getName(), t), t);
			chunk.set("x", new CInt(l.getBlockX() >> 4, t), t);
			chunk.set("z", new CInt(l.getBlockZ() >> 4, t), t);
			chunk.set("world", l.getWorld().getName(), t);
			return chunk;
		}

		@Override
		public String docs() {
			return "array {[locationArray]} Returns an array of (x, z, world) coordinates of the chunk of either the"
					+ " location specified or the location of the player running the command.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class, CREInsufficientArgumentsException.class,
					CRENotFoundException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
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
	public static class spawn_falling_block extends AbstractFunction implements Optimizable {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class};
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
			MCPlayer p = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
			MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], p != null ? p.getWorld() : null, t);

			MCMaterial mat = StaticLayer.GetMaterial(args[1].val());
			if(mat == null) {
				mat = Static.ParseItemNotation(getName(), args[1].val(), 1, t).getType();
			}
			if(!mat.isBlock()) {
				throw new CREIllegalArgumentException("The value \"" + args[1].val()
						+ "\" is not a valid block material.", t);
			}
			MCFallingBlock block = loc.getWorld().spawnFallingBlock(loc, mat.createBlockData());

			if(args.length == 3) {
				block.setVelocity(ObjectGenerator.GetGenerator().vector(args[2], t));
			}

			return new CString(block.getUniqueId().toString(), t);
		}

		@Override
		public String getName() {
			return "spawn_falling_block";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public String docs() {
			return "string {locationArray, blockName, [vectorArray]} Spawns a falling block entity of the specified"
					+ " block type at the specified location, applying the vector array as a velocity if given."
					+ " Values for the vector array are doubles, and 1.0 seems to imply about 3 times walking speed."
					+ " Gravity applies for y.";
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
			if(children.size() > 1 && children.get(1).getData().val().contains(":")) {
				MSLog.GetLogger().w(MSLog.Tags.DEPRECATION, "The 1:1 format is deprecated in spawn_falling_block()", t);
			}
			return null;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(Optimizable.OptimizationOption.OPTIMIZE_DYNAMIC);
		}
	}

	@api
	public static class world_info extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInvalidWorldException.class};
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
			MCWorld w = Static.getServer().getWorld(args[0].val());
			if(w == null) {
				throw new CREInvalidWorldException("Unknown world: " + args[0], t);
			}
			CArray ret = CArray.GetAssociativeArray(t);
			ret.set("name", new CString(w.getName(), t), t);
			ret.set("seed", new CInt(w.getSeed(), t), t);
			ret.set("environment", new CString(w.getEnvironment().name(), t), t);
			ret.set("generator", new CString(w.getGenerator(), t), t);
			ret.set("worldtype", new CString(w.getWorldType().name(), t), t);
			ret.set("sealevel", new CInt(w.getSeaLevel(), t), t);
			ret.set("maxheight", new CInt(w.getMaxHeight(), t), t);
			return ret;
		}

		@Override
		public String getName() {
			return "world_info";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "array {world} Returns an associative array of all the info needed to duplicate the world."
					+ " The keys are name, seed, environment, generator, worldtype, sealevel and maxheight.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
	public static class unload_world extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInvalidWorldException.class};
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
			boolean save = true;
			if(args.length == 2) {
				save = ArgumentValidation.getBoolean(args[1], t);
			}
			MCWorld world = Static.getServer().getWorld(args[0].val());
			if(world == null) {
				throw new CREInvalidWorldException("Unknown world: " + args[0].val(), t);
			}
			return CBoolean.get(Static.getServer().unloadWorld(world, save));
		}

		@Override
		public String getName() {
			return "unload_world";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "boolean {world, [save]} Unloads a world, and saves it if save is true (defaults true),"
					+ " and returns whether or not the operation was successful.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
	public static class get_difficulty extends AbstractFunction {

		@Override
		public String getName() {
			return "get_difficulty";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
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
		public Boolean runAsync() {
			return false;
		}

		@Override
		public String docs() {
			return "string {world} Returns the difficulty of the world, It will be one of "
					+ StringUtils.Join(MCDifficulty.values(), ", ", ", or ", " or ") + ".";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCWorld world = Static.getServer().getWorld(args[0].val());
			if(world == null) {
				throw new CREInvalidWorldException("Unknown world: " + args[0].val(), t);
			}
			return new CString(world.getDifficulty().toString(), t);
		}
	}

	@api
	public static class set_difficulty extends AbstractFunction {

		@Override
		public String getName() {
			return "set_difficulty";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInvalidWorldException.class, CREFormatException.class};
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
		public String docs() {
			return "void {[world], difficulty} Sets the difficulty of the world with the given name, or all worlds"
					+ " if the name is not given. The difficulty can be "
					+ StringUtils.Join(MCDifficulty.values(), ", ", ", or ", " or ") + ".";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCDifficulty difficulty;
			if(args.length == 1) {
				try {
					difficulty = MCDifficulty.valueOf(args[0].val().toUpperCase());
				} catch (IllegalArgumentException exception) {
					throw new CREFormatException("The difficulty \"" + args[0].val() + "\" does not exist.", t);
				}
				for(MCWorld world : Static.getServer().getWorlds()) {
					world.setDifficulty(difficulty);
				}
			} else {
				MCWorld world = Static.getServer().getWorld(args[0].val());
				if(world == null) {
					throw new CREInvalidWorldException("Unknown world: " + args[0].val(), t);
				}
				try {
					difficulty = MCDifficulty.valueOf(args[1].val().toUpperCase());
				} catch (IllegalArgumentException exception) {
					throw new CREFormatException("The difficulty \"" + args[1].val() + "\" does not exist.", t);
				}
				world.setDifficulty(difficulty);
			}
			return CVoid.VOID;
		}
	}

	@api
	public static class get_pvp extends AbstractFunction {

		@Override
		public String getName() {
			return "get_pvp";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
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
		public Boolean runAsync() {
			return false;
		}

		@Override
		public String docs() {
			return "boolean {world} Returns if PVP is allowed in the world.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCWorld world = Static.getServer().getWorld(args[0].val());
			if(world == null) {
				throw new CREInvalidWorldException("Unknown world: " + args[0].val(), t);
			}
			return CBoolean.get(world.getPVP());
		}
	}

	@api
	public static class set_pvp extends AbstractFunction {

		@Override
		public String getName() {
			return "set_pvp";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
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
		public Boolean runAsync() {
			return false;
		}

		@Override
		public String docs() {
			return "void {[world], boolean} Sets if PVP is allowed in the world with the given name,"
					+ " or all worlds if the name is not given.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if(args.length == 1) {
				boolean pvp = ArgumentValidation.getBoolean(args[0], t);
				for(MCWorld world : Static.getServer().getWorlds()) {
					world.setPVP(pvp);
				}
			} else {
				MCWorld world = Static.getServer().getWorld(args[0].val());
				if(world == null) {
					throw new CREInvalidWorldException("Unknown world: " + args[0].val(), t);
				}
				world.setPVP(ArgumentValidation.getBoolean(args[1], t));
			}
			return CVoid.VOID;
		}
	}

	@api
	public static class get_gamerule extends AbstractFunction {

		@Override
		public String getName() {
			return "get_gamerule";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInvalidWorldException.class, CREFormatException.class};
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
		public String docs() {
			return "mixed {world, [gameRule]} Returns an associative array containing the values of all existing"
					+ " gamerules for the given world. If the gameRule parameter is specified, the function only"
					+ " returns that one value instead of an array."
					+ " The gameRule can be " + StringUtils.Join(MCGameRule.values(), ", ", ", or ", " or ") + ".";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCWorld world = Static.getServer().getWorld(args[0].val());
			if(world == null) {
				throw new CREInvalidWorldException("Unknown world: " + args[0].val(), t);
			}
			if(args.length == 1) {
				CArray gameRules = CArray.GetAssociativeArray(t);
				for(String gameRule : world.getGameRules()) {
					gameRules.set(new CString(gameRule, t),
							Static.resolveConstruct(world.getGameRuleValue(gameRule), t), t);
				}
				return gameRules;
			} else {
				try {
					MCGameRule gameRule = MCGameRule.valueOf(args[1].val().toUpperCase());
					String value = world.getGameRuleValue(gameRule.getGameRule());
					if(value.isEmpty()) {
						throw new CREFormatException("The gamerule \"" + args[1].val()
								+ "\" does not exist in this version.", t);
					}
					return Static.resolveConstruct(value, t);
				} catch (IllegalArgumentException exception) {
					throw new CREFormatException("The gamerule \"" + args[1].val() + "\" does not exist.", t);
				}
			}
		}
	}

	@api
	public static class set_gamerule extends AbstractFunction {

		@Override
		public String getName() {
			return "set_gamerule";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInvalidWorldException.class, CREFormatException.class, CRECastException.class};
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
		public String docs() {
			return "boolean {[world], gameRule, value} Sets the value of the gamerule for the specified world. If world is"
					+ " not given the value is set for all worlds. Returns true if successful. gameRule can be "
					+ StringUtils.Join(MCGameRule.values(), ", ", ", or ", " or ") + ".";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCGameRule gameRule;
			boolean success = false;
			if(args.length == 2) {
				gameRule = ArgumentValidation.getEnum(args[0], MCGameRule.class, t);
				String value = Static.getObject(args[1], t, gameRule.getRuleType()).val();
				for(MCWorld world : Static.getServer().getWorlds()) {
					success = world.setGameRuleValue(gameRule, value);
				}
			} else {
				gameRule = ArgumentValidation.getEnum(args[1], MCGameRule.class, t);
				MCWorld world = Static.getServer().getWorld(args[0].val());
				if(world == null) {
					throw new CREInvalidWorldException("Unknown world: " + args[0].val(), t);
				}
				success = world.setGameRuleValue(gameRule, Static.getObject(args[2], t, gameRule.getRuleType()).val());
			}
			return CBoolean.get(success);
		}
	}

	@api
	public static class set_keep_spawn_loaded extends AbstractFunction {

		@Override
		public String getName() {
			return "set_keep_spawn_loaded";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "void {world, boolean} Sets whether or not the spawn chunks in the given world should stay loaded.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_4;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInvalidWorldException.class};
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCWorld world = Static.getServer().getWorld(args[0].val());
			if(world == null) {
				throw new CREInvalidWorldException("Unknown world: " + args[0].val(), t);
			}
			world.setKeepSpawnInMemory(ArgumentValidation.getBooleanObject(args[1], t));
			return CVoid.VOID;
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

	@api
	public static class location_shift extends AbstractFunction {

		@Override
		public String getName() {
			return "location_shift";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInvalidWorldException.class, CREFormatException.class};
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
		public String docs() {
			return "array {origin, target, [distance] | origin, direction, [distance]} Returns a location array that"
					+ " is the specified distance from the origin location along a vector. ---- If a target location is"
					+ " specified, the vector is gotten from that. (the target's world is ignored) If a direction is"
					+ " specified, that vector is use instead. Distance defaults to 1.0. Direction can be one of "
					+ StringUtils.Join(MCBlockFace.values(), ", ", ", or ", " or ") + ".";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
			MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], p != null ? p.getWorld() : null, t);
			double distance = 1;
			if(args.length == 3) {
				distance = Static.getNumber(args[2], t);
			}
			Vector3D vector;
			if(args[1].isInstanceOf(CArray.TYPE)) {
				MCLocation to = ObjectGenerator.GetGenerator().location(args[1], loc.getWorld(), t);
				vector = to.toVector().subtract(loc.toVector()).normalize();
			} else {
				try {
					MCBlockFace facing = MCBlockFace.valueOf(args[1].val().toUpperCase());
					vector = new Vector3D(facing.getModX(), facing.getModY(), facing.getModZ()).normalize();
				} catch (IllegalArgumentException iae) {
					throw new CREFormatException("Expected a location array or direction.", t);
				}
			}
			loc.add(vector.multiply(distance));
			return ObjectGenerator.GetGenerator().location(loc);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Using a target location to teleport the player towards it.",
				"set_ploc(location_shift(ploc(), pcursor()));",
				"{0: 0.0, 1: 64.0, 2: 1.0, 3: world, 4: 0.0, 5: 90.0, x: 0.0, y: 64.0, z: 1.0, world: world, yaw: 0.0, pitch: 90.0}"),
				new ExampleScript("Using a direction to get the block 2 meters above the player's targeted block.",
				"location_shift(pcursor(), 'UP', 2)",
				"{0: 0.0, 1: 66.0, 2: 0.0, 3: world, 4: 0.0, 5: 0.0, x: 0.0, y: 66.0, z: 0.0, world: world, yaw: 0.0, pitch: 0.0}")};
		}
	}

	@api
	public static class get_yaw extends AbstractFunction {

		@Override
		public String getName() {
			return "get_yaw";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInvalidWorldException.class, CREFormatException.class};
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
		public String docs() {
			return "double {location_from, location_to} Calculate yaw from one location to another on the X-Z plane."
					+ " The rotation is measured in degrees (0-359.99...) relative to the (x=0,z=1) vector, which"
					+ " points south. Throws a FormatException if locations have differing worlds.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {

			MCPlayer p = env.getEnv(CommandHelperEnvironment.class).GetPlayer();

			MCLocation from = ObjectGenerator.GetGenerator().location(args[0], p != null ? p.getWorld() : null, t);
			MCLocation to = ObjectGenerator.GetGenerator().location(args[1], p != null ? p.getWorld() : null, t);

			MCLocation subtract;
			try {
				subtract = to.subtract(from);
			} catch (IllegalArgumentException ex) {
				throw new CREFormatException("Locations are in differing worlds.", t);
			}
			double dX = subtract.getX();
			double dZ = subtract.getZ();

			double yaw = java.lang.Math.atan(dX / dZ) * 180 / java.lang.Math.PI; // In degrees [-90:90].
			if(dZ < 0) { // Bottom circle quadrant.
				yaw += 180;
			} else if(dX < 0) { // Top left half quadrant.
				yaw += 360;
			}

			return new CDouble(360 - yaw, t);
		}
	}

	@api
	public static class get_pitch extends AbstractFunction {

		@Override
		public String getName() {
			return "get_pitch";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInvalidWorldException.class, CREFormatException.class};
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
		public String docs() {
			return "double {location_from, location_to} Calculate pitch from one location to another."
					+ " This will be from -90.0 to 90.0, which is up and down respectively.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {

			MCPlayer p = env.getEnv(CommandHelperEnvironment.class).GetPlayer();

			MCLocation from = ObjectGenerator.GetGenerator().location(args[0], p != null ? p.getWorld() : null, t);
			MCLocation to = ObjectGenerator.GetGenerator().location(args[1], p != null ? p.getWorld() : null, t);

			MCLocation subtract = to.subtract(from);
			double dX = subtract.getX();
			double dY = subtract.getY();
			double dZ = subtract.getZ();

			double distanceXZ = java.lang.Math.sqrt(dX * dX + dZ * dZ);

			double pitch = java.lang.Math.atan(dY / distanceXZ) * -180 / java.lang.Math.PI;

			return new CDouble(pitch, t);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_vector extends AbstractFunction {

		@Override
		public String getName() {
			return "get_vector";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRERangeException.class, CREFormatException.class};
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
		public String docs() {
			return "array {locationArray, [magnitude]} Returns a vector from the yaw and pitch in a location array."
					+ " All other values in the location array are ignored."
					+ " The second parameter, that defines the magnitude of the vector, defaults to 1.0.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_2;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
			Vector3D v = ObjectGenerator.GetGenerator().location(args[0], p == null ? null : p.getWorld(), t).getDirection();
			if(args.length == 2) {
				v = v.multiply(Static.getDouble(args[1], t));
			}
			return ObjectGenerator.GetGenerator().vector(v);
		}
	}

	@api
	public static class distance extends AbstractFunction {

		@Override
		public String getName() {
			return "distance";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRERangeException.class, CREFormatException.class, CREInvalidWorldException.class,
					CREIllegalArgumentException.class};
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
		public String docs() {
			return "double {locationA, locationB} Returns the distance between two locations.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_2;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCLocation loc1 = ObjectGenerator.GetGenerator().location(args[0], null, t);
			MCLocation loc2 = ObjectGenerator.GetGenerator().location(args[1], null, t);
			try {
				return new CDouble(loc1.distance(loc2), t);
			} catch (IllegalArgumentException iae) {
				throw new CREIllegalArgumentException("Cannot measure distance between two different worlds.", t);
			}
		}
	}

	@api
	public static class save_world extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInvalidWorldException.class};
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
			MCWorld world = Static.getWorld(args[0], t);
			world.save();
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "save_world";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "void {world_name} Saves the specified world.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

	}

	@api
	public static class get_temperature extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class};
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
		public Mixed exec(Target target, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer player = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			MCWorld world = null;
			if(player != null) {
				world = player.getWorld();
			}
			MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], world, target);
			return new CDouble(loc.getBlock().getTemperature(), target);
		}

		@Override
		public String getName() {
			return "get_temperature";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "double {locationArray} Returns the current temperature at the location given.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
	public static class get_world_border extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInvalidWorldException.class};
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
			MCWorld w = Static.getServer().getWorld(args[0].val());
			if(w == null) {
				throw new CREInvalidWorldException("Unknown world: " + args[0], t);
			}
			MCWorldBorder wb = w.getWorldBorder();
			CArray ret = CArray.GetAssociativeArray(t);
			ret.set("width", new CDouble(wb.getSize(), t), t);
			ret.set("center", ObjectGenerator.GetGenerator().location(wb.getCenter(), false), t);
			ret.set("damagebuffer", new CDouble(wb.getDamageBuffer(), t), t);
			ret.set("damageamount", new CDouble(wb.getDamageAmount(), t), t);
			ret.set("warningtime", new CInt(wb.getWarningTime(), t), t);
			ret.set("warningdistance", new CInt(wb.getWarningDistance(), t), t);
			return ret;
		}

		@Override
		public String getName() {
			return "get_world_border";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "array {world_name} Returns an associative array of all information about the world's border."
					+ " The keys are width, center, damagebuffer, damageamount, warningtime, warningdistance.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_2;
		}
	}

	@api
	public static class set_world_border extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREFormatException.class, CREInvalidWorldException.class,
					CRERangeException.class};
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
			MCWorld w = Static.getServer().getWorld(args[0].val());
			if(w == null) {
				throw new CREInvalidWorldException("Unknown world: " + args[0], t);
			}
			MCWorldBorder wb = w.getWorldBorder();
			Mixed c = args[1];
			if(!(c.isInstanceOf(CArray.TYPE))) {
				throw new CREFormatException("Expected array but given \"" + args[1].val() + "\"", t);
			}
			CArray params = (CArray) c;
			if(params.containsKey("width")) {
				if(params.containsKey("seconds")) {
					wb.setSize(ArgumentValidation.getDouble(params.get("width", t), t),
							ArgumentValidation.getInt32(params.get("seconds", t), t));
				} else {
					wb.setSize(ArgumentValidation.getDouble(params.get("width", t), t));
				}
			}
			if(params.containsKey("center")) {
				wb.setCenter(ObjectGenerator.GetGenerator().location(params.get("center", t), w, t));
			}
			if(params.containsKey("damagebuffer")) {
				wb.setDamageBuffer(ArgumentValidation.getDouble(params.get("damagebuffer", t), t));
			}
			if(params.containsKey("damageamount")) {
				wb.setDamageAmount(ArgumentValidation.getDouble(params.get("damageamount", t), t));
			}
			if(params.containsKey("warningtime")) {
				wb.setWarningTime(ArgumentValidation.getInt32(params.get("warningtime", t), t));
			}
			if(params.containsKey("warningdistance")) {
				wb.setWarningDistance(ArgumentValidation.getInt32(params.get("warningdistance", t), t));
			}
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "set_world_border";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "void {world_name, paramArray} Updates the world's border with the given values. In addition to the"
					+ " keys returned by get_world_border(), you can also specify the \"seconds\". This is time in"
					+ " which the border will move from the previous width to the new \"width\".";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_2;
		}
	}
}
