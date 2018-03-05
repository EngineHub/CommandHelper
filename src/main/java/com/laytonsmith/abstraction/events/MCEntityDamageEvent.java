package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.enums.MCDamageCause;
import com.laytonsmith.core.events.BindableEvent;

public interface MCEntityDamageEvent extends BindableEvent {

	MCDamageCause getCause();

	MCEntity getEntity();

	double getFinalDamage();

	double getDamage();

	void setDamage(double damage);
}
