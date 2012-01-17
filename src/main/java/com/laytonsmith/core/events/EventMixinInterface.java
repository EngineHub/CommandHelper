/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.events;

import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.exceptions.EventException;
import java.util.Map;

/**
 * The abstract event mixin contains functions that are common to all
 * event types in a particular implementation.
 * @author layton
 */
public interface EventMixinInterface {
    /**
     * Cancel this event, if possible.
     * @param e 
     */
    public void cancel(Object e);
    /**
     * Return if this event is cancellable
     * @param o
     * @return 
     */
    public boolean isCancellable(Object o);    
    public Map<String, Construct> evaluate_helper(Object e) throws EventException;
    /**
     * Manually trigger this implementation specific event
     * @param e 
     */
    public void manualTrigger(Object e);
}
