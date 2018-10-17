package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.entities.MCExperienceOrb;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.events.BindableEvent;

public interface MCItemMendEvent extends BindableEvent {

    public MCExperienceOrb getExperienceOrb();

    public MCItemStack getItem();

    public CInt getRepairAmount();

    public MCPlayer getPlayer();

    public boolean isCancelled();

    public void setCancelled(boolean cancelled);

    public void setRepairAmount(int amount);

}
