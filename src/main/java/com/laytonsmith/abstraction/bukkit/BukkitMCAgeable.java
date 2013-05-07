package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCAgeable;
import com.laytonsmith.annotations.WrappedItem;
import org.bukkit.entity.Ageable;

/**
 * 
 * @author jb_aero
 */
public class BukkitMCAgeable extends BukkitMCLivingEntity implements MCAgeable {

	@WrappedItem Ageable a;

	@Override
	public Ageable getHandle() {
		return a;
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
