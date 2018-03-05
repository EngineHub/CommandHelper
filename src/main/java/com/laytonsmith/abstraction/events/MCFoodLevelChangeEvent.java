package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCHumanEntity;
import com.laytonsmith.core.events.BindableEvent;

public interface MCFoodLevelChangeEvent extends BindableEvent {

	MCHumanEntity getEntity();

	int getDifference();

	int getFoodLevel();

	void setFoodLevel(int level);

	boolean isCancelled();

	void setCancelled(boolean cancel);
}
