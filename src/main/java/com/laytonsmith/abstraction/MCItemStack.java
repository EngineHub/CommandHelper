package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.enums.MCEnchantment;

import java.util.Map;

public interface MCItemStack extends AbstractionObject {

	/**
	 * Returns the item type in this stack. May return air if empty.
	 *
	 * @return the item type
	 */
	MCMaterial getType();

	/**
	 * Returns the default maximum stack size for the item type in this stack, or if the item has
	 * a max_stack_size item data component, that component's value will be returned instead.
	 * Use {@link MCMaterial#getMaxStackSize()} to always get the default max stack size for this item type.
	 *
	 * @return the effective maximum stack size
	 */
	int maxStackSize();

	/**
	 * Returns the quantity of items in this stack.
	 *
	 * @return the stack quantity
	 */
	int getAmount();

	/**
	 * Sets the quantity of items in this stack. Setting to zero makes this stack empty.
	 *
	 * @param amt the stack quantity
	 */
	void setAmount(int amt);

	/**
	 * Returns if the stack has any valid items in it.
	 * This should be checked first before using most other methods.
	 *
	 * @return true if not empty
	 */
	boolean isEmpty();

	void addEnchantment(MCEnchantment e, int level);

	void addUnsafeEnchantment(MCEnchantment e, int level);

	Map<MCEnchantment, Integer> getEnchantments();

	void removeEnchantment(MCEnchantment e);

	boolean hasItemMeta();

	MCItemMeta getItemMeta();

	void setItemMeta(MCItemMeta im);
}
