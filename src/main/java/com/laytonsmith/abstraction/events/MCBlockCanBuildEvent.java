package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCBlockData;
import com.laytonsmith.core.events.BindableEvent;

public interface MCBlockCanBuildEvent extends BindableEvent {

    public MCBlock getBlock();
    public boolean isBuildable();
    public void setBuildable(boolean cancel);

}
