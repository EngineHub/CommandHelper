

package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.*;
import com.laytonsmith.annotations.WrappedItem;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Tameable;

/**
 *
 * @author layton
 */
public class BukkitMCTameable extends BukkitMCAgeable implements MCTameable{

    @WrappedItem Tameable t;
    public BukkitMCTameable(Entity t){
        super((LivingEntity) t);
        this.t = (Tameable)t;
    }
    
    public BukkitMCTameable(AbstractionObject a){
        super((LivingEntity)a.getHandle());
        this.t = ((Tameable)a.getHandle());
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
}
