package com.laytonsmith.abstraction.enums;

import com.laytonsmith.annotations.MEnum;

@MEnum("com.commandhelper.TeleportCause")
public enum MCTeleportCause {
	COMMAND,
	END_PORTAL,
	ENDER_PEARL,
	NETHER_PORTAL,
	PLUGIN,
	SPECTATE,
	END_GATEWAY,
	CHORUS_FRUIT,
	UNKNOWN
}
