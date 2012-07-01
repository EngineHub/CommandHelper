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
    public BukkitMCTameable(AbstractionObject a){
        super((Entity)a.getHandle());
        this.t = ((Tameable)a.getHandle());
    }
    
    public BukkitMCTameable(Entity t){
        super(t);
        this.t = (Tameable)t;
    }
    
    public Object getHandle(){
        return t;
    }
    public MCAnimalTamer getOwner() {
        if(t.getOwner() == null){
            return null;
        }
        return new BukkitMCAnimalTamer(t.getOwner());
    }

    @Override
    public MCEntityType getType() {
        //TODO: Once all the mob types get added, remove this, and make thi0s class abstract
        return MCEntityType.UNKNOWN;
    }

    @Override
    public boolean isTameable() {
        return true;
    }

    public boolean isTamed() {
        return t.isTamed();
    }

    public void setOwner(MCAnimalTamer at) {
        t.setOwner(((BukkitMCAnimalTamer)at).at);
    }

    public void setTamed(boolean bln) {
        t.setTamed(bln);
    }
    
}
