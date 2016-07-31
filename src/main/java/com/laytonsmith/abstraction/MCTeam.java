package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.enums.MCNameTagVisibility;
import com.laytonsmith.abstraction.enums.MCOption;
import com.laytonsmith.abstraction.enums.MCOptionStatus;

import java.util.Set;

/**
 * 
 * @author jb_aero
 */
public interface MCTeam {
	public void addEntry(String entry);
	public boolean allowFriendlyFire();
	public boolean canSeeFriendlyInvisibles();
	public String getDisplayName();
	public String getName();
	public MCNameTagVisibility getNameTagVisibility();
	public MCOptionStatus getOption(MCOption option);
	public Set<String> getEntries();
	public String getPrefix();
	public MCScoreboard getScoreboard();
	public int getSize();
	public String getSuffix();
	public boolean hasEntry(String entry);
	public boolean removeEntry(String entry);
	public void setAllowFriendlyFire(boolean enabled);
	public void setCanSeeFriendlyInvisibles(boolean enabled);
	public void setDisplayName(String displayName);
	public void setNameTagVisibility(MCNameTagVisibility visibility);
	public void setOption(MCOption option, MCOptionStatus status);
	public void setPrefix(String prefix);
	public void setSuffix(String suffix);
	public void unregister();
}
