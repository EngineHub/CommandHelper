

package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCAnimalTamer;
import com.laytonsmith.abstraction.MCHumanEntity;
import com.laytonsmith.abstraction.MCOfflinePlayer;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCHumanEntity;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.HumanEntity;

/**
 *
 * 
 */
public class BukkitMCAnimalTamer implements MCAnimalTamer{
    AnimalTamer at;
    public BukkitMCAnimalTamer(AnimalTamer at){
        this.at = at;
    }
    
    public BukkitMCAnimalTamer(AbstractionObject a){
        this((AnimalTamer)null);
        if(a instanceof MCAnimalTamer){
            this.at = ((AnimalTamer)a.getHandle());
        } else {
            throw new ClassCastException();
        }
    }
    
	@Override
    public Object getHandle(){
        return at;
    }

	public AnimalTamer _tamer() {
		return at;
	}

    public MCOfflinePlayer getOfflinePlayer() {
        if(at instanceof OfflinePlayer){
            return new BukkitMCOfflinePlayer((OfflinePlayer)at);
        }
        return null;
    }

    public boolean isOfflinePlayer() {
        return at instanceof OfflinePlayer;
    }

    public boolean isHumanEntity() {
        return at instanceof HumanEntity;
    }

    public MCHumanEntity getHumanEntity() {
        if(at instanceof HumanEntity){
            return new BukkitMCHumanEntity((HumanEntity)at);
        }
        return null;
    }
	
	@Override
	public String toString() {
		return at.toString();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof BukkitMCAnimalTamer?at.equals(((BukkitMCAnimalTamer)obj).at):false);
	}

	@Override
	public int hashCode() {
		return at.hashCode();
	}

	@Override
	public String getName() {
		return at.getName();
	}
}
