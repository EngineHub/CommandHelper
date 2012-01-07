/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.events;

import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.exceptions.EventException;
import java.util.Map;

/**
 * The abstract event mixin contains functions that are common to all
 * event types in a particular implementation.
 * @author layton
 */
public interface AbstractEventMixin {
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
