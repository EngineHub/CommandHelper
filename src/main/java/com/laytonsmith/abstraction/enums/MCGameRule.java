package com.laytonsmith.abstraction.enums;

import com.laytonsmith.annotations.MEnum;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.natives.interfaces.Mixed;

@MEnum("com.commandhelper.GameRule")
public enum MCGameRule {
	ANNOUNCEADVANCEMENTS("announceAdvancements"),
	BLOCKEXPLOSIONDROPDECAY("blockExplosionDropDecay"),
	COMMANDBLOCKOUTPUT("commandBlockOutput"),
	COMMANDMODIFICATIONBLOCKLIMIT("commandModificationBlockLimit"),
	DISABLEELYTRAMOVEMENTCHECK("disableElytraMovementCheck"),
	DISABLERAIDS("disableRaids"),
	DODAYLIGHTCYCLE("doDaylightCycle"),
	DOENTITYDROPS("doEntityDrops"),
	DOFIRETICK("doFireTick"),
	DOIMMEDIATERESPAWN("doImmediateRespawn"),
	DOINSOMNIA("doInsomnia"),
	DOLIMITEDCRAFTING("doLimitedCrafting"),
	DOMOBLOOT("doMobLoot"),
	DOMOBSPAWNING("doMobSpawning"),
	DOPATROLSPAWNING("doPatrolSpawning"),
	DOTILEDROPS("doTileDrops"),
	DOTRADERSPAWNING("doTraderSpawning"),
	DOVINESSPREAD("doVinesSpread"),
	DOWARDENSPAWNING("doWardenSpawning"),
	DOWEATHERCYCLE("doWeatherCycle"),
	DROWNINGDAMAGE("drowningDamage"),
	ENDERPEARLSVANISHONDEATH("enderPearlsVanishOnDeath"),
	FALLDAMAGE("fallDamage"),
	FIREDAMAGE("fireDamage"),
	FORGIVEDEADPLAYERS("forgiveDeadPlayers"),
	FREEZEDAMAGE("freezeDamage"),
	GLOBALSOUNDEVENTS("globalSoundEvents"),
	KEEPINVENTORY("keepInventory"),
	LAVASOURCECONVERSION("lavaSourceConversion"),
	LOGADMINCOMMANDS("logAdminCommands"),
	MAXCOMMANDCHAINLENGTH("maxCommandChainLength", CInt.class),
	MAXCOMMANDFORKCOUNT("maxCommandForkCount", CInt.class),
	MAXENTITYCRAMMING("maxEntityCramming", CInt.class),
	MOBEXPLOSIONDROPDECAY("mobExplosionDropDecay"),
	MOBGRIEFING("mobGriefing"),
	NATURALREGENERATION("naturalRegeneration"),
	PLAYERSNETHERPORTALCREATIVEDELAY("playersNetherPortalCreativeDelay", CInt.class),
	PLAYERSNETHERPORTALDEFAULTDELAY("playersNetherPortalDefaultDelay", CInt.class),
	PLAYERSSLEEPINGPERCENTAGE("playersSleepingPercentage", CInt.class),
	PROJECTILESCANBREAKBLOCKS("projectilesCanBreakBlocks"),
	RANDOMTICKSPEED("randomTickSpeed", CInt.class),
	REDUCEDDEBUGINFO("reducedDebugInfo"),
	SENDCOMMANDFEEDBACK("sendCommandFeedback"),
	SHOWDEATHMESSAGES("showDeathMessages"),
	SNOWACCUMULATIONHEIGHT("snowAccumulationHeight", CInt.class),
	SPAWNCHUNKRADIUS("spawnChunkRadius", CInt.class),
	SPAWNRADIUS("spawnRadius", CInt.class),
	SPECTATORSGENERATECHUNKS("spectatorsGenerateChunks"),
	TNTEXPLOSIONDROPDECAY("tntExplosionDropDecay"),
	UNIVERSALANGER("universalAnger"),
	WATERSOURCECONVERSION("waterSourceConversion");

	private final String gameRule;
	private final Class<? extends Mixed> ruleType;

	MCGameRule(String gameRule) {
		this.gameRule = gameRule;
		this.ruleType = CBoolean.class;
	}

	MCGameRule(String gameRule, Class<? extends Mixed> type) {
		this.gameRule = gameRule;
		this.ruleType = type;
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
		return this.gameRule;
	}

	public Class<? extends Mixed> getRuleType() {
		return this.ruleType;
	}
}
