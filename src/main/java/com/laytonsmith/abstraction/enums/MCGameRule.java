package com.laytonsmith.abstraction.enums;

/**
 * Gamerule names
 *
 * @author Hekta
 */
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