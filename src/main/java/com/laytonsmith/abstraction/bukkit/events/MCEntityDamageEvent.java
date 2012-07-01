package com.laytonsmith.abstraction.bukkit.events;

import com.laytonsmith.abstraction.MCDamageCause;
import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCEntityType;

public interface MCEntityDamageEvent {

	public abstract MCDamageCause getCause();

	public abstract int getDamage();

	public abstract void setDamage(int damage);

	public abstract MCEntity getEntity();

	public abstract MCEntityType getEntityType();

}