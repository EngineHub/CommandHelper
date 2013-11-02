package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCMaterial;

/**
 *
 * @author import
 */
public interface MCFallingBlock extends MCEntity {
	public boolean getDropItem();
	public void setDropItem(boolean drop);
	public MCMaterial getMaterial();
}