package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.MCColor;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCNote;
import com.laytonsmith.abstraction.MCOfflinePlayer;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCBlockData;
import com.laytonsmith.abstraction.blocks.MCBlockState;
import com.laytonsmith.abstraction.blocks.MCCommandBlock;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.blocks.MCSign;
import com.laytonsmith.abstraction.blocks.MCSkull;
import com.laytonsmith.abstraction.enums.MCBiomeType;
import com.laytonsmith.abstraction.enums.MCInstrument;
import com.laytonsmith.abstraction.enums.MCParticle;
import com.laytonsmith.abstraction.enums.MCSound;
import com.laytonsmith.abstraction.enums.MCSoundCategory;
import com.laytonsmith.abstraction.enums.MCTone;
import com.laytonsmith.abstraction.enums.MCTreeType;
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

import java.util.EnumSet;
import java.util.List;
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
			return "void {locationArray, lineArray | locationArray, line1, [line2, [line3, [line4]]]}"
					+ " Sets the text of the sign at the given location. If the block at x,y,z isn't a sign,"
					+ " a RangeException is thrown. If a text line cannot fit on the sign, it'll be cut off.";
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
			MCWorld w = null;
			MCCommandSender sender = environment.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			if(sender instanceof MCPlayer) {
				w = ((MCPlayer) sender).getWorld();
			}
			MCLocation l = ObjectGenerator.GetGenerator().location(args[0], w, t);
			if(l.getBlock().isSign()) {
				String line1 = "";
				String line2 = "";
				String line3 = "";
				String line4 = "";
				if(args.length == 2 && args[1].isInstanceOf(CArray.TYPE)) {
					CArray ca = (CArray) args[1];
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
				MCSign s = l.getBlock().getSign();
				s.setLine(0, line1);
				s.setLine(1, line2);
				s.setLine(2, line3);
				s.setLine(3, line4);
				return CVoid.VOID;
			} else {
				throw new CRERangeException("The block at the specified location is not a sign", t);
			}
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
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "array {locationArray} Given a location array, returns an array of 4 strings of the text in the sign"
					+ " at that location. If the location given isn't a sign, then a RangeException is thrown.";
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
			MCCommandSender sender = environment.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCWorld w = null;
			if(sender instanceof MCPlayer) {
				w = ((MCPlayer) sender).getWorld();
			}
			MCLocation l = ObjectGenerator.GetGenerator().location(args[0], w, t);
			if(l.getBlock().isSign()) {
				MCSign s = l.getBlock().getSign();
				CString line1 = new CString(s.getLine(0), t);
				CString line2 = new CString(s.getLine(1), t);
				CString line3 = new CString(s.getLine(2), t);
				CString line4 = new CString(s.getLine(3), t);
				return new CArray(t, line1, line2, line3, line4);
			} else {
				throw new CRERangeException("The block at the specified location is not a sign", t);
			}
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
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {locationArray} Returns true if the sign at this location has glowing text.";
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
			MCLocation l = ObjectGenerator.GetGenerator().location(args[0], w, t);
			if(l.getBlock().isSign()) {
				MCSign s = l.getBlock().getSign();
				return CBoolean.get(s.isGlowingText());
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
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "void {locationArray, isGlowing} Sets the text on a sign to be glowing or not.";
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
			MCLocation l = ObjectGenerator.GetGenerator().location(args[0], w, t);
			if(l.getBlock().isSign()) {
				MCSign s = l.getBlock().getSign();
				s.setGlowingText(ArgumentValidation.getBooleanObject(args[1], t));
				return CVoid.VOID;
			} else {
				throw new CRERangeException("The block at the specified location is not a sign", t);
			}
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
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "void {locationArray, owner}"
					+ " Sets the owner of the skull at the given location by name or uuid."
					+ " Supplying null will clear the skull owner, but due to limitations in Bukkit, clients will only"
					+ " see this change after reloading the block."
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
			MCWorld defaultWorld = null;
			MCCommandSender sender = environment.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			if(sender instanceof MCPlayer) {
				defaultWorld = ((MCPlayer) sender).getWorld();
			}
			MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], defaultWorld, t);
			MCBlock block = loc.getBlock();
			MCBlockState blockState = block.getState();
			if(blockState instanceof MCSkull) {
				MCSkull skull = (MCSkull) blockState;
				MCOfflinePlayer owner = (args[1] instanceof CNull ? null : Static.GetUser(args[1], t));
				skull.setOwningPlayer(owner);
				skull.update();
				return CVoid.VOID;
			} else {
				throw new CRERangeException("The block at the specified location is not a skull", t);
			}
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
					+ " {name: NAME, uuid: UUID}, or null if the skull does not have an owner. The value at the 'name'"
					+ " key will be an empty string if the server does not know the player's name."
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
			MCWorld defaultWorld = null;
			MCCommandSender sender = environment.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			if(sender instanceof MCPlayer) {
				defaultWorld = ((MCPlayer) sender).getWorld();
			}
			MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], defaultWorld, t);
			MCBlockState blockState = loc.getBlock().getState();
			if(blockState instanceof MCSkull) {
				MCSkull skull = (MCSkull) blockState;
				MCOfflinePlayer owner = skull.getOwningPlayer();
				if(owner == null) {
					return CNull.NULL;
				} else {
					CArray ret = new CArray(t);
					ret.set("name", owner.getName());
					ret.set("uuid", owner.getUniqueID().toString());
					return ret;
				}
			} else {
				throw new CRERangeException("The block at the specified location is not a skull", t);
			}
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
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "void {locationArray} Mostly simulates a block break at a location. Does not trigger an event.";
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
			MCLocation l;
			MCPlayer p;
			p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			MCWorld w = (p != null ? p.getWorld() : null);
			l = ObjectGenerator.GetGenerator().location(args[0], w, t);
			l.breakBlock();
			return CVoid.VOID;
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
			return "void {x, z, [world], biome | locationArray, biome} Sets the biome of the specified block column."
					+ " The location array's y value is ignored. ----"
					+ " Biome may be one of the following: " + StringUtils.Join(MCBiomeType.types(), ", ", ", or ");
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class, CRECastException.class,
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
			int x;
			int z;
			MCCommandSender sender = environment.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCWorld w = null;
			if(sender instanceof MCPlayer) {
				w = ((MCPlayer) sender).getWorld();
			}
			if(args.length == 2) {
				MCLocation l = ObjectGenerator.GetGenerator().location(args[0], w, t);
				x = l.getBlockX();
				z = l.getBlockZ();
				w = l.getWorld();
			} else {
				x = ArgumentValidation.getInt32(args[0], t);
				z = ArgumentValidation.getInt32(args[1], t);
				if(args.length != 3) {
					w = Static.getServer().getWorld(args[2].val());
				}
			}
			MCBiomeType bt;
			try {
				bt = MCBiomeType.valueOf(args[args.length - 1].val());
				if(bt == null) {
					throw new CRENotFoundException(
							"Could not find the internal biome type object (are you running in cmdline mode?)", t);
				}
			} catch (IllegalArgumentException e) {
				throw new CREFormatException("The biome type \"" + args[1].val() + "\" does not exist.", t);
			}
			if(w == null) {
				throw new CREInvalidWorldException("The specified world doesn't exist, or no world was provided", t);
			}
			w.setBiome(x, z, bt);
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

			if(children.size() < 1) {
				return null;
			}
			Mixed c = children.get(children.size() - 1).getData();
			if(c.isInstanceOf(CString.TYPE)) {
				try {
					MCBiomeType.valueOf(c.val());
				} catch (IllegalArgumentException ex) {
					env.getEnv(CompilerEnvironment.class).addCompilerWarning(fileOptions,
							new CompilerWarning(ex.getMessage(), t, null));
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
			return new Integer[]{1, 2, 3};
		}

		@Override
		public String docs() {
			return "string {x, z, [world] | locationArray} Returns the biome type of this block column. The location"
					+ " array's y value is ignored. ---- The value returned"
					+ " may be one of the following: " + StringUtils.Join(MCBiomeType.types(), ", ", ", or ");
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class, CRECastException.class,
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
		public Mixed exec(Target t, com.laytonsmith.core.environments.Environment environment, Mixed... args) throws ConfigRuntimeException {
			int x;
			int z;
			MCCommandSender sender = environment.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCWorld w = null;
			if(sender instanceof MCPlayer) {
				w = ((MCPlayer) sender).getWorld();
			}
			if(args.length == 1) {
				MCLocation l = ObjectGenerator.GetGenerator().location(args[0], w, t);
				x = l.getBlockX();
				z = l.getBlockZ();
				w = l.getWorld();
			} else {
				x = ArgumentValidation.getInt32(args[0], t);
				z = ArgumentValidation.getInt32(args[1], t);
				if(args.length != 2) {
					w = Static.getServer().getWorld(args[2].val());
				}
			}
			if(w == null) {
				throw new CREInvalidWorldException("The specified world doesn't exist, or no world was provided", t);
			}
			MCBiomeType bt = w.getBiome(x, z);
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
			double x = 0;
			double z = 0;
			MCWorld w = null;
			String world = null;
			MCCommandSender sender = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			if(sender instanceof MCPlayer) {
				w = ((MCPlayer) sender).getWorld();
			}

			if(args[0].isInstanceOf(CArray.TYPE) && !(args.length == 3)) {
				MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], w, t);
				x = loc.getX();
				z = loc.getZ();
				world = loc.getWorld().getName();
				if(args.length == 2) {
					world = args[1].val();
				}
			} else if(args.length == 2 || args.length == 3) {
				x = ArgumentValidation.getDouble(args[0], t);
				z = ArgumentValidation.getDouble(args[1], t);
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
			MCBlock highestBlock = w.getHighestBlockAt((int) java.lang.Math.floor(x), (int) java.lang.Math.floor(z));
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
			return new Integer[]{1, 2, 3};
		}

		@Override
		public String docs() {
			return "void {locationArray, [size], [safe]} Creates an explosion with a given size at a given location."
					+ " Size defaults to size of a creeper (3), and null uses the default. If safe is true,"
					+ " (defaults to false) the explosion won't hurt the surrounding blocks. If size is 0, and safe is"
					+ " true, you will still see the animation and hear the sound, but players won't be hurt, and"
					+ " neither will the blocks.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class, CRECastException.class, CRELengthException.class, CREInvalidWorldException.class};
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
			double x = 0;
			double y = 0;
			double z = 0;
			float size = 3;
			MCWorld w = null;
			MCPlayer m = null;
			boolean safe = false;

			if(args.length >= 3) {
				safe = ArgumentValidation.getBoolean(args[2], t);
			}
			if(args.length >= 2) {
				if(!(args[1] instanceof CNull)) {
					size = ArgumentValidation.getInt(args[1], t);
				}
			}

			if(size > 100) {
				throw new CRERangeException("A bit excessive, don't you think? Let's scale that back some, huh?", t);
			}

			if(!(args[0].isInstanceOf(CArray.TYPE))) {
				throw new CRECastException("Expecting an array at parameter 1 of explosion", t);
			}

			MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], w, t);
			w = loc.getWorld();
			x = loc.getX();
			z = loc.getZ();
			y = loc.getY();

			if(w == null) {
				if(!(env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer)) {
					throw new CREPlayerOfflineException(this.getName() + " needs a world in the location array,"
							+ " or a player so it can take the current world of that player.", t);
				}

				m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
				w = m.getWorld();
			}

			w.explosion(x, y, z, size, safe);
			return CVoid.VOID;
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
					tone = MCTone.valueOf(ttone.trim().replaceAll("#", ""));
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
					+ " Possible particle types: " + StringUtils.Join(MCParticle.types(), ", ", ", or ", " or ") + ".\n"
					+ " Some particles have more specific keys and/or special behavior, but the common keys for the"
					+ " particle array are \"count\" (usually the number of particles to be spawned), \"speed\""
					+ " (usually the velocity of the particle), \"xoffset\", \"yoffset\", and \"zoffset\""
					+ " (usually the ranges from center within which the particle may be offset on that axis).\n"
					+ " BLOCK_DUST, BLOCK_CRACK and FALLING_DUST particles can take a block type name parameter"
					+ " under the key \"block\" (default: STONE).\n"
					+ " ITEM_CRACK particles can take an item array or name under the key \"item\" (default: STONE).\n"
					+ " REDSTONE particles take an RGB color array (each 0 - 255) or name under the key \"color\""
					+ " (default: RED)."
					+ " DUST_COLOR_TRANSITION particles take a \"tocolor\" in addition \"color\"."
					+ " VIBRATION particles take a \"destination\" location array or entity UUID.";
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

				if(pa.containsKey("block")) {
					String value = pa.get("block", t).val();
					MCMaterial mat = StaticLayer.GetMaterial(value);
					if(mat != null) {
						try {
							data = mat.createBlockData();
						} catch (IllegalArgumentException ex) {
							throw new CREIllegalArgumentException(value + " is not a block.", t);
						}
					} else {
						throw new CREIllegalArgumentException("Could not find material from " + value, t);
					}

				} else if(pa.containsKey("item")) {
					Mixed value = pa.get("item", t);
					if(value.isInstanceOf(CArray.TYPE)) {
						data = ObjectGenerator.GetGenerator().item(pa.get("item", t), t);
					} else {
						MCMaterial mat = StaticLayer.GetMaterial(value.val());
						if(mat != null) {
							if(mat.isItem()) {
								data = StaticLayer.GetItemStack(mat, 1);
							} else {
								throw new CREIllegalArgumentException(value + " is not an item type.", t);
							}
						} else {
							throw new CREIllegalArgumentException("Could not find material from " + value, t);
						}
					}

				} else if(pa.containsKey("color")) {
					Mixed c = pa.get("color", t);
					MCColor color;
					if(c.isInstanceOf(CArray.TYPE)) {
						color = ObjectGenerator.GetGenerator().color((CArray) c, t);
					} else {
						color = StaticLayer.GetConvertor().GetColor(c.val(), t);
					}
					if(pa.containsKey("tocolor")) {
						Mixed sc = pa.get("tocolor", t);
						MCColor[] colors = new MCColor[2];
						colors[0] = color;
						if(sc.isInstanceOf(CArray.TYPE)) {
							colors[1] = ObjectGenerator.GetGenerator().color((CArray) sc, t);
						} else {
							colors[1] = StaticLayer.GetConvertor().GetColor(sc.val(), t);
						}
						data = colors;
					} else {
						data = color;
					}

				} else if(pa.containsKey("destination")) {
					Mixed d = pa.get("destination", t);
					if(d.isInstanceOf(CArray.TYPE)) {
						data = ObjectGenerator.GetGenerator().location(d, l.getWorld(), t);
					} else {
						data = Static.getEntity(d, t);
					}
				}

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
					if(args[2] instanceof CArray) {
						CArray players = (CArray) args[2];
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

			MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], null, t);
			MCSound sound;
			MCSoundCategory category = null;
			float volume = 1;
			float pitch = 1;

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

			if(args.length == 3) {
				java.util.List<MCPlayer> players = new java.util.ArrayList<MCPlayer>();
				if(args[2].isInstanceOf(CArray.TYPE)) {
					for(String key : ((CArray) args[2]).stringKeySet()) {
						players.add(Static.GetPlayer(((CArray) args[2]).get(key, t), t));
					}
				} else {
					players.add(Static.GetPlayer(args[2], t));
				}

				if(category == null) {
					for(MCPlayer p : players) {
						p.playSound(loc, sound, volume, pitch);
					}
				} else {
					for(MCPlayer p : players) {
						p.playSound(loc, sound, category, volume, pitch);
					}
				}

			} else if(category == null) {
				loc.getWorld().playSound(loc, sound, volume, pitch);
			} else {
				loc.getWorld().playSound(loc, sound, category, volume, pitch);
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
			return "void {locationArray, soundArray[, players]} Plays a sound at the"
					+ " given location. SoundArray is in an associative array with"
					+ " keys 'sound', 'category', 'volume', 'pitch', where all are optional except sound."
					+ " Volume, if greater than 1.0 (default), is the distance in chunks players can hear the sound."
					+ " Pitch has a range of 0.5 - 2.0, where where 1.0 is the middle pitch and default. Players can"
					+ " be a single player or an array of players to play the sound to, if"
					+ " not given, all players can potentially hear it. ---- Possible categories: "
					+ StringUtils.Join(MCSoundCategory.values(), ", ", ", or ", " or ") + "."
					+ " \n\nPossible sounds: " + StringUtils.Join(MCSound.types(), "<br>");
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
			if(child.getData() instanceof CFunction && child.getData().val().equals("array")) {
				for(ParseTree node : child.getChildren()) {
					if(node.getData() instanceof CFunction && node.getData().val().equals("centry")) {
						children = node.getChildren();
						if(children.get(0).getData().val().equals("sound")
								&& children.get(1).getData().isInstanceOf(CString.TYPE)) {
							try {
								MCSound.MCVanillaSound.valueOf(children.get(1).getData().val().toUpperCase());
							} catch (IllegalArgumentException ex) {
								env.getEnv(CompilerEnvironment.class).addCompilerWarning(fileOptions,
										new CompilerWarning(ex.getMessage(), t, null));
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

			MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], null, t);
			String path;
			MCSoundCategory category = null;
			float volume = 1;
			float pitch = 1;

			if(!(args[1].isInstanceOf(CArray.TYPE))) {
				throw new CREFormatException("An array was expected but received " + args[1], t);
			}

			CArray sa = (CArray) args[1];

			path = sa.get("sound", t).val();

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

			if(args.length == 3) {
				java.util.List<MCPlayer> players = new java.util.ArrayList<MCPlayer>();
				if(args[2].isInstanceOf(CArray.TYPE)) {
					for(String key : ((CArray) args[2]).stringKeySet()) {
						players.add(Static.GetPlayer(((CArray) args[2]).get(key, t), t));
					}
				} else {
					players.add(Static.GetPlayer(args[2], t));
				}

				if(category == null) {
					for(MCPlayer p : players) {
						p.playSound(loc, path, volume, pitch);
					}
				} else {
					for(MCPlayer p : players) {
						p.playSound(loc, path, category, volume, pitch);
					}
				}
			} else if(category == null) {
				loc.getWorld().playSound(loc, path, volume, pitch);
			} else {
				loc.getWorld().playSound(loc, path, category, volume, pitch);
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
			return "void {locationArray, soundArray[, players]} Plays a sound at the"
					+ " given location. SoundArray is in an associative array with"
					+ " keys 'sound', 'category', 'volume', 'pitch', where all are optional except sound."
					+ " Volume, if greater than 1.0 (default), is the distance in chunks players can hear the sound."
					+ " Pitch has a range of 0.5 - 2.0, where where 1.0 is the middle pitch and default. Players can"
					+ " be a single player or an array of players to play the sound to, if"
					+ " not given, all players can potentially hear it. Sound is"
					+ " a sound path, separated by periods. ---- Possible categories: "
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
}
