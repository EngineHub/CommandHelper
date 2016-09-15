package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCColor;
import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.abstraction.MCPotionData;
import com.laytonsmith.abstraction.MCProjectileSource;
import com.laytonsmith.abstraction.enums.MCParticle;

import java.util.List;

public interface MCAreaEffectCloud extends MCEntity {

	public MCPotionData getBasePotionData();
	public MCColor getColor();
	public List<MCLivingEntity.MCEffect> getCustomEffects();
	public int getDuration();
	public int getDurationOnUse();
	public MCParticle getParticle();
	public float getRadius();
	public float getRadiusOnUse();
	public float getRadiusPerTick();
	public int getReapplicationDelay();
	public MCProjectileSource getSource();
	public int getWaitTime();

	public void addCustomEffect(MCLivingEntity.MCEffect effect);
	public void clearCustomEffects();
	public void setBasePotionData(MCPotionData data);
	public void setColor(MCColor color);
	public void setDuration(int ticks);
	public void setDurationOnUse(int ticks);
	public void setParticle(MCParticle particle);
	public void setRadius(float radius);
	public void setRadiusOnUse(float radius);
	public void setRadiusPerTick(float radius);
	public void setReapplicationDelay(int ticks);
	public void setSource(MCProjectileSource source);
	public void setWaitTime(int ticks);

}