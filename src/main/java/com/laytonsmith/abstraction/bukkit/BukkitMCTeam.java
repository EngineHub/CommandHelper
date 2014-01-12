package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCOfflinePlayer;
import com.laytonsmith.abstraction.MCScoreboard;
import com.laytonsmith.abstraction.MCTeam;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.OfflinePlayer;
import org.bukkit.scoreboard.Team;

public class BukkitMCTeam implements MCTeam {

	Team t;
	public BukkitMCTeam(Team team) {
		t = team;
	}

	@Override
	public void addPlayer(MCOfflinePlayer player) {
		t.addPlayer((OfflinePlayer) player.getHandle());
	}

	@Override
	public boolean allowFriendlyFire() {
		return t.allowFriendlyFire();
	}

	@Override
	public boolean canSeeFriendlyInvisibles() {
		return t.canSeeFriendlyInvisibles();
	}

	@Override
	public String getDisplayName() {
		return t.getDisplayName();
	}

	@Override
	public String getName() {
		return t.getName();
	}

	@Override
	public Set<MCOfflinePlayer> getPlayers() {
		Set<MCOfflinePlayer> ret = new HashSet<MCOfflinePlayer>();
		for (OfflinePlayer o : t.getPlayers()) {
			ret.add(new BukkitMCOfflinePlayer(o));
		}
		return ret;
	}

	@Override
	public String getPrefix() {
		return t.getPrefix();
	}

	@Override
	public MCScoreboard getScoreboard() {
		return new BukkitMCScoreboard(t.getScoreboard());
	}

	@Override
	public int getSize() {
		return t.getSize();
	}

	@Override
	public String getSuffix() {
		return t.getSuffix();
	}

	@Override
	public boolean hasPlayer(MCOfflinePlayer player) {
		return t.hasPlayer((OfflinePlayer) player.getHandle());
	}

	@Override
	public boolean removePlayer(MCOfflinePlayer player) {
		return t.removePlayer((OfflinePlayer) player.getHandle());
	}

	@Override
	public void setAllowFriendlyFire(boolean enabled) {
		t.setAllowFriendlyFire(enabled);
	}

	@Override
	public void setCanSeeFriendlyInvisibles(boolean enabled) {
		t.setCanSeeFriendlyInvisibles(enabled);
	}

	@Override
	public void setDisplayName(String displayName) {
		t.setDisplayName(displayName);
	}

	@Override
	public void setPrefix(String prefix) {
		t.setPrefix(prefix);
	}

	@Override
	public void setSuffix(String suffix) {
		t.setSuffix(suffix);
	}

	@Override
	public void unregister() {
		t.unregister();
	}
}
