package com.laytonsmith.abstraction.enums;

import com.laytonsmith.annotations.MEnum;

@MEnum("com.commandhelper.FishingState")
public enum MCFishingState {
	CAUGHT_ENTITY,
	CAUGHT_FISH,
	FAILED_ATTEMPT,
	FISHING,
	IN_GROUND,
	BITE
}
