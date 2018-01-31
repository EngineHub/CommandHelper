
package com.laytonsmith.abstraction.enums;

import com.laytonsmith.annotations.MEnum;

@MEnum("Rotation")
public enum MCRotation {
	CLOCKWISE,
	CLOCKWISE_135,
	CLOCKWISE_45,
	COUNTER_CLOCKWISE,
	COUNTER_CLOCKWISE_45,
	FLIPPED,
	FLIPPED_45,
	NONE
}
