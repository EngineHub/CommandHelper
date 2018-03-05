package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCPlayer;

public interface MCPlayerDeathEvent extends MCEntityDeathEvent {

	@Override
	MCPlayer getEntity();

	String getDeathMessage();

	void setDeathMessage(String nval);

	boolean getKeepLevel();

	void setKeepLevel(boolean keepLevel);

	boolean getKeepInventory();

	void setKeepInventory(boolean keepLevel);

	int getNewExp();

	void setNewExp(int exp);

	int getNewLevel();

	void setNewLevel(int level);

	int getNewTotalExp();

	void setNewTotalExp(int totalExp);

	MCEntity getKiller();
}
