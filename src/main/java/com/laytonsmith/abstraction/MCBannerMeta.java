package com.laytonsmith.abstraction;

import java.util.List;

public interface MCBannerMeta extends MCItemMeta {

	void addPattern(MCPattern pattern);

	MCPattern getPattern(int i);

	List<MCPattern> getPatterns();

	int numberOfPatterns();

	void removePattern(int i);

	void setPattern(int i, MCPattern pattern);

	void setPatterns(List<MCPattern> patterns);

}
