package com.laytonsmith.abstraction.enums;

import com.laytonsmith.annotations.MEnum;

/**
 * 
 * @author jb_aero
 */
@MEnum("SpawnReason")
public enum MCSpawnReason {
	BED,
	BREEDING,
	BUILD_IRONGOLEM,
	BUILD_SNOWMAN,
	BUILD_WITHER,
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
	REINFORCEMENTS,
	SLIME_SPLIT,
	SPAWNER,
	SPAWNER_EGG,
	VILLAGE_DEFENSE,
	VILLAGE_INVASION,
	NETHER_PORTAL,
	DISPENSE_EGG
}
