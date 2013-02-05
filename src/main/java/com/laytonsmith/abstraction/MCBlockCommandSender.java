package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.blocks.MCBlock;

/**
 *
 * @author Layton
 */
public interface MCBlockCommandSender extends MCCommandSender {
	MCBlock getBlock();
}
