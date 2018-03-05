package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.enums.MCGameMode;
import com.laytonsmith.core.events.BindableEvent;

public interface MCGamemodeChangeEvent extends BindableEvent {

	MCPlayer getPlayer();

	MCGameMode getNewGameMode();
}
