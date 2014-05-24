package com.laytonsmith.abstraction;

import java.util.Set;

/**
 * 
 * @author jb_aero
 */
public interface MCTeam {
	public void addPlayer(MCOfflinePlayer player);
	public boolean allowFriendlyFire();
	public boolean canSeeFriendlyInvisibles();
	public String getDisplayName();
	public String getName();
	public Set<MCOfflinePlayer> getPlayers();
	public String getPrefix();
	public MCScoreboard getScoreboard();
	public int getSize();
	public String getSuffix();
	public boolean hasPlayer(MCOfflinePlayer player);
	public boolean removePlayer(MCOfflinePlayer player);
	public void setAllowFriendlyFire(boolean enabled);
	public void setCanSeeFriendlyInvisibles(boolean enabled);
	public void setDisplayName(String displayName);
	public void setPrefix(String prefix);
	public void setSuffix(String suffix);
	public void unregister();
}
