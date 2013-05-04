package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCObjective;
import com.laytonsmith.abstraction.MCOfflinePlayer;
import com.laytonsmith.abstraction.MCScore;
import com.laytonsmith.abstraction.MCScoreboard;
import com.laytonsmith.annotations.WrappedItem;
import org.bukkit.scoreboard.Score;

public class BukkitMCScore implements MCScore {

	@WrappedItem Score s;
	public BukkitMCScore(Score score) {
		s = score;
	}

	public MCObjective getObjective() {
		return new BukkitMCObjective(s.getObjective());
	}

	public MCOfflinePlayer getPlayer() {
		return new BukkitMCOfflinePlayer(s.getPlayer());
	}

	public int getScore() {
		return s.getScore();
	}

	public MCScoreboard getScoreboard() {
		return new BukkitMCScoreboard(s.getScoreboard());
	}

	public void setScore(int score) {
		s.setScore(score);
	}
}
