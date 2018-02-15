package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.enums.MCRegainReason;
import com.laytonsmith.core.events.BindableEvent;

public interface MCEntityRegainHealthEvent extends BindableEvent {
	double getAmount();
	void setAmount(double amount);

	MCEntity getEntity();
	MCRegainReason getRegainReason();
}
