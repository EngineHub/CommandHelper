package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.core.events.BindableEvent;
import org.bukkit.inventory.ItemStack;

public interface MCFurnaceSmeltEvent extends BindableEvent {

    public MCItemStack getResult();

    public MCItemStack getSource();

    public MCBlock getBlock();

    public boolean isCancelled();

    public void setCancelled(boolean cancel);

    public void setResult(ItemStack result);

}
