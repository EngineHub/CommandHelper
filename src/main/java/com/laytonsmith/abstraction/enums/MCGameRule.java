package com.laytonsmith.abstraction.enums;

import com.laytonsmith.annotations.MEnum;
import com.laytonsmith.core.constructs.Construct.ConstructType;

@MEnum("com.commandhelper.GameRule")
public enum MCGameRule {
	ANNOUNCEADVANCEMENTS("announceAdvancements"),
	COMMANDBLOCKOUTPUT("commandBlockOutput"),
	DISABLEELYTRAMOVEMENTCHECK("disableElytraMovementCheck"),
	DODAYLIGHTCYCLE("doDaylightCycle"),
	DOENTITYDROPS("doEntityDrops"),
	DOFIRETICK("doFireTick"),
	DOLIMITEDCRAFTING("doLimitedCrafting"),
	DOMOBLOOT("doMobLoot"),
	DOMOBSPAWNING("doMobSpawning"),
	DOTILEDROPS("doTileDrops"),
	DOWEATHERCYCLE("doWeatherCycle"),
	KEEPINVENTORY("keepInventory"),
	LOGADMINCOMMANDS("logAdminCommands"),
	MAXCOMMANDCHAINLENGTH("maxCommandChainLength", ConstructType.INT),
	MAXENTITYCRAMMING("maxEntityCramming", ConstructType.INT),
	MOBGRIEFING("mobGriefing"),
	NATURALREGENERATION("naturalRegeneration"),
	RANDOMTICKSPEED("randomTickSpeed", ConstructType.INT),
	REDUCEDDEBUGINFO("reducedDebugInfo"),
	SENDCOMMANDFEEDBACK("sendCommandFeedback"),
	SHOWDEATHMESSAGES("showDeathMessages"),
	SPAWNRADIUS("spawnRadius", ConstructType.INT),
	SPECTATORSGENERATECHUNKS("spectatorsGenerateChunks");

	private final String gameRule;
	private final ConstructType ruleType;

	MCGameRule(String gameRule) {
		this.gameRule = gameRule;
		this.ruleType = ConstructType.BOOLEAN;
	}

	MCGameRule(String gameRule, ConstructType type) {
		this.gameRule = gameRule;
		this.ruleType = type;
	}

	public String getGameRule() {
		return this.gameRule;
	}

	public ConstructType getRuleType() {
		return this.ruleType;
	}
}
