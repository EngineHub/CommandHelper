package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.entities.MCHanging;
import com.laytonsmith.core.events.BindableEvent;

public interface MCHangingPlaceEvent extends BindableEvent {

	MCHanging getEntity();

	MCPlayer getPlayer();

}
