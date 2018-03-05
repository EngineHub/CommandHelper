package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.enums.MCDyeColor;

import java.util.List;

public interface MCBannerMeta extends MCItemMeta {

	void addPattern(MCPattern pattern);

	MCDyeColor getBaseColor();

	MCPattern getPattern(int i);

	List<MCPattern> getPatterns();

	int numberOfPatterns();

	void removePattern(int i);

	void setBaseColor(MCDyeColor color);

	void setPattern(int i, MCPattern pattern);

	void setPatterns(List<MCPattern> patterns);

}
