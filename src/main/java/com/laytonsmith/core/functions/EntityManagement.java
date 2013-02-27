package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.StringUtils;
import com.laytonsmith.abstraction.*;
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
import java.util.List;
import java.util.Map;

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
	public static class entity_loc extends EntityFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.BadEntityException, ExceptionType.CastException};
		}

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCEntity e = Static.getEntity(Static.getInt32(args[0], t), t);
			return ObjectGenerator.GetGenerator().location(e.getLocation());
		}

		public String getName() {
			return "entity_loc";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
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
	public static class set_entity_loc extends EntityFunction {

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

		public Integer[] numArgs() {
			return new Integer[]{2};
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
	public static class entity_velocity extends EntityFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.BadEntityException, ExceptionType.CastException};
		}

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {

			MCEntity e = Static.getEntity(Static.getInt32(args[0], t), t);
			CArray va = ObjectGenerator.GetGenerator().velocity(e.getVelocity(), t);
			return va;
		}

		public String getName() {
			return "entity_velocity";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
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
	public static class set_entity_velocity extends EntityFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.FormatException, ExceptionType.CastException,
					ExceptionType.BadEntityException};
		}

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {

			MCEntity e = Static.getEntity(Static.getInt32(args[0], t), t);
			e.setVelocity(ObjectGenerator.GetGenerator().velocity(args[1], t));
			return new CVoid(t);
		}

		public String getName() {
			return "set_entity_velocity";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
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
	public static class entity_remove extends EntityFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.BadEntityException, ExceptionType.CastException};
		}

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

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "void {entityID} Removes the specified entity from the world, without any drops or animations. "
				+ "Note: you can't remove players. As a safety measure for working with NPC plugins, it will "
				+ "not work on anything human, even if it is not a player.";
		}

	}

	@api
	public static class entity_type extends EntityFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.BadEntityException, ExceptionType.CastException};
		}

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCEntity ent = Static.getEntity((int) Static.getInt(args[0], t), t);
			if (ent == null) {
				return new CNull(t);
			} else {
				return new CString(ent.getType().name(), t);
			}
		}

		public String getName() {
			return "entity_type";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "string {entityID} Returns the EntityType of the entity with the specified ID.";
		}
	}

	@api
	public static class get_mob_age extends EntityFunction {

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

		public Integer[] numArgs() {
			return new Integer[]{1};
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

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_mob_effect extends EntityFunction {

		public String getName() {
			return "set_mob_effect";
		}

		public Integer[] numArgs() {
			return new Integer[]{3, 4, 5};
		}

		public String docs() {
			return "boolean {entityId, potionID, strength, [seconds], [ambient]} Effect is 1-19. Seconds defaults to 30."
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

		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			int id = (int) Static.getInt(args[0], t);
			MCEntity ent = Static.getEntity(id, t);

			if (ent == null) {
				return new CNull(t);
			} else if (ent instanceof MCLivingEntity) {

				MCLivingEntity mob = ((MCLivingEntity) ent);

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
			} else {
				throw new ConfigRuntimeException("Entity (" + id + ") is not living", ExceptionType.BadEntityException, t);
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

			CArray entities = new CArray(t);

			for (int chX = 0 - chunkRadius; chX <= chunkRadius; chX++) {
				for (int chZ = 0 - chunkRadius; chZ <= chunkRadius; chZ++) {
					int x = (int) loc.getX();
					int y = (int) loc.getY();
					int z = (int) loc.getZ();
					for (MCEntity e : ObjectGenerator.GetGenerator()
							.location(
								new CArray(t,
										new CInt(x + (chX * 16), t),
										new CInt(y, t),
										new CInt(z + (chZ * 16), t),
										new CString(loc.getWorld().getName(), t)
								), loc.getWorld(), t
							).getChunk().getEntities()) {

						if (e.getLocation().distance(loc) <= dist && e.getLocation().getBlock() != loc.getBlock()) {
							if (types.isEmpty() || types.contains(e.getType().name())) {
								entities.push(new CInt(e.getEntityId(), t));
							}
						}
					}
				}
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
	public static class get_mob_target extends EntityFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.BadEntityException};
		}

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

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "entityID {entityID} Gets the mob's target if it has one, and returns the target's entityID."
					+ " If there is no target, null is returned instead.";
		}
	}
	
	//@api
	public static class set_mob_target extends EntityFunction {

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

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public String docs() {
			return "void {entityID, entityID} The first ID is the entity who is targetting, the second is the target."
					+ " It can also be set to null to clear the current target.";
		}
	}
	
	@api
	public static class get_mob_equipment extends EntityFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.BadEntityException};
		}

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

		public Integer[] numArgs() {
			return new Integer[]{1};
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
	public static class set_mob_equipment extends EntityFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.BadEntityException,
					ExceptionType.FormatException};
		}

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

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public String docs() {
			return "void {entityID, array} Takes an associative array with keys representing equipment slots and values"
					+ " of itemArrays, the same used by set_pinv. The equipment slots are: " 
					+ StringUtils.Join(MCEquipmentSlot.values(), ", ", ", or ", " or ");
		}
	}
}
