package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCPlayer;

/**
 *
 */
public interface MCPlayerDeathEvent extends MCEntityDeathEvent {

	public MCPlayer getEntity();

	public String getDeathMessage();

	public void setDeathMessage(String nval);

	public boolean getKeepLevel();
	
	public void setKeepLevel(boolean keepLevel);
	
	public int getNewExp();
	
	public void setNewExp(int exp);
	
	public int getNewLevel();
	
	public void setNewLevel(int level);
	
	public int getNewTotalExp();
	
	public void setNewTotalExp(int totalExp);
	
	public MCEntity getKiller();
}
