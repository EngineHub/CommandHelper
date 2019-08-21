package com.laytonsmith.abstraction.bukkit.events;

import com.laytonsmith.abstraction.entities.MCLightningStrike;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.bukkit.BukkitMCWorld;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCLightningStrike;
import com.laytonsmith.abstraction.events.MCLightningStrikeEvent;
import com.laytonsmith.abstraction.events.MCThunderChangeEvent;
import com.laytonsmith.abstraction.events.MCWeatherChangeEvent;
import com.laytonsmith.abstraction.events.MCWeatherEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.weather.WeatherEvent;

public final class BukkitWeatherEvents {

	public abstract static class BukkitMCWeatherEvent implements MCWeatherEvent {

		private final WeatherEvent event;

		public BukkitMCWeatherEvent(WeatherEvent event) {
			this.event = event;
		}

		@Override
		public Object _GetObject() {
			return this.event;
		}

		@Override
		public MCWorld getWorld() {
			return new BukkitMCWorld(this.event.getWorld());
		}
	}

	public static class BukkitMCLightningStrikeEvent extends BukkitMCWeatherEvent implements MCLightningStrikeEvent {

		private final LightningStrikeEvent event;

		public BukkitMCLightningStrikeEvent(LightningStrikeEvent event) {
			super(event);
			this.event = event;
		}

		@Override
		public MCLightningStrike getLightning() {
			return new BukkitMCLightningStrike(this.event.getLightning());
		}
	}

	public static class BukkitMCThunderChangeEvent extends BukkitMCWeatherEvent implements MCThunderChangeEvent {

		private final ThunderChangeEvent event;

		public BukkitMCThunderChangeEvent(ThunderChangeEvent event) {
			super(event);
			this.event = event;
		}

		@Override
		public boolean toThunderState() {
			return this.event.toThunderState();
		}
	}

	public static class BukkitMCWeatherChangeEvent extends BukkitMCWeatherEvent implements MCWeatherChangeEvent {

		private final WeatherChangeEvent event;

		public BukkitMCWeatherChangeEvent(WeatherChangeEvent event) {
			super(event);
			this.event = event;
		}

		@Override
		public boolean toWeatherState() {
			return this.event.toWeatherState();
		}
	}
}
