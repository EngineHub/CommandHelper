package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Vector3D;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.MCAgeable;
import com.laytonsmith.abstraction.MCArmorStand;
import com.laytonsmith.abstraction.MCBlockCommandSender;
import com.laytonsmith.abstraction.MCChunk;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCEnderCrystal;
import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCEntityEquipment;
import com.laytonsmith.abstraction.MCExperienceOrb;
import com.laytonsmith.abstraction.MCFireball;
import com.laytonsmith.abstraction.MCHumanEntity;
import com.laytonsmith.abstraction.MCItem;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLightningStrike;
import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCMaterialData;
import com.laytonsmith.abstraction.MCPainting;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCPotionData;
import com.laytonsmith.abstraction.MCProjectile;
import com.laytonsmith.abstraction.MCProjectileSource;
import com.laytonsmith.abstraction.MCTNT;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCBlockFace;
import com.laytonsmith.abstraction.blocks.MCBlockProjectileSource;
import com.laytonsmith.abstraction.entities.*;
import com.laytonsmith.abstraction.entities.MCHorse.MCHorseColor;
import com.laytonsmith.abstraction.entities.MCHorse.MCHorsePattern;
import com.laytonsmith.abstraction.entities.MCHorse.MCHorseVariant;
import com.laytonsmith.abstraction.enums.MCArt;
import com.laytonsmith.abstraction.enums.MCBodyPart;
import com.laytonsmith.abstraction.enums.MCDyeColor;
import com.laytonsmith.abstraction.enums.MCEntityEffect;
import com.laytonsmith.abstraction.enums.MCEntityType;
import com.laytonsmith.abstraction.enums.MCEquipmentSlot;
import com.laytonsmith.abstraction.enums.MCOcelotType;
import com.laytonsmith.abstraction.enums.MCParticle;
import com.laytonsmith.abstraction.enums.MCProfession;
import com.laytonsmith.abstraction.enums.MCProjectileType;
import com.laytonsmith.abstraction.enums.MCRabbitType;
import com.laytonsmith.abstraction.enums.MCRotation;
import com.laytonsmith.abstraction.enums.MCSkeletonType;
import com.laytonsmith.abstraction.enums.MCVersion;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.seealso;
import com.laytonsmith.core.ArgumentValidation;
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
import com.laytonsmith.core.exceptions.CRE.CREIndexOverflowException;
import com.laytonsmith.core.exceptions.CRE.CREInvalidWorldException;
import com.laytonsmith.core.exceptions.CRE.CRELengthException;
import com.laytonsmith.core.exceptions.CRE.CRENotFoundException;
import com.laytonsmith.core.exceptions.CRE.CREPlayerOfflineException;
import com.laytonsmith.core.exceptions.CRE.CRERangeException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.CRE.CREUnageableMobException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author jb_aero
 */
public class EntityManagement {
	public static String docs(){
        return "This class of functions allow entities to be managed.";
    }

	public static abstract class EntityFunction extends AbstractFunction {
		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}
	}

	public static abstract class EntityGetterFunction extends EntityFunction {
		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRELengthException.class, CREBadEntityException.class};
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}
	}

	public static abstract class EntitySetterFunction extends EntityFunction {
		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class, CRELengthException.class,
					CREBadEntityException.class};
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}
	}

	@api
	public static class all_entities extends EntityFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInvalidWorldException.class, CREFormatException.class,
					CRECastException.class};
		}

		@Override
		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			CArray ret = new CArray(t);
			if (args.length == 0) {
				for (MCWorld w : Static.getServer().getWorlds()) {
					for (MCEntity e : w.getEntities()) {
						ret.push(new CString(e.getUniqueId().toString(), t), t);
					}
				}
			} else {
				MCWorld w;
				MCChunk c;
				if (args.length == 3) {
					w = Static.getServer().getWorld(args[0].val());
					if (w == null) {
						throw new CREInvalidWorldException("Unknown world: " + args[0].val(), t);
					}
					try {
						int x = Static.getInt32(args[1], t);
						int z = Static.getInt32(args[2], t);
						c = w.getChunkAt(x, z);
					} catch (ConfigRuntimeException cre) {
						CArray l = CArray.GetAssociativeArray(t);
						l.set("x", args[1], t);
						l.set("z", args[2], t);
						c = w.getChunkAt(ObjectGenerator.GetGenerator().location(l, w, t));
					}
					for (MCEntity e : c.getEntities()) {
						ret.push(new CString(e.getUniqueId().toString(), t), t);
					}
				} else {
					if (args[0] instanceof CArray) {
						c = ObjectGenerator.GetGenerator().location(args[0], null, t).getChunk();
						for (MCEntity e : c.getEntities()) {
							ret.push(new CString(e.getUniqueId().toString(), t), t);
						}
					} else {
						w = Static.getServer().getWorld(args[0].val());
						if (w == null) {
							throw new CREInvalidWorldException("Unknown world: " + args[0].val(), t);
						}
						for (MCEntity e : w.getEntities()) {
							ret.push(new CString(e.getUniqueId().toString(), t), t);
						}
					}
				}
			}
			return ret;
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public String getName() {
			return "all_entities";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1, 3};
		}

		@Override
		public String docs() {
			return "array {[world, [x, z]] | [locationArray]} Returns an array of IDs for all entities in the given"
					+ " scope. With no args, this will return all entities loaded on the entire server. If the first"
					+ " argument is given and is a location, only entities in the chunk containin that location will"
					+ " be returned, or if it is a world only entities in that world will be returned. If all three"
					+ " arguments are given, only entities in the chunk with those coords will be returned. This can"
					+ " take chunk coords (ints) or location coords (doubles).";
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
					new ExampleScript("Getting all entities in a world", "msg(all_entities(pworld()))",
							"Sends you an array of all entities in your world."),
					new ExampleScript("Getting entities in a chunk", "msg(all_entities(pworld(), 5, -3))",
							"Sends you an array of all entities in chunk (5,-3)."),
					new ExampleScript("Getting entities in your chunk", "msg(all_entities(ploc()))",
							"Sends you an array of all entities in the chunk you are in.")
			};
		}

	}

	@api
	public static class entity_exists extends EntityGetterFunction {

		@Override
		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCEntity e;
			try {
				e = Static.getEntity(args[0], t);
			} catch (ConfigRuntimeException cre) {
				return CBoolean.FALSE;
			}
			return CBoolean.TRUE;
		}

		@Override
		public String getName() {
			return "entity_exists";
		}

		@Override
		public String docs() {
			return "boolean {entityID} Returns true if entity exists, otherwise false.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class is_entity_living extends EntityGetterFunction {

		@Override
		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCEntity e;

			try {
				e = Static.getEntity(args[0], t);
			} catch (ConfigRuntimeException cre) {
				return CBoolean.FALSE;
			}

			return CBoolean.get(e instanceof MCLivingEntity);
		}

		@Override
		public String getName() {
			return "is_entity_living";
		}

		@Override
		public String docs() {
			return "boolean {entityID} Returns true if entity is living, otherwise false.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class entity_loc extends EntityGetterFunction {

		@Override
		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCEntity e = Static.getEntity(args[0], t);
			return ObjectGenerator.GetGenerator().location(e.getLocation());
		}

		@Override
		public String getName() {
			return "entity_loc";
		}

		@Override
		public String docs() {
			return "locationArray {entityID} Returns the location array for this entity, if it exists."
					+ " This array will be compatible with any function that expects a location.";
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
					new ExampleScript("Sample output", "entity_loc(5048)",
							"{0: -3451.96, 1: 65.0, 2: 718.521, 3: world, 4: -170.9, 5: 35.5, pitch: 35.5,"
							+ " world: world, x: -3451.96, y: 65.0, yaw: -170.9, z: 718.521}")
			};
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

	}

	@api
	public static class set_entity_loc extends EntitySetterFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREBadEntityException.class, CREFormatException.class,
					CRECastException.class, CREInvalidWorldException.class, CRELengthException.class};
		}

		@Override
		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCEntity e = Static.getEntity(args[0], t);
			MCLocation l;
			if (args[1] instanceof CArray) {
				l = ObjectGenerator.GetGenerator().location((CArray) args[1], e.getWorld(), t);
			} else {
				throw new CREFormatException("An array was expected but recieved " + args[1], t);
			}
			return CBoolean.get(e.teleport(l));
		}

		@Override
		public String getName() {
			return "set_entity_loc";
		}

		@Override
		public String docs() {
			return "boolean {entityID, locationArray} Teleports the entity to the given location and returns whether"
					+ " the action was successful. Note this can set both location and direction.";
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Teleporting an entity to you", "set_entity_loc(386, ploc())",
						"The entity will teleport to the block you are standing on."),
				new ExampleScript("Teleporting an entity to another", "set_entity_loc(201, entity_location(10653))",
						"The entity will teleport to the other and face the same direction, if they both exist."),
				new ExampleScript("Setting location with a normal array",
						"set_entity_loc(465, array(214, 64, 1812, 'world', -170, 10))", "This set location and direction."),
				new ExampleScript("Setting location with an associative array",
						"set_entity_loc(852, array(x: 214, y: 64, z: 1812, world: 'world', yaw: -170, pitch: 10))",
						"This also sets location and direction")
			};
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

	}

	@api
	public static class entity_velocity extends EntityGetterFunction {

		@Override
		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {

			MCEntity e = Static.getEntity(args[0], t);
			CArray va = ObjectGenerator.GetGenerator().vector(e.getVelocity(), t);
			va.set("magnitude", new CDouble(e.getVelocity().length(), t), t);
			return va;
		}

		@Override
		public String getName() {
			return "entity_velocity";
		}

		@Override
		public String docs() {
			return "array {entityID} Returns an associative array indicating the x/y/z components of this entity's velocity."
					+ " As a convenience, the magnitude is also included.";
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
					new ExampleScript("A stationary entity", "msg(entity_velocity(235))",
							"{magnitude: 0.0, x: 0.0, y: 0.0, z: 0.0}")
			};
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

	}

	@api
	public static class set_entity_velocity extends EntitySetterFunction {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCEntity e = Static.getEntity(args[0], t);
			e.setVelocity(ObjectGenerator.GetGenerator().vector(args[1], t));
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "set_entity_velocity";
		}

		@Override
		public String docs() {
			return "void {entityID, array} Sets the velocity of this entity according to the supplied xyz array. All 3"
					+ " values default to 0, so an empty array will simply stop the entity's motion. Both normal and"
					+ " associative arrays are accepted.";
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
					new ExampleScript("Setting a bounce with a normal array", "set_entity_velocity(235, array(0, 0.5, 0))",
							"The entity just hopped, unless it was an item frame or painting."),
					new ExampleScript("Setting a bounce with an associative array", "set_entity_velocity(235, array(y: 0.5))",
							"The entity just hopped, unless it was an item frame or painting.")
			};
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

	}

	@api
	public static class entity_remove extends EntityGetterFunction {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCEntity ent = Static.getEntity(args[0], t);
			if (ent == null) {
				return CVoid.VOID;
			} else if (ent instanceof MCHumanEntity) {
				throw new CREBadEntityException("Cannot remove human entity (" + ent.getUniqueId() + ")!", t);
			} else {
				ent.remove();
				return CVoid.VOID;
			}
		}

		@Override
		public String getName() {
			return "entity_remove";
		}

		@Override
		public String docs() {
			return "void {entityID} Removes the specified entity from the world, without any drops or animations. "
				+ "Note: you can't remove players. As a safety measure for working with NPC plugins, it will "
				+ "not work on anything human, even if it is not a player.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

	}

	@api
	public static class entity_type extends EntityGetterFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCEntity ent;
			try {
				ent = Static.getEntity(args[0], t);
			} catch (ConfigRuntimeException cre) {
				return CNull.NULL;
			}
			return new CString(ent.getType().name(), t);
		}

		@Override
		public String getName() {
			return "entity_type";
		}

		@Override
		public String docs() {
			return "mixed {entityID} Returns the EntityType of the entity with the specified ID."
					+ " Returns null if the entity does not exist.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class get_entity_breedable extends EntityGetterFunction {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCEntity ent = Static.getEntity(args[0], t);

			if (ent instanceof MCAgeable){
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
	public static class set_entity_breedable extends EntitySetterFunction {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			boolean breed = Static.getBoolean(args[1]);

			MCEntity ent = Static.getEntity(args[0], t);

			if (ent instanceof MCAgeable){
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
	public static class get_entity_age extends EntityGetterFunction {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCEntity ent = Static.getEntity(args[0], t);
			if (ent == null) {
				return CNull.NULL;
			} else {
				return new CInt(ent.getTicksLived(), t);
			}
		}

		@Override
		public String getName() {
			return "get_entity_age";
		}

		@Override
		public String docs() {
			return "int {entityID} Returns the entity age as an integer, represented by server ticks.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class set_entity_age extends EntitySetterFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREBadEntityException.class,
					CRERangeException.class, CRELengthException.class};
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			int age = Static.getInt32(args[1], t);

			if (age < 1) {
				throw new CRERangeException("Entity age can't be less than 1 server tick.", t);
			}

			MCEntity ent = Static.getEntity(args[0], t);
			if (ent == null) {
				return CNull.NULL;
			} else {
				ent.setTicksLived(age);
				return CVoid.VOID;
			}
		}

		@Override
		public String getName() {
			return "set_entity_age";
		}

		@Override
		public String docs() {
			return "void {entityID, int} Sets the age of the entity to the specified int, represented by server ticks.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class get_mob_age extends EntityGetterFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREUnageableMobException.class, CRELengthException.class,
					CREBadEntityException.class};
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCLivingEntity ent = Static.getLivingEntity(args[0], t);
			if (ent == null) {
				return CNull.NULL;
			} else if (ent instanceof MCAgeable) {
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
	public static class set_mob_age extends EntityFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREUnageableMobException.class, CRECastException.class,
					CREBadEntityException.class, CRELengthException.class};
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			int age = Static.getInt32(args[1], t);
			boolean lock = false;
			if (args.length == 3) {
				lock = (boolean) Static.getBoolean(args[2]);
			}
			MCLivingEntity ent = Static.getLivingEntity(args[0], t);
			if (ent == null) {
				return CNull.NULL;
			} else if (ent instanceof MCAgeable) {
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
	public static class get_mob_effects extends EntityGetterFunction {

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
			return new ExampleScript[]{new ExampleScript("Basic use", "msg(get_mob_effects(259))",
					"{{ambient: false, id: 1, seconds: 30, strength: 1}}")};
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

	}

	@api
	public static class set_mob_effect extends EntityFunction {

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
					+ " defaults to 30. If the potionID is out of range, a RangeException is thrown, because out of"
					+ " range potion effects cause the client to crash, fairly hardcore. See"
					+ " http://www.minecraftwiki.net/wiki/Potion_effects for a complete list of potions that can be"
					+ " added. To remove an effect, set the seconds to 0. Strength is the number of levels to add to the"
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
			int seconds = 30;
			boolean ambient = false;
			boolean particles = true;
			if (args.length >= 4) {
				seconds = Static.getInt32(args[3], t);
			}
			if (args.length == 5) {
				ambient = Static.getBoolean(args[4]);
			}
			if (args.length == 6) {
				particles = Static.getBoolean(args[5]);
			}

			if (seconds == 0) {
				return CBoolean.get(mob.removeEffect(effect));
			} else {
				mob.addEffect(effect, strength, seconds, ambient, particles, t);
				return CBoolean.TRUE;
			}
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api(environments={CommandHelperEnvironment.class})
	public static class shoot_projectile extends EntityFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREBadEntityException.class, CREBadEntityTypeException.class,
				CREFormatException.class, CREPlayerOfflineException.class};
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {

			MCPlayer p = env.getEnv(CommandHelperEnvironment.class).GetPlayer();

			MCLivingEntity shooter = null;
			MCLivingEntity target;

			UUID shooter_id = null;
			UUID target_id = null;

			MCLocation from = null;
			MCLocation to = null;

			MCLocation shifted_from;

			MCEntityType entity_shoot = null;
			MCProjectileType projectile_shoot = null;

			double speed = 0.0;

			if (args.length >= 1) {
				try {
					shooter_id = Static.GetPlayer(args[0], t).getUniqueId();
				} catch (ConfigRuntimeException notPlayer) {
					try {
						shooter_id = Static.GetUUID(args[0], t);
					} catch (ConfigRuntimeException notEntIdEither) {
					}
				}

				if (shooter_id == null) {
					try {
						from = ObjectGenerator.GetGenerator().location(args[0], p != null ? p.getWorld() : null, t);
					} catch (ConfigRuntimeException badLocation) {
					}
				}

				if (shooter_id == null && from == null) {
					throw new CREFormatException("Could not find an entity or location matching " + args[0] + "!", t);
				}
			} else {
				Static.AssertPlayerNonNull(p, t);
				shooter_id = p.getUniqueId();
			}

			if (args.length >= 3) {

				try {
					target_id = Static.GetPlayer(args[2], t).getUniqueId();
				} catch (ConfigRuntimeException notPlayer) {
					try {
						target_id = Static.GetUUID(args[2], t);
					} catch (ConfigRuntimeException notEntIdEither) {
					}
				}

				if (target_id == null) {
					try {
						to = ObjectGenerator.GetGenerator().location(args[2], null, t);
					} catch (ConfigRuntimeException badLocation) {
					}
				}

				if (target_id == null && to == null) {
					throw new CREFormatException("Could not find an entity or location matching " + args[2] + " for target!", t);
				}
			}

			if (args.length == 4) {
				speed = Static.getDouble(args[3], t);
			}

			if (shooter_id != null) {
				shooter = Static.getLivingByUUID(shooter_id, t);
				from = shooter.getEyeLocation();
			}

			if (target_id != null) {
				target = Static.getLivingByUUID(target_id, t);
				to = target.getEyeLocation();
			}

			if (args.length >= 2) {

				if (shooter_id != null && to == null) {
					try {
						projectile_shoot = MCProjectileType.valueOf(args[1].val().toUpperCase());
					} catch (IllegalArgumentException badEnum) {
						throw new CREFormatException(args[1] + " is not a valid Projectile", t);
					}
				} else {
					try {
						entity_shoot = MCEntityType.valueOf(args[1].val().toUpperCase());
					} catch (IllegalArgumentException badEnum) {
						throw new CREBadEntityTypeException(args[1] + " is not a valid entity type", t);
					}
				}
			} else {
				if (shooter_id != null && to == null) {
					projectile_shoot = MCProjectileType.FIREBALL;
				} else {
					entity_shoot = MCEntityType.valueOfVanillaType(MCEntityType.MCVanillaEntityType.FIREBALL);
				}
			}

			if (args.length < 3 && shooter_id == null) {
				throw new CREFormatException("You must specify target location if you want shoot from location, not entity.", t);
			}

			if (shooter_id != null && to == null) {
				MCProjectile projectile = shooter.launchProjectile(projectile_shoot);

				return new CString(projectile.getUniqueId().toString(), t);
			} else {
				Vector3D velocity = to.toVector().subtract(from.toVector()).normalize();

				if (shooter_id != null) {
					shifted_from = from.add(velocity);
				} else {
					shifted_from = from;
				}

				MCEntity entity = from.getWorld().spawn(shifted_from, entity_shoot);

				if (speed == 0.0) {
					entity.setVelocity(velocity);
				} else {
					entity.setVelocity(velocity.multiply(speed));
				}

				return new CString(entity.getUniqueId().toString(), t);
			}
		}

		@Override
		public String getName() {
			return "shoot_projectile";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1, 2, 3, 4};
		}

		@Override
		public String docs() {
			return "int {[entity[, projectile]] | player, projectile, target[, speed]} shoots an entity from the"
					+ " specified location (can be entityID, player name or location array), or the current player"
					+ " if no arguments are passed. If no entity type is specified, it defaults to a fireball."
					+ " If provide three arguments, with target (entityID, player name or location array), entity will"
					+ " shoot to target location. Last, fourth argument, is double and specifies the speed"
					+ " of projectile. Returns the EntityID of the entity. Valid projectile types: "
					+ StringUtils.Join(MCProjectileType.values(), ", ", ", or ", " or ");
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class entities_in_radius extends EntityFunction {

		@Override
		public String getName() {
			return "entities_in_radius";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			MCPlayer p = env.getEnv(CommandHelperEnvironment.class).GetPlayer();

			MCLocation loc;
			int dist;
			List<String> types = new ArrayList<String>();

			if (!(args[0] instanceof CArray)) {
				throw new CREBadEntityException("Expecting an array at parameter 1 of entities_in_radius", t);
			}

			loc = ObjectGenerator.GetGenerator().location(args[0], p != null ? p.getWorld() : null, t);
			dist = Static.getInt32(args[1], t);

			if (args.length == 3) {
				if (args[2] instanceof CArray) {
					CArray ta = (CArray) args[2];
					for (int i = 0; i < ta.size(); i++) {
						types.add(ta.get(i, t).val());
					}
				} else {
					types.add(args[2].val());
				}

				types = prepareTypes(t, types);
			}

			// The idea and code comes from skore87 (http://forums.bukkit.org/members/skore87.105075/)
			// http://forums.bukkit.org/threads/getnearbyentities-of-a-location.101499/#post-1341141
			int chunkRadius = dist < 16 ? 1 : (dist - (dist % 16)) / 16;

			Set<UUID> eSet = new HashSet<>();
			for (int chX = 0 - chunkRadius; chX <= chunkRadius; chX++) {
				for (int chZ = 0 - chunkRadius; chZ <= chunkRadius; chZ++) {
					MCLocation nl = StaticLayer.GetLocation(loc.getWorld(), loc.getX()+(chX*16), loc.getY(), loc.getZ()+(chZ*16));
					for (MCEntity e : nl.getChunk().getEntities()) {
						if (!e.getWorld().equals(loc.getWorld())) {
							// We can't measure entity distances that are in different worlds!
							continue;
						}
						if (e.getLocation().distance(loc) <= dist && e.getLocation().getBlock() != loc.getBlock()) {
							if (types.isEmpty() || types.contains(e.getType().name())) {
								eSet.add(e.getUniqueId());
							}
						}
					}
				}
			}
			CArray entities = new CArray(t);
			for (UUID e : eSet) {
				entities.push(new CString(e.toString(), t), t);
			}

			return entities;
		}

		private List<String> prepareTypes(Target t, List<String> types) {

			List<String> newTypes = new ArrayList<String>();
			MCEntityType entityType = null;

			for (String type : types) {

				try {
					entityType = MCEntityType.valueOf(type.toUpperCase());
				} catch (IllegalArgumentException e) {
					throw new CREBadEntityException(String.format("Wrong entity type: %s", type), t);
				}

				newTypes.add(entityType.name());
			}

			return newTypes;
		}

		@Override
		public String docs() {
			return "array {location array, distance, [type] | location array, distance, [arrayTypes]} Returns an array of"
					+ " all entities within the given radius. Set type argument to filter entities to a specific type. You"
					+ " can pass an array of types. Valid types (case doesn't matter): "
					+ StringUtils.Join(MCEntityType.types(), ", ", ", or ", " or ");
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREBadEntityException.class,
					CREFormatException.class};
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	//@api
	public static class get_mob_target extends EntityGetterFunction {

		@Override
		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCLivingEntity le = Static.getLivingEntity(args[0], t);
			if (le.getTarget(t) == null) {
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
	public static class set_mob_target extends EntitySetterFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREBadEntityException.class, CRELengthException.class};
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCLivingEntity le = Static.getLivingEntity(args[0], t);
			MCLivingEntity target = null;
			if (!(args[1] instanceof CNull)) {
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
	public static class get_mob_equipment extends EntityGetterFunction {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCLivingEntity le = Static.getLivingEntity(args[0], t);
			MCEntityEquipment eq = le.getEquipment();
			if (eq == null) {
				throw new CREBadEntityTypeException("Entities of type \"" + le.getType() + "\" do not have equipment.", t);
			}
			Map<MCEquipmentSlot, MCItemStack> eqmap = le.getEquipment().getAllEquipment();
			CArray ret = CArray.GetAssociativeArray(t);
			for (MCEquipmentSlot key : eqmap.keySet()) {
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
			return "equipmentArray {entityID} Returns an associative array showing the equipment this mob is wearing."
					+ " This does not work on most \"dumb\" entities, only mobs (entities with AI).";
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
					new ExampleScript("Getting a mob's equipment", "get_mob_equipment(276)", "{boots: null,"
							+ " chestplate: null, helmet: {data: 0, enchants: {} meta: null, type: 91}, leggings: null,"
							+ " off_hand: null, weapon: {data: 5, enchants: {} meta: {display: Excalibur, lore: null},"
							+ " type: 276}}")
			};
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class set_mob_equipment extends EntitySetterFunction {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCLivingEntity le = Static.getLivingEntity(args[0], t);
			MCEntityEquipment ee = le.getEquipment();
			if (ee == null) {
				throw new CREBadEntityTypeException("Entities of type \"" + le.getType() + "\" do not have equipment.", t);
			}
			Map<MCEquipmentSlot, MCItemStack> eq = ee.getAllEquipment();
			if (args[1] instanceof CNull) {
				ee.clearEquipment();
				return CVoid.VOID;
			} else if (args[1] instanceof CArray) {
				CArray ea = (CArray) args[1];
				for (String key : ea.stringKeySet()) {
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
				new ExampleScript("Basic usage", "set_mob_equipment(spawn_mob('SKELETON')[0], array(WEAPON: array(type: 261)))", "Gives a bow to a skeleton")
			};
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class get_max_health extends EntityGetterFunction {

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
	public static class set_max_health extends EntitySetterFunction {

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
					"set_max_health(256, 10)", "The entity will now only have 5 hearts max.")};
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class entity_onfire extends EntityGetterFunction {

		@Override
		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCEntity ent = Static.getEntity(args[0], t);
			return new CInt(Static.ticksToMs(ent.getFireTicks())/1000, t);
		}

		@Override
		public String getName() {
			return "entity_onfire";
		}

		@Override
		public String docs() {
			return "int {entityID} Returns the number of seconds until this entity"
					+ " stops being on fire, 0 if it already isn't.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

	}

	@api
	public static class set_entity_onfire extends EntitySetterFunction {

		@Override
		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCEntity ent = Static.getEntity(args[0], t);
			int setTicks = (int) Static.msToTicks(Static.getInt(args[1], t)*1000);
			if (setTicks < 0) {
				throw new CREFormatException("Seconds cannot be less than 0", t);
			}
			ent.setFireTicks(setTicks);
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "set_entity_onfire";
		}

		@Override
		public String docs() {
			return "void {entityID, seconds} Sets the entity on fire for the"
					+ " given number of seconds. Throws a FormatException"
					+ " if seconds is less than 0.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

	}

	@api
	public static class play_entity_effect extends EntitySetterFunction {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCEntity ent = Static.getEntity(args[0], t);
			MCEntityEffect mee;
			try {
				mee = MCEntityEffect.valueOf(args[1].val().toUpperCase());
			} catch (IllegalArgumentException iae) {
				throw new CREFormatException("Unknown effect at arg 2.", t);
			}
			ent.playEffect(mee);
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "play_entity_effect";
		}

		@Override
		public String docs() {
			return "void {entityID, effect} Plays the given visual effect on the"
					+ " entity. Non-applicable effects simply won't happen. Note:"
					+ " the death effect makes the mob invisible to players and"
					+ " immune to melee attacks. When used on players, they are"
					+ " shown the respawn menu, but because they are not actually"
					+ " dead, they can only log out. Possible effects are: "
					+ StringUtils.Join(MCEntityEffect.values(), ", ", ", or ", " or ");
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

	}

	@api
	public static class get_mob_name extends EntityGetterFunction {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCEntity le = Static.getEntity(args[0], t);
			try {
				return new CString(le.getCustomName(), t);
			} catch (IllegalArgumentException e) {
				throw new CRECastException(e.getMessage(), t);
			}
		}

		@Override
		public String getName() {
			return "get_mob_name";
		}

		@Override
		public String docs() {
			return "string {entityID} Returns the name of the given mob.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class set_mob_name extends EntitySetterFunction {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCEntity le = Static.getEntity(args[0], t);
			try {
				le.setCustomName(args[1].val());
			} catch (IllegalArgumentException e) {
				throw new CRECastException(e.getMessage(), t);
			}
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "set_mob_name";
		}

		@Override
		public String docs() {
			return "void {entityID, name} Sets the name of the given mob. This"
					+ " automatically truncates if it is more than 64 characters.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class spawn_entity extends EntityFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREFormatException.class,
					CREBadEntityException.class, CREInvalidWorldException.class,
					CREPlayerOfflineException.class, CRENotFoundException.class};
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCCommandSender cs = environment.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			int qty = 1;
			CArray ret = new CArray(t);
			MCEntityType entType;
			MCLocation l;
			MCEntity ent;
			if (args.length == 3) {
				l = ObjectGenerator.GetGenerator().location(args[2], null, t);
			} else {
				if (cs instanceof MCPlayer) {
					l = ((MCPlayer) cs).getLocation();
				} else if (cs instanceof MCBlockCommandSender){
					l = ((MCBlockCommandSender) cs).getBlock().getRelative(MCBlockFace.UP).getLocation();
				} else {
					throw new CREPlayerOfflineException("A physical commandsender must exist or location must be explicit.", t);
				}
			}
			if (args.length >= 2) {
				qty = Static.getInt32(args[1], t);
			}
			try {
				entType = MCEntityType.valueOf(args[0].val().toUpperCase());
				if (entType == null) {
					throw new CRENotFoundException(
							"Could not find the entity type internal object (are you running in cmdline mode?)", t);
				}
				if (!entType.isSpawnable()) {
					throw new CREFormatException("Unspawnable entitytype: " + args[0].val(), t);
				}
			} catch (IllegalArgumentException iae) {
				throw new CREFormatException("Unknown entitytype: " + args[0].val(), t);
			}
			for (int i = 0; i < qty; i++) {
				switch (entType.getAbstracted()) {
					case DROPPED_ITEM:
						CArray c = CArray.GetAssociativeArray(t);
						c.set("type", new CInt(1, t), t);
						c.set("qty", new CInt(qty, t), t);
						MCItemStack is = ObjectGenerator.GetGenerator().item(c, t);
						ent = l.getWorld().dropItem(l, is);
						qty = 0;
						break;
					case FALLING_BLOCK:
						ent = l.getWorld().spawnFallingBlock(l, 12, (byte) 0);
						break;
					case ITEM_FRAME:
					case LEASH_HITCH:
					case PAINTING:
						try {
							ent = l.getWorld().spawn(l.getBlock().getLocation(), entType);
						} catch(NullPointerException | IllegalArgumentException ex){
							throw new CREFormatException("Unspawnable location for " + entType.getAbstracted().name(), t);
						}
						break;
					default:
						ent = l.getWorld().spawn(l, entType);
				}
				ret.push(new CString(ent.getUniqueId().toString(), t), t);
			}
			return ret;
		}

		@Override
		public String getName() {
			return "spawn_entity";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3};
		}

		@Override
		public String docs() {
			List<String> spawnable = new ArrayList<String>();
			for (MCEntityType type : MCEntityType.values()) {
				if (type.isSpawnable()) {
					spawnable.add(type.name());
				}
			}
			return "array {entityType, [qty], [location]} Spawns the specified number of entities of the given type"
					+ " at the given location. Returns an array of entityIDs of what is spawned. Qty defaults to 1"
					+ " and location defaults to the location of the commandsender, if it is a block or player."
					+ " If the commandsender is console, location must be supplied. ---- Entitytype can be one of "
					+ StringUtils.Join(spawnable, ", ", " or ", ", or ")
					+ ". Falling_blocks will be sand by default, and dropped_items will be stone,"
					+ " as these entities already have their own functions for spawning.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class set_entity_rider extends EntitySetterFunction {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCEntity horse, rider;
			boolean success;
			if (args[0] instanceof CNull) {
				horse = null;
			} else {
				horse = Static.getEntity(args[0], t);
			}
			if (args[1] instanceof CNull) {
				rider = null;
			} else {
				rider = Static.getEntity(args[1], t);
			}
			if ((horse == null && rider == null) || horse == rider) {
				throw new CREFormatException("Horse and rider cannot be the same entity", t);
			} else if (horse == null) {
				success = rider.leaveVehicle();
			} else if (rider == null) {
				success = horse.eject();
			} else {
				success = horse.setPassenger(rider);
			}
			return CBoolean.get(success);
		}

		@Override
		public String getName() {
			return "set_entity_rider";
		}

		@Override
		public String docs() {
			return "boolean {horse, rider} Sets the way two entities are stacked. Horse and rider are entity ids."
					+ " If rider is null, horse will eject its current rider, if it has one. If horse is null,"
					+ " rider will leave whatever it is riding. If horse and rider are both valid entities,"
					+ " rider will ride horse. The function returns the success of whatever operation is done."
					+ " If horse and rider are both null, or otherwise the same, a FormatException is thrown.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class get_entity_rider extends EntityGetterFunction {

		@Override
		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCEntity ent = Static.getEntity(args[0], t);
			if (ent.getPassenger() != null) {
				return new CString(ent.getPassenger().getUniqueId().toString(), t);
			}
			return CNull.NULL;
		}

		@Override
		public String getName() {
			return "get_entity_rider";
		}

		@Override
		public String docs() {
			return "mixed {entityID} Returns the ID of the given entity's rider, or null if it doesn't have one.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class get_entity_vehicle extends EntityGetterFunction {

		@Override
		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCEntity ent = Static.getEntity(args[0], t);
			if (ent.isInsideVehicle()) {
				return new CString(ent.getVehicle().getUniqueId().toString(), t);
			}
			return CNull.NULL;
		}

		@Override
		public String getName() {
			return "get_entity_vehicle";
		}

		@Override
		public String docs() {
			return "mixed {entityID} Returns the ID of the given entity's vehicle, or null if it doesn't have one.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class get_entity_max_speed extends EntityGetterFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREBadEntityException.class, CREBadEntityTypeException.class, CRELengthException.class};
		}

		@Override
		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {

			MCEntity e = Static.getEntity(args[0], t);

			if (e instanceof MCBoat) {
				return new CDouble(((MCBoat) e).getMaxSpeed(), t);
			} else if(e instanceof MCMinecart) {
				return new CDouble(((MCMinecart) e).getMaxSpeed(), t);
			}

			throw new CREBadEntityTypeException("Given entity must be a boat or minecart.", t);
		}

		@Override
		public String getName() {
			return "get_entity_max_speed";
		}

		@Override
		public String docs() {
			return "double {entityID} Returns a max speed for given entity. Make sure that the entity is a boat"
					+ " or minecart.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class set_entity_max_speed extends EntitySetterFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREBadEntityException.class, CREBadEntityTypeException.class,
					CRECastException.class, CRELengthException.class};
		}

		@Override
		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {

			MCEntity e = Static.getEntity(args[0], t);
			double speed = Static.getDouble(args[1], t);

			if (e instanceof MCBoat) {
				((MCBoat) e).setMaxSpeed(speed);
			} else if(e instanceof MCMinecart) {
				((MCMinecart) e).setMaxSpeed(speed);
			} else {
				throw new CREBadEntityTypeException("Given entity must be a boat or minecart.", t);
			}

			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "set_entity_max_speed";
		}

		@Override
		public String docs() {
			return "void {entityID} Sets a max speed for given entity. Make sure that the entity is a boat"
					+ " or minecart.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class get_equipment_droprates extends EntityGetterFunction {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCEntityEquipment eq = Static.getLivingEntity(args[0], t).getEquipment();
			if (eq.getHolder() instanceof MCPlayer) {
				throw new CREBadEntityException(getName() + " does not work on players.", t);
			}
			CArray ret = CArray.GetAssociativeArray(t);
			for (Map.Entry<MCEquipmentSlot, Float> ent : eq.getAllDropChances().entrySet()) {
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
	public static class set_equipment_droprates extends EntitySetterFunction {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCEntityEquipment ee = Static.getLivingEntity(args[0], t).getEquipment();
			Map<MCEquipmentSlot, Float> eq = ee.getAllDropChances();
			if (ee.getHolder() instanceof MCPlayer) {
				throw new CREBadEntityException(getName() + " does not work on players.", t);
			}
			if (args[1] instanceof CNull) {
				for (Map.Entry<MCEquipmentSlot, Float> ent : eq.entrySet()) {
					eq.put(ent.getKey(), 0F);
				}
			} else if (args[1] instanceof CArray) {
				CArray ea = (CArray) args[1];
				for (String key : ea.stringKeySet()) {
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
	public static class get_name_visible extends EntityGetterFunction {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			try {
				return CBoolean.get(Static.getEntity(args[0], t).isCustomNameVisible());
			} catch (IllegalArgumentException e) {
				throw new CRECastException(e.getMessage(), t);
			}
		}

		@Override
		public String getName() {
			return "get_name_visible";
		}

		@Override
		public String docs() {
			return "boolean {entityID} Returns whether or not a mob's custom name is always visible."
					+ " If this is true it will be as visible as player names, otherwise it will only be"
					+ " visible when near the mob.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class set_name_visible extends EntitySetterFunction {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			try {
				Static.getEntity(args[0], t).setCustomNameVisible(Static.getBoolean(args[1]));
			} catch (IllegalArgumentException e) {
				throw new CRECastException(e.getMessage(), t);
			}
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "set_name_visible";
		}

		@Override
		public String docs() {
			return "void {entityID, boolean} Sets the visibility of a mob's custom name."
					+ " True means it will be visible from a distance, like a playername."
					+ " False means it will only be visible when near the mob.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class can_pickup_items extends EntityGetterFunction {

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
	public static class set_can_pickup_items extends EntitySetterFunction {

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
	public static class get_entity_persistence extends EntityGetterFunction {

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
	public static class set_entity_persistence extends EntitySetterFunction {

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

	@api public static class get_art_at extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREBadEntityException.class, CREFormatException.class};
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
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCWorld w = null;
			if(environment.getEnv(CommandHelperEnvironment.class).GetPlayer() != null){
				w = environment.getEnv(CommandHelperEnvironment.class).GetPlayer().getWorld();
			}
			List<MCEntity> es = StaticLayer.GetConvertor().GetEntitiesAt(ObjectGenerator.GetGenerator().location(args[0], w, t), 1);
			for(MCEntity e : es){
				if(e instanceof MCPainting){
					return new CString(((MCPainting)e).getArt().name(), t);
				}
			}
			throw new CREBadEntityException("There is no painting at the specified location", t);
		}

		@Override
		public String getName() {
			return "get_art_at";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "string {locationArray} Gets the specified art at the given location. If the item"
					+ " at the specified location isn't a painting, an ----"
					+ " Will be one of the following: " + StringUtils.Join(MCArt.values(), ", ") + ".";
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_1;
		}

	}

	@api public static class set_art_at extends AbstractFunction {

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
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCWorld w = null;
			if(environment.getEnv(CommandHelperEnvironment.class).GetPlayer() != null){
				w = environment.getEnv(CommandHelperEnvironment.class).GetPlayer().getWorld();
			}
			MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], w, t);
			MCArt art;
			try{
				art = MCArt.valueOf(args[1].val());
			} catch(IllegalArgumentException e){
				throw new CREFormatException("Invalid type: " + args[1].val(), t);
			}
			//If there's already a painting there, just use that one. Otherwise, spawn a new one.

			MCPainting p = null;
			for(MCEntity e : StaticLayer.GetConvertor().GetEntitiesAt(loc, 1)){
				if(e instanceof MCPainting){
					p = (MCPainting)e;
					break;
				}
			}
			if(p == null){
				p = (MCPainting) loc.getWorld().spawn(loc, MCEntityType.MCVanillaEntityType.PAINTING);
			}
			boolean worked = p.setArt(art);
			if(!worked){
				p.remove();
			}
			return CBoolean.get(worked);
		}

		@Override
		public String getName() {
			return "set_art_at";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public String docs() {
			return "boolean {locationArray, art} Sets the art at the specified location. If the art"
					+ " doesn't fit, nothing happens, and false is returned. Otherwise, true is returned."
					+ " ---- Art may be one of the following: " + StringUtils.Join(MCArt.values(), ", ");
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class get_leashholder extends EntityGetterFunction {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCLivingEntity le = Static.getLivingEntity(args[0], t);
			if (!le.isLeashed()) {
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
	public static class set_leashholder extends EntitySetterFunction {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCLivingEntity le = Static.getLivingEntity(args[0], t);
			MCEntity holder;
			if (args[1] instanceof CNull) {
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
	public static class entity_grounded extends EntityGetterFunction {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			return CBoolean.get(Static.getEntity(args[0], t).isOnGround());
		}

		@Override
		public String getName() {
			return "entity_grounded";
		}

		@Override
		public String docs() {
			return "boolean {entityID} returns whether the entity is touching the ground";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class entity_air extends EntityGetterFunction {

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
	public static class set_entity_air extends EntitySetterFunction {

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
	public static class entity_max_air extends EntityGetterFunction {

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
	public static class set_entity_max_air extends EntitySetterFunction {

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
	public static class entity_line_of_sight extends EntityFunction {

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
			return "array {entityID, [transparents, [maxDistance]]} Returns an array containg all blocks along the living entity's line of sight."
					+ " transparents is an array of block IDs, only air by default."
					+ " maxDistance represent the maximum distance to scan, it may be limited by the server by at least 100 blocks, no less.";
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCLivingEntity entity = Static.getLivingEntity(args[0], t);
			HashSet<Byte> transparents = null;
			int maxDistance = 512;
			if (args.length >= 2) {
				CArray givenTransparents = Static.getArray(args[1], t);
				if (givenTransparents.inAssociativeMode()) {
					throw new CRECastException("The array must not be associative.", t);
				}
				transparents = new HashSet<Byte>();
				for (Construct blockID : givenTransparents.asList()) {
					transparents.add(Static.getInt8(blockID, t));
				}
			}
			if (args.length == 3) {
				maxDistance = Static.getInt32(args[2], t);
			}
			CArray lineOfSight = new CArray(t);
			for (MCBlock block : entity.getLineOfSight(transparents, maxDistance)) {
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
	public static class entity_can_see extends EntityFunction {

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
	public static class entity_id extends EntityGetterFunction {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCEntity entity = Static.getEntityByUuid(Static.GetUUID(args[0], t), t);
			return new CString(entity.getUniqueId().toString(), t);
		}

		@Override
		public String getName() {
			return "entity_id";
		}

		@Override
		public String docs() {
			return "string {entityUUID} returns the entity id for unique persistent UUID";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

	}

	@api
	public static class entity_uuid extends EntityGetterFunction {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCEntity entity = Static.getEntity(args[0], t);
			return new CString(entity.getUniqueId().toString(), t);
		}

		@Override
		public String getName() {
			return "entity_uuid";
		}

		@Override
		public String docs() {
			return "string {entityID} returns the persistent unique id of the entity";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	@seealso(set_entity_spec.class)
	public static class entity_spec extends EntityGetterFunction {

		@Override
		public String getName() {
			return "entity_spec";
		}

		@Override
		public String docs() {
			String docs = getBundledDocs();
			docs = docs.replace("%BODY_PART%", "pose" + StringUtils.Join(MCBodyPart.humanoidParts(), ", pose", ", or pose", " or pose"));
			docs = docs.replace("%HORSE_COLOR%", StringUtils.Join(MCHorseColor.values(), ", ", ", or ", " or "));
			docs = docs.replace("%HORSE_STYLE%", StringUtils.Join(MCHorsePattern.values(), ", ", ", or ", " or "));
			docs = docs.replace("%HORSE_VARIANT%", StringUtils.Join(MCHorseVariant.values(), ", ", ", or ", " or "));
			docs = docs.replace("%ROTATION%", StringUtils.Join(MCRotation.values(), ", ", ", or ", " or "));
			docs = docs.replace("%OCELOT_TYPE%", StringUtils.Join(MCOcelotType.values(), ", ", ", or ", " or "));
			docs = docs.replace("%ART%", StringUtils.Join(MCArt.values(), ", ", ", or ", " or "));
			docs = docs.replace("%DYE_COLOR%", StringUtils.Join(MCDyeColor.values(), ", ", ", or ", " or "));
			docs = docs.replace("%SKELETON_TYPE%", StringUtils.Join(MCSkeletonType.values(), ", ", ", or ", " or "));
			docs = docs.replace("%PROFESSION%", StringUtils.Join(MCProfession.values(), ", ", ", or ", " or "));
			docs = docs.replace("%RABBIT_TYPE%", StringUtils.Join(MCRabbitType.values(), ", ", ", or ", " or "));
			docs = docs.replace("%PARTICLE%", StringUtils.Join(MCParticle.values(), ", ", ", or ", " or "));
			for (Field field : entity_spec.class.getDeclaredFields()) {
				try {
					String name = field.getName();
					if (name.startsWith("KEY_")) {
						docs = docs.replace("%" + name + "%", (String) field.get(null));
					}
				} catch (IllegalArgumentException | IllegalAccessException ex) {
					ex.printStackTrace();
				}
			}
			return docs;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCEntity entity = Static.getEntity(args[0], t);
			CArray specArray = CArray.GetAssociativeArray(t);

			switch (entity.getType().getAbstracted()) {
				case AREA_EFFECT_CLOUD:
					MCAreaEffectCloud cloud = (MCAreaEffectCloud) entity;
					specArray.set(entity_spec.KEY_AREAEFFECTCLOUD_COLOR, ObjectGenerator.GetGenerator().color(cloud.getColor(), t), t);
					specArray.set(entity_spec.KEY_AREAEFFECTCLOUD_DURATION, new CInt(cloud.getDuration(), t), t);
					specArray.set(entity_spec.KEY_AREAEFFECTCLOUD_DURATIONONUSE, new CInt(cloud.getDurationOnUse(), t), t);
					specArray.set(entity_spec.KEY_AREAEFFECTCLOUD_PARTICLE, new CString(cloud.getParticle().name(), t), t);
					CArray meta = CArray.GetAssociativeArray(t);
					CArray effects = ObjectGenerator.GetGenerator().potions(cloud.getCustomEffects(), t);
					meta.set("potions", effects, t);
					meta.set("base", ObjectGenerator.GetGenerator().potionData(cloud.getBasePotionData(), t), t);
					specArray.set(entity_spec.KEY_AREAEFFECTCLOUD_POTIONMETA, meta, t);
					specArray.set(entity_spec.KEY_AREAEFFECTCLOUD_RADIUS, new CDouble(cloud.getRadius(), t), t);
					specArray.set(entity_spec.KEY_AREAEFFECTCLOUD_RADIUSONUSE, new CDouble(cloud.getRadiusOnUse(), t), t);
					specArray.set(entity_spec.KEY_AREAEFFECTCLOUD_RADIUSPERTICK, new CDouble(cloud.getRadiusPerTick(), t), t);
					specArray.set(entity_spec.KEY_AREAEFFECTCLOUD_REAPPLICATIONDELAY, new CInt(cloud.getReapplicationDelay(), t), t);
					MCProjectileSource cloudSource = cloud.getSource();
					if(cloudSource instanceof MCBlockProjectileSource){
						MCLocation blockLocation = ((MCBlockProjectileSource) cloudSource).getBlock().getLocation();
						CArray locationArray = ObjectGenerator.GetGenerator().location(blockLocation, false);
						specArray.set(entity_spec.KEY_AREAEFFECTCLOUD_SOURCE, locationArray, t);
					} else if (cloudSource instanceof MCEntity) {
						String entityUUID = ((MCEntity) cloudSource).getUniqueId().toString();
						specArray.set(entity_spec.KEY_AREAEFFECTCLOUD_SOURCE, new CString(entityUUID, t), t);
					} else {
						specArray.set(entity_spec.KEY_AREAEFFECTCLOUD_SOURCE, CNull.NULL, t);
					}
					specArray.set(entity_spec.KEY_AREAEFFECTCLOUD_WAITTIME, new CInt(cloud.getWaitTime(), t), t);
					break;
				case ARROW:
					MCArrow arrow = (MCArrow) entity;
					specArray.set(entity_spec.KEY_ARROW_CRITICAL, CBoolean.get(arrow.isCritical()), t);
					specArray.set(entity_spec.KEY_ARROW_KNOCKBACK, new CInt(arrow.getKnockbackStrength(), t), t);
					break;
				case ARMOR_STAND:
					MCArmorStand stand = (MCArmorStand) entity;
					specArray.set(entity_spec.KEY_ARMORSTAND_ARMS, CBoolean.get(stand.hasArms()), t);
					specArray.set(entity_spec.KEY_ARMORSTAND_BASEPLATE, CBoolean.get(stand.hasBasePlate()), t);
					specArray.set(entity_spec.KEY_ARMORSTAND_GRAVITY, CBoolean.get(stand.hasGravity()), t);
					Boolean marker = stand.isMarker();
					if(marker != null) { // unsupported before 1.8.7
						specArray.set(entity_spec.KEY_ARMORSTAND_MARKER, CBoolean.get(marker), t);
					}
					specArray.set(entity_spec.KEY_ARMORSTAND_SMALLSIZE, CBoolean.get(stand.isSmall()), t);
					specArray.set(entity_spec.KEY_ARMORSTAND_VISIBLE, CBoolean.get(stand.isVisible()), t);
					CArray poses = CArray.GetAssociativeArray(t);
					Map<MCBodyPart, Vector3D> poseMap = stand.getAllPoses();
					for (MCBodyPart key : poseMap.keySet()) {
						poses.set("pose" + key.name(), ObjectGenerator.GetGenerator().vector(poseMap.get(key), t), t);
					}
					specArray.set(entity_spec.KEY_ARMORSTAND_POSES, poses, t);
					break;
				case CREEPER:
					MCCreeper creeper = (MCCreeper) entity;
					specArray.set(entity_spec.KEY_CREEPER_POWERED, CBoolean.get(creeper.isPowered()), t);
					break;
				case DROPPED_ITEM:
					MCItem item = (MCItem) entity;
					specArray.set(entity_spec.KEY_DROPPED_ITEM_ITEMSTACK, ObjectGenerator.GetGenerator().item(item.getItemStack(), t), t);
					specArray.set(entity_spec.KEY_DROPPED_ITEM_PICKUPDELAY, new CInt(item.getPickupDelay(), t), t);
					break;
				case ENDER_CRYSTAL:
					MCEnderCrystal endercrystal = (MCEnderCrystal) entity;
					if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_9)){
						specArray.set(entity_spec.KEY_ENDERCRYSTAL_BASE, CBoolean.get(endercrystal.isShowingBottom()), t);
						MCLocation location = endercrystal.getBeamTarget();
						if(location == null){
							specArray.set(entity_spec.KEY_ENDERCRYSTAL_BEAMTARGET, CNull.NULL, t);
						} else {
							specArray.set(entity_spec.KEY_ENDERCRYSTAL_BEAMTARGET,
									ObjectGenerator.GetGenerator().location(location, false), t);
						}
					}
					break;
				case ENDERMAN:
					MCEnderman enderman = (MCEnderman) entity;
					MCMaterialData carried = enderman.getCarriedMaterial();
					if (carried != null) {
						specArray.set(entity_spec.KEY_ENDERMAN_CARRIED, new CString(carried.getMaterial().getName(), t), t);
					} else {
						specArray.set(entity_spec.KEY_ENDERMAN_CARRIED, CNull.NULL, t);
					}
					break;
				case EXPERIENCE_ORB:
					MCExperienceOrb orb = (MCExperienceOrb) entity;
					specArray.set(entity_spec.KEY_EXPERIENCE_ORB_AMOUNT, new CInt(orb.getExperience(), t), t);
					break;
				case FALLING_BLOCK:
					MCFallingBlock block = (MCFallingBlock) entity;
					specArray.set(entity_spec.KEY_FALLING_BLOCK_BLOCK, new CInt(block.getMaterial().getName(), t), t);
					specArray.set(entity_spec.KEY_FALLING_BLOCK_DROPITEM, CBoolean.get(block.getDropItem()), t);
					break;
				case FIREBALL:
				case SMALL_FIREBALL:
					MCFireball ball = (MCFireball) entity;
					specArray.set(entity_spec.KEY_FIREBALL_DIRECTION, ObjectGenerator.GetGenerator().vector(ball.getDirection(), t), t);
					break;
				case FISHING_HOOK:
					MCFishHook hook = (MCFishHook) entity;
					specArray.set(entity_spec.KEY_FISHING_HOOK_CHANCE, new CDouble(hook.getBiteChance(), t), t);
					break;
				case GUARDIAN:
					MCGuardian guardian = (MCGuardian) entity;
					specArray.set(entity_spec.KEY_GUARDIAN_ELDER, CBoolean.get(guardian.isElder()), t);
					break;
				case HORSE:
					MCHorse horse = (MCHorse) entity;
					specArray.set(entity_spec.KEY_HORSE_COLOR, new CString(horse.getColor().name(), t), t);
					specArray.set(entity_spec.KEY_HORSE_STYLE, new CString(horse.getPattern().name(), t), t);
					specArray.set(entity_spec.KEY_HORSE_VARIANT, new CString(horse.getVariant().name(), t), t);
					specArray.set(entity_spec.KEY_HORSE_CHEST, CBoolean.get(horse.hasChest()), t);
					specArray.set(entity_spec.KEY_HORSE_JUMP, new CDouble(horse.getJumpStrength(), t), t);
					specArray.set(entity_spec.KEY_HORSE_DOMESTICATION, new CInt(horse.getDomestication(), t), t);
					specArray.set(entity_spec.KEY_HORSE_MAXDOMESTICATION, new CInt(horse.getMaxDomestication(), t), t);
					specArray.set(entity_spec.KEY_HORSE_ARMOR, ObjectGenerator.GetGenerator().item(horse.getArmor(), t), t);
					specArray.set(entity_spec.KEY_HORSE_SADDLE, ObjectGenerator.GetGenerator().item(horse.getSaddle(), t), t);
					break;
				case IRON_GOLEM:
					MCIronGolem golem = (MCIronGolem) entity;
					specArray.set(entity_spec.KEY_IRON_GOLEM_PLAYERCREATED, CBoolean.get(golem.isPlayerCreated()), t);
					break;
				case ITEM_FRAME:
					MCItemFrame frame = (MCItemFrame) entity;
					MCItemStack itemstack = frame.getItem();
					if (itemstack != null) {
						specArray.set(entity_spec.KEY_ITEM_FRAME_ITEM, ObjectGenerator.GetGenerator().item(frame.getItem(), t), t);
					} else {
						specArray.set(entity_spec.KEY_ITEM_FRAME_ITEM, CNull.NULL, t);
					}
					specArray.set(entity_spec.KEY_ITEM_FRAME_ROTATION, new CString(frame.getRotation().name(), t), t);
					break;
				case LIGHTNING:
					MCLightningStrike lightning = (MCLightningStrike) entity;
					specArray.set(entity_spec.KEY_LIGHTNING_EFFECT, CBoolean.get(lightning.isEffect()), t);
					break;
				case MAGMA_CUBE:
				case SLIME:
					MCSlime cube = (MCSlime) entity;
					specArray.set(entity_spec.KEY_SLIME_SIZE, new CInt(cube.getSize(), t), t);
					break;
				case MINECART:
					MCMinecart minecart = (MCMinecart) entity;
					specArray.set(entity_spec.KEY_MINECART_BLOCK, new CString(minecart.getDisplayBlock().getMaterial().getName(), t), t);
					specArray.set(entity_spec.KEY_MINECART_OFFSET, new CInt(minecart.getDisplayBlockOffset(), t), t);
					break;
				case MINECART_COMMAND:
					MCCommandMinecart commandminecart = (MCCommandMinecart) entity;
					specArray.set(entity_spec.KEY_MINECART_COMMAND_COMMAND, new CString(commandminecart.getCommand(), t), t);
					specArray.set(entity_spec.KEY_MINECART_COMMAND_CUSTOMNAME, new CString(commandminecart.getName(), t), t);
					break;
				case OCELOT:
					MCOcelot ocelot = (MCOcelot) entity;
					specArray.set(entity_spec.KEY_OCELOT_TYPE, new CString(ocelot.getCatType().name(), t), t);
					specArray.set(entity_spec.KEY_OCELOT_SITTING, CBoolean.get(ocelot.isSitting()), t);
					break;
				case PAINTING:
					MCPainting painting = (MCPainting) entity;
					specArray.set(entity_spec.KEY_PAINTING_ART, new CString(painting.getArt().name(), t), t);
					break;
				case PIG:
					MCPig pig = (MCPig) entity;
					specArray.set(entity_spec.KEY_PIG_SADDLED, CBoolean.get(pig.isSaddled()), t);
					break;
				case PIG_ZOMBIE:
					MCPigZombie pigZombie = (MCPigZombie) entity;
					specArray.set(entity_spec.KEY_PIG_ZOMBIE_ANGRY, CBoolean.get(pigZombie.isAngry()), t);
					specArray.set(entity_spec.KEY_PIG_ZOMBIE_ANGER, new CInt(pigZombie.getAnger(), t), t);
					specArray.set(entity_spec.KEY_ZOMBIE_BABY, CBoolean.get(pigZombie.isBaby()), t);
					specArray.set(entity_spec.KEY_ZOMBIE_VILLAGER, CBoolean.get(pigZombie.isVillager()), t);
					break;
				case PRIMED_TNT:
					MCTNT tnt = (MCTNT) entity;
					specArray.set(entity_spec.KEY_PRIMED_TNT_FUSETICKS, new CInt(tnt.getFuseTicks(), t), t);
					MCEntity source = tnt.getSource();
					if (source != null) {
						specArray.set(entity_spec.KEY_PRIMED_TNT_SOURCE, new CString(source.getUniqueId().toString(), t), t);
					} else {
						specArray.set(entity_spec.KEY_PRIMED_TNT_SOURCE, CNull.NULL, t);
					}
					break;
				case RABBIT:
					MCRabbit rabbit = (MCRabbit) entity;
					specArray.set(entity_spec.KEY_RABBIT_TYPE, new CString(rabbit.getRabbitType().name(), t), t);
					break;
				case SHEEP:
					MCSheep sheep = (MCSheep) entity;
					specArray.set(entity_spec.KEY_SHEEP_COLOR, new CString(sheep.getColor().name(), t), t);
					specArray.set(entity_spec.KEY_SHEEP_SHEARED, CBoolean.get(sheep.isSheared()), t);
					break;
				case SKELETON:
					MCSkeleton skeleton = (MCSkeleton) entity;
					specArray.set(entity_spec.KEY_SKELETON_TYPE, new CString(skeleton.getSkeletonType().name(), t), t);
					break;
				case SNOWMAN:
					if (Static.getVersion().gte(MCVersion.MC1_9_4)) {
						MCSnowman snowman = (MCSnowman) entity;
						specArray.set(entity_spec.KEY_SNOWMAN_DERP, CBoolean.GenerateCBoolean(snowman.isDerp(), t), t);
					}
					break;
				case LINGERING_POTION:
				case SPLASH_POTION:
					MCThrownPotion potion = (MCThrownPotion) entity;
					specArray.set(entity_spec.KEY_SPLASH_POTION_ITEM, ObjectGenerator.GetGenerator().item(potion.getItem(), t), t);
					break;
				case TIPPED_ARROW:
					MCTippedArrow tippedarrow = (MCTippedArrow) entity;
					specArray.set(entity_spec.KEY_ARROW_CRITICAL, CBoolean.get(tippedarrow.isCritical()), t);
					specArray.set(entity_spec.KEY_ARROW_KNOCKBACK, new CInt(tippedarrow.getKnockbackStrength(), t), t);
					CArray tippedmeta = CArray.GetAssociativeArray(t);
					CArray tippedeffects = ObjectGenerator.GetGenerator().potions(tippedarrow.getCustomEffects(), t);
					tippedmeta.set("potions", tippedeffects, t);
					tippedmeta.set("base", ObjectGenerator.GetGenerator().potionData(tippedarrow.getBasePotionData(), t), t);
					specArray.set(entity_spec.KEY_TIPPEDARROW_POTIONMETA, tippedmeta, t);
					break;
				case VILLAGER:
					MCVillager villager = (MCVillager) entity;
					specArray.set(entity_spec.KEY_VILLAGER_PROFESSION, new CString(villager.getProfession().name(), t), t);
					break;
				case WITHER_SKULL:
					MCWitherSkull skull = (MCWitherSkull) entity;
					specArray.set(entity_spec.KEY_WITHER_SKULL_CHARGED, CBoolean.get(skull.isCharged()), t);
					specArray.set(entity_spec.KEY_FIREBALL_DIRECTION, ObjectGenerator.GetGenerator().vector(skull.getDirection(), t), t);
					break;
				case WOLF:
					MCWolf wolf = (MCWolf) entity;
					specArray.set(entity_spec.KEY_WOLF_ANGRY, CBoolean.get(wolf.isAngry()), t);
					specArray.set(entity_spec.KEY_WOLF_COLOR, new CString(wolf.getCollarColor().name(), t), t);
					specArray.set(entity_spec.KEY_WOLF_SITTING, CBoolean.get(wolf.isSitting()), t);
					break;
				case ZOMBIE:
					MCZombie zombie = (MCZombie) entity;
					specArray.set(entity_spec.KEY_ZOMBIE_BABY, CBoolean.get(zombie.isBaby()), t);
					specArray.set(entity_spec.KEY_ZOMBIE_VILLAGER, CBoolean.get(zombie.isVillager()), t);
					break;
			}
			return specArray;
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		//used to ensure that the indexes are the same in entity_spec(), set_entity_spec(), and in the documentation.
		private static final String KEY_AREAEFFECTCLOUD_COLOR = "color";
		private static final String KEY_AREAEFFECTCLOUD_DURATION = "duration";
		private static final String KEY_AREAEFFECTCLOUD_DURATIONONUSE = "durationonuse";
		private static final String KEY_AREAEFFECTCLOUD_PARTICLE = "particle";
		private static final String KEY_AREAEFFECTCLOUD_POTIONMETA = "potionmeta";
		private static final String KEY_AREAEFFECTCLOUD_RADIUS = "radius";
		private static final String KEY_AREAEFFECTCLOUD_RADIUSONUSE = "radiusonuse";
		private static final String KEY_AREAEFFECTCLOUD_RADIUSPERTICK = "radiuspertick";
		private static final String KEY_AREAEFFECTCLOUD_REAPPLICATIONDELAY = "reapplicationdelay";
		private static final String KEY_AREAEFFECTCLOUD_SOURCE = "source";
		private static final String KEY_AREAEFFECTCLOUD_WAITTIME = "waittime";
		private static final String KEY_ARROW_CRITICAL = "critical";
		private static final String KEY_ARROW_KNOCKBACK = "knockback";
		private static final String KEY_ARMORSTAND_ARMS = "arms";
		private static final String KEY_ARMORSTAND_BASEPLATE = "baseplate";
		private static final String KEY_ARMORSTAND_GRAVITY = "gravity";
		private static final String KEY_ARMORSTAND_MARKER = "marker";
		private static final String KEY_ARMORSTAND_POSES = "poses";
		private static final String KEY_ARMORSTAND_SMALLSIZE = "small";
		private static final String KEY_ARMORSTAND_VISIBLE = "visible";
		private static final String KEY_CREEPER_POWERED = "powered";
		private static final String KEY_DROPPED_ITEM_ITEMSTACK = "itemstack";
		private static final String KEY_DROPPED_ITEM_PICKUPDELAY = "pickupdelay";
		private static final String KEY_ENDERCRYSTAL_BASE = "base";
		private static final String KEY_ENDERCRYSTAL_BEAMTARGET = "beamtarget";
		private static final String KEY_ENDERMAN_CARRIED = "carried";
		private static final String KEY_EXPERIENCE_ORB_AMOUNT = "amount";
		private static final String KEY_FALLING_BLOCK_BLOCK = "block";
		private static final String KEY_FALLING_BLOCK_DROPITEM = "dropitem";
		private static final String KEY_FIREBALL_DIRECTION = "direction";
		private static final String KEY_FISHING_HOOK_CHANCE = "chance";
		private static final String KEY_GUARDIAN_ELDER = "elder";
		private static final String KEY_HORSE_COLOR = "color";
		private static final String KEY_HORSE_STYLE = "style";
		private static final String KEY_HORSE_VARIANT = "variant";
		private static final String KEY_HORSE_CHEST = "chest";
		private static final String KEY_HORSE_JUMP = "jump";
		private static final String KEY_HORSE_DOMESTICATION = "domestication";
		private static final String KEY_HORSE_MAXDOMESTICATION = "maxdomestication";
		private static final String KEY_HORSE_ARMOR = "armor";
		private static final String KEY_HORSE_SADDLE = "saddle";
		private static final String KEY_IRON_GOLEM_PLAYERCREATED = "playercreated";
		private static final String KEY_ITEM_FRAME_ITEM = "item";
		private static final String KEY_ITEM_FRAME_ROTATION = "rotation";
		private static final String KEY_LIGHTNING_EFFECT = "effect";
		private static final String KEY_MINECART_BLOCK = "block";
		private static final String KEY_MINECART_OFFSET = "offset";
		private static final String KEY_MINECART_COMMAND_COMMAND = "command";
		private static final String KEY_MINECART_COMMAND_CUSTOMNAME = "customname";
		private static final String KEY_OCELOT_TYPE = "type";
		private static final String KEY_OCELOT_SITTING = "sitting";
		private static final String KEY_PAINTING_ART = "type";
		private static final String KEY_PIG_SADDLED = "saddled";
		private static final String KEY_PIG_ZOMBIE_ANGRY = "angry";
		private static final String KEY_PIG_ZOMBIE_ANGER = "anger";
		private static final String KEY_RABBIT_TYPE = "type";
		private static final String KEY_PRIMED_TNT_FUSETICKS = "fuseticks";
		private static final String KEY_PRIMED_TNT_SOURCE = "source";
		private static final String KEY_SHEEP_COLOR = "color";
		private static final String KEY_SHEEP_SHEARED = "sheared";
		private static final String KEY_SKELETON_TYPE = "type";
		private static final String KEY_SLIME_SIZE = "size";
		private static final String KEY_SNOWMAN_DERP = "derp";
		private static final String KEY_SPLASH_POTION_ITEM = "item";
		private static final String KEY_TIPPEDARROW_POTIONMETA = "potionmeta";
		private static final String KEY_VILLAGER_PROFESSION = "profession";
		private static final String KEY_WITHER_SKULL_CHARGED = "charged";
		private static final String KEY_WOLF_ANGRY = "angry";
		private static final String KEY_WOLF_COLOR = "color";
		private static final String KEY_WOLF_SITTING = "sitting";
		private static final String KEY_ZOMBIE_BABY = "baby";
		private static final String KEY_ZOMBIE_VILLAGER = "villager";
	}

	@api
	@seealso(entity_spec.class)
	public static class set_entity_spec extends EntitySetterFunction {

		@Override
		public String getName() {
			return "set_entity_spec";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{
					CRECastException.class, CREBadEntityException.class, CREIndexOverflowException.class,
					CREIndexOverflowException.class, CRERangeException.class, CREFormatException.class,
					CRELengthException.class
			};
		}

		@Override
		public String docs() {
			return "void {entityID, specArray} Sets the data in the specArray to the given entity."
					+ " The specArray must follow the same format as entity_spec()."
					+ " See the documentation for that function for info on available options."
					+ " All indices in the specArray are optional.";
		}

		private static void throwException(String index, Target t) throws ConfigRuntimeException {
			throw new CREIndexOverflowException("Unknown or uneditable specification: " + index, t);
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCEntity entity = Static.getEntity(args[0], t);
			CArray specArray = Static.getArray(args[1], t);

			switch (entity.getType().getAbstracted()) {
				case AREA_EFFECT_CLOUD:
					MCAreaEffectCloud cloud = (MCAreaEffectCloud) entity;
					for (String index : specArray.stringKeySet()) {
						switch (index.toLowerCase()) {
							case entity_spec.KEY_AREAEFFECTCLOUD_COLOR:
								if (specArray.get(index, t) instanceof CArray){
									CArray color = (CArray) specArray.get(index, t);
									cloud.setColor(ObjectGenerator.GetGenerator().color(color, t));
								} else {
									throw new CRECastException("AreaEffectCloud color must be an array", t);
								}
								break;
							case entity_spec.KEY_AREAEFFECTCLOUD_DURATION:
								cloud.setDuration(ArgumentValidation.getInt32(specArray.get(index, t), t));
								break;
							case entity_spec.KEY_AREAEFFECTCLOUD_DURATIONONUSE:
								cloud.setDurationOnUse(ArgumentValidation.getInt32(specArray.get(index, t), t));
								break;
							case entity_spec.KEY_AREAEFFECTCLOUD_PARTICLE:
								String particleName = specArray.get(index, t).val();
								try {
									cloud.setParticle(MCParticle.valueOf(particleName));
								} catch(IllegalArgumentException ex){
									throw new CREFormatException("Invalid particle type: " + particleName, t);
								}
								break;
							case entity_spec.KEY_AREAEFFECTCLOUD_POTIONMETA:
								Construct c = specArray.get(index, t);
								if(c instanceof CArray){
									CArray meta = (CArray) c;
									if(meta.containsKey("base")){
										Construct base = meta.get("base", t);
										if(base instanceof CArray){
											MCPotionData pd = ObjectGenerator.GetGenerator().potionData((CArray) base, t);
											cloud.setBasePotionData(pd);
										}
									}
									if(meta.containsKey("potions")){
										cloud.clearCustomEffects();
										Construct potions = meta.get("potions", t);
										if(potions instanceof CArray){
											List<MCLivingEntity.MCEffect> list = ObjectGenerator.GetGenerator().potions((CArray) potions, t);
											for(MCLivingEntity.MCEffect effect : list) {
												cloud.addCustomEffect(effect);
											}
										}
									}
								} else {
									throw new CRECastException("AreaEffectCloud potion meta must be an array", t);
								}
								break;
							case entity_spec.KEY_AREAEFFECTCLOUD_RADIUS:
								cloud.setRadius(ArgumentValidation.getDouble32(specArray.get(index, t), t));
								break;
							case entity_spec.KEY_AREAEFFECTCLOUD_RADIUSONUSE:
								cloud.setRadiusOnUse(ArgumentValidation.getDouble32(specArray.get(index, t), t));
								break;
							case entity_spec.KEY_AREAEFFECTCLOUD_RADIUSPERTICK:
								cloud.setRadiusPerTick(ArgumentValidation.getDouble32(specArray.get(index, t), t));
								break;
							case entity_spec.KEY_AREAEFFECTCLOUD_REAPPLICATIONDELAY:
								cloud.setReapplicationDelay(ArgumentValidation.getInt32(specArray.get(index, t), t));
								break;
							case entity_spec.KEY_AREAEFFECTCLOUD_SOURCE:
								Construct cloudSource = specArray.get(index, t);
								if(cloudSource instanceof CNull){
									cloud.setSource(null);
								} else if(cloudSource instanceof CArray){
									MCBlock b = ObjectGenerator.GetGenerator().location(cloudSource, cloud.getWorld(), t).getBlock();
									if(b.isDispenser()){
										cloud.setSource(b.getDispenser().getBlockProjectileSource());
									} else {
										throw new CRECastException("AreaEffectCloud block source must be a dispenser", t);
									}
								} else {
									cloud.setSource(Static.getLivingEntity(cloudSource, t));
								}
								break;
							case entity_spec.KEY_AREAEFFECTCLOUD_WAITTIME:
								cloud.setWaitTime(ArgumentValidation.getInt32(specArray.get(index, t), t));
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case ARROW:
					MCArrow arrow = (MCArrow) entity;
					for (String index : specArray.stringKeySet()) {
						switch (index.toLowerCase()) {
							case entity_spec.KEY_ARROW_CRITICAL:
								arrow.setCritical(Static.getBoolean(specArray.get(index, t)));
								break;
							case entity_spec.KEY_ARROW_KNOCKBACK:
								int k = Static.getInt32(specArray.get(index, t), t);
								if (k < 0) {
									throw new CRERangeException("Knockback can not be negative.", t);
								} else {
									arrow.setKnockbackStrength(k);
								}
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case ARMOR_STAND:
					MCArmorStand stand = (MCArmorStand) entity;
					for (String index : specArray.stringKeySet()) {
						switch (index.toLowerCase()) {
							case entity_spec.KEY_ARMORSTAND_ARMS:
								stand.setHasArms(Static.getBoolean(specArray.get(index, t)));
								break;
							case entity_spec.KEY_ARMORSTAND_BASEPLATE:
								stand.setHasBasePlate(Static.getBoolean(specArray.get(index, t)));
								break;
							case entity_spec.KEY_ARMORSTAND_GRAVITY:
								stand.setHasGravity(Static.getBoolean(specArray.get(index, t)));
								break;
							case entity_spec.KEY_ARMORSTAND_MARKER:
								stand.setMarker(Static.getBoolean(specArray.get(index, t)));
								break;
							case entity_spec.KEY_ARMORSTAND_SMALLSIZE:
								stand.setSmall(Static.getBoolean(specArray.get(index, t)));
								break;
							case entity_spec.KEY_ARMORSTAND_VISIBLE:
								stand.setVisible(Static.getBoolean(specArray.get(index, t)));
								break;
							case entity_spec.KEY_ARMORSTAND_POSES:
								Map<MCBodyPart, Vector3D> poseMap = stand.getAllPoses();
								if (specArray.get(index, t) instanceof CArray) {
									CArray poseArray = (CArray) specArray.get(index, t);
									for (MCBodyPart key : poseMap.keySet()) {
										try {
											poseMap.put(key, ObjectGenerator.GetGenerator().vector(poseMap.get(key),
													poseArray.get("pose" + key.name(), t), t));
										} catch (ConfigRuntimeException cre) {
											// Ignore, this just means the user didn't modify a body part
										}
									}
								}
								if (specArray.get(index, t) instanceof CNull) {
									for (MCBodyPart key : poseMap.keySet()) {
										poseMap.put(key, Vector3D.ZERO);
									}
								}
								stand.setAllPoses(poseMap);
								break;
						}
					}
					break;
				case CREEPER:
					MCCreeper creeper = (MCCreeper) entity;
					for (String index : specArray.stringKeySet()) {
						switch (index.toLowerCase()) {
							case entity_spec.KEY_CREEPER_POWERED:
								creeper.setPowered(Static.getBoolean(specArray.get(index, t)));
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case DROPPED_ITEM:
					MCItem item = (MCItem) entity;
					for (String index : specArray.stringKeySet()) {
						switch (index.toLowerCase()) {
							case entity_spec.KEY_DROPPED_ITEM_ITEMSTACK:
								item.setItemStack(ObjectGenerator.GetGenerator().item(specArray.get(index, t), t));
								break;
							case entity_spec.KEY_DROPPED_ITEM_PICKUPDELAY:
								item.setPickupDelay(Static.getInt32(specArray.get(index, t), t));
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case ENDER_CRYSTAL:
					MCEnderCrystal endercrystal = (MCEnderCrystal) entity;
					for (String index : specArray.stringKeySet()) {
						switch (index.toLowerCase()) {
							case entity_spec.KEY_ENDERCRYSTAL_BASE:
								endercrystal.setShowingBottom(Static.getBoolean(specArray.get(index, t)));
								break;
							case entity_spec.KEY_ENDERCRYSTAL_BEAMTARGET:
								Construct c = specArray.get(index, t);
								if(c instanceof CNull){
									endercrystal.setBeamTarget(null);
								} else if(c instanceof CArray){
									MCLocation l = ObjectGenerator.GetGenerator().location((CArray) c, endercrystal.getWorld(), t);
									endercrystal.setBeamTarget(l);
								} else {
									throw new CRECastException("EnderCrystal beam target must be an array or null", t);
								}
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case ENDERMAN:
					MCEnderman enderman = (MCEnderman) entity;
					for (String index : specArray.stringKeySet()) {
						switch (index.toLowerCase()) {
							case entity_spec.KEY_ENDERMAN_CARRIED:
								enderman.setCarriedMaterial(ObjectGenerator.GetGenerator().material(specArray.get(index, t), t).getData());
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case EXPERIENCE_ORB:
					MCExperienceOrb orb = (MCExperienceOrb) entity;
					for (String index : specArray.stringKeySet()) {
						switch (index.toLowerCase()) {
							case entity_spec.KEY_EXPERIENCE_ORB_AMOUNT:
								orb.setExperience(Static.getInt32(specArray.get(index, t), t));
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case FALLING_BLOCK:
					MCFallingBlock block = (MCFallingBlock) entity;
					for (String index : specArray.stringKeySet()) {
						switch (index.toLowerCase()) {
							case entity_spec.KEY_FALLING_BLOCK_DROPITEM:
								block.setDropItem(Static.getBoolean(specArray.get(index, t)));
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case FIREBALL:
				case SMALL_FIREBALL:
					MCFireball ball = (MCFireball) entity;
					for (String index : specArray.stringKeySet()) {
						switch (index.toLowerCase()) {
							case entity_spec.KEY_FIREBALL_DIRECTION:
								ball.setDirection(ObjectGenerator.GetGenerator().vector(specArray.get(index, t), t));
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case FISHING_HOOK:
					MCFishHook hook = (MCFishHook) entity;
					for (String index : specArray.stringKeySet()) {
						switch (index.toLowerCase()) {
							case entity_spec.KEY_FISHING_HOOK_CHANCE:
								try {
									hook.setBiteChance(Static.getDouble(specArray.get(index, t), t));
								} catch (IllegalArgumentException exception) {
									throw new CRERangeException("The chance must be between 0.0 and 1.0", t);
								}
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case GUARDIAN:
					MCGuardian guardian = (MCGuardian) entity;
					for (String index : specArray.stringKeySet()) {
						switch (index.toLowerCase()) {
							case entity_spec.KEY_GUARDIAN_ELDER:
								guardian.setElder(Static.getBoolean(specArray.get(index, t)));
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case HORSE:
					MCHorse horse = (MCHorse) entity;
					for (String index : specArray.stringKeySet()) {
						switch (index.toLowerCase()) {
							case entity_spec.KEY_HORSE_COLOR:
								try {
									horse.setColor(MCHorseColor.valueOf(specArray.get(index, t).val().toUpperCase()));
								} catch (IllegalArgumentException exception) {
									throw new CREFormatException("Invalid horse color: " + specArray.get(index, t).val(), t);
								}
								break;
							case entity_spec.KEY_HORSE_STYLE:
								try {
									horse.setPattern(MCHorsePattern.valueOf(specArray.get(index, t).val().toUpperCase()));
								} catch (IllegalArgumentException exception) {
									throw new CREFormatException("Invalid horse style: " + specArray.get(index, t).val(), t);
								}
								break;
							case entity_spec.KEY_HORSE_VARIANT:
								try {
									horse.setVariant(MCHorseVariant.valueOf(specArray.get(index, t).val().toUpperCase()));
								} catch (IllegalArgumentException exception) {
									throw new CREFormatException("Invalid horse variant: " + specArray.get(index, t).val(), t);
								}
								break;
							case entity_spec.KEY_HORSE_CHEST:
								horse.setHasChest(Static.getBoolean(specArray.get(index, t)));
								break;
							case entity_spec.KEY_HORSE_JUMP:
								try {
									horse.setJumpStrength(Static.getDouble(specArray.get(index, t), t));
								} catch (IllegalArgumentException exception) {
									throw new CRERangeException("The jump strength must be between 0.0 and 2.0", t);
								}
								break;
							case entity_spec.KEY_HORSE_DOMESTICATION:
								try {
									horse.setDomestication(Static.getInt32(specArray.get(index, t), t));
								} catch (IllegalArgumentException exception) {
									throw new CRERangeException("The domestication level can not be higher than the max domestication level.", t);
								}
								break;
							case entity_spec.KEY_HORSE_MAXDOMESTICATION:
								horse.setMaxDomestication(Static.getInt32(specArray.get(index, t), t));
								break;
							case entity_spec.KEY_HORSE_SADDLE:
								horse.setSaddle(ObjectGenerator.GetGenerator().item(specArray.get(index, t), t));
								break;
							case entity_spec.KEY_HORSE_ARMOR:
								horse.setArmor(ObjectGenerator.GetGenerator().item(specArray.get(index, t), t));
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case IRON_GOLEM:
					MCIronGolem golem = (MCIronGolem) entity;
					for (String index : specArray.stringKeySet()) {
						switch (index.toLowerCase()) {
							case entity_spec.KEY_IRON_GOLEM_PLAYERCREATED:
								golem.setPlayerCreated(Static.getBoolean(specArray.get(index, t)));
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case ITEM_FRAME:
					MCItemFrame frame = (MCItemFrame) entity;
					for (String index : specArray.stringKeySet()) {
						switch (index.toLowerCase()) {
							case entity_spec.KEY_ITEM_FRAME_ITEM:
								frame.setItem(ObjectGenerator.GetGenerator().item(specArray.get(index, t), t));
								if (specArray.get(index, t) instanceof CNull) {
									frame.setItem(null);
								} else {
									frame.setItem(ObjectGenerator.GetGenerator().item(specArray.get(index, t), t));
								}
								break;
							case entity_spec.KEY_ITEM_FRAME_ROTATION:
								try {
									frame.setRotation(MCRotation.valueOf(specArray.get(index, t).val().toUpperCase()));
								} catch (IllegalArgumentException exception) {
									throw new CREFormatException("Invalid rotation type: " + specArray.get(index, t).val(), t);
								}
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case MAGMA_CUBE:
				case SLIME:
					MCSlime cube = (MCSlime) entity;
					for (String index : specArray.stringKeySet()) {
						switch (index.toLowerCase()) {
							case entity_spec.KEY_SLIME_SIZE:
								cube.setSize(Static.getInt32(specArray.get(index, t), t));
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case MINECART:
					MCMinecart minecart = (MCMinecart) entity;
					for (String index : specArray.stringKeySet()) {
						switch (index.toLowerCase()) {
							case entity_spec.KEY_MINECART_BLOCK:
								minecart.setDisplayBlock(ObjectGenerator.GetGenerator().material(specArray.get(index, t), t).getData());
								break;
							case entity_spec.KEY_MINECART_OFFSET:
								minecart.setDisplayBlockOffset(Static.getInt32(specArray.get(index, t), t));
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case MINECART_COMMAND:
					MCCommandMinecart commandminecart = (MCCommandMinecart) entity;
					for (String index : specArray.stringKeySet()) {
						switch (index.toLowerCase()) {
							case entity_spec.KEY_MINECART_COMMAND_CUSTOMNAME:
								if(specArray.get(index, t) instanceof CNull) {
									commandminecart.setName(null);
								} else {
									commandminecart.setName(specArray.get(index, t).val());
								}
								break;
							case entity_spec.KEY_MINECART_COMMAND_COMMAND:
								if(specArray.get(index, t) instanceof CNull) {
									commandminecart.setCommand(null);
								} else {
									commandminecart.setCommand(specArray.get(index, t).val());
								}
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case OCELOT:
					MCOcelot ocelot = (MCOcelot) entity;
					for (String index : specArray.stringKeySet()) {
						switch (index.toLowerCase()) {
							case entity_spec.KEY_OCELOT_TYPE:
								try {
									ocelot.setCatType(MCOcelotType.valueOf(specArray.get(index, t).val().toUpperCase()));
								} catch (IllegalArgumentException exception) {
									throw new CREFormatException("Invalid ocelot type: " + specArray.get(index, t).val(), t);
								}
								break;
							case entity_spec.KEY_OCELOT_SITTING:
								ocelot.setSitting(Static.getBoolean(specArray.get(index, t)));
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case PAINTING:
					MCPainting painting = (MCPainting) entity;
					for (String index : specArray.stringKeySet()) {
						switch (index.toLowerCase()) {
							case entity_spec.KEY_PAINTING_ART:
								try {
									painting.setArt(MCArt.valueOf(specArray.get(index, t).val().toUpperCase()));
								} catch (IllegalArgumentException exception) {
									throw new CREFormatException("Invalid art type: " + specArray.get(index, t).val(), t);
								}
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case PIG:
					MCPig pig = (MCPig) entity;
					for (String index : specArray.stringKeySet()) {
						switch (index.toLowerCase()) {
							case entity_spec.KEY_PIG_SADDLED:
								pig.setSaddled(Static.getBoolean(specArray.get(index, t)));
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case PIG_ZOMBIE:
					MCPigZombie pigZombie = (MCPigZombie) entity;
					for (String index : specArray.stringKeySet()) {
						switch (index.toLowerCase()) {
							case entity_spec.KEY_ZOMBIE_BABY:
								pigZombie.setBaby(Static.getBoolean(specArray.get(index, t)));
								break;
							case entity_spec.KEY_ZOMBIE_VILLAGER:
								pigZombie.setVillager(Static.getBoolean(specArray.get(index, t)));
								break;
							case entity_spec.KEY_PIG_ZOMBIE_ANGRY:
								pigZombie.setAngry(Static.getBoolean(specArray.get(index, t)));
								break;
							case entity_spec.KEY_PIG_ZOMBIE_ANGER:
								pigZombie.setAnger(Static.getInt32(specArray.get(index, t), t));
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case PRIMED_TNT:
					MCTNT tnt = (MCTNT) entity;
					for (String index : specArray.stringKeySet()) {
						switch (index.toLowerCase()) {
							case entity_spec.KEY_PRIMED_TNT_FUSETICKS:
								tnt.setFuseTicks(Static.getInt32(specArray.get(index, t), t));
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case RABBIT:
					MCRabbit rabbit = (MCRabbit) entity;
					for (String index : specArray.stringKeySet()) {
						switch (index.toLowerCase()) {
							case entity_spec.KEY_RABBIT_TYPE:
								try {
									rabbit.setRabbitType(MCRabbitType.valueOf(specArray.get(index, t).val().toUpperCase()));
								} catch (IllegalArgumentException exception) {
									throw new CREFormatException("Invalid rabbit type: " + specArray.get(index, t).val(), t);
								}
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case SHEEP:
					MCSheep sheep = (MCSheep) entity;
					for (String index : specArray.stringKeySet()) {
						switch (index.toLowerCase()) {
							case entity_spec.KEY_SHEEP_COLOR:
								try {
									sheep.setColor(MCDyeColor.valueOf(specArray.get(index, t).val().toUpperCase()));
								} catch (IllegalArgumentException exception) {
									throw new CREFormatException("Invalid sheep color: " + specArray.get(index, t).val(), t);
								}
								break;
							case entity_spec.KEY_SHEEP_SHEARED:
								sheep.setSheared(Static.getBoolean(specArray.get(index, t)));
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case SKELETON:
					MCSkeleton skeleton = (MCSkeleton) entity;
					for (String index : specArray.stringKeySet()) {
						switch (index.toLowerCase()) {
							case entity_spec.KEY_SKELETON_TYPE:
								try {
									skeleton.setSkeletonType(MCSkeletonType.valueOf(specArray.get(index, t).val().toUpperCase()));
								} catch (IllegalArgumentException exception) {
									throw new CREFormatException("Invalid skeleton type: " + specArray.get(index, t).val(), t);
								}
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case SNOWMAN:
					if (Static.getVersion().gte(MCVersion.MC1_9_4)) {
						MCSnowman snowman = (MCSnowman) entity;
						for (String index : specArray.stringKeySet()) {
							switch (index.toLowerCase()) {
								case entity_spec.KEY_SNOWMAN_DERP:
									snowman.setDerp(Static.getBoolean(specArray.get(index, t)));
									break;
								default:
									throwException(index, t);
							}
						}
					}
					break;
				case LINGERING_POTION:
				case SPLASH_POTION:
					MCThrownPotion potion = (MCThrownPotion) entity;
					for (String index : specArray.stringKeySet()) {
						switch (index.toLowerCase()) {
							case entity_spec.KEY_SPLASH_POTION_ITEM:
								MCItemStack potionItem = ObjectGenerator.GetGenerator().item(specArray.get(index, t), t);
								try {
									potion.setItem(potionItem);
								} catch(IllegalArgumentException ex){
									throw new CREFormatException("Invalid potion type: " + potionItem.getType().getName(), t);
								}
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case TIPPED_ARROW:
					MCTippedArrow tippedarrow = (MCTippedArrow) entity;
					for (String index : specArray.stringKeySet()) {
						switch (index.toLowerCase()) {
							case entity_spec.KEY_ARROW_CRITICAL:
								tippedarrow.setCritical(Static.getBoolean(specArray.get(index, t)));
								break;
							case entity_spec.KEY_ARROW_KNOCKBACK:
								int k = Static.getInt32(specArray.get(index, t), t);
								if (k < 0) {
									throw new CRERangeException("Knockback can not be negative.", t);
								} else {
									tippedarrow.setKnockbackStrength(k);
								}
								break;
							case entity_spec.KEY_TIPPEDARROW_POTIONMETA:
								Construct c = specArray.get(index, t);
								if(c instanceof CArray){
									CArray meta = (CArray) c;
									if(meta.containsKey("base")){
										Construct base = meta.get("base", t);
										if(base instanceof CArray){
											MCPotionData pd = ObjectGenerator.GetGenerator().potionData((CArray) base, t);
											tippedarrow.setBasePotionData(pd);
										}
									}
									if(meta.containsKey("potions")){
										tippedarrow.clearCustomEffects();
										Construct potions = meta.get("potions", t);
										if(potions instanceof CArray){
											List<MCLivingEntity.MCEffect> list = ObjectGenerator.GetGenerator().potions((CArray) potions, t);
											for(MCLivingEntity.MCEffect effect : list) {
												tippedarrow.addCustomEffect(effect);
											}
										}
									}
								} else {
									throw new CRECastException("TippedArrow potion meta must be an array", t);
								}
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case VILLAGER:
					MCVillager villager = (MCVillager) entity;
					for (String index : specArray.stringKeySet()) {
						switch (index.toLowerCase()) {
							case entity_spec.KEY_VILLAGER_PROFESSION:
								try {
									villager.setProfession(MCProfession.valueOf(specArray.get(index, t).val().toUpperCase()));
								} catch (IllegalArgumentException exception) {
									throw new CREFormatException("Invalid profession: " + specArray.get(index, t).val(), t);
								}
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case WITHER_SKULL:
					MCWitherSkull skull = (MCWitherSkull) entity;
					for (String index : specArray.stringKeySet()) {
						switch (index.toLowerCase()) {
							case entity_spec.KEY_WITHER_SKULL_CHARGED:
								skull.setCharged(Static.getBoolean(specArray.get(index, t)));
								break;
							case entity_spec.KEY_FIREBALL_DIRECTION:
								skull.setDirection(ObjectGenerator.GetGenerator().vector(specArray.get(index, t), t));
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case WOLF:
					MCWolf wolf = (MCWolf) entity;
					for (String index : specArray.stringKeySet()) {
						switch (index.toLowerCase()) {
							case entity_spec.KEY_WOLF_ANGRY:
								wolf.setAngry(Static.getBoolean(specArray.get(index, t)));
								break;
							case entity_spec.KEY_WOLF_COLOR:
								try {
									wolf.setCollarColor(MCDyeColor.valueOf(specArray.get(index, t).val().toUpperCase()));
								} catch (IllegalArgumentException exception) {
									throw new CREFormatException("Invalid collar color: " + specArray.get(index, t).val(), t);
								}
								break;
							case entity_spec.KEY_WOLF_SITTING:
								wolf.setSitting(Static.getBoolean(specArray.get(index, t)));
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case ZOMBIE:
					MCZombie zombie = (MCZombie) entity;
					for (String index : specArray.stringKeySet()) {
						switch (index.toLowerCase()) {
							case entity_spec.KEY_ZOMBIE_BABY:
								zombie.setBaby(Static.getBoolean(specArray.get(index, t)));
								break;
							case entity_spec.KEY_ZOMBIE_VILLAGER:
								zombie.setVillager(Static.getBoolean(specArray.get(index, t)));
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				default:
					for (String index : specArray.stringKeySet()) {
						throwException(index, t);
					}
			}

			return CVoid.VOID;
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class get_projectile_shooter extends EntityGetterFunction {

		@Override
		public String getName() {
			return "get_projectile_shooter";
		}

		@Override
		public String docs() {
			return "mixed {entityID} Returns the shooter of the given projectile, can be null."
					+ " If the shooter is an entity, that entity's ID will be return, but if it is a block,"
					+ " that block's location will be returned.";
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCEntity entity = Static.getEntity(args[0], t);

			if (entity instanceof MCProjectile) {
				MCProjectileSource shooter = ((MCProjectile) entity).getShooter();

				if (shooter instanceof MCBlockProjectileSource) {
					return ObjectGenerator.GetGenerator().location(((MCBlockProjectileSource) shooter).getBlock().getLocation(), false);
				} else if (shooter instanceof MCEntity) {
					return new CString(((MCEntity) shooter).getUniqueId().toString(), t);
				} else {
					return CNull.NULL;
				}
			} else {
				throw new CREBadEntityException("The given entity is not a projectile.", t);
			}
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class set_projectile_shooter extends EntitySetterFunction {

		@Override
		public String getName() {
			return "set_projectile_shooter";
		}

		@Override
		public String docs() {
			return "void {entityID, shooterID} Sets the shooter of the given projectile. This can be entity UUID,"
					+ " dispenser location array (throws CastException if not a dispenser), or null.";
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCEntity entity = Static.getEntity(args[0], t);

			if (entity instanceof MCProjectile) {
				if (args[1] instanceof CNull) {
					((MCProjectile) entity).setShooter(null);
				} else if (args[1] instanceof CArray) {
					MCBlock b = ObjectGenerator.GetGenerator().location(args[1], entity.getWorld(), t).getBlock();
					if(b.isDispenser()){
						((MCProjectile) entity).setShooter(b.getDispenser().getBlockProjectileSource());
					} else {
						throw new CRECastException("Given block location is not a dispenser.", t);
					}
				} else {
					((MCProjectile) entity).setShooter(Static.getLivingEntity(args[1], t));
				}
			} else {
				throw new CREBadEntityException("The given entity is not a projectile.", t);
			}

			return CVoid.VOID;
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class get_projectile_bounce extends EntityGetterFunction {

		@Override
		public String getName() {
			return "get_projectile_bounce";
		}

		@Override
		public String docs() {
			return "boolean {entityID} Returns whether or not the given projectile should bounce or not when it hits something.";
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCEntity entity = Static.getEntity(args[0], t);

			if (entity instanceof MCProjectile) {
				return CBoolean.get(((MCProjectile) entity).doesBounce());
			} else {
				throw new CREBadEntityException("The given entity is not a projectile.", t);
			}
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class set_projectile_bounce extends EntitySetterFunction {

		@Override
		public String getName() {
			return "set_projectile_bounce";
		}

		@Override
		public String docs() {
			return "void {entityID, boolean} Sets whether or not the given projectile should bounce or not when it hits something.";
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCEntity entity = Static.getEntity(args[0], t);

			if (entity instanceof MCProjectile) {
				((MCProjectile) entity).setBounce(Static.getBoolean(args[1]));
			} else {
				throw new CREBadEntityException("The given entity is not a projectile.", t);
			}

			return CVoid.VOID;
 		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class damage_entity extends EntityFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CRELengthException.class,
				CREBadEntityTypeException.class, CREBadEntityException.class};
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCEntity entity = Static.getEntity(args[0], t);

			if (!(entity instanceof MCLivingEntity)) {
				throw new CREBadEntityTypeException("The entity id provided doesn't"
					+ " belong to a living entity", t);
			}

			MCLivingEntity living = (MCLivingEntity)entity;

			double damage = Static.getDouble(args[1], t);
			if (args.length == 3) {
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
	public static class entity_fall_distance extends EntityGetterFunction {

		@Override
		public String getName() {
			return "entity_fall_distance";
		}

		@Override
		public String docs() {
			return "double {entityID} Returns the distance the entity has fallen.";
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			return new CDouble(Static.getEntity(args[0], t).getFallDistance(), t);
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class set_entity_fall_distance extends EntitySetterFunction {

		@Override
		public String getName() {
			return "set_entity_fall_distance";
		}

		@Override
		public String docs() {
			return "void {entityID, double} Sets the distance the entity has fallen.";
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			Static.getEntity(args[0], t).setFallDistance(ArgumentValidation.getDouble32(args[1], t));
			return CVoid.VOID;
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}
	
	@api
	public static class set_entity_glowing extends EntitySetterFunction {
		public String getName() {
			return "set_entity_glowing";
		}

		public String docs() {
			return "void {Entity ID, boolean} If true, applies glowing effect to the entity";
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			Static.getEntity(args[0], t).setGlowing(Static.getBoolean(args[1]));
			return CVoid.VOID;
		}

		public Version since() {
			return CHVersion.V3_3_2;
		}
	}

	@api
	public static class get_entity_glowing extends EntityGetterFunction {
		public String getName() {
			return "get_entity_glowing";
		}

		public String docs() {
			return "boolean {Entity} Returns true if the entity is glowing";
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCEntity e = Static.getEntity(args[0], t);
			return CBoolean.GenerateCBoolean(e.isGlowing(), t);
		}

		public Version since() {
			return CHVersion.V3_3_2;
		}
	}
	
	@api
	public static class set_entity_gliding extends EntitySetterFunction {
		public String getName() {
			return "set_entity_gliding";
		}

		public String docs() {
			return "void {Entity, boolean} If possible, makes the entity glide";
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
	public static class get_entity_gliding extends EntityGetterFunction {
		public String getName() {
			return "get_entity_gliding";
		}

		public String docs() {
			return "boolean {Entity} Returns true if the given entity is gliding";
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			return CBoolean.GenerateCBoolean(Static.getLivingEntity(args[0], t).isGliding(), t);
		}

		public Version since() {
			return CHVersion.V3_3_2;
		}
	}
	
	@api
	public static class get_entity_ai extends EntityGetterFunction {
		public String getName() {
			return "get_entity_ai";
		}

		public String docs() {
			return "boolean {Entity} Returns true if the given entity has AI";
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			return CBoolean.GenerateCBoolean(Static.getLivingEntity(args[0], t).hasAI(), t);
		}

		public Version since() {
			return CHVersion.V3_3_2;
		}
	}
	
	@api
	public static class set_entity_ai extends EntitySetterFunction {
		public String getName() {
			return "set_entity_ai";
		}

		public String docs() {
			return "void {Entity, boolean} enables or disables the entity AI";
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
