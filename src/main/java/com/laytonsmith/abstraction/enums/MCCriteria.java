package com.laytonsmith.abstraction.enums;

import com.laytonsmith.annotations.MEnum;

/**
 * Criteria names which trigger an objective to be modified by actions in-game
 *
 */
@MEnum("com.commandhelper.Criteria")
public enum MCCriteria {
	DEATHCOUNT("deathCount"),
	HEALTH("health"),
	PLAYERKILLCOUNT("playerKillCount"),
	TOTALKILLCOUNT("totalKillCount"),
	DUMMY("dummy");

	private final String criteria;

	MCCriteria(String crit) {
		criteria = crit;
	}

	public String getCriteria() {
		return criteria;
	}
}
