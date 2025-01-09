/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.abstraction.MCAttributeModifier;
import com.laytonsmith.abstraction.MCItemMeta;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLeatherArmorMeta;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.entities.MCTropicalFish;
import com.laytonsmith.abstraction.enums.MCAttribute;
import com.laytonsmith.abstraction.enums.MCAxolotlType;
import com.laytonsmith.abstraction.enums.MCDyeColor;
import com.laytonsmith.abstraction.enums.MCEntityType;
import com.laytonsmith.abstraction.enums.MCEquipmentSlot;
import com.laytonsmith.abstraction.enums.MCEquipmentSlotGroup;
import com.laytonsmith.abstraction.enums.MCFireworkType;
import com.laytonsmith.abstraction.enums.MCItemFlag;
import com.laytonsmith.abstraction.enums.MCPatternShape;
import com.laytonsmith.abstraction.enums.MCPotionType;
import com.laytonsmith.abstraction.enums.MCTagType;
import com.laytonsmith.abstraction.enums.MCTrimMaterial;
import com.laytonsmith.abstraction.enums.MCTrimPattern;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.ArgumentValidation;
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

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ItemMeta {

	public static String docs() {
		return "These functions manipulate an item's meta data. The items are modified in a player's inventory.";
	}

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
			MCPlayer p;
			MCItemStack is;
			Mixed slot;
			if(args.length == 2) {
				p = Static.GetPlayer(args[0], t);
				slot = args[1];
			} else {
				p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
				Static.AssertPlayerNonNull(p, t);
				slot = args[0];
			}
			is = p.getItemAt(slot instanceof CNull ? null : ArgumentValidation.getInt32(slot, t));
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
			String docs = getBundledDocs();
			docs = docs.replace("%ITEM_FLAGS%", StringUtils.Join(MCItemFlag.values(), ", ", ", or ", " or "));
			docs = docs.replace("%POTION_TYPES%", StringUtils.Join(MCPotionType.values(), ", ", ", or ", " or "));
			docs = docs.replace("%DYE_COLORS%", StringUtils.Join(MCDyeColor.values(), ", ", ", or ", " or "));
			docs = docs.replace("%PATTERN_SHAPES%", StringUtils.Join(MCPatternShape.values(), ", ", ", or ", " or "));
			docs = docs.replace("%FISH_PATTERNS%", StringUtils.Join(MCTropicalFish.MCPattern.values(), ", ", ", or ", " or "));
			docs = docs.replace("%FIREWORK_TYPES%", StringUtils.Join(MCFireworkType.values(), ", ", ", or ", " or "));
			List<String> spawnable = new ArrayList<>();
			for(MCEntityType type : MCEntityType.values()) {
				if(type.isSpawnable()) {
					spawnable.add(type.name());
				}
			}
			docs = docs.replace("%ENTITY_TYPES%", StringUtils.Join(spawnable, ", ", ", or ", " or "));
			docs = docs.replace("%ATTRIBUTES%", StringUtils.Join(MCAttribute.values(), ", ", ", or ", " or "));
			docs = docs.replace("%OPERATIONS%", StringUtils.Join(MCAttributeModifier.Operation.values(), ", ", ", or ", " or "));
			docs = docs.replace("%SLOTS%", StringUtils.Join(MCEquipmentSlot.values(), ", ", ", or ", " or "));
			docs = docs.replace("%SLOTGROUPS%", StringUtils.Join(MCEquipmentSlotGroup.values(), ", ", ", or ", " or "));
			docs = docs.replace("%AXOLOTL_TYPES%", StringUtils.Join(MCAxolotlType.values(), ", ", ", or ", " or "));
			docs = docs.replace("%TRIM_PATTERNS%", StringUtils.Join(MCTrimPattern.values(), ", ", ", or ", " or "));
			docs = docs.replace("%TRIM_MATERIALS%", StringUtils.Join(MCTrimMaterial.values(), ", ", ", or ", " or "));
			docs = docs.replace("%TAG_TYPES%", StringUtils.Join(MCTagType.values(), ", ", ", or ", " or "));
			return docs;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Demonstrates generic meta for a tool in your main hand.",
					"msg(get_itemmeta(null))",
					"{display: AmazingSword, enchants: {}, lore: {Look at my sword, my sword is amazing}, repair: 0,"
							+ " model: null, flags: {}, modifiers: null, tags: null, damage: 0, unbreakable: false}"),
				new ExampleScript("Demonstrates a written book (excluding generic meta)",
					"msg(get_itemmeta(null))",
					"{author: PseudoKnight, pages: {Once upon a time..., The End.}, title: Short Story,"
							+ " generation: ORIGINAL}"),
				new ExampleScript("Demonstrates an EnchantedBook (excluding generic meta)",
					"msg(get_itemmeta(null))",
					"{stored: {flame: {elevel: 1, etype: ARROW_FIRE}}}"),
				new ExampleScript("Demonstrates a custom firework (excluding generic meta)",
					"msg(get_itemmeta(null))",
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
			MCPlayer p;
			Mixed slot;
			Mixed meta;
			MCItemStack is;
			if(args.length == 3) {
				p = Static.GetPlayer(args[0], t);
				slot = args[1];
				meta = args[2];
			} else {
				p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
				Static.AssertPlayerNonNull(p, t);
				slot = args[0];
				meta = args[1];
			}
			is = p.getItemAt(slot instanceof CNull ? null : ArgumentValidation.getInt32(slot, t));
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
			return "void {[player,] slot, ItemMetaArray} Applies the data from the given array to the item at the"
					+ " specified slot. Unused fields will be ignored. If null or an empty array is supplied, or if"
					+ " none of the given fields are applicable, the item will become default, as this function"
					+ " overwrites any existing data. See {{function|get_itemmeta}} for available fields."
					+ " If the item does not yet exist, use {{function|set_pinv}} instead.";
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
				"The item in your hands is now amazing, but has no other meta."),
				new ExampleScript("Demonstrates a written book",
				"set_itemmeta(null, array(author: 'Writer', pages: array('Once upon a time', 'The end.'), title: 'Epic Story'))",
				"This will write a very short story in a book"),
				new ExampleScript("Demonstrates an EnchantedBook",
				"set_itemmeta(null, array(stored: array(sharpness: 25, 'unbreaking': 3)))",
				"This enchanted book now contains Unbreaking 3 and Sharpness 25"),
				new ExampleScript("Demonstrates coloring leather armor",
				"set_itemmeta(102, array(color: array(r: 50, b: 150, g: 100)))",
				"This will make your leather chestplate blue-ish"),
				new ExampleScript("Demonstrates a player head", "set_itemmeta(103, array(owneruuid: puuid()))",
				"This puts your skin on the player head you are wearing"),
				new ExampleScript("Demonstrates making a custom potion",
				"set_itemmeta(5, array(display: Potion of Frog Leaping, potions: array(speed: array(strength: 4,"
						+ " seconds: 90, ambient: true, particles: false, icon: true))))",
				"Turns the potion in slot 5 into a Potion of Leaping V with no potion particles."),
				new ExampleScript("Demonstrates hiding a potion effect",
				"@meta = get_itemmeta(4);"
						+ "@meta['flags'] = array('HIDE_POTION_EFFECTS');"
						+ "set_itemmeta(4, @meta);",
				"Hides the text indicating meta information for the item in slot 4."
				+ " The flag HIDE_POTION_EFFECTS hides specific item meta like book meta, potion effects,"
				+ " a music disc's author and name, firework meta, map meta, and stored enchantments."),
				new ExampleScript("Demonstrates making a custom banner",
				"set_itemmeta(0, array(basecolor: 'SILVER', patterns: array(array(color: 'BLACK', shape: 'SKULL'))))",
				"This banner will now be silver with a black skull."),
				new ExampleScript("Demonstrates making a custom firework",
				"set_itemmeta(null, array('firework': array('strength': 1, 'effects': array(array("
				+ "'type': 'CREEPER', colors: array(array('r': 0, 'g': 255, 'b': 0)))))));",
				"This firework will now store a green creeper face effect.")
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
				slot = ArgumentValidation.getInt32(args[1], t);
			} else {
				slot = ArgumentValidation.getInt32(args[0], t);
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
				slot = ArgumentValidation.getInt32(args[1], t);
				if(args[2].isInstanceOf(CArray.TYPE)) {
					color = (CArray) args[2];
				} else {
					throw new CREFormatException("Expected an array but received " + args[2] + " instead.", t);
				}
			} else {
				slot = ArgumentValidation.getInt32(args[0], t);
				if(args[1].isInstanceOf(CArray.TYPE)) {
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
			MCPlayer p;
			Mixed slot;
			if(args.length == 2) {
				p = Static.GetPlayer(args[0], t);
				slot = args[1];
			} else {
				p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
				Static.AssertPlayerNonNull(p, t);
				slot = args[0];
			}
			MCItemStack is = p.getItemAt(slot instanceof CNull ? null : ArgumentValidation.getInt32(slot, t));
			if(is == null) {
				return CBoolean.FALSE;
			}
			return CBoolean.get(is.getItemMeta() instanceof MCLeatherArmorMeta);
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
