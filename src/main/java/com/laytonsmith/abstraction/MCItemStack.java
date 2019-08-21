package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.blocks.MCMaterial;
import java.util.Map;

public interface MCItemStack extends AbstractionObject {
	/**
	 * @deprecated Magic value
	 */
	@Deprecated
	int getTypeId();

	void addEnchantment(MCEnchantment e, int level);

	void addUnsafeEnchantment(MCEnchantment e, int level);

	Map<MCEnchantment, Integer> getEnchantments();

	void removeEnchantment(MCEnchantment e);

	MCMaterial getType();

	void setType(MCMaterial type);

	int maxStackSize();

	int getAmount();

	void setAmount(int amt);

	boolean hasItemMeta();

	MCItemMeta getItemMeta();

	void setItemMeta(MCItemMeta im);

	boolean isEmpty();
}
