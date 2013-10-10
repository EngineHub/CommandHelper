package com.laytonsmith.abstraction.bukkit.events;

import com.laytonsmith.abstraction.MCCraftingInventory;
import com.laytonsmith.abstraction.MCEnchantment;
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
import com.laytonsmith.abstraction.bukkit.BukkitMCHumanEntity;
import com.laytonsmith.abstraction.bukkit.BukkitMCInventory;
import com.laytonsmith.abstraction.bukkit.BukkitMCInventoryView;
import com.laytonsmith.abstraction.bukkit.BukkitMCItemStack;
import com.laytonsmith.abstraction.bukkit.BukkitMCPlayer;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlock;
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
import com.laytonsmith.abstraction.events.MCPrepareItemCraftEvent;
import com.laytonsmith.abstraction.events.MCPrepareItemEnchantEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
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
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author jb_aero
 */
public class BukkitInventoryEvents {
	public static class BukkitMCInventoryEvent implements MCInventoryEvent {
		InventoryEvent event;

		public BukkitMCInventoryEvent(InventoryEvent e) {
			event = e;
		}

		public List<MCHumanEntity> getViewers() {
			List<MCHumanEntity> viewers = new ArrayList<MCHumanEntity>();

			for (HumanEntity viewer : event.getViewers()) {
				viewers.add(new BukkitMCHumanEntity(viewer));
			}

			return viewers;
		}

		public MCInventoryView getView() {
			return new BukkitMCInventoryView(event.getView());
		}

		public MCInventory getInventory() {
			return new BukkitMCInventory(event.getInventory());
		}

		public Object _GetObject() {
			return event;
		}
	}

	public static class BukkitMCInventoryInteractEvent extends BukkitMCInventoryEvent
	implements MCInventoryInteractEvent {
		InventoryInteractEvent iie;

		public BukkitMCInventoryInteractEvent(InventoryInteractEvent e) {
			super(e);
			iie = e;
		}

		public MCHumanEntity getWhoClicked() {
			return new BukkitMCHumanEntity(iie.getWhoClicked());
		}

		public void setResult(MCResult newResult) {
			iie.setResult(Result.valueOf(newResult.name()));
		}

		public MCResult getResult() {
			return BukkitMCResult.getConvertor().getAbstractedEnum(iie.getResult());
		}

		public boolean isCanceled() {
			return iie.isCancelled();
		}

        public void setCancelled(boolean cancelled) {
            iie.setCancelled(cancelled);
        }
	}

	public static class BukkitMCInventoryOpenEvent extends BukkitMCInventoryEvent
	implements MCInventoryOpenEvent {
		InventoryOpenEvent ioe;

		public BukkitMCInventoryOpenEvent(InventoryOpenEvent e) {
			super(e);
			ioe = e;
		}

		public MCHumanEntity getPlayer() {
			return new BukkitMCHumanEntity(ioe.getPlayer());
		}
	}

	public static class BukkitMCInventoryCloseEvent extends BukkitMCInventoryEvent
	implements MCInventoryCloseEvent {
		InventoryCloseEvent ice;

		public BukkitMCInventoryCloseEvent(InventoryCloseEvent e) {
			super(e);
			ice = e;
		}

		public MCHumanEntity getPlayer() {
			return new BukkitMCHumanEntity(ice.getPlayer());
		}
	}

	public static class BukkitMCInventoryClickEvent extends BukkitMCInventoryInteractEvent
	implements MCInventoryClickEvent {

		InventoryClickEvent ic;
		public BukkitMCInventoryClickEvent(InventoryClickEvent e) {
			super(e);
			this.ic = e;
		}

		public MCItemStack getCurrentItem() {
			return new BukkitMCItemStack(ic.getCurrentItem());
		}

		public MCItemStack getCursor() {
			return new BukkitMCItemStack(ic.getCursor());
		}

		public int getSlot() {
			return ic.getSlot();
		}

		public int getRawSlot() {
			return ic.getRawSlot();
		}

		public MCSlotType getSlotType() {
			return MCSlotType.valueOf(ic.getSlotType().name());
		}

		public boolean isLeftClick() {
			return ic.getClick().isLeftClick();
		}

		public boolean isRightClick() {
			return ic.getClick().isRightClick();
		}

		public boolean isShiftClick() {
			return ic.getClick().isShiftClick();
		}

		public boolean isCreativeClick() {
			return ic.getClick().isCreativeAction();
		}
		
		public boolean isKeyboardClick() {
			return ic.getClick().isKeyboardClick();
		}

		public void setCurrentItem(MCItemStack slot) {
			if (slot != null) {
				ic.setCurrentItem(((BukkitMCItemStack) slot).asItemStack());
			} else {
				ic.setCurrentItem(null);
			}
		}

		public void setCursor(MCItemStack cursor) {
			ic.setCursor(((BukkitMCItemStack) cursor).asItemStack());
		}

		public MCInventoryAction getAction() {
			return BukkitMCInventoryAction.getConvertor().getAbstractedEnum(ic.getAction());
		}

		public MCClickType getClickType() {
			return BukkitMCClickType.getConvertor().getAbstractedEnum((ic.getClick()));
		}
	}

	public static class BukkitMCInventoryDragEvent extends BukkitMCInventoryInteractEvent
	implements MCInventoryDragEvent {

		InventoryDragEvent id;
		public BukkitMCInventoryDragEvent(InventoryDragEvent e) {
			super(e);
			this.id = e;
		}

		public Map<Integer, MCItemStack> getNewItems() {
			Map<Integer, MCItemStack> ret = new HashMap<Integer, MCItemStack>();

			for (Map.Entry<Integer, ItemStack> ni : id.getNewItems().entrySet()) {
				Integer key = ni.getKey();
				ItemStack value = ni.getValue();
				ret.put(key, new BukkitMCItemStack(value));
			}
			
			return ret;
		}

		public Set<Integer> getRawSlots() {
			Set<Integer> ret = new HashSet<Integer>();

			for (Integer rs : id.getRawSlots()) {
				ret.add(rs);
			}
			
			return ret;
		}

		public Set<Integer> getInventorySlots() {
			Set<Integer> ret = new HashSet<Integer>();

			for (Integer is : id.getInventorySlots()) {
				ret.add(is);
			}

			return ret;
		}

		public MCItemStack getCursor() {
			return new BukkitMCItemStack(id.getCursor());
		}

		public void setCursor(MCItemStack newCursor) {
			id.setCursor(((BukkitMCItemStack) newCursor).asItemStack());
		}

		public MCItemStack getOldCursor() {
			return new BukkitMCItemStack(id.getOldCursor());
		}

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
		
		public MCBlock getEnchantBlock() {
			return new BukkitMCBlock(ei.getEnchantBlock());
		}
		
		public MCPlayer GetEnchanter() {
			return new BukkitMCPlayer(ei.getEnchanter());
		}
		
		public Map<MCEnchantment, Integer> getEnchantsToAdd() {
			Map<MCEnchantment, Integer> ret = new HashMap<MCEnchantment, Integer>();

			for (Map.Entry<Enchantment, Integer> ea : ei.getEnchantsToAdd().entrySet()) {
				Enchantment key = ea.getKey();
				Integer value = ea.getValue();
				ret.put(new BukkitMCEnchantment(key), value);
			}
			
			return ret;
		}
		
		public void setEnchantsToAdd(Map<MCEnchantment, Integer> enchants) {
			Map<Enchantment, Integer> ret = ei.getEnchantsToAdd();
			ret.clear();
			
//			for (Map.Entry<MCEnchantment, Integer> ea : enchants.entrySet()) {
//				MCEnchantment key = ea.getKey();
//				Integer value = ea.getValue();
//				ret.put(((BukkitMCEnchantment) key).asEnchantment(), value);
//			}
			
			Map<Enchantment, Integer> enchantments = new HashMap<Enchantment, Integer>();
			
			for (Map.Entry<MCEnchantment, Integer> ea : enchants.entrySet()) {
				MCEnchantment key = ea.getKey();
				Integer value = ea.getValue();
				enchantments.put(((BukkitMCEnchantment) key).asEnchantment(), value);
			}
			
			ItemStack item = ei.getItem();
			item.addUnsafeEnchantments(enchantments);
		}
		
		
		
		public MCItemStack getItem() {
			return new BukkitMCItemStack(ei.getItem());
		}
		
		public void setItem(MCItemStack i) {
			ItemStack item = ei.getItem();
			ItemStack is = ((BukkitMCItemStack) i).asItemStack();
			
			item.setAmount(is.getAmount());
			item.setData(is.getData());
			item.setDurability(is.getDurability());
			item.setItemMeta(is.getItemMeta());
			item.setType(is.getType());
		}
		
		public void setExpLevelCost(int level) {
			ei.setExpLevelCost(level);
		}
		
		public int getExpLevelCost() {
			return ei.getExpLevelCost();
		}
		
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
		
		public MCBlock getEnchantBlock() {
			return new BukkitMCBlock(pie.getEnchantBlock());
		}
		
		public MCPlayer getEnchanter() {
			return new BukkitMCPlayer(pie.getEnchanter());
		}
		
		public int getEnchantmentBonus() {
			return pie.getEnchantmentBonus();
		}
		
		public int[] getExpLevelCostsOffered() {
			return pie.getExpLevelCostsOffered();
		}
		
		public MCItemStack getItem() {
			return new BukkitMCItemStack(pie.getItem());
		}
		
		public void setItem(MCItemStack i) {
			ItemStack item = pie.getItem();
			ItemStack is = ((BukkitMCItemStack) i).asItemStack();
			
			item.setAmount(is.getAmount());
			item.setData(is.getData());
			item.setDurability(is.getDurability());
			item.setItemMeta(is.getItemMeta());
			item.setType(is.getType());
		}
	}
	
	public static class BukkitMCItemHeldEvent implements MCItemHeldEvent {

		PlayerItemHeldEvent ih;
		public BukkitMCItemHeldEvent(PlayerItemHeldEvent event) {
			ih = event;
		}
		
		@Override
		public MCPlayer getPlayer() {
			return new com.laytonsmith.abstraction.bukkit.BukkitMCPlayer(ih.getPlayer());
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
