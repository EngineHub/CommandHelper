package com.laytonsmith.core.events.drivers;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.MCAnvilInventory;
import com.laytonsmith.abstraction.MCEnchantmentOffer;
import com.laytonsmith.abstraction.MCGrindstoneInventory;
import com.laytonsmith.abstraction.MCHumanEntity;
import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCSmithingInventory;
import com.laytonsmith.abstraction.MCVirtualInventoryHolder;
import com.laytonsmith.abstraction.enums.MCClickType;
import com.laytonsmith.abstraction.enums.MCDragType;
import com.laytonsmith.abstraction.enums.MCInventoryAction;
import com.laytonsmith.abstraction.enums.MCSlotType;
import com.laytonsmith.abstraction.enums.MCVersion;
import com.laytonsmith.abstraction.events.MCEnchantItemEvent;
import com.laytonsmith.abstraction.events.MCInventoryClickEvent;
import com.laytonsmith.abstraction.events.MCInventoryCloseEvent;
import com.laytonsmith.abstraction.events.MCInventoryDragEvent;
import com.laytonsmith.abstraction.events.MCInventoryOpenEvent;
import com.laytonsmith.abstraction.events.MCItemHeldEvent;
import com.laytonsmith.abstraction.events.MCItemSwapEvent;
import com.laytonsmith.abstraction.events.MCPrepareAnvilEvent;
import com.laytonsmith.abstraction.events.MCPrepareGrindstoneEvent;
import com.laytonsmith.abstraction.events.MCPrepareItemCraftEvent;
import com.laytonsmith.abstraction.events.MCPrepareItemEnchantEvent;
import com.laytonsmith.abstraction.events.MCPrepareSmithingEvent;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.events.AbstractEvent;
import com.laytonsmith.core.events.BindableEvent;
import com.laytonsmith.core.events.BoundEvent;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.Prefilters;
import com.laytonsmith.core.events.Prefilters.PrefilterType;
import com.laytonsmith.core.exceptions.CRE.CREBindException;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.EventException;
import com.laytonsmith.core.exceptions.PrefilterNonMatchException;
import com.laytonsmith.core.functions.InventoryManagement;
import com.laytonsmith.core.natives.interfaces.Mixed;

import java.util.Map;

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
			return "{virtual: <boolean match> If the inventory is virtually held in CH"
					+ " | slottype: <macro> " + StringUtils.Join(MCSlotType.values(), ", ", ", or ")
					+ " | clicktype: <macro> " + StringUtils.Join(MCClickType.values(), ", ", ", or ")
					+ " | action: <macro> " + StringUtils.Join(MCInventoryAction.values(), ", ", ", or ")
					+ " | slotitem: <string match> The item type | player: <macro>}"
					+ " Fired when a player interacts with any inventory GUI. See also inventory_drag."
					+ " {slottype: The type of slot being clicked | clicktype: The type of input"
					+ " | action: The resulting action | slotitem: The item in the clicked slot"
					+ " | player: The player who clicked | viewers: Every player looking at this inventory"
					+ " | leftclick: If left mouse click | rightclick: If right mouse click"
					+ " | keyboardclick: If key press | shiftclick: If shift held"
					+ " | creativeclick: If action could only be performed in creative mode, like clone stack."
					+ " | slot: The slot number clicked. Will be -999 if the click occurred outside the window."
					+ " | rawslot: The slot number clicked in the whole inventory window. This will be higher than"
					+ " inventorysize if the click did not occur in the open inventory."
					+ " | inventorytype: The type of open inventory (if self-inventory, this will be CRAFTING)"
					+ " | inventorysize: Number of slots in the open inventory"
					+ " | cursoritem: The item on the cursor"
					+ " | inventory: An array of all the items in the open inventory"
					+ " | hotbarbutton: The hotbar slot (0-8) if a number key was pressed, or -1 none.}"
					+ " {slotitem: The item to put in the clicked slot"
					+ " | cursoritem: The item to put on the cursor}"
					+ " {}";
		}

		@Override
		@SuppressWarnings("deprecation")
		public void bind(BoundEvent event) {
			// handle deprecated prefilters
			Map<String, Mixed> prefilter = event.getPrefilter();
			if(prefilter.containsKey("slotitem")) {
				Mixed type = prefilter.get("slotitem");
				if(type.isInstanceOf(CString.TYPE) && type.val().contains(":") || ArgumentValidation.isNumber(type)) {
					MCItemStack is = Static.ParseItemNotation(null, prefilter.get("slotitem").val(), 1, event.getTarget());
					prefilter.put("slotitem", new CString(is.getType().getName(), event.getTarget()));
					MSLog.GetLogger().w(MSLog.Tags.DEPRECATION, "The item notation format for the \"slotitem\" prefilter"
							+ " in " + getName() + " is deprecated. Converted to " + is.getType().getName(), event.getTarget());
				}
			}
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent event) throws PrefilterNonMatchException {
			if(event instanceof MCInventoryClickEvent) {
				MCInventoryClickEvent e = (MCInventoryClickEvent) event;

				if(prefilter.containsKey("virtual")) {
					boolean isVirtual = e.getInventory().getHolder() instanceof MCVirtualInventoryHolder;
					if(isVirtual != ArgumentValidation.getBoolean(prefilter.get("virtual"), Target.UNKNOWN)) {
						return false;
					}
				}
				Prefilters.match(prefilter, "action", e.getAction().name(), PrefilterType.MACRO);
				Prefilters.match(prefilter, "player", e.getWhoClicked().getName(), PrefilterType.MACRO);
				Prefilters.match(prefilter, "clicktype", e.getClickType().name(), PrefilterType.MACRO);
				Prefilters.match(prefilter, "slottype", e.getSlotType().name(), PrefilterType.MACRO);
				Prefilters.match(prefilter, "slotitem", e.getCurrentItem().getType().getName(), PrefilterType.STRING_MATCH);

				return true;
			}
			return false;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			throw new CREBindException("Unsupported Operation", Target.UNKNOWN);
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent event) throws EventException {
			if(event instanceof MCInventoryClickEvent) {
				MCInventoryClickEvent e = (MCInventoryClickEvent) event;
				Map<String, Mixed> map = evaluate_helper(event);
				Target t = Target.UNKNOWN;

				map.put("player", new CString(e.getWhoClicked().getName(), t));
				CArray viewers = new CArray(t);
				for(MCHumanEntity viewer : e.getViewers()) {
					viewers.push(new CString(viewer.getName(), t), t);
				}
				map.put("viewers", viewers);

				map.put("action", new CString(e.getAction().name(), t));
				map.put("clicktype", new CString(e.getClickType().name(), t));

				map.put("leftclick", CBoolean.get(e.isLeftClick()));
				map.put("rightclick", CBoolean.get(e.isRightClick()));
				map.put("shiftclick", CBoolean.get(e.isShiftClick()));
				map.put("creativeclick", CBoolean.get(e.isCreativeClick()));
				map.put("keyboardclick", CBoolean.get(e.isKeyboardClick()));
				map.put("cursoritem", ObjectGenerator.GetGenerator().item(e.getCursor(), t));

				map.put("slot", new CInt(e.getSlot(), t));
				map.put("rawslot", new CInt(e.getRawSlot(), t));
				map.put("hotbarbutton", new CInt(e.getHotbarButton(), t));
				map.put("slottype", new CString(e.getSlotType().name(), t));
				map.put("slotitem", ObjectGenerator.GetGenerator().item(e.getCurrentItem(), t));

				CArray items = CArray.GetAssociativeArray(t);
				MCInventory inv = e.getInventory();
				for(int i = 0; i < inv.getSize(); i++) {
					items.set(i, ObjectGenerator.GetGenerator().item(inv.getItem(i), t), t);
				}
				map.put("inventory", items);
				map.put("inventorytype", new CString(inv.getType().name(), t));
				map.put("inventorysize", new CInt(inv.getSize(), t));

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
		public boolean modifyEvent(String key, Mixed value, BindableEvent event) {
			if(event instanceof MCInventoryClickEvent) {
				MCInventoryClickEvent e = (MCInventoryClickEvent) event;

				if(key.equalsIgnoreCase("slotitem")) {
					e.setCurrentItem(ObjectGenerator.GetGenerator().item(value, value.getTarget()));
					return true;
				}
				if(key.equalsIgnoreCase("cursoritem")) {
					e.setCursor(ObjectGenerator.GetGenerator().item(value, value.getTarget()));
					return true;
				}
			}
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
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
			return "{virtual: <boolean match> Whether or not this inventory is virtually stored in CH"
					+ " | world: <macro> World name"
					+ " | type: <string match> Can be " + StringUtils.Join(MCDragType.values(), ", ", ", or ")
					+ " | cursoritem: <string match> old item type held by the cursor before event starts}"
					+ "Fired when a player clicks (by left or right mouse button) a slot in an inventory and then drags"
					+ " the mouse across slots. "
					+ "{player: The player who clicked | newcursoritem: item on cursor, after event"
					+ " | oldcursoritem: item on cursor, before event | slots: used slots"
					+ " | rawslots: used slots, as the numbers of the slots in whole inventory window"
					+ " | newitems: array of items which are dropped in selected slots | inventorytype"
					+ " | inventorysize: number of slots in opened inventory}"
					+ "{cursoritem: the item on the cursor, after event} "
					+ "{} ";
		}

		@Override
		@SuppressWarnings("deprecation")
		public void bind(BoundEvent event) {
			// handle deprecated prefilters
			Map<String, Mixed> prefilter = event.getPrefilter();
			if(prefilter.containsKey("cursoritem")) {
				Mixed type = prefilter.get("cursoritem");
				if(type.isInstanceOf(CString.TYPE) && type.val().contains(":") || ArgumentValidation.isNumber(type)) {
					MCItemStack is = Static.ParseItemNotation(null, prefilter.get("cursoritem").val(), 1, event.getTarget());
					prefilter.put("cursoritem", new CString(is.getType().getName(), event.getTarget()));
					MSLog.GetLogger().w(MSLog.Tags.DEPRECATION, "The item notation format for the \"cursoritem\" prefilter"
							+ " in " + getName() + " is deprecated. Converted to " + is.getType().getName(), event.getTarget());
				}
			}
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent event) throws PrefilterNonMatchException {
			if(event instanceof MCInventoryDragEvent) {
				MCInventoryDragEvent e = (MCInventoryDragEvent) event;

				if(prefilter.containsKey("virtual")) {
					boolean isVirtual = e.getInventory().getHolder() instanceof MCVirtualInventoryHolder;
					if(isVirtual != ArgumentValidation.getBoolean(prefilter.get("virtual"), Target.UNKNOWN)) {
						return false;
					}
				}
				Prefilters.match(prefilter, "world", e.getWhoClicked().getWorld().getName(), PrefilterType.MACRO);
				Prefilters.match(prefilter, "type", e.getType().name(), PrefilterType.STRING_MATCH);
				Prefilters.match(prefilter, "cursoritem", e.getOldCursor().getType().getName(), PrefilterType.STRING_MATCH);

				return true;
			}
			return false;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			return null;
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent event) throws EventException {
			if(event instanceof MCInventoryDragEvent) {
				MCInventoryDragEvent e = (MCInventoryDragEvent) event;
				Map<String, Mixed> map = evaluate_helper(event);

				map.put("player", new CString(e.getWhoClicked().getName(), Target.UNKNOWN));
				map.put("newcursoritem", ObjectGenerator.GetGenerator().item(e.getCursor(), Target.UNKNOWN));
				map.put("oldcursoritem", ObjectGenerator.GetGenerator().item(e.getOldCursor(), Target.UNKNOWN));

				CArray slots = new CArray(Target.UNKNOWN);
				for(Integer slot : e.getInventorySlots()) {
					slots.push(new CInt(slot.intValue(), Target.UNKNOWN), Target.UNKNOWN);
				}
				map.put("slots", slots);

				CArray rawSlots = new CArray(Target.UNKNOWN);
				for(Integer slot : e.getRawSlots()) {
					rawSlots.push(new CInt(slot.intValue(), Target.UNKNOWN), Target.UNKNOWN);
				}
				map.put("rawslots", rawSlots);

				CArray newItems = CArray.GetAssociativeArray(Target.UNKNOWN);
				for(Map.Entry<Integer, MCItemStack> ni : e.getNewItems().entrySet()) {
					Integer key = ni.getKey();
					MCItemStack value = ni.getValue();
					newItems.set(key.intValue(), ObjectGenerator.GetGenerator().item(value, Target.UNKNOWN), Target.UNKNOWN);
				}
				map.put("newitems", newItems);

				CArray items = CArray.GetAssociativeArray(Target.UNKNOWN);
				MCInventory inv = e.getInventory();
				for(int i = 0; i < inv.getSize(); i++) {
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
		public boolean modifyEvent(String key, Mixed value, BindableEvent event) {
			if(event instanceof MCInventoryDragEvent) {
				MCInventoryDragEvent e = (MCInventoryDragEvent) event;

				if(key.equalsIgnoreCase("cursoritem")) {
					e.setCursor(ObjectGenerator.GetGenerator().item(value, value.getTarget()));
					return true;
				}
			}
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
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
			return "{virtual: <boolean match> Whether or not this inventory is virtually stored in CH} "
					+ "Fired when a player opens an inventory. "
					+ "{player: The player | inventory: the inventory items in this inventory"
					+ " | inventorytype: type of inventory | virtual"
					+ " | holder: block location array, entity UUID, or virtual id for this inventory (can be null)}"
					+ "{} "
					+ "{} ";
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent event) throws PrefilterNonMatchException {
			if(event instanceof MCInventoryOpenEvent) {
				MCInventoryOpenEvent e = (MCInventoryOpenEvent) event;
				if(prefilter.containsKey("virtual")) {
					boolean isVirtual = e.getInventory().getHolder() instanceof MCVirtualInventoryHolder;
					if(isVirtual != ArgumentValidation.getBoolean(prefilter.get("virtual"), Target.UNKNOWN)) {
						return false;
					}
				}
			}
			return true;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			return null;
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent event) throws EventException {
			if(event instanceof MCInventoryOpenEvent) {
				MCInventoryOpenEvent e = (MCInventoryOpenEvent) event;
				Map<String, Mixed> map = evaluate_helper(event);
				Target t = Target.UNKNOWN;

				map.put("player", new CString(e.getPlayer().getName(), t));

				CArray items = CArray.GetAssociativeArray(t);
				MCInventory inv = e.getInventory();
				for(int i = 0; i < inv.getSize(); i++) {
					Mixed c = ObjectGenerator.GetGenerator().item(inv.getItem(i), t);
					items.set(i, c, t);
				}
				map.put("inventory", items);

				map.put("inventorytype", new CString(inv.getType().name(), t));
				map.put("holder", InventoryManagement.GetInventoryHolder(inv, t));
				map.put("virtual", CBoolean.get(inv.getHolder() instanceof MCVirtualInventoryHolder));

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
		public boolean modifyEvent(String key, Mixed value, BindableEvent event) {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
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
			return "{virtual: <boolean match> Whether or not this inventory is virtually stored in CH} "
					+ "Fired when a player closes an inventory. "
					+ "{player: The player | inventory: the inventory items in this inventory"
					+ " | inventorytype: type of inventory | virtual"
					+ " | holder: block location array, entity UUID, or virtual id for this inventory (can be null)}"
					+ "{} "
					+ "{} ";
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent event) throws PrefilterNonMatchException {
			if(event instanceof MCInventoryCloseEvent) {
				MCInventoryCloseEvent e = (MCInventoryCloseEvent) event;
				if(prefilter.containsKey("virtual")) {
					boolean isVirtual = e.getInventory().getHolder() instanceof MCVirtualInventoryHolder;
					if(isVirtual != ArgumentValidation.getBoolean(prefilter.get("virtual"), Target.UNKNOWN)) {
						return false;
					}
				}
			}
			return true;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			return null;
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent event) throws EventException {
			if(event instanceof MCInventoryCloseEvent) {
				MCInventoryCloseEvent e = (MCInventoryCloseEvent) event;
				Map<String, Mixed> map = evaluate_helper(event);
				Target t = Target.UNKNOWN;

				map.put("player", new CString(e.getPlayer().getName(), t));

				CArray items = CArray.GetAssociativeArray(t);
				MCInventory inv = e.getInventory();
				for(int i = 0; i < inv.getSize(); i++) {
					Mixed c = ObjectGenerator.GetGenerator().item(inv.getItem(i), t);
					items.set(i, c, t);
				}
				map.put("inventory", items);

				map.put("inventorytype", new CString(inv.getType().name(), t));
				map.put("holder", InventoryManagement.GetInventoryHolder(inv, t));
				map.put("virtual", CBoolean.get(inv.getHolder() instanceof MCVirtualInventoryHolder));

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
		public boolean modifyEvent(String key, Mixed value, BindableEvent event) {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
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
					+ "option: The enchantment option the player clicked | "
					+ "levelhint: The level displayed as a hint to the player (MC 1.20.1+) | "
					+ "enchanthint: The enchantment displayed as a hint to the player (MC 1.20.1+)}"
					+ "{levels: The amount of levels to use | "
					+ "item: The item to be enchanted | "
					+ "enchants: The enchants to add to the item}"
					+ "{}";
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			return true;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			return null;
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent event) throws EventException {
			if(event instanceof MCEnchantItemEvent) {
				MCEnchantItemEvent e = (MCEnchantItemEvent) event;
				Map<String, Mixed> map = evaluate_helper(event);

				map.put("player", new CString(e.GetEnchanter().getName(), Target.UNKNOWN));
				map.put("item", ObjectGenerator.GetGenerator().item(e.getItem(), Target.UNKNOWN));
				map.put("inventorytype", new CString(e.getInventory().getType().name(), Target.UNKNOWN));
				map.put("levels", new CInt(e.getExpLevelCost(), Target.UNKNOWN));
				map.put("enchants", ObjectGenerator.GetGenerator().enchants(e.getEnchantsToAdd(), Target.UNKNOWN));
				map.put("location", ObjectGenerator.GetGenerator().location(e.getEnchantBlock().getLocation(), false));
				map.put("option", new CInt(e.whichButton(), Target.UNKNOWN));
				if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_20_1)) {
					map.put("levelhint", new CInt(e.getLevelHint(), Target.UNKNOWN));
					map.put("enchanthint", new CString(e.getEnchantmentHint().name().toLowerCase(), Target.UNKNOWN));
				}

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
		public boolean modifyEvent(String key, Mixed value, BindableEvent event) {
			if(event instanceof MCEnchantItemEvent) {
				MCEnchantItemEvent e = (MCEnchantItemEvent) event;

				if(key.equalsIgnoreCase("levels")) {
					e.setExpLevelCost(ArgumentValidation.getInt32(value, value.getTarget()));
					return true;
				}

				if(key.equalsIgnoreCase("item")) {
					e.setItem(ObjectGenerator.GetGenerator().item(value, value.getTarget()));
					return true;
				}

				if(key.equalsIgnoreCase("enchants")) {
					e.setEnchantsToAdd((ObjectGenerator.GetGenerator().enchants((CArray) value, value.getTarget())));
					return true;
				}
			}
			return false;
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
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
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			return true;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			return null;
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent event) throws EventException {
			if(event instanceof MCPrepareItemEnchantEvent) {
				Target t = Target.UNKNOWN;
				MCPrepareItemEnchantEvent e = (MCPrepareItemEnchantEvent) event;
				Map<String, Mixed> map = evaluate_helper(event);

				map.put("player", new CString(e.getEnchanter().getName(), t));
				map.put("item", ObjectGenerator.GetGenerator().item(e.getItem(), t));
				map.put("inventorytype", new CString(e.getInventory().getType().name(), t));
				map.put("enchantmentbonus", new CInt(e.getEnchantmentBonus(), t));

				CArray expCostsCArray = new CArray(t);
				MCEnchantmentOffer[] offers = e.getOffers();
				for(MCEnchantmentOffer offer : offers) {
					expCostsCArray.push(new CInt(offer.getCost(), t), t);
				}
				map.put("expcosts", expCostsCArray);

				map.put("location", ObjectGenerator.GetGenerator().location(e.getEnchantBlock().getLocation(), false));

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
		public boolean modifyEvent(String key, Mixed value, BindableEvent event) {
			if(event instanceof MCPrepareItemEnchantEvent) {
				Target t = value.getTarget();
				MCPrepareItemEnchantEvent e = (MCPrepareItemEnchantEvent) event;

				if(key.equalsIgnoreCase("item")) {
					e.setItem(ObjectGenerator.GetGenerator().item(value, t));
					return true;
				}

				if(key.equalsIgnoreCase("expcosts")) {
					if(value.isInstanceOf(CArray.TYPE)) {
						CArray cExpCosts = (CArray) value;
						if(!cExpCosts.inAssociativeMode()) {
							MCEnchantmentOffer[] offers = e.getOffers();

							for(int i = 0; i <= 2; i++) {
								MCEnchantmentOffer offer = offers[i];
								Mixed cost = cExpCosts.get(i, t);
								offer.setCost(ArgumentValidation.getInt32(cost, t));
							}
						} else {
							throw new CREFormatException("Expected a normal array!", t);
						}
					} else {
						throw new CREFormatException("Expected an array!", t);
					}
				}
			}
			return false;
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
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
			return "{player: <string match>}"
					+ " Fires when a player changes which quickbar slot they have selected."
					+ " {player | to | from: the slot the player is switching from}"
					+ " {to: the slot that the player is switching to}"
					+ " {}";
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent event) throws PrefilterNonMatchException {
			if(event instanceof MCItemHeldEvent) {
				MCItemHeldEvent e = (MCItemHeldEvent) event;
				if(prefilter.containsKey("player") && !e.getPlayer().getName().equals(prefilter.get("player").val())) {
					return false;
				}
				return true;
			}
			return false;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			throw ConfigRuntimeException.CreateUncatchableException("Unsupported operation.", Target.UNKNOWN);
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent event) throws EventException {
			if(event instanceof MCItemHeldEvent) {
				MCItemHeldEvent e = (MCItemHeldEvent) event;
				Map<String, Mixed> ret = evaluate_helper(e);
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
		public boolean modifyEvent(String key, Mixed value, BindableEvent event) {
			if(event instanceof MCItemHeldEvent) {
				MCItemHeldEvent e = (MCItemHeldEvent) event;
				if("to".equals(key)) {
					e.getPlayer().getInventory().setHeldItemSlot(ArgumentValidation.getInt32(value, value.getTarget()));
					return true;
				}
			}
			return false;
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
	public static class item_swap extends AbstractEvent {

		@Override
		public String getName() {
			return "item_swap";
		}

		@Override
		public String docs() {
			return "{player: <macro>"
					+ " | main_hand: <string match> The name of the item being swapped to the main hand (or AIR)"
					+ " | off_hand: <string match> The name of the item being swapped to the off hand (or AIR)}"
					+ " Fires when a player swaps the items between their main and off hands."
					+ " {player | main_hand: The item array being swapped to the main hand (or null)"
					+ " | off_hand: The item array being swapped to the off hand (or null)}"
					+ " {main_hand | off_hand}"
					+ " {}";
		}

		@Override
		@SuppressWarnings("deprecation")
		public void bind(BoundEvent event) {
			// handle deprecated prefilters
			Map<String, Mixed> prefilter = event.getPrefilter();
			if(prefilter.containsKey("main_hand")) {
				Mixed type = prefilter.get("main_hand");
				if(type.isInstanceOf(CString.TYPE) && type.val().contains(":") || ArgumentValidation.isNumber(type)) {
					MSLog.GetLogger().w(MSLog.Tags.DEPRECATION, "The item notation format in the \"main_hand\""
							+ " prefilter in " + getName() + " is deprecated.", event.getTarget());
					MCItemStack is = Static.ParseItemNotation(null, prefilter.get("main_hand").val(), 1, event.getTarget());
					prefilter.put("main_hand", new CString(is.getType().getName(), event.getTarget()));
				}
			}
			if(prefilter.containsKey("off_hand")) {
				Mixed type = prefilter.get("off_hand");
				if(type.isInstanceOf(CString.TYPE) && type.val().contains(":") || ArgumentValidation.isNumber(type)) {
					MSLog.GetLogger().w(MSLog.Tags.DEPRECATION, "The item notation format in the \"off_hand\""
							+ " prefilter in " + getName() + " is deprecated.", event.getTarget());
					MCItemStack is = Static.ParseItemNotation(null, prefilter.get("off_hand").val(), 1, event.getTarget());
					prefilter.put("off_hand", new CString(is.getType().getName(), event.getTarget()));
				}
			}
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent event) throws PrefilterNonMatchException {
			if(event instanceof MCItemSwapEvent) {
				MCItemSwapEvent e = (MCItemSwapEvent) event;

				Prefilters.match(prefilter, "player", e.getPlayer().getName(), PrefilterType.MACRO);
				if(prefilter.containsKey("main_hand")) {
					String value = prefilter.get("main_hand").val();
					if(!e.getMainHandItem().getType().getName().equals(value)) {
						return false;
					}
				}
				if(prefilter.containsKey("off_hand")) {
					String value = prefilter.get("off_hand").val();
					if(!e.getOffHandItem().getType().getName().equals(value)) {
						return false;
					}
				}

				return true;
			}
			return false;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			throw ConfigRuntimeException.CreateUncatchableException("Unsupported operation.", Target.UNKNOWN);
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent event) throws EventException {
			if(event instanceof MCItemSwapEvent) {
				MCItemSwapEvent e = (MCItemSwapEvent) event;
				Map<String, Mixed> ret = evaluate_helper(e);
				ret.put("main_hand", ObjectGenerator.GetGenerator().item(e.getMainHandItem(), Target.UNKNOWN));
				ret.put("off_hand", ObjectGenerator.GetGenerator().item(e.getOffHandItem(), Target.UNKNOWN));
				return ret;
			} else {
				throw new EventException("Event received was not an MCItemSwapEvent");
			}
		}

		@Override
		public Driver driver() {
			return Driver.ITEM_SWAP;
		}

		@Override
		public boolean modifyEvent(String key, Mixed value, BindableEvent event) {
			if(event instanceof MCItemSwapEvent) {
				MCItemSwapEvent e = (MCItemSwapEvent) event;
				if("main_hand".equals(key)) {
					e.setMainHandItem(ObjectGenerator.GetGenerator().item(value, value.getTarget()));
					return true;
				}
				if("off_hand".equals(key)) {
					e.setOffHandItem(ObjectGenerator.GetGenerator().item(value, value.getTarget()));
					return true;
				}
			}
			return false;
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_2;
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
					+ " Fires when a slot is clicked in a crafting matrix."
					+ " Can also fire when clearing an inventory with the the /clear command or shift-clicking the"
					+ " 'Destroy Item' button in creative mode."
					+ " {player: the name of the player who clicked"
					+ " | viewers | matrix: an array of item arrays in the crafting matrix | result"
					+ " | isRepair: true if this event was triggered by a repair operation"
					+ " | recipe: an array of data about the formed recipe, or null if there is not one}"
					+ " { result: the item array product of the recipe. }"
					+ " {}";
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent event) throws PrefilterNonMatchException {
			if(event instanceof MCPrepareItemCraftEvent) {
				return true;
			}
			return false;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			throw ConfigRuntimeException.CreateUncatchableException("Unsupported operation.", Target.UNKNOWN);
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent event) throws EventException {
			if(event instanceof MCPrepareItemCraftEvent e) {
				Map<String, Mixed> ret = evaluate_helper(e);
				Target t = Target.UNKNOWN;
				CArray viewers = new CArray(t);
				for(MCHumanEntity v : e.getViewers()) {
					viewers.push(new CString(v.getName(), t), t);
				}
				ret.put("viewers", viewers);
				ret.put("player", new CString(e.getPlayer().getName(), t));
				ret.put("recipe", ObjectGenerator.GetGenerator().recipe(e.getRecipe(), t));
				ret.put("isRepair", CBoolean.get(e.isRepair()));
				CArray matrix = CArray.GetAssociativeArray(t);
				MCItemStack[] mi = e.getInventory().getMatrix();
				for(int i = 0; i < mi.length; i++) {
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
		public boolean modifyEvent(String key, Mixed value, BindableEvent event) {
			if(event instanceof MCPrepareItemCraftEvent e) {
				Target t = Target.UNKNOWN;
				if("result".equals(key)) {
					e.getInventory().setResult(ObjectGenerator.GetGenerator().item(value, t));
					return true;
				}
			}
			return false;
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
	public static class item_pre_anvil extends AbstractEvent {

		@Override
		public String getName() {
			return "item_pre_anvil";
		}

		@Override
		public String docs() {
			return "{}"
					+ " Fires when a slot other than the result is clicked in an anvil."
					+ " Can fire multiple times per click."
					+ " { player: the player using the anvil."
					+ " | first_item: the first item being used in the recipe."
					+ " | second_item: the second item being used in the recipe."
					+ " | result: the result of the recipe."
					+ " | max_repair_cost: the maximum possible cost of this repair."
					+ " | level_repair_cost: how many levels are needed to perform this repair."
					+ " | item_repair_cost: how many items are needed to perform this repair (MC 1.18.1+). }"
					+ " { result: the result of the recipe."
					+ " | level_repair_cost: how many levels are needed to perform this repair."
					+ " | item_repair_cost: how many items are needed to perform this repair (MC 1.18.1+)."
					+ " | max_repair_cost: the maximum possible cost of this repair. Values exceeding 40 will be"
					+ " respected, but may still show as too expensive on the client. }"
					+ " {}";
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent event) throws PrefilterNonMatchException {
			if(event instanceof MCPrepareAnvilEvent) {
				return true;
			}
			return false;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			throw ConfigRuntimeException.CreateUncatchableException("Unsupported operation.", Target.UNKNOWN);
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent event) throws EventException {
			if(event instanceof MCPrepareAnvilEvent e) {
				MCAnvilInventory anvil = (MCAnvilInventory) e.getInventory();
				Map<String, Mixed> ret = evaluate_helper(e);

				ret.put("player", new CString(e.getPlayer().getName(), Target.UNKNOWN));
				ret.put("first_item", ObjectGenerator.GetGenerator().item(anvil.getFirstItem(), Target.UNKNOWN));
				ret.put("second_item", ObjectGenerator.GetGenerator().item(anvil.getSecondItem(), Target.UNKNOWN));
				ret.put("result", ObjectGenerator.GetGenerator().item(anvil.getResult(), Target.UNKNOWN));
				ret.put("level_repair_cost", new CInt(anvil.getRepairCost(), Target.UNKNOWN));
				if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_18_1)) {
					ret.put("item_repair_cost", new CInt(anvil.getRepairCostAmount(), Target.UNKNOWN));
				}
				ret.put("max_repair_cost", new CInt(anvil.getMaximumRepairCost(), Target.UNKNOWN));

				return ret;
			} else {
				throw new EventException("Event received was not an MCPrepareAnvilEvent.");
			}
		}

		@Override
		public Driver driver() {
			return Driver.ITEM_PRE_ANVIL;
		}

		@Override
		public boolean modifyEvent(String key, Mixed value, BindableEvent event) {
			if(event instanceof MCPrepareAnvilEvent e) {
				Target t = value.getTarget();
				if(key.equalsIgnoreCase("result")) {
					e.setResult(ObjectGenerator.GetGenerator().item(value, t));
					return true;
				}

				MCAnvilInventory anvil = (MCAnvilInventory) e.getInventory();
				if(key.equalsIgnoreCase("level_repair_cost")) {
					anvil.setRepairCost(ArgumentValidation.getInt32(value, t));
					return true;
				}

				if(key.equalsIgnoreCase("item_repair_cost")) {
					if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_18_1)) {
						anvil.setRepairCostAmount(ArgumentValidation.getInt32(value, t));
						return true;
					} else {
						return false;
					}
				}

				if(key.equalsIgnoreCase("max_repair_cost")) {
					anvil.setMaximumRepairCost(ArgumentValidation.getInt32(value, t));
					return true;
				}
			}
			return false;
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}
	}

	@api
	public static class item_pre_smithing extends AbstractEvent {

		@Override
		public String getName() {
			return "item_pre_smithing";
		}

		@Override
		public String docs() {
			return "{}"
					+ " Fires when a slot other than the result is clicked in a smithing table."
					+ " Can fire multiple times per click."
					+ " { player: the player using the smithing table."
					+ " | first_item: the first item being used in the recipe."
					+ " | second_item: the second item being used in the recipe."
					+ " | third_item: the third item being used in the recipe. (MC 1.20+)"
					+ " | recipe: information about the formed recipe, or null if there's not one."
					+ " | result: the result of the recipe. }"
					+ " { result: the result of the smithing table recipe. While the result will appear on the"
					+ " client-side, all items must be populated before a recipe can be executed. }"
					+ " {}";
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent event) throws PrefilterNonMatchException {
			if(event instanceof MCPrepareSmithingEvent) {
				return true;
			}
			return false;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			throw ConfigRuntimeException.CreateUncatchableException("Unsupported operation.", Target.UNKNOWN);
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent event) throws EventException {
			if(event instanceof MCPrepareSmithingEvent e) {
				MCSmithingInventory smithing = (MCSmithingInventory) e.getInventory();
				Map<String, Mixed> ret = evaluate_helper(e);

				ret.put("player", new CString(e.getPlayer().getName(), Target.UNKNOWN));
				ret.put("first_item", ObjectGenerator.GetGenerator().item(smithing.getInputTemplate(), Target.UNKNOWN));
				ret.put("second_item", ObjectGenerator.GetGenerator().item(smithing.getInputEquipment(), Target.UNKNOWN));
				if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_20)) {
					ret.put("third_item", ObjectGenerator.GetGenerator().item(smithing.getInputMaterial(), Target.UNKNOWN));
				}
				ret.put("recipe", ObjectGenerator.GetGenerator().recipe(smithing.getRecipe(), Target.UNKNOWN));
				ret.put("result", ObjectGenerator.GetGenerator().item(smithing.getResult(), Target.UNKNOWN));

				return ret;
			} else {
				throw new EventException("Event received was not an MCPrepareSmithingEvent.");
			}
		}

		@Override
		public Driver driver() {
			return Driver.ITEM_PRE_SMITHING;
		}

		@Override
		public boolean modifyEvent(String key, Mixed value, BindableEvent event) {
			if(event instanceof MCPrepareSmithingEvent e) {
				Target t = value.getTarget();
				if(key.equalsIgnoreCase("result")) {
					e.setResult(ObjectGenerator.GetGenerator().item(value, t));
					return true;
				}
			}
			return false;
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}
	}

	@api
	public static class item_pre_grindstone extends AbstractEvent {

		@Override
		public String getName() {
			return "item_pre_grindstone";
		}

		@Override
		public String docs() {
			return "{}"
					+ " Fires when a slot other than the result is clicked in a grindstone. (MC 1.19.3+)"
					+ " Can fire multiple times per click."
					+ " { player: the player using the grindstone."
					+ " | upper_item: the first item being used in the recipe."
					+ " | lower_item: the second item being used in the recipe."
					+ " | result: the result of the recipe. }"
					+ " { result: the result of the grindstone recipe. }"
					+ " {}";
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent event) throws PrefilterNonMatchException {
			if(event instanceof MCPrepareGrindstoneEvent) {
				return true;
			}
			return false;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			throw ConfigRuntimeException.CreateUncatchableException("Unsupported operation.", Target.UNKNOWN);
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent event) throws EventException {
			if(event instanceof MCPrepareGrindstoneEvent e) {
				MCGrindstoneInventory grindstone = (MCGrindstoneInventory) e.getInventory();
				Map<String, Mixed> ret = evaluate_helper(e);

				ret.put("player", new CString(e.getPlayer().getName(), Target.UNKNOWN));
				ret.put("upper_item", ObjectGenerator.GetGenerator().item(grindstone.getUpperItem(), Target.UNKNOWN));
				ret.put("lower_item", ObjectGenerator.GetGenerator().item(grindstone.getLowerItem(), Target.UNKNOWN));
				ret.put("result", ObjectGenerator.GetGenerator().item(grindstone.getResult(), Target.UNKNOWN));

				return ret;
			} else {
				throw new EventException("Event received was not an MCPrepareGrindstoneEvent.");
			}
		}

		@Override
		public Driver driver() {
			return Driver.ITEM_PRE_GRINDSTONE;
		}

		@Override
		public boolean modifyEvent(String key, Mixed value, BindableEvent event) {
			if(event instanceof MCPrepareGrindstoneEvent e) {
				Target t = value.getTarget();
				if(key.equalsIgnoreCase("result")) {
					e.setResult(ObjectGenerator.GetGenerator().item(value, t));
					return true;
				}
			}
			return false;
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}
	}
}
