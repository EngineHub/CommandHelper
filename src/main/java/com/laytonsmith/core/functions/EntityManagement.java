package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Vector3D;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.MCAgeable;
import com.laytonsmith.abstraction.MCArmorStand;
import com.laytonsmith.abstraction.MCBlockCommandSender;
import com.laytonsmith.abstraction.MCChunk;
import com.laytonsmith.abstraction.MCColor;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCEnderCrystal;
import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCExperienceOrb;
import com.laytonsmith.abstraction.MCFireball;
import com.laytonsmith.abstraction.MCFireworkEffect;
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
import com.laytonsmith.abstraction.MCTameable;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCBlockFace;
import com.laytonsmith.abstraction.blocks.MCBlockProjectileSource;
import com.laytonsmith.abstraction.entities.*;
import com.laytonsmith.abstraction.entities.MCHorse.MCHorseColor;
import com.laytonsmith.abstraction.entities.MCHorse.MCHorsePattern;
import com.laytonsmith.abstraction.entities.MCHorse.MCHorseVariant;
import com.laytonsmith.abstraction.entities.MCLlama.MCLlamaColor;
import com.laytonsmith.abstraction.enums.MCArt;
import com.laytonsmith.abstraction.enums.MCBodyPart;
import com.laytonsmith.abstraction.enums.MCDyeColor;
import com.laytonsmith.abstraction.enums.MCEnderDragonPhase;
import com.laytonsmith.abstraction.enums.MCEntityEffect;
import com.laytonsmith.abstraction.enums.MCEntityType;
import com.laytonsmith.abstraction.enums.MCFireworkType;
import com.laytonsmith.abstraction.enums.MCOcelotType;
import com.laytonsmith.abstraction.enums.MCParrotType;
import com.laytonsmith.abstraction.enums.MCParticle;
import com.laytonsmith.abstraction.enums.MCProfession;
import com.laytonsmith.abstraction.enums.MCProjectileType;
import com.laytonsmith.abstraction.enums.MCRabbitType;
import com.laytonsmith.abstraction.enums.MCRotation;
import com.laytonsmith.abstraction.enums.MCSkeletonType;
import com.laytonsmith.abstraction.enums.MCVersion;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.hide;
import com.laytonsmith.annotations.seealso;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.CHLog;
import com.laytonsmith.core.CHVersion;
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
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jb_aero
 */
public class EntityManagement {

    public static String docs() {
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
    public static class is_tameable extends AbstractFunction {

	@Override
	public String getName() {
	    return "is_tameable";
	}

	@Override
	public Integer[] numArgs() {
	    return new Integer[]{1};
	}

	@Override
	public String docs() {
	    return "boolean {entityID} Returns true or false if the specified entity is tameable";
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
	    MCEntity e = Static.getEntity(args[0], t);
	    boolean ret;
	    if (e == null) {
		ret = false;
	    } else if (e instanceof MCTameable) {
		ret = true;
	    } else {
		ret = false;
	    }
	    return CBoolean.get(ret);
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

	    if (ent instanceof MCAgeable) {
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

	    if (ent instanceof MCAgeable) {
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

    @api(environments = {CommandHelperEnvironment.class})
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
		    MCLocation nl = StaticLayer.GetLocation(loc.getWorld(), loc.getX() + (chX * 16), loc.getY(), loc.getZ() + (chZ * 16));
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

    @api
    public static class entity_onfire extends EntityGetterFunction {

	@Override
	public Construct exec(Target t, Environment environment,
		Construct... args) throws ConfigRuntimeException {
	    MCEntity ent = Static.getEntity(args[0], t);
	    return new CInt(ent.getFireTicks() / 20, t);
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
	    int seconds = Static.getInt32(args[1], t);
	    if (seconds < 0) {
		throw new CRERangeException("Seconds cannot be less than 0", t);
	    } else if (seconds > Integer.MAX_VALUE / 20) {
		throw new CRERangeException("Seconds cannot be greater than 107374182", t);
	    }
	    ent.setFireTicks(seconds * 20);
	    return CVoid.VOID;
	}

	@Override
	public String getName() {
	    return "set_entity_onfire";
	}

	@Override
	public String docs() {
	    return "void {entityID, seconds} Sets the entity on fire for the"
		    + " given number of seconds. Throws a RangeException"
		    + " if seconds is less than 0 or greater than 107374182.";
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
		} else if (cs instanceof MCBlockCommandSender) {
		    l = ((MCBlockCommandSender) cs).getBlock().getRelative(MCBlockFace.UP).getLocation();
		} else if (cs instanceof MCCommandMinecart) {
		    l = ((MCCommandMinecart) cs).getLocation().add(0, 1, 0); // One block above the minecart.
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
			ent = l.getWorld().dropItem(l, StaticLayer.GetItemStack(1, qty));
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
			} catch (NullPointerException | IllegalArgumentException ex) {
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
		try {
		    success = horse.setPassenger(rider);
		} catch (IllegalStateException ex) {
		    throw new CREFormatException("Circular entity riding!"
			    + " One entity is already a passenger of the other.", t);
		}
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
	    } else if (e instanceof MCMinecart) {
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
	    } else if (e instanceof MCMinecart) {
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
    public static class get_art_at extends AbstractFunction {

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
	    if (environment.getEnv(CommandHelperEnvironment.class).GetPlayer() != null) {
		w = environment.getEnv(CommandHelperEnvironment.class).GetPlayer().getWorld();
	    }
	    List<MCEntity> es = StaticLayer.GetConvertor().GetEntitiesAt(ObjectGenerator.GetGenerator().location(args[0], w, t), 1);
	    for (MCEntity e : es) {
		if (e instanceof MCPainting) {
		    return new CString(((MCPainting) e).getArt().name(), t);
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

    @api
    public static class set_art_at extends AbstractFunction {

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
	    if (environment.getEnv(CommandHelperEnvironment.class).GetPlayer() != null) {
		w = environment.getEnv(CommandHelperEnvironment.class).GetPlayer().getWorld();
	    }
	    MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], w, t);
	    MCArt art;
	    try {
		art = MCArt.valueOf(args[1].val());
	    } catch (IllegalArgumentException e) {
		throw new CREFormatException("Invalid type: " + args[1].val(), t);
	    }
	    //If there's already a painting there, just use that one. Otherwise, spawn a new one.

	    MCPainting p = null;
	    for (MCEntity e : StaticLayer.GetConvertor().GetEntitiesAt(loc, 1)) {
		if (e instanceof MCPainting) {
		    p = (MCPainting) e;
		    break;
		}
	    }
	    if (p == null) {
		p = (MCPainting) loc.getWorld().spawn(loc, MCEntityType.MCVanillaEntityType.PAINTING);
	    }
	    boolean worked = p.setArt(art);
	    if (!worked) {
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
    @hide("Deprecated.")
    public static class entity_id extends EntityGetterFunction implements Optimizable {

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

	@Override
	public ParseTree optimizeDynamic(Target t, List<ParseTree> children, FileOptions fileOptions) throws ConfigCompileException, ConfigRuntimeException {
	    CHLog.GetLogger().w(CHLog.Tags.DEPRECATION, "The function entity_id() is deprecated.", t);
	    return Optimizable.PULL_ME_UP;
	}

	@Override
	public Set<OptimizationOption> optimizationOptions() {
	    return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC);
	}
    }

    @api
    @hide("Deprecated.")
    public static class entity_uuid extends EntityGetterFunction implements Optimizable {

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

	@Override
	public ParseTree optimizeDynamic(Target t, List<ParseTree> children, FileOptions fileOptions) throws ConfigCompileException, ConfigRuntimeException {
	    CHLog.GetLogger().w(CHLog.Tags.DEPRECATION, "The function entity_uuid() is deprecated.", t);
	    return Optimizable.PULL_ME_UP;
	}

	@Override
	public Set<OptimizationOption> optimizationOptions() {
	    return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC);
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
	    docs = docs.replace("%LLAMA_COLOR%", StringUtils.Join(MCLlamaColor.values(), ", ", ", or ", " or "));
	    docs = docs.replace("%ROTATION%", StringUtils.Join(MCRotation.values(), ", ", ", or ", " or "));
	    docs = docs.replace("%OCELOT_TYPE%", StringUtils.Join(MCOcelotType.values(), ", ", ", or ", " or "));
	    docs = docs.replace("%PARROT_TYPE%", StringUtils.Join(MCParrotType.values(), ", ", ", or ", " or "));
	    docs = docs.replace("%ART%", StringUtils.Join(MCArt.values(), ", ", ", or ", " or "));
	    docs = docs.replace("%DYE_COLOR%", StringUtils.Join(MCDyeColor.values(), ", ", ", or ", " or "));
	    docs = docs.replace("%SKELETON_TYPE%", StringUtils.Join(MCSkeletonType.values(), ", ", ", or ", " or "));
	    docs = docs.replace("%PROFESSION%", StringUtils.Join(MCProfession.values(), ", ", ", or ", " or "));
	    docs = docs.replace("%RABBIT_TYPE%", StringUtils.Join(MCRabbitType.values(), ", ", ", or ", " or "));
	    docs = docs.replace("%PARTICLE%", StringUtils.Join(MCParticle.values(), ", ", ", or ", " or "));
	    docs = docs.replace("%ENDERDRAGON_PHASE%", StringUtils.Join(MCEnderDragonPhase.values(), ", ", ", or ", " or "));
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
		    if (cloudSource instanceof MCBlockProjectileSource) {
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
		    if (marker != null) { // unsupported before 1.8.7
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
		    specArray.set(entity_spec.KEY_CREEPER_MAXFUSETICKS, new CInt(creeper.getMaxFuseTicks(), t), t);
		    specArray.set(entity_spec.KEY_CREEPER_EXPLOSIONRADIUS, new CInt(creeper.getExplosionRadius(), t), t);
		    break;
		case DONKEY:
		case MULE:
		    MCChestedHorse chestedhorse = (MCChestedHorse) entity;
		    specArray.set(entity_spec.KEY_HORSE_CHEST, CBoolean.get(chestedhorse.hasChest()), t);
		    specArray.set(entity_spec.KEY_HORSE_JUMP, new CDouble(chestedhorse.getJumpStrength(), t), t);
		    specArray.set(entity_spec.KEY_HORSE_DOMESTICATION, new CInt(chestedhorse.getDomestication(), t), t);
		    specArray.set(entity_spec.KEY_HORSE_MAXDOMESTICATION, new CInt(chestedhorse.getMaxDomestication(), t), t);
		    specArray.set(entity_spec.KEY_HORSE_SADDLE, ObjectGenerator.GetGenerator().item(chestedhorse.getSaddle(), t), t);
		    break;
		case DROPPED_ITEM:
		    MCItem item = (MCItem) entity;
		    specArray.set(entity_spec.KEY_DROPPED_ITEM_ITEMSTACK, ObjectGenerator.GetGenerator().item(item.getItemStack(), t), t);
		    specArray.set(entity_spec.KEY_DROPPED_ITEM_PICKUPDELAY, new CInt(item.getPickupDelay(), t), t);
		    break;
		case ENDER_CRYSTAL:
		    MCEnderCrystal endercrystal = (MCEnderCrystal) entity;
		    if (Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_9)) {
			specArray.set(entity_spec.KEY_ENDERCRYSTAL_BASE, CBoolean.get(endercrystal.isShowingBottom()), t);
			MCLocation location = endercrystal.getBeamTarget();
			if (location == null) {
			    specArray.set(entity_spec.KEY_ENDERCRYSTAL_BEAMTARGET, CNull.NULL, t);
			} else {
			    specArray.set(entity_spec.KEY_ENDERCRYSTAL_BEAMTARGET,
				    ObjectGenerator.GetGenerator().location(location, false), t);
			}
		    }
		    break;
		case ENDER_DRAGON:
		    MCEnderDragon enderdragon = (MCEnderDragon) entity;
		    if (Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_9_X)) {
			specArray.set(entity_spec.KEY_ENDERDRAGON_PHASE, new CString(enderdragon.getPhase().name(), t), t);
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
		case HUSK:
		    MCZombie husk = (MCZombie) entity;
		    specArray.set(entity_spec.KEY_ZOMBIE_BABY, CBoolean.get(husk.isBaby()), t);
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
		case LLAMA:
		    MCLlama llama = (MCLlama) entity;
		    specArray.set(entity_spec.KEY_HORSE_COLOR, new CString(llama.getLlamaColor().name(), t), t);
		    specArray.set(entity_spec.KEY_HORSE_CHEST, CBoolean.get(llama.hasChest()), t);
		    specArray.set(entity_spec.KEY_HORSE_DOMESTICATION, new CInt(llama.getDomestication(), t), t);
		    specArray.set(entity_spec.KEY_HORSE_MAXDOMESTICATION, new CInt(llama.getMaxDomestication(), t), t);
		    specArray.set(entity_spec.KEY_HORSE_SADDLE, ObjectGenerator.GetGenerator().item(llama.getSaddle(), t), t);
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
		case PARROT:
		    MCParrot parrot = (MCParrot) entity;
		    specArray.set(entity_spec.KEY_PARROT_SITTING, CBoolean.get(parrot.isSitting()), t);
		    specArray.set(entity_spec.KEY_PARROT_TYPE, new CString(parrot.getVariant().name(), t), t);
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
		case SHULKER_BULLET:
		    MCShulkerBullet bullet = (MCShulkerBullet) entity;
		    MCEntity target = bullet.getTarget();
		    if (target == null) {
			specArray.set(entity_spec.KEY_SHULKERBULLET_TARGET, CNull.NULL, t);
		    } else {
			specArray.set(entity_spec.KEY_SHULKERBULLET_TARGET, new CString(target.getUniqueId().toString(), t), t);
		    }
		    break;
		case SKELETON:
		case STRAY:
		case WITHER_SKELETON:
		    MCSkeleton skeleton = (MCSkeleton) entity;
		    specArray.set(entity_spec.KEY_SKELETON_TYPE, new CString(skeleton.getSkeletonType().name(), t), t);
		    break;
		case SKELETON_HORSE:
		case ZOMBIE_HORSE:
		    MCAbstractHorse undeadhorse = (MCAbstractHorse) entity;
		    specArray.set(entity_spec.KEY_HORSE_JUMP, new CDouble(undeadhorse.getJumpStrength(), t), t);
		    specArray.set(entity_spec.KEY_HORSE_DOMESTICATION, new CInt(undeadhorse.getDomestication(), t), t);
		    specArray.set(entity_spec.KEY_HORSE_MAXDOMESTICATION, new CInt(undeadhorse.getMaxDomestication(), t), t);
		    specArray.set(entity_spec.KEY_HORSE_SADDLE, ObjectGenerator.GetGenerator().item(undeadhorse.getSaddle(), t), t);
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
		case ZOMBIE_VILLAGER:
		    MCZombieVillager zombievillager = (MCZombieVillager) entity;
		    specArray.set(entity_spec.KEY_ZOMBIE_BABY, CBoolean.get(zombievillager.isBaby()), t);
		    specArray.set(entity_spec.KEY_VILLAGER_PROFESSION, new CString(zombievillager.getProfession().name(), t), t);
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
	private static final String KEY_CREEPER_MAXFUSETICKS = "maxfuseticks";
	private static final String KEY_CREEPER_EXPLOSIONRADIUS = "explosionradius";
	private static final String KEY_DROPPED_ITEM_ITEMSTACK = "itemstack";
	private static final String KEY_DROPPED_ITEM_PICKUPDELAY = "pickupdelay";
	private static final String KEY_ENDERCRYSTAL_BASE = "base";
	private static final String KEY_ENDERCRYSTAL_BEAMTARGET = "beamtarget";
	private static final String KEY_ENDERDRAGON_PHASE = "phase";
	private static final String KEY_ENDERMAN_CARRIED = "carried";
	private static final String KEY_EXPERIENCE_ORB_AMOUNT = "amount";
	private static final String KEY_FALLING_BLOCK_BLOCK = "block";
	private static final String KEY_FALLING_BLOCK_DROPITEM = "dropitem";
	private static final String KEY_FIREBALL_DIRECTION = "direction";
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
	private static final String KEY_PARROT_SITTING = "sitting";
	private static final String KEY_PARROT_TYPE = "type";
	private static final String KEY_PIG_SADDLED = "saddled";
	private static final String KEY_PIG_ZOMBIE_ANGRY = "angry";
	private static final String KEY_PIG_ZOMBIE_ANGER = "anger";
	private static final String KEY_RABBIT_TYPE = "type";
	private static final String KEY_PRIMED_TNT_FUSETICKS = "fuseticks";
	private static final String KEY_PRIMED_TNT_SOURCE = "source";
	private static final String KEY_SHEEP_COLOR = "color";
	private static final String KEY_SHEEP_SHEARED = "sheared";
	private static final String KEY_SHULKERBULLET_TARGET = "target";
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
				if (specArray.get(index, t) instanceof CArray) {
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
				} catch (IllegalArgumentException ex) {
				    throw new CREFormatException("Invalid particle type: " + particleName, t);
				}
				break;
			    case entity_spec.KEY_AREAEFFECTCLOUD_POTIONMETA:
				Construct c = specArray.get(index, t);
				if (c instanceof CArray) {
				    CArray meta = (CArray) c;
				    if (meta.containsKey("base")) {
					Construct base = meta.get("base", t);
					if (base instanceof CArray) {
					    MCPotionData pd = ObjectGenerator.GetGenerator().potionData((CArray) base, t);
					    cloud.setBasePotionData(pd);
					}
				    }
				    if (meta.containsKey("potions")) {
					cloud.clearCustomEffects();
					Construct potions = meta.get("potions", t);
					if (potions instanceof CArray) {
					    List<MCLivingEntity.MCEffect> list = ObjectGenerator.GetGenerator().potions((CArray) potions, t);
					    for (MCLivingEntity.MCEffect effect : list) {
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
				if (cloudSource instanceof CNull) {
				    cloud.setSource(null);
				} else if (cloudSource instanceof CArray) {
				    MCBlock b = ObjectGenerator.GetGenerator().location(cloudSource, cloud.getWorld(), t).getBlock();
				    if (b.isDispenser()) {
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
			    case entity_spec.KEY_CREEPER_MAXFUSETICKS:
				try {
				    creeper.setMaxFuseTicks(Static.getInt32(specArray.get(index, t), t));
				} catch (IllegalArgumentException ex) {
				    throw new CRERangeException("Ticks must not be negative.", t);
				}
				break;
			    case entity_spec.KEY_CREEPER_EXPLOSIONRADIUS:
				try {
				    creeper.setExplosionRadius(Static.getInt32(specArray.get(index, t), t));
				} catch (IllegalArgumentException ex) {
				    throw new CRERangeException("Radius must not be negative.", t);
				}
				break;
			    default:
				throwException(index, t);
			}
		    }
		    break;
		case DONKEY:
		case MULE:
		    MCChestedHorse chestedhorse = (MCChestedHorse) entity;
		    for (String index : specArray.stringKeySet()) {
			switch (index.toLowerCase()) {
			    case entity_spec.KEY_HORSE_CHEST:
				chestedhorse.setHasChest(Static.getBoolean(specArray.get(index, t)));
				break;
			    case entity_spec.KEY_HORSE_JUMP:
				try {
				    chestedhorse.setJumpStrength(Static.getDouble(specArray.get(index, t), t));
				} catch (IllegalArgumentException exception) {
				    throw new CRERangeException("The jump strength must be between 0.0 and 2.0", t);
				}
				break;
			    case entity_spec.KEY_HORSE_DOMESTICATION:
				try {
				    chestedhorse.setDomestication(Static.getInt32(specArray.get(index, t), t));
				} catch (IllegalArgumentException exception) {
				    throw new CRERangeException("The domestication level can not be higher than the max domestication level.", t);
				}
				break;
			    case entity_spec.KEY_HORSE_MAXDOMESTICATION:
				chestedhorse.setMaxDomestication(Static.getInt32(specArray.get(index, t), t));
				break;
			    case entity_spec.KEY_HORSE_SADDLE:
				chestedhorse.setSaddle(ObjectGenerator.GetGenerator().item(specArray.get(index, t), t));
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
				if (c instanceof CNull) {
				    endercrystal.setBeamTarget(null);
				} else if (c instanceof CArray) {
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
		case ENDER_DRAGON:
		    MCEnderDragon enderdragon = (MCEnderDragon) entity;
		    for (String index : specArray.stringKeySet()) {
			switch (index.toLowerCase()) {
			    case entity_spec.KEY_ENDERDRAGON_PHASE:
				enderdragon.setPhase(MCEnderDragonPhase.valueOf(specArray.get(index, t).val().toUpperCase()));
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
		case HUSK:
		    MCZombie husk = (MCZombie) entity;
		    for (String index : specArray.stringKeySet()) {
			switch (index.toLowerCase()) {
			    case entity_spec.KEY_ZOMBIE_BABY:
				husk.setBaby(Static.getBoolean(specArray.get(index, t)));
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
		case LLAMA:
		    MCLlama llama = (MCLlama) entity;
		    for (String index : specArray.stringKeySet()) {
			switch (index.toLowerCase()) {
			    case entity_spec.KEY_HORSE_COLOR:
				try {
				    llama.setLlamaColor(MCLlamaColor.valueOf(specArray.get(index, t).val().toUpperCase()));
				} catch (IllegalArgumentException exception) {
				    throw new CREFormatException("Invalid llama color: " + specArray.get(index, t).val(), t);
				}
				break;
			    case entity_spec.KEY_HORSE_CHEST:
				llama.setHasChest(Static.getBoolean(specArray.get(index, t)));
				break;
			    case entity_spec.KEY_HORSE_DOMESTICATION:
				try {
				    llama.setDomestication(Static.getInt32(specArray.get(index, t), t));
				} catch (IllegalArgumentException exception) {
				    throw new CRERangeException("The domestication level can not be higher than the max domestication level.", t);
				}
				break;
			    case entity_spec.KEY_HORSE_MAXDOMESTICATION:
				llama.setMaxDomestication(Static.getInt32(specArray.get(index, t), t));
				break;
			    case entity_spec.KEY_HORSE_SADDLE:
				llama.setSaddle(ObjectGenerator.GetGenerator().item(specArray.get(index, t), t));
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
				if (specArray.get(index, t) instanceof CNull) {
				    commandminecart.setName(null);
				} else {
				    commandminecart.setName(specArray.get(index, t).val());
				}
				break;
			    case entity_spec.KEY_MINECART_COMMAND_COMMAND:
				if (specArray.get(index, t) instanceof CNull) {
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
		case PARROT:
		    MCParrot parrot = (MCParrot) entity;
		    for (String index : specArray.stringKeySet()) {
			switch (index.toLowerCase()) {
			    case entity_spec.KEY_PARROT_SITTING:
				parrot.setSitting(Static.getBoolean(specArray.get(index, t)));
				break;
			    case entity_spec.KEY_PARROT_TYPE:
				try {
				    parrot.setVariant(MCParrotType.valueOf(specArray.get(index, t).val().toUpperCase()));
				} catch (IllegalArgumentException exception) {
				    throw new CREFormatException("Invalid parrot type: " + specArray.get(index, t).val(), t);
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
		case SHULKER_BULLET:
		    MCShulkerBullet bullet = (MCShulkerBullet) entity;
		    for (String index : specArray.stringKeySet()) {
			switch (index.toLowerCase()) {
			    case entity_spec.KEY_SHULKERBULLET_TARGET:
				Construct c = specArray.get(index, t);
				if (c instanceof CNull) {
				    bullet.setTarget(null);
				} else {
				    bullet.setTarget(Static.getEntity(c, t));
				}
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
		case SKELETON_HORSE:
		case ZOMBIE_HORSE:
		    MCAbstractHorse undeadhorse = (MCAbstractHorse) entity;
		    for (String index : specArray.stringKeySet()) {
			switch (index.toLowerCase()) {
			    case entity_spec.KEY_HORSE_JUMP:
				try {
				    undeadhorse.setJumpStrength(Static.getDouble(specArray.get(index, t), t));
				} catch (IllegalArgumentException exception) {
				    throw new CRERangeException("The jump strength must be between 0.0 and 2.0", t);
				}
				break;
			    case entity_spec.KEY_HORSE_DOMESTICATION:
				try {
				    undeadhorse.setDomestication(Static.getInt32(specArray.get(index, t), t));
				} catch (IllegalArgumentException exception) {
				    throw new CRERangeException("The domestication level can not be higher than the max domestication level.", t);
				}
				break;
			    case entity_spec.KEY_HORSE_MAXDOMESTICATION:
				undeadhorse.setMaxDomestication(Static.getInt32(specArray.get(index, t), t));
				break;
			    case entity_spec.KEY_HORSE_SADDLE:
				undeadhorse.setSaddle(ObjectGenerator.GetGenerator().item(specArray.get(index, t), t));
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
				} catch (IllegalArgumentException ex) {
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
				if (c instanceof CArray) {
				    CArray meta = (CArray) c;
				    if (meta.containsKey("base")) {
					Construct base = meta.get("base", t);
					if (base instanceof CArray) {
					    MCPotionData pd = ObjectGenerator.GetGenerator().potionData((CArray) base, t);
					    tippedarrow.setBasePotionData(pd);
					}
				    }
				    if (meta.containsKey("potions")) {
					tippedarrow.clearCustomEffects();
					Construct potions = meta.get("potions", t);
					if (potions instanceof CArray) {
					    List<MCLivingEntity.MCEffect> list = ObjectGenerator.GetGenerator().potions((CArray) potions, t);
					    for (MCLivingEntity.MCEffect effect : list) {
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
		case ZOMBIE_VILLAGER:
		    MCZombieVillager zombievillager = (MCZombieVillager) entity;
		    for (String index : specArray.stringKeySet()) {
			switch (index.toLowerCase()) {
			    case entity_spec.KEY_ZOMBIE_BABY:
				zombievillager.setBaby(Static.getBoolean(specArray.get(index, t)));
				break;
			    case entity_spec.KEY_VILLAGER_PROFESSION:
				try {
				    zombievillager.setProfession(MCProfession.valueOf(specArray.get(index, t).val().toUpperCase()));
				} catch (IllegalArgumentException exception) {
				    throw new CREFormatException("Invalid profession: " + specArray.get(index, t).val(), t);
				}
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
		    if (b.isDispenser()) {
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
	    return "void {entityID, boolean} If true, applies glowing effect to the entity (MC 1.9)";
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
	    return "boolean {entityID} Returns true if the entity is glowing (MC 1.9)";
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
    public static class get_entity_silent extends EntityGetterFunction {

	public String getName() {
	    return "get_entity_silent";
	}

	public String docs() {
	    return "boolean {entityID} Returns true if the entity produces sounds (MC 1.9.4)";
	}

	public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
	    return CBoolean.GenerateCBoolean(Static.getEntity(args[0], t).isSilent(), t);
	}

	public Version since() {
	    return CHVersion.V3_3_2;
	}
    }

    @api
    public static class set_entity_silent extends EntitySetterFunction {

	public String getName() {
	    return "set_entity_silent";
	}

	public String docs() {
	    return "void {entityID, boolean} Sets whether or not entity produces sounds (MC 1.9.4)";
	}

	public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
	    MCEntity e = Static.getEntity(args[0], t);
	    e.setSilent(Static.getBoolean(args[1]));
	    return CVoid.VOID;
	}

	public Version since() {
	    return CHVersion.V3_3_2;
	}
    }

    @api
    public static class get_entity_gravity extends EntityGetterFunction {

	public String getName() {
	    return "get_entity_gravity";
	}

	public String docs() {
	    return "boolean {entityID} Returns true if gravity applies to the entity (MC 1.10)";
	}

	public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
	    return CBoolean.GenerateCBoolean(Static.getEntity(args[0], t).hasGravity(), t);
	}

	public Version since() {
	    return CHVersion.V3_3_2;
	}
    }

    @api
    public static class set_entity_gravity extends EntitySetterFunction {

	public String getName() {
	    return "set_entity_gravity";
	}

	public String docs() {
	    return "void {entityID, boolean} Sets whether or not gravity applies to the entity (MC 1.10)";
	}

	public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
	    MCEntity e = Static.getEntity(args[0], t);
	    e.setHasGravity(Static.getBoolean(args[1]));
	    return CVoid.VOID;
	}

	public Version since() {
	    return CHVersion.V3_3_2;
	}
    }

    @api
    public static class get_entity_invulnerable extends EntityGetterFunction {

	public String getName() {
	    return "get_entity_invulnerable";
	}

	public String docs() {
	    return "boolean {entityID} Returns true if the entity cannot be damaged (MC 1.9.2)";
	}

	public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
	    return CBoolean.GenerateCBoolean(Static.getEntity(args[0], t).isInvulnerable(), t);
	}

	public Version since() {
	    return CHVersion.V3_3_2;
	}
    }

    @api
    public static class set_entity_invulnerable extends EntitySetterFunction {

	public String getName() {
	    return "set_entity_invulnerable";
	}

	public String docs() {
	    return "void {entityID, boolean} If set to true the entity cannot be damaged, except by players in"
		    + " creative mode (MC 1.9.2)";
	}

	public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
	    MCEntity e = Static.getEntity(args[0], t);
	    e.setInvulnerable(Static.getBoolean(args[1]));
	    return CVoid.VOID;
	}

	public Version since() {
	    return CHVersion.V3_3_2;
	}
    }

    @api
    public static class get_scoreboard_tags extends EntityGetterFunction {

	public String getName() {
	    return "get_scoreboard_tags";
	}

	public String docs() {
	    return "array {entityID} Returns an array of scoreboard tags for this entity. (MC 1.10.2)";
	}

	public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
	    MCEntity e = Static.getEntity(args[0], t);
	    CArray tags = new CArray(t);
	    for (String tag : e.getScoreboardTags()) {
		tags.push(new CString(tag, t), t);
	    }
	    return tags;
	}

	public Version since() {
	    return CHVersion.V3_3_2;
	}
    }

    @api
    public static class add_scoreboard_tag extends EntitySetterFunction {

	public String getName() {
	    return "add_scoreboard_tag";
	}

	public String docs() {
	    return "boolean {entityID, tag} Adds a tag to the entity. Returns whether or not it was successful. (MC 1.10.2)";
	}

	public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
	    MCEntity e = Static.getEntity(args[0], t);
	    return CBoolean.get(e.addScoreboardTag(args[1].val()));
	}

	public Version since() {
	    return CHVersion.V3_3_2;
	}
    }

    @api
    public static class remove_scoreboard_tag extends EntitySetterFunction {

	public String getName() {
	    return "remove_scoreboard_tag";
	}

	public String docs() {
	    return "boolean {entityID, tag} Removes a tag from the entity. Returns whether or not it was successful. (MC 1.10.2)";
	}

	public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
	    MCEntity e = Static.getEntity(args[0], t);
	    return CBoolean.get(e.removeScoreboardTag(args[1].val()));
	}

	public Version since() {
	    return CHVersion.V3_3_2;
	}
    }

    @api(environments = {CommandHelperEnvironment.class})
    public static class drop_item extends AbstractFunction {

	@Override
	public String getName() {
	    return "drop_item";
	}

	@Override
	public Integer[] numArgs() {
	    return new Integer[]{1, 2, 3};
	}

	@Override
	public String docs() {
	    return "int {[player/LocationArray], itemArray, [spawnNaturally]} Drops the specified item stack at the"
		    + " specified player's feet (or at an arbitrary Location, if an array is given), and returns its"
		    + " entity id. spawnNaturally takes a boolean, which forces the way the item will be spawned. If"
		    + " true, the item will be dropped with a random velocity.";
	}

	@Override
	public Class<? extends CREThrowable>[] thrown() {
	    return new Class[]{CRECastException.class, CREFormatException.class, CREPlayerOfflineException.class, CREInvalidWorldException.class};
	}

	@Override
	public boolean isRestricted() {
	    return true;
	}

	@Override
	public CHVersion since() {
	    return CHVersion.V3_2_0;
	}

	@Override
	public Boolean runAsync() {
	    return false;
	}

	@Override
	public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
	    MCLocation l;
	    MCItemStack is;
	    boolean natural;
	    if (args.length == 1) {
		if (env.getEnv(CommandHelperEnvironment.class).GetPlayer() != null) {
		    l = env.getEnv(CommandHelperEnvironment.class).GetPlayer().getEyeLocation();
		    natural = false;
		} else {
		    throw new CREPlayerOfflineException("Invalid sender!", t);
		}
		is = ObjectGenerator.GetGenerator().item(args[0], t);
	    } else {
		MCPlayer p;
		if (args[0] instanceof CArray) {
		    p = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
		    l = ObjectGenerator.GetGenerator().location(args[0], (p != null ? p.getWorld() : null), t);
		    natural = true;
		} else {
		    p = Static.GetPlayer(args[0].val(), t);
		    l = p.getEyeLocation();
		    natural = false;
		}
		is = ObjectGenerator.GetGenerator().item(args[1], t);
	    }
	    if (is.getTypeId() == 0) {
		// can't drop air
		return CNull.NULL;
	    }
	    if (args.length == 3) {
		natural = Static.getBoolean(args[2]);
	    }
	    MCItem item;
	    if (natural) {
		item = l.getWorld().dropItemNaturally(l, is);
	    } else {
		item = l.getWorld().dropItem(l, is);
	    }
	    return new CString(item.getUniqueId().toString(), t);
	}
    }

    @api(environments = {CommandHelperEnvironment.class})
    public static class launch_firework extends AbstractFunction {

	@Override
	public Class<? extends CREThrowable>[] thrown() {
	    return new Class[]{CREFormatException.class, CRERangeException.class, CREInvalidWorldException.class};
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
	    MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
	    MCWorld w = null;
	    if (p != null) {
		w = p.getWorld();
	    }
	    MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], w, t);
	    CArray options;
	    if (args.length == 2) {
		options = Static.getArray(args[1], t);
	    } else {
		options = CArray.GetAssociativeArray(t);
	    }

	    int strength = 2;
	    if (options.containsKey("strength")) {
		strength = Static.getInt32(options.get("strength", t), t);
		if (strength < 0 || strength > 128) {
		    throw new CRERangeException("Strength must be between 0 and 128", t);
		}
	    }

	    List<MCFireworkEffect> effects = new ArrayList<>();
	    if (options.containsKey("effects")) {
		Construct cEffects = options.get("effects", t);
		if (cEffects instanceof CArray) {
		    for (Construct c : ((CArray) cEffects).asList()) {
			effects.add(ObjectGenerator.GetGenerator().fireworkEffect((CArray) c, t));
		    }
		} else {
		    throw new CREFormatException("Firework effects must be an array.", t);
		}
	    } else {
		effects.add(ObjectGenerator.GetGenerator().fireworkEffect(options, t));
	    }

	    MCFirework firework = loc.getWorld().launchFirework(loc, strength, effects);
	    return new CString(firework.getUniqueId().toString(), t);
	}

	@Override
	public String getName() {
	    return "launch_firework";
	}

	@Override
	public Integer[] numArgs() {
	    return new Integer[]{1, 2};
	}

	@Override
	public String docs() {
	    Class c;
	    try {
		//Since MCColor actually depends on a bukkit server, we don't want to require that for
		//the sake of documentation, so we'll build the color list much more carefully.
		//Note the false, so we don't actually initialize the class.
		c = Class.forName(MCColor.class.getName(), false, this.getClass().getClassLoader());
	    } catch (ClassNotFoundException ex) {
		//Hrm...
		Logger.getLogger(Minecraft.class.getName()).log(Level.SEVERE, null, ex);
		return "";
	    }
	    List<String> names = new ArrayList<String>();
	    for (Field f : c.getFields()) {
		if (f.getType() == MCColor.class) {
		    names.add(f.getName());
		}
	    }
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
		    + "The \"named colors\" can be one of: " + StringUtils.Join(names, ", ", " or ");
	}

	@Override
	public CHVersion since() {
	    return CHVersion.V3_3_1;
	}

    }

}
