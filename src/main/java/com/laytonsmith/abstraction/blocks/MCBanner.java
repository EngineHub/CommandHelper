package com.laytonsmith.abstraction.blocks;

import com.laytonsmith.abstraction.MCPattern;
import com.laytonsmith.abstraction.enums.MCDyeColor;

import java.util.List;

public interface MCBanner extends MCBlockState {
	MCDyeColor getBaseColor();
	void setBaseColor(MCDyeColor color);
	int numberOfPatterns();
	List<MCPattern> getPatterns();
	void addPattern(MCPattern pattern);
}
