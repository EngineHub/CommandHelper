

package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCAnimalTamer;
import com.laytonsmith.abstraction.MCHumanEntity;
import com.laytonsmith.abstraction.MCOfflinePlayer;
import com.laytonsmith.annotations.WrappedItem;
import com.laytonsmith.annotations.testing.AbstractConstructor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.HumanEntity;

/**
 *
 * @author layton
 */
public class BukkitMCAnimalTamer implements MCAnimalTamer{
    @WrappedItem AnimalTamer at;
    public BukkitMCAnimalTamer(AnimalTamer at){
        this.at = at;
    }
    
    @AbstractConstructor
	public BukkitMCAnimalTamer(AbstractionObject a){
        this.at = a.getHandle();
    }
    
    public AnimalTamer getHandle(){
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

	public String getName() {
		return at.getName();
	}
}
