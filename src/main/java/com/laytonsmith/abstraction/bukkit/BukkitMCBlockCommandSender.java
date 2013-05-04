package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCBlockCommandSender;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlock;
import org.bukkit.command.BlockCommandSender;

/**
 *
 * @author Layton
 */
public class BukkitMCBlockCommandSender extends BukkitMCCommandSender implements MCBlockCommandSender {

	BlockCommandSender bcs;
	public BukkitMCBlockCommandSender(AbstractionObject a){
        super(a);
		this.bcs = a.getHandle();
	}
	
	public BukkitMCBlockCommandSender(BlockCommandSender bcs){
		super(bcs);
		this.bcs = bcs;
	}
	
	public MCBlock getBlock() {
		return new BukkitMCBlock(bcs.getBlock());
	}

}
