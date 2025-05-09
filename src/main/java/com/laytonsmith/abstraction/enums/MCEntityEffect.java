package com.laytonsmith.abstraction.enums;

import com.laytonsmith.annotations.MEnum;

@MEnum("com.commandhelper.EntityEffect")
public enum MCEntityEffect {
	ARROW_PARTICLES,
	RABBIT_JUMP,
	DEATH, // deprecated for EGG_BREAK, SNOWBALL_BREAK, ENTITY_DEATH
	EGG_BREAK,
	SNOWBALL_BREAK,
	ENTITY_DEATH,
	HURT,
	SHEEP_EAT, // deprecated for SHEEP_EAT_GRASS, TNT_MINECART_IGNITE
	SHEEP_EAT_GRASS,
	TNT_MINECART_IGNITE,
	WOLF_HEARTS,
	WOLF_SHAKE,
	WOLF_SMOKE,
	IRON_GOLEM_ROSE,
	VILLAGER_HEART,
	VILLAGER_ANGRY,
	VILLAGER_HAPPY,
	WITCH_MAGIC,
	ZOMBIE_TRANSFORM,
	FIREWORK_EXPLODE,
	LOVE_HEARTS,
	SQUID_ROTATE,
	ENTITY_POOF,
	GUARDIAN_TARGET,
	SHIELD_BLOCK, // deprecated 1.21.5
	SHIELD_BREAK, // deprecated 1.21.5
	ARMOR_STAND_HIT,
	THORNS_HURT,
	IRON_GOLEM_SHEATH,
	TOTEM_RESURRECT,
	HURT_DROWN,
	HURT_EXPLOSION,
	DOLPHIN_FED,
	RAVAGER_STUNNED,
	CAT_TAME_FAIL,
	CAT_TAME_SUCCESS,
	VILLAGER_SPLASH,
	PLAYER_BAD_OMEN_RAID,
	HURT_BERRY_BUSH,
	FOX_CHEW,
	TELEPORT_ENDER,
	BREAK_EQUIPMENT_MAIN_HAND,
	BREAK_EQUIPMENT_OFF_HAND,
	BREAK_EQUIPMENT_HELMET,
	BREAK_EQUIPMENT_CHESTPLATE,
	BREAK_EQUIPMENT_LEGGINGS,
	BREAK_EQUIPMENT_BOOTS,
	RESET_SPAWNER_MINECART_DELAY,
	FANG_ATTACK,
	HOGLIN_ATTACK,
	RAVAGER_ATTACK,
	WARDEN_ATTACK,
	ZOGLIN_ATTACK,
	HONEY_BLOCK_SLIDE_PARTICLES,
	HONEY_BLOCK_FALL_PARTICLES,
	SWAP_HAND_ITEMS,
	WOLF_SHAKE_STOP,
	GOAT_LOWER_HEAD,
	GOAT_RAISE_HEAD,
	SPAWN_DEATH_SMOKE,
	WARDEN_TENDRIL_SHAKE,
	WARDEN_SONIC_ATTACK,
	SNIFFER_DIG,
}
