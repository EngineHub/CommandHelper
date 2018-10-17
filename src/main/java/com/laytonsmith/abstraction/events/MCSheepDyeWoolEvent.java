package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.entities.MCSheep;
import com.laytonsmith.core.events.BindableEvent;
import org.bukkit.DyeColor;

public interface MCSheepDyeWoolEvent extends BindableEvent {

	DyeColor getColor();

	MCSheep getEntity();

	boolean isCancelled();

	void setCancelled(boolean cancelled);

	void setColor(DyeColor color);

}
