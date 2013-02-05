package com.laytonsmith.core.events.drivers;

import java.util.Map;

import com.laytonsmith.PureUtilities.StringUtils;
import com.laytonsmith.abstraction.enums.MCSlotType;
import com.laytonsmith.abstraction.events.MCInventoryClickEvent;
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
import com.laytonsmith.core.exceptions.EventException;
import com.laytonsmith.core.exceptions.PrefilterNonMatchException;

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
					+ " | slotitem: <item match> } "
					+ "Fired when a player clicks a slot in any inventory. "
					+ "{player: The player who clicked | " /*"{player: The player who clicked | viewers: everyone looking in this inventory | "*/
					+ "rightclick: true/false if this was a right click | shiftclick: true/false if shift was being held | "
					+ "slot: the number of the slot | slottype | slotitem | inventorytype | cursoritem} "
					+ "{slotitem: the item currently in the clicked slot | cursoritem: the item on the cursor} "
					+ "{} ";
		}

		public boolean matches(Map<String, Construct> prefilter, BindableEvent event)
				throws PrefilterNonMatchException {
			if (event instanceof MCInventoryClickEvent) {
				MCInventoryClickEvent e = (MCInventoryClickEvent) event;
				
				Prefilters.match(prefilter, "slottype", e.getSlotType().name(), PrefilterType.MACRO);
				Prefilters.match(prefilter, "slotitem", Static.ParseItemNotation(e.getCurrentItem()), PrefilterType.ITEM_MATCH);
				
				return true;
			}
			return false;
		}

		public BindableEvent convert(CArray manualObject) {
			return null;
		}

		public Map<String, Construct> evaluate(BindableEvent event)
				throws EventException {
			if (event instanceof MCInventoryClickEvent) {
				MCInventoryClickEvent e = (MCInventoryClickEvent) event;
				Map<String, Construct> map = evaluate_helper(event);
				
				map.put("player", new CString(e.getWhoClicked().getName(), Target.UNKNOWN));
//				CArray viewers = new CArray(Target.UNKNOWN);
//				for (MCHumanEntity viewer : e.getViewers()) {
//					viewers.push(new CString(viewer.getName(), Target.UNKNOWN));
//				}
//				map.put("viewers", viewers);
				
				map.put("rightclick", new CBoolean(e.isRightClick(), Target.UNKNOWN));
				map.put("shiftclick", new CBoolean(e.isShiftClick(), Target.UNKNOWN));
				map.put("cursoritem", ObjectGenerator.GetGenerator().item(e.getCursor(), Target.UNKNOWN));
				
				map.put("slot", new CInt(e.getSlot(), Target.UNKNOWN));
				map.put("slottype", new CString(e.getSlotType().name(), Target.UNKNOWN));
				map.put("slotitem", ObjectGenerator.GetGenerator().item(e.getCurrentItem(), Target.UNKNOWN));
				map.put("inventorytype", new CString(e.getInventory().getType().name(), Target.UNKNOWN));
				
				return map;
			} else {
				throw new EventException("Cannot convert e to MCInventoryClickEvent");
			}
		}

		public Driver driver() {
			return Driver.INVENTORY_CLICK;
		}

		public boolean modifyEvent(String key, Construct value,
				BindableEvent event) {
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

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}
}
