package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCLivingEntity;
import java.util.Set;

public interface MCPotionSplashEvent extends MCProjectileHitEvent {
	Set<MCLivingEntity> getAffectedEntities();
	double getIntensity(MCLivingEntity le);
	void setIntensity(MCLivingEntity le, double intensity);
}
