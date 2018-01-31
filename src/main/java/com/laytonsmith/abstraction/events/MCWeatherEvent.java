package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.core.events.BindableEvent;

public interface MCWeatherEvent extends BindableEvent {
	MCWorld getWorld();
}
