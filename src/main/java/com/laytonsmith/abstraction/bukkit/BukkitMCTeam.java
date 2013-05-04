package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCOfflinePlayer;
import com.laytonsmith.abstraction.MCScoreboard;
import com.laytonsmith.abstraction.MCTeam;
import com.laytonsmith.annotations.WrappedItem;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.OfflinePlayer;
import org.bukkit.scoreboard.Team;

public class BukkitMCTeam implements MCTeam {

	@WrappedItem Team t;
	public BukkitMCTeam(Team team) {
		t = team;
	}

	public void addPlayer(MCOfflinePlayer player) {
		t.addPlayer((OfflinePlayer) player.getHandle());
	}

	public boolean allowFriendlyFire() {
		return t.allowFriendlyFire();
	}

	public boolean canSeeFriendlyInvisibles() {
		return t.canSeeFriendlyInvisibles();
	}

	public String getDisplayName() {
		return t.getDisplayName();
	}

	public String getName() {
		return t.getName();
	}

	public Set<MCOfflinePlayer> getPlayers() {
		Set<MCOfflinePlayer> ret = new HashSet<MCOfflinePlayer>();
		for (OfflinePlayer o : t.getPlayers()) {
			ret.add(new BukkitMCOfflinePlayer(o));
		}
		return ret;
	}

	public String getPrefix() {
		return t.getPrefix();
	}

	public MCScoreboard getScoreboard() {
		return new BukkitMCScoreboard(t.getScoreboard());
	}

	public int getSize() {
		return t.getSize();
	}

	public String getSuffix() {
		return t.getSuffix();
	}

	public boolean hasPlayer(MCOfflinePlayer player) {
		return t.hasPlayer((OfflinePlayer) player.getHandle());
	}

	public boolean removePlayer(MCOfflinePlayer player) {
		return t.removePlayer((OfflinePlayer) player.getHandle());
	}

	public void setAllowFriendlyFire(boolean enabled) {
		t.setAllowFriendlyFire(enabled);
	}

	public void setCanSeeFriendlyInvisibles(boolean enabled) {
		t.setCanSeeFriendlyInvisibles(enabled);
	}

	public void setDisplayName(String displayName) {
		t.setDisplayName(displayName);
	}

	public void setPrefix(String prefix) {
		t.setPrefix(prefix);
	}

	public void setSuffix(String suffix) {
		t.setSuffix(suffix);
	}

	public void unregister() {
		t.unregister();
	}
}
