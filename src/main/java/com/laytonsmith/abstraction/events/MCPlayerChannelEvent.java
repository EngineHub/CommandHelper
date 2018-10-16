package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.events.BindableEvent;

public interface MCPlayerChannelEvent extends BindableEvent {

    public CString getChannel();

    public CString getType();

    public MCPlayer getPlayer();

}
