package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCWorld;

public interface MCWorldChangedEvent extends MCPlayerEvent {

	MCWorld getFrom();

	MCWorld getTo();
}
