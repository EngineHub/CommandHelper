/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.*;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Tameable;
import org.bukkit.event.entity.EntityDamageEvent;

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
        this.e = ((Entity)a.getHandle());
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
    
    public void fireEntityDamageEvent(MCDamageCause dc) {
        EntityDamageEvent ede = new EntityDamageEvent(e, EntityDamageEvent.DamageCause.valueOf(dc.name()), 9001);
        CommandHelperPlugin.self.getServer().getPluginManager().callEvent(ede);
    }
    
}
