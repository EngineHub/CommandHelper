package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCObjective;
import com.laytonsmith.abstraction.MCScore;
import com.laytonsmith.abstraction.MCScoreboard;
import com.laytonsmith.abstraction.MCTeam;
import com.laytonsmith.abstraction.enums.MCDisplaySlot;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCDisplaySlot;
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
		if(o == null) {
			return null;
		}
		return new BukkitMCObjective(o);
	}

	@Override
	public MCObjective getObjective(String name) {
		Objective o = s.getObjective(name);
		if(o == null) {
			return null;
		}
		return new BukkitMCObjective(o);
	}

	@Override
	public Set<MCObjective> getObjectives() {
		Set<MCObjective> ret = new HashSet<>();
		for(Objective o : s.getObjectives()) {
			ret.add(new BukkitMCObjective(o));
		}
		return ret;
	}

	@Override
	public Set<MCObjective> getObjectivesByCriteria(String criteria) {
		Set<MCObjective> ret = new HashSet<>();
		// deprecated 1.19.2
		for(Objective o : s.getObjectivesByCriteria(criteria)) {
			ret.add(new BukkitMCObjective(o));
		}
		return ret;
	}

	@Override
	public Set<String> getEntries() {
		return s.getEntries();
	}

	@Override
	public MCTeam getPlayerTeam(String entry) {
		Team t = s.getEntryTeam(entry);
		if(t == null) {
			return null;
		}
		return new BukkitMCTeam(t);
	}

	@Override
	public Set<MCScore> getScores(String entry) {
		Set<MCScore> ret = new HashSet<>();
		for(Score o : s.getScores(entry)) {
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
		Set<MCTeam> ret = new HashSet<>();
		for(Team t : s.getTeams()) {
			ret.add(new BukkitMCTeam(t));
		}
		return ret;
	}

	@Override
	public MCObjective registerNewObjective(String name, String criteria) {
		// deprecated 1.19.2
		return new BukkitMCObjective(s.registerNewObjective(name, criteria));
	}

	@Override
	public MCTeam registerNewTeam(String name) {
		return new BukkitMCTeam(s.registerNewTeam(name));
	}

	@Override
	public void resetScores(String entry) {
		s.resetScores(entry);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof MCScoreboard && s.equals(((BukkitMCScoreboard) obj).s);
	}

	@Override
	public int hashCode() {
		return s.hashCode();
	}
}
