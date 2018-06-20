package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.blocks.MCMaterial;
import java.util.Map;

public interface MCItemStack extends AbstractionObject {

	MCMaterialData getData();

	short getDurability();

	/**
	 * @deprecated Magic value
	 */
	@Deprecated
	int getTypeId();

	void setDurability(short data);

	void addEnchantment(MCEnchantment e, int level);

	void addUnsafeEnchantment(MCEnchantment e, int level);

	Map<MCEnchantment, Integer> getEnchantments();

	void removeEnchantment(MCEnchantment e);

	MCMaterial getType();

	/**
	 * @deprecated Magic value
	 */
	@Deprecated
	void setTypeId(int type);

	int maxStackSize();

	int getAmount();

	void setAmount(int amt);

	void setData(int data);

	boolean hasItemMeta();

	MCItemMeta getItemMeta();

	void setItemMeta(MCItemMeta im);
}
