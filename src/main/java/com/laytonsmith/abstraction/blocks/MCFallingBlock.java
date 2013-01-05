
package com.laytonsmith.abstraction.blocks;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCMetadatable;

/**
 *
 * @author import
 */
public interface MCFallingBlock extends MCEntity, MCMetadatable {
	public byte getBlockData();
	public int getBlockId();
	public boolean getDropItem();
	public MCMaterial getMaterial();
	public void setDropItem(boolean drop);
}
