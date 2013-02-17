package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.StringUtils;
import com.laytonsmith.abstraction.*;
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

/**
 *
 * @author jb_aero
 */
public class EntityManagement {
	public static String docs(){
        return "This class of functions allow entities to be managed.";
    }

	@api
	public static class all_entities extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.InvalidWorldException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
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
				MCWorld w = Static.getServer().getWorld(args[0].val());
				if (args.length == 3) {
					int x = Static.getInt32(args[1], t);
					int z = Static.getInt32(args[2], t);
					for (MCEntity e : w.getChunkAt(x, z).getEntities()) {
						ret.push(new CInt(e.getEntityId(), t));
					}
				} else {
					for (MCEntity e : w.getEntities()) {
						ret.push(new CInt(e.getEntityId(), t));
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
			return "array {[world, [x, z]]} Returns an array of IDs for all entities in the given scope. With no args,"
					+ " this will return all entities loaded on the entire server. If the first argument is given, only"
					+ " entities in that world will be returned, and if all 3 arguments are given, only entities in the"
					+ " chunk with those coords will be returned."; 
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
					new ExampleScript("Basic use", "msg(all_entities(pworld()))", 
							"Sends you an array of all entities in your world."),
					new ExampleScript("Basic use", "msg(all_entities(pworld(), ploc()[0], ploc()[2]))", 
							"Sends you an array of all entities in the same chunk as you.")
			};
		}
		
	}
	
	@api
	public static class entity_loc extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.BadEntityException, ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
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

		public CHVersion since() {
			return CHVersion.V3_3_1;
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
	public static class set_entity_loc extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.BadEntityException, ExceptionType.FormatException,
					ExceptionType.CastException, ExceptionType.InvalidWorldException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
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

		public CHVersion since() {
			return CHVersion.V3_3_1;
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
	public static class entity_velocity extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.BadEntityException, ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
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

		public CHVersion since() {
			return CHVersion.V3_3_1;
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
	public static class set_entity_velocity extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.FormatException, ExceptionType.CastException, 
					ExceptionType.BadEntityException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
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

		public CHVersion since() {
			return CHVersion.V3_3_1;
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
	public static class entity_remove extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.BadEntityException, ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCEntity ent = Static.getEntity((int) Static.getInt(args[0], t), t);
			if (ent == null) {
				return new CVoid(t);
			} else if (ent instanceof MCHumanEntity) {
				throw new ConfigRuntimeException("Cannot remove human entity (" + ent.getEntityId() + ")!", ExceptionType.BadEntityException, t);
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

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

	}

	@api
	public static class entity_type extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.BadEntityException, ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return false;
		}

		public Boolean runAsync() {
			return false;
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

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

	}

	@api
	public static class get_mob_age extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.UnageableMobException, ExceptionType.CastException, ExceptionType.BadEntityException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			int id = Static.getInt32(args[0], t);
			MCEntity ent = Static.getLivingEntity(id, t);
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

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

	}

	@api
	public static class set_mob_age extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.UnageableMobException, ExceptionType.CastException, ExceptionType.BadEntityException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			int id = Static.getInt32(args[0], t);
			int age = Static.getInt32(args[1], t);
			boolean lock = false;
			if (args.length == 3) {
				lock = (boolean) Static.getBoolean(args[2]);
			}
			MCEntity ent = Static.getLivingEntity(id, t);
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
					+ " if lockAge is true, but by default it will not. Throws a UnageableMobException if the mob does not age naturally.";
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_mob_effect extends AbstractFunction {

		public String getName() {
			return "set_mob_effect";
		}

		public Integer[] numArgs() {
			return new Integer[]{3, 4};
		}

		public String docs() {
			return "boolean {mobId, potionID, strength, [seconds]} Not all potions work of course, but effect is 1-19. Seconds defaults to 30."
					+ " If the potionID is out of range, a RangeException is thrown, because out of range potion effects"
					+ " cause the client to crash, fairly hardcore. See http://www.minecraftwiki.net/wiki/Potion_effects for a"
					+ " complete list of potions that can be added. To remove an effect, set the strength (or duration) to 0."
					+ " It returns true if the effect was added or removed as desired. It returns false if the effect was"
					+ " not added or removed as desired (however, this currently only will happen if an effect is attempted"
					+ " to be removed, yet isn't already on the mob).";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.FormatException, ExceptionType.BadEntityException,
                        ExceptionType.RangeException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		public Boolean runAsync() {
			return false;
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
				if (args.length == 4) {
					seconds = Static.getInt32(args[3], t);
				}

				if (seconds == 0 || strength == 0) {
					return new CBoolean(mob.removeEffect(effect), t);
				} else {
					mob.addEffect(effect, strength, seconds, t);
					return new CBoolean(true, t);
				}
			} else {
				throw new ConfigRuntimeException("Entity (" + id + ") is not living", ExceptionType.BadEntityException, t);
			}
		}
	}

	@api(environments={CommandHelperEnvironment.class})
	public static class shoot_projectile extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.BadEntityException, ExceptionType.FormatException, ExceptionType.PlayerOfflineException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCCommandSender cmdsr = environment.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCEntity ent = null;
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
			if (ent instanceof MCLivingEntity) {
				MCProjectile shot = ((MCLivingEntity) ent).launchProjectile(toShoot);
				return new CInt(shot.getEntityId(), t);
			} else {
				throw new ConfigRuntimeException("Entity (" + id + ") is not living", ExceptionType.BadEntityException, t);
			}
		}

		public String getName() {
			return "shoot_projectile";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1, 2};
		}

		public String docs() {
			return "int {[player[, projectile]] | [entityID[, projectile]]} shoots a projectile from the entity or player "
					+ "specified, or the current player if no arguments are passed. If no projectile is specified, "
					+ "it defaults to a fireball. Returns the EntityID of the projectile. Valid projectiles: "
					+ StringUtils.Join(MCProjectileType.values(), ", ", ", or ", " or ");
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

	}

}
