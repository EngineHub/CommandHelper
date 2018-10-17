package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.entities.MCVillager;
import com.laytonsmith.core.events.BindableEvent;
import org.bukkit.inventory.MerchantRecipe;

public interface MCVillagerAcquireTradeEvent extends BindableEvent {

	MCVillager getEntity();

	MerchantRecipe getRecipe();

	boolean isCancelled();

	void setCancelled(boolean cancelled);

	void setRecipe(MerchantRecipe recipe);

}
