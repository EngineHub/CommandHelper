/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

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
    public static String docs(){
        return "Provides methods for dealing with enchanted items";
    }
    
    @api public static class enchant_item implements Function{

        public String getName() {
            return "enchant_inv";
        }

        public Integer[] numArgs() {
            return new Integer[]{3, 4};
        }

        public String docs() {
            return "void {[player], slot, type, level} Adds an enchantment to an item in the player's inventory.";
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
            if(args.length == 4){
                m = Static.GetPlayer(args[0].val(), line_num, f);
                offset = 0;
            }
            int slot = (int) Static.getInt(args[1 - offset]);
            Enchantment e = Enchantment.getByName(args[2 - offset].val());
            int level = (int) Static.getInt(args[3 - offset]);
            
            ItemStack is = m.getInventory().getItem(slot);
            is.addEnchantment(e, level);            
            return new CVoid(line_num, f);
        }
        
    }
}
