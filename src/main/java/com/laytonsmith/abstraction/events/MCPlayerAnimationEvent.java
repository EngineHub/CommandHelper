package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.events.BindableEvent;
import org.bukkit.event.player.PlayerAnimationType;

public interface MCPlayerAnimationEvent extends BindableEvent {

	PlayerAnimationType getAnimationType();

	MCPlayer getPlayer();

	boolean isCancelled();

	void setCancelled(boolean cancel);

}
