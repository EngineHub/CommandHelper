package com.laytonsmith.core.events.drivers;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.MCHumanEntity;
import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.abstraction.enums.MCClickType;
import com.laytonsmith.abstraction.enums.MCDragType;
import com.laytonsmith.abstraction.enums.MCInventoryAction;
import com.laytonsmith.abstraction.enums.MCSlotType;
import com.laytonsmith.abstraction.events.MCEnchantItemEvent;
import com.laytonsmith.abstraction.events.MCInventoryClickEvent;
import com.laytonsmith.abstraction.events.MCInventoryCloseEvent;
import com.laytonsmith.abstraction.events.MCInventoryDragEvent;
import com.laytonsmith.abstraction.events.MCInventoryOpenEvent;
import com.laytonsmith.abstraction.events.MCItemHeldEvent;
import com.laytonsmith.abstraction.events.MCPrepareItemCraftEvent;
import com.laytonsmith.abstraction.events.MCPrepareItemEnchantEvent;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
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

		@Override
		public String getName() {
			return "inventory_click";
		}

		@Override
		public String docs() {
			return "{slottype: <macro> The type of slot being clicked, can be "
					+ StringUtils.Join(MCSlotType.values(), ", ", ", or ")
					+ " | clicktype: <macro> One of " + StringUtils.Join(MCClickType.values(), ", ", ", or ")
					+ " | action: <macro> One of " + StringUtils.Join(MCInventoryAction.values(), ", ", ", or ")
					+ " | slotitem: <item match> | player: <macro>}"
					+ " Fired when a player clicks a slot in any inventory. "
					+ " {player: The player who clicked | viewers: everyone looking in this inventory"
					+ " | leftclick: true/false if this was a left click | keyboardclick: true/false if a key was pressed"
					+ " | rightclick: true/false if this was a right click | shiftclick: true/false if shift was being held"
					+ " | creativeclick: true/false if this action could only be performed in creative mode"
					+ " | slot: the number of the slot | rawslot: the number of the slot in whole inventory window | slottype"
					+ " | slotitem | inventorytype | inventorysize: number of slots in opened inventory | cursoritem"
					+ " | inventory: all the items in the (top) inventory | clicktype | action}"
					+ " {slotitem: the item currently in the clicked slot | cursoritem: the item on the cursor (may cause"
					+ " unexpected behavior)}"
					+ " {}";
		}

		@Override
		public boolean matches(Map<String, Construct> prefilter, BindableEvent event)
				throws PrefilterNonMatchException {
			if (event instanceof MCInventoryClickEvent) {
				MCInventoryClickEvent e = (MCInventoryClickEvent) event;

				Prefilters.match(prefilter, "action", e.getAction().name(), PrefilterType.MACRO);
				Prefilters.match(prefilter, "player", e.getWhoClicked().getName(), PrefilterType.MACRO);
				Prefilters.match(prefilter, "clicktype", e.getClickType().name(), PrefilterType.MACRO);
				Prefilters.match(prefilter, "slottype", e.getSlotType().name(), PrefilterType.MACRO);
				Prefilters.match(prefilter, "slotitem", Static.ParseItemNotation(e.getCurrentItem()), PrefilterType.ITEM_MATCH);

				return true;
			}
			return false;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			throw ConfigRuntimeException.BuildException("Unsupported Operation", ExceptionType.BindException, Target.UNKNOWN);
		}

		@Override
		public Map<String, Construct> evaluate(BindableEvent event) throws EventException {
			if (event instanceof MCInventoryClickEvent) {
				MCInventoryClickEvent e = (MCInventoryClickEvent) event;
				Map<String, Construct> map = evaluate_helper(event);

				map.put("player", new CString(e.getWhoClicked().getName(), Target.UNKNOWN));
				CArray viewers = new CArray(Target.UNKNOWN);
				for (MCHumanEntity viewer : e.getViewers()) {
					viewers.push(new CString(viewer.getName(), Target.UNKNOWN), Target.UNKNOWN);
				}
				map.put("viewers", viewers);

				map.put("action", new CString(e.getAction().name(), Target.UNKNOWN));
				map.put("clicktype", new CString(e.getClickType().name(), Target.UNKNOWN));

				map.put("leftclick", CBoolean.get(e.isLeftClick()));
				map.put("rightclick", CBoolean.get(e.isRightClick()));
				map.put("shiftclick", CBoolean.get(e.isShiftClick()));
				map.put("creativeclick", CBoolean.get(e.isCreativeClick()));
				map.put("keyboardclick", CBoolean.get(e.isKeyboardClick()));
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

		@Override
		public Driver driver() {
			return Driver.INVENTORY_CLICK;
		}

		@Override
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

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

	}

	@api
	public static class inventory_drag extends AbstractEvent {

		@Override
		public String getName() {
			return "inventory_drag";
		}

		@Override
		public String docs() {
			return "{world: <string match> World name | type: <macro> Can be " + StringUtils.Join(MCDragType.values(), ", ", ", or ")
					+ " | cursoritem: <item match> item in hand, before event starts}"
					+ "Fired when a player clicks (by left or right mouse button) a slot in inventory and drag mouse across slots. "
					+ "{player: The player who clicked | newcursoritem: item on cursor, after event | oldcursoritem: item on cursor,"
					+ " before event | slots: used slots | rawslots: used slots, as the numbers of the slots in whole inventory window"
					+ " | newitems: array of items which are dropped in selected slots | inventorytype | inventorysize: number of slots in"
					+ " opened inventory} {cursoritem: the item on the cursor, after event} "
					+ "{} ";
		}

		@Override
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

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			return null;
		}

		@Override
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
					slots.push(new CInt(slot.intValue(), Target.UNKNOWN), Target.UNKNOWN);
				}
				map.put("slots", slots);

				CArray rawSlots = new CArray(Target.UNKNOWN);
				for (Integer slot : e.getRawSlots()) {
					rawSlots.push(new CInt(slot.intValue(), Target.UNKNOWN), Target.UNKNOWN);
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

		@Override
		public Driver driver() {
			return Driver.INVENTORY_DRAG;
		}

		@Override
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

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

	}

	@api
	public static class inventory_open extends AbstractEvent {

		@Override
		public String getName() {
			return "inventory_open";
		}

		@Override
		public String docs() {
			return "{} "
					+ "Fired when a player opens an inventory. "
					+ "{player: The player | " /*"{player: The player who clicked | viewers: everyone looking in this inventory | "*/
					+ "inventory: the inventory items in this inventory | "
					+ "inventorytype: type of inventory} "
					+ "{} "
					+ "{} ";
		}

		@Override
		public boolean matches(Map<String, Construct> prefilter, BindableEvent event)
				throws PrefilterNonMatchException {
			return true;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			return null;
		}

		@Override
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

		@Override
		public Driver driver() {
			return Driver.INVENTORY_OPEN;
		}

		@Override
		public boolean modifyEvent(String key, Construct value,
				BindableEvent event) {
			return false;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

	}

	@api
	public static class inventory_close extends AbstractEvent {

		@Override
		public String getName() {
			return "inventory_close";
		}

		@Override
		public String docs() {
			return "{} "
					+ "Fired when a player closes an inventory. "
					+ "{player: The player | " /*"{player: The player who clicked | viewers: everyone looking in this inventory | "*/
					+ "inventory: the inventory items in this inventory | "
					+ "inventorytype: type of inventory} "
					+ "{} "
					+ "{} ";
		}

		@Override
		public boolean matches(Map<String, Construct> prefilter, BindableEvent event)
				throws PrefilterNonMatchException {
			return true;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			return null;
		}

		@Override
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

		@Override
		public Driver driver() {
			return Driver.INVENTORY_CLOSE;
		}

		@Override
		public boolean modifyEvent(String key, Construct value,
				BindableEvent event) {
			return false;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

	}

	@api
	public static class item_enchant extends AbstractEvent {

		@Override
		public String getName() {
			return "item_enchant";
		}

		@Override
		public String docs() {
			return "{} "
					+ "Fired when a player enchants an item. "
					+ "{player: The player that enchanted the item | "
					+ "item: The item to be enchanted | "
					+ "inventorytype: type of inventory | "
					+ "levels: The amount of levels the player used | "
					+ "enchants: Array of added enchantments | "
					+ "location: Location of the used enchantment table | "
					+ "option: The enchantment option the player clicked}"
					+ "{levels: The amount of levels to use | "
					+ "item: The item to be enchanted | "
					+ "enchants: The enchants to add to the item}"
					+ "{}";
		}

		@Override
		public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			return true;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			return null;
		}

		@Override
		public Map<String, Construct> evaluate(BindableEvent event) throws EventException {
			if (event instanceof MCEnchantItemEvent) {
				MCEnchantItemEvent e = (MCEnchantItemEvent) event;
				Map<String, Construct> map = evaluate_helper(event);

				map.put("player", new CString(e.GetEnchanter().getName(), Target.UNKNOWN));
				map.put("item", ObjectGenerator.GetGenerator().item(e.getItem(), Target.UNKNOWN));
				map.put("inventorytype", new CString(e.getInventory().getType().name(), Target.UNKNOWN));
				map.put("levels", new CInt(e.getExpLevelCost(), Target.UNKNOWN));
				map.put("enchants", ObjectGenerator.GetGenerator().enchants(e.getEnchantsToAdd(), Target.UNKNOWN));

				CArray loc = ObjectGenerator.GetGenerator().location(e.getEnchantBlock().getLocation());

				loc.remove(new CString("yaw", Target.UNKNOWN));
				loc.remove(new CString("pitch", Target.UNKNOWN));
				loc.remove(new CString("4", Target.UNKNOWN));
				loc.remove(new CString("5", Target.UNKNOWN));

				map.put("location", loc);

				map.put("option", new CInt(e.whichButton(), Target.UNKNOWN));

				return map;
			} else {
				throw new EventException("Cannot convert e to MCEnchantItemEvent");
			}
		}

		@Override
		public Driver driver() {
			return Driver.ITEM_ENCHANT;
		}

		@Override
		public boolean modifyEvent(String key, Construct value, BindableEvent event) {
			if (event instanceof MCEnchantItemEvent) {
				MCEnchantItemEvent e = (MCEnchantItemEvent) event;

				if (key.equalsIgnoreCase("levels")) {
					e.setExpLevelCost(Static.getInt32(value, Target.UNKNOWN));
					return true;
				}

				if (key.equalsIgnoreCase("item")) {
					e.setItem(ObjectGenerator.GetGenerator().item(value, Target.UNKNOWN));
					return true;
				}

				if (key.equalsIgnoreCase("enchants")) {
					e.setEnchantsToAdd((ObjectGenerator.GetGenerator().enchants((CArray) value, Target.UNKNOWN)));
					return true;
				}
			}
			return false;
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class item_pre_enchant extends AbstractEvent {

		@Override
		public String getName() {
			return "item_pre_enchant";
		}

		@Override
		public String docs() {
			return "{} "
					+ "Fired when a player places an item in an enchantment table "
					+ "{player: The player that placed the item | "
					+ "item: The item to be enchanted | "
					+ "inventorytype: Type of inventory | "
					+ "enchantmentbonus: the amount of bookshelves influencing the enchantment table | "
					+ "expcosts: The offered costs of the 3 options | "
					+ "location: Location of the used enchantment table}"
					+ "{item: The item to be enchanted | "
					+ "expcosts: The costs of the 3 options on the enchantment table}"
					+ "{}";
		}

		@Override
		public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			return true;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			return null;
		}

		@Override
		public Map<String, Construct> evaluate(BindableEvent event) throws EventException {
			if (event instanceof MCPrepareItemEnchantEvent) {
				MCPrepareItemEnchantEvent e = (MCPrepareItemEnchantEvent) event;
				Map<String, Construct> map = evaluate_helper(event);

				map.put("player", new CString(e.getEnchanter().getName(), Target.UNKNOWN));
				map.put("item", ObjectGenerator.GetGenerator().item(e.getItem(), Target.UNKNOWN));
				map.put("inventorytype", new CString(e.getInventory().getType().name(), Target.UNKNOWN));
				map.put("enchantmentbonus", new CInt(e.getEnchantmentBonus(), Target.UNKNOWN));

				int[] expCosts = e.getExpLevelCostsOffered();
				CArray expCostsCArray = new CArray(Target.UNKNOWN);

				for (int i = 0; i < expCosts.length; i++) {
					int j = expCosts[i];
					expCostsCArray.push(new CInt(j, Target.UNKNOWN), i, Target.UNKNOWN);
				}

				map.put("expcosts", expCostsCArray);

				CArray loc = ObjectGenerator.GetGenerator().location(e.getEnchantBlock().getLocation());

				loc.remove(new CString("yaw", Target.UNKNOWN));
				loc.remove(new CString("pitch", Target.UNKNOWN));
				loc.remove(new CString("4", Target.UNKNOWN));
				loc.remove(new CString("5", Target.UNKNOWN));

				map.put("location", loc);

				return map;
			} else {
				throw new EventException("Cannot convert e to MCPrepareItemEnchantEvent");
			}
		}

		@Override
		public Driver driver() {
			return Driver.ITEM_PRE_ENCHANT;
		}

		@Override
		public boolean modifyEvent(String key, Construct value, BindableEvent event) {
			if (event instanceof MCPrepareItemEnchantEvent) {
				MCPrepareItemEnchantEvent e = (MCPrepareItemEnchantEvent) event;

				if (key.equalsIgnoreCase("item")) {
					e.setItem(ObjectGenerator.GetGenerator().item(value, Target.UNKNOWN));
					return true;
				}

				if (key.equalsIgnoreCase("expcosts")) {
					if (value instanceof CArray) {
						CArray CexpCosts = (CArray) value;
						if (!CexpCosts.inAssociativeMode()) {
							int[] ExpCosts = e.getExpLevelCostsOffered();

							for (int i = 0; i <= 2; i++) {
								if (CexpCosts.get(i, Target.UNKNOWN) instanceof CInt) {
									ExpCosts[i] = (int) ((CInt) CexpCosts.get(i, Target.UNKNOWN)).getInt();
								} else {
									throw ConfigRuntimeException.BuildException("Expected an intger at index " + i + "!", ExceptionType.FormatException, Target.UNKNOWN);
								}
							}
						} else {
							throw ConfigRuntimeException.BuildException("Expected a normal array!", ExceptionType.FormatException, Target.UNKNOWN);
						}
					} else {
						throw ConfigRuntimeException.BuildException("Expected an array!", ExceptionType.FormatException, Target.UNKNOWN);
					}
				}
			}
			return false;
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_1;
		}

	}

	@api
	public static class item_held extends AbstractEvent {

		@Override
		public String getName() {
			return "item_held";
		}

		@Override
		public String docs() {
			return "{}"
					+ " Fires when a player changes which quickbar slot they have selected."
					+ " {player | to | from: the slot the player is switching from}"
					+ " {to: the slot that the player is switching to}"
					+ " {}";
		}

		@Override
		public boolean matches(Map<String, Construct> prefilter, BindableEvent event) throws PrefilterNonMatchException {
			if (event instanceof MCItemHeldEvent) {
				return true;
			}
			return false;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			throw ConfigRuntimeException.CreateUncatchableException("Unsupported operation.", Target.UNKNOWN);
		}

		@Override
		public Map<String, Construct> evaluate(BindableEvent event) throws EventException {
			if (event instanceof MCItemHeldEvent) {
				MCItemHeldEvent e = (MCItemHeldEvent) event;
				Map<String, Construct> ret = evaluate_helper(e);
				ret.put("to", new CInt(e.getNewSlot(), Target.UNKNOWN));
				ret.put("from", new CInt(e.getPreviousSlot(), Target.UNKNOWN));
				return ret;
			} else {
				throw new EventException("Event received was not an MCItemHeldEvent");
			}
		}

		@Override
		public Driver driver() {
			return Driver.ITEM_HELD;
		}

		@Override
		public boolean modifyEvent(String key, Construct value, BindableEvent event) {
			if (event instanceof MCItemHeldEvent) {
				MCItemHeldEvent e = (MCItemHeldEvent) event;
				if ("to".equals(key)) {
					e.getPlayer().getInventory().setHeldItemSlot(Static.getInt32(value, Target.UNKNOWN));
					return true;
				}
			}
			return false;
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class item_pre_craft extends AbstractEvent {

		@Override
		public String getName() {
			return "item_pre_craft";
		}

		@Override
		public String docs() {
			return "{}"
					+ " Fires when a recipe is formed in a crafting matrix, but the result has not yet been clicked."
					+ " {viewers: all humanentities viewing the screen this event takes place in | matrix | result"
					+ " | isRepair: true if this event was triggered by a repair operation (different than normal crafting)"
					+ " | recipe: information about the formed recipe, or null if there is not one}"
					+ " {}"/*" {matrix: the slots that make up the crafting grid | result: the result slot of crafting}"*/
					+ " {}";
		}

		@Override
		public boolean matches(Map<String, Construct> prefilter, BindableEvent event) throws PrefilterNonMatchException {
			if (event instanceof MCPrepareItemCraftEvent) {
				return true;
			}
			return false;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			throw ConfigRuntimeException.CreateUncatchableException("Unsupported operation.", Target.UNKNOWN);
		}

		@Override
		public Map<String, Construct> evaluate(BindableEvent event) throws EventException {
			if (event instanceof MCPrepareItemCraftEvent) {
				MCPrepareItemCraftEvent e = (MCPrepareItemCraftEvent) event;
				Map<String, Construct> ret = evaluate_helper(e);
				Target t = Target.UNKNOWN;
				CArray viewers = new CArray(t);
				for (MCHumanEntity v : e.getViewers()) {
					viewers.push(new CString(v.getName(), t), t);
				}
				ret.put("viewers", viewers);
				ret.put("recipe", ObjectGenerator.GetGenerator().recipe(e.getRecipe(), t));
				ret.put("isRepair", CBoolean.get(e.isRepair()));
				CArray matrix = CArray.GetAssociativeArray(t);
				MCItemStack[] mi = e.getInventory().getMatrix();
				for (int i=0; i<mi.length; i++) {
					matrix.set(i, ObjectGenerator.GetGenerator().item(mi[i], t), t);
				}
				ret.put("matrix", matrix);
				ret.put("result", ObjectGenerator.GetGenerator().item(e.getInventory().getResult(), t));
				return ret;
			} else {
				throw new EventException("Event received was not an MCPrepareItemCraftEvent.");
			}
		}

		@Override
		public Driver driver() {
			return Driver.ITEM_PRE_CRAFT;
		}

		@Override
		public boolean modifyEvent(String key, Construct value, BindableEvent event) {
			/*if (event instanceof MCPrepareItemCraftEvent) {
				MCPrepareItemCraftEvent e = (MCPrepareItemCraftEvent) event;
				if ("result".equals(key)) {
					e.getInventory().setResult(ObjectGenerator.GetGenerator().item(value, Target.UNKNOWN));
					return true;
				}
				if ("matrix".equals(key)) {
					if (value instanceof CArray) {
						CArray va = (CArray) value;
						MCItemStack[] old = e.getInventory().getMatrix();
						MCItemStack[] repl = new MCItemStack[old.length];
						for (int i=0; i<repl.length; i++) {
							if (va.containsKey(i)) {
								repl[i] = ObjectGenerator.GetGenerator().item(va, Target.UNKNOWN);
							}
						}
						e.getInventory().setMatrix(repl);
						return true;
					} else if (value instanceof CNull) {
						MCItemStack[] old = e.getInventory().getMatrix();
						MCItemStack[] repl = new MCItemStack[old.length];
						e.getInventory().setMatrix(repl);
						return true;
					} else {
						throw ConfigRuntimeException.BuildException("Expected an array but recieved " + value,
								ExceptionType.CastException, Target.UNKNOWN);
					}
				}
			} */
			return false;
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_1;
		}
	}
}
