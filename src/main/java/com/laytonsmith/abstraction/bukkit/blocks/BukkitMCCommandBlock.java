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
	
	@Override
	public String getCommand() {
		return cb.getCommand();
	}

	@Override
	public String getName() {
		return cb.getName();
	}

	@Override
	public void setCommand(String command) {
		cb.setCommand(command);
		cb.update();
	}

	@Override
	public void setName(String name) {
		cb.setName(name);
		cb.update();
	}
}
