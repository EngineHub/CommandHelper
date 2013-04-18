package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCAgeable;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

/**
 * 
 * @author jb_aero
 */
public class BukkitMCAgeable extends BukkitMCLivingEntity implements MCAgeable {

	Ageable a;
	public BukkitMCAgeable(Entity be) {
		super((LivingEntity) be);
		this.a = (Ageable) be;
	}
	
	public BukkitMCAgeable(AbstractionObject ao){
        super((LivingEntity)ao.getHandle());
        this.a = ((Ageable)ao.getHandle());
    }
	
	public boolean getCanBreed() {
		return a.canBreed();
	}
	
	public void setCanBreed(boolean breed) {
		a.setBreed(breed);
	}
	
	public int getAge() {
		return a.getAge();
	}
	
	public void setAge(int age) {
		a.setAge(age);
	}
	
	public boolean getAgeLock() {
		return a.getAgeLock();
	}
	
	public void setAgeLock(boolean lock) {
		a.setAgeLock(lock);
	}
	
	public boolean isAdult() {
		return a.isAdult();
	}
	
	public void setAdult() {
		a.setAdult();
	}
	
	public void setBaby() {
		a.setBaby();
	}

}
