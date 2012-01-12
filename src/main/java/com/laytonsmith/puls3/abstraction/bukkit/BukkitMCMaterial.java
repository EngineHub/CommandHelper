/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.puls3.abstraction.bukkit;

import com.laytonsmith.puls3.abstraction.blocks.MCMaterial;
import org.bukkit.Material;

/**
 *
 * @author layton
 */
class BukkitMCMaterial implements MCMaterial {
    
    Material m;

    public BukkitMCMaterial(Material type) {
        m = type;
    }

    public short getMaxDurability() {
        return m.getMaxDurability();
    }
    
}
