package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.events.BindableEvent;

/**
 *
 * @author jb_aero
 */
public interface MCPlayerInteractEntityEvent extends BindableEvent {
    public MCEntity getEntity();
    public boolean isCancelled();
    public void setCancelled(boolean cancelled);
    public MCPlayer getPlayer();
}