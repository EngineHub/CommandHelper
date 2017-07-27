package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCObjective;
import com.laytonsmith.abstraction.MCOfflinePlayer;
import com.laytonsmith.abstraction.MCScore;
import com.laytonsmith.abstraction.MCScoreboard;
import org.bukkit.scoreboard.Score;

public class BukkitMCScore implements MCScore {

	Score s;
	public BukkitMCScore(Score score) {
		s = score;
	}

	@Override
	public MCObjective getObjective() {
		return new BukkitMCObjective(s.getObjective());
	}

	@Override
	public int getScore() {
		return s.getScore();
	}

	@Override
	public MCScoreboard getScoreboard() {
		return new BukkitMCScoreboard(s.getScoreboard());
	}

	@Override
	public void setScore(int score) {
		s.setScore(score);
	}
}
