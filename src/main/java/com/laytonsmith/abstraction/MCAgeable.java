package com.laytonsmith.abstraction;

public interface MCAgeable extends MCLivingEntity {
	boolean getCanBreed();
	void setCanBreed(boolean breed);
	int getAge();
	void setAge(int age);
	boolean getAgeLock();
	void setAgeLock(boolean lock);
	boolean isAdult();
	void setAdult();
	void setBaby();
}
