/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.Constructs.CArray;
import com.laytonsmith.aliasengine.Constructs.CNull;
import com.laytonsmith.aliasengine.Constructs.CString;
import com.laytonsmith.aliasengine.Constructs.CVoid;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.Env;
import com.laytonsmith.aliasengine.Static;
import com.laytonsmith.aliasengine.api;
import com.laytonsmith.aliasengine.exceptions.ConfigRuntimeException;
import com.laytonsmith.aliasengine.functions.Exceptions.ExceptionType;
import java.io.File;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Layton
 */
public class Enchantments {

    public static String docs() {
        return "Provides methods for dealing with enchanted items";
    }
    
    /**
     * Converts the wiki version string to the bukkit version string. If the specified string isn't
     * in the wiki, the string is returned unchanged.
     * @param wikiVersion
     * @return 
     */
    public static String ConvertName(String wikiVersion){
        String lc = wikiVersion.toLowerCase().trim();
        if(lc.equals("protection")){
            return "PROTECTION_ENVIRONMENTAL";
        } else if(lc.equals("fire protection")){
            return "PROTECTION_FIRE";
        } else if(lc.equals("feather falling")){
            return "PROTECTION_FALL";
        } else if(lc.equals("blast protection")){
            return "PROTECTION_EXPLOSIONS";
        } else if(lc.equals("projectile protection")){
            return "PROTECTION_PROJECTILE";
        } else if(lc.equals("respiration")){
            return "OXYGEN";
        } else if(lc.equals("aqua affinity")){
            return "WATER_WORKER";
        } else if(lc.equals("sharpness")){
            return "DAMAGE_ALL";
        } else if(lc.equals("smite")){
            return "DAMAGE_UNDEAD";
        } else if(lc.equals("bane of arthropods")){
            return "DAMAGE_ARTHROPODS";
        } else if(lc.equals("knockback")){
            return "KNOCKBACK";
        } else if(lc.equals("fire aspect")){
            return "FIRE_ASPECT";
        } else if(lc.equals("looting")){
            return "LOOT_BONUS_MOBS";
        } else if(lc.equals("efficiency")){
            return "DIG_SPEED";
        } else if(lc.equals("silk touch")){
            return "SILK_TOUCH";
        } else if(lc.equals("unbreaking")){
            return "DURABILITY";
        } else if(lc.equals("fortune")){
            return "LOOT_BONUS_BLOCKS";
        } else {
            return wikiVersion;
        }
    }
    
    /**
     * Converts the roman numeral into an integer (as a string). If the value
     * passed in is already an integer, it is returned as is.
     * @param romanNumeral
     * @return 
     */
    public static String ConvertLevel(String romanNumeral){
        String lc = romanNumeral.toLowerCase().trim();
        int i = 0;
        if(lc.equals("i")){
            i = 1;
        } else if(lc.equals("ii")){
            i = 2;
        } else if(lc.equals("iii")){
            i = 3;
        } else if(lc.equals("iv")){
            i = 4;
        } else if(lc.equals("v")){
            i = 5;
        } else {
            return romanNumeral;
        }
        return Integer.toString(i);
    }

    @api
    public static class enchant_inv implements Function {

        public String getName() {
            return "enchant_inv";
        }

        public Integer[] numArgs() {
            return new Integer[]{3, 4};
        }

        public String docs() {
            return "void {[player], slot, type, level} Adds an enchantment to an item in the player's inventory. Type can be a single string,"
                    + " or an array of enchantment names. If slot is null, the currently selected slot is used. If the enchantment cannot be applied"
                    + " to the specified item, an EnchantmentException is thrown, and if the level specified is not valid, a RangeException is thrown."
                    + " If type is an array, level must also be an array, with equal number of values in it, with each int corresponding to the appropriate"
                    + " type. You may use either the bukkit names for enchantments, or the name shown on the wiki: [http://www.minecraftwiki.net/wiki/Enchanting#Enchantment_Types],"
                    + " and level may be a roman numeral as well.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.EnchantmentException, ExceptionType.PlayerOfflineException};
        }

        public boolean isRestricted() {
            return true;
        }

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.3.0";
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(int line_num, File f, Env environment, Construct... args) throws ConfigRuntimeException {
            Player m = environment.GetPlayer();
            int offset = 1;
            if (args.length == 4) {
                m = Static.GetPlayer(args[0].val(), line_num, f);
                offset = 0;
            }
            ItemStack is = null;
            if (args[1 - offset] instanceof CNull) {
                is = m.getItemInHand();
            } else {
                int slot = (int) Static.getInt(args[1 - offset]);
                is = m.getInventory().getItem(slot);
            }
            CArray enchantArray = new CArray(line_num, f);
            if (!(args[2 - offset] instanceof CArray)) {
                enchantArray.push(args[2 - offset]);
            } else {
                enchantArray = (CArray) args[2 - offset];
            }

            CArray levelArray = new CArray(line_num, f);
            if (!(args[3 - offset] instanceof CArray)) {
                levelArray.push(args[3 - offset]);
            } else {
                levelArray = (CArray) args[3 - offset];
            }
            for (Construct key : enchantArray.keySet()) {
                Enchantment e = Enchantment.getByName(Enchantments.ConvertName(enchantArray.get(key, line_num).val()).toUpperCase());
                if (e.canEnchantItem(is)) {
                    int level = (int) Static.getInt(new CString(Enchantments.ConvertLevel(levelArray.get(key, line_num).val()), line_num, f));
                    if (e.getMaxLevel() >= level && level > 0) {
                        is.addEnchantment(e, level);
                    } else {
                        throw new ConfigRuntimeException(level + " is too high for the " + e.getName() + " enchantment. The range is 1-" + e.getMaxLevel(), ExceptionType.RangeException, line_num, f);
                    }
                } else {
                    throw new ConfigRuntimeException(enchantArray.get(key, line_num).val().toUpperCase() + " cannot be applied to this item", ExceptionType.EnchantmentException, line_num, f);
                }
            }
            return new CVoid(line_num, f);
        }
    }

    @api
    public static class enchant_rm_inv implements Function {

        public String getName() {
            return "enchant_rm_inv";
        }

        public Integer[] numArgs() {
            return new Integer[]{2, 3};
        }

        public String docs() {
            return "void {[player], slot, type} Removes an enchantment from an item. type may be a valid enchantment, or an array of enchantment names. It"
                    + " can also be null, and all enchantments will be removed. If an enchantment is specified, and the item is not enchanted with that,"
                    + " it is simply ignored.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.EnchantmentException, ExceptionType.PlayerOfflineException};
        }

        public boolean isRestricted() {
            return true;
        }

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.3.0";
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(int line_num, File f, Env environment, Construct... args) throws ConfigRuntimeException {
            Player m = environment.GetPlayer();
            int offset = 1;
            if (args.length == 3) {
                m = Static.GetPlayer(args[0].val(), line_num, f);
                offset = 0;
            }
            ItemStack is = null;
            if (args[1 - offset] instanceof CNull) {
                is = m.getItemInHand();
            } else {
                int slot = (int) Static.getInt(args[1 - offset]);
                is = m.getInventory().getItem(slot);
            }

            CArray enchantArray = new CArray(line_num, f);
            if (!(args[2 - offset] instanceof CArray) && !(args[2 - offset] instanceof CNull)) {
                enchantArray.push(args[2 - offset]);
            } else if (args[2 - offset] instanceof CNull) {
                for (Enchantment e : is.getEnchantments().keySet()) {
                    is.removeEnchantment(e);
                }
            } else {
                enchantArray = (CArray) args[2 - offset];
            }
            for (Construct key : enchantArray.keySet()) {
                Enchantment e = Enchantment.getByName(enchantArray.get(key, line_num).val().toUpperCase());
                is.removeEnchantment(e);
            }
            return new CVoid(line_num, f);
        }
    }
}
