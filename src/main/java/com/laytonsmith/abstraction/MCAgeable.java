package com.laytonsmith.abstraction;

/**
 * 
 * @author jb_aero
 */
public interface MCAgeable extends MCLivingEntity {

	public boolean getCanBreed();
	public void setCanBreed(boolean breed);
	public int getAge();
	public void setAge(int age);
	public boolean getAgeLock();
	public void setAgeLock(boolean lock);
	public boolean isAdult();
	public void setAdult();
	public void setBaby();
	
}
