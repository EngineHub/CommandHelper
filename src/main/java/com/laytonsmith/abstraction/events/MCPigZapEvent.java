package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.entities.MCLightningStrike;
import com.laytonsmith.abstraction.entities.MCPig;
import com.laytonsmith.abstraction.entities.MCPigZombie;
import com.laytonsmith.core.events.BindableEvent;

public interface MCPigZapEvent extends BindableEvent {

    public MCPig getEntity();

    public MCLightningStrike getLightning();

    public MCPigZombie getPigZombie();

    public boolean isCancelled();

    public void setCancelled(boolean cancelled);

}
