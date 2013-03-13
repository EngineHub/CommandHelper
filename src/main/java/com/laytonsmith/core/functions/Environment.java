package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.StringUtils;
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
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.noboilerplate;
import com.laytonsmith.core.*;
import com.laytonsmith.core.arguments.Argument;
import com.laytonsmith.core.arguments.ArgumentBuilder;
import com.laytonsmith.core.arguments.Generic;
import com.laytonsmith.core.arguments.Signature;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.laytonsmith.core.natives.MBlockInfo;
import com.laytonsmith.core.natives.MLocation;
import com.laytonsmith.core.natives.annotations.Ranged;
import com.laytonsmith.core.natives.interfaces.MObject;
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
			return "Gets the id of the block at x, y, z. This function expects "
					+ "either 1 or 3 arguments. If 1 argument is passed, it should be an array with the x, y, z"
					+ " coordinates. The format of the return will be x:y where x is the id of the block, and"
					+ " y is the meta data for the block. All blocks will return in this format, but blocks"
					+ " that don't have meta data normally will return 0 in y. If world isn't specified, the current"
					+ " player's world is used.";
		}
		
		public Argument returnType() {
			return new Argument("The id of the block at the given location", CString.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Signature(1, 
							new Argument("The x coordinate", CNumber.class, "x"),
							new Argument("The y coordinate", CNumber.class, "y"),
							new Argument("The z coordinate", CNumber.class, "z"),
							new Argument("The world", CString.class, "world").setOptionalDefaultNull()
						),
						new Signature(2, 
							new Argument("The location of the block", MLocation.class, "locationArray")
						)
					);
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
				x = args[0].primitive(t).castToDouble(t);	
				y = args[1].primitive(t).castToDouble(t);
				z = args[2].primitive(t).castToDouble(t);
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
			return new Integer[]{2, 4, 5};
		}

		public String docs() {
			return "Sets the id of the block at the x y z coordinates specified. If the"
					+ " first argument passed is an array, it should be x, y, z, world coordinates. id must"
					+ " be a blocktype identifier similar to the type returned from get_block_at, except if the meta"
					+ " value is not specified, 0 is used. If world isn't specified, the current player's world"
					+ " is used.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Signature(1,
							new Argument("", CNumber.class, "x"),
							new Argument("", CNumber.class, "y"),
							new Argument("", CNumber.class, "z"),
							new Argument("", CString.class, CInt.class, "id"),
							new Argument("", CString.class, "world").setOptionalDefaultNull()
						), new Signature(2, 
							new Argument("", MLocation.class, "locationArray"),
							new Argument("", CString.class, CInt.class, "id")
						)
					);
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
			String id = null;
			String world = null;
			MCWorld w = null;
			MCCommandSender sender = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			if (sender instanceof MCPlayer) {
				w = ((MCPlayer) sender).getWorld();
			}
			if (args.length == 2) {
				if (!(args[0] instanceof CArray)) {
					throw new ConfigRuntimeException("set_block_at expects param 1 to be an array", ExceptionType.CastException, t);
				}
				MCLocation l = ObjectGenerator.GetGenerator().location(args[0], w, t);
				x = l.getBlockX();
				y = l.getBlockY();
				z = l.getBlockZ();
				world = l.getWorld().getName();
				id = args[1].val();

			} else {
				x = args[0].primitive(t).castToDouble(t);
				y = args[1].primitive(t).castToDouble(t);
				z = args[2].primitive(t).castToDouble(t);
				id = args[3].val();
				if (args.length == 5) {
					world = args[4].val();
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
			b.setTypeId(idata);
			b.setData(imeta);

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
			return "Sets the text of the sign at the given location. If the block at x,y,z isn't a sign,"
					+ " a RangeException is thrown. If the text on a line overflows 15 characters, it is simply"
					+ " truncated.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Signature(1, 
							new Argument("", MLocation.class, "locationArray"),
							new Argument("A length 4 array that contains the text to put on the sign", CArray.class, "lineArray").setGenerics(new Generic(CString.class))
						), new Signature(2, 
							new Argument("", MLocation.class, "locationArray"),
							new Argument("The first line of text", CString.class, "line1"),
							new Argument("The second line of text", CString.class, "line2").setOptionalDefault(""),
							new Argument("The third line of text", CString.class, "line3").setOptionalDefault(""),
							new Argument("The fourth line of text", CString.class, "line4").setOptionalDefault("")
						)
					);
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
		
		public Argument returnType() {
			return new Argument("An array of length 4, with the text of the sign", CArray.class).setGenerics(new Generic(CString.class));
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", MLocation.class, "location")
					);
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
		
		public Argument returnType() {
			return new Argument("True, if the block at the location is a sign", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", MLocation.class, "location")
					);
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
			return "Mostly simulates a block break at a location. Does not trigger an event. Only works with"
					+ " craftbukkit.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", MLocation.class, "location")
					);
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
			return "Sets the biome of the specified block column."
					+ " The location array's y value is ignored. ----"
					+ " Biome may be one of the following: " + StringUtil.joinString(MCBiomeType.values(), ", ", 0);
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Signature(1, 
							new Argument("", CNumber.class, "x"),
							new Argument("", CNumber.class, "z"),
							new Argument("", CString.class, "world").setOptionalDefaultNull(),
							new Argument("The biome type to set for the column specified", MCBiomeType.class, "biome")
						), new Signature(2, 
							new Argument("", MLocation.class, "location"),
							new Argument("The biome type to set for the column specified", MCBiomeType.class, "biome")
						)
					);
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
				x = args[0].primitive(t).castToInt32(t);
				z = args[1].primitive(t).castToInt32(t);
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
			return "Returns the biome type of this block column. The location array's"
					+ " y value is ignored. ---- The value returned"
					+ " may be one of the following: " + StringUtil.joinString(MCBiomeType.values(), ", ", 0);
		}
		
		public Argument returnType() {
			return new Argument("The biome currently set for this column", MCBiomeType.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Signature(1, 
							new Argument("", CNumber.class, "x"),
							new Argument("", CNumber.class, "z"),
							new Argument("", CString.class, "world").setOptionalDefaultNull()
						), new Signature(2, 
							new Argument("", MLocation.class, "location")
						)
					);
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
				x = args[0].primitive(t).castToInt32(t);
				z = args[1].primitive(t).castToInt32(t);
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
			return "Gets the location of the highest block at a x and a z."
					+ "The y value of the location is ignored, and may be set to anything.";
		}
		
		public Argument returnType() {
			return new Argument("", MLocation.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Signature(1, 
							new Argument("", CNumber.class, "x"),
							new Argument("", CNumber.class, "z"),
							new Argument("", CString.class, "world").setOptionalDefaultNull()
						), new Signature(2, 
							new Argument("", MLocation.class, "location")
						)
					);
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
			} else if (args.length == 2 || args.length == 3) {
				x = args[0].primitive(t).castToDouble(t);
				z = args[1].primitive(t).castToDouble(t);
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
			x = java.lang.Math.floor(x);
			y = java.lang.Math.floor(y) - 1;
			z = java.lang.Math.floor(z);
			MCBlock b = w.getHighestBlockAt((int) x, (int) z);
			return new CArray(t,
					new CInt(b.getX(), t),
					new CInt(b.getY(), t),
					new CInt(b.getZ(), t));
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
			return "Creates an explosion with the given size at the given location."
					+ "Size defaults to size of a creeper (3), and null uses the default. If safe is true, (defaults to false)"
					+ " the explosion"
					+ " won't hurt the surrounding blocks. If size is 0, and safe is true, you will still see the animation"
					+ " and hear the sound, but players won't be hurt, and neither will the blocks.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", MLocation.class, "location"),
						new Argument("The size of the explosion", CInt.class, "size").addAnnotation(new Ranged(0, Integer.MAX_VALUE)).setOptionalDefault(3),
						new Argument("Whether or not the explosion will hurt surrounding blocks", CBoolean.class, "safe").setOptionalDefault(false)
					);
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
				safe = args[2].primitive(t).castToBoolean();
			}
			if (args.length >= 2) {
				if (!(args[1].isNull())) {
					size = args[1].primitive(t).castToInt32(t);
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
				int octave = ((CArray) args[noteOffset]).get("octave").primitive(t).castToInt32(t);
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
			return "Plays a note for the given player, at the given location."
					+ " Player defaults to the current player, and location defaults to the player's location. Instrument may be one of:"
					+ " " + StringUtils.Join(MCInstrument.values(), ", ", ", or ") + ", and note is an associative array with 2 values,"
					+ " array(octave: 0, tone: 'F#') where octave is either 0, 1, or 2, and tone is one of the notes "
					+ StringUtils.Join(MCTone.values(), ", ", ", or ") + ", optionally suffixed with a pound symbol, which denotes a sharp."
					+ " (Not all notes can be sharped.)";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CString.class, "player").setOptionalDefaultNull(),
						new Argument("The instrument to play", MCInstrument.class, "instrument"),
						new Argument("The note, formatted as described", CArray.class, "note"),
						new Argument("The location to play the note at", MLocation.class, "location").setOptionalDefaultNull()
					);
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
			int volume = 1; int pitch = 1;
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
				volume = Static.getInt32(sa.get("volume", t), t);
			}
			if (sa.containsKey("pitch")) {
				pitch = Static.getInt32(sa.get("pitch", t), t);
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
			MBlockInfo bi = new MBlockInfo();
			bi.solid = b.isSolid();
			bi.flammable = b.isFlammable();
			bi.transparent = b.isTransparent();
			bi.occluding = b.isOccluding();
			bi.burnable = b.isBurnable();
			return bi.deconstruct(t);
			//return new CBoolean(l.getBlock().isSolid(), t);
		}

		public String getName() {
			return "get_block_info";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "Returns an object with various information about a block";
		}
		
		public Argument returnType() {
			return new Argument("", MBlockInfo.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", MLocation.class, "location")
					);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}
}
