package com.laytonsmith.abstraction;

import java.util.List;

public interface MCMerchantRecipe extends MCRecipe {

	int getMaxUses();

	void setMaxUses(int maxUses);

	int getUses();

	void setUses(int uses);

	boolean hasExperienceReward();

	void setHasExperienceReward(boolean flag);

	List<MCItemStack> getIngredients();

	void setIngredients(List<MCItemStack> ingredients);
}
