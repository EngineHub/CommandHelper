package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.entities.MCEntity;
import com.laytonsmith.abstraction.entities.MCHanging;
import com.laytonsmith.abstraction.enums.MCRemoveCause;
import com.laytonsmith.core.events.BindableEvent;

/**
 * 
 * @author Hekta
 */
public interface MCHangingBreakEvent extends BindableEvent {

	public MCHanging getEntity();
	public MCRemoveCause getCause();
	public MCEntity getRemover();
}