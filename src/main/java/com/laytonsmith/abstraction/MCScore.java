package com.laytonsmith.abstraction;

public interface MCScore {
	MCObjective getObjective();
	int getScore();
	MCScoreboard getScoreboard();
	void setScore(int score);
}
