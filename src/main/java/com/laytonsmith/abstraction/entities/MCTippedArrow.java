package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.abstraction.MCPotionData;

import java.util.List;

public interface MCTippedArrow extends MCArrow {

	MCPotionData getBasePotionData();

	List<MCLivingEntity.MCEffect> getCustomEffects();

	void addCustomEffect(MCLivingEntity.MCEffect effect);

	void clearCustomEffects();

	void setBasePotionData(MCPotionData data);
}
