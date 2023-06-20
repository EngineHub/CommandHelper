package com.laytonsmith.abstraction.blocks;

public interface MCSign extends MCBlockState, MCSignText {

	/**
	 * Gets the back text for this sign block.
	 * Using the methods directly on the sign object will apply to the front text for backwards compatibility.
	 *
	 * @return Back sign text object (null if unavailable)
	 */
	MCSignText getBackText();

	enum Side {
		FRONT,
		BACK
	}
}
