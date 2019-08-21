package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.MCCreatureSpawner;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCOfflinePlayer;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCPlugin;
import com.laytonsmith.abstraction.MCPluginManager;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.enums.MCEffect;
import com.laytonsmith.abstraction.enums.MCEntityType;
import com.laytonsmith.annotations.api;
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
import com.laytonsmith.core.events.drivers.ServerEvents;
import com.laytonsmith.core.exceptions.CRE.CREBadEntityException;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CREIllegalArgumentException;
import com.laytonsmith.core.exceptions.CRE.CRENotFoundException;
import com.laytonsmith.core.exceptions.CRE.CREPlayerOfflineException;
import com.laytonsmith.core.exceptions.CRE.CRERangeException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Minecraft {

	public static String docs() {
		return "These functions provide a hook into game functionality.";
	}

	private static final SortedMap<String, Mixed> DATA_VALUE_LOOKUP = new TreeMap<>();

	static {
		Properties p1 = new Properties();
		try {
			p1.load(Minecraft.class.getResourceAsStream("/data_values.txt"));
			Enumeration e = p1.propertyNames();
			while(e.hasMoreElements()) {
				String name = e.nextElement().toString();
				DATA_VALUE_LOOKUP.put(name, new CString(p1.getProperty(name), Target.UNKNOWN));
			}
		} catch (IOException ex) {
			Logger.getLogger(Minecraft.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@api
	public static class data_values extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "data_values";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			if(args[0].isInstanceOf(CInt.TYPE)) {
				return new CInt(Static.getInt(args[0], t), t);
			}
			String c = args[0].val();
			MCMaterial mat = StaticLayer.GetMaterial("LEGACY_" + c.toUpperCase());
			if(mat != null) {
				return new CInt(mat.getType(), t);
			}
			String changed = c;
			if(changed.contains(":")) {
				//Split on that, and reverse. Change wool:red to redwool
				String split[] = changed.split(":");
				if(split.length == 2) {
					changed = split[1] + split[0];
				}
			}
			//Remove anything that isn't a letter or a number
			changed = changed.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
			//Do a lookup in the DataLookup table
			if(DATA_VALUE_LOOKUP.containsKey(changed)) {
				String split[] = DATA_VALUE_LOOKUP.get(changed).toString().split(":");
				if(split[1].equals("0")) {
					return new CInt(split[0], t);
				}
				return new CString(split[0] + ":" + split[1], t);
			}
			return CNull.NULL;
		}

		@Override
		public String docs() {
			return "int {var1} Does a lookup to return the data value of a name. For instance, returns 1 for 'stone'."
					+ " If the data value cannot be found, null is returned.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
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

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {
			MSLog.GetLogger().w(MSLog.Tags.DEPRECATION, "data_values() is deprecated.", t);
			return null;
		}

		@Override
		public Set<Optimizable.OptimizationOption> optimizationOptions() {
			return EnumSet.of(Optimizable.OptimizationOption.OPTIMIZE_DYNAMIC);
		}
	}

	@api
	public static class data_name extends AbstractFunction {

		@Override
		public String getName() {
			return "data_name";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "string {item} Returns a modern material for the a legacy item id, name, notation, or array."
					+ " If an invalid argument is passed in, null is returned."
					+ " Given 1 or '1:0', returns 'STONE'."
					+ " Given an item array with {name: STONE, data: 1}, returns 'GRANITE'.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREFormatException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
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
			int i = -1;
			int i2 = -1;
			if(args[0].isInstanceOf(CString.TYPE)) {
				//We also accept item notation
				if(args[0].val().contains(":")) {
					String[] split = args[0].val().split(":");
					try {
						i = Integer.parseInt(split[0]);
						i2 = Integer.parseInt(split[1]);
					} catch (NumberFormatException e) {
					} catch (ArrayIndexOutOfBoundsException e) {
						throw new CREFormatException("Incorrect format for the item notation: " + args[0].val(), t);
					}
				}
			} else if(args[0].isInstanceOf(CArray.TYPE)) {
				MCItemStack is = ObjectGenerator.GetGenerator().item(args[0], t, true);
				return new CString(is.getType().getName(), t);
			}
			if(i == -1) {
				try {
					i = Static.getInt32(args[0], t);
				} catch (CRECastException ex) {
					// possibly a material name
					MCMaterial mat = StaticLayer.GetMaterialFromLegacy(args[0].val(), 0);
					if(mat == null) {
						return CNull.NULL;
					}
					return new CString(mat.getName(), t);
				}
			}
			if(i2 == -1) {
				i2 = 0;
			}
			try {
				return new CString(StaticLayer.GetMaterialFromLegacy(i, i2).getName(), t);
			} catch (NullPointerException e) {
				return CNull.NULL;
			}
		}
	}

	@api
	public static class convert_legacy_item extends AbstractFunction {

		@Override
		public String getName() {
			return "convert_legacy_item";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "array {itemArray} Converts old pre-1.13 item arrays to new item arrays."
					+ " Almost all item arrays will be converted successfully when passing them to a function that"
					+ " accepts item arrays. However, if the array is missing the 'type' key, several item types"
					+ " might not convert accurately due to name conflicts."
					+ " This function offers convenience and ensures better conversion accuracy."
					+ " Use this if you have item arrays stored in a database and want to convert them all at once."
					+ " Passing new item arrays to this function is not supported."
					+ " Conversions may not be supported in far future versions of Minecraft.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREFormatException.class, CRERangeException.class,
					CRENotFoundException.class};
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
			CArray item = Static.getArray(args[0], t);
			MCItemStack is = ObjectGenerator.GetGenerator().item(item, t, true);
			return ObjectGenerator.GetGenerator().item(is, t);
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_3;
		}
	}

	@api
	public static class max_stack_size extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "max_stack_size";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "integer {itemArray} Given an item array, returns the maximum allowed stack size."
					+ " This method will accept an item array like is returned from pinv().";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREFormatException.class, CRERangeException.class,
				CRENotFoundException.class};
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
			Mixed id = args[0];
			if(id.isInstanceOf(CArray.TYPE)) {
				MCItemStack is = ObjectGenerator.GetGenerator().item(id, t);
				return new CInt(is.getType().getMaxStackSize(), t);
			}
			// legacy
			int type;
			int data = 0;
			try {
				int separatorIndex = id.val().indexOf(':');
				if(separatorIndex != -1) {
					type = Integer.parseInt(id.val().substring(0, separatorIndex));
					data = Integer.parseInt(id.val().substring(separatorIndex + 1));
				} else {
					type = Integer.parseInt(id.val());
				}
			} catch (NumberFormatException e) {
				throw new CREFormatException("Invalid item notation: " + id.val(), t);
			}
			MCMaterial mat = StaticLayer.GetMaterialFromLegacy(type, data);
			if(mat == null) {
				throw new CRENotFoundException("A material type could not be found based on the given id.", t);
			}
			return new CInt(mat.getMaxStackSize(), t);
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {
			if(children.size() < 1) {
				return null;
			}
			if(children.get(0).getData().isInstanceOf(CString.TYPE) && children.get(0).getData().val().contains(":")
					|| ArgumentValidation.isNumber(children.get(0).getData())) {
				MSLog.GetLogger().w(MSLog.Tags.DEPRECATION, "Numeric ids are deprecated in max_stack_size()", t);
			}
			return null;
		}

		@Override
		public Set<Optimizable.OptimizationOption> optimizationOptions() {
			return EnumSet.of(Optimizable.OptimizationOption.OPTIMIZE_DYNAMIC);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class make_effect extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "make_effect";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public String docs() {
			return "void {locationArray, effect, [radius]} Plays the specified effect at the given location"
					+ " for all players within the radius (or 64 by default). The effect can be one of the following: "
					+ StringUtils.Join(MCEffect.values(), ", ", ", or ", " or ")
					+ ". Additional data can be supplied with the syntax EFFECT:DATA. The STEP_SOUND effect takes an"
					+ " int of a legacy block id, SMOKE takes an int as a direction (4 is upwards), and POTION_BREAK"
					+ " takes an int as a color.";
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
		public MSVersion since() {
			return MSVersion.V3_1_3;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
			MCLocation l = ObjectGenerator.GetGenerator().location(args[0], p == null ? null : p.getWorld(), t);
			MCEffect e;
			String preEff = args[1].val();
			int data = 0;
			int radius = 64;
			int index = preEff.indexOf(':');
			if(index != -1) {
				try {
					data = Integer.parseInt(preEff.substring(index + 1));
				} catch (NumberFormatException ex) {
					throw new CRECastException("Effect data expected an integer", t);
				}
				preEff = preEff.substring(0, index);
			}
			try {
				e = MCEffect.valueOf(preEff.toUpperCase());
			} catch (IllegalArgumentException ex) {
				MSLog.GetLogger().e(MSLog.Tags.GENERAL, "The effect type " + args[1].val() + " is not valid", t);
				return CVoid.VOID;
			}
			if(e.equals(MCEffect.STEP_SOUND)) {
				MCMaterial mat = StaticLayer.GetMaterialFromLegacy(data, 0);
				if(mat == null || !mat.isBlock()) {
					throw new CREFormatException("This effect requires a valid BlockID", t);
				}
			}
			if(args.length == 3) {
				radius = Static.getInt32(args[2], t);
			}
			l.getWorld().playEffect(l, e, data, radius);
			return CVoid.VOID;
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {

			if(children.size() < 2) {
				return null;
			}
			Mixed effect = children.get(1).getData();
			if(effect.isInstanceOf(CString.TYPE)) {
				String effectName = effect.val().split(":")[0].toUpperCase();
				try {
					MCEffect.valueOf(effectName);
				} catch (IllegalArgumentException ex) {
					MSLog.GetLogger().w(MSLog.Tags.DEPRECATION, "The effect type " + effectName + " is not valid", t);
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
	public static class get_server_info extends AbstractFunction {

		@Override
		public String getName() {
			return "get_server_info";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "mixed {[value]} Returns various information about server."
					+ "If value is set, it should be an integer of one of the following indexes,"
					+ " and only that information for that index will be returned."
					+ " ---- Otherwise if value is not specified (or is -1), it returns an array of"
					+ " information with the following pieces of information in the specified index: "
					+ "<ul><li>0 - Server name; the name of the server in server.properties.</li>"
					+ "<li>1 - API version; The version of the plugin API this server is implementing.</li>"
					+ "<li>2 - Server version; The bare version string of the server implementation.</li>"
					+ "<li>3 - Allow flight; If true, Minecraft's inbuilt anti fly check is disabled.</li>"
					+ "<li>4 - Allow nether; is true, the Nether dimension is enabled</li>"
					+ "<li>5 - Allow end; if true, the End is enabled</li>"
					+ "<li>6 - World container; The path to the world container.</li>"
					+ "<li>7 - Max player limit; returns the player limit.</li>"
					+ "<li>8 - Operators; An array of operators on the server.</li>"
					+ "<li>9 - Plugins; An array of plugins loaded by the server.</li>"
					+ "<li>10 - Online Mode; If true, users are authenticated with Mojang before login</li>"
					+ "<li>11 - Server port; Get the game port that the server runs on</li>"
					+ "<li>12 - Server IP; Get the IP that the server runs on</li>"
					+ "<li>13 - Uptime; The number of milliseconds the server has been running</li>"
					+ "<li>14 - gcmax; The maximum amount of memory that the Java virtual machine will attempt to use, in bytes</li>"
					+ "<li>15 - gctotal; The total amount of memory in the Java virtual machine, in bytes</li>"
					+ "<li>16 - gcfree; The amount of free memory in the Java Virtual Machine, in bytes</li>"
					+ "<li>17 - MOTD; The message displayed on the server list.</li></ul>";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CRERangeException.class, CRENotFoundException.class};
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
			return true;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			MCServer server = StaticLayer.GetServer();
			int index = -1;
			if(args.length == 0) {
				index = -1;
			} else if(args.length == 1) {
				index = Static.getInt32(args[0], t);
			}

			if(index < -1 || index > 16) {
				throw new CRERangeException(this.getName() + " expects the index to be between -1 and 16 (inclusive)", t);
			}

			ArrayList<Mixed> retVals = new ArrayList<>();

			if(index == 0 || index == -1) {
				//Server name
				retVals.add(new CString(server.getServerName(), t));
			}

			if(index == 1 || index == -1) {
				// API Version
				retVals.add(new CString(server.getAPIVersion(), t));
			}
			if(index == 2 || index == -1) {
				// Server Version
				retVals.add(new CString(server.getServerVersion(), t));
			}
			if(index == 3 || index == -1) {
				//Allow flight
				retVals.add(CBoolean.get(server.getAllowFlight()));
			}
			if(index == 4 || index == -1) {
				//Allow nether
				retVals.add(CBoolean.get(server.getAllowNether()));
			}
			if(index == 5 || index == -1) {
				//Allow end
				retVals.add(CBoolean.get(server.getAllowEnd()));
			}
			if(index == 6 || index == -1) {
				//World container
				retVals.add(new CString(server.getWorldContainer(), t));
			}
			if(index == 7 || index == -1) {
				//Max player limit
				retVals.add(new CInt(server.getMaxPlayers(), t));
			}
			if(index == 8 || index == -1) {
				//Array of op's
				CArray co = new CArray(t);
				List<MCOfflinePlayer> so = server.getOperators();
				for(MCOfflinePlayer o : so) {
					if(o == null) {
						continue;
					}
					CString os = new CString(o.getName(), t);
					co.push(os, t);
				}
				retVals.add(co);
			}
			if(index == 9 || index == -1) {
				//Array of plugins
				CArray co = new CArray(t);
				MCPluginManager plugManager = server.getPluginManager();
				if(plugManager == null) {
					throw new CRENotFoundException(this.getName()
							+ " could not receive the server plugins. Are you running in cmdline mode?", t);
				}
				List<MCPlugin> plugs = plugManager.getPlugins();
				for(MCPlugin p : plugs) {
					if(p == null) {
						continue;
					}
					CString name = new CString(p.getName(), t);
					co.push(name, t);
				}

				retVals.add(co);
			}
			if(index == 10 || index == -1) {
				//Online Mode
				retVals.add(CBoolean.get(server.getOnlineMode()));
			}
			if(index == 11 || index == -1) {
				//Server port
				retVals.add(new CInt(server.getPort(), t));
			}
			if(index == 12 || index == -1) {
				//Server Ip
				retVals.add(new CString(server.getIp(), t));
			}
			if(index == 13 || index == -1) {
				//Uptime
				long uptime = System.currentTimeMillis() - ManagementFactory.getRuntimeMXBean().getStartTime();
				retVals.add(new CInt(uptime, t));
			}
			if(index == 14 || index == -1) {
				//gcmax
				retVals.add(new CInt((Runtime.getRuntime().maxMemory()), t));
			}
			if(index == 15 || index == -1) {
				//gctotal
				retVals.add(new CInt((Runtime.getRuntime().totalMemory()), t));
			}
			if(index == 16 || index == -1) {
				//gcfree
				retVals.add(new CInt((Runtime.getRuntime().freeMemory()), t));
			}
			if(index == 17 || index == -1) {
				//motd
				retVals.add(new CString(server.getMotd(), t));
			}

			if(retVals.size() == 1) {
				return retVals.get(0);
			}

			CArray ca = new CArray(t);
			for(Mixed c : retVals) {
				ca.push(c, t);
			}
			return ca;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_banned_players extends AbstractFunction {

		@Override
		public String getName() {
			return "get_banned_players";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "array {} An array of players banned on the server.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
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
			return true;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			MCServer server = StaticLayer.GetServer();
			CArray co = new CArray(t);
			List<MCOfflinePlayer> so = server.getBannedPlayers();
			for(MCOfflinePlayer o : so) {
				if(o == null) {
					continue;
				}
				CString os = new CString(o.getName(), t);
				co.push(os, t);
			}
			return co;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_whitelisted_players extends AbstractFunction {

		@Override
		public String getName() {
			return "get_whitelisted_players";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "array {} An array of players whitelisted on the server.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
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
			return true;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			MCServer server = StaticLayer.GetServer();
			CArray co = new CArray(t);
			List<MCOfflinePlayer> so = server.getWhitelistedPlayers();
			for(MCOfflinePlayer o : so) {
				if(o == null) {
					continue;
				}
				CString os = new CString(o.getName(), t);
				co.push(os, t);
			}
			return co;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_spawner_type extends AbstractFunction {

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
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCWorld w = null;
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if(p != null) {
				w = p.getWorld();
			}
			MCLocation location = ObjectGenerator.GetGenerator().location(args[0], w, t);
			if(location.getBlock().getState() instanceof MCCreatureSpawner) {
				String type = ((MCCreatureSpawner) location.getBlock().getState()).getSpawnedType().name();
				return new CString(type, t);
			} else {
				throw new CREFormatException("The block at " + location.toString() + " is not a spawner block", t);
			}
		}

		@Override
		public String getName() {
			return "get_spawner_type";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "string {locationArray} Gets the entity type that will spawn from the specified mob spawner.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_spawner_type extends AbstractFunction {

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
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCWorld w = null;
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if(p != null) {
				w = p.getWorld();
			}
			MCLocation location = ObjectGenerator.GetGenerator().location(args[0], w, t);
			MCEntityType type;
			try {
				type = MCEntityType.valueOf(args[1].val().toUpperCase());
			} catch (IllegalArgumentException iae) {
				throw new CREBadEntityException("Not a registered entity type: " + args[1].val(), t);
			}
			if(location.getBlock().getState() instanceof MCCreatureSpawner) {
				((MCCreatureSpawner) location.getBlock().getState()).setSpawnedType(type);
				return CVoid.VOID;
			} else {
				throw new CREFormatException("The block at " + location.toString() + " is not a spawner block", t);
			}
		}

		@Override
		public String getName() {
			return "set_spawner_type";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "void {locationArray, type} Sets the mob spawner's entity type at the location specified."
					+ " If the location is not a mob spawner, or if the type is invalid, a FormatException is thrown."
					+ " ---- The type may be one of either "
					+ StringUtils.Join(MCEntityType.MCVanillaEntityType.values(), ", ", ", or ");
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

	}

	@api
	public static class send_resourcepack extends AbstractFunction {

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
			MCPlayer p = Static.GetPlayer(args[0], t);
			p.sendResourcePack(args[1].val());
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "send_resourcepack";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "void {player, url} Sends a resourcepack URL to the player's client."
					+ " If the client has not been requested to change resources in the"
					+ " past, they will receive a confirmation dialog before downloading"
					+ " and switching to the new pack. Clients that ignore server resources"
					+ " will not receive the request, so this function will not affect them.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
	public static class get_ip_bans extends AbstractFunction {

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
			for(String ip : s.getIPBans()) {
				ret.push(new CString(ip, t), t);
			}
			return ret;
		}

		@Override
		public String getName() {
			return "get_ip_bans";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "array {} Returns an array of entries from banned-ips.txt.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
	public static class set_ip_banned extends AbstractFunction {

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
			String ip = args[0].val();
			if(ArgumentValidation.getBoolean(args[1], t)) {
				s.banIP(ip);
			} else {
				s.unbanIP(ip);
			}
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "set_ip_banned";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "void {address, banned} If banned is true, address is added to banned-ips.txt,"
					+ " if false, the address is removed.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
	public static class material_info extends AbstractFunction implements Optimizable {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREFormatException.class, CREIllegalArgumentException.class};
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCMaterial mat = StaticLayer.GetMaterial(args[0].val());
			if(mat == null) {
				try {
					mat = StaticLayer.GetMaterialFromLegacy(Static.getInt32(args[0], t), 0);
				} catch (CRECastException ex) {
					throw new CREIllegalArgumentException("Unable to get the material from: " + args[0].val(), t);
				}
			}
			if(args.length == 2) {
				switch(args[1].val()) {
					case "maxStacksize":
						return new CInt(mat.getMaxStackSize(), t);
					case "maxDurability":
						return new CInt(mat.getMaxDurability(), t);
					case "hasGravity":
						return CBoolean.get(mat.hasGravity());
					case "isBlock":
						return CBoolean.get(mat.isBlock());
					case "isBurnable":
						return CBoolean.get(mat.isBurnable());
					case "isEdible":
						return CBoolean.get(mat.isEdible());
					case "isFlammable":
						return CBoolean.get(mat.isFlammable());
					case "isOccluding":
						return CBoolean.get(mat.isOccluding());
					case "isRecord":
						return CBoolean.get(mat.isRecord());
					case "isSolid":
						return CBoolean.get(mat.isSolid());
					case "isTransparent":
						return CBoolean.get(mat.isTransparent());
					case "isInteractable":
						return CBoolean.get(mat.isInteractable());
					case "hardness":
						if(!mat.isBlock()) {
							throw new CREIllegalArgumentException(
									"Invalid trait \"hardness\" for non-block material " + mat.getName(), t);
						}
						return new CDouble(mat.getHardness(), t);
					case "blastResistance":
						if(!mat.isBlock()) {
							throw new CREIllegalArgumentException(
									"Invalid trait \"blastResistance\" for non-block material " + mat.getName(), t);
						}
						return new CDouble(mat.getBlastResistance(), t);
					default:
						throw new CREFormatException("Invalid argument for material_info: " + args[1].val(), t);
				}
			}
			CArray ret = CArray.GetAssociativeArray(t);
			ret.set("maxStacksize", new CInt(mat.getMaxStackSize(), t), t);
			ret.set("maxDurability", new CInt(mat.getMaxDurability(), t), t);
			ret.set("hasGravity", CBoolean.get(mat.hasGravity()), t);
			ret.set("isBlock", CBoolean.get(mat.isBlock()), t);
			ret.set("isBurnable", CBoolean.get(mat.isBurnable()), t);
			ret.set("isEdible", CBoolean.get(mat.isEdible()), t);
			ret.set("isFlammable", CBoolean.get(mat.isFlammable()), t);
			ret.set("isOccluding", CBoolean.get(mat.isOccluding()), t);
			ret.set("isRecord", CBoolean.get(mat.isRecord()), t);
			ret.set("isSolid", CBoolean.get(mat.isSolid()), t);
			ret.set("isTransparent", CBoolean.get(mat.isTransparent()), t);
			ret.set("isInteractable", CBoolean.get(mat.isInteractable()), t);
			if(mat.isBlock()) {
				ret.set("hardness", new CDouble(mat.getHardness(), t), t);
				ret.set("blastResistance", new CDouble(mat.getBlastResistance(), t), t);
			}
			return ret;
		}

		@Override
		public String getName() {
			return "material_info";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "mixed {material, [trait]} Returns an array of info about the material. If a trait is specified,"
					+ " it returns only that trait. Available traits: hasGravity, isBlock, isBurnable, isEdible,"
					+ " isFlammable, isOccluding, isRecord, isSolid, isTransparent, isInteractable, maxDurability,"
					+ " hardness (for block materials only), blastResistance (for block materials only),"
					+ " and maxStacksize. The accuracy of these values depend on the server implementation.";
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
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {
			if(children.size() < 1) {
				return null;
			}
			if(ArgumentValidation.isNumber(children.get(0).getData())) {
				MSLog.GetLogger().w(MSLog.Tags.DEPRECATION, "Numeric ids are deprecated in material_info()", t);
			}
			return null;
		}

		@Override
		public Set<Optimizable.OptimizationOption> optimizationOptions() {
			return EnumSet.of(Optimizable.OptimizationOption.OPTIMIZE_DYNAMIC);
		}
	}

	@api
	public static class shutdown_server extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "shutdown_server";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
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
		public Boolean runAsync() {
			return false;
		}

		@Override
		public String docs() {
			return "void {} Shuts down the minecraft server instance.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws CancelCommandException {
			Static.getServer().shutdown();
			throw new CancelCommandException("", t);
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.TERMINAL
			);
		}
	}

	@api
	public static class monitor_redstone extends AbstractFunction {

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
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCWorld world = null;
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if(p != null) {
				world = p.getWorld();
			}
			MCLocation location = ObjectGenerator.GetGenerator().location(args[0], world, t);
			boolean add = true;
			if(args.length > 1) {
				add = ArgumentValidation.getBoolean(args[1], t);
			}
			Map<MCLocation, Boolean> redstoneMonitors = ServerEvents.getRedstoneMonitors();
			if(add) {
				redstoneMonitors.put(location, location.getBlock().isBlockPowered());
			} else {
				redstoneMonitors.remove(location);
			}
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "monitor_redstone";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "void {location, [isMonitored]} Sets up a location to be monitored for redstone changes."
					+ " If a location is monitored, it will cause redstone_changed events to be triggered. By default,"
					+ " isMonitored is true, however, setting it to false will remove the previously monitored location"
					+ " from the list of monitors.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

	}

	@api
	public static class all_materials extends AbstractFunction {
		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return Boolean.FALSE;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			CArray mats = new CArray(t);
			for(MCMaterial mat : StaticLayer.GetMaterialValues()) {
				if(!mat.isLegacy()) {
					mats.push(new CString(mat.getName(), t), t);
				}
			}
			return mats;
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_4;
		}

		@Override
		public String getName() {
			return "all_materials";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "array {} Returns an array of all material names.";
		}
	}
}
