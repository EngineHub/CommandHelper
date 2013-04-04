package com.laytonsmith.abstraction.enums;

import java.util.ArrayList;
import java.util.List;

/**
 * Criteria names which trigger an objective to be modified by actions in-game
 * 
 * @author jb_aero
 */
public enum MCCriteria {
	DEATH_COUNT("deathCount"),
	HEALTH("health"),
	PLAYER_KILL_COUNT("playerKillCount"),
	TOTAL_KILL_COUNT("totalKillCount"),
	/**
	 * This is the exception, all user defined criteria use this.
	 */
	DUMMY("dummy");
	
	private String criteria;
	
	MCCriteria(String crit) {
		this.criteria = crit;
	}
	
	public String getValue() {
		return this.criteria;
	}
	
	/**
	 * @return List of the criteria automatically incremented by the server
	 */
	public static List<String> auto() {
		List<String> ret = new ArrayList<String>();
		for (MCCriteria c : values()) {
			if (c != MCCriteria.DUMMY) {
				ret.add(c.name());
				ret.add(c.getValue());
			}
		}
		return ret;
	}
	
	public static MCCriteria convert(String value) {
		MCCriteria ret = MCCriteria.DUMMY;
		for (MCCriteria c : values()) {
			if (c.getValue().equals(value)) {
				ret = c;
			}
			if (c.name().equals(value.toUpperCase())) {
				ret = c;
			}
		}
		return ret;
	}
}
