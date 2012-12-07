/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.events.BindableEvent;

/**
 *
 * @author jb_aero
 */
public interface MCPlayerDropItemEvent extends BindableEvent {
    public MCItemStack getItemDrop();
    public void setItem(MCItemStack stack);
    public boolean isCancelled();
    public void setCancelled(boolean cancelled);
    public MCPlayer getPlayer();
}
