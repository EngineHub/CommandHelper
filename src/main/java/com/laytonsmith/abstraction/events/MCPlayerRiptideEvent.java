package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.events.BindableEvent;

public interface MCPlayerRiptideEvent extends BindableEvent {

	MCItemStack getItem();

	MCPlayer getPlayer();

}
