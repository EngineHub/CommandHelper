package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCLightningStrike;

public interface MCLightningStrikeEvent extends MCWeatherEvent {
	MCLightningStrike getLightning();
}