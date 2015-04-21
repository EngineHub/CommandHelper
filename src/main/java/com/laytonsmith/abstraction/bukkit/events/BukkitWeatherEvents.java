package com.laytonsmith.abstraction.bukkit.events;

import com.laytonsmith.abstraction.MCLightningStrike;
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

/**
 * @author jacobwgillespie
 */
public final class BukkitWeatherEvents {

    public static abstract class BukkitMCWeatherEvent implements MCWeatherEvent {

        private final WeatherEvent _event;

        public BukkitMCWeatherEvent(WeatherEvent event) {
            _event = event;
        }

        @Override
        public Object _GetObject() {
            return _event;
        }

        @Override
        public MCWorld getWorld() {
            return new BukkitMCWorld(_event.getWorld());
        }
    }

    public static class BukkitMCLightningStrikeEvent extends BukkitMCWeatherEvent implements MCLightningStrikeEvent {

        private final LightningStrikeEvent _event;

        public BukkitMCLightningStrikeEvent(LightningStrikeEvent event) {
            super(event);
            _event = event;
        }

        @Override
        public MCLightningStrike getLightning() {
            return new BukkitMCLightningStrike(_event.getLightning());
        }
    }

    public static class BukkitMCThunderChangeEvent extends BukkitMCWeatherEvent implements MCThunderChangeEvent {

        private final ThunderChangeEvent _event;

        public BukkitMCThunderChangeEvent(ThunderChangeEvent event) {
            super(event);
            _event = event;
        }

        @Override
        public boolean toThunderState() {
            return _event.toThunderState();
        }
    }

    public static class BukkitMCWeatherChangeEvent extends BukkitMCWeatherEvent implements MCWeatherChangeEvent {

        private final WeatherChangeEvent _event;

        public BukkitMCWeatherChangeEvent(WeatherChangeEvent event) {
            super(event);
            _event = event;
        }

        @Override
        public boolean toWeatherState() {
            return _event.toWeatherState();
        }
    }
}
