package com.laytonsmith.abstraction.enums;

import com.laytonsmith.PureUtilities.ClassLoading.DynamicEnum;
import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.annotations.MDynamicEnum;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.constructs.Target;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@MDynamicEnum("com.commandhelper.EntityType")
public abstract class MCEntityType<Concrete> extends DynamicEnum<MCEntityType.MCVanillaEntityType, Concrete> {

	protected static final Map<String, MCEntityType> MAP = new HashMap<>();
	protected static final Map<MCVanillaEntityType, MCEntityType> VANILLA_MAP = new HashMap<>();

	protected Class<? extends MCEntity> wrapperClass;

	public MCEntityType(MCVanillaEntityType abstractedType, Concrete concreteType) {
		super(abstractedType, concreteType);
	}

	/**
	 * Utility method for spawn_entity
	 *
	 * @return whether the implemented api can spawn this entity
	 */
	public abstract boolean isSpawnable();

	public abstract boolean isProjectile();

	@Override
	public MCVanillaEntityType getAbstracted() {
		return super.getAbstracted();
	}

	public Class<? extends MCEntity> getWrapperClass() {
		return wrapperClass;
	}

	public static MCEntityType valueOf(String test) throws IllegalArgumentException {
		MCEntityType ret = MAP.get(test);
		if(ret == null) {
			switch(test) {
				case "TIPPED_ARROW":
					MSLog.GetLogger().e(MSLog.Tags.GENERAL,
							"TIPPED_ARROW entity type was removed in 1.14. Converted to ARROW.",
							Target.UNKNOWN);
					return MAP.get("ARROW");
				case "LINGERING_POTION":
					MSLog.GetLogger().e(MSLog.Tags.GENERAL,
							"LINGERING_POTION entity type was removed in 1.14. Converted to SPLASH_POTION.",
							Target.UNKNOWN);
					return MAP.get("SPLASH_POTION");
				case "PIG_ZOMBIE":
					MSLog.GetLogger().e(MSLog.Tags.GENERAL,
							"PIG_ZOMBIE entity type was renamed in 1.16. Converted to ZOMBIFIED_PIGLIN.",
							Target.UNKNOWN);
					return MAP.get("ZOMBIFIED_PIGLIN");
			}
			throw new IllegalArgumentException("Unknown entity type: " + test);
		}
		return ret;
	}

	public static MCEntityType valueOfVanillaType(MCVanillaEntityType type) {
		return VANILLA_MAP.get(type);
	}

	/**
	 * @return Names of available entity types
	 */
	public static Set<String> types() {
		if(MAP.isEmpty()) { // docs mode
			Set<String> dummy = new HashSet<>();
			for(final MCVanillaEntityType t : MCVanillaEntityType.values()) {
				if(t.existsIn(MCVersion.CURRENT)) {
					dummy.add(t.name());
				}
			}
			return dummy;
		}
		return MAP.keySet();
	}

	/**
	 * @return Our own EntityType list
	 */
	public static List<MCEntityType> values() {
		if(MAP.isEmpty()) { // docs mode
			ArrayList<MCEntityType> dummy = new ArrayList<>();
			for(final MCVanillaEntityType t : MCVanillaEntityType.values()) {
				if(!t.existsIn(MCVersion.CURRENT)) {
					continue;
				}
				dummy.add(new MCEntityType<>(t, null) {
					@Override
					public String name() {
						return t.name();
					}

					@Override
					public boolean isSpawnable() {
						return t.isSpawnable();
					}

					@Override
					public boolean isProjectile() {
						return t.isProjectile();
					}
				});
			}
			return dummy;
		}
		return new ArrayList<>(MAP.values());
	}

	public enum MCVanillaEntityType {
		ALLAY(true, false, MCVersion.MC1_19),
		AREA_EFFECT_CLOUD,
		ARMOR_STAND,
		ARROW,
		AXOLOTL(true, false, MCVersion.MC1_17),
		BAT,
		BEE(true, false, MCVersion.MC1_15),
		BLAZE,
		BLOCK_DISPLAY(true, false, MCVersion.MC1_19_X),
		BOAT,
		BREEZE(true, false, MCVersion.MC1_20_X),
		CAMEL(true, false, MCVersion.MC1_19_3),
		CAT(true, false, MCVersion.MC1_14),
		CAVE_SPIDER,
		CHEST_BOAT(true, false, MCVersion.MC1_19),
		CHICKEN,
		COD,
		COW,
		CREEPER,
		DOLPHIN,
		DRAGON_FIREBALL,
		DROPPED_ITEM,
		DROWNED,
		DONKEY,
		EGG,
		ELDER_GUARDIAN,
		ENDERMAN,
		ENDERMITE,
		ENDER_CRYSTAL,
		ENDER_DRAGON,
		ENDER_EYE,
		ENDER_PEARL,
		EVOKER,
		EVOKER_FANGS,
		EXPERIENCE_ORB,
		FALLING_BLOCK,
		FIREBALL,
		FIREWORK,
		FISHING_HOOK(false),
		FOX(true, false, MCVersion.MC1_14),
		FROG(true, false, MCVersion.MC1_19),
		GHAST,
		GIANT,
		GLOW_ITEM_FRAME(true, false, MCVersion.MC1_17),
		GLOW_SQUID(true, false, MCVersion.MC1_17),
		GOAT(true, false, MCVersion.MC1_17),
		GUARDIAN,
		HOGLIN(true, false, MCVersion.MC1_16),
		HORSE,
		HUSK,
		ILLUSIONER,
		INTERACTION(true, false, MCVersion.MC1_19_X),
		IRON_GOLEM,
		ITEM_DISPLAY(true, false, MCVersion.MC1_19_X),
		ITEM_FRAME,
		LLAMA,
		LLAMA_SPIT(true, true),
		LEASH_HITCH,
		LIGHTNING,
		MAGMA_CUBE,
		MARKER(true, false, MCVersion.MC1_17),
		MINECART,
		MINECART_CHEST,
		MINECART_COMMAND,
		MINECART_FURNACE,
		MINECART_HOPPER,
		MINECART_MOB_SPAWNER,
		MINECART_TNT,
		MULE,
		MUSHROOM_COW,
		OCELOT,
		PAINTING,
		PANDA(true, false, MCVersion.MC1_14),
		PARROT,
		PHANTOM,
		PIG,
		PIGLIN(true, false, MCVersion.MC1_16),
		PIGLIN_BRUTE(true, false, MCVersion.MC1_16_X),
		PILLAGER(true, false, MCVersion.MC1_14),
		PLAYER(false),
		POLAR_BEAR,
		PRIMED_TNT,
		PUFFERFISH,
		RABBIT,
		RAVAGER(true, false, MCVersion.MC1_14),
		SALMON,
		SHEEP,
		SILVERFISH,
		SKELETON,
		SHULKER,
		SHULKER_BULLET,
		SKELETON_HORSE,
		SLIME,
		SMALL_FIREBALL,
		SNIFFER(true, false, MCVersion.MC1_19_X),
		SNOWBALL,
		SNOWMAN,
		SQUID,
		SPECTRAL_ARROW,
		SPIDER,
		SPLASH_POTION,
		STRAY,
		STRIDER(true, false, MCVersion.MC1_16),
		TADPOLE(true, false, MCVersion.MC1_19),
		TEXT_DISPLAY(true, false, MCVersion.MC1_19_X),
		THROWN_EXP_BOTTLE,
		TRADER_LLAMA(true, false, MCVersion.MC1_14),
		TRIDENT,
		TROPICAL_FISH,
		TURTLE,
		VEX,
		VILLAGER,
		VINDICATOR,
		WANDERING_TRADER(true, false, MCVersion.MC1_14),
		WARDEN(true, false, MCVersion.MC1_19),
		WIND_CHARGE(true, true, MCVersion.MC1_20_X),
		WITCH,
		WITHER,
		WITHER_SKELETON,
		WITHER_SKULL,
		WOLF,
		ZOGLIN(true, false, MCVersion.MC1_16),
		ZOMBIE,
		ZOMBIE_HORSE,
		ZOMBIE_VILLAGER,
		ZOMBIFIED_PIGLIN(true, false, MCVersion.MC1_16),
		/**
		 * An unknown entity without an Entity Class
		 */
		UNKNOWN(false);

		private final boolean canSpawn;
		private final boolean canShoot;
		private final MCVersion from;
		private final MCVersion to;

		MCVanillaEntityType() {
			this.canSpawn = true;
			this.canShoot = false;
			this.from = MCVersion.MC1_0;
			this.to = MCVersion.FUTURE;
		}

		/**
		 * @param spawnable true if the entity is spawnable
		 */
		MCVanillaEntityType(boolean spawnable) {
			this.canSpawn = spawnable;
			this.canShoot = false;
			this.from = MCVersion.MC1_0;
			this.to = MCVersion.FUTURE;
		}

		/**
		 * @param spawnable true if the entity is spawnable
		 * @param projectile true if the entity is a projectile
		 */
		MCVanillaEntityType(boolean spawnable, boolean projectile) {
			this.canSpawn = spawnable;
			this.canShoot = projectile;
			this.from = MCVersion.MC1_0;
			this.to = MCVersion.FUTURE;
		}

		/**
		 * @param spawnable true if the entity is spawnable
		 * @param projectile true if the entity is a projectile
		 * @param added the version this entity was added
		 */
		MCVanillaEntityType(boolean spawnable, boolean projectile, MCVersion added) {
			this.canSpawn = spawnable;
			this.canShoot = projectile;
			this.from = added;
			this.to = MCVersion.FUTURE;
		}

		/**
		 * @param spawnable true if the entity is spawnable
		 * @param projectile true if the entity is a projectile
		 * @param added the version this entity was added
		 * @param removed the version this entity was removed
		 */
		MCVanillaEntityType(boolean spawnable, boolean projectile, MCVersion added, MCVersion removed) {
			this.canSpawn = spawnable;
			this.canShoot = projectile;
			this.from = added;
			this.to = removed;
		}

		// This is here only for site-based documentation of some functions
		public boolean isSpawnable() {
			return this.canSpawn;
		}

		public boolean isProjectile() {
			return this.canShoot;
		}

		public boolean existsIn(MCVersion version) {
			return version.gte(from) && version.lte(to);
		}
	}
}
