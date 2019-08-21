package com.laytonsmith.abstraction.enums;

public enum MCDamageCause {
	/**
	 * Damage caused when an entity contacts a block such as a Cactus.
	 */
	CONTACT,
	/**
	 * Damage caused when an entity attacks another entity.
	 */
	ENTITY_ATTACK,
	/**
	 * Damage caused when attacked by a projectile.
	 */
	PROJECTILE,
	/**
	 * Damage caused by being put in a block
	 */
	SUFFOCATION,
	/**
	 * Damage caused when an entity falls a distance greater than 3 blocks
	 */
	FALL,
	/**
	 * Damage caused by direct exposure to fire
	 */
	FIRE,
	/**
	 * Damage caused due to burns caused by fire
	 */
	FIRE_TICK,
	/**
	 * Damage caused by direct exposure to lava
	 */
	LAVA,
	/**
	 * Damage caused by running out of air while in water
	 */
	DROWNING,
	/**
	 * Damage caused by being in the area when a block explodes.
	 */
	BLOCK_EXPLOSION,
	/**
	 * Damage caused by being in the area when an entity, such as a Creeper, explodes.
	 */
	ENTITY_EXPLOSION,
	/**
	 * Damage caused by falling into the void
	 */
	VOID,
	/**
	 * Damage caused by being struck by lightning
	 */
	LIGHTNING,
	/**
	 * Damage caused by committing suicide using the command "/kill"
	 */
	SUICIDE,
	/**
	 * Damage caused by starving due to having an empty hunger bar
	 */
	STARVATION,
	/**
	 * Damage caused due to an ongoing poison effect
	 */
	POISON,
	/**
	 * Damage caused by being hit by a damage potion or spell
	 */
	MAGIC,
	/**
	 * Damage caused due to a snowman melting
	 */
	MELTING,
	/**
	 * Damage caused by Wither potion effect
	 */
	WITHER,
	/**
	 * Damage caused by being hit by a falling block which deals damage
	 */
	FALLING_BLOCK,
	/**
	 * Damage caused in retaliation to another attack by the Thorns enchantment.
	 */
	THORNS,
	/**
	 * Damage caused by a dragon breathing fire.
	 */
	DRAGON_BREATH,
	/**
	 * Damage caused when an entity runs into a wall.
	 */
	FLY_INTO_WALL,
	/**
	 * Damage caused when an entity steps on MAGMA.
	 */
	HOT_FLOOR,
	/**
	 * Damage caused when an entity is colliding with too many entities due to the maxEntityCramming game rule.
	 */
	CRAMMING,
	/**
	 * Damage caused when an entity attacks another entity in a sweep attack.
	 */
	ENTITY_SWEEP_ATTACK,
	/**
	 * Damage caused when an entity that should be in water is not.
	 */
	DRYOUT,
	/**
	 * Custom damage.
	 */
	CUSTOM
}
