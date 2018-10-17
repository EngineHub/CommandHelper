package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.events.BindableEvent;

public interface MCEntityAirChangeEvent extends BindableEvent {

    public CInt getAmount();

    public MCEntity getEntity();

    public boolean isCancelled();

    public void setAmount(int amount);

    public void setCancelled(boolean cancelled);

}
