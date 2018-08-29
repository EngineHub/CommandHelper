package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCScoreboard;
import com.laytonsmith.abstraction.MCTeam;
import com.laytonsmith.abstraction.enums.MCOption;
import com.laytonsmith.abstraction.enums.MCOptionStatus;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCOption;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCOptionStatus;
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
		t.addEntry(entry);
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
	public MCOptionStatus getOption(MCOption option) {
		OptionStatus os = t.getOption(BukkitMCOption.getConvertor().getConcreteEnum(option));
		return MCOptionStatus.valueOf(os.name());
	}

	@Override
	public Set<String> getEntries() {
		Set<String> ret = new HashSet<>();
		ret.addAll(t.getEntries());
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
		return t.hasEntry(entry);
	}

	@Override
	public boolean removeEntry(String entry) {
		return t.removeEntry(entry);
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
