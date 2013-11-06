package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCAnimalTamer;
import com.laytonsmith.abstraction.entities.MCHumanEntity;
import com.laytonsmith.abstraction.MCOfflinePlayer;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCHumanEntity;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.HumanEntity;

/**
 *
 * @author layton
 */
public class BukkitMCAnimalTamer implements MCAnimalTamer {

	protected AnimalTamer animalTamer;

	public BukkitMCAnimalTamer(AnimalTamer tamer) {
		this.animalTamer = tamer;
	}
	
	public BukkitMCAnimalTamer(AbstractionObject ao) {
		this((AnimalTamer) ao.getHandle());
	}

	@Override
	public AnimalTamer getHandle() {
		return animalTamer;
	}

    public MCOfflinePlayer getOfflinePlayer() {
        if(animalTamer instanceof OfflinePlayer){
            return new BukkitMCOfflinePlayer((OfflinePlayer)animalTamer);
        }
        return null;
    }

    public boolean isOfflinePlayer() {
        return animalTamer instanceof OfflinePlayer;
    }

    public boolean isHumanEntity() {
        return animalTamer instanceof HumanEntity;
    }

    public MCHumanEntity getHumanEntity() {
        if(animalTamer instanceof HumanEntity){
            return new BukkitMCHumanEntity((HumanEntity)animalTamer);
        }
        return null;
    }
	
	@Override
	public String toString() {
		return animalTamer.toString();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof BukkitMCAnimalTamer?animalTamer.equals(((BukkitMCAnimalTamer)obj).animalTamer):false);
	}

	@Override
	public int hashCode() {
		return animalTamer.hashCode();
	}

	public String getName() {
		return animalTamer.getName();
	}
}