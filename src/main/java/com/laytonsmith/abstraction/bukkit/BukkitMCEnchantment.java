/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCEnchantment;
import com.laytonsmith.abstraction.MCItemStack;
import org.bukkit.enchantments.Enchantment;

/**
 *
 * @author layton
 */
public class BukkitMCEnchantment implements MCEnchantment{
    Enchantment e;
    public BukkitMCEnchantment(AbstractionObject a){
        this((Enchantment)null);
        if(a instanceof MCEnchantment){
            this.e = ((Enchantment)a.getHandle());
        } else {
            throw new ClassCastException();
        }
    }
    
    public BukkitMCEnchantment(Enchantment e){
        this.e = e;
    }
    
    Enchantment __Enchantment() {
        return e;
    }

    public boolean canEnchantItem(MCItemStack is) {
        return e.canEnchantItem(((BukkitMCItemStack)is).is);
    }

    public Object getHandle(){
        return e;
    }

    public int getMaxLevel() {
        return e.getMaxLevel();
    }

    public String getName() {
        return e.getName();
    }
}
