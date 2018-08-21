package com.laytonsmith.abstraction.blocks;

import com.laytonsmith.abstraction.MCPattern;

import java.util.List;

public interface MCBanner extends MCBlockState {
	int numberOfPatterns();

	List<MCPattern> getPatterns();

	void addPattern(MCPattern pattern);
}
