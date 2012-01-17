/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCEntity;
import org.bukkit.entity.Entity;

/**
 *
 * @author layton
 */
class BukkitMCEntity implements MCEntity {

    Entity e;
    public BukkitMCEntity(Entity e) {
        this.e = e;
    }
    
    public int getEntityId(){
        return e.getEntityId();
    }
    
}
