/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit;

import org.bukkit.entity.LivingEntity;

import com.laytonsmith.abstraction.MCLivingEntity;

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
    public void damage(int i) {
        le.damage(i);
    }

    public int getHealth() {
        return le.getHealth();
    }

    public int getMaxHealth() {
        return le.getMaxHealth();
    }

    public void setHealth(int i) {
        le.setHealth(i);
    }
}
