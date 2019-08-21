package com.laytonsmith.core.events;

import com.laytonsmith.core.exceptions.EventException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.Map;

/**
 * The abstract event mixin contains functions that are common to all or most event types in a particular
 * implementation. Concrete classes are also required to implement a constructor with the signature "public
 * ClassName(AbstractEvent mySuper)", because the class is reflectively constructed.
 *
 */
public interface EventMixinInterface {

	/**
	 * Cancel this event, if possible.
	 *
	 * @param e
	 */
	public void cancel(BindableEvent e, boolean state);

	/**
	 * Return if this event is cancellable
	 *
	 * @param o
	 * @return
	 */
	public boolean isCancellable(BindableEvent o);

	/**
	 * This constructs the common elements in an event.
	 *
	 * @param e
	 * @return
	 * @throws EventException
	 */
	public Map<String, Mixed> evaluate_helper(BindableEvent e) throws EventException;

	/**
	 * Manually trigger this implementation specific event
	 *
	 * @param e
	 */
	public void manualTrigger(BindableEvent e);

	/**
	 * Is this event cancelled?
	 *
	 * @param o
	 * @return
	 */
	public boolean isCancelled(BindableEvent o);
}
