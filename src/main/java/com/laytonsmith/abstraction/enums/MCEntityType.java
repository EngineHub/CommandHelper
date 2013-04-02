
package com.laytonsmith.abstraction.enums;

import com.laytonsmith.annotations.typename;
import com.laytonsmith.core.constructs.CPrimitive;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.MEnum;

/**
 *
 * @author Layton
 */
@typename("EntityType")
public enum MCEntityType implements MEnum {
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
	/**
	 * An unknown entity without an Entity Class
	 */
	UNKNOWN(false);
	
	private boolean apiCanSpawn;
	
	private MCEntityType(boolean spawnable) {
		this.apiCanSpawn = spawnable;
	}
	
	public boolean isSpawnable() {
		return this.apiCanSpawn;
	}
	
	public Object value() {
		return this;
	}

	public String val() {
		return name();
	}

	public boolean isNull() {
		return false;
	}

	public String typeName() {
		return this.getClass().getAnnotation(typename.class).value();
	}

	public CPrimitive primitive(Target t) throws ConfigRuntimeException {
		throw new Error();
	}

	public boolean isImmutable() {
		return true;
	}
}
