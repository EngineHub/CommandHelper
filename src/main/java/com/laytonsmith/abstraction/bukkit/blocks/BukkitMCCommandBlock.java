package com.laytonsmith.abstraction.bukkit.blocks;

import com.laytonsmith.abstraction.blocks.MCCommandBlock;
import org.bukkit.block.CommandBlock;

/**
 * 
 * @author jb_aero
 */
public class BukkitMCCommandBlock extends BukkitMCBlockState implements
		MCCommandBlock {

	CommandBlock cb;
	public BukkitMCCommandBlock(CommandBlock block) {
		super(block);
		cb = block;
	}
	
	public String getCommand() {
		return cb.getCommand();
	}

	public String getName() {
		return cb.getName();
	}

	public void setCommand(String command) {
		cb.setCommand(command);
	}

	public void setName(String name) {
		cb.setName(name);
	}
}
