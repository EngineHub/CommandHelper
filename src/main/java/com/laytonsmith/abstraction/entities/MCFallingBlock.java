
package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.blocks.MCMaterial;

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
