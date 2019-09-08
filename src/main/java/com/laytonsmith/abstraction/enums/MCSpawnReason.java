package com.laytonsmith.abstraction.enums;

import com.laytonsmith.annotations.MEnum;

@MEnum("com.commandhelper.SpawnReason")
public enum MCSpawnReason {
	BREEDING,
	BUILD_IRONGOLEM,
	BUILD_SNOWMAN,
	BUILD_WITHER,
	/**
	 * Deprecated as of 1.14, no longer used.
	 */
	CHUNK_GEN,
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
	EXPLOSION
}
