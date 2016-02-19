package com.laytonsmith.abstraction.blocks;


public interface MCCommandBlock extends MCBlockState {
	
	public String getCommand();
	
	public String getName();
	
	public void setCommand(String command);
	
	public void setName(String name);
}
