package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.entities.MCTropicalFish;
import com.laytonsmith.abstraction.enums.MCDyeColor;

public interface MCTropicalFishBucketMeta extends MCItemMeta {

	MCDyeColor getPatternColor();
	void setPatternColor(MCDyeColor color);
	MCDyeColor getBodyColor();
	void setBodyColor(MCDyeColor color);
	MCTropicalFish.MCPattern getPattern();
	void setPattern(MCTropicalFish.MCPattern pattern);
	boolean hasVariant();

}
