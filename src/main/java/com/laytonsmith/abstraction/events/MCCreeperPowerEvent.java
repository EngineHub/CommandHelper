package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.entities.MCLightningStrike;
import com.laytonsmith.core.events.BindableEvent;

public interface MCCreeperPowerEvent extends BindableEvent  {

    public String getCause();

    public MCEntity getEntity();

    public MCLightningStrike getLightning();

    public boolean isCancelled();

    public void setCancelled(boolean cancelled);

}
