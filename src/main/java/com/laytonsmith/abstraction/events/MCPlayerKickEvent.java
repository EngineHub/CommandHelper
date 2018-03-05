package com.laytonsmith.abstraction.events;

public interface MCPlayerKickEvent extends MCPlayerEvent {

	String getMessage();

	void setMessage(String message);

	String getReason();

	void setReason(String message);

	boolean isCancelled();

	void setCancelled(boolean cancelled);
}
