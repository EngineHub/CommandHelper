package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.Weather;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCWeather;

/**
 *
 * @author Hekta
 */
public class BukkitMCWeather extends BukkitMCEntity implements MCWeather {

	public BukkitMCWeather(Weather weather) {
		super(weather);
	}

	public BukkitMCWeather(AbstractionObject ao) {
		this((Weather) ao.getHandle());
	}

	@Override
	public Weather getHandle() {
		return (Weather) metadatable;
	}
}