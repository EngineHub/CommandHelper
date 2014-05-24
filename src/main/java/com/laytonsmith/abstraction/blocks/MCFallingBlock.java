
package com.laytonsmith.abstraction.blocks;

import com.laytonsmith.abstraction.MCEntity;

/**
 *
 * @author import
 */
public interface MCFallingBlock extends MCEntity {
	public byte getBlockData();
	public int getBlockId();
	public boolean getDropItem();
	public MCMaterial getMaterial();
	public void setDropItem(boolean drop);
}
