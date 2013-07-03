package com.laytonsmith.core.events.drivers;

import com.laytonsmith.PureUtilities.StringUtils;
import com.laytonsmith.abstraction.MCHumanEntity;
import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.abstraction.enums.MCClickType;
import com.laytonsmith.abstraction.enums.MCDragType;
import com.laytonsmith.abstraction.enums.MCInventoryAction;
import com.laytonsmith.abstraction.enums.MCSlotType;
import com.laytonsmith.abstraction.events.MCInventoryClickEvent;
import com.laytonsmith.abstraction.events.MCInventoryCloseEvent;
import com.laytonsmith.abstraction.events.MCInventoryDragEvent;
import com.laytonsmith.abstraction.events.MCInventoryOpenEvent;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.events.AbstractEvent;
import com.laytonsmith.core.events.BindableEvent;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.Prefilters;
import com.laytonsmith.core.events.Prefilters.PrefilterType;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.EventException;
import com.laytonsmith.core.exceptions.PrefilterNonMatchException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;

import java.util.Map;

/**
 *
 * @author jb_aero
 */
public class InventoryEvents {
	public static String docs() {
		return "Contains events related to inventory.";
	}

	@api
	public static class inventory_click extends AbstractEvent {

		public String getName() {
			return "inventory_click";
		}

		public String docs() {
			return "{slottype: <macro> The type of slot being clicked, can be "
					+ StringUtils.Join(MCSlotType.values(), ", ", ", or ")
					+ " | clicktype: <macro> One of " + StringUtils.Join(MCClickType.values(), ", ", ", or ")
					+ " | action: <macro> One of " + StringUtils.Join(MCInventoryAction.values(), ", ", ", or ")
					+ " | slotitem: <item match> }"
					+ " Fired when a player clicks a slot in any inventory. "
					+ " {player: The player who clicked | viewers: everyone looking in this inventory"
					+ " | leftclick: true/false if this was a left click | keyboardclick: true/false if a key was pressed"
					+ " | rightclick: true/false if this was a right click | shiftclick: true/false if shift was being held"
					+ " | creativeclick: true/false if this action could only be performed in creative mode"
					+ " | slot: the number of the slot | rawslot: the number of the slot in whole inventory window | slottype"
					+ " | slotitem | inventorytype | inventorysize: number of slots in opened inventory | cursoritem"
					+ " | inventory: all the items in the (top) inventory | clicktype | action}"
					+ " {slotitem: the item currently in the clicked slot | cursoritem: the item on the cursor}"
					+ " {}";
		}

		public boolean matches(Map<String, Construct> prefilter, BindableEvent event)
				throws PrefilterNonMatchException {
			if (event instanceof MCInventoryClickEvent) {
				MCInventoryClickEvent e = (MCInventoryClickEvent) event;

				Prefilters.match(prefilter, "player", e.getWhoClicked().getName(), PrefilterType.MACRO);
				Prefilters.match(prefilter, "clicktype", e.getClickType().name(), PrefilterType.MACRO);
				Prefilters.match(prefilter, "slottype", e.getSlotType().name(), PrefilterType.MACRO);
				Prefilters.match(prefilter, "slotitem", Static.ParseItemNotation(e.getCurrentItem()), PrefilterType.ITEM_MATCH);

				return true;
			}
			return false;
		}

		public BindableEvent convert(CArray manualObject) {
			throw new ConfigRuntimeException("Unsupported Operation", ExceptionType.BindException, Target.UNKNOWN);
		}

		public Map<String, Construct> evaluate(BindableEvent event) throws EventException {
			if (event instanceof MCInventoryClickEvent) {
				MCInventoryClickEvent e = (MCInventoryClickEvent) event;
				Map<String, Construct> map = evaluate_helper(event);

				map.put("player", new CString(e.getWhoClicked().getName(), Target.UNKNOWN));
				CArray viewers = new CArray(Target.UNKNOWN);
				for (MCHumanEntity viewer : e.getViewers()) {
					viewers.push(new CString(viewer.getName(), Target.UNKNOWN));
				}
				map.put("viewers", viewers);
				
				map.put("action", new CString(e.getAction().name(), Target.UNKNOWN));
				map.put("clicktype", new CString(e.getClickType().name(), Target.UNKNOWN));

				map.put("leftclick", new CBoolean(e.isLeftClick(), Target.UNKNOWN));
				map.put("rightclick", new CBoolean(e.isRightClick(), Target.UNKNOWN));
				map.put("shiftclick", new CBoolean(e.isShiftClick(), Target.UNKNOWN));
				map.put("creativeclick", new CBoolean(e.isCreativeClick(), Target.UNKNOWN));
				map.put("keyboardclick", new CBoolean(e.isKeyboardClick(), Target.UNKNOWN));
				map.put("cursoritem", ObjectGenerator.GetGenerator().item(e.getCursor(), Target.UNKNOWN));

				map.put("slot", new CInt(e.getSlot(), Target.UNKNOWN));
				map.put("rawslot", new CInt(e.getRawSlot(), Target.UNKNOWN));
				map.put("slottype", new CString(e.getSlotType().name(), Target.UNKNOWN));
				map.put("slotitem", ObjectGenerator.GetGenerator().item(e.getCurrentItem(), Target.UNKNOWN));

				CArray items = CArray.GetAssociativeArray(Target.UNKNOWN);
				MCInventory inv = e.getInventory();
				for (int i = 0; i < inv.getSize(); i++) {
					items.set(i, ObjectGenerator.GetGenerator().item(inv.getItem(i), Target.UNKNOWN), Target.UNKNOWN);
				}
				map.put("inventory", items);
				map.put("inventorytype", new CString(inv.getType().name(), Target.UNKNOWN));
				map.put("inventorysize", new CInt(inv.getSize(), Target.UNKNOWN));

				return map;
			} else {
				throw new EventException("Cannot convert e to MCInventoryClickEvent");
			}
		}

		public Driver driver() {
			return Driver.INVENTORY_CLICK;
		}

		public boolean modifyEvent(String key, Construct value, BindableEvent event) {
			if (event instanceof MCInventoryClickEvent) {
				MCInventoryClickEvent e = (MCInventoryClickEvent) event;

				if (key.equalsIgnoreCase("slotitem")) {
					e.setCurrentItem(ObjectGenerator.GetGenerator().item(value, Target.UNKNOWN));
					return true;
				}
				if (key.equalsIgnoreCase("cursoritem")) {
					e.setCursor(ObjectGenerator.GetGenerator().item(value, Target.UNKNOWN));
					return true;
				}
			}
			return false;
		}

		@Override
		public void cancel(BindableEvent o, boolean state) {
			MCInventoryClickEvent ic = ((MCInventoryClickEvent)o);
            ic.setCancelled(state);
			StaticLayer.GetServer().getPlayer(ic.getWhoClicked().getName()).updateInventory();
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

	}

	@api
	public static class inventory_drag extends AbstractEvent {

		public String getName() {
			return "inventory_drag";
		}

		public String docs() {
			return "{world: <string match> World name | type: <macro> Can be " + StringUtils.Join(MCDragType.values(), ", ", ", or ") + " } "
					+ " | cursoritem: <item match> item in hand, before event starts"
					+ "Fired when a player clicks (by left or right mouse button) a slot in inventory and drag mouse across slots. "
					+ "{player: The player who clicked | newcursoritem: item on cursro, after event | oldcursoritem: item on cursor,"
					+ " before event | slots: used slots | rawslots: used slots, as the numbers of the slots in whole inventory window"
					+ " | newitems: array of items which are dropped in selected slots | inventorytype | inventorysize: number of slots in"
					+ " opened inventory} {cursoritem: the item on the cursor, after event} "
					+ "{} ";
		}

		public boolean matches(Map<String, Construct> prefilter, BindableEvent event)
				throws PrefilterNonMatchException {
			if (event instanceof MCInventoryDragEvent) {
				MCInventoryDragEvent e = (MCInventoryDragEvent) event;

				Prefilters.match(prefilter, "world", e.getWhoClicked().getWorld().getName(), PrefilterType.MACRO);
				Prefilters.match(prefilter, "type", e.getType().name(), PrefilterType.MACRO);
				Prefilters.match(prefilter, "cursoritem", Static.ParseItemNotation(e.getOldCursor()), PrefilterType.ITEM_MATCH);

				return true;
			}
			return false;
		}

		public BindableEvent convert(CArray manualObject) {
			return null;
		}

		public Map<String, Construct> evaluate(BindableEvent event)
				throws EventException {
			if (event instanceof MCInventoryDragEvent) {
				MCInventoryDragEvent e = (MCInventoryDragEvent) event;
				Map<String, Construct> map = evaluate_helper(event);

				map.put("player", new CString(e.getWhoClicked().getName(), Target.UNKNOWN));
				map.put("newcursoritem", ObjectGenerator.GetGenerator().item(e.getCursor(), Target.UNKNOWN));
				map.put("oldcursoritem", ObjectGenerator.GetGenerator().item(e.getOldCursor(), Target.UNKNOWN));

				CArray slots = new CArray(Target.UNKNOWN);
				for (Integer slot : e.getInventorySlots()) {
					slots.push(new CInt(slot.intValue(), Target.UNKNOWN));
				}
				map.put("slots", slots);

				CArray rawSlots = new CArray(Target.UNKNOWN);
				for (Integer slot : e.getRawSlots()) {
					rawSlots.push(new CInt(slot.intValue(), Target.UNKNOWN));
				}
				map.put("rawslots", rawSlots);

				CArray newItems = CArray.GetAssociativeArray(Target.UNKNOWN);
				for (Map.Entry<Integer, MCItemStack> ni : e.getNewItems().entrySet()) {
					Integer key = ni.getKey();
					MCItemStack value = ni.getValue();
					newItems.set(key.intValue(), ObjectGenerator.GetGenerator().item(value, Target.UNKNOWN), Target.UNKNOWN);
				}
				map.put("newitems", newItems);

				CArray items = CArray.GetAssociativeArray(Target.UNKNOWN);
				MCInventory inv = e.getInventory();
				for (int i = 0; i < inv.getSize(); i++) {
					items.set(i, ObjectGenerator.GetGenerator().item(inv.getItem(i), Target.UNKNOWN), Target.UNKNOWN);
				}
				map.put("inventory", items);
				map.put("inventorytype", new CString(inv.getType().name(), Target.UNKNOWN));
				map.put("inventorysize", new CInt(inv.getSize(), Target.UNKNOWN));

				map.put("type", new CString(e.getType().name(), Target.UNKNOWN));

				return map;
			} else {
				throw new EventException("Cannot convert e to MCInventoryDragEvent");
			}
		}

		public Driver driver() {
			return Driver.INVENTORY_DRAG;
		}

		public boolean modifyEvent(String key, Construct value,
				BindableEvent event) {
			if (event instanceof MCInventoryDragEvent) {
				MCInventoryDragEvent e = (MCInventoryDragEvent) event;

				if (key.equalsIgnoreCase("cursoritem")) {
					e.setCursor(ObjectGenerator.GetGenerator().item(value, Target.UNKNOWN));
					return true;
				}
			}
			return false;
		}

		@Override
		public void cancel(BindableEvent o, boolean state) {
			MCInventoryDragEvent id = ((MCInventoryDragEvent)o);
            id.setCancelled(state);
			StaticLayer.GetServer().getPlayer(id.getWhoClicked().getName()).updateInventory();
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

	}

	@api
	public static class inventory_open extends AbstractEvent {

		public String getName() {
			return "inventory_open";
		}

		public String docs() {
			return "{} "
					+ "Fired when a player opens an inventory. "
					+ "{player: The player | " /*"{player: The player who clicked | viewers: everyone looking in this inventory | "*/
					+ "inventory: the inventory items in this inventory | "
					+ "inventorytype: type of inventory} "
					+ "{} "
					+ "{} ";
		}

		public boolean matches(Map<String, Construct> prefilter, BindableEvent event)
				throws PrefilterNonMatchException {
			return true;
		}

		public BindableEvent convert(CArray manualObject) {
			return null;
		}

		public Map<String, Construct> evaluate(BindableEvent event)
				throws EventException {
			if (event instanceof MCInventoryOpenEvent) {
				MCInventoryOpenEvent e = (MCInventoryOpenEvent) event;
				Map<String, Construct> map = evaluate_helper(event);

				map.put("player", new CString(e.getPlayer().getName(), Target.UNKNOWN));

				CArray items = CArray.GetAssociativeArray(Target.UNKNOWN);
				MCInventory inv = e.getInventory();

				for (int i = 0; i < inv.getSize(); i++) {
					Construct c = ObjectGenerator.GetGenerator().item(inv.getItem(i), Target.UNKNOWN);
					items.set(i, c, Target.UNKNOWN);
				}

				map.put("inventory", items);

				map.put("inventorytype", new CString(e.getInventory().getType().name(), Target.UNKNOWN));

				return map;
			} else {
				throw new EventException("Cannot convert e to MCInventoryOpenEvent");
			}
		}

		public Driver driver() {
			return Driver.INVENTORY_OPEN;
		}

		public boolean modifyEvent(String key, Construct value,
				BindableEvent event) {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

	}

	@api
	public static class inventory_close extends AbstractEvent {

		public String getName() {
			return "inventory_close";
		}

		public String docs() {
			return "{} "
					+ "Fired when a player closes an inventory. "
					+ "{player: The player | " /*"{player: The player who clicked | viewers: everyone looking in this inventory | "*/
					+ "inventory: the inventory items in this inventory | "
					+ "inventorytype: type of inventory} "
					+ "{} "
					+ "{} ";
		}

		public boolean matches(Map<String, Construct> prefilter, BindableEvent event)
				throws PrefilterNonMatchException {
			return true;
		}

		public BindableEvent convert(CArray manualObject) {
			return null;
		}

		public Map<String, Construct> evaluate(BindableEvent event)
				throws EventException {
			if (event instanceof MCInventoryCloseEvent) {
				MCInventoryCloseEvent e = (MCInventoryCloseEvent) event;
				Map<String, Construct> map = evaluate_helper(event);

				map.put("player", new CString(e.getPlayer().getName(), Target.UNKNOWN));

				CArray items = CArray.GetAssociativeArray(Target.UNKNOWN);
				MCInventory inv = e.getInventory();

				for (int i = 0; i < inv.getSize(); i++) {
					Construct c = ObjectGenerator.GetGenerator().item(inv.getItem(i), Target.UNKNOWN);
					items.set(i, c, Target.UNKNOWN);
				}

				map.put("inventory", items);

				map.put("inventorytype", new CString(e.getInventory().getType().name(), Target.UNKNOWN));

				return map;
			} else {
				throw new EventException("Cannot convert e to MCInventoryCloseEvent");
			}
		}

		public Driver driver() {
			return Driver.INVENTORY_CLOSE;
		}

		public boolean modifyEvent(String key, Construct value,
				BindableEvent event) {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

	}
}
