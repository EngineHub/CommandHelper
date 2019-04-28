package com.laytonsmith.abstraction.blocks;

import java.util.Collection;

import com.laytonsmith.abstraction.MCLivingEntity;

public interface MCBeacon extends MCBlockState {
	Collection<MCLivingEntity> getEntitiesInRange();
//	MCPotionEffect getPrimaryEffect();
//	MCPotionEffect getSecondaryEffect();
	int getTier();
//	void setPrimaryEffect(MCPotionEffect effect);
//	void setSecondaryEffect(MCPotionEffect effect);
}
