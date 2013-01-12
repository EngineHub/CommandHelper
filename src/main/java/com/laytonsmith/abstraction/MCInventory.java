
package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.enums.MCInventoryType;

/**
 *
 * @author Layton
 */
public interface MCInventory extends AbstractionObject {
	public MCInventoryType getType();
	public int getSize();
	public MCItemStack getItem(int index);
	public void setItem(int index, MCItemStack stack);
}
