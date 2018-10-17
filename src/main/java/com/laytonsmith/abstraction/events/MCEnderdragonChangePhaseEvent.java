package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.events.BindableEvent;
import org.bukkit.entity.EnderDragon;

public interface MCEnderdragonChangePhaseEvent extends BindableEvent {

	CString getCurrentPhase();

	MCEntity getEntity();

	CString getNewPhase();

	boolean iscancelled();

	void setCancelled(boolean cancelled);

	void setNewPhase(EnderDragon.Phase newPhase);
}
