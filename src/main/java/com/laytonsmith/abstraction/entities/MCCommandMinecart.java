package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCCommandSender;

public interface MCCommandMinecart extends MCMinecart, MCCommandSender {
	public String getName();
	public void setName(String cmd);
	public String getCommand();
	public void setCommand(String cmd);
}
