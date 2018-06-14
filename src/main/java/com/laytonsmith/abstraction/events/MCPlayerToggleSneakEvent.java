package com.laytonsmith.abstraction.events;

public interface MCPlayerToggleSneakEvent extends MCPlayerEvent {

	boolean isSneaking();

	void setCancelled(boolean state);

	boolean isCancelled();
}
