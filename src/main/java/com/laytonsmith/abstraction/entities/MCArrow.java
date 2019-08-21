package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.abstraction.MCPotionData;

import java.util.List;

public interface MCArrow extends MCProjectile {

	int getKnockbackStrength();

	void setKnockbackStrength(int strength);

	boolean isCritical();

	void setCritical(boolean critical);

	double getDamage();

	void setDamage(double damage);

	MCPotionData getBasePotionData();

	List<MCLivingEntity.MCEffect> getCustomEffects();

	void addCustomEffect(MCLivingEntity.MCEffect effect);

	void clearCustomEffects();

	void setBasePotionData(MCPotionData pd);
}
