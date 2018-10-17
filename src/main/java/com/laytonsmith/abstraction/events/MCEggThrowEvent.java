package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.events.BindableEvent;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EntityType;

public interface MCEggThrowEvent extends BindableEvent {

	Egg getEgg();

	MCPlayer getPlayer();

	EntityType getHatchingType();

	byte getNumHatches();

	boolean isHatching();

	void setHatching(boolean hatching);

	void setHatchingType(EntityType hatchingType);

	void setNumHatches(byte numHatches);

}
