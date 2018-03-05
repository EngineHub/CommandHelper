package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.enums.MCDisplaySlot;
import java.util.Set;

public interface MCScoreboard {

	void clearSlot(MCDisplaySlot slot);

	MCObjective getObjective(MCDisplaySlot slot);

	MCObjective getObjective(String name);

	/**
	 *
	 * @return Set of all objectives on this scoreboard
	 */
	Set<MCObjective> getObjectives();

	Set<MCObjective> getObjectivesByCriteria(String criteria);

	/**
	 *
	 * @return Set of all players tracked by this scoreboard
	 */
	Set<String> getEntries();

	MCTeam getPlayerTeam(String entry);

	Set<MCScore> getScores(String entry);

	MCTeam getTeam(String teamName);

	Set<MCTeam> getTeams();

	MCObjective registerNewObjective(String name, String criteria);

	MCTeam registerNewTeam(String name);

	void resetScores(String entry);
}
