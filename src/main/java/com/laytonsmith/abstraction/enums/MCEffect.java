package com.laytonsmith.abstraction.enums;

import com.laytonsmith.annotations.MEnum;

@MEnum("com.commandhelper.Effect")
public enum MCEffect {
	/**
	 * VISUAL
	 */
	BEE_GROWTH, // integer, Paper, added in 1.20.6, partially replaces VILLAGER_PLANT_GROW
	BONE_MEAL_USE, // integer
	COMPOSTER_FILL_ATTEMPT, // boolean for success
	COPPER_WAX_OFF,
	COPPER_WAX_ON,
	DRAGON_BREATH,
	DRIPPING_DRIPSTONE,
	ELECTRIC_SPARK, // Axis at which particles are shown
	ENDER_DRAGON_DESTROY_BLOCK,
	END_GATEWAY_SPAWN,
	END_PORTAL_FRAME_FILL,
	ENDER_SIGNAL,
	INSTANT_POTION_BREAK, // Color of particles
	LAVA_INTERACT,
	MOBSPAWNER_FLAMES,
	OXIDISED_COPPER_SCRAPE,
	PARTICLES_EGG_CRACK, // Paper, added 1.20 - 1.20.4
	PARTICLES_AND_SOUND_BRUSH_BLOCK_COMPLETE, // BlockData, Paper, added 1.20 - 1.20.4
	PARTICLES_SCULK_CHARGE, // integer, Paper, added 1.20 - 1.20.4
	POTION_BREAK, // Color
	REDSTONE_TORCH_BURNOUT,
	SHOOT_WHITE_SMOKE, // BlockFace for the direction, Paper, added 1.20 - 1.20.4
	SMASH_ATTACK, // integer, Paper, added in 1.20.5
	SMOKE, // BlockFace for the direction of the smoke particles
	SPAWN_COBWEB, // Paper, added in 1.20.6
	SPONGE_DRY,
	TRIAL_SPAWNER_BECOME_OMINOUS, // boolean, Paper, added 1.20.6
	TRIAL_SPAWNER_DETECT_PLAYER, // integer, Paper, added 1.20.4
	TRIAL_SPAWNER_DETECT_PLAYER_OMINOUS, // integer, Paper, added 1.20.6
	TRIAL_SPAWNER_EJECT_ITEM, // Paper, added 1.20.4
	TRIAL_SPAWNER_SPAWN, // boolean, Paper, added 1.20.4
	TRIAL_SPAWNER_SPAWN_ITEM, // boolean, Paper, added 1.20.6
	TRIAL_SPAWNER_SPAWN_MOB_AT, // boolean, Paper, added 1.20.4
	TURTLE_EGG_PLACEMENT, // integer, Paper, added in 1.20.6
	VAULT_ACTIVATE, // boolean, Paper, added in 1.20.6
	VAULT_DEACTIVATE, // boolean, Paper, added in 1.20.6
	VAULT_EJECT_ITEM, // Paper, added in 1.20.6
	VILLAGER_PLANT_GROW, // integer, deprecated in 1.20.6, partially replaced by BEE_GROWTH
	/**
	 * SOUND
	 */
	ANVIL_BREAK,
	ANVIL_LAND,
	ANVIL_USE,
	BAT_TAKEOFF,
	BLAZE_SHOOT,
	BOOK_PAGE_TURN,
	BOW_FIRE,
	BREWING_STAND_BREW,
	CHORUS_FLOWER_DEATH,
	CHORUS_FLOWER_GROW,
	CLICK1,
	CLICK2,
	CRAFTER_CRAFT, // Paper, added 1.20.4
	CRAFTER_FAIL, // Paper, added 1.20.4
	DOOR_CLOSE, // deprecated in 1.19.3
	DOOR_TOGGLE, // deprecated in 1.19.3
	ENDERDRAGON_GROWL,
	ENDERDRAGON_SHOOT,
	ENDEREYE_LAUNCH, // deprecated in 1.21
	EXTINGUISH,
	FENCE_GATE_CLOSE, // deprecated in 1.19.3
	FENCE_GATE_TOGGLE, // deprecated in 1.19.3
	FIREWORK_SHOOT,
	GHAST_SHOOT,
	GHAST_SHRIEK,
	GRINDSTONE_USE,
	HUSK_CONVERTED_TO_ZOMBIE,
	IRON_DOOR_CLOSE, // deprecated in 1.19.3
	IRON_DOOR_TOGGLE, // deprecated in 1.19.3
	IRON_TRAPDOOR_CLOSE, // deprecated in 1.19.3
	IRON_TRAPDOOR_TOGGLE, // deprecated in 1.19.3
	PARTICLES_SCULK_SHRIEK, // Paper, added 1.20 - 1.20.4
	PHANTOM_BITE,
	POINTED_DRIPSTONE_DRIP_LAVA_INTO_CAULDRON,
	POINTED_DRIPSTONE_DRIP_WATER_INTO_CAULDRON,
	POINTED_DRIPSTONE_LAND,
	PORTAL_TRAVEL,
	RECORD_PLAY, // Material for record item
	SKELETON_CONVERTED_TO_STRAY,
	SMITHING_TABLE_USE,
	SOUND_STOP_JUKEBOX_SONG, // Paper, added 1.20 - 1.20.4
	SOUND_WITH_CHARGE_SHOT, // Paper, added 1.21
	STEP_SOUND, // Material for block type stepped on
	TRAPDOOR_CLOSE, // deprecated in 1.19.3
	TRAPDOOR_TOGGLE, // deprecated in 1.19.3
	ZOMBIE_CHEW_WOODEN_DOOR,
	ZOMBIE_CHEW_IRON_DOOR,
	ZOMBIE_DESTROY_DOOR,
	WITHER_BREAK_BLOCK,
	WITHER_SHOOT,
	ZOMBIE_CONVERTED_TO_DROWNED,
	ZOMBIE_CONVERTED_VILLAGER,
	ZOMBIE_INFECT
}
