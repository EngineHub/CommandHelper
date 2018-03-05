package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.enums.MCEntityType;
import com.laytonsmith.abstraction.enums.MCTargetReason;
import com.laytonsmith.core.events.BindableEvent;

public interface MCEntityTargetEvent extends BindableEvent {

	MCEntity getTarget();

	void setTarget(MCEntity target);

	MCEntity getEntity();

	MCEntityType getEntityType();

	MCTargetReason getReason();
}
