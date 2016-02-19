package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.MCEnchantment;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREEnchantmentException;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CREPlayerOfflineException;
import com.laytonsmith.core.exceptions.CRE.CRERangeException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 */
public class Enchantments {

	public static String docs() {
		return "Provides methods for dealing with enchanted items";
	}

	/**
	 * Converts the wiki version string to the bukkit version string. If the
	 * specified string isn't in the wiki, the string is returned unchanged.
	 *
	 * @param wikiVersion
	 * @return
	 */
	public static String ConvertName(String wikiVersion) {
		String lc = wikiVersion.toUpperCase().trim();
		if (lc.equals("PROTECTION")) {
			return "PROTECTION_ENVIRONMENTAL";
		} else if (lc.equals("FIRE PROTECTION")) {
			return "PROTECTION_FIRE";
		} else if (lc.equals("FEATHER FALLING")) {
			return "PROTECTION_FALL";
		} else if (lc.equals("BLAST PROTECTION")) {
			return "PROTECTION_EXPLOSIONS";
		} else if (lc.equals("PROJECTILE PROTECTION")) {
			return "PROTECTION_PROJECTILE";
		} else if (lc.equals("RESPIRATION")) {
			return "OXYGEN";
		} else if (lc.equals("AQUA AFFINITY")) {
			return "WATER_WORKER";
		} else if (lc.equals("SHARPNESS")) {
			return "DAMAGE_ALL";
		} else if (lc.equals("SMITE")) {
			return "DAMAGE_UNDEAD";
		} else if (lc.equals("BANE OF ARTHROPODS")) {
			return "DAMAGE_ARTHROPODS";
		} else if (lc.equals("KNOCKBACK")) {
			return "KNOCKBACK";
		} else if (lc.equals("FIRE ASPECT")) {
			return "FIRE_ASPECT";
		} else if (lc.equals("LOOTING")) {
			return "LOOT_BONUS_MOBS";
		} else if (lc.equals("EFFICIENCY")) {
			return "DIG_SPEED";
		} else if (lc.equals("SILK TOUCH")) {
			return "SILK_TOUCH";
		} else if (lc.equals("UNBREAKING")) {
			return "DURABILITY";
		} else if (lc.equals("FORTUNE")) {
			return "LOOT_BONUS_BLOCKS";
		} else if (lc.equals("POWER")){
			return "ARROW_DAMAGE";
		} else if(lc.equals("PUNCH")){
			return "ARROW_KNOCKBACK";
		} else if(lc.equals("FLAME")){
			return "ARROW_FIRE";
		} else if(lc.equals("INFINITY")){
			return "ARROW_INFINITE";
		} else {
			return wikiVersion;
		}
	}

	/**
	 * Converts the roman numeral into an integer (as a string). If the value
	 * passed in is already an integer, it is returned as is.
	 *
	 * @param romanNumeral
	 * @return
	 */
	public static String ConvertLevel(String romanNumeral) {
		String lc = romanNumeral.toLowerCase().trim();
		try {
			Integer.parseInt(lc);
			return lc;
		} catch (NumberFormatException e) {
			//Maybe roman numeral?
		}
		int i = romanToWestern(lc);
//        if(lc.equals("i")){
//            i = 1;
//        } else if(lc.equals("ii")){
//            i = 2;
//        } else if(lc.equals("iii")){
//            i = 3;
//        } else if(lc.equals("iv")){
//            i = 4;
//        } else if(lc.equals("v")){
//            i = 5;
//        } else {
//            return romanNumeral;
//        }
		return Integer.toString(i);
	}

	public static int romanToWestern(String roman) {

		int western = 0; //the numerical version
		char currentChar;
		char nextChar;

		int i = 0;

		while (i < roman.length()) {
			currentChar = roman.charAt(i);
			if (i < roman.length() - 1) {
				nextChar = roman.charAt(i + 1);
				if (getValue(currentChar) < getValue(nextChar)) {
					western += (getValue(nextChar) - getValue(currentChar));
					i += 2;
				} else {
					western += getValue(currentChar);
					i++;
				}
			} else {
				western += getValue(currentChar);
				i++;
			}
		}
		return western;

	}

	private static int getValue(char l) { //Converts the numeral to a number
		String letter = String.valueOf(l);
		if (letter.equalsIgnoreCase("I")) {
			return 1;
		}
		if (letter.equalsIgnoreCase("V")) {
			return 5;
		}
		if (letter.equalsIgnoreCase("X")) {
			return 10;
		}
		if (letter.equalsIgnoreCase("L")) {
			return 50;
		}
		if (letter.equalsIgnoreCase("C")) {
			return 100;
		}
		if (letter.equalsIgnoreCase("D")) {
			return 500;
		}
		if (letter.equalsIgnoreCase("M")) {
			return 1000;
		}
		return 0;
	}

	@api(environments={CommandHelperEnvironment.class})
	public static class enchant_inv extends AbstractFunction {

		@Override
		public String getName() {
			return "enchant_inv";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{3, 4};
		}

		@Override
		public String docs() {
			return "void {[player], slot, type, level} Adds an enchantment to an item in the player's inventory. Type can be a single string,"
					+ " or an array of enchantment names. If slot is null, the currently selected slot is used. If the enchantment cannot be applied"
					+ " to the specified item, an EnchantmentException is thrown, and if the level specified is not valid, a RangeException is thrown."
					+ " If type is an array, level must also be an array, with equal number of values in it, with each int corresponding to the appropriate"
					+ " type. You may use either the bukkit names for enchantments, or the name shown on the wiki: [http://www.minecraftwiki.net/wiki/Enchanting#Enchantment_Types],"
					+ " and level may be a roman numeral as well.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREEnchantmentException.class, CREPlayerOfflineException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCPlayer m = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			int offset = 1;
			if (args.length == 4) {
				m = Static.GetPlayer(args[0].val(), t);
				offset = 0;
			} else if (m == null) {
				throw ConfigRuntimeException.BuildException("Invalid sender!", CREPlayerOfflineException.class, t);
			}
			MCItemStack is = m.getItemAt(args[1 - offset] instanceof CNull ? null : Static.getInt32(args[1 - offset], t));
			if (is == null) {
				throw new CRECastException("There is no item at slot " + args[1 - offset], t);
			}
//            if (args[1 - offset] instanceof CNull) {
//                is = m.getItemInHand();
//            } else {
//                int slot = Static.getInt32(args[1 - offset]);
//                is = m.getInventory().getItem(slot);
//            }
			CArray enchantArray = new CArray(t);
			if (!(args[2 - offset] instanceof CArray)) {
				enchantArray.push(args[2 - offset], t);
			} else {
				enchantArray = (CArray) args[2 - offset];
			}

			CArray levelArray = new CArray(t);
			if (!(args[3 - offset] instanceof CArray)) {
				levelArray.push(args[3 - offset], t);
			} else {
				levelArray = (CArray) args[3 - offset];
			}
			for (String key : enchantArray.stringKeySet()) {
				MCEnchantment e = StaticLayer.GetEnchantmentByName(Enchantments.ConvertName(enchantArray.get(key, t).val()));
				if (e == null) {
					throw ConfigRuntimeException.BuildException(enchantArray.get(key, t).val().toUpperCase() + " is not a valid enchantment type", CREEnchantmentException.class, t);
				}
				if (e.canEnchantItem(is)) {
					int level = Static.getInt32(new CString(Enchantments.ConvertLevel(levelArray.get(key, t).val()), t), t);
					if (e.getMaxLevel() >= level && level > 0) {
						is.addEnchantment(e, level);
					} else {
						throw ConfigRuntimeException.BuildException("Level must be greater than 0, and less than " + e.getMaxLevel() + " but was " + level, CRERangeException.class, t);
					}
				} else {
					throw ConfigRuntimeException.BuildException(enchantArray.get(key, t).val().toUpperCase() + " cannot be applied to this item", CREEnchantmentException.class, t);
				}
			}
			return CVoid.VOID;
		}
	}

	@api(environments={CommandHelperEnvironment.class})
	public static class enchant_rm_inv extends AbstractFunction {

		@Override
		public String getName() {
			return "enchant_rm_inv";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public String docs() {
			return "void {[player], slot, type} Removes an enchantment from an item. type may be a valid enchantment, or an array of enchantment names. It"
					+ " can also be null, and all enchantments will be removed. If an enchantment is specified, and the item is not enchanted with that,"
					+ " it is simply ignored.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREEnchantmentException.class, CREPlayerOfflineException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCPlayer m = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			int offset = 1;
			if (args.length == 3) {
				m = Static.GetPlayer(args[0].val(), t);
				offset = 0;
			}
			Static.AssertPlayerNonNull(m, t);
			MCItemStack is = m.getItemAt(args[1 - offset] instanceof CNull?null:Static.getInt32(args[1 - offset], t));
			if (is == null) {
				throw new CRECastException("There is no item at slot " + args[1 - offset], t);
			}
//            if (args[1 - offset] instanceof CNull) {
//                is = m.getItemInHand();
//            } else {
//                int slot = Static.getInt32(args[1 - offset]);
//                is = m.getInventory().getItem(slot);
//            }

			CArray enchantArray = new CArray(t);
			if (!(args[2 - offset] instanceof CArray) && !(args[2 - offset] instanceof CNull)) {
				enchantArray.push(args[2 - offset], t);
			} else if (args[2 - offset] instanceof CNull) {
				for (MCEnchantment e : is.getEnchantments().keySet()) {
					is.removeEnchantment(e);
				}
			} else {
				enchantArray = (CArray) args[2 - offset];
			}
			for (String key : enchantArray.stringKeySet()) {
				MCEnchantment e = StaticLayer.GetEnchantmentByName(Enchantments.ConvertName(enchantArray.get(key, t).val()));
				if (e == null) {
					throw ConfigRuntimeException.BuildException(enchantArray.get(key, t).val().toUpperCase() + " is not a valid"
							+ " enchantment type", CREEnchantmentException.class, t);
				}
				is.removeEnchantment(e);
			}
			return CVoid.VOID;
		}
	}

	@api(environments={CommandHelperEnvironment.class})
	public static class get_enchant_inv extends AbstractFunction {

		@Override
		public String getName() {
			return "get_enchant_inv";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "array {[player], slot} Returns an array of arrays of the enchantments and their levels on the given"
					+ " item. For example: array(array(DAMAGE_ALL, DAMAGE_UNDEAD), array(1, 2))";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCPlayer m = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			Construct slot;
			if (args.length == 2) {
				m = Static.GetPlayer(args[0].val(), t);
				slot = args[1];
			} else {
				slot = args[0];
				if (m == null) {
					throw ConfigRuntimeException.BuildException("Invalid sender!", CREPlayerOfflineException.class, t);
				}
			}
			MCItemStack is = m.getItemAt(slot instanceof CNull ? null : Static.getInt32(slot, t));
			if (is == null) {
				throw new CRECastException("There is no item at slot " + slot, t);
			}
//            if(slot instanceof CNull){
//                is = m.getItemInHand();
//            } else {
//                int slotID = Static.getInt32(slot);
//                is = m.getInventory().getItem(slotID);
//            }
			CArray enchants = new CArray(t);
			CArray levels = new CArray(t);
			for (Map.Entry<MCEnchantment, Integer> entry : is.getEnchantments().entrySet()) {
				MCEnchantment e = entry.getKey();
				Integer l = entry.getValue();
				enchants.push(new CString(e.getName(), t), t);
				levels.push(new CInt(l, t), t);
			}

			return new CArray(t, enchants, levels);
		}
	}

	@api
	public static class can_enchant_target extends AbstractFunction {

		@Override
		public String getName() {
			return "can_enchant_target";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "boolean {name, targetItem} Given an enchantment name, and target item id,"
					+ " returns wether or not that item can be enchanted with that enchantment."
					+ " Throws an EnchantmentException if the name is not a valid enchantment"
					+ " type.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREEnchantmentException.class, CREFormatException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			try {
				String name = Enchantments.ConvertName(args[0].val());
				MCEnchantment e = StaticLayer.GetEnchantmentByName(name);
				MCItemStack is = Static.ParseItemNotation(this.getName(), args[1].val(), 1, t);
				return CBoolean.get(e.canEnchantItem(is));
			} catch (NullPointerException e) {
				throw ConfigRuntimeException.BuildException(args[0].val().toUpperCase() + " is not a known enchantment type.",
						CREEnchantmentException.class, t);
			}
		}
	}

	@api
	public static class get_enchant_max extends AbstractFunction {

		@Override
		public String getName() {
			return "get_enchant_max";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "int {name} Given an enchantment name, returns the max level it can be."
					+ " If name is not a valid enchantment, an EnchantException is thrown.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREEnchantmentException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			try {
				String name = Enchantments.ConvertName(args[0].val());
				MCEnchantment e = StaticLayer.GetEnchantmentByName(name);
				return new CInt(e.getMaxLevel(), t);
			} catch (NullPointerException e) {
				throw ConfigRuntimeException.BuildException(args[0].val().toUpperCase() + " is not a known enchantment type.",
						CREEnchantmentException.class, t);
			}
		}
	}

	@api
	public static class get_enchants extends AbstractFunction {

		private static Map<String, CArray> cache = new HashMap<String, CArray>();

		@Override
		public String getName() {
			return "get_enchants";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "array {item} Given an item id, returns the enchantments that can"
					+ " be validly added to this item. This may return an empty array.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCItemStack is = Static.ParseItemNotation(this.getName(), args[0].val(), 1, t);
			/**
			 * Because enchantment types won't change from run to run, we can
			 * cache here, and save time on duplicate lookups.
			 */
			if (cache.containsKey(args[0].val())) {
				return cache.get(args[0].val()).clone();
			}
			CArray ca = new CArray(t);
			for (MCEnchantment e : StaticLayer.GetEnchantmentValues()) {
				if (e.canEnchantItem(is)) {
					ca.push(new CString(e.getName(), t), t);
				}
			}
			cache.put(args[0].val(), ca);
			return ca.clone();
		}
	}

	@api
	public static class is_enchantment extends AbstractFunction {

		@Override
		public String getName() {
			return "is_enchantment";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {name} Returns true if this name is a valid enchantment type. Note"
					+ " that either the bukkit names or the wiki names are valid.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			try {
				MCEnchantment e = StaticLayer.GetEnchantmentByName(args[0].val());
				return CBoolean.TRUE;
			} catch (NullPointerException e) {
				return CBoolean.FALSE;
			}
		}
	}
	
	@api
	public static class enchantment_list extends AbstractFunction{

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return null;
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCEnchantment[] enchantments = StaticLayer.GetEnchantmentValues();
			CArray ret = new CArray(t);
			for(MCEnchantment e : enchantments){
				ret.push(new CString(e.getName(), t), t);
			}
			return ret;
		}

		@Override
		public String getName() {
			return "enchantment_list";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "array {} Returns an informational list of all valid enchantment names. Note that this will"
					+ " simply cover all enchantment types, but may not be a comprehensive list of names that"
					+ " can be accepted, there may be more, however, the list returned here is \"comprehensive\""
					+ " and \"official\". Additionally, this may vary from server type to server type.";
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}
}
