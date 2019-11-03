package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.MCAttributeModifier;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.MCAnimalTamer;
import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCEntityEquipment;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.entities.MCAgeable;
import com.laytonsmith.abstraction.entities.MCHorse;
import com.laytonsmith.abstraction.entities.MCTameable;
import com.laytonsmith.abstraction.enums.MCAttribute;
import com.laytonsmith.abstraction.enums.MCCreeperType;
import com.laytonsmith.abstraction.enums.MCDyeColor;
import com.laytonsmith.abstraction.enums.MCEquipmentSlot;
import com.laytonsmith.abstraction.enums.MCMobs;
import com.laytonsmith.abstraction.enums.MCOcelotType;
import com.laytonsmith.abstraction.enums.MCPigType;
import com.laytonsmith.abstraction.enums.MCPotionEffectType;
import com.laytonsmith.abstraction.enums.MCProfession;
import com.laytonsmith.abstraction.enums.MCWolfType;
import com.laytonsmith.abstraction.enums.MCZombieType;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.hide;
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
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CREBadEntityException;
import com.laytonsmith.core.exceptions.CRE.CREBadEntityTypeException;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CREIllegalArgumentException;
import com.laytonsmith.core.exceptions.CRE.CREInvalidWorldException;
import com.laytonsmith.core.exceptions.CRE.CRELengthException;
import com.laytonsmith.core.exceptions.CRE.CRENotFoundException;
import com.laytonsmith.core.exceptions.CRE.CREPlayerOfflineException;
import com.laytonsmith.core.exceptions.CRE.CRERangeException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.CRE.CREUnageableMobException;
import com.laytonsmith.core.exceptions.CRE.CREUntameableMobException;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MobManagement {

	public static String docs() {
		return "These functions manage specifically living entities. If the entity specified is not living, a"
				+ " BadEntityTypeException will be thrown.";
	}

	@api(environments = {CommandHelperEnvironment.class})
	@hide("Deprecated for spawn_entity().")
	public static class spawn_mob extends AbstractFunction implements Optimizable {

		// The max amount of mobs that can be spawned at once by this function.
		private static final int SPAWN_LIMIT = 10000;

		@Override
		public String getName() {
			return "spawn_mob";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3};
		}

		@Override
		public String docs() {
			return "array {mobType, [qty], [location]} Spawns qty mob of one of the following types at location."
					+ " qty defaults to 1, and location defaults to the location of the player."
					+ " An array of the entity UUIDs spawned is returned. (deprecated for {{function|spawn_entity}})"
					+ " ---- mobType can be one of: " + StringUtils.Join(MCMobs.values(), ", ", ", or ", " or ") + "."
					+ " Further, subtypes can be applied by specifying MOBTYPE:SUBTYPE,"
					+ " for example the sheep subtype can be any of the dye colors: "
					+ StringUtils.Join(MCDyeColor.values(), ", ", ", or ", " or ") + "."
					+ " COLOR defaults to white if not specified."
					+ " For mobs with multiple subtypes, separate each type with a \"-\"."
					+ " Zombies can be any non-conflicting two of: " + StringUtils.Join(MCZombieType.values(), ", ", ", or ", " or ") + "."
					+ " Ocelots may be one of: " + StringUtils.Join(MCOcelotType.values(), ", ", ", or ", " or ") + "."
					+ " Villagers can have a profession as a subtype: " + StringUtils.Join(MCProfession.values(), ", ", ", or ", " or ")
					+ ", defaulting to farmer if not specified. PigZombies' subtype represents their anger,"
					+ " and accepts an integer, where 0 is neutral and 400 is the normal response to being attacked."
					+ " Defaults to 0. Similarly, Slime and MagmaCube size can be set by integer,"
					+ " otherwise will be a random natural size. If a material is specified as the subtype for Endermen,"
					+ " they will hold that material, otherwise they will hold nothing."
					+ " Creepers can be set to " + StringUtils.Join(MCCreeperType.values(), ", ", ", or ", " or ") + "."
					+ " Wolves can be " + StringUtils.Join(MCWolfType.values(), ", ", ", or ", " or ") + "."
					+ " Pigs can be " + StringUtils.Join(MCPigType.values(), ", ", ", or ", " or ") + "."
					+ " Horses can have a color: " + StringUtils.Join(MCHorse.MCHorseColor.values(), ", ", ", or ", " or ") + ","
					+ " and a pattern: " + StringUtils.Join(MCHorse.MCHorsePattern.values(), ", ", ", or ", " or ") + "."
					+ " If qty is larger than " + spawn_mob.SPAWN_LIMIT + ", a RangeException will be thrown.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CRERangeException.class, CREFormatException.class,
				CREPlayerOfflineException.class, CREInvalidWorldException.class, CRENotFoundException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_1_2;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			String mob = args[0].val();
			String secondary = "";
			if(mob.contains(":")) {
				secondary = mob.substring(mob.indexOf(':') + 1);
				mob = mob.substring(0, mob.indexOf(':'));
			}
			int qty = 1;
			if(args.length > 1) {
				qty = Static.getInt32(args[1], t);
				if(qty > spawn_mob.SPAWN_LIMIT) {
					throw new CRERangeException("You can not spawn more than " + spawn_mob.SPAWN_LIMIT
							+ " mobs at once using the " + this.getName() + " function.", t);
				}
			}
			MCLocation l;
			MCPlayer p = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if(args.length == 3) {
				l = ObjectGenerator.GetGenerator().location(args[2], (p != null ? p.getWorld() : null), t);
			} else if(p != null) {
				l = p.getLocation();
			} else {
				throw new CREPlayerOfflineException("Invalid sender!", t);
			}

			if(l == null) { // Happends when executed by a fake player.
				throw new CRENotFoundException(
						"Could not find the location of the player (are you running in cmdline mode?)", t);
			}

			try {
				return l.getWorld().spawnMob(MCMobs.valueOf(mob.toUpperCase().replaceAll("[ _]", "")), secondary, qty, l, t);
			} catch (IllegalArgumentException e) {
				throw new CREFormatException("Invalid mob name: " + mob, t);
			}
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {
			env.getEnv(CompilerEnvironment.class).addCompilerWarning(fileOptions,
					new CompilerWarning(getName() + " is deprecated for spawn_entity().", t, null));
			return null;
		}

		@Override
		public Set<Optimizable.OptimizationOption> optimizationOptions() {
			return EnumSet.of(Optimizable.OptimizationOption.OPTIMIZE_DYNAMIC);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	@hide("Deprecated")
	public static class tame_mob extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "tame_mob";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "void {[player], entityUUID} Tames any tameable mob to the specified player."
					+ " (deprecated for {{function|set_mob_owner}}) ----"
					+ " Offline players are supported, but this means that partial matches are NOT supported."
					+ " You must type the players name exactly. Setting the player to null will untame the mob."
					+ " If the entity doesn't exist, nothing happens.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREUntameableMobException.class, CRELengthException.class,
				CREBadEntityException.class};
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
			String player = null;
			MCPlayer mcPlayer = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if(mcPlayer != null) {
				player = mcPlayer.getName();
			}
			Mixed entityID = null;
			if(args.length == 2) {
				if(args[0] instanceof CNull) {
					player = null;
				} else {
					player = args[0].val();
				}
				entityID = args[1];
			} else {
				entityID = args[0];
			}
			MCLivingEntity e = Static.getLivingEntity(entityID, t);
			if(e == null) {
				return CVoid.VOID;
			} else if(e.isTameable()) {
				MCTameable mct = ((MCTameable) e);
				if(player != null) {
					mct.setOwner(Static.getServer().getOfflinePlayer(player));
				} else {
					mct.setOwner(null);
				}
				return CVoid.VOID;
			} else {
				throw new CREUntameableMobException("The specified entity is not tameable", t);
			}
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {
			env.getEnv(CompilerEnvironment.class).addCompilerWarning(fileOptions,
					new CompilerWarning(getName() + " is deprecated for set_mob_owner().", t, null));
			return null;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC);
		}
	}

	@api
	public static class get_mob_owner extends AbstractFunction {

		@Override
		public String getName() {
			return "get_mob_owner";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "string {entityUUID} Returns the owner's name, or null if the mob is unowned."
					+ "An UntameableMobException is thrown if mob isn't tameable to begin with.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREUntameableMobException.class, CRELengthException.class,
				CREBadEntityException.class, CREIllegalArgumentException.class};
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
			MCLivingEntity mob = Static.getLivingEntity(args[0], t);
			if(!mob.isTameable()) {
				throw new CREUntameableMobException("The specified entity is not tameable", t);
			}
			MCAnimalTamer owner = ((MCTameable) mob).getOwner();
			if(owner == null) {
				return CNull.NULL;
			} else {
				return new CString(owner.getName(), t);
			}
		}
	}

	@api
	public static class set_mob_owner extends AbstractFunction {

		@Override
		public String getName() {
			return "set_mob_owner";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "void {entityUUID, player} Sets the tameable mob to the specified player. Offline players are"
					+ " supported, but this means that partial matches are NOT supported. You must type the player's"
					+ " name exactly. Setting the player to null will untame the mob.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREUntameableMobException.class, CRELengthException.class,
				CREBadEntityException.class, CREIllegalArgumentException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_2;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCLivingEntity mob = Static.getLivingEntity(args[0], t);
			Mixed player = args[1];
			if(!mob.isTameable()) {
				throw new CREUntameableMobException("The specified entity is not tameable", t);
			}
			MCTameable mct = ((MCTameable) mob);
			if(player instanceof CNull) {
				mct.setOwner(null);
			} else {
				mct.setOwner(Static.getServer().getOfflinePlayer(player.val()));
			}
			return CVoid.VOID;
		}
	}

	@api
	public static class set_entity_health extends AbstractFunction {

		@Override
		public String getName() {
			return "set_entity_health";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "void {entityUUID, healthPercent} Sets the specified entity's health as a percentage,"
					+ " where 0 kills it and 100 gives it full health."
					+ " An exception is thrown if the entity by that UUID doesn't exist or isn't a LivingEntity.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREBadEntityException.class,
				CRERangeException.class, CRELengthException.class};
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
			MCLivingEntity e = Static.getLivingEntity(args[0], t);
			double percent = Static.getDouble(args[1], t);
			if(percent < 0 || percent > 100) {
				throw new CRERangeException("Health was expected to be a percentage between 0 and 100", t);
			} else {
				e.setHealth(percent / 100.0 * e.getMaxHealth());
			}
			return CVoid.VOID;
		}
	}

	@api
	public static class get_entity_health extends AbstractFunction {

		@Override
		public String getName() {
			return "get_entity_health";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "double {entityUUID} Returns the entity's health as a percentage of its maximum health."
					+ " If the specified entity doesn't exist, or is not a LivingEntity, a format exception is thrown.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRELengthException.class, CREBadEntityException.class};
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
			MCLivingEntity e = Static.getLivingEntity(args[0], t);
			return new CDouble(e.getHealth() / e.getMaxHealth() * 100.0, t);
		}
	}

	@api
	public static class get_entity_breedable extends EntityManagement.EntityGetterFunction {

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCEntity ent = Static.getEntity(args[0], t);

			if(ent instanceof MCAgeable) {
				return CBoolean.get(((MCAgeable) ent).getCanBreed());
			} else {
				throw new CREBadEntityException("Entity ID must be from an ageable entity!", t);
			}
		}

		@Override
		public String getName() {
			return "get_entity_breedable";
		}

		@Override
		public String docs() {
			return "boolean {entityUUID} Returns if an entity is set to be breedable.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
	public static class set_entity_breedable extends EntityManagement.EntitySetterFunction {

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			boolean breed = ArgumentValidation.getBoolean(args[1], t);

			MCEntity ent = Static.getEntity(args[0], t);

			if(ent instanceof MCAgeable) {
				((MCAgeable) ent).setCanBreed(breed);
			} else {
				throw new CREBadEntityException("Entity ID must be from an ageable entity!", t);
			}

			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "set_entity_breedable";
		}

		@Override
		public String docs() {
			return "void {entityUUID, boolean} Set an entity to be breedable.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
	public static class get_mob_age extends EntityManagement.EntityGetterFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREUnageableMobException.class, CRELengthException.class,
				CREBadEntityException.class};
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCLivingEntity ent = Static.getLivingEntity(args[0], t);
			if(ent == null) {
				return CNull.NULL;
			} else if(ent instanceof MCAgeable) {
				MCAgeable mob = ((MCAgeable) ent);
				return new CInt(mob.getAge(), t);
			} else {
				throw new CREUnageableMobException("The specified entity does not age", t);
			}
		}

		@Override
		public String getName() {
			return "get_mob_age";
		}

		@Override
		public String docs() {
			return "int {entityUUID} Returns the mob's age as an integer. Zero represents the point of adulthood. Throws an"
					+ " UnageableMobException if the mob is not a type that ages";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
	public static class set_mob_age extends EntityManagement.EntityFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREUnageableMobException.class, CRECastException.class,
				CREBadEntityException.class, CRELengthException.class};
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			int age = Static.getInt32(args[1], t);
			boolean lock = false;
			if(args.length == 3) {
				lock = ArgumentValidation.getBoolean(args[2], t);
			}
			MCLivingEntity ent = Static.getLivingEntity(args[0], t);
			if(ent == null) {
				return CNull.NULL;
			} else if(ent instanceof MCAgeable) {
				MCAgeable mob = ((MCAgeable) ent);
				mob.setAge(age);
				mob.setAgeLock(lock);
				return CVoid.VOID;
			} else {
				throw new CREUnageableMobException("The specified entity does not age", t);
			}
		}

		@Override
		public String getName() {
			return "set_mob_age";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public String docs() {
			return "void {entityUUID, int[, lockAge]} sets the age of the mob to the specified int, and locks it at"
					+ " that age if lockAge is true, but by default it will not."
					+ " Throws a UnageableMobException if the mob does not age naturally.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
	public static class get_mob_effects extends EntityManagement.EntityGetterFunction {

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCLivingEntity mob = Static.getLivingEntity(args[0], t);
			return ObjectGenerator.GetGenerator().potions(mob.getEffects(), t);
		}

		@Override
		public String getName() {
			return "get_mob_effects";
		}

		@Override
		public String docs() {
			return "array {entityUUID} Returns an array of potion effect arrays showing"
					+ " the effects on this mob.";
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{new ExampleScript("Basic use",
				"msg(get_mob_effects('091a595d-3d2f-4df4-b493-951dc4bed7f2'))",
				"{speed: {ambient: false, icon: true, id: 1, particles: true, seconds: 30.0, strength: 1}}")};
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

	}

	@api
	public static class set_mob_effect extends EntityManagement.EntityFunction {

		@Override
		public String getName() {
			return "set_mob_effect";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3, 4, 5, 6, 7};
		}

		@Override
		public String docs() {
			return "boolean {entityUUID, potionEffect, [strength], [seconds], [ambient], [particles], [icon]}"
					+ " Adds one, or modifies an existing, potion effect on a mob."
					+ " The potionEffect can be " + StringUtils.Join(MCPotionEffectType.types(), ", ", ", or ", " or ")
					+ ". It also accepts an integer corresponding to the effect id listed on the Minecraft wiki."
					+ " Strength is an integer representing the power level of the effect, starting at 0."
					+ " Seconds defaults to 30.0. To remove an effect, set the seconds to 0."
					+ " If seconds is less than 0 or greater than 107374182 a RangeException is thrown."
					+ " Ambient takes a boolean of whether the particles should be more transparent."
					+ " Particles takes a boolean of whether the particles should be visible at all."
					+ " Icon takes a boolean for whether or not to show the icon to the entity if it's a player."
					+ " The function returns whether or not the effect was modified.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRELengthException.class, CREFormatException.class,
				CREBadEntityException.class, CRERangeException.class};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCLivingEntity mob = Static.getLivingEntity(args[0], t);

			MCPotionEffectType type = null;
			if(args[1].isInstanceOf(CString.TYPE)) {
				try {
					type = MCPotionEffectType.valueOf(args[1].val().toUpperCase());
				} catch (IllegalArgumentException ex) {
					// maybe it's a number id
				}
			}
			if(type == null) {
				try {
					type = MCPotionEffectType.getById(Static.getInt32(args[1], t));
				} catch (IllegalArgumentException ex) {
					throw new CREFormatException("Invalid potion effect type: " + args[1].val(), t);
				}
			}

			int strength = 0;
			double seconds = 30.0;
			boolean ambient = false;
			boolean particles = true;
			boolean icon = true;
			if(args.length >= 3) {
				strength = Static.getInt32(args[2], t);

				if(args.length >= 4) {
					seconds = Static.getDouble(args[3], t);
					if(seconds < 0.0) {
						throw new CRERangeException("Seconds cannot be less than 0.0", t);
					} else if(seconds * 20 > Integer.MAX_VALUE) {
						throw new CRERangeException("Seconds cannot be greater than 107374182.0", t);
					}

					if(args.length >= 5) {
						ambient = ArgumentValidation.getBoolean(args[4], t);

						if(args.length >= 6) {
							particles = ArgumentValidation.getBoolean(args[5], t);

							if(args.length == 7) {
								icon = ArgumentValidation.getBoolean(args[6], t);
							}
						}
					}
				}
			}

			if(seconds == 0.0) {
				return CBoolean.get(mob.removeEffect(type));
			} else {
				return CBoolean.get(mob.addEffect(type, strength, (int) (seconds * 20), ambient, particles, icon));
			}
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
	public static class get_mob_target extends EntityManagement.EntityGetterFunction {

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCLivingEntity le = Static.getLivingEntity(args[0], t);
			if(le.getTarget(t) == null) {
				return CNull.NULL;
			} else {
				return new CString(le.getTarget(t).getUniqueId().toString(), t);
			}
		}

		@Override
		public String getName() {
			return "get_mob_target";
		}

		@Override
		public String docs() {
			return "string {entityUUID} Gets the mob's target if it has one, and returns the target's entityUUID."
					+ " If there is no target, null is returned instead. Not all mobs will have a returnable target.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
	public static class set_mob_target extends EntityManagement.EntitySetterFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREBadEntityException.class, CRELengthException.class};
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCLivingEntity le = Static.getLivingEntity(args[0], t);
			MCLivingEntity target = null;
			if(!(args[1] instanceof CNull)) {
				target = Static.getLivingEntity(args[1], t);
			}
			le.setTarget(target, t);
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "set_mob_target";
		}

		@Override
		public String docs() {
			return "void {entityUUID, entityUUID} The first is the entity that is targeting, the second is the target."
					+ " It can also be set to null to clear the current target. Not all mobs can have their target set.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
	public static class get_mob_equipment extends EntityManagement.EntityGetterFunction {

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCLivingEntity le = Static.getLivingEntity(args[0], t);
			MCEntityEquipment eq = le.getEquipment();
			if(eq == null) {
				throw new CREBadEntityTypeException("Entities of type \"" + le.getType() + "\" do not have equipment.", t);
			}
			Map<MCEquipmentSlot, MCItemStack> eqmap = le.getEquipment().getAllEquipment();
			CArray ret = CArray.GetAssociativeArray(t);
			for(MCEquipmentSlot key : eqmap.keySet()) {
				ret.set(key.name().toLowerCase(), ObjectGenerator.GetGenerator().item(eqmap.get(key), t), t);
			}
			return ret;
		}

		@Override
		public String getName() {
			return "get_mob_equipment";
		}

		@Override
		public String docs() {
			return "array {entityUUID} Returns an associative array showing the equipment this mob is wearing."
					+ " This does not work on most \"dumb\" entities, only mobs (entities with AI).";
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Getting a mob's equipment",
				"get_mob_equipment('091a595d-3d2f-4df4-b493-951dc4bed7f2')",
				"{boots: null, chestplate: null, helmet: {data: 0, enchants: {} meta: null, name:"
				+ " JACK_O_LANTERN}, leggings: null, off_hand: null, weapon: {data: 5, enchants: {} meta:"
				+ " {display: Excalibur, lore: null}, name: DIAMOND_SWORD}}")
			};
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
	public static class set_mob_equipment extends EntityManagement.EntitySetterFunction {

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCLivingEntity le = Static.getLivingEntity(args[0], t);
			MCEntityEquipment ee = le.getEquipment();
			if(ee == null) {
				throw new CREBadEntityTypeException("Entities of type \"" + le.getType() + "\" do not have equipment.", t);
			}
			Map<MCEquipmentSlot, MCItemStack> eq = ee.getAllEquipment();
			if(args[1] instanceof CNull) {
				ee.clearEquipment();
				return CVoid.VOID;
			} else if(args[1].isInstanceOf(CArray.TYPE)) {
				CArray ea = (CArray) args[1];
				for(String key : ea.stringKeySet()) {
					try {
						eq.put(MCEquipmentSlot.valueOf(key.toUpperCase()), ObjectGenerator.GetGenerator().item(ea.get(key, t), t));
					} catch (IllegalArgumentException iae) {
						throw new CREFormatException("Not an equipment slot: " + key, t);
					}
				}
			} else {
				throw new CREFormatException("Expected argument 2 to be an array", t);
			}
			ee.setAllEquipment(eq);
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "set_mob_equipment";
		}

		@Override
		public String docs() {
			return "void {entityUUID, array} Takes an associative array with keys representing equipment slots and"
					+ " values of itemArrays, the same used by set_pinv. This does not work on most \"dumb\" entities,"
					+ " only mobs (entities with AI). Unless a mod, plugin, or future update changes vanilla functionality,"
					+ " only humanoid mobs will render their equipment slots. The equipment slots are: "
					+ StringUtils.Join(MCEquipmentSlot.values(), ", ", ", or ", " or ");
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage",
				"set_mob_equipment(spawn_mob('SKELETON')[0], array(WEAPON: array(name: 'BOW')))",
				"Gives a bow to a skeleton")
			};
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
	public static class get_max_health extends EntityManagement.EntityGetterFunction {

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCLivingEntity le = Static.getLivingEntity(args[0], t);
			return new CDouble(le.getMaxHealth(), t);
		}

		@Override
		public String getName() {
			return "get_max_health";
		}

		@Override
		public String docs() {
			return "double {entityUUID} Returns the maximum health of this living entity.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
	public static class set_max_health extends EntityManagement.EntitySetterFunction {

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCLivingEntity le = Static.getLivingEntity(args[0], t);
			le.setMaxHealth(Static.getDouble(args[1], t));
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "set_max_health";
		}

		@Override
		public String docs() {
			return "void {entityUUID, double} Sets the max health of a living entity, players included."
					+ " This value is persistent, and will not reset even after server restarts.";
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{new ExampleScript("Basic use",
				"set_max_health('091a595d-3d2f-4df4-b493-951dc4bed7f2', 10.0)",
				"The entity will now only have 5 hearts max (10 half-hearts).")};
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
	public static class get_equipment_droprates extends EntityManagement.EntityGetterFunction {

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCEntityEquipment eq = Static.getLivingEntity(args[0], t).getEquipment();
			if(eq.getHolder() instanceof MCPlayer) {
				throw new CREBadEntityException(getName() + " does not work on players.", t);
			}
			CArray ret = CArray.GetAssociativeArray(t);
			for(Map.Entry<MCEquipmentSlot, Float> ent : eq.getAllDropChances().entrySet()) {
				ret.set(ent.getKey().name(), new CDouble(ent.getValue(), t), t);
			}
			return ret;
		}

		@Override
		public String getName() {
			return "get_equipment_droprates";
		}

		@Override
		public String docs() {
			return "array {entityUUID} Returns an associative array of the drop rate for each equipment slot."
					+ " If the rate is 0, the equipment will not drop. If it is 1, it is guaranteed to drop.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
	public static class set_equipment_droprates extends EntityManagement.EntitySetterFunction {

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCEntityEquipment ee = Static.getLivingEntity(args[0], t).getEquipment();
			Map<MCEquipmentSlot, Float> eq = ee.getAllDropChances();
			if(ee.getHolder() instanceof MCPlayer) {
				throw new CREBadEntityException(getName() + " does not work on players.", t);
			}
			if(args[1] instanceof CNull) {
				for(Map.Entry<MCEquipmentSlot, Float> ent : eq.entrySet()) {
					eq.put(ent.getKey(), 0F);
				}
			} else if(args[1].isInstanceOf(CArray.TYPE)) {
				CArray ea = (CArray) args[1];
				for(String key : ea.stringKeySet()) {
					try {
						eq.put(MCEquipmentSlot.valueOf(key.toUpperCase()), Static.getDouble32(ea.get(key, t), t));
					} catch (IllegalArgumentException iae) {
						throw new CREFormatException("Not an equipment slot: " + key, t);
					}
				}
			} else {
				throw new CREFormatException("Expected argument 2 to be an array", t);
			}
			ee.setAllDropChances(eq);
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "set_equipment_droprates";
		}

		@Override
		public String docs() {
			return "void {entityUUID, array} Sets the drop chances for each equipment slot on a mob,"
					+ " but does not work on players. Passing null instead of an array will automatically"
					+ " set all rates to 0, which will cause nothing to drop. A rate of 1 will guarantee a drop.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
	public static class can_pickup_items extends EntityManagement.EntityGetterFunction {

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			return CBoolean.get(Static.getLivingEntity(args[0], t).getCanPickupItems());
		}

		@Override
		public String getName() {
			return "can_pickup_items";
		}

		@Override
		public String docs() {
			return "boolean {entityUUID} Returns whether the specified living entity can pick up items.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
	public static class set_can_pickup_items extends EntityManagement.EntitySetterFunction {

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Static.getLivingEntity(args[0], t).setCanPickupItems(ArgumentValidation.getBoolean(args[1], t));
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "set_can_pickup_items";
		}

		@Override
		public String docs() {
			return "void {entityUUID, boolean} Sets a living entity's ability to pick up items.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
	public static class get_entity_persistence extends EntityManagement.EntityGetterFunction {

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			return CBoolean.get(!Static.getLivingEntity(args[0], t).getRemoveWhenFarAway());
		}

		@Override
		public String getName() {
			return "get_entity_persistence";
		}

		@Override
		public String docs() {
			return "boolean {entityUUID} Returns whether the specified living entity will despawn."
					+ " True means it will not.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
	public static class set_entity_persistence extends EntityManagement.EntitySetterFunction {

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Static.getLivingEntity(args[0], t).setRemoveWhenFarAway(!ArgumentValidation.getBoolean(args[1], t));
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "set_entity_persistence";
		}

		@Override
		public String docs() {
			return "void {entityUUID, boolean} Sets whether a living entity will despawn. True means it will not.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
	public static class get_leashholder extends EntityManagement.EntityGetterFunction {

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCLivingEntity le = Static.getLivingEntity(args[0], t);
			if(!le.isLeashed()) {
				return CNull.NULL;
			}
			return new CString(le.getLeashHolder().getUniqueId().toString(), t);
		}

		@Override
		public String getName() {
			return "get_leashholder";
		}

		@Override
		public String docs() {
			return "string {entityUUID} Returns the UUID of the entity that is holding the given living entity's leash,"
					+ " or null if it isn't being held.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
	public static class set_leashholder extends EntityManagement.EntitySetterFunction {

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCLivingEntity le = Static.getLivingEntity(args[0], t);
			MCEntity holder;
			if(args[1] instanceof CNull) {
				holder = null;
			} else {
				holder = Static.getEntity(args[1], t);
			}
			le.setLeashHolder(holder);
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "set_leashholder";
		}

		@Override
		public String docs() {
			return "void {entityUUID, entityUUID} The first argument is the entity to be held on a leash,"
					+ " and must be living. The second is the holder of the leash. This does not have to be living,"
					+ " but the only non-living entity that will persist as a holder across restarts is the leash hitch."
					+ " Players, bats, enderdragons, withers and certain other entities can not be held by leashes due"
					+ " to minecraft limitations.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
	public static class entity_air extends EntityManagement.EntityGetterFunction {

		@Override
		public String getName() {
			return "entity_air";
		}

		@Override
		public String docs() {
			return "int {entityUUID} Returns the amount of air the specified living entity has remaining.";
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			return new CInt(Static.getLivingEntity(args[0], t).getRemainingAir(), t);
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
	public static class set_entity_air extends EntityManagement.EntitySetterFunction {

		@Override
		public String getName() {
			return "set_entity_air";
		}

		@Override
		public String docs() {
			return "void {entityUUID, int} Sets the amount of air the specified living entity has remaining.";
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Static.getLivingEntity(args[0], t).setRemainingAir(Static.getInt32(args[1], t));
			return CVoid.VOID;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
	public static class entity_max_air extends EntityManagement.EntityGetterFunction {

		@Override
		public String getName() {
			return "entity_max_air";
		}

		@Override
		public String docs() {
			return "int {entityUUID} Returns the maximum amount of air the specified living entity can have.";
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			return new CInt(Static.getLivingEntity(args[0], t).getMaximumAir(), t);
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
	public static class set_entity_max_air extends EntityManagement.EntitySetterFunction {

		@Override
		public String getName() {
			return "set_entity_max_air";
		}

		@Override
		public String docs() {
			return "void {entityUUID, int} Sets the maximum amount of air the specified living entity can have.";
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Static.getLivingEntity(args[0], t).setMaximumAir(Static.getInt32(args[1], t));
			return CVoid.VOID;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
	public static class entity_line_of_sight extends EntityManagement.EntityFunction {

		@Override
		public String getName() {
			return "entity_line_of_sight";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3};
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CRELengthException.class,
				CREBadEntityException.class};
		}

		@Override
		public String docs() {
			return "array {entityUUID, [transparents, [maxDistance]]} Returns an array containing all blocks along the"
					+ " living entity's line of sight. transparents is an array of block IDs, only air by default."
					+ " maxDistance represents the maximum distance to scan. The server may cap the scan distance,"
					+ " but probably by not any less than 100 meters.";
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCLivingEntity entity = Static.getLivingEntity(args[0], t);
			HashSet<MCMaterial> transparents = null;
			int maxDistance = 512;
			if(args.length >= 2) {
				CArray givenTransparents = Static.getArray(args[1], t);
				if(givenTransparents.inAssociativeMode()) {
					throw new CRECastException("The array must not be associative.", t);
				}
				transparents = new HashSet<>();
				for(Mixed mat : givenTransparents.asList()) {
					MCMaterial material = StaticLayer.GetMaterial(mat.val());
					if(material != null) {
						transparents.add(StaticLayer.GetMaterial(mat.val()));
						continue;
					}
					try {
						material = StaticLayer.GetMaterialFromLegacy(Static.getInt16(mat, t), 0);
						if(material != null) {
							MSLog.GetLogger().w(MSLog.Tags.DEPRECATION, "The id \"" + mat.val() + "\" is deprecated."
									+ " Converted to \"" + material.getName() + "\"", t);
							transparents.add(material);
							continue;
						}
					} catch (CRECastException ex) {
						// ignore and throw a more specific message
					}
					throw new CREFormatException("Could not find a material by the name \"" + mat.val() + "\"", t);
				}
			}
			if(args.length == 3) {
				maxDistance = Static.getInt32(args[2], t);
			}
			CArray lineOfSight = new CArray(t);
			for(MCBlock block : entity.getLineOfSight(transparents, maxDistance)) {
				lineOfSight.push(ObjectGenerator.GetGenerator().location(block.getLocation(), false), t);
			}
			return lineOfSight;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
	public static class entity_can_see extends EntityManagement.EntityFunction {

		@Override
		public String getName() {
			return "entity_can_see";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRELengthException.class, CREBadEntityException.class};
		}

		@Override
		public String docs() {
			return "boolean {entityUUID, otherEntityUUID} Returns whether or not the first entity can have the other"
					+ " entity in an unimpeded line of sight, ignoring the direction it's facing."
					+ " For instance, for players this mean that it can have the other entity on its screen and that"
					+ " this one is not hidden by opaque blocks."
					+ " This uses the same algorithm that hostile mobs use to find the closest player.";
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			return CBoolean.get(Static.getLivingEntity(args[0], t).hasLineOfSight(Static.getEntity(args[1], t)));
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
	public static class damage_entity extends EntityManagement.EntityFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CRELengthException.class,
				CREBadEntityTypeException.class, CREBadEntityException.class};
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCEntity entity = Static.getEntity(args[0], t);

			if(!(entity instanceof MCLivingEntity)) {
				throw new CREBadEntityTypeException("The entity id provided doesn't"
						+ " belong to a living entity", t);
			}

			MCLivingEntity living = (MCLivingEntity) entity;

			double damage = Static.getDouble(args[1], t);
			if(args.length == 3) {
				MCEntity source = Static.getEntity(args[2], t);
				living.damage(damage, source);
			} else {
				living.damage(damage);
			}

			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "damage_entity";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public String docs() {
			return "void {entityUUID, amount, [sourceEntityUUID]} Damage an entity. If given,"
					+ " the source entity will be attributed as the damager.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
	public static class set_entity_gliding extends EntityManagement.EntitySetterFunction {

		@Override
		public String getName() {
			return "set_entity_gliding";
		}

		@Override
		public String docs() {
			return "void {entityUUID, boolean} If possible, makes the entity glide.";
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCLivingEntity e = Static.getLivingEntity(args[0], t);
			boolean glide = ArgumentValidation.getBoolean(args[1], t);

			e.setGliding(glide);

			return CVoid.VOID;
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_2;
		}
	}

	@api
	public static class get_entity_gliding extends EntityManagement.EntityGetterFunction {

		@Override
		public String getName() {
			return "get_entity_gliding";
		}

		@Override
		public String docs() {
			return "boolean {entityUUID} Returns true if the given entity is gliding.";
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			return CBoolean.GenerateCBoolean(Static.getLivingEntity(args[0], t).isGliding(), t);
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_2;
		}
	}

	@api
	public static class get_entity_ai extends EntityManagement.EntityGetterFunction {

		@Override
		public String getName() {
			return "get_entity_ai";
		}

		@Override
		public String docs() {
			return "boolean {entityUUID} Returns true if the given entity has AI.";
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			return CBoolean.GenerateCBoolean(Static.getLivingEntity(args[0], t).hasAI(), t);
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_2;
		}
	}

	@api
	public static class set_entity_ai extends EntityManagement.EntitySetterFunction {

		@Override
		public String getName() {
			return "set_entity_ai";
		}

		@Override
		public String docs() {
			return "void {entityUUID, boolean} enables or disables the entity AI.";
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCLivingEntity e = Static.getLivingEntity(args[0], t);
			boolean ai = ArgumentValidation.getBoolean(args[1], t);

			e.setAI(ai);

			return CVoid.VOID;
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_2;
		}
	}

	@api
	public static class is_mob_collidable extends EntityManagement.EntityGetterFunction {

		@Override
		public String getName() {
			return "is_mob_collidable";
		}

		@Override
		public String docs() {
			return "boolean {entityUUID} Returns whether another entity, like an arrow, will collide with this mob.";
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			return CBoolean.GenerateCBoolean(Static.getLivingEntity(args[0], t).isCollidable(), t);
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_3;
		}
	}

	@api
	public static class set_mob_collidable extends EntityManagement.EntitySetterFunction {

		@Override
		public String getName() {
			return "set_mob_collidable";
		}

		@Override
		public String docs() {
			return "void {entityUUID, boolean} Sets whether or not other entities will collide with this mob.";
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCLivingEntity e = Static.getLivingEntity(args[0], t);
			boolean collidable = ArgumentValidation.getBoolean(args[1], t);
			e.setCollidable(collidable);
			return CVoid.VOID;
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_3;
		}
	}

	@api
	public static class entity_attribute_value extends AbstractFunction {

		@Override
		public String getName() {
			return "entity_attribute_value";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "double {entityUUID, attribute} Gets the effective value for the attribute of this entity"
					+ " after all modifiers have been applied to the base value. ----"
					+ " Available attributes: " + StringUtils.Join(MCAttribute.values(), ", ", ", and ");
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CRELengthException.class, CREFormatException.class,
					CREBadEntityTypeException.class, CREBadEntityException.class};
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
			MCLivingEntity e = Static.getLivingEntity(args[0], t);
			MCAttribute attribute;
			try {
				attribute = MCAttribute.valueOf(args[1].val());
			} catch (IllegalArgumentException ex) {
				throw new CREFormatException("Invalid attribute name: " + args[1].val(), t);
			}
			try {
				return new CDouble(e.getAttributeValue(attribute), t);
			} catch (IllegalArgumentException ex) {
				throw new CREBadEntityTypeException(ex.getMessage(), t);
			}
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_4;
		}
	}

	@api
	public static class entity_attribute_base extends AbstractFunction {

		@Override
		public String getName() {
			return "entity_attribute_base";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "double {entityUUID, attribute} Gets the base value for the given attribute of this entity."
					+ " ---- Available attributes: " + StringUtils.Join(MCAttribute.values(), ", ", ", and ");
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CRELengthException.class, CREFormatException.class,
					CREBadEntityTypeException.class, CREBadEntityException.class};
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
			MCLivingEntity e = Static.getLivingEntity(args[0], t);
			MCAttribute attribute;
			try {
				attribute = MCAttribute.valueOf(args[1].val());
			} catch (IllegalArgumentException ex) {
				throw new CREFormatException("Invalid attribute name: " + args[1].val(), t);
			}
			try {
				return new CDouble(e.getAttributeBase(attribute), t);
			} catch (IllegalArgumentException ex) {
				throw new CREBadEntityTypeException(ex.getMessage(), t);
			}
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_4;
		}
	}

	@api
	public static class entity_attribute_default extends AbstractFunction {

		@Override
		public String getName() {
			return "entity_attribute_default";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "double {entityUUID, attribute} Gets the default value for the attribute of this entity's type."
					+ " ---- Available attributes: " + StringUtils.Join(MCAttribute.values(), ", ", ", and ");
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CRELengthException.class, CREFormatException.class,
					CREBadEntityTypeException.class, CREBadEntityException.class};
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
			MCLivingEntity e = Static.getLivingEntity(args[0], t);
			MCAttribute attribute;
			try {
				attribute = MCAttribute.valueOf(args[1].val());
			} catch (IllegalArgumentException ex) {
				throw new CREFormatException("Invalid attribute name: " + args[1].val(), t);
			}
			try {
				return new CDouble(e.getAttributeDefault(attribute), t);
			} catch (IllegalArgumentException ex) {
				throw new CREBadEntityTypeException(ex.getMessage(), t);
			}
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_4;
		}
	}

	@api
	public static class entity_attribute_modifiers extends AbstractFunction {

		@Override
		public String getName() {
			return "entity_attribute_modifiers";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "array {entityUUID, attribute} Gets an array of modifier arrays for this entity's attribute."
					+ " ---- Available attributes: " + StringUtils.Join(MCAttribute.values(), ", ", ", and ");
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CRELengthException.class, CREFormatException.class,
					CREBadEntityTypeException.class, CREBadEntityException.class};
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
			MCLivingEntity e = Static.getLivingEntity(args[0], t);
			MCAttribute attribute;
			try {
				attribute = MCAttribute.valueOf(args[1].val());
			} catch (IllegalArgumentException ex) {
				throw new CREFormatException("Invalid attribute name: " + args[1].val(), t);
			}
			try {
				CArray ret = new CArray(t);
				for(MCAttributeModifier m : e.getAttributeModifiers(attribute)) {
					ret.push(ObjectGenerator.GetGenerator().attributeModifier(m, t), t);
				}
				return ret;
			} catch (IllegalArgumentException ex) {
				throw new CREBadEntityTypeException(ex.getMessage(), t);
			}
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_4;
		}
	}

	@api
	public static class set_entity_attribute_base extends AbstractFunction {

		@Override
		public String getName() {
			return "set_entity_attribute_base";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{3};
		}

		@Override
		public String docs() {
			return "void {entityUUID, attribute, value} Sets the base value for the entity attribute."
					+ " Accepts a double as the value."
					+ " ---- Available attributes: " + StringUtils.Join(MCAttribute.values(), ", ", ", and ");
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CRELengthException.class, CREFormatException.class,
					CREBadEntityTypeException.class, CREBadEntityException.class};
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
			MCLivingEntity e = Static.getLivingEntity(args[0], t);
			MCAttribute attribute;
			try {
				attribute = MCAttribute.valueOf(args[1].val());
			} catch (IllegalArgumentException ex) {
				throw new CREFormatException("Invalid attribute name: " + args[1].val(), t);
			}
			double base = Static.getDouble(args[2], t);
			try {
				e.setAttributeBase(attribute, base);
			} catch (IllegalArgumentException ex) {
				throw new CREBadEntityTypeException(ex.getMessage(), t);
			}
			return CVoid.VOID;
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_4;
		}
	}

	@api
	public static class reset_entity_attribute_base extends AbstractFunction {

		@Override
		public String getName() {
			return "reset_entity_attribute_base";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "void {entityUUID, attribute} Resets the base attribute value to the default for this entity."
					+ " ---- Available attributes: " + StringUtils.Join(MCAttribute.values(), ", ", ", and ");
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CRELengthException.class, CREFormatException.class,
					CREBadEntityTypeException.class, CREBadEntityException.class};
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
			MCLivingEntity e = Static.getLivingEntity(args[0], t);
			MCAttribute attribute;
			try {
				attribute = MCAttribute.valueOf(args[1].val());
			} catch (IllegalArgumentException ex) {
				throw new CREFormatException("Invalid attribute name: " + args[1].val(), t);
			}
			try {
				e.resetAttributeBase(attribute);
			} catch (IllegalArgumentException ex) {
				throw new CREBadEntityTypeException(ex.getMessage(), t);
			}
			return CVoid.VOID;
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_4;
		}
	}

	@api
	public static class add_entity_attribute_modifier extends AbstractFunction {

		@Override
		public String getName() {
			return "add_entity_attribute_modifier";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "void {entityUUID, modifier} Adds an attribute modifier to an entity."
					+ " Throws BadEntityTypeException if the attribute type does not apply to this entity type."
					+ " See {{function|get_itemmeta}} for how to define an attribute modifier array.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CRELengthException.class,
					CREBadEntityTypeException.class, CREBadEntityException.class};
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
			MCLivingEntity e = Static.getLivingEntity(args[0], t);
			MCAttributeModifier modifier = ObjectGenerator.GetGenerator().attributeModifier(Static.getArray(args[1], t), t);
			try {
				e.addAttributeModifier(modifier);
			} catch (IllegalArgumentException ex) {
				throw new CREBadEntityTypeException(ex.getMessage(), t);
			}
			return CVoid.VOID;
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_4;
		}
	}

	@api
	public static class remove_entity_attribute_modifier extends AbstractFunction {

		@Override
		public String getName() {
			return "remove_entity_attribute_modifier";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public String docs() {
			return "void {entityUUID, modifier | entityUUID, attribute, id} Removes an attribute modifier from an"
					+ " entity. A modifier array can be provided, or both an attribute name and either the UUID or"
					+ " name (if it's unique) for the modifier can be provided as the identifier.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CRELengthException.class, CREFormatException.class,
					CREBadEntityTypeException.class, CREBadEntityException.class};
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
			MCLivingEntity e = Static.getLivingEntity(args[0], t);
			MCAttributeModifier modifier = null;
			if(args.length == 2) {
				modifier = ObjectGenerator.GetGenerator().attributeModifier(Static.getArray(args[1], t), t);
			} else {
				MCAttribute attribute;
				try {
					attribute = MCAttribute.valueOf(args[1].val());
				} catch (IllegalArgumentException ex) {
					throw new CREFormatException("Invalid attribute name: " + args[1].val(), t);
				}
				List<MCAttributeModifier> modifiers = e.getAttributeModifiers(attribute);
				String name = args[2].val();
				UUID id = null;
				if(name.length() == 36 || name.length() == 32) {
					try {
						id = UUID.fromString(name);
					} catch (IllegalArgumentException ex) {
						// not UUID
					}
				}
				for(MCAttributeModifier m : modifiers) {
					if(id != null && m.getUniqueId().compareTo(id) == 0
							|| id == null && name.equals(m.getAttributeName())) {
						modifier = m;
						break;
					}
				}
				if(modifier == null) {
					return CVoid.VOID;
				}
			}
			try {
				e.removeAttributeModifier(modifier);
			} catch (IllegalArgumentException ex) {
				throw new CREBadEntityTypeException(ex.getMessage(), t);
			}
			return CVoid.VOID;
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_4;
		}
	}
}
