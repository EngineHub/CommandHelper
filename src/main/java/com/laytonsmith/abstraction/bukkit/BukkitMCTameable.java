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
public class BukkitMCTameable implements MCTameable{

    Tameable t;
    public BukkitMCTameable(Tameable t){
        this.t = t;
    }
    
    public BukkitMCTameable(AbstractionObject a){
        this((Tameable)null);
        if(a instanceof MCTameable){
            this.t = ((Tameable)a.getHandle());
        } else {
            throw new ClassCastException();
        }
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

    public int getEntityId() {
        if(t instanceof Entity){
            return ((Entity)t).getEntityId();
        }
        return 0;
    }

    public boolean isTameable() {
        return true;
    }

    public MCTameable getMCTameable() {
        return new BukkitMCTameable(t);
    }

    public MCDamageCause getLastDamageCause() {
        return MCDamageCause.valueOf(((Entity)t).getLastDamageCause().getCause().name());
    }

    public MCLivingEntity getLivingEntity() {
        if(t instanceof LivingEntity){
            return new BukkitMCLivingEntity((LivingEntity)t);
        }
        return null;
    }

    public boolean isLivingEntity() {
        return t instanceof LivingEntity;
    }
    
    

    
}
