package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.entities.MCPigZombie;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.events.BindableEvent;

public interface MCPigZombieAngerEvent extends BindableEvent {

    public MCPigZombie getEntity();

    public CInt getNewAnger();

    public MCEntity getTarget();

    public boolean isCancelled();

    public void setCancelled(boolean cancelled);

    public void setNewAnger(int newAnger);

}
