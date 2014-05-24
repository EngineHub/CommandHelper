package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.enums.MCDisplaySlot;
import java.util.Set;

public interface MCScoreboard {
	public void clearSlot(MCDisplaySlot slot);
	public MCObjective getObjective(MCDisplaySlot slot);
	public MCObjective getObjective(String name);
	/**
	 * 
	 * @return Set of all objectives on this scoreboard
	 */
	public Set<MCObjective> getObjectives();
	public Set<MCObjective> getObjectivesByCriteria(String criteria);
	/**
	 * 
	 * @return Set of all players tracked by this scoreboard
	 */
	public Set<MCOfflinePlayer> getPlayers();
	public MCTeam getPlayerTeam(MCOfflinePlayer player);
	public Set<MCScore> getScores(MCOfflinePlayer player);
	public MCTeam getTeam(String teamName);
	public Set<MCTeam> getTeams();
	public MCObjective registerNewObjective(String name, String criteria);
	public MCTeam registerNewTeam(String name);
	public void resetScores(MCOfflinePlayer player);
}
