
package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.entities.MCItem;
import com.laytonsmith.abstraction.MCItemStack;

/**
 *
 * @author jb_aero
 */
public interface MCPlayerDropItemEvent extends MCPlayerEvent {
    public MCItem getItemDrop();
    public void setItemStack(MCItemStack stack);
    public boolean isCancelled();
    public void setCancelled(boolean cancelled);
}
