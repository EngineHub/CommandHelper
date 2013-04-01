
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
    DROPPED_ITEM,
    EXPERIENCE_ORB,
    PAINTING,
    ARROW,
    SNOWBALL,
    FIREBALL,
    SMALL_FIREBALL,
	ITEM_FRAME,
	WITHER_SKULL,
	WITHER,
	BAT,
	WITCH,
    ENDER_PEARL,
    ENDER_SIGNAL,
    THROWN_EXP_BOTTLE,
    PRIMED_TNT,
    FALLING_BLOCK,
    MINECART,
    BOAT,
    CREEPER,
    SKELETON,
    SPIDER,
    GIANT,
    ZOMBIE,
    SLIME,
    GHAST,
    PIG_ZOMBIE,
    ENDERMAN,
    CAVE_SPIDER,
    SILVERFISH,
    BLAZE,
    MAGMA_CUBE,
    ENDER_DRAGON,
    PIG,
    SHEEP,
    COW,
    CHICKEN,
    SQUID,
    WOLF,
    MUSHROOM_COW,
    SNOWMAN,
    OCELOT,
    IRON_GOLEM,
    VILLAGER,
    ENDER_CRYSTAL,
    // These don't have an entity ID in nms.EntityTypes.
    SPLASH_POTION,
    EGG,
    FISHING_HOOK,
    /**
     * Spawn with {@link World#strikeLightning}.
     */
    LIGHTNING,
    WEATHER,
    PLAYER,
    COMPLEX_PART,
	FIREWORK,
	MINECART_CHEST,
	MINECART_FURNACE,
	MINECART_TNT,
	MINECART_HOPPER,
	MINECART_MOB_SPAWNER,
    /**
     * An unknown entity without an Entity Class
     */
    UNKNOWN;

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
