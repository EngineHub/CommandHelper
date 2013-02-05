

package com.laytonsmith.abstraction.bukkit.events.drivers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import com.laytonsmith.abstraction.bukkit.events.BukkitInventoryEvents;
import com.laytonsmith.abstraction.bukkit.events.BukkitInventoryEvents.BukkitMCInventoryClickEvent;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventUtils;

/**
 *
 * @author Layton
 */
public class BukkitInventoryListener implements Listener{
    
	@EventHandler(priority=EventPriority.LOWEST)
	public void onInvClick(InventoryClickEvent event) {
		BukkitMCInventoryClickEvent ice = new BukkitInventoryEvents.BukkitMCInventoryClickEvent(event);
		EventUtils.TriggerListener(Driver.INVENTORY_CLICK, "inventory_click", ice);
	}
}
