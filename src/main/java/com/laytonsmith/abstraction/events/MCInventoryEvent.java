package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCHumanEntity;
import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.MCInventoryView;
import com.laytonsmith.core.events.BindableEvent;
import java.util.List;

public interface MCInventoryEvent extends BindableEvent {

	MCInventory getInventory();

	MCInventoryView getView();

	List<MCHumanEntity> getViewers();
}
