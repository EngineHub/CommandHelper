
package com.laytonsmith.core.events;

/**
 * Some events may have a custom "cancel" option, even though the underlying event
 * isn't cancellable. If so, it should implement this interface.
 */
public interface CancellableEvent {
	/**
	 * If the state is true, cancels the underlying event.
	 * @param state 
	 */
	void cancel(boolean state);
}
