package com.laytonsmith.abstraction.events;

public interface MCItemHeldEvent extends MCPlayerEvent {

	int getNewSlot();

	int getPreviousSlot();
}
