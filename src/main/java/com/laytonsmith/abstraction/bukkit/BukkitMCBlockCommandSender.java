package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.AbstractionUtils;
import com.laytonsmith.abstraction.MCBlockCommandSender;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlock;
import com.laytonsmith.annotations.WrappedItem;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Layton
 */
public class BukkitMCBlockCommandSender extends BukkitMCCommandSender implements MCBlockCommandSender {

	@WrappedItem BlockCommandSender bcs;

	@Override
	public BlockCommandSender getHandle() {
		return bcs;
	}
	
	public MCBlock getBlock() {
		return AbstractionUtils.wrap(bcs.getBlock());
	}

}
