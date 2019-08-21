package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.abstraction.enums.MCDyeColor;
import com.laytonsmith.annotations.MEnum;

public interface MCTropicalFish extends MCLivingEntity {

	MCDyeColor getPatternColor();
	void setPatternColor(MCDyeColor color);
	MCDyeColor getBodyColor();
	void setBodyColor(MCDyeColor color);
	MCTropicalFish.MCPattern getPattern();
	void setPattern(MCTropicalFish.MCPattern pattern);

	@MEnum("com.commandhelper.TropicalFishPattern")
	enum MCPattern {
		KOB,
		SUNSTREAK,
		SNOOPER,
		DASHER,
		BRINELY,
		SPOTTY,
		FLOPPER,
		STRIPEY,
		GLITTER,
		BLOCKFISH,
		BETTY,
		CLAYFISH
	}

}
