package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.core.events.BindableEvent;

public interface MCBlockDamageEvent extends BindableEvent{

    public boolean getInstaBreak();

    public MCItemStack getItemInHand();

    public MCPlayer getPlayer();

    public MCBlock getBlock();

    public boolean isCancelled();

    public void setCancelled(boolean cancel);

    public void setInstaBreak(boolean bool);

}
