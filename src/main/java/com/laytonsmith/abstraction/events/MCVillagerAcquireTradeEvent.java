package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCMerchantRecipe;
import com.laytonsmith.abstraction.entities.MCVillager;
import com.laytonsmith.core.events.BindableEvent;

public interface MCVillagerAcquireTradeEvent extends BindableEvent {

	MCVillager getEntity();

	MCMerchantRecipe getRecipe();

	boolean isCancelled();

	void setCancelled(boolean cancelled);

	void setRecipe(MCMerchantRecipe recipe);

}
