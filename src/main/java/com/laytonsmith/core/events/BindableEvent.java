package com.laytonsmith.core.events;

/**
 *
 * @author layton
 */
public interface BindableEvent {
    /**
     * This returns the underlying event object, as needed by the abstraction layer implementation 
     * when dealing with events generically. If this event represents a non-minecraft
     * event, and nothing is wrapped, then this should return null.
     * @return 
     */
    public Object _GetObject();
    
}
