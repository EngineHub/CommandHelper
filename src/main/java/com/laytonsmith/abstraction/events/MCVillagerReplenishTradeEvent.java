package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.entities.MCVillager;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.events.BindableEvent;
import org.bukkit.inventory.MerchantRecipe;

public interface MCVillagerReplenishTradeEvent extends BindableEvent {

    public CInt getBonus();

    public MCVillager getEntity();

    public MerchantRecipe getRecipe();

    public boolean isCancelled();

    public void setBonus(int bonus);

    public void setCancelled(boolean cancelled);

    public void setRecipe(MerchantRecipe recipe);

}
