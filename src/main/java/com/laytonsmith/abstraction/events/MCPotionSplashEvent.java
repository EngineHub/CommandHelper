package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.entities.MCLivingEntity;
import java.util.Set;

/**
 * 
 * @author jb_aero
 */
public interface MCPotionSplashEvent extends MCProjectileHitEvent {
	public Set<MCLivingEntity> getAffectedEntities();
	public double getIntensity(MCLivingEntity le);
	public void setIntensity(MCLivingEntity le, double intensity);
}
