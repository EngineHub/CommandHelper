/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCEntityType;
import com.laytonsmith.abstraction.MCLivingEntity;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

/**
 *
 * @author layton
 */
public class BukkitMCLivingEntity extends BukkitMCEntity implements MCLivingEntity{
    
    LivingEntity le;
    public BukkitMCLivingEntity(LivingEntity le){
        super(le);
        this.le = le;
    }
    public int getHealth() {
        return le.getHealth();
    }

    public void setHealth(int i) {
        le.setHealth(i);
    }

    public int getMaxHealth() {
        return le.getMaxHealth();
    }

    public void damage(int i) {
        le.damage(i);
    }
    
}
