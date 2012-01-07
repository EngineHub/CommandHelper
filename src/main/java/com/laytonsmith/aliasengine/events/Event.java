/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.events;

import com.laytonsmith.aliasengine.Constructs.CArray;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.Script;
import com.laytonsmith.aliasengine.exceptions.EventException;
import com.laytonsmith.aliasengine.exceptions.PrefilterNonMatchException;
import java.util.Map;

/**
 * This interface should be implemented to allow the bind() function to bind to
 * a particular event type. To be recognized as an event type, it should also tag
 * itself with @api, and it will be included in the EventList.
 * @author layton
 */
public interface Event extends Comparable<Event>{
    /**
     * This should return the name of the event.
     * @return 
     */
    public String getName();
    /**
     * This should return the docs that are used in the wiki. The format should
     * be as follows: {prefilter: explanation | ...} Documentation {event_obj: explanation | ...}
     * The explanation for the prefilter may follow certain formatting, which will expand to a link:
     * &lt;string match&gt;<br />
     * &lt;regex&gt;<br />
     * &lt;math match&gt;<br />
     * &lt;expr&gt;
     * @return 
     */
    public String docs();
    /**
     * The version this event was added to CommandHelper. It should follow the 
     * format 0.0.0.
     * @return 
     */
    public String since();
    /**
     * This function should return true if the event code should be run, based
     * on this prefilter and triggering event's parameters.
     */
    public boolean matches(Map<String, Construct> prefilter, Object e) throws PrefilterNonMatchException;
    
    /**
     * If an event is manually triggered, then it may be required for an event
     * object to be faked, so the rest of the event will work properly.
     * @param manualObject
     * @return 
     */
    public Object convert(CArray manualObject);
    
    /**
     * This function is called when an event is triggered. It passes the event, and expects
     * back a Map, which will be converted into a CArray, and passed to the bound event,
     * as the event object. If an EventException is thrown, it is considered a fatal error,
     * and will throw an uncatchable CH exception.
     * @param e
     * @return 
     */
    public Map<String, Construct> evaluate(Object e) throws EventException;
    
    /**
     * This is called to determine if an event is cancellable in the first place
     */
    public boolean isCancellable(Object e);
    
    /**
     * This is called if the script attempts to cancel the event, so the underlying
     * event can also be cancelled. If the underlying event is not cancellable, this
     * should throw an EventException, which is caught in the triggering code, and
     * at this time ignored.
     */
    public void cancel(Object e) throws EventException;
    
    
    /**
     * This function returns the "driver" class of the event needed to trigger it.
     * Though not strictly needed, this method helps optimize code. All events may
     * more strictly filter events based on other conditions, but all events must
     * have a single Type of event that drives the CH event. This is also the type of
     * the event that will be sent to the matches function.
     */
    public Type driver();
    
    /**
     * This function is called once a script binds to this event, which gives 
     * this event type a chance to "activate" if needed. It may throw an 
     * UnsupportedOperationException if it is not needed. The listener
     * is automatically registered, based on the driver returned.
     */
    public void bind();
    
    /**
     * This function is called once when the plugin starts up, to give this
     * event a chance to make a hook into the server if it needs it.
     * It may throw an UnsupportedOperationException if it is not needed.
     */
    public void hook();
    
    /**
     * Because an event type knows best how to actually trigger an event, the prebuild,
     * preconfigured script, and the BoundEvent generating the action are passed to
     * the Event itself. AbstractEvent's default implementation is to simply run the
     * script, but an event can choose to override this functionality if needed.
     */
    public void execute(Script s, BoundEvent b) throws EventException;
    
    /**
     * If it is required to do something extra for server wide events, this can be
     * done here. This is called when the EventHandler is instructed to manually trigger
     * this event server-wide.
     * @param e 
     */
    public void manualTrigger(Object e);
    
    /**
     * If the event is an external event, and there is no reason to attempt a serverwide manual
     * triggering, this function should return false, in which case the serverWide variable
     * is ignored, and it is only piped through CH specific handlers.
     * @return 
     */
    public boolean supportsExternal();
    
    /**
     * Called when a script wishes to modify an event specific parameter, this function
     * takes a key, a construct, and the underlying event.
     */
    public void modifyEvent(String key, Construct value, Object event);
    
}
