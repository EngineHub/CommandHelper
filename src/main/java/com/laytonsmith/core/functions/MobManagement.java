package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.MCAgeable;
import com.laytonsmith.abstraction.MCAnimalTamer;
import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCEntityEquipment;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCTameable;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.entities.MCHorse;
import com.laytonsmith.abstraction.enums.MCCreeperType;
import com.laytonsmith.abstraction.enums.MCDyeColor;
import com.laytonsmith.abstraction.enums.MCEquipmentSlot;
import com.laytonsmith.abstraction.enums.MCMobs;
import com.laytonsmith.abstraction.enums.MCOcelotType;
import com.laytonsmith.abstraction.enums.MCPigType;
import com.laytonsmith.abstraction.enums.MCProfession;
import com.laytonsmith.abstraction.enums.MCSkeletonType;
import com.laytonsmith.abstraction.enums.MCWolfType;
import com.laytonsmith.abstraction.enums.MCZombieType;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.hide;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
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

import java.util.HashSet;
import java.util.Map;

public class MobManagement {
	public static String docs(){
		return "These functions manage specifically living entities. If the entity specified is not living, a"
				+ " BadEntityTypeException will be thrown.";
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class spawn_mob extends AbstractFunction {

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
			return "array {mobType, [qty], [location]} Spawns qty mob of one of the following types at location. qty defaults to 1, and location defaults"
					+ " to the location of the player. An array of the entity IDs spawned is returned."
					+ " ---- mobType can be one of: " + StringUtils.Join(MCMobs.values(), ", ", ", or ", " or ") + "."
					+ " Spelling matters, but capitalization doesn't. At this time, the function is limited to spawning a maximum of 50 at a time."
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
					+ ", and pigs can be " + StringUtils.Join(MCPigType.values(), ", ", ", or ", " or ") + "."
					+ " Horses can have three different subTypes, the variant: " + StringUtils.Join(MCHorse.MCHorseVariant.values(), ", ", ", or ", " or ") + ","
					+ " the color: " + StringUtils.Join(MCHorse.MCHorseColor.values(), ", ", ", or ", " or ") + ","
					+ " and the pattern: " + StringUtils.Join(MCHorse.MCHorsePattern.values(), ", ", ", or ", " or ") + "."
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
		public CHVersion since() {
			return CHVersion.V3_1_2;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
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

			try{
				return l.getWorld().spawnMob(MCMobs.valueOf(mob.toUpperCase().replaceAll(" ", "")), secondary, qty, l, t);
			} catch(IllegalArgumentException e){
				throw new CREFormatException("Invalid mob name: " + mob, t);
			}
		}
	}

	@api(environments={CommandHelperEnvironment.class})
	@hide("Deprecated")
	public static class tame_mob extends AbstractFunction {

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
			return "void {[player], entityID} Tames any tameable mob to the specified player. Offline players are"
					+ " supported, but this means that partial matches are NOT supported. You must type the players"
					+ " name exactly. Setting the player to null will untame the mob. If the entity doesn't exist,"
					+ " nothing happens.";
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
		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			String player = null;
			MCPlayer mcPlayer = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if(mcPlayer != null) {
				player = mcPlayer.getName();
			}
			Construct entityID = null;
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
			} else if(e instanceof MCTameable) {
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
			return "string {entityID} Returns the owner's name, or null if the mob is unowned. An UntameableMobException is thrown if"
					+ " mob isn't tameable to begin with.";
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
		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCLivingEntity mob = Static.getLivingEntity(args[0], t);
			if(!(mob instanceof MCTameable)) {
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
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "void {entityID, player} Sets the tameable mob to the specified player. Offline players are"
					+ " supported, but this means that partial matches are NOT supported. You must type the players"
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
		public CHVersion since() {
			return CHVersion.V3_3_2;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCLivingEntity mob = Static.getLivingEntity(args[0], t);
			Construct player = args[1];
			if(!(mob instanceof MCTameable)) {
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
			return "void {entityID, healthPercent} Sets the specified entity's health as a percentage,"
					+ " where 0 kills it and 100 gives it full health."
					+ " An exception is thrown if the entityID doesn't exist or isn't a LivingEntity.";
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
		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
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
			return "double {entityID} Returns the entity's health as a percentage of its maximum health."
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
		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCLivingEntity e = Static.getLivingEntity(args[0], t);
			return new CDouble(e.getHealth() / e.getMaxHealth() * 100.0, t);
		}
	}

	@api
	public static class get_entity_breedable extends EntityManagement.EntityGetterFunction {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCEntity ent = Static.getEntity(args[0], t);

			if(ent instanceof MCAgeable){
				return CBoolean.get(((MCAgeable)ent).getCanBreed());
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
			return "boolean {entityID} Returns if an entity is set to be breedable.";
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class set_entity_breedable extends EntityManagement.EntitySetterFunction {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			boolean breed = Static.getBoolean(args[1]);

			MCEntity ent = Static.getEntity(args[0], t);

			if(ent instanceof MCAgeable){
				((MCAgeable)ent).setCanBreed(breed);
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
			return "void {entityID, boolean} Set an entity to be breedable.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
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
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
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
			return "int {entityID} Returns the mob's age as an integer. Zero represents the point of adulthood. Throws an"
					+ " UnageableMobException if the mob is not a type that ages";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
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
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			int age = Static.getInt32(args[1], t);
			boolean lock = false;
			if(args.length == 3) {
				lock = (boolean) Static.getBoolean(args[2]);
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
			return "void {entityID, int[, lockAge]} sets the age of the mob to the specified int, and locks it at that age"
					+ " if lockAge is true, but by default it will not. Throws a UnageableMobException if the mob does"
					+ " not age naturally.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class get_mob_effects extends EntityManagement.EntityGetterFunction {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCLivingEntity mob = Static.getLivingEntity(args[0], t);
			return ObjectGenerator.GetGenerator().potions(mob.getEffects(), t);
		}

		@Override
		public String getName() {
			return "get_mob_effects";
		}

		@Override
		public String docs() {
			return "array {entityID} Returns an array of potion arrays showing"
					+ " the effects on this mob.";
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{new ExampleScript("Basic use",
					"msg(get_mob_effects('091a595d-3d2f-4df4-b493-951dc4bed7f2'))",
					"{{ambient: false, id: 1, seconds: 30.0, strength: 1}}")};
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
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
			return new Integer[]{3, 4, 5, 6};
		}

		@Override
		public String docs() {
			return "boolean {entityId, potionID, strength, [seconds], [ambient], [particles]} Effect is 1-23. Seconds"
					+ " defaults to 30.0. If the potionID is out of range, a RangeException is thrown, because out of"
					+ " range potion effects cause the client to crash, fairly hardcore. See"
					+ " http://www.minecraftwiki.net/wiki/Potion_effects for a complete list of potions that can be"
					+ " added. To remove an effect, set the seconds to 0. If seconds is less than 0 or greater than"
					+ " 107374182 a RangeException is thrown. Strength is the number of levels to add to the"
					+ " base power (effect level 1). Ambient takes a boolean of whether the particles should be less"
					+ " noticeable. Particles takes a boolean of whether the particles should be visible at all. The"
					+ " function returns true if the effect was added or removed as desired, and false if it wasn't"
					+ " (however, this currently only will happen if an effect is attempted to be removed, yet isn't"
					+ " already on the mob).";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRELengthException.class, CREFormatException.class,
					CREBadEntityException.class, CRERangeException.class};
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			MCLivingEntity mob = Static.getLivingEntity(args[0], t);

			int effect = Static.getInt32(args[1], t);

			int strength = Static.getInt32(args[2], t);
			double seconds = 30.0;
			boolean ambient = false;
			boolean particles = true;
			if(args.length >= 4) {
				seconds = Static.getDouble(args[3], t);
				if(seconds < 0.0) {
					throw new CRERangeException("Seconds cannot be less than 0.0", t);
				} else if(seconds * 20 > Integer.MAX_VALUE) {
					throw new CRERangeException("Seconds cannot be greater than 107374182.0", t);
				}
			}
			if(args.length == 5) {
				ambient = Static.getBoolean(args[4]);
			}
			if(args.length == 6) {
				particles = Static.getBoolean(args[5]);
			}

			if(seconds == 0.0) {
				return CBoolean.get(mob.removeEffect(effect));
			} else {
				mob.addEffect(effect, strength, (int)(seconds * 20), ambient, particles, t);
				return CBoolean.TRUE;
			}
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	//@api
	public static class get_mob_target extends EntityManagement.EntityGetterFunction {

		@Override
		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
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
			return "entityID {entityID} Gets the mob's target if it has one, and returns the target's entityID."
					+ " If there is no target, null is returned instead.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	//@api
	public static class set_mob_target extends EntityManagement.EntitySetterFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREBadEntityException.class, CRELengthException.class};
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
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
			return "void {entityID, entityID} The first ID is the entity who is targetting, the second is the target."
					+ " It can also be set to null to clear the current target.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class get_mob_equipment extends EntityManagement.EntityGetterFunction {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
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
			return "array {entityID} Returns an associative array showing the equipment this mob is wearing."
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
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class set_mob_equipment extends EntityManagement.EntitySetterFunction {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCLivingEntity le = Static.getLivingEntity(args[0], t);
			MCEntityEquipment ee = le.getEquipment();
			if(ee == null) {
				throw new CREBadEntityTypeException("Entities of type \"" + le.getType() + "\" do not have equipment.", t);
			}
			Map<MCEquipmentSlot, MCItemStack> eq = ee.getAllEquipment();
			if(args[1] instanceof CNull) {
				ee.clearEquipment();
				return CVoid.VOID;
			} else if(args[1] instanceof CArray) {
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
			return "void {entityID, array} Takes an associative array with keys representing equipment slots and values"
					+ " of itemArrays, the same used by set_pinv. This does not work on most \"dumb\" entities,"
					+ " only mobs (entities with AI). Unless a mod, plugin, or future update changes vanilla functionality,"
					+ " only humanoid mobs will render their equipment slots. The equipment slots are: "
					+ StringUtils.Join(MCEquipmentSlot.values(), ", ", ", or ", " or ");
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage",
						"set_mob_equipment(spawn_mob('SKELETON')[0], array(WEAPON: array(name: BOW)))",
						"Gives a bow to a skeleton")
			};
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class get_max_health extends EntityManagement.EntityGetterFunction {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCLivingEntity le = Static.getLivingEntity(args[0], t);
			return new CDouble(le.getMaxHealth(), t);
		}

		@Override
		public String getName() {
			return "get_max_health";
		}

		@Override
		public String docs() {
			return "double {entityID} Returns the maximum health of this living entity.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class set_max_health extends EntityManagement.EntitySetterFunction {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
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
			return "void {entityID, double} Sets the max health of a living entity, players included."
					+ " This value is persistent, and will not reset even after server restarts.";
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{new ExampleScript("Basic use",
					"set_max_health('091a595d-3d2f-4df4-b493-951dc4bed7f2', 10.0)",
					"The entity will now only have 5 hearts max (10 half-hearts).")};
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class get_equipment_droprates extends EntityManagement.EntityGetterFunction {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
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
			return "array {entityID} Returns an associative array of the drop rate for each equipment slot."
					+ " If the rate is 0, the equipment will not drop. If it is 1, it is guaranteed to drop.";
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class set_equipment_droprates extends EntityManagement.EntitySetterFunction {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCEntityEquipment ee = Static.getLivingEntity(args[0], t).getEquipment();
			Map<MCEquipmentSlot, Float> eq = ee.getAllDropChances();
			if(ee.getHolder() instanceof MCPlayer) {
				throw new CREBadEntityException(getName() + " does not work on players.", t);
			}
			if(args[1] instanceof CNull) {
				for(Map.Entry<MCEquipmentSlot, Float> ent : eq.entrySet()) {
					eq.put(ent.getKey(), 0F);
				}
			} else if(args[1] instanceof CArray) {
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
			return "void {entityID, array} Sets the drop chances for each equipment slot on a mob,"
					+ " but does not work on players. Passing null instead of an array will automatically"
					+ " set all rates to 0, which will cause nothing to drop. A rate of 1 will guarantee a drop.";
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class can_pickup_items extends EntityManagement.EntityGetterFunction {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			return CBoolean.get(Static.getLivingEntity(args[0], t).getCanPickupItems());
		}

		@Override
		public String getName() {
			return "can_pickup_items";
		}

		@Override
		public String docs() {
			return "boolean {entityID} Returns whether the specified living entity can pick up items.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class set_can_pickup_items extends EntityManagement.EntitySetterFunction {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			Static.getLivingEntity(args[0], t).setCanPickupItems(Static.getBoolean(args[1]));
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "set_can_pickup_items";
		}

		@Override
		public String docs() {
			return "void {entityID, boolean} Sets a living entity's ability to pick up items.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class get_entity_persistence extends EntityManagement.EntityGetterFunction {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			return CBoolean.get(!Static.getLivingEntity(args[0], t).getRemoveWhenFarAway());
		}

		@Override
		public String getName() {
			return "get_entity_persistence";
		}

		@Override
		public String docs() {
			return "boolean {entityID} Returns whether the specified living entity will despawn. True means it will not.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class set_entity_persistence extends EntityManagement.EntitySetterFunction {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			Static.getLivingEntity(args[0], t).setRemoveWhenFarAway(!Static.getBoolean(args[1]));
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "set_entity_persistence";
		}

		@Override
		public String docs() {
			return "void {entityID, boolean} Sets whether a living entity will despawn. True means it will not.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class get_leashholder extends EntityManagement.EntityGetterFunction {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
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
			return "int {entityID} Returns the entityID of the entity that is holding the given living entity's leash,"
					+ " or null if it isn't being held.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class set_leashholder extends EntityManagement.EntitySetterFunction {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
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
			return "void {entityID, entityID} The first entity is the entity to be held on a leash, and must be living."
					+ " The second entity is the holder of the leash. This does not have to be living,"
					+ " but the only non-living entity that will persist as a holder across restarts is the leash hitch."
					+ " Bats, enderdragons, players, and withers can not be held by leashes due to minecraft limitations.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
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
			return "int {entityID} Returns the amount of air the specified living entity has remaining.";
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			return new CInt(Static.getLivingEntity(args[0], t).getRemainingAir(), t);
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
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
			return "void {entityID, int} Sets the amount of air the specified living entity has remaining.";
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			Static.getLivingEntity(args[0], t).setRemainingAir(Static.getInt32(args[1], t));
			return CVoid.VOID;
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
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
			return "int {entityID} Returns the maximum amount of air the specified living entity can have.";
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			return new CInt(Static.getLivingEntity(args[0], t).getMaximumAir(), t);
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
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
			return "void {entityID, int} Sets the maximum amount of air the specified living entity can have.";
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			Static.getLivingEntity(args[0], t).setMaximumAir(Static.getInt32(args[1], t));
			return CVoid.VOID;
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
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
			return "array {entityID, [transparents, [maxDistance]]} Returns an array containing all blocks along the"
					+ " living entity's line of sight. transparents is an array of block IDs, only air by default."
					+ " maxDistance represents the maximum distance to scan. The server may cap the scan distance,"
					+ " but probably by not any less than 100 meters.";
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCLivingEntity entity = Static.getLivingEntity(args[0], t);
			HashSet<Short> transparents = null;
			int maxDistance = 512;
			if(args.length >= 2) {
				CArray givenTransparents = Static.getArray(args[1], t);
				if(givenTransparents.inAssociativeMode()) {
					throw new CRECastException("The array must not be associative.", t);
				}
				transparents = new HashSet<>();
				for(Construct blockID : givenTransparents.asList()) {
					transparents.add(Static.getInt16(blockID, t));
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
		public CHVersion since() {
			return CHVersion.V3_3_1;
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
			return "boolean {entityID, otherEntityID} Returns if the entity can have the other entity in his line of sight."
					+ " For instance for players this mean that it can have the other entity on its screen and that this one is not hidden by opaque blocks."
					+ " This uses the same algorithm that hostile mobs use to find the closest player.";
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			return CBoolean.get(Static.getLivingEntity(args[0], t).hasLineOfSight(Static.getEntity(args[1], t)));
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
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
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCEntity entity = Static.getEntity(args[0], t);

			if(!(entity instanceof MCLivingEntity)) {
				throw new CREBadEntityTypeException("The entity id provided doesn't"
					+ " belong to a living entity", t);
			}

			MCLivingEntity living = (MCLivingEntity)entity;

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
			return "void {entityId, amount, [sourceEntityId]} Damage an entity. If given,"
					+ " the source entity will be attributed as the damager.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class set_entity_gliding extends EntityManagement.EntitySetterFunction {
		public String getName() {
			return "set_entity_gliding";
		}

		public String docs() {
			return "void {entityID, boolean} If possible, makes the entity glide (MC 1.9)";
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCLivingEntity e = Static.getLivingEntity(args[0], t);
			boolean glide = Static.getBoolean(args[1]);

			e.setGliding(glide);

			return CVoid.VOID;
		}

		public Version since() {
			return CHVersion.V3_3_2;
		}
	}

	@api
	public static class get_entity_gliding extends EntityManagement.EntityGetterFunction {
		public String getName() {
			return "get_entity_gliding";
		}

		public String docs() {
			return "boolean {entityID} Returns true if the given entity is gliding (MC 1.9)";
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			return CBoolean.GenerateCBoolean(Static.getLivingEntity(args[0], t).isGliding(), t);
		}

		public Version since() {
			return CHVersion.V3_3_2;
		}
	}
	
	@api
	public static class get_entity_ai extends EntityManagement.EntityGetterFunction {
		public String getName() {
			return "get_entity_ai";
		}

		public String docs() {
			return "boolean {entityID} Returns true if the given entity has AI (MC 1.9.2)";
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			return CBoolean.GenerateCBoolean(Static.getLivingEntity(args[0], t).hasAI(), t);
		}

		public Version since() {
			return CHVersion.V3_3_2;
		}
	}
	
	@api
	public static class set_entity_ai extends EntityManagement.EntitySetterFunction {
		public String getName() {
			return "set_entity_ai";
		}

		public String docs() {
			return "void {entityID, boolean} enables or disables the entity AI (MC 1.9.2)";
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCLivingEntity e = Static.getLivingEntity(args[0], t);
			boolean ai = Static.getBoolean(args[1]);

			e.setAI(ai);

			return CVoid.VOID;
		}

		public Version since() {
			return CHVersion.V3_3_2;
		}
	}
}
