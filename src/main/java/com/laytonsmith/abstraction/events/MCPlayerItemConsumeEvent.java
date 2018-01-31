package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCItemStack;

public interface MCPlayerItemConsumeEvent extends MCPlayerEvent {
	MCItemStack getItem();
	void setItem(MCItemStack item);
}
