
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCCreatureSpawner;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlockState;
import com.laytonsmith.abstraction.enums.MCEntityType;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCEntityType;
import org.bukkit.block.CreatureSpawner;

/**
 *
 * 
 */
public class BukkitMCCreatureSpawner extends BukkitMCBlockState implements MCCreatureSpawner{
	
	CreatureSpawner cs;
	public BukkitMCCreatureSpawner(CreatureSpawner cs){
		super(cs);
		this.cs = cs;
	}

	@Override
	public MCEntityType getSpawnedType() {
		return BukkitMCEntityType.valueOfConcrete(cs.getSpawnedType());
	}

	@Override
	public void setSpawnedType(MCEntityType type) {
		cs.setSpawnedType(((BukkitMCEntityType) type).getConcrete());
	}
	
}
