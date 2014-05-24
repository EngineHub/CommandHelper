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
	DOFIRETICK("doFireTick"),
	DOMOBLOOT("doMobLoot"),
	DOMOBSPAWNING("doMobSpawning"),
	DOTILEDROPS("doTileDrops"),
	KEEPINVENTORY("keepInventory"),
	MOBGRIEFING("mobGriefing"),
	NATURALREGENERATION("naturalRegeneration");

	private final String gameRule;

	MCGameRule(String gameRule) {
		this.gameRule = gameRule;
	}

    public String getGameRule() {
        return this.gameRule;
    }
}