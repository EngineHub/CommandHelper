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

@MDynamicEnum("EntityType")
public abstract class MCEntityType<Concrete> extends DynamicEnum<MCEntityType.MCVanillaEntityType, Concrete> {

	// To be filled by the implementer
	protected static Map<String, MCEntityType> mappings;
	protected static Map<MCVanillaEntityType, MCEntityType> vanilla;

	@SuppressWarnings("checkstyle:staticvariablename") // Fixing this violation might break dependents.
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
		if(mappings == null) {
			return null;
		}
		MCEntityType ret = mappings.get(test);
		if(ret == null) {
			throw new IllegalArgumentException("Unknown entity type: " + test);
		}
		return ret;
	}

	public static MCEntityType valueOfVanillaType(MCVanillaEntityType type) {
		if(vanilla == null) {
			return null;
		}
		return vanilla.get(type);
	}

	/**
	 * @return Names of available entity types
	 */
	public static Set<String> types() {
		if(NULL == null) { // docs mode
			Set<String> dummy = new HashSet<>();
			for(final MCVanillaEntityType t : MCVanillaEntityType.values()) {
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
		if(NULL == null) { // docs mode
			ArrayList<MCEntityType> dummy = new ArrayList<>();
			for(final MCVanillaEntityType t : MCVanillaEntityType.values()) {
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
		AREA_EFFECT_CLOUD(true),
		ARMOR_STAND(true),
		ARROW(true),
		BAT(true),
		BLAZE(true),
		BOAT(true),
		CAVE_SPIDER(true),
		CHICKEN(true),
		COD(true),
		COMPLEX_PART(false),
		COW(true),
		CREEPER(true),
		DOLPHIN(true),
		DRAGON_FIREBALL(true),
		DROPPED_ITEM(true),
		DROWNED(true),
		DONKEY(true),
		EGG(true),
		ELDER_GUARDIAN(true),
		ENDERMAN(true),
		ENDERMITE(true),
		ENDER_CRYSTAL(true),
		ENDER_DRAGON(true),
		ENDER_EYE(true),
		ENDER_PEARL(true),
		EVOKER(true),
		EVOKER_FANGS(true),
		EXPERIENCE_ORB(true),
		FALLING_BLOCK(true),
		FIREBALL(true),
		FIREWORK(true),
		FISHING_HOOK(false),
		GHAST(true),
		GIANT(true),
		GUARDIAN(true),
		HORSE(true),
		HUSK(true),
		ILLUSIONER(true),
		IRON_GOLEM(true),
		ITEM_FRAME(true),
		LLAMA(true),
		LLAMA_SPIT(false),
		LEASH_HITCH(true),
		LIGHTNING(true),
		LINGERING_POTION(true),
		MAGMA_CUBE(true),
		MINECART(true),
		MINECART_CHEST(true),
		MINECART_COMMAND(true),
		MINECART_FURNACE(true),
		MINECART_HOPPER(true),
		MINECART_MOB_SPAWNER(true),
		MINECART_TNT(true),
		MULE(true),
		MUSHROOM_COW(true),
		OCELOT(true),
		PAINTING(true),
		PARROT(true),
		PHANTOM(true),
		PIG(true),
		PIG_ZOMBIE(true),
		PLAYER(false),
		POLAR_BEAR(true),
		PRIMED_TNT(true),
		PUFFERFISH(true),
		RABBIT(true),
		SALMON(true),
		SHEEP(true),
		SILVERFISH(true),
		SKELETON(true),
		SHULKER(true),
		SHULKER_BULLET(true),
		SKELETON_HORSE(true),
		SLIME(true),
		SMALL_FIREBALL(true),
		SNOWBALL(true),
		SNOWMAN(true),
		SQUID(true),
		SPECTRAL_ARROW(true),
		SPIDER(true),
		SPLASH_POTION(true),
		STRAY(true),
		THROWN_EXP_BOTTLE(true),
		TIPPED_ARROW(true),
		TRIDENT(true),
		TROPICAL_FISH(true),
		TURTLE(true),
		VEX(true),
		VILLAGER(true),
		VINDICATOR(true),
		WEATHER(false),
		WITCH(true),
		WITHER(true),
		WITHER_SKELETON(true),
		WITHER_SKULL(true),
		WOLF(true),
		ZOMBIE(true),
		ZOMBIE_HORSE(true),
		ZOMBIE_VILLAGER(true),
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
			return Static.getServer().getMinecraftVersion().gte(version);
		}
	}
}
