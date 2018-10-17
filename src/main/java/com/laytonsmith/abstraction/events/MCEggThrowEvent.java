package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.events.BindableEvent;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EntityType;

public interface MCEggThrowEvent extends BindableEvent {

    public Egg getEgg();

    public MCPlayer getPlayer();

    public EntityType getHatchingType();

    public byte getNumHatches();

    public boolean isHatching();

    public void setHatching(boolean hatching);

    public void setHatchingType(EntityType hatchingType);

    public void setNumHatches(byte numHatches);

}
