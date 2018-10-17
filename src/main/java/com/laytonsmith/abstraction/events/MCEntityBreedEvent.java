package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.events.BindableEvent;

public interface MCEntityBreedEvent extends BindableEvent {

    public MCItemStack getBredWith();

    public MCLivingEntity getBreeder();

    public MCLivingEntity getEntity();

    public CInt getExperience();

    public MCLivingEntity getFather();

    public MCLivingEntity getMother();

    public boolean isCancelled();

    public void setCancelled(boolean cancelled);

    public void setExperience(int experience);

}
