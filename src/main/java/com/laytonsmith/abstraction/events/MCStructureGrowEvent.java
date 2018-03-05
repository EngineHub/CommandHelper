package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.blocks.MCBlockState;
import com.laytonsmith.abstraction.enums.MCTreeType;
import java.util.List;

public interface MCStructureGrowEvent extends MCWorldEvent {

	List<MCBlockState> getBlocks();

	MCLocation getLocation();

	MCPlayer getPlayer();

	MCTreeType getSpecies();

	boolean isFromBonemeal();
}
