package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCObjective;
import com.laytonsmith.abstraction.MCOfflinePlayer;
import com.laytonsmith.abstraction.MCScore;
import com.laytonsmith.abstraction.MCScoreboard;
import com.laytonsmith.abstraction.MCTeam;
import com.laytonsmith.abstraction.enums.MCDisplaySlot;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCDisplaySlot;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.bukkit.OfflinePlayer;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class BukkitMCScoreboard implements MCScoreboard {

	Scoreboard s;
	public BukkitMCScoreboard(Scoreboard sb) {
		s = sb;
	}

	@Override
	public void clearSlot(MCDisplaySlot slot) {
		s.clearSlot(BukkitMCDisplaySlot.getConvertor().getConcreteEnum(slot));
	}

	@Override
	public MCObjective getObjective(MCDisplaySlot slot) {
		Objective o = s.getObjective(BukkitMCDisplaySlot.getConvertor().getConcreteEnum(slot));
		if (o == null) {
			return null;
		}
		return new BukkitMCObjective(o);
	}

	@Override
	public MCObjective getObjective(String name) {
		Objective o = s.getObjective(name);
		if (o == null) {
			return null;
		}
		return new BukkitMCObjective(o);
	}

	@Override
	public Set<MCObjective> getObjectives() {
		Set<MCObjective> ret = new HashSet<MCObjective>();
		for (Objective o : s.getObjectives()) {
			ret.add(new BukkitMCObjective(o));
		}
		return ret;
	}

	@Override
	public Set<MCObjective> getObjectivesByCriteria(String criteria) {
		Set<MCObjective> ret = new HashSet<MCObjective>();
		for (Objective o : s.getObjectivesByCriteria(criteria)) {
			ret.add(new BukkitMCObjective(o));
		}
		return ret;
	}

	@Override
	public Set<MCOfflinePlayer> getPlayers() {
		Set<MCOfflinePlayer> ret = new HashSet<MCOfflinePlayer>();
		for (OfflinePlayer o : s.getPlayers()) {
			ret.add(new BukkitMCOfflinePlayer(o));
		}
		return ret;
	}

	@Override
	public MCTeam getPlayerTeam(MCOfflinePlayer player) {
		Team t = s.getPlayerTeam((OfflinePlayer) player.getHandle());
		if(t == null) {
			return null;
		}
		return new BukkitMCTeam(t);
	}

	@Override
	public Set<MCScore> getScores(MCOfflinePlayer player) {
		Set<MCScore> ret = new HashSet<MCScore>();
		for (Score o : s.getScores((OfflinePlayer) player.getHandle())) {
			ret.add(new BukkitMCScore(o));
		}
		return ret;
	}

	@Override
	public MCTeam getTeam(String teamName) {
		Team t = s.getTeam(teamName);
		if(t == null) {
			return null;
		}
		return new BukkitMCTeam(t);
	}

	@Override
	public Set<MCTeam> getTeams() {
		Set<MCTeam> ret = new HashSet<MCTeam>();
		for (Team t : s.getTeams()) {
			ret.add(new BukkitMCTeam(t));
		}
		return ret;
	}

	@Override
	public MCObjective registerNewObjective(String name, String criteria) {
		return new BukkitMCObjective(s.registerNewObjective(name, criteria));
	}

	@Override
	public MCTeam registerNewTeam(String name) {
		return new BukkitMCTeam(s.registerNewTeam(name));
	}

	@Override
	public void resetScores(MCOfflinePlayer player) {
		s.resetScores((OfflinePlayer) player.getHandle());
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MCScoreboard) {
			return s.equals(((BukkitMCScoreboard) obj).s);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return s.hashCode();
	}
}
