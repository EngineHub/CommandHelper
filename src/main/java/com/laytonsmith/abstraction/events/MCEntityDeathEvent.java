package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.core.events.BindableEvent;
import java.util.List;

public interface MCEntityDeathEvent extends BindableEvent {
	int getDroppedExp();
	List<MCItemStack> getDrops();
	MCLivingEntity getEntity();
	void setDroppedExp(int exp);
	void clearDrops();
	void addDrop(MCItemStack is);
}
