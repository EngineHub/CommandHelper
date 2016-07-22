package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.abstraction.MCPotionData;

import java.util.List;

public interface MCTippedArrow extends MCArrow {

	public MCPotionData getBasePotionData();
	public List<MCLivingEntity.MCEffect> getCustomEffects();
	public void addCustomEffect(MCLivingEntity.MCEffect effect);
	public void clearCustomEffects();
	public void setBasePotionData(MCPotionData data);

}