package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.abstraction.MCScoreboard;
import com.laytonsmith.abstraction.MCTeam;
import java.util.HashSet;
import java.util.Set;

import com.laytonsmith.abstraction.enums.MCNameTagVisibility;
import com.laytonsmith.abstraction.enums.MCVersion;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCNameTagVisibility;
import com.laytonsmith.core.Static;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Team;

public class BukkitMCTeam implements MCTeam {

	Team t;
	public BukkitMCTeam(Team team) {
		t = team;
	}

	@Override
	public void addEntry(String entry) {
		if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_8_7)){
			t.addEntry(entry);
		} else {
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
	public MCNameTagVisibility getNameTagVisibility() {
		NameTagVisibility ntv = t.getNameTagVisibility();
		return BukkitMCNameTagVisibility.getConvertor().getAbstractedEnum(ntv);
	}

	@Override
	public Set<String> getEntries() {
		Set<String> ret = new HashSet<String>();
		if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_8_7)){
			for (String e : t.getEntries()) {
				ret.add(e);
			}
		} else {
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
		if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_8_7)){
			return t.hasEntry(entry);
		} else {
			OfflinePlayer player = Bukkit.getOfflinePlayer(entry);
			return (boolean) ReflectionUtils.invokeMethod(t, "hasPlayer", player);
		}
	}

	@Override
	public boolean removeEntry(String entry) {
		if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_8_7)){
			return t.removeEntry(entry);
		} else {
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
	public void setNameTagVisibility(MCNameTagVisibility visibility) {
		t.setNameTagVisibility(BukkitMCNameTagVisibility.getConvertor().getConcreteEnum(visibility));
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
