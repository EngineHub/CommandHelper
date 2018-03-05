package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCHanging;
import com.laytonsmith.abstraction.enums.MCRemoveCause;
import com.laytonsmith.core.events.BindableEvent;

public interface MCHangingBreakEvent extends BindableEvent {

	MCHanging getEntity();

	MCRemoveCause getCause();

	MCEntity getRemover();
}
