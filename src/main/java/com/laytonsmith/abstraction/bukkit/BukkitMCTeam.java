package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.abstraction.MCScoreboard;
import com.laytonsmith.abstraction.MCTeam;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scoreboard.Team;

public class BukkitMCTeam implements MCTeam {

	Team t;
	public BukkitMCTeam(Team team) {
		t = team;
	}

	@Override
	public void addEntry(String entry) {
		if(ReflectionUtils.hasMethod(t.getClass(), "addEntry", null, String.class)){
			// Spigot method
			t.addEntry(entry);
		} else {
			// Bukkit method
			OfflinePlayer player = Bukkit.getOfflinePlayer(entry);
			ReflectionUtils.invokeMethod(t, "addPlayer", player);
		}
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
	public Set<String> getEntries() {
		Set<String> ret = new HashSet<String>();
		if(ReflectionUtils.hasMethod(t.getClass(), "getEntries", null)) {
			// Spigot method
			for (String e : t.getEntries()) {
				ret.add(e);
			}
		} else {
			// Bukkit method
			for (OfflinePlayer o : (Set<OfflinePlayer>) ReflectionUtils.invokeMethod(t, "getPlayers")) {
				ret.add(o.getName());
			}
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
	public boolean hasEntry(String entry) {
		if(ReflectionUtils.hasMethod(t.getClass(), "hasEntry", null, String.class)){
			// Spigot method
			return t.hasEntry(entry);
		} else {
			// Bukkit method
			OfflinePlayer player = Bukkit.getOfflinePlayer(entry);
			return (boolean) ReflectionUtils.invokeMethod(t, "hasPlayer", player);
		}
	}

	@Override
	public boolean removeEntry(String entry) {
		if(ReflectionUtils.hasMethod(t.getClass(), "removeEntry", null, String.class)){
			// Spigot method
			return t.removeEntry(entry);
		} else {
			// Bukkit method
			OfflinePlayer player = Bukkit.getOfflinePlayer(entry);
			return (boolean) ReflectionUtils.invokeMethod(t, "removePlayer", player);
		}
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
