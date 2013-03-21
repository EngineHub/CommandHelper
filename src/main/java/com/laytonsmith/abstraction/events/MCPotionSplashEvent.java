package com.laytonsmith.abstraction.events;

import java.util.Set;

import com.laytonsmith.abstraction.MCLivingEntity;

/**
 * 
 * @author jb_aero
 */
public interface MCPotionSplashEvent extends MCProjectileHitEvent {
	public Set<MCLivingEntity> getAffectedEntities();
	public double getIntensity(MCLivingEntity le);
	public void setIntensity(MCLivingEntity le, double intensity);
}
