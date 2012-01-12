/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.puls3.abstraction.bukkit;

import com.laytonsmith.puls3.abstraction.MCAnimalTamer;
import com.laytonsmith.puls3.abstraction.MCTameable;
import org.bukkit.entity.Entity;
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
    
    

    
}
