package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.abstraction.MCPotionData;
import com.laytonsmith.abstraction.enums.MCPotionType;

import java.util.List;

public interface MCArrow extends MCProjectile {

	int getKnockbackStrength();

	void setKnockbackStrength(int strength);

	boolean isCritical();

	void setCritical(boolean critical);

	double getDamage();

	void setDamage(double damage);

	MCPotionData getBasePotionData();

	MCPotionType getBasePotionType();

	List<MCLivingEntity.MCEffect> getCustomEffects();

	void addCustomEffect(MCLivingEntity.MCEffect effect);

	void clearCustomEffects();

	void setBasePotionData(MCPotionData pd);

	void setBasePotionType(MCPotionType type);

	int getPierceLevel();

	void setPierceLevel(int level);

	PickupStatus getPickupStatus();

	void setPickupStatus(PickupStatus status);

	enum PickupStatus {
		ALLOWED,
		DISALLOWED,
		CREATIVE_ONLY
	}
}
