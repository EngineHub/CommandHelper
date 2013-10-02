package com.laytonsmith.abstraction.bukkit.events.drivers;

import com.laytonsmith.abstraction.bukkit.events.BukkitInventoryEvents;
import com.laytonsmith.abstraction.bukkit.events.BukkitInventoryEvents.BukkitMCEnchantItemEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitInventoryEvents.BukkitMCInventoryClickEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitInventoryEvents.BukkitMCInventoryCloseEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitInventoryEvents.BukkitMCInventoryDragEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitInventoryEvents.BukkitMCInventoryOpenEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitInventoryEvents.BukkitMCItemHeldEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitInventoryEvents.BukkitMCPrepareItemCraftEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitInventoryEvents.BukkitMCPrepareItemEnchantEvent;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;

/**
 *
 * @author Layton
 */
public class BukkitInventoryListener implements Listener{
    
	@EventHandler(priority=EventPriority.LOWEST)
	public void onInvClick(InventoryClickEvent event) {
		BukkitMCInventoryClickEvent ice = new BukkitInventoryEvents.BukkitMCInventoryClickEvent(event);
		EventUtils.TriggerExternal(ice);
		EventUtils.TriggerListener(Driver.INVENTORY_CLICK, "inventory_click", ice);
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onInvDrag(InventoryDragEvent event) {
		BukkitMCInventoryDragEvent ide = new BukkitInventoryEvents.BukkitMCInventoryDragEvent(event);
		EventUtils.TriggerExternal(ide);
		EventUtils.TriggerListener(Driver.INVENTORY_DRAG, "inventory_drag", ide);
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onInvOpen(InventoryOpenEvent event) {
		BukkitMCInventoryOpenEvent ioe = new BukkitInventoryEvents.BukkitMCInventoryOpenEvent(event);
		EventUtils.TriggerExternal(ioe);
		EventUtils.TriggerListener(Driver.INVENTORY_OPEN, "inventory_open", ioe);
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onInvClose(InventoryCloseEvent event) {
		BukkitMCInventoryCloseEvent ice = new BukkitInventoryEvents.BukkitMCInventoryCloseEvent(event);
		EventUtils.TriggerExternal(ice);
		EventUtils.TriggerListener(Driver.INVENTORY_CLOSE, "inventory_close", ice);
	}
	
	@EventHandler(priority= EventPriority.LOWEST)
	public void onItemEnchant(EnchantItemEvent event) {
		BukkitMCEnchantItemEvent eie = new BukkitInventoryEvents.BukkitMCEnchantItemEvent(event);
		EventUtils.TriggerExternal(eie);
		EventUtils.TriggerListener(Driver.ITEM_ENCHANT, "item_enchant", eie);
	}
	
	@EventHandler(priority= EventPriority.LOWEST)
	public void onPreEnchant(PrepareItemEnchantEvent event) {
		BukkitMCPrepareItemEnchantEvent pie = new BukkitInventoryEvents.BukkitMCPrepareItemEnchantEvent(event);
		EventUtils.TriggerExternal(pie);
		EventUtils.TriggerListener(Driver.ITEM_PRE_ENCHANT, "item_pre_enchant", pie);
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onItemHeld(PlayerItemHeldEvent event) {
		BukkitMCItemHeldEvent ih = new BukkitInventoryEvents.BukkitMCItemHeldEvent(event);
		EventUtils.TriggerExternal(ih);
		EventUtils.TriggerListener(Driver.ITEM_HELD, "item_held", ih);
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onPreCraft(PrepareItemCraftEvent event) {
		BukkitMCPrepareItemCraftEvent pc = new BukkitInventoryEvents.BukkitMCPrepareItemCraftEvent(event);
		EventUtils.TriggerExternal(pc);
		EventUtils.TriggerListener(Driver.ITEM_PRE_CRAFT, "item_pre_craft", pc);
	}
}
