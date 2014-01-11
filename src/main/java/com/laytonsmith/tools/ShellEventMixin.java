
package com.laytonsmith.tools;

import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.events.AbstractEvent;
import com.laytonsmith.core.events.BindableEvent;
import com.laytonsmith.core.events.EventMixinInterface;
import com.laytonsmith.core.exceptions.EventException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class ShellEventMixin implements EventMixinInterface {
	
	AbstractEvent event;
	
	public ShellEventMixin(AbstractEvent e){
		this.event = e;
	}

	@Override
	public void cancel(BindableEvent e, boolean state) {
		//TODO: This needs to be done better
	}

	@Override
	public boolean isCancellable(BindableEvent o) {
		//TODO: This needs to be done better
		return false;
	}

	@Override
	public Map<String, Construct> evaluate_helper(BindableEvent e) throws EventException {
		return new HashMap<String, Construct>();
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
