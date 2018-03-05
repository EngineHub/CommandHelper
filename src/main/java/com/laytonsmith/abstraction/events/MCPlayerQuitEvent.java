package com.laytonsmith.abstraction.events;

public interface MCPlayerQuitEvent extends MCPlayerEvent {

	String getMessage();

	void setMessage(String message);
}
