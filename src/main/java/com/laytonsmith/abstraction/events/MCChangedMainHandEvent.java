package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.events.BindableEvent;
import org.bukkit.inventory.MainHand;

public interface MCChangedMainHandEvent extends BindableEvent {

    public MCPlayer getPlayer();

    public MainHand getMainHand();

}
