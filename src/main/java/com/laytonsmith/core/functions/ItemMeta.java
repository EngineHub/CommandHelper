/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.abstraction.MCItemMeta;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLeatherArmorMeta;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.enums.MCItemFlag;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CRENotFoundException;
import com.laytonsmith.core.exceptions.CRE.CREPlayerOfflineException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 *
 */
public class ItemMeta {

	public static String docs() {
		return "These functions manipulate an item's meta data. The items are modified in a player's inventory.";
	}

	private static final String APPLICABLE_ITEM_META = "<ul>"
			+ "<li>All items - \"display\" (string), \"lore\" (array of strings), \"enchants\" (An associative array of"
			+ " enchantments with the vanilla name of enchantment as the key, and value being an enchantment array with"
			+ " the key \"elevel\" for the level of the enchantment),"
			+ " \"flags\" (array). Possible flags: " + StringUtils.Join(MCItemFlag.values(), ", ", " or ") + "</li>"
			+ "<li>Damageable: \"damage\" on the item (0 is undamaged; each item type has its own max durability),"
			+ " \"unbreakable\" (boolean) and \"repair\" (int, repair cost)</li>"
			+ "<li>Books - \"title\" (string), author (string), \"pages\" (array of strings)</li>"
			+ "<li>EnchantedBooks - \"stored\" (associative array of enchantments (see Example))</li>"
			+ "<li>Leather Armor - \"color\" (color array (see Example))</li>"
			+ "<li>Player Skulls - \"owner\" (string)</li>"
			+ "<li>Potions - \"potions\" (array of custom potion effects (see get_peffect()),"
			+ " \"base\" (an array with the keys \"type\", \"extended\", and \"upgraded\")</li>"
			+ "<li>Banners - \"patterns\" (an array of pattern arrays, each with the keys"
			+ " \"shape\" and \"color\")</li>"
			+ "<li>Shields - \"basecolor\" (string), and \"patterns\" like in Banners."
			+ "<li>Fireworks - \"firework\" (array with strength (int), \"effects\" (array of effect arrays (see Example)))</li>"
			+ "<li>Firework Charges - \"effect\" (single Firework effect array)</li>"
			+ "<li>Storage Blocks - \"inventory\" (an array of item arrays)</li>"
			+ "<li>Mob Spawners - \"spawntype\" (an entity type)</li>"
			+ "<li>Furnace - \"burntime\" (int), \"cooktime\" (int), and in \"inventory\" these keys can exist if an"
			+ " item exists in that slot: \"result\", \"fuel\", and \"smelting\".</li>"
			+ "<li>Brewing Stand - \"brewtime\" (int), \"fuel\" (int), and in \"inventory\" these keys can exist if an"
			+ " item exists in that slot: \"fuel\", \"ingredient\", \"leftbottle\", \"middlebottle\", and \"rightbottle\".</li>"
			+ "<li>Tropical Fish Bucket - \"fishcolor\" (the base dye color of the fish), \"fishpatterncolor\" (the"
			+ " color of the pattern on the fish), and \"fishpattern\" (the pattern type on the fish).</li>"
			+ "</ul>";

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_itemmeta extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CRECastException.class};
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
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			MCItemStack is;
			Mixed slot;
			if(args.length == 2) {
				p = Static.GetPlayer(args[0], t);
				slot = args[1];
			} else {
				slot = args[0];
			}
			Static.AssertPlayerNonNull(p, t);
			if(slot instanceof CNull) {
				is = p.getItemAt(null);
			} else {
				is = p.getItemAt(Static.getInt32(slot, t));
			}
			if(is == null) {
				throw new CRECastException("There is no item at slot " + slot, t);
			}
			return ObjectGenerator.GetGenerator().itemMeta(is, t);
		}

		@Override
		public String getName() {
			return "get_itemmeta";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "array {[player,] inventorySlot} Returns an associative array of known ItemMeta for the slot given,"
					+ " or null if there isn't any. All items can have a display(name), lore, and/or enchants, "
					+ " and more info will be available for the items that have it. ---- Returned keys: "
					+ APPLICABLE_ITEM_META;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Demonstrates a generic item without meta", "msg(get_itemmeta(null))",
				"null"),
				new ExampleScript("Demonstrates a generic item with meta", "msg(get_itemmeta(null))",
				"{display: AmazingSword, enchants: {}, lore: {Look at my sword, my sword is amazing}}"),
				new ExampleScript("Demonstrates a written book", "msg(get_itemmeta(null))",
				"{author: Notch, display: null, enchants: {}, lore: null,"
				+ " pages: {This is page 1, This is page 2}, title: Example Book}"),
				new ExampleScript("Demonstrates an EnchantedBook", "msg(get_itemmeta(null))",
				"{display: null, enchants: {}, lore: null, stored: {flame: 1}}"),
				new ExampleScript("Demonstrates a piece of leather armor", "msg(get_itemmeta(null))",
				"{color: {b: 106, g: 160, r: 64}, display: null, enchants: {}, lore: null}"),
				new ExampleScript("Demonstrates a skull", "msg(get_itemmeta(null))",
				"{display: null, enchants: {}, lore: null, owner: Herobrine}"),
				new ExampleScript("Demonstrates a custom potion", "msg(get_itemmeta(null))",
				"{display: null, enchants: {}, lore: null, main: 8,"
				+ " potions: {{ambient: true, id: 8, seconds: 180, strength: 5}}}"),
				new ExampleScript("Demonstrates a custom banner", "msg(get_itemmeta(0))",
				"{basecolor: WHITE, patterns: {{color: BLACK, shape: SKULL}, {color: RED, shape: CROSS}}}"),
				new ExampleScript("Demonstrates a custom firework", "msg(get_itemmeta(null))",
				"{firework: {effects: {{colors: {{b: 240, g: 240, r: 240}, {b: 44, g: 49, r: 179},"
				+ " {b: 146, g: 49, r: 37}}, fade: {}, flicker: true, trail: false, type: STAR},"
				+ " {colors: {{b: 255, g: 255, r: 255}}, fade: {{b: 0, g: 0, r: 255}}, flicker: false,"
				+ " trail: true, type: BURST}}, strength: 2}}")
			};
		}

	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_itemmeta extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CREFormatException.class,
				CRECastException.class, CRENotFoundException.class};
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
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			Mixed slot;
			Mixed meta;
			MCItemStack is;
			if(args.length == 3) {
				p = Static.GetPlayer(args[0], t);
				slot = args[1];
				meta = args[2];
			} else {
				slot = args[0];
				meta = args[1];
			}
			Static.AssertPlayerNonNull(p, t);
			if(slot instanceof CNull) {
				is = p.getItemAt(null);
			} else {
				is = p.getItemAt(Static.getInt32(slot, t));
			}
			if(is == null) {
				throw new CRECastException("There is no item at slot " + slot, t);
			}
			is.setItemMeta(ObjectGenerator.GetGenerator().itemMeta(meta, is.getType(), t));
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "set_itemmeta";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public String docs() {
			return "void {[player,] inventorySlot, ItemMetaArray} Applies the data from the given array to the item at the"
					+ " specified slot. Unused fields will be ignored. If null or an empty array is supplied, or if none of"
					+ " the given fields are applicable, the item will become default, as this function overwrites any"
					+ " existing data. ---- Available fields: " + APPLICABLE_ITEM_META;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Demonstrates removing all meta", "set_itemmeta(null, null)",
				"This will make the item in your hand completely ordinary"),
				new ExampleScript("Demonstrates a generic item with meta",
				"set_itemmeta(null, array(display: 'Amazing Sword', lore: array('Look at my sword', 'my sword is amazing')))",
				"The item in your hands is now amazing"),
				new ExampleScript("Demonstrates a written book",
				"set_itemmeta(null, array(author: 'Writer', pages: array('Once upon a time', 'The end.'), title: 'Epic Story'))",
				"This will write a very short story"),
				new ExampleScript("Demonstrates an EnchantedBook",
				"set_itemmeta(null, array('stored': array('sharpness': 25, 'unbreaking': 3)))",
				"This book now contains Unbreaking 3 and Sharpness 25"),
				new ExampleScript("Demonstrates coloring leather armor",
				"set_itemmeta(102, array(color: array(r: 50, b: 150, g: 100)))",
				"This will make your chestplate blue-ish"),
				new ExampleScript("Demonstrates a skull", "set_itemmeta(103, array(owner: 'Notch'))",
				"This puts Notch's skin on the skull you are wearing"),
				new ExampleScript("Demonstrates making a custom potion",
				"set_itemmeta(5, array(potions: array(array(id: 8, strength: 4, seconds: 90, ambient: true))))",
				"Turns the potion in slot 5 into a Potion of Leaping V"),
				new ExampleScript("Demonstrates hiding a potion effect",
				"set_itemmeta(4, array(flags: array(HIDE_POTION_EFFECTS)))",
				"Hides the text indicating meta information for the item in slot 4."
				+ " The flag HIDE_POTION_EFFECTS hides specific item meta like book meta, potion effects,"
				+ " a music disc's author and name, firework meta, map meta, and stored enchantments."),
				new ExampleScript("Demonstrates making a custom banner",
				"set_itemmeta(0, array(basecolor: SILVER, patterns: array(array(color: BLACK, shape: SKULL))))",
				"This banner will be silver with a black skull."),
				new ExampleScript("Demonstrates making a custom firework",
				"set_itemmeta(null, array('firework': array('strength': 1, 'effects': array(array("
				+ "'type': 'CREEPER', colors: array(array('r': 0, 'g': 255, 'b': 0)))))));",
				"This firework will store a green creeper face effect.")
			};
		}

	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_armor_color extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREPlayerOfflineException.class};
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
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			int slot;
			if(args.length == 2) {
				p = Static.GetPlayer(args[0], t);
				slot = Static.getInt32(args[1], t);
			} else {
				slot = Static.getInt32(args[0], t);
			}
			Static.AssertPlayerNonNull(p, t);
			MCItemStack is = p.getItemAt(slot);
			if(is == null) {
				throw new CRECastException("There is no item at slot " + slot, t);
			}
			MCItemMeta im = is.getItemMeta();
			if(im instanceof MCLeatherArmorMeta) {
				return ObjectGenerator.GetGenerator().color(((MCLeatherArmorMeta) im).getColor(), t);
			} else {
				throw new CRECastException("The item at slot " + slot + " is not leather armor.", t);
			}
		}

		@Override
		public String getName() {
			return "get_armor_color";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "colorArray {[player], inventorySlot} Returns a color array for the color of the leather armor at"
					+ " the given slot. A CastException is thrown if this is not leather armor at that slot. The color"
					+ " array returned will look like: array(r: 0, g: 0, b: 0)";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_armor_color extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREPlayerOfflineException.class, CREFormatException.class};
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
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			int slot;
			CArray color;
			if(args.length == 3) {
				p = Static.GetPlayer(args[0], t);
				slot = Static.getInt32(args[1], t);
				if(args[2].isInstanceOf(CArray.class)) {
					color = (CArray) args[2];
				} else {
					throw new CREFormatException("Expected an array but received " + args[2] + " instead.", t);
				}
			} else {
				slot = Static.getInt32(args[0], t);
				if(args[1].isInstanceOf(CArray.class)) {
					color = (CArray) args[1];
				} else {
					throw new CREFormatException("Expected an array but received " + args[1] + " instead.", t);
				}
			}
			Static.AssertPlayerNonNull(p, t);
			MCItemStack is = p.getItemAt(slot);
			if(is == null) {
				throw new CRECastException("There is no item at slot " + slot, t);
			}
			MCItemMeta im = is.getItemMeta();
			if(im instanceof MCLeatherArmorMeta) {
				((MCLeatherArmorMeta) im).setColor(ObjectGenerator.GetGenerator().color(color, t));
				is.setItemMeta(im);
			} else {
				throw new CRECastException("The item at slot " + slot + " is not leather armor", t);
			}
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "set_armor_color";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public String docs() {
			return "void {[player], slot, colorArray} Sets the color of the leather armor at the given slot. colorArray"
					+ " should be an array that matches one of the formats: array(r: 0, g: 0, b: 0)"
					+ " array(red: 0, green: 0, blue: 0)"
					+ " array(0, 0, 0)";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class is_leather_armor extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREPlayerOfflineException.class};
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
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			int slot;
			if(args.length == 2) {
				p = Static.GetPlayer(args[0], t);
				slot = Static.getInt32(args[1], t);
			} else {
				slot = Static.getInt32(args[0], t);
			}
			Static.AssertPlayerNonNull(p, t);
			return CBoolean.get(p.getItemAt(slot).getItemMeta() instanceof MCLeatherArmorMeta);
		}

		@Override
		public String getName() {
			return "is_leather_armor";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "boolean {[player], itemSlot} Returns true if the item at the given slot is a piece of leather"
					+ " armor, that is, if dying it is allowed.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

	}
}
