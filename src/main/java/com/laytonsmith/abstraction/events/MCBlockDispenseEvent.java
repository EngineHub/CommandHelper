package com.laytonsmith.abstraction.events;

import com.laytonsmith.PureUtilities.Vector3D;
import com.laytonsmith.abstraction.MCItemStack;

public interface MCBlockDispenseEvent extends MCBlockEvent {
	MCItemStack getItem();
	void setItem(MCItemStack item);
	Vector3D getVelocity();
	void setVelocity(Vector3D vel);
	boolean isCancelled();
	void setCancelled(boolean cancel);
}