package com.laytonsmith.abstraction.bukkit.events.drivers;

import com.laytonsmith.abstraction.bukkit.events.BukkitWorldEvents;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.event.world.WorldUnloadEvent;

public class BukkitWorldListener implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	public void onStructureGrow(StructureGrowEvent event) {
		EventUtils.TriggerListener(Driver.TREE_GROW, "tree_grow",
				new BukkitWorldEvents.BukkitMCStructureGrowEvent(event));
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onWorldSave(WorldSaveEvent event) {
		EventUtils.TriggerListener(Driver.WORLD_SAVE, "world_save",
				new BukkitWorldEvents.BukkitMCWorldSaveEvent(event));
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onWorldUnload(WorldUnloadEvent event) {
		EventUtils.TriggerListener(Driver.WORLD_UNLOAD, "world_unload",
				new BukkitWorldEvents.BukkitMCWorldUnloadEvent(event));
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onWorldLoad(WorldLoadEvent event) {
		EventUtils.TriggerListener(Driver.WORLD_LOAD, "world_load",
				new BukkitWorldEvents.BukkitMCWorldLoadEvent(event));
	}
}
