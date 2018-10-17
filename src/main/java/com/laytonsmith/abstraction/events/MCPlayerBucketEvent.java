package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCBlockFace;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.events.BindableEvent;
import org.bukkit.inventory.ItemStack;

public interface MCPlayerBucketEvent extends BindableEvent {

    public MCBlock getBlockClicked();

    public MCBlockFace getBlockFace();

    public MCMaterial getBucket();

    public MCItemStack getItemStack();

    public CString getType();

    public MCPlayer getPlayer();

    public boolean isCancelled();

    public void setCancelled(boolean cancel);

    public void setItemStack(ItemStack is);
}
