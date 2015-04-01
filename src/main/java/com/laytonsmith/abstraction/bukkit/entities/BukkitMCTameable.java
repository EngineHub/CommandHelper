

package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCAnimalTamer;
import com.laytonsmith.abstraction.MCTameable;
import com.laytonsmith.abstraction.bukkit.BukkitMCAnimalTamer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Tameable;

/**
 *
 * 
 */
public class BukkitMCTameable extends BukkitMCAgeable implements MCTameable{

    Tameable t;
    public BukkitMCTameable(Entity t){
		super(t);
		this.t = (Tameable) t;
	}
    
    public BukkitMCTameable(AbstractionObject a){
        super((LivingEntity)a.getHandle());
        this.t = ((Tameable)a.getHandle());
    }

	@Override
    public boolean isTamed() {
        return t.isTamed();
    }

	@Override
    public void setTamed(boolean bln) {
        t.setTamed(bln);
    }

	@Override
    public MCAnimalTamer getOwner() {
        if(t.getOwner() == null){
            return null;
        }
        return new BukkitMCAnimalTamer(t.getOwner());
    }

	@Override
    public void setOwner(MCAnimalTamer at) {
        if (at == null) {
            t.setOwner(null);
        } else {
			t.setOwner(((BukkitMCAnimalTamer) at)._tamer());
		}
    }
}
