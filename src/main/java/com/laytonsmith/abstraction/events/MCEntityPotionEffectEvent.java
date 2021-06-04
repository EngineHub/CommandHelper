package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.abstraction.enums.MCPotionAction;
import com.laytonsmith.abstraction.enums.MCPotionCause;
import com.laytonsmith.core.events.BindableEvent;

import java.util.Optional;

public interface MCEntityPotionEffectEvent extends BindableEvent {

	MCLivingEntity getEntity();

	Optional<MCLivingEntity.MCEffect> getNewEffect();

	Optional<MCLivingEntity.MCEffect> getOldEffect();

	MCPotionAction getAction();

	MCPotionCause getCause();
}
