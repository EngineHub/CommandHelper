package com.laytonsmith.abstraction.blocks;

import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCMaterialData;
import com.laytonsmith.abstraction.MCMetadatable;

/**
 *
 * 
 */
public interface MCBlockState extends MCMetadatable {

	public MCMaterialData getData();

	public int getTypeId();

	public MCBlock getBlock();

	public MCLocation getLocation();

	public void update();
}