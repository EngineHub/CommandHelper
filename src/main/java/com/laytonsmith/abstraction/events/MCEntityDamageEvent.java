package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCDamageCause;
import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCEntityType;
import com.laytonsmith.core.events.BindableEvent;

public interface MCEntityDamageEvent extends BindableEvent{

	public abstract MCDamageCause getCause();

	public abstract int getDamage();

	public abstract MCEntity getEntity();

	public abstract MCEntityType getEntityType();

	public abstract void setDamage(int damage);

}