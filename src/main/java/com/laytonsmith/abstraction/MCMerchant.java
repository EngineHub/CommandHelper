package com.laytonsmith.abstraction;

import java.util.List;

public interface MCMerchant extends AbstractionObject {

	boolean isTrading();

	MCHumanEntity getTrader();

	List<MCMerchantRecipe> getRecipes();

	void setRecipes(List<MCMerchantRecipe> recipes);

	String getTitle();
}
