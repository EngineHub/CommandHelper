package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.entities.MCPlayer;
import com.laytonsmith.abstraction.enums.MCGameMode;
import com.laytonsmith.core.events.BindableEvent;

public interface MCGamemodeChangeEvent extends BindableEvent {

	public MCPlayer getPlayer();
	public MCGameMode getNewGameMode();
}
