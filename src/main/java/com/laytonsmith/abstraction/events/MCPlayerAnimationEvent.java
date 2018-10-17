package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.events.BindableEvent;
import org.bukkit.event.player.PlayerAnimationType;

public interface MCPlayerAnimationEvent extends BindableEvent {

    public PlayerAnimationType getAnimationType();

    public MCPlayer getPlayer();

    public boolean isCancelled();

    public void setCancelled(boolean cancel);

}
