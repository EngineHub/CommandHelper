package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.entities.MCAbstractHorse;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.events.BindableEvent;

public interface MCHorseJumpEvent extends BindableEvent {

    public MCAbstractHorse getEntity();

    public CDouble getPower();

    public boolean isCancelled();

    public void setCancelled(boolean cancelled);

    public void setPower(float power);

}
