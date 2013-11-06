package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.entities.MCHumanEntity;
import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.MCInventoryView;
import com.laytonsmith.core.events.BindableEvent;
import java.util.List;

/**
 *
 * @author import
 */
public interface MCInventoryEvent extends BindableEvent {
	public MCInventory getInventory();
	public MCInventoryView getView();
	public List<MCHumanEntity> getViewers();
}
