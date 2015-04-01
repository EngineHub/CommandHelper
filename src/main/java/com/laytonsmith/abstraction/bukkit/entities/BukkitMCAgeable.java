package com.laytonsmith.abstraction.bukkit.entities;

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
	
	@Override
	public boolean getCanBreed() {
		return a.canBreed();
	}
	
	@Override
	public void setCanBreed(boolean breed) {
		a.setBreed(breed);
	}
	
	@Override
	public int getAge() {
		return a.getAge();
	}
	
	@Override
	public void setAge(int age) {
		a.setAge(age);
	}
	
	@Override
	public boolean getAgeLock() {
		return a.getAgeLock();
	}
	
	@Override
	public void setAgeLock(boolean lock) {
		a.setAgeLock(lock);
	}
	
	@Override
	public boolean isAdult() {
		return a.isAdult();
	}
	
	@Override
	public void setAdult() {
		a.setAdult();
	}
	
	@Override
	public void setBaby() {
		a.setBaby();
	}

}
