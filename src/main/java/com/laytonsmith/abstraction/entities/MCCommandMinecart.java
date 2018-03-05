package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCCommandSender;

public interface MCCommandMinecart extends MCMinecart, MCCommandSender {

	@Override
	String getName();

	void setName(String cmd);

	String getCommand();

	void setCommand(String cmd);
}
