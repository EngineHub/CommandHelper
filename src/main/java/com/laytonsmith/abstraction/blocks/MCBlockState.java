package com.laytonsmith.abstraction.blocks;

import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCMaterialData;
import com.laytonsmith.abstraction.MCMetadatable;

public interface MCBlockState extends MCMetadatable {

	MCMaterialData getData();

	MCMaterial getType();

	/**
	 * @deprecated Magic value
	 */
	@Deprecated
	int getTypeId();

	/**
	 * @deprecated Magic value
	 */
	@Deprecated
	void setTypeId(int type);

	void setRawData(byte data);

	MCBlock getBlock();

	MCLocation getLocation();

	void update();
}
