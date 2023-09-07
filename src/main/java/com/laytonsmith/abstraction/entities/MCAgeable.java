package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCLivingEntity;

public interface MCAgeable extends MCLivingEntity {

	int getAge();

	void setAge(int age);

	boolean isAdult();

	void setAdult();

	boolean isBaby();

	void setBaby();
}
