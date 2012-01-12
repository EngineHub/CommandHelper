/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.puls3.abstraction.bukkit;

import com.laytonsmith.puls3.abstraction.MCEnchantment;
import com.laytonsmith.puls3.abstraction.MCItemStack;
import org.bukkit.enchantments.Enchantment;

/**
 *
 * @author layton
 */
public class BukkitMCEnchantment implements MCEnchantment{
    Enchantment e;
    public BukkitMCEnchantment(Enchantment e){
        this.e = e;
    }

    Enchantment __Enchantment() {
        return e;
    }

    public boolean canEnchantItem(MCItemStack is) {
        return e.canEnchantItem(((BukkitMCItemStack)is).is);
    }

    public int getMaxLevel() {
        return e.getMaxLevel();
    }

    public String getName() {
        return e.getName();
    }
}
