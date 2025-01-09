package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCNote;
import com.laytonsmith.abstraction.MCOfflinePlayer;
import com.laytonsmith.abstraction.MCPattern;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCPlayerProfile;
import com.laytonsmith.abstraction.MCProfileProperty;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.abstraction.blocks.MCBanner;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCBlockData;
import com.laytonsmith.abstraction.blocks.MCBlockState;
import com.laytonsmith.abstraction.blocks.MCCommandBlock;
import com.laytonsmith.abstraction.blocks.MCDecoratedPot;
import com.laytonsmith.abstraction.blocks.MCEndGateway;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.blocks.MCSign;
import com.laytonsmith.abstraction.blocks.MCSign.Side;
import com.laytonsmith.abstraction.blocks.MCSignText;
import com.laytonsmith.abstraction.blocks.MCSkull;
import com.laytonsmith.abstraction.enums.MCBiomeType;
import com.laytonsmith.abstraction.enums.MCDyeColor;
import com.laytonsmith.abstraction.enums.MCInstrument;
import com.laytonsmith.abstraction.enums.MCParticle;
import com.laytonsmith.abstraction.enums.MCPatternShape;
import com.laytonsmith.abstraction.enums.MCSound;
import com.laytonsmith.abstraction.enums.MCSoundCategory;
import com.laytonsmith.abstraction.enums.MCTone;
import com.laytonsmith.abstraction.enums.MCTreeType;
import com.laytonsmith.abstraction.enums.MCVersion;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.noboilerplate;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Optimizable;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.compiler.CompilerWarning;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.exceptions.CRE.CREBadEntityException;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CREIllegalArgumentException;
import com.laytonsmith.core.exceptions.CRE.CREInvalidWorldException;
import com.laytonsmith.core.exceptions.CRE.CRELengthException;
import com.laytonsmith.core.exceptions.CRE.CRENotFoundException;
import com.laytonsmith.core.exceptions.CRE.CREPlayerOfflineException;
import com.laytonsmith.core.exceptions.CRE.CRERangeException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.Arrays;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Environment {

	public static String docs() {
		return "Allows you to manipulate the environment around the player";
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_block extends AbstractFunction {

		@Override
		public String getName() {
			return "get_block";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "string {locationArray} Gets the type the block at the location.";
		}

		@Override
		public Mixed exec(Target t, com.laytonsmith.core.environments.Environment env, Mixed... args)
				throws CancelCommandException, ConfigRuntimeException {
			MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], null, t);
			MCBlock b = loc.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
			if(b == null) {
				throw new CRENotFoundException(
						"Could not find the block in " + this.getName() + " (are you running in cmdline mode?)", t);
			}
			return new CString(b.getType().getName(), t);
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class, CRECastException.class, CREInvalidWorldException.class,
					CRENotFoundException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_3;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_block extends AbstractFunction {

		@Override
		public String getName() {
			return "set_block";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public String docs() {
			return "void {locationArray, blockName, [physics]} Sets the block at the location."
					+ " The physics boolean determines whether or not this causes a block update. Defaults to true.";
		}

		@Override
		public Mixed exec(Target t, com.laytonsmith.core.environments.Environment env, Mixed... args)
				throws CancelCommandException, ConfigRuntimeException {
			MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], null, t);
			boolean physics = true;
			if(args.length == 3) {
				physics = ArgumentValidation.getBoolean(args[2], t);
			}
			MCMaterial mat = StaticLayer.GetMaterial(args[1].val());
			if(mat == null) {
				throw new CREIllegalArgumentException("Cannot find the material \"" + args[1].val() + "\".", t);
			}
			loc.getBlock().setType(mat, physics);
			return CVoid.VOID;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class, CRECastException.class, CREInvalidWorldException.class,
					CREIllegalArgumentException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_3;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_blockdata_string extends AbstractFunction {

		@Override
		public String getName() {
			return "get_blockdata_string";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "string {locationArray} Gets the block data at the location in a string format."
					+ " Forward compatibility is not ensured.";
		}

		@Override
		public Mixed exec(Target t, com.laytonsmith.core.environments.Environment env, Mixed... args)
				throws CancelCommandException, ConfigRuntimeException {
			MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], null, t);
			MCBlock b = loc.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
			if(b == null) {
				throw new CRENotFoundException("Could not find the block in " + this.getName() + " (cmdline mode?)", t);
			}
			return new CString(b.getBlockData().getAsString(), t);
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class, CRECastException.class, CREInvalidWorldException.class,
					CRENotFoundException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_3;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_blockdata_string extends AbstractFunction {

		@Override
		public String getName() {
			return "set_blockdata_string";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public String docs() {
			return "void {locationArray, data, [physics]} Sets the block at the location from a blockdata string."
					+ " Forward compatibility is not ensured.";
		}

		@Override
		public Mixed exec(Target t, com.laytonsmith.core.environments.Environment env, Mixed... args)
				throws CancelCommandException, ConfigRuntimeException {
			MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], null, t);
			MCBlock b = loc.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
			if(b == null) {
				throw new CRENotFoundException("Could not find the block in " + this.getName() + " (cmdline mode?)", t);
			}
			MCBlockData bd;
			try {
				bd = Static.getServer().createBlockData(args[1].val());
			} catch (IllegalArgumentException ex) {
				throw new CREIllegalArgumentException("Cannot create block data from string: " + args[1].val(), t);
			}
			boolean physics = true;
			if(args.length == 3) {
				physics = ArgumentValidation.getBoolean(args[2], t);
			}
			b.setBlockData(bd, physics);
			return CVoid.VOID;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class, CRECastException.class, CREInvalidWorldException.class,
					CRENotFoundException.class, CREIllegalArgumentException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_3;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_blockdata extends AbstractFunction {

		@Override
		public String getName() {
			return "get_blockdata";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "array {locationArray} Gets the block data as an array at the location.";
		}

		@Override
		public Mixed exec(Target t, com.laytonsmith.core.environments.Environment env, Mixed... args)
				throws CancelCommandException, ConfigRuntimeException {
			MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], null, t);
			MCBlock b = loc.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
			if(b == null) {
				throw new CRENotFoundException("Could not find the block in " + this.getName() + " (cmdline mode?)", t);
			}
			return ObjectGenerator.GetGenerator().blockData(b.getBlockData(), t);
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class, CRECastException.class, CREInvalidWorldException.class,
					CRENotFoundException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_4;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_blockdata extends AbstractFunction {

		@Override
		public String getName() {
			return "set_blockdata";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public String docs() {
			return "void {locationArray, data, [physics]} Sets the block at the location from a blockdata object."
					+ " Blockdata can be an associative array or string format."
					+ " If an array, a 'block' key must exist with the block material."
					+ " All the other keys must be a blockstate and its value.";
		}

		@Override
		public Mixed exec(Target t, com.laytonsmith.core.environments.Environment env, Mixed... args)
				throws CancelCommandException, ConfigRuntimeException {
			MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], null, t);
			MCBlock b = loc.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
			if(b == null) {
				throw new CRENotFoundException("Could not find the block in " + this.getName() + " (cmdline mode?)", t);
			}
			boolean physics = true;
			if(args.length == 3) {
				physics = ArgumentValidation.getBooleanish(args[2], t);
			}
			MCBlockData bd;
			try {
				if(args[1] instanceof CArray) {
					CArray bda = (CArray) args[1];
					if(bda.size() == 1) {
						MCMaterial mat = StaticLayer.GetMaterial(bda.get("block", t).val().toUpperCase());
						if(mat == null) {
							throw new CREIllegalArgumentException("Cannot find material \""
									+ bda.get("block", t).val() + "\".", t);
						}
						b.setType(mat);
						return CVoid.VOID;
					}
					bd = ObjectGenerator.GetGenerator().blockData((CArray) args[1], t);
				} else {
					bd = Static.getServer().createBlockData(args[1].val());
				}
			} catch (IllegalArgumentException ex) {
				throw new CREIllegalArgumentException("Cannot create block data from: " + args[1].val(), t);
			}
			b.setBlockData(bd, physics);
			return CVoid.VOID;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class, CRECastException.class, CREInvalidWorldException.class,
					CRENotFoundException.class, CREIllegalArgumentException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_4;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	@noboilerplate //This function seems to cause a OutOfMemoryError for some reason?
	public static class set_sign_text extends AbstractFunction {

		@Override
		public String getName() {
			return "set_sign_text";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3, 4, 5};
		}

		@Override
		public String docs() {
			return "void {locationArray, [side], lineArray | locationArray, line1, [line2, [line3, [line4]]]}"
					+ " Sets the text on the side of a sign at the given location."
					+ " Side can be FRONT (default) or BACK. (MC 1.20+)"
					+ " If the block at x,y,z isn't a sign, a RangeException is thrown."
					+ " If a text line cannot fit on the sign, it'll be cut off.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRERangeException.class, CREFormatException.class};
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
		public Mixed exec(Target t, com.laytonsmith.core.environments.Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer player = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			MCWorld world = null;
			if(player != null) {
				world = player.getWorld();
			}
			MCBlock block = ObjectGenerator.GetGenerator().location(args[0], world, t).getBlock();
			MCSign.Side side = Side.FRONT;
			String line1 = "";
			String line2 = "";
			String line3 = "";
			String line4 = "";
			if((args.length == 2 || args.length == 3) && args[args.length - 1].isInstanceOf(CArray.TYPE)) {
				if(args.length == 3) {
					try {
						side = MCSign.Side.valueOf(args[1].val());
					} catch (IllegalArgumentException ex) {
						throw new CREFormatException("Invalid sign side: " + args[1].val(), t);
					}
				}
				CArray ca = (CArray) args[args.length - 1];
				if(ca.size() >= 1) {
					line1 = ca.get(0, t).val();
				}
				if(ca.size() >= 2) {
					line2 = ca.get(1, t).val();
				}
				if(ca.size() >= 3) {
					line3 = ca.get(2, t).val();
				}
				if(ca.size() >= 4) {
					line4 = ca.get(3, t).val();
				}

			} else {
				if(args.length >= 2) {
					line1 = args[1].val();
				}
				if(args.length >= 3) {
					line2 = args[2].val();
				}
				if(args.length >= 4) {
					line3 = args[3].val();
				}
				if(args.length >= 5) {
					line4 = args[4].val();
				}
			}
			MCSign sign;
			try {
				sign = block.getSign();
			} catch (ClassCastException ex) {
				throw new CRERangeException("The block at the specified location is not a sign", t);
			}
			MCSignText text = sign;
			if(side == Side.BACK) {
				text = sign.getBackText();
				if(text == null) {
					throw new CRERangeException("Sign does not have back text.", t);
				}
			}
			text.setLine(0, line1);
			text.setLine(1, line2);
			text.setLine(2, line3);
			text.setLine(3, line4);
			sign.update();
			return CVoid.VOID;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_sign_text extends AbstractFunction {

		@Override
		public String getName() {
			return "get_sign_text";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "array {locationArray, [side]} Gets an array of 4 strings of the text on the side of a sign."
					+ " Side can be FRONT (default) or BACK. (MC 1.20+)"
					+ " If the location given isn't a sign, then a RangeException is thrown.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRERangeException.class, CREFormatException.class};
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
		public Mixed exec(Target t, com.laytonsmith.core.environments.Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer player = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			MCWorld world = null;
			if(player != null) {
				world = player.getWorld();
			}
			MCBlock block = ObjectGenerator.GetGenerator().location(args[0], world, t).getBlock();
			MCSignText text;
			try {
				text = block.getSign();
			} catch (ClassCastException ex) {
				throw new CRERangeException("The block at the specified location is not a sign", t);
			}
			if(args.length == 2) {
				try {
					if(MCSign.Side.valueOf(args[1].val()) == Side.BACK) {
						text = ((MCSign) text).getBackText();
						if(text == null) {
							throw new CRERangeException("Sign does not have back text.", t);
						}
					}
				} catch (IllegalArgumentException ex) {
					throw new CREFormatException("Invalid sign side: " + args[1].val(), t);
				}
			}
			CString line1 = new CString(text.getLine(0), t);
			CString line2 = new CString(text.getLine(1), t);
			CString line3 = new CString(text.getLine(2), t);
			CString line4 = new CString(text.getLine(3), t);
			return new CArray(t, line1, line2, line3, line4);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class is_sign_at extends AbstractFunction {

		@Override
		public String getName() {
			return "is_sign_at";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {locationArray} Returns true if the block at this location is a sign.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class};
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
		public Mixed exec(Target t, com.laytonsmith.core.environments.Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCCommandSender sender = environment.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCWorld w = null;
			if(sender instanceof MCPlayer) {
				w = ((MCPlayer) sender).getWorld();
			}
			MCLocation l = ObjectGenerator.GetGenerator().location(args[0], w, t);
			return CBoolean.get(l.getBlock().isSign());
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class is_sign_text_glowing extends AbstractFunction {

		@Override
		public String getName() {
			return "is_sign_text_glowing";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "boolean {locationArray, [side]} Returns true if the sign side at this location has glowing text. (MC 1.17+)"
					+ " Side can be FRONT (default) or BACK. (MC 1.20+)";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class, CREInvalidWorldException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_5;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, com.laytonsmith.core.environments.Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCCommandSender sender = environment.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCWorld w = null;
			if(sender instanceof MCPlayer) {
				w = ((MCPlayer) sender).getWorld();
			}
			MCBlock b = ObjectGenerator.GetGenerator().location(args[0], w, t).getBlock();
			if(b.isSign()) {
				MCSignText text = b.getSign();
				if(args.length == 2) {
					try {
						if(MCSign.Side.valueOf(args[1].val()) == Side.BACK) {
							text = ((MCSign) text).getBackText();
							if(text == null) {
								throw new CRERangeException("Sign does not have back text.", t);
							}
						}
					} catch (IllegalArgumentException ex) {
						throw new CREFormatException("Invalid sign side: " + args[1].val(), t);
					}
				}
				return CBoolean.get(text.isGlowingText());
			} else {
				throw new CRERangeException("The block at the specified location is not a sign", t);
			}
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_sign_text_glowing extends AbstractFunction {

		@Override
		public String getName() {
			return "set_sign_text_glowing";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public String docs() {
			return "void {locationArray, [side], isGlowing} Sets the text on a sign side to be glowing or not. (MC 1.17+)"
					+ " Side can be FRONT (default) or BACK. (MC 1.20+)";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRERangeException.class, CREFormatException.class, CREInvalidWorldException.class,
					CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_5;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, com.laytonsmith.core.environments.Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCCommandSender sender = environment.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCWorld w = null;
			if(sender instanceof MCPlayer) {
				w = ((MCPlayer) sender).getWorld();
			}
			MCBlock b = ObjectGenerator.GetGenerator().location(args[0], w, t).getBlock();
			if(b.isSign()) {
				MCSign sign = b.getSign();
				MCSignText text = sign;
				if(args.length == 3) {
					try {
						if(MCSign.Side.valueOf(args[1].val()) == Side.BACK) {
							text = sign.getBackText();
							if(text == null) {
								throw new CRERangeException("Sign does not have back text.", t);
							}
						}
					} catch (IllegalArgumentException ex) {
						throw new CREFormatException("Invalid sign side: " + args[1].val(), t);
					}
				}
				text.setGlowingText(ArgumentValidation.getBooleanObject(args[args.length - 1], t));
				sign.update();
				return CVoid.VOID;
			} else {
				throw new CRERangeException("The block at the specified location is not a sign", t);
			}
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class is_sign_waxed extends AbstractFunction {

		@Override
		public String getName() {
			return "is_sign_waxed";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {locationArray} Returns whether a sign is waxed and uneditable. (MC 1.20.1+)";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class, CREInvalidWorldException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_5;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, com.laytonsmith.core.environments.Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCCommandSender sender = environment.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCWorld w = null;
			if(sender instanceof MCPlayer) {
				w = ((MCPlayer) sender).getWorld();
			}
			MCBlock b = ObjectGenerator.GetGenerator().location(args[0], w, t).getBlock();
			if(!b.isSign()) {
				throw new CRERangeException("The block at the specified location is not a sign", t);
			}
			MCSign sign = b.getSign();
			return CBoolean.get(sign.isWaxed());
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_sign_waxed extends AbstractFunction {

		@Override
		public String getName() {
			return "set_sign_waxed";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "void {locationArray, boolean} Sets a sign to be waxed or not. (MC 1.20.1+)"
					+ " If a sign is waxed, it is not editable by players.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRERangeException.class, CREFormatException.class, CREInvalidWorldException.class,
					CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_5;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, com.laytonsmith.core.environments.Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCCommandSender sender = environment.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCWorld w = null;
			if(sender instanceof MCPlayer) {
				w = ((MCPlayer) sender).getWorld();
			}
			MCBlock b = ObjectGenerator.GetGenerator().location(args[0], w, t).getBlock();
			if(!b.isSign()) {
				throw new CRERangeException("The block at the specified location is not a sign", t);
			}
			MCSign sign = b.getSign();
			sign.setWaxed(ArgumentValidation.getBooleanObject(args[1], t));
			sign.update();
			return CVoid.VOID;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_skull_owner extends AbstractFunction {

		@Override
		public String getName() {
			return "set_skull_owner";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public String docs() {
			return "void {locationArray, owner, [texture]}"
					+ " Sets the owner of the skull at the given location by name or uuid."
					+ " Supplying null will clear the skull owner, but due to limitations in Bukkit, clients will only"
					+ " see this change after reloading the block."
					+ " If owner is not null, the texture value as a Base64 encoded string may be provided. (Paper only)"
					+ " If no world is provided and the function is executed by a player, the player's world is used."
					+ " If the block at the given location isn't a skull, a RangeException is thrown.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRERangeException.class, CREFormatException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_4;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, com.laytonsmith.core.environments.Environment environment, Mixed... args)
				throws ConfigRuntimeException {
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], p != null ? p.getWorld() : null, t);
			MCBlock block = loc.getBlock();
			MCBlockState blockState = block.getState();
			if(!(blockState instanceof MCSkull skull)) {
				throw new CRERangeException("The block at the specified location is not a skull", t);
			}
			MCOfflinePlayer owner = (args[1] instanceof CNull ? null : Static.GetUser(args[1], t));
			if(owner != null && args.length == 3) {
				MCPlayerProfile profile = Static.getServer().getPlayerProfile(owner.getUniqueID(), owner.getName());
				if(profile != null) {
					if(args[2] instanceof CNull) {
						profile.removeProperty("textures");
					} else {
						profile.setProperty(new MCProfileProperty("textures", args[2].val(), null));
					}
					skull.setPlayerProfile(profile);
					skull.update();
					return CVoid.VOID;
				}
			}
			skull.setOwningPlayer(owner);
			skull.update();
			return CVoid.VOID;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_skull_owner extends AbstractFunction {

		@Override
		public String getName() {
			return "get_skull_owner";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "array {locationArray}"
					+ " Returns the owner name and uuid of the skull at the given location as an array in format:"
					+ " {name: string, uuid: string, texture: string}, or null if the skull does not have an owner."
					+ " The value at the 'name' key will be an empty string if the server does not know the player's name."
					+ " The 'texture' value is the Base64 encoded string of the textures property, or null. (Paper only)"
					+ " If no world is provided and the function is executed by a player, the player's world is used."
					+ " If the block at the given location isn't a skull, a RangeException is thrown.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRERangeException.class, CREFormatException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_4;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, com.laytonsmith.core.environments.Environment environment, Mixed... args)
				throws ConfigRuntimeException {
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], p != null ? p.getWorld() : null, t);
			MCBlockState blockState = loc.getBlock().getState();
			if(!(blockState instanceof MCSkull skull)) {
				throw new CRERangeException("The block at the specified location is not a skull", t);
			}
			MCOfflinePlayer owner = skull.getOwningPlayer();
			if(owner == null) {
				return CNull.NULL;
			}
			CArray ret = new CArray(t);
			ret.set("name", owner.getName());
			ret.set("uuid", owner.getUniqueID().toString());
			MCPlayerProfile playerProfile = skull.getPlayerProfile();
			if(playerProfile != null) {
				MCProfileProperty textureProperty = playerProfile.getProperty("textures");
				if(textureProperty != null) {
					ret.set("texture", skull.getPlayerProfile().getProperty("textures").getValue());
				} else {
					ret.set("texture", CNull.NULL, t);
				}
			}
			return ret;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class break_block extends AbstractFunction {

		@Override
		public String getName() {
			return "break_block";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "boolean {locationArray, [itemArray]} Mostly simulates a block break at a location."
					+ " Optional item array to simulate tool used to break block."
					+ " Returns true if block was not already air and either no tool was given, a correct tool was given,"
					+ " or the block type does not require a specific tool."
					+ " Does not trigger an event.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class, CREInvalidWorldException.class, CRECastException.class,
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
		public Mixed exec(Target t, com.laytonsmith.core.environments.Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			MCWorld w = (p != null ? p.getWorld() : null);
			MCBlock b = ObjectGenerator.GetGenerator().location(args[0], w, t).getBlock();
			boolean success;
			if(args.length == 2) {
				MCItemStack item = ObjectGenerator.GetGenerator().item(args[1], t);
				success = b.breakNaturally(item);
			} else {
				success = b.breakNaturally(null);
			}
			return CBoolean.get(success);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_biome extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "set_biome";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3, 4};
		}

		@Override
		public String docs() {
			return "void {x, z, [world], biome | locationArray, biome} Sets the biome at a block location."
					+ " Minecraft only provides one quarter precision for three dimensional biomes, so this sets"
					+ " the nearest center of a 4x4x4 region."
					+ " When not using a location array, the entire column at the x and z coordinates are set."
					+ " ---- Biome may be one of the following: " + StringUtils.Join(MCBiomeType.types(), ", ", ", or ");
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class, CRECastException.class, CRENotFoundException.class,
					CREInvalidWorldException.class};
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
		public Mixed exec(Target t, com.laytonsmith.core.environments.Environment environment, Mixed... args) throws ConfigRuntimeException {
			int x;
			int z;
			MCCommandSender sender = environment.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCWorld w = null;
			if(sender instanceof MCPlayer) {
				w = ((MCPlayer) sender).getWorld();
			}
			MCBiomeType biomeType;
			try {
				biomeType = MCBiomeType.valueOf(args[args.length - 1].val());
				if(biomeType == null) {
					throw new CRENotFoundException(
							"Could not find the internal biome type object (are you running in cmdline mode?)", t);
				}
			} catch (IllegalArgumentException e) {
				throw new CREFormatException("The biome type \"" + args[args.length - 1].val() + "\" does not exist.", t);
			}
			if(args.length == 2) {
				MCLocation location = ObjectGenerator.GetGenerator().location(args[0], w, t);
				location.getWorld().setBiome(location, biomeType);
			} else {
				x = ArgumentValidation.getInt32(args[0], t);
				z = ArgumentValidation.getInt32(args[1], t);
				if(args.length == 4) {
					w = Static.getServer().getWorld(args[2].val());
				} else if(w == null) {
					throw new CREInvalidWorldException("No world was provided", t);
				}
				w.setBiome(x, z, biomeType);
			}
			return CVoid.VOID;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ParseTree optimizeDynamic(Target t, com.laytonsmith.core.environments.Environment env,
				Set<Class<? extends com.laytonsmith.core.environments.Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {

			if(children.size() < 2) {
				return null;
			}
			Mixed c = children.get(children.size() - 1).getData();
			if(c.isInstanceOf(CString.TYPE)) {
				try {
					MCBiomeType.MCVanillaBiomeType.valueOf(c.val());
				} catch (IllegalArgumentException ex) {
					env.getEnv(CompilerEnvironment.class).addCompilerWarning(fileOptions, new CompilerWarning(
							c.val() + " is not a valid enum in com.commandhelper.BiomeType",
							c.getTarget(), null));
				}
			}
			return null;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_biome extends AbstractFunction {

		@Override
		public String getName() {
			return "get_biome";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "string {locationArray} Returns the biome type at a block location."
					+ " ---- The value returned may be one of the following: " + StringUtils.Join(MCBiomeType.types(), ", ", ", or ");
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class, CRECastException.class, CREInvalidWorldException.class,
					CRENotFoundException.class};
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
		public Mixed exec(Target t, com.laytonsmith.core.environments.Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCCommandSender sender = environment.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCWorld w = null;
			if(sender instanceof MCPlayer) {
				w = ((MCPlayer) sender).getWorld();
			}
			MCLocation location = ObjectGenerator.GetGenerator().location(args[0], w, t);
			MCBiomeType bt = location.getWorld().getBiome(location);
			if(bt == null) {
				throw new CRENotFoundException("Could not find the biome type (are you running in cmdline mode?)", t);
			}
			return new CString(bt.name(), t);
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_highest_block_at extends AbstractFunction {

		@Override
		public String getName() {
			return "get_highest_block_at";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3};
		}

		@Override
		public String docs() {
			return "array {x, z, [world] | locationArray, [world]} Gets a location array for the highest block at a"
					+ " specific x and z column. If a location array is specified, the y coordinate is ignored.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class, CRECastException.class,
				CRELengthException.class, CREInvalidWorldException.class,
				CRENotFoundException.class};
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
		public Mixed exec(Target t, com.laytonsmith.core.environments.Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			int x = 0;
			int z = 0;
			MCWorld w = null;
			String world = null;
			MCPlayer player = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if(player != null) {
				w = player.getWorld();
			}

			if(args.length < 3 && args[0].isInstanceOf(CArray.TYPE)) {
				MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], w, t);
				x = loc.getBlockX();
				z = loc.getBlockZ();
				w = loc.getWorld();
				if(args.length == 2) {
					world = args[1].val();
				}
			} else if(args.length > 1) {
				x = (int) java.lang.Math.floor(ArgumentValidation.getDouble(args[0], t));
				z = (int) java.lang.Math.floor(ArgumentValidation.getDouble(args[1], t));
				if(args.length == 3) {
					world = args[2].val();
				}
			}
			if(world != null) {
				w = Static.getServer().getWorld(world);
			}
			if(w == null) {
				throw new CREInvalidWorldException("The specified world " + world + " doesn't exist", t);
			}
			MCBlock highestBlock = w.getHighestBlockAt(x, z);
			if(highestBlock == null) {
				throw new CRENotFoundException(
						"Could not find the highest block in " + this.getName() + " (are you running in cmdline mode?)", t);
			}
			return ObjectGenerator.GetGenerator().location(highestBlock.getLocation(), false);
		}

		@Override
		public Boolean runAsync() {
			return false;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class explosion extends AbstractFunction {

		@Override
		public String getName() {
			return "explosion";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3, 4, 5};
		}

		@Override
		public String docs() {
			return "boolean {locationArray, [size], [safe], [fire], [source]} Creates an explosion at a location."
					+ " Size defaults to size of a creeper (3), where 0 does no damage to entities."
					+ " If safe is true (defaults to false) the explosion won't break blocks."
					+ " If fire is true (defaults to true unless safe is true), the explosion may leave behind fire."
					+ " The source must be an entity UUID, which can be used for block and entity damage attribution."
					+ " The explosion will not damage source entity."
					+ " Returns false if the explosion was canceled.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class, CRECastException.class, CRELengthException.class,
					CREInvalidWorldException.class, CRERangeException.class, CREBadEntityException.class};
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
		public Mixed exec(Target t, com.laytonsmith.core.environments.Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			MCLocation loc;
			float size = 3;
			boolean safe = false;
			boolean fire = true;
			MCEntity source = null;

			if(!(args[0].isInstanceOf(CArray.TYPE))) {
				throw new CRECastException("Expecting an array at parameter 1 of explosion", t);
			}
			if(args.length >= 2) {
				if(!(args[1] instanceof CNull)) {
					size = ArgumentValidation.getInt(args[1], t);
					if(size > 100) {
						throw new CRERangeException("A bit excessive, don't you think? Let's scale that back some, huh?", t);
					}
				}
				if(args.length >= 3) {
					safe = ArgumentValidation.getBooleanObject(args[2], t);
					if(args.length >= 4) {
						fire = ArgumentValidation.getBooleanObject(args[3], t);
						if(args.length == 5) {
							source = Static.getEntity(args[4], t);
						}
					} else {
						fire = !safe;
					}
				}
			}

			MCWorld w = null;
			MCPlayer p = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if(p != null) {
				w = p.getWorld();
			}
			loc = ObjectGenerator.GetGenerator().location(args[0], w, t);
			w = loc.getWorld();

			return CBoolean.get(w.explosion(loc, size, safe, fire, source));
		}

		@Override
		public Boolean runAsync() {
			return false;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class play_note extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CRERangeException.class,
				CREFormatException.class, CREPlayerOfflineException.class};
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
		public Mixed exec(Target t, com.laytonsmith.core.environments.Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			MCInstrument i = null;
			MCNote n = null;
			MCLocation l;
			int instrumentOffset;
			int noteOffset;
			if(args.length == 2) {
				Static.AssertPlayerNonNull(p, t);
				instrumentOffset = 0;
				noteOffset = 1;
				l = p.getLocation();
			} else if(args.length == 4) {
				p = Static.GetPlayer(args[0], t);
				instrumentOffset = 1;
				noteOffset = 2;
				l = ObjectGenerator.GetGenerator().location(args[3], p.getWorld(), t);
			} else {
				if(!(args[1].isInstanceOf(CArray.TYPE)) && args[2].isInstanceOf(CArray.TYPE)) {
					//Player provided, location not
					instrumentOffset = 1;
					noteOffset = 2;
					p = Static.GetPlayer(args[0], t);
					l = p.getLocation();
				} else {
					//location provided, player not
					instrumentOffset = 0;
					noteOffset = 1;
					Static.AssertPlayerNonNull(p, t);
					l = ObjectGenerator.GetGenerator().location(args[2], p.getWorld(), t);
				}
			}
			try {
				i = MCInstrument.valueOf(args[instrumentOffset].val().toUpperCase().trim());
			} catch (IllegalArgumentException e) {
				throw new CREFormatException("Instrument provided is not a valid type, required one of: "
						+ StringUtils.Join(MCInstrument.values(), ", ", ", or "), t);
			}
			MCTone tone = null;
			if(args[noteOffset].isInstanceOf(CArray.TYPE)) {
				int octave = ArgumentValidation.getInt32(((CArray) args[noteOffset]).get("octave", t), t);
				if(octave < 0 || octave > 2) {
					throw new CRERangeException("The octave must be 0, 1, or 2, but was " + octave, t);
				}
				String ttone = ((CArray) args[noteOffset]).get("tone", t).val().toUpperCase().trim();
				try {
					tone = MCTone.valueOf(ttone.trim().replace("#", ""));
				} catch (IllegalArgumentException e) {
					throw new CREFormatException("Expected the tone parameter to be one of: "
							+ StringUtils.Join(MCTone.values(), ", ", ", or ") + " but it was " + ttone, t);
				}
				boolean sharped = false;
				if(ttone.trim().endsWith("#")) {
					sharped = true;
				}
				try {
					n = StaticLayer.GetConvertor().GetNote(octave, tone, sharped);
				} catch (IllegalArgumentException e) {
					throw new CREFormatException(e.getMessage(), t);
				}
			} else {
				throw new CRECastException("Expected an array for note parameter, but " + args[noteOffset] + " found instead", t);
			}
			Static.AssertPlayerNonNull(p, t);
			p.playNote(l, i, n);
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "play_note";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3, 4};
		}

		@Override
		public String docs() {
			return "void {[player], instrument, note, [locationArray]} Plays a note for the given player, at the given"
					+ " note block location. Player defaults to the current player, and location defaults to the"
					+ " player's location. Instrument may be one of: "
					+ StringUtils.Join(MCInstrument.values(), ", ", ", or ")
					+ ", and note is an associative array with 2 values, array(octave: 0, tone: 'F#') where octave is"
					+ " either 0, 1, or 2, and tone is one of the notes "
					+ StringUtils.Join(MCTone.values(), ", ", ", or ")
					+ ", optionally suffixed with a pound symbol, which denotes a sharp."
					+ " (Not all notes can be sharped.)";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}


	@api(environments = {CommandHelperEnvironment.class})
	public static class spawn_particle extends AbstractFunction {

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_3;
		}

		@Override
		public String getName() {
			return "spawn_particle";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public String docs() {
			return "void {location, particle[, players]} Spawns particles at the specified location. The players"
					+ " parameter can be one player or an array of players. If none is given, all players within 32"
					+ " meters will see the particle. The particle parameter can be a particle name or an associative"
					+ " array defining the characteristics of the particle to be spawned. The array requires the"
					+ " particle name under the key \"particle\". ----"
					+ " Possible particle types: " + StringUtils.Join(MCParticle.types(), ", ", ", or ", " or ") + "."
					+ " <br><br>Some particles have more specific keys and/or special behavior, but the common keys for"
					+ " the particle array are \"count\" (usually the number of particles to be spawned), \"speed\""
					+ " (usually the velocity of the particle), \"xoffset\", \"yoffset\", and \"zoffset\""
					+ " (usually the ranges from center within which the particle may be offset on that axis)."
					+ " <br><br>BLOCK_DUST, BLOCK_CRACK, BLOCK_CRUMBLE, BLOCK_MARKER, DUST_PILLAR, and FALLING_DUST"
					+ " particles can take a block type name parameter under the key \"block\" (default: STONE)."
					+ " <br>ITEM_CRACK particles can take an item array or name under the key \"item\" (default: STONE)."
					+ " <br>REDSTONE and SPELL_MOB particles take an RGB color array (each 0 - 255) or name under the key \"color\""
					+ " (defaults to RED for REDSTONE, WHITE for SPELL_MOB)."
					+ " <br>DUST_COLOR_TRANSITION particles take a \"tocolor\" in addition \"color\"."
					+ " <br>VIBRATION particles take a \"destination\" location array or entity UUID."
					+ " <br>SCULK_CHARGE particles take an \"angle\" in radians. (defaults to 0.0)"
					+ " <br>SHRIEK particles take an integer \"delay\" in ticks before playing. (defaults to 0)"
					+ " <br>TRAIL particles take a \"target\" location array, a \"color\", and a \"duration\" integer in ticks.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRERangeException.class, CRECastException.class, CREFormatException.class,
					CREPlayerOfflineException.class, CRELengthException.class};
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
		public Mixed exec(Target t, com.laytonsmith.core.environments.Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCLocation l = ObjectGenerator.GetGenerator().location(args[0], null, t);
			MCParticle p;
			int count = 0;
			double offsetX = 0.0;
			double offsetY = 0.0;
			double offsetZ = 0.0;
			double speed = 0.0;
			Object data = null;

			if(args[1].isInstanceOf(CArray.TYPE)) {
				CArray pa = (CArray) args[1];
				try {
					p = MCParticle.valueOf(pa.get("particle", t).val().toUpperCase());
				} catch (IllegalArgumentException ex) {
					throw new CREIllegalArgumentException("Particle name '" + pa.get("particle", t).val()
							+ "' is invalid.", t);
				}

				if(pa.containsKey("count")) {
					count = ArgumentValidation.getInt32(pa.get("count", t), t);
				}
				if(pa.containsKey("xoffset")) {
					offsetX = ArgumentValidation.getDouble(pa.get("xoffset", t), t) / 4.0D; // radius in approx. meters
				}
				if(pa.containsKey("yoffset")) {
					offsetY = ArgumentValidation.getDouble(pa.get("yoffset", t), t) / 4.0D;
				}
				if(pa.containsKey("zoffset")) {
					offsetZ = ArgumentValidation.getDouble(pa.get("zoffset", t), t) / 4.0D;
				}
				if(pa.containsKey("speed")) {
					speed = ArgumentValidation.getDouble(pa.get("speed", t), t);
				}

				data = ObjectGenerator.GetGenerator().particleData(p, l, pa, t);

			} else {
				try {
					p = MCParticle.valueOf(args[1].val().toUpperCase());
				} catch (IllegalArgumentException ex) {
					throw new CREIllegalArgumentException("Particle name '" + args[1].val() + "' is invalid.", t);
				}
			}

			try {
				if(args.length == 3) {
					MCPlayer player;
					if(args[2] instanceof CArray players) {
						if(players.isAssociative()) {
							throw new CREIllegalArgumentException("Players argument must be a normal array.", t);
						}
						for(Mixed playerName : players.asList()) {
							player = Static.GetPlayer(playerName, t);
							player.spawnParticle(l, p, count, offsetX, offsetY, offsetZ, speed, data);
						}
					} else {
						player = Static.GetPlayer(args[2], t);
						player.spawnParticle(l, p, count, offsetX, offsetY, offsetZ, speed, data);
					}
				} else {
					l.getWorld().spawnParticle(l, p, count, offsetX, offsetY, offsetZ, speed, data);
				}
			} catch (IllegalArgumentException ex) {
				throw new CREIllegalArgumentException("Given unsupported data for particle type " + p.name(), t);
			}
			return CVoid.VOID;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class play_sound extends AbstractFunction implements Optimizable {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInvalidWorldException.class,
				CRECastException.class, CREFormatException.class,
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
		public Mixed exec(Target t,
				com.laytonsmith.core.environments.Environment environment,
				Mixed... args) throws ConfigRuntimeException {

			MCLocation loc = null;
			MCEntity ent = null;
			if(args[0].isInstanceOf(CArray.TYPE)) {
				loc = ObjectGenerator.GetGenerator().location(args[0], null, t);
			} else if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_18_1)) {
				ent = Static.getEntity(args[0], t);
			} else {
				throw new CREFormatException("Expecting a location array on versions prior to MC 1.18.1", t);
			}

			MCSound sound;
			MCSoundCategory category = null;
			float volume = 1;
			float pitch = 1;
			Long seed = null;

			if(!(args[1].isInstanceOf(CArray.TYPE))) {
				throw new CREFormatException("An array was expected but received " + args[1], t);
			}

			CArray sa = (CArray) args[1];

			try {
				sound = MCSound.valueOf(sa.get("sound", t).val().toUpperCase());
			} catch (IllegalArgumentException iae) {
				MSLog.GetLogger().e(MSLog.Tags.GENERAL, "Sound name '" + sa.get("sound", t).val()
						+ "' is invalid.", t);
				return CVoid.VOID;
			}

			if(sa.containsKey("category")) {
				try {
					category = MCSoundCategory.valueOf(sa.get("category", t).val().toUpperCase());
				} catch (IllegalArgumentException iae) {
					throw new CREFormatException("Sound category '" + sa.get("category", t).val() + "' is invalid.", t);
				}
			}

			if(sa.containsKey("volume")) {
				volume = ArgumentValidation.getDouble32(sa.get("volume", t), t);
			}

			if(sa.containsKey("pitch")) {
				pitch = ArgumentValidation.getDouble32(sa.get("pitch", t), t);
			}

			if(sa.containsKey("seed") && Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_20_2)) {
				seed = ArgumentValidation.getInt(sa.get("seed", t), t);
			}

			if(args.length == 3) {
				if(args[2].isInstanceOf(CArray.TYPE)) {
					CArray players = (CArray) args[2];
					for(String key : players.stringKeySet()) {
						MCPlayer p = Static.GetPlayer(players.get(key, t), t);
						if(ent != null) {
							p.playSound(ent, sound, category, volume, pitch, seed);
						} else {
							p.playSound(loc, sound, category, volume, pitch, seed);
						}
					}
				} else {
					MCPlayer p = Static.GetPlayer(args[2], t);
					if(ent != null) {
						p.playSound(ent, sound, category, volume, pitch, seed);
					} else {
						p.playSound(loc, sound, category, volume, pitch, seed);
					}
				}
			} else if(loc == null) {
				ent.getWorld().playSound(ent, sound, category, volume, pitch, seed);
			} else {
				loc.getWorld().playSound(loc, sound, category, volume, pitch, seed);
			}
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "play_sound";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public String docs() {
			return "void {source, soundArray[, players]} Plays a sound at the given source."
					+ " Source can be a location array (or entity UUID on MC 1.18.1+)."
					+ " The soundArray is in an associative array with the keys:"
					+ " 'sound' 'category', 'volume' (float), 'pitch' (float), 'seed' (int; MC 1.20.2+)"
					+ " -- all are optional except sound."
					+ " Volume, if greater than 1.0 (default), controls the distance players can hear the sound."
					+ " Pitch has a clamped range of 0.5 - 2.0, where where 1.0 is the middle pitch and default."
					+ " Seed is an integer that determines which sound variant is played."
					+ " Players can be a single player or an array of players to play the sound to, if not given,"
					+ " all players can potentially hear it. ---- Possible categories: "
					+ StringUtils.Join(MCSoundCategory.values(), ", ", ", or ", " or ") + "."
					+ " \n\nPossible sounds:\n" + StringUtils.Join(MCSound.values(), "<br>");
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ParseTree optimizeDynamic(Target t, com.laytonsmith.core.environments.Environment env,
				Set<Class<? extends com.laytonsmith.core.environments.Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {

			if(children.size() < 2) {
				return null;
			}
			ParseTree child = children.get(1);
			if(child.getData() instanceof CFunction && (child.getData().val().equals(DataHandling.array.NAME)
					|| child.getData().val().equals(DataHandling.associative_array.NAME))) {
				for(ParseTree node : child.getChildren()) {
					if(node.getData() instanceof CFunction && node.getData().val().equals(Compiler.centry.NAME)) {
						children = node.getChildren();
						if(children.get(0).getData().val().equals("sound")
								&& children.get(1).getData().isInstanceOf(CString.TYPE)) {
							try {
								MCSound.MCVanillaSound.valueOf(children.get(1).getData().val().toUpperCase());
							} catch (IllegalArgumentException ex) {
								env.getEnv(CompilerEnvironment.class).addCompilerWarning(fileOptions,
										new CompilerWarning(children.get(1).getData().val()
												+ " is not a valid enum in com.commandhelper.Sound",
												children.get(1).getTarget(), null));
							}
						}
					}
				}
			}
			return null;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class play_named_sound extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInvalidWorldException.class,
				CRECastException.class, CREFormatException.class,
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
		public Mixed exec(Target t,
				com.laytonsmith.core.environments.Environment environment,
				Mixed... args) throws ConfigRuntimeException {

			MCLocation loc = null;
			MCEntity ent = null;
			if(args[0].isInstanceOf(CArray.TYPE)) {
				loc = ObjectGenerator.GetGenerator().location(args[0], null, t);
			} else if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_19_3)) {
				ent = Static.getEntity(args[0], t);
			} else {
				throw new CREFormatException("Expecting a location array on versions prior to MC 1.19.3", t);
			}

			String path;
			MCSoundCategory category = null;
			float volume = 1;
			float pitch = 1;
			Long seed = null;

			if(!(args[1].isInstanceOf(CArray.TYPE))) {
				throw new CREFormatException("An array was expected but received " + args[1], t);
			}

			CArray sa = (CArray) args[1];

			path = ArgumentValidation.getStringObject(sa.get("sound", t), t);

			if(sa.containsKey("category")) {
				try {
					category = MCSoundCategory.valueOf(sa.get("category", t).val().toUpperCase());
				} catch (IllegalArgumentException iae) {
					throw new CREFormatException("Sound category '" + sa.get("category", t).val() + "' is invalid.", t);
				}
			}

			if(sa.containsKey("volume")) {
				volume = ArgumentValidation.getDouble32(sa.get("volume", t), t);
			}

			if(sa.containsKey("pitch")) {
				pitch = ArgumentValidation.getDouble32(sa.get("pitch", t), t);
			}

			if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_20_2) && sa.containsKey("seed")) {
				seed = ArgumentValidation.getInt(sa.get("seed", t), t);
			}

			if(args.length == 3) {
				java.util.List<MCPlayer> players = new java.util.ArrayList<>();
				if(args[2].isInstanceOf(CArray.TYPE)) {
					for(String key : ((CArray) args[2]).stringKeySet()) {
						players.add(Static.GetPlayer(((CArray) args[2]).get(key, t), t));
					}
				} else {
					players.add(Static.GetPlayer(args[2], t));
				}

				try {
					for(MCPlayer p : players) {
						if(loc == null) {
							p.playSound(ent, path, category, volume, pitch, seed);
						} else {
							p.playSound(loc, path, category, volume, pitch, seed);
						}
					}
				} catch(Exception ex) {
					throw new CREFormatException(ex.getMessage(), t);
				}
			} else {
				try {
					if(loc == null) {
						ent.getWorld().playSound(ent, path, category, volume, pitch, seed);
					} else {
						loc.getWorld().playSound(loc, path, category, volume, pitch, seed);
					}
				} catch(Exception ex) {
					throw new CREFormatException(ex.getMessage(), t);
				}
			}
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "play_named_sound";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public String docs() {
			return "void {source, soundArray[, players]} Plays a sound at the given source."
					+ " Source can be a location array (or entity UUID on MC 1.19.3+)."
					+ " The soundArray is in an associative array with the keys:"
					+ " 'sound', 'category', 'volume' (float), 'pitch' (float), 'seed' (int; MC 1.20.2+)"
					+ " -- all are optional except sound."
					+ " Volume, if greater than 1.0 (default), controls the distance players can hear the sound."
					+ " Pitch has a clamped range of 0.5 - 2.0, where where 1.0 is the middle pitch and default."
					+ " Seed is an integer that determines which sound variant is played."
					+ " Players can be a single player or an array of players to play the sound to, if not given,"
					+ " all players can potentially hear it. Sound is a sound path, separated by periods."
					+ " ---- Possible categories: "
					+ StringUtils.Join(MCSoundCategory.values(), ", ", ", or ", " or ") + ".";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_block_info extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREFormatException.class};
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
		public Mixed exec(Target t, com.laytonsmith.core.environments.Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			MCLocation l = ObjectGenerator.GetGenerator().location(args[0], p == null ? null : p.getWorld(), t);
			MCBlock b = l.getBlock();
			if(args.length == 2) {
				switch(args[1].val()) {
					case "solid":
						return CBoolean.get(b.isSolid());
					case "flammable":
						return CBoolean.get(b.isFlammable());
					case "transparent":
						return CBoolean.get(b.isTransparent());
					case "occluding":
						return CBoolean.get(b.isOccluding());
					case "burnable":
						return CBoolean.get(b.isBurnable());
					default:
						throw new CREFormatException("Invalid argument for block info", t);
				}
			}
			CArray array = CArray.GetAssociativeArray(t);
			array.set("solid", CBoolean.get(b.isSolid()), t);
			array.set("flammable", CBoolean.get(b.isFlammable()), t);
			array.set("transparent", CBoolean.get(b.isTransparent()), t);
			array.set("occluding", CBoolean.get(b.isOccluding()), t);
			array.set("burnable", CBoolean.get(b.isBurnable()), t);
			return array;
		}

		@Override
		public String getName() {
			return "get_block_info";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "mixed {locationArray, [index]} Returns an associative array with various information about a block."
					+ " If an index is specified, it will return a boolean. ---- The accuracy of these values will"
					+ " depend on the server implementation."
					+ "<ul>"
					+ " <li>solid: If a block is solid (i.e. dirt or stone, as opposed to a torch or water)</li>"
					+ " <li>flammable: Indicates if a block can catch fire</li>"
					+ " <li>transparent: Indicates if light can pass through</li>"
					+ " <li>occluding: indicates If the block fully blocks vision</li>"
					+ " <li>burnable: Indicates if the block can burn away</li>"
					+ "</ul>";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_light_at extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREInvalidWorldException.class,
				CREFormatException.class};
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
		public String getName() {
			return "get_light_at";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "int {locationArray} Returns the combined light level at a block, taking into account all sources.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Mixed exec(Target t, com.laytonsmith.core.environments.Environment environment, Mixed... args)
				throws ConfigRuntimeException {
			MCWorld w = null;
			MCPlayer pl = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if(pl != null) {
				w = pl.getWorld();
			}
			MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], w, t);
			return new CInt(loc.getBlock().getLightLevel(), t);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_sky_light_at extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREInvalidWorldException.class,
					CREFormatException.class};
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
		public String getName() {
			return "get_sky_light_at";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "int {locationArray} Returns the sky light level for a location."
					+ " Disregards all block sources of light, where 15 is directly beneath the sky during the day.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_4;
		}

		@Override
		public Mixed exec(Target t, com.laytonsmith.core.environments.Environment environment, Mixed... args)
				throws ConfigRuntimeException {
			MCWorld w = null;
			MCPlayer pl = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if(pl != null) {
				w = pl.getWorld();
			}
			MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], w, t);
			return new CInt(loc.getBlock().getLightFromSky(), t);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_block_light_at extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREInvalidWorldException.class,
					CREFormatException.class};
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
		public String getName() {
			return "get_block_light_at";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "int {locationArray} Returns the block light level at a location."
					+ " This counts block sources like torches and lava, disregarding light from the sky.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_4;
		}

		@Override
		public Mixed exec(Target t, com.laytonsmith.core.environments.Environment environment, Mixed... args)
				throws ConfigRuntimeException {
			MCWorld w = null;
			MCPlayer pl = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if(pl != null) {
				w = pl.getWorld();
			}
			MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], w, t);
			return new CInt(loc.getBlock().getLightFromBlocks(), t);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class is_block_powered extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREInvalidWorldException.class,
				CREFormatException.class};
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
		public String getName() {
			return "is_block_powered";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "boolean {locationArray, [checkMode]} Returns whether or not a block is being supplied with power."
					+ "checkMode can be: \"BOTH\" (Check both direct and indirect power),"
					+ " \"DIRECT_ONLY\" (Check direct power only) or \"INDIRECT_ONLY\" (Check indirect power only)."
					+ " CheckMode defaults to \"BOTH\".";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Mixed exec(Target t, com.laytonsmith.core.environments.Environment environment, Mixed... args)
				throws ConfigRuntimeException {
			MCWorld w = null;
			MCPlayer pl = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if(pl != null) {
				w = pl.getWorld();
			}
			MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], w, t);
			CheckMode mode;
			if(args.length == 2) {
				try {
					mode = CheckMode.valueOf(args[1].val().toUpperCase());
				} catch (IllegalArgumentException e) {
					throw new CREFormatException("Invalid checkMode: " + args[1].val() + ".", t);
				}
			} else {
				mode = CheckMode.BOTH; // Default to BOTH to make it backwards compatible.
			}
			boolean ret;
			switch(mode) {
				case BOTH: {
					ret = loc.getBlock().isBlockPowered() || loc.getBlock().isBlockIndirectlyPowered();
					break;
				}
				case DIRECT_ONLY: {
					ret = loc.getBlock().isBlockPowered();
					break;
				}
				case INDIRECT_ONLY: {
					ret = loc.getBlock().isBlockIndirectlyPowered();
					break;
				}
				default: { // Should not be able to run.
					throw new CREFormatException("Invalid checkMode: " + args[1].val() + ".", t);
				}
			}
			return CBoolean.get(ret);
		}

		public enum CheckMode {
			BOTH,
			DIRECT_ONLY,
			INDIRECT_ONLY
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_block_power extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREInvalidWorldException.class,
				CREFormatException.class};
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
		public String getName() {
			return "get_block_power";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "int {locationArray} Returns the redstone power level that is supplied to this block [0-15]."
					+ " If is_block_powered(locationArray, 'DIRECT_ONLY') returns true, a redstone dust placed at the"
					+ " given location would be powered the return value - 1.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Mixed exec(Target t, com.laytonsmith.core.environments.Environment environment, Mixed... args)
				throws ConfigRuntimeException {
			MCWorld w = null;
			MCPlayer pl = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if(pl != null) {
				w = pl.getWorld();
			}
			MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], w, t);
			return new CInt(loc.getBlock().getBlockPower(), t);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class generate_tree extends AbstractFunction {

		@Override
		public String getName() {
			return "generate_tree";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREBadEntityException.class, CREFormatException.class};
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
			return "boolean {locationArray, [treeType]} Generates a tree at the given location and returns if the"
					+ " generation succeeded or not. The treeType can be "
					+ StringUtils.Join(MCTreeType.values(), ", ", ", or ", " or ")
					+ ", defaulting to TREE.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Mixed exec(Target t, com.laytonsmith.core.environments.Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCTreeType treeType;
			if(args.length == 1) {
				treeType = MCTreeType.TREE;
			} else {
				try {
					treeType = MCTreeType.valueOf(args[1].val().toUpperCase());
				} catch (IllegalArgumentException exception) {
					throw new CREFormatException("The tree type \"" + args[1].val() + "\" does not exist.", t);
				}
			}
			MCPlayer psender = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			MCLocation location = ObjectGenerator.GetGenerator().location(args[0], (psender != null ? psender.getWorld() : null), t);
			return CBoolean.get(location.getWorld().generateTree(location, treeType));
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class is_block_lockable extends AbstractFunction {

		@Override
		public String getName() {
			return "is_block_lockable";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
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
		public Boolean runAsync() {
			return false;
		}

		@Override
		public String docs() {
			return "boolean {locationArray} Returns whether or not the block is lockable."
					+ " ---- Locks prevent players from accessing the block's interface unless they are holding an item"
					+ " key with the same display name as the lock.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_4;
		}

		@Override
		public Mixed exec(Target t, com.laytonsmith.core.environments.Environment environment, Mixed... args)
				throws ConfigRuntimeException {
			MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], null, t);
			return CBoolean.get(loc.getBlock().getState().isLockable());
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class is_block_locked extends AbstractFunction {

		@Override
		public String getName() {
			return "is_block_locked";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREFormatException.class, CREInvalidWorldException.class,
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
		public String docs() {
			return "boolean {locationArray} Returns whether or not the block is locked if its lockable."
					+ " ---- Locks prevent players from accessing the block's interface unless they are holding an item"
					+ " key with the same display name as the lock."
					+ " Throws IllegalArgumentException if used on a block that isn't lockable.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_4;
		}

		@Override
		public Mixed exec(Target t, com.laytonsmith.core.environments.Environment environment, Mixed... args)
				throws ConfigRuntimeException {
			MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], null, t);
			MCBlockState block = loc.getBlock().getState();
			if(!block.isLockable()) {
				throw new CREIllegalArgumentException("Block is not lockable.", t);
			}
			return CBoolean.get(block.isLocked());
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_block_lock extends AbstractFunction {

		@Override
		public String getName() {
			return "get_block_lock";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREFormatException.class, CREInvalidWorldException.class,
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
		public String docs() {
			return "string {locationArray} Returns the string lock for this block if its lockable."
					+ " ---- Locks prevent players from accessing the block's interface unless they are holding an item"
					+ " key with the same display name as the lock."
					+ " Throws IllegalArgumentException if used on a block that isn't lockable.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_4;
		}

		@Override
		public Mixed exec(Target t, com.laytonsmith.core.environments.Environment environment, Mixed... args)
				throws ConfigRuntimeException {
			MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], null, t);
			MCBlockState block = loc.getBlock().getState();
			if(!block.isLockable()) {
				throw new CREIllegalArgumentException("Block is not lockable.", t);
			}
			return new CString(block.getLock(), t);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_block_lock extends AbstractFunction {

		@Override
		public String getName() {
			return "set_block_lock";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREFormatException.class, CREInvalidWorldException.class,
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
		public String docs() {
			return "void {locationArray, string} Sets the string lock for this block if its lockable."
					+ " Set an empty string or null to remove lock."
					+ " ---- Locks prevent players from accessing the block's interface unless they are holding an item"
					+ " key with the same display name as the lock."
					+ " Throws IllegalArgumentException if used on a block that isn't lockable.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_4;
		}

		@Override
		public Mixed exec(Target t, com.laytonsmith.core.environments.Environment environment, Mixed... args)
				throws ConfigRuntimeException {
			MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], null, t);
			MCBlockState block = loc.getBlock().getState();
			if(!block.isLockable()) {
				throw new CREIllegalArgumentException("Block is not lockable.", t);
			}
			block.setLock(Construct.nval(args[1]));
			return CVoid.VOID;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_block_command extends AbstractFunction {

		@Override
		public String getName() {
			return "get_block_command";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREFormatException.class};
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
			return "string {locationArray} Returns the command string in the Command Block at the given location.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Mixed exec(Target t, com.laytonsmith.core.environments.Environment environment, Mixed... args)
				throws ConfigRuntimeException {
			MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], null, t);
			if(loc.getBlock().isCommandBlock()) {
				MCCommandBlock cb = loc.getBlock().getCommandBlock();
				return new CString(cb.getCommand(), t);
			} else {
				throw new CREFormatException("The block at the specified location is not a command block", t);
			}
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_block_command extends AbstractFunction {

		@Override
		public String getName() {
			return "set_block_command";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREFormatException.class};
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
			return "void {locationArray, [cmd]} Sets a command to a Command Block at the given location."
					+ "If no command is given or parameter is null, it clears the Command Block.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Mixed exec(Target t, com.laytonsmith.core.environments.Environment environment, Mixed... args)
				throws ConfigRuntimeException {
			String cmd = null;
			if(args.length == 2 && !(args[1] instanceof CNull)) {
				if(!(args[1].isInstanceOf(CString.TYPE))) {
					throw new CRECastException("Parameter 2 of " + getName() + " must be a string or null", t);
				}
				cmd = args[1].val();
			}

			MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], null, t);
			if(loc.getBlock().isCommandBlock()) {
				MCCommandBlock cb = loc.getBlock().getCommandBlock();
				cb.setCommand(cmd);
				return CVoid.VOID;
			} else {
				throw new CREFormatException("The block at the specified location is not a command block", t);
			}
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_command_block_name extends AbstractFunction {

		@Override
		public String getName() {
			return "get_command_block_name";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREFormatException.class};
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
			return "string {locationArray} Returns the name of the Command Block at the given location.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Mixed exec(Target t, com.laytonsmith.core.environments.Environment environment, Mixed... args)
				throws ConfigRuntimeException {
			MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], null, t);
			if(loc.getBlock().isCommandBlock()) {
				MCCommandBlock cb = loc.getBlock().getCommandBlock();
				return new CString(cb.getName(), t);
			} else {
				throw new CREFormatException("The block at the specified location is not a command block", t);
			}
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_command_block_name extends AbstractFunction {

		@Override
		public String getName() {
			return "set_command_block_name";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREFormatException.class};
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
			return "void {locationArray, [name]} Sets the name of the Command Block at the given location."
					+ "If no name is given or name is null, the Command Block's name is reset to @.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Mixed exec(Target t, com.laytonsmith.core.environments.Environment environment, Mixed... args
		) throws ConfigRuntimeException {
			String name = null;
			if(args.length == 2 && !(args[1] instanceof CNull)) {
				if(!(args[1].isInstanceOf(CString.TYPE))) {
					throw new CRECastException("Parameter 2 of " + getName() + " must be a string or null", t);
				}
				name = args[1].val();
			}

			MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], null, t);
			if(loc.getBlock().isCommandBlock()) {
				MCCommandBlock cb = loc.getBlock().getCommandBlock();
				cb.setName(name);
				return CVoid.VOID;
			} else {
				throw new CREFormatException("The block at the specified location is not a command block", t);
			}
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_banner_patterns extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[] {CREFormatException.class};
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
		public Mixed exec(Target t, com.laytonsmith.core.environments.Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], null, t);
			MCBlock b = loc.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
			if(b.getState() instanceof MCBanner banner) {
				CArray patterns = new CArray(t, banner.numberOfPatterns());
				for(MCPattern p : banner.getPatterns()) {
					CArray pattern = CArray.GetAssociativeArray(t);
					pattern.set("color", p.getColor().name());
					pattern.set("shape", p.getShape().name());
					patterns.push(pattern, t);
				}
				return patterns;
			} else {
				throw new CREFormatException("Block at location isn't a banner.", t);
			}
		}

		@Override
		public String getName() {
			return "get_banner_patterns";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public CClassType getReturnType(Target t, List<CClassType> argTypes, List<Target> argTargets,
				com.laytonsmith.core.environments.Environment env, Set<ConfigCompileException> exceptions) {
			return CArray.TYPE;
		}


		@Override
		public String docs() {
			return "array {array location} Returns the banner pattern information for the standing banner at"
					+ " the given location. This will be an array of items containing the \"color\" and \"shape\""
					+ " parameters. ---- Valid colors are: " + StringUtils.Join(MCDyeColor.values(), ", ", ", or ")
					+ " and valid shapes are " + StringUtils.Join(MCPatternShape.values(), ", ", ", or ");
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}

	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_banner_patterns extends AbstractFunction implements Optimizable {

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
		public CClassType getReturnType(Target t, List<CClassType> argTypes, List<Target> argTargets, com.laytonsmith.core.environments.Environment env, Set<ConfigCompileException> exceptions) {
			return CVoid.TYPE;
		}

		@Override
		public Mixed exec(Target t, com.laytonsmith.core.environments.Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], null, t);
			MCBlock b = loc.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
			CArray patterns = ArgumentValidation.getArray(args[1], t);
			if(b.getState() instanceof MCBanner banner) {
				banner.clearPatterns();
				for(Mixed mp : patterns.asList()) {
					CArray p = ArgumentValidation.getArray(mp, t);
					MCDyeColor color;
					try {
						color = MCDyeColor.valueOf(p.get("color", t).val());
					} catch (IllegalArgumentException ex) {
						throw new CREFormatException("Invalid color name", t);
					}
					MCPatternShape shape;
					try {
						shape = MCPatternShape.valueOf(p.get("shape", t).val());
					} catch (IllegalArgumentException ex) {
						throw new CREFormatException("Invalid shape name", t);
					}
					MCPattern pattern = StaticLayer.GetConvertor().GetPattern(color, shape);

					banner.addPattern(pattern);
					banner.update();
				}

				return CVoid.VOID;
			} else {
				throw new CREFormatException("Block at location isn't a banner.", t);
			}
		}

		@Override
		public String getName() {
			return "set_banner_patterns";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "void {array location, array patterns} Overwrites the banner patterns for the standing banner at"
					+ " the given location. Patterns should be an array of associative arrays containing the keys"
					+ " \"color\" and \"shape\". ---- In vanilla Minecraft, only 6 patterns are allowed, however"
					+ " no such limit is enforced here directly."
					+ " Valid colors are: " + StringUtils.Join(MCDyeColor.values(), ", ", ", or ")
					+ " and valid shapes are " + StringUtils.Join(MCPatternShape.values(), ", ", ", or ");
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}

		@Override
		public ParseTree optimizeDynamic(Target t, com.laytonsmith.core.environments.Environment env,
				Set<Class<? extends com.laytonsmith.core.environments.Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {

			if(children.size() < 2) {
				return null;
			}
			ParseTree child = children.get(1);
			if(child.getData() instanceof CFunction && child.getData().val().equals(DataHandling.array.NAME)) {
				for(ParseTree node1 : child.getChildren()) {
					if(node1.getData() instanceof CFunction && (child.getData().val().equals(DataHandling.array.NAME)
							|| child.getData().val().equals(DataHandling.associative_array.NAME))) {
						for(ParseTree node : node1.getChildren()) {
							if(node.getData() instanceof CFunction && node.getData().val().equals(Compiler.centry.NAME)) {
								children = node.getChildren();
								if(children.get(0).getData().val().equals("color")
										&& children.get(1).getData().isInstanceOf(CString.TYPE)) {
									try {
										MCDyeColor.valueOf(children.get(1).getData().val());
									} catch (IllegalArgumentException ex) {
										env.getEnv(CompilerEnvironment.class).addCompilerWarning(fileOptions,
												new CompilerWarning(children.get(1).getData().val()
													+ " is not a valid enum in com.commandhelper.DyeColor",
														children.get(1).getTarget(), null));
									}
								} else if(children.get(0).getData().val().equals("shape")
										&& children.get(1).getData().isInstanceOf(CString.TYPE)) {
									try {
										MCPatternShape.valueOf(children.get(1).getData().val());
									} catch (IllegalArgumentException ex) {
										env.getEnv(CompilerEnvironment.class).addCompilerWarning(fileOptions,
												new CompilerWarning(children.get(1).getData().val()
													+ " is not a valid enum in com.commandhelper.PatternShape",
														children.get(1).getTarget(), null));
									}
								} else {
									List<String> validProps = Arrays.asList("color", "shape");
									String prop = children.get(0).getData().val();
									if(!validProps.contains(prop)) {
										env.getEnv(CompilerEnvironment.class).addCompilerWarning(fileOptions,
													new CompilerWarning("Unexpected entry, this will be ignored.",
															children.get(0).getTarget(), null));
									}
								}
							}
						}
					}
				}
			}
			return null;
		}

		@Override
		public Set<Optimizable.OptimizationOption> optimizationOptions() {
			return EnumSet.of(Optimizable.OptimizationOption.OPTIMIZE_DYNAMIC);
		}

	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class bone_meal_block extends AbstractFunction {

		@Override
		public String getName() {
			return "bone_meal_block";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {locationArray} Applies bone meal to a block, if possible."
					+ " Returns true if it was successfully applied, as some block types cannot be bone-mealed.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREFormatException.class, CREInvalidWorldException.class,
					CRERangeException.class};
		}

		@Override
		public Mixed exec(Target t, com.laytonsmith.core.environments.Environment environment, Mixed... args)
				throws ConfigRuntimeException {
			MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], null, t);
			return CBoolean.get(loc.getBlock().applyBoneMeal());
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
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
	public static class get_decorated_pot_sherds extends AbstractFunction {

		@Override
		public String getName() {
			return "get_decorated_pot_sherds";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
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
		public Boolean runAsync() {
			return false;
		}

		@Override
		public String docs() {
			return "array {locationArray} Returns an associative array of sherds for each side of a decorated pot."
					+ " The keys for each side are 'front', 'back', 'left', and 'right'."
					+ " The values are one of any sherd materials or a brick if there's no decoration on that side.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}

		@Override
		public Mixed exec(Target t, com.laytonsmith.core.environments.Environment environment, Mixed... args)
				throws ConfigRuntimeException {
			MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], null, t);
			MCBlockState bs = loc.getBlock().getState();
			if(bs instanceof MCDecoratedPot decoratedPot) {
				CArray sherds = CArray.GetAssociativeArray(t);
				Map<MCDecoratedPot.Side, MCMaterial> potSherds = decoratedPot.getSherds();
				for(Map.Entry<MCDecoratedPot.Side, MCMaterial> side : potSherds.entrySet()) {
					sherds.set(side.getKey().name().toLowerCase(), side.getValue().name());
				}
				return sherds;
			} else {
				throw new CREFormatException("The block at the specified location is not a decorated pot", t);
			}
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_decorated_pot_sherds extends AbstractFunction {

		@Override
		public String getName() {
			return "set_decorated_pot_sherds";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
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
		public Boolean runAsync() {
			return false;
		}

		@Override
		public String docs() {
			return "void {locationArray, array} Set the sherds on the sides of a decorated pot."
					+ " Takes an associative array with a key for each side: 'front', 'back', 'left', and 'right'."
					+ " Each side accepts a sherd material name, or if no decoration is desired on that side, a brick."
					+ " Throws a FormatException if a side or material is not valid.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}

		@Override
		public Mixed exec(Target t, com.laytonsmith.core.environments.Environment environment, Mixed... args)
				throws ConfigRuntimeException {
			MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], null, t);
			MCBlockState bs = loc.getBlock().getState();
			if(!(bs instanceof MCDecoratedPot decoratedPot)) {
				throw new CREFormatException("The block at the specified location is not a decorated pot", t);
			}
			Mixed sherds = args[1];
			if(sherds.isInstanceOf(CArray.TYPE)) {
				CArray sherdArray = (CArray) sherds;
				if(sherdArray.isAssociative()) {
					for(String key : sherdArray.stringKeySet()) {
						MCDecoratedPot.Side side;
						try {
							side = MCDecoratedPot.Side.valueOf(key.toUpperCase());
						} catch (IllegalArgumentException ex) {
							throw new CREFormatException("Invalid decorated pot side: " + key, t);
						}
						try {
							decoratedPot.setSherd(side, MCMaterial.valueOf(sherdArray.get(key, t).val()));
						} catch (IllegalArgumentException ex) {
							throw new CREFormatException(ex.getMessage(), t);
						}
					}
					bs.update();
					return CVoid.VOID;
				}
			}
			throw new CREFormatException("Expected associative array for decorated pots.", t);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_end_gateway_exit extends AbstractFunction {

		@Override
		public String getName() {
			return "set_end_gateway_exit";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
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
		public Boolean runAsync() {
			return false;
		}

		@Override
		public String docs() {
			return "void {gatewayLocation, exitLocation, [exactTeleport]} Sets an end gateways teleport exit location."
					+ " The exit location must be null or a location array with the same world as the end gateway."
					+ " Optionally change whether the end gateway teleports to the exact location or finds a nearby one.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}

		@Override
		public Mixed exec(Target t, com.laytonsmith.core.environments.Environment environment, Mixed... args)
				throws ConfigRuntimeException {
			MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], null, t);
			MCBlockState bs = loc.getBlock().getState();
			if(!(bs instanceof MCEndGateway endGateway)) {
				throw new CREFormatException("The block at the specified location is not an end gateway", t);
			}
			if(args[1] instanceof CNull) {
				endGateway.setExitLocation(null);
			} else {
				MCLocation exit = ObjectGenerator.GetGenerator().location(args[1], loc.getWorld(), t);
				try {
					endGateway.setExitLocation(exit);
				} catch (IllegalArgumentException ex) {
					throw new CREInvalidWorldException(ex.getMessage(), t);
				}
			}
			if(args.length == 3) {
				boolean exact = ArgumentValidation.getBooleanObject(args[2], t);
				endGateway.setExactTeleport(exact);
			}
			endGateway.update();
			return CVoid.VOID;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_end_gateway_age extends AbstractFunction {

		@Override
		public String getName() {
			return "set_end_gateway_age";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
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
		public Boolean runAsync() {
			return false;
		}

		@Override
		public String docs() {
			return "void {gatewayLocation, ticks} Sets an end gateway's age in ticks."
					+ " A magenta beam is emitted from 0 to 200 ticks (10 seconds)."
					+ " Also a purple beam is emitted very 2400 positive ticks (2 minutes)."
					+ " Setting this to a negative number, like math_const('LONG_MIN'), can be used to prevent beams.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}

		@Override
		public Mixed exec(Target t, com.laytonsmith.core.environments.Environment environment, Mixed... args)
				throws ConfigRuntimeException {
			MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], null, t);
			MCBlockState bs = loc.getBlock().getState();
			if(!(bs instanceof MCEndGateway endGateway)) {
				throw new CREFormatException("The block at the specified location is not an end gateway", t);
			}
			endGateway.setAge(ArgumentValidation.getInt(args[1], t));
			endGateway.update();
			return CVoid.VOID;
		}
	}
}
