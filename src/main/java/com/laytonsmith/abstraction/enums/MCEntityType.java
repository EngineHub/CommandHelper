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
						return false;
					}
				});
			}
			return dummy;
		}
		return new ArrayList<>(MAP.values());
	}

	public enum MCVanillaEntityType {
		ALLAY(true, MCVersion.MC1_19),
		AREA_EFFECT_CLOUD,
		ARMADILLO(true, MCVersion.MC1_20_6),
		ARMOR_STAND,
		ARROW,
		AXOLOTL(true, MCVersion.MC1_17),
		BAT,
		BEE(true, MCVersion.MC1_15),
		BLAZE,
		BLOCK_DISPLAY(true, MCVersion.MC1_19_4),
		BOAT,
		BOGGED(true, MCVersion.MC1_20_6),
		BREEZE(true, MCVersion.MC1_20_4),
		BREEZE_WIND_CHARGE(true, MCVersion.MC1_20_6),
		CAMEL(true, MCVersion.MC1_19_3),
		CAT(true, MCVersion.MC1_14),
		CAVE_SPIDER,
		CHEST_BOAT(true, MCVersion.MC1_19),
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
		FOX(true, MCVersion.MC1_14),
		FROG(true, MCVersion.MC1_19),
		GHAST,
		GIANT,
		GLOW_ITEM_FRAME(true, MCVersion.MC1_17),
		GLOW_SQUID(true, MCVersion.MC1_17),
		GOAT(true, MCVersion.MC1_17),
		GUARDIAN,
		HOGLIN(true, MCVersion.MC1_16),
		HORSE,
		HUSK,
		ILLUSIONER,
		INTERACTION(true, MCVersion.MC1_19_4),
		IRON_GOLEM,
		ITEM_DISPLAY(true, MCVersion.MC1_19_4),
		ITEM_FRAME,
		LLAMA,
		LLAMA_SPIT,
		LEASH_HITCH,
		LIGHTNING,
		MAGMA_CUBE,
		MARKER(true, MCVersion.MC1_17),
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
		OMINOUS_ITEM_SPAWNER(true, MCVersion.MC1_20_6),
		PAINTING,
		PANDA(true, MCVersion.MC1_14),
		PARROT,
		PHANTOM,
		PIG,
		PIGLIN(true, MCVersion.MC1_16),
		PIGLIN_BRUTE(true, MCVersion.MC1_16_X),
		PILLAGER(true, MCVersion.MC1_14),
		PLAYER(false),
		POLAR_BEAR,
		PRIMED_TNT,
		PUFFERFISH,
		RABBIT,
		RAVAGER(true, MCVersion.MC1_14),
		SALMON,
		SHEEP,
		SILVERFISH,
		SKELETON,
		SHULKER,
		SHULKER_BULLET,
		SKELETON_HORSE,
		SLIME,
		SMALL_FIREBALL,
		SNIFFER(true, MCVersion.MC1_19_4),
		SNOWBALL,
		SNOWMAN,
		SQUID,
		SPECTRAL_ARROW,
		SPIDER,
		SPLASH_POTION,
		STRAY,
		STRIDER(true, MCVersion.MC1_16),
		TADPOLE(true, MCVersion.MC1_19),
		TEXT_DISPLAY(true, MCVersion.MC1_19_4),
		THROWN_EXP_BOTTLE,
		TRADER_LLAMA(true, MCVersion.MC1_14),
		TRIDENT,
		TROPICAL_FISH,
		TURTLE,
		VEX,
		VILLAGER,
		VINDICATOR,
		WANDERING_TRADER(true, MCVersion.MC1_14),
		WARDEN(true, MCVersion.MC1_19),
		WIND_CHARGE(true, MCVersion.MC1_20_4),
		WITCH,
		WITHER,
		WITHER_SKELETON,
		WITHER_SKULL,
		WOLF,
		ZOGLIN(true, MCVersion.MC1_16),
		ZOMBIE,
		ZOMBIE_HORSE,
		ZOMBIE_VILLAGER,
		ZOMBIFIED_PIGLIN(true, MCVersion.MC1_16),
		/**
		 * An unknown entity without an Entity Class
		 */
		UNKNOWN(false);

		private final boolean canSpawn;
		private final MCVersion from;
		private final MCVersion to;

		MCVanillaEntityType() {
			this.canSpawn = true;
			this.from = MCVersion.MC1_0;
			this.to = MCVersion.FUTURE;
		}

		/**
		 * @param spawnable true if the entity is spawnable
		 */
		MCVanillaEntityType(boolean spawnable) {
			this.canSpawn = spawnable;
			this.from = MCVersion.MC1_0;
			this.to = MCVersion.FUTURE;
		}

		/**
		 * @param spawnable true if the entity is spawnable
		 * @param added the version this entity was added
		 */
		MCVanillaEntityType(boolean spawnable, MCVersion added) {
			this.canSpawn = spawnable;
			this.from = added;
			this.to = MCVersion.FUTURE;
		}

		/**
		 * @param spawnable true if the entity is spawnable
		 * @param added the version this entity was added
		 * @param removed the version this entity was removed or renamed
		 */
		MCVanillaEntityType(boolean spawnable, MCVersion added, MCVersion removed) {
			this.canSpawn = spawnable;
			this.from = added;
			this.to = removed;
		}

		// This is here only for site-based documentation of some functions
		public boolean isSpawnable() {
			return this.canSpawn;
		}

		public boolean existsIn(MCVersion version) {
			return version.gte(from) && version.lte(to);
		}
	}
}
