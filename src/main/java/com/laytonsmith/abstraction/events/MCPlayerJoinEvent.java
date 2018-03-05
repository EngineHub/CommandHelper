package com.laytonsmith.abstraction.events;

public interface MCPlayerJoinEvent extends MCPlayerEvent {

	String getJoinMessage();

	void setJoinMessage(String message);
}
