package com.laytonsmith.abstraction.bukkit.events;

import com.laytonsmith.abstraction.MCCraftingInventory;
import com.laytonsmith.abstraction.MCEnchantment;
import com.laytonsmith.abstraction.MCEnchantmentOffer;
import com.laytonsmith.abstraction.MCHumanEntity;
import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.MCInventoryView;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCRecipe;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.bukkit.BukkitConvertor;
import com.laytonsmith.abstraction.bukkit.BukkitMCCraftingInventory;
import com.laytonsmith.abstraction.bukkit.BukkitMCEnchantment;
import com.laytonsmith.abstraction.bukkit.BukkitMCEnchantmentOffer;
import com.laytonsmith.abstraction.bukkit.BukkitMCInventory;
import com.laytonsmith.abstraction.bukkit.BukkitMCInventoryView;
import com.laytonsmith.abstraction.bukkit.BukkitMCItemStack;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlock;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCHumanEntity;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCPlayer;
import com.laytonsmith.abstraction.enums.MCClickType;
import com.laytonsmith.abstraction.enums.MCDragType;
import com.laytonsmith.abstraction.enums.MCInventoryAction;
import com.laytonsmith.abstraction.enums.MCResult;
import com.laytonsmith.abstraction.enums.MCSlotType;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCClickType;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCInventoryAction;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCResult;
import com.laytonsmith.abstraction.events.MCEnchantItemEvent;
import com.laytonsmith.abstraction.events.MCInventoryClickEvent;
import com.laytonsmith.abstraction.events.MCInventoryCloseEvent;
import com.laytonsmith.abstraction.events.MCInventoryDragEvent;
import com.laytonsmith.abstraction.events.MCInventoryEvent;
import com.laytonsmith.abstraction.events.MCInventoryInteractEvent;
import com.laytonsmith.abstraction.events.MCInventoryOpenEvent;
import com.laytonsmith.abstraction.events.MCItemHeldEvent;
import com.laytonsmith.abstraction.events.MCItemSwapEvent;
import com.laytonsmith.abstraction.events.MCPrepareItemCraftEvent;
import com.laytonsmith.abstraction.events.MCPrepareItemEnchantEvent;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Result;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BukkitInventoryEvents {

	public static class BukkitMCInventoryEvent implements MCInventoryEvent {

		InventoryEvent event;

		public BukkitMCInventoryEvent(InventoryEvent e) {
			event = e;
		}

		@Override
		public List<MCHumanEntity> getViewers() {
			List<MCHumanEntity> viewers = new ArrayList<>();
			for(HumanEntity viewer : event.getViewers()) {
				viewers.add(new BukkitMCHumanEntity(viewer));
			}
			return viewers;
		}

		@Override
		public MCInventoryView getView() {
			return new BukkitMCInventoryView(event.getView());
		}

		@Override
		public MCInventory getInventory() {
			return new BukkitMCInventory(event.getInventory());
		}

		@Override
		public Object _GetObject() {
			return event;
		}
	}

	public static class BukkitMCInventoryInteractEvent extends BukkitMCInventoryEvent implements MCInventoryInteractEvent {

		InventoryInteractEvent iie;

		public BukkitMCInventoryInteractEvent(InventoryInteractEvent e) {
			super(e);
			iie = e;
		}

		@Override
		public MCHumanEntity getWhoClicked() {
			return new BukkitMCHumanEntity(iie.getWhoClicked());
		}

		@Override
		public void setResult(MCResult newResult) {
			iie.setResult(Result.valueOf(newResult.name()));
		}

		@Override
		public MCResult getResult() {
			return BukkitMCResult.getConvertor().getAbstractedEnum(iie.getResult());
		}

		@Override
		public boolean isCanceled() {
			return iie.isCancelled();
		}

		@Override
		public void setCancelled(boolean cancelled) {
			iie.setCancelled(cancelled);
		}
	}

	public static class BukkitMCInventoryOpenEvent extends BukkitMCInventoryEvent implements MCInventoryOpenEvent {

		InventoryOpenEvent ioe;

		public BukkitMCInventoryOpenEvent(InventoryOpenEvent e) {
			super(e);
			ioe = e;
		}

		@Override
		public MCHumanEntity getPlayer() {
			return new BukkitMCHumanEntity(ioe.getPlayer());
		}
	}

	public static class BukkitMCInventoryCloseEvent extends BukkitMCInventoryEvent implements MCInventoryCloseEvent {

		InventoryCloseEvent ice;

		public BukkitMCInventoryCloseEvent(InventoryCloseEvent e) {
			super(e);
			ice = e;
		}

		@Override
		public MCHumanEntity getPlayer() {
			return new BukkitMCHumanEntity(ice.getPlayer());
		}
	}

	public static class BukkitMCInventoryClickEvent extends BukkitMCInventoryInteractEvent implements MCInventoryClickEvent {

		InventoryClickEvent ic;

		public BukkitMCInventoryClickEvent(InventoryClickEvent e) {
			super(e);
			this.ic = e;
		}

		@Override
		public MCItemStack getCurrentItem() {
			return new BukkitMCItemStack(ic.getCurrentItem());
		}

		@Override
		public MCItemStack getCursor() {
			return new BukkitMCItemStack(ic.getCursor());
		}

		@Override
		public int getSlot() {
			return ic.getSlot();
		}

		@Override
		public int getRawSlot() {
			return ic.getRawSlot();
		}

		@Override
		public int getHotbarButton() {
			return ic.getHotbarButton();
		}

		@Override
		public MCSlotType getSlotType() {
			return MCSlotType.valueOf(ic.getSlotType().name());
		}

		@Override
		public boolean isLeftClick() {
			return ic.getClick().isLeftClick();
		}

		@Override
		public boolean isRightClick() {
			return ic.getClick().isRightClick();
		}

		@Override
		public boolean isShiftClick() {
			return ic.getClick().isShiftClick();
		}

		@Override
		public boolean isCreativeClick() {
			return ic.getClick().isCreativeAction();
		}

		@Override
		public boolean isKeyboardClick() {
			return ic.getClick().isKeyboardClick();
		}

		@Override
		public void setCurrentItem(MCItemStack slot) {
			if(slot != null) {
				ic.setCurrentItem(((BukkitMCItemStack) slot).asItemStack());
			} else {
				ic.setCurrentItem(null);
			}
		}

		@Override
		public void setCursor(MCItemStack cursor) {
			// deprecated in 1.5 because it can create client/server desync
			ic.setCursor(((BukkitMCItemStack) cursor).asItemStack());
		}

		@Override
		public MCInventoryAction getAction() {
			return BukkitMCInventoryAction.getConvertor().getAbstractedEnum(ic.getAction());
		}

		@Override
		public MCClickType getClickType() {
			return BukkitMCClickType.getConvertor().getAbstractedEnum((ic.getClick()));
		}
	}

	public static class BukkitMCInventoryDragEvent extends BukkitMCInventoryInteractEvent implements MCInventoryDragEvent {

		InventoryDragEvent id;

		public BukkitMCInventoryDragEvent(InventoryDragEvent e) {
			super(e);
			this.id = e;
		}

		@Override
		public Map<Integer, MCItemStack> getNewItems() {
			Map<Integer, MCItemStack> ret = new HashMap<>();

			for(Map.Entry<Integer, ItemStack> ni : id.getNewItems().entrySet()) {
				Integer key = ni.getKey();
				ItemStack value = ni.getValue();
				ret.put(key, new BukkitMCItemStack(value));
			}
			return ret;
		}

		@Override
		public Set<Integer> getRawSlots() {
			Set<Integer> ret = new HashSet<>();
			for(Integer rs : id.getRawSlots()) {
				ret.add(rs);
			}
			return ret;
		}

		@Override
		public Set<Integer> getInventorySlots() {
			Set<Integer> ret = new HashSet<>();
			for(Integer is : id.getInventorySlots()) {
				ret.add(is);
			}
			return ret;
		}

		@Override
		public MCItemStack getCursor() {
			return new BukkitMCItemStack(id.getCursor());
		}

		@Override
		public void setCursor(MCItemStack newCursor) {
			id.setCursor(((BukkitMCItemStack) newCursor).asItemStack());
		}

		@Override
		public MCItemStack getOldCursor() {
			return new BukkitMCItemStack(id.getOldCursor());
		}

		@Override
		public MCDragType getType() {
			return MCDragType.valueOf(id.getType().name());
		}
	}

	public static class BukkitMCEnchantItemEvent extends BukkitMCInventoryEvent implements MCEnchantItemEvent {

		EnchantItemEvent ei;

		public BukkitMCEnchantItemEvent(EnchantItemEvent e) {
			super(e);
			this.ei = e;
		}

		@Override
		public MCBlock getEnchantBlock() {
			return new BukkitMCBlock(ei.getEnchantBlock());
		}

		@Override
		public MCPlayer GetEnchanter() {
			return new BukkitMCPlayer(ei.getEnchanter());
		}

		@Override
		public Map<MCEnchantment, Integer> getEnchantsToAdd() {
			Map<MCEnchantment, Integer> ret = new HashMap<>();
			for(Map.Entry<Enchantment, Integer> ea : ei.getEnchantsToAdd().entrySet()) {
				Enchantment key = ea.getKey();
				Integer value = ea.getValue();
				ret.put(new BukkitMCEnchantment(key), value);
			}
			return ret;
		}

		@Override
		public void setEnchantsToAdd(Map<MCEnchantment, Integer> enchants) {
			Map<Enchantment, Integer> ret = ei.getEnchantsToAdd();
			ret.clear();

//			for(Map.Entry<MCEnchantment, Integer> ea : enchants.entrySet()) {
//				MCEnchantment key = ea.getKey();
//				Integer value = ea.getValue();
//				ret.put(((BukkitMCEnchantment) key).asEnchantment(), value);
//			}
			Map<Enchantment, Integer> enchantments = new HashMap<>();

			for(Map.Entry<MCEnchantment, Integer> ea : enchants.entrySet()) {
				MCEnchantment key = ea.getKey();
				Integer value = ea.getValue();
				enchantments.put(((BukkitMCEnchantment) key).asEnchantment(), value);
			}

			ItemStack item = ei.getItem();
			item.addUnsafeEnchantments(enchantments);
		}

		@Override
		public MCItemStack getItem() {
			return new BukkitMCItemStack(ei.getItem());
		}

		@Override
		public void setItem(MCItemStack i) {
			ItemStack item = ei.getItem();
			ItemStack is = ((BukkitMCItemStack) i).asItemStack();

			item.setAmount(is.getAmount());
			item.setType(is.getType());
			item.setItemMeta(is.getItemMeta());
		}

		@Override
		public void setExpLevelCost(int level) {
			ei.setExpLevelCost(level);
		}

		@Override
		public int getExpLevelCost() {
			return ei.getExpLevelCost();
		}

		@Override
		public int whichButton() {
			return ei.whichButton();
		}
	}

	public static class BukkitMCPrepareItemEnchantEvent extends BukkitMCInventoryEvent implements MCPrepareItemEnchantEvent {

		PrepareItemEnchantEvent pie;

		public BukkitMCPrepareItemEnchantEvent(PrepareItemEnchantEvent e) {
			super(e);
			this.pie = e;
		}

		@Override
		public MCBlock getEnchantBlock() {
			return new BukkitMCBlock(pie.getEnchantBlock());
		}

		@Override
		public MCPlayer getEnchanter() {
			return new BukkitMCPlayer(pie.getEnchanter());
		}

		@Override
		public int getEnchantmentBonus() {
			return pie.getEnchantmentBonus();
		}

		@Override
		public MCEnchantmentOffer[] getOffers() {
			EnchantmentOffer[] offers = pie.getOffers();
			MCEnchantmentOffer[] ret = new MCEnchantmentOffer[offers.length];
			for(int i = 0; i < offers.length; i++) {
				ret[i] = new BukkitMCEnchantmentOffer(offers[i]);
			}
			return ret;
		}

		@Override
		public MCItemStack getItem() {
			return new BukkitMCItemStack(pie.getItem());
		}

		@Override
		public void setItem(MCItemStack i) {
			ItemStack item = pie.getItem();
			ItemStack is = ((BukkitMCItemStack) i).asItemStack();

			item.setAmount(is.getAmount());
			item.setType(is.getType());
			item.setItemMeta(is.getItemMeta());
		}
	}

	public static class BukkitMCItemHeldEvent implements MCItemHeldEvent {

		PlayerItemHeldEvent ih;

		public BukkitMCItemHeldEvent(PlayerItemHeldEvent event) {
			ih = event;
		}

		@Override
		public MCPlayer getPlayer() {
			return new BukkitMCPlayer(ih.getPlayer());
		}

		@Override
		public Object _GetObject() {
			return ih;
		}

		@Override
		public int getNewSlot() {
			return ih.getNewSlot();
		}

		@Override
		public int getPreviousSlot() {
			return ih.getPreviousSlot();
		}
	}

	public static class BukkitMCItemSwapEvent implements MCItemSwapEvent {

		PlayerSwapHandItemsEvent is;

		public BukkitMCItemSwapEvent(PlayerSwapHandItemsEvent event) {
			is = event;
		}

		public BukkitMCItemSwapEvent(Event event) {
			is = (PlayerSwapHandItemsEvent) event;
		}

		@Override
		public MCPlayer getPlayer() {
			return new BukkitMCPlayer(is.getPlayer());
		}

		@Override
		public Object _GetObject() {
			return is;
		}

		@Override
		public MCItemStack getMainHandItem() {
			return new BukkitMCItemStack(is.getMainHandItem());
		}

		@Override
		public MCItemStack getOffHandItem() {
			return new BukkitMCItemStack(is.getOffHandItem());
		}

		@Override
		public void setMainHandItem(MCItemStack item) {
			is.setMainHandItem((ItemStack) item.getHandle());
		}

		@Override
		public void setOffHandItem(MCItemStack item) {
			is.setOffHandItem((ItemStack) item.getHandle());
		}
	}

	public static class BukkitMCPrepareItemCraftEvent extends BukkitMCInventoryEvent implements MCPrepareItemCraftEvent {

		PrepareItemCraftEvent e;

		public BukkitMCPrepareItemCraftEvent(PrepareItemCraftEvent event) {
			super(event);
			e = event;
		}

		@Override
		public MCRecipe getRecipe() {
			return BukkitConvertor.BukkitGetRecipe(e.getRecipe());
		}

		@Override
		public boolean isRepair() {
			return e.isRepair();
		}

		@Override
		public MCCraftingInventory getInventory() {
			return new BukkitMCCraftingInventory(e.getInventory());
		}
	}
}
