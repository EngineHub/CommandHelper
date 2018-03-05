package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCHumanEntity;
import com.laytonsmith.abstraction.enums.MCResult;

public interface MCInventoryInteractEvent extends MCInventoryEvent {

	MCHumanEntity getWhoClicked();

	void setResult(MCResult newResult);

	MCResult getResult();

	boolean isCanceled();

	void setCancelled(boolean toCancel);
}
