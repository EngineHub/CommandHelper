

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
    public BukkitMCEnchantment(Enchantment e){
        this.e = e;
    }
    
    public BukkitMCEnchantment(AbstractionObject a){
        this((Enchantment)null);
        if(a instanceof MCEnchantment){
            this.e = ((Enchantment)a.getHandle());
        } else {
            throw new ClassCastException();
        }
    }
    
    public Object getHandle(){
        return e;
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
