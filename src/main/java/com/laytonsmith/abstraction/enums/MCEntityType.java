package com.laytonsmith.abstraction.enums;

import com.laytonsmith.PureUtilities.ClassLoading.DynamicEnum;
import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.annotations.MDynamicEnum;
import com.laytonsmith.annotations.MEnum;
import com.laytonsmith.core.Static;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 *
 */
@MDynamicEnum("EntityType")
public abstract class MCEntityType<Concrete> extends DynamicEnum<MCEntityType.MCVanillaEntityType,Concrete> {

	// To be filled by the implementer
	protected static Map<String, MCEntityType> mappings;
	protected static Map<MCVanillaEntityType, MCEntityType> vanilla;

	public static MCEntityType NULL = null;

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

	@Override
	public MCVanillaEntityType getAbstracted() {
		return super.getAbstracted();
	}

	public Class<? extends MCEntity> getWrapperClass() {
		return wrapperClass;
	}

	public static MCEntityType valueOf(String test) throws IllegalArgumentException {
		if (mappings == null) {
			return null;
		}
		MCEntityType ret = mappings.get(test);
		if (ret == null) {
			throw new IllegalArgumentException("Unknown entity type: " + test);
		}
		return ret;
	}

	public static MCEntityType valueOfVanillaType(MCVanillaEntityType type) {
		if (vanilla == null) {
			return null;
		}
		return vanilla.get(type);
	}

	/**
	 * @return Names of available entity types
	 */
	public static Set<String> types() {
		if (NULL == null) { // docs mode
			Set<String> dummy = new HashSet<>();
			for (final MCVanillaEntityType t : MCVanillaEntityType.values()) {
				dummy.add(t.name());
			}
			return dummy;
		}
		return mappings.keySet();
	}

	/**
	 * @return Our own EntityType list
	 */
	public static Collection<MCEntityType> values() {
		if (NULL == null) { // docs mode
			ArrayList<MCEntityType> dummy = new ArrayList<>();
			for (final MCVanillaEntityType t : MCVanillaEntityType.values()) {
				dummy.add(new MCEntityType<Object>(t, null) {
					@Override
					public String name() {
						return t.name();
					}

					@Override
					public String concreteName() {
						return t.name();
					}

					@Override
					public boolean isSpawnable() {
						return t.isSpawnable();
					}
				});
			}
			return dummy;
		}
		return mappings.values();
	}

	@MEnum("VanillaEntityType")
	public enum MCVanillaEntityType {
		ARMOR_STAND(true, MCVersion.MC1_8),
		ARROW(true),
		/**
		 * Spawn with world.dropItem()
		 */
		BAT(true, MCVersion.MC1_4),
		BLAZE(true),
		BOAT(true),
		CAVE_SPIDER(true),
		CHICKEN(true),
		COMPLEX_PART(false),
		COW(true),
		CREEPER(true),
		DROPPED_ITEM(true),
		EGG(true),
		ENDERMAN(true),
		ENDERMITE(true, MCVersion.MC1_8),
		ENDER_CRYSTAL(true),
		ENDER_DRAGON(true),
		ENDER_EYE(false),
		ENDER_PEARL(false),
		EXPERIENCE_ORB(true),
		/**
		 * Spawn with world.spawnFallingBlock()
		 * I'm not sure what version we switched to FALLING_BLOCK from FALLING_SAND,
		 * but it was after 1.0
		 */
		FALLING_BLOCK(true),
		FIREBALL(true),
		FIREWORK(true, MCVersion.MC1_4_7),
		FISHING_HOOK(false),
		GHAST(true),
		GIANT(true),
		GUARDIAN(true, MCVersion.MC1_8),
		HORSE(true, MCVersion.MC1_6),
		IRON_GOLEM(true, MCVersion.MC1_2),
		ITEM_FRAME(false, MCVersion.MC1_4_5),
		LEASH_HITCH(false, MCVersion.MC1_6),
		/**
		 * Spawn with world.strikeLightning()
		 */
		LIGHTNING(true),
		MAGMA_CUBE(true),
		MINECART(true),
		MINECART_CHEST(true),
		MINECART_COMMAND(false, MCVersion.MC1_7),
		MINECART_FURNACE(true),
		MINECART_HOPPER(true, MCVersion.MC1_5),
		MINECART_MOB_SPAWNER(true, MCVersion.MC1_5),
		MINECART_TNT(true, MCVersion.MC1_5),
		MUSHROOM_COW(true),
		OCELOT(true, MCVersion.MC1_2),
		PAINTING(false),
		PIG(true),
		PIG_ZOMBIE(true),
		PLAYER(false),
		PRIMED_TNT(true),
		RABBIT(true, MCVersion.MC1_8),
		SHEEP(true),
		SILVERFISH(true),
		SKELETON(true),
		SLIME(true),
		SMALL_FIREBALL(true),
		SNOWBALL(true),
		SNOWMAN(true),
		SQUID(true),
		SPIDER(true),
		SPLASH_POTION(true),
		THROWN_EXP_BOTTLE(true),
		WEATHER(false),
		WITCH(true, MCVersion.MC1_4_5),
		WITHER(true, MCVersion.MC1_4),
		WITHER_SKULL(true, MCVersion.MC1_4),
		WOLF(true),
		VILLAGER(true),
		ZOMBIE(true),
		/**
		 * An unknown entity without an Entity Class
		 */
		UNKNOWN(false);

		private final boolean apiCanSpawn;
		private final MCVersion version;

		/**
		 * @param spawnable true if the entity is spawnable
		 */
		MCVanillaEntityType(boolean spawnable) {
			this.apiCanSpawn = spawnable;
			this.version = MCVersion.MC1_0;
		}

		/**
		 * @param spawnable true if the entity is spawnable
		 * @param added the version this entity was added
		 */
		MCVanillaEntityType(boolean spawnable, MCVersion added) {
			this.apiCanSpawn = spawnable;
			this.version = added;
		}

		// This is here only for site-based documentation of some functions
		public boolean isSpawnable() {
			return this.apiCanSpawn;
		}

		public boolean existsInCurrent() {
			return Static.getServer().getMinecraftVersion().ordinal() >= version.ordinal();
		}
	}
}
