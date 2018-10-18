package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.enums.MCMainHand;
import com.laytonsmith.core.events.BindableEvent;

public interface MCChangedMainHandEvent extends BindableEvent {

	MCPlayer getPlayer();

	MCMainHand getMainHand();

}
