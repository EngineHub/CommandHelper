package com.laytonsmith.abstraction.blocks;

import com.laytonsmith.abstraction.MCItemStack;

import java.util.Map;

public interface MCDecoratedPot extends MCBlockState {

	Map<Side, MCMaterial> getSherds();

	void setSherd(Side side, MCMaterial sherd);

	MCItemStack getItemStack();

	void setItemStack(MCItemStack item);

	enum Side {
		BACK,
		FRONT,
		LEFT,
		RIGHT
	}
}
