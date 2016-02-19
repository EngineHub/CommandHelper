package com.laytonsmith.abstraction.events;

public interface MCExpChangeEvent extends MCPlayerEvent {
	public int getAmount();
	public void setAmount(int amount);
}
