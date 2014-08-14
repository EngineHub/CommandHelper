package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.blocks.MCBlockState;
import com.laytonsmith.abstraction.enums.MCTreeType;
import java.util.List;

/**
 *
 * @author KingFisher
 */
public interface MCStructureGrowEvent extends MCWorldEvent {

	public List<MCBlockState> getBlocks();

	public MCLocation getLocation();

	public MCPlayer getPlayer();

	public MCTreeType getSpecies();

	public boolean isFromBonemeal();
}