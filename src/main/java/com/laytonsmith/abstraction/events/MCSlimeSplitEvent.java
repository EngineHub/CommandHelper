package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.entities.MCSlime;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.events.BindableEvent;

public interface MCSlimeSplitEvent extends BindableEvent {

	CInt getCount();

	MCSlime getEntity();

	boolean isCancelled();

	void setCancelled(boolean cancelled);

	void setCount(int count);

}
