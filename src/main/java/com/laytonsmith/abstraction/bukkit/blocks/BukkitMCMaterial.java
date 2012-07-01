/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit.blocks;

import com.laytonsmith.abstraction.blocks.MCMaterial;
import org.bukkit.Material;

/**
 *
 * @author layton
 */
public class BukkitMCMaterial implements MCMaterial {
    Material m;

    public BukkitMCMaterial(Material type) {
        this.m = type;
    }

    public short getMaxDurability() {
        return this.m.getMaxDurability();
    }

    public int getMaxStackSize() {
        return m.getMaxStackSize();
    }

    public int getType() {
        return m.getId();
    }
    
}
