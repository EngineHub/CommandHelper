package com.laytonsmith.abstraction.enums;

import com.laytonsmith.annotations.MEnum;

@MEnum("FishingState")
public enum MCFishingState {
	CAUGHT_ENTITY,
	CAUGHT_FISH,
	FAILED_ATTEMPT,
	FISHING,
	IN_GROUND
}
