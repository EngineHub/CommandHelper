package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCItemStack;

public interface MCItemSwapEvent extends MCPlayerEvent {
	MCItemStack getMainHandItem();
	MCItemStack getOffHandItem();
	void setMainHandItem(MCItemStack item);
	void setOffHandItem(MCItemStack item);
}
