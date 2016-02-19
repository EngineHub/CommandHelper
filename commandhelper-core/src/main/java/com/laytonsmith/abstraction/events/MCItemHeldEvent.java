package com.laytonsmith.abstraction.events;

public interface MCItemHeldEvent extends MCPlayerEvent {
	public int getNewSlot();
	public int getPreviousSlot();
}
