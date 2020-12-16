package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.entities.MCThrownPotion;
import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.core.events.BindableEvent;

import java.util.Map;

public interface MCPotionSplashEvent extends BindableEvent {

	MCThrownPotion getEntity();

	Map<MCLivingEntity, Double> getAffectedEntities();

	void setIntensity(MCLivingEntity le, double intensity);
}
