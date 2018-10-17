package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.core.events.BindableEvent;

public interface MCBlockCanBuildEvent extends BindableEvent {

	MCBlock getBlock();

	boolean isBuildable();

	void setBuildable(boolean cancel);

}
