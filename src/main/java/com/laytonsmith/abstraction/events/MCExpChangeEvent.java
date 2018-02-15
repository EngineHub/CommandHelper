package com.laytonsmith.abstraction.events;

public interface MCExpChangeEvent extends MCPlayerEvent {
	int getAmount();
	void setAmount(int amount);
}
