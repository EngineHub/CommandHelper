package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.entities.MCLivingEntity;
import com.laytonsmith.core.events.BindableEvent;
import java.util.List;

/**
 * 
 * @author jb_aero
 */
public interface MCEntityDeathEvent extends BindableEvent {

	public int getDroppedExp();
	public List<MCItemStack> getDrops();
	public MCLivingEntity getEntity();
	public void setDroppedExp(int exp);
	public void clearDrops();
	public void addDrop(MCItemStack is);
}
