
package com.laytonsmith.abstraction.enums;

import com.laytonsmith.annotations.MEnum;

/**
 *
 * 
 */
@MEnum("EntityType")
public enum MCEntityType {
	/**
	 * Spawn with {@link World#dropItem}}
	 */
	DROPPED_ITEM(true),
	EXPERIENCE_ORB(true),
	PAINTING(false),
	ARROW(true),
	SNOWBALL(true),
	FIREBALL(true),
	SMALL_FIREBALL(true),
	ITEM_FRAME(false),
	WITHER_SKULL(true),
	WITHER(true),
	BAT(true),
	WITCH(true),
	ENDER_PEARL(false),
	ENDER_SIGNAL(false),
	THROWN_EXP_BOTTLE(true),
	PRIMED_TNT(true),
	/**
	 * Spawn with {@link World#spawnFallingBlock}}
	 */
	FALLING_BLOCK(true),
	MINECART(true),
	BOAT(true),
	CREEPER(true),
	SKELETON(true),
	SPIDER(true),
	GIANT(true),
	ZOMBIE(true),
	SLIME(true),
	GHAST(true),
	PIG_ZOMBIE(true),
	ENDERMAN(true),
	CAVE_SPIDER(true),
	SILVERFISH(true),
	BLAZE(true),
	MAGMA_CUBE(true),
	ENDER_DRAGON(true),
	PIG(true),
	SHEEP(true),
	COW(true),
	CHICKEN(true),
	SQUID(true),
	WOLF(true),
	MUSHROOM_COW(true),
	SNOWMAN(true),
	OCELOT(true),
	IRON_GOLEM(true),
	VILLAGER(true),
	HORSE(true),
	LEASH_HITCH(false),
	ENDER_CRYSTAL(true),
	// These don't have an entity ID in nms.EntityTypes.
	SPLASH_POTION(true),
	EGG(true),
	FISHING_HOOK(false),
	/**
	 * Spawn with {@link World#strikeLightning}.
	 */
	LIGHTNING(true),
	WEATHER(true),
	PLAYER(false),
	COMPLEX_PART(false),
	FIREWORK(true),
	MINECART_CHEST(true),
	MINECART_FURNACE(true),
	MINECART_TNT(true),
	MINECART_HOPPER(true),
	MINECART_MOB_SPAWNER(true),
	MINECART_COMMAND(false),
	/**
	 * An unknown entity without an Entity Class
	 */
	UNKNOWN(false);
	
	private boolean apiCanSpawn;
	
	/**
	 * 
	 * @param spawnable true if the entity is spawnable
	 */
	MCEntityType(boolean spawnable) {
		this.apiCanSpawn = spawnable;
	}
	
	public boolean isSpawnable() {
		return this.apiCanSpawn;
	}
}
