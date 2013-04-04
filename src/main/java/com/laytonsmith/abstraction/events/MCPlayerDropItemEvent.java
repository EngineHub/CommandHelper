
package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCItem;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.events.BindableEvent;

/**
 *
 * @author jb_aero
 */
public interface MCPlayerDropItemEvent extends BindableEvent {
    public MCItem getItemDrop();
    public void setItemStack(MCItemStack stack);
    public boolean isCancelled();
    public void setCancelled(boolean cancelled);
    public MCPlayer getPlayer();
}
