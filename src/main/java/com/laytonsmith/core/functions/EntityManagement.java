package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.StringUtils;
import com.laytonsmith.abstraction.*;
import com.laytonsmith.abstraction.enums.MCProjectileType;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
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
			MCEntity ent = Static.getEntity(id, t);
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
			MCEntity ent = Static.getEntity(id, t);
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
			ent = Static.getEntity(id, t);
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
