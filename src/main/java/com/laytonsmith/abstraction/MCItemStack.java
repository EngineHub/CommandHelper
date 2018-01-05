
package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.blocks.MCMaterial;
import java.util.Map;

public interface MCItemStack extends AbstractionObject{
	MCMaterialData getData();
	short getDurability();
	@Deprecated
	int getTypeId();
	void setDurability(short data);
	void addEnchantment(MCEnchantment e, int level);
	void addUnsafeEnchantment(MCEnchantment e, int level);
	Map<MCEnchantment, Integer> getEnchantments();
	void removeEnchantment(MCEnchantment e);
	MCMaterial getType();
	@Deprecated
	void setTypeId(int type);
	int maxStackSize();
	int getAmount();
	void setData(int data);
	boolean hasItemMeta();
	MCItemMeta getItemMeta();
	void setItemMeta(MCItemMeta im);
}
