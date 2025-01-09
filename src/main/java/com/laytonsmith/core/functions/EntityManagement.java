package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Vector3D;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.MCBlockCommandSender;
import com.laytonsmith.abstraction.MCChunk;
import com.laytonsmith.abstraction.MCColor;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCFireworkEffect;
import com.laytonsmith.abstraction.MCFireworkMeta;
import com.laytonsmith.abstraction.MCHumanEntity;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCPotionData;
import com.laytonsmith.abstraction.MCProjectileSource;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCBlockData;
import com.laytonsmith.abstraction.blocks.MCBlockFace;
import com.laytonsmith.abstraction.blocks.MCBlockProjectileSource;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.entities.MCAbstractHorse;
import com.laytonsmith.abstraction.entities.MCAreaEffectCloud;
import com.laytonsmith.abstraction.entities.MCArmorStand;
import com.laytonsmith.abstraction.entities.MCArrow;
import com.laytonsmith.abstraction.entities.MCAxolotl;
import com.laytonsmith.abstraction.entities.MCBee;
import com.laytonsmith.abstraction.entities.MCBlockDisplay;
import com.laytonsmith.abstraction.entities.MCBoat;
import com.laytonsmith.abstraction.entities.MCCat;
import com.laytonsmith.abstraction.entities.MCChestedHorse;
import com.laytonsmith.abstraction.entities.MCCommandMinecart;
import com.laytonsmith.abstraction.entities.MCCreeper;
import com.laytonsmith.abstraction.entities.MCDisplay;
import com.laytonsmith.abstraction.entities.MCEnderCrystal;
import com.laytonsmith.abstraction.entities.MCEnderDragon;
import com.laytonsmith.abstraction.entities.MCEnderSignal;
import com.laytonsmith.abstraction.entities.MCEnderman;
import com.laytonsmith.abstraction.entities.MCEvokerFangs;
import com.laytonsmith.abstraction.entities.MCExperienceOrb;
import com.laytonsmith.abstraction.entities.MCFallingBlock;
import com.laytonsmith.abstraction.entities.MCFireball;
import com.laytonsmith.abstraction.entities.MCFirework;
import com.laytonsmith.abstraction.entities.MCFox;
import com.laytonsmith.abstraction.entities.MCFrog;
import com.laytonsmith.abstraction.entities.MCFrog.MCFrogType;
import com.laytonsmith.abstraction.entities.MCGoat;
import com.laytonsmith.abstraction.entities.MCHanging;
import com.laytonsmith.abstraction.entities.MCHorse;
import com.laytonsmith.abstraction.entities.MCHorse.MCHorseColor;
import com.laytonsmith.abstraction.entities.MCHorse.MCHorsePattern;
import com.laytonsmith.abstraction.entities.MCInteraction;
import com.laytonsmith.abstraction.entities.MCInteraction.MCPreviousInteraction;
import com.laytonsmith.abstraction.entities.MCIronGolem;
import com.laytonsmith.abstraction.entities.MCItem;
import com.laytonsmith.abstraction.entities.MCItemDisplay;
import com.laytonsmith.abstraction.entities.MCItemDisplay.ModelTransform;
import com.laytonsmith.abstraction.entities.MCItemFrame;
import com.laytonsmith.abstraction.entities.MCItemProjectile;
import com.laytonsmith.abstraction.entities.MCLightningStrike;
import com.laytonsmith.abstraction.entities.MCLlama;
import com.laytonsmith.abstraction.entities.MCLlama.MCLlamaColor;
import com.laytonsmith.abstraction.entities.MCMinecart;
import com.laytonsmith.abstraction.entities.MCMushroomCow;
import com.laytonsmith.abstraction.entities.MCPainting;
import com.laytonsmith.abstraction.entities.MCPanda;
import com.laytonsmith.abstraction.entities.MCParrot;
import com.laytonsmith.abstraction.entities.MCPhantom;
import com.laytonsmith.abstraction.entities.MCPig;
import com.laytonsmith.abstraction.entities.MCPiglin;
import com.laytonsmith.abstraction.entities.MCPigZombie;
import com.laytonsmith.abstraction.entities.MCProjectile;
import com.laytonsmith.abstraction.entities.MCPufferfish;
import com.laytonsmith.abstraction.entities.MCRabbit;
import com.laytonsmith.abstraction.entities.MCSheep;
import com.laytonsmith.abstraction.entities.MCShulker;
import com.laytonsmith.abstraction.entities.MCShulkerBullet;
import com.laytonsmith.abstraction.entities.MCSlime;
import com.laytonsmith.abstraction.entities.MCSnowman;
import com.laytonsmith.abstraction.entities.MCSpectralArrow;
import com.laytonsmith.abstraction.entities.MCStrider;
import com.laytonsmith.abstraction.entities.MCTNT;
import com.laytonsmith.abstraction.entities.MCTextDisplay;
import com.laytonsmith.abstraction.entities.MCThrownPotion;
import com.laytonsmith.abstraction.entities.MCTransformation;
import com.laytonsmith.abstraction.entities.MCTrident;
import com.laytonsmith.abstraction.entities.MCTropicalFish;
import com.laytonsmith.abstraction.entities.MCVex;
import com.laytonsmith.abstraction.entities.MCVillager;
import com.laytonsmith.abstraction.entities.MCWitherSkull;
import com.laytonsmith.abstraction.entities.MCWolf;
import com.laytonsmith.abstraction.entities.MCZoglin;
import com.laytonsmith.abstraction.entities.MCZombie;
import com.laytonsmith.abstraction.entities.MCZombieVillager;
import com.laytonsmith.abstraction.enums.MCArt;
import com.laytonsmith.abstraction.enums.MCAxolotlType;
import com.laytonsmith.abstraction.enums.MCBodyPart;
import com.laytonsmith.abstraction.enums.MCCatType;
import com.laytonsmith.abstraction.enums.MCDyeColor;
import com.laytonsmith.abstraction.enums.MCEnderDragonPhase;
import com.laytonsmith.abstraction.enums.MCEntityEffect;
import com.laytonsmith.abstraction.enums.MCEntityType;
import com.laytonsmith.abstraction.enums.MCFireworkType;
import com.laytonsmith.abstraction.enums.MCFoxType;
import com.laytonsmith.abstraction.enums.MCMushroomCowType;
import com.laytonsmith.abstraction.enums.MCParrotType;
import com.laytonsmith.abstraction.enums.MCParticle;
import com.laytonsmith.abstraction.enums.MCPotionType;
import com.laytonsmith.abstraction.enums.MCProfession;
import com.laytonsmith.abstraction.enums.MCRabbitType;
import com.laytonsmith.abstraction.enums.MCRotation;
import com.laytonsmith.abstraction.enums.MCTreeSpecies;
import com.laytonsmith.abstraction.enums.MCVersion;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.seealso;
import com.laytonsmith.core.ArgumentValidation;
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
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CClosure;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CFunction;
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
import com.laytonsmith.core.exceptions.CRE.CREIndexOverflowException;
import com.laytonsmith.core.exceptions.CRE.CREInvalidWorldException;
import com.laytonsmith.core.exceptions.CRE.CRELengthException;
import com.laytonsmith.core.exceptions.CRE.CREPlayerOfflineException;
import com.laytonsmith.core.exceptions.CRE.CRERangeException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.CRE.CREUnsupportedOperationException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.tuple.Triple;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class EntityManagement {

	public static String docs() {
		return "This class of functions allow entities to be managed.";
	}

	public abstract static class EntityFunction extends AbstractFunction {

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}
	}

	public abstract static class EntityGetterFunction extends EntityFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRELengthException.class, CREBadEntityException.class};
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}
	}

	public abstract static class EntitySetterFunction extends EntityFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class, CRELengthException.class, CREBadEntityException.class};
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class all_entities extends EntityFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInvalidWorldException.class, CREFormatException.class, CRECastException.class};
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			CArray ret = new CArray(t);
			if(args.length == 0) {
				for(MCWorld w : Static.getServer().getWorlds()) {
					for(MCEntity e : w.getEntities()) {
						ret.push(new CString(e.getUniqueId().toString(), t), t);
					}
				}
			} else {
				MCWorld w;
				MCChunk c;
				if(args.length == 3) {
					w = Static.getServer().getWorld(args[0].val());
					if(w == null) {
						throw new CREInvalidWorldException("Unknown world: " + args[0].val(), t);
					}
					try {
						int x = ArgumentValidation.getInt32(args[1], t);
						int z = ArgumentValidation.getInt32(args[2], t);
						c = w.getChunkAt(x, z);
					} catch(ConfigRuntimeException cre) {
						CArray l = CArray.GetAssociativeArray(t);
						l.set("x", args[1], t);
						l.set("z", args[2], t);
						c = w.getChunkAt(ObjectGenerator.GetGenerator().location(l, w, t));
					}
					for(MCEntity e : c.getEntities()) {
						ret.push(new CString(e.getUniqueId().toString(), t), t);
					}
				} else {
					if(args[0].isInstanceOf(CArray.TYPE)) {
						c = ObjectGenerator.GetGenerator().location(args[0], null, t).getChunk();
						for(MCEntity e : c.getEntities()) {
							ret.push(new CString(e.getUniqueId().toString(), t), t);
						}
					} else {
						w = Static.getServer().getWorld(args[0].val());
						if(w == null) {
							throw new CREInvalidWorldException("Unknown world: " + args[0].val(), t);
						}
						for(MCEntity e : w.getEntities()) {
							ret.push(new CString(e.getUniqueId().toString(), t), t);
						}
					}
				}
			}
			return ret;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
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
					+ " argument is given and is a location, only entities in the chunk containing that location will"
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

	@api(environments = {CommandHelperEnvironment.class})
	public static class entity_exists extends EntityGetterFunction {

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			try {
				Static.getEntity(args[0], t);
			} catch(ConfigRuntimeException cre) {
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
			return "boolean {entityUUID} Returns true if entity exists, otherwise false.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class is_entity_living extends EntityGetterFunction {

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCEntity e;

			try {
				e = Static.getEntity(args[0], t);
			} catch(ConfigRuntimeException cre) {
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
			return "boolean {entityUUID} Returns true if entity is living, otherwise false.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
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
			return "boolean {entityUUID} Returns true or false if the specified entity is tameable";
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
			MCEntity e = Static.getEntity(args[0], t);
			return CBoolean.get(e instanceof MCLivingEntity && ((MCLivingEntity) e).isTameable());
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class entity_loc extends EntityGetterFunction {

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCEntity e = Static.getEntity(args[0], t);
			return ObjectGenerator.GetGenerator().location(e.getLocation());
		}

		@Override
		public String getName() {
			return "entity_loc";
		}

		@Override
		public String docs() {
			return "locationArray {entityUUID} Returns the location array for this entity, if it exists."
					+ " This array will be compatible with any function that expects a location.";
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Sample output", "entity_loc('091a595d-3d2f-4df4-b493-951dc4bed7f2')",
				"{0: -3451.96, 1: 65.0, 2: 718.521, 3: world, 4: -170.9, 5: 35.5, pitch: 35.5,"
				+ " world: world, x: -3451.96, y: 65.0, yaw: -170.9, z: 718.521}")
			};
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_entity_loc extends EntitySetterFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREBadEntityException.class, CREFormatException.class, CRECastException.class,
				CREInvalidWorldException.class, CRELengthException.class, CREIllegalArgumentException.class};
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCEntity e = Static.getEntity(args[0], t);
			MCLocation l;
			if(args[1].isInstanceOf(CArray.TYPE)) {
				l = ObjectGenerator.GetGenerator().location(args[1], e.getWorld(), t);
			} else {
				throw new CREFormatException("An array was expected but received " + args[1], t);
			}
			try {
				if(!Float.isFinite(l.getPitch())) {
					throw new CREIllegalArgumentException("pitch not finite", t);
				}
				return CBoolean.get(e.teleport(l));
			} catch(IllegalArgumentException ex) {
				throw new CREIllegalArgumentException(ex.getMessage(), t);
			}
		}

		@Override
		public String getName() {
			return "set_entity_loc";
		}

		@Override
		public String docs() {
			return "boolean {entityUUID, locationArray} Teleports the entity to the given location and returns whether"
					+ " the action was successful. Note this can set both location and direction. On paper servers,"
					+ " this teleports passengers along with the entity.";
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Teleporting an entity to you",
				"set_entity_loc('091a595d-3d2f-4df4-b493-951dc4bed7f2', ploc())",
				"The entity will teleport to the block you are standing on."),
				new ExampleScript("Teleporting an entity to another",
				"set_entity_loc('091a595d-3d2f-4df4-b493-951dc4bed7f2', entity_loc('82ed3624-b86b-41ef-9cde-4f3ea818b8e5'))",
				"The entity will teleport to the other and face the same direction, if they both exist."),
				new ExampleScript("Setting location with a normal array",
				"set_entity_loc('82ed3624-b86b-41ef-9cde-4f3ea818b8e5', array(214, 64, 1812, 'world', -170, 10))",
				"This set location and direction."),
				new ExampleScript("Setting location with an associative array",
				"set_entity_loc('82ed3624-b86b-41ef-9cde-4f3ea818b8e5', array(x: 214, y: 64, z: 1812, world: 'world', yaw: -170, pitch: 10))",
				"This also sets location and direction")
			};
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class entity_velocity extends EntityGetterFunction {

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCEntity e = Static.getEntity(args[0], t);
			CArray va  = ObjectGenerator.GetGenerator().vector(e.getVelocity(), t);
			va.set("magnitude", new CDouble(e.getVelocity().length(), t), t);
			return va;
		}

		@Override
		public String getName() {
			return "entity_velocity";
		}

		@Override
		public String docs() {
			return "array {entityUUID} Returns an entity's motion vector represented as an associative array with the"
					+ " the keys x, y, and z. As a convenience, the magnitude is also included.";
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("A stationary entity",
				"msg(entity_velocity('091a595d-3d2f-4df4-b493-951dc4bed7f2'))",
				"{magnitude: 0.0, x: 0.0, y: 0.0, z: 0.0}")
			};
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_entity_velocity extends EntitySetterFunction {

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCEntity e = Static.getEntity(args[0], t);
			try {
				e.setVelocity(ObjectGenerator.GetGenerator().vector(args[1], t));
			} catch(IllegalArgumentException ex) {
				throw new CREIllegalArgumentException(ex.getMessage(), t);
			}
			return CVoid.VOID;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class, CRELengthException.class, CREBadEntityException.class,
				CREIllegalArgumentException.class};
		}

		@Override
		public String getName() {
			return "set_entity_velocity";
		}

		@Override
		public String docs() {
			return "void {entityUUID, array} Sets the velocity of this entity according to the supplied xyz array."
					+ " All 3 values default to 0, so an empty array will simply stop the entity's motion. Both normal"
					+ " and associative arrays are accepted.";
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Setting a bounce with a normal array",
				"set_entity_velocity('091a595d-3d2f-4df4-b493-951dc4bed7f2', array(0, 0.5, 0))",
				"The entity just hopped, unless it was an item frame or painting."),
				new ExampleScript("Setting a bounce with an associative array",
				"set_entity_velocity('091a595d-3d2f-4df4-b493-951dc4bed7f2', array(y: 0.5))",
				"The entity just hopped, unless it was an item frame or painting.")
			};
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class entity_remove extends EntityGetterFunction {

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCEntity ent = Static.getEntity(args[0], t);
			if(ent instanceof MCHumanEntity) {
				throw new CREBadEntityException("Cannot remove human entity (" + ent.getUniqueId() + ")!", t);
			}
			ent.remove();
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "entity_remove";
		}

		@Override
		public String docs() {
			return "void {entityUUID} Removes the specified entity from the world, without any drops or animations. "
					+ "Note: you can't remove players. As a safety measure for working with NPC plugins, it will "
					+ "not work on anything human, even if it is not a player.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_entity_saves_on_unload extends EntityGetterFunction {

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCEntity ent = Static.getEntity(args[0], t);
			return CBoolean.get(ent.savesOnUnload());
		}

		@Override
		public String getName() {
			return "get_entity_saves_on_unload";
		}

		@Override
		public String docs() {
			return "void {entityUUID} Gets whether the entity will be saved to disk when it is unloaded.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_5;
		}

	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_entity_saves_on_unload extends EntitySetterFunction {

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCEntity ent = Static.getEntity(args[0], t);
			ent.setSavesOnUnload(ArgumentValidation.getBooleanObject(args[1], t));
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "set_entity_saves_on_unload";
		}

		@Override
		public String docs() {
			return "void {entityUUID, boolean} Sets whether the entity is saved to disk when it is unloaded."
					+ " By default an entity is saved. Setting this to false disables that."
					+ " Can be used on players to disable player data saving on quit.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_5;
		}

	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class entity_type extends EntityGetterFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCEntity ent;
			try {
				ent = Static.getEntity(args[0], t);
			} catch(ConfigRuntimeException cre) {
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
			return "string {entityUUID} Returns the EntityType of the entity with the specified ID."
					+ " Returns null if the entity does not exist.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_entity_age extends EntityGetterFunction {

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			return new CInt(Static.getEntity(args[0], t).getTicksLived(), t);
		}

		@Override
		public String getName() {
			return "get_entity_age";
		}

		@Override
		public String docs() {
			return "int {entityUUID} Returns the entity age as an integer, represented by server ticks.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_entity_age extends EntitySetterFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREBadEntityException.class, CRERangeException.class,
				CRELengthException.class};
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			int age = ArgumentValidation.getInt32(args[1], t);
			MCEntity ent = Static.getEntity(args[0], t);
			try {
				ent.setTicksLived(age);
			} catch(IllegalArgumentException ex) {
				throw new CRERangeException(ex.getMessage(), t);
			}
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "set_entity_age";
		}

		@Override
		public String docs() {
			return "void {entityUUID, int} Sets the age of the entity to the specified int,"
					+ " represented by server ticks. Entity age cannot be less than 1 server tick,";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class shoot_projectile extends EntityFunction implements Optimizable {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREBadEntityException.class, CREBadEntityTypeException.class, CREFormatException.class,
				CREPlayerOfflineException.class};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {

			MCPlayer p = env.getEnv(CommandHelperEnvironment.class).GetPlayer();

			MCLivingEntity shooter = null;
			MCLivingEntity target = null;

			MCLocation from = null;
			MCLocation to = null;

			MCEntityType projectile = null;

			double speed = 0.0;

			if(args.length >= 1) {
				if(args[0] instanceof CArray) {
					from = ObjectGenerator.GetGenerator().location(args[0], p != null ? p.getWorld() : null, t);
				} else if(args[0].val().length() > 16) {
					shooter = Static.getLivingEntity(args[0], t);
				} else {
					shooter = Static.GetPlayer(args[0], t);
				}

				if(args.length >= 2) {
					try {
						projectile = MCEntityType.valueOf(args[1].val().toUpperCase());
					} catch(IllegalArgumentException ex) {
						throw new CREBadEntityTypeException(args[1] + " is not a valid entity type", t);
					}

					if(args.length >= 3) {
						if(args[2] instanceof CArray) {
							to = ObjectGenerator.GetGenerator().location(args[2], p != null ? p.getWorld() : null, t);
						} else if(args[2].val().length() > 16) {
							target = Static.getLivingEntity(args[2], t);
						} else {
							target = Static.GetPlayer(args[2], t);
						}

						if(args.length == 4) {
							speed = ArgumentValidation.getDouble(args[3], t);
						}
					}
				}

			} else {
				Static.AssertPlayerNonNull(p, t);
				shooter = p;
			}

			if(projectile == null) {
				projectile = MCEntityType.valueOfVanillaType(MCEntityType.MCVanillaEntityType.FIREBALL);
				if(projectile == null) {
					throw new CREBadEntityTypeException("Default projectile is invalid.", t);
				}
			}

			if(shooter != null && projectile.isProjectile() && args.length < 3) {
				return new CString(shooter.launchProjectile(projectile).getUniqueId().toString(), t);
			}

			if(from == null) {
				if(shooter == null) {
					throw new CREIllegalArgumentException("Invalid shooter.", t);
				}
				from = shooter.getEyeLocation();
			}

			Vector3D velocity;
			if(to == null) {
				if(target != null) {
					velocity = target.getEyeLocation().toVector().subtract(from.toVector()).normalize();
				} else {
					velocity = from.getDirection();
				}
			} else {
				velocity = to.toVector().subtract(from.toVector()).normalize();
			}

			if(shooter != null) {
				from = from.add(velocity);
			}
			MCEntity entity = from.getWorld().spawn(from, projectile);
			if(speed == 0.0) {
				entity.setVelocity(velocity);
			} else {
				entity.setVelocity(velocity.multiply(speed));
			}
			if(shooter != null && entity instanceof MCProjectile) {
				((MCProjectile) entity).setShooter(shooter);
			}
			return new CString(entity.getUniqueId().toString(), t);
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
			return "string {[entity[, projectile]] | player, projectile, target[, speed]} Shoots an entity from the"
					+ " specified location (can be an entity UUID, player name or location array), or the current"
					+ " player if no arguments are passed. If no entity type is specified, it defaults to a fireball."
					+ " If provide three arguments, with target (entity UUID, player name or location array), entity"
					+ " will shoot to target location. Last, fourth argument, is double and specifies the speed"
					+ " of projectile. Returns the UUID of the entity.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ParseTree optimizeDynamic(Target t, com.laytonsmith.core.environments.Environment env,
				Set<Class<? extends com.laytonsmith.core.environments.Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {

			if(children.size() < 2) {
				return null;
			}
			Mixed c = children.get(1).getData();
			if(c.isInstanceOf(CString.TYPE)) {
				try {
					MCEntityType.MCVanillaEntityType.valueOf(c.val().toUpperCase());
				} catch(IllegalArgumentException ex) {
					env.getEnv(CompilerEnvironment.class).addCompilerWarning(fileOptions, new CompilerWarning(
							c.val() + " is not a valid enum in com.commandhelper.EntityType",
							c.getTarget(), null));
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
	public static class entities_in_radius extends EntityFunction implements Optimizable {

		@Override
		public String getName() {
			return "entities_in_radius";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = env.getEnv(CommandHelperEnvironment.class).GetPlayer();

			MCLocation loc;
			int dist;
			List<String> types = new ArrayList<>();

			if(!(args[0].isInstanceOf(CArray.TYPE))) {
				throw new CREBadEntityException("Expecting an array at parameter 1 of entities_in_radius", t);
			}

			loc = ObjectGenerator.GetGenerator().location(args[0], p != null ? p.getWorld() : null, t);
			dist = ArgumentValidation.getInt32(args[1], t);

			if(dist < 0) {
				throw new CRERangeException("Distance cannot be negative.", t);
			}

			if(args.length == 3) {
				if(args[2].isInstanceOf(CArray.TYPE)) {
					CArray ta = (CArray) args[2];
					for(int i = 0; i < ta.size(); i++) {
						types.add(ta.get(i, t).val());
					}
				} else {
					types.add(args[2].val());
				}
				types = prepareTypes(t, types);
			}

			MCWorld world = loc.getWorld();
			int chunkRadius = (dist + 16) / 16;
			int distanceSquared = dist * dist;
			int centerX = loc.getBlockX() >> 4;
			int centerZ = loc.getBlockZ() >> 4;

			CArray entities = new CArray(t);
			for(int offsetX = 0 - chunkRadius; offsetX <= chunkRadius; offsetX++) {
				for(int offsetZ = 0 - chunkRadius; offsetZ <= chunkRadius; offsetZ++) {
					if(!world.isChunkLoaded(centerX + offsetX, centerZ + offsetZ)) {
						continue;
					}
					for(MCEntity e : world.getChunkAt(centerX + offsetX, centerZ + offsetZ).getEntities()) {
						if(e.getLocation().distanceSquared(loc) <= distanceSquared) {
							if(types.isEmpty() || types.contains(e.getType().name())) {
								entities.push(new CString(e.getUniqueId().toString(), t), t);
							}
						}
					}
				}
			}
			return entities;
		}

		private List<String> prepareTypes(Target t, List<String> types) {
			List<String> newTypes = new ArrayList<>();
			MCEntityType entityType;
			for(String type : types) {
				try {
					entityType = MCEntityType.valueOf(type.toUpperCase());
				} catch(IllegalArgumentException e) {
					throw new CREBadEntityException(String.format("Wrong entity type: %s", type), t);
				}
				newTypes.add(entityType.name());
			}
			return newTypes;
		}

		@Override
		public String docs() {
			return "array {locationArray, distance, [type] | locationArray, distance, [arrayTypes]} Returns an array of"
					+ " all entities within the given distance from the location. Set type argument to filter entities"
					+ " to a specific entity type. You can pass an array of types. ---- Valid types: "
					+ StringUtils.Join(MCEntityType.types(), ", ", ", or ", " or ");
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREBadEntityException.class, CREFormatException.class,
				CRERangeException.class};
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ParseTree optimizeDynamic(Target t, com.laytonsmith.core.environments.Environment env,
				Set<Class<? extends com.laytonsmith.core.environments.Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {

			if(children.size() < 3) {
				return null;
			}
			Mixed c = children.get(children.size() - 1).getData();
			if(c.isInstanceOf(CString.TYPE)) {
				try {
					MCEntityType.MCVanillaEntityType.valueOf(c.val().toUpperCase());
				} catch(IllegalArgumentException ex) {
					env.getEnv(CompilerEnvironment.class).addCompilerWarning(fileOptions, new CompilerWarning(
							c.val() + " is not a valid enum in com.commandhelper.EntityType",
							c.getTarget(), null));
				}
			} else if(c instanceof CFunction && c.val().equals(DataHandling.array.NAME)) {
				for(ParseTree node : children.get(children.size() - 1).getChildren()) {
					if(node.getData().isInstanceOf(CString.TYPE)) {
						try {
							MCEntityType.MCVanillaEntityType.valueOf(node.getData().val().toUpperCase());
						} catch(IllegalArgumentException ex) {
							env.getEnv(CompilerEnvironment.class).addCompilerWarning(fileOptions, new CompilerWarning(
									node.getData().val() + " is not a valid enum in com.commandhelper.EntityType",
									node.getTarget(), null));
						}
					}
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
	public static class entity_onfire extends EntityGetterFunction {

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCEntity ent = Static.getEntity(args[0], t);
			return new CInt(ent.getFireTicks() / 20, t);
		}

		@Override
		public String getName() {
			return "entity_onfire";
		}

		@Override
		public String docs() {
			return "int {entityUUID} Returns the number of seconds until this entity"
					+ " stops being on fire, 0 if it already isn't.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_entity_onfire extends EntitySetterFunction {

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCEntity ent = Static.getEntity(args[0], t);
			int seconds = ArgumentValidation.getInt32(args[1], t);
			if(seconds < 0) {
				throw new CRERangeException("Seconds cannot be less than 0", t);
			} else if(seconds > Integer.MAX_VALUE / 20) {
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
			return "void {entityUUID, seconds} Sets the entity on fire for the"
					+ " given number of seconds. Throws a RangeException"
					+ " if seconds is less than 0 or greater than 107374182.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class play_entity_effect extends EntitySetterFunction {

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCEntity ent = Static.getEntity(args[0], t);
			MCEntityEffect mee;
			try {
				mee = MCEntityEffect.valueOf(args[1].val().toUpperCase());
			} catch(IllegalArgumentException iae) {
				throw new CREFormatException("Unknown entity effect: " + args[1].val(), t);
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
			return "void {entityUUID, effect} Plays the given visual effect on the"
					+ " entity. Non-applicable effects simply won't happen. Note:"
					+ " the death effect makes the mob invisible to players and"
					+ " immune to melee attacks. When used on players, they are"
					+ " shown the respawn menu, but because they are not actually"
					+ " dead, they can only log out. ---- Possible effects are: "
					+ StringUtils.Join(MCEntityEffect.values(), ", ", ", or ", " or ");
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_mob_name extends EntityGetterFunction {

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCEntity le = Static.getEntity(args[0], t);
			try {
				return new CString(le.getCustomName(), t);
			} catch(IllegalArgumentException e) {
				throw new CRECastException(e.getMessage(), t);
			}
		}

		@Override
		public String getName() {
			return "get_mob_name";
		}

		@Override
		public String docs() {
			return "string {entityUUID} Returns the name of the given mob.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_mob_name extends EntitySetterFunction {

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCEntity le = Static.getEntity(args[0], t);
			try {
				le.setCustomName(args[1].val());
			} catch(IllegalArgumentException e) {
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
			return "void {entityUUID, name} Sets the name of the given mob. This"
					+ " automatically truncates if it is more than 64 characters.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class spawn_entity extends EntityFunction implements Optimizable {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREFormatException.class, CREBadEntityException.class,
				CREInvalidWorldException.class, CREPlayerOfflineException.class, CREIllegalArgumentException.class};
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCCommandSender cs = environment.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			int qty = 1;
			CArray ret = new CArray(t);
			MCEntityType entType;
			MCLocation l;
			MCEntity ent;
			CClosure consumer = null;
			if(args.length >= 3) {
				l = ObjectGenerator.GetGenerator().location(args[2], null, t);
				if(args.length == 4) {
					if(args[3].isInstanceOf(CClosure.TYPE)) {
						consumer = (CClosure) args[3];
					} else {
						throw new CREIllegalArgumentException("Expected a closure as last argument for spawn_entity().", t);
					}
				}
			} else {
				if(cs instanceof MCPlayer) {
					l = ((MCPlayer) cs).getLocation();
				} else if(cs instanceof MCBlockCommandSender) {
					l = ((MCBlockCommandSender) cs).getBlock().getRelative(MCBlockFace.UP).getLocation();
				} else if(cs instanceof MCCommandMinecart) {
					l = ((MCCommandMinecart) cs).getLocation().add(0, 1, 0); // One block above the minecart.
				} else {
					throw new CREPlayerOfflineException("A physical commandsender must exist or location must be explicit.", t);
				}
			}
			if(args.length >= 2) {
				qty = ArgumentValidation.getInt32(args[1], t);
			}
			try {
				entType = MCEntityType.valueOf(args[0].val().toUpperCase());
				if(!entType.isSpawnable()) {
					throw new CREFormatException("spawn_entity() cannot spawn this entity type: " + args[0].val(), t);
				}
			} catch(IllegalArgumentException iae) {
				throw new CREFormatException("Unknown entity type: " + args[0].val(), t);
			}
			for(int i = 0; i < qty; i++) {
				switch(entType.getAbstracted()) {
					case DROPPED_ITEM:
						int itemQty = java.lang.Math.min(qty - i, 64);
						ent = l.getWorld().dropItem(l, StaticLayer.GetItemStack("STONE", itemQty));
						i += itemQty - 1;
						break;
					case FALLING_BLOCK:
						ent = l.getWorld().spawnFallingBlock(l, StaticLayer.GetMaterial("SAND").createBlockData());
						break;
					case ITEM_FRAME:
					case LEASH_HITCH:
					case PAINTING:
						try {
							if(consumer != null) {
								ent = l.getWorld().spawn(l.getBlock().getLocation(), entType, consumer);
							} else {
								ent = l.getWorld().spawn(l.getBlock().getLocation(), entType);
							}
						} catch(NullPointerException | IllegalArgumentException | IllegalStateException ex) {
							throw new CREFormatException("Unspawnable location for " + entType.getAbstracted().name(), t);
						}
						break;
					default:
						try {
							if(consumer != null) {
								ent = l.getWorld().spawn(l, entType, consumer);
							} else {
								ent = l.getWorld().spawn(l, entType);
							}
						} catch(IllegalArgumentException ex) {
							throw new CREFormatException(ex.getMessage(), t);
						}
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
			return new Integer[]{1, 2, 3, 4};
		}

		@Override
		public String docs() {
			List<String> spawnable = new ArrayList<>();
			for(MCEntityType type : MCEntityType.values()) {
				if(type.isSpawnable()) {
					spawnable.add(type.name());
				}
			}
			return "array {entityType, [qty], [location], [closure]} Spawns the specified number of entities of the"
					+ " given type at the given location. Returns an array of entity UUIDs of what is spawned."
					+ " Qty defaults to 1 and location defaults to the location of the commandsender,"
					+ " if it is a block or player. If the commandsender is console, location must be supplied."
					+ " ---- Entitytype can be one of " + StringUtils.Join(spawnable, ", ", " or ", ", or ") + "."
					+ " FALLING_BLOCK will be SAND (can use {{function|spawn_falling_block}} instead)."
					+ " DROPPED_ITEM will be STONE (can use {{function|drop_item}} instead)."
					+ " If not FALLING_BLOCK or DROPPED_ITEM, a closure can be used as the last argument to modify the"
					+ " entity before adding it to the world. The entity's UUID is passed to the closure.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Applying entity attributes before adding it to the world.",
				"spawn_entity('ZOMBIE', 1, ptarget_space(),"
				+ " closure(@id){ set_entity_spec(@id, array('baby': true)); set_entity_ai(@id, false); })",
				"Creates a zombie, changes it to a baby zombie without AI, then adds it to the world."),
			};
		}

		@Override
		public ParseTree optimizeDynamic(Target t, com.laytonsmith.core.environments.Environment env,
				Set<Class<? extends com.laytonsmith.core.environments.Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {

			if(children.size() < 1) {
				return null;
			}
			Mixed c = children.get(0).getData();
			if(c.isInstanceOf(CString.TYPE)) {
				try {
					MCEntityType.MCVanillaEntityType type = MCEntityType.MCVanillaEntityType.valueOf(c.val().toUpperCase());
					if(!type.isSpawnable()) {
						env.getEnv(CompilerEnvironment.class).addCompilerWarning(fileOptions, new CompilerWarning(
								"The entity type " + c.val() + " cannot be spawned by " + getName(),
								c.getTarget(), null));
					}
				} catch(IllegalArgumentException ex) {
					env.getEnv(CompilerEnvironment.class).addCompilerWarning(fileOptions, new CompilerWarning(
							c.val() + " is not a valid enum in com.commandhelper.EntityType",
							c.getTarget(), null));
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
	public static class set_entity_rider extends EntitySetterFunction {

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCEntity horse;
			MCEntity rider;
			boolean success;
			if(args[0] instanceof CNull) {
				horse = null;
			} else {
				horse = Static.getEntity(args[0], t);
			}
			if(args[1] instanceof CNull) {
				rider = null;
			} else {
				rider = Static.getEntity(args[1], t);
			}
			if((horse == null && rider == null) || args[0].val().equals(args[1].val())) {
				throw new CREFormatException("Horse and rider cannot be the same entity", t);
			} else if(horse == null) {
				success = rider.leaveVehicle();
			} else if(rider == null) {
				success = horse.eject();
			} else {
				try {
					success = horse.setPassenger(rider);
				} catch(IllegalStateException ex) {
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
					+ " If horse and rider are both null, or otherwise the same, a FormatException is thrown."
					+ " If a horse already has a rider, this will add the new rider without ejecting the existing one.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_entity_rider extends EntityGetterFunction {

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCEntity ent = Static.getEntity(args[0], t);
			List<MCEntity> passengers = ent.getPassengers();
			if(!passengers.isEmpty()) {
				return new CString(passengers.get(0).getUniqueId().toString(), t);
			}
			return CNull.NULL;
		}

		@Override
		public String getName() {
			return "get_entity_rider";
		}

		@Override
		public String docs() {
			return "string {entityUUID} Returns the UUID of the given entity's rider, or null if it doesn't have one."
					+ " If there are multiple riders, only the first is returned.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_entity_riders extends EntityGetterFunction {

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCEntity ent = Static.getEntity(args[0], t);
			List<MCEntity> riders = ent.getPassengers();
			CArray ret = new CArray(t);
			for(MCEntity rider : riders) {
				ret.push(new CString(rider.getUniqueId().toString(), t), t);
			}
			return ret;
		}

		@Override
		public String getName() {
			return "get_entity_riders";
		}

		@Override
		public String docs() {
			return "array {entityUUID} Returns an array of UUIDs for the given entity's riders.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_3;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_entity_vehicle extends EntityGetterFunction {

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCEntity ent = Static.getEntity(args[0], t);
			if(ent.isInsideVehicle()) {
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
			return "string {entityUUID} Returns the UUID of the given entity's vehicle, or null if none exists.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_entity_max_speed extends EntityGetterFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREBadEntityException.class, CREBadEntityTypeException.class, CRELengthException.class};
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCEntity e = Static.getEntity(args[0], t);
			if(e instanceof MCMinecart) {
				return new CDouble(((MCMinecart) e).getMaxSpeed(), t);
			}
			throw new CREBadEntityTypeException("Given entity must be a minecart.", t);
		}

		@Override
		public String getName() {
			return "get_entity_max_speed";
		}

		@Override
		public String docs() {
			return "double {entityUUID} Returns a max speed for given entity. Make sure that the entity is a minecart.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_entity_max_speed extends EntitySetterFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREBadEntityException.class, CREBadEntityTypeException.class,
				CRECastException.class, CRELengthException.class};
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCEntity e = Static.getEntity(args[0], t);
			double speed = ArgumentValidation.getDouble(args[1], t);
			if(e instanceof MCMinecart) {
				((MCMinecart) e).setMaxSpeed(speed);
			} else {
				throw new CREBadEntityTypeException("Given entity must be a minecart.", t);
			}
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "set_entity_max_speed";
		}

		@Override
		public String docs() {
			return "void {entityUUID} Sets a max speed for given entity. Make sure that the entity is a minecart.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_name_visible extends EntityGetterFunction {

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			try {
				return CBoolean.get(Static.getEntity(args[0], t).isCustomNameVisible());
			} catch(IllegalArgumentException e) {
				throw new CRECastException(e.getMessage(), t);
			}
		}

		@Override
		public String getName() {
			return "get_name_visible";
		}

		@Override
		public String docs() {
			return "boolean {entityUUID} Returns whether or not a mob's custom name is always visible."
					+ " If this is true it will be as visible as player names, otherwise it will only be"
					+ " visible when near the mob.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_name_visible extends EntitySetterFunction {

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			try {
				Static.getEntity(args[0], t).setCustomNameVisible(ArgumentValidation.getBoolean(args[1], t));
			} catch(IllegalArgumentException e) {
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
			return "void {entityUUID, boolean} Sets the visibility of a mob's custom name."
					+ " True means it will be visible from a distance, like a playername."
					+ " False means it will only be visible when near the mob.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
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
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCWorld w = null;
			if(environment.getEnv(CommandHelperEnvironment.class).GetPlayer() != null) {
				w = environment.getEnv(CommandHelperEnvironment.class).GetPlayer().getWorld();
			}
			List<MCEntity> es = StaticLayer.GetConvertor().GetEntitiesAt(ObjectGenerator.GetGenerator().location(args[0], w, t), 1);
			for(MCEntity e : es) {
				if(e instanceof MCPainting) {
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
			return MSVersion.V3_3_1;
		}

	}

	@api(environments = {CommandHelperEnvironment.class})
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
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCWorld w = null;
			if(environment.getEnv(CommandHelperEnvironment.class).GetPlayer() != null) {
				w = environment.getEnv(CommandHelperEnvironment.class).GetPlayer().getWorld();
			}
			MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], w, t);
			MCArt art;
			try {
				art = MCArt.valueOf(args[1].val());
			} catch(IllegalArgumentException e) {
				throw new CREFormatException("Invalid type: " + args[1].val(), t);
			}
			//If there's already a painting there, just use that one. Otherwise, spawn a new one.
			MCPainting p = null;
			for(MCEntity e : StaticLayer.GetConvertor().GetEntitiesAt(loc, 1)) {
				if(e instanceof MCPainting) {
					p = (MCPainting) e;
					break;
				}
			}
			if(p == null) {
				p = (MCPainting) loc.getWorld().spawn(loc, MCEntityType.MCVanillaEntityType.PAINTING);
			}
			boolean worked = p.setArt(art);
			if(!worked) {
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
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class entity_grounded extends EntityGetterFunction {

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			return CBoolean.get(Static.getEntity(args[0], t).isOnGround());
		}

		@Override
		public String getName() {
			return "entity_grounded";
		}

		@Override
		public String docs() {
			return "boolean {entityUUID} returns whether the entity is touching the ground";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
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
			docs = docs.replace("%LLAMA_COLOR%", StringUtils.Join(MCLlamaColor.values(), ", ", ", or ", " or "));
			docs = docs.replace("%ROTATION%", StringUtils.Join(MCRotation.values(), ", ", ", or ", " or "));
			docs = docs.replace("%PARROT_TYPE%", StringUtils.Join(MCParrotType.values(), ", ", ", or ", " or "));
			docs = docs.replace("%ART%", StringUtils.Join(MCArt.values(), ", ", ", or ", " or "));
			docs = docs.replace("%DYE_COLOR%", StringUtils.Join(MCDyeColor.values(), ", ", ", or ", " or "));
			docs = docs.replace("%PROFESSION%", StringUtils.Join(MCProfession.values(), ", ", ", or ", " or "));
			docs = docs.replace("%RABBIT_TYPE%", StringUtils.Join(MCRabbitType.values(), ", ", ", or ", " or "));
			docs = docs.replace("%PARTICLE%", StringUtils.Join(MCParticle.types(), ", ", ", or ", " or "));
			docs = docs.replace("%ENDERDRAGON_PHASE%", StringUtils.Join(MCEnderDragonPhase.values(), ", ", ", or ", " or "));
			docs = docs.replace("%FISH_PATTERN%", StringUtils.Join(MCTropicalFish.MCPattern.values(), ", ", ", or ", " or "));
			docs = docs.replace("%CAT_TYPE%", StringUtils.Join(MCCatType.values(), ", ", ", or ", " or "));
			docs = docs.replace("%FOX_TYPE%", StringUtils.Join(MCFoxType.values(), ", ", ", or ", " or "));
			docs = docs.replace("%MUSHROOM_COW_TYPE%", StringUtils.Join(MCMushroomCowType.values(), ", ", ", or ", " or "));
			docs = docs.replace("%PANDA_GENE%", StringUtils.Join(MCPanda.Gene.values(), ", ", ", or ", " or "));
			docs = docs.replace("%AXOLOTL_TYPE%", StringUtils.Join(MCAxolotlType.values(), ", ", ", or ", " or "));
			docs = docs.replace("%FROG_TYPE%", StringUtils.Join(MCFrogType.values(), ", ", ", or ", " or "));
			docs = docs.replace("%ITEM_DISPLAY%", StringUtils.Join(ModelTransform.values(), ", ", ", or "));
			docs = docs.replace("%WOLF_TYPE%", StringUtils.Join(MCWolf.Variant.values(), ", ", ", or "));
			for(Field field : entity_spec.class.getDeclaredFields()) {
				try {
					String name = field.getName();
					if(name.startsWith("KEY_")) {
						docs = docs.replace("%" + name + "%", (String) field.get(null));
					}
				} catch(IllegalArgumentException | IllegalAccessException ex) {
					ex.printStackTrace();
				}
			}
			return docs;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCEntity entity = Static.getEntity(args[0], t);
			CArray specArray = CArray.GetAssociativeArray(t);
			switch(entity.getType().getAbstracted()) {
				case AREA_EFFECT_CLOUD:
					MCAreaEffectCloud cloud = (MCAreaEffectCloud) entity;
					specArray.set(entity_spec.KEY_AREAEFFECTCLOUD_COLOR, ObjectGenerator.GetGenerator().color(cloud.getColor(), t), t);
					specArray.set(entity_spec.KEY_AREAEFFECTCLOUD_DURATION, new CInt(cloud.getDuration(), t), t);
					specArray.set(entity_spec.KEY_AREAEFFECTCLOUD_DURATIONONUSE, new CInt(cloud.getDurationOnUse(), t), t);
					specArray.set(entity_spec.KEY_AREAEFFECTCLOUD_PARTICLE, new CString(cloud.getParticle().name(), t), t);
					CArray meta = CArray.GetAssociativeArray(t);
					CArray effects = ObjectGenerator.GetGenerator().potions(cloud.getCustomEffects(), t);
					meta.set("potions", effects, t);
					if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_20_6)) {
						MCPotionType potionType = cloud.getBasePotionType();
						if(potionType == null) {
							meta.set("potiontype", CNull.NULL, t);
						} else {
							meta.set("potiontype", potionType.name());
						}
					} else {
						meta.set("base", ObjectGenerator.GetGenerator().potionData(cloud.getBasePotionData(), t), t);
					}
					specArray.set(entity_spec.KEY_AREAEFFECTCLOUD_POTIONMETA, meta, t);
					specArray.set(entity_spec.KEY_AREAEFFECTCLOUD_RADIUS, new CDouble(cloud.getRadius(), t), t);
					specArray.set(entity_spec.KEY_AREAEFFECTCLOUD_RADIUSONUSE, new CDouble(cloud.getRadiusOnUse(), t), t);
					specArray.set(entity_spec.KEY_AREAEFFECTCLOUD_RADIUSPERTICK, new CDouble(cloud.getRadiusPerTick(), t), t);
					specArray.set(entity_spec.KEY_AREAEFFECTCLOUD_REAPPLICATIONDELAY, new CInt(cloud.getReapplicationDelay(), t), t);
					MCProjectileSource cloudSource = cloud.getSource();
					if(cloudSource instanceof MCBlockProjectileSource) {
						MCLocation blockLocation = ((MCBlockProjectileSource) cloudSource).getBlock().getLocation();
						CArray locationArray = ObjectGenerator.GetGenerator().location(blockLocation, false);
						specArray.set(entity_spec.KEY_AREAEFFECTCLOUD_SOURCE, locationArray, t);
					} else if(cloudSource instanceof MCEntity) {
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
					specArray.set(entity_spec.KEY_ARROW_DAMAGE, new CDouble(arrow.getDamage(), t), t);
					specArray.set(entity_spec.KEY_ARROW_PIERCE_LEVEL, new CInt(arrow.getPierceLevel(), t), t);
					specArray.set(entity_spec.KEY_ARROW_PICKUP, new CString(arrow.getPickupStatus().name(), t), t);
					CArray tippedmeta = CArray.GetAssociativeArray(t);
					CArray tippedeffects = ObjectGenerator.GetGenerator().potions(arrow.getCustomEffects(), t);
					tippedmeta.set("potions", tippedeffects, t);
					if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_20_6)) {
						MCPotionType potionType = arrow.getBasePotionType();
						if(potionType == null) {
							tippedmeta.set("potiontype", CNull.NULL, t);
						} else {
							tippedmeta.set("potiontype", potionType.name());
						}
					} else {
						tippedmeta.set("base", ObjectGenerator.GetGenerator().potionData(arrow.getBasePotionData(), t), t);
					}
					specArray.set(entity_spec.KEY_TIPPEDARROW_POTIONMETA, tippedmeta, t);
					break;
				case ARMOR_STAND:
					MCArmorStand stand = (MCArmorStand) entity;
					specArray.set(entity_spec.KEY_ARMORSTAND_ARMS, CBoolean.get(stand.hasArms()), t);
					specArray.set(entity_spec.KEY_ARMORSTAND_BASEPLATE, CBoolean.get(stand.hasBasePlate()), t);
					specArray.set(entity_spec.KEY_ARMORSTAND_GRAVITY, CBoolean.get(stand.hasGravity()), t);
					specArray.set(entity_spec.KEY_ARMORSTAND_MARKER, CBoolean.get(stand.isMarker()), t);
					specArray.set(entity_spec.KEY_ARMORSTAND_SMALLSIZE, CBoolean.get(stand.isSmall()), t);
					specArray.set(entity_spec.KEY_ARMORSTAND_VISIBLE, CBoolean.get(stand.isVisible()), t);
					CArray poses = CArray.GetAssociativeArray(t);
					Map<MCBodyPart, Vector3D> poseMap = stand.getAllPoses();
					for(MCBodyPart key : poseMap.keySet()) {
						poses.set("pose" + key.name(), ObjectGenerator.GetGenerator().vector(poseMap.get(key), t), t);
					}
					specArray.set(entity_spec.KEY_ARMORSTAND_POSES, poses, t);
					break;
				case AXOLOTL:
					MCAxolotl axolotl = (MCAxolotl) entity;
					specArray.set(entity_spec.KEY_AXOLOTL_TYPE, new CString(axolotl.getAxolotlType().name(), t), t);
					break;
				case BEE:
					MCBee bee = (MCBee) entity;
					specArray.set(entity_spec.KEY_BEE_ANGER, new CInt(bee.getAnger(), t), t);
					specArray.set(entity_spec.KEY_BEE_NECTAR, CBoolean.get(bee.hasNectar()), t);
					specArray.set(entity_spec.KEY_BEE_STUNG, CBoolean.get(bee.hasStung()), t);
					MCLocation flower = bee.getFlowerLocation();
					if(flower == null) {
						specArray.set(entity_spec.KEY_BEE_FLOWER_LOCATION, CNull.NULL, t);
					} else {
						specArray.set(entity_spec.KEY_BEE_FLOWER_LOCATION, ObjectGenerator.GetGenerator().location(flower), t);
					}
					MCLocation hive = bee.getHiveLocation();
					if(hive == null) {
						specArray.set(entity_spec.KEY_BEE_HIVE_LOCATION, CNull.NULL, t);
					} else {
						specArray.set(entity_spec.KEY_BEE_HIVE_LOCATION, ObjectGenerator.GetGenerator().location(hive), t);
					}
					break;
				case BLOCK_DISPLAY:
					MCBlockDisplay blockDisplay = (MCBlockDisplay) entity;
					specArray.set(KEY_DISPLAY_BLOCK, ObjectGenerator.GetGenerator().blockData(blockDisplay.getBlockData(), t), t);
					break;
				case BOAT:
					MCBoat boat = (MCBoat) entity;
					specArray.set(entity_spec.KEY_BOAT_TYPE, new CString(boat.getWoodType().name(), t), t);
					break;
				case CAT:
					MCCat cat = (MCCat) entity;
					specArray.set(entity_spec.KEY_CAT_TYPE, new CString(cat.getCatType().name(), t), t);
					specArray.set(entity_spec.KEY_GENERIC_SITTING, CBoolean.get(cat.isSitting()), t);
					specArray.set(entity_spec.KEY_CAT_COLOR, new CString(cat.getCollarColor().name(), t), t);
					break;
				case CREEPER:
					MCCreeper creeper = (MCCreeper) entity;
					specArray.set(entity_spec.KEY_CREEPER_POWERED, CBoolean.get(creeper.isPowered()), t);
					specArray.set(entity_spec.KEY_CREEPER_FUSETICKS, new CInt(creeper.getFuseTicks(), t), t);
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
					UUID owner = item.getOwner();
					if(owner == null) {
						specArray.set(entity_spec.KEY_DROPPED_ITEM_OWNER, CNull.NULL, t);
					} else {
						specArray.set(entity_spec.KEY_DROPPED_ITEM_OWNER, new CString(owner.toString(), t), t);
					}
					UUID thrower = item.getThrower();
					if(thrower == null) {
						specArray.set(entity_spec.KEY_DROPPED_ITEM_THROWER, CNull.NULL, t);
					} else {
						specArray.set(entity_spec.KEY_DROPPED_ITEM_THROWER, new CString(thrower.toString(), t), t);
					}
					if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_18_X)) {
						specArray.set(entity_spec.KEY_DROPPED_ITEM_DESPAWN, CBoolean.get(item.willDespawn()), t);
					} else {
						specArray.set(entity_spec.KEY_DROPPED_ITEM_DESPAWN, CBoolean.TRUE, t);
					}
					break;
				case ENDER_CRYSTAL:
					MCEnderCrystal endercrystal = (MCEnderCrystal) entity;
					specArray.set(entity_spec.KEY_ENDERCRYSTAL_BASE, CBoolean.get(endercrystal.isShowingBottom()), t);
					MCLocation location = endercrystal.getBeamTarget();
					if(location == null) {
						specArray.set(entity_spec.KEY_ENDERCRYSTAL_BEAMTARGET, CNull.NULL, t);
					} else {
						specArray.set(entity_spec.KEY_ENDERCRYSTAL_BEAMTARGET,
								ObjectGenerator.GetGenerator().location(location, false), t);
					}
					break;
				case ENDER_EYE:
					MCEnderSignal endereye = (MCEnderSignal) entity;
					specArray.set(entity_spec.KEY_ENDEREYE_DESPAWNTICKS, new CInt(endereye.getDespawnTicks(), t), t);
					specArray.set(entity_spec.KEY_ENDEREYE_DROP, CBoolean.get(endereye.getDropItem()), t);
					specArray.set(entity_spec.KEY_ENDEREYE_ITEM, ObjectGenerator.GetGenerator().item(endereye.getItem(), t), t);
					specArray.set(entity_spec.KEY_ENDEREYE_TARGET, ObjectGenerator.GetGenerator().location(endereye.getTargetLocation(), false), t);
					break;
				case ENDER_DRAGON:
					MCEnderDragon enderdragon = (MCEnderDragon) entity;
					specArray.set(entity_spec.KEY_ENDERDRAGON_PHASE, new CString(enderdragon.getPhase().name(), t), t);
					break;
				case ENDERMAN:
					MCEnderman enderman = (MCEnderman) entity;
					MCBlockData carried = enderman.getCarriedMaterial();
					specArray.set(entity_spec.KEY_ENDERMAN_CARRIED,
							(carried == null ? CNull.NULL : new CString(carried.getMaterial().getName(), t)), t);
					break;
				case EVOKER_FANGS:
					MCEvokerFangs fangs = (MCEvokerFangs) entity;
					MCLivingEntity fangsource = fangs.getOwner();
					if(fangsource == null) {
						specArray.set(entity_spec.KEY_EVOKERFANGS_SOURCE, CNull.NULL, t);
					} else {
						specArray.set(entity_spec.KEY_EVOKERFANGS_SOURCE, new CString(fangsource.getUniqueId().toString(), t), t);
					}
					break;
				case EXPERIENCE_ORB:
					MCExperienceOrb orb = (MCExperienceOrb) entity;
					specArray.set(entity_spec.KEY_EXPERIENCE_ORB_AMOUNT, new CInt(orb.getExperience(), t), t);
					break;
				case FALLING_BLOCK:
					MCFallingBlock block = (MCFallingBlock) entity;
					specArray.set(entity_spec.KEY_FALLING_BLOCK_BLOCK, new CString(block.getMaterial().getName(), t), t);
					specArray.set(entity_spec.KEY_FALLING_BLOCK_DROPITEM, CBoolean.get(block.getDropItem()), t);
					specArray.set(entity_spec.KEY_FALLING_BLOCK_DAMAGE, CBoolean.get(block.canHurtEntities()), t);
					break;
				case FIREBALL:
				case SMALL_FIREBALL:
					MCFireball ball = (MCFireball) entity;
					specArray.set(entity_spec.KEY_FIREBALL_DIRECTION, ObjectGenerator.GetGenerator().vector(ball.getDirection(), t), t);
					break;
				case FIREWORK:
					MCFirework firework = (MCFirework) entity;
					MCFireworkMeta fmeta = firework.getFireWorkMeta();
					specArray.set(entity_spec.KEY_FIREWORK_STRENGTH, new CInt(fmeta.getStrength(), t), t);
					CArray fe = new CArray(t);
					for(MCFireworkEffect effect : fmeta.getEffects()) {
						fe.push(ObjectGenerator.GetGenerator().fireworkEffect(effect, t), t);
					}
					specArray.set(entity_spec.KEY_FIREWORK_EFFECTS, fe, t);
					specArray.set(entity_spec.KEY_FIREWORK_ANGLED, CBoolean.get(firework.isShotAtAngle()), t);
					break;
				case FOX:
					MCFox fox = (MCFox) entity;
					specArray.set(entity_spec.KEY_GENERIC_SITTING, CBoolean.get(fox.isSitting()), t);
					specArray.set(entity_spec.KEY_FOX_CROUCHING, CBoolean.get(fox.isCrouching()), t);
					specArray.set(entity_spec.KEY_FOX_TYPE, fox.getVariant().name(), t);
					break;
				case FROG:
					MCFrog frog = (MCFrog) entity;
					specArray.set(entity_spec.KEY_FROG_TYPE, frog.getFrogType().name(), t);
					break;
				case GOAT:
					MCGoat goat = (MCGoat) entity;
					specArray.set(entity_spec.KEY_GOAT_SCREAMING, CBoolean.get(goat.isScreaming()), t);
					break;
				case HORSE:
					MCHorse horse = (MCHorse) entity;
					specArray.set(entity_spec.KEY_HORSE_COLOR, new CString(horse.getColor().name(), t), t);
					specArray.set(entity_spec.KEY_HORSE_STYLE, new CString(horse.getPattern().name(), t), t);
					specArray.set(entity_spec.KEY_HORSE_JUMP, new CDouble(horse.getJumpStrength(), t), t);
					specArray.set(entity_spec.KEY_HORSE_DOMESTICATION, new CInt(horse.getDomestication(), t), t);
					specArray.set(entity_spec.KEY_HORSE_MAXDOMESTICATION, new CInt(horse.getMaxDomestication(), t), t);
					specArray.set(entity_spec.KEY_HORSE_ARMOR, ObjectGenerator.GetGenerator().item(horse.getArmor(), t), t);
					specArray.set(entity_spec.KEY_HORSE_SADDLE, ObjectGenerator.GetGenerator().item(horse.getSaddle(), t), t);
					break;
				case INTERACTION:
					MCInteraction interaction = (MCInteraction) entity;
					specArray.set(entity_spec.KEY_INTERACTION_WIDTH, new CDouble(interaction.getWidth(), t), t);
					specArray.set(entity_spec.KEY_INTERACTION_HEIGHT, new CDouble(interaction.getHeight(), t), t);
					specArray.set(entity_spec.KEY_INTERACTION_RESPONSE, CBoolean.get(interaction.isResponsive()), t);
					MCPreviousInteraction attack = interaction.getLastAttack();
					if(attack != null) {
						CArray attackArray = CArray.GetAssociativeArray(t);
						attackArray.set("puuid", attack.getUuid().toString());
						attackArray.set("timestamp", new CInt(attack.getTimestamp(), t), t);
						specArray.set(entity_spec.KEY_INTERACTION_ATTACK, attackArray, t);
					} else {
						specArray.set(entity_spec.KEY_INTERACTION_ATTACK, CNull.NULL, t);
					}
					MCPreviousInteraction interact = interaction.getLastInteraction();
					if(interact != null) {
						CArray interactionArray = CArray.GetAssociativeArray(t);
						interactionArray.set("puuid", interact.getUuid().toString());
						interactionArray.set("timestamp", new CInt(interact.getTimestamp(), t), t);
						specArray.set(entity_spec.KEY_INTERACTION_INTERACTION, interactionArray, t);
					} else {
						specArray.set(entity_spec.KEY_INTERACTION_INTERACTION, CNull.NULL, t);
					}
					break;
				case IRON_GOLEM:
					MCIronGolem golem = (MCIronGolem) entity;
					specArray.set(entity_spec.KEY_IRON_GOLEM_PLAYERCREATED, CBoolean.get(golem.isPlayerCreated()), t);
					break;
				case ITEM_DISPLAY:
					MCItemDisplay itemDisplay = (MCItemDisplay) entity;
					specArray.set(KEY_DISPLAY_ITEM, ObjectGenerator.GetGenerator().item(itemDisplay.getItem(), t), t);
					specArray.set(KEY_DISPLAY_ITEM_DISPLAY, itemDisplay.getItemModelTransform().name(), t);
					break;
				case ITEM_FRAME:
				case GLOW_ITEM_FRAME:
					MCItemFrame frame = (MCItemFrame) entity;
					specArray.set(entity_spec.KEY_ITEM_FRAME_FIXED, CBoolean.get(frame.isFixed()), t);
					specArray.set(entity_spec.KEY_ITEM_FRAME_ITEM, ObjectGenerator.GetGenerator().item(frame.getItem(), t), t);
					specArray.set(entity_spec.KEY_ITEM_FRAME_ROTATION, new CString(frame.getRotation().name(), t), t);
					specArray.set(entity_spec.KEY_ITEM_FRAME_VISIBLE, CBoolean.get(frame.isVisible()), t);
					break;
				case LIGHTNING:
					MCLightningStrike lightning = (MCLightningStrike) entity;
					specArray.set(entity_spec.KEY_LIGHTNING_EFFECT, CBoolean.get(lightning.isEffect()), t);
					break;
				case LLAMA:
				case TRADER_LLAMA:
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
				case MINECART_FURNACE:
				case MINECART_HOPPER:
				case MINECART_MOB_SPAWNER:
				case MINECART_TNT:
					MCMinecart minecart = (MCMinecart) entity;
					specArray.set(entity_spec.KEY_MINECART_BLOCK, new CString(minecart.getDisplayBlock().getMaterial().getName(), t), t);
					specArray.set(entity_spec.KEY_MINECART_OFFSET, new CInt(minecart.getDisplayBlockOffset(), t), t);
					break;
				case MINECART_COMMAND:
					MCCommandMinecart commandminecart = (MCCommandMinecart) entity;
					specArray.set(entity_spec.KEY_MINECART_COMMAND_COMMAND, new CString(commandminecart.getCommand(), t), t);
					specArray.set(entity_spec.KEY_MINECART_COMMAND_CUSTOMNAME, new CString(commandminecart.getName(), t), t);
					specArray.set(entity_spec.KEY_MINECART_BLOCK, new CString(commandminecart.getDisplayBlock().getMaterial().getName(), t), t);
					specArray.set(entity_spec.KEY_MINECART_OFFSET, new CInt(commandminecart.getDisplayBlockOffset(), t), t);
					break;
				case MUSHROOM_COW:
					MCMushroomCow cow = (MCMushroomCow) entity;
					specArray.set(entity_spec.KEY_MUSHROOM_COW_TYPE, cow.getVariant().name(), t);
					break;
				case PAINTING:
					MCPainting painting = (MCPainting) entity;
					specArray.set(entity_spec.KEY_PAINTING_ART, new CString(painting.getArt().name(), t), t);
					break;
				case PANDA:
					MCPanda panda = (MCPanda) entity;
					specArray.set(entity_spec.KEY_PANDA_MAINGENE, new CString(panda.getMainGene().name(), t), t);
					specArray.set(entity_spec.KEY_PANDA_HIDDENGENE, new CString(panda.getHiddenGene().name(), t), t);
					if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_19)) {
						specArray.set(entity_spec.KEY_PANDA_EATING, CBoolean.get(panda.isEating()), t);
						specArray.set(entity_spec.KEY_PANDA_ONBACK, CBoolean.get(panda.isOnBack()), t);
						specArray.set(entity_spec.KEY_PANDA_ROLLING, CBoolean.get(panda.isRolling()), t);
						specArray.set(entity_spec.KEY_PANDA_SNEEZING, CBoolean.get(panda.isSneezing()), t);
					}
					break;
				case PARROT:
					MCParrot parrot = (MCParrot) entity;
					specArray.set(entity_spec.KEY_GENERIC_SITTING, CBoolean.get(parrot.isSitting()), t);
					specArray.set(entity_spec.KEY_PARROT_TYPE, new CString(parrot.getVariant().name(), t), t);
					break;
				case PHANTOM:
					MCPhantom phantom = (MCPhantom) entity;
					specArray.set(entity_spec.KEY_PHANTOM_SIZE, new CInt(phantom.getSize(), t), t);
					break;
				case PIG:
					MCPig pig = (MCPig) entity;
					specArray.set(entity_spec.KEY_STEERABLE_SADDLED, CBoolean.get(pig.isSaddled()), t);
					break;
				case PIGLIN:
					MCPiglin piglin = (MCPiglin) entity;
					specArray.set(entity_spec.KEY_GENERIC_BABY, CBoolean.get(piglin.isBaby()), t);
					specArray.set(entity_spec.KEY_PIGLIN_ZOMBIFICATION_IMMUNE, CBoolean.get(piglin.isImmuneToZombification()), t);
					break;
				case PRIMED_TNT:
					MCTNT tnt = (MCTNT) entity;
					specArray.set(entity_spec.KEY_PRIMED_TNT_FUSETICKS, new CInt(tnt.getFuseTicks(), t), t);
					MCEntity source = tnt.getSource();
					if(source != null) {
						specArray.set(entity_spec.KEY_PRIMED_TNT_SOURCE, new CString(source.getUniqueId().toString(), t), t);
					} else {
						specArray.set(entity_spec.KEY_PRIMED_TNT_SOURCE, CNull.NULL, t);
					}
					break;
				case PUFFERFISH:
					MCPufferfish puffer = (MCPufferfish) entity;
					specArray.set(entity_spec.KEY_PUFFERFISH_SIZE, new CInt(puffer.getPuffState(), t), t);
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
				case SHULKER:
					MCShulker shulker = (MCShulker) entity;
					specArray.set(entity_spec.KEY_SHULKER_COLOR, new CString(shulker.getColor().name(), t), t);
					break;
				case SHULKER_BULLET:
					MCShulkerBullet bullet = (MCShulkerBullet) entity;
					MCEntity target = bullet.getTarget();
					if(target == null) {
						specArray.set(entity_spec.KEY_SHULKERBULLET_TARGET, CNull.NULL, t);
					} else {
						specArray.set(entity_spec.KEY_SHULKERBULLET_TARGET, new CString(target.getUniqueId().toString(), t), t);
					}
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
					MCSnowman snowman = (MCSnowman) entity;
					specArray.set(entity_spec.KEY_SNOWMAN_DERP, CBoolean.GenerateCBoolean(snowman.isDerp(), t), t);
					break;
				case SPECTRAL_ARROW:
					MCSpectralArrow spectral = (MCSpectralArrow) entity;
					specArray.set(entity_spec.KEY_ARROW_CRITICAL, CBoolean.get(spectral.isCritical()), t);
					specArray.set(entity_spec.KEY_ARROW_KNOCKBACK, new CInt(spectral.getKnockbackStrength(), t), t);
					specArray.set(entity_spec.KEY_ARROW_DAMAGE, new CDouble(spectral.getDamage(), t), t);
					specArray.set(entity_spec.KEY_SPECTRAL_ARROW_GLOWING_TICKS, new CInt(spectral.getGlowingTicks(), t), t);
					break;
				case SPLASH_POTION:
					MCThrownPotion potion = (MCThrownPotion) entity;
					specArray.set(entity_spec.KEY_SPLASH_POTION_ITEM, ObjectGenerator.GetGenerator().item(potion.getItem(), t), t);
					break;
				case STRIDER:
					MCStrider strider = (MCStrider) entity;
					specArray.set(entity_spec.KEY_STEERABLE_SADDLED, CBoolean.get(strider.isSaddled()), t);
					break;
				case TEXT_DISPLAY:
					MCTextDisplay tDisplay = (MCTextDisplay) entity;
					specArray.set(entity_spec.KEY_DISPLAY_TEXT, tDisplay.getText());
					specArray.set(entity_spec.KEY_DISPLAY_TEXT_ALIGNMENT, tDisplay.getAlignment().name());
					specArray.set(entity_spec.KEY_DISPLAY_TEXT_LINE_WIDTH, new CInt(tDisplay.getLineWidth(), t), t);
					specArray.set(entity_spec.KEY_DISPLAY_TEXT_SEE_THROUGH, CBoolean.get(tDisplay.isVisibleThroughBlocks()), t);
					specArray.set(entity_spec.KEY_DISPLAY_TEXT_SHADOW, CBoolean.get(tDisplay.hasShadow()), t);
					MCColor color = tDisplay.getBackgroundColor();
					if(color == null) {
						specArray.set(entity_spec.KEY_DISPLAY_TEXT_BACKGROUND_COLOR, CNull.NULL, t);
					} else {
						specArray.set(entity_spec.KEY_DISPLAY_TEXT_BACKGROUND_COLOR,
								ObjectGenerator.GetGenerator().transparentColor(color, t), t);
					}
					long opacity = tDisplay.getOpacity();
					if(opacity < 0) {
						opacity += 256;
					}
					specArray.set(entity_spec.KEY_DISPLAY_TEXT_OPACITY, new CInt(opacity, t), t);
					break;
				case TRIDENT:
					MCTrident trident = (MCTrident) entity;
					specArray.set(entity_spec.KEY_ARROW_CRITICAL, CBoolean.get(trident.isCritical()), t);
					specArray.set(entity_spec.KEY_ARROW_KNOCKBACK, new CInt(trident.getKnockbackStrength(), t), t);
					specArray.set(entity_spec.KEY_ARROW_DAMAGE, new CDouble(trident.getDamage(), t), t);
					break;
				case TROPICAL_FISH:
					MCTropicalFish fish = (MCTropicalFish) entity;
					specArray.set(entity_spec.KEY_TROPICALFISH_COLOR, new CString(fish.getBodyColor().name(), t), t);
					specArray.set(entity_spec.KEY_TROPICALFISH_PATTERN, new CString(fish.getPattern().name(), t), t);
					specArray.set(entity_spec.KEY_TROPICALFISH_PATTERNCOLOR, new CString(fish.getPatternColor().name(), t), t);
					break;
				case VEX:
					MCVex vex = (MCVex) entity;
					specArray.set(entity_spec.KEY_VEX_CHARGING, CBoolean.get(vex.isCharging()), t);
					break;
				case VILLAGER:
					MCVillager villager = (MCVillager) entity;
					specArray.set(entity_spec.KEY_VILLAGER_PROFESSION, new CString(villager.getProfession().name(), t), t);
					specArray.set(entity_spec.KEY_VILLAGER_LEVEL, new CInt(villager.getLevel(), t), t);
					specArray.set(entity_spec.KEY_VILLAGER_EXPERIENCE, new CInt(villager.getExperience(), t), t);
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
					specArray.set(entity_spec.KEY_GENERIC_SITTING, CBoolean.get(wolf.isSitting()), t);
					if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_19)) {
						specArray.set(entity_spec.KEY_WOLF_INTERESTED, CBoolean.get(wolf.isInterested()), t);
						if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_20_6)) {
							specArray.set(entity_spec.KEY_WOLF_TYPE, new CString(wolf.getWolfVariant().name(), t), t);
						}
					}
					break;
				case ZOGLIN:
					MCZoglin zoglin = (MCZoglin) entity;
					specArray.set(entity_spec.KEY_GENERIC_BABY, CBoolean.get(zoglin.isBaby()), t);
					break;
				case ZOMBIE:
				case DROWNED:
				case HUSK:
					MCZombie zombie = (MCZombie) entity;
					specArray.set(entity_spec.KEY_GENERIC_BABY, CBoolean.get(zombie.isBaby()), t);
					if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_19)) {
						specArray.set(entity_spec.KEY_ZOMBIE_BREAK_DOORS, CBoolean.get(zombie.canBreakDoors()), t);
					}
					break;
				case ZOMBIE_VILLAGER:
					MCZombieVillager zombievillager = (MCZombieVillager) entity;
					specArray.set(entity_spec.KEY_GENERIC_BABY, CBoolean.get(zombievillager.isBaby()), t);
					specArray.set(entity_spec.KEY_VILLAGER_PROFESSION, new CString(zombievillager.getProfession().name(), t), t);
					if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_19)) {
						specArray.set(entity_spec.KEY_ZOMBIE_BREAK_DOORS, CBoolean.get(zombievillager.canBreakDoors()), t);
					}
					break;
				case ZOMBIFIED_PIGLIN:
					MCPigZombie pigZombie = (MCPigZombie) entity;
					specArray.set(entity_spec.KEY_ZOMBIFIED_PIGLIN_ANGRY, CBoolean.get(pigZombie.isAngry()), t);
					specArray.set(entity_spec.KEY_ZOMBIFIED_PIGLIN_ANGER, new CInt(pigZombie.getAnger(), t), t);
					specArray.set(entity_spec.KEY_GENERIC_BABY, CBoolean.get(pigZombie.isBaby()), t);
					if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_19)) {
						specArray.set(entity_spec.KEY_ZOMBIE_BREAK_DOORS, CBoolean.get(pigZombie.canBreakDoors()), t);
					}
					break;
			}
			return specArray;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		//used to ensure that the indexes are the same in entity_spec(), set_entity_spec(), and in the documentation.
		private static final String KEY_GENERIC_SITTING = "sitting";
		private static final String KEY_GENERIC_BABY = "baby";
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
		private static final String KEY_ARROW_DAMAGE = "damage";
		private static final String KEY_ARROW_PIERCE_LEVEL = "piercelevel";
		private static final String KEY_ARROW_PICKUP = "pickup";
		private static final String KEY_ARMORSTAND_ARMS = "arms";
		private static final String KEY_ARMORSTAND_BASEPLATE = "baseplate";
		private static final String KEY_ARMORSTAND_GRAVITY = "gravity";
		private static final String KEY_ARMORSTAND_MARKER = "marker";
		private static final String KEY_ARMORSTAND_POSES = "poses";
		private static final String KEY_ARMORSTAND_SMALLSIZE = "small";
		private static final String KEY_ARMORSTAND_VISIBLE = "visible";
		private static final String KEY_AXOLOTL_TYPE = "type";
		private static final String KEY_BEE_HIVE_LOCATION = "hivelocation";
		private static final String KEY_BEE_FLOWER_LOCATION = "flowerlocation";
		private static final String KEY_BEE_NECTAR = "nector";
		private static final String KEY_BEE_STUNG = "stung";
		private static final String KEY_BEE_ANGER = "anger";
		private static final String KEY_BOAT_TYPE = "type";
		private static final String KEY_CAT_TYPE = "type";
		private static final String KEY_CAT_COLOR = "color";
		private static final String KEY_CREEPER_POWERED = "powered";
		private static final String KEY_CREEPER_FUSETICKS = "fuseticks";
		private static final String KEY_CREEPER_MAXFUSETICKS = "maxfuseticks";
		private static final String KEY_CREEPER_EXPLOSIONRADIUS = "explosionradius";
		private static final String KEY_DISPLAY_BLOCK = "blockdata";
		private static final String KEY_DISPLAY_ITEM = "item";
		private static final String KEY_DISPLAY_ITEM_DISPLAY = "itemdisplay";
		private static final String KEY_DISPLAY_TEXT = "text";
		private static final String KEY_DISPLAY_TEXT_ALIGNMENT = "alignment";
		private static final String KEY_DISPLAY_TEXT_LINE_WIDTH = "linewidth";
		private static final String KEY_DISPLAY_TEXT_SEE_THROUGH = "seethrough";
		private static final String KEY_DISPLAY_TEXT_SHADOW = "shadow";
		private static final String KEY_DISPLAY_TEXT_BACKGROUND_COLOR = "bgcolor";
		private static final String KEY_DISPLAY_TEXT_OPACITY = "opacity";
		private static final String KEY_DROPPED_ITEM_ITEMSTACK = "itemstack";
		private static final String KEY_DROPPED_ITEM_PICKUPDELAY = "pickupdelay";
		private static final String KEY_DROPPED_ITEM_OWNER = "owner";
		private static final String KEY_DROPPED_ITEM_THROWER = "thrower";
		private static final String KEY_DROPPED_ITEM_DESPAWN = "despawn";
		private static final String KEY_ENDERCRYSTAL_BASE = "base";
		private static final String KEY_ENDERCRYSTAL_BEAMTARGET = "beamtarget";
		private static final String KEY_ENDEREYE_DESPAWNTICKS = "despawnticks";
		private static final String KEY_ENDEREYE_DROP = "drop";
		private static final String KEY_ENDEREYE_ITEM = "item";
		private static final String KEY_ENDEREYE_TARGET = "target";
		private static final String KEY_ENDERDRAGON_PHASE = "phase";
		private static final String KEY_ENDERMAN_CARRIED = "carried";
		private static final String KEY_EXPERIENCE_ORB_AMOUNT = "amount";
		private static final String KEY_EVOKERFANGS_SOURCE = "source";
		private static final String KEY_FALLING_BLOCK_BLOCK = "block";
		private static final String KEY_FALLING_BLOCK_DROPITEM = "dropitem";
		private static final String KEY_FALLING_BLOCK_DAMAGE = "damage";
		private static final String KEY_FIREBALL_DIRECTION = "direction";
		private static final String KEY_FIREWORK_STRENGTH = "strength";
		private static final String KEY_FIREWORK_EFFECTS = "effects";
		private static final String KEY_FIREWORK_ANGLED = "angled";
		private static final String KEY_FOX_CROUCHING = "crouching";
		private static final String KEY_FOX_TYPE = "type";
		private static final String KEY_FROG_TYPE = "type";
		private static final String KEY_GOAT_SCREAMING = "screaming";
		private static final String KEY_HORSE_COLOR = "color";
		private static final String KEY_HORSE_STYLE = "style";
		private static final String KEY_HORSE_CHEST = "chest";
		private static final String KEY_HORSE_JUMP = "jump";
		private static final String KEY_HORSE_DOMESTICATION = "domestication";
		private static final String KEY_HORSE_MAXDOMESTICATION = "maxdomestication";
		private static final String KEY_HORSE_ARMOR = "armor";
		private static final String KEY_HORSE_SADDLE = "saddle";
		private static final String KEY_INTERACTION_WIDTH = "width";
		private static final String KEY_INTERACTION_HEIGHT = "height";
		private static final String KEY_INTERACTION_RESPONSE = "response";
		private static final String KEY_INTERACTION_ATTACK = "lastattack";
		private static final String KEY_INTERACTION_INTERACTION = "lastinteraction";
		private static final String KEY_IRON_GOLEM_PLAYERCREATED = "playercreated";
		private static final String KEY_ITEM_FRAME_FIXED = "fixed";
		private static final String KEY_ITEM_FRAME_ITEM = "item";
		private static final String KEY_ITEM_FRAME_ROTATION = "rotation";
		private static final String KEY_ITEM_FRAME_VISIBLE = "visible";
		private static final String KEY_LIGHTNING_EFFECT = "effect";
		private static final String KEY_MINECART_BLOCK = "block";
		private static final String KEY_MINECART_OFFSET = "offset";
		private static final String KEY_MINECART_COMMAND_COMMAND = "command";
		private static final String KEY_MINECART_COMMAND_CUSTOMNAME = "customname";
		private static final String KEY_MUSHROOM_COW_TYPE = "type";
		private static final String KEY_PAINTING_ART = "type";
		private static final String KEY_PANDA_MAINGENE = "maingene";
		private static final String KEY_PANDA_HIDDENGENE = "hiddengene";
		private static final String KEY_PANDA_EATING = "eating";
		private static final String KEY_PANDA_ONBACK = "onback";
		private static final String KEY_PANDA_ROLLING = "rolling";
		private static final String KEY_PANDA_SNEEZING = "sneezing";
		private static final String KEY_PARROT_TYPE = "type";
		private static final String KEY_PHANTOM_SIZE = "size";
		private static final String KEY_PIGLIN_ZOMBIFICATION_IMMUNE = "zombificationimmune";
		private static final String KEY_PUFFERFISH_SIZE = "size";
		private static final String KEY_ZOMBIFIED_PIGLIN_ANGRY = "angry";
		private static final String KEY_ZOMBIFIED_PIGLIN_ANGER = "anger";
		private static final String KEY_RABBIT_TYPE = "type";
		private static final String KEY_PRIMED_TNT_FUSETICKS = "fuseticks";
		private static final String KEY_PRIMED_TNT_SOURCE = "source";
		private static final String KEY_SHEEP_COLOR = "color";
		private static final String KEY_SHEEP_SHEARED = "sheared";
		private static final String KEY_SHULKER_COLOR = "color";
		private static final String KEY_SHULKERBULLET_TARGET = "target";
		private static final String KEY_SLIME_SIZE = "size";
		private static final String KEY_SNOWMAN_DERP = "derp";
		private static final String KEY_SPECTRAL_ARROW_GLOWING_TICKS = "glowingticks";
		private static final String KEY_SPLASH_POTION_ITEM = "item";
		private static final String KEY_STEERABLE_SADDLED = "saddled";
		private static final String KEY_TIPPEDARROW_POTIONMETA = "potionmeta";
		private static final String KEY_TROPICALFISH_COLOR = "color";
		private static final String KEY_TROPICALFISH_PATTERN = "pattern";
		private static final String KEY_TROPICALFISH_PATTERNCOLOR = "patterncolor";
		private static final String KEY_VEX_CHARGING = "charging";
		private static final String KEY_VILLAGER_PROFESSION = "profession";
		private static final String KEY_VILLAGER_LEVEL = "level";
		private static final String KEY_VILLAGER_EXPERIENCE = "experience";
		private static final String KEY_WITHER_SKULL_CHARGED = "charged";
		private static final String KEY_WOLF_ANGRY = "angry";
		private static final String KEY_WOLF_COLOR = "color";
		private static final String KEY_WOLF_INTERESTED = "interested";
		private static final String KEY_WOLF_TYPE = "type";
		private static final String KEY_ZOMBIE_BREAK_DOORS = "breakdoors";
	}

	@api(environments = {CommandHelperEnvironment.class})
	@seealso(entity_spec.class)
	public static class set_entity_spec extends EntitySetterFunction {

		@Override
		public String getName() {
			return "set_entity_spec";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREBadEntityException.class, CREIndexOverflowException.class,
				CREIndexOverflowException.class, CRERangeException.class, CREFormatException.class,
				CRELengthException.class, CREInvalidWorldException.class};
		}

		@Override
		public String docs() {
			return "void {entityUUID, specArray} Sets the data in the specArray to the given entity."
					+ " The specArray must follow the same format as entity_spec()."
					+ " See the documentation for that function for info on available options."
					+ " All indices in the specArray are optional.";
		}

		private static void throwException(String index, Target t) throws ConfigRuntimeException {
			throw new CREIndexOverflowException("Unknown or uneditable specification: " + index, t);
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCEntity entity = Static.getEntity(args[0], t);
			CArray specArray = ArgumentValidation.getArray(args[1], t);

			switch(entity.getType().getAbstracted()) {
				case AREA_EFFECT_CLOUD:
					MCAreaEffectCloud cloud = (MCAreaEffectCloud) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_AREAEFFECTCLOUD_COLOR:
								Mixed colorMixed = specArray.get(index, t);
								if(colorMixed.isInstanceOf(CArray.TYPE)) {
									CArray color = (CArray) colorMixed;
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
								Mixed particleMixed = specArray.get(index, t);
								if(particleMixed.isInstanceOf(CArray.TYPE)) {
									CArray pa = (CArray) particleMixed;
									MCParticle p;
									try {
										p = MCParticle.valueOf(pa.get("particle", t).val().toUpperCase());
									} catch(IllegalArgumentException ex) {
										throw new CREIllegalArgumentException("Particle name '"
												+ pa.get("particle", t).val() + "' is invalid.", t);
									}
									try {
										cloud.setParticle(p, ObjectGenerator.GetGenerator().particleData(p,
												cloud.getLocation(), pa, t));
									} catch(IllegalArgumentException ex) {
										throw new CREFormatException("Invalid particle data for " + p.name(), t);
									}
								} else {
									String particleName = particleMixed.val();
									try {
										cloud.setParticle(MCParticle.valueOf(particleName), null);
									} catch(IllegalArgumentException ex) {
										throw new CREFormatException("Invalid particle data: " + particleName, t);
									}
								}
								break;
							case entity_spec.KEY_AREAEFFECTCLOUD_POTIONMETA:
								Mixed c = specArray.get(index, t);
								if(c.isInstanceOf(CArray.TYPE)) {
									CArray meta = (CArray) c;
									if(meta.containsKey("potiontype")
											&& Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_20_6)) {
										Mixed potiontype = meta.get("potiontype", t);
										if(potiontype instanceof CNull) {
											cloud.setBasePotionType(null);
										} else {
											cloud.setBasePotionType(MCPotionType.valueOf(potiontype.val()));
										}
									} else if(meta.containsKey("base")) {
										Mixed base = meta.get("base", t);
										if(base.isInstanceOf(CArray.TYPE)) {
											if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_20_6)) {
												MCPotionType type = ObjectGenerator.GetGenerator().legacyPotionData((CArray) base, t);
												cloud.setBasePotionType(type);
											} else {
												MCPotionData pd = ObjectGenerator.GetGenerator().potionData((CArray) base, t);
												cloud.setBasePotionData(pd);
											}
										}
									}
									if(meta.containsKey("potions")) {
										cloud.clearCustomEffects();
										Mixed potions = meta.get("potions", t);
										if(potions.isInstanceOf(CArray.TYPE)) {
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
								Mixed cloudSource = specArray.get(index, t);
								if(cloudSource instanceof CNull) {
									cloud.setSource(null);
								} else if(cloudSource.isInstanceOf(CArray.TYPE)) {
									MCBlock b = ObjectGenerator.GetGenerator().location(cloudSource, cloud.getWorld(), t).getBlock();
									if(b.isDispenser()) {
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
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_ARROW_CRITICAL:
								arrow.setCritical(ArgumentValidation.getBoolean(specArray.get(index, t), t));
								break;
							case entity_spec.KEY_ARROW_KNOCKBACK:
								int k = ArgumentValidation.getInt32(specArray.get(index, t), t);
								if(k < 0) {
									throw new CRERangeException("Knockback can not be negative.", t);
								} else {
									arrow.setKnockbackStrength(k);
								}
								break;
							case entity_spec.KEY_ARROW_DAMAGE:
								double d = ArgumentValidation.getDouble(specArray.get(index, t), t);
								if(d < 0) {
									throw new CRERangeException("Damage cannot be negative.", t);
								}
								arrow.setDamage(d);
								break;
							case entity_spec.KEY_ARROW_PIERCE_LEVEL:
								int level = ArgumentValidation.getInt32(specArray.get(index, t), t);
								if(level < 0 || level > 127) {
									throw new CRERangeException("Pierce level must be 0 to 127.", t);
								}
								arrow.setPierceLevel(level);
								break;
							case entity_spec.KEY_ARROW_PICKUP:
								try {
									arrow.setPickupStatus(MCArrow.PickupStatus.valueOf(specArray.get(index, t).val()));
								} catch(IllegalArgumentException ex) {
									throw new CREFormatException("Invalid arrow pickup status: "
											+ specArray.get(index, t).val(), t);
								}
								break;
							case entity_spec.KEY_TIPPEDARROW_POTIONMETA:
								Mixed c = specArray.get(index, t);
								if(c.isInstanceOf(CArray.TYPE)) {
									CArray meta = (CArray) c;
									if(meta.containsKey("potiontype")
											&& Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_20_6)) {
										Mixed potiontype = meta.get("potiontype", t);
										if(potiontype instanceof CNull) {
											arrow.setBasePotionType(null);
										} else {
											arrow.setBasePotionType(MCPotionType.valueOf(potiontype.val()));
										}
									} else if(meta.containsKey("base")) {
										Mixed base = meta.get("base", t);
										if(base.isInstanceOf(CArray.TYPE)) {
											if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_20_6)) {
												MCPotionType type = ObjectGenerator.GetGenerator().legacyPotionData((CArray) base, t);
												arrow.setBasePotionType(type);
											} else {
												MCPotionData pd = ObjectGenerator.GetGenerator().potionData((CArray) base, t);
												arrow.setBasePotionData(pd);
											}
										}
									}
									if(meta.containsKey("potions")) {
										arrow.clearCustomEffects();
										Mixed potions = meta.get("potions", t);
										if(potions.isInstanceOf(CArray.TYPE)) {
											List<MCLivingEntity.MCEffect> list = ObjectGenerator.GetGenerator().potions((CArray) potions, t);
											for(MCLivingEntity.MCEffect effect : list) {
												arrow.addCustomEffect(effect);
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
				case ARMOR_STAND:
					MCArmorStand stand = (MCArmorStand) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_ARMORSTAND_ARMS:
								stand.setHasArms(ArgumentValidation.getBoolean(specArray.get(index, t), t));
								break;
							case entity_spec.KEY_ARMORSTAND_BASEPLATE:
								stand.setHasBasePlate(ArgumentValidation.getBoolean(specArray.get(index, t), t));
								break;
							case entity_spec.KEY_ARMORSTAND_GRAVITY:
								stand.setHasGravity(ArgumentValidation.getBoolean(specArray.get(index, t), t));
								break;
							case entity_spec.KEY_ARMORSTAND_MARKER:
								stand.setMarker(ArgumentValidation.getBoolean(specArray.get(index, t), t));
								break;
							case entity_spec.KEY_ARMORSTAND_SMALLSIZE:
								stand.setSmall(ArgumentValidation.getBoolean(specArray.get(index, t), t));
								break;
							case entity_spec.KEY_ARMORSTAND_VISIBLE:
								stand.setVisible(ArgumentValidation.getBoolean(specArray.get(index, t), t));
								break;
							case entity_spec.KEY_ARMORSTAND_POSES:
								Map<MCBodyPart, Vector3D> poseMap = stand.getAllPoses();
								Mixed posesMixed = specArray.get(index, t);
								if(posesMixed.isInstanceOf(CArray.TYPE)) {
									CArray poseArray = (CArray) posesMixed;
									for(MCBodyPart key : poseMap.keySet()) {
										try {
											poseMap.put(key, ObjectGenerator.GetGenerator().vector(poseMap.get(key),
													poseArray.get("pose" + key.name(), t), t));
										} catch(ConfigRuntimeException cre) {
											// Ignore, this just means the user didn't modify a body part
										}
									}
								}
								if(posesMixed instanceof CNull) {
									for(MCBodyPart key : poseMap.keySet()) {
										poseMap.put(key, Vector3D.ZERO);
									}
								}
								stand.setAllPoses(poseMap);
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case AXOLOTL:
					MCAxolotl axolotl = (MCAxolotl) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_AXOLOTL_TYPE:
								try {
									axolotl.setAxolotlType(MCAxolotlType.valueOf(specArray.get(index, t).val().toUpperCase()));
								} catch(IllegalArgumentException exception) {
									throw new CREFormatException("Invalid axolotl type: " + specArray.get(index, t).val(), t);
								}
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case BEE:
					MCBee bee = (MCBee) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_BEE_ANGER:
								bee.setAnger(ArgumentValidation.getInt32(specArray.get(index, t), t));
								break;
							case entity_spec.KEY_BEE_NECTAR:
								bee.setHasNectar(ArgumentValidation.getBooleanObject(specArray.get(index, t), t));
								break;
							case entity_spec.KEY_BEE_STUNG:
								bee.setHasStung(ArgumentValidation.getBooleanObject(specArray.get(index, t), t));
								break;
							case entity_spec.KEY_BEE_FLOWER_LOCATION:
								Mixed flower = specArray.get(index, t);
								if(flower instanceof CNull) {
									bee.setFlowerLocation(null);
								} else {
									bee.setFlowerLocation(ObjectGenerator.GetGenerator().location(flower, null, t));
								}
								break;
							case entity_spec.KEY_BEE_HIVE_LOCATION:
								Mixed hive = specArray.get(index, t);
								if(hive instanceof CNull) {
									bee.setHiveLocation(null);
								} else {
									bee.setHiveLocation(ObjectGenerator.GetGenerator().location(hive, null, t));
								}
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case BLOCK_DISPLAY:
					MCBlockDisplay bDisplay = (MCBlockDisplay) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_DISPLAY_BLOCK:
								MCBlockData bd;
								Mixed m = specArray.get(index, t);
								try {
									if(m.isInstanceOf(CArray.TYPE)) {
										bd = ObjectGenerator.GetGenerator().blockData((CArray) m, t);
									} else {
										bd = Static.getServer().createBlockData(m.val().toLowerCase());
									}
								} catch(IllegalArgumentException ex) {
									throw new CREFormatException("Cannot create block data from: " + m.val(), t);
								}
								bDisplay.setBlockData(bd);
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case BOAT:
					MCBoat boat = (MCBoat) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_BOAT_TYPE:
								try {
									boat.setWoodType(MCTreeSpecies.valueOf(specArray.get(index, t).val().toUpperCase()));
								} catch(IllegalArgumentException ex) {
									throw new CREFormatException("Invalid boat type: " + specArray.get(index, t).val(), t);
								}
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case CAT:
					MCCat cat = (MCCat) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_CAT_TYPE:
								try {
									cat.setCatType(MCCatType.valueOf(specArray.get(index, t).val().toUpperCase()));
								} catch(IllegalArgumentException exception) {
									throw new CREFormatException("Invalid cat type: " + specArray.get(index, t).val(), t);
								}
								break;
							case entity_spec.KEY_GENERIC_SITTING:
								cat.setSitting(ArgumentValidation.getBoolean(specArray.get(index, t), t));
								break;
							case entity_spec.KEY_CAT_COLOR:
								try {
									cat.setCollarColor(MCDyeColor.valueOf(specArray.get(index, t).val().toUpperCase()));
								} catch(IllegalArgumentException exception) {
									throw new CREFormatException("Invalid collar color: " + specArray.get(index, t).val(), t);
								}
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case CREEPER:
					MCCreeper creeper = (MCCreeper) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_CREEPER_POWERED:
								creeper.setPowered(ArgumentValidation.getBoolean(specArray.get(index, t), t));
								break;
							case entity_spec.KEY_CREEPER_MAXFUSETICKS:
								try {
									creeper.setMaxFuseTicks(ArgumentValidation.getInt32(specArray.get(index, t), t));
								} catch(IllegalArgumentException ex) {
									throw new CRERangeException("Ticks must not be negative.", t);
								}
								break;
							case entity_spec.KEY_CREEPER_FUSETICKS:
								try {
									creeper.setFuseTicks(ArgumentValidation.getInt32(specArray.get(index, t), t));
								} catch(IllegalArgumentException ex) {
									throw new CRERangeException(ex.getMessage(), t);
								}
								break;
							case entity_spec.KEY_CREEPER_EXPLOSIONRADIUS:
								try {
									creeper.setExplosionRadius(ArgumentValidation.getInt32(specArray.get(index, t), t));
								} catch(IllegalArgumentException ex) {
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
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_HORSE_CHEST:
								chestedhorse.setHasChest(ArgumentValidation.getBoolean(specArray.get(index, t), t));
								break;
							case entity_spec.KEY_HORSE_JUMP:
								try {
									chestedhorse.setJumpStrength(ArgumentValidation.getDouble(specArray.get(index, t), t));
								} catch(IllegalArgumentException exception) {
									throw new CRERangeException("The jump strength must be between 0.0 and 2.0", t);
								}
								break;
							case entity_spec.KEY_HORSE_DOMESTICATION:
								try {
									chestedhorse.setDomestication(ArgumentValidation.getInt32(specArray.get(index, t), t));
								} catch(IllegalArgumentException exception) {
									throw new CRERangeException("The domestication level can not be higher than the max domestication level.", t);
								}
								break;
							case entity_spec.KEY_HORSE_MAXDOMESTICATION:
								chestedhorse.setMaxDomestication(ArgumentValidation.getInt32(specArray.get(index, t), t));
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
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_DROPPED_ITEM_ITEMSTACK:
								item.setItemStack(ObjectGenerator.GetGenerator().item(specArray.get(index, t), t));
								break;
							case entity_spec.KEY_DROPPED_ITEM_PICKUPDELAY:
								item.setPickupDelay(ArgumentValidation.getInt32(specArray.get(index, t), t));
								break;
							case entity_spec.KEY_DROPPED_ITEM_OWNER:
								Mixed owner = specArray.get(index, t);
								if(owner instanceof CNull) {
									item.setOwner(null);
								} else {
									item.setOwner(Static.GetUUID(owner, t));
								}
								break;
							case entity_spec.KEY_DROPPED_ITEM_THROWER:
								Mixed thrower = specArray.get(index, t);
								if(thrower instanceof CNull) {
									item.setThrower(null);
								} else {
									item.setThrower(Static.GetUUID(thrower, t));
								}
								break;
							case entity_spec.KEY_DROPPED_ITEM_DESPAWN:
								if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_18_X)) {
									item.setWillDespawn(ArgumentValidation.getBooleanObject(specArray.get(index, t), t));
								}
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case ENDER_CRYSTAL:
					MCEnderCrystal endercrystal = (MCEnderCrystal) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_ENDERCRYSTAL_BASE:
								endercrystal.setShowingBottom(ArgumentValidation.getBoolean(specArray.get(index, t), t));
								break;
							case entity_spec.KEY_ENDERCRYSTAL_BEAMTARGET:
								Mixed c = specArray.get(index, t);
								if(c instanceof CNull) {
									endercrystal.setBeamTarget(null);
								} else if(c.isInstanceOf(CArray.TYPE)) {
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
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_ENDERDRAGON_PHASE:
								try {
									enderdragon.setPhase(MCEnderDragonPhase.valueOf(specArray.get(index, t).val().toUpperCase()));
								} catch(IllegalArgumentException ex) {
									throw new CREFormatException("Invalid EnderDragon phase: " + specArray.get(index, t).val(), t);
								}
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case ENDER_EYE:
					MCEnderSignal endereye = (MCEnderSignal) entity;
					// Order matters here. Target must be set first or it will reset despawn ticks and drop.
					if(specArray.containsKey(entity_spec.KEY_ENDEREYE_TARGET)) {
						Mixed targetLoc = specArray.get(entity_spec.KEY_ENDEREYE_TARGET, t);
						try {
							endereye.setTargetLocation(ObjectGenerator.GetGenerator().location(targetLoc, null, t));
						} catch(IllegalArgumentException ex) {
							throw new CREInvalidWorldException("An EnderEye cannot target a location in another world.", t);
						}
					}
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_ENDEREYE_DESPAWNTICKS:
								endereye.setDespawnTicks(ArgumentValidation.getInt32(specArray.get(index, t), t));
								break;
							case entity_spec.KEY_ENDEREYE_DROP:
								endereye.setDropItem(ArgumentValidation.getBoolean(specArray.get(index, t), t));
								break;
							case entity_spec.KEY_ENDEREYE_ITEM:
								Mixed enderItem = specArray.get(index, t);
								if(enderItem instanceof CNull) {
									endereye.setItem(null);
								} else {
									endereye.setItem(ObjectGenerator.GetGenerator().item(enderItem, t));
								}
								break;
							case entity_spec.KEY_ENDEREYE_TARGET:
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case ENDERMAN:
					MCEnderman enderman = (MCEnderman) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_ENDERMAN_CARRIED:
								MCMaterial mat = ObjectGenerator.GetGenerator().material(specArray.get(index, t), t);
								enderman.setCarriedMaterial(mat.createBlockData());
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case EVOKER_FANGS:
					MCEvokerFangs fangs = (MCEvokerFangs) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_EVOKERFANGS_SOURCE:
								Mixed source = specArray.get(index, t);
								if(source instanceof CNull) {
									fangs.setOwner(null);
								} else {
									fangs.setOwner(Static.getLivingEntity(source, t));
								}
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case EXPERIENCE_ORB:
					MCExperienceOrb orb = (MCExperienceOrb) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_EXPERIENCE_ORB_AMOUNT:
								orb.setExperience(ArgumentValidation.getInt32(specArray.get(index, t), t));
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case FALLING_BLOCK:
					MCFallingBlock block = (MCFallingBlock) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_FALLING_BLOCK_DROPITEM:
								block.setDropItem(ArgumentValidation.getBoolean(specArray.get(index, t), t));
								break;
							case entity_spec.KEY_FALLING_BLOCK_DAMAGE:
								block.setHurtEntities(ArgumentValidation.getBoolean(specArray.get(index, t), t));
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case FIREBALL:
				case DRAGON_FIREBALL:
				case SMALL_FIREBALL:
					MCFireball ball = (MCFireball) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_FIREBALL_DIRECTION:
								ball.setDirection(ObjectGenerator.GetGenerator().vector(specArray.get(index, t), t));
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case FIREWORK:
					MCFirework firework = (MCFirework) entity;
					MCFireworkMeta fm = firework.getFireWorkMeta();
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_FIREWORK_STRENGTH:
								fm.setStrength(ArgumentValidation.getInt32(specArray.get(index, t), t));
								break;
							case entity_spec.KEY_FIREWORK_EFFECTS:
								fm.clearEffects();
								CArray effects = ArgumentValidation.getArray(specArray.get(index, t), t);
								for(Mixed eff : effects.asList()) {
									if(eff.isInstanceOf(CArray.TYPE)) {
										fm.addEffect(ObjectGenerator.GetGenerator().fireworkEffect((CArray) eff, t));
									} else {
										throw new CRECastException("Firework effect expected to be an array.", t);
									}
								}
								break;
							case entity_spec.KEY_FIREWORK_ANGLED:
								firework.setShotAtAngle(ArgumentValidation.getBooleanObject(specArray.get(index, t), t));
								break;
							default:
								throwException(index, t);
						}
					}
					firework.setFireWorkMeta(fm);
					break;
				case FOX:
					MCFox fox = (MCFox) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_GENERIC_SITTING:
								fox.setSitting(ArgumentValidation.getBoolean(specArray.get(index, t), t));
								break;
							case entity_spec.KEY_FOX_CROUCHING:
								fox.setCrouching(ArgumentValidation.getBoolean(specArray.get(index, t), t));
								break;
							case entity_spec.KEY_FOX_TYPE:
								try {
									fox.setVariant(MCFoxType.valueOf(specArray.get(index, t).val().toUpperCase()));
								} catch(IllegalArgumentException exception) {
									throw new CREFormatException("Invalid fox type: " + specArray.get(index, t).val(), t);
								}
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case FROG:
					MCFrog frog = (MCFrog) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_FROG_TYPE:
								try {
									frog.setFrogType(MCFrogType.valueOf(specArray.get(index, t).val().toUpperCase()));
								} catch(IllegalArgumentException exception) {
									throw new CREFormatException("Invalid frog type: " + specArray.get(index, t).val(), t);
								}
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case GOAT:
					MCGoat goat = (MCGoat) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_GOAT_SCREAMING:
								goat.setScreaming(ArgumentValidation.getBooleanObject(specArray.get(index, t), t));
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case HORSE:
					MCHorse horse = (MCHorse) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_HORSE_COLOR:
								try {
									horse.setColor(MCHorseColor.valueOf(specArray.get(index, t).val().toUpperCase()));
								} catch(IllegalArgumentException exception) {
									throw new CREFormatException("Invalid horse color: " + specArray.get(index, t).val(), t);
								}
								break;
							case entity_spec.KEY_HORSE_STYLE:
								try {
									horse.setPattern(MCHorsePattern.valueOf(specArray.get(index, t).val().toUpperCase()));
								} catch(IllegalArgumentException exception) {
									throw new CREFormatException("Invalid horse style: " + specArray.get(index, t).val(), t);
								}
								break;
							case entity_spec.KEY_HORSE_JUMP:
								try {
									horse.setJumpStrength(ArgumentValidation.getDouble(specArray.get(index, t), t));
								} catch(IllegalArgumentException exception) {
									throw new CRERangeException("The jump strength must be between 0.0 and 2.0", t);
								}
								break;
							case entity_spec.KEY_HORSE_DOMESTICATION:
								try {
									horse.setDomestication(ArgumentValidation.getInt32(specArray.get(index, t), t));
								} catch(IllegalArgumentException exception) {
									throw new CRERangeException("The domestication level can not be higher than the max domestication level.", t);
								}
								break;
							case entity_spec.KEY_HORSE_MAXDOMESTICATION:
								horse.setMaxDomestication(ArgumentValidation.getInt32(specArray.get(index, t), t));
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
				case INTERACTION:
					MCInteraction interaction = (MCInteraction) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_INTERACTION_HEIGHT:
								interaction.setHeight(ArgumentValidation.getDouble(specArray.get(index, t), t));
								break;
							case entity_spec.KEY_INTERACTION_WIDTH:
								interaction.setWidth(ArgumentValidation.getDouble(specArray.get(index, t), t));
								break;
							case entity_spec.KEY_INTERACTION_RESPONSE:
								interaction.setResponsive(ArgumentValidation.getBooleanish(specArray.get(index, t), t));
								break;
							case entity_spec.KEY_INTERACTION_ATTACK:
							case entity_spec.KEY_INTERACTION_INTERACTION:
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case IRON_GOLEM:
					MCIronGolem golem = (MCIronGolem) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_IRON_GOLEM_PLAYERCREATED:
								golem.setPlayerCreated(ArgumentValidation.getBoolean(specArray.get(index, t), t));
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case ITEM_DISPLAY:
					MCItemDisplay itemDisplay = (MCItemDisplay) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_DISPLAY_ITEM:
								itemDisplay.setItem(ObjectGenerator.GetGenerator().item(specArray.get(index, t), t));
								break;
							case entity_spec.KEY_DISPLAY_ITEM_DISPLAY:
								try {
									itemDisplay.setItemModelTransform(ModelTransform.valueOf(specArray.get(index, t).val()));
								} catch(IllegalArgumentException ex) {
									throw new CREFormatException("Invalid display item model transform: "
											+ specArray.get(index, t).val(), t);
								}
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case ITEM_FRAME:
				case GLOW_ITEM_FRAME:
					MCItemFrame frame = (MCItemFrame) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_ITEM_FRAME_FIXED:
								frame.setFixed(ArgumentValidation.getBoolean(specArray.get(index, t), t));
								break;
							case entity_spec.KEY_ITEM_FRAME_ITEM:
								frame.setItem(ObjectGenerator.GetGenerator().item(specArray.get(index, t), t));
								break;
							case entity_spec.KEY_ITEM_FRAME_ROTATION:
								try {
									frame.setRotation(MCRotation.valueOf(specArray.get(index, t).val().toUpperCase()));
								} catch(IllegalArgumentException exception) {
									throw new CREFormatException("Invalid rotation type: " + specArray.get(index, t).val(), t);
								}
								break;
							case entity_spec.KEY_ITEM_FRAME_VISIBLE:
								frame.setVisible(ArgumentValidation.getBoolean(specArray.get(index, t), t));
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case LLAMA:
					MCLlama llama = (MCLlama) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_HORSE_COLOR:
								try {
									llama.setLlamaColor(MCLlamaColor.valueOf(specArray.get(index, t).val().toUpperCase()));
								} catch(IllegalArgumentException exception) {
									throw new CREFormatException("Invalid llama color: " + specArray.get(index, t).val(), t);
								}
								break;
							case entity_spec.KEY_HORSE_CHEST:
								llama.setHasChest(ArgumentValidation.getBoolean(specArray.get(index, t), t));
								break;
							case entity_spec.KEY_HORSE_DOMESTICATION:
								try {
									llama.setDomestication(ArgumentValidation.getInt32(specArray.get(index, t), t));
								} catch(IllegalArgumentException exception) {
									throw new CRERangeException("The domestication level can not be higher than the max domestication level.", t);
								}
								break;
							case entity_spec.KEY_HORSE_MAXDOMESTICATION:
								llama.setMaxDomestication(ArgumentValidation.getInt32(specArray.get(index, t), t));
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
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_SLIME_SIZE:
								cube.setSize(ArgumentValidation.getInt32(specArray.get(index, t), t));
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case MINECART:
				case MINECART_FURNACE:
				case MINECART_HOPPER:
				case MINECART_MOB_SPAWNER:
				case MINECART_TNT:
					MCMinecart minecart = (MCMinecart) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_MINECART_BLOCK:
								MCMaterial mat = ObjectGenerator.GetGenerator().material(specArray.get(index, t), t);
								minecart.setDisplayBlock(mat.createBlockData());
								break;
							case entity_spec.KEY_MINECART_OFFSET:
								minecart.setDisplayBlockOffset(ArgumentValidation.getInt32(specArray.get(index, t), t));
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case MINECART_COMMAND:
					MCCommandMinecart commandminecart = (MCCommandMinecart) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_MINECART_COMMAND_CUSTOMNAME:
								Mixed customName = specArray.get(index, t);
								if(customName instanceof CNull) {
									commandminecart.setName(null);
								} else {
									commandminecart.setName(customName.val());
								}
								break;
							case entity_spec.KEY_MINECART_COMMAND_COMMAND:
								Mixed command = specArray.get(index, t);
								if(command instanceof CNull) {
									commandminecart.setCommand(null);
								} else {
									commandminecart.setCommand(command.val());
								}
								break;
							case entity_spec.KEY_MINECART_BLOCK:
								MCMaterial mat = ObjectGenerator.GetGenerator().material(specArray.get(index, t), t);
								commandminecart.setDisplayBlock(mat.createBlockData());
								break;
							case entity_spec.KEY_MINECART_OFFSET:
								commandminecart.setDisplayBlockOffset(ArgumentValidation.getInt32(specArray.get(index, t), t));
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case MUSHROOM_COW:
					MCMushroomCow cow = (MCMushroomCow) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_MUSHROOM_COW_TYPE:
								try {
									cow.setVariant(MCMushroomCowType.valueOf(specArray.get(index, t).val().toUpperCase()));
								} catch(IllegalArgumentException exception) {
									throw new CREFormatException("Invalid mushroom cow type: " + specArray.get(index, t).val(), t);
								}
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case PAINTING:
					MCPainting painting = (MCPainting) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_PAINTING_ART:
								try {
									painting.setArt(MCArt.valueOf(specArray.get(index, t).val().toUpperCase()));
								} catch(IllegalArgumentException exception) {
									throw new CREFormatException("Invalid art type: " + specArray.get(index, t).val(), t);
								}
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case PANDA:
					MCPanda panda = (MCPanda) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_PANDA_MAINGENE:
								try {
									panda.setMainGene(MCPanda.Gene.valueOf(specArray.get(index, t).val().toUpperCase()));
								} catch(IllegalArgumentException exception) {
									throw new CREFormatException("Invalid panda gene: " + specArray.get(index, t).val(), t);
								}
								break;
							case entity_spec.KEY_PANDA_HIDDENGENE:
								try {
									panda.setHiddenGene(MCPanda.Gene.valueOf(specArray.get(index, t).val().toUpperCase()));
								} catch(IllegalArgumentException exception) {
									throw new CREFormatException("Invalid panda gene: " + specArray.get(index, t).val(), t);
								}
								break;
							case entity_spec.KEY_PANDA_EATING:
								panda.setEating(ArgumentValidation.getBooleanObject(specArray.get(index, t), t));
								break;
							case entity_spec.KEY_PANDA_ONBACK:
								panda.setOnBack(ArgumentValidation.getBooleanObject(specArray.get(index, t), t));
								break;
							case entity_spec.KEY_PANDA_ROLLING:
								panda.setRolling(ArgumentValidation.getBooleanObject(specArray.get(index, t), t));
								break;
							case entity_spec.KEY_PANDA_SNEEZING:
								panda.setSneezing(ArgumentValidation.getBooleanObject(specArray.get(index, t), t));
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case PARROT:
					MCParrot parrot = (MCParrot) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_GENERIC_SITTING:
								parrot.setSitting(ArgumentValidation.getBoolean(specArray.get(index, t), t));
								break;
							case entity_spec.KEY_PARROT_TYPE:
								try {
									parrot.setVariant(MCParrotType.valueOf(specArray.get(index, t).val().toUpperCase()));
								} catch(IllegalArgumentException exception) {
									throw new CREFormatException("Invalid parrot type: " + specArray.get(index, t).val(), t);
								}
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case PHANTOM:
					MCPhantom phantom = (MCPhantom) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_PHANTOM_SIZE:
								phantom.setSize(ArgumentValidation.getInt32(specArray.get(index, t), t));
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case PIG:
					MCPig pig = (MCPig) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_STEERABLE_SADDLED:
								pig.setSaddled(ArgumentValidation.getBoolean(specArray.get(index, t), t));
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case PIGLIN:
					MCPiglin piglin = (MCPiglin) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_GENERIC_BABY:
								if(ArgumentValidation.getBooleanObject(specArray.get(index, t), t)) {
									piglin.setBaby();
								} else {
									piglin.setAdult();
								}
								break;
							case entity_spec.KEY_PIGLIN_ZOMBIFICATION_IMMUNE:
								piglin.setImmuneToZombification(ArgumentValidation.getBooleanObject(specArray.get(index, t), t));
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case PRIMED_TNT:
					MCTNT tnt = (MCTNT) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_PRIMED_TNT_FUSETICKS:
								tnt.setFuseTicks(ArgumentValidation.getInt32(specArray.get(index, t), t));
								break;
							case entity_spec.KEY_PRIMED_TNT_SOURCE:
								Mixed source = specArray.get(index, t);
								if(source instanceof CNull) {
									tnt.setSource(null);
								} else {
									tnt.setSource(Static.getLivingEntity(specArray.get(index, t), t));
								}
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case RABBIT:
					MCRabbit rabbit = (MCRabbit) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_RABBIT_TYPE:
								try {
									rabbit.setRabbitType(MCRabbitType.valueOf(specArray.get(index, t).val().toUpperCase()));
								} catch(IllegalArgumentException exception) {
									throw new CREFormatException("Invalid rabbit type: " + specArray.get(index, t).val(), t);
								}
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case PUFFERFISH:
					MCPufferfish puffer = (MCPufferfish) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_PUFFERFISH_SIZE:
								puffer.setPuffState(ArgumentValidation.getInt32(specArray.get(index, t), t));
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case SHEEP:
					MCSheep sheep = (MCSheep) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_SHEEP_COLOR:
								try {
									sheep.setColor(MCDyeColor.valueOf(specArray.get(index, t).val().toUpperCase()));
								} catch(IllegalArgumentException exception) {
									throw new CREFormatException("Invalid sheep color: " + specArray.get(index, t).val(), t);
								}
								break;
							case entity_spec.KEY_SHEEP_SHEARED:
								sheep.setSheared(ArgumentValidation.getBoolean(specArray.get(index, t), t));
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case SHULKER:
					MCShulker shulker = (MCShulker) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_SHULKER_COLOR:
								try {
									shulker.setColor(MCDyeColor.valueOf(specArray.get(index, t).val().toUpperCase()));
								} catch(IllegalArgumentException exception) {
									throw new CREFormatException("Invalid shulker color: " + specArray.get(index, t).val(), t);
								}
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case SHULKER_BULLET:
					MCShulkerBullet bullet = (MCShulkerBullet) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_SHULKERBULLET_TARGET:
								Mixed c = specArray.get(index, t);
								if(c instanceof CNull) {
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
				case SKELETON_HORSE:
				case ZOMBIE_HORSE:
					MCAbstractHorse undeadhorse = (MCAbstractHorse) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_HORSE_JUMP:
								try {
									undeadhorse.setJumpStrength(ArgumentValidation.getDouble(specArray.get(index, t), t));
								} catch(IllegalArgumentException exception) {
									throw new CRERangeException("The jump strength must be between 0.0 and 2.0", t);
								}
								break;
							case entity_spec.KEY_HORSE_DOMESTICATION:
								try {
									undeadhorse.setDomestication(ArgumentValidation.getInt32(specArray.get(index, t), t));
								} catch(IllegalArgumentException exception) {
									throw new CRERangeException("The domestication level can not be higher than the max domestication level.", t);
								}
								break;
							case entity_spec.KEY_HORSE_MAXDOMESTICATION:
								undeadhorse.setMaxDomestication(ArgumentValidation.getInt32(specArray.get(index, t), t));
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
					MCSnowman snowman = (MCSnowman) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_SNOWMAN_DERP:
								snowman.setDerp(ArgumentValidation.getBoolean(specArray.get(index, t), t));
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case SPECTRAL_ARROW:
					MCSpectralArrow spectral = (MCSpectralArrow) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_ARROW_CRITICAL:
								spectral.setCritical(ArgumentValidation.getBoolean(specArray.get(index, t), t));
								break;
							case entity_spec.KEY_ARROW_KNOCKBACK:
								int k = ArgumentValidation.getInt32(specArray.get(index, t), t);
								if(k < 0) {
									throw new CRERangeException("Knockback can not be negative.", t);
								} else {
									spectral.setKnockbackStrength(k);
								}
								break;
							case entity_spec.KEY_ARROW_DAMAGE:
								double d = ArgumentValidation.getDouble(specArray.get(index, t), t);
								if(d < 0) {
									throw new CRERangeException("Damage cannot be negative.", t);
								}
								spectral.setDamage(d);
								break;
							case entity_spec.KEY_SPECTRAL_ARROW_GLOWING_TICKS:
								spectral.setGlowingTicks(ArgumentValidation.getInt32(specArray.get(index, t), t));
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case SPLASH_POTION:
					MCThrownPotion potion = (MCThrownPotion) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_SPLASH_POTION_ITEM:
								MCItemStack potionItem = ObjectGenerator.GetGenerator().item(specArray.get(index, t), t);
								try {
									potion.setItem(potionItem);
								} catch(IllegalArgumentException ex) {
									throw new CREFormatException("Invalid potion type: " + potionItem.getType().getName(), t);
								}
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case STRIDER:
					MCStrider strider = (MCStrider) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_STEERABLE_SADDLED:
								strider.setSaddled(ArgumentValidation.getBoolean(specArray.get(index, t), t));
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case TEXT_DISPLAY:
					MCTextDisplay tDisplay = (MCTextDisplay) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_DISPLAY_TEXT:
								tDisplay.setText(specArray.get(index, t).val());
								break;
							case entity_spec.KEY_DISPLAY_TEXT_ALIGNMENT:
								try {
									tDisplay.setAlignment(MCTextDisplay.Alignment.valueOf(specArray.get(index, t).val()));
								} catch(IllegalArgumentException ex) {
									throw new CREFormatException("Invalid text alignment: " + specArray.get(index, t).val(), t);
								}
								break;
							case entity_spec.KEY_DISPLAY_TEXT_LINE_WIDTH:
								tDisplay.setLineWidth(ArgumentValidation.getInt32(specArray.get(index, t), t));
								break;
							case entity_spec.KEY_DISPLAY_TEXT_SEE_THROUGH:
								tDisplay.setVisibleThroughBlocks(ArgumentValidation.getBooleanObject(specArray.get(index, t), t));
								break;
							case entity_spec.KEY_DISPLAY_TEXT_SHADOW:
								tDisplay.setHasShadow(ArgumentValidation.getBooleanObject(specArray.get(index, t), t));
								break;
							case entity_spec.KEY_DISPLAY_TEXT_BACKGROUND_COLOR:
								Mixed color = specArray.get(index, t);
								if(color.isInstanceOf(CArray.TYPE)) {
									tDisplay.setBackgroundColor(ObjectGenerator.GetGenerator().color((CArray) color, t));
								} else if(color instanceof CNull) {
									tDisplay.setBackgroundColor(null);
								} else {
									throw new CRECastException("Expected a color array for text display background color.", t);
								}
								break;
							case entity_spec.KEY_DISPLAY_TEXT_OPACITY:
								long opacity = ArgumentValidation.getInt(specArray.get(index, t), t);
								if(opacity < 0 || opacity > 255) {
									throw new CRERangeException("Text opacity outside valid range.", t);
								}
								tDisplay.setOpacity((byte) opacity);
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case TRIDENT:
					MCTrident trident = (MCTrident) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_ARROW_CRITICAL:
								trident.setCritical(ArgumentValidation.getBoolean(specArray.get(index, t), t));
								break;
							case entity_spec.KEY_ARROW_KNOCKBACK:
								int k = ArgumentValidation.getInt32(specArray.get(index, t), t);
								if(k < 0) {
									throw new CRERangeException("Knockback can not be negative.", t);
								} else {
									trident.setKnockbackStrength(k);
								}
								break;
							case entity_spec.KEY_ARROW_DAMAGE:
								double d = ArgumentValidation.getDouble(specArray.get(index, t), t);
								if(d < 0) {
									throw new CRERangeException("Damage cannot be negative.", t);
								}
								trident.setDamage(d);
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case TROPICAL_FISH:
					MCTropicalFish fish = (MCTropicalFish) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_TROPICALFISH_COLOR:
								try {
									fish.setBodyColor(MCDyeColor.valueOf(specArray.get(index, t).val().toUpperCase()));
								} catch(IllegalArgumentException exception) {
									throw new CREFormatException("Invalid fish color: " + specArray.get(index, t).val(), t);
								}
								break;
							case entity_spec.KEY_TROPICALFISH_PATTERNCOLOR:
								try {
									fish.setPatternColor(MCDyeColor.valueOf(specArray.get(index, t).val().toUpperCase()));
								} catch(IllegalArgumentException exception) {
									throw new CREFormatException("Invalid fish pattern color: " + specArray.get(index, t).val(), t);
								}
								break;
							case entity_spec.KEY_TROPICALFISH_PATTERN:
								try {
									fish.setPattern(MCTropicalFish.MCPattern.valueOf(specArray.get(index, t).val().toUpperCase()));
								} catch(IllegalArgumentException exception) {
									throw new CREFormatException("Invalid fish pattern: " + specArray.get(index, t).val(), t);
								}
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case VEX:
					MCVex vex = (MCVex) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_VEX_CHARGING:
								vex.setCharging(ArgumentValidation.getBooleanObject(specArray.get(index, t), t));
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case VILLAGER:
					MCVillager villager = (MCVillager) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_VILLAGER_PROFESSION:
								try {
									villager.setProfession(MCProfession.valueOf(specArray.get(index, t).val().toUpperCase()));
								} catch(IllegalArgumentException exception) {
									throw new CREFormatException("Invalid profession: " + specArray.get(index, t).val(), t);
								}
								break;
							case entity_spec.KEY_VILLAGER_LEVEL:
								try {
									villager.setLevel(ArgumentValidation.getInt32(specArray.get(index, t), t));
								} catch(IllegalArgumentException exception) {
									throw new CRERangeException("Expected profession level to be 1-5, but got "
											+ specArray.get(index, t).val(), t);
								}
								break;
							case entity_spec.KEY_VILLAGER_EXPERIENCE:
								try {
									villager.setExperience(ArgumentValidation.getInt32(specArray.get(index, t), t));
								} catch(IllegalArgumentException exception) {
									throw new CRERangeException("Expected experience to be a positive number, but got "
											+ specArray.get(index, t).val(), t);
								}
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case WITHER_SKULL:
					MCWitherSkull skull = (MCWitherSkull) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_WITHER_SKULL_CHARGED:
								skull.setCharged(ArgumentValidation.getBoolean(specArray.get(index, t), t));
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
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_WOLF_ANGRY:
								wolf.setAngry(ArgumentValidation.getBoolean(specArray.get(index, t), t));
								break;
							case entity_spec.KEY_WOLF_COLOR:
								try {
									wolf.setCollarColor(MCDyeColor.valueOf(specArray.get(index, t).val().toUpperCase()));
								} catch(IllegalArgumentException exception) {
									throw new CREFormatException("Invalid collar color: " + specArray.get(index, t).val(), t);
								}
								break;
							case entity_spec.KEY_GENERIC_SITTING:
								wolf.setSitting(ArgumentValidation.getBoolean(specArray.get(index, t), t));
								break;
							case entity_spec.KEY_WOLF_INTERESTED:
								if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_20_6)) {
									wolf.setInterested(ArgumentValidation.getBooleanObject(specArray.get(index, t), t));
								}
								break;
							case entity_spec.KEY_WOLF_TYPE:
								if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_20_6)) {
									try {
										MCWolf.Variant type = MCWolf.Variant.valueOf(specArray.get(index, t).val());
										wolf.setWolfVariant(type);
									} catch (IllegalArgumentException ex) {
										throw new CREFormatException("Invalid wolf type: " + specArray.get(index, t).val(), t);
									}
								}
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case ZOGLIN:
					MCZoglin zoglin = (MCZoglin) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_GENERIC_BABY:
								if(ArgumentValidation.getBooleanObject(specArray.get(index, t), t)) {
									zoglin.setBaby();
								} else {
									zoglin.setAdult();
								}
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case ZOMBIE:
				case DROWNED:
				case HUSK:
					MCZombie zombie = (MCZombie) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_GENERIC_BABY:
								if(ArgumentValidation.getBooleanObject(specArray.get(index, t), t)) {
									zombie.setBaby();
								} else {
									zombie.setAdult();
								}
								break;
							case entity_spec.KEY_ZOMBIE_BREAK_DOORS:
								zombie.setCanBreakDoors(ArgumentValidation.getBooleanObject(specArray.get(index, t), t));
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case ZOMBIE_VILLAGER:
					MCZombieVillager zombievillager = (MCZombieVillager) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_GENERIC_BABY:
								if(ArgumentValidation.getBooleanObject(specArray.get(index, t), t)) {
									zombievillager.setBaby();
								} else {
									zombievillager.setAdult();
								}
								break;
							case entity_spec.KEY_VILLAGER_PROFESSION:
								try {
									zombievillager.setProfession(MCProfession.valueOf(specArray.get(index, t).val().toUpperCase()));
								} catch(IllegalArgumentException exception) {
									throw new CREFormatException("Invalid profession: " + specArray.get(index, t).val(), t);
								}
								break;
							case entity_spec.KEY_ZOMBIE_BREAK_DOORS:
								zombievillager.setCanBreakDoors(ArgumentValidation.getBooleanObject(specArray.get(index, t), t));
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				case ZOMBIFIED_PIGLIN:
					MCPigZombie pigZombie = (MCPigZombie) entity;
					for(String index : specArray.stringKeySet()) {
						switch(index.toLowerCase()) {
							case entity_spec.KEY_GENERIC_BABY:
								if(ArgumentValidation.getBooleanObject(specArray.get(index, t), t)) {
									pigZombie.setBaby();
								} else {
									pigZombie.setAdult();
								}
								break;
							case entity_spec.KEY_ZOMBIFIED_PIGLIN_ANGRY:
								pigZombie.setAngry(ArgumentValidation.getBoolean(specArray.get(index, t), t));
								break;
							case entity_spec.KEY_ZOMBIFIED_PIGLIN_ANGER:
								pigZombie.setAnger(ArgumentValidation.getInt32(specArray.get(index, t), t));
								break;
							case entity_spec.KEY_ZOMBIE_BREAK_DOORS:
								pigZombie.setCanBreakDoors(ArgumentValidation.getBooleanObject(specArray.get(index, t), t));
								break;
							default:
								throwException(index, t);
						}
					}
					break;
				default:
					for(String index : specArray.stringKeySet()) {
						throwException(index, t);
					}
			}

			return CVoid.VOID;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_projectile_shooter extends EntityGetterFunction {

		@Override
		public String getName() {
			return "get_projectile_shooter";
		}

		@Override
		public String docs() {
			return "mixed {entityUUID} Returns the shooter of the given projectile, can be null."
					+ " If the shooter is an entity, that entity's ID will be return, but if it is a block,"
					+ " that block's location will be returned.";
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCEntity entity = Static.getEntity(args[0], t);

			if(entity instanceof MCProjectile) {
				MCProjectileSource shooter = ((MCProjectile) entity).getShooter();
				if(shooter instanceof MCBlockProjectileSource) {
					return ObjectGenerator.GetGenerator().location(((MCBlockProjectileSource) shooter).getBlock().getLocation(), false);
				} else if(shooter instanceof MCEntity) {
					return new CString(((MCEntity) shooter).getUniqueId().toString(), t);
				} else {
					return CNull.NULL;
				}
			} else {
				throw new CREBadEntityException("The given entity is not a projectile.", t);
			}
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_projectile_shooter extends EntitySetterFunction {

		@Override
		public String getName() {
			return "set_projectile_shooter";
		}

		@Override
		public String docs() {
			return "void {entityUUID, shooter} Sets the shooter of the given projectile. This can be an entity UUID,"
					+ " dispenser location array (throws CastException if not a dispenser), or null.";
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCEntity entity = Static.getEntity(args[0], t);
			if(entity instanceof MCProjectile) {
				if(args[1] instanceof CNull) {
					((MCProjectile) entity).setShooter(null);
				} else if(args[1].isInstanceOf(CArray.TYPE)) {
					MCBlock b = ObjectGenerator.GetGenerator().location(args[1], entity.getWorld(), t).getBlock();
					if(b.isDispenser()) {
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
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_projectile_bounce extends EntityGetterFunction {

		@Override
		public String getName() {
			return "get_projectile_bounce";
		}

		@Override
		public String docs() {
			return "boolean {entityUUID} Returns if the given projectile should bounce when it hits something. (deprecated)";
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCEntity entity = Static.getEntity(args[0], t);
			if(entity instanceof MCProjectile) {
				return CBoolean.get(((MCProjectile) entity).doesBounce());
			} else {
				throw new CREBadEntityException("The given entity is not a projectile.", t);
			}
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_projectile_bounce extends EntitySetterFunction {

		@Override
		public String getName() {
			return "set_projectile_bounce";
		}

		@Override
		public String docs() {
			return "void {entityUUID, boolean} Sets if the given projectile should bounce when it hits something. (deprecated)";
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCEntity entity = Static.getEntity(args[0], t);
			if(entity instanceof MCProjectile) {
				((MCProjectile) entity).setBounce(ArgumentValidation.getBoolean(args[1], t));
			} else {
				throw new CREBadEntityException("The given entity is not a projectile.", t);
			}
			return CVoid.VOID;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_projectile_item extends EntityGetterFunction {

		@Override
		public String getName() {
			return "get_projectile_item";
		}

		@Override
		public String docs() {
			return "array {entityUUID} Returns the displayed item for some projectiles."
					+ " This can be used on throwable projectiles (snowballs, eggs, exp bottles, enderpearls)"
					+ " as well as large and small fireballs.";
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCEntity entity = Static.getEntity(args[0], t);
			if(entity instanceof MCItemProjectile) {
				return ObjectGenerator.GetGenerator().item(((MCItemProjectile) entity).getItem(), t);
			} else {
				throw new CREBadEntityException("The given entity does not have a display item.", t);
			}
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_4;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_projectile_item extends EntitySetterFunction {

		@Override
		public String getName() {
			return "set_projectile_item";
		}

		@Override
		public String docs() {
			return "void {entityUUID, itemArray} Sets the displayed item for some projectiles."
					+ " This can be used on throwable projectiles (snowballs, eggs, exp bottles, enderpearls)"
					+ " as well as large and small fireballs.";
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCEntity entity = Static.getEntity(args[0], t);
			if(entity instanceof MCItemProjectile) {
				((MCItemProjectile) entity).setItem(ObjectGenerator.GetGenerator().item(args[1], t));
			} else {
				throw new CREBadEntityException("The given entity does not have a display item.", t);
			}
			return CVoid.VOID;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_4;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class entity_fall_distance extends EntityGetterFunction {

		@Override
		public String getName() {
			return "entity_fall_distance";
		}

		@Override
		public String docs() {
			return "double {entityUUID} Returns the distance the entity has fallen.";
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			return new CDouble(Static.getEntity(args[0], t).getFallDistance(), t);
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_entity_fall_distance extends EntitySetterFunction {

		@Override
		public String getName() {
			return "set_entity_fall_distance";
		}

		@Override
		public String docs() {
			return "void {entityUUID, double} Sets the distance the entity has fallen.";
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Static.getEntity(args[0], t).setFallDistance(ArgumentValidation.getDouble32(args[1], t));
			return CVoid.VOID;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_entity_glowing extends EntitySetterFunction {

		@Override
		public String getName() {
			return "set_entity_glowing";
		}

		@Override
		public String docs() {
			return "void {entityUUID, boolean} If true, applies glowing effect to the entity";
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Static.getEntity(args[0], t).setGlowing(ArgumentValidation.getBoolean(args[1], t));
			return CVoid.VOID;
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_2;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_entity_glowing extends EntityGetterFunction {

		@Override
		public String getName() {
			return "get_entity_glowing";
		}

		@Override
		public String docs() {
			return "boolean {entityUUID} Returns true if the entity is glowing";
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCEntity e = Static.getEntity(args[0], t);
			return CBoolean.GenerateCBoolean(e.isGlowing(), t);
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_2;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_entity_silent extends EntityGetterFunction {

		@Override
		public String getName() {
			return "get_entity_silent";
		}

		@Override
		public String docs() {
			return "boolean {entityUUID} Returns true if the entity produces sounds";
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			return CBoolean.GenerateCBoolean(Static.getEntity(args[0], t).isSilent(), t);
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_2;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_entity_silent extends EntitySetterFunction {

		@Override
		public String getName() {
			return "set_entity_silent";
		}

		@Override
		public String docs() {
			return "void {entityUUID, boolean} Sets whether or not entity produces sounds";
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCEntity e = Static.getEntity(args[0], t);
			e.setSilent(ArgumentValidation.getBoolean(args[1], t));
			return CVoid.VOID;
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_2;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_entity_gravity extends EntityGetterFunction {

		@Override
		public String getName() {
			return "get_entity_gravity";
		}

		@Override
		public String docs() {
			return "boolean {entityUUID} Returns true if gravity applies to the entity.";
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			return CBoolean.GenerateCBoolean(Static.getEntity(args[0], t).hasGravity(), t);
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_2;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_entity_gravity extends EntitySetterFunction {

		@Override
		public String getName() {
			return "set_entity_gravity";
		}

		@Override
		public String docs() {
			return "void {entityUUID, boolean} Sets whether or not gravity applies to the entity.";
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCEntity e = Static.getEntity(args[0], t);
			e.setHasGravity(ArgumentValidation.getBoolean(args[1], t));
			return CVoid.VOID;
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_2;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_entity_invulnerable extends EntityGetterFunction {

		@Override
		public String getName() {
			return "get_entity_invulnerable";
		}

		@Override
		public String docs() {
			return "boolean {entityUUID} Returns true if the entity cannot be damaged";
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			return CBoolean.GenerateCBoolean(Static.getEntity(args[0], t).isInvulnerable(), t);
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_2;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_entity_invulnerable extends EntitySetterFunction {

		@Override
		public String getName() {
			return "set_entity_invulnerable";
		}

		@Override
		public String docs() {
			return "void {entityUUID, boolean} If set to true the entity cannot be damaged, except by players in"
					+ " creative mode";
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCEntity e = Static.getEntity(args[0], t);
			e.setInvulnerable(ArgumentValidation.getBoolean(args[1], t));
			return CVoid.VOID;
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_2;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_entity_freezing extends EntityGetterFunction {

		@Override
		public String getName() {
			return "get_entity_freezing";
		}

		@Override
		public String docs() {
			return "int {entityUUID} Returns the number of ticks the entity has been freezing in powdered snow. (MC 1.17+)"
					+ " Counts down by 2 every tick when entity is thawing.";
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCEntity e = Static.getEntity(args[0], t);
			return new CInt(e.getFreezingTicks(), t);
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_entity_freezing extends EntitySetterFunction {

		@Override
		public String getName() {
			return "set_entity_freezing";
		}

		@Override
		public String docs() {
			return "void {entityUUID, int} Sets how many server ticks the entity has been freezing. (MC 1.17+)"
					+ " Must be above zero, and will clamp to the maximum the server allows.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class, CRELengthException.class, CREBadEntityException.class,
				CRERangeException.class};
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCEntity entity = Static.getEntity(args[0], t);
			int ticks = ArgumentValidation.getInt32(args[1], t);
			if(ticks < 0) {
				throw new CRERangeException("Freezing ticks must not be below zero.", t);
			}
			entity.setFreezingTicks(ticks);
			return CVoid.VOID;
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_scoreboard_tags extends EntityGetterFunction {

		@Override
		public String getName() {
			return "get_scoreboard_tags";
		}

		@Override
		public String docs() {
			return "array {entityUUID} Returns an array of scoreboard tags for this entity.";
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCEntity e = Static.getEntity(args[0], t);
			CArray tags = new CArray(t);
			for(String tag : e.getScoreboardTags()) {
				tags.push(new CString(tag, t), t);
			}
			return tags;
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_2;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class has_scoreboard_tag extends AbstractFunction {

		@Override
		public String getName() {
			return "has_scoreboard_tag";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "boolean {entityUUID, tag} Returns whether this entity has a specific tag.";
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCEntity e = Static.getEntity(args[0], t);
			return CBoolean.get(e.hasScoreboardTag(args[1].val()));
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREBadEntityException.class, CRELengthException.class, CREFormatException.class};
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
		public Version since() {
			return MSVersion.V3_3_5;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class add_scoreboard_tag extends EntitySetterFunction {

		@Override
		public String getName() {
			return "add_scoreboard_tag";
		}

		@Override
		public String docs() {
			return "boolean {entityUUID, tag} Adds a tag to the entity. Returns whether or not it was successful.";
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCEntity e = Static.getEntity(args[0], t);
			return CBoolean.get(e.addScoreboardTag(args[1].val()));
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_2;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class remove_scoreboard_tag extends EntitySetterFunction {

		@Override
		public String getName() {
			return "remove_scoreboard_tag";
		}

		@Override
		public String docs() {
			return "boolean {entityUUID, tag} Removes a tag from the entity. Returns whether or not it was successful.";
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCEntity e = Static.getEntity(args[0], t);
			return CBoolean.get(e.removeScoreboardTag(args[1].val()));
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_2;
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
			return "string {[player/LocationArray], itemArray, [spawnNaturally]} Drops the specified item stack at the"
					+ " specified player's feet (or at an arbitrary Location, if an array is given), and returns its"
					+ " entity UUID. spawnNaturally takes a boolean, which forces the way the item will be spawned."
					+ " If true, the item will be dropped with a random velocity.";
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
		public MSVersion since() {
			return MSVersion.V3_2_0;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCLocation l;
			MCItemStack is;
			boolean natural;
			if(args.length == 1) {
				if(env.getEnv(CommandHelperEnvironment.class).GetPlayer() != null) {
					l = env.getEnv(CommandHelperEnvironment.class).GetPlayer().getEyeLocation();
					natural = false;
				} else {
					throw new CREPlayerOfflineException("Invalid sender!", t);
				}
				is = ObjectGenerator.GetGenerator().item(args[0], t);
			} else {
				MCPlayer p;
				if(args[0].isInstanceOf(CArray.TYPE)) {
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
			if(is.isEmpty()) {
				// can't drop air
				return CNull.NULL;
			}
			if(args.length == 3) {
				natural = ArgumentValidation.getBoolean(args[2], t);
			}
			MCItem item;
			if(natural) {
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
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			MCWorld w = null;
			if(p != null) {
				w = p.getWorld();
			}
			MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], w, t);
			CArray options;
			if(args.length == 2) {
				options = ArgumentValidation.getArray(args[1], t);
			} else {
				options = CArray.GetAssociativeArray(t);
			}

			int strength = 2;
			if(options.containsKey("strength")) {
				strength = ArgumentValidation.getInt32(options.get("strength", t), t);
				if(strength > 127) {
					throw new CRERangeException("Strength cannot be higher than 127", t);
				}
			}

			List<MCFireworkEffect> effects = new ArrayList<>();
			if(options.containsKey("effects")) {
				Mixed cEffects = options.get("effects", t);
				if(cEffects.isInstanceOf(CArray.TYPE)) {
					for(Mixed c : ((CArray) cEffects).asList()) {
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
			} catch(ClassNotFoundException ex) {
				//Hrm...
				Logger.getLogger(Minecraft.class.getName()).log(Level.SEVERE, null, ex);
				return "";
			}
			List<String> names = new ArrayList<>();
			for(Field f : c.getFields()) {
				if(f.getType() == MCColor.class) {
					names.add(f.getName());
				}
			}
			return "string {locationArray, [optionsArray]} Launches a firework rocket."
					+ " The location array specifies where it is launched from,"
					+ " and the options array is an associative array described below."
					+ " All parameters in the array are optional, and default to the specified values if not set."
					+ " The default options being set will make it look like a normal firework, with a white explosion."
					+ " Returns the firework rocket entity's UUID. ----"
					+ " The options array may have the following keys:\n"
					+ "{| cellspacing=\"1\" cellpadding=\"1\" border=\"1\" class=\"wikitable\"\n"
					+ "! Array key !! Description !! Default\n"
					+ "|-\n"
					+ "| strength || A number indicating the flight duration of the rocket equal to the amount of"
					+ " gunpowder used to craft a rocket. (negative numbers detonate immediately) || 2\n"
					+ "|-\n"
					+ "| flicker || A boolean, determining if the firework will flicker || false\n"
					+ "|-\n"
					+ "| trail || A boolean, determining if the firework will leave a trail || true\n"
					+ "|-\n"
					+ "| colors || An array of colors, or a pipe separated string of color names"
					+ " for instance: array('WHITE') or 'WHITE<nowiki>|</nowiki>BLUE'. If you want custom colors,"
					+ " you must use an array, though you can still use color names as an item in the array,"
					+ " for instance: array('ORANGE', array(30, 45, 150))."
					+ " These colors are used as the primary colors. || 'WHITE'\n"
					+ "|-\n"
					+ "| fade || An array of colors to be used as the fade colors. This parameter should be formatted"
					+ " the same as the colors parameter || array()\n"
					+ "|-\n"
					+ "| type || An enum value of one of the firework types, one of: "
					+ StringUtils.Join(MCFireworkType.values(), ", ", " or ")
					+ " || " + MCFireworkType.BALL.name() + "\n"
					+ "|}\n"
					+ "The \"named colors\" can be one of: " + StringUtils.Join(names, ", ", " or ");
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_hanging_direction extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREBadEntityException.class, CRELengthException.class, CREFormatException.class};
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
		public Mixed exec(Target t, com.laytonsmith.core.environments.Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCEntity entity = Static.getEntity(args[0], t);
			if(entity instanceof MCHanging hanging) {
				return new CString(hanging.getFacing().name(), t);
			}
			throw new CREBadEntityException("Not a hanging entity.", t);
		}

		@Override
		public String getName() {
			return "get_hanging_direction";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public CClassType getReturnType(Target t, List<CClassType> argTypes, List<Target> argTargets,
				com.laytonsmith.core.environments.Environment env, Set<ConfigCompileException> exceptions) {
			return CString.TYPE;
		}

		@Override
		public String docs() {
			return "string {entityUUID} Returns the direction a hanging entity is facing.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}

	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_hanging_direction extends AbstractFunction implements Optimizable {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREBadEntityException.class, CRELengthException.class, CREFormatException.class,
				CREIllegalArgumentException.class};
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
		public CClassType getReturnType(Target t, List<CClassType> argTypes, List<Target> argTargets, com.laytonsmith.core.environments.Environment env, Set<ConfigCompileException> exceptions) {
			return CVoid.TYPE;
		}

		@Override
		public Mixed exec(Target t, com.laytonsmith.core.environments.Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCEntity entity = Static.getEntity(args[0], t);
			if(entity instanceof MCHanging hanging) {
				MCBlockFace face;
				try {
					face = MCBlockFace.valueOf(args[1].val());
					boolean force = false;
					if(args.length == 3) {
						force = ArgumentValidation.getBooleanObject(args[2], t);
					}
					hanging.setFacingDirection(face, force);
					return CVoid.VOID;
				} catch(IllegalArgumentException ex) {
					throw new CREIllegalArgumentException("Invalid direction for " + entity.getType().name() + ": "
							+ args[1].val(), t);
				}
			}
			throw new CREBadEntityException("Not a hanging entity.", t);
		}

		@Override
		public String getName() {
			return "set_hanging_direction";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public String docs() {
			return "void {entityUUID, direction, [force]} Sets the direction a hanging entity is facing."
					+ " Valid directions are NORTH, SOUTH, EAST, and WEST. UP and DOWN are also valid for item frames."
					+ " A hanging will not change direction if there's no supporting block in the new position."
					+ " However, the 'force' parameter can be set to true to override this behavior."
					+ " While leash hitches are technically hangings, they don't support different directions.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}

		@Override
		public ParseTree optimizeDynamic(Target t, com.laytonsmith.core.environments.Environment env,
				Set<Class<? extends com.laytonsmith.core.environments.Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {

			if(children.size() < 2) {
				return null;
			}
			Mixed data = children.get(1).getData();
			if(data instanceof CString) {
				try {
					MCBlockFace.valueOf(data.val());
					// also check for unsupported BlockFaces
					if(!data.val().contains("_") && !data.val().equals("SELF")) {
						return null;
					}
				} catch(IllegalArgumentException ex) {
					// invalid BlockFace
				}
				throw new CREIllegalArgumentException("Invalid direction. Must be one of"
						+ " UP, DOWN, NORTH, SOUTH, EAST or WEST", data.getTarget());
			}
			return null;
		}

		@Override
		public Set<Optimizable.OptimizationOption> optimizationOptions() {
			return EnumSet.of(Optimizable.OptimizationOption.OPTIMIZE_DYNAMIC);
		}

	}

	@api(environments = {CommandHelperEnvironment.class})
	@seealso({set_display_entity.class})
	public static class get_display_entity extends AbstractFunction {

		@Override
		public String getName() {
			return "get_display_entity";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "array {entityUUID} Returns an associative array of display entity data."
					+ " Array keys are: 'billboard', 'brightness', 'glowcolor', 'height', 'width',"
					+ " 'viewrange', 'shadowradius', 'shadowstrength', 'teleportduration', and 'transformation'. ---- "
					+ " The following values are common to all display entity types. Data about specific display entity"
					+ " types (block, text, and item display entities) can be found in {{function|entity_spec}}.\n\n"
					+ "* '''billboard''' (string) : Controls which axes the rendered entity rotates around the entity"
					+ " location when the viewing player's position or facing changes. FIXED (default) will not rotate."
					+ " HORIZONTAL or VERTICAL rotate on their respective axes. CENTER rotates on both axes.\n"
					+ "* '''brightness''' (array) : Controls the brightness when rendering the display entity."
					+ " A null value (default) will render the entity based on the environment."
					+ " An array with int values for the keys '''\"block\"''' and '''\"sky\"''' simulate the rendering"
					+ " brightness from those respective light sources. Each must be from 0 - 15."
					+ " Optionally a single int can be provided and will be used for both sky and block sources.\n"
					+ "* '''glowcolor''' (array) : An RGB array for the entity glow color. If null (default), the"
					+ " entity will use its scoreboard team color, if it has one.\n"
					+ "* '''height''' (double) : The maximum height of the entity's bounding box. (default: 0.0)"
					+ " Spans vertically from the entity's y location to (y+height), and is used for culling."
					+ " If the client's field of view does not include this box, the entity will not be rendered."
					+ " If either width or height is 0.0, culling is disabled.\n"
					+ "* '''width''' (double) : The maximum width of the entity's bounding box. (default: 0.0)"
					+ " Spans horizontally (width/2) from entity location.\n"
					+ "* '''viewrange''' (double) : The relative distance the entity will be viewable."
					+ " The default is 1.0, which is 64 meters multiplied by the player's entity distance scaling."
					+ " This can also be limited by the world's entity-tracking-range for display entities.\n"
					+ "* '''shadowradius''' (double) : The visible radius in meters of the entity's shadow."
					+ " Effective range is from 0.0 (default) to 64.0.\n"
					+ "* '''shadowstrength''' (double) : The opacity of the entity's shadow as a function of distance"
					+ " to a block below the entity within shadowradius. (default: 1.0)\n"
					+ "* '''teleportduration''' (int) : The duration in ticks a teleport is interpolated on the client."
					+ " Range is strictly from 0 - 59. (default: 0) (MC 1.20.2+)\n"
					+ "* '''interpolationduration''' (int) : The duration in ticks of interpolations. (non-teleport)\n"
					+ "* '''transformation''' (array) : An associative array that includes 4 values, leftRotation,"
					+ " rightRotation, scale, and translation. Both leftRotation and rightRotation have x, y, z, and w"
					+ " values, and scale and translation have x, y, and z values. All are 32 bit floats.\n"
					+ "\n";
		}

		@Override
		public Mixed exec(Target t, com.laytonsmith.core.environments.Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCEntity entity = Static.getEntity(args[0], t);
			if(!(entity instanceof MCDisplay display)) {
				throw new CREBadEntityException("Not a display entity.", t);
			}
			CArray info = CArray.GetAssociativeArray(t);
			info.set("billboard", display.getBillboard().name());
			MCDisplay.Brightness brightness = display.getBrightness();
			if(brightness != null) {
				CArray brightnessArray = CArray.GetAssociativeArray(t);
				brightnessArray.set("block", new CInt(brightness.block(), t), t);
				brightnessArray.set("sky", new CInt(brightness.block(), t), t);
				info.set("brightness", brightnessArray, t);
			} else {
				info.set("brightness", CNull.NULL, t);
			}
			MCColor color = display.getGlowColorOverride();
			if(color != null) {
				info.set("glowcolor", ObjectGenerator.GetGenerator().color(color, t), t);
			} else {
				info.set("glowcolor", CNull.NULL, t);
			}
			info.set("height", new CDouble(display.getDisplayHeight(), t), t);
			info.set("width", new CDouble(display.getDisplayWidth(), t), t);
			info.set("viewrange", new CDouble(display.getViewRange(), t), t);
			info.set("shadowradius", new CDouble(display.getShadowRadius(), t), t);
			info.set("shadowstrength", new CDouble(display.getShadowStrength(), t), t);
			if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_20_2)) {
				info.set("teleportduration", new CInt(display.getTeleportDuration(), t), t);
			}
			info.set("interpolationduration", new CInt(display.getInterpolationDurationTicks(), t), t);
			MCTransformation tr = display.getTransformation();
			CArray transformation = GetArrayFromTransformation(tr);
			info.set("transformation", transformation, t);
			return info;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREBadEntityException.class, CRELengthException.class, CREFormatException.class};
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

	}

	public static CArray GetArrayFromTransformation(MCTransformation tr) {
		Target t = Target.UNKNOWN;
		Quaternionf leftRotationT = tr.getLeftRotation();
		Quaternionf rightRotationT = tr.getRightRotation();
		Vector3f scaleT = tr.getScale();
		Vector3f translationT = tr.getTranslation();
		CArray leftRotation = new CArray(t, 4);
		leftRotation.set("w", leftRotationT.w);
		leftRotation.set("x", leftRotationT.x);
		leftRotation.set("y", leftRotationT.y);
		leftRotation.set("z", leftRotationT.z);
		CArray rightRotation = new CArray(t, 4);
		rightRotation.set("w", rightRotationT.w);
		rightRotation.set("x", rightRotationT.x);
		rightRotation.set("y", rightRotationT.y);
		rightRotation.set("z", rightRotationT.z);
		CArray scale = new CArray(t, 3);
		scale.set("x", scaleT.x);
		scale.set("y", scaleT.y);
		scale.set("z", scaleT.z);
		CArray translation = new CArray(t, 3);
		translation.set("x", translationT.x);
		translation.set("y", translationT.y);
		translation.set("z", translationT.z);
		CArray transformation = new CArray(t, 4);
		transformation.set("leftRotation", leftRotation, t);
		transformation.set("rightRotation", rightRotation, t);
		transformation.set("scale", scale, t);
		transformation.set("translation", translation, t);
		return transformation;
	}

	public static MCTransformation GetTransformationFromMatrix(float[] f) {
		Matrix4f matrix4f = new Matrix4f(
				f[0], f[1], f[2], f[3],
				f[4], f[5], f[6], f[7],
				f[8], f[9], f[10], f[11],
				f[12], f[13], f[14], f[15]);
		matrix4f.transpose();
		Matrix3f matrix3f = new Matrix3f(matrix4f);
		Vector3f translation = matrix4f.getTranslation(new Vector3f());

		float multiplier = 1.0F / matrix4f.m33();
		if(multiplier != 1.0F) {
			matrix3f.scale(multiplier);
			translation.mul(multiplier);
		}

		Triple<Quaternionf, Vector3f, Quaternionf> triple = null;
		Class MatrixUtil = ReflectionUtils.forName("com.mojang.math.MatrixUtil");
		for(String method : new String[]{"svdDecompose", "a"}) {
			if(ReflectionUtils.hasMethod(MatrixUtil, method, Triple.class, Matrix3f.class)) {
				triple = (Triple) ReflectionUtils.invokeMethod(MatrixUtil, null, method,
						new Class[]{Matrix3f.class},
						new Object[]{matrix3f});
				break;
			}
		}
		if(triple == null) {
			throw new Error("Cannot find svdDecompose method.");
		}

		Vector3f scale = triple.getMiddle();
		Quaternionf leftRotation = triple.getLeft().rotateY((float) java.lang.Math.PI); // 180 deg
		Quaternionf rightRotation = triple.getRight();
		MCTransformation tr = StaticLayer.GetTransformation(leftRotation, rightRotation, scale, translation);
		return tr;
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_transformation_from_matrix extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRELengthException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			float f[] = new float[16];
			if(args.length == 1) {
				CArray array = ArgumentValidation.getArray(args[0], t);
				if(array.size() != 16) {
					throw new CRELengthException("Input array expected to have length 16", t);
				}
				for(int i = 0; i < 16; i++) {
					f[i] = ArgumentValidation.getDouble32(array.get(i, t), t);
				}
			} else {
				for(int i = 0; i < 16; i++) {
					f[i] = ArgumentValidation.getDouble32(args[i], t);
				}
			}
			MCTransformation tr = GetTransformationFromMatrix(f);
			return GetArrayFromTransformation(tr);
		}

		@Override
		public String getName() {
			return "get_transformation_from_matrix";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 16};
		}

		@Override
		public String docs() {
			return "array {array matrix | float f0, float f1, float f2, float f3, float f4, float f5, float f6, float"
					+ " f7, float f8, float f9, float f10, float f11, float f12, float f13, float f14, float f15}"
					+ " Converts a Minecraft transformation matrix into a transformation object. This is the same"
					+ " underlying algorithm that set_display_entity uses when accepting a transformation matrix.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}

	}

	@api(environments = {CommandHelperEnvironment.class})
	@seealso({get_display_entity.class})
	public static class set_display_entity extends AbstractFunction {

		@Override
		public String getName() {
			return "set_display_entity";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "void {entityUUID, array} Sets the data for a display entity."
					+ " See {{function|get_display_entity}} for details about the array format. ---- "
					+ " Note that there a couple additional points for setting the data that is unique to"
					+ " the set function:\n\n"
					+ "The '''transformation''' property can be provided in the same"
					+ " format as received (i.e. with the leftRotation, rightRotation, scale, and translation"
					+ " properties) but you can also provide it as a length 16 array of floats. These are"
					+ " accepted in the same order that the /data command would accept them. Regardless of"
					+ " how they are input, the function will always return the complex object. Sub-properties"
					+ " of the transformation property are optional - missing values will not be changed.\n\n"
					+ "The '''startinterpolation''' (int) property, if given, interpolates from the current values on"
					+ " the client to any changed values the server sent this tick. It halts existing interpolations,"
					+ " then starts interpolating on the current tick plus the given number of ticks (can be negative),"
					+ " and finishes after 'interpolationduration' ticks."
					+ " The following values are interpolated if changed:"
					+ " transformation, shadow radius, shadow strength, text background, and text opacity."
					+ " If none of those values are changed, it will use the last set again for interpolation."
					+ " If the values are changed but 'startinterpolation' isn't sent, it will continue interpolation"
					+ " for the remaining duration but with the new values.";
		}

		@Override
		public Mixed exec(Target t, com.laytonsmith.core.environments.Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCEntity entity = Static.getEntity(args[0], t);
			if(!(entity instanceof MCDisplay display)) {
				throw new CREBadEntityException("Not a display entity.", t);
			}
			CArray info = ArgumentValidation.getArray(args[1], t);
			if(!info.isAssociative()) {
				throw new CREIllegalArgumentException("Expected an associative array but found a normal array.", t);
			}
			if(info.containsKey("billboard")) {
				try {
					MCDisplay.Billboard billboard = MCDisplay.Billboard.valueOf(info.get("billboard", t).val());
					display.setBillboard(billboard);
				} catch(IllegalArgumentException ex) {
					throw new CREFormatException("Invalid billboard type for display entity.", t);
				}
			}
			if(info.containsKey("brightness")) {
				Mixed m = info.get("brightness", t);
				if(m instanceof CNull) {
					display.setBrightness(null);
				} else {
					MCDisplay.Brightness brightness;
					if(m.isInstanceOf(CArray.TYPE)) {
						CArray brightnessArray = (CArray) m;
						if(!brightnessArray.isAssociative()) {
							throw new CREIllegalArgumentException(
									"Expected an associative array for brightness but found a normal array.", t);
						}
						int blockBrightness = ArgumentValidation.getInt32(brightnessArray.get("block", t), t);
						int skyBrightness = ArgumentValidation.getInt32(brightnessArray.get("sky", t), t);
						brightness = new MCDisplay.Brightness(blockBrightness, skyBrightness);
					} else {
						int level = ArgumentValidation.getInt32(m, t);
						brightness = new MCDisplay.Brightness(level, level);
					}
					try {
						display.setBrightness(brightness);
					} catch(IllegalArgumentException ex) {
						throw new CREIllegalArgumentException(ex.getMessage(), t);
					}
				}
			}
			if(info.containsKey("glowcolor")) {
				Mixed m = info.get("glowcolor", t);
				if(!(m instanceof CNull)) {
					MCColor color = ObjectGenerator.GetGenerator().color(ArgumentValidation.getArray(m, t), t);
					display.setGlowColorOverride(color);
				}
			}
			if(info.containsKey("height")) {
				display.setDisplayHeight((float) ArgumentValidation.getDouble(info.get("height", t), t));
			}
			if(info.containsKey("width")) {
				display.setDisplayWidth((float) ArgumentValidation.getDouble(info.get("width", t), t));
			}
			if(info.containsKey("viewrange")) {
				display.setViewRange((float) ArgumentValidation.getDouble(info.get("viewrange", t), t));
			}
			if(info.containsKey("shadowradius")) {
				display.setShadowRadius((float) ArgumentValidation.getDouble(info.get("shadowradius", t), t));
			}
			if(info.containsKey("shadowstrength")) {
				display.setShadowStrength((float) ArgumentValidation.getDouble(info.get("shadowstrength", t), t));
			}
			if(info.containsKey("teleportduration") && Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_20_2)) {
				int ticks = ArgumentValidation.getInt32(info.get("teleportduration", t), t);
				if(ticks < 0 || ticks > 59) {
					throw new CRERangeException("Teleport duration must be from 0 - 59, but got " + ticks, t);
				}
				display.setTeleportDuration(ticks);
			}
			if(info.containsKey("interpolationduration")) {
				display.setInterpolationDurationTicks(ArgumentValidation.getInt32(info.get("interpolationduration", t), t));
			}
			if(info.containsKey("startinterpolation")) {
				display.setInterpolationDelayTicks(ArgumentValidation.getInt32(info.get("startinterpolation", t), t));
			}
			if(info.containsKey("transformation")) {
				CArray transformation = ArgumentValidation.getArray(info.get("transformation", t), t);
				if(transformation.size() == 16) {
					float[] f = new float[16];
					for(int i = 0; i < 16; i++) {
						f[i] = ArgumentValidation.getDouble32(transformation.get(i, t), t);
					}
					MCTransformation tr = GetTransformationFromMatrix(f);
					display.setTransformation(tr);
				} else {
					MCTransformation existingTransformation = display.getTransformation();
					Quaternionf leftRotation;
					if(transformation.containsKey("leftRotation")) {
						CArray leftRotationC = ArgumentValidation.getArray(transformation.get("leftRotation", t), t);
						leftRotation = new Quaternionf(
								ArgumentValidation.getDouble(leftRotationC.get("x", t), t),
								ArgumentValidation.getDouble(leftRotationC.get("y", t), t),
								ArgumentValidation.getDouble(leftRotationC.get("z", t), t),
								ArgumentValidation.getDouble(leftRotationC.get("w", t), t));
					} else {
						leftRotation = existingTransformation.getLeftRotation();
					}
					Quaternionf rightRotation;
					if(transformation.containsKey("rightRotation")) {
						CArray rightRotationC = ArgumentValidation.getArray(transformation.get("rightRotation", t), t);
						rightRotation = new Quaternionf(
								ArgumentValidation.getDouble(rightRotationC.get("x", t), t),
								ArgumentValidation.getDouble(rightRotationC.get("y", t), t),
								ArgumentValidation.getDouble(rightRotationC.get("z", t), t),
								ArgumentValidation.getDouble(rightRotationC.get("w", t), t));
					} else {
						rightRotation = existingTransformation.getRightRotation();
					}
					Vector3f scale;
					if(transformation.containsKey("scale")) {
						CArray scaleC = ArgumentValidation.getArray(transformation.get("scale", t), t);
						scale = new Vector3f(
								ArgumentValidation.getDouble32(scaleC.get("x", t), t),
								ArgumentValidation.getDouble32(scaleC.get("y", t), t),
								ArgumentValidation.getDouble32(scaleC.get("z", t), t));
					} else {
						scale = existingTransformation.getScale();
					}
					Vector3f translation;
					if(transformation.containsKey("translation")) {
						CArray translationC = ArgumentValidation.getArray(transformation.get("translation", t), t);
						translation = new Vector3f(
								ArgumentValidation.getDouble32(translationC.get("x", t), t),
								ArgumentValidation.getDouble32(translationC.get("y", t), t),
								ArgumentValidation.getDouble32(translationC.get("z", t), t));
					} else {
						translation = existingTransformation.getTranslation();
					}
					MCTransformation tr = StaticLayer.GetTransformation(leftRotation, rightRotation, scale, translation);
					display.setTransformation(tr);
				}
			}
			return CVoid.VOID;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREBadEntityException.class, CRELengthException.class, CREFormatException.class,
				CREIllegalArgumentException.class, CRECastException.class, CRERangeException.class};
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

	}

	@api
	public static class get_entity_transient_id extends EntityGetterFunction {

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCEntity entity = Static.getEntity(args[0], t);
			return new CInt(entity.getEntityId(), t);
		}

		@Override
		public String getName() {
			return "get_entity_transient_id";
		}

		@Override
		public String docs() {
			return "int {uuid} Given a permanent entity uuid, returns the transient entity id. This should not"
					+ " be stored, as it is reset every restart, and is only useful for dealing with low level packets.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}

	}

	@api
	public static class entity_in_water extends EntityGetterFunction {

		@Override
		public String getName() {
			return "entity_in_water";
		}

		@Override
		public String docs() {
			return "boolean {entityUUID} Returns whether an entity is colliding with water."
					+ " This accounts for waterlogged blocks, bubble columns, and fluid height."
					+ " It does not account for water cauldrons.";
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCEntity entity = Static.getEntity(args[0], t);
			return CBoolean.get(entity.isInWater());
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}
	}

	@api
	public static class set_entity_rotation extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREUnsupportedOperationException.class, CRELengthException.class};
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
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCEntity entity = Static.getEntity(args[0], t);

			if(entity instanceof MCPlayer) {
				throw new CREUnsupportedOperationException(getName() + " cannot be used on players.", t);
			}

			float yaw = (float) ArgumentValidation.getDouble(args[1], t);
			float pitch;
			if(args.length == 3) {
				pitch = (float) ArgumentValidation.getDouble(args[2], t);
			} else {
				pitch = entity.getLocation().getPitch();
			}

			entity.setRotation(yaw, pitch);
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "set_entity_rotation";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public String docs() {
			return "void {entityUUID, yaw, [pitch]} Sets an entity's yaw and pitch without teleporting or ejecting. If used"
					+ " on a player, an UnsupportedOperationException is thrown.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}

	}
}
