/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.puls3.abstraction.bukkit.blocks;

import com.laytonsmith.puls3.abstraction.blocks.MCMaterial;
import org.bukkit.Material;

/**
 *
 * @author layton
 */
class BukkitMCMaterial implements MCMaterial {
    Material m;

    public BukkitMCMaterial(Material type) {
        this.m = type;
    }

    public short getMaxDurability() {
        return this.m.getMaxDurability();
    }
    
}
