package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.core.events.BindableEvent;

public interface MCEntityBreedEvent extends BindableEvent {

	MCItemStack getBredWith();

	MCLivingEntity getBreeder();

	MCLivingEntity getEntity();

	int getExperience();

	MCLivingEntity getFather();

	MCLivingEntity getMother();

	boolean isCancelled();

	void setCancelled(boolean cancelled);

	void setExperience(int experience);

}
