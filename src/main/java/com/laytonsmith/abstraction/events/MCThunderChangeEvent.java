package com.laytonsmith.abstraction.events;

public interface MCThunderChangeEvent extends MCWeatherEvent {
	boolean toThunderState();
}
