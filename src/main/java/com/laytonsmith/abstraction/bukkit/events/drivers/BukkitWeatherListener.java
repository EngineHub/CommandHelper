package com.laytonsmith.abstraction.bukkit.events.drivers;

import com.laytonsmith.abstraction.bukkit.events.BukkitWeatherEvents;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

/**
 * @author jacobwgillespie
 */
public class BukkitWeatherListener implements Listener{
    @EventHandler(priority = EventPriority.LOWEST)
    public void onLightningStrike(LightningStrikeEvent event) {
        EventUtils.TriggerListener(Driver.LIGHTNING_STRIKE, "lightning_strike", new BukkitWeatherEvents.BukkitMCLightningStrikeEvent(event));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onThunderChange(ThunderChangeEvent event) {
        EventUtils.TriggerListener(Driver.THUNDER_CHANGE, "thunder_change", new BukkitWeatherEvents.BukkitMCThunderChangeEvent(event));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWeatherChange(WeatherChangeEvent event) {
        EventUtils.TriggerListener(Driver.WEATHER_CHANGE, "weather_change", new BukkitWeatherEvents.BukkitMCWeatherChangeEvent(event));
    }
}
