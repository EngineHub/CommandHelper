package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCHumanEntity;
import org.bukkit.entity.HumanEntity;

/**
 *
 * @author layton
 */
class BukkitMCHumanEntity implements MCHumanEntity {
    
    HumanEntity he;

    public BukkitMCHumanEntity(HumanEntity humanEntity) {
        he = humanEntity;
    }

    public String getName() {
        return he.getName();
    }
    
}
