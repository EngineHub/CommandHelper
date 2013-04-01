package com.laytonsmith.abstraction.enums.bukkit;

import org.bukkit.WeatherType;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCWeather;
import com.laytonsmith.annotations.abstractionenum;

/**
 * 
 * @author jb_aero
 */
@abstractionenum(
		implementation=Implementation.Type.BUKKIT,
		forAbstractEnum=MCWeather.class,
		forConcreteEnum=WeatherType.class
		)
public class BukkitMCWeather extends EnumConvertor<MCWeather, WeatherType> {

	private static BukkitMCWeather instance;

	public static BukkitMCWeather getConvertor() {
		if (instance == null) {
			instance = new BukkitMCWeather();
		}
		return instance;
	}
}
