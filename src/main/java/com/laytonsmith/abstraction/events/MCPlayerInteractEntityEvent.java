package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.enums.MCEquipmentSlot;

/**
 *
 * @author jb_aero
 */
public interface MCPlayerInteractEntityEvent extends MCPlayerEvent {
    public MCEntity getEntity();
    public boolean isCancelled();
    public void setCancelled(boolean cancelled);
    public MCEquipmentSlot getHand();
}