package com.laytonsmith.abstraction.blocks;


public interface MCCommandBlock extends MCBlockState {
	String getCommand();
	String getName();
	void setCommand(String command);
	void setName(String name);
}
