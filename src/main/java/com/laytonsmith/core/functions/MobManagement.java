package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.MCAttributeModifier;
import com.laytonsmith.abstraction.MCLeashable;
import com.laytonsmith.abstraction.MCNamespacedKey;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.MCAnimalTamer;
import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCEntityEquipment;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.entities.MCAgeable;
import com.laytonsmith.abstraction.entities.MCAnimal;
import com.laytonsmith.abstraction.entities.MCArmorStand;
import com.laytonsmith.abstraction.entities.MCBreedable;
import com.laytonsmith.abstraction.entities.MCTameable;
import com.laytonsmith.abstraction.enums.MCAttribute;
import com.laytonsmith.abstraction.enums.MCEquipmentSlot;
import com.laytonsmith.abstraction.enums.MCPotionEffectType;
import com.laytonsmith.abstraction.enums.MCVersion;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.seealso;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Static;
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
import com.laytonsmith.core.exceptions.CRE.CRELengthException;
import com.laytonsmith.core.exceptions.CRE.CRERangeException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.CRE.CREUnageableMobException;
import com.laytonsmith.core.exceptions.CRE.CREUntameableMobException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MobManagement {

	public static String docs() {
		return "These functions manage specifically living entities. If the entity specified is not living, a"
				+ " BadEntityTypeException will be thrown.";
	}

	@api(environments = {CommandHelperEnvironment.class})
	@seealso({get_mob_owner_uuid.class, set_mob_owner.class})
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
					+ " Use {{function|get_mob_owner_uuid}} to get the owner's unique id."
					+ " An UntameableMobException is thrown if mob isn't tameable to begin with.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREUntameableMobException.class, CRELengthException.class,
					CREBadEntityException.class, CREFormatException.class};
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

	@api(environments = {CommandHelperEnvironment.class})
	@seealso({get_mob_owner.class, set_mob_owner.class})
	public static class get_mob_owner_uuid extends AbstractFunction {

		@Override
		public String getName() {
			return "get_mob_owner_uuid";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "string {entityUUID} Returns the owner's UUID, or null if the mob is unowned."
					+ "An UntameableMobException is thrown if mob isn't tameable to begin with.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREUntameableMobException.class, CRELengthException.class,
					CREBadEntityException.class, CREFormatException.class};
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
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCLivingEntity mob = Static.getLivingEntity(args[0], t);
			if(!mob.isTameable()) {
				throw new CREUntameableMobException("The specified entity is not tameable", t);
			}
			MCAnimalTamer owner = ((MCTameable) mob).getOwner();
			if(owner == null) {
				return CNull.NULL;
			} else {
				return new CString(owner.getUniqueID().toString(), t);
			}
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	@seealso({get_mob_owner.class, get_mob_owner_uuid.class})
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
			return "void {entityUUID, player} Sets the tameable mob to the specified player."
					+ " The player argument supports offline players, so it should be a UUID or an exact player name."
					+ " Setting the player to null will untame the mob.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREUntameableMobException.class, CRELengthException.class,
					CREBadEntityException.class, CREFormatException.class};
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
				mct.setOwner(Static.GetUser(player, t));
			}
			return CVoid.VOID;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
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
			double percent = ArgumentValidation.getDouble(args[1], t);
			if(percent < 0 || percent > 100) {
				throw new CRERangeException("Health was expected to be a percentage between 0 and 100", t);
			} else {
				e.setHealth(percent / 100.0 * e.getMaxHealth());
			}
			return CVoid.VOID;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
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

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_entity_breedable extends EntityManagement.EntityGetterFunction {

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCEntity ent = Static.getEntity(args[0], t);

			if(ent instanceof MCBreedable) {
				return CBoolean.get(((MCBreedable) ent).getCanBreed());
			} else {
				throw new CREBadEntityException("Entity ID must be from an breedable entity!", t);
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

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_entity_breedable extends EntityManagement.EntitySetterFunction {

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			boolean breed = ArgumentValidation.getBoolean(args[1], t);

			MCEntity ent = Static.getEntity(args[0], t);

			if(ent instanceof MCBreedable) {
				((MCBreedable) ent).setCanBreed(breed);
			} else {
				throw new CREBadEntityException("Entity ID must be from an breedable entity!", t);
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

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_mob_age extends EntityManagement.EntityGetterFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREUnageableMobException.class, CRELengthException.class,
				CREBadEntityException.class};
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCLivingEntity ent = Static.getLivingEntity(args[0], t);
			if(ent instanceof MCAgeable mob) {
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

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_mob_age extends EntityManagement.EntityFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREUnageableMobException.class, CRECastException.class,
				CREBadEntityException.class, CRELengthException.class};
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			int age = ArgumentValidation.getInt32(args[1], t);
			boolean lock = false;
			if(args.length == 3) {
				lock = ArgumentValidation.getBoolean(args[2], t);
			}
			MCLivingEntity ent = Static.getLivingEntity(args[0], t);
			if(ent instanceof MCAgeable mob) {
				mob.setAge(age);
				if(mob instanceof MCBreedable breedable) {
					breedable.setAgeLock(lock);
				}
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
					+ " that age if lockAge is true, but by default it will not. (locking only applies to breedable mobs)"
					+ " Throws a UnageableMobException if the mob does not age naturally.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_mob_love_ticks extends EntityManagement.EntityGetterFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREBadEntityTypeException.class, CRELengthException.class,
					CREBadEntityException.class};
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCLivingEntity ent = Static.getLivingEntity(args[0], t);
			if(ent instanceof MCAnimal animal) {
				return new CInt(animal.getLoveTicks(), t);
			} else {
				throw new CREBadEntityTypeException("The specified entity cannot be in love.", t);
			}
		}

		@Override
		public String getName() {
			return "get_mob_love_ticks";
		}

		@Override
		public String docs() {
			return "int {entityUUID} Returns the number of ticks remaining that this mob will be in love mode.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_5;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_mob_love_ticks extends EntityManagement.EntitySetterFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREBadEntityTypeException.class, CRECastException.class,
					CREBadEntityException.class, CRELengthException.class};
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCLivingEntity ent = Static.getLivingEntity(args[0], t);
			int ticks = ArgumentValidation.getInt32(args[1], t);
			if(ent instanceof MCAnimal animal) {
				animal.setLoveTicks(ticks);
				return CVoid.VOID;
			} else {
				throw new CREBadEntityTypeException("The specified entity cannot be in love.", t);
			}
		}

		@Override
		public String getName() {
			return "set_mob_love_ticks";
		}

		@Override
		public String docs() {
			return "void {entityUUID, int} Sets the number of ticks that this mob will be in love mode.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_5;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
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

	@api(environments = {CommandHelperEnvironment.class})
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
			return "boolean {entityUUID, potionEffect, [strength], [seconds], [ambient], [particles]}"
					+ " Adds one, or modifies an existing, potion effect on a mob."
					+ " The potionEffect can be " + StringUtils.Join(MCPotionEffectType.types(), ", ", ", or ", " or ")
					+ ". It also accepts an integer corresponding to the effect id listed on the Minecraft wiki."
					+ " Strength is an integer representing the power level of the effect, starting at 0."
					+ " Seconds defaults to 30.0. To remove an effect, set the seconds to 0."
					+ " If seconds is greater than 107374182 a RangeException is thrown."
					+ " Negative seconds makes the effect infinite. (or max in versions prior to 1.19.4)"
					+ " Ambient takes a boolean of whether the particles should be more transparent."
					+ " Particles takes a boolean of whether the particles should be visible at all."
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
					type = MCPotionEffectType.getById(ArgumentValidation.getInt32(args[1], t));
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
				strength = ArgumentValidation.getInt32(args[2], t);

				if(args.length >= 4) {
					seconds = ArgumentValidation.getDouble(args[3], t);
					if(seconds * 20 > Integer.MAX_VALUE) {
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

	@api(environments = {CommandHelperEnvironment.class})
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

	@api(environments = {CommandHelperEnvironment.class})
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

	@api(environments = {CommandHelperEnvironment.class})
	@seealso(set_mob_equipment.class)
	public static class get_mob_equipment extends EntityManagement.EntityGetterFunction {

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCLivingEntity le = Static.getLivingEntity(args[0], t);
			MCEntityEquipment eq = le.getEquipment();
			if(eq == null) {
				throw new CREBadEntityTypeException("Entities of type \"" + le.getType() + "\" do not have equipment.", t);
			}
			Map<MCEquipmentSlot, MCItemStack> eqmap = eq.getAllEquipment();
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
					+ " The equipment slots are: weapon, off_hand, helmet, chestplate, leggings, boots,"
					+ " body (MC 1.20.6+), and saddle (MC 1.21.5+)."
					+ " This works on mobs, players, mannequins, and armor stands.";
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

	@api(environments = {CommandHelperEnvironment.class})
	@seealso(get_mob_equipment.class)
	public static class set_mob_equipment extends EntityManagement.EntitySetterFunction {

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCLivingEntity le = Static.getLivingEntity(args[0], t);
			MCEntityEquipment ee = le.getEquipment();
			if(ee == null) {
				throw new CREBadEntityTypeException("Entities of type \"" + le.getType() + "\" do not have equipment.", t);
			}
			if(args[1] instanceof CNull) {
				ee.clearEquipment();
				return CVoid.VOID;
			}
			Map<MCEquipmentSlot, MCItemStack> eq = ee.getAllEquipment();
			if(args[1].isInstanceOf(CArray.TYPE)) {
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
					+ " values of item arrays. The equipment slots are: weapon, off_hand, helmet, chestplate, leggings,"
					+ " boots, body (MC 1.20.6+), and saddle (MC 1.21.5+)."
					+ " This works on mobs, players, mannequins, and armor stands."
					+ " While you may set any slot for any of these entities, some slots are not used by some entities."
					+ " Setting unused slots is not officially supported behavior, so your results may vary.";
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage",
				"set_mob_equipment(spawn_entity('SKELETON')[0], array(weapon: array(name: 'BOW')))",
				"Gives a bow to a skeleton")
			};
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
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

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_max_health extends EntityManagement.EntitySetterFunction {

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCLivingEntity le = Static.getLivingEntity(args[0], t);
			le.setMaxHealth(ArgumentValidation.getDouble(args[1], t));
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

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_equipment_droprates extends EntityManagement.EntityGetterFunction {

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCLivingEntity le = Static.getLivingEntity(args[0], t);
			if(le instanceof MCPlayer || le instanceof MCArmorStand) {
				throw new CREBadEntityException(getName() + "() does not work on type: " + le.getType(), t);
			}
			MCEntityEquipment eq = le.getEquipment();
			if(eq == null) {
				throw new CREBadEntityTypeException("Entities of type \"" + le.getType() + "\" do not have equipment.", t);
			}
			CArray ret = CArray.GetAssociativeArray(t);
			for(Map.Entry<MCEquipmentSlot, Float> ent : eq.getAllDropChances().entrySet()) {
				ret.set(ent.getKey().name().toLowerCase(), new CDouble(ent.getValue(), t), t);
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
					+ " If the rate is 0.0, the equipment will not drop. A rate of 1.0 will guarantee a drop"
					+ " if the entity is killed by a player. A rate above 1.0 will guarantee a drop by any cause."
					+ " Non-mobs, like players and armor stands, cannot have their drop-rates modified.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_equipment_droprates extends EntityManagement.EntitySetterFunction {

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCLivingEntity le = Static.getLivingEntity(args[0], t);
			if(le instanceof MCPlayer || le instanceof MCArmorStand) {
				throw new CREBadEntityException(getName() + "() does not work on type: " + le.getType(), t);
			}
			MCEntityEquipment ee = le.getEquipment();
			if(ee == null) {
				throw new CREBadEntityTypeException("Entities of type \"" + le.getType() + "\" do not have equipment.", t);
			}
			Map<MCEquipmentSlot, Float> eq = ee.getAllDropChances();
			if(args[1] instanceof CNull) {
				for(Map.Entry<MCEquipmentSlot, Float> ent : eq.entrySet()) {
					eq.put(ent.getKey(), 0F);
				}
			} else if(args[1].isInstanceOf(CArray.TYPE)) {
				CArray ea = (CArray) args[1];
				for(String key : ea.stringKeySet()) {
					try {
						eq.put(MCEquipmentSlot.valueOf(key.toUpperCase()), ArgumentValidation.getDouble32(ea.get(key, t), t));
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
					+ " set all rates to 0.0, which will cause nothing to drop. A rate of 1.0 will guarantee a drop"
					+ " if the entity is killed by a player. A rate above 1.0 will guarantee a drop by any cause."
					+ " Non-mobs, like players and armor stands, cannot have their drop-rates modified.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
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

	@api(environments = {CommandHelperEnvironment.class})
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

	@api(environments = {CommandHelperEnvironment.class})
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
			return "boolean {entityUUID} Returns whether a living entity will despawn when players are far enough away.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
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
			return "void {entityUUID, boolean} Sets whether a living entity will despawn when players are far enough away.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_leashholder extends EntityManagement.EntityGetterFunction {

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCEntity e = Static.getEntity(args[0], t);
			if(!(e instanceof MCLeashable)) {
				throw new CREBadEntityException("Entity type is not leashable.", t);
			}
			if(!((MCLeashable) e).isLeashed()) {
				return CNull.NULL;
			}
			return new CString(((MCLeashable) e).getLeashHolder().getUniqueId().toString(), t);
		}

		@Override
		public String getName() {
			return "get_leashholder";
		}

		@Override
		public String docs() {
			return "string {entityUUID} Returns the UUID of the entity that is holding the given entity's leash,"
					+ " or null if it isn't being held. Only mobs and boats can be leashed, otherwise a"
					+ " BadEntityException is thrown. Boats are only supported on Paper 1.21.3+ and Spigot 1.21.10+.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_leashholder extends EntityManagement.EntitySetterFunction {

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCEntity e = Static.getEntity(args[0], t);
			MCEntity holder;
			if(args[1] instanceof CNull) {
				holder = null;
			} else {
				holder = Static.getEntity(args[1], t);
			}
			if(!(e instanceof MCLeashable)) {
				throw new CREBadEntityException("Entity type is not leashable.", t);
			}
			((MCLeashable) e).setLeashHolder(holder);
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "set_leashholder";
		}

		@Override
		public String docs() {
			return "void {entityUUID, holderUUID} The first argument is the entity to be held on a leash, and must be a"
					+ " mob or a boat. The second is the holder of the leash and can be many types of entities,"
					+ " notably a leash hitch. Certain entity types may be excluded from being leashed depending on the"
					+ " Minecraft version. Boats are only supported on Paper 1.21.3+ and Spigot 1.21.10+.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
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

	@api(environments = {CommandHelperEnvironment.class})
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
			Static.getLivingEntity(args[0], t).setRemainingAir(ArgumentValidation.getInt32(args[1], t));
			return CVoid.VOID;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
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

	@api(environments = {CommandHelperEnvironment.class})
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
			Static.getLivingEntity(args[0], t).setMaximumAir(ArgumentValidation.getInt32(args[1], t));
			return CVoid.VOID;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
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
				CArray givenTransparents = ArgumentValidation.getArray(args[1], t);
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
						material = StaticLayer.GetMaterialFromLegacy(ArgumentValidation.getInt16(mat, t), 0);
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
				maxDistance = ArgumentValidation.getInt32(args[2], t);
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

	@api(environments = {CommandHelperEnvironment.class})
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

	@api(environments = {CommandHelperEnvironment.class})
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

			double damage = ArgumentValidation.getDouble(args[1], t);
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

	@api(environments = {CommandHelperEnvironment.class})
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

	@api(environments = {CommandHelperEnvironment.class})
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

	@api(environments = {CommandHelperEnvironment.class})
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

	@api(environments = {CommandHelperEnvironment.class})
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

	@api(environments = {CommandHelperEnvironment.class})
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

	@api(environments = {CommandHelperEnvironment.class})
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

	@api(environments = {CommandHelperEnvironment.class})
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

	@api(environments = {CommandHelperEnvironment.class})
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

	@api(environments = {CommandHelperEnvironment.class})
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

	@api(environments = {CommandHelperEnvironment.class})
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

	@api(environments = {CommandHelperEnvironment.class})
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
			double base = ArgumentValidation.getDouble(args[2], t);
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

	@api(environments = {CommandHelperEnvironment.class})
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

	@api(environments = {CommandHelperEnvironment.class})
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
			MCAttributeModifier modifier = ObjectGenerator.GetGenerator().attributeModifier(ArgumentValidation.getArray(args[1], t), t);
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

	@api(environments = {CommandHelperEnvironment.class})
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
			return "void {entityUUID, modifier | entityUUID, attribute, id} Removes an attribute modifier from an entity."
					+ " Can provide either a modifier array or just the attribute and namespaced id of the modifier.";
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
				modifier = ObjectGenerator.GetGenerator().attributeModifier(ArgumentValidation.getArray(args[1], t), t);
			} else {
				MCAttribute attribute;
				try {
					attribute = MCAttribute.valueOf(args[1].val());
				} catch (IllegalArgumentException ex) {
					throw new CREFormatException("Invalid attribute name: " + args[1].val(), t);
				}
				List<MCAttributeModifier> modifiers = e.getAttributeModifiers(attribute);
				String id = args[2].val();
				if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_21)) {
					MCNamespacedKey key = null;
					if(id.length() == 36) {
						try {
							UUID.fromString(id);
							key = StaticLayer.GetConvertor().GetNamespacedKey("minecraft:" + id);
						} catch (IllegalArgumentException ex) {
							// not legacy UUID
						}
					}
					if(key == null) {
						key = StaticLayer.GetConvertor().GetNamespacedKey(id);
					}
					for(MCAttributeModifier m : modifiers) {
						if(m.getKey().equals(key)) {
							modifier = m;
							break;
						}
					}
				} else {
					UUID uuid = null;
					if(id.length() == 36) {
						try {
							uuid = UUID.fromString(id);
						} catch (IllegalArgumentException ex) {
							// not UUID
						}
					}
					for(MCAttributeModifier m : modifiers) {
						if(uuid != null && m.getUniqueId().compareTo(uuid) == 0
								|| uuid == null && id.equals(m.getAttributeName())) {
							modifier = m;
							break;
						}
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

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_entity_immunity_ticks extends EntityManagement.EntityGetterFunction {

		@Override
		public String getName() {
			return "get_entity_immunity_ticks";
		}

		@Override
		public String docs() {
			return "int {entityUUID} Gets the number of immunity ticks a living entity has remaining."
					+ " After being damaged, an entity is given 10 ticks of immunity from equal or lesser damage.";
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			return new CInt(Static.getLivingEntity(args[0], t).getNoDamageTicks(), t);
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_4;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_entity_immunity_ticks extends EntityManagement.EntitySetterFunction {

		@Override
		public String getName() {
			return "set_entity_immunity_ticks";
		}

		@Override
		public String docs() {
			return "void {entityUUID, int} Sets the number of immunity ticks a living entity has remaining.";
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Static.getLivingEntity(args[0], t).setNoDamageTicks(ArgumentValidation.getInt32(args[1], t));
			return CVoid.VOID;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_4;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class is_entity_sleeping extends EntityManagement.EntityGetterFunction {

		@Override
		public String getName() {
			return "is_entity_sleeping";
		}

		@Override
		public String docs() {
			return "boolean {entityUUID} Gets if a living entity is sleeping or not."
					+ " Only some entity types can sleep.";
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			return CBoolean.get(Static.getLivingEntity(args[0], t).isSleeping());
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_4;
		}
	}
}
