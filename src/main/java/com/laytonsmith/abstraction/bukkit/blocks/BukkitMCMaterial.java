

package com.laytonsmith.abstraction.bukkit.blocks;

import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.annotations.WrappedItem;
import org.bukkit.Material;

/**
 *
 * @author layton
 */
public class BukkitMCMaterial implements MCMaterial {
    @WrappedItem Material m;

    public BukkitMCMaterial(Material type) {
        this.m = type;
    }

    public short getMaxDurability() {
        return this.m.getMaxDurability();
    }

    public int getType() {
        return m.getId();
    }

    public int getMaxStackSize() {
        return m.getMaxStackSize();
    }
    
}
