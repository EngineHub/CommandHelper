package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCNote;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCSign;
import com.laytonsmith.abstraction.enums.MCBiomeType;
import com.laytonsmith.abstraction.enums.MCInstrument;
import com.laytonsmith.abstraction.enums.MCSound;
import com.laytonsmith.abstraction.enums.MCTone;
import com.laytonsmith.abstraction.enums.MCTreeType;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.noboilerplate;
import com.laytonsmith.core.*;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.sk89q.util.StringUtil;

/**
 *
 * @author Layton
 */
public class Environment {

	public static String docs() {
		return "Allows you to manipulate the environment around the player";
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_block_at extends AbstractFunction {

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
			return new ExceptionType[]{ExceptionType.FormatException, ExceptionType.CastException, ExceptionType.LengthException, ExceptionType.InvalidWorldException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_0_2;
		}

		public Construct exec(Target t, com.laytonsmith.core.environments.Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			double x = 0;
			double y = 0;
			double z = 0;
			MCWorld w = null;
			MCCommandSender sender = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
			String world = null;
			if (sender instanceof MCPlayer) {
				w = ((MCPlayer) sender).getWorld();
			}
			if (args.length == 1 || args.length == 2) {
				if (args[0] instanceof CArray) {
					MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], w, t);
					x = loc.getX();
					y = loc.getY();
					z = loc.getZ();
					world = loc.getWorld().getName();
				} else {
					throw new ConfigRuntimeException("get_block_at expects param 1 to be an array", ExceptionType.CastException, t);
				}
				if (args.length == 2) {
					world = args[1].val();
				}
			} else if (args.length == 3 || args.length == 4) {
				x = Static.getDouble(args[0], t);
				y = Static.getDouble(args[1], t);
				z = Static.getDouble(args[2], t);
				if (args.length == 4) {
					world = args[3].val();
				}
			}
			if (world != null) {
				w = Static.getServer().getWorld(world);
			}
			if (w == null) {
				throw new ConfigRuntimeException("The specified world " + world + " doesn't exist", ExceptionType.InvalidWorldException, t);
			}
			x = java.lang.Math.floor(x);
			y = java.lang.Math.floor(y);
			z = java.lang.Math.floor(z);
			MCBlock b = w.getBlockAt((int) x, (int) y, (int) z);
			return new CString(b.getTypeId() + ":" + b.getData(), t);
		}

		public Boolean runAsync() {
			return false;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_block_at extends AbstractFunction {

		public String getName() {
			return "set_block_at";
		}

		public Integer[] numArgs() {
			return new Integer[]{2, 3, 4, 5, 6};
		}

		public String docs() {
			return "void {x, y, z, id, [world] [physics] | locationArray, id, [physics]} Sets the id of the block at"
					+ " the x y z coordinates specified. If the first argument passed is an array,"
					+ " it should be x, y, z, world coordinates. Id must be a blocktype identifier similar to the type"
					+ " returned from get_block_at, except if the meta value is not specified, 0 is used."
					+ " If world isn't specified, the current player's world is used. Physics (which defaults to true)"
					+ " specifies whether or not to update the surrounding blocks when this block is set.";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.LengthException, ExceptionType.FormatException, ExceptionType.InvalidWorldException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_0_2;
		}

		public Construct exec(Target t, com.laytonsmith.core.environments.Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			double x = 0;
			double y = 0;
			double z = 0;
			boolean physics = true;
			String id = null;
			String world = null;
			MCWorld w = null;
			MCCommandSender sender = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			if (sender instanceof MCPlayer) {
				w = ((MCPlayer) sender).getWorld();
			}
			if (args.length < 4) {
				if (!(args[0] instanceof CArray)) {
					throw new ConfigRuntimeException("set_block_at expects param 1 to be an array", ExceptionType.CastException, t);
				}
				MCLocation l = ObjectGenerator.GetGenerator().location(args[0], w, t);
				x = l.getBlockX();
				y = l.getBlockY();
				z = l.getBlockZ();
				world = l.getWorld().getName();
				id = args[1].val();
				if (args.length == 3) {
					physics = Static.getBoolean(args[2]);
				}

			} else {
				x = Static.getNumber(args[0], t);
				y = Static.getNumber(args[1], t);
				z = Static.getNumber(args[2], t);
				id = args[3].val();
				if (args.length >= 5) {
					world = args[4].val();
				}
				if (args.length == 6) {
					physics = Static.getBoolean(args[2]);
				}
			}
			if (world != null) {
				w = Static.getServer().getWorld(world);
			}
			if (w == null) {
				throw new ConfigRuntimeException("The specified world " + world + " doesn't exist", ExceptionType.InvalidWorldException, t);
			}
			x = java.lang.Math.floor(x);
			y = java.lang.Math.floor(y);
			z = java.lang.Math.floor(z);
			int ix = (int) x;
			int iy = (int) y;
			int iz = (int) z;
			MCBlock b = w.getBlockAt(ix, iy, iz);
			StringBuilder data = new StringBuilder();
			StringBuilder meta = new StringBuilder();
			boolean inMeta = false;
			for (int i = 0; i < id.length(); i++) {
				Character c = id.charAt(i);
				if (!inMeta) {
					if (!Character.isDigit(c) && c != ':') {
						throw new ConfigRuntimeException("id must be formatted as such: 'x:y' where x and y are integers", ExceptionType.FormatException,
								t);
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
			if (idata > StaticLayer.GetConvertor().getMaxBlockID()) {
				throw new ConfigRuntimeException("Maximum blockID exceeded: " + idata +
						". Attempting to set a block to an item id can corrupt chunks!", ExceptionType.CastException, t);
			}
			b.setTypeAndData(idata, imeta, physics);

			return new CVoid(t);
		}

		public Boolean runAsync() {
			return false;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	@noboilerplate //This function seems to cause a OutOfMemoryError for some reason?
	public static class set_sign_text extends AbstractFunction {

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

		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, com.laytonsmith.core.environments.Environment environment, Construct... args) throws ConfigRuntimeException {
			MCWorld w = null;
			MCCommandSender sender = environment.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			if (sender instanceof MCPlayer) {
				w = ((MCPlayer) sender).getWorld();
			}
			MCLocation l = ObjectGenerator.GetGenerator().location(args[0], w, t);
			if (l.getBlock().isSign()) {
				String line1 = "";
				String line2 = "";
				String line3 = "";
				String line4 = "";
				if (args.length == 2 && args[1] instanceof CArray) {
					CArray ca = (CArray) args[1];
					if (ca.size() >= 1) {
						line1 = ca.get(0, t).val();
					}
					if (ca.size() >= 2) {
						line2 = ca.get(1, t).val();
					}
					if (ca.size() >= 3) {
						line3 = ca.get(2, t).val();
					}
					if (ca.size() >= 4) {
						line4 = ca.get(3, t).val();
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
				MCSign s = l.getBlock().getSign();
				s.setLine(0, line1);
				s.setLine(1, line2);
				s.setLine(2, line3);
				s.setLine(3, line4);
				return new CVoid(t);
			} else {
				throw new ConfigRuntimeException("The block at the specified location is not a sign", ExceptionType.RangeException, t);
			}
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_sign_text extends AbstractFunction {

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

		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, com.laytonsmith.core.environments.Environment environment, Construct... args) throws ConfigRuntimeException {
			MCCommandSender sender = environment.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCWorld w = null;
			if (sender instanceof MCPlayer) {
				w = ((MCPlayer) sender).getWorld();
			}
			MCLocation l = ObjectGenerator.GetGenerator().location(args[0], w, t);
			if (l.getBlock().isSign()) {
				MCSign s = l.getBlock().getSign();
				CString line1 = new CString(s.getLine(0), t);
				CString line2 = new CString(s.getLine(1), t);
				CString line3 = new CString(s.getLine(2), t);
				CString line4 = new CString(s.getLine(3), t);
				return new CArray(t, line1, line2, line3, line4);
			} else {
				throw new ConfigRuntimeException("The block at the specified location is not a sign", ExceptionType.RangeException, t);
			}
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class is_sign_at extends AbstractFunction {

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

		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, com.laytonsmith.core.environments.Environment environment, Construct... args) throws ConfigRuntimeException {
			MCCommandSender sender = environment.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCWorld w = null;
			if (sender instanceof MCPlayer) {
				w = ((MCPlayer) sender).getWorld();
			}
			MCLocation l = ObjectGenerator.GetGenerator().location(args[0], w, t);
			return new CBoolean(l.getBlock().isSign(), t);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class break_block extends AbstractFunction {

		public String getName() {
			return "break_block";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "void {locationArray} Mostly simulates a block break at a location. Does not trigger an event. Only works with"
					+ " craftbukkit.";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.FormatException};
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

		public Construct exec(Target t, com.laytonsmith.core.environments.Environment environment, Construct... args) throws ConfigRuntimeException {
			MCLocation l;
			MCPlayer p;
			p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			MCWorld w = (p != null ? p.getWorld() : null);
			l = ObjectGenerator.GetGenerator().location(args[0], w, t);
			l.breakBlock();
			return new CVoid(t);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_biome extends AbstractFunction {

		public String getName() {
			return "set_biome";
		}

		public Integer[] numArgs() {
			return new Integer[]{2, 3, 4};
		}

		public String docs() {
			return "void {x, z, [world], biome | locationArray, biome} Sets the biome of the specified block column."
					+ " The location array's y value is ignored. ----"
					+ " Biome may be one of the following: " + StringUtil.joinString(MCBiomeType.values(), ", ", 0);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.FormatException, ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, com.laytonsmith.core.environments.Environment environment, Construct... args) throws ConfigRuntimeException {
			int x;
			int z;
			MCCommandSender sender = environment.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCWorld w = null;
			if (sender instanceof MCPlayer) {
				w = ((MCPlayer) sender).getWorld();
			}
			if (args.length == 2) {
				MCLocation l = ObjectGenerator.GetGenerator().location(args[0], w, t);
				x = l.getBlockX();
				z = l.getBlockZ();
				w = l.getWorld();
			} else {
				x = Static.getInt32(args[0], t);
				z = Static.getInt32(args[1], t);
				if (args.length != 3) {
					w = Static.getServer().getWorld(args[2].val());
				}
			}
			MCBiomeType bt;
			try {
				bt = MCBiomeType.valueOf(args[args.length - 1].val());
			} catch (IllegalArgumentException e) {
				throw new ConfigRuntimeException("The biome type \"" + args[1].val() + "\" does not exist.", ExceptionType.FormatException, t);
			}
			if (w == null) {
				throw new ConfigRuntimeException("The specified world doesn't exist, or no world was provided", ExceptionType.InvalidWorldException, t);
			}
			w.setBiome(x, z, bt);
			return new CVoid(t);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_biome extends AbstractFunction {

		public String getName() {
			return "get_biome";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3};
		}

		public String docs() {
			return "string {x, z, [world] | locationArray} Returns the biome type of this block column. The location array's"
					+ " y value is ignored. ---- The value returned"
					+ " may be one of the following: " + StringUtil.joinString(MCBiomeType.values(), ", ", 0);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.FormatException, ExceptionType.CastException, ExceptionType.InvalidWorldException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, com.laytonsmith.core.environments.Environment environment, Construct... args) throws ConfigRuntimeException {
			int x;
			int z;
			MCCommandSender sender = environment.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCWorld w = null;
			if (sender instanceof MCPlayer) {
				w = ((MCPlayer) sender).getWorld();
			}
			if (args.length == 1) {
				MCLocation l = ObjectGenerator.GetGenerator().location(args[0], w, t);
				x = l.getBlockX();
				z = l.getBlockZ();
				w = l.getWorld();
			} else {
				x = Static.getInt32(args[0], t);
				z = Static.getInt32(args[1], t);
				if (args.length != 2) {
					w = Static.getServer().getWorld(args[2].val());
				}
			}
			if (w == null) {
				throw new ConfigRuntimeException("The specified world doesn't exist, or no world was provided", ExceptionType.InvalidWorldException, t);
			}
			MCBiomeType bt = w.getBiome(x, z);
			return new CString(bt.name(), t);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_highest_block_at extends AbstractFunction {

		public String getName() {
			return "get_highest_block_at";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3};
		}

		public String docs() {
			return "array {x, z, [world] | xyzArray, [world]} Gets the xyz of the highest block at a x and a z."
					+ "It works the same as get_block_at, except that it doesn't matter now what the Y is."
					+ "You can set it to -1000 or to 92374 it will just be ignored.";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.FormatException, ExceptionType.CastException, ExceptionType.LengthException, ExceptionType.InvalidWorldException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		public Construct exec(Target t, com.laytonsmith.core.environments.Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			double x = 0;
			double z = 0;
			MCWorld w = null;
			String world = null;
			MCCommandSender sender = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			if (sender instanceof MCPlayer) {
				w = ((MCPlayer) sender).getWorld();
			}

			if (args[0] instanceof CArray && !(args.length == 3)) {
				MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], w, t);
				x = loc.getX();
				z = loc.getZ();
				world = loc.getWorld().getName();
				if (args.length == 2) {
					world = args[1].val();
				}
			} else if (args.length == 2 || args.length == 3) {
				x = Static.getDouble(args[0], t);
				z = Static.getDouble(args[1], t);
				if (args.length == 3) {
					world = args[2].val();
				}
			}
			if (world != null) {
				w = Static.getServer().getWorld(world);
			}
			if (w == null) {
				throw new ConfigRuntimeException("The specified world " + world + " doesn't exist", ExceptionType.InvalidWorldException, t);
			}
			return ObjectGenerator.GetGenerator().location(w.getHighestBlockAt((int) java.lang.Math.floor(x), (int) java.lang.Math.floor(z)).getLocation(), false);
		}

		public Boolean runAsync() {
			return false;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class explosion extends AbstractFunction {

		public String getName() {
			return "explosion";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3};
		}

		public String docs() {
			return "void {Locationarray, [size], [safe]} Creates an explosion with the given size at the given location."
					+ "Size defaults to size of a creeper (3), and null uses the default. If safe is true, (defaults to false)"
					+ " the explosion"
					+ " won't hurt the surrounding blocks. If size is 0, and safe is true, you will still see the animation"
					+ " and hear the sound, but players won't be hurt, and neither will the blocks.";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.FormatException, ExceptionType.CastException, ExceptionType.LengthException, ExceptionType.InvalidWorldException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		public Construct exec(Target t, com.laytonsmith.core.environments.Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			double x = 0;
			double y = 0;
			double z = 0;
			float size = 3;
			MCWorld w = null;
			MCPlayer m = null;
			boolean safe = false;

			if (args.length >= 3) {
				safe = Static.getBoolean(args[2]);
			}
			if (args.length >= 2) {
				if (!(args[1] instanceof CNull)) {
					size = Static.getInt(args[1], t);
				}
			}

			if (size > 100) {
				throw new ConfigRuntimeException("A bit excessive, don't you think? Let's scale that back some, huh?",
						ExceptionType.RangeException, t);
			}

			if (!(args[0] instanceof CArray)) {
				throw new ConfigRuntimeException("Expecting an array at parameter 1 of explosion",
						ExceptionType.CastException, t);
			}

			MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], w, t);
			w = loc.getWorld();
			x = loc.getX();
			z = loc.getZ();
			y = loc.getY();

			if (w == null) {
				if (!(env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer)) {
					throw new ConfigRuntimeException(this.getName() + " needs a world in the location array, or a player so it can take the current world of that player.", ExceptionType.PlayerOfflineException, t);
				}

				m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
				w = m.getWorld();
			}

			w.explosion(x, y, z, size, safe);
			return new CVoid(t);
		}

		public Boolean runAsync() {
			return false;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class play_note extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.RangeException,
						ExceptionType.FormatException, ExceptionType.PlayerOfflineException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, com.laytonsmith.core.environments.Environment environment, Construct... args) throws ConfigRuntimeException {
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			MCInstrument i = null;
			MCNote n = null;
			MCLocation l = null;
			int instrumentOffset = 0;
			int noteOffset = 0;
			if (args.length == 2) {
				Static.AssertPlayerNonNull(p, t);
				instrumentOffset = 0;
				noteOffset = 1;
				l = p.getLocation();
			} else if (args.length == 4) {
				p = Static.GetPlayer(args[0], t);
				instrumentOffset = 1;
				noteOffset = 2;
				l = ObjectGenerator.GetGenerator().location(args[3], p.getWorld(), t);
			} else {
				if (!(args[1] instanceof CArray) && args[2] instanceof CArray) {
					//Player provided, location not
					instrumentOffset = 1;
					noteOffset = 2;
					p = Static.GetPlayer(args[0], t);
					l = p.getLocation();
				} else {
					//location provided, player not
					instrumentOffset = 0;
					noteOffset = 1;
					l = ObjectGenerator.GetGenerator().location(args[2], p.getWorld(), t);
				}
			}
			try {
				i = MCInstrument.valueOf(args[instrumentOffset].val().toUpperCase().trim());
			} catch (IllegalArgumentException e) {
				throw new Exceptions.FormatException("Instrument provided is not a valid type, required one of: " + StringUtils.Join(MCInstrument.values(), ", ", ", or "), t);
			}
			MCTone tone = null;
			if (args[noteOffset] instanceof CArray) {
				int octave = Static.getInt32(((CArray) args[noteOffset]).get("octave"), t);
				if (octave < 0 || octave > 2) {
					throw new Exceptions.RangeException("The octave must be 0, 1, or 2, but was " + octave, t);
				}
				String ttone = ((CArray) args[noteOffset]).get("tone").val().toUpperCase().trim();
				try {
					tone = MCTone.valueOf(ttone.trim().replaceAll("#", ""));
				} catch (IllegalArgumentException e) {
					throw new Exceptions.FormatException("Expected the tone parameter to be one of: "
							+ StringUtils.Join(MCTone.values(), ", ", ", or ") + " but it was " + ttone, t);
				}
				boolean sharped = false;
				if (ttone.trim().endsWith("#")) {
					sharped = true;
				}
				try{
					n = StaticLayer.GetConvertor().GetNote(octave, tone, sharped);
				} catch(IllegalArgumentException e){
					throw new Exceptions.FormatException(e.getMessage(), t);
				}
			} else {
				throw new Exceptions.CastException("Expected an array for note parameter, but " + args[noteOffset] + " found instead", t);
			}
			p.playNote(l, i, n);
			return new CVoid(t);
		}

		public String getName() {
			return "play_note";
		}

		public Integer[] numArgs() {
			return new Integer[]{2, 3, 4};
		}

		public String docs() {
			return "void {[player], instrument, note, [location]} Plays a note for the given player, at the given location."
					+ " Player defaults to the current player, and location defaults to the player's location. Instrument may be one of:"
					+ " " + StringUtils.Join(MCInstrument.values(), ", ", ", or ") + ", and note is an associative array with 2 values,"
					+ " array(octave: 0, tone: 'F#') where octave is either 0, 1, or 2, and tone is one of the notes "
					+ StringUtils.Join(MCTone.values(), ", ", ", or ") + ", optionally suffixed with a pound symbol, which denotes a sharp."
					+ " (Not all notes can be sharped.)";
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}
	
	@api
	public static class play_sound extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.InvalidWorldException,
					ExceptionType.CastException, ExceptionType.FormatException,
					ExceptionType.PlayerOfflineException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t,
				com.laytonsmith.core.environments.Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCLocation l = ObjectGenerator.GetGenerator().location(args[0], null, t);
			MCSound sound = MCSound.BREATH;
			float volume = 1, pitch = 1;
			if (!(args[1] instanceof CArray)) {
				throw new Exceptions.FormatException("An array was expected but recieved " + args[1], t);
			}
			CArray sa = (CArray) args[1];
			if (sa.containsKey("sound")) {
				try {
					sound = MCSound.valueOf(sa.get("sound", t).val().toUpperCase());
				} catch (IllegalArgumentException iae) {

				}
			} else {
				throw new Exceptions.FormatException("Sound field was missing.", t);
			}
			if (sa.containsKey("volume")) {
				volume = Static.getDouble32(sa.get("volume", t), t);
			}
			if (sa.containsKey("pitch")) {
				pitch = Static.getDouble32(sa.get("pitch", t), t);
			}
			if (args.length == 3) {
				java.util.List<MCPlayer> players = new java.util.ArrayList<MCPlayer>();
				if (args[2] instanceof CArray) {
					for (String key : ((CArray) args[2]).keySet()) {
						players.add(Static.GetPlayer(((CArray) args[2]).get(key, t), t));
					}
				} else {
					players.add(Static.GetPlayer(args[2], t));
				}
				for (MCPlayer p : players) {
					p.playSound(l, sound, volume, pitch);
				}
			} else {
				l.getWorld().playSound(l, sound, volume, pitch);
			}
			return new CVoid(t);
		}

		public String getName() {
			return "play_sound";
		}

		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		public String docs() {
			return "void {locationArray, soundArray[, players]} Plays a sound at the"
					+ " given location. SoundArray is in an associative array with"
					+ " keys 'sound', 'volume', 'pitch', where volume and pitch"
					+ " are optional and default to 1. Players can be a single"
					+ " player or an array of players to play the sound to, if"
					+ " not given, all players can potentially hear it. ----"
					+ " Possible sounds: " 
					+ StringUtils.Join(MCSound.values(), ", ", ", or ", " or ");
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_block_info extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.FormatException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, com.laytonsmith.core.environments.Environment environment, Construct... args) throws ConfigRuntimeException {
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			MCLocation l = ObjectGenerator.GetGenerator().location(args[0], p == null ? null : p.getWorld(), t);
			MCBlock b = l.getBlock();
			CArray array = new CArray(t);
			array.set("solid", new CBoolean(b.isSolid(), t), t);
			array.set("flammable", new CBoolean(b.isFlammable(), t), t);
			array.set("transparent", new CBoolean(b.isTransparent(), t), t);
			array.set("occluding", new CBoolean(b.isOccluding(), t), t);
			array.set("burnable", new CBoolean(b.isBurnable(), t), t);
			return array;
			//return new CBoolean(l.getBlock().isSolid(), t);
		}

		public String getName() {
			return "get_block_info";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "array {locationArray} Returns an associative array with various information about a block ---- <ul>"
					+ " <li>solid: If a block is solid (i.e. dirt or stone, as opposed to a torch or water)</li>"
					+ " <li>flammable: Indicates if a block can catch fire</li>"
					+ " <li>transparent: Indicates if light can pass through</li>"
					+ " <li>occluding: indicates If the block fully blocks vision</li>"
					+ " <li>burnable: Indicates if the block can burn away</li>"
					+ "</ul>";
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}
	
	@api(environments = {CommandHelperEnvironment.class})
	public static class get_light_at extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.InvalidWorldException,
					ExceptionType.FormatException};
		}

		public boolean isRestricted() {
			return false;
		}

		public Boolean runAsync() {
			return false;
		}

		public String getName() {
			return "get_light_at";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "int {locationArray} Returns the combined light level at a block, taking into account all sources.";
		}

		public Version since() {
			return CHVersion.V3_3_1;
		}

		public Construct exec(Target t, com.laytonsmith.core.environments.Environment environment, Construct... args)
				throws ConfigRuntimeException {
			MCWorld w = null;
			MCPlayer pl = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if (pl instanceof MCPlayer) {
				w = pl.getWorld();
			}
			MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], w, t);
			return new CInt(loc.getBlock().getLightLevel(), t);
		}
	}
	
	@api(environments = {CommandHelperEnvironment.class})
	public static class is_block_powered extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.InvalidWorldException,
					ExceptionType.FormatException};
		}

		public boolean isRestricted() {
			return false;
		}

		public Boolean runAsync() {
			return false;
		}

		public String getName() {
			return "is_block_powered";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "boolean {locationArray} Returns whether or not a block is being supplied with power."
					+ " Be aware that this interprets 'powered' differently than a human would. For example,"
					+ " using this on a piston next to a redstone block will return false.";
		}

		public Version since() {
			return CHVersion.V3_3_1;
		}

		public Construct exec(Target t, com.laytonsmith.core.environments.Environment environment, Construct... args)
				throws ConfigRuntimeException {
			MCWorld w = null;
			MCPlayer pl = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if (pl instanceof MCPlayer) {
				w = pl.getWorld();
			}
			MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], w, t);
			return new CBoolean(loc.getBlock().isBlockPowered(), t);
		}
	}

	@api
	public static class generate_tree extends AbstractFunction {

		public String getName() {
			return "generate_tree";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.BadEntityException, ExceptionType.FormatException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public String docs() {
			return "boolean {locationArray, [treeType]} Generates a tree at the given location and returns if the generation succeeded or not."
					+ " treeType can be " + StringUtils.Join(MCTreeType.values(), ", ", ", or ", " or ") + ", defaulting to TREE.";
		}

		public Version since() {
			return CHVersion.V3_3_1;
		}

		public Construct exec(Target t, com.laytonsmith.core.environments.Environment environment, Construct... args) throws ConfigRuntimeException {
			MCTreeType treeType;
			if (args.length == 1) {
				treeType = MCTreeType.TREE;
			} else {
				try {
					treeType = MCTreeType.valueOf(args[1].val().toUpperCase());
				} catch (IllegalArgumentException exception) {
					throw new ConfigRuntimeException("The tree type \"" + args[1].val() + "\" does not exist.", ExceptionType.FormatException, t);
				}
			}
			MCPlayer psender = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			MCLocation location = ObjectGenerator.GetGenerator().location(args[0], (psender != null ? psender.getWorld() : null), t);
			return new CBoolean(location.getWorld().generateTree(location, treeType), t);
		}
	}
}
