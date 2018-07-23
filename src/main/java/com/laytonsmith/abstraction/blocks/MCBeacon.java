package com.laytonsmith.abstraction.blocks;

import java.util.Collection;

import com.laytonsmith.abstraction.MCBeaconInventory;
import com.laytonsmith.abstraction.MCInventoryHolder;
import com.laytonsmith.abstraction.MCLivingEntity;

public interface MCBeacon extends MCBlockState, MCInventoryHolder {
	Collection<MCLivingEntity> getEntitiesInRange();
	@Override
	MCBeaconInventory getInventory();
//	MCPotionEffect getPrimaryEffect();
//	MCPotionEffect getSecondaryEffect();
	int getTier();
//	void setPrimaryEffect(MCPotionEffect effect);
//	void setSecondaryEffect(MCPotionEffect effect);
}
