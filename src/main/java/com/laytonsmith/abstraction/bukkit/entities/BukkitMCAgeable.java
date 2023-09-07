package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCAgeable;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;

public class BukkitMCAgeable extends BukkitMCLivingEntity implements MCAgeable {

	Ageable a;

	public BukkitMCAgeable(Entity be) {
		super(be);
		this.a = (Ageable) be;
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
	public boolean isAdult() {
		return a.isAdult();
	}

	@Override
	public void setAdult() {
		a.setAdult();
	}

	@Override
	public boolean isBaby() {
		return !a.isAdult();
	}

	@Override
	public void setBaby() {
		a.setBaby();
	}

}
