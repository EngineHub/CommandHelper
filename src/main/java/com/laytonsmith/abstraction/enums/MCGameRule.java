package com.laytonsmith.abstraction.enums;

import com.laytonsmith.annotations.MEnum;

/**
 * Gamerule names
 *
 * @author Hekta
 */
@MEnum("GameRule")
public enum MCGameRule {
	COMMANDBLOCKOUTPUT("commandBlockOutput"),
	DODAYLIGHTCYCLE("doDaylightCycle"),
	DOENTITYDROPS("doEntityDrops"),
	DOFIRETICK("doFireTick"),
	DOMOBLOOT("doMobLoot"),
	DOMOBSPAWNING("doMobSpawning"),
	DOTILEDROPS("doTileDrops"),
	KEEPINVENTORY("keepInventory"),
	LOGADMINCOMMANDS("logAdminCommands"),
	MOBGRIEFING("mobGriefing"),
	NATURALREGENERATION("naturalRegeneration"),
	RANDOMTICKSPEED("randomTickSpeed"),
	REDUCEDDEBUGINFO("reducedDebugInfo"),
	SENDCOMMANDFEEDBACK("sendCommandFeedback"),
	SHOWDEATHMESSAGES("showDeathMessages");

	private final String gameRule;

	MCGameRule(String gameRule) {
		this.gameRule = gameRule;
	}

    public String getGameRule() {
        return this.gameRule;
    }
}
