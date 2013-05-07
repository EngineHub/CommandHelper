

package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCEnchantment;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.annotations.WrappedItem;
import org.bukkit.enchantments.Enchantment;

/**
 *
 * @author layton
 */
public class BukkitMCEnchantment implements MCEnchantment{
    @WrappedItem Enchantment e;
    
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
	
	@Override
	public String toString() {
		return e.toString();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof BukkitMCEnchantment?e.equals(((BukkitMCEnchantment)obj).e):false);
	}

	@Override
	public int hashCode() {
		return e.hashCode();
	}
}
