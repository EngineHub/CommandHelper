package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.blocks.MCBlock;
import java.util.List;

public interface MCBlockPistonExtendEvent extends MCBlockPistonEvent {
	List<MCBlock> getPushedBlocks();
}
