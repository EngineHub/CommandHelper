package com.laytonsmith.abstraction.bukkit.events;

import java.util.Collections;
import java.util.List;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;

import com.laytonsmith.abstraction.MCHumanEntity;
import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.MCInventoryView;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.bukkit.BukkitMCHumanEntity;
import com.laytonsmith.abstraction.bukkit.BukkitMCInventory;
import com.laytonsmith.abstraction.bukkit.BukkitMCInventoryView;
import com.laytonsmith.abstraction.bukkit.BukkitMCItemStack;
import com.laytonsmith.abstraction.enums.MCSlotType;
import com.laytonsmith.abstraction.events.MCInventoryClickEvent;

/**
 *
 * @author jb_aero
 */
public class BukkitInventoryEvents {

	public static class BukkitMCInventoryClickEvent implements MCInventoryClickEvent {

		InventoryClickEvent ic;
		public BukkitMCInventoryClickEvent(InventoryClickEvent e) {
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

		public MCSlotType getSlotType() {
			return MCSlotType.valueOf(ic.getSlotType().name());
		}

		public MCHumanEntity getWhoClicked() {
			return new BukkitMCHumanEntity(ic.getWhoClicked());
		}

		public boolean isLeftClick() {
			return ic.isLeftClick();
		}

		public boolean isRightClick() {
			return ic.isRightClick();
		}

		public boolean isShiftClick() {
			return ic.isShiftClick();
		}

		public void setCurrentItem(MCItemStack slot) {
			ic.setCurrentItem(((BukkitMCItemStack) slot).asItemStack());
		}

		public void setCursor(MCItemStack cursor) {
			ic.setCursor(((BukkitMCItemStack) cursor).asItemStack());
		}

		public List<MCHumanEntity> getViewers() {
			List<MCHumanEntity> viewers = Collections.emptyList();
			for (HumanEntity viewer : ic.getViewers()) {
				viewers.add(new BukkitMCHumanEntity(viewer));
			}
			return viewers;
		}

		public MCInventoryView getView() {
			return new BukkitMCInventoryView(ic.getView());
		}

		public MCInventory getInventory() {
			return new BukkitMCInventory(ic.getInventory());
		}

		public Object _GetObject() {
			return ic;
		}
		
	}
}
