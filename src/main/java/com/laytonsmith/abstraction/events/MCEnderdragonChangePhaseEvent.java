package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.events.BindableEvent;
import org.bukkit.entity.EnderDragon;

public interface MCEnderdragonChangePhaseEvent extends BindableEvent {

    public CString getCurrentPhase();

    public MCEntity getEntity();

    public CString getNewPhase();

    public boolean iscancelled();

    public void setCancelled(boolean cancelled);

    public void setNewPhase(EnderDragon.Phase newPhase);
}
