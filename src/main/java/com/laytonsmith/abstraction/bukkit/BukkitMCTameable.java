/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Tameable;

/**
 *
 * @author layton
 */
public class BukkitMCTameable extends BukkitMCEntity implements MCTameable{

    Tameable t;
    public BukkitMCTameable(Entity t){
        super(t);
        this.t = (Tameable)t;
    }
    
    public BukkitMCTameable(AbstractionObject a){
        super((Entity)a.getHandle());
        this.t = ((Tameable)a.getHandle());
    }
    
    public Object getHandle(){
        return t;
    }
    public boolean isTamed() {
        return t.isTamed();
    }

    public void setTamed(boolean bln) {
        t.setTamed(bln);
    }

    public MCAnimalTamer getOwner() {
        if(t.getOwner() == null){
            return null;
        }
        return new BukkitMCAnimalTamer(t.getOwner());
    }

    public void setOwner(MCAnimalTamer at) {
        t.setOwner(((BukkitMCAnimalTamer)at).at);
    }

    @Override
    public boolean isTameable() {
        return true;
    }   
    
}
