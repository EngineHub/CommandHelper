package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.abstraction.MCObjective;
import com.laytonsmith.abstraction.MCOfflinePlayer;
import com.laytonsmith.abstraction.MCScore;
import com.laytonsmith.abstraction.MCScoreboard;
import com.laytonsmith.abstraction.MCTeam;
import com.laytonsmith.abstraction.enums.MCDisplaySlot;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCDisplaySlot;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashSet;
import java.util.Set;

public class BukkitMCScoreboard implements MCScoreboard {

	Scoreboard s;
	public BukkitMCScoreboard(Scoreboard sb) {
		s = sb;
	}

	public Scoreboard _scoreboard() {
		return s;
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
	public Set<String> getEntries() {
		if(ReflectionUtils.hasMethod(s.getClass(), "getEntries", null)){
			// This is the newer method, just call the method and return it.
			return (Set<String>) ReflectionUtils.invokeMethod(s, "getEntries");
		} else {
			// Old style, where we have to build it from the list of players
			Set<String> ret = new HashSet<>();
			for (OfflinePlayer o : s.getPlayers()) {
				// Deprecated usage, but required.
				ret.add(o.getName());
			}
			return ret;
		}
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
	public Set<MCScore> getScores(String entry) {
		Set<MCScore> ret = new HashSet<>();
		if(ReflectionUtils.hasMethod(s.getClass(), "getScores", null, OfflinePlayer.class)){
			// Old style, we have to build the list of offline players ourselves
			OfflinePlayer player = Bukkit.getOfflinePlayer(entry);
			for (Score o : (Set<Score>) ReflectionUtils.invokeMethod(s, "getScores", player)) {
				ret.add(new BukkitMCScore(o));
			}
		} else {
			// New style
			for (Score o : (Set<Score>) ReflectionUtils.invokeMethod(s, "getScores", entry)) {
				ret.add(new BukkitMCScore(o));
			}
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
		Set<MCTeam> ret = new HashSet<>();
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
	public void resetScores(String entry) {
		if(ReflectionUtils.hasMethod(s.getClass(), "resetScores", null, String.class)){
			// New style
			ReflectionUtils.invokeMethod(s, "resetScores", entry);
		} else {
			OfflinePlayer player = Bukkit.getOfflinePlayer(entry);
			ReflectionUtils.invokeMethod(s, "resetScores", player);
		}
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
