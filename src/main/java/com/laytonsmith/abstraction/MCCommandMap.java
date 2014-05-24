package com.laytonsmith.abstraction;

import java.util.List;

public interface MCCommandMap extends AbstractionObject {

	public void clearCommands();
	
	public boolean isCommand(String name);
	
	/**
	 * 
	 * @param name name of the command
	 * @return a command if found, or null if one isn't
	 */
	public MCCommand getCommand(String name);
	
	public List<MCCommand> getCommands();
	
	/**
	 * 
	 * @param fallback the text added to the start of the command if the chosen name is already taken
	 * @param cmd
	 * @return
	 */
	public boolean register(String fallback, MCCommand cmd);
	
	/**
	 * 
	 * @param label
	 * @param fallback the text added to the start of the command if the chosen name is already taken
	 * @param cmd
	 * @return
	 */
	public boolean register(String label, String fallback, MCCommand cmd);
	
	public boolean unregister(MCCommand cmd);
}
