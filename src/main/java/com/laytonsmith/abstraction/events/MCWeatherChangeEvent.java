package com.laytonsmith.abstraction.events;

public interface MCWeatherChangeEvent extends MCWeatherEvent {
	boolean toWeatherState();
}
