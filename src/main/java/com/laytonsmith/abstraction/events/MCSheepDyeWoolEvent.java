package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.entities.MCSheep;
import com.laytonsmith.abstraction.enums.MCDyeColor;
import com.laytonsmith.core.events.BindableEvent;
import org.bukkit.DyeColor;

public interface MCSheepDyeWoolEvent extends BindableEvent {

    public DyeColor getColor();

    public MCSheep getEntity();

    public boolean isCancelled();

    public void setCancelled(boolean cancelled);

    public void setColor(DyeColor color);

}
