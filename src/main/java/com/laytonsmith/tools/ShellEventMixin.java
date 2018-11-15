package com.laytonsmith.tools;

import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.events.AbstractEvent;
import com.laytonsmith.core.events.BindableEvent;
import com.laytonsmith.core.events.CancellableEvent;
import com.laytonsmith.core.events.EventMixinInterface;
import com.laytonsmith.core.exceptions.EventException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class ShellEventMixin implements EventMixinInterface {

	AbstractEvent event;

	public ShellEventMixin(AbstractEvent e) {
		this.event = e;
	}

	@Override
	public void cancel(BindableEvent e, boolean state) {
		if(e instanceof CancellableEvent) {
			((CancellableEvent) e).cancel(state);
		}
	}

	@Override
	public boolean isCancellable(BindableEvent o) {
		return o instanceof CancellableEvent;
	}

	@Override
	public Map<String, Mixed> evaluate_helper(BindableEvent e) throws EventException {
		return new HashMap<>();
	}

	@Override
	public void manualTrigger(BindableEvent e) {
		throw new UnsupportedOperationException("TODO: Not supported yet.");
	}

	@Override
	public boolean isCancelled(BindableEvent o) {
		return false;
	}

}
