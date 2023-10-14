package com.laytonsmith.abstraction.blocks;

import java.util.Map;

public interface MCDecoratedPot extends MCBlockState {

	Map<Side, MCMaterial> getSherds();

	void setSherd(Side side, MCMaterial sherd);

	enum Side {
		BACK,
		FRONT,
		LEFT,
		RIGHT
	}
}
