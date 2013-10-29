package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCHanging;
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