package com.laytonsmith.abstraction.enums;

import com.laytonsmith.annotations.MEnum;
import com.laytonsmith.core.Static;

import java.util.HashMap;
import java.util.Map;

@MEnum("com.commandhelper.GameRule")
public enum MCGameRule {
	ADVANCE_TIME("doDaylightCycle"),
	ADVANCE_WEATHER("doWeatherCycle"),
	BLOCK_DROPS("doTileDrops"),
	ALLOW_ENTERING_NETHER_USING_PORTALS("allowEnteringNetherUsingPortals"),
	BLOCK_EXPLOSION_DROP_DECAY("blockExplosionDropDecay"),
	COMMAND_BLOCK_OUTPUT("commandBlockOutput"),
	COMMAND_BLOCKS_WORK("commandBlocksEnabled"),
	DROWNING_DAMAGE("drowningDamage"),
	ELYTRA_MOVEMENT_CHECK("disableElytraMovementCheck"),
	ENDER_PEARLS_VANISH_ON_DEATH("enderPearlsVanishOnDeath"),
	ENTITY_DROPS("doEntityDrops"),
	FALL_DAMAGE("fallDamage"),
	FIRE_DAMAGE("fireDamage"),
	FIRE_SPREAD_RADIUS_AROUND_PLAYER,
	FORGIVE_DEAD_PLAYERS("forgiveDeadPlayers"),
	FREEZE_DAMAGE("freezeDamage"),
	GLOBAL_SOUND_EVENTS("globalSoundEvents"),
	IMMEDIATE_RESPAWN("doImmediateRespawn"),
	KEEP_INVENTORY("keepInventory"),
	LAVA_SOURCE_CONVERSION("lavaSourceConversion"),
	LIMITED_CRAFTING("doLimitedCrafting"),
	LOCATOR_BAR("locatorBar"),
	LOG_ADMIN_COMMANDS("logAdminCommands"),
	MAX_BLOCK_MODIFICATIONS("commandModificationBlockLimit"),
	MAX_COMMAND_FORKS("maxCommandForkCount"),
	MAX_COMMAND_SEQUENCE_LENGTH("maxCommandChainLength"),
	MAX_ENTITY_CRAMMING("maxEntityCramming"),
	MAX_MINECART_SPEED("minecartMaxSpeed"),
	MAX_SNOW_ACCUMULATION_HEIGHT("snowAccumulationHeight"),
	MOB_DROPS("doMobLoot"),
	MOB_EXPLOSION_DROP_DECAY("mobExplosionDropDecay"),
	MOB_GRIEFING("mobGriefing"),
	NATURAL_HEALTH_REGENERATION("naturalRegeneration"),
	PLAYER_MOVEMENT_CHECK("disablePlayerMovementCheck"),
	PLAYERS_NETHER_PORTAL_CREATIVE_DELAY("playersNetherPortalCreativeDelay"),
	PLAYERS_NETHER_PORTAL_DEFAULT_DELAY("playersNetherPortalDefaultDelay"),
	PLAYERS_SLEEPING_PERCENTAGE("playersSleepingPercentage"),
	PROJECTILES_CAN_BREAK_BLOCKS("projectilesCanBreakBlocks"),
	PVP("pvp"),
	RAIDS("disableRaids"),
	RANDOM_TICK_SPEED("randomTickSpeed"),
	REDUCED_DEBUG_INFO("reducedDebugInfo"),
	RESPAWN_RADIUS("spawnRadius"),
	SEND_COMMAND_FEEDBACK("sendCommandFeedback"),
	SHOW_ADVANCEMENT_MESSAGES("announceAdvancements"),
	SHOW_DEATH_MESSAGES("showDeathMessages"),
	SPAWN_MOBS("doMobSpawning"),
	SPAWN_MONSTERS("spawnMonsters"),
	SPAWN_PATROLS("doPatrolSpawning"),
	SPAWN_PHANTOMS("doInsomnia"),
	SPAWN_WANDERING_TRADERS("doTraderSpawning"),
	SPAWN_WARDENS("doWardenSpawning"),
	SPAWNER_BLOCKS_WORK("spawnerBlocksEnabled"),
	SPECTATORS_GENERATE_CHUNKS("spectatorsGenerateChunks"),
	SPREAD_VINES("doVinesSpread"),
	TNT_EXPLODES("tntExplodes"),
	TNT_EXPLOSION_DROP_DECAY("tntExplosionDropDecay"),
	UNIVERSAL_ANGER("universalAnger"),
	WATER_SOURCE_CONVERSION("waterSourceConversion");

	private static final Map<String, String> BY_LEGACY_NAME = new HashMap<>();

	public static String getByLegacyName(String name) {
		if(BY_LEGACY_NAME.isEmpty()) {
			for(MCGameRule rule : MCGameRule.values()) {
				if(rule.legacyName != null) {
					if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_21_11)) {
						BY_LEGACY_NAME.put(rule.legacyName.toLowerCase(), rule.getRuleName());
					} else {
						BY_LEGACY_NAME.put(rule.legacyName.toLowerCase(), rule.legacyName);
					}
				}
			}
		}

		name = name.toLowerCase();
		String ruleName = BY_LEGACY_NAME.get(name);
		if(ruleName != null) {
			return ruleName;
		}
		// try removed game rules
		MCVersion version = Static.getServer().getMinecraftVersion();
		if(version.lt(MCVersion.MC1_21_11)) {
			if(name.equals("allowfireticksawayfromplayer")) {
				return "allowFireTicksAwayFromPlayer";
			} else if(name.equals("dofiretick")) {
				return "doFireTick";
			}
			if(version.lt(MCVersion.MC1_21_9)) {
				if(name.equals("spawnchunkradius")) {
					return "spawnChunkRadius";
				}
			}
		}
		return null;
	}

	public static boolean isBoolInvertedFromLegacy(String name) {
		return name.equals("elytra_movement_check")
				|| name.equals("player_movement_check")
				|| name.equals("raids");
	}

	private final String legacyName;

	MCGameRule() {
		this.legacyName = null;
	}

	MCGameRule(String legacyName) {
		this.legacyName = legacyName;
	}

	public static String[] getGameRules() {
		MCGameRule[] values = MCGameRule.values();
		String[] names = new String[values.length];
		for(int i = 0; i < values.length; i++) {
			names[i] = values[i].getRuleName();
		}
		return names;
	}

	public String getRuleName() {
		return this.name().toLowerCase();
	}
}
