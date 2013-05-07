package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.AbstractionUtils;
import com.laytonsmith.abstraction.MCObjective;
import com.laytonsmith.abstraction.MCOfflinePlayer;
import com.laytonsmith.abstraction.MCScore;
import com.laytonsmith.abstraction.MCScoreboard;
import com.laytonsmith.annotations.WrappedItem;
import org.bukkit.scoreboard.Score;

public class BukkitMCScore implements MCScore {

	@WrappedItem Score s;

	public MCObjective getObjective() {
		return AbstractionUtils.wrap(s.getObjective());
	}

	public MCOfflinePlayer getPlayer() {
		return AbstractionUtils.wrap(s.getPlayer());
	}

	public int getScore() {
		return s.getScore();
	}

	public MCScoreboard getScoreboard() {
		return AbstractionUtils.wrap(s.getScoreboard());
	}

	public void setScore(int score) {
		s.setScore(score);
	}

	public <T> T getHandle() {
		return (T) s;
	}
}
