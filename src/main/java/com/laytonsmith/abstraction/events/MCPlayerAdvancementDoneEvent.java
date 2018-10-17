package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.events.BindableEvent;
import org.bukkit.advancement.Advancement;

public interface MCPlayerAdvancementDoneEvent extends BindableEvent {

    public Advancement getAdvancement();

    public MCPlayer getPlayer();

}
