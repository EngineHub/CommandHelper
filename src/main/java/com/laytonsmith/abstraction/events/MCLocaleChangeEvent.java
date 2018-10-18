package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.events.BindableEvent;

public interface MCLocaleChangeEvent extends BindableEvent {

	String getLocale();

	MCPlayer getPlayer();

}
