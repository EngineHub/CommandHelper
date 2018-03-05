package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.enums.MCNameTagVisibility;
import com.laytonsmith.abstraction.enums.MCOption;
import com.laytonsmith.abstraction.enums.MCOptionStatus;

import java.util.Set;

public interface MCTeam {

	void addEntry(String entry);

	boolean allowFriendlyFire();

	boolean canSeeFriendlyInvisibles();

	String getDisplayName();

	String getName();

	MCNameTagVisibility getNameTagVisibility();

	MCOptionStatus getOption(MCOption option);

	Set<String> getEntries();

	String getPrefix();

	MCScoreboard getScoreboard();

	int getSize();

	String getSuffix();

	boolean hasEntry(String entry);

	boolean removeEntry(String entry);

	void setAllowFriendlyFire(boolean enabled);

	void setCanSeeFriendlyInvisibles(boolean enabled);

	void setDisplayName(String displayName);

	void setNameTagVisibility(MCNameTagVisibility visibility);

	void setOption(MCOption option, MCOptionStatus status);

	void setPrefix(String prefix);

	void setSuffix(String suffix);

	void unregister();
}
