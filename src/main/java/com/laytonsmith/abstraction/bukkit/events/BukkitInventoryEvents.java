package com.laytonsmith.abstraction.bukkit.events;

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
import com.laytonsmith.abstraction.events.MCInventoryCloseEvent;
import com.laytonsmith.abstraction.events.MCInventoryEvent;
import com.laytonsmith.abstraction.events.MCInventoryOpenEvent;
import java.util.Collections;
import java.util.List;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

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
			List<MCHumanEntity> viewers = Collections.emptyList();

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

	public static class BukkitMCInventoryClickEvent extends BukkitMCInventoryEvent
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
			if (slot != null) {
				ic.setCurrentItem(((BukkitMCItemStack) slot).asItemStack());
			} else {
				ic.setCurrentItem(null);
			}
		}

		public void setCursor(MCItemStack cursor) {
			ic.setCursor(((BukkitMCItemStack) cursor).asItemStack());
		}

        public void setCancelled(boolean cancelled) {
            ic.setCancelled(cancelled);
        }
	}
}
