package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.blocks.MCBlock;

/**
 *
 * 
 */
public interface MCBlockCommandSender extends MCCommandSender {
	MCBlock getBlock();
}
