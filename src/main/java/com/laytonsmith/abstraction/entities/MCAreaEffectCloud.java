package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCColor;
import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.abstraction.MCPotionData;
import com.laytonsmith.abstraction.MCProjectileSource;
import com.laytonsmith.abstraction.enums.MCParticle;

import java.util.List;

public interface MCAreaEffectCloud extends MCEntity {

	MCPotionData getBasePotionData();

	MCColor getColor();

	List<MCLivingEntity.MCEffect> getCustomEffects();

	int getDuration();

	int getDurationOnUse();

	MCParticle getParticle();

	float getRadius();

	float getRadiusOnUse();

	float getRadiusPerTick();

	int getReapplicationDelay();

	MCProjectileSource getSource();

	int getWaitTime();

	void addCustomEffect(MCLivingEntity.MCEffect effect);

	void clearCustomEffects();

	void setBasePotionData(MCPotionData data);

	void setColor(MCColor color);

	void setDuration(int ticks);

	void setDurationOnUse(int ticks);

	void setParticle(MCParticle particle, Object data);

	void setRadius(float radius);

	void setRadiusOnUse(float radius);

	void setRadiusPerTick(float radius);

	void setReapplicationDelay(int ticks);

	void setSource(MCProjectileSource source);

	void setWaitTime(int ticks);

}
