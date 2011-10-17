/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.events;

import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.GenericTreeNode;
import java.util.List;
import java.util.Map;

/**
 * This interface should be implemented to allow the bind() function to bind to
 * a particular event type. To be recognized as an event type, it should also tag
 * itself with @api, and it will be included in the EventList.
 * @author layton
 */
public interface Event {
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
     * on this prefilter and triggering event.
     */
    public boolean matches(Map<String, String> prefilter, org.bukkit.event.Event e);
    
    /**
     * This function returns the "driver" class of the event needed to trigger it.
     * Though not strictly needed, this method helps optimize code. All events may
     * more strictly filter events based on other conditions, but all events must
     * have a single Bukkit event that drives the CH event. This is also the type of
     * the event that will be sent to the matches function.
     */
    public Class driver();
    
    /**
     * This function is called once a script binds to this event, which gives 
     * this event type a chance to "active" if needed. It may throw an 
     * UnsupportedOperationException if it is not needed.
     */
    public void bind();
    
    /**
     * This function is called once when the plugin starts up, to give this
     * event a chance to make any extra hooks into the server if it needs it.
     * It may throw an UnsupportedOperationException if it is not needed.
     */
    public void hook();
    
}
