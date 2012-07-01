package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.core.events.BindableEvent;

/**
 *
 * @author layton
 */
public interface MCWorldChangedEvent extends BindableEvent {
    public MCWorld getFrom();
    public MCPlayer getPlayer();
    public MCWorld getTo();
}
