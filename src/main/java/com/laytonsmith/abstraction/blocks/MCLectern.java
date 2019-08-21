package com.laytonsmith.abstraction.blocks;

import com.laytonsmith.abstraction.MCInventoryHolder;

public interface MCLectern extends MCBlockState, MCInventoryHolder {
	int getPage();
	void setPage(int page);
}
