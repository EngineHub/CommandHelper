/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction;

/**
 *
 * @author Layton
 */
public enum MCEntityType {
    ARROW,
    BLAZE,
    BOAT,
    CAVE_SPIDER,
    CHICKEN,
    COMPLEX_PART,
    COW,
    CREEPER,
    DROPPED_ITEM,
    EGG,
    ENDER_CRYSTAL,
    ENDER_DRAGON,
    ENDER_PEARL,
    ENDER_SIGNAL,
    ENDERMAN,
    EXPERIENCE_ORB,
    FALLING_BLOCK,
    FIREBALL,
    FISHING_HOOK,
    GHAST,
    GIANT,
    IRON_GOLEM,
    /**
     * Spawn with {@link World#strikeLightning}.
     */
    LIGHTNING,
    MAGMA_CUBE,
    MINECART,
    MUSHROOM_COW,
    OCELOT,
    PAINTING,
    PIG,
    PIG_ZOMBIE,
    PLAYER,
    PRIMED_TNT,
    SHEEP,
    SILVERFISH,
    SKELETON,
    SLIME,
    SMALL_FIREBALL,
    SNOWBALL,
    SNOWMAN,
    SPIDER,
    // These don't have an entity ID in nms.EntityTypes.
    SPLASH_POTION,
    SQUID,
    THROWN_EXP_BOTTLE,
    /**
     * An unknown entity without an Entity Class
     */
    UNKNOWN,
    VILLAGER,
    WEATHER,
    WOLF,
    ZOMBIE;
}
