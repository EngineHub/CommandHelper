package com.laytonsmith.abstraction.events;

public interface MCPlayerCommandEvent extends MCPlayerEvent {

	String getCommand();

	void cancel();

	void setCommand(String val);

	boolean isCancelled();
}
