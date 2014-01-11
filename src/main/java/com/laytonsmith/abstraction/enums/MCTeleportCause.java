package com.laytonsmith.abstraction.enums;

import com.laytonsmith.annotations.MEnum;

@MEnum("TeleportCause")
public enum MCTeleportCause {
	COMMAND,
	END_PORTAL,
	ENDER_PEARL,
	NETHER_PORTAL,
	PLUGIN,
	UNKNOWN
}
