package com.laytonsmith.abstraction.enums;

import com.laytonsmith.annotations.MEnum;

@MEnum("com.commandhelper.SpawnReason")
public enum MCSpawnReason {
	BEEHIVE,
	BREEDING,
	BUCKET,
	BUILD_IRONGOLEM,
	BUILD_SNOWMAN,
	BUILD_WITHER,
	/**
	 * Spawned by vanilla /summon command
	 */
	COMMAND,
	/**
	 * Spawned by plugins
	 */
	CUSTOM,
	/**
	 * Missing spawn reason
	 */
	DEFAULT,
	/**
	 * The kind of egg you throw
	 */
	EGG,
	JOCKEY,
	LIGHTNING,
	NATURAL,
	PATROL,
	PIGLIN_ZOMBIFIED,
	RAID,
	REINFORCEMENTS,
	SHOULDER_ENTITY,
	SLIME_SPLIT,
	SPAWNER,
	SPAWNER_EGG,
	VILLAGE_DEFENSE,
	VILLAGE_INVASION,
	NETHER_PORTAL,
	DISPENSE_EGG,
	INFECTION,
	CURED,
	OCELOT_BABY,
	SILVERFISH_BLOCK,
	MOUNT,
	TRAP,
	ENDER_PEARL,
	DROWNED,
	SHEARED,
	EXPLOSION,
	FROZEN,
	SPELL,
	METAMORPHOSIS,
	DUPLICATION,
	ENCHANTMENT,
	TRIAL_SPAWNER,
	POTION_EFFECT,
}
