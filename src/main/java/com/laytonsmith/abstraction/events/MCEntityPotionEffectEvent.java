package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.core.events.BindableEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;

import java.util.Optional;

public interface MCEntityPotionEffectEvent extends BindableEvent {

	MCLivingEntity getEntity();

	Optional<MCLivingEntity.MCEffect> getNewEffect();

	Optional<MCLivingEntity.MCEffect> getOldEffect();

	EntityPotionEffectEvent.Action getAction();

	EntityPotionEffectEvent.Cause getCause();
}
