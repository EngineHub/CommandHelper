package com.laytonsmith.abstraction.bukkit;

import com.methodscript.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.abstraction.MCScoreboard;
import com.laytonsmith.abstraction.MCTeam;
import com.laytonsmith.abstraction.enums.MCNameTagVisibility;
import com.laytonsmith.abstraction.enums.MCOption;
import com.laytonsmith.abstraction.enums.MCOptionStatus;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCOption;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCOptionStatus;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.OptionStatus;

import java.util.HashSet;
import java.util.Set;

public class BukkitMCTeam implements MCTeam {

	Team t;

	public BukkitMCTeam(Team team) {
		t = team;
	}

	@Override
	public void addEntry(String entry) {
		try {
			t.addEntry(entry);
		} catch (NoSuchMethodError ex) {
			// Probably 1.8.5 or prior
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
		// deprecated in 1.9
		NameTagVisibility ntv = t.getNameTagVisibility();
		return MCNameTagVisibility.valueOf(ntv.name());
	}

	@Override
	public MCOptionStatus getOption(MCOption option) {
		OptionStatus os = t.getOption(BukkitMCOption.getConvertor().getConcreteEnum(option));
		return MCOptionStatus.valueOf(os.name());
	}

	@Override
	public Set<String> getEntries() {
		Set<String> ret = new HashSet<>();
		try {
			for(String e : t.getEntries()) {
				ret.add(e);
			}
		} catch (NoSuchMethodError ex) {
			// Probably 1.8.5 or prior
			for(OfflinePlayer o : (Set<OfflinePlayer>) ReflectionUtils.invokeMethod(t, "getPlayers")) {
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
		try {
			return t.hasEntry(entry);
		} catch (NoSuchMethodError ex) {
			// Probably 1.8.5 or prior
			OfflinePlayer player = Bukkit.getOfflinePlayer(entry);
			return (boolean) ReflectionUtils.invokeMethod(t, "hasPlayer", player);
		}
	}

	@Override
	public boolean removeEntry(String entry) {
		try {
			return t.removeEntry(entry);
		} catch (NoSuchMethodError ex) {
			// Probably 1.8.5 or prior
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
		t.setNameTagVisibility(NameTagVisibility.valueOf(visibility.name()));
	}

	@Override
	public void setOption(MCOption option, MCOptionStatus status) {
		t.setOption(BukkitMCOption.getConvertor().getConcreteEnum(option), BukkitMCOptionStatus.getConvertor().getConcreteEnum(status));
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
