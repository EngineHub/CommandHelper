package com.laytonsmith.abstraction;

import java.util.List;

public interface MCCommandMap extends AbstractionObject {

	public void clearCommands();
	
	public boolean isCommand(String name);
	
	public MCCommand getCommand(String name);
	
	public List<MCCommand> getCommands();
	
	public boolean register(String fallback, MCCommand cmd);
	
	public boolean register(String label, String fallback, MCCommand cmd);
	
	public boolean unregister(MCCommand cmd);
}
