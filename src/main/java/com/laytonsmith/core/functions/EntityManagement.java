package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.*;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCBlockFace;
import com.laytonsmith.abstraction.entities.MCBoat;
import com.laytonsmith.abstraction.entities.MCMinecart;
import com.laytonsmith.abstraction.enums.MCArt;
import com.laytonsmith.abstraction.enums.MCEntityEffect;
import com.laytonsmith.abstraction.enums.MCEntityType;
import com.laytonsmith.abstraction.enums.MCEquipmentSlot;
import com.laytonsmith.abstraction.enums.MCProjectileType;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author jb_aero
 */
public class EntityManagement {
	public static String docs(){
        return "This class of functions allow entities to be managed.";
    }

	public static abstract class EntityFunction extends AbstractFunction {
		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	public static abstract class EntityGetterFunction extends EntityFunction {
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.BadEntityException};
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}
	}

	public static abstract class EntitySetterFunction extends EntityFunction {
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.FormatException, ExceptionType.CastException,
					ExceptionType.BadEntityException};
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}
	}

	@api
	public static class all_entities extends EntityFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.InvalidWorldException, ExceptionType.FormatException,
					ExceptionType.CastException};
		}

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			CArray ret = new CArray(t);
			if (args.length == 0) {
				for (MCWorld w : Static.getServer().getWorlds()) {
					for (MCEntity e : w.getEntities()) {
						ret.push(new CInt(e.getEntityId(), t));
					}
				}
			} else {
				MCWorld w;
				MCChunk c;
				if (args.length == 3) {
					w = Static.getServer().getWorld(args[0].val());
					if (w == null) {
						throw new ConfigRuntimeException("Unknown world: " + args[0].val(), ExceptionType.InvalidWorldException, t);
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
						ret.push(new CInt(e.getEntityId(), t));
					}
				} else {
					if (args[0] instanceof CArray) {
						c = ObjectGenerator.GetGenerator().location(args[0], null, t).getChunk();
						for (MCEntity e : c.getEntities()) {
							ret.push(new CInt(e.getEntityId(), t));
						}
					} else {
						w = Static.getServer().getWorld(args[0].val());
						if (w == null) {
							throw new ConfigRuntimeException("Unknown world: " + args[0].val(), ExceptionType.InvalidWorldException, t);
						}
						for (MCEntity e : w.getEntities()) {
							ret.push(new CInt(e.getEntityId(), t));
						}
					}
				}
			}
			return ret;
		}

		public String getName() {
			return "all_entities";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1, 3};
		}

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

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCEntity e;
			try {
				e = Static.getEntity(Static.getInt32(args[0], t), t);
			} catch (ConfigRuntimeException cre) {
				return new CBoolean(false, t);
			}
			return new CBoolean(true, t);
		}

		public String getName() {
			return "entity_exists";
		}

		public String docs() {
			return "boolean {entityID} Returns true if entity exists, otherwise false.";
		}
	}

	@api
	public static class entity_loc extends EntityGetterFunction {

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCEntity e = Static.getEntity(Static.getInt32(args[0], t), t);
			return ObjectGenerator.GetGenerator().location(e.getLocation());
		}

		public String getName() {
			return "entity_loc";
		}

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

	}

	@api
	public static class set_entity_loc extends EntitySetterFunction {

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.BadEntityException, ExceptionType.FormatException,
					ExceptionType.CastException, ExceptionType.InvalidWorldException};
		}

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCEntity e = Static.getEntity(Static.getInt32(args[0], t), t);
			MCLocation l;
			if (args[1] instanceof CArray) {
				l = ObjectGenerator.GetGenerator().location((CArray) args[1], e.getWorld(), t);
			} else {
				throw new Exceptions.FormatException("An array was expected but recieved " + args[1], t);
			}
			return new CBoolean(e.teleport(l), t);
		}

		public String getName() {
			return "set_entity_loc";
		}

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

	}

	@api
	public static class entity_velocity extends EntityGetterFunction {

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {

			MCEntity e = Static.getEntity(Static.getInt32(args[0], t), t);
			CArray va = ObjectGenerator.GetGenerator().velocity(e.getVelocity(), t);
			return va;
		}

		public String getName() {
			return "entity_velocity";
		}

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

	}

	@api
	public static class set_entity_velocity extends EntitySetterFunction {

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {

			MCEntity e = Static.getEntity(Static.getInt32(args[0], t), t);
			e.setVelocity(ObjectGenerator.GetGenerator().velocity(args[1], t));
			return new CVoid(t);
		}

		public String getName() {
			return "set_entity_velocity";
		}

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

	}

	@api
	public static class entity_remove extends EntityGetterFunction {

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCEntity ent = Static.getEntity((int) Static.getInt(args[0], t), t);
			if (ent == null) {
				return new CVoid(t);
			} else if (ent instanceof MCHumanEntity) {
				throw new ConfigRuntimeException("Cannot remove human entity (" + ent.getEntityId() + ")!",
						ExceptionType.BadEntityException, t);
			} else {
				ent.remove();
				return new CVoid(t);
			}
		}

		public String getName() {
			return "entity_remove";
		}

		public String docs() {
			return "void {entityID} Removes the specified entity from the world, without any drops or animations. "
				+ "Note: you can't remove players. As a safety measure for working with NPC plugins, it will "
				+ "not work on anything human, even if it is not a player.";
		}

	}

	@api
	public static class entity_type extends EntityGetterFunction {

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCEntity ent;
			int id = Static.getInt32(args[0], t);
			try {
				ent = Static.getEntity(id, t);
			} catch (ConfigRuntimeException cre) {
				return new CNull(t);
			}
			return new CString(ent.getType().name(), t);
		}

		public String getName() {
			return "entity_type";
		}

		public String docs() {
			return "mixed {entityID} Returns the EntityType of the entity with the specified ID."
					+ " Returns null if the entity does not exist.";
		}
	}

	@api
	public static class get_entity_age extends EntityGetterFunction {

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			int id = Static.getInt32(args[0], t);
			MCEntity ent = Static.getEntity(id, t);
			if (ent == null) {
				return new CNull(t);
			} else {
				return new CInt(ent.getTicksLived(), t);
			}
		}

		public String getName() {
			return "get_entity_age";
		}

		public String docs() {
			return "int {entityID} Returns the entity age as an integer, represented by server ticks.";
		}
	}

	@api
	public static class set_entity_age extends EntitySetterFunction {

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.BadEntityException,
					ExceptionType.RangeException};
		}

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			int id = Static.getInt32(args[0], t);
			int age = Static.getInt32(args[1], t);

			if (age < 1) {
				throw new ConfigRuntimeException("Entity age can't be less than 1 server tick.", ExceptionType.RangeException, t);
			}

			MCEntity ent = Static.getEntity(id, t);
			if (ent == null) {
				return new CNull(t);
			} else {
				ent.setTicksLived(age);
				return new CVoid(t);
			}
		}

		public String getName() {
			return "set_entity_age";
		}

		public String docs() {
			return "void {entityID, int} Sets the age of the entity to the specified int, represented by server ticks.";
		}
	}

	@api
	public static class get_mob_age extends EntityGetterFunction {

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.UnageableMobException, ExceptionType.CastException,
					ExceptionType.BadEntityException};
		}

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			int id = Static.getInt32(args[0], t);
			MCLivingEntity ent = Static.getLivingEntity(id, t);
			if (ent == null) {
				return new CNull(t);
			} else if (ent instanceof MCAgeable) {
				MCAgeable mob = ((MCAgeable) ent);
				return new CInt(mob.getAge(), t);
			} else {
				throw new ConfigRuntimeException("The specified entity does not age", ExceptionType.UnageableMobException, t);
			}
		}

		public String getName() {
			return "get_mob_age";
		}

		public String docs() {
			return "int {entityID} Returns the mob's age as an integer. Zero represents the point of adulthood. Throws an"
					+ " UnageableMobException if the mob is not a type that ages";
		}
	}

	@api
	public static class set_mob_age extends EntityFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.UnageableMobException, ExceptionType.CastException,
					ExceptionType.BadEntityException};
		}

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			int id = Static.getInt32(args[0], t);
			int age = Static.getInt32(args[1], t);
			boolean lock = false;
			if (args.length == 3) {
				lock = (boolean) Static.getBoolean(args[2]);
			}
			MCLivingEntity ent = Static.getLivingEntity(id, t);
			if (ent == null) {
				return new CNull(t);
			} else if (ent instanceof MCAgeable) {
				MCAgeable mob = ((MCAgeable) ent);
				mob.setAge(age);
				mob.setAgeLock(lock);
				return new CVoid(t);
			} else {
				throw new ConfigRuntimeException("The specified entity does not age", ExceptionType.UnageableMobException, t);
			}
		}

		public String getName() {
			return "set_mob_age";
		}

		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		public String docs() {
			return "void {entityID, int[, lockAge]} sets the age of the mob to the specified int, and locks it at that age"
					+ " if lockAge is true, but by default it will not. Throws a UnageableMobException if the mob does"
					+ " not age naturally.";
		}
	}

	@api
	public static class get_mob_effects extends EntityGetterFunction {

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCLivingEntity mob = Static.getLivingEntity(Static.getInt32(args[0], t), t);
			return ObjectGenerator.GetGenerator().potions(mob.getEffects(), t);
		}

		public String getName() {
			return "get_mob_effects";
		}

		public String docs() {
			return "array {entityID} Returns an array of potion arrays showing"
					+ " the effects on this mob.";
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{new ExampleScript("Basic use", "msg(get_mob_effects(259))",
					"{{ambient: false, id: 1, seconds: 30, strength: 1}}")};
		}

	}

	@api
	public static class set_mob_effect extends EntityFunction {

		public String getName() {
			return "set_mob_effect";
		}

		public Integer[] numArgs() {
			return new Integer[]{3, 4, 5};
		}

		public String docs() {
			return "boolean {entityId, potionID, strength, [seconds], [ambient]} Effect is 1-23. Seconds defaults to 30."
					+ " If the potionID is out of range, a RangeException is thrown, because out of range potion effects"
					+ " cause the client to crash, fairly hardcore. See http://www.minecraftwiki.net/wiki/Potion_effects"
					+ " for a complete list of potions that can be added. To remove an effect, set the seconds to 0."
					+ " Strength is the number of levels to add to the base power (effect level 1). Ambient takes a boolean"
					+ " of whether the particles should be less noticeable. The function returns true if the effect was"
					+ " added or removed as desired, and false if it wasn't (however, this currently only will happen if"
					+ " an effect is attempted to be removed, yet isn't already on the mob).";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.FormatException,
					ExceptionType.BadEntityException, ExceptionType.RangeException};
		}

		public Construct exec(Target t, Environment env, Construct... args)
				throws ConfigRuntimeException {
			MCLivingEntity mob = Static.getLivingEntity(Static.getInt32(args[0], t), t);

			int effect = Static.getInt32(args[1], t);

			int strength = Static.getInt32(args[2], t);
			int seconds = 30;
			boolean ambient = false;
			if (args.length >= 4) {
				seconds = Static.getInt32(args[3], t);
			}
			if (args.length == 5) {
				ambient = Static.getBoolean(args[4]);
			}

			if (seconds == 0) {
				return new CBoolean(mob.removeEffect(effect), t);
			} else {
				mob.addEffect(effect, strength, seconds, ambient, t);
				return new CBoolean(true, t);
			}
		}
	}

	@api(environments={CommandHelperEnvironment.class})
	public static class shoot_projectile extends EntityFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.BadEntityException, ExceptionType.FormatException,
					ExceptionType.PlayerOfflineException};
		}

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCCommandSender cmdsr = environment.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCLivingEntity ent = null;
			int id;
			MCProjectileType toShoot = MCProjectileType.FIREBALL;
			if (args.length >= 1) {
				try {
					id = Static.GetPlayer(args[0], t).getEntityId();
				} catch (ConfigRuntimeException notPlayer) {
					try {
						id = Static.getInt32(args[0], t);
					} catch (ConfigRuntimeException notEntIDEither) {
						throw new ConfigRuntimeException("Could not find an entity matching " + args[0] + "!",
								ExceptionType.BadEntityException, t);
					}
				}
			} else {
				if (cmdsr instanceof MCPlayer) {
					id = ((MCLivingEntity) cmdsr).getEntityId();
					Static.AssertPlayerNonNull((MCPlayer) cmdsr, t);
				} else {
					throw new ConfigRuntimeException("A player was expected!", ExceptionType.PlayerOfflineException, t);
				}
			}
			if (args.length == 2) {
				try {
					toShoot = MCProjectileType.valueOf(args[1].toString().toUpperCase());
				} catch (IllegalArgumentException badEnum) {
					throw new ConfigRuntimeException(args[1] + " is not a valid Projectile", ExceptionType.FormatException, t);
				}
			}
			ent = Static.getLivingEntity(id, t);
			return new CInt(ent.launchProjectile(toShoot).getEntityId(), t);
		}

		public String getName() {
			return "shoot_projectile";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1, 2};
		}

		public String docs() {
			return "int {[player[, projectile]] | [entityID[, projectile]]} shoots a projectile from the entity or player"
					+ " specified, or the current player if no arguments are passed. If no projectile is specified,"
					+ " it defaults to a fireball. Returns the EntityID of the projectile. Valid projectiles: "
					+ StringUtils.Join(MCProjectileType.values(), ", ", ", or ", " or ");
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class entities_in_radius extends EntityFunction {

		public String getName() {
			return "entities_in_radius";
		}

		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			MCPlayer p = env.getEnv(CommandHelperEnvironment.class).GetPlayer();

			MCLocation loc;
			int dist;
			List<String> types = new ArrayList<String>();

			if (!(args[0] instanceof CArray)) {
				throw new ConfigRuntimeException("Expecting an array at parameter 1 of entities_in_radius",
						ExceptionType.BadEntityException, t);
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

			Set<Integer> eSet = new HashSet<Integer>();
			for (int chX = 0 - chunkRadius; chX <= chunkRadius; chX++) {
				for (int chZ = 0 - chunkRadius; chZ <= chunkRadius; chZ++) {
					MCLocation nl = StaticLayer.GetLocation(loc.getWorld(), loc.getX()+(chX*16), loc.getY(), loc.getZ()+(chZ*16));
					for (MCEntity e : nl.getChunk().getEntities()) {
						if (e.getLocation().distance(loc) <= dist && e.getLocation().getBlock() != loc.getBlock()) {
							if (types.isEmpty() || types.contains(e.getType().name())) {
								eSet.add(e.getEntityId());
							}
						}
					}
				}
			}
			CArray entities = new CArray(t);
			for(int e : eSet){
				entities.push(new CInt(e, t));
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
					throw new ConfigRuntimeException(String.format("Wrong entity type: %s", type),
							ExceptionType.BadEntityException, t);
				}

				newTypes.add(entityType.name());
			}

			return newTypes;
		}

		public String docs() {
			return "array {location array, distance, [type] | location array, distance, [arrayTypes]} Returns an array of"
					+ " all entities within the given radius. Set type argument to filter entities to a specific type. You"
					+ " can pass an array of types. Valid types (case doesn't matter): "
					+ StringUtils.Join(MCEntityType.values(), ", ", ", or ", " or ");
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.BadEntityException,
					ExceptionType.FormatException};
		}
	}

	//@api
	public static class get_mob_target extends EntityGetterFunction {

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCLivingEntity le = Static.getLivingEntity(Static.getInt32(args[0], t), t);
			if (le.getTarget(t) == null) {
				return new CNull(t);
			} else {
				return new CInt(le.getTarget(t).getEntityId(), t);
			}
		}

		public String getName() {
			return "get_mob_target";
		}

		public String docs() {
			return "entityID {entityID} Gets the mob's target if it has one, and returns the target's entityID."
					+ " If there is no target, null is returned instead.";
		}
	}

	//@api
	public static class set_mob_target extends EntitySetterFunction {

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.BadEntityException, ExceptionType.CastException};
		}

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCLivingEntity le = Static.getLivingEntity(Static.getInt32(args[0], t), t);
			MCLivingEntity target = null;
			if (!(args[1] instanceof CNull)) {
				target = Static.getLivingEntity(Static.getInt32(args[1], t), t);
			}
			le.setTarget(target, t);
			return new CVoid(t);
		}

		public String getName() {
			return "set_mob_target";
		}

		public String docs() {
			return "void {entityID, entityID} The first ID is the entity who is targetting, the second is the target."
					+ " It can also be set to null to clear the current target.";
		}
	}

	@api
	public static class get_mob_equipment extends EntityGetterFunction {

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCLivingEntity le = Static.getLivingEntity(Static.getInt32(args[0], t), t);
			Map<MCEquipmentSlot, MCItemStack> eq = le.getEquipment().getAllEquipment();
			CArray ret = CArray.GetAssociativeArray(t);
			for (MCEquipmentSlot key : eq.keySet()) {
				ret.set(key.name().toLowerCase(), ObjectGenerator.GetGenerator().item(eq.get(key), t), t);
			}
			return ret;
		}

		public String getName() {
			return "get_mob_equipment";
		}

		public String docs() {
			return "equipmentArray {entityID} Returns an associative array showing the equipment this entity is wearing.";
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
					new ExampleScript("Getting a mob's equipment", "get_mob_equipment(276)", "{boots: null,"
							+ " chestplate: null, helmet: {data: 0, enchants: {} meta: null, type: 91}, leggings: null,"
							+ " weapon: {data: 5, enchants: {} meta: {display: Excalibur, lore: null}, type: 276}}")
			};
		}
	}

	@api
	public static class set_mob_equipment extends EntitySetterFunction {

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCEntityEquipment ee = Static.getLivingEntity(Static.getInt32(args[0], t), t).getEquipment();
			Map<MCEquipmentSlot, MCItemStack> eq = ee.getAllEquipment();
			if (args[1] instanceof CNull) {
				ee.clearEquipment();
				return new CVoid(t);
			} else if (args[1] instanceof CArray) {
				CArray ea = (CArray) args[1];
				for (String key : ea.keySet()) {
					try {
						eq.put(MCEquipmentSlot.valueOf(key.toUpperCase()), ObjectGenerator.GetGenerator().item(ea.get(key, t), t));
					} catch (IllegalArgumentException iae) {
						throw new Exceptions.FormatException("Not an equipment slot: " + key, t);
					}
				}
			} else {
				throw new Exceptions.FormatException("Expected argument 2 to be an array", t);
			}
			ee.setAllEquipment(eq);
			return new CVoid(t);
		}

		public String getName() {
			return "set_mob_equipment";
		}

		public String docs() {
			return "void {entityID, array} Takes an associative array with keys representing equipment slots and values"
					+ " of itemArrays, the same used by set_pinv. The equipment slots are: "
					+ StringUtils.Join(MCEquipmentSlot.values(), ", ", ", or ", " or ");
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "set_mob_equipment(spawn_mob('SKELETON')[0], array(WEAPON: array(type: 261)))", "Gives a bow to a skeleton")
			};
		}
	}

	@api
	public static class get_max_health extends EntityGetterFunction {

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCLivingEntity le = Static.getLivingEntity(Static.getInt32(args[0], t), t);
			return new CDouble(le.getMaxHealth(), t);
		}

		public String getName() {
			return "get_max_health";
		}

		public String docs() {
			return "double {entityID} Returns the maximum health of this living entity.";
		}
	}

	@api
	public static class set_max_health extends EntitySetterFunction {

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCLivingEntity le = Static.getLivingEntity(Static.getInt32(args[0], t), t);
			le.setMaxHealth(Static.getDouble(args[1], t));
			return new CVoid(t);
		}

		public String getName() {
			return "set_max_health";
		}

		public String docs() {
			return "void {entityID, double} Sets the max health of a living entity, players included."
					+ " This value is persistent, and will not reset even after server restarts.";
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{new ExampleScript("Basic use",
					"set_max_health(256, 10)", "The entity will now only have 5 hearts max.")};
		}
	}

	@api
	public static class entity_onfire extends EntityGetterFunction {

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCEntity ent = Static.getEntity(Static.getInt32(args[0], t), t);
			return new CInt(Static.ticksToMs(ent.getFireTicks())/1000, t);
		}

		public String getName() {
			return "entity_onfire";
		}

		public String docs() {
			return "int {entityID} Returns the number of seconds until this entity"
					+ " stops being on fire, 0 if it already isn't.";
		}

	}

	@api
	public static class set_entity_onfire extends EntitySetterFunction {

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCEntity ent = Static.getEntity(Static.getInt32(args[0], t), t);
			int setTicks = (int) Static.msToTicks(Static.getInt(args[1], t)*1000);
			if (setTicks < 0) {
				throw new Exceptions.FormatException("Seconds cannot be less than 0", t);
			}
			ent.setFireTicks(setTicks);
			return new CVoid(t);
		}

		public String getName() {
			return "set_entity_onfire";
		}

		public String docs() {
			return "void {entityID, seconds} Sets the entity on fire for the"
					+ " given number of seconds. Throws a FormatException"
					+ " if seconds is less than 0.";
		}

	}

	@api
	public static class play_entity_effect extends EntitySetterFunction {

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCEntity ent = Static.getEntity(Static.getInt32(args[0], t), t);
			MCEntityEffect mee;
			try {
				mee = MCEntityEffect.valueOf(args[1].val().toUpperCase());
			} catch (IllegalArgumentException iae) {
				throw new Exceptions.FormatException("Unknown effect at arg 2.", t);
			}
			ent.playEffect(mee);
			return new CVoid(t);
		}

		public String getName() {
			return "play_entity_effect";
		}

		public String docs() {
			return "void {entityID, effect} Plays the given visual effect on the"
					+ " entity. Non-applicable effects simply won't happen. Note:"
					+ " the death effect makes the mob invisible to players and"
					+ " immune to melee attacks. When used on players, they are"
					+ " shown the respawn menu, but because they are not actually"
					+ " dead, they can only log out. Possible effects are: "
					+ StringUtils.Join(MCEntityEffect.values(), ", ", ", or ", " or ");
		}

	}
	
	@api
	public static class get_mob_name extends EntityGetterFunction {

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCLivingEntity le = Static.getLivingEntity(Static.getInt32(args[0], t), t);
			return new CString(le.getCustomName(), t);
		}

		public String getName() {
			return "get_mob_name";
		}

		public String docs() {
			return "string {entityID} Returns the name of the given mob.";
		}
	}
	
	@api
	public static class set_mob_name extends EntitySetterFunction {

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCLivingEntity le = Static.getLivingEntity(Static.getInt32(args[0], t), t);
			le.setCustomName(args[1].val());
			return new CVoid(t);
		}

		public String getName() {
			return "set_mob_name";
		}

		public String docs() {
			return "void {entityID, name} Sets the name of the given mob. This"
					+ " automatically truncates if it is more than 64 characters.";
		}
	}
	
	@api(environments = {CommandHelperEnvironment.class})
	public static class spawn_entity extends EntityFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.FormatException,
					ExceptionType.BadEntityException, ExceptionType.InvalidWorldException,
					ExceptionType.PlayerOfflineException};
		}

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
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
					throw new ConfigRuntimeException("A physical commandsender must exist or location must be explicit.",
							ExceptionType.PlayerOfflineException, t);
				}
			}
			if (args.length >= 2) {
				qty = Static.getInt32(args[1], t);
			}
			try {
				entType = MCEntityType.valueOf(args[0].val().toUpperCase());
				if (!entType.isSpawnable()) {
					throw new Exceptions.FormatException("Unspawnable entitytype: " + args[0].val(), t);
				}
			} catch (IllegalArgumentException iae) {
				throw new Exceptions.FormatException("Unknown entitytype: " + args[0].val(), t);
			}
			for (int i = 0; i < qty; i++) {
				switch (entType) {
					case DROPPED_ITEM:
						CArray c = new CArray(t);
						c.set("type", new CInt(1, t), t);
						c.set("qty", new CInt(qty, t), t);
						MCItemStack is = ObjectGenerator.GetGenerator().item(c, t);
						ent = l.getWorld().dropItem(l, is);
						qty = 0;
						break;
					case FALLING_BLOCK:
						ent = l.getWorld().spawnFallingBlock(l, 12, (byte) 0);
						break;
					default:
						ent = l.getWorld().spawn(l, entType);
				}
				ret.push(new CInt(ent.getEntityId(), t));
			}
			return ret;
		}

		public String getName() {
			return "spawn_entity";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3};
		}

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
	}

	@api
	public static class set_entity_rider extends EntitySetterFunction {
	
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCEntity horse, rider;
			boolean success;
			if (args[0] instanceof CNull) {
				horse = null;
			} else {
				horse = Static.getEntity(Static.getInt32(args[0], t), t);
			}
			if (args[1] instanceof CNull) {
				rider = null;
			} else {
				rider = Static.getEntity(Static.getInt32(args[1], t), t);
			}
			if ((horse == null && rider == null) || horse == rider) {
				throw new Exceptions.FormatException("Horse and rider cannot be the same entity", t);
			} else if (horse == null) {
				success = rider.leaveVehicle();
			} else if (rider == null) {
				success = horse.eject();
			} else {
				success = horse.setPassenger(rider);
			}
			return new CBoolean(success, t);
		}
	
		public String getName() {
			return "set_entity_rider";
		}
	
		public String docs() {
			return "boolean {horse, rider} Sets the way two entities are stacked. Horse and rider are entity ids."
					+ " If rider is null, horse will eject its current rider, if it has one. If horse is null,"
					+ " rider will leave whatever it is riding. If horse and rider are both valid entities,"
					+ " rider will ride horse. The function returns the success of whatever operation is done."
					+ " If horse and rider are both null, or otherwise the same, a FormatException is thrown.";
		}
	}
	
	@api
	public static class get_entity_rider extends EntityGetterFunction {

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCEntity ent = Static.getEntity(Static.getInt32(args[0], t), t);
			if (ent.getPassenger() instanceof MCEntity) {
				return new CInt(ent.getPassenger().getEntityId(), t);
			}
			return null;
		}

		public String getName() {
			return "get_entity_rider";
		}

		public String docs() {
			return "mixed {entityID} Returns the ID of the given entity's rider, or null if it doesn't have one.";
		}
	}
	
	@api
	public static class get_entity_vehicle extends EntityGetterFunction {

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCEntity ent = Static.getEntity(Static.getInt32(args[0], t), t);
			if (ent.isInsideVehicle()) {
				return new CInt(ent.getVehicle().getEntityId(), t);
			}
			return new CNull(t);
		}

		public String getName() {
			return "get_entity_vehicle";
		}

		public String docs() {
			return "mixed {entityID} Returns the ID of the given entity's vehicle, or null if it doesn't have one.";
		}
	}

	@api
	public static class get_entity_max_speed extends EntityGetterFunction {

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.BadEntityException, ExceptionType.BadEntityTypeException, ExceptionType.CastException};
		}

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {

			MCEntity e = Static.getEntity(Static.getInt32(args[0], t), t);

			if (e instanceof MCBoat) {
				return new CDouble(((MCBoat) e).getMaxSpeed(), t);
			} else if(e instanceof MCMinecart) {
				return new CDouble(((MCMinecart) e).getMaxSpeed(), t);
			}

			throw new ConfigRuntimeException("Given entity must be a boat or minecart.",
					ExceptionType.BadEntityTypeException, t);
		}

		public String getName() {
			return "get_entity_max_speed";
		}

		public String docs() {
			return "double {entityID} Returns a max speed for given entity. Make sure that the entity is a boat"
					+ " or minecart.";
		}
	}

	@api
	public static class set_entity_max_speed extends EntitySetterFunction {

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.BadEntityException, ExceptionType.BadEntityTypeException, ExceptionType.CastException};
		}

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {

			MCEntity e = Static.getEntity(Static.getInt32(args[0], t), t);
			CDouble speed = new CDouble(args[1].val(), t);

			if (e instanceof MCBoat) {
				((MCBoat) e).setMaxSpeed(speed.getDouble());
			} else if(e instanceof MCMinecart) {
				((MCMinecart) e).setMaxSpeed(speed.getDouble());
			} else {
				throw new ConfigRuntimeException("Given entity must be a boat or minecart.",
						ExceptionType.BadEntityTypeException, t);
			}

			return new CVoid(t);
		}

		public String getName() {
			return "set_entity_max_speed";
		}

		public String docs() {
			return "void {entityID} Sets a max speed for given entity. Make sure that the entity is a boat"
					+ " or minecart.";
		}
	}
	
	@api
	public static class get_equipment_droprates extends EntityGetterFunction {

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCEntityEquipment eq = Static.getLivingEntity(Static.getInt32(args[0], t), t).getEquipment();
			if (eq.getHolder() instanceof MCPlayer) {
				throw new ConfigRuntimeException(getName() + " does not work on players.", ExceptionType.BadEntityException, t);
			}
			CArray ret = new CArray(t);
			for (Map.Entry<MCEquipmentSlot, Float> ent : eq.getAllDropChances().entrySet()) {
				ret.set(ent.getKey().name(), new CDouble(ent.getValue(), t), t);
			}
			return ret;
		}

		public String getName() {
			return "get_equipment_droprates";
		}

		public String docs() {
			return "array {entityID} Returns an associative array of the drop rate for each equipment slot."
					+ " If the rate is 0, the equipment will not drop. If it is 1, it is guaranteed to drop.";
		}
	}
	
	@api
	public static class set_equipment_droprates extends EntitySetterFunction {

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCEntityEquipment ee = Static.getLivingEntity(Static.getInt32(args[0], t), t).getEquipment();
			Map<MCEquipmentSlot, Float> eq = ee.getAllDropChances();
			if (ee.getHolder() instanceof MCPlayer) {
				throw new ConfigRuntimeException(getName() + " does not work on players.", ExceptionType.BadEntityException, t);
			}
			if (args[1] instanceof CNull) {
				for (Map.Entry<MCEquipmentSlot, Float> ent : eq.entrySet()) {
					eq.put(ent.getKey(), 0F);
				}
			} else if (args[1] instanceof CArray) {
				CArray ea = (CArray) args[1];
				for (String key : ea.keySet()) {
					try {
						eq.put(MCEquipmentSlot.valueOf(key.toUpperCase()), Static.getDouble32(ea.get(key, t), t));
					} catch (IllegalArgumentException iae) {
						throw new Exceptions.FormatException("Not an equipment slot: " + key, t);
					}
				}
			} else {
				throw new Exceptions.FormatException("Expected argument 2 to be an array", t);
			}
			ee.setAllDropChances(eq);
			return new CVoid(t);
		}

		public String getName() {
			return "set_equipment_droprates";
		}

		public String docs() {
			return "void {entityID, array} Sets the drop chances for each equipment slot on a mob,"
					+ " but does not work on players. Passing null instead of an array will automatically"
					+ " set all rates to 0, which will cause nothing to drop. A rate of 1 will guarantee a drop.";
		}
	}
	
	@api
	public static class get_name_visible extends EntityGetterFunction {

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			return new CBoolean(Static.getLivingEntity(Static.getInt32(args[0], t), t).isCustomNameVisible(), t);
		}

		public String getName() {
			return "get_name_visible";
		}

		public String docs() {
			return "boolean {entityID} Returns whether or not a mob's custom name is always visible."
					+ " If this is true it will be as visible as player names, otherwise it will only be"
					+ " visible when near the mob.";
		}
	}
	
	@api
	public static class set_name_visible extends EntitySetterFunction {

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			Static.getLivingEntity(Static.getInt32(args[0], t), t).setCustomNameVisible(Static.getBoolean(args[1]));
			return new CVoid(t);
		}

		public String getName() {
			return "set_name_visible";
		}

		public String docs() {
			return "void {entityID, boolean} Sets the visibility of a mob's custom name."
					+ " True means it will be visible from a distance, like a playername."
					+ " False means it will only be visible when near the mob.";
		}
	}
	
	@api
	public static class can_pickup_items extends EntityGetterFunction {

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			return new CBoolean(Static.getLivingEntity(Static.getInt32(args[0], t), t).getCanPickupItems(), t);
		}

		public String getName() {
			return "can_pickup_items";
		}

		public String docs() {
			return "boolean {entityID} Returns whether the specified living entity can pick up items.";
		}
	}
	
	@api
	public static class set_can_pickup_items extends EntitySetterFunction {

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			Static.getLivingEntity(Static.getInt32(args[0], t), t).setCanPickupItems(Static.getBoolean(args[1]));
			return new CVoid(t);
		}

		public String getName() {
			return "set_can_pickup_items";
		}

		public String docs() {
			return "void {entityID, boolean} Sets a living entity's ability to pick up items.";
		}
	}
	
	@api
	public static class get_entity_persistence extends EntityGetterFunction {

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			return new CBoolean(!Static.getLivingEntity(Static.getInt32(args[0], t), t).getRemoveWhenFarAway(), t);
		}

		public String getName() {
			return "get_entity_persistence";
		}

		public String docs() {
			return "boolean {entityID} Returns whether the specified living entity will despawn. True means it will not.";
		}
	}
	
	@api
	public static class set_entity_persistence extends EntitySetterFunction {

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			Static.getLivingEntity(Static.getInt32(args[0], t), t).setRemoveWhenFarAway(!Static.getBoolean(args[1]));
			return new CVoid(t);
		}

		public String getName() {
			return "set_entity_persistence";
		}

		public String docs() {
			return "void {entityID, boolean} Sets whether a living entity will despawn. True means it will not.";
		}
	}
	
	@api public static class get_art_at extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.BadEntityException, ExceptionType.FormatException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

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
			throw new ConfigRuntimeException("There is no painting at the specified location", ExceptionType.BadEntityException, t);
		}

		public String getName() {
			return "get_art_at";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "string {locationArray} Gets the specified art at the given location. If the item"
					+ " at the specified location isn't a painting, an ----"
					+ " Will be one of the following: " + StringUtils.Join(MCArt.values(), ", ") + ".";
		}

		public Version since() {
			return CHVersion.V3_3_1;
		}
		
	}
	
	@api public static class set_art_at extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.FormatException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

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
				throw new ConfigRuntimeException("Invalid type: " + args[1].val(), ExceptionType.FormatException, t);
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
				p = (MCPainting)loc.getWorld().spawn(loc, MCEntityType.PAINTING);
			}
			boolean worked = p.setArt(art);
			if(!worked){
				p.remove();
			}
			return new CBoolean(worked, t);
		}

		public String getName() {
			return "set_art_at";
		}

		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		public String docs() {
			return "boolean {locationArray, art} Sets the art at the specified location. If the art"
					+ " doesn't fit, nothing happens, and false is returned. Otherwise, true is returned."
					+ " ---- Art may be one of the following: " + StringUtils.Join(MCArt.values(), ", ");
		}

		public Version since() {
			return CHVersion.V3_3_1;
		}
	}
	
	@api
	public static class get_leashholder extends EntityGetterFunction {

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCLivingEntity le = Static.getLivingEntity(Static.getInt32(args[0], t), t);
			if (!le.isLeashed()) {
				return new CNull(t);
			}
			return new CInt(le.getLeashHolder().getEntityId(), t);
		}

		public String getName() {
			return "get_leashholder";
		}

		public String docs() {
			return "int {entityID} Returns the entityID of the entity that is holding the given living entity's leash,"
					+ " or null if it isn't being held.";
		}
	}
	
	@api
	public static class set_leashholder extends EntitySetterFunction {

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCLivingEntity le = Static.getLivingEntity(Static.getInt32(args[0], t), t);
			MCEntity holder;
			if (args[1] instanceof CNull) {
				holder = null;
			} else {
				holder = Static.getEntity(Static.getInt32(args[1], t), t);
			}
			le.setLeashHolder(holder);
			return new CVoid(t);
		}

		public String getName() {
			return "set_leashholder";
		}

		public String docs() {
			return "void {entityID, entityID} The first entity is the entity to be held on a leash, and must be living."
					+ " The second entity is the holder of the leash. This does not have to be living,"
					+ " but the only non-living entity that will persist as a holder across restarts is the leash hitch."
					+ " Bats, enderdragons, players, and withers can not be held by leashes due to minecraft limitations.";
		}
	}
	
	@api
	public static class entity_grounded extends EntityGetterFunction {

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCEntity e = Static.getEntity(Static.getInt32(args[0], t), t);
			
			return new CBoolean(e.isOnGround(), t);
		}

		public String getName() {
			return "entity_grounded";
		}

		public String docs() {
			return "boolean {entityID} returns whether the entity is touching the ground";
		}
	}

	@api
	public static class entity_air extends EntityGetterFunction {

		public String getName() {
			return "entity_air";
		}

		public String docs() {
			return "int {entityID} Returns the amount of air the specified living entity has remaining.";
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			return new CInt(Static.getLivingEntity(Static.getInt32(args[0], t), t).getRemainingAir(), t);
		}
	}

	@api
	public static class set_entity_air extends EntitySetterFunction {

		public String getName() {
			return "set_entity_air";
		}

		public String docs() {
			return "void {entityID, int} Sets the amount of air the specified living entity has remaining.";
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			Static.getLivingEntity(Static.getInt32(args[0], t), t).setRemainingAir(Static.getInt32(args[1], t));
			return new CVoid(t);
		}
	}

	@api
	public static class entity_max_air extends EntityGetterFunction {

		public String getName() {
			return "entity_max_air";
		}

		public String docs() {
			return "int {entityID} Returns the maximum amount of air the specified living entity can have.";
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			return new CInt(Static.getLivingEntity(Static.getInt32(args[0], t), t).getMaximumAir(), t);
		}
	}

	@api
	public static class set_entity_max_air extends EntitySetterFunction {

		public String getName() {
			return "set_entity_max_air";
		}

		public String docs() {
			return "void {entityID, int} Sets the maximum amount of air the specified living entity can have.";
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			Static.getLivingEntity(Static.getInt32(args[0], t), t).setMaximumAir(Static.getInt32(args[1], t));
			return new CVoid(t);
		}
	}

	@api
	public static class entity_line_of_sight extends EntityFunction {

		public String getName() {
			return "entity_line_of_sight";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3};
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.BadEntityException};
		}

		public String docs() {
			return "array {entityID, [transparents, [maxDistance]]} Returns an array containg all blocks along the living entity's line of sight."
					+ " transparents is an array of block IDs, only air by default."
					+ " maxDistance represent the maximum distance to scan, it may be limited by the server by at least 100 blocks, no less.";
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCLivingEntity entity = Static.getLivingEntity(Static.getInt32(args[0], t), t);
			HashSet<Byte> transparents = null;
			int maxDistance = 512;
			if (args.length >= 2) {
				CArray givenTransparents = Static.getArray(args[1], t);
				if (givenTransparents.inAssociativeMode()) {
					throw new ConfigRuntimeException("The array must not be associative.", ExceptionType.CastException, t);
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
				lineOfSight.push(ObjectGenerator.GetGenerator().location(block.getLocation(), false));
			}
			return lineOfSight;
		}
	}

	@api
	public static class entity_can_see extends EntityFunction {

		public String getName() {
			return "entity_can_see";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.BadEntityException};
		}

		public String docs() {
			return "boolean {entityID, otherEntityID} Returns if the entity can have the other entity in his line of sight."
					+ " For instance for players this mean that it can have the other entity on its screen and that this one is not hidden by opaque blocks."
					+ " This uses the same algorithm that hostile mobs use to find the closest player.";
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			return new CBoolean(Static.getLivingEntity(Static.getInt32(args[0], t), t).hasLineOfSight(Static.getEntity(Static.getInt32(args[1], t), t)), t);
		}
	}
	
	@api
	public static class entity_uuid extends EntityGetterFunction {

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCEntity entity = Static.getEntity(Static.getInt32(args[0], t), t);
			return new CString(entity.getUniqueId().toString(), t);
		}

		public String getName() {
			return "entity_uuid";
		}

		public String docs() {
			return "string {entityID} returns the persistent unique id of the entity";
		}
		
	}
}
