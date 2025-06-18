package com.laytonsmith.abstraction.enums;

import com.laytonsmith.annotations.MEnum;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.natives.interfaces.Mixed;

@MEnum("com.commandhelper.GameRule")
public enum MCGameRule {
	ALLOWFIRETICKSAWAYFROMPLAYER("allowFireTicksAwayFromPlayer"),
	ANNOUNCEADVANCEMENTS("announceAdvancements"),
	BLOCKEXPLOSIONDROPDECAY("blockExplosionDropDecay"),
	COMMANDBLOCKOUTPUT("commandBlockOutput"),
	COMMANDMODIFICATIONBLOCKLIMIT("commandModificationBlockLimit", CInt.class),
	DISABLEELYTRAMOVEMENTCHECK("disableElytraMovementCheck"),
	DISABLEPLAYERMOVEMENTCHECK("disablePlayerMovementCheck"),
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
	LOCATORBAR("locatorBar"),
	LOGADMINCOMMANDS("logAdminCommands"),
	MAXCOMMANDCHAINLENGTH("maxCommandChainLength", CInt.class),
	MAXCOMMANDFORKCOUNT("maxCommandForkCount", CInt.class),
	MAXENTITYCRAMMING("maxEntityCramming", CInt.class),
	MINECARTMAXSPEED("minecartMaxSpeed", CInt.class),
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
	TNTEXPLODES("tntExplodes"),
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

	public Object convertValue(Mixed value, Target t) {
		if(this.ruleType == CBoolean.class) {
			return ArgumentValidation.getBooleanish(value, t);
		} else if(this.ruleType == CInt.class) {
			return ArgumentValidation.getInt(value, t);
		}
		MSLog.GetLogger().e(MSLog.Tags.RUNTIME, "The gamerule \"" + this.gameRule + "\""
				+ " has an invalid type.", t);
		return null;
	}

	public Mixed constructValue(Object value, Target t) {
		try {
			if(this.ruleType == CBoolean.class) {
				return CBoolean.get((Boolean) value);
			} else if(this.ruleType == CInt.class) {
				return new CInt((Integer) value, t);
			}
		} catch(ClassCastException ex) {}
		MSLog.GetLogger().e(MSLog.Tags.RUNTIME, "The gamerule \"" + this.gameRule + "\""
				+ " has an invalid type.", t);
		return new CString(value.toString(), t);
	}
}
