package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.enums.MCEnderDragonPhase;
import com.laytonsmith.core.events.BindableEvent;

public interface MCEnderdragonChangePhaseEvent extends BindableEvent {

	String getCurrentPhase();

	MCEntity getEntity();

	String getNewPhase();

	boolean iscancelled();

	void setCancelled(boolean cancelled);

	void setNewPhase(MCEnderDragonPhase newPhase);
}
