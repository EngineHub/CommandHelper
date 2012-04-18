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
public class BukkitMCEntity implements MCEntity {

    Entity e;
    public BukkitMCEntity(Entity e) {
        this.e = e;
    }
    
    public BukkitMCEntity(AbstractionObject a){
        this((Entity)null);
        if(a instanceof MCEntity){
            this.e = ((Entity)a.getHandle());
        } else {
            throw new ClassCastException();
        }
    }
    
    public Object getHandle(){
        return e;
    }
    
    public int getEntityId(){
        return e.getEntityId();
    }

    public boolean isTameable() {
        return e instanceof Tameable;
    }

    public MCTameable getMCTameable() {
        return new BukkitMCTameable((Tameable)e);
    }
    
    public Entity _Entity(){
        return e;
    }

    public MCDamageCause getLastDamageCause() {
        return MCDamageCause.valueOf(e.getLastDamageCause().getCause().name());
    }

    public MCLivingEntity getLivingEntity() {
        if(e instanceof LivingEntity){
            return new BukkitMCLivingEntity((LivingEntity)e);
        }
        return null;
    }

    public boolean isLivingEntity() {
        return e instanceof LivingEntity;
    }
    
}
