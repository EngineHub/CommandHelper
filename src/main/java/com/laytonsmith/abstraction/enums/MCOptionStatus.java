package com.laytonsmith.abstraction.enums;

import com.laytonsmith.annotations.MEnum;

@MEnum("com.commandhelper.OptionStatus")
public enum MCOptionStatus {
	ALWAYS,
	FOR_OTHER_TEAMS,
	FOR_OWN_TEAM,
	NEVER
}
