package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCMerchantRecipe;
import com.laytonsmith.abstraction.entities.MCVillager;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.events.BindableEvent;

public interface MCVillagerReplenishTradeEvent extends BindableEvent {

	CInt getBonus();

	MCVillager getEntity();

	MCMerchantRecipe getRecipe();

	boolean isCancelled();

	void setBonus(int bonus);

	void setCancelled(boolean cancelled);

	void setRecipe(MCMerchantRecipe recipe);

}
