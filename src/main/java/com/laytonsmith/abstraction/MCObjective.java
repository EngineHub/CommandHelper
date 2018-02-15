package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.enums.MCDisplaySlot;

public interface MCObjective {
	String getCriteria();
	String getDisplayName();
	MCDisplaySlot getDisplaySlot();
	String getName();
	MCScore getScore(String entry);
	MCScoreboard getScoreboard();
	boolean isModifiable();
	void setDisplayName(String displayName);
	void setDisplaySlot(MCDisplaySlot slot);
	void unregister();
}
