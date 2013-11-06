package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCPlugin;
import com.laytonsmith.core.events.BindableEvent;

/**
 *
 * @author Hekta
 */
public interface MCPluginEvent extends BindableEvent {

	public MCPlugin getPlugin();
}