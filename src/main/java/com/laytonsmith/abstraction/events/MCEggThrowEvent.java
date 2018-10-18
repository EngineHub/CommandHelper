package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.entities.MCEgg;
import com.laytonsmith.abstraction.enums.MCEntityType;
import com.laytonsmith.core.events.BindableEvent;

public interface MCEggThrowEvent extends BindableEvent {

	MCEgg getEgg();

	MCPlayer getPlayer();

	MCEntityType getHatchingType();

	byte getNumHatches();

	boolean isHatching();

	void setHatching(boolean hatching);

	void setHatchingType(MCEntityType hatchingType);

	void setNumHatches(byte numHatches);

}
