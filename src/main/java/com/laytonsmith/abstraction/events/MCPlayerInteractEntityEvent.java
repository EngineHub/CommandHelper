package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.entities.MCEntity;

/**
 *
 * @author jb_aero
 */
public interface MCPlayerInteractEntityEvent extends MCPlayerEvent {
    public MCEntity getEntity();
    public boolean isCancelled();
    public void setCancelled(boolean cancelled);
}