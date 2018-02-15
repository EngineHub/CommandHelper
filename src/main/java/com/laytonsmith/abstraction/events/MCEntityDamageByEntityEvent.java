package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;

public interface MCEntityDamageByEntityEvent extends MCEntityDamageEvent {
	MCEntity getDamager();
}