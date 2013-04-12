package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.events.BindableEvent;

/**
 *
 * @author Jason Unger <entityreborn@gmail.com>
 */
public interface MCPlayerEvent extends BindableEvent {
	MCPlayer getPlayer();
}
