package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCItemStack;

public interface MCItemSwapEvent extends MCPlayerEvent {
	public MCItemStack getMainHandItem();
	public MCItemStack getOffHandItem();
	public void setMainHandItem(MCItemStack item);
	public void setOffHandItem(MCItemStack item);
}
