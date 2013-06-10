package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.StringUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.*;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.enums.MCCreeperType;
import com.laytonsmith.abstraction.enums.MCDyeColor;
import com.laytonsmith.abstraction.enums.MCEffect;
import com.laytonsmith.abstraction.enums.MCEntityType;
import com.laytonsmith.abstraction.enums.MCFireworkType;
import com.laytonsmith.abstraction.enums.MCMobs;
import com.laytonsmith.abstraction.enums.MCOcelotType;
import com.laytonsmith.abstraction.enums.MCPigType;
import com.laytonsmith.abstraction.enums.MCProfession;
import com.laytonsmith.abstraction.enums.MCSkeletonType;
import com.laytonsmith.abstraction.enums.MCWolfType;
import com.laytonsmith.abstraction.enums.MCZombieType;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.documentation;
import com.laytonsmith.annotations.typename;
import com.laytonsmith.core.*;
import com.laytonsmith.core.arguments.ArgList;
import com.laytonsmith.core.arguments.Argument;
import com.laytonsmith.core.arguments.ArgumentBuilder;
import com.laytonsmith.core.arguments.Generic;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.laytonsmith.core.natives.MItemStack;
import com.laytonsmith.core.natives.MLocation;
import com.laytonsmith.core.natives.annotations.FormatString;
import com.laytonsmith.core.natives.annotations.Ranged;
import com.laytonsmith.core.natives.interfaces.MObject;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Layton
 */
public class Minecraft {

	public static String docs() {
		return "These functions provide a hook into game functionality.";
	}
	private static final SortedMap<String, Construct> DataValueLookup = new TreeMap<String, Construct>();
	private static final SortedMap<String, Construct> DataNameLookup = new TreeMap<String, Construct>();

	static {
		Properties p1 = new Properties();
		try {
			p1.load(Minecraft.class.getResourceAsStream("/data_values.txt"));
			Enumeration e = p1.propertyNames();
			while (e.hasMoreElements()) {
				String name = e.nextElement().toString();
				DataValueLookup.put(name, new CString(p1.getProperty(name).toString(), Target.UNKNOWN));
			}
		} catch (IOException ex) {
			Logger.getLogger(Minecraft.class.getName()).log(Level.SEVERE, null, ex);
		}

		Properties p2 = new Properties();
		try {
			p2.load(Minecraft.class.getResourceAsStream("/data_names.txt"));
			Enumeration e = p2.propertyNames();
			while (e.hasMoreElements()) {
				String name = e.nextElement().toString();
				DataNameLookup.put(name, new CString(p2.getProperty(name).toString(), Target.UNKNOWN));
			}
		} catch (IOException ex) {
			Logger.getLogger(Minecraft.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@api
	public static class data_values extends AbstractFunction {

		public String getName() {
			return "data_values";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			if (args[0] instanceof CInt) {
				return new CInt(args[0].primitive(t).castToInt32(t), t);
			} else {
				String c = args[0].val();
				int number = StaticLayer.LookupItemId(c);
				if (number != -1) {
					return new CInt(number, t);
				}
				String changed = c;
				if (changed.contains(":")) {
					//Split on that, and reverse. Change wool:red to redwool
					String split[] = changed.split(":");
					if (split.length == 2) {
						changed = split[1] + split[0];
					}
				}
				//Remove anything that isn't a letter or a number
				changed = changed.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
				//Do a lookup in the DataLookup table
				if (DataValueLookup.containsKey(changed)) {
					String split[] = DataValueLookup.get(changed).toString().split(":");
					if (split[1].equals("0")) {
						return new CInt(Long.parseLong(split[0]), t);
					}
					return new CString(split[0] + ":" + split[1], t);
				}
				return null;
			}
		}

		public String docs() {
			return "Does a lookup to return the data value of a name. For instance, returns 1 for 'stone'. If an integer is given,"
					+ " simply returns that number. If the data value cannot be found, null is returned. Item names are drawn from"
					+ " an internally maintained list, which can be output by running the jar from command line with the 'data-values'"
					+ " option.";
		}
		
		public Argument returnType() {
			return new Argument("", CInt.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("The name of the item to look up", CString.class, "itemName")
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
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

	@api
	public static class data_name extends AbstractFunction {

		public String getName() {
			return "data_name";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "Performs the reverse functionality as data_values. Given 1, returns 'Stone'. Note that the enum value"
					+ " given in bukkit's Material class is what is returned as a fallback, if the id doesn't match a value in the internally maintained list."
					+ " If a completely invalid argument is passed"
					+ " in, null is returned.";
		}
		
		public Argument returnType() {
			return new Argument("", CString.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("Either an ItemStack, or just the integer type, or the old string notation", CString.class, CInt.class, MItemStack.class, "item")
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.FormatException};
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			int i = -1;
			int i2 = -1;
			if (args[0] instanceof CString) {
				//We also accept item notation
				if (args[0].val().contains(":")) {
					String[] split = args[0].val().split(":");
					try {
						i = Integer.parseInt(split[0]);
						i2 = Integer.parseInt(split[1]);
					} catch (NumberFormatException e) {
					} catch (ArrayIndexOutOfBoundsException e){
						throw new Exceptions.FormatException("Incorrect format for the item notation: " + args[0].val(), t);
					}
				}
			} else if (args[0] instanceof CArray) {
				MCItemStack is = ObjectGenerator.GetGenerator().item(args[0], t);
				i = is.getTypeId();
				i2 = (int) is.getData().getData();
			}
			if (i == -1) {
				i = args[0].primitive(t).castToInt32(t);
			}
			if (i2 == -1) {
				i2 = 0;
			}
			if (DataNameLookup.containsKey(i + "_" + i2)) {
				return DataNameLookup.get(i + "_" + i2);
			} else if (DataNameLookup.containsKey(i + "_0")) {
				return DataNameLookup.get(i + "_0");
			} else {
				try {
					return new CString(StaticLayer.LookupMaterialName(i), t);
				} catch (NullPointerException e) {
					return null;
				}
			}
		}
	}

	@api
	public static class max_stack_size extends AbstractFunction {

		public String getName() {
			return "max_stack_size";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "Given an item type, returns"
					+ " the maximum allowed stack size. This method will accept either"
					+ " a single data value (i.e. 278) or an item array like is returned"
					+ " from pinv(). Additionally, if a single value, it can also be in"
					+ " the old item notation (i.e. '35:11'), though for the purposes of this"
					+ " function, the data is unneccesary.";
		}
		
		public Argument returnType() {
			return new Argument("", CInt.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("Either an ItemStack, or just the integer type, or the old string notation", CString.class, CInt.class, MItemStack.class, "item")
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.FormatException};
		}

		public boolean isRestricted() {
			return false;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if (args[0] instanceof CArray) {
				MCItemStack is = ObjectGenerator.GetGenerator().item(args[0], t);
				return new CInt(is.getType().getMaxStackSize(), t);
			} else {
				String item = args[0].val();
				if (item.contains(":")) {
					String[] split = item.split(":");
					item = split[0];
				}
				try {
					int iitem = Integer.parseInt(item);
					int max = StaticLayer.GetItemStack(iitem, 1).getType().getMaxStackSize();
					return new CInt(max, t);
				} catch (NumberFormatException e) {
				}
			}
			throw new ConfigRuntimeException("Improper value passed to max_stack. Expecting a number, or an item array, but received \"" + args[0].val() + "\"", ExceptionType.CastException, t);
		}

		public CHVersion since() {
			return CHVersion.V3_3_0;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class spawn_mob extends AbstractFunction {

		public String getName() {
			return "spawn_mob";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3};
		}

		public String docs() {
			return "array {mobType, [qty], [location]} Spawns qty mob of one of the following types at location. qty defaults to 1, and location defaults"
					+ " to the location of the player. ---- mobType can be one of: " + StringUtils.Join(MCMobs.values(), ", ", ", or ", " or ") 
					+ ". Spelling matters, but capitalization doesn't. At this time, the function is limited to spawning a maximum of 50 at a time."
					+ " Further, subtypes can be applied by specifying MOBTYPE:SUBTYPE, for example the sheep subtype can be any of the dye colors: " 
					+ StringUtils.Join(MCDyeColor.values(), ", ", ", or ", " or ") + ". COLOR defaults to white if not specified. For mobs with multiple"
					+ " subtypes, separate each type with a \"-\", currently only zombies which, using ZOMBIE:TYPE1-TYPE2 can be any non-conflicting two of: "
					+ StringUtils.Join(MCZombieType.values(), ", ", ", or ", " or ") + ", but default to normal zombies. Ocelots may be one of: " 
					+ StringUtils.Join(MCOcelotType.values(), ", ", ", or ", " or ") + ", defaulting to the wild variety. Villagers can have a profession as a subtype: "
					+ StringUtils.Join(MCProfession.values(), ", ", ", or ", " or ") + ", defaulting to farmer if not specified. Skeletons can be "
					+ StringUtils.Join(MCSkeletonType.values(), ", ", ", or ", " or ") + ". PigZombies' subtype represents their anger,"
					+ " and accepts an integer, where 0 is neutral and 400 is the normal response to being attacked. Defaults to 0. Similarly, Slime"
					+ " and MagmaCube size can be set by integer, otherwise will be a random natural size. If a material is specified as the subtype"
					+ " for Endermen, they will hold that material, otherwise they will hold nothing. Creepers can be set to " 
					+ StringUtils.Join(MCCreeperType.values(), ", ", ", or ", " or ") + ", wolves can be " + StringUtils.Join(MCWolfType.values(), ", ", ", or ", " or ") 
					+ ", and pigs can be " + StringUtils.Join(MCPigType.values(), ", ", ", or ", " or ") + ". An array of the entity IDs spawned is returned.";
		}
		
		public Argument returnType() {
			return new Argument("", CArray.class).setGenerics(new Generic(CInt.class));
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("The mob type to spawn", CString.class, MCMobs.class, "mobType"),
						new Argument("The number of mobs to spawn", CInt.class, "qty").setOptionalDefault(1),
						new Argument("The location to spawn the mobs at", MLocation.class, "location").setOptionalDefaultNull()
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.RangeException, ExceptionType.FormatException};
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
			String mob = args[0].val();
			String secondary = "";
			if (mob.contains(":")) {
				secondary = mob.substring(mob.indexOf(':') + 1);
				mob = mob.substring(0, mob.indexOf(':'));
			}
			int qty = 1;
			if (args.length > 1) {
				qty = args[1].primitive(t).castToInt32(t);
			}
			if (qty > 50) {
				throw new ConfigRuntimeException("A bit excessive, don't you think? Let's scale that back some, huh?",
						ExceptionType.RangeException, t);
			}
			MCLocation l = null;
			if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
				l = env.getEnv(CommandHelperEnvironment.class).GetPlayer().getLocation();
			}
			if (args.length > 2) {
				if (args[2] instanceof CArray) {
					CArray ca = (CArray) args[2];
					l = ObjectGenerator.GetGenerator().location(ca, (l != null ? l.getWorld() : null), t);
				} else {
					throw new ConfigRuntimeException("Expected argument 3 to spawn_mob to be an array",
							ExceptionType.CastException, t);
				}
			}
			if (l.getWorld() != null) {
				try{
					return l.getWorld().spawnMob(MCMobs.valueOf(mob.toUpperCase().replaceAll(" ", "")), secondary, qty, l, t);
				} catch(IllegalArgumentException e){
					throw new ConfigRuntimeException("Invalid mob name: " + mob, ExceptionType.FormatException, t);
				}
			} else {
				throw new ConfigRuntimeException("World was not specified", ExceptionType.InvalidWorldException, t);
			}
		}
	}

	@api(environments={CommandHelperEnvironment.class})
	public static class tame_mob extends AbstractFunction {

		public String getName() {
			return "tame_mob";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "Tames the entity specified to the player. Wolves and ocelots are supported. Offline players"
					+ " are supported, but this means that partial matches are NOT supported. You must type the players name exactly. Setting"
					+ " the player to null will untame the mob. If the entity doesn't exist, nothing happens.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CString.class, "player").setOptionalDefaultNull(),
						new Argument("", CInt.class, "entityID")
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.UntameableMobException, ExceptionType.CastException, ExceptionType.BadEntityException};
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
			String player = environment.getEnv(CommandHelperEnvironment.class).GetPlayer().getName();
			Mixed entityID = null;
			if (args.length == 2) {
				if (args[0] == null) {
					player = null;
				} else {
					player = args[0].val();
				}
				entityID = args[1];
			} else {
				entityID = args[0];
			}
			int id = entityID.primitive(t).castToInt32(t);
			MCLivingEntity e = Static.getLivingEntity(id, t);
			if (e == null) {
				return new CVoid(t);
			} else if (e instanceof MCTameable) {
				MCTameable mct = ((MCTameable) e);
				if (player != null) {
					mct.setOwner(Static.getServer().getOfflinePlayer(player));
				} else {
					mct.setOwner(null);
				}
				return new CVoid(t);
			} else {
				throw new ConfigRuntimeException("The specified entity is not tameable", ExceptionType.UntameableMobException, t);
			}
		}
	}

	@api
	public static class get_mob_owner extends AbstractFunction {

		public String getName() {
			return "get_mob_owner";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "Returns the owner's name, or null if the mob is unowned. An UntameableMobException is thrown if"
					+ " mob isn't tameable to begin with.";
		}
		
		public Argument returnType() {
			return new Argument("", CString.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CInt.class, "entityID")
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.UntameableMobException, ExceptionType.CastException, ExceptionType.BadEntityException};
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
			int id = args[0].primitive(t).castToInt32(t);
			MCLivingEntity e = Static.getLivingEntity(id, t);
			if (e == null) {
				return null;
			} else if (e instanceof MCTameable) {
				MCAnimalTamer at = ((MCTameable) e).getOwner();
				if (null != at) {
					return new CString(at.getName(), t);
				} else {
					return null;
				}
			} else {
				throw new ConfigRuntimeException("The specified entity is not tameable", ExceptionType.UntameableMobException, t);
			}
		}
	}

	@api
	public static class is_tameable extends AbstractFunction {

		public String getName() {
			return "is_tameable";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "Returns true or false if the specified entity is tameable";
		}
		
		public Argument returnType() {
			return new Argument("", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CInt.class, "entityID")
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.BadEntityException};
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
			int id = args[0].primitive(t).castToInt32(t);
			MCEntity e = Static.getEntity(id, t);
			boolean ret;
			if (e == null) {
				ret = false;
			} else if (e instanceof MCTameable) {
				ret = true;
			} else {
				ret = false;
			}
			return new CBoolean(ret, t);
		}
	}

	@api(environments={CommandHelperEnvironment.class})
	public static class make_effect extends AbstractFunction {

		public String getName() {
			return "make_effect";
		}

		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		public String docs() {
			return "Plays the specified effect (sound effect) at the given location, for all players within"
					+ " the radius (or 64 by default). The effect can be one of the following: "
					+ StringUtils.Join(MCEffect.values(), ", ", ", or ", " or ")
					+ ". Additional data can be supplied with the syntax EFFECT:DATA. The RECORD_PLAY effect takes the item"
					+ " id of a disc as data, STEP_SOUND takes a blockID and SMOKE takes a direction bit (4 is upwards).";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", MLocation.class, "location"),
						new Argument("", MCEffect.class, "effect"),
						new Argument("", CInt.class, "radius").setOptionalDefault(64)
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.FormatException};
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
			MCLocation l = ObjectGenerator.GetGenerator().location(args[0], (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer ? env.getEnv(CommandHelperEnvironment.class).GetPlayer().getWorld() : null), t);
			MCEffect e = null;
			String preEff = args[1].val();
			int data = 0;
			int radius = 64;
			if (preEff.contains(":")) {
				try {
					data = Integer.parseInt(preEff.substring(preEff.indexOf(':') + 1));
				} catch (NumberFormatException ex) {
					throw new ConfigRuntimeException("Effect data expected an integer", ExceptionType.CastException, t);
				}
				preEff = preEff.substring(0, preEff.indexOf(':'));
			}
			try {
				e = MCEffect.valueOf(preEff.toUpperCase());
			} catch (IllegalArgumentException ex) {
				throw new ConfigRuntimeException("The effect type " + args[1].val() + " is not valid", ExceptionType.FormatException, t);
			}
			if (e.equals(MCEffect.STEP_SOUND) && (data < 0 || data > StaticLayer.GetConvertor().getMaxBlockID())) {
				throw new ConfigRuntimeException("This effect requires a valid BlockID", ExceptionType.FormatException, t);
			}
			if (args.length == 3) {
				radius = args[2].primitive(t).castToInt32(t);
			}
			l.getWorld().playEffect(l, e, data, radius);
			return new CVoid(t);
		}
	}

	@api
	public static class set_entity_health extends AbstractFunction {

		public String getName() {
			return "set_entity_health";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public String docs() {
			return "Sets the specified entity's health (0 kills it), or ignores this call if the entityID doesn't exist or isn't"
					+ "a LivingEntity.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CInt.class, "entityID"),
						new Argument("", CInt.class, "percentage").addAnnotation(new Ranged(0, 101))
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.BadEntityException};
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
			MCLivingEntity e = Static.getLivingEntity(args[0].primitive(t).castToInt32(t), t);
			int health = (int) ((double) args[1].primitive(t).castToInt32(t) / 100.0 * (double) e.getMaxHealth());
			if (health != 0) {
				e.setHealth(health);
			} else {
				e.damage(e.getMaxHealth() + 1); //Since mobs can now have custom max health
			}
			return new CVoid(t);
		}
	}

	@api
	public static class get_entity_health extends AbstractFunction {

		public String getName() {
			return "get_entity_health";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "int {entityID} Returns the entity's health, as a percentage. If the specified entity doesn't exist, or is not"
					+ " a LivingEntity, a format exception is thrown.";
		}
		
		public Argument returnType() {
			return new Argument("", CInt.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CInt.class, "entityID")
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.FormatException, ExceptionType.BadEntityException};
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
			MCLivingEntity e = Static.getLivingEntity(args[0].primitive(t).castToInt32(t), t);
			int h = (int) (((double) e.getHealth() / (double) e.getMaxHealth()) * 100);
			return new CInt(h, t);
		}
	}
	
	@typename("ServerInfo")
	public static class MServerInfo extends MObject {
		@documentation(docs="The name of the server in server.properties")
		public String serverName;		
		@documentation(docs="The Minecraft API version that is used in this build.")
		public String apiVersion;		
		@documentation(docs="The implementation API version that is used in this build.")
		public String implementationVersion;		
		@documentation(docs="If true, Minecraft's inbuilt anti fly check is enabled.")
		public boolean allowFlight;		
		@documentation(docs="If true, the Nether is enabled.")
		public boolean allowNether;		
		@documentation(docs="If true, the End is enabled.")
		public boolean allowEnd;		
		@documentation(docs="The path to the world container.")
		public String worldContainer;		
		@documentation(docs="The max player count that this server allows.")
		public int maxPlayerLimit;		
		@documentation(docs="A list of operators on the server.")
		public List<String> operators;		
		@documentation(docs="A list of plugins loaded by the server.")
		public List<String> plugins;
		@documentation(docs="If true, users are authenticated with Mojang before login.")
		public boolean onlineMode;

		@Override
		protected String alias(String field) {
			if("0".equals(field)){
				return "serverName";
			} else if("1".equals(field)){
				return "apiVersion";
			} else if("2".equals(field)){
				return "implementationVersion";
			} else if("3".equals(field)){
				return "allowFlight";
			} else if("4".equals(field)){
				return "allowNether";
			} else if("5".equals(field)){
				return "allowEnd";
			} else if("6".equals(field)){
				return "worldContainer";
			} else if("7".equals(field)){
				return "maxPlayerLimit";
			} else if("8".equals(field)){
				return "operators";
			} else if("9".equals(field)){
				return "plugins";
			} else if("10".equals(field)){
				return "onlineMode";
			} else {
				return null;
			}
		}
		
	}

	@api(environments={CommandHelperEnvironment.class})
	public static class get_server_info extends AbstractFunction {

		public String getName() {
			return "get_server_info";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		public String docs() {
			return "Returns various information about server.";
		}
		
		public Argument returnType() {
			return new Argument("", MServerInfo.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.NONE;
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.RangeException};
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		public Boolean runAsync() {
			return true;
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			MCServer server = env.getEnv(CommandHelperEnvironment.class).GetCommandSender().getServer();

			MServerInfo info = new MServerInfo();
			info.serverName = server.getServerName();
			info.apiVersion = server.getVersion();
			info.implementationVersion = server.getModVersion();
			info.allowFlight = server.getAllowFlight();
			info.allowNether = server.getAllowNether();
			info.allowEnd = server.getAllowEnd();
			info.worldContainer = server.getWorldContainer();
			info.maxPlayerLimit = server.getMaxPlayers();
			info.operators = new ArrayList<String>();
			for(MCOfflinePlayer o : server.getOperators()){
				info.operators.add(o.getName());
			}
			info.plugins = new ArrayList<String>();
			for(MCPlugin p: server.getPluginManager().getPlugins()){
				info.plugins.add(p.getName());
			}
			info.onlineMode = server.getOnlineMode();

			return info.deconstruct(t);
		}
	}

	@api(environments={CommandHelperEnvironment.class})
	public static class get_banned_players extends AbstractFunction {

		public String getName() {
			return "get_banned_players";
		}

		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		public String docs() {
			return "An array of players banned on the server.";
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
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		public Boolean runAsync() {
			return true;
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			MCServer server = env.getEnv(CommandHelperEnvironment.class).GetCommandSender().getServer();

			CArray co = new CArray(t);
			List<MCOfflinePlayer> so = server.getBannedPlayers();
			for (MCOfflinePlayer o : so) {
				if (o == null) {
					continue;
				}
				CString os = new CString(o.getName(), t);
				co.push(os);
			}
			return co;
		}
	}

	@api(environments={CommandHelperEnvironment.class})
	public static class get_whitelisted_players extends AbstractFunction {

		public String getName() {
			return "get_whitelisted_players";
		}

		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		public String docs() {
			return "An array of players whitelisted on the server.";
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
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		public Boolean runAsync() {
			return true;
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			MCServer server = env.getEnv(CommandHelperEnvironment.class).GetCommandSender().getServer();

			CArray co = new CArray(t);
			List<MCOfflinePlayer> so = server.getWhitelistedPlayers();
			for (MCOfflinePlayer o : so) {
				if (o == null) {
					continue;
				}
				CString os = new CString(o.getName(), t);
				co.push(os);
			}
			return co;
		}
	}
	
	@api(environments={CommandHelperEnvironment.class})
	public static class get_spawner_type extends AbstractFunction{

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.FormatException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCWorld w = null;
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if(p != null){
				w = p.getWorld();
			}
			MCLocation location = ObjectGenerator.GetGenerator().location(args[0], w, t);
			
			if(location.getBlock().getState() instanceof MCCreatureSpawner){
				String type = ((MCCreatureSpawner)location.getBlock().getState()).getSpawnedType().name();
				return new CString(type, t);
			} else {
				throw new Exceptions.FormatException("The block at " + location.toString() + " is not a spawner block", t);
			}
		}

		public String getName() {
			return "get_spawner_type";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "Gets the spawner type of the specified mob spawner."
					+ " Valid types will be one of the mob types.";
		}
		
		public Argument returnType() {
			return new Argument("", MCEntityType.class);
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
	
	@api(environments={CommandHelperEnvironment.class})
	public static class set_spawner_type extends AbstractFunction{

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.FormatException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCWorld w = null;
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if(p != null){
				w = p.getWorld();
			}
			MCLocation location = ObjectGenerator.GetGenerator().location(args[0], w, t);
			MCEntityType type = MCEntityType.valueOf(args[1].val().toUpperCase());
			if(location.getBlock().getState() instanceof MCCreatureSpawner){
				((MCCreatureSpawner)location.getBlock().getState()).setSpawnedType(type);
				return new CVoid(t);
			} else {
				throw new Exceptions.FormatException("The block at " + location.toString() + " is not a spawner block", t);
			}
		}

		public String getName() {
			return "set_spawner_type";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public String docs() {
			return "Sets the mob spawner type at the location specified."
					+ " If the location is not a mob spawner, a FormatException is thrown.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", MLocation.class, "location"),
						new Argument("The spawner type to create", MCEntityType.class, "type")
					);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}
	
	@api(environments={CommandHelperEnvironment.class})
	public static class launch_firework extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.FormatException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			MCWorld w = null;
			if(p != null){
				w = p.getWorld();
			}
			MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], w, t);
			CArray options = new CArray(t);
			if(args.length == 2){
				options = Static.getArray(args[1], t);
			}
			int strength = 2;
			boolean flicker = false;
			boolean trail = true;
			Set<MCColor> colors = new HashSet<MCColor>();
			colors.add(MCColor.WHITE);
			Set<MCColor> fade = new HashSet<MCColor>();
			MCFireworkType type = MCFireworkType.BALL;
			
			if(options.containsKey("strength")){
				strength = options.get("strength").primitive(t).castToInt32(t);
				if (strength < 0 || strength > 128) {
					throw new ConfigRuntimeException("Strength must be between 0 and 128", ExceptionType.RangeException, t);
				}
			}
			if(options.containsKey("flicker")){
				flicker = options.get("flicker").primitive(t).castToBoolean();
			}
			if(options.containsKey("trail")){
				trail = options.get("trail").primitive(t).castToBoolean();
			}
			if(options.containsKey("colors")){
				colors = parseColors(options.get("colors"), t);
			}
			if(options.containsKey("fade")){
				fade = parseColors(options.get("fade"), t);
			}
			if(options.containsKey("type")){
				try{
					type = MCFireworkType.valueOf(options.get("type").val().toUpperCase());
				} catch(IllegalArgumentException e){
					throw new Exceptions.FormatException("Invalid type: " + options.get("type").val(), t);
				}
			}
			
			MCFireworkBuilder fw = StaticLayer.GetConvertor().GetFireworkBuilder();
			fw.setStrength(strength);
			fw.setFlicker(flicker);
			fw.setTrail(trail);
			fw.setType(type);
			for(MCColor color : colors){
				fw.addColor(color);
			}
			
			for(MCColor color : fade){
				fw.addFadeColor(color);
			}
			
			return new CInt(fw.launch(loc), t);
		}
		
		private Set<MCColor> parseColors(Mixed c, Target t){
			Set<MCColor> colors = new HashSet<MCColor>();
			if(c instanceof CArray){
				CArray ca = ((CArray)c);
				if(ca.size() == 3
						&& ca.get(0) instanceof CInt
						&& ca.get(1) instanceof CInt
						&& ca.get(2) instanceof CInt
						){
					//It's a single custom color
					colors.add(parseColor(ca, t));
				} else {
					for(String key : ca.keySet()){
						Mixed val = ca.get(key);
						if(val instanceof CArray){
							colors.add(parseColor(((CArray)val), t));
						} else if(val instanceof CString){
							colors.addAll(parseColor(((CString)val), t));
						}
					}
				}
			} else if(c instanceof CString){
				colors.addAll(parseColor(((CString)c), t));
			}
			return colors;
		}
		
		private MCColor parseColor(CArray ca, Target t){
			return StaticLayer.GetConvertor().GetColor(
							ca.get(0).primitive(t).castToInt32(t), 
							ca.get(1).primitive(t).castToInt32(t), 
							ca.get(2).primitive(t).castToInt32(t)
						);
		}
		
		private List<MCColor> parseColor(CString cs, Target t){
			String split[] = cs.val().split("\\|");
			List<MCColor> colors = new ArrayList<MCColor>();
			for(String s : split){
				if(MCColor.STANDARD_COLORS.containsKey(s.toUpperCase())){
					 colors.add(MCColor.STANDARD_COLORS.get(s.toUpperCase()));
				} else {
					throw new Exceptions.FormatException("Unknown color type: " + s, t);
				}
			}
			return colors;
		}

		public String getName() {
			return "launch_firework";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "void {locationArray, [optionsArray]} Launches a firework. The location array specifies where it is launched from,"
					+ " and the options array is an associative array described below. All parameters in the associative array are"
					+ " optional, and default to the specified values if not set. The default options being set will make it look like"
					+ " a normal firework, with a white explosion. ----"
					+ " The options array may have the following keys:\n"
					+ "{| cellspacing=\"1\" cellpadding=\"1\" border=\"1\" class=\"wikitable\"\n"
					+ "! Array key !! Description !! Default\n"
					+ "|-\n"
					+ "| strength || A number specifying how far up the firework should go || 2\n"
					+ "|-\n"
					+ "| flicker || A boolean, determining if the firework will flicker\n || false\n"
					+ "|-\n"
					+ "| trail || A boolean, determining if the firework will leave a trail || true\n"
					+ "|-\n"
					+ "| colors || An array of colors, or a pipe seperated string of color names (for the named colors only)"
					+ " for instance: array('WHITE') or 'WHITE<nowiki>|</nowiki>BLUE'. If you want custom colors, you must use an array, though"
					+ " you can still use color names as an item in the array, for instance: array('ORANGE', array(30, 45, 150))."
					+ " These colors are used as the primary colors. || 'WHITE'\n"
					+ "|-\n"
					+ "| fade || An array of colors to be used as the fade colors. This parameter should be formatted the same as"
					+ " the colors parameter || array()\n"
					+ "|-\n"
					+ "| type || An enum value of one of the firework types, one of: " + StringUtils.Join(MCFireworkType.values(), ", ", " or ")
					+ " || " + MCFireworkType.BALL.name() + "\n"
					+ "|}\n"
					+ "The \"named colors\" can be one of: " + StringUtils.Join(MCColor.STANDARD_COLORS.keySet(), ", ", " or ");
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", MLocation.class, ""),
						new Argument("", CArray.class, "")
					);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}
	
	@api
	public static class send_texturepack extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.FormatException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, Environment environment,
				Mixed... args) throws ConfigRuntimeException {
			ArgList list = getBuilder().parse(args, this, t);
			String player = list.getStringWithNull("player", t);
			String url = list.getString("url", t);
			MCPlayer p = Static.GetPlayer(player, environment, t);
			p.sendTexturePack(url);
			return new CVoid(t);
		}

		public String getName() {
			return "send_texturepack";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public String docs() {
			return "void {player, url} Sends a texturepack URL to the player's client."
					+ " If the client has not been requested to change textures in the"
					+ " past, they will recieve a confirmation dialog before downloading"
					+ " and switching to the new pack. Clients that ignore server textures"
					+ " will not recieve the request, so this function will not affect them.";
		}

		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						PlayerManagement.PLAYER_ARG,
						new Argument("The url to send.", CString.class, "url")
					);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}	
	}
	
	@api
	public static class get_ip_bans extends AbstractFunction {

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
			MCServer s = Static.getServer();
			CArray ret = new CArray(t);
			for (String ip : s.getIPBans()) {
				ret.push(new CString(ip, t));
			}
			return ret;
		}

		public String getName() {
			return "get_ip_bans";
		}

		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		public String docs() {
			return "Returns an array of entries from banned-ips.txt.";
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
	
	@api
	public static class set_ip_banned extends AbstractFunction {

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
			ArgList list = getBuilder().parse(args, this, t);
			String address = list.getString("address", t);
			boolean banned = list.getBoolean("banned", t);
			MCServer s = Static.getServer();
			if (banned) {
				s.banIP(address);
			} else {
				s.unbanIP(address);
			}
			return new CVoid(t);
		}

		public String getName() {
			return "set_ip_banned";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public String docs() {
			return "If banned is true, address is added to banned-ips.txt,"
					+ " if false, the address is removed.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CString.class, "address"),
						new Argument("", CBoolean.class, "banned")
					);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}
	
	@api
	public static class material_info extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.FormatException};
		}

		public Construct exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCMaterial i = StaticLayer.GetConvertor().getMaterial(args[0].primitive(t).castToInt32(t));
			CArray ret = new CArray(t);
			ret.set("maxStacksize", new CInt(i.getMaxStackSize(), t), t);
			ret.set("maxDurability", new CInt(i.getMaxDurability(), t), t);
			ret.set("hasGravity", new CBoolean(i.hasGravity(), t), t);
			ret.set("isBlock", new CBoolean(i.isBlock(), t), t);
			ret.set("isBurnable", new CBoolean(i.isBurnable(), t), t);
			ret.set("isEdible", new CBoolean(i.isEdible(), t), t);
			ret.set("isFlammable", new CBoolean(i.isFlammable(), t), t);
			ret.set("isOccluding", new CBoolean(i.isOccluding(), t), t);
			ret.set("isRecord", new CBoolean(i.isRecord(), t), t);
			ret.set("isSolid", new CBoolean(i.isSolid(), t), t);
			ret.set("isTransparent", new CBoolean(i.isTransparent(), t), t);
			return ret;
		}

		public String getName() {
			return "material_info";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "Returns an array of info about the material.";
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Version since() {
			return CHVersion.V3_3_1;
		}

		public Argument returnType() {
			return new Argument("", CArray.class); //TODO: Change to an object
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(new Argument("", CInt.class, "materialID"));
		}
	}
}
