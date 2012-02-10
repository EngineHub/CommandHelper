/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.events;

import com.laytonsmith.core.Env;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.exceptions.EventException;
import com.laytonsmith.core.exceptions.PrefilterNonMatchException;
import java.util.Map;

/**
 * This interface contains all the methods needed to manipulate a particular
 * event in a particular implementation. Methods that are common to all events
 * in this implementation go in AbstractEventMixin instead.
 * @author layton
 */
public interface EventHandlerInterface {
    /**
     * If a given prefilter matches the given implementation specific event.
     * @param prefilter
     * @param e
     * @return
     * @throws PrefilterNonMatchException 
     */
    public boolean matches(Map<String, Construct> prefilter, Object e) throws PrefilterNonMatchException;
    /**
     * Given the specified event, change the event data into a Map of data (which is then further
     * converted into a CArray later)
     * @param e
     * @param mixin
     * @return
     * @throws EventException 
     */
    public Map<String, Construct> evaluate(Object e, EventMixinInterface mixin) throws EventException;
    /**
     * Converts a CArray into an actual implementation specific event, so it can be
     * manually triggered
     * @param manual
     * @return 
     */
    public Object convert(CArray manual);
    /**
     * Given a key, a value, and an event, this function should modify the implementation
     * event as specified.
     * @param key
     * @param value
     * @param event 
     */
    public boolean modifyEvent(String key, Construct value, Object event);
    
    /**
     * If a custom mixin is needed, this event may specify it here. Null should be
     * returned if a custom mixin isn't needed. A UnsupportedOperationException will
     * also be caught and ignored.
     * @return 
     */
    public EventMixinInterface customMixin(AbstractEvent e);
    
    /**
     * At the last moment, an event is allowed to customize the environment or server
     * if needed. Most events probably won't need this hook, and can safely throw
     * an UnsupportedOperationException.
     * @param env
     * @param activeEvent 
     */
    public void preExecution(Env env, BoundEvent.ActiveEvent activeEvent);
    
    /**
     * Right after the script is run, this function is fired, allowing
     * an event to unhook if hooks had previously been inserted that shouldn't
     * remain.
     * @param env
     * @param activeEvent 
     */
    public void postExecution(Env env, BoundEvent.ActiveEvent activeEvent);
}
