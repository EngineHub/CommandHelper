package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.entities.MCLightningStrike;

public interface MCLightningStrikeEvent extends MCWeatherEvent {

	MCLightningStrike getLightning();

	Cause getCause();

	enum Cause {
		COMMAND,
		CUSTOM,
		ENCHANTMENT,
		SPAWNER,
		TRIDENT,
		TRAP,
		WEATHER,
		UNKNOWN
	}
}
