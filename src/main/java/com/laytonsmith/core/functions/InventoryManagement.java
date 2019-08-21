package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCDoubleChest;
import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCHumanEntity;
import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.MCInventoryHolder;
import com.laytonsmith.abstraction.MCInventoryView;
import com.laytonsmith.abstraction.MCItemMeta;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCPlayerInventory;
import com.laytonsmith.abstraction.MCVirtualInventoryHolder;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.abstraction.blocks.MCBlockState;
import com.laytonsmith.abstraction.enums.MCInventoryType;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Optimizable;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CNumber;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CREBadEntityException;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CREIllegalArgumentException;
import com.laytonsmith.core.exceptions.CRE.CREInsufficientArgumentsException;
import com.laytonsmith.core.exceptions.CRE.CREInvalidWorldException;
import com.laytonsmith.core.exceptions.CRE.CRELengthException;
import com.laytonsmith.core.exceptions.CRE.CRENotFoundException;
import com.laytonsmith.core.exceptions.CRE.CREPlayerOfflineException;
import com.laytonsmith.core.exceptions.CRE.CRERangeException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InventoryManagement {

	public static String docs() {
		return "Provides methods for managing inventory related tasks.";
	}

	private static final String ITEM_OBJECT = " An item is an associative array with the following keys,"
			+ " name: the string id of the item,"
			+ " qty: The number of items in their inventory,"
			+ " meta: An array of item meta or null if none exists (see {{function|get_itemmeta}} for details).";

	@api(environments = {CommandHelperEnvironment.class})
	public static class pinv extends AbstractFunction {

		@Override
		public String getName() {
			return "pinv";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1, 2};
		}

		@Override
		public String docs() {
			return "array {[player, [slot]]} Gets the inventory information for the specified player, or the current "
					+ " player if none specified. If the index is specified, only the slot given will be returned."
					+ " The index of the array in the array is 0 - 35, 100 - 103, -106, which corresponds to the slot"
					+ " in the player's inventory. To access armor slots, you may also specify the index. (100 - 103)."
					+ " The quick bar is 0 - 8. If index is null, the item in the player's hand is returned, regardless"
					+ " of what slot is selected. If index is -106, the player's off-hand item is returned. If there is"
					+ " no item at the slot specified, null is returned."
					+ " ---- If all slots are requested, an associative array of item objects is returned, and if"
					+ " only one item is requested, just that single item object is returned." + ITEM_OBJECT;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CRELengthException.class, CRECastException.class,
					CRERangeException.class, CRENotFoundException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_1_3;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			Integer index = -1;
			boolean all;
			MCPlayer m;
			if(args.length == 0) {
				all = true;
				m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
				Static.AssertPlayerNonNull(m, t);
			} else if(args.length == 1) {
				all = true;
				m = Static.GetPlayer(args[0], t);
			} else {
				if(args[1] instanceof CNull) {
					index = null;
				} else {
					index = Static.getInt32(args[1], t);
				}
				all = false;
				m = Static.GetPlayer(args[0], t);
			}
			if(all) {
				CArray ret = CArray.GetAssociativeArray(t);
				for(int i = 0; i < 36; i++) {
					ret.set(i, getInvSlot(m, i, t), t);
				}
				for(int i = 100; i < 104; i++) {
					ret.set(i, getInvSlot(m, i, t), t);
				}
				ret.set(-106, getInvSlot(m, -106, t), t);
				return ret;
			} else {
				return getInvSlot(m, index, t);
			}
		}

		private Mixed getInvSlot(MCPlayer m, Integer slot, Target t) {
			MCPlayerInventory inv = m.getInventory();
			if(inv == null) {
				throw new CRENotFoundException(
						"Could not find the inventory of the given player (are you running in cmdline mode?)", t);
			}
			if(slot == null) {
				return ObjectGenerator.GetGenerator().item(inv.getItemInMainHand(), t);
			}
			if(slot.equals(36)) {
				slot = 100;
			}
			if(slot.equals(37)) {
				slot = 101;
			}
			if(slot.equals(38)) {
				slot = 102;
			}
			if(slot.equals(39)) {
				slot = 103;
			}
			MCItemStack is;
			if(slot >= 0 && slot <= 35) {
				is = inv.getItem(slot);
			} else if(slot.equals(100)) {
				is = inv.getBoots();
			} else if(slot.equals(101)) {
				is = inv.getLeggings();
			} else if(slot.equals(102)) {
				is = inv.getChestplate();
			} else if(slot.equals(103)) {
				is = inv.getHelmet();
			} else if(slot.equals(-106)) {
				is = inv.getItemInOffHand();
			} else {
				throw new CRERangeException("Slot index must be 0-35, or 100-103, or -106", t);
			}
			return ObjectGenerator.GetGenerator().item(is, t);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class close_pinv extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CRELengthException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p;

			if(args.length == 1) {
				p = Static.GetPlayer(args[0], t);
			} else {
				p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			}

			Static.AssertPlayerNonNull(p, t);
			p.closeInventory();

			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "close_pinv";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "void {[player]} Closes the inventory of the current player, or of the specified player.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class pworkbench extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CRELengthException.class,
					CREInsufficientArgumentsException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p;

			if(args.length == 1) {
				p = Static.GetPlayer(args[0], t);
			} else {
				p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
				if(p == null) {
					throw new CREInsufficientArgumentsException(
							"You have to specify a player when running " + this.getName() + " from console.", t);
				}
			}

			p.openWorkbench(p.getLocation(), true);

			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "pworkbench";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "void {[player]} Shows a workbench to the current player, "
					+ "or a specified player.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class show_enderchest extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CRELengthException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer player;
			MCPlayer other;

			if(args.length == 1) {
				player = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
				other = Static.GetPlayer(args[0], t);
			} else if(args.length == 2) {
				other = Static.GetPlayer(args[0], t);
				player = Static.GetPlayer(args[1], t);
			} else {
				player = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
				other = player;
			}

			Static.AssertPlayerNonNull(player, t);
			Static.AssertPlayerNonNull(other, t);
			player.openInventory(other.getEnderChest());

			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "show_enderchest";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1, 2};
		}

		@Override
		public String docs() {
			return "void {[player [, player]]} Shows the enderchest of either the current player "
					+ " or the specified player if given. If a second player is specified, shows the"
					+ " second player the contents of the first player's enderchest.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class penchanting extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CRELengthException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p;

			if(args.length == 1) {
				p = Static.GetPlayer(args[0], t);
			} else {
				p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			}

			Static.AssertPlayerNonNull(p, t);
			p.openEnchanting(p.getLocation(), true);

			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "penchanting";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "void {[player]} Shows an enchanting to the current player, "
					+ " or a specified player. Note that power is defined by how many"
					+ " bookcases are near.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_pinv extends AbstractFunction {

		@Override
		public String getName() {
			return "set_pinv";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3};
		}

		@Override
		public String docs() {
			return "void {[player, [slot]], array} Sets a player's inventory to the specified inventory array."
					+ " An inventory array is one that matches what is returned by pinv(), so set_pinv(pinv()),"
					+ " while pointless, would be a correct call. If a slot is specified as the second argument,"
					+ " only that slot is set with the given item array. ---- An inventory array must be associative,"
					+ " however, it may skip items, in which case, only the specified values will be changed. If"
					+ " a key is out of range, or otherwise improper, a warning is emitted, and it is skipped,"
					+ " but the function will not fail as a whole. A simple way to set one item in a player's"
					+ " inventory would be: set_pinv(player(), 2, array(name: STONE, qty: 64)). This sets the player's"
					+ " second slot to be a stack of stone. set_pinv(array(103: array(type: 298))) gives them a hat."
					+ " To set the item in hand, use something like set_pinv(player(), null, array(type: 298))."
					+ " If you set a null key in an inventory array, only one of the items will be used (which one is"
					+ " undefined). Use an index of -106 to set the item in the player's off-hand.";

		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CRECastException.class, CREFormatException.class,
					CRELengthException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_2_0;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCPlayer m;
			Mixed arg;
			if(args.length == 3) {
				// Single item
				m = Static.GetPlayer(args[0], t);
				MCItemStack is = ObjectGenerator.GetGenerator().item(args[2], t);
				if(args[1] instanceof CNull) {
					m.setItemInHand(is);
				} else {
					setInvSlot(m.getInventory(), Static.getInt32(args[1], t), is);
				}
				return CVoid.VOID;
			} else if(args.length == 1) {
				m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
				Static.AssertPlayerNonNull(m, t);
				arg = args[0];
			} else {
				m = Static.GetPlayer(args[0], t);
				arg = args[1];
			}
			if(!(arg.isInstanceOf(CArray.TYPE))) {
				throw new CRECastException("Expecting an array as the last argument.", t);
			}
			CArray array = (CArray) arg;
			MCPlayerInventory inv = m.getInventory();
			for(String key : array.stringKeySet()) {
				if(key.isEmpty() || key.equals("null")) {
					//It was a null key
					MCItemStack is = ObjectGenerator.GetGenerator().item(array.get("", t), t);
					m.setItemInHand(is);
				} else {
					try {
						int index = Integer.parseInt(key);
						MCItemStack is = ObjectGenerator.GetGenerator().item(array.get(index, t), t);
						setInvSlot(inv, index, is);
					} catch (NumberFormatException e) {
						ConfigRuntimeException.DoWarning("Expecting integer value for key in array passed to"
								+ " set_pinv(), but \"" + key + "\" was found. Ignoring.");
					}
				}
			}
			return CVoid.VOID;
		}

		private void setInvSlot(MCPlayerInventory inv, Integer index, MCItemStack is) {
			if(index >= 0 && index <= 35) {
				inv.setItem(index, is);
			} else if(index == 100) {
				inv.setBoots(is);
			} else if(index == 101) {
				inv.setLeggings(is);
			} else if(index == 102) {
				inv.setChestplate(is);
			} else if(index == 103) {
				inv.setHelmet(is);
			} else if(index == -106) {
				inv.setItemInOffHand(is);
			} else {
				ConfigRuntimeException.DoWarning("Ignoring out of range slot (" + index + ") passed to set_pinv().");
			}
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class clear_pinv extends AbstractFunction {

		@Override
		public String getName() {
			return "clear_pinv";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "void {[player]} Clears a player's entire inventory (including armor).";

		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CRELengthException.class, CRENotFoundException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_2;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p;
			if(args.length == 0) {
				p = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
				Static.AssertPlayerNonNull(p, t);
			} else {
				p = Static.GetPlayer(args[0].val(), t);
			}
			MCPlayerInventory inv = p.getInventory();
			if(inv == null) {
				throw new CRENotFoundException(
						"Could not find the inventory of the given player (are you running in cmdline mode?)", t);
			}
			inv.clear();
			return CVoid.VOID;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class phas_item extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "phas_item";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "int {[player], itemArray} Returns the quantity of the specified item that the player is carrying"
					+ " (including armor slots). This counts across all slots in inventory. Recall that 0 is false, and"
					+ " anything else is true, so this can be used to get the total, or just see if they have the item."
					+ ITEM_MATCHING;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CREFormatException.class, CRERangeException.class,
					CRECastException.class, CRENotFoundException.class, CRELengthException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p;
			MCItemStack is;
			Mixed c;
			CArray ca = null;
			if(args.length == 1) {
				p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
				Static.AssertPlayerNonNull(p, t);
				c = args[0];
			} else {
				p = Static.GetPlayer(args[0], t);
				c = args[1];
			}

			if(c instanceof CNull) {
				return new CInt(0, t);
			}

			if(c.isInstanceOf(CArray.TYPE)) {
				ca = (CArray) c;
				is = ObjectGenerator.GetGenerator().item(ca, t);
			} else {
				is = Static.ParseItemNotation(null, c.val(), 1, t);
			}

			MCPlayerInventory inv = p.getInventory();
			if(inv == null) {
				throw new CRENotFoundException(
						"Could not find the inventory of the given player (are you running in cmdline mode?)", t);
			}

			int total = 0;
			for(int i = 0; i < 36; i++) {
				MCItemStack iis = inv.getItem(i);
				total += total(ca, is, iis, t);
			}
			total += total(ca, is, inv.getBoots(), t);
			total += total(ca, is, inv.getLeggings(), t);
			total += total(ca, is, inv.getChestplate(), t);
			total += total(ca, is, inv.getHelmet(), t);
			total += total(ca, is, inv.getItemInOffHand(), t);
			return new CInt(total, t);
		}

		private int total(CArray map, MCItemStack is, MCItemStack iis, Target t) {
			if(IsMatch(map, is, iis, t)) {
				return iis.getAmount();
			}
			return 0;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {
			if(children.size() > 0 && children.get(children.size() - 1).getData().isInstanceOf(CString.TYPE)) {
				MSLog.GetLogger().w(MSLog.Tags.DEPRECATION, "The string item format in " + getName() + " is deprecated.", t);
			}
			return null;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Demonstrates item name and meta matching.",
					"phas_item(array('name': 'DIAMOND_SWORD', 'meta': array('display': 'The Slasher')))",
					"Returns how many diamond swords with the name 'The Slasher' that are in the player's inventory."),
				new ExampleScript("Demonstrates plain item matching.",
					"phas_item(array('name': 'DIAMOND', 'meta': array('display': null, 'lore': null)))",
					"Returns the number of diamonds the player has, provided that they have no display name or lore."),
			};
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class pitem_slot extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "pitem_slot";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "array {[player], itemArray} Given an item array, returns the slot numbers"
					+ " that the matching item has at least one item in." + ITEM_MATCHING;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREFormatException.class, CRELengthException.class,
					CREPlayerOfflineException.class, CRENotFoundException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p;
			Mixed item;
			CArray ca = null;
			if(args.length == 1) {
				p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
				Static.AssertPlayerNonNull(p, t);
				item = args[0];
			} else {
				p = Static.GetPlayer(args[0], t);
				item = args[1];
			}
			MCItemStack is;
			if(item instanceof CNull) {
				ca = null;
				is = StaticLayer.GetItemStack("AIR", 1);
			} else if(item.isInstanceOf(CArray.TYPE)) {
				ca = (CArray) item;
				is = ObjectGenerator.GetGenerator().item(ca, t);
			} else {
				is = Static.ParseItemNotation(null, item.val(), 1, t);
			}

			MCPlayerInventory inv = p.getInventory();
			if(inv == null) {
				throw new CRENotFoundException(
						"Could not find the inventory of the given player (are you running in cmdline mode?)", t);
			}
			CArray ret = new CArray(t);
			for(int i = 0; i < 36; i++) {
				if(IsMatch(ca, is, inv.getItem(i), t)) {
					ret.push(new CInt(i, t), t);
				}
			}
			if(IsMatch(ca, is, inv.getBoots(), t)) {
				ret.push(new CInt(100, t), t);
			}
			if(IsMatch(ca, is, inv.getLeggings(), t)) {
				ret.push(new CInt(101, t), t);
			}
			if(IsMatch(ca, is, inv.getChestplate(), t)) {
				ret.push(new CInt(102, t), t);
			}
			if(IsMatch(ca, is, inv.getHelmet(), t)) {
				ret.push(new CInt(103, t), t);
			}
			if(IsMatch(ca, is, inv.getItemInOffHand(), t)) {
				ret.push(new CInt(-106, t), t);
			}
			return ret;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {
			if(children.size() > 0 && children.get(children.size() - 1).getData().isInstanceOf(CString.TYPE)) {
				MSLog.GetLogger().w(MSLog.Tags.DEPRECATION, "The string item format in " + getName() + " is deprecated.", t);
			}
			return null;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Demonstrates item name and meta matching.",
						"pitem_slot(array('name': 'DIAMOND_SWORD', 'meta': array('display': 'The Slasher')))",
						"Returns an array of slot numbers of the player's inventory that contain a diamond sword"
								+ " with the name 'The Slasher', regardless of the item's lore or enchantments."),
				new ExampleScript("Demonstrates item quantity matching.",
						"pitem_slot(player(), array('name': 'DIAMOND', 'qty': 8))",
						"Returns an array of slot numbers that have one ore more diamonds. Ignores 'qty'."),
				new ExampleScript("Demonstrates plain item matching.",
						"pitem_slot(array('name': 'DIAMOND', 'meta': array('display': null, 'lore': null)))",
						"Returns an array of slots that contain diamonds, if they have no lore or display name."),
			};
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class pgive_item extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "pgive_item";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3, 4};
		}

		@Override
		public String docs() {
			return "int {[player], itemArray} Gives a player the specified item. Unlike"
					+ " set_pinv(), this does not specify a slot. The qty is distributed in the player's inventory,"
					+ " first filling up slots that have the same item type, up to the max stack size, then fills up"
					+ " empty slots, until either the entire inventory is filled, or the entire amount has been given."
					+ " If the player's inv is full, the number of items that were not added is returned, which will be"
					+ " less than or equal to the quantity provided. Otherwise, returns 0. This function will not touch"
					+ " the player's armor slots however.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREFormatException.class, CREPlayerOfflineException.class,
					CRENotFoundException.class, CREIllegalArgumentException.class, CRELengthException.class,
					CREInsufficientArgumentsException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p;
			MCItemStack is;
			int itemOffset = 0;

			if(args.length == 2) {
				if(args[1] instanceof CNull) {
					return new CInt(0, t);
				}
				if(args[1].isInstanceOf(CArray.TYPE)) {
					itemOffset = 1;
				}
			} else if(args.length == 3) {
				if(args[0].isInstanceOf(CString.TYPE)) { // we assume player here, apparently
					itemOffset = 1;
				}
			} else if(args.length == 4) {
				itemOffset = 1;
			}

			if(itemOffset == 0) {
				p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
				Static.AssertPlayerNonNull(p, t);
			} else {
				p = Static.GetPlayer(args[0], t);
			}

			if(args[itemOffset].isInstanceOf(CArray.TYPE)) {
				is = ObjectGenerator.GetGenerator().item(args[itemOffset], t);
			} else if(args.length > 1) {
				is = Static.ParseItemNotation(null, args[itemOffset].val(), Static.getInt32(args[itemOffset + 1], t), t);
				if(args.length > itemOffset + 2) {
					is.setItemMeta(ObjectGenerator.GetGenerator().itemMeta(args[itemOffset + 2], is.getType(), t));
				}
			} else {
				throw new CREInsufficientArgumentsException("Expecting a qty for string item format.", t);
			}

			MCInventory inv = p.getInventory();
			if(inv != null) {
				Map<Integer, MCItemStack> h;
				try {
					h = p.getInventory().addItem(is);
				} catch (IllegalArgumentException e) {
					throw new CREIllegalArgumentException("Item value is invalid", t);
				}
				if(!h.isEmpty()) {
					return new CInt(h.get(0).getAmount(), t);
				}
			}
			return new CInt(0, t);
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {
			if(children.size() > 2 || children.size() == 2
					&& (children.get(1).getData().isInstanceOf(CString.TYPE) || children.get(1).getData().isInstanceOf(CInt.TYPE))) {
				MSLog.GetLogger().w(MSLog.Tags.DEPRECATION, "The string item format in " + getName() + " is deprecated.", t);
			}
			return null;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC);
		}

	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class ptake_item extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "ptake_item";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3};
		}

		@Override
		public String docs() {
			return "int {[player], itemArray} Works in reverse of pgive_item(), but returns the"
					+ " number of items actually taken, which will be from 0 to qty." + ITEM_MATCHING;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREPlayerOfflineException.class, CRERangeException.class,
					CREFormatException.class, CRENotFoundException.class, CRELengthException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p;
			MCItemStack is;
			int itemOffset = 0;
			CArray ca = null;

			if(args.length == 2) {
				if(args[1] instanceof CNull) {
					return new CInt(0, t);
				}
				if(args[1].isInstanceOf(CArray.TYPE)) {
					itemOffset = 1;
				}
			} else if(args.length == 3) {
				itemOffset = 1;
			}

			if(itemOffset == 0) {
				p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
				Static.AssertPlayerNonNull(p, t);
			} else {
				p = Static.GetPlayer(args[0], t);
			}

			if(args[itemOffset].isInstanceOf(CArray.TYPE)) {
				ca = (CArray) args[itemOffset];
				is = ObjectGenerator.GetGenerator().item(ca, t);
			} else {
				is = Static.ParseItemNotation(null, args[itemOffset].val(), Static.getInt32(args[itemOffset + 1], t), t);
			}

			int total = is.getAmount();
			int remaining = is.getAmount();
			MCPlayerInventory inv = p.getInventory();
			if(inv == null) {
				throw new CRENotFoundException(
						"Could not find the inventory of the given player (are you running in cmdline mode?)", t);
			}

			for(int i = 35; i >= 0; i--) {
				MCItemStack iis = inv.getItem(i);
				if(remaining <= 0) {
					break;
				}
				if(IsMatch(ca, is, iis, t)) {
					//Take the minimum of either: remaining, or iis.getAmount()
					int toTake = java.lang.Math.min(remaining, iis.getAmount());
					remaining -= toTake;
					int replace = iis.getAmount() - toTake;
					if(replace == 0) {
						inv.clear(i);
					} else {
						iis.setAmount(replace);
						inv.setItem(i, iis);
					}
				}
			}
			return new CInt(total - remaining, t);

		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {
			if(children.size() > 2 || children.size() == 2
					&& (children.get(1).getData().isInstanceOf(CString.TYPE) || children.get(1).getData().isInstanceOf(CInt.TYPE))) {
				MSLog.GetLogger().w(MSLog.Tags.DEPRECATION, "The string item format in " + getName() + " is deprecated.", t);
			}
			return null;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Demonstrates item name and meta matching.",
					"ptake_item(array('name': 'DIAMOND_SWORD', 'meta': array('display': 'The Slasher')))",
					"Removes one diamond sword with the name 'The Slasher' from the player's inventory."
							+ " This removes the item regardless of lore or enchantments."),
				new ExampleScript("Demonstrates item quantity matching.",
					"ptake_item(player(), array('name': 'DIAMOND', 'qty': 8))",
					"Removes up to eight diamonds from a player's inventory and returns how many."
							+ " This removes the item even if it has a different display name."),
				new ExampleScript("Demonstrates plain item matching.",
					"ptake_item(array('name': 'DIAMOND', 'meta': array('display': null, 'lore': null)))",
					"This will remove one diamond, provided that the diamond has no display name or lore."),
			};
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class pgive_enderchest_item extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "pgive_enderchest_item";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3, 4};
		}

		@Override
		public String docs() {
			return "int {[player], itemArray} Adds the specified item to a player's"
					+ " enderchest. Unlike set_penderchest(), this does not specify a slot. The items are distributed"
					+ " in the player's inventory, first filling up slots that have the same item type, up to the max"
					+ " stack size, then fills up empty slots, until either the entire inventory is filled or the"
					+ " entire amount has been given. If the player's enderchest is full, the number of items that were"
					+ " not added is returned, which will be less than or equal to the quantity provided. Otherwise,"
					+ " returns 0.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class, CRECastException.class, CRERangeException.class,
					CREPlayerOfflineException.class, CRENotFoundException.class, CREIllegalArgumentException.class,
					CRELengthException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p;
			MCItemStack is;
			int itemOffset = 0;

			if(args.length == 2) {
				if(args[1] instanceof CNull) {
					return new CInt(0, t);
				}
				if(args[1].isInstanceOf(CArray.TYPE)) {
					itemOffset = 1;
				}
			} else if(args.length == 3) {
				if(args[0].isInstanceOf(CString.TYPE)) { // we assume player here, apparently
					itemOffset = 1;
				}
			} else if(args.length == 4) {
				itemOffset = 1;
			}

			if(itemOffset == 0) {
				p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
				Static.AssertPlayerNonNull(p, t);
			} else {
				p = Static.GetPlayer(args[0], t);
			}

			if(args[itemOffset].isInstanceOf(CArray.TYPE)) {
				is = ObjectGenerator.GetGenerator().item(args[itemOffset], t);
			} else {
				is = Static.ParseItemNotation(null, args[itemOffset].val(), Static.getInt32(args[itemOffset + 1], t), t);
				if(args.length > itemOffset + 2) {
					is.setItemMeta(ObjectGenerator.GetGenerator().itemMeta(args[itemOffset + 2], is.getType(), t));
				}
			}

			MCInventory inv = p.getEnderChest();
			if(inv != null) {
				Map<Integer, MCItemStack> h;
				try {
					h = p.getEnderChest().addItem(is);
				} catch (IllegalArgumentException e) {
					throw new CREIllegalArgumentException("Item value is invalid", t);
				}
				if(!h.isEmpty()) {
					return new CInt(h.get(0).getAmount(), t);
				}
			}
			return new CInt(0, t);
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {
			if(children.size() > 2 || children.size() == 2
					&& (children.get(1).getData().isInstanceOf(CString.TYPE) || children.get(1).getData().isInstanceOf(CInt.TYPE))) {
				MSLog.GetLogger().w(MSLog.Tags.DEPRECATION, "The string item format in " + getName() + " is deprecated.", t);
			}
			return null;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class ptake_enderchest_item extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "ptake_enderchest_item";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3};
		}

		@Override
		public String docs() {
			return "int {[player], itemArray} Works in reverse of pgive_enderchest_item(), but"
					+ " returns the number of items actually taken, which will be from 0 to qty." + ITEM_MATCHING;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREPlayerOfflineException.class, CRELengthException.class,
					CREFormatException.class, CRENotFoundException.class, CRERangeException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p;
			MCItemStack is;
			int itemOffset = 0;
			CArray ca = null;

			if(args.length == 2) {
				if(args[1] instanceof CNull) {
					return new CInt(0, t);
				}
				if(args[1].isInstanceOf(CArray.TYPE)) {
					itemOffset = 1;
				}
			} else if(args.length == 3) {
				itemOffset = 1;
			}

			if(itemOffset == 0) {
				p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
				Static.AssertPlayerNonNull(p, t);
			} else {
				p = Static.GetPlayer(args[0], t);
			}

			if(args[itemOffset].isInstanceOf(CArray.TYPE)) {
				ca = (CArray) args[itemOffset];
				is = ObjectGenerator.GetGenerator().item(ca, t);
			} else {
				is = Static.ParseItemNotation(null, args[itemOffset].val(), Static.getInt32(args[itemOffset + 1], t), t);
			}

			int total = is.getAmount();
			int remaining = is.getAmount();
			MCInventory inv = p.getEnderChest();
			if(inv == null) {
				throw new CRENotFoundException(
						"Could not find the enderchest inventory of the given player (are you running in cmdline mode?)", t);
			}

			for(int i = 26; i >= 0; i--) {
				MCItemStack iis = inv.getItem(i);
				if(remaining <= 0) {
					break;
				}
				if(IsMatch(ca, is, iis, t)) {
					//Take the minimum of either: remaining, or iis.getAmount()
					int toTake = java.lang.Math.min(remaining, iis.getAmount());
					remaining -= toTake;
					int replace = iis.getAmount() - toTake;
					if(replace == 0) {
						inv.clear(i);
					} else {
						iis.setAmount(replace);
						inv.setItem(i, iis);
					}
				}
			}
			return new CInt(total - remaining, t);
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {
			if(children.size() > 2 || children.size() == 2
					&& (children.get(1).getData().isInstanceOf(CString.TYPE) || children.get(1).getData().isInstanceOf(CInt.TYPE))) {
				MSLog.GetLogger().w(MSLog.Tags.DEPRECATION, "The string item format in " + getName() + " is deprecated.", t);
			}
			return null;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Demonstrates item name and meta matching.",
					"ptake_enderchest_item(array('name': 'DIAMOND_SWORD', 'meta': array('display': 'The Slasher')))",
					"Removes one diamond sword with the name 'The Slasher' from the player's enderchest."
							+ " This removes the item regardless of lore or enchantments."),
				new ExampleScript("Demonstrates item quantity matching.",
					"ptake_enderchest_item(player(), array('name': 'DIAMOND', 'qty': 8))",
					"Removes up to eight diamonds from a player's enderchest and returns how many."
							+ " This removes the item even if it has a different display name."),
				new ExampleScript("Demonstrates plain item matching.",
					"ptake_enderchest_item(array('name': 'DIAMOND', 'meta': array('display': null, 'lore': null)))",
					"This will remove one diamond, provided that the diamond has no display name or lore."),
			};
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_penderchest extends AbstractFunction {

		@Override
		public String getName() {
			return "set_penderchest";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "void {[player], invArray} Sets a player's enderchest's inventory to the specified inventory object."
					+ " An inventory object is one that matches what is returned by penderchest(), so set_penderchest(penderchest()),"
					+ " while pointless, would be a correct call. ---- The array must be associative, "
					+ " however, it may skip items, in which case, only the specified values will be changed. If"
					+ " a key is out of range, or otherwise improper, a warning is emitted, and it is skipped,"
					+ " but the function will not fail as a whole. A simple way to set one item in a player's"
					+ " enderchest would be: set_penderchest(array(2: array(type: 1, qty: 64))) This sets the chest's second slot"
					+ " to be a stack of stone. set_penderchest(array(103: array(type: 298))) gives them a hat."
					+ " Note that this uses the unsafe"
					+ " enchantment mechanism to add enchantments, so any enchantment value will work.";

		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CRECastException.class, CREFormatException.class,
					CRELengthException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();

			MCPlayer m = null;

			if(p instanceof MCPlayer) {
				m = (MCPlayer) p;
			}

			Mixed arg;

			if(args.length == 2) {
				m = Static.GetPlayer(args[0], t);
				arg = args[1];
			} else {
				arg = args[0];
			}

			if(!(arg.isInstanceOf(CArray.TYPE))) {
				throw new CRECastException("Expecting an array as argument " + (args.length == 1 ? "1" : "2"), t);
			}

			CArray array = (CArray) arg;

			Static.AssertPlayerNonNull(m, t);

			for(String key : array.stringKeySet()) {
				try {
					int index = -2;

					try {
						index = Integer.parseInt(key);
					} catch (NumberFormatException e) {
						if(key.isEmpty()) {
							throw new CRERangeException("Slot index must be 0-26", t);
						} else {
							throw e;
						}
					}

					MCItemStack is = ObjectGenerator.GetGenerator().item(array.get(index, t), t);

					if(index >= 0 && index <= 26) {
						m.getEnderChest().setItem(index, is);
					} else {
						ConfigRuntimeException.DoWarning("Out of range value (" + index + ") found in array passed to set_penderchest(), so ignoring.");
					}
				} catch (NumberFormatException e) {
					ConfigRuntimeException.DoWarning("Expecting integer value for key in array passed to set_penderchest(), but \"" + key + "\" was found. Ignoring.");
				}
			}
			return CVoid.VOID;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class penderchest extends AbstractFunction {

		@Override
		public String getName() {
			return "penderchest";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1, 2};
		}

		@Override
		public String docs() {
			return "array {[player, [index]]} Gets the inventory for the specified player's enderchest, or the current"
					+ " player if none specified. If the index is specified, only the slot given will be returned."
					+ " The index of the array in the array is 0 - 26, which corresponds to the slot in the enderchest"
					+ " inventory. If there is no item at the slot specified, null is returned."
					+ " ---- If all slots are requested, an associative array of item objects is returned, and if"
					+ " only one item is requested, just that single item object is returned." + ITEM_OBJECT;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CRELengthException.class, CRECastException.class,
					CRERangeException.class, CRENotFoundException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();

			Integer index = -1;
			boolean all = false;
			MCPlayer m = null;

			if(args.length == 0) {
				all = true;

				if(p instanceof MCPlayer) {
					m = (MCPlayer) p;
				}
			} else if(args.length == 1) {
				all = true;

				m = Static.GetPlayer(args[0], t);
			} else if(args.length == 2) {
				if(args[1] instanceof CNull) {
					throw new CRERangeException("Slot index must be 0-26", t);
				} else {
					index = Static.getInt32(args[1], t);
				}

				all = false;
				m = Static.GetPlayer(args[0], t);
			}

			Static.AssertPlayerNonNull(m, t);

			if(all) {
				CArray ret = CArray.GetAssociativeArray(t);

				for(int i = 0; i < 27; i++) {
					ret.set(i, getInvSlot(m, i, t), t);
				}

				return ret;
			} else {
				return getInvSlot(m, index, t);
			}
		}

		private Mixed getInvSlot(MCPlayer m, Integer slot, Target t) {
			MCInventory inv = m.getEnderChest();
			if(inv == null) {
				throw new CRENotFoundException(
						"Could not find the enderchest inventory of the given player (are you running in cmdline mode?)", t);
			}
			if(slot < 0 || slot > 26) {
				throw new CRERangeException("Slot index must be 0-26", t);
			}
			MCItemStack is = inv.getItem(slot);
			return ObjectGenerator.GetGenerator().item(is, t);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_inventory_item extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class, CREBadEntityException.class, CREInvalidWorldException.class,
					CRECastException.class, CRERangeException.class, CREIllegalArgumentException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCWorld w = null;
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if(p != null) {
				w = p.getWorld();
			}

			MCInventory inv = GetInventory(args[0], w, t);
			int slot = Static.getInt32(args[1], t);
			try {
				MCItemStack is = inv.getItem(slot);
				return ObjectGenerator.GetGenerator().item(is, t);
			} catch (ArrayIndexOutOfBoundsException e) {
				throw new CRERangeException("Index out of bounds for the inventory type.", t);
			}
		}

		@Override
		public String getName() {
			return "get_inventory_item";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "array {specifier, slot} If a number is provided, it is assumed to be an entity, and if the entity supports"
					+ " inventories, it will be valid. Otherwise, if a location array is provided, it is assumed to be a block (chest, brewer, etc)"
					+ " and interpreted thusly. Depending on the inventory type, the max index will vary. If the index is too large, a RangeException is thrown,"
					+ " otherwise, the item at that location is returned as an item array, or null, if no item is there. You can determine the inventory type"
					+ " (and thus the max index count) with get_inventory_type(). An itemArray, like the one used by pinv/set_pinv is returned.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_inventory_item extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class, CREBadEntityException.class, CREInvalidWorldException.class,
					CRECastException.class, CRERangeException.class, CREIllegalArgumentException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCWorld w = null;
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if(p != null) {
				w = p.getWorld();
			}

			MCInventory inv = GetInventory(args[0], w, t);
			int slot = Static.getInt32(args[1], t);
			MCItemStack is = ObjectGenerator.GetGenerator().item(args[2], t);
			try {
				inv.setItem(slot, is);
				return CVoid.VOID;
			} catch (ArrayIndexOutOfBoundsException e) {
				throw new CRERangeException("Index out of bounds for the inventory type.", t);
			}
		}

		@Override
		public String getName() {
			return "set_inventory_item";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{3};
		}

		@Override
		public String docs() {
			return "void {specifier, index, itemArray} Sets the specified item in the specified inventory slot."
					+ " The specifier can be an entity UUID, block location array or virtual inventory id. ---- "
					+ ITEM_OBJECT;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_inventory_type extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREFormatException.class, CREBadEntityException.class,
					CREInvalidWorldException.class, CREIllegalArgumentException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCWorld w = null;
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if(p != null) {
				w = p.getWorld();
			}

			MCInventory inv = GetInventory(args[0], w, t);
			return new CString(inv.getType().name(), t);
		}

		@Override
		public String getName() {
			return "get_inventory_type";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "string {specifier} Returns the inventory type at the location specified, or of the entity specified."
					+ " If the entity or location specified is not capable of having an inventory, a FormatException is thrown."
					+ " ---- Note that not all valid inventory types may actually be returnable, due to lack of support"
					+ " in the server, but the valid return types are: " + StringUtils.Join(MCInventoryType.values(), ", ");
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_inventory_size extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class, CREBadEntityException.class, CREInvalidWorldException.class,
					CRECastException.class, CREIllegalArgumentException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCWorld w = null;
			if(environment.getEnv(CommandHelperEnvironment.class).GetPlayer() != null) {
				w = environment.getEnv(CommandHelperEnvironment.class).GetPlayer().getWorld();
			}
			MCInventory inventory = InventoryManagement.GetInventory(args[0], w, t);
			return new CInt(inventory.getSize(), t);
		}

		@Override
		public String getName() {
			return "get_inventory_size";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "int {specifier} Returns the max size of the inventory specified."
					+ " If the block or entity can't have an inventory, a FormatException is thrown.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_inventory_name extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREBadEntityException.class, CRECastException.class, CREFormatException.class,
					CREInvalidWorldException.class, CREIllegalArgumentException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCWorld w = null;
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if(p != null) {
				w = p.getWorld();
			}
			MCInventory inventory = InventoryManagement.GetInventory(args[0], w, t);
			try {
				return new CString(inventory.getTitle(), t);
			} catch (NullPointerException | ClassCastException ex) {
				throw new CREIllegalArgumentException("This inventory is not capable of being named.", t);
			}
		}

		@Override
		public String getName() {
			return "get_inventory_name";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "string {specifier} Returns the name of the inventory specified. If the block or entity"
					+ " can't have an inventory or a name, an IllegalArgumentException is thrown.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_2;
		}

	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class pinv_open extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CRELengthException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p1 = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			MCPlayer p2;
			if(args.length == 2) {
				p1 = Static.GetPlayer(args[0], t);
				p2 = Static.GetPlayer(args[1], t);
			} else {
				p2 = Static.GetPlayer(args[0], t);
			}
			Static.AssertPlayerNonNull(p1, t);
			p1.openInventory(p2.getInventory());
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "pinv_open";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "void {[playerToShow,] playerInventory} Opens a player's inventory, shown to the player specified's screen.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_inventory extends AbstractFunction {

		@Override
		public String getName() {
			return "get_inventory";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "array {specifier, [index]} Gets an array of the specified inventory."
					+ " If the block or entity can't have an inventory, a FormatException is thrown. If the index is specified,"
					+ " only the slot given will be returned. The max index of the array in the array is different for"
					+ " different types of inventories. If there is no item at the slot specified, null is returned."
					+ " ---- If all slots are requested, an associative array of item objects is returned, and if"
					+ " only one item is requested, just that single item object is returned." + ITEM_OBJECT;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CRERangeException.class, CREFormatException.class,
					CREBadEntityException.class, CREInvalidWorldException.class, CREIllegalArgumentException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {

			MCInventory inventory = InventoryManagement.GetInventory(args[0], null, t);

			Integer size = inventory.getSize();
			Integer index = -1;

			if(args.length == 2) {
				index = Static.getInt32(args[1], t);

				if(index < 0 || index >= size) {
					throw new CRERangeException("Slot index must be 0-" + (size - 1), t);
				}
			}

			if(index == -1) {
				CArray ret = CArray.GetAssociativeArray(t);
				for(int i = 0; i < size; i++) {
					ret.set(i, ObjectGenerator.GetGenerator().item(inventory.getItem(i), t), t);
				}

				return ret;
			} else {
				return ObjectGenerator.GetGenerator().item(inventory.getItem(index), t);
			}
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_inventory extends AbstractFunction {

		@Override
		public String getName() {
			return "set_inventory";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "void {specifier, invArray} Sets a block or entity inventory to the specified inventory object."
					+ " The specifier can be an entity UUID, location array, or virtual inventory ID."
					+ " If the block or entity can't have an inventory, a FormatException is thrown."
					+ " An inventory object invArray is one that matches what is returned by get_inventory(), so"
					+ " set_inventory(123, get_inventory(123)) while pointless, would be a correct call."
					+ " ---- The array must be associative, however, it may skip items, in which case, only the specified"
					+ " values will be changed. If a key is out of range, or otherwise improper, a warning is emitted,"
					+ " and it is skipped, but the function will not fail as a whole. A simple way to set one item would be:"
					+ " set_inventory(123, array(2: array(type: 1, qty: 64))) This sets the inventory second slot"
					+ " to be a stack of stone for entity with ID = 123. Note that this uses the unsafe"
					+ " enchantment mechanism to add enchantments, so any enchantment value will work.";

		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREFormatException.class, CREBadEntityException.class,
					CREInvalidWorldException.class, CRERangeException.class, CREIllegalArgumentException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {

			MCInventory inventory = InventoryManagement.GetInventory(args[0], null, t);
			Integer size = inventory.getSize();

			if(!(args[1].isInstanceOf(CArray.TYPE))) {
				throw new CRECastException("Expecting an array as argument 2", t);
			}

			CArray array = (CArray) args[1];

			for(String key : array.stringKeySet()) {
				try {
					int index = Integer.parseInt(key);
					if(index < 0 || index >= size) {
						ConfigRuntimeException.DoWarning("Out of range value (" + index + ") found in array passed to set_inventory(), so ignoring.");
					} else {
						MCItemStack is = ObjectGenerator.GetGenerator().item(array.get(index, t), t);
						inventory.setItem(index, is);
					}
				} catch (NumberFormatException e) {
					ConfigRuntimeException.DoWarning("Expecting integer value for key in array passed to set_inventory(), but \"" + key + "\" was found. Ignoring.");
				}
			}
			return CVoid.VOID;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class add_to_inventory extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "add_to_inventory";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3, 4};
		}

		@Override
		public String docs() {
			return "int {specifier, itemArray} Add to inventory the specified item."
					+ " The specifier must be a location array, entity UUID, or virtual inventory id."
					+ " The items are distributed in the inventory, first filling up slots that have the same item type,"
					+ " up to the max stack size, then fills up empty slots, until either the entire inventory is filled,"
					+ " or the entire amount has been given. If the inventory is full, number of items that were not"
					+ " added is returned, which will be less than or equal to the quantity provided. Otherwise, returns 0.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREFormatException.class, CREBadEntityException.class,
					CREInvalidWorldException.class, CREIllegalArgumentException.class, CRENotFoundException.class,
					CRERangeException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCInventory inventory = InventoryManagement.GetInventory(args[0], null, t);
			MCItemStack is;
			if(args.length == 2) {
				is = ObjectGenerator.GetGenerator().item(args[1], t);
			} else {
				is = Static.ParseItemNotation(this.getName(), args[1].val(), Static.getInt32(args[2], t), t);
				if(args.length == 4) {
					is.setItemMeta(ObjectGenerator.GetGenerator().itemMeta(args[3], is.getType(), t));
				}
			}

			Map<Integer, MCItemStack> h;
			try {
				h = inventory.addItem(is);
			} catch (IllegalArgumentException e) {
				throw new CREIllegalArgumentException("Item value is invalid", t);
			}

			if(h.isEmpty()) {
				return new CInt(0, t);
			} else {
				return new CInt(h.get(0).getAmount(), t);
			}
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {
			if(children.size() > 2) {
				MSLog.GetLogger().w(MSLog.Tags.DEPRECATION, "The string item format in " + getName() + " is deprecated.", t);
			}
			return null;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class take_from_inventory extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "take_from_inventory";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public String docs() {
			return "int {specifier, itemArray | specifier, itemID, qty} Works in reverse of add_to_inventory(), but"
					+ " returns the number of items actually taken, which will be from 0 to qty. Target must be a"
					+ " location array or entity UUID." + ITEM_MATCHING;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREFormatException.class, CREBadEntityException.class,
					CREInvalidWorldException.class, CRERangeException.class, CRENotFoundException.class,
					CREIllegalArgumentException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCInventory inventory = InventoryManagement.GetInventory(args[0], null, t);
			Integer size = inventory.getSize();
			CArray ca = null;

			MCItemStack is;
			if(args.length == 2) {
				ca = (CArray) args[1];
				is = ObjectGenerator.GetGenerator().item(ca, t);
			} else {
				is = Static.ParseItemNotation(this.getName(), args[1].val(), Static.getInt32(args[2], t), t);
			}

			int total = is.getAmount();
			int remaining = is.getAmount();
			for(int i = size - 1; i >= 0; i--) {
				MCItemStack iis = inventory.getItem(i);
				if(remaining <= 0) {
					break;
				}
				if(IsMatch(ca, is, iis, t)) {
					//Take the minimum of either: remaining, or iis.getAmount()
					int toTake = java.lang.Math.min(remaining, iis.getAmount());
					remaining -= toTake;
					int replace = iis.getAmount() - toTake;
					if(replace == 0) {
						inventory.clear(i);
					} else {
						iis.setAmount(replace);
						inventory.setItem(i, iis);
					}
				}
			}
			return new CInt(total - remaining, t);
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {
			if(children.size() == 3) {
				MSLog.GetLogger().w(MSLog.Tags.DEPRECATION, "The string item format in " + getName() + " is deprecated.", t);
			}
			return null;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Demonstrates item name and meta matching.",
					"take_from_inventory(puuid(), array('name': 'DIAMOND_SWORD', 'meta': array('display': 'Slasher')))",
					"Removes one diamond sword with the name 'Slasher' from the player's inventory."
							+ " This removes the item regardless of lore or enchantments."),
				new ExampleScript("Demonstrates item quantity matching.",
					"take_from_inventory(@location, array('name': 'DIAMOND', 'qty': 8))",
					"Removes up to eight diamonds from an inventory at a storage block location and returns how many."
							+ " This removes the item even if it has a different display name."),
				new ExampleScript("Demonstrates plain item matching.",
					"take_from_inventory(@uuid, array('name': 'DIAMOND', 'meta': array('display': null, 'lore': null)))",
					"This will remove 1 diamond from an entity, provided that the diamond has no display name or lore"),
			};
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_pheld_slot extends AbstractFunction {

		@Override
		public String getName() {
			return "set_pheld_slot";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "void {[player], slotNumber} Sets the selected quickbar slot of the given or executing player"
					+ " to the given slot. The slot number is in range of [0-8].";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRERangeException.class, CREPlayerOfflineException.class, CREFormatException.class,
					CRENotFoundException.class, CRELengthException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer player;
			switch(args.length) {
				case 1: {
					MCCommandSender sender = environment.getEnv(CommandHelperEnvironment.class).GetCommandSender();
					if(sender instanceof MCPlayer) {
						player = (MCPlayer) sender;
					} else {
						throw new CREPlayerOfflineException("The command sender is not online (are you running this from console?).", t);
					}
					break;
				}
				case 2: {
					player = Static.GetPlayer(args[0], t);
					break;
				}
				default: {
					throw new CREFormatException("Wrong number of arguments passed to " + this.getName(), t);
				}
			}

			int slot;
			try {
				slot = Integer.parseInt(args[args.length - 1].val());
			} catch (NumberFormatException e) {
				throw new CREFormatException("Slot number must be an integer in range of [0-8].", t);
			}
			if(slot < 0 || slot > 8) {
				throw new CRERangeException("Slot number must be an integer in range of [0-8].", t);
			}

			MCPlayerInventory pinv = player.getInventory();
			if(pinv == null) {
				throw new CRENotFoundException(
						"Could not find the inventory of the given player (are you running in cmdline mode?)", t);
			}
			pinv.setHeldItemSlot(slot);
			return CVoid.VOID;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class pheld_slot extends AbstractFunction {

		@Override
		public String getName() {
			return "pheld_slot";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "int {[player]} Returns the selected quickbar slot of the given or executing player."
					+ " The slot number is in range of [0-8].";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CRELengthException.class, CREFormatException.class,
					CRENotFoundException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer player;
			switch(args.length) {
				case 0: {
					MCCommandSender sender = environment.getEnv(CommandHelperEnvironment.class).GetCommandSender();
					if(sender instanceof MCPlayer) {
						player = (MCPlayer) sender;
					} else {
						throw new CREPlayerOfflineException("The command sender is not online (are you running this from console?).", t);
					}
					break;
				}
				case 1: {
					player = Static.GetPlayer(args[0], t);
					break;
				}
				default: {
					throw new CREFormatException("Wrong number of arguments passed to " + this.getName(), t);
				}
			}

			MCPlayerInventory pinv = player.getInventory();
			if(pinv == null) {
				throw new CRENotFoundException(
						"Could not find the inventory of the given player (are you running in cmdline mode?)", t);
			}
			int slot = pinv.getHeldItemSlot();
			return new CInt(slot, t);
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class popen_inventory extends AbstractFunction {

		@Override
		public String getName() {
			return "popen_inventory";
		}

		@Override
		public String docs() {
			return "void {[player], specifier} Opens an inventory for a player. The specifier must be an entity UUID,"
					+ " location array of a container block, or a virtual inventory id.";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p;
			MCInventory inv;
			if(args.length == 2) {
				p = Static.GetPlayer(args[0], t);
				inv = GetInventory(args[1], p.getWorld(), t);
			} else {
				p = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
				Static.AssertPlayerNonNull(p, t);
				inv = GetInventory(args[0], p.getWorld(), t);
			}
			p.openInventory(inv);
			return CVoid.VOID;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CREFormatException.class, CREBadEntityException.class,
					CREInvalidWorldException.class, CRECastException.class, CRELengthException.class,
					CREIllegalArgumentException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_2;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class pinventory_holder extends AbstractFunction {

		@Override
		public String getName() {
			return "pinventory_holder";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "mixed {[player]} Returns the block location, entity UUID, or virtual id of the inventory the player"
					+ " is currently viewing. If the player is viewing their own inventory or no inventory, the"
					+ " player's UUID is returned. When the inventory is virtual but has no id, it will return null."
					+ " The returned value can be used in other inventory functions unless it is null.";
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p;
			if(args.length == 1) {
				p = Static.GetPlayer(args[0], t);
			} else {
				p = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
				Static.AssertPlayerNonNull(p, t);
			}
			MCInventoryView view = p.getOpenInventory();
			if(view == null) {
				// probably tests
				return CNull.NULL;
			}
			return GetInventoryHolder(view.getTopInventory(), t);
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CREFormatException.class, CREBadEntityException.class,
					CREInvalidWorldException.class, CRECastException.class, CRELengthException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_2;
		}
	}

	@api
	public static class get_inventory_viewers extends AbstractFunction {

		@Override
		public String getName() {
			return "get_inventory_viewers";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "array {specifier} Gets all players currently viewing this inventory."
					+ " The specifier can be an entity UUID, block location array, or virtual inventory id.";
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCInventory inv = GetInventory(args[0], null, t);
			CArray list = new CArray(t);
			for(MCHumanEntity viewer : inv.getViewers()) {
				list.push(new CString(viewer.getName(), t), t);
			}
			return list;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class, CREBadEntityException.class, CREInvalidWorldException.class,
					CRECastException.class, CREIllegalArgumentException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_2;
		}
	}

	@api
	public static class get_virtual_inventories extends AbstractFunction {

		@Override
		public String getName() {
			return "get_virtual_inventories";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "array {} Returns an array of virtual inventory ids.";
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			CArray list = new CArray(t);
			for(String id : VIRTUAL_INVENTORIES.keySet()) {
				list.push(new CString(id, t), t);
			}
			return list;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_2;
		}
	}

	@api
	public static class create_virtual_inventory extends AbstractFunction {

		@Override
		public String getName() {
			return "create_virtual_inventory";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3, 4};
		}

		@Override
		public String docs() {
			List<String> virtual = new ArrayList<>();
			for(MCInventoryType type : MCInventoryType.values()) {
				if(type.canVirtualize()) {
					virtual.add(type.name());
				}
			}
			return "void {id, [type/size], [title], [inventory]} Creates a virtual inventory and holds it under the"
					+ " specified id. The string id should not be a UUID."
					+ " If the id is already in use, an IllegalArgumentException will be thrown."
					+ " You can use this id in other inventory functions to modify the contents, among other things."
					+ " If a size is specified instead of a type, it is rounded up to the nearest multiple of 9."
					+ " Size cannot be higher than 54."
					+ " A title for the top of the inventory may be given, but it will use the default for that"
					+ " that inventory type if null is specified."
					+ " An optional inventory array may be specified, otherwise the inventory will start empty."
					+ " Available inventory types: " + StringUtils.Join(virtual, ", ", " or ", ", or ");
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			String id = args[0].val();
			if(VIRTUAL_INVENTORIES.get(id) != null) {
				throw new CREIllegalArgumentException("An inventory using the id \"" + id + "\" already exists.", t);
			}

			MCInventoryType type = null;
			int size = 54;
			String title = null;
			if(args.length > 1) {
				if(args[1].isInstanceOf(CNumber.TYPE)) {
					size = Static.getInt32(args[1], t);
					if(size < 9) {
						size = 9; // minimum
					} else {
						size = (size + 8) / 9 * 9; // must be a multiple of 9
					}
					if(size > 54) {
						throw new CREIllegalArgumentException("A virtual inventory size cannot be higher than 54.", t);
					}
				} else {
					try {
						type = MCInventoryType.valueOf(args[1].val().toUpperCase());
					} catch (IllegalArgumentException iae) {
						throw new CREIllegalArgumentException("Invalid inventory type: " + args[1].val().toUpperCase(), t);
					}
					if(!type.canVirtualize()) {
						throw new CREIllegalArgumentException("Unable to create a virtual " + args[1].val().toUpperCase(), t);
					}
				}
				if(args.length > 2) {
					title = Construct.nval(args[2]);
				}
			}

			MCInventoryHolder holder = StaticLayer.GetConvertor().CreateInventoryHolder(id);
			MCInventory inv;
			if(type == null) {
				inv = Static.getServer().createInventory(holder, size, title);
			} else {
				inv = Static.getServer().createInventory(holder, type, title);
			}

			if(args.length == 4) {
				if(!(args[3].isInstanceOf(CArray.TYPE))) {
					throw new CRECastException("Inventory argument not an array in " + getName(), t);
				}
				CArray array = (CArray) args[3];
				for(String key : array.stringKeySet()) {
					try {
						int index = Integer.parseInt(key);
						if(index < 0 || index >= size) {
							ConfigRuntimeException.DoWarning("Out of range value (" + index + ") found in array passed to "
									+ getName() + "(), so ignoring.");
						} else {
							MCItemStack is = ObjectGenerator.GetGenerator().item(array.get(index, t), t);
							inv.setItem(index, is);
						}
					} catch (NumberFormatException e) {
						ConfigRuntimeException.DoWarning("Expecting integer value for key in array passed to "
								+ getName() + "(), but \"" + key + "\" was found. Ignoring.");
					}
				}
			}

			VIRTUAL_INVENTORIES.put(id, inv);
			return CVoid.VOID;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRERangeException.class, CRECastException.class, CREFormatException.class,
					CREIllegalArgumentException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_2;
		}
	}

	@api
	public static class delete_virtual_inventory extends AbstractFunction {

		@Override
		public String getName() {
			return "delete_virtual_inventory";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {id} Deletes a virtual inventory. The inventory will be closed for all viewers."
					+ " Returns whether or not an inventory with that id existed and was removed.";
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			String id = args[0].val();
			MCInventory inv = VIRTUAL_INVENTORIES.get(id);
			if(inv != null) {
				for(MCHumanEntity viewer : inv.getViewers()) {
					viewer.closeInventory();
				}
				VIRTUAL_INVENTORIES.remove(id);
				return CBoolean.TRUE;
			}
			return CBoolean.FALSE;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_2;
		}
	}

	public static final HashMap<String, MCInventory> VIRTUAL_INVENTORIES = new HashMap<>();

	/**
	 * Returns the inventory that this construct specifies.
	 * @param specifier The construct representing the inventory holder, whether entity UUID, location array or virtual id.
	 * @param t
	 * @return
	 */
	private static MCInventory GetInventory(Mixed specifier, MCWorld w, Target t) {
		MCInventory inv;
		if(specifier.isInstanceOf(CArray.TYPE)) {
			MCLocation l = ObjectGenerator.GetGenerator().location(specifier, w, t);
			inv = StaticLayer.GetConvertor().GetLocationInventory(l);
			if(inv == null) {
				throw new CREIllegalArgumentException("The location specified is not capable of having an inventory.", t);
			}
			return inv;
		}
		if(specifier.val().length() == 36 || specifier.val().length() == 32) {
			try {
				MCEntity entity = Static.getEntity(specifier, t);
				inv = StaticLayer.GetConvertor().GetEntityInventory(entity);
				if(inv == null) {
					throw new CREIllegalArgumentException("The entity specified is not capable of having an inventory.", t);
				}
				return inv;
			} catch (CREFormatException iae) {
				// not a UUID
			}
		}
		inv = VIRTUAL_INVENTORIES.get(specifier.val());
		if(inv == null) {
			throw new CREIllegalArgumentException("An inventory for \"" + specifier.val() + "\" does not exist.", t);
		}
		return inv;
	}

	/**
	 * Returns a construct representing an inventory's holder that can be used in inventory functions.
	 * This returns CNull if this inventory does not have a holder or if it's a virtual inventory from another plugin.
	 * @param inv
	 * @param t
	 * @return The construct representation of the inventory holder
	 */
	public static Mixed GetInventoryHolder(MCInventory inv, Target t) {
		MCInventoryHolder h = inv.getHolder();
		if(h instanceof MCEntity) {
			return new CString(((MCEntity) h).getUniqueId().toString(), t);
		} else if(h instanceof MCBlockState) {
			return ObjectGenerator.GetGenerator().location(((MCBlockState) h).getLocation(), false);
		} else if(h instanceof MCDoubleChest) {
			return ObjectGenerator.GetGenerator().location(((MCDoubleChest) h).getLocation(), false);
		} else if(h instanceof MCVirtualInventoryHolder) {
			return new CString(((MCVirtualInventoryHolder) h).getID(), t);
		}
		return CNull.NULL;
	}

	private static final String ITEM_MATCHING = " ---- The item array also serves as a map for what to compare."
			+ " If included in the array, the values for the keys \"display\", \"lore\" and "
			+ " \"enchants\" from the meta array, will be compared to the items in the inventory."
			+ " More keys may be added in the future.";

	/**
	 * Gets whether or not the two items are a match based on the given item array map.
	 *
	 * @param map The original item array map of what to compare
	 * @param is The MCItemStack to compare all other items with, converted from the item array map
	 * @param iis The current MCItemStack we're comparing
	 * @param t
	 * @return Whether or not the items are a match
	 */
	private static boolean IsMatch(CArray map, MCItemStack is, MCItemStack iis, Target t) {
		if(!is.getType().equals(iis.getType())) {
			return false;
		}
		if(map != null && map.containsKey("meta")) {
			Mixed c = map.get("meta", t);
			if(c instanceof CNull) {
				if(iis.hasItemMeta()) {
					return false;
				}
			} else {
				if(!iis.hasItemMeta()) {
					return false;
				}
				CArray metamap = (CArray) c;
				MCItemMeta im = is.getItemMeta();
				MCItemMeta iim = iis.getItemMeta();
				if(metamap.containsKey("display")) {
					if(im.hasDisplayName()) {
						if(!iim.hasDisplayName() || !im.getDisplayName().equals(iim.getDisplayName())) {
							return false;
						}
					} else if(iim.hasDisplayName()) {
						return false;
					}
				}
				if(metamap.containsKey("lore") && im.hasLore() && !im.getLore().equals(iim.getLore())) {
					return false;
				}
				if(metamap.containsKey("enchants") && !im.getEnchants().equals(iim.getEnchants())) {
					return false;
				}
			}
		}
		return true;
	}
}
