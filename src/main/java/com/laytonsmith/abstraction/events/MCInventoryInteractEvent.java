package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.entities.MCHumanEntity;
import com.laytonsmith.abstraction.enums.MCResult;

/**
 *
 * @author import
 */
public interface MCInventoryInteractEvent extends MCInventoryEvent {
	public MCHumanEntity getWhoClicked();
	public void setResult(MCResult newResult);
	public MCResult getResult();
	public boolean isCanceled();
	public void setCancelled(boolean toCancel);
}
