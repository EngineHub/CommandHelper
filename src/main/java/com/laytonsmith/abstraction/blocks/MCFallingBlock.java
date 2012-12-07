/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.blocks;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCMetadatable;
import com.laytonsmith.abstraction.blocks.MCMaterial;

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
