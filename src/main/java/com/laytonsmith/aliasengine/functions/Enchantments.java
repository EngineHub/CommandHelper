/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.abstraction.MCEnchantment;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.aliasengine.Constructs.CArray;
import com.laytonsmith.aliasengine.Constructs.CBoolean;
import com.laytonsmith.aliasengine.Constructs.CInt;
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
import java.util.HashMap;
import java.util.Map;

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
            MCPlayer m = environment.GetPlayer();
            int offset = 1;
            if (args.length == 4) {
                m = Static.GetPlayer(args[0].val(), line_num, f);
                offset = 0;
            }
            MCItemStack is = null;
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
                MCEnchantment e = StaticLayer.GetEnchantmentByName(Enchantments.ConvertName(enchantArray.get(key, line_num).val()).toUpperCase());
                if(e == null){
                    throw new ConfigRuntimeException(enchantArray.get(key, line_num).val().toUpperCase() + " is not a valid enchantment type", ExceptionType.EnchantmentException, line_num, f);
                }
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
            MCPlayer m = environment.GetPlayer();
            int offset = 1;
            if (args.length == 3) {
                m = Static.GetPlayer(args[0].val(), line_num, f);
                offset = 0;
            }
            MCItemStack is = null;
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
                for (MCEnchantment e : is.getEnchantments().keySet()) {
                    is.removeEnchantment(e);
                }
            } else {
                enchantArray = (CArray) args[2 - offset];
            }
            for (Construct key : enchantArray.keySet()) {
                MCEnchantment e = StaticLayer.GetEnchantmentByName(enchantArray.get(key, line_num).val().toUpperCase());
                is.removeEnchantment(e);
            }
            return new CVoid(line_num, f);
        }
    }
    
    @api public static class get_enchant_inv implements Function{

        public String getName() {
            return "get_enchant_inv";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public String docs() {
            return "array {[player], slot} Returns an array of arrays of the enchantments and their levels on the given"
                    + " item. For example: array(array(DAMAGE_ALL, DAMAGE_UNDEAD), array(1, 2))";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.CastException};
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
            MCPlayer m = environment.GetPlayer();
            Construct slot;
            if(args.length == 2){
                m = Static.GetPlayer(args[0].val(), line_num, f);
                slot = args[1];
            } else {
                slot = args[0];
            }            
            MCItemStack is;
            if(slot instanceof CNull){
                is = m.getItemInHand();
            } else {
                int slotID = (int) Static.getInt(slot);
                is = m.getInventory().getItem(slotID);
            }
            CArray enchants = new CArray(line_num, f);
            CArray levels = new CArray(line_num, f);
            for(Map.Entry<MCEnchantment, Integer> entry : is.getEnchantments().entrySet()){
                MCEnchantment e = entry.getKey();
                Integer l = entry.getValue();
                enchants.push(new CString(e.getName(), line_num, f));
                levels.push(new CInt(l, line_num, f));
            }
            
            return new CArray(line_num, f, enchants, levels);
        }
        
    }
    
    @api public static class can_enchant_target implements Function{

        public String getName() {
            return "can_enchant_target";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            return "boolean {name, targetItem} Given an enchantment name, and target item id,"
                    + " returns wether or not that item can be enchanted with that enchantment."
                    + " Throws an EnchantmentException if the name is not a valid enchantment"
                    + " type.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.EnchantmentException, ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return false;
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
            String name = Enchantments.ConvertName(args[0].val().toUpperCase());
            MCEnchantment e = StaticLayer.GetEnchantmentByName(name);
            MCItemStack is = Static.ParseItemNotation(this.getName(), args[1].val(), 1, line_num, f);
            return new CBoolean(e.canEnchantItem(is), line_num, f);
        }
        
    }
    
    @api public static class get_enchant_max implements Function{

        public String getName() {
            return "get_enchant_max";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "int {name} Given an enchantment name, returns the max level it can be."
                    + " If name is not a valid enchantment, an EnchantException is thrown.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.EnchantmentException};
        }

        public boolean isRestricted() {
            return false;
        }

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.3.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env environment, Construct... args) throws ConfigRuntimeException {
            String name = Enchantments.ConvertName(args[0].val().toUpperCase());
            MCEnchantment e = StaticLayer.GetEnchantmentByName(name);
            return new CInt(e.getMaxLevel(), line_num, f);
        }
        
    }
    
    @api public static class get_enchants implements Function{
        
        private static Map<String, CArray> cache = new HashMap<String, CArray>();

        public String getName() {
            return "get_enchants";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "array {item} Given an item id, returns the enchantments that can"
                    + " be validly added to this item. This may return an empty array.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return false;
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
            MCItemStack is = Static.ParseItemNotation(this.getName(), args[0].val(), 1, line_num, f);
            /**
             * Because enchantment types won't change from run to run, we can
             * cache here, and save time on duplicate lookups.
             */
            if(cache.containsKey(args[0].val())){
                try {
                    return cache.get(args[0].val()).clone();
                } catch (CloneNotSupportedException ex) {
                    throw new ConfigRuntimeException(ex.getMessage(), null, line_num, f, ex);
                }
            }
            CArray ca = new CArray(line_num, f);
            for(MCEnchantment e : StaticLayer.GetEnchantmentValues()){
                if(e.canEnchantItem(is)){
                    ca.push(new CString(e.getName(), line_num, f));
                }
            }
            cache.put(args[0].val(), ca);
            try {
                return ca.clone();
            } catch (CloneNotSupportedException ex) {
                throw new ConfigRuntimeException(ex.getMessage(), null, line_num, f, ex);
            }
        }
        
    }
    
    @api public static class is_enchantment implements Function{

        public String getName() {
            return "is_enchantment";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "boolean {name} Returns true if this name is a valid enchantment type. Note"
                    + " that either the bukkit names or the wiki names are valid.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{};
        }

        public boolean isRestricted() {
            return false;
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
            MCEnchantment e = StaticLayer.GetEnchantmentByName(args[0].val());
            return new CBoolean(e != null, line_num, f);
        }
        
    }
}
